package cn.dacas.emmclient.util;

import android.content.SharedPreferences;

import cn.dacas.emmclient.main.EmmClientApplication;

public class PrefUtils {
	//apps
	public static final String PREF_NAME = "PREF";
	public static final String BLACK_KEY = "BLACK";
	public static final String NECESSARY_KEY = "NECESSARY";
	
	//
	public static final String LAST_POLICY_KEY = "LASTPOLICY";
	
	public static final String DEVICE_BINDER = "DEVICEBINDER";
	public static final String BINDER_NAME = "BINDERNAME";
	public static final String DEVICE_REPORTED = "DEVICEREPORTED";

	public static final String CURRENT_ACCOUNT = "CURRENTACCOUNT";
	public static final String CURRENT_NAME = "CURRENTNAME";
	public static final String PASS_WORD = "PASSWORD";
	public static final String ACCESS_TOKEN = "ACCESSTOKEN";
	public static final String REFRESH_TOKEN = "REFRESHTOKEN";
	public static final String ACCESS_TOKEN_DEVICE = "ACCESSTOKENDEVICE";
	public static final String REFRESH_TOKEN_DEVICE = "REFRESHTOKENDEVICE";
	
	public static final String MSG_COUNT = "UNREADMSGCOUNT";

    public static final String ADDR_WEBSERVICE="WEBSERVICEADDRESS";
	
	
	//user: account & password 
	public static final String PASSWORD_SETTING_WORDS = "PASSWORDSETTINGWORDS";		//字母密码存放
	public static final String PASSWORD_SETTING_PATTERN = "PASSWORDSETTINGPATTERN";	//手势密码存放
	
	
	//device info
	public static final String DEVICE_INFO = "DEVICEINFO";
	public static final String DEVICE_TYPE = "DEVICETYPE";

    public static void setWebServiceAddress(String addr) {
        SharedPreferences settings = EmmClientApplication.getContext().getSharedPreferences(PrefUtils.PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ADDR_WEBSERVICE,addr);
        editor.commit();
    }

    public static String getWebserviceAddress() {
        SharedPreferences settings = EmmClientApplication.getContext().getSharedPreferences(PrefUtils.PREF_NAME, 0);
        return settings.getString(ADDR_WEBSERVICE,null);
    }
}
