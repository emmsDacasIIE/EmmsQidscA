package cn.dacas.emmclient.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cn.dacas.emmclient.util.PrefUtils;

public class AddressManager {

	// 安全接入服务器地址
	private static String ADDR_FORWARD;

	// 消息推送服务器地址
	private static String ADDR_MSG;
	private static String ADDR_RG_MSG;

	// webservice服务器地址
	private static String ADDR_WEBSERVICE ;

	// baidu 地图
	private static String ADDR_BDMAP;

	private static String ADDR_UPDATE;

	/**
	 *
	 * @param type 1: app; 2:doc 3:appicon
	 * @return
	 */
	public static String getAddrFile(int type) {
		String path = UrlManager.getWebServicePath();
		if (type == 1) {
			return "http://"+ADDR_WEBSERVICE.split(":")[0]+":8085"+path+"/user/apps";
		}else if (type == 2) {
			return "http://"+ADDR_WEBSERVICE.split(":")[0]+":8085"+path+"/user/docs";
		}else if (type == 3) {
			return "https://"+ADDR_WEBSERVICE.split(":")[0]+":8443"+path+"/apps/icon";
		}
		else {
			return "";
		}

	}


	public static String getAddrForward() {
		return ADDR_FORWARD;
	}

	public static void setAddrForward(String addrForward) {
		ADDR_FORWARD = addrForward;
		PrefUtils.setAddrForward(addrForward);
	}

	public static String getAddrRgMsg() {
		return ADDR_RG_MSG;
	}

	public static String getAddrMsg() {
		return ADDR_MSG;
	}

	public static void setAddrMsg(String addrMsg) {
		ADDR_MSG = addrMsg;
		PrefUtils.setAddrMsg(addrMsg);
	}

	public static String getAddrWebservice() {
		return ADDR_WEBSERVICE;
	}
	public static String getAddrCommandServer(){
		return  getAddrWebservice();
	}

	public static void setAddrWebservice(String addrWebservice) {
		ADDR_WEBSERVICE = addrWebservice;
		PrefUtils.setAddrWebservice(addrWebservice);
	}

	public static String getAddrBdmap() {
		return ADDR_BDMAP;
	}

	public static void setAddrBdmap(String addrBdmap) {
		ADDR_BDMAP = addrBdmap;
	}

	public static String getAddrUpdate() {
		return ADDR_UPDATE;
	}

	public static void setAddrUpdate(String addrUpdate) {
		ADDR_UPDATE = addrUpdate;
		PrefUtils.setAddrUpdate(addrUpdate);
	}

	public static void initIpSettings() {
		ADDR_BDMAP="159.226.94.159:3000";//
		ADDR_RG_MSG = "192.168.151.175:8000";
		ADDR_FORWARD = PrefUtils.getAddrForward();
		ADDR_MSG = PrefUtils.getAddrMsg();
		ADDR_WEBSERVICE = PrefUtils.getAddrWebservice();
		ADDR_UPDATE = PrefUtils.getAddrUpdate();
	}

	public static String encodeUrl(String appUrl) {
		String netUrl = appUrl;
		int index = appUrl.lastIndexOf('/');
		if (index != -1) {
			try {
				String str = appUrl
						.substring(index + 1);
				str = URLEncoder.encode(str, "utf-8");
				str = str.replaceAll("\\+", "%20");
				netUrl = appUrl.substring(0, index + 1)
						+ str;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return netUrl;
	}

	public static String parseAddress(String url) {
		String ans = "";
		String X[] = url.split("/");
		int size = X.length;
		for (int i = 0; i < size; i++) {
			try {
				ans += URLEncoder.encode(X[i], "UTF-8");
				if (i != size - 1)
					ans += "/";
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ans.replace("%3A", ":").replace("+","%20");
	}

}