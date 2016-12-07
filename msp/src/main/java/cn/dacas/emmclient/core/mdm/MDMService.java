package cn.dacas.emmclient.core.mdm;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.WindowManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.dacas.emmclient.manager.AddressManager;
import cn.dacas.emmclient.model.DeviceModel;
import cn.dacas.emmclient.msgpush.MsgWorker;
import cn.dacas.emmclient.msgpush.PushMsgReceiver;
import cn.dacas.emmclient.webservice.QdParser;
import cn.dacas.pushmessagesdk.PushMsgManager;
import cn.dacas.emmclient.business.BusinessListener;
import cn.dacas.emmclient.controller.ControllerListener;
import cn.dacas.emmclient.controller.McmController;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.core.forward.IForward;
import cn.dacas.emmclient.core.mam.MApplicationManager;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.manager.UrlManager;
import cn.dacas.emmclient.model.ActivateDevice;
import cn.dacas.emmclient.model.CheckAccount;
import cn.dacas.emmclient.model.McmMessageModel;
import cn.dacas.emmclient.ui.activity.mainframe.MsgDetailActivity;
import cn.dacas.emmclient.util.BroadCastDef;
import cn.dacas.emmclient.util.GlobalConsts;
import cn.dacas.emmclient.util.PhoneInfoExtractor;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.QDLog;
import cn.dacas.emmclient.webservice.qdvolley.MyJsonObjectRequest;
import cn.dacas.emmclient.webservice.qdvolley.UpdateTokenRequest;

import de.greenrobot.event.EventBus;

/**
 * 设备管理服务
 */
public class MDMService extends Service implements ControllerListener {

    public static final String TAG = "MDMService";
    private static Context mContext;
    private ActivateDevice activate;


    private LocationClient mLocationClient;
    private MyLocationListener mMyLocationListener;
    private BDLocation curLocation;
    private IForward forward;

    private String email;

    private String owner;

    private MsgWorker mMsgWorker;

    public interface CmdCode {
        int ENFORCE = 1;
        // 提示用户选项
        int WARN = 2;
        int PUSH_MSG = 3;
        int ERASE_CORP = 4;
        int ERROR_FORMAT = 5;
        int ERASE_ALL = 6;
        int FACTORY = 7;
        int ERROR_MSG_SERVER = 8;
        int PUSH_APP = 9;
        int PUSH_FILE = 10;
        int REFRESH_ALL = 11;
        int DEVICE_INFO_CHANGE = 12;

        int AUTH_STATE_CHANGE = 1010;// 授权状态改变
        int OP_CAMERA = 1030;
        int OP_PUSH_MSG = 1055;
        int OP_WIFI_CONFIG = 1020;
        int OP_SET_MUTE = 1025;
        int OP_LOCK_KEY = 1035;
        int OP_LOCK = 1036; // 锁屏
        int OP_REFRESH = 1045;
        int OP_ERASE_CORP = 1050;
        int OP_ERASE_ALL = 1060;
        int OP_FACTORY = 1065;
        int OP_POLICY1 = 1070;
        int OP_INSTALL_POLICY2 = 1075;
        int OP_REMOVE_PROFILE = 1076;
        int OP_PUSH_FILE = 1080; // fileUrlList以,分割
        int OP_CHANGE_DEVICE_TYPE = 1015; // 设备类型改变
        int DELETE_DEVICE = 1090; // 服务器端删除设备
        int FORBIDDEN_DEVICE = 1091;
        int NEW_COMMANDS = 1111;// 新指令

        int ALERT_DIALOG = 1200;//提示
    }

    public static final String PREF_NAME = "APP_CAPA";
    public static final String DEVICE = "deviceType";

    private DeviceAdminMonitor mMonitor;

    PushMsgManager pushMsgManager;

    //接收EventBus发生的消息
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

