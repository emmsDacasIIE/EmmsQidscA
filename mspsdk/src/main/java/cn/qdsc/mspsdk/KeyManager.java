package cn.qdsc.mspsdk;

import android.content.Context;
import android.util.Base64;

import org.json.JSONObject;

import cn.qdsc.cipher.QdAes128Cipher;
import cn.qdsc.cipher.QdAes256Cipher;
import cn.qdsc.cipher.QdCipher;
import cn.qdsc.cipher.QdSMCipher;
import cn.qdsc.utils.PrefUtils;

/**
 * Created by jzhou on 2015-11-26.
 */
public class KeyManager {

    private PrefUtils util=null;
    private static KeyManager mKeyManager=null;
    private QdCipher mCipher;
    private Context mContext;

    private KeyManager(Context ctx) {
        mContext=ctx;
        mCipher=new QdSMCipher(ctx);
        util=new PrefUtils(ctx);
    }

    public static KeyManager getInstance(Context ctx) {
        if (mKeyManager==null) {
            synchronized (KeyManager.class) {
                if (mKeyManager==null)
                    mKeyManager=new KeyManager(ctx);
            }
        }
        return mKeyManager;
    }

    public void setCipher(QdCipher cipher) {
        mCipher=cipher;
    }

    public QdKey getQdKey(int mode,String fileName) throws Exception {
        boolean newKey=true;
        if (containsQdKey(fileName)) {
            String info = util.getChaosKeyInfo(fileName);
            JSONObject obj = new JSONObject(info);
            int alg=obj.getInt("algorithm");
            if (mode== QdSecureContainer.Mode.Decrypt || alg==mCipher.getAlgorithm()) {
                newKey=false;
                QdKey keyInfo = new QdKey();
                keyInfo.setAlgorithm(alg);
                QdCipher cipher = null;
                if (alg == QdSecureContainer.Algorithm.AES_128) {
                    cipher = new QdAes128Cipher(mContext);
                } else if (alg == QdSecureContainer.Algorithm.AES_256) {
                    cipher = new QdAes256Cipher(mContext);
                } else if (alg == QdSecureContainer.Algorithm.SM4) {
                    cipher = new QdSMCipher(mContext);
                }
                byte[] keyEnc= Base64.decode(obj.getString("key"), Base64.NO_PADDING);
                byte[] key = cipher.decrypt(cipher.getMasterKey(), keyEnc);
                keyInfo.setKey(key);
                return keyInfo;
            }
        }
        if (newKey) {
            QdKey newKeyInfo=generateQdKey();
            setQdKey(fileName, newKeyInfo);
            return newKeyInfo;
        }
        return null;
    }

    public boolean deleteQdKey(String fileName) {
        if (containsQdKey(fileName)) {
            util.deleteChaosKeyInfo(fileName);
            return true;
        }
        else
            return false;
    }

    private QdKey generateQdKey() throws Exception {
        QdKey keyInfo=new QdKey();
        keyInfo.setAlgorithm(mCipher.getAlgorithm());
        keyInfo.setKey(mCipher.generateKey());
        return keyInfo;
    }

    public boolean containsQdKey(String fileName) {
        return util.containsKey(fileName);
    }

    private void setQdKey(String fileName,QdKey key) throws Exception {
        byte[] keyArray=key.getKey();
        byte[] keyEncrypted=mCipher.encrypt(mCipher.getMasterKey(),keyArray);
        String keyChaos="{\"algorithm\":" + key.getAlgorithm() + ", \"key\":\"" + Base64.encodeToString(keyEncrypted,Base64.NO_PADDING) + "\"}";
        util.setChaosKeyInfo(fileName, keyChaos);
    }

}
