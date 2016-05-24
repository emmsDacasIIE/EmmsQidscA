package cn.qdsc.msp.model;

import java.io.Serializable;

/**
 * Created by lenovo on 2016-1-21.
 */
public class MdmWifiModel implements Serializable  {
    private String ssid;
    private  String password;
    private  String encryptionType;

    public String getSsid() {
        return ssid;
    }

    public MdmWifiModel(String ssid,String password,String encryptionType) {
        this.ssid=ssid;
        this.password=password;
        this.encryptionType=encryptionType;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }
}
