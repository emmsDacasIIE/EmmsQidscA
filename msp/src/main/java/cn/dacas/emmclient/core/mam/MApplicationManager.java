package cn.dacas.emmclient.core.mam;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.RemoteException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MApplicationManager {
	private static MApplicationManager myActivityManager = null;

	private Context context = null;

	/**
	 * get an instance of Class ApplicationManager
	 * @param context Context
	 * @return an instance
     */
	public static MApplicationManager getApplicationManager(Context context) {
		if (myActivityManager == null) {
			myActivityManager = new MApplicationManager(context);
		}
		return myActivityManager;
	}

	/**
	 * Constructor:
	 * 1. getApplicationContext from context
	 * 2. call getAllInstalledPkgs() to get info about all Packages installed;
	 * @param context Context
     */
	private MApplicationManager(Context context) {
		this.context = context.getApplicationContext();
		getAllInstalledPkgs();
	}


	private PackageManager packageManager = null;
	private ActivityManager activityManager = null;
	private List<AppInfo> allPackages = null;
	private List<RunningServiceInfo> runningServices = null;

	/**
	 * set List<RunningServiceInfo> runningServices
	 */
	public void getRunningServices() {
		if (runningServices == null) {
			if (activityManager == null) {
				activityManager = (ActivityManager) context
						.getSystemService(Context.ACTIVITY_SERVICE);
			}
			runningServices = activityManager.getRunningServices(100);
		}
	}

	/**
	 * set [Field] allPackages
	 * @return a list of AppInfo, with content Info of all installed Packages
     */
	public List<AppInfo> getAllInstalledPkgs() {
		if (allPackages == null) {
			List<PackageInfo> tmpPackages;
			if (packageManager == null) {
				packageManager = context.getPackageManager();
			}
			tmpPackages = packageManager.getInstalledPackages(0);

			allPackages = new ArrayList<AppInfo>();

			boolean noSuchMethod = false;
			for (PackageInfo pkg : tmpPackages) {
				AppInfo app = new AppInfo();
				app.setPkgInfo(pkg);

				if (!noSuchMethod) {
					try {
						queryPacakgeSize(app);
					} catch (Exception e) {
						e.printStackTrace();
						noSuchMethod = true; //No such a method is called getPackageSizeInfo in Class PackageManager
					}
				}

				app.setAppLabel(packageManager.getApplicationLabel(
						app.getPkgInfo().applicationInfo).toString());
				app.setAppIcon(app.getPkgInfo().applicationInfo
						.loadIcon(packageManager));
				allPackages.add(app);
			}
		}
		return allPackages;
	}

	public List<AppInfo> getInstallUserPkgs() {
		List<AppInfo> allPackages = getAllInstalledPkgs();
		List<AppInfo> userPackages = new ArrayList<AppInfo>();
		for (AppInfo app : allPackages) {
			if ((app.getPkgInfo().applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				userPackages.add(app);
			}
		}
		return userPackages;
	}

	// 指定安装文件，安装应用,filePath为绝对路径
	public void installApp(String filePath) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(filePath)),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	public int uninstallAppList(String[] appNameArray) {
		// 这里要根据广播消息判断卸载是否成功！ 当前只是弹出卸载弹窗
		ArrayList<String> appNameList = new ArrayList<String>();
		for (int idx = 0; idx < appNameArray.length; idx++) {
			appNameList.add(appNameArray[idx]);
		}

		getAllInstalledPkgs();

		for (AppInfo app : allPackages) {
			if (appNameList.contains(packageManager.getApplicationLabel(app
					.getPkgInfo().applicationInfo))) {
				uninstallApp(app.getPkgInfo().packageName);
			}
		}
		return 0;
	}

	// 卸载应用
	public void uninstallApp(String packageName) {
		Uri uri = Uri.parse("package:" + packageName);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * if packageName is in the list allPackages, it will be removed;
	 * 每次开机过程，只运行一次读取安装应用列表的操作
	 */
	public void appRemoved(String packageName) {
		int location = -1;
		for (int idx = 0; idx < allPackages.size(); idx++) {
			if (allPackages.get(idx).equals(packageName)) {
				location = idx;
				break;
			}
		}
		if (location != -1) {
			allPackages.remove(location);
		}
	}

	/**
	 * add info about packageName into allPackages
	 * @param packageName
     */
	public void appAdded(String packageName) {
		// 在需要的时候会重新从系统读取安装应用列表
		if (packageManager == null) {
			packageManager = context.getPackageManager();
		}
		try {
			AppInfo app = new AppInfo();
			app.setPkgInfo(packageManager.getPackageInfo(packageName, 0));

			try {
				queryPacakgeSize(app);
			} catch (Exception e) {
				e.printStackTrace();
			}
			app.setAppLabel(packageManager.getApplicationLabel(
					app.getPkgInfo().applicationInfo).toString());
			app.setAppIcon(app.getPkgInfo().applicationInfo
					.loadIcon(packageManager));

			allPackages.add(app);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}



	protected void finalize() throws Throwable {
		super.finalize();
	}

	/**
	 * get Info about App Size
	 * In this method, the method getPackageSizeInfo in Class PackageManager will be invoked by Reflect!
	 * @param appInfo AppInfo
	 * @throws Exception
     */
	public void queryPacakgeSize(AppInfo appInfo) throws Exception {
		if ((appInfo == null) || (appInfo.getPkgInfo() == null)
				|| (appInfo.getPkgInfo().packageName == null)) {
			return;
		}

		if (packageManager == null) {
			packageManager = context.getPackageManager();
		}

		if (android.os.Build.VERSION.SDK_INT <= 16) {
			try {
				//lizhongyi
				Method getPackageSizeInfo = packageManager.getClass()
						.getDeclaredMethod("getPackageSizeInfo", String.class,
								IPackageStatsObserver.class);
				PkgSizeObserver pkgSizeObserver = new PkgSizeObserver();
				pkgSizeObserver.setAppInfo(appInfo);
				getPackageSizeInfo.invoke(packageManager,
						appInfo.getPkgInfo().packageName, pkgSizeObserver);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				//reflect the method getPackageSizeInfo in Class PackageManager
				Method getPackageSizeInfo = packageManager.getClass()
						.getDeclaredMethod("getPackageSizeInfo", String.class,
								int.class, IPackageStatsObserver.class);
				//a sub class of IPackageStatsObserver
				PkgSizeObserver pkgSizeObserver = new PkgSizeObserver();
				pkgSizeObserver.setAppInfo(appInfo);
				getPackageSizeInfo.invoke(packageManager,
						appInfo.getPkgInfo().packageName,//String
						android.os.Process.myUid() / 100000, //int
						pkgSizeObserver);// IPackageStatsObserver
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * to set appInfo about size, like cacheSize, codeSize, dataSize;
	 */
	public class PkgSizeObserver extends IPackageStatsObserver.Stub {
		private AppInfo appInfo = null;

		public void setAppInfo(AppInfo appInfo) {
			this.appInfo = appInfo;
		}

		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			if (appInfo != null) {
				appInfo.setCachesize(pStats.cacheSize); //B
				appInfo.setCodesize(pStats.codeSize);   //B
				appInfo.setDatasize(pStats.dataSize);   //B
			}
		}
	}

	//将当前安装的应用程序打包成JSON字符串，在MDMService的getDeviceInfoDetail上传到服务器

	/**
	 * make the list allPackages into JsonArray Format
	 * @return JsonArray
     */
	public String getAppsInJson() {
		JSONObject jsonApps = new JSONObject();
		JSONArray appArray = new JSONArray();

			getAllInstalledPkgs();

			for (int idx = 0; idx < allPackages.size(); idx++) {
				AppInfo appInfo = allPackages.get(idx);
				JSONObject app = new JSONObject();
				try {
					app.put("N", appInfo.getAppLabel()); // String name
					app.put("I", appInfo.getPackageName()); // String
					app.put("V", appInfo.getVersion()); // String
					app.put("AS", appInfo.getCodesize()); // long
					app.put("DS", appInfo.getDatasize()); // long
					app.put("T", appInfo.getType()); // int
					appArray.put(app);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		try {
			jsonApps.put("A", appArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonApps.toString();
	}

	public String getServicesInJson() {
		JSONObject jsonServices = new JSONObject();
		JSONArray serviceArray = new JSONArray();

			if (runningServices == null) {
				getRunningServices();
			}
			for (RunningServiceInfo service : runningServices) {
				JSONObject serviceObj = new JSONObject();
				try {
					ApplicationInfo appInfo = packageManager
							.getApplicationInfo(
									service.service.getPackageName(), 0);

					serviceObj.put("A",
							packageManager.getApplicationLabel(appInfo));// 应用名
					String[] shortServiceName = service.service
							.getShortClassName().split("\\.");
					serviceObj
							.put("S",
									(shortServiceName.length > 0) ? shortServiceName[shortServiceName.length - 1]
											: null); // 服务名
					serviceObj.put("I", service.uid); // uid
					serviceObj.put("M", 0); // 运行内存

					long activeLength = android.os.SystemClock
							.elapsedRealtime() - service.activeSince;
					long days = activeLength / (24 * 60 * 60 * 1000);
					activeLength %= 24 * 60 * 60 * 1000;
					long hours = activeLength / (60 * 60 * 1000);
					activeLength %= 60 * 60 * 1000;
					long minutes = activeLength / (60 * 1000);
					activeLength %= 60 * 1000;
					long seconds = activeLength / 1000;
					serviceObj.put("T", days + "天" + hours + "小时" + minutes
							+ "分" + seconds + "秒"); // 运行时间

					serviceArray.put(serviceObj);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		try {
			jsonServices.put("services", serviceArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonServices.toString();
	}

	public static class AppInfo {
		private PackageInfo pkgInfo;

		// 应用名
		// this.getPackageManager().getApplicationLabel(pkg.applicationInfo).toString();
		private String appLabel;

		// 包名
		// pkgInfo.packageName
		public String getPackageName() {
			return (pkgInfo == null) ? null : pkgInfo.packageName;
		}

		// 应用图标
		// pkgInfo.applicationInfo.loadIcon(pm)
		private Drawable appIcon;

		// 应用id
		// pkgInfo.applicationInfo.uid
		public int getAppUid() {
			return ((pkgInfo == null) || (pkgInfo.applicationInfo == null)) ? -1
					: pkgInfo.applicationInfo.uid;
		}

		// 版本
		// pkgInfo.versionName
		public String getVersion() {
			return (pkgInfo == null) ? null : pkgInfo.versionName;
		}

		// 应用大小
		private long codesize;

		// 数据大小
		private long datasize;

		// 缓存大小
		private long cachesize;

		// 安装类型
		// pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM
		public static final int APP_TYPE_SYSTEM = 0;
		public static final int APP_TYPE_THIRD = 1;
		public static final int APP_TYPE_UNKNOWN = -1;

		public int getType() {
			if ((pkgInfo == null) || (pkgInfo.applicationInfo == null)) {
				return APP_TYPE_UNKNOWN;
			}
			return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? APP_TYPE_SYSTEM
					: APP_TYPE_THIRD;
		}

		// 服务信息
		// pkgInfo.services
		public ServiceInfo[] getServices() {
			if (pkgInfo == null) {
				return null;
			}
			return pkgInfo.services;
		}

		public AppInfo() {
		}

		public PackageInfo getPkgInfo() {
			return pkgInfo;
		}

		public void setPkgInfo(PackageInfo pkgInfo) {
			this.pkgInfo = pkgInfo;
		}

		public String getAppLabel() {
			return appLabel;
		}

		public void setAppLabel(String appLabel) {
			this.appLabel = appLabel;
		}

		public Drawable getAppIcon() {
			return appIcon;
		}

		public void setAppIcon(Drawable appIcon) {
			this.appIcon = appIcon;
		}

		public long getCodesize() {
			return codesize;
		}

		public void setCodesize(long codesize) {
			this.codesize = codesize;
		}

		public long getDatasize() {
			return datasize;
		}

		public void setDatasize(long datasize) {
			this.datasize = datasize;
		}

		public long getCachesize() {
			return cachesize;
		}

		public void setCachesize(long cachesize) {
			this.cachesize = cachesize;
		}
	}
}
