package cn.qdsc.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public final class ConvertUtils {
	private final static char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private ConvertUtils() {
	}
	
	public static String zeroString(int len) {
		StringBuilder sb=new StringBuilder();
		for (int i=0;i<len;i++)
			sb.append('0');
		return sb.toString();
	}
	
	public static byte[] zeroByteArray(int len) {
		byte[] z=new byte[len];
		Arrays.fill(z, (byte)0);
		return z;
	}
	
	public static byte[] toBytes(int a) {
		return new byte[] { (byte) (0x000000ff & (a >>> 24)),
				(byte) (0x000000ff & (a >>> 16)),
				(byte) (0x000000ff & (a >>> 8)), (byte) (0x000000ff & (a)) };
	}
	
	public static String xorString(String r1,String r2)
	{
		byte[] b1=toBytes(r1);
		byte[] b2=toBytes(r2);
		byte[] b=new byte[b1.length];
		for (int i=0;i<b1.length;i++)
			b[i]=(byte)(b1[i]^b2[i]);
		return toHexString(b);
	}
	
	public static byte[] xorByteArray(byte[] b1,byte[] b2)
	{
		byte[] b=new byte[b1.length];
		for (int i=0;i<b1.length;i++)
			b[i]=(byte)(b1[i]^b2[i]);
		return b;
	}
	
	public static byte[] toBytes(String str)
	{
		int len = str.length() / 2;
	    byte[] result = new byte[len];
	    int cnt = 0;
	    while (cnt < len)
	    {
	        String s=str.substring(cnt*2,cnt*2+2);
	    	result[cnt]=(byte) Integer.parseInt(s, 16);  
	        cnt++;
	    }
	    return result;
	}

	public static int toInt(byte[] b, int s, int n) {
		int ret = 0;

		final int e = s + n;
		for (int i = s; i < e; ++i) {
			ret <<= 8;
			ret |= b[i] & 0xFF;
		}
		return ret;
	}

	public static int toIntR(byte[] b, int s, int n) {
		int ret = 0;

		for (int i = s; (i >= 0 && n > 0); --i, --n) {
			ret <<= 8;
			ret |= b[i] & 0xFF;
		}
		return ret;
	}

	public static int toInt(byte... b) {
		int ret = 0;
		for (final byte a : b) {
			ret <<= 8;
			ret |= a & 0xFF;
		}
		return ret;
	}

	public static String toHexString(byte[] d, int s, int len) {
		final char[] ret = new char[len * 2];
		final int e = s + len;

		int x = 0;
		for (int i = s; i < e; ++i) {
			final byte v = d[i];
			ret[x++] = HEX[0x0F & (v >> 4)];
			ret[x++] = HEX[0x0F & v];
		}
		return new String(ret);
	}
	
	public static String toHexString(byte[] d)
	{
		return toHexString(d,0,d.length);
	}
	
	public static String toHexString(byte[] d, int offset)
	{
		return toHexString(d,offset,d.length-offset);
	}
	


	public static String toHexStringR(byte[] d, int s, int n) {
		final char[] ret = new char[n * 2];

		int x = 0;
		for (int i = s + n - 1; i >= s; --i) {
			final byte v = d[i];
			ret[x++] = HEX[0x0F & (v >> 4)];
			ret[x++] = HEX[0x0F & v];
		}
		return new String(ret);
	}

	public static int parseInt(String txt, int radix, int def) {
		int ret;
		try {
			ret = Integer.valueOf(txt, radix);
		} catch (Exception e) {
			ret = def;
		}

		return ret;
	}
	
	public static String toAmountString(float value) {
		return String.format("%.2f", value);
	}
	
	public static short ByteArrayToShort(byte[] buf,int offset) {
		short value = 0;
		for (int i = 0; i < 2; i++) {
			value = (short) (value << 8);
			value |= buf[1+offset-i] & 0xff;
		}
		return value;
	}
	
	public static short ByteArrayToShort(byte[] buf) {
		return ByteArrayToShort(buf,0);
	}
	
	public static byte[] ShortToByteArray(short r){
		byte[] buf=new byte[2];
		for (int i = 0; i < 2; i++) {
			buf[i] = (byte) (r >>> (i * 8));
		}
		return buf;
	}
	
	public static int ByteArrayToInt(byte[] buf,int offset) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			value = value << 8;
			value |= buf[3+offset-i] & 0xff;
		}
		return value;
	}
	
	public static int ByteArrayToInt(byte[] buf) {
		return ByteArrayToInt(buf,0);
	}
	
	public static byte[] IntToByteArray(int r) {
		byte[] buf=new byte[4];
		for (int i = 0; i < 4; i++) {
			buf[i] = (byte) (r >>> (i * 8));
		}
		return buf;
	}
	
	public static void IntToByteArray(int r, byte[] buf, int offset) {
		for (int i = 0; i < 4; i++) {
			buf[offset + i] = (byte) (r >>> (i * 8));
		}
	}
	
	public static long ByteArrayToLong(byte[] buf, int offset) {
		long value = 0;
		for (int i = 0; i < 8; i++) {
			value = value << 8;
			value |= buf[7+offset-i] & 0xff;
		}
		return value;
	}
	
	public static long ByteArrayToLong(byte[] buf) {
		return ByteArrayToLong(buf,0);
	}
	
	public static byte[] LongToByteArray(long r) {
		byte[] buf=new byte[8];
		for (int i = 0; i < 8; i++) {
			buf[i] = (byte) (r >>> (i * 8));
		}
		return buf;
	}
	
	public static void LongToByteArray(long r, byte[] buf, int offset) {
		for (int i = 0; i < 8; i++) {
			buf[offset + i] = (byte) (r >>> (i * 8));
		}
	}

	public static byte[] join(byte[] ...bs) {
		ByteArrayOutputStream bos=new ByteArrayOutputStream(); 
		byte[] buf=null;
		try {
			for (byte[] b:bs) {
					bos.write(b);
			}
			buf=bos.toByteArray();
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buf;
	}
	
	public static byte[] sub(byte[] source,int offset,final int len) {
		byte[] r=new byte[len];
		System.arraycopy(source, offset, r, 0, len);
		return r;
	}

}