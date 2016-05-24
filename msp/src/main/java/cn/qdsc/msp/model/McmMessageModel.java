package cn.qdsc.msp.model;

/**
 * Created by lenovo on 2015-12-7.
 */
public class McmMessageModel {

    public int id;
    public String title;
    public String content;
    public String created_at;

    public transient  int readed;
    public transient  boolean isCheck;
    public transient  boolean needShowCheck;

    public McmMessageModel()  {

    }

    public McmMessageModel(int id, String title,String content,String created_at,int readed, boolean needShowCheck) {
        this.title = title;
        this.id=id;
        this.content=content;
        this.created_at=created_at;
        this.readed = readed;
        this.needShowCheck = needShowCheck;
    }

}
