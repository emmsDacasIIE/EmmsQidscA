package cn.qdsc.mspsdk;

import android.util.Base64;

/**
 * Created by jzhou on 2015-11-26.
 */
public class QdKey {

    private int algorithm;
    private String key;

    public int getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(int algorithm) {
        this.algorithm = algorithm;
    }

    public byte[] getKey() {
        return Base64.decode(key,Base64.NO_PADDING);
    }

    public void setKey(byte[] key) {
        this.key = Base64.encodeToString(key,Base64.NO_PADDING);
    }

    public void setKey(String key) {
        this.key=key;
    }
}
