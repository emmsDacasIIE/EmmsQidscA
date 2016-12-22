package cn.dacas.emmclient.core.update;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.manager.AddressManager;
import cn.dacas.emmclient.manager.UrlManager;
import cn.dacas.emmclient.model.MamAppInfoModel;
import cn.dacas.emmclient.security.ssl.IgnoreCertTrustManager;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.QDLog;
import cn.dacas.emmclient.webservice.qdvolley.MyJsonObjectRequest;
import cn.dacas.emmclient.webservice.qdvolley.UpdateTokenRequest;
import de.greenrobot.event.EventBus;

import static android.R.attr.id;
import static com.baidu.location.h.i.A;


public class UpdateManager
{
	/* 下载中 */
	private static final int DOWNLOAD = 1;
	/* 下载结束 */
	private static final int DOWNLOAD_FINISH = 2;

	private static final int UPDATE_AVAILBLE = 3;

	private MamAppInfoModel appInfoModel;
	/* 下载保存路径 */
	private String mSavePath;
	/* 记录进度条数量 */
	private int progress;
	/* 是否取消更新 */
	private boolean cancelUpdate = false;

	private Context mContext;
	/* 更新进度条 */
	private ProgressBar mProgress;
	private Dialog mDownloadDialog;
	
	private boolean isUpdate = false;
	
	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			// 正在下载
			case DOWNLOAD:
				// 设置进度条位置
				mProgress.setProgress(progress);
				break;
			case DOWNLOAD_FINISH:
				// 安装文件
				installApk();
				break;
			case UPDATE_AVAILBLE:
				// 显示提示对话框
				showNoticeDialog();
				break;
			default:
				break;
			}
		}
	};

	public UpdateManager(Context context)
	{
		this.mContext = context;
	}

	/**
	 * 检测软件更新
	 */
	@Deprecated
	public void checkUpdate()
	{
		// 把version.xml放到网络上，然后获取文件信息
		Thread checkVersionThread = new Thread(new Runnable(){

			@Override
			public void run() {
				/*InputStream inStream;
				try {
					String ip = AddressManager.getAddrUpdate();
					if(ip == null)
						return ;
					// 创建连接
					String url = "http://"+ip+"/EMMS-WS/resource/client/android/version.xml";
					inStream = getInputStreamFromUrl(url);
					// 解析XML文件。 由于XML文件比较小，因此使用DOM方式进行解析
					ParseXmlService service = new ParseXmlService();
					try
					{
						mHashMap = service.parseXml(inStream);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					if (null != mHashMap)
					{
						String version = mHashMap.get("version");
						if(version != null){
							int serviceCode = Integer.valueOf(version);
							// 版本判断
							if (serviceCode > getVersionCode(mContext))
							{
								mHandler.sendEmptyMessage(UPDATE_AVAILBLE);
							}
						}
					}
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}*/
			}});
		checkVersionThread.start();
	}

	public void checkClientUpdate(){
		String url = UrlManager.getUpdateUrl()
				+"?access_token=" + PrefUtils.getDeviceToken().getAccessToken()
				+"&platform=android";
		JsonObjectRequest request = new JsonObjectRequest(
				Request.Method.GET,
				url,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						QDLog.d("update",response.toString());
						appInfoModel = new MamAppInfoModel();
						try {
							appInfoModel.pkgName = response.getString("package_name");
							appInfoModel.appName = response.getString("name");
							appInfoModel.url = response.getString("url");
							appInfoModel.appVersionCode = response.getInt("version_code");
							if(!appInfoModel.pkgName.equals(EmmClientApplication.getContext().getPackageName()))
								throw new Exception("pkgName isn't consistent!");
							if(appInfoModel.appVersionCode > getVersionCode(EmmClientApplication.getContext()))
								//EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_NO_UpdateApk));
								mHandler.sendEmptyMessage(UPDATE_AVAILBLE);
							else
								EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_NO_UpdateApk));
						} catch (Exception e) {
							QDLog.e("update",e.toString());
						}
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						QDLog.e("update",error.toString());
						EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_Error_UpdateApk));
					}
				}
		);

		EmmClientApplication.mVolleyQueue.add(request);
	}
	/*
	 * 从url得到输入流
	 */
	public InputStream getInputStreamFromUrl(String urlStr)
	        throws MalformedURLException, IOException {
	    URL url = new URL(urlStr);
	    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
	    InputStream inputStream = urlConn.getInputStream();
	    return inputStream;
	}



	/**
	 * 获取软件版本号
	 * 
	 * @param context
	 * @return
	 */
	private int getVersionCode(Context context)
	{
		int versionCode = 0;
		try
		{
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = context.getPackageManager().getPackageInfo("cn.dacas.emmclient", 0).versionCode;
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * 显示软件更新对话框
	 */
	private void showNoticeDialog()
	{
		// 构造对话框
		Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_update_title);
		builder.setMessage(R.string.soft_update_info);
		// 更新
		builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				// 显示下载对话框
				showDownloadDialog();
			}
		});
		// 稍后更新
		builder.setNegativeButton(R.string.soft_update_later, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

	/**
	 * 显示软件下载对话框
	 */
	private void showDownloadDialog()
	{
		// 构造软件下载对话框
		Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_updating);
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		// 取消更新
		builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				// 设置取消状态
				cancelUpdate = true;
			}
		});
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		// 现在文件
		downloadApk();
	}

	/**
	 * 下载apk文件
	 */
	private void downloadApk()
	{
		// 启动新线程下载软件
		new downloadApkThread().start();
	}

	/**
	 * 下载文件线程
	 * 
	 * @author coolszy
	 *@date 2012-4-26
	 *@blog http://blog.92coding.com
	 */
	private class downloadApkThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory() + "/";
					mSavePath = sdpath + "download";
					URL url = new URL(appInfoModel.url);
					// 创建连接
					IgnoreCertTrustManager.allowAllSSL();
					HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists())
					{
						file.mkdir();
					}
					File apkFile = new File(mSavePath, appInfoModel.appName);
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do
					{
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						if(length > 0)
							progress = (int) (((float) count / length) * 100);
						else
							progress ++;

						progress = progress % 99;
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0)
						{
							// 下载完成
							progress = 0;
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
		}
	};

	/**
	 * 安装APK文件
	 */
	private void installApk()
	{
		File apkfile = new File(mSavePath, appInfoModel.appName);
		if (!apkfile.exists())
		{
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		mContext.startActivity(i);
	}
}
