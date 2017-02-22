package cn.dacas.emmclient.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import cn.dacas.emmclient.model.CheckAccount;
import cn.qdsc.cipher.QdAes128Cipher;

/**
 * Created by Sun RX on 2017-2-21.
 * get Url Scheme by app's packageName
 */

public class UrlSchemeFactory {
    static final String keyBytes = "abcdefgabcdefg12";
    private QdAes128Cipher mQdAes128Cipher;

    public UrlSchemeFactory(Context context){
        mQdAes128Cipher = new QdAes128Cipher(context);
    }

    private String getAppUrl(String pkgname, CheckAccount account){
        String url = "emm://"+pkgname+"/start.work?";
        return url+"appID="+ mQdAes128Cipher.encrytString("302a7d556175264c7e5b326827497349",keyBytes)
                +"&appSecret="+mQdAes128Cipher.encrytString("4770414c283a20347c7b553650425773",keyBytes)
                +"&username="+mQdAes128Cipher.encrytString(account.getCurrentAccount(),keyBytes)
                +"&password="+mQdAes128Cipher.encrytString(account.getCurrentPassword(),keyBytes);
    }

    public Intent getUrlSchemeIntent(String packageName,CheckAccount account){
        Uri data = Uri.parse(getAppUrl(packageName,account));
        Intent intent = new Intent(Intent.ACTION_MAIN,data);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
