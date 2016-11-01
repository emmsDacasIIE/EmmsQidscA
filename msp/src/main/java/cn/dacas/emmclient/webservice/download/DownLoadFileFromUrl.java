package cn.dacas.emmclient.webservice.download;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.dacas.emmclient.util.QDLog;


public class DownLoadFileFromUrl {

    private static final String TAG = "DownLoadFileFromUrl";

    /* 下载中 */
    public  static final int DOWNLOADING = 1;
    /* 下载结束 */
    public static final int DOWNLOAD_FINISH = 2;

    /* 下载出现异常，中断 */
    public static final int DOWNLOAD_STOP = 3;

    /* 下载出现异常，中断 */
    public static final int SAVE_FILE_LENGTH = 99;

    /* 文档名称 */
//    String mFileNameStr;
//    String mUrlStr;

    /* 是否取消更新 */
    boolean cancelDownload = false;

    private Context mContext;


    ////add by lizhongyi
    MyDownloadListener mMyDownloadListener;

    public static List<MyDownloadListener> mDownloadListenerList = new ArrayList<>();

//    Map<String,Boolean> mCancelDownloadMap = new HashMap<>();
    public static List<DownloadFileThread> mCancelDownloadList = new ArrayList<>();
//    public static List<ProgressBarAsyncTask> mCancelDownloadList = new ArrayList<>();

//    DownloadDataInfo mDownloadDataInfo;

//    MyDownloadListener.Download_Type eDownload_Type;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            notifyUi(msg);

//            int progess = msg.arg1;
//            DownloadDataInfo downloadDataInfo = (DownloadDataInfo)msg.obj;
//            switch (msg.what) {
//                // 正在下载
//                case DOWNLOADING:
//                    // 设置进度条位置
////                    mProgress.setProgress(progress);
////                    progressText.setText(progress + "%");
//                    mMyDownloadListener.onDownloadStatus(MyDownloadListener.Download_Status.Downloading,progess,downloadDataInfo);
//                    break;
//                case DOWNLOAD_FINISH:
//
//                    mMyDownloadListener.onDownloadStatus(MyDownloadListener.Download_Status.Finished,progess,downloadDataInfo);
//                    // TODO 更新数据库并打开该文件
//                    break;
//                case DOWNLOAD_STOP: //中止下载
//
//                    mMyDownloadListener.onDownloadStatus(MyDownloadListener.Download_Status.Stop,progess,downloadDataInfo);
//                    break;

//                default:
//                    break;
//            }
        }
    };

    private void notifyUi(Message msg) {
        int progress = msg.arg1;
        DownloadDataInfo downloadDataInfo = (DownloadDataInfo)msg.obj;
        for (int i = 0; i< mDownloadListenerList.size();i++) {
            String fileName = mDownloadListenerList.get(i).getFileName();
            if (fileName.equals(downloadDataInfo.fileName)) {
                switch (msg.what) {
                    // 正在下载
                    case DOWNLOADING:
                        // 设置进度条位置
//                    mProgress.setProgress(progress);
//                    progressText.setText(progress + "%");
                        mDownloadListenerList.get(i).onDownloadStatus(MyDownloadListener.Download_Status.Downloading, progress, downloadDataInfo);
                        break;
                    case DOWNLOAD_FINISH:

                        mDownloadListenerList.get(i).onDownloadStatus(MyDownloadListener.Download_Status.Finished, progress, downloadDataInfo);
                        // TODO 更新数据库并打开该文件
                        break;
                    case DOWNLOAD_STOP: //中止下载
                        mDownloadListenerList.get(i).onDownloadStatus(MyDownloadListener.Download_Status.Stop, progress, downloadDataInfo);
                        break;
                    case SAVE_FILE_LENGTH:
                        mDownloadListenerList.get(i).onDownloadStatus(MyDownloadListener.Download_Status.FirstDownload, progress, downloadDataInfo);
                        break;
                    default:
                        break;
                }

            }
        }
    }

    //////构造函数
//    public DownLoadFileFromUrl(Context context, MyDownloadListener.Download_Type type,MyDownloadListener listener) {
//        this.mContext = context;
//        eDownload_Type = type;
//        mMyDownloadListener = listener;
//    }

    public DownLoadFileFromUrl(Context context) {
        this.mContext = context;
//        mMyDownloadListener = listener;
        if (mCancelDownloadList!=null){
            mCancelDownloadList.clear();
        }

    }

    public DownLoadFileFromUrl(Context context, MyDownloadListener listener) {
        this.mContext = context;
//        mMyDownloadListener = listener;
        if (mCancelDownloadList!=null){
            mCancelDownloadList.clear();
        }

    }

    public void startDownloadFile(MyDownloadListener.Download_Type type,String fileName,String url) {
//
        // 下载文件
        downloadFile(type,fileName,url);
    }

    public void startDownloadFile(MyDownloadListener.Download_Type type,String fileName,String url,MyDownloadListener listener) {
//        switch (eDownload_Type) {
//            case Type_App:
//            case Type_Doc:
////                this.mFileNameStr=fileName;
////                this.mUrlStr=url;
////                this.cancelDownload=false;
//                break;
//            default:
//                break;
//
//        }
//        mDownloadDataInfo = new DownloadDataInfo(this.fileName,url);

        // 下载文件
        downloadFile(type,fileName,url,listener);
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
     * 下载文件,用handler + thread
     */
    public void downloadFile(MyDownloadListener.Download_Type type, String fileName, String url) {
    }

    /**
     * 下载文件,用asynctatsk
     */
    public void downloadFile(MyDownloadListener.Download_Type type, String fileName, String url,MyDownloadListener listener) {
//        mMyDownloadListener = listener;

        boolean find = false;
        for (int i = 0; i<mDownloadListenerList.size();i++) {
            if (mDownloadListenerList.get(i).getFileName().equals(listener.getFileName())) {
                mDownloadListenerList.remove(i);
                mDownloadListenerList.add(listener);
                find = true;
                break;
            }
        }
        if (!find) {
            mDownloadListenerList.add(listener);
        }

        downloadFile(type, fileName, url);


        //asynctask
//        ProgressBarAsyncTask task = new ProgressBarAsyncTask(listener, fileName,type);
////        mCancelDownloadList.add(task);
//        task.execute();
    }


    ////////////handler + thread///////

    //停止全部下载线程
    public static void removeAllDownThread() {
        for (int i = 0; i< mCancelDownloadList.size();i++) {
            if (mCancelDownloadList.get(i).isAlive()) {
                QDLog.i(TAG, "removeAllDownThread============");
                mCancelDownloadList.get(i).interrupt();
                mCancelDownloadList.remove(i);
            }
        }
    }


    ///////////////////////////async task ///////
//    private void removeDownThread(String fileName) {
//        for (int i = 0; i< mCancelDownloadList.size();i++) {
//            if (mCancelDownloadList.get(i).getDownloadFileName().equals(fileName)) {
//                mCancelDownloadList.get(i).cancelByUi();
//                mCancelDownloadList.remove(i);
//                return;
//            }
//        }
//    }
//
//
//    public static boolean isRunningDownThread(String fileName) {
//        for (int i = 0; i< mCancelDownloadList.size();i++) {
//            if (mCancelDownloadList.get(i).getDownloadFileName().equals(fileName)
//                    && mCancelDownloadList.get(i).isRunning()) {
//                return true;
//            }
//        }
//        return false;
//
//    }


    public void setContext(Context context) {
        mContext = context;
    }

}