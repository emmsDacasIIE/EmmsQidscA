package cn.qdsc.msp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.qdsc.msp.model.MamAppInfoModel;
import cn.qdsc.msp.model.TokenModel;
import cn.qdsc.msp.security.AESUtil;
import cn.qdsc.msp.webservice.QdParser;

/**
 * Util class for SharedPreferences
 */
public class PrefUtils {
	//apps
	public static final String PREF_NAME = "PREF";
	
	public static final String MSG_COUNT = "UNREADMSGCOUNT";


	
	
	//user: account & password 
	public static final String PASSWORD_SETTING_WORDS = "PASSWORDSETTINGWORDS";		//字母密码存放
	public static final String PASSWORD_SETTING_PATTERN = "PASSWORDSETTINGPATTERN";	//手势密码存放
	
	
	//device info
	public static final String DEVICE_INFO = "DEVICEINFO";
	public static final String DEVICE_TYPE = "DEVICETYPE";
	private static SharedPreferencesHelper spHelper=null;

	public static void Init(Context mContext) {
		spHelper=SharedPreferencesHelper.getInstance(mContext);
	}


	//------------------服务器地址-----------------------//
	private static final String ADDR_WEBSERVICE="WEBSERVICEADDRESS";
	private static final String ADDR_MSG="MSGADDRESS";
	private static final String ADDR_FORWARD="FORWARDADDRESS";
	private static final String ADDR_UPDATE="UPDATEADDRESS";

	static String[] addressList = {
			"192.168.151.137:8443", //ADDR_WEBSERVICE
			"192.168.151.175:3544",//ADDR_MSG
			"192.168.0.23:43546",//ADDR_FORWARD
			"192.168.151.137:8080" //ADDR_UPDATE
	};

    public static void setAddrWebservice(String addr) {
        spHelper.putString(ADDR_WEBSERVICE,addr);
    }

    public static String getAddrWebservice() {
        //return  spHelper.getString(ADDR_WEBSERVICE,"192.168.0.22:8443");
		return  spHelper.getString(ADDR_WEBSERVICE,addressList[0]);
    }

	public static void setAddrMsg(String addr) {
		spHelper.putString(ADDR_MSG,addr);
	}

	public static String getAddrMsg() {
		//return  spHelper.getString(ADDR_MSG,"192.168.0.23:43544");
		return  spHelper.getString(ADDR_MSG,addressList[1]);
	}

	public static void setAddrForward(String addr) {
		spHelper.putString(ADDR_FORWARD,addr);
	}

	public static String getAddrForward() {
		//return  spHelper.getString(ADDR_FORWARD,"192.168.0.23:43546");
		return  spHelper.getString(ADDR_FORWARD,addressList[2]);
	}


	public static void setAddrUpdate(String addr) {
		 spHelper.putString(ADDR_UPDATE,addr);
	}

	public static String getAddrUpdate() {
		//return spHelper.getString(ADDR_UPDATE, "192.168.0.22:8080");
		return spHelper.getString(ADDR_UPDATE, addressList[3]);
	}


	//--------------隐私设置配置--------------------
	public static final String HARDKEY = "allowHardInfo";
	public static final String SYSKEY = "allowSysInfo";
	public static final String LOCKEY = "allowLocationInfo";
	public static final String NETKEY = "allowNetInfo";
	public static final String APPKEY = "allAppInfo";

	public static boolean getHardPrivacy() {
		return spHelper.getBoolean(HARDKEY,true);
	}

	public static boolean getSysPrivacy() {
		return spHelper.getBoolean(SYSKEY,true);
	}

	public static boolean getLockPrivacy() {
		return spHelper.getBoolean(LOCKEY,true);
	}

	public static boolean getNetPrivacy() {
		return spHelper.getBoolean(NETKEY,true);
	}

	public static boolean getAppPrivacy() {
		return spHelper.getBoolean(APPKEY,true);
	}

	public static void putHardPrivacy(boolean value) {
		spHelper.putBoolean(HARDKEY,value);
	}

	public static void putSysPrivacy(boolean value) {
		spHelper.putBoolean(SYSKEY, value);
	}

	public static void putLockPrivacy(boolean value) {
		spHelper.putBoolean(LOCKEY,value);
	}

	public static void putNetPrivacy(boolean value) {
		spHelper.putBoolean(NETKEY,value);
	}

	public static void putAppPrivacy(boolean value) {
		spHelper.putBoolean(APPKEY,value);
	}

	//-------------加解密记录-------------------//
	public static final String SERCURITYRECORDS = "SecurityRecords";

	public static synchronized ArrayList<String> getSecurityRecords() {
		return spHelper.getStringList(SERCURITYRECORDS);
	}

	public static synchronized  void addSecurityRecord(String record) {
		ArrayList<String> list=getSecurityRecords();
		SimpleDateFormat formatter = new SimpleDateFormat ("HH:mm  ");
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		String str = formatter.format(curDate);
		list.add(str+record);
		if (list.size()>8) list.remove(0);
		spHelper.putStringList(SERCURITYRECORDS,list);
	}

	//------------存储appList----------------//
	public static final String APPLIST = "AppList";

	public static  void putAppList(ArrayList<MamAppInfoModel> list) {
		Gson gson = new Gson();
		String listStr = gson.toJson(list);
		spHelper.putString(APPLIST,listStr);
	}

