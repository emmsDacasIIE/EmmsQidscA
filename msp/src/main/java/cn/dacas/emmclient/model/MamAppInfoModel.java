package cn.dacas.emmclient.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * 从MamAppInfoModel而来
 * Created by lenovo on 2015/11/30.
 */
public class MamAppInfoModel implements Comparable {

    public String id;  //从网络请求得到的值，做为唯一标识进行数据库的插入。

    @SerializedName("name")
    public String appName = "";

    @SerializedName("description")
    public String appDesc = "";

    @SerializedName("version_code")
    public int appVersionCode;

    @SerializedName("version_name")
    public String appVersion = "";

    @SerializedName("package_name")
    public String pkgName = "";

    public String file_name= "";

    @SerializedName("icon_path")
    public String iconUrl = "";

    @SerializedName("created_at")
    public String created_time = "";
    @SerializedName("updated_at")
    public String updated_time = "";

    public String type = "" ; //APK;WEB

    @SerializedName("url")
    public String url = "";

    public transient int  appType;   //企业应用、个人应用

    public transient int progress;

    public transient boolean isCanceled = false;

    public transient boolean sso = true;

    public MamAppInfoModel() {

    }


    @Override
    public int compareTo(@NonNull Object another) {
        MamAppInfoModel tgt = (MamAppInfoModel) another;
        if (type.equalsIgnoreCase(tgt.type)) {
            if (tgt.isApk())
                return pkgName.compareTo(tgt.pkgName);
            else
                return file_name.compareTo(tgt.file_name);
        }
        return type.compareTo(tgt.type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof  MamAppInfoModel) {
            MamAppInfoModel app = (MamAppInfoModel)obj;
            if (type.equalsIgnoreCase(app.type)) {
                if (app.isApk()) {
                    if(pkgName == null || app.pkgName == null)
                        return false;
                    return pkgName.equals(app.pkgName);
                }
                else {
                    if(file_name == null || app.file_name ==null)
                        return false;
                    return file_name.equals(app.file_name);
                }
            }
        }
        return false;
    }

    public boolean isApk() {
        return type.equalsIgnoreCase("APK");
    }

    public boolean isWeb() {
        return type.equalsIgnoreCase("WEB");
    }


    @Override
    public String toString() {
        return "MamAppInfoModel{" +
                "appName='" + appName + '\'' +
                ", appDesc='" + appDesc + '\'' +
                ", appVersionCode=" + appVersionCode +
                ", appVersion='" + appVersion + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", appType=" + appType +
                ", id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", isCancel='" + isCanceled + '\'' +
                '}';
    }



}
