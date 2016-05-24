package cn.dacas.emmclient.worker;

import android.annotation.TargetApi;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.interception.ActivityStack;
import cn.dacas.emmclient.interception.DisableDlg;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.worker.ScreenObserver.ScreenStateListener;

public class DeviceAdminWorker {
	private static String TAG = DeviceAdminWorker.class.getSimpleName();
	private static DeviceAdminWorker mDeviceAdminWorker = null;

	// Interaction with the DevicePolicyManager
	private DevicePolicyManager mDPM = null;
	private ComponentName mDeviceAdminSample = null;
	private Context context = null;
	private static final long MS_PER_DAY = 86400 * 1000;
	private static final long MS_PER_SECOND = 1000;

	// 根据设备的API level判断设备是否支持Device Admin的接口
	public static final int API_LEVEL = android.os.Build.VERSION.SDK_INT;
	// 错误码定义
	public static final int SUCCESS = 0;
	public static final int ERROR_UNACTIVATED = -5;
	public static final int ERROR_PASSWD_QUALITY = -6;
	public static final int ERROR_UNSUPPORTED = -10;
	public static final int ERROR_FORMAT = -15; // 消息格式错误
	public static final int ERROR_PIN = -20;
	public static final int ERROR_ACCESS_FILE = -25;
	public static final int ERROR_PROTOCAL = -30; // 数据包格式错误
	public static final int ERROR_UNKNOWN = -35; // 未知错误
	public static final int ERROR_UNREGISTERED_DEVICE = -40; // 未注册的设备
	public static final int ERROR_CANNOT_CONNECT = -45; // 无法连接服务器
	public static final int ERROR_RCV_BUFF = -50; // 接收缓冲区太小
	public static final int ERROR_INFO_UNAVAILABLE = -55;

	private HashMap<String, Boolean> disableApps;
	private HashMap<String, Integer> verifyApps;
	private ArrayList<String> blackApps;
	private ArrayList<String> whiteApps;
	private boolean disableWifi, disableDataNetwork, disableBluetooth;
	private List<String> ssidWhiteList, ssidBlackList;

//	private int maxCount = 1000;
	private int maxCount = 5;
	private int scanDelay = 2000;
	private String[] googlePlayNames, youTubeNames, emailNames, browserNames,
			settingsNames, galleryNames, gmailNames, googleMapNames;
	DisableDlg disableDlg = null;
	Handler handler = new Handler();
	int counter = 0;

