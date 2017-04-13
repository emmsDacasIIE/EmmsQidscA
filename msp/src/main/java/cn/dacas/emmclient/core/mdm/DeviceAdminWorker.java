package cn.dacas.emmclient.core.mdm;

import android.annotation.TargetApi;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import cn.dacas.emmclient.util.QDLog;
import cn.dacas.emmclient.util.WifiAdmin;
import de.greenrobot.event.EventBus;

import static android.R.attr.enabled;


public class DeviceAdminWorker {
	private static String TAG = DeviceAdminWorker.class.getSimpleName();
	private static DeviceAdminWorker mDeviceAdminWorker = null;

	// Interaction with the DevicePolicyManager
	private DevicePolicyManager mDPM = null;
	private ComponentName mDeviceAdminSample = null;
	private Context mContext = null;
	private static final long MS_PER_DAY = 86400 * 1000;
	private static final long MS_PER_SECOND = 1000;

	// 根据设备的API level判断设备是否支持Device Admin的接口
	public static final int API_LEVEL = Build.VERSION.SDK_INT;
	// 错误码定义
	public static final int SUCCESS = 0;
	public static final int ERROR_UNACTIVATED = -5;
	public static final int ERROR_PASSWD_QUALITY = -6;
	public static final int ERROR_UNSUPPORTED = -10;

	private HashMap<String, Boolean> disableApps;



	public static DeviceAdminWorker getDeviceAdminWorker(Context mContext) {
		if (mDeviceAdminWorker == null) {
			mDeviceAdminWorker = new DeviceAdminWorker(mContext);
		}
		return mDeviceAdminWorker;
	}

	private DeviceAdminWorker(Context ctx) {
		this.mContext = ctx.getApplicationContext();
		// Prepare to work with the DPM
		this.mDPM = (DevicePolicyManager) mContext
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		this.mDeviceAdminSample = new ComponentName(mContext,
				DeviceAdminSampleReceiver.class);
	}

	//	@Override
	public void finalize() {
		try {
			super.finalize();
			EventBus.getDefault().unregister(this);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public int getBluetoothState() {
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
		android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.getWifiState();
	}

	// 打开、关闭wifi
	public void setWifiState(boolean enable) {
		android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(enable);
	}

	// 打开、关闭wifi
	public void disconnectWifi() {
		android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.getConnectionInfo().getNetworkId();
		WifiAdmin wifiAdmin = new WifiAdmin(mContext);
		wifiAdmin.disconnectWifi(wifiManager.getConnectionInfo().getNetworkId());
		//wifiManager.disconnect();
	}


	/**
	 * 得到数据连接状态
	 * @return
     */
	public int getDataConnection() {
		TelephonyManager telephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDataState();
	}

	// 打开、关闭数据连接
	// 关于禁止数据网络连接，这个功能在Android 5 以上版本中，只有系统APP才能调用，也就是所设备必须root权限才可以使用
	public int setDataConnection(boolean enable) {
		//关于禁止数据网络连接，这个功能在Android 5 以上版本中，只有系统APP才能调用，也就是所设备必须root权限才可以使用，下面是具体的解释
		// http://stackoverflow.com/questions/29340150/android-l-5-x-turn-on-off-mobile-data-programmatically
		try {
			TelephonyManager telephonyService = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
			if (null != setMobileDataEnabledMethod)
			{
				setMobileDataEnabledMethod.invoke(telephonyService, enable);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*ConnectivityManager connManager = (ConnectivityManager) mContext
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
		} catch (Exception e) {
			e.printStackTrace();
		}*/
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
		WifiManager wifiManager =(android.net.wifi.WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		QDLog.d("[WifiAdmin]",wifiManager.getConnectionInfo().getSSID()+"<>"+"\"" + ssid + "\"");
		if(wifiManager.getConnectionInfo().getSSID().equals("\"" + ssid + "\"")) {
			QDLog.d("[WifiAdmin]","Has been connecting Wifi:"+ssid);
			return 0;
		}
		WifiAdmin wifiAdmin = new WifiAdmin(mContext);
		return (wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(ssid, passwd, typeInt)) == -1)
				? ERROR_UNSUPPORTED : 0;
	}

	// 设置静音
	public int setMute(boolean setMute) {
		if (setMute) {
			AudioManager audioManager = (AudioManager) mContext
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
			AudioManager audioManager = (AudioManager) mContext
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
		//lizhongyi, 下面这个不应该注释掉
//		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//				context.getString(R.string.add_admin_extra_app_text));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
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

	// TODO: 2017-2-7  策略信息中并没有该项
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
//			String status = context.getString(R.string.admin_receiver_status,
//					msg);
//			Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onEnabled(Context context, Intent intent) {
//			showToast(context,
//					context.getString(R.string.admin_receiver_status_enabled));
		}

		@Override
		public CharSequence onDisableRequested(Context context, Intent intent) {
//			return context
//					.getString(R.string.admin_receiver_status_disable_warning);
			return "确定要取消激活设备吗";
		}

		@Override
		public void onDisabled(Context context, Intent intent) {
//			showToast(context,
//					context.getString(R.string.admin_receiver_status_disabled));
		}

		@Override
		public void onPasswordChanged(Context context, Intent intent) {
//			showToast(
//					context,
//					context.getString(R.string.admin_receiver_status_pw_changed));
		}

		@Override
		public void onPasswordFailed(Context context, Intent intent) {
//			showToast(context,
//					context.getString(R.string.admin_receiver_status_pw_failed));
		}

		@Override
		public void onPasswordSucceeded(Context context, Intent intent) {
//			showToast(
//					context,
//					context.getString(R.string.admin_receiver_status_pw_succeeded));
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
//			String message = context
//					.getString(expired ? R.string.expiration_status_past
//							: R.string.expiration_status_future);
//			showToast(context, message);
		}
	}
}
