package cn.dacas.emmclient.webservice.download;

/**
 * Created by lizhongyi on 2015/12/16.
 */
public class DownloadDataInfo {

    public MyDownloadListener.Download_Type type;
    public String fileName;
    public String url;
    public boolean cancle;

    public DownloadDataInfo(MyDownloadListener.Download_Type type,String fileName,String url) {
        this.type=type;
        this.fileName = fileName;
        this.url = url;
        cancle=false;
    }
}