	private ScreenObserver mScreenObserver;

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			// 要做的事情
			Log.d(TAG,"start to scan");
			String topPkg = ActivityStack.getForegroundPkg(context);
			Boolean state = disableApps.get(topPkg);
			String me = context.getApplicationContext().getPackageName();
			if (state != null && state) {
				disableDlg.showDisableDlg();
			} else if (!topPkg.equals(me) && !isSystemApp(topPkg)
					&& whiteApps.size() > 0 && !whiteApps.contains(topPkg)) {
				disableDlg.showDisableDlg();
			} else if (blackApps.contains(topPkg)) {
				disableDlg.showDisableDlg();
			}
            EmmClientApplication.intervals+=2;
            if (EmmClientApplication.intervals>=EmmClientApplication.LockSecs)
                EmmClientApplication.intervals=EmmClientApplication.LockSecs;
            if (!topPkg.equals(me)) EmmClientApplication.runningBackground=true;
//			else {
//				Iterator<String> iter = verifyApps.keySet().iterator();
//				while (iter.hasNext()) {
//					String key = iter.next();
//					int count = verifyApps.get(key);
//					if (topPkg.equalsIgnoreCase(key)) {
//						boolean needVerify=true;
//						if (topPkg.equals(me)) {
//							CheckAccount account=EmmClientApplication.mCheckAccount;
//							if (account==null || account.getCurrentAccount()==null
//									|| EmmClientApplication.mDb.getPatternPassword((account.getCurrentAccount()))==null||
//									EmmClientApplication.mActivateDevice.getDeviceType().equals("COPE-PUBLIC"))
//								needVerify=false;
//						}
//						// 若空闲时间达到阈值，进行验证
//						if (needVerify && count >= maxCount) {
//							Log.d(TAG, "log for verify");
//							Intent intent = new Intent(context, UnlockActivity.class);
//                            intent.putExtra("key",key);
//							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            context.startActivity(intent);
//						} else {
//							// 若在时间内使用app，空闲时间归零
//							verifyApps.put(key, 0);
//						}
//					} else {
//						// 未使用app,空闲时间增加
//						verifyApps.put(key, ++count > maxCount ? maxCount
//								: count);
//					}
//				}
//			}
			handler.postDelayed(this, scanDelay);
		}
	};

	private boolean isSystemApp(String pkgName) {
		try {
			PackageManager manager = this.context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(pkgName, 0);
			return ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void startScanPolicy() {
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, scanDelay);
	}

	public void stopScanPolicy() {
		handler.removeCallbacks(runnable);
	}

	public void resetVerifyTime(String key) {
		verifyApps.put(key, 0);
	}

	public void showDisableToast() {
		Toast.makeText(context, "该功能已经被禁用", Toast.LENGTH_SHORT).show();
	}

	public static DeviceAdminWorker getDeviceAdminWorker(Context context) {
		if (mDeviceAdminWorker == null) {
			mDeviceAdminWorker = new DeviceAdminWorker(context);
		}
		return mDeviceAdminWorker;
	}

	private DeviceAdminWorker(Context context) {
		this.context = context;

		// Prepare to work with the DPM
		this.mDPM = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		this.mDeviceAdminSample = new ComponentName(context,
				DeviceAdminSampleReceiver.class);

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

		disableApps = new HashMap<String, Boolean>();
		verifyApps = new HashMap<String, Integer>();
		blackApps = new ArrayList<String>();
		whiteApps = new ArrayList<String>();
		verifyApps.put(context.getApplicationContext().getPackageName(), 0);
		// 初始化邮件验证功能
		for (String mail : emailNames) {
			verifyApps.put(mail, maxCount);
		}
		for (String mail : gmailNames) {
			verifyApps.put(mail, maxCount);
		}

		disableWifi = disableDataNetwork = disableBluetooth = false;
		ssidWhiteList = new ArrayList<String>();
		ssidBlackList = new ArrayList<String>();
		disableDlg = new DisableDlg(context);
		mScreenObserver = new ScreenObserver(context);
		mScreenObserver.setScreenStateListener(new ScreenStateListener() {

			@Override
			public void onScreenOn() {
			}

			@Override
			public void onScreenOff() {
			}

		});
	}

	private DeviceAdminWorker() {
		
	}

	public void setDisableWifi(boolean state) {
		if (state)
			setWifiState(false);
		this.disableWifi = state;
	}

	public void setDisableDataNetwork(boolean state) {
		if (state)
			setDataConnection(false);
		this.disableDataNetwork = state;
	}

	public void setDisableBluetooth(boolean state) {
		if (state)
			setBluetoothState(false);
		this.disableBluetooth = state;
	}

	public void setSsidWhiteList(List<String> list) {
		this.ssidWhiteList.clear();
		for (String s : list)
			this.ssidWhiteList.add(s);
	}

	public void setSsidBlackList(List<String> list) {
		this.ssidBlackList.clear();
		for (String s : list)
			this.ssidBlackList.add(s);
	}

	public int getBluettothState() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			return ERROR_UNSUPPORTED;
		}
		return bluetoothAdapter.getState();
	}

	// 打开、关闭蓝牙
	public int setBluetoothState(boolean enable) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			return ERROR_UNSUPPORTED;
		}
		if (enable) {
			bluetoothAdapter.enable();
		} else {
			bluetoothAdapter.disable();
		}
		return 0;
	}

	public int getWifiState() {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.getWifiState();
	}

	// 打开、关闭wifi
	public void setWifiState(boolean enable) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(enable);
	}

	public int getDataConnection() {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDataState();
	}

	// 打开、关闭数据连接
	public int setDataConnection(boolean enable) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		Class<?> cmClass = connManager.getClass();
		Class<?>[] argClasses = new Class[1];
		argClasses[0] = boolean.class;

		// 反射ConnectivityManager中hide的方法setMobileDataEnabled，可以开启和关闭GPRS网络
		Method method;
		try {
			method = cmClass.getMethod("setMobileDataEnabled", argClasses);
			method.invoke(connManager, enable);
			return 0;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ERROR_UNSUPPORTED;
	}

	// 配置wifi
	// ERROR_UNSUPPORTED说明无法配置ssid和密码，0表示配置成功
	public int configWifi(String ssid, String passwd,String type) {
		int typeInt;
		if(type.equals("WPA")){
			typeInt = 3;
		}
		else if (type.equals("WEP")){
			typeInt = 2;
		}
		else{
			typeInt = 1;
		}
		WifiAdmin wifiAdmin = new WifiAdmin(context);
		return wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(ssid, passwd, typeInt)) == -1 ? ERROR_UNSUPPORTED
				: 0;
	}

	// 设置静音
	public int setMute(boolean setMute) {
		if (setMute) {
			AudioManager audioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0); // mute
																			// music
																			// stream
			audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0); // mute
																			// ring
																			// stream
			if (Build.VERSION.SDK_INT >= 8) {
				audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
						AudioManager.AUDIOFOCUS_GAIN);
				audioManager.requestAudioFocus(null, AudioManager.STREAM_RING,
						AudioManager.AUDIOFOCUS_GAIN);
			}
		} else {
			AudioManager audioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			int maxMusic = audioManager
					.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			audioManager
					.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusic, 0); // mute
			// music
			// stream
			int maxRing = audioManager
					.getStreamMaxVolume(AudioManager.STREAM_RING);
			audioManager.setStreamVolume(AudioManager.STREAM_RING, maxRing, 0); // mute
			// ring
			// stream
			if (Build.VERSION.SDK_INT >= 8) {
				audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
						AudioManager.AUDIOFOCUS_GAIN);
				audioManager.requestAudioFocus(null, AudioManager.STREAM_RING,
						AudioManager.AUDIOFOCUS_GAIN);
			}
		}
		return 0;
	}

	public boolean isDeviceAdminActive() {
		return mDPM.isAdminActive(mDeviceAdminSample);
	}

	public boolean isPasswdSufficient() {
		if (isDeviceAdminActive()) {
			return mDPM.isActivePasswordSufficient();
		} else {
			activateDeviceAdmin();
			return false;
		}
	}

	// 重置设备的锁屏密码，考虑是否提醒用户（企业管理员设置了BYOD锁屏，以某种方式提醒用户；如果设备丢失，不能直接提示）
	public int resetPasswd(String newPasswd) {
		if (isDeviceAdminActive()) {
			boolean succeed = mDPM.resetPassword(newPasswd,
					DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
			if (succeed) {
				return 0;
			} else {
				return ERROR_PASSWD_QUALITY;
			}
		} else {
			// 激活时间与用户的操作有关，所以不等待，直接返回操作失败
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	public void activateDeviceAdmin() {
		// Launch the activity to have the user enable our admin.
		// 提醒用户激活设备管理员功能
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
				mDeviceAdminSample);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
				context.getString(R.string.add_admin_extra_app_text));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	// 开关相机， disableCamera = true表示关闭相机
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public int setCameraDisabled(boolean disableCamera) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 14) {
				return ERROR_UNSUPPORTED;
			}
			mDPM.setCameraDisabled(mDeviceAdminSample, disableCamera);
			return 0;
		} else {
			//
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	public boolean getCameraDisabled() {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 14) {
				return false;
			}
			return mDPM.getCameraDisabled(mDeviceAdminSample);
		} else {
			//
			activateDeviceAdmin();
			return false;
		}
	}

	/*
	 * valuea域为密码类型 DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED,
	 * DevicePolicyManager.PASSWORD_QUALITY_SOMETHING,
	 * DevicePolicyManager.PASSWORD_QUALITY_NUMERIC,
	 * DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC,
	 * DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC,
	 * DevicePolicyManager.PASSWORD_QUALITY_COMPLEX
	 */

	public int setPasswdQuality(int value) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 11) {
				return ERROR_UNSUPPORTED;
			}
			mDPM.setPasswordQuality(mDeviceAdminSample, value);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	public String getPasswordQuality() {
		if (isDeviceAdminActive()) {
			int quality = mDPM.getPasswordQuality(mDeviceAdminSample);
			switch (quality) {
			case DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK:
				return "图案";
			case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
				return "数字";
			case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
				return "字母";
			case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
				return "数字和字母";
			default:
				return "其它";
			}
		} else {
			activateDeviceAdmin();
			return "未知";
		}
	}

	public int setPasswordMinimumLength(int value) {
		if (isDeviceAdminActive()) {
			mDPM.setPasswordMinimumLength(mDeviceAdminSample, value);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	/*
	 * -1无法获取
	 */
	public int getPasswordMinimumLength() {
		if (isDeviceAdminActive()) {
			return mDPM.getPasswordMinimumLength(mDeviceAdminSample);
		} else {
			activateDeviceAdmin();
			return -1;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public int setPasswordMinimumLetters(int value) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 11) {
				return ERROR_UNSUPPORTED;
			}
			mDPM.setPasswordMinimumLetters(mDeviceAdminSample, value);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public int setPasswordMinimumNumeric(int value) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 11) {
				return ERROR_UNSUPPORTED;
			}
			mDPM.setPasswordMinimumNumeric(mDeviceAdminSample, value);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public int setPasswordMinimumLowerCase(int value) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 11) {
				return ERROR_UNSUPPORTED;
			}
			mDPM.setPasswordMinimumLowerCase(mDeviceAdminSample, value);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public int setPasswordMinimumUpperCase(int value) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 11) {
				return ERROR_UNSUPPORTED;
			}
			mDPM.setPasswordMinimumUpperCase(mDeviceAdminSample, value);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public int setPasswordMinimumSymbols(int value) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 11) {
				return ERROR_UNSUPPORTED;
			}
			mDPM.setPasswordMinimumSymbols(mDeviceAdminSample, value);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	/*
	 * -1无法获取
	 */
	public int getPasswordMinimumSymbols() {
		if (isDeviceAdminActive()) {
			return mDPM.getPasswordMinimumSymbols(mDeviceAdminSample);
		} else {
			activateDeviceAdmin();
			return -1;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public int setPasswordMinimumNonLetter(int value) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 11) {
				return ERROR_UNSUPPORTED;
			}
			mDPM.setPasswordMinimumNonLetter(mDeviceAdminSample, value);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public int setPasswordHistoryLength(int value) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 11) {
				return ERROR_UNSUPPORTED;
			}
			mDPM.setPasswordHistoryLength(mDeviceAdminSample, value);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	/*
	 * -1无法获取
	 */
	public int getPasswordHistoryLength() {
		if (isDeviceAdminActive()) {
			return mDPM.getPasswordHistoryLength(mDeviceAdminSample);
		} else {
			activateDeviceAdmin();
			return -1;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public int setPasswordExpirationTimeout(int value) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 11) {
				return ERROR_UNSUPPORTED;
			}
			mDPM.setPasswordExpirationTimeout(mDeviceAdminSample, value
					* MS_PER_DAY);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	/*
	 * -1无法获取
	 */
	public long getPasswordExpirationTimeout() {
		if (isDeviceAdminActive()) {
			long ret = mDPM.getPasswordExpirationTimeout(mDeviceAdminSample);
			return ret / MS_PER_DAY;
		} else {
			activateDeviceAdmin();
			return -1;
		}
	}

	public int setMaximumTimeToLock(int seconds) {
		if (isDeviceAdminActive()) {
			mDPM.setMaximumTimeToLock(mDeviceAdminSample, seconds
					* MS_PER_SECOND);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	/*
	 * -1表示未激活设备管理器
	 */
	public long getMaximumTimeToLock() {
		if (isDeviceAdminActive()) {
			return mDPM.getMaximumTimeToLock(mDeviceAdminSample)
					/ MS_PER_SECOND;
		} else {
			activateDeviceAdmin();
			return -1;
		}
	}

	public int setMaximumFailedPasswordsForWipe(int times) {
		if (isDeviceAdminActive()) {
			mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdminSample, times);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	/*
	 * -1表示管理器未激活
	 */
	public int getMaximumFailedPasswordsForWipe() {
		if (isDeviceAdminActive()) {
			return mDPM.getMaximumFailedPasswordsForWipe(mDeviceAdminSample);
		} else {
			activateDeviceAdmin();
			return -1;
		}
	}

	public int lockNow() {
		if (isDeviceAdminActive()) {
			mDPM.lockNow();
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public int wipeData(boolean withExternalStorage) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL >= 9) {
//				mDPM.wipeData(withExternalStorage ? DevicePolicyManager.WIPE_EXTERNAL_STORAGE
//						: 0);
				mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
			} else {
				// WIPE_EXTERNAL_STORAGE is not supported under API level 9
				mDPM.wipeData(0);
			}

			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public int setStorageEncryption(boolean willEncrypt) {
		if (isDeviceAdminActive()) {
			if (API_LEVEL < 11) {
				return ERROR_UNSUPPORTED;
			}
			mDPM.setStorageEncryption(mDeviceAdminSample, willEncrypt);
			return 0;
		} else {
			activateDeviceAdmin();
			return ERROR_UNACTIVATED;
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

	public int setBlackApps(List<String> blacks) {
		blackApps.clear();
		for (String name : blacks) {
			blackApps.add(name);
		}
		return 0;
	}

	public int setWhiteApps(List<String> whites) {
		whiteApps.clear();
		for (String name : whites) {
			whiteApps.add(name);
		}
		return 0;
	}

	public static class NetworkStateReceiver extends BroadcastReceiver {
		public static final String PREF_NAME = "APP_CAPA";
		public static final String DEVICE = "deviceType";

		@Override
		public void onReceive(Context context, Intent intent) {
			DeviceAdminWorker mDeviceAdminWorker = DeviceAdminWorker
					.getDeviceAdminWorker(context);
			String action = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)
					&& mDeviceAdminWorker.disableBluetooth) {
				String stateExtra = BluetoothAdapter.EXTRA_STATE;
				int state = intent.getIntExtra(stateExtra, -1);
				if (state == BluetoothAdapter.STATE_TURNING_ON
						|| state == BluetoothAdapter.STATE_ON) {
					mDeviceAdminWorker.setBluetoothState(false);
					mDeviceAdminWorker.showDisableToast();
				}
			}
			else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				ConnectivityManager connManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				// State state = connManager.getActiveNetworkInfo().getState();
				State stateWifi = connManager.getNetworkInfo(
						ConnectivityManager.TYPE_WIFI).getState();
				boolean openWifi = true, openMobile = true;
				if (State.CONNECTED == stateWifi
						|| State.CONNECTING == stateWifi) {
					if (mDeviceAdminWorker.disableWifi) { // 判断是否正在使用WIFI网络
						mDeviceAdminWorker.setWifiState(false);
						mDeviceAdminWorker.showDisableToast();
						openWifi = false;
					} else {
						PhoneInfoExtractor mPhoneInfoExtractor = PhoneInfoExtractor
								.getPhoneInfoExtractor(context);
						String ssid = mPhoneInfoExtractor.getWifiInfo().ssid;
						if (mDeviceAdminWorker.ssidWhiteList.size() > 0) {
							if (!mDeviceAdminWorker.ssidWhiteList
									.contains(ssid)) {
								mDeviceAdminWorker.setWifiState(false);
								openWifi = false;
								Toast.makeText(context, "Wifi不在白名单内",
										Toast.LENGTH_SHORT).show();
							}
						} else if (mDeviceAdminWorker.ssidBlackList
								.contains(ssid)) {
							mDeviceAdminWorker.setWifiState(false);
							openWifi = false;
							Toast.makeText(context, "Wifi在黑名单内",
									Toast.LENGTH_SHORT).show();
						}
					}
				}
				State stateMobile = connManager.getNetworkInfo(
						ConnectivityManager.TYPE_MOBILE).getState(); // 获取网络连接状态
				if (mDeviceAdminWorker.disableDataNetwork
						&& (State.CONNECTED == stateMobile || State.CONNECTING == stateMobile)) { // 判断是否正在使用GPRS网络
					mDeviceAdminWorker.setDataConnection(false);
					openMobile = false;
					mDeviceAdminWorker.showDisableToast();
				}
				// 重新上线后更新策略
				if ((openWifi && State.CONNECTED == stateWifi)
						|| (openMobile && State.CONNECTED == stateMobile)) {
					((EmmClientApplication) context.getApplicationContext())
							.getPolicyManager().updatePolicy();
					Log.d(TAG, "enforce online policy");
				}
			}

		}

	}

	/**
	 * Sample implementation of a DeviceAdminReceiver. Your controller must
	 * provide one, although you may or may not implement all of the methods
	 * shown here.
	 * 
	 * All callbacks are on the UI thread and your implementations should not
	 * engage in any blocking operations, including disk I/O.
	 */
	public static class DeviceAdminSampleReceiver extends DeviceAdminReceiver {
		void showToast(Context context, String msg) {
			String status = context.getString(R.string.admin_receiver_status,
					msg);
			Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onEnabled(Context context, Intent intent) {
			showToast(context,
					context.getString(R.string.admin_receiver_status_enabled));
		}

		@Override
		public CharSequence onDisableRequested(Context context, Intent intent) {
			return context
					.getString(R.string.admin_receiver_status_disable_warning);
		}

		@Override
		public void onDisabled(Context context, Intent intent) {
			showToast(context,
					context.getString(R.string.admin_receiver_status_disabled));
		}

		@Override
		public void onPasswordChanged(Context context, Intent intent) {
			showToast(
					context,
					context.getString(R.string.admin_receiver_status_pw_changed));
		}

		@Override
		public void onPasswordFailed(Context context, Intent intent) {
			showToast(context,
					context.getString(R.string.admin_receiver_status_pw_failed));
		}

		@Override
		public void onPasswordSucceeded(Context context, Intent intent) {
			showToast(
					context,
					context.getString(R.string.admin_receiver_status_pw_succeeded));
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onPasswordExpiring(Context context, Intent intent) {
			DevicePolicyManager dpm = (DevicePolicyManager) context
					.getSystemService(Context.DEVICE_POLICY_SERVICE);
			if (DeviceAdminWorker.API_LEVEL < 11) {
				return;
			}
			long expr = dpm.getPasswordExpiration(new ComponentName(context,
					DeviceAdminSampleReceiver.class));
			long delta = expr - System.currentTimeMillis();
			boolean expired = delta < 0L;
			String message = context
					.getString(expired ? R.string.expiration_status_past
							: R.string.expiration_status_future);
			showToast(context, message);
		}
	}
}
