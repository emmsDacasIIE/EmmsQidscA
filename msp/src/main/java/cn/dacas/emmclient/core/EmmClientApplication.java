package cn.dacas.emmclient.core;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
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

import cn.dacas.emmclient.core.mdm.MDMService;
import cn.dacas.emmclient.db.DatabaseEngine;
import cn.dacas.emmclient.model.ActivateDevice;
import cn.dacas.emmclient.model.CheckAccount;
import cn.dacas.emmclient.model.DeviceModel;
import cn.dacas.emmclient.model.UserModel;
import cn.dacas.emmclient.security.ssl.SslHttpStack;
import cn.dacas.emmclient.manager.AddressManager;
import cn.dacas.emmclient.ui.activity.loginbind.UserLoginActivity;
import cn.dacas.emmclient.util.PhoneInfoExtractor;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.QDLog;
import cn.dacas.emmclient.util.SdcardManager;
import cn.qdsc.mspsdk.QdSecureContainer;


public class EmmClientApplication extends Application {

	private static final String TAG = "EmmClientApplication";

	public static ActivateDevice mActivateDevice = null;
	public static CheckAccount mCheckAccount = null;
	public static PhoneInfoExtractor mPhoneInfoExtractor=null;
	public static DatabaseEngine mDatabaseEngine=null;
	//SDK
	public static QdSecureContainer mSecureContainer=null;

	ArrayList<String> packagNameList;
	private MyReceiver receiver;
	//private static Context mContext;
	public static RequestQueue mVolleyQueue;
	private JobManager jobManager;

	public static int intervals=0;  //使用时长
	public static int foregroundIntervals=0;  //前台无操作时长
    public static boolean runningBackground=false;
    public static final int LockSecs=60*5;

	public static UserModel mUserModel = null;
	public static DeviceModel mDeviceModel = null;
	public static boolean isFloating=false;

	public static String imei;

	public static EmmClientApplication instance;

	public EmmClientApplication(){
		instance = this;
	}

	static public EmmClientApplication getInstance(){
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = EmmClientApplication.this;
		PrefUtils.Init(EmmClientApplication.this);
		String json="{\"vpnGatewayAddress\": \"192.168.0.100:443\"}";
		imei = PhoneInfoExtractor.getIMEI(this);
		PrefUtils.putVpnSettings(EmmClientApplication.this,json);
		mActivateDevice = ActivateDevice
				.getActivateDeviceInstance(EmmClientApplication.this);
		mCheckAccount = CheckAccount
				.getCheckAccountInstance(EmmClientApplication.this);
		mPhoneInfoExtractor=PhoneInfoExtractor.getPhoneInfoExtractor(EmmClientApplication.this);
		mDatabaseEngine=new DatabaseEngine(EmmClientApplication.this);
		mDatabaseEngine.init();
        mSecureContainer=QdSecureContainer.getInstance(EmmClientApplication.this);

		mVolleyQueue= Volley.newRequestQueue(EmmClientApplication.getContext(),new SslHttpStack(false));
		initImageLoader(this);
		initIpSettings(this);
		
		Intent intentMDM = new Intent(this, MDMService.class);
		configureJobManager();
		this.startService(intentMDM);


//		UninstallMonitorFunc umFunc = new UninstallMonitorFunc(mContext);
//		String pkg = this.getPackageName();
//		umFunc.StartUninstallMonitor(urlStr, pkg);

		String logOUt = SdcardManager.getSdcardPath();
		QDLog.i(TAG, "onCreate========logOUt===========" + logOUt);
		QDLog.i(TAG,"onCreate===================");
		//getJobManager().addJobInBackground(new BasedMDMJobTask("JOB Test!"));

		/*PushMsgManager pushMsgManager = new PushMsgManager(this, UrlManager.getMsgPushUrl());
		try {
			//1. 注册APP，如果发现已经有reg_id则直接请求要关注的主题
			pushMsgManager.registerPush(
					UrlManager.getRegMsgPushUrl(),// Web adder
					"046e2930-7cc2-4398-9b1c-65852317de29",// client_id
					"6668b6a3-8486-4165-a418-374194ad47d3");// client_secret
			//pushMsgManager.addFullTopicToLists("AD",PushMsgManager.CommCodeType.NET_GetAliase);
			//pushMsgManager.addFullTopicToLists("AD",PushMsgManager.CommCodeType.NET_GetTopics);
			//pushMsgManager.addFullTopicToLists("AD",PushMsgManager.CommCodeType.NET_GetAccounts);
		}catch (Exception e)
		{
			e.printStackTrace();
		}*/
	}

	private void initIpSettings(Context context) {
		AddressManager.initIpSettings();
	}


	public static Context getContext()
	{
		//return mContext;
		return instance.getApplicationContext();
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

	private void configureJobManager() {
		Configuration configuration = new Configuration.Builder(this)
				.minConsumerCount(1)//always keep at least one consumer alive
				.maxConsumerCount(3)//up to 3 consumers at a time
				.loadFactor(3)//3 jobs per consumer
				.consumerKeepAlive(120)//wait 2 minute
				.build();
		jobManager = new JobManager(configuration);
	}

	public JobManager getJobManager(){
		if(jobManager == null)
			configureJobManager();
		return jobManager;
	}

	/**
	 * clear mCheckAccount and mUserModel
	 * @return Intent to UserLoginActivity
     */
	public static Intent getExitApplicationIntent(){
		mCheckAccount.clearCurrentAccount();
		mUserModel = null;
		Intent intent = new Intent(getContext(), UserLoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		return intent;
	}

	private class MyReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
				String packName = intent.getDataString().substring(8);
				QDLog.e(intent.getDataString() + "====", packName);
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
