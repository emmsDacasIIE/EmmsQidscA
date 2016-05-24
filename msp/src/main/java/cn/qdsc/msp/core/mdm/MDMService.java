package cn.qdsc.msp.core.mdm;

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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.dacas.emmclient.msgpush.MsgPushListener;
import cn.dacas.emmclient.msgpush.RegMsgPush;
import cn.qdsc.msp.business.BusinessListener;
import cn.qdsc.msp.controller.ControllerListener;
import cn.qdsc.msp.controller.McmController;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.core.forward.IForward;
import cn.qdsc.msp.core.mam.MApplicationManager;
import cn.qdsc.msp.event.MessageEvent;
import cn.qdsc.msp.model.ActivateDevice;
import cn.qdsc.msp.model.CheckAccount;
import cn.qdsc.msp.model.McmMessageModel;
import cn.qdsc.msp.ui.activity.mainframe.NewMainActivity;
import cn.qdsc.msp.util.BroadCastDef;
import cn.qdsc.msp.util.GlobalConsts;
import cn.qdsc.msp.util.PhoneInfoExtractor;
import cn.qdsc.msp.util.PrefUtils;
import cn.qdsc.msp.util.QDLog;
import cn.qdsc.msp.webservice.qdvolley.MyJsonObjectRequest;
import cn.qdsc.msp.webservice.qdvolley.UpdateTokenRequest;
import de.greenrobot.event.EventBus;

;


public class MDMService extends Service implements ControllerListener {

    private static final String TAG = "MDMService";
    private static Context mContext;
    private ActivateDevice activate;


    private LocationClient mLocationClient;
    private MyLocationListener mMyLocationListener;
    private BDLocation curLocation;
    private IForward forward;

    private String email;

    private String owner;

    private MsgListener mMsgListener;

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

