package cn.qdsc.msp.util;

import cn.qdsc.msp.manager.AddressManager;

/**
 * Created by Srx on 2016-8-15.
 */
public class BuildUrlUtils {

    static String proType="https://";

    static public String getWebAddrUrl(){
        return proType + AddressManager.getAddrWebservice();
    }
}
