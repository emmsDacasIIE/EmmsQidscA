package cn.dacas.emmclient.worker;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.security.ssl.IgnoreCertTrustManager;

public class DownLoadAppFromUrl {
	/* 下载中 */
	private static final int DOWNLOAD = 1;
	/* 下载结束 */
	private static final int DOWNLOAD_FINISH = 2;
	/* 下载保存路径 */
	private String mSavePath;
	private String fileName;
	private String urlP;
	/* 记录进度条数量 */
	private int progress=0;
	/* 是否取消更新 */
	private boolean cancelDownload = false;

	private Context mContext;
	/* 更新进度条 */
	private ProgressBar mProgress;
	private TextView progressText;
	private Dialog mDownloadDialog;
		
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
				progressText.setText(progress+"%");
				break;
			case DOWNLOAD_FINISH:
				//更新数据库并安装文件
				installApk();
				break;
			default:
				break;
			}
		};
	};
	
	private void notifyDataChange(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		this.mContext.sendBroadcast(intent);
	}

	public DownLoadAppFromUrl(Context context)
	{
		this.mContext = context;
	}
	
	public void startDownload(String fileName, String urlP){
		// 显示提示对话框
		this.fileName = fileName;
		this.urlP = urlP;
		showNoticeDialog();
	}
	
	public static void startDownloadAppList(Context context, Map<String, String> nameUrl){
		Set<String> names = nameUrl.keySet();
		for(String name : names){
			new DownLoadAppFromUrl(context).startDownload(name, nameUrl.get(name));
		}
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
	 * 显示软件更新对话框
	 */
	private void showNoticeDialog()
	{
		// 构造对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.app_download_title);
		builder.setMessage(mContext.getString(R.string.app_download_info)+":"+fileName);
		// 更新
		builder.setPositiveButton(R.string.app_donwload_btn, new OnClickListener()
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
		builder.setNegativeButton(R.string.app_download_later, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		Dialog noticeDialog = builder.create();
		
		noticeDialog.getWindow().setType(
				(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		noticeDialog.show();
	}

	/**
	 * 显示软件下载对话框
	 */
	private void showDownloadDialog()
	{
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(mContext.getString(R.string.app_downloading)+":"+fileName);
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.appdownload_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.app_download_progress);
		progressText = (TextView) v.findViewById(R.id.progressText);
		builder.setView(v);
		// 取消更新
		builder.setNegativeButton(R.string.app_download_cancel, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				// 设置取消状态
				cancelDownload = true;
			}
		});
		mDownloadDialog = builder.create();
		mDownloadDialog.setCancelable(false);
		mDownloadDialog.getWindow().setType(
				(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
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

					SSLContext sc = SSLContext.getInstance("TLS");
					sc.init(null, new TrustManager[]{new IgnoreCertTrustManager()}, new SecureRandom());
					HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
					HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
					HttpsURLConnection conn = (HttpsURLConnection) new URL(urlP).openConnection();
                    conn.setRequestProperty("Content-Type", "application/octet-stream");
                    conn.setRequestProperty("Connection", "Keep-Alive");
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
					File apkFile = new File(mSavePath, fileName+".apk");
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
						if(length > 0){
							progress = (int) (((float) count / length) * 100);
						}else{
							progress=0;
						}
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0)
						{
							// 下载完成
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelDownload);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
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
		File apkfile = new File(mSavePath, fileName+".apk");
		if (!apkfile.exists())
		{
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(i);
	}

    private class MyHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub
            return true;
        }
    }
}


