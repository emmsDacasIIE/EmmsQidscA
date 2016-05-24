package cn.dacas.emmclient.main;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.dacas.emmclient.db.EmmClientDb;
import cn.dacas.emmclient.mdm.MDMService;
import cn.dacas.emmclient.security.ssl.SslHttpStack;
import cn.dacas.emmclient.util.NetworkDef;
import cn.dacas.emmclient.worker.PhoneInfoExtractor;
import cn.dacas.emmclient.worker.PolicyManager;

public class EmmClientApplication extends Application {

	public static ActivateDevice mActivateDevice = null;
	public static  CheckAccount mCheckAccount = null;
	public static PhoneInfoExtractor mPhoneInfo=null;
	public static EmmClientDb mDb=null;
	private PolicyManager mPolicyManager;
	ArrayList<String> packagNameList;
	private MyReceiver receiver;
	private static Context mContext;
	public static RequestQueue mVolleyQueue;
	public static int intervals=0;
    public static boolean runningBackground=false;
    public static final int LockSecs=60*5;

	@Override
	public void onCreate() {
		super.onCreate();
		mActivateDevice = ActivateDevice
				.getActivateDeviceInstance(EmmClientApplication.this);
		mCheckAccount = CheckAccount
				.getCheckAccountInstance(EmmClientApplication.this);
		mPhoneInfo=PhoneInfoExtractor.getPhoneInfoExtractor(EmmClientApplication.this);
        mDb=new EmmClientDb(EmmClientApplication.this);
        mDb.open();
		mPolicyManager = PolicyManager.getMPolicyManager(this);
		mContext=this.getApplicationContext();
		mVolleyQueue= Volley.newRequestQueue(EmmClientApplication.getContext(),new SslHttpStack(false));
		initImageLoader(mContext);
		initIpSettings(mContext);
		
		Intent intentMDM = new Intent(mContext, MDMService.class);
		this.startService(intentMDM);

		//安装EMMS-WPS
		initpackagNameList();
		// 监听系统新安装程序的广播
		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);// 注册广播机制
		filter.addDataScheme("package"); // 必须添加这项，否则拦截不到广播
		registerReceiver(receiver, filter);
		if (!detectApk("cn.wps.moffice_eng")) {
			// get the cacheDir.
			File fileDir = getFilesDir();
			final String cachePath = fileDir.getAbsolutePath()
					+ "/WPS.apk";
			retrieveApkFromAssets(this, "WPS.apk", cachePath);
			installApk(this, cachePath);
		}
	}

	private void initIpSettings(Context context) {
		NetworkDef.initIpSettings(context);
		
	}

	public ActivateDevice getActivateDevice() {
		return mActivateDevice;
	}

	public CheckAccount getCheckAccount() {
		return mCheckAccount;
	}

	public PolicyManager getPolicyManager() {
		return mPolicyManager;
	}

	public static Context getContext()
	{
		return mContext;
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024)
				// 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
	
	private void initpackagNameList() {
		// 初始化小模块列表
		packagNameList = new ArrayList<String>();
		PackageManager manager = this.getPackageManager();
		List<PackageInfo> pkgList = manager.getInstalledPackages(0);
		for (int i = 0; i < pkgList.size(); i++) {
			PackageInfo pI = pkgList.get(i);
			packagNameList.add(pI.packageName.toLowerCase());
		}
	}

	// 捆绑安装
	public boolean retrieveApkFromAssets(Context context, String fileName,
			String path) {
		boolean bRet = false;

		try {
			File file = new File(path);
			if (file.exists()) {
				return true;
			} else {
				file.createNewFile();
				InputStream is = context.getAssets().open(fileName);
				FileOutputStream fos = new FileOutputStream(file);

				byte[] temp = new byte[1024];
				int i = 0;
				while ((i = is.read(temp)) != -1) {
					fos.write(temp, 0, i);
				}
				fos.flush();
				fos.close();
				is.close();

				bRet = true;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return bRet;
	}

	/**
	 * 提示用户安装程序
	 */
	public void installApk(final Context context,final String filePath) {
		// 修改apk权限
		try {
			String command = "chmod " + "777" + " " + filePath;
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// install the apk.
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.parse("file://" + filePath),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	/**
	 * 检测是否已经安装
	 * 
	 * @param packageName
	 * @return true已安装 false未安装
	 */
	private boolean detectApk(String packageName) {
		return packagNameList.contains(packageName.toLowerCase());

	}

	private class MyReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
				String packName = intent.getDataString().substring(8);
				Log.e(intent.getDataString() + "====", packName);
				// packName为所安装的程序的包名
				packagNameList.add(packName.toLowerCase());

				// 删除file目录下的所有已安装的apk文件
				File file = getFilesDir();
				File[] files = file.listFiles();
				for (File f : files) {
					if (f.getName().endsWith(".apk")) {
						f.delete();
					}
				}
			}
		}
	}
}
