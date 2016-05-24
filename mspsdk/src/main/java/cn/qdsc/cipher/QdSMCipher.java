package cn.qdsc.cipher;

import android.content.Context;

import cn.dacas.security.NativeCode;
import cn.qdsc.mspsdk.QdSecureContainer;
import cn.qdsc.utils.SecurityUtils;

/**
 * Created by lenovo on 2015-12-11.
 */
public class QdSMCipher extends   QdCipher {

    protected static byte[] masterKey=null;
    public QdSMCipher(Context ctx) {
        super(ctx);
    }

    @Override
    public byte[] encrypt(byte[] key, byte[] plain) throws Exception {
        byte[] c=new byte[plain.length];
        NativeCode.encrypt(key, plain, c);
        return c;
    }

    @Override
    public byte[] decrypt(byte[] key, byte[] cipher) throws Exception {
        byte[] p=new byte[cipher.length];
        NativeCode.decrypt(key,cipher,p);
        return p;
    }

    @Override
    public byte[] getMasterKey() {
        if (masterKey==null) {
            masterKey=new byte[16];
            String path="/data/data/"+mContext.getPackageName()+"/pdrf.0";
            NativeCode.getMasterKey(mContext, path,masterKey);
        }
        return  masterKey;
    }

    @Override
    public byte[] generateKey() {
        return  SecurityUtils.getRandomByteArray(16);
    }

    @Override
    public int getAlgorithm() {
        return QdSecureContainer.Algorithm.SM4;
    }
}
