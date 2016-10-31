package cn.qdsc.msp.manager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.qdsc.msp.util.PrefUtils;
import cn.qdsc.msp.util.QDLog;
import cn.qdsc.msp.webservice.qdvolley.UpdateTokenRequest;

/**
 * Created by Sun RX on 2016-8-16.
 * UrlManager to bulid Url with https/http and Ip address
 * 1. to set token Url and Web Server Url independently.
 * 2. to build MsgPushServer Url and RegisterMsgPush Url
 */

public class UrlManager {
    static String TAG = "BuildUrl";
    static String tokenIp = AddressManager.getAddrWebservice().split(":")[0];
    static String tokenPort = AddressManager.getAddrWebservice().split(":")[1];

    static String WebServicePath ="/api/v1";
    static String tokenPath = "/api/v1/oauth/token";
    static String regMsgPushPath = "/client/devices";
    public static String cmdServerPath = "/command/server";
    static String[] protocols = {"https://","http://","tcp://"};

    interface protocolType{
        int HTTPS = 0;
        int HTTP = 1;
        int TCP = 2;
    }

    public static String getWebSeviceUrl(int type){
        if(type> protocols.length-1)
            type = 0;
        return protocols[type]+AddressManager.getAddrWebservice()+WebServicePath;
    }

    public static String getWebServiceUrl(){
        return protocols[0]+AddressManager.getAddrWebservice()+WebServicePath;
    }

    public static String getWebServiceUrlWithPort(String port){
        return protocols[0]
                +AddressManager.getAddrWebservice().split(":")[0]
                +":"
                +port
                +WebServicePath;
    }

    public static String getTokenServiceUrl(){
        return protocols[0]+(tokenIp + ":" +tokenPort)+ tokenPath;
    }

    public static String getWebServicePath() {
        return WebServicePath;
    }

    public static String getTokenPath() {
        return tokenPath;
    }

    public static String BuildWebServiceUrl(String apiUrl, int type)
    {
        String baseUrl=getWebServiceUrl();
        String token=null;
        if (type== UpdateTokenRequest.TokenType.USER)
            token = PrefUtils.getUserToken().getAccessToken();
        else if (type== UpdateTokenRequest.TokenType.DEVICE)
            token = PrefUtils.getDeviceToken().getAccessToken();
        else token=null;
        if (token==null) return baseUrl+apiUrl;
        Pattern p = Pattern.compile("\\?\\S*=");
        Matcher m = p.matcher(apiUrl);
        if (m.find()) {
            QDLog.i(TAG,"Request========" + baseUrl+apiUrl+"&access_token="+token);
            return baseUrl+apiUrl+"&access_token="+token;
        }
        else {
            QDLog.i(TAG,"Request========" + baseUrl+apiUrl+"&access_token="+token);
            return baseUrl + apiUrl + "?access_token=" + token;
        }
    }

    public static String urWithToken(String url, int tokenType){
        String token;
        if (tokenType== UpdateTokenRequest.TokenType.USER)
            token = PrefUtils.getUserToken().getAccessToken();
        else if (tokenType== UpdateTokenRequest.TokenType.DEVICE)
            token = PrefUtils.getDeviceToken().getAccessToken();
        else token=null;
        if (token==null)
            return url;

        Pattern p = Pattern.compile("\\?\\S*=");
        Matcher m = p.matcher(url);
        if (m.find()) {
            QDLog.i(TAG,"Request========" + url+"&access_token="+token);
            return url+"&access_token="+token;
        }
        else {
            QDLog.i(TAG,"Request========" + url+"&access_token="+token);
            return url + "?access_token=" + token;
        }
    }

    public static String getMsgPushUrl(){
        return protocols[protocolType.TCP]+AddressManager.getAddrMsg();
    }

    public static String getRegMsgPushUrl(){
        return protocols[protocolType.HTTP]+AddressManager.getAddrRgMsg()+regMsgPushPath;
    }

    public static String getCmdServerUrl(){
        return protocols[protocolType.HTTPS]+AddressManager.getAddrCommandServer()+cmdServerPath;
    }
}
