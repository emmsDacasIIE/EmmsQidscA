package cn.qdsc.cipher;

import android.content.Context;

/**
 * Created by jzhou on 2015-11-26.
 */
public abstract class QdCipher {

    protected Context mContext;
    public QdCipher(Context ctx)
    {
        mContext=ctx;
    }
    public abstract byte[] encrypt(byte[] key, byte[] plain) throws Exception;
    public abstract byte[] decrypt(byte[] key, byte[] cipher) throws Exception;
    public abstract byte[] getMasterKey();
    public abstract byte[] generateKey();
    public abstract  int getAlgorithm();
}
