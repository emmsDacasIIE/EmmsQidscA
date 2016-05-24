package cn.dacas.emmclient.mdm;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import cn.dacas.emmclient.db.EmmClientDb;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.forward.IForward;
import cn.dacas.emmclient.main.ActivateDevice;
import cn.dacas.emmclient.main.CheckAccount;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.msgpush.MsgPushListener;
import cn.dacas.emmclient.msgpush.RegMsgPush;
import cn.dacas.emmclient.ui.DeviceMessageActivity;
import cn.dacas.emmclient.ui.NewMainActivity.PlaceholderFragment;
import cn.dacas.emmclient.util.BroadCastDef;
import cn.dacas.emmclient.util.MyJsonArrayRequest;
import cn.dacas.emmclient.util.MyJsonObjectRequest;
import cn.dacas.emmclient.util.NetworkDef;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.UpdateTokenRequest;
import cn.dacas.emmclient.worker.DeviceAdminWorker;
import cn.dacas.emmclient.worker.DownLoadAppFromUrl;
import cn.dacas.emmclient.worker.DownLoadFileFromUrl;
import cn.dacas.emmclient.worker.MApplicationManager;
import cn.dacas.emmclient.worker.PhoneInfoExtractor;
import cn.dacas.emmclient.worker.PhoneInfoExtractor.WifiNetworkInfo;
import de.greenrobot.event.EventBus;

public class MDMService extends Service {
    private Context context;
    private ActivateDevice activate;

    private static DeviceAdminWorker mDeviceAdminWorker;
    private static PhoneInfoExtractor mPhoneInfoExtractor;
    private static MApplicationManager mApplicationManager;

    private LocationClient mLocationClient;
    private MyLocationListener mMyLocationListener;
    private BDLocation curLocation;
    private IForward forward;

    private String email;

    private String owner;

    private static Map<String, String> appNameUrl;
    private static Map<String, String> fileNameUrl;

    private MsgListener listener;

    private static final int PUSH_MSG = 3;
    private static final int ERASE_CORP = 4;
    private static final int ERROR_FORMAT = 5;
    private static final int ERASE_ALL = 6;
    private static final int FACTORY = 7;
    private static final int ERROR_MSG_SERVER = 8;
    private static final int PUSH_APP = 9;
    private static final int PUSH_FILE = 10;
    private static final int REFRESH_ALL = 11;
    private static final int DEVICE_TYPE = 12;

    private static final int AUTH_STATE_CHANGE = 1010;// 授权状态改变
    private static final int OP_CAMERA = 1030;
    private static final int OP_PUSH_MSG = 1055;
    private static final int OP_WIFI_CONFIG = 1020;
    private static final int OP_SET_MUTE = 1025;
    private static final int OP_LOCK_KEY = 1035;
    private static final int OP_LOCK = 1036; // 锁屏
    private static final int OP_REFRESH = 1045;
    private static final int OP_ERASE_CORP = 1050;
    private static final int OP_ERASE_ALL = 1060;
    private static final int OP_FACTORY = 1065;
    private static final int OP_POLICY1 = 1070;
    private static final int OP_POLICY2 = 1075;
    private static final int OP_PUSH_FILE = 1080; // fileUrlList以,分割
    private static final int OP_CHANGE_DEVICE_TYPE = 1015; // 设备类型改变
    private static final int DELETE_DEVICE = 1090; // 服务器端删除设备
    private static final int NEW_COMMANDS = 1111;// 新指令

    public static final String PREF_NAME = "APP_CAPA";
    public static final String DEVICE = "deviceType";

    // 强制实施选项
    private static final int ENFORCE = 1;
    // 提示用户选项
    private static final int WARN = 2;

    private static boolean isTransactionOpen = false;