    private DeviceAdminMonitor mMonitor;

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
            case MessageEvent.Event_StartMsgPush:
                startMsgPush();
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
//        if (MDMService.this.owner!=null) return;
        Log.d("MDMService", "start transaction");
        updateDeviceInfo();
//        uploadLocation();
        MDMService.this.owner = owner;
        startMsgPush();
        // 1）每次注册成功，都向服务器询问当前策略
        // 2）在运行期间的策略更新依赖于“消息通知”
        PolicyManager.getMPolicyManager(mContext).updatePolicy();
        mMonitor.startScanPolicy();
    }

    private void startMsgPush() {
        String ip = cn.qdsc.msp.manager.AddressManager.getAddrMsg();
        RegMsgPush sub = new RegMsgPush();
        Log.d("MDMService", "MsgPush reg to " + ip);
        QDLog.writeMsgPushLog("try to connect to" + ip);
        sub.init(PhoneInfoExtractor.getIMEI(mContext), mMsgListener,
                new String[]{ip});
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
        mMcmController.startForwarding(email);

    }

    private boolean needLoaction() {
        String account = CheckAccount.getCheckAccountInstance(mContext)
                .getCurrentAccount();
        if (account == null || account.equals(""))
            return false;
        String device_type = EmmClientApplication.mActivateDevice.getDeviceType();
        if (device_type == null || device_type.equals(""))
            return false;
        if (device_type.equals("BYOD") || device_type.equals("UNKNOWN")) {
            SharedPreferences settings = mContext.getSharedPreferences(
                    PREF_NAME, 0);
            if (settings.getBoolean("allowLocationInfo", false) == false)
                return false;
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        activate = EmmClientApplication.mActivateDevice;

        mMsgListener = new MsgListener();
        mMsgListener.setContext(mContext);
        mMsgListener.setHandler(uiHandler);

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

        mMonitor=new DeviceAdminMonitor(mContext);

        // deal with app running while service down
        String owner = PrefUtils.getAdministrator();
        if (owner != null) {
            startTransaction(owner);
        }

        //////初始化Controller
        mMcmController = new McmController(mContext,this);



    }

    @Override
    public int onStartCommand(Intent it, int flags, int startId) {
        super.onStartCommand(it, flags, startId);
        if (mMonitor==null)
            mMonitor=new DeviceAdminMonitor(mContext);
        // deal with app running while service down
        String owner = PrefUtils.getAdministrator();
        if (owner != null) {
            startTransaction(owner);
        }
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
                        NotificationManager notificationManager = (NotificationManager) mContext
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                        Intent notificationIntent = new Intent(
                                mContext.getApplicationContext(),
//                                DeviceMessageActivity.class); // 点击该通知后要跳转的Activity
                                NewMainActivity.class);
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        notificationIntent.putExtra("FromMsg", true);
                        int unreadCount = PrefUtils.getMsgUnReadCount();
                        if (unreadCount > 1)
                            content = "您有" + String.valueOf(unreadCount) + "条未读消息。";

                        PendingIntent contentItent = PendingIntent.getActivity(
                                mContext.getApplicationContext(), 0,
                                notificationIntent, 0);
                        Notification.Builder notificationBuilder = new Notification.Builder(
                                mContext);
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
                        DeviceAdminWorker.getDeviceAdminWorker(mContext).wipeData(false);
                    } else {
                        builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("恢复出厂设置");
                        builder.setMessage("是否确定执行？");

                        builder.setPositiveButton("确定", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                new Thread(new Runnable() {
                                    public void run() {
                                        DeviceAdminWorker.getDeviceAdminWorker(mContext).wipeData(false);
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
                        DeviceAdminWorker.getDeviceAdminWorker(mContext).wipeData(true);
                    } else {
                        builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("擦除设备所有数据");
                        builder.setMessage("包括恢复出厂和sd卡擦除，是否确定执行？");

                        builder.setPositiveButton("确定", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                new Thread(new Runnable() {
                                    public void run() {
                                        DeviceAdminWorker.getDeviceAdminWorker(mContext).wipeData(true);
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
//                                EmmClientApplication.mDb.clearCorpData();
                            }
                        }).start();
                    } else {
                        builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("擦除企业数据");
                        builder.setMessage("是否确定执行？");

                        builder.setPositiveButton("确定", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                new Thread(new Runnable() {
                                    public void run() {
//                                        EmmClientApplication.mDb.clearCorpData();
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
                    builder = new AlertDialog.Builder(mContext);
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
                    builder = new AlertDialog.Builder(mContext);
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
                case REFRESH_ALL:
                    updateDeviceInfo();
                    break;
                case AUTH_STATE_CHANGE:
//                    DisableTask dt1 = new DisableTask(MDMService.this);
//                    dt1.execute(DisableTask.AUTH_STATE_CHANGE);
                    break;
                case DELETE_DEVICE:
//                    DisableTask dt2 = new DisableTask(MDMService.this);
//                    dt2.execute(DisableTask.DELETE_DEVICE);
                    break;
                case DEVICE_TYPE:
                    downloadDeviceInfo();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.unRegisterLocationListener(mMyLocationListener);
        if (mMonitor!=null) {
            mMonitor.stopScanPolicy();
            mMonitor=null;
        }
        EventBus.getDefault().unregister(this);
        sendBroadcast(new Intent("cn.dacas.intent.action.SERVICE_RESTART"));
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

        public Handler getHandler() {
           return this.hdler;
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
            QDLog.writeMsgPushLog("receive msg=" + msg);

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
                        DeviceAdminWorker.getDeviceAdminWorker(mContext).setMute(true);
                    } else {
                        DeviceAdminWorker.getDeviceAdminWorker(mContext).setMute(false);
                    }
                    EmmClientApplication.mDatabaseEngine.setMute(OP_SET_MUTE);
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_LOCK_KEY:
                    String passwdLock = jsonObject.getString("passwd");
                    ret = DeviceAdminWorker.getDeviceAdminWorker(mContext).resetPasswd(passwdLock);
                    DeviceAdminWorker.getDeviceAdminWorker(mContext).lockNow();
                    EmmClientApplication.mDatabaseEngine.setLockScreenCode(ret, OP_LOCK_KEY);
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_LOCK:
                    DeviceAdminWorker.getDeviceAdminWorker(mContext).lockNow();
                    break;
                case OP_FACTORY:
                    if (hdler != null) {
                        String option = jsonObject.getString("option");

                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = FACTORY;
                        handlerMsg.arg2 = option.equals("enforce") ? ENFORCE : WARN;
                        hdler.sendMessage(handlerMsg);
                        EmmClientApplication.mDatabaseEngine.reFactory(OP_FACTORY);

                    }
//                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_ERASE_CORP:
                    if (hdler != null) {
                        String option = jsonObject.getString("option");

                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = ERASE_CORP;
                        handlerMsg.arg2 = option.equals("enforce") ? ENFORCE : WARN;
                        hdler.sendMessage(handlerMsg);
                        EmmClientApplication.mDatabaseEngine.eraseCorp(OP_ERASE_CORP);

//
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
                        EmmClientApplication.mDatabaseEngine.eraseDeviceAllData(OP_ERASE_ALL);

                    }
//                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_REFRESH:
                    // TODO：重新上传设备信息
                    if (hdler != null) {
                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = REFRESH_ALL;
                        hdler.sendMessage(handlerMsg);
                    }
                    EmmClientApplication.mDatabaseEngine.refreshDevice(OP_REFRESH);
//                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case OP_POLICY1:
                case OP_POLICY2:
                    Log.d("POLICY", "Receive new policy notification");
                    EmmClientApplication.mDatabaseEngine.pushPolicy(OP_POLICY1);

                    PolicyManager.getMPolicyManager(mContext).updatePolicy();
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;

                case AUTH_STATE_CHANGE:
                    // TODO：重新上传设备信息
                    if (hdler != null) {
                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = AUTH_STATE_CHANGE;
                        hdler.sendMessage(handlerMsg);
                    }
                    EmmClientApplication.mDatabaseEngine.changeDeviceState(AUTH_STATE_CHANGE);
                    notifyDataChange(BroadCastDef.OP_LOG);
                    break;
                case DELETE_DEVICE:
                    if (hdler != null) {
                        Message handlerMsg = Message.obtain();
                        handlerMsg.arg1 = DELETE_DEVICE;
                        hdler.sendMessage(handlerMsg);
                    }

                    EmmClientApplication.mDatabaseEngine.deleteDeviceFromService(DELETE_DEVICE);
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
                if (action.equals(GlobalConsts.GET_MESSAGE)) {
                    QDLog.i(TAG,"get_message==========");
                    getMessages();
                }
            }

        };

        public void registerBoradcastReceiver() {
            IntentFilter myIntentFilter = new IntentFilter();
            myIntentFilter.addAction(GlobalConsts.GET_MESSAGE);
            // 注册广播
            registerReceiver(mBroadcastReceiver, myIntentFilter);
        }

        private void getMessages() {
            QDLog.i(TAG,"========getMessages function==========");
            final int maxId=PrefUtils.getMsgMaxId();
            mMcmController.FetchMessageList(maxId);
            PrefUtils.addSecurityRecord("加密一条消息");

        }

        private void getCommands() {
            QDLog.i(TAG,"========getCommands function==========");

            mMcmController.FetchMessageCommand();

        }

        public void notifyDataChange(String action) {
            QDLog.i(TAG,"========notifyDataChange action==========" + action);
            Intent intent = new Intent();
            intent.setAction(action);
            ctxt.sendBroadcast(intent);
        }

        @Override
        public void onState(boolean state) {
            // TODO Auto-generated method stub
            Bundle params = new Bundle();
            params.putBoolean("online", state);
            QDLog.writeMsgPushLog("state=" + (state?"online":"offline"));
            params.putLong("lastOnlineTime", System.currentTimeMillis());
            EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_OnlineState, params));
        }

    }

    private String getDeviceInfoDetail() {
        JSONObject jsonObject = new JSONObject();
        PhoneInfoExtractor mPhoneInfoExtractor=PhoneInfoExtractor.getPhoneInfoExtractor(mContext);
        DeviceAdminWorker mDeviceAdminWorker=DeviceAdminWorker.getDeviceAdminWorker(mContext);
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

            PhoneInfoExtractor.WifiNetworkInfo wifiInfo = mPhoneInfoExtractor.getWifiInfo();
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
                    MApplicationManager.getApplicationManager(mContext).getServicesInJson());
            jsonObject.put("softInfo", MApplicationManager.getApplicationManager(mContext).getAppsInJson());
            jsonObject.put("belongsPolicyName",
                    PolicyManager.getMPolicyManager(mContext).getPolicy().getName());
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
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.PUT, "/client/devices/" + PhoneInfoExtractor.getIMEI(mContext) + "/infos",
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
        final PhoneInfoExtractor mPhoneInfoExtractor=EmmClientApplication.mPhoneInfoExtractor;
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.PUT, "/client/devices/" + mPhoneInfoExtractor.getIMEI(mContext) + "/locations",
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
                map.put("model", mPhoneInfoExtractor.getDeviceManufacturer() + " " + mPhoneInfoExtractor.getDeviceModel());
                map.put("operate_system", mPhoneInfoExtractor.getOsAndVersion());
                map.put("show", PrefUtils.getLockPrivacy() ? "true" : "false");
                String ms = new JSONObject(map).toString();
                return ms.getBytes();
            }
        };
        EmmClientApplication.mVolleyQueue.add(request);
    }

    public static void downloadDeviceInfo() {
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET, "/client/devices/" + PhoneInfoExtractor.getIMEI(mContext),
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


    /////controller请求返回的结果/////////////
    private McmController mMcmController;

    @Override
    public void OnNotify(BusinessListener.BusinessResultCode resCode, BusinessListener.BusinessType type, Object data1, Object data2) {

        QDLog.i(TAG, "OnNotify========" + resCode + "," + type);
        switch (resCode) {
            //请求OK
            case  ResultCode_Sucess:
                switch (type) {
                    case BusinessType_MessageList:
                        if (data1 != null) {
                            List<McmMessageModel> currentList = (List<McmMessageModel>) data1;

                            //考虑在线程中执行，因为全是数据库操作lizy
                            UpdateMessageList(currentList);
                            return;
                        }
                        break;

                    case BusinessType_getCommands:
                        if (data1 != null) {
                            List<String> currentList = (List<String>) data1;

                            //考虑在线程中执行，因为全是数据库操作lizy
                            UpdateCommandList(currentList);

                            return;
                        }
                        break;

                    case BusinessType_startForwarding:
                        if (data1 != null) {
                            HashMap<Integer, String> map = (HashMap<Integer, String>) data1;

                            forward.addMapping(map);
                            return;
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                //response错误
//                showToastMsg(resCode,type);
//                showDocList(false);
        }
    }

    //////需要接口调用的函数////
    //from MDMService
    private void UpdateMessageList(List<McmMessageModel> modelList) {

        if (modelList != null && modelList.size() <= 0) {
            return;
        }

        for (int i = modelList.size() -1; i >= 0;i--) {


            McmMessageModel m = modelList.get(i);
            QDLog.i(TAG, "UpdateMessageList=============" + m.content);
            QDLog.i(TAG, "UpdateMessageList=============" + m.title);
            m.readed = 0;

            boolean bRes = EmmClientApplication.mDatabaseEngine.updateMessageData(m.title, m.content, m.created_at,""+m.readed);
            QDLog.i(TAG, "UpdateMessageList=============" + bRes);
            //save data
            saveUnreadCount(m.id);

            //send action
            mMsgListener.notifyDataChange(GlobalConsts.NEW_MESSAGE);

            //send msg
            sendMsg(m.content);

        }
//        sendBroadcast
        QDLog.i(TAG, "UpdateMessageList======OP_MSG=======" + BroadCastDef.OP_MSG);
        mMsgListener.notifyDataChange(BroadCastDef.OP_MSG);

    }



    //from MDMService
    private void UpdateCommandList(List<String> modelList) {

        if (modelList != null && modelList.size() <= 0) {
            return;
        }

        for (int i = 0; i < modelList.size();i++) {

            QDLog.i(TAG, "UpdateCommandList=============" + modelList.get(i));
            String m = modelList.get(i);
            try {
                mMsgListener.dealMessage(m);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 存储未读条数
    private void saveUnreadCount(int modelId) {
        final int maxId =PrefUtils.getMsgMaxId();
        int mId = Math.max(modelId, maxId);
        int unreadCount = PrefUtils.getMsgUnReadCount();
        PrefUtils.putMsgMaxId(mId);
        PrefUtils.putMsgUnReadCount(unreadCount+1);
    }

    //send handler msg
    private void sendMsg(String msg) {
        Handler handler = mMsgListener.getHandler();
        QDLog.i(TAG,"sendMsg =========="+msg);
        if (handler != null) {
            QDLog.i(TAG,"sendMsg ======111===="+msg);
            Bundle bundle = new Bundle();
            bundle.putCharSequence("msg", msg);
            Message handlerMsg = Message.obtain();
            handlerMsg.arg1 = PUSH_MSG;
            handlerMsg.setData(bundle);
            handler.sendMessageAtFrontOfQueue(handlerMsg);
        }
    }

//    private void notifyDataChange(String action) {
//        Intent intent = new Intent();
//        intent.setAction(action);
//        context.sendBroadcast(intent);
//    }

}
