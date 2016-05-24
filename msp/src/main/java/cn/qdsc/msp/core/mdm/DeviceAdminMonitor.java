package cn.qdsc.msp.core.mdm;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.core.mam.AppManager;
import cn.qdsc.msp.event.MessageEvent;
import cn.qdsc.msp.ui.floatwindow.QdWindowManager;
import cn.qdsc.msp.manager.ActivityManager;
import cn.qdsc.msp.util.PhoneInfoExtractor;
import cn.qdsc.msp.util.WifiAdmin;
import de.greenrobot.event.EventBus;

/**
 * Created by lenovo on 2016-1-21.
 */
public class DeviceAdminMonitor {

    private Context mContext;
    private HashMap<String, Boolean> disableApps;
    private int scanDelay = 1000;
    private String[] googlePlayNames, youTubeNames, emailNames, browserNames,
            settingsNames, galleryNames, gmailNames, googleMapNames;
    DisableDlg disableDlg=null;
    Handler handler=null;


    public DeviceAdminMonitor(Context context) {
        mContext=context;
        googlePlayNames = new String[] { "com.android.vending" };
        youTubeNames = new String[] { "com.google.android.youtube" };
        emailNames = new String[] { "com.android.email",
                "com.google.android.email", "com.htc.android.mail",
                "com.lenovo.email" };
        browserNames = new String[] { "com.htc.sense.browser",
                "com.android.browser", "com.lenovo.browser",
                "com.google.android.browser", "com.wserandroid.browser",
                "com.sec.android.app.sbrowser" };
        settingsNames = new String[] { "com.android.settings" };
        galleryNames = new String[] { "com.android.gallery3d", "com.htc.album",
                "com.lenovo.scgmtk", "com.sonyericsson.album", "com.sec.android.gallery3d",
                "com.google.android.gallery3d"};
        gmailNames = new String[] { "com.google.android.gm" };
        googleMapNames = new String[] { "com.google.android.apps.maps" };
        disableApps = new HashMap<>();

        disableDlg = new DisableDlg(mContext);
        handler=new Handler();
        EventBus.getDefault().register(this);
    }

    //	@Override
    public void finalize() {
        try {
            super.finalize();
            EventBus.getDefault().unregister(this);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // 要做的事情
            String topPkg = AppManager.getForegroundPkg(mContext);
            Boolean state = disableApps.get(topPkg);
            String me = mContext.getApplicationContext().getPackageName();
            //处理悬浮窗
            List<String> strs = AppManager.getHomes(mContext);
            if(strs != null && strs.size() > 0) {
                boolean isHome=strs.contains(topPkg);
                if ( EmmClientApplication.isFloating &&isHome && !QdWindowManager.isWindowShowing()) {
                    QdWindowManager.createfloatWindow(mContext);
                }
                else if ((!isHome ||!EmmClientApplication.isFloating) && QdWindowManager.isWindowShowing()) {
                    QdWindowManager.removeFloatWindow(mContext);
                }
            }
            //处理应用黑白名单
            PolicyContent policy=PolicyManager.getMPolicyManager(mContext).getPolicy();
            setGooglePlayDisable(policy.isDisableGooglePlay());
            setYouTubeDisable(policy.isDisableYouTube());
            setEmailDisable(policy.isDisableEmail());
            setBrowserDisable(policy.isDisableBrowser());
            setSettingsDisable(policy.isDisableSettings());
            setGalleryDisable(policy.isDisableGallery());
            setGmailDisable(policy.isDisableGmail());
            setGoogleMapDisable(policy.isDisableGoogleMap());

            if (state != null && state) {
                disableDlg.showDisableDlg();
            } else if (!topPkg.equals(me) && !PhoneInfoExtractor.isSystemApp(mContext,topPkg)
                    && policy.getWhiteApps().size() > 0 && !policy.getWhiteApps().contains(topPkg)) {
                disableDlg.showDisableDlg();
            } else if (policy.getBlackApps().contains(topPkg)) {
                disableDlg.showDisableDlg();
            }
            EmmClientApplication.intervals+=1;
            if (EmmClientApplication.intervals>=EmmClientApplication.LockSecs)
                EmmClientApplication.intervals=EmmClientApplication.LockSecs;
            if (!topPkg.equals(me)) {
                EmmClientApplication.foregroundIntervals=0;
                EmmClientApplication.runningBackground=true;
            }
            else {
                EmmClientApplication.foregroundIntervals += 1;
                if (EmmClientApplication.foregroundIntervals >= EmmClientApplication.LockSecs)
                {
                    ActivityManager.gotoUnlockActivity();
                    EmmClientApplication.foregroundIntervals=0;
                }
            }
            handler.postDelayed(this, scanDelay);
        }
    };



