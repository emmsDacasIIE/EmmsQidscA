package cn.dacas.emmclient.security;

public class EncryptApi {
	static{
		System.loadLibrary("encFile");
	}
	public static native int encFile(String plainFilePath, String cipherFilePath, String keyStr);
	public static native int decFile(String cipherFilePath, String plainFilePath, String keyStr);
}
