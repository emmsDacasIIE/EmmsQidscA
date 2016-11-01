package cn.dacas.emmclient.model;

/**
 * Created by lenovo on 2015/11/30.
 */
public class LoginModel {
    public String access_token;
    public String refresh_token;
    public String owner_username; //用户名
    public String owner_name;     //责任人
    public String type;

    public void setAccess_token(String token) {
        access_token = token;
    }

    public void setRefresh_token(String refreshToken) {
        refresh_token = refreshToken;

    }

    @Override
    public String toString() {
        return "LoginModel{" +
                "access_token='" + access_token + '\'' +
                ", refresh_token='" + refresh_token + '\'' +
                ", owner_username='" + owner_username + '\'' +
                ", owner_name='" + owner_name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
