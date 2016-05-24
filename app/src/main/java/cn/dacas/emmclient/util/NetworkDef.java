package cn.dacas.emmclient.util;

import android.content.Context;

import cn.dacas.emmclient.R;


public class NetworkDef {

	public static String PROTOCOL = "https://";

	// 安全接入服务器地址
	private static String ADDR_FORWARD;

	// 消息推送服务器地址
	private static String ADDR_MSG;

	// webservice服务器地址
	private static String ADDR_WEBSERVICE ;

	// baidu 地图
	private static String ADDR_BDMAP;

	private static String ADDR_UPDATE;

	// 超时时间
	private final int timeOut = 5000;

	private int ipNum = -1;
	
	private static int addrType=2;

	public static void initIpSettings(Context context) {
		// init msgpush ip
		String[] ips=null;
        if (addrType==1)
			ips=context.getResources().getStringArray(R.array.nancheServer);
		else if (addrType==2)
			ips=context.getResources().getStringArray(R.array.localServer);
		else if (addrType==3)
			ips=context.getResources().getStringArray(R.array.dcsServer);
		else if (addrType==4)
			ips=context.getResources().getStringArray(R.array.qdscServer);
		ADDR_MSG=ips[0];
		ADDR_WEBSERVICE=PrefUtils.getWebserviceAddress();
		ADDR_FORWARD=ips[2];
		ADDR_BDMAP=ips[3];
		ADDR_UPDATE=ips[4];
		return;
	}


	public static String getAvailableMsgPushIp() {
		return ADDR_MSG;
	}

	public static String getAvailableForwardIp() {
		return ADDR_FORWARD;
	}

	public static String getAvailableBDMapIp() {
		return ADDR_BDMAP;
	}

	public static String getAvailableUpdateIp() {
		return ADDR_UPDATE;
	}

	public static String getAddrWebservice() {
		return ADDR_WEBSERVICE;
	}

	public static void setAddrWebservice(String addrWebservice) {
		ADDR_WEBSERVICE = addrWebservice;
		PrefUtils.setWebServiceAddress(addrWebservice);
	}
}