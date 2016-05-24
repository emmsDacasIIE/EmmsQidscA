package cn.qdsc.msp.webservice.download;

/**
 * Created by lizhongyi on 2015/11/30.
 */
public interface MyDownloadListener {

    public DownloadDataInfo mDownloadDataInfo = null;

    public static enum Download_Status {
        FirstDownload, //首次下载
        Downloading,  //下载中
        Stop,  //下载异常中止
        Finished //下载正常结束
    }

    public static enum Download_Type {
        Type_App,
        Type_Doc
    }

    /**
     *
     * @param status
     * @param progress
     * @param dataInfo 正在下载的文件的信息.为了回调到UI,显示一些信息时用，暂时是doc用到
     */
    public void onDownloadStatus(Download_Status status,int progress,DownloadDataInfo dataInfo);

    public String getFileName();

}
