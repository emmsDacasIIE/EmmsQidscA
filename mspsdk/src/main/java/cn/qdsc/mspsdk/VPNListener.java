package cn.qdsc.mspsdk;

/**
 * Created by lenovo on 2016-1-29.
 */
public interface VPNListener {
    public void onLoginSuccess();
    public void onLoginFail();
    public void onLogout();
}