    public void startScanPolicy() {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, scanDelay);
    }

    public void stopScanPolicy() {
        handler.removeCallbacks(runnable);
    }

    public void showDisableToast() {
        Toast.makeText(mContext, "该功能已经被禁用", Toast.LENGTH_SHORT).show();
    }

    public void onEventBackgroundThread(MessageEvent event) {
        DeviceAdminWorker mDeviceAdminWorker=DeviceAdminWorker.getDeviceAdminWorker(mContext);
        PolicyContent policy= PolicyManager.getMPolicyManager(mContext).getPolicy();
        switch (event.type) {
            case MessageEvent.Event_Bluetooth_State_Changed:
                if ( policy.isDisableBluetooth()) {
                    int state = event.params.getInt("State");
                    if (state == BluetoothAdapter.STATE_TURNING_ON
                            || state == BluetoothAdapter.STATE_ON) {
                        mDeviceAdminWorker.setBluetoothState(false);
                        showDisableToast();
                    }
                }
                break;
            case MessageEvent.Event_Network_State_Changed:
                ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo.State stateWifi = connManager.getNetworkInfo(
                        ConnectivityManager.TYPE_WIFI).getState();
                boolean openWifi = true, openMobile = true;
                if (NetworkInfo.State.CONNECTED == stateWifi
                        || NetworkInfo.State.CONNECTING == stateWifi) {
                    if (policy.isDisableWifi()) { // 判断是否正在使用WIFI网络
                        mDeviceAdminWorker.setWifiState(false);
                        showDisableToast();
                        openWifi = false;
                    } else {
                        WifiAdmin admin=new WifiAdmin(mContext);
                        String ssid = admin.getWifiInfo().getSSID();
                        if (policy.getSsidWhiteList().size() > 0) {
                            if (!policy.getSsidWhiteList().contains(ssid)) {
//                                mDeviceAdminWorker.setWifiState(false);
                                mDeviceAdminWorker.disconnectWifi();
                                openWifi = false;
                                Toast.makeText(mContext, "Wifi不在白名单内", Toast.LENGTH_SHORT).show();
                            }
                        } else if (policy.getSsidBlackList().contains(ssid)) {
//                            mDeviceAdminWorker.setWifiState(false);
                            mDeviceAdminWorker.disconnectWifi();
                            openWifi = false;
                            Toast.makeText(mContext, "Wifi在黑名单内", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                NetworkInfo.State stateMobile = connManager.getNetworkInfo(
                        ConnectivityManager.TYPE_MOBILE).getState(); // 获取网络连接状态
                if (policy.isDisableDataNetwork()
                        && (NetworkInfo.State.CONNECTED == stateMobile || NetworkInfo.State.CONNECTING == stateMobile)) { // 判断是否正在使用GPRS网络
                    mDeviceAdminWorker.setDataConnection(false);
                    openMobile = false;
                    showDisableToast();
                }
                // 重新上线后更新策略
                if ((openWifi && NetworkInfo.State.CONNECTED == stateWifi)
                        || (openMobile && NetworkInfo.State.CONNECTED == stateMobile)) {
                    PolicyManager.getMPolicyManager(mContext).updatePolicy();
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_StartMsgPush));
                }
                break;
            default:
                break;
        }
    }

    public int setGooglePlayDisable(boolean state) {
        for (String name : googlePlayNames) {
            disableApps.put(name, state);
        }
        return 0;
    }

    public int setYouTubeDisable(boolean state) {
        for (String name : youTubeNames) {
            disableApps.put(name, state);
        }
        return 0;
    }

    public int setEmailDisable(boolean state) {
        for (String name : emailNames) {
            disableApps.put(name, state);
        }
        return 0;
    }

    public int setBrowserDisable(boolean state) {
        for (String name : browserNames) {
            disableApps.put(name, state);
        }
        return 0;
    }

    public int setSettingsDisable(boolean state) {
        for (String name : settingsNames) {
            disableApps.put(name, state);
        }
        return 0;
    }

    public int setGalleryDisable(boolean state) {
        for (String name : galleryNames) {
            disableApps.put(name, state);
        }
        return 0;
    }

    public int setGmailDisable(boolean state) {
        for (String name : gmailNames) {
            disableApps.put(name, state);
        }
        return 0;
    }

    public int setGoogleMapDisable(boolean state) {
        for (String name : googleMapNames) {
            disableApps.put(name, state);
        }
        return 0;
    }
}