    /**
     * 上传位置服务，
     * 如果needLoaction()=true 开始定位，并立即要求获得定位，
     * 否者就停止
     */
    private void uploadLocation() {
        if (needLocation()) {
            mLocationClient.start();
            mLocationClient.requestLocation();
        } else {
            mLocationClient.stop();
        }
    }

    private void startTransaction(final String owner) {
        //if (MDMService.this.owner!=null) return;
        QDLog.d("MDMService", "start transaction");
        //updateDeviceInfo();
        //uploadLocation();
        MDMService.this.owner = owner;
        //Looper.prepare();
        startMsgPush();

        // 1）每次注册成功，都向服务器询问当前策略
        // 2）在运行期间的策略更新依赖于“消息通知”
        PolicyManager.getMPolicyManager(mContext).updatePolicy();
        mMonitor.startScanPolicy();
    }

    /**
     * init RegMsgPush, -> init PushMsgManager
     */
    private void startMsgPush() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                if(ActivateDevice.online || mMsgWorker.isWorking())
                    return;
                String ip = AddressManager.getAddrMsg();
                QDLog.d("MDMService", "MsgPush reg to " + ip);
                QDLog.writeMsgPushLog("try to connect to" + ip);
                String imei = PhoneInfoExtractor.getIMEI(mContext);
                QDLog.d(TAG, "IMEI: "+imei);

