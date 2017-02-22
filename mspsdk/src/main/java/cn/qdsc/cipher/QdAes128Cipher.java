package cn.qdsc.cipher;

import android.content.Context;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

    public byte[] getEncCode(byte[] byteE, String seed) {
        byte[] byteFina = null;
        Cipher cipher = null;

        try {
            SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(seed.getBytes()), "AES");

            cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            //cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            byteFina = cipher.doFinal(byteE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            cipher = null;
        }
        return byteFina;
    }

    public byte[] getDecCode(byte[] byteD, String seed) {
        byte[] byteFina = null;
        Cipher cipher = null;

        try {
            SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(seed.getBytes()), "AES");
            cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(
                    new byte[cipher.getBlockSize()]));
            byteFina = cipher.doFinal(byteD);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            cipher = null;
        }
        return byteFina;
    }

    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
                    16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    public String encrytString(String plain, String seed){
        return parseByte2HexStr(getEncCode(plain.getBytes(),seed));
    }

    public String decryptString(String encode, String seed){
        return new String(getEncCode(parseHexStr2Byte(encode),seed));
    }
}
