package cn.dacas.emmclient.model;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

/**
 * Created by lenovo on 2016-1-30.
 */
public class DeviceModel {
    private boolean status;
    private String name;//设备名称
    private String owner_account; //责任人账号

    @SerializedName("owner_username")
    private String owner_name;     //责任人姓名
    private String type;  //设备类型

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getOwner_account() {
        return owner_account;
    }

    public void setOwner_account(String owner_account) {
        this.owner_account = owner_account;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