                //Init PushMsgManager
                PushMsgReceiver.setMsgWorker(mMsgWorker);
                try {
                    pushMsgManager = new PushMsgManager.Builder(getApplicationContext(), UrlManager.getMsgPushUrl())
                            .setRegServerUrl(UrlManager.getRegMsgPushUrl())
                            .setClientIdAndKey("046e2930-7cc2-4398-9b1c-65852317de29",
                                    "6668b6a3-8486-4165-a418-374194ad47d3")
                            .buildAndRun();
                    pushMsgManager.addFullTopicToLists(imei,PushMsgManager.CommCodeType.NET_GetAliase);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }).start();
    }


    private void stopForwarding() {
        email = null;
        forward.stopMapping();
    }

    private void startForwarding(final String email) {
        MDMService.this.email = email;
        mMcmController.startForwarding(email);

    }

    /**
     * 判断当前是否需要定位
     * @return boolen
     */
    private boolean needLocation() {
        String account = CheckAccount.getCheckAccountInstance(mContext)
                .getCurrentAccount();
        //没有登录时，不需要定位
        if (account == null || account.equals(""))
            return false;
        String device_type = EmmClientApplication.mActivateDevice.getDeviceType();
        //设备没有类型的时候不需要定位
        if (device_type == null || device_type.equals(""))
            return false;
        //未知设备类型且设置不允许定位的，不定位
        if (device_type.equals("BYOD") || device_type.equals("UNKNOWN")) {
            SharedPreferences settings = mContext.getSharedPreferences(
                    PREF_NAME, 0);
            if (!settings.getBoolean("allowLocationInfo", false))
                return false;
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        activate = EmmClientApplication.mActivateDevice;

        //////初始化Controller
        mMcmController = new McmController(mContext,this);

        mMsgWorker = new MsgWorker(mContext,uiHandler,mMcmController);

        //baidulocation init
        mMyLocationListener = new MyLocationListener();
        mLocationClient = new LocationService(getApplication(),mMyLocationListener).getLocationClient();

        //forward = new IForward();
        EventBus.getDefault().register(this);

        mMonitor=new DeviceAdminMonitor(mContext);

        // deal with app running while service down
        String owner = PrefUtils.getAdministrator();
        if (owner != null) {
            startTransaction(owner);
        }
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

            AlertDialog.Builder builder;
            AlertDialog alertDialog;

            switch (code) {
                case CmdCode.PUSH_MSG:
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
                                //点击该通知后要跳转的Activity
                                //DeviceMessageActivity.class);
                                MsgDetailActivity.class);
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
                        notification.flags |= Notification.FLAG_ONGOING_EVENT;
                        // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
                        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                        notification.flags |= Notification.FLAG_AUTO_CANCEL;
                        notificationManager.notify(0, notification);
                    }
                    break;
                case CmdCode.FACTORY:
                    if (msg.arg2 == CmdCode.ENFORCE) {
                        DeviceAdminWorker.getDeviceAdminWorker(mContext).wipeData(false);
                    } else {
                        new Thread(new Runnable() {
                            public void run() {
                                DeviceAdminWorker.getDeviceAdminWorker(mContext).wipeData(false);
                            }
                        }).start();
                        builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("恢复出厂设置");
                        builder.setMessage("该设备已经被恢复出厂设置");
                        builder.setPositiveButton("OK", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        alertDialog = builder.create();
                        alertDialog.getWindow().setType(
                                (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                        alertDialog.show();
                    }
                    break;
                case CmdCode.ERASE_ALL:
                    if (msg.arg2 == CmdCode.ENFORCE) {
                        DeviceAdminWorker.getDeviceAdminWorker(mContext).wipeData(true);
                    } else {
                        new Thread(new Runnable() {
                            public void run() {
                                DeviceAdminWorker.getDeviceAdminWorker(mContext).wipeData(true);
                            }
                        }).start();
                        builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("擦除设备所有数据");
                        builder.setMessage("该设备已被擦除所有数据，包括恢复出厂和sd卡擦除");

                        builder.setPositiveButton("OK", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        alertDialog = builder.create();
                        alertDialog.getWindow().setType(
                                (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                        alertDialog.show();
                    }
                    break;
                case CmdCode.ERASE_CORP:
                    if (msg.arg2 == CmdCode.ENFORCE) {
                        new Thread(new Runnable() {
                            public void run() {
                               EmmClientApplication.mDatabaseEngine.clearCorpData();
                            }
                        }).start();
                    } else {
                        new Thread(new Runnable() {
                            public void run() {
                                EmmClientApplication.mSecureContainer.deletAllFiles();
                                EmmClientApplication.mDatabaseEngine.clearCorpData();
                            }
                        }).start();
                        builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("擦除企业数据");
                        builder.setMessage("该设备已被擦除企业数据");
                        builder.setPositiveButton("OK", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        alertDialog = builder.create();
                        alertDialog.getWindow().setType(
                                (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                        alertDialog.show();
                    }
                    break;
                case CmdCode.ERROR_FORMAT:
                    builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("格式错误");
                    builder.setMessage("来自消息推送服务器的消息格式不合法！");

                    builder.setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertDialog = builder.create();
                    alertDialog.getWindow().setType(
                            (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    alertDialog.show();
                    break;
                case CmdCode.ERROR_MSG_SERVER:
                    builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("服务器错误");
                    builder.setMessage("无法连接消息推送服务器!");

                    builder.setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertDialog = builder.create();
                    alertDialog.getWindow().setType(
                            (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    alertDialog.show();
                    break;
                case CmdCode.REFRESH_ALL:
                    String uuid = (String) msg.obj;
                    HashMap<String, JSONObject> map = new HashMap<>();
                    map.put("query_responses", getDeviceInfoDetail());
                    mMsgWorker.sendStatusToServer("Acknowledged", uuid, map);
                    updatePrivacySetting(PrefUtils.getNetPrivacy());
                    break;
                case CmdCode.AUTH_STATE_CHANGE:
//                    DisableTask dt1 = new DisableTask(MDMService.this);
//                    dt1.execute(DisableTask.AUTH_STATE_CHANGE);
                    break;
                case CmdCode.DELETE_DEVICE:
//                    DisableTask dt2 = new DisableTask(MDMService.this);
//                    dt2.execute(DisableTask.DELETE_DEVICE);
                    break;
                case CmdCode.DEVICE_INFO_CHANGE:
                    downloadDeviceInfo();
                    break;
                case CmdCode.ALERT_DIALOG:
                    builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("消息提醒");
                    String s = (String) msg.obj;
                    builder.setMessage(s);

                    builder.setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertDialog = builder.create();
                    alertDialog.getWindow().setType(
                            (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    alertDialog.show();
                    break;
                case CmdCode.FORBIDDEN_DEVICE:
                    startActivity(EmmClientApplication.getExitApplicationIntent());
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

    private JSONObject getDeviceInfoDetail() {
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
            e.printStackTrace();
        }
        return jsonObject;
    }

    static public void updatePrivacySetting(final Boolean isChecked) {
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.PUT,
                "/client/devices/" + PhoneInfoExtractor.getIMEI(mContext) + "/privacy",
                UpdateTokenRequest.TokenType.DEVICE, null, null) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }

            @Override
            public byte[] getBody() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("network_show",isChecked);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonObject.toString().getBytes();
            }
        };
        EmmClientApplication.mVolleyQueue.add(request);
    }

    /**
     * 上传设备基本信息
     */
    private void updateDeviceInfo() {
        updatePrivacySetting(PrefUtils.getNetPrivacy());
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
                return getDeviceInfoDetail().toString().getBytes();
            }
        };
        EmmClientApplication.mVolleyQueue.add(request);
    }

    /**
     *上传定位信息
     */
    private void updateLocationInfo() {
        final PhoneInfoExtractor mPhoneInfoExtractor=EmmClientApplication.mPhoneInfoExtractor;
        //访问Web服务器，汇报地址，没有响应函数
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
                // 获取系统当前时间
                Timestamp timeStamp = new Timestamp(EmmClientApplication.mActivateDevice.lastOnlineTime);
                String onlineTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(timeStamp);
                //加入定位信息
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

    public void downloadDeviceInfo() {
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET, "/client/devices/" + PhoneInfoExtractor.getIMEI(mContext),
                UpdateTokenRequest.TokenType.DEVICE, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject deviceInfo) {
                try {
                    DeviceModel deviceModel = QdParser.parseDevice(deviceInfo);
                    EmmClientApplication.mDeviceModel = deviceModel;
                    if(!deviceModel.isStatus()) {
                        Message message = Message.obtain();
                        message.arg1 = CmdCode.FORBIDDEN_DEVICE;
                        uiHandler.sendMessage(message);
                    }
                    if(!deviceModel.getType().equals(EmmClientApplication.mActivateDevice.getDeviceType())) {
                        EmmClientApplication.mActivateDevice.setDeviceType(deviceInfo.getString("type"));
                        Message message = Message.obtain();
                        message.arg1 = CmdCode.ALERT_DIALOG;
                        message.obj = "设备类型改变为："+deviceModel.getType();
                        uiHandler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, null);
        EmmClientApplication.mVolleyQueue.add(request);
    }

    /**
     * 自定义定位监听类，设置在收到定位信息后，如何做
     */
    public class MyLocationListener implements BDLocationListener {
        /**
         * 异步收到定位信息，更新当前定位，将定位信息上传给服务器
         * @param location
         */
        @Override
        public void onReceiveLocation(BDLocation location) {
            // Receive Location
            curLocation = location;
            updateLocationInfo();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
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
                            //forward.addMapping(map);
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

            //send action好像并没有什么用
            mMsgWorker.notifyDataChange(GlobalConsts.NEW_MESSAGE);

            //send msg 发出消息，显示通知
            //mMsgWorker.sendMsg(m.content);

        }
        //sendBroadcast
        QDLog.i(TAG, "UpdateMessageList======OP_MSG=======" + BroadCastDef.OP_MSG);
        mMsgWorker.notifyDataChange(BroadCastDef.OP_MSG);

    }



    //from MDMService
    private void UpdateCommandList(List<String> modelList) {
        if (modelList == null || modelList.size() <= 0) {
            return;
        }
        for (int i = 0; i < modelList.size(); i++) {
            QDLog.i(TAG, "UpdateCommandList=============" + modelList.get(i));
            String m = modelList.get(i);
            try {
                //mMsgWorker.dealMessage(m);
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

}
