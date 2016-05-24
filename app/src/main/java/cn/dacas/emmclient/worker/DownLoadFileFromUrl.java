package cn.dacas.emmclient.worker;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.db.EmmClientDb;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.mcm.DocListFragment;
import cn.dacas.emmclient.mcm.FileOpener;
import cn.dacas.emmclient.security.EncryptApi;

public class DownLoadFileFromUrl {
    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    /* 下载保存路径 */
    private String mSavePath;
    /* 文档的附加说明 */
    private String fileTag;
    /* 文档名，包含完整后缀名 */
    private String fileName;
    private String fileNameWithTime;
    private String urlP;
    /* 记录进度条数量 */
    private int progress;
    /* 是否取消更新 */
    private boolean cancelDownload = false;

    private Context mContext;
    /* 更新进度条 */
    private ProgressBar mProgress;
    private TextView progressText;

    private Dialog mDownloadDialog;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
                    mProgress.setProgress(progress);
                    progressText.setText(progress + "%");
                    break;
                case DOWNLOAD_FINISH:
                    // TODO 更新数据库并打开该文件

                     String filePath = EncryptFile(mSavePath + "/" + fileNameWithTime, urlP);
                    EmmClientApplication.mDb.updateOrInsertItemByInfo(EmmClientDb.CORPFILE_DATABASE_TABLE, new String[]{"filetag"},
                            new String[]{fileName}, new String[]{"filetag", "isnative", "path"},
                            new String[]{fileName, "y", filePath});
                    // FileOpener opener = new FileOpener(mContext);
                    // opener.openFile(new File(mSavePath+"/"+fileName));

                    // openCipherFile(filePath, urlP);

                    notifyDataChange(DocListFragment.ACTION_REFRESH_DOC);
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public static void openCipherFile(Context ctx, String cipherPath, String key) {
        String sdpath = Environment.getExternalStorageDirectory() + "/";
        key = it.sauronsoftware.base64.Base64.encode(key);
        String tmpPath = sdpath + "tmp/" + cipherPath.substring(cipherPath.lastIndexOf("/") + 1);
        tmpPath = tmpPath.substring(0, tmpPath.lastIndexOf("("));
        File tmpFile = new File(tmpPath);
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
        EncryptApi.decFile(cipherPath, tmpPath, key);
        FileOpener opener = new FileOpener(ctx);
        opener.openFile(new File(tmpPath));
    }

    private void notifyDataChange(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        this.mContext.sendBroadcast(intent);
    }

    /*
     * 加密文件，返回密文文件路径
     */
    private String EncryptFile(String originalPath, String key) {
        String cipherPath = originalPath.substring(0, originalPath.lastIndexOf("/")) + "/1"
                + originalPath.substring(originalPath.lastIndexOf("/") + 1);
        key = it.sauronsoftware.base64.Base64.encode(key);
        EncryptApi.encFile(originalPath, cipherPath, key);
        File originalFile = new File(originalPath);

        if (originalFile.exists()) {
            originalFile.delete();
        }

        return cipherPath;
    }

    public DownLoadFileFromUrl(Context context) {
        this.mContext = context;
    }

    public void startDownload(String fileTag, String urlP) {
        // 显示提示对话框
        this.fileTag = fileTag;
        this.urlP = urlP;
        this.fileNameWithTime = fileTag;
        this.fileName = fileNameWithTime.substring(0, fileNameWithTime.lastIndexOf("("));
        showNoticeDialog();
    }

    public static void startDownloadFileList(Context context, Map<String, String> nameUrl) {
        Set<String> fileTags = nameUrl.keySet();
        for (String fileTag : fileTags) {
            new DownLoadFileFromUrl(context).startDownload(fileTag, nameUrl.get(fileTag));
        }
    }

    /*
     * 从url得到输入流
     */
    public InputStream getInputStreamFromUrl(String urlStr) throws MalformedURLException, IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        InputStream inputStream = urlConn.getInputStream();
        return inputStream;
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        // 构造对话框
        Builder builder = new Builder(mContext);
        builder.setTitle(R.string.file_download_title);
        builder.setMessage(mContext.getString(R.string.file_download_info) + ":" + fileName);
        // 更新
        builder.setPositiveButton(R.string.file_donwload_btn, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        // 稍后更新
        builder.setNegativeButton(R.string.file_download_later, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();

        noticeDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        noticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        // 构造软件下载对话框
        Builder builder = new Builder(mContext);
        builder.setTitle(mContext.getString(R.string.file_downloading) + ":" + fileTag);
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.appdownload_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.app_download_progress);
        progressText = (TextView) v.findViewById(R.id.progressText);
        builder.setView(v);
        // 取消更新
        builder.setNegativeButton(R.string.file_download_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 设置取消状态
                cancelDownload = true;
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.setCancelable(false);
        mDownloadDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        mDownloadDialog.show();
        // 现在文件
        downloadFile();
    }

    /**
     * 下载apk文件
     */
    private void downloadFile() {
        // 启动新线程下载软件
        new downloadFileThread().start();
    }

    /**
     * 下载文件线程
     */
    private class downloadFileThread extends Thread {
        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    mSavePath = sdpath + "download";
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
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
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, fileNameWithTime);
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        if (length > 0) {
                            progress = (int) (((float) count / length) * 100);
                        } else {
                            progress = 0;
                        }

                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
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

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                mDownloadDialog.dismiss();
                Looper.prepare();
                Toast.makeText(mContext, "无法连接文件服务器！", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (KeyManagementException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }


        private class MyHostnameVerifier implements HostnameVerifier {

            @Override
            public boolean verify(String hostname, SSLSession session) {
                // TODO Auto-generated method stub
                return true;
            }
        }

        private class MyTrustManager implements X509TrustManager {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // TODO Auto-generated method stub

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // TODO Auto-generated method stub

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                // TODO Auto-generated method stub
                return null;
            }
        }
    }

}