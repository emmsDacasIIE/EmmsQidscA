package cn.qdsc.utils;

import java.util.Random;

/**
 * Created by lenovo on 2015-11-27.
 */
public class SecurityUtils {

    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();

        for(int i = 0 ; i < length; ++i){
            int number = random.nextInt(62);//[0,62)
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static byte[] getRandomByteArray(int length) {
        byte[] result=new byte[length];
        Random random = new Random();
        random.nextBytes(result);
        return result;
    }
}
