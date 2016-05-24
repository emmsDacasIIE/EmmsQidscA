package cn.dacas.security;

import android.content.Context;

public class NativeCode {

    static {
        System.loadLibrary("qdmsp");
    }

    public static native int getMasterKey(Context ctx, String path, byte[] key);
    public static native int encrypt(byte[] key, byte[] plain, byte[] cipher);
    public static native int decrypt(byte[] key, byte[] cipher, byte[] plain);
}
