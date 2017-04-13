package cn.qdsc.cipher;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.qdsc.utils.PrefUtils;
import cn.qdsc.utils.SecurityUtils;

/**
 * Created by lenovo on 2015-11-26.
 */
public abstract  class QdAesCipher extends QdCipher {

    private static final String pin="12345678";
    protected static byte[] masterKey=null;

    public QdAesCipher(Context ctx) {
        super(ctx);
    }

    @Override
    public byte[] encrypt(byte[] key, byte[] plain) throws Exception{
        byte[] rawKey = getRawKey(key);
        byte[] result = doEncrypt(rawKey, plain);
        return result;
    }

    @Override
    public byte[] decrypt(byte[] key, byte[] cipher) throws Exception {
        byte[] rawKey = getRawKey(key);
        byte[] result = doDecrypt(rawKey, cipher);
        return result;
    }

    @Override
    public byte[] getMasterKey() {
        if (masterKey==null) {
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();
            if (null == imei)
                imei = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            PrefUtils util = new PrefUtils(mContext);
            String rnd = util.getRootKeyRandom();
            if (rnd == null) {
                rnd = SecurityUtils.getRandomString(32);
                util.setRootKeyRandom(rnd);
            }
            masterKey= (imei + pin + rnd).getBytes();
        }
        return masterKey;
    }

    @Override
    public byte[] generateKey() {
        return  SecurityUtils.getRandomByteArray(32);
    }

    @Override
    public abstract  int getAlgorithm();

    public abstract  int getKeySize();

    protected   byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        // SHA1PRNG 强随机种子算法, 要区别4.2以上版本的调用方法
        SecureRandom sr = null;
        if (android.os.Build.VERSION.SDK_INT >=  17) {
            sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        } else {
            sr = SecureRandom.getInstance("SHA1PRNG");
        }
        sr.setSeed(seed);
        kgen.init(getKeySize(), sr); //256 bits or 128 bits,192bits
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }




    private byte[] doEncrypt(byte[] key, byte[] data) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec,new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        byte[] encrypted = cipher.doFinal(data);
        return encrypted;
    }

    private  byte[] doDecrypt(byte[] key, byte[] data) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec,new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        byte[] decrypted = cipher.doFinal(data);
        return decrypted;
    }

}
