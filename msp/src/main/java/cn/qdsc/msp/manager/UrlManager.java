package cn.qdsc.msp.manager;

/**
 * Created by Sun RX on 2016-8-16.
 */

public class UrlManager {
    static int HTTPS = 0;
    static int HTTP = 1;
    static String[] protType= {"https://","http://"};
    static String getWebServeUrl(int type){
        if(type> protType.length-1)
            type = 0;

        return protType[type]+AddressManager.getAddrWebservice();
    }

    static String getWebServeUrl(){
        return protType[0]+AddressManager.getAddrWebservice();
    }
}
