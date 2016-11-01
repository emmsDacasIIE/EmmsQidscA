package cn.dacas.emmclient.model;

/**
 * 从MamAppInfoModel而来
 * Created by lenovo on 2015/11/30.
 */
public class McmDocInfoModel implements Comparable {
    public String fileId;
    public String fileName;
    public String fileRecvTime;
    public String isNative;
    public String url;
    public String path;

    public int fav; //0:未收藏； 1：收藏
    public int len; //文档长度

    /**
     * 为了UI显示
     * 0:全部
     * 1：已下载
     * 2：未下载
     * 3：已收藏，用fav来表示更好？
     */

    public int status;
    public int progress;

    public McmDocInfoModel() {

    }

    public McmDocInfoModel(String fileId, String fileName, String fileRecvTime,
                       String isNative, String url, String path,int fav,int len) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileRecvTime = fileRecvTime;
        this.isNative = isNative;
        this.url = url;
        this.path = path;
        this.fav = fav;
        this.len = len;

    }

    @Override
    public int compareTo(Object another) {
        // TODO Auto-generated method stub
        McmDocInfoModel tgt = (McmDocInfoModel) another;
        return this.fileRecvTime.compareTo(tgt.fileRecvTime);
    }
}

