package cn.qdsc.msp.webservice.download;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.manager.AddressManager;
import cn.qdsc.msp.util.PrefUtils;
import cn.qdsc.mspsdk.QdSecureContainer;

/**
 * Created by lizhongyi on 2015/12/12.
 */
public class DownloadFileThread extends Thread {
    public static final String TAG = "DownloadFileThread";

//    private DownLoadFileFromUrl mDownLoadFileFromUrl;
    private static final int CacheSize = 1024*10;
    private Context mContext;

    Handler mDownloadHandler;

    /* 是否取消更新 */
    DownloadDataInfo info;

    public DownloadFileThread() {

    }


    public DownloadFileThread(Context ctx,DownloadDataInfo info,Handler handler) {
        mContext=ctx;
        this.info=info;
        this.mDownloadHandler=handler;
    }


    private void sendMessage(int status, int progress) {
        Message msg = new Message();
        msg.what = status;
        msg.arg1 = progress;
//        DownloadDataInfo downloadDataInfo = new DownloadDataInfo(DownloadFileStr,DownloadUrlStr);
//        msg.obj = downloadDataInfo;
        mDownloadHandler.sendMessage(msg);
    }

    @Override
    public void run() {
        int oldProgress=0;
        try {
            String finalUrl = "";
            String name=info.fileName;
            if (info.type == MyDownloadListener.Download_Type.Type_App) {
                finalUrl = AddressManager.getAddrFile(1)+"/"+ AddressManager.parseAddress(name);
            } else if (info.type == MyDownloadListener.Download_Type.Type_Doc) {
                finalUrl = AddressManager.getAddrFile(2)+"/"+ AddressManager.parseAddress(name);
            }
            String tempfullpath= QdSecureContainer.getInstance(EmmClientApplication.getContext()).getDirTempPath()+name;
            File file = new File(tempfullpath);
            if (file.exists()) file.delete();
            FileOutputStream fos = new FileOutputStream(file);
            long range=0;
//            long range=file.length();

//            SSLContext sc = SSLContext.getInstance("TLS");
//            sc.init(null, new TrustManager[]{new MyX509TrustManager()}, new SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
//            HttpsURLConnection conn = (HttpsURLConnection) new URL(mDownLoadFileFromUrl.url).openConnection();
            
//            String finalUrl=mDownLoadFileFromUrl.url+ URLEncoder.encode(mDownLoadFileFromUrl.fileName, "UTF-8");
            HttpURLConnection conn=(HttpURLConnection)new URL(finalUrl).openConnection();
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Range", String.valueOf(range));
            conn.connect();
            // 获取文件大小
            int length = conn.getContentLength();

//            if (length>0) {
//                //将大小通过msg写入到数据库中。
//                sendMessage(DownLoadFileFromUrl.SAVE_FILE_LENGTH, length);
//            }

            // 创建输入流
            InputStream is = conn.getInputStream();

//            int count = 0, progress = 0;

            int count = (int)range, progress = 0;

            long fileTotalSize = range + length;

            // 缓存
            byte buf[] = new byte[CacheSize];
            // 写入到文件中
            do {
                int numread = is.read(buf);
                count += numread;
                // 计算进度条位置
                if (fileTotalSize > 0) {
                    progress = (int)((count * 1.0 / fileTotalSize) * 100);
                } else {
                    progress = 0;
                }

                if (progress > 0 && progress>oldProgress) {
                    sendMessage(DownLoadFileFromUrl.DOWNLOADING, progress);
                    oldProgress=progress;
                }

                // 更新进度
                if (numread <= 0) {
                    // 下载完成
                    EmmClientApplication.mSecureContainer.encryptFromFile(name);
                    PrefUtils.addSecurityRecord("加密一条数据");
                    sendMessage(DownLoadFileFromUrl.DOWNLOAD_FINISH, 100);
                    break;
                }
                // 写入文件
                fos.write(buf, 0, numread);

//                    int len,numread=0;
//                    len = is.read(buf);
//                    if (len<=0) {
//                        EmmClientApplication.mSecureContainer.encryptFinal(null);
//                        break;
//                    }
//                    while (len>0 && numread<CacheSize) {
//                        numread=numread+len;
//                         if (numread<CacheSize) len=is.read(buf, numread, CacheSize - numread);
//                    }
//                    if (numread<CacheSize) {
//                        if (firstRead) {
//                            EmmClientApplication.mSecureContainer.encrypt(name, ConvertUtils.sub(buf, 0, numread));
//                        }
//                        else {
//                            EmmClientApplication.mSecureContainer.encryptFinal(ConvertUtils.sub(buf, 0, numread));
//                        }
//                        // 下载完成
//                        sendMessage(mDownLoadFileFromUrl.DOWNLOAD_FINISH, 100);
//                        break;
//                    }
//                    if (firstRead) {
//                        EmmClientApplication.mSecureContainer.encryptInit(name,buf);
//                        firstRead=false;
//                    }
//                    else
//                        EmmClientApplication.mSecureContainer.encryptAppend(buf);
//                    count += numread;
//                    // 计算进度条位置
//                    if (length > 0) {
//                        int progress = (int) (((float) count / length) * 100);
//                        // 更新进度
//                        sendMessage(mDownLoadFileFromUrl.DOWNLOADING, progress);
//                    }

            } while (!info.cancle);// 点击取消就停止下载.   //mDownLoadFileFromUrl.cancelDownload
            fos.close();
            is.close();
        } catch (ConnectException e) {
            e.printStackTrace();
            sendMessage(DownLoadFileFromUrl.DOWNLOAD_STOP, -1);
            return;
        }catch (FileNotFoundException e) {
            e.printStackTrace();
            sendMessage(DownLoadFileFromUrl.DOWNLOAD_STOP, -2);
            return;
        }
        catch (Exception e) {
            e.printStackTrace();
            sendMessage(DownLoadFileFromUrl.DOWNLOAD_STOP, -3);
            return;
        }
    }

//    private boolean isCancelDownload() {
//        if (DownLoadFileFromUrl.mCancelDownloadList.contains(DownloadFileStr)) {
//            return false;
//        }else {
//            return true;
//        }
//    }
}