    public void onEventBackgroundThread(MessageEvent event) {
        switch (event.type) {
            case MessageEvent.Event_StartTransaction:
                String owner = event.params.getString("owner");
                startTransaction(owner);
                break;
            case MessageEvent.Event_StartForwarding:
                String email = event.params.getString("email");
                startForwarding(email);
                break;
            case MessageEvent.Event_StopForwarding:
                stopForwarding();
            case MessageEvent.Event_UploadLocation:
                uploadLocation();
                break;
            default:
                break;
        }
    }

    private void uploadLocation() {
        if (needLoaction()) {
            mLocationClient.start();
            mLocationClient.requestLocation();
        } else {
            mLocationClient.stop();
        }
    }

    private void startTransaction(final String owner) {
        Log.d("MDMService", "start transaction");
        updateDeviceInfo();
        uploadLocation();
        MDMService.this.owner = owner;
        String ip = NetworkDef.getAvailableMsgPushIp();
        RegMsgPush sub = new RegMsgPush();
        Log.d("MDMService", "MsgPush reg to " + ip);
        sub.init(mPhoneInfoExtractor.getIMEI(), listener,
                new String[]{ip});
        // 1）每次注册成功，都向服务器询问当前策略
        // 2）在运行期间的策略更新依赖于“消息通知”
        ((EmmClientApplication) MDMService.this.getApplicationContext())
                .getPolicyManager().updatePolicy();
        mDeviceAdminWorker.startScanPolicy();
    }


    private void stopForwarding() {
        // TODO Auto-generated method stub
        email = null;
        forward.stopMapping();
    }

