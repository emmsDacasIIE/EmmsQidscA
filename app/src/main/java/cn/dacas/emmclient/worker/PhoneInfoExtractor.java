package cn.dacas.emmclient.worker;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import cn.dacas.emmclient.mdm.AppCapabilityDlg;
import cn.dacas.emmclient.util.QDLog;

/*
 * 静态的信息直接存放在类定义中，动态的数据或读写时间耗时的数据每次都要重新获取
 */
public class PhoneInfoExtractor {
	private static final String TAG = "PhoneInfoExtractor";
	public static final int API_LEVEL = android.os.Build.VERSION.SDK_INT;

	private static PhoneInfoExtractor phoneInfoExtractor = null;

	private Context context = null;

	private SharedPreferences settings = null;

	// IMEI号
	private String imei = null;

	// 生产商
	private String manufacturer = null;
	private TelephonyManager telephonyManager = null;

	// 设备型号
	private String deviceModel = null;

	// 操作系统类型
	private String osAndVersion = null;

	// 內核版本
	private String kernelVersion = null;

	// 基带版本
	private String baseband = null;

	// build版本
	private String buildVersion = null;

	private ActivityManager activityManager = null;

	// 电路卡识别码ICCID
	private String iccid = null;

	// 获取cpu核数目、cpu名称、cpu最大主频
	private int cpuCount = 0;
	private String cpuName = null;
	private long cpuMaxFreq = 0;

	// 内存信息
	private long totalMem = 0;
	private long externalTotalStorage = 0;

	// 屏幕分辨率
	private String display = null;
	// 屏幕尺寸
	private double displayInch = 0;

	// IMSI-国际移动用户识别码
	private String imsi = null;

	// 移动运营商
	private String networkOpetator = null;

	// sim卡运营商
	private String simOperator = null;

	// 国家
	private String country = null;

	// wifi信息
	public class WifiNetworkInfo {
		public String mac = null;
		public String lastConnected = null;
		public String ip = null;
		public String ssid = null;
	}

	// 两种更新方式，如果仍未null，读取当前连接的信息，其他方式通过广播填充
	private WifiNetworkInfo wifiNetworkInfo = null;

	public static PhoneInfoExtractor getPhoneInfoExtractor(Context ctxt) {
		if (phoneInfoExtractor == null) {
			phoneInfoExtractor = new PhoneInfoExtractor(ctxt);
		}
		return phoneInfoExtractor;
	}

	private PhoneInfoExtractor(Context ctxt) {
		context = ctxt;

		settings = context.getSharedPreferences(AppCapabilityDlg.PREF_NAME, 0);

		//监听wifi状态的变化，实时修改当前的SSID、最后连接时间等信息
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(mReceiver, mFilter);
	}

	public boolean getAppCapability(String key) {
		return true;
		//return settings.getBoolean(key, true);
	}
	
	public boolean getLocCapability() {
		return settings.getBoolean(AppCapabilityDlg.LOCKEY, false);
	}

