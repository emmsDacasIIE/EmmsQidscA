package cn.qdsc.msp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lenovo on 2015-12-4.
 */
public class TokenModel {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("refresh_token")
    private String refreshToken;

    public TokenModel(String accessToken,String refreshToken) {
        this.accessToken=accessToken;
        this.refreshToken=refreshToken;
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
