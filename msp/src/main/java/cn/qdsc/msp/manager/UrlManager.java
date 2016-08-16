package cn.qdsc.msp.manager;

/**
 * Created by Sun RX on 2016-8-16.
 * UrlManager to bulid Url with https/http and Ip address
 */

public class UrlManager {
    static int HTTPS = 0;
    static int HTTP = 1;
    static String[] protType= {"https://","http://"};
    static String getWebServerUrl(int type){
        if(type> protType.length-1)
            type = 0;

        return protType[type]+AddressManager.getAddrWebservice();
    }

    static String getWebServerUrl(){
        return protType[0]+AddressManager.getAddrWebservice();
    }

    static String getWebServerUrlWithAuthPort(){
        return protType[0]+AddressManager.getAddrWebservice();
    }
}
