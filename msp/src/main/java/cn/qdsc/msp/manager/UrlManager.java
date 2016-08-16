package cn.qdsc.msp.manager;

/**
 * Created by Sun RX on 2016-8-16.
 * UrlManager to bulid Url with https/http and Ip address
 */

public class UrlManager {
    public static int HTTPS = 0;
    public static int HTTP = 1;
    static String authIp = AddressManager.getAddrWebservice().split(":")[0];
    static String authPort = AddressManager.getAddrWebservice().split(":")[1];
    static String WebServicePath ="";
    static String AuthPath = "";
    static String[] protType= {"https://","http://"};

    public static String getWebSeviceUrl(int type){
        if(type> protType.length-1)
            type = 0;
        return protType[type]+AddressManager.getAddrWebservice()+WebServicePath;
    }

    public static String getWebServiceUrl(){
        return protType[0]+AddressManager.getAddrWebservice()+WebServicePath;
    }

    public static String getWebServiceUrlWithAuthPort(){
        return protType[0]+(authIp+authPort)+AuthPath;
    }
}
