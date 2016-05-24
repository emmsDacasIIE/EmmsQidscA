package cn.qdsc.cipher;

import android.content.Context;

import cn.qdsc.mspsdk.QdSecureContainer;

/**
 * Created by lenovo on 2015-12-14.
 */
public class QdAes128Cipher extends  QdAesCipher {
    public QdAes128Cipher(Context ctx) {
        super(ctx);
    }

    @Override
    public int getAlgorithm() {
        return QdSecureContainer.Algorithm.AES_128;
    }

    @Override
    public int getKeySize() {
        return 128;
    }
}