	public static ArrayList<MamAppInfoModel> getApplist() {
		ArrayList<MamAppInfoModel> list=new ArrayList<>();
		String listStr= spHelper.getString(APPLIST, null);
		if (listStr!=null) {
			try {
				list=QdParser.parseAppList(new JSONArray(listStr));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	//消息
	public static int getMsgMaxId() {
		return spHelper.getInt("max_id",0);
	}

	public static void putMsgMaxId(int value) {
		spHelper.putInt("max_id",value);
	}

	public static int getMsgUnReadCount() {
		return  spHelper.getInt("unread_count",0);
	}

	public static  void putMsgUnReadCount(int value) {
		spHelper.putInt("unread_count",value);
	}

	//设备、用户相关

	public static final String DEVICE_BINDER = "DEVICEBINDER";
	public static final String BINDER_NAME = "BINDERNAME";
	public static final String DEVICE_REPORTED = "DEVICEREPORTED";

	public static final String CURRENT_ACCOUNT = "CURRENTACCOUNT";
	public static final String CURRENT_NAME = "CURRENTNAME";
	public static final String PASS_WORD = "PASSWORD";



	public static void putAdministrator(String deviceBinder) {
		if (deviceBinder==null) return;
		String chaos=AESUtil.encrypt(DEVICE_BINDER, deviceBinder);
		spHelper.putString(DEVICE_BINDER,chaos);
	}

	public static String getAdministrator() {
		String chaos = spHelper.getString(PrefUtils.DEVICE_BINDER, null);
		if (chaos==null) return null;
		return AESUtil.decrypt(DEVICE_BINDER, chaos);
	}

	//token相关
	private static final String ACCESS_TOKEN_DEVICE = "ACCESSTOKENDEVICE";
	private static final String REFRESH_TOKEN_DEVICE = "REFRESHTOKENDEVICE";
	public static final String ACCESS_TOKEN_USER = "ACCESSTOKEN";
	public static final String REFRESH_TOKEN_USER = "REFRESHTOKEN";

	public static void putDeviceToken(String accessToken,String refreshToken) {
		spHelper.putString(PrefUtils.ACCESS_TOKEN_DEVICE, accessToken == null ? null
				: AESUtil.encrypt(PrefUtils.ACCESS_TOKEN_DEVICE, accessToken));
		spHelper.putString(PrefUtils.REFRESH_TOKEN_DEVICE, refreshToken == null ? null
				: AESUtil.encrypt(PrefUtils.REFRESH_TOKEN_DEVICE, refreshToken));
	}

	public static void putDeviceToken(TokenModel token) {
		putDeviceToken(token.getAccessToken(),token.getRefreshToken());
	}

	public static TokenModel getDeviceToken() {
		String accessTokenChaos =spHelper.getString(PrefUtils.ACCESS_TOKEN_DEVICE,
				null);
		String refreshTokenChaos = spHelper.getString(PrefUtils.REFRESH_TOKEN_DEVICE,
				null);
		String accessToken = (accessTokenChaos == null) ? null : AESUtil.decrypt(
				PrefUtils.ACCESS_TOKEN_DEVICE, accessTokenChaos);
		String refreshToken = (refreshTokenChaos == null) ? null : AESUtil
				.decrypt(PrefUtils.REFRESH_TOKEN_DEVICE, refreshTokenChaos);
		return new TokenModel(accessToken,refreshToken);
	}

	public static void putUserToken(String accessToken,String refreshToken) {
		spHelper.putString(PrefUtils.ACCESS_TOKEN_USER, accessToken == null ? null
				: AESUtil.encrypt(PrefUtils.ACCESS_TOKEN_USER, accessToken));
		spHelper.putString(PrefUtils.REFRESH_TOKEN_USER, refreshToken == null ? null
				: AESUtil.encrypt(PrefUtils.REFRESH_TOKEN_USER, refreshToken));
	}

	public static void putUserToken(TokenModel token) {
		putUserToken(token.getAccessToken(), token.getRefreshToken());
	}

	public static TokenModel getUserToken() {
		String accessTokenChaos =spHelper.getString(PrefUtils.ACCESS_TOKEN_USER,
				null);
		String refreshTokenChaos = spHelper.getString(PrefUtils.REFRESH_TOKEN_USER,
				null);
		String accessToken = (accessTokenChaos == null) ? null : AESUtil.decrypt(
				PrefUtils.ACCESS_TOKEN_USER, accessTokenChaos);
		String refreshToken = (refreshTokenChaos == null) ? null : AESUtil
				.decrypt(PrefUtils.REFRESH_TOKEN_USER, refreshTokenChaos);
		return new TokenModel(accessToken,refreshToken);
	}

	//shared settings

	private static final String MSP_SHARE_SETTING = "MSPSHARESETTING";

	public static void putVpnSettings(Context mContext,String vpnSetting) {
		SharedPreferences share=mContext.getSharedPreferences(MSP_SHARE_SETTING, Context.MODE_WORLD_READABLE|Context.MODE_MULTI_PROCESS);
		SharedPreferences.Editor editor = share.edit();
		editor.putString("VPN",vpnSetting);
		editor.commit();
	}

	public static  String getVpnSettings(Context mContext) {
		SharedPreferences share=mContext.getSharedPreferences(MSP_SHARE_SETTING, Context.MODE_WORLD_READABLE|Context.MODE_MULTI_PROCESS);
		return share.getString("VPN",null);
	}

}
