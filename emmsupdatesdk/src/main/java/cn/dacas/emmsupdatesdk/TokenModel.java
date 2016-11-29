package cn.dacas.emmsupdatesdk;

import org.json.JSONObject;

/**
 * Created by Administrator on 2016-11-10.
 */

public class TokenModel {
    //@SerializedName("access_token")
    private String accessToken;
    //@SerializedName("refresh_token")
    private String refreshToken;

    public TokenModel(String accessToken,String refreshToken) {
        this.accessToken=accessToken;
        this.refreshToken=refreshToken;
    }

    public TokenModel(JSONObject jsonObject){
        //TODO parse JsonObject to TokenModel
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