	// 网络状态发生变化
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@SuppressLint("SimpleDateFormat")
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					ConnectivityManager.CONNECTIVITY_ACTION)) {
				setWifiInfo(null);

				ConnectivityManager conManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo network = conManager.getActiveNetworkInfo();
				if (network != null) {
					if (network.getTypeName().equals("WIFI")) {
						WifiNetworkInfo wifiNetworkInfo = new WifiNetworkInfo();

						WifiManager wifiManager = (WifiManager) context
								.getSystemService(Context.WIFI_SERVICE);
						WifiInfo wifiInfo = wifiManager.getConnectionInfo();

						// 如果当前没有wifi连接，wifiInfo也不会为null，它会是 <unknown ssid>等信息.
						int ip = wifiInfo.getIpAddress();
						wifiNetworkInfo.ip = "" + (ip & 0xFF) + "."
								+ ((ip >> 8) & 0xFF) + "."
								+ ((ip >> 16) & 0xFF) + "."
								+ ((ip >> 24) & 0xFF);

						SimpleDateFormat df = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");// 定义格式，不显示毫秒
						Timestamp now = new Timestamp(
								System.currentTimeMillis());// 获取系统当前时间
						wifiNetworkInfo.lastConnected = df.format(now);

						wifiNetworkInfo.mac = wifiInfo.getMacAddress();

						if (wifiInfo.getSSID().equals("<unknown ssid>")) {
							wifiNetworkInfo.ssid = "No Wifi Connected";
						} else {
							wifiNetworkInfo.ssid = wifiInfo.getSSID();
						}

						setWifiInfo(wifiNetworkInfo);
					}
				}
			}
		}
	};

	// 获取设备生产商
	public String getDeviceManufacturer() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return "隐私";
		}

		if (manufacturer == null) {
			manufacturer = Build.MANUFACTURER;
		}
		return manufacturer;
	}

	// 获取IMEI
	public String getIMEI() {
		if (imei == null) {
			if (telephonyManager == null) {
				telephonyManager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
			}
			imei = telephonyManager.getDeviceId();
			if (null==imei)
				imei = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);   
		}
		return imei;
	}

	// 获取设备型号
	public String getDeviceModel() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return "隐私";
		}

		if (deviceModel == null) {
			deviceModel = Build.MODEL;
		}
		return deviceModel;
	}

	// 获取系统信息
	public String getOsAndVersion() {
		if (osAndVersion == null) {
			osAndVersion = "android " + Build.VERSION.RELEASE;
		}
		return osAndVersion;
	}

	public String getOs() {
		if (!getAppCapability(AppCapabilityDlg.SYSKEY)) {
			return "隐私";
		}

		return "Android";
	}

	public String getOsVersion() {
		if (!getAppCapability(AppCapabilityDlg.SYSKEY)) {
			return "隐私";
		}

		return Build.VERSION.RELEASE;
	}

	// 接口版本
	public int getSDKInt() {
		if (!getAppCapability(AppCapabilityDlg.SYSKEY)) {
			return 0;
		}

		return android.os.Build.VERSION.SDK_INT;
	}

	// 内核版本
	public String getKernelVersion() {
		if (!getAppCapability(AppCapabilityDlg.SYSKEY)) {
			return "隐私";
		}
		if (kernelVersion == null) {
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream("/proc/version");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return kernelVersion;
			}

			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream), 8 * 1024);
			StringBuilder info = new StringBuilder("");
			String line = null;
			try {
				while ((line = bufferedReader.readLine()) != null) {
					info.append(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					bufferedReader.close();
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				if (!info.equals("")) {
					final String keyword = "version ";
					int index = info.indexOf(keyword);
					line = info.substring(index + keyword.length());
					index = line.indexOf(" ");
					kernelVersion = line.substring(0, index);
				}
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
		return kernelVersion;
	}

	// 基带版本
	@SuppressWarnings("rawtypes")
	public String getBaseband() {
		if (!getAppCapability(AppCapabilityDlg.SYSKEY)) {
			return "隐私";
		}

		if (baseband == null) {
			try {
				Class cl = Class.forName("android.os.SystemProperties");
				Object invoker = cl.newInstance();
				Method m = cl.getMethod("get", new Class[] { String.class,
						String.class });
				baseband = (String) m.invoke(invoker, new Object[] {
						"gsm.version.baseband", "no message" });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return baseband;
	}

	// build版本
	public String getBuildVersion() {
		if (!getAppCapability(AppCapabilityDlg.SYSKEY)) {
			return "隐私";
		}

		if (buildVersion == null) {
			buildVersion = android.os.Build.VERSION.CODENAME;
		}
		return buildVersion;
	}

	public String getPhoneNumber() {
		if (!getAppCapability(AppCapabilityDlg.NETKEY)) {
			return "隐私";
		}

		if (telephonyManager == null) {
			telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
		}
		String phoneNumber = telephonyManager.getLine1Number();
		if (phoneNumber == null || phoneNumber.equals("")) {
			return "无法获取";
		}
		return phoneNumber;
	}

	// 获取电路卡识别码
	public String getICCID() {
		if (!getAppCapability(AppCapabilityDlg.NETKEY)) {
			return "隐私";
		}

		if (iccid == null) {
			if (telephonyManager == null) {
				telephonyManager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
			}
			iccid = telephonyManager.getSimSerialNumber();
		}

		if (iccid == null) {
			return "未插入电路卡";
		} else {
			return iccid;
		}
	}

	// 查询设备是否在漫游,返回的是GSM状态
	public boolean isRoaming() {
		if (!getAppCapability(AppCapabilityDlg.NETKEY)) {
			return false;
		}

		if (telephonyManager == null) {
			telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
		}
		return telephonyManager.isNetworkRoaming();
	}

	// 查询设备是否root
	public boolean isRooted() {
		String[] filePath = new String[] { "/system/bin/su", "/sbin/su",
				"/system/xbin/su" };
		for (String suPath : filePath) {
			if (new File(suPath).exists()) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<String> getAccount() {
		ArrayList<String> accountNameList = new ArrayList<String>();
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return accountNameList;
		}

		Pattern emailPattern = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(context).getAccounts();
		for (Account account : accounts) {
			if (emailPattern.matcher(account.name).matches()) {
				accountNameList.add(account.name);
			}
		}
		return accountNameList;
	}

	// 查询CPU名字
	public String getCpuName() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return "隐私";
		}

		if (cpuName == null) {
			FileReader fr = null;
			BufferedReader br = null;
			try {
				fr = new FileReader("/proc/cpuinfo");
				br = new BufferedReader(fr);
				String text = br.readLine();
				String[] array = text.split(":\\s+", 2);
				if (array.length >= 2) {
					cpuName = array[1];
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fr != null)
					try {
						fr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				if (br != null)
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		return cpuName;
	}

	public int getCpuCoreNum() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return 0;
		}

		class CpuFilter implements FileFilter {
			public boolean accept(File pathname) {
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		if (cpuCount == 0) {
			try {
				File dir = new File("/sys/devices/system/cpu/");
				File[] files = dir.listFiles(new CpuFilter());
				cpuCount = files.length;
			} catch (Exception e) {
				cpuCount = 1;
			}
		}
		return cpuCount;
	}

	// 获取CPU的最大频率
	public long getCpuMaxFrequence() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return 0;
		}

		if (cpuMaxFreq == 0) {
			ProcessBuilder cmd;
			try {
				String[] args = { "/system/bin/cat",
						"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
				cmd = new ProcessBuilder(args);

				Process process = cmd.start();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				cpuMaxFreq = Long.parseLong(reader.readLine());
				reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return cpuMaxFreq;
	}

	// 获得剩余内存,KB
	public long getAvailMem() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return 0;
		}

		if (activityManager == null) {
			activityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
		}
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(mi);
		return mi.availMem / 1024;
	}

	// 获取总共内存
	public long getTotalMem() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return 0;
		}

		if (totalMem == 0) {
			if (API_LEVEL >= 16) {
				if (activityManager == null) {
					activityManager = (ActivityManager) context
							.getSystemService(Context.ACTIVITY_SERVICE);
				}
				ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
				activityManager.getMemoryInfo(mi);

				totalMem = mi.totalMem / 1024;
			} else {
				// 读取/proc/meminfo文件
				BufferedReader br;
				String text;
				try {
					br = new BufferedReader(new FileReader("/proc/meminfo"));
					text = br.readLine();
					String[] textArray = text.split("\\s+");
					totalMem = Integer.parseInt(textArray[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return totalMem;
	}

	// 获得运行内存
	public long getRuningMem() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return 0;
		}

		return getTotalMem() - getAvailMem();
	}

	// 获得总共外加内存（理解为sd卡外部存储）,MB
	@SuppressWarnings("deprecation")
	public long getExternalTotalStorage() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return 0;
		}
		if (externalTotalStorage == 0) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path.getPath());
				long blkSize = stat.getBlockSize();
				long blkCount = stat.getBlockCount();
				externalTotalStorage = (blkSize / 1024) * (blkCount / 1024);
			} else {
				externalTotalStorage = 0;
			}
		}
		return externalTotalStorage;
	}

	// 获取剩余外加内存（理解为sd卡外部存储）
	@SuppressWarnings("deprecation")
	public long getExternalAvail() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return 0;
		}

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			int blks = stat.getAvailableBlocks();
			int blkSize = stat.getBlockSize();
			return (blks / 1024) * (blkSize / 1024);
		} else {
			return 0;
		}
	}

	// 获取屏幕分辨率
	public String getDisplay() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return "隐私";
		}

		if (display == null) {
			DisplayMetrics dm = new DisplayMetrics();
			dm = context.getResources().getDisplayMetrics();
			display = dm.heightPixels + "x" + dm.widthPixels;
		}
		return display;
	}

	// 获取屏幕尺寸，这个计算结果不太准确
	public double getDisplayInch() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return 0;
		}

		if (displayInch == 0) {
			DisplayMetrics dm = new DisplayMetrics();
			dm = context.getResources().getDisplayMetrics();

			int width = dm.widthPixels;
			int height = dm.heightPixels;
			float xdpi = dm.xdpi;
			float ydpi = dm.ydpi;

			displayInch = Math.sqrt(Math.pow(width / xdpi, 2)
					+ Math.pow(height / ydpi, 2));

			BigDecimal big = new BigDecimal(displayInch);
			displayInch = big.setScale(1, RoundingMode.DOWN).doubleValue();
		}
		return displayInch;
	}

	// 中文返回zh
	public String getLanguage() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return "隐私";
		}
		String language = Locale.getDefault().getLanguage();
		if (language.equals("zh")) {
			return "中文";
		} else {
			return language;
		}
	}

	// 获取当前时区 格林尼治标准时间+0800 Asia/Shanghai
	public String getTimeZone() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return "隐私";
		}

		TimeZone tz = TimeZone.getDefault();
		String timezone = tz.getDisplayName(false, TimeZone.SHORT) + " "
				+ tz.getID();
		return timezone;
	}

	// 获取IMSI-国际移动用户识别码
	public String getIMSI() {
		if (!getAppCapability(AppCapabilityDlg.HARDKEY)) {
			return "隐私";
		}

		if (imsi == null) {
			if (telephonyManager == null) {
				telephonyManager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
			}
			// return null if not available
			imsi = telephonyManager.getSubscriberId();
		}
		return imsi;
	}

	// 获取运营商名称 如CHN-UNICOM
	// CTC中国电信
	public String getNetworkOperator() {
		if (!getAppCapability(AppCapabilityDlg.NETKEY)) {
			return "隐私";
		}

		if (networkOpetator == null) {
			if (telephonyManager == null) {
				telephonyManager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
			}
			networkOpetator = telephonyManager.getNetworkOperatorName();
		}
		
		if(networkOpetator == null || networkOpetator.equals("")){
			return "无法获取";
		}

		return networkOpetator;
	}

	public String getSimOperatorName() {
		if (!getAppCapability(AppCapabilityDlg.NETKEY)) {
			return "隐私";
		}

		if (simOperator == null) {
			if (telephonyManager == null) {
				telephonyManager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
			}
			simOperator = telephonyManager.getSimOperatorName();
		}
		
		if(simOperator == null || simOperator.equals("")){
			return "无法获取";
		}
		
		return simOperator;
	}

	// 获取国家 如 CN
	public String getCountry() {
		if (!getAppCapability(AppCapabilityDlg.NETKEY)) {
			return "隐私";
		}

		if (country == null) {
			country = Locale.getDefault().getCountry();
		}

		if (country.equals("CN")) {
			return "中国";
		}
		return country;
	}

	public boolean isConnected() {
		if (!getAppCapability(AppCapabilityDlg.NETKEY)) {
			return false;
		}

		boolean isConnected = false;
		ConnectivityManager conManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = conManager.getActiveNetworkInfo();
		if (network != null) {
			isConnected = network.isAvailable();
		}
		return isConnected;
	}

	// 获取当前网络类型，如WIFI、MOBILE等
	public String getNetworkType() {
		if (!getAppCapability(AppCapabilityDlg.NETKEY)) {
			return "隐私";
		}

		String type = null;
		ConnectivityManager conManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = conManager.getActiveNetworkInfo();
		if (network != null) {
			type = network.getTypeName();
		}
		return type;
	}

	@SuppressLint("SimpleDateFormat")
	public WifiNetworkInfo getWifiInfo() {
		if (!getAppCapability(AppCapabilityDlg.NETKEY)) {
			return new WifiNetworkInfo();
		}

		try {
			if (wifiNetworkInfo == null) {
				// 如果广播没有对它进行初始化，则直接从当前wifi连接中读取
				WifiManager wifiManager = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();

				if (wifiInfo == null) {
					return new WifiNetworkInfo();
				}

				// 如果当前没有wifi连接，wifiInfo也不会为null，它会是 <unknown ssid>等信息.
				wifiNetworkInfo = new WifiNetworkInfo();

				int ip = wifiInfo.getIpAddress();
				wifiNetworkInfo.ip = "" + (ip & 0xFF) + "."
						+ ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
						+ ((ip >> 24) & 0xFF);

				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");// 定义格式，不显示毫秒
				Timestamp now = new Timestamp(System.currentTimeMillis());// 获取系统当前时间
				wifiNetworkInfo.lastConnected = df.format(now);

				wifiNetworkInfo.mac = wifiInfo.getMacAddress();

				if ((wifiInfo.getSSID() == null)
						|| wifiInfo.getSSID().equals("<unknown ssid>")) {
					wifiNetworkInfo.ssid = "No Wifi Connected";
				} else {
					wifiNetworkInfo.ssid = wifiInfo.getSSID();
				}
			}
			return wifiNetworkInfo;
		} catch (Exception e) {
			e.printStackTrace();
			return new WifiNetworkInfo();
		}
	}

	public boolean isDataSyncOpen() {
		return ContentResolver.getMasterSyncAutomatically();
	}

	// 该方法为broadcastReceiver调用
	public void setWifiInfo(WifiNetworkInfo info) {
		wifiNetworkInfo = info;
	}

	public static int getWindowHeight(Context context) {
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int windowHeight = manager.getDefaultDisplay().getHeight();
		QDLog.i(TAG, "getWindowHeight=============w" + windowHeight);
		return  windowHeight;
	}

	public static int getWindowWidth(Context context) {
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int windowWidth = manager.getDefaultDisplay().getWidth();
		QDLog.i(TAG, "getWindowWidth=============w" + windowWidth);
		return windowWidth;
	}
}
