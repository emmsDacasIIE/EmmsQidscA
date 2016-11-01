package cn.dacas.emmclient.webservice.download;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * 为了忽略ssl证书。
 * Created by lizhongyi on 2015/12/12.
 */
public class MyHostnameVerifier implements HostnameVerifier {


    @Override
    public boolean verify(String hostname, SSLSession session) {
        // TODO Auto-generated method stub
        return true;
    }
}
