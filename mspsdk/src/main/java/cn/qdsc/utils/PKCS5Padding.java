package cn.qdsc.utils;

/**
 * Created by lenovo on 2015-12-14.
 */
public class PKCS5Padding {
    public static byte[] padding(byte[] source) {
        int len=source==null?16:16-(source.length%16);
        byte[] paddingArray=new byte[len];
        for (int i=0;i<len;i++)
            paddingArray[i]=(byte)len;
        if (source==null) return paddingArray;
        else return ConvertUtils.join(source,paddingArray);
    }

    public static byte[] unPadding(byte[] source) {
        byte len=source[source.length-1];
        return ConvertUtils.sub(source,0,source.length-len);
    }
}