    private void startForwarding(final String email) {
        // TODO Auto-generated method stub
        MDMService.this.email = email;
        MDMService.this.email = email;
        MyJsonArrayRequest request = new MyJsonArrayRequest(Request.Method.GET, "/user/apps?platforms=ANDROID", UpdateTokenRequest.TokenType.USER,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray array) {
                        HashMap<Integer, String> map = new HashMap<>();
                        try {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject json = (JSONObject) array.get(i);
                                String accessStr = json.getString("secureAccesses");
                                JSONArray accessArr = new JSONArray(accessStr);
                                for (int idx = 0; idx < accessArr.length(); idx++) {
                                    JSONObject obj = (JSONObject) accessArr.get(idx);
                                    String localPort = (String) obj.get("localPort");
                                    String remoteIp = (String) obj.get("remoteIp");
                                    String remotePort = (String) obj.get("remotePort");
                                    map.put(Integer.parseInt(localPort), remoteIp + ":"
                                            + remotePort);
                                }
                            }
                            forward.addMapping(map);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
        EmmClientApplication.mVolleyQueue.add(request);
    }

    private boolean needLoaction() {
        String account = CheckAccount.getCheckAccountInstance(context)
                .getCurrentAccount();
        if (account == null || account.equals(""))
            return false;
        String device_type = EmmClientApplication.mActivateDevice.getDeviceType();
        if (device_type == null || device_type.equals(""))
            return false;
        if (device_type.equals("BYOD") || device_type.equals("UNKNOWN")) {
            SharedPreferences settings = context.getSharedPreferences(
                    PREF_NAME, 0);
            if (settings.getBoolean("allowLocationInfo", false) == false)
                return false;
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        mDeviceAdminWorker = DeviceAdminWorker.getDeviceAdminWorker(context);
        mApplicationManager = MApplicationManager.getMyActivityManager(context);
        mPhoneInfoExtractor = PhoneInfoExtractor.getPhoneInfoExtractor(context);

        activate = ((EmmClientApplication) MDMService.this
                .getApplicationContext()).getActivateDevice();

        listener = new MsgListener();
        listener.setContext(context);
        listener.setHandler(uiHandler);

        mLocationClient = new LocationClient(
                MDMService.this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(60 * 1000);// 设置发起定位请求的间隔时间为1分钟
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向

        mLocationClient.setLocOption(option);
        forward = new IForward();
        EventBus.getDefault().register(this);

        // deal with app running while service down
        String owner = EmmClientApplication.mActivateDevice.getDeviceBinder();
        if (this.owner == null && owner != null) {
            startTransaction(owner);
        }
    }

    @Override
    public int onStartCommand(Intent it, int flags, int startId) {
        super.onStartCommand(it, flags, startId);
        return START_STICKY;
    }

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int code = msg.arg1;
            Bundle bd = msg.getData();

            AlertDialog.Builder builder = null;
            AlertDialog alertDialog = null;

            switch (code) {
                case PUSH_MSG:
                    String content = null;
                    if (bd != null) {
                        content = (String) bd.getCharSequence("msg");
                    }

                    if (content != null) {
                        // 创建一个NotificationManager的引用
                        NotificationManager notificationManager = (NotificationManager) context
                                .getSystemService(android.content.Context.NOTIFICATION_SERVICE);
                        Intent notificationIntent = new Intent(
                                context.getApplicationContext(),
                                DeviceMessageActivity.class); // 点击该通知后要跳转的Activity
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        notificationIntent.putExtra("FromMsg", true);

                        SharedPreferences unreadMsgCount = context
                                .getSharedPreferences(PrefUtils.MSG_COUNT, 0);
                        int unreadCount = unreadMsgCount.getInt("unread_count", 0);
                        if (unreadCount > 1)
                            content = "您有" + String.valueOf(unreadCount) + "条未读消息。";

                        PendingIntent contentItent = PendingIntent.getActivity(
                                context.getApplicationContext(), 0,
                                notificationIntent, 0);
                        Notification.Builder notificationBuilder = new Notification.Builder(
                                context);
                        notificationBuilder
                                .setSmallIcon(android.R.drawable.sym_action_chat)
                                .setTicker("企业消息")
                                .setWhen(System.currentTimeMillis())
                                .setDefaults(
                                        Notification.DEFAULT_VIBRATE
                                                | Notification.DEFAULT_SOUND)
                                .setContentTitle("企业消息").setContentText(content)
                                .setContentIntent(contentItent);

                        Notification notification = notificationBuilder.build();
                        notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
                        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                        notification.flags |= Notification.FLAG_AUTO_CANCEL;
                        notificationManager.notify(0, notification);
                    }
                    break;
                case FACTORY:
                    if (msg.arg2 == ENFORCE) {
                        mDeviceAdminWorker.wipeData(false);
                    } else {
                        builder = new AlertDialog.Builder(context);
                        builder.setTitle("恢复出厂设置");
                        builder.setMessage("是否确定执行？");

                        builder.setPositiveButton("确定", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                new Thread(new Runnable() {
                                    public void run() {
                                        mDeviceAdminWorker.wipeData(false);
                                    }
                                }).start();
                                dialog.cancel();
                            }
                        });

                        builder.setNegativeButton("取消", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                dialog.cancel();
                            }
                        });

                        alertDialog = builder.create();
                        alertDialog.getWindow().setType(
                                (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                        alertDialog.show();
                    }
                    break;
                case ERASE_ALL:
                    if (msg.arg2 == ENFORCE) {
                        mDeviceAdminWorker.wipeData(true);
                    } else {
                        builder = new AlertDialog.Builder(context);
                        builder.setTitle("擦除设备所有数据");
                        builder.setMessage("包括恢复出厂和sd卡擦除，是否确定执行？");

                        builder.setPositiveButton("确定", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                new Thread(new Runnable() {
                                    public void run() {
                                        mDeviceAdminWorker.wipeData(true);
                                    }
                                }).start();
                                dialog.cancel();
                            }
                        });

                        builder.setNegativeButton("取消", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                dialog.cancel();
                            }
                        });

                        alertDialog = builder.create();
                        alertDialog.getWindow().setType(
                                (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                        alertDialog.show();
                    }

                    break;
                case ERASE_CORP:
                    if (msg.arg2 == ENFORCE) {
                        new Thread(new Runnable() {
                            public void run() {
                                EmmClientApplication.mDb.clearCorpData();
                            }
                        }).start();
                    } else {
                        builder = new AlertDialog.Builder(context);
                        builder.setTitle("擦除企业数据");
                        builder.setMessage("是否确定执行？");

                        builder.setPositiveButton("确定", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                new Thread(new Runnable() {
                                    public void run() {
                                        EmmClientApplication.mDb.clearCorpData();
                                    }
                                }).start();
                                dialog.cancel();
                            }
                        });

                        builder.setNegativeButton("取消", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                dialog.cancel();
                            }
                        });

                        alertDialog = builder.create();
                        alertDialog.getWindow().setType(
                                (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                        alertDialog.show();
                    }
                    break;
                case ERROR_FORMAT:
                    builder = new AlertDialog.Builder(context);
                    builder.setTitle("格式错误");
                    builder.setMessage("来自消息推送服务器的消息格式不合法！");

                    builder.setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            dialog.cancel();
                        }
                    });

                    alertDialog = builder.create();
                    alertDialog.getWindow().setType(
                            (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    alertDialog.show();
                    break;
                case ERROR_MSG_SERVER:
                    builder = new AlertDialog.Builder(context);
                    builder.setTitle("服务器错误");
                    builder.setMessage("无法连接消息推送服务器!");

                    builder.setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            dialog.cancel();
                        }
                    });

                    alertDialog = builder.create();
                    alertDialog.getWindow().setType(
                            (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    alertDialog.show();
                    break;
                case PUSH_APP:
                    DownLoadAppFromUrl.startDownloadAppList(
                            context.getApplicationContext(), appNameUrl);
                    break;
                case PUSH_FILE:
                    DownLoadFileFromUrl.startDownloadFileList(
                            context.getApplicationContext(), fileNameUrl);
                    break;
                case REFRESH_ALL:
                    updateDeviceInfo();
                    break;
                case AUTH_STATE_CHANGE:
                    DisableTask dt1 = new DisableTask(MDMService.this);
                    dt1.execute(DisableTask.AUTH_STATE_CHANGE);
                    break;
                case DELETE_DEVICE:
                    DisableTask dt2 = new DisableTask(MDMService.this);
                    dt2.execute(DisableTask.DELETE_DEVICE);
                    break;
                case DEVICE_TYPE:
                    downloadDeviceInfo();

                default:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.unRegisterLocationListener(mMyLocationListener);
        mDeviceAdminWorker.stopScanPolicy();
        EventBus.getDefault().unregister(this);
        sendBroadcast(new Intent("cn.dacas.intent.action.SERVICE_RESTART"));
    }

    protected void exitProgram() {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(getPackageName());
        System.exit(0);
    }

    public class MsgListener implements MsgPushListener {
        private Handler hdler = null;
        private Context ctxt = null;

        public MsgListener() {
            registerBoradcastReceiver();
        }

        public void setHandler(Handler handler) {
            this.hdler = handler;
        }

        public void setContext(Context context) {
            this.ctxt = context;
        }

        @Override
        public void onMessage(byte[] byteArr) {
            // TODO Auto-generated method stub
            String msg = new String(byteArr, 0, byteArr.length,
                    Charset.forName("utf-8"));
            Log.v("MsgPush", msg);

            try {
                dealMessage(msg);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                if (hdler != null) {
                    // 来自服务器的消息格式错误
                    Message message = Message.obtain();
                    message.arg1 = ERROR_FORMAT;
                    hdler.sendMessage(message);
                }
                e.printStackTrace();
            }
        }

        private void dealMessage(String message) throws JSONException {
            JSONTokener jsonParser = new JSONTokener(message);
            JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
            String code = jsonObject.getString("command");

            int reqCode = Integer.parseInt(code);
            int ret;

            switch (reqCode) {
                case NEW_COMMANDS:
                    getCommands();
                    break;
                case OP_PUSH_MSG:
                    getMessages();

                    break;
                case OP_SET_MUTE:
                    String stateMute = jsonObject.getString("state");
                    if (stateMute.equals("true")) {
                        mDeviceAdminWorker.setMute(true);
                    } else {
                        mDeviceAdminWorker.setMute(false);
                    }

                    EmmClientApplication.mDb.updateOrInsertItemByInfo(
                            EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                            null,
                            null,
                            new String[]{"code", "content", "state", "time"},
                            new String[]{Integer.toString(OP_SET_MUTE), "设置静音",
                                    "s", Long.toString(System.currentTimeMillis())});
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_LOCK_KEY:
                    String passwdLock = jsonObject.getString("passwd");
                    ret = mDeviceAdminWorker.resetPasswd(passwdLock);
                    mDeviceAdminWorker.lockNow();
                    EmmClientApplication.mDb.updateOrInsertItemByInfo(
                            EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                            null,
                            null,
                            new String[]{"code", "content", "state", "time"},
                            new String[]{Integer.toString(OP_LOCK_KEY), "设置锁屏密码",
                                    (ret == 0) ? "s" : "r",
                                    Long.toString(System.currentTimeMillis())});
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_LOCK:
                    mDeviceAdminWorker.lockNow();
                    break;
                case OP_FACTORY:
                    if (hdler != null) {
                        String option = jsonObject.getString("option");

                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = FACTORY;
                        handlerMsg.arg2 = option.equals("enforce") ? ENFORCE : WARN;
                        hdler.sendMessage(handlerMsg);

                        EmmClientApplication.mDb.updateOrInsertItemByInfo(
                                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                                null,
                                null,
                                new String[]{"code", "content", "state", "time"},
                                new String[]{Integer.toString(OP_FACTORY),
                                        "恢复出厂设置", "s",
                                        Long.toString(System.currentTimeMillis())});
                    }
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_ERASE_CORP:
                    if (hdler != null) {
                        String option = jsonObject.getString("option");

                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = ERASE_CORP;
                        handlerMsg.arg2 = option.equals("enforce") ? ENFORCE : WARN;
                        hdler.sendMessage(handlerMsg);

                        EmmClientApplication.mDb.updateOrInsertItemByInfo(
                                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                                null,
                                null,
                                new String[]{"code", "content", "state", "time"},
                                new String[]{Integer.toString(OP_ERASE_CORP),
                                        "擦除企业数据", "s",
                                        Long.toString(System.currentTimeMillis())});
                    }
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_ERASE_ALL:
                    if (hdler != null) {
                        String option = jsonObject.getString("option");

                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = ERASE_ALL;
                        handlerMsg.arg2 = option.equals("enforce") ? ENFORCE : WARN;
                        hdler.sendMessage(handlerMsg);

                        EmmClientApplication.mDb.updateOrInsertItemByInfo(
                                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                                null,
                                null,
                                new String[]{"code", "content", "state", "time"},
                                new String[]{Integer.toString(OP_ERASE_ALL),
                                        "擦除设备上的所有数据", "s",
                                        Long.toString(System.currentTimeMillis())});
                    }
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_REFRESH:
                    // TODO：重新上传设备信息
                    if (hdler != null) {
                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = REFRESH_ALL;
                        hdler.sendMessage(handlerMsg);
                    }
                    EmmClientApplication.mDb.updateOrInsertItemByInfo(
                            EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                            null,
                            null,
                            new String[]{"code", "content", "state", "time"},
                            new String[]{Integer.toString(OP_REFRESH), "刷新设备",
                                    "s", Long.toString(System.currentTimeMillis())});
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_POLICY1:
                case OP_POLICY2:
                    Log.d("POLICY", "Receive new policy notification");
                    EmmClientApplication.mDb.updateOrInsertItemByInfo(
                            EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                            null,
                            null,
                            new String[]{"code", "content", "state", "time"},
                            new String[]{Integer.toString(OP_POLICY1), "企业策略推送",
                                    "s", Long.toString(System.currentTimeMillis())});
                    ((EmmClientApplication) ctxt.getApplicationContext())
                            .getPolicyManager().updatePolicy();
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;

                case AUTH_STATE_CHANGE:
                    // TODO：重新上传设备信息
                    if (hdler != null) {
                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = AUTH_STATE_CHANGE;
                        hdler.sendMessage(handlerMsg);
                    }
                    EmmClientApplication.mDb.updateOrInsertItemByInfo(
                            EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                            null,
                            null,
                            new String[]{"code", "content", "state", "time"},
                            new String[]{Integer.toString(AUTH_STATE_CHANGE),
                                    "改变设备状态", "s",
                                    Long.toString(System.currentTimeMillis())});
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case DELETE_DEVICE:
                    if (hdler != null) {
                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = DELETE_DEVICE;
                        hdler.sendMessage(handlerMsg);
                    }
                    // ActivateDevice activate = ((EmmClientApplication)
                    // ctxt.getApplicationContext()).getActivateDevice();
                    // activate.clearActivateInfo();
                    // Intent intent = new Intent(ctxt, AppStartActivity.class);
                    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    // ctxt.startActivity(intent);
                    EmmClientApplication.mDb.updateOrInsertItemByInfo(
                            EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                            null,
                            null,
                            new String[]{"code", "content", "state", "time"},
                            new String[]{Integer.toString(DELETE_DEVICE),
                                    "服务器端删除设备", "s",
                                    Long.toString(System.currentTimeMillis())});
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_CHANGE_DEVICE_TYPE:
                    if (hdler != null) {
                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = DEVICE_TYPE;
                        hdler.sendMessage(handlerMsg);
                    }
                default:
                    break;
            }
        }

        private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("get_message")) {
                    getMessages();
                }
            }

        };

        public void registerBoradcastReceiver() {
            IntentFilter myIntentFilter = new IntentFilter();
            myIntentFilter.addAction("get_message");
            // 注册广播
            registerReceiver(mBroadcastReceiver, myIntentFilter);
        }

        private void getMessages() {
            final SharedPreferences msgCount = ctxt.getSharedPreferences(
                    PrefUtils.MSG_COUNT, 0);
            final int maxId = msgCount.getInt("max_id", 0);
            MyJsonArrayRequest request = new MyJsonArrayRequest(Request.Method.GET, "/client/devices/" + EmmClientApplication.mPhoneInfo.getIMEI() +
                    "/messages?message_id=" + maxId, UpdateTokenRequest.TokenType.DEVICE, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray array) {
                    try {
                        for (int i = array.length() - 1; i >= 0; i--) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            String msg = jsonObject.getString("content");
                            String created_at = jsonObject
                                    .getString("created_at");
                            int id = jsonObject.getInt("id");
                            int mId = Math.max(id, maxId);
                            EmmClientApplication.mDb.updateOrInsertItemByInfo(
                                    EmmClientDb.DEVICEMSG_DATABASE_TABLE, null,
                                    null, new String[]{"msg", "time"},
                                    new String[]{msg, created_at});

                            // 存储未读条数
                            int unreadCount = msgCount
                                    .getInt("unread_count", 0);
                            SharedPreferences.Editor editor = msgCount.edit();
                            editor.putInt("unread_count", unreadCount + 1);
                            editor.putInt("max_id", mId);
                            editor.commit();
                            if (hdler != null) {
                                Intent intent = new Intent();
                                intent.setAction(PlaceholderFragment.NEW_MESSAGE);
                                ctxt.sendBroadcast(intent);
                                Bundle bd = new Bundle();
                                bd.putCharSequence("msg", msg);
                                Message handlerMsg = Message.obtain();
                                handlerMsg.arg1 = PUSH_MSG;
                                handlerMsg.setData(bd);
                                hdler.sendMessageAtFrontOfQueue(handlerMsg);
                            }
                        }
                        notifyDataChange(BroadCastDef.OP_MSG);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, null);
            EmmClientApplication.mVolleyQueue.add(request);
        }

        private void getCommands() {
            MyJsonArrayRequest request = new MyJsonArrayRequest(Request.Method.GET, "/client/devices/" + EmmClientApplication.mPhoneInfo.getIMEI()
                    + "/commands", UpdateTokenRequest.TokenType.DEVICE, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray array) {
                    try {
                        for (int i = 0; i < array.length(); i++) {
                            String command = array.getJSONObject(i).toString();
                            dealMessage(command);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, null);
            EmmClientApplication.mVolleyQueue.add(request);
        }

        private void notifyDataChange(String action) {
            Intent intent = new Intent();
            intent.setAction(action);
            ctxt.sendBroadcast(intent);
        }

        @Override
        public void onState(boolean state) {
            // TODO Auto-generated method stub
            Bundle params = new Bundle();
            params.putBoolean("online", state);
            params.putLong("lastOnlineTime", System.currentTimeMillis());
            EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_OnlineState, params));
        }

    }

    private String getDeviceInfoDetail() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("manufacturers",
                    mPhoneInfoExtractor.getDeviceManufacturer());
            jsonObject.put("model", mPhoneInfoExtractor.getDeviceModel());
            jsonObject.put("operatingSystem", mPhoneInfoExtractor.getOs());
            jsonObject.put("freeMemory", mPhoneInfoExtractor.getAvailMem()
                    / 1024 + "MB");
            jsonObject.put("phoneNumber", mPhoneInfoExtractor.getPhoneNumber());
            jsonObject.put("circuitCardId", mPhoneInfoExtractor.getICCID());
            jsonObject.put("phoneRoaming", mPhoneInfoExtractor.isRoaming());
            jsonObject.put("root", mPhoneInfoExtractor.isRooted());
            jsonObject.put("deviceSerialNumber", mPhoneInfoExtractor.getIMSI());
            jsonObject.put("processorName", mPhoneInfoExtractor.getCpuName());
            jsonObject.put("processorSpeed",
                    mPhoneInfoExtractor.getCpuMaxFrequence() / 1024 + "MHz");
            jsonObject.put("numberOfProcessors",
                    mPhoneInfoExtractor.getCpuCoreNum() + "核");
            jsonObject.put("runningMemory", mPhoneInfoExtractor.getRuningMem()
                    / 1024 + "MB");
            jsonObject.put("totalMemory", mPhoneInfoExtractor.getTotalMem()
                    / 1024 + "MB");
            jsonObject.put("totalExternalMemory",
                    mPhoneInfoExtractor.getExternalTotalStorage() + "MB");
            jsonObject.put("remainingExternalMemory",
                    mPhoneInfoExtractor.getExternalAvail() + "MB");

			/*
             * 应用数据
			 */
            jsonObject.put("screenSeparatelyRate",
                    mPhoneInfoExtractor.getDisplay());
            jsonObject.put("screenSize", mPhoneInfoExtractor.getDisplayInch()
                    + "寸");
            jsonObject.put("systemLanguage", mPhoneInfoExtractor.getLanguage());
            jsonObject.put("timeZone", mPhoneInfoExtractor.getTimeZone());
            jsonObject.put("userId", mPhoneInfoExtractor.getIMSI());
            jsonObject.put("simCardOperators",
                    mPhoneInfoExtractor.getSimOperatorName());
            jsonObject.put("networkOperators",
                    mPhoneInfoExtractor.getNetworkOperator());
            jsonObject.put("nationality", mPhoneInfoExtractor.getCountry());
            jsonObject.put("openNetwork", mPhoneInfoExtractor.isConnected());
            jsonObject.put("networkType", mPhoneInfoExtractor.getNetworkType());

            WifiNetworkInfo wifiInfo = mPhoneInfoExtractor.getWifiInfo();
            jsonObject.put("wifiMacAddress", wifiInfo.mac);
            jsonObject.put("lastConnectionTime", wifiInfo.lastConnected);
            jsonObject.put("ipAddress", wifiInfo.ip);
            jsonObject.put("serviceSetIdentifier", wifiInfo.ssid);
            jsonObject.put("country", mPhoneInfoExtractor.getCountry());
            jsonObject.put("buildVersion",
                    mPhoneInfoExtractor.getBuildVersion());
            jsonObject
                    .put("email",
                            mPhoneInfoExtractor.getAccount().size() > 0 ? mPhoneInfoExtractor
                                    .getAccount().get(0) : "未设置邮箱");
            jsonObject.put("systemVersion", mPhoneInfoExtractor.getOsVersion());
            jsonObject.put("kernelVersion",
                    mPhoneInfoExtractor.getKernelVersion());
            jsonObject.put("interfaceVersion",
                    String.valueOf(mPhoneInfoExtractor.getSDKInt()));
            jsonObject
                    .put("basebandVersion", mPhoneInfoExtractor.getBaseband());
            jsonObject.put("passwordPolicyRules",
                    mDeviceAdminWorker.isPasswdSufficient());
            jsonObject.put("dataSynchronization",
                    mPhoneInfoExtractor.isDataSyncOpen());
            jsonObject.put("serviceInfo",
                    mApplicationManager.getServicesInJson());
            jsonObject.put("softInfo", mApplicationManager.getAppsInJson());
            jsonObject.put("belongsPolicyName",
                    ((EmmClientApplication) MDMService.this
                            .getApplicationContext()).getPolicyManager()
                            .getPolicy().getName());
            jsonObject.put("policyCompletionStatus", "完成");
            jsonObject.put("strategyReleasedVersion", "1.0");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /*
     * 上传设备基本信息
     */
    private void updateDeviceInfo() {
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.PUT, "/client/devices/" + EmmClientApplication.mPhoneInfo.getIMEI() + "/infos",
                UpdateTokenRequest.TokenType.DEVICE, null, null) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }

            @Override
            public byte[] getBody() {
                return getDeviceInfoDetail().getBytes();
            }
        };
        EmmClientApplication.mVolleyQueue.add(request);
    }

    private void updateLocationInfo() {
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.PUT, "/client/devices/" + EmmClientApplication.mPhoneInfo.getIMEI() + "/locations",
                UpdateTokenRequest.TokenType.DEVICE, null, null) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }

            @Override
            public byte[] getBody() {
                Map<String, String> map = new HashMap<>();
                Timestamp timeStamp = new Timestamp(EmmClientApplication.mActivateDevice.lastOnlineTime);// 获取系统当前时间
                String onlineTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(timeStamp);
                map.put("lng", String.valueOf(curLocation.getLongitude()));
                map.put("lat", String.valueOf(curLocation.getLatitude()));

                String loginName = EmmClientApplication.mCheckAccount.getCurrentName();
                map.put("login_name", (loginName == null ? "无人使用" : loginName));
                map.put("login_email", (email == null ? "无人使用" : email));
                map.put(
                        "online_at", (activate.online ? (new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss").format(new Timestamp(System
                                .currentTimeMillis()))) : onlineTime));
                map.put("owner", owner);
                map.put("model", EmmClientApplication.mPhoneInfo.getDeviceManufacturer() + " " + EmmClientApplication.mPhoneInfo.getDeviceModel());
                map.put("operate_system", EmmClientApplication.mPhoneInfo.getOsAndVersion());
                map.put("show", EmmClientApplication.mPhoneInfo.getLocCapability() ? "true" : "false");
                String ms = new JSONObject(map).toString();
                return ms.getBytes();
            }
        };
        EmmClientApplication.mVolleyQueue.add(request);
    }

    public static void downloadDeviceInfo() {
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET, "/client/devices/" + EmmClientApplication.mPhoneInfo.getIMEI(),
                UpdateTokenRequest.TokenType.DEVICE, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject deviceInfo) {
                try {
                    EmmClientApplication.mActivateDevice.setDeviceType(deviceInfo.getString("type"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, null);
        EmmClientApplication.mVolleyQueue.add(request);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // Receive Location
            curLocation = location;
            updateLocationInfo();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return myBinder;
    }

    public class EMMSBinder extends Binder {

        public MDMService getService() {
            return MDMService.this;
        }
    }

    private EMMSBinder myBinder = new EMMSBinder();

}
