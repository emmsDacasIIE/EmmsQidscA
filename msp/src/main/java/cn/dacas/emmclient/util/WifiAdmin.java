package cn.dacas.emmclient.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.List;

public class WifiAdmin {
	private static final String TAG = "[WifiAdmin]";
	private android.net.wifi.WifiManager mWifiManager;
	private WifiInfo mWifiInfo;
	private List<ScanResult> mWifiList = null;
	private List<WifiConfiguration> mWifiConfiguration;
	private WifiLock mWifiLock;
	public WifiAdmin(Context context) {
		mWifiManager = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	public boolean openWifi() {//打开wifi
		if (!mWifiManager.isWifiEnabled()) {
			QDLog.i(TAG, "setWifiEnabled.....");
			mWifiManager.setWifiEnabled(true);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			QDLog.i(TAG, "setWifiEnabled.....end");
		}
		return mWifiManager.isWifiEnabled();
	}

	public void closeWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	public int checkState() {
		return mWifiManager.getWifiState();
	}

	public void acquireWifiLock() {//锁定wifiLock
		mWifiLock.acquire();
	}

	public void releaseWifiLock() {//解锁wifiLock
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	public void creatWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("Test");
	}

	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfiguration;
	}

	public void connectConfiguration(int index) {//指定配置好的网络进行连接
		if (index > mWifiConfiguration.size()) {
			return;
		}
		mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
	}

	public void startScan() {//wifi扫描
		boolean scan = mWifiManager.startScan();
		QDLog.i(TAG, "startScan result:" + scan);
		mWifiList = mWifiManager.getScanResults();
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();

		if (mWifiList != null) {
			QDLog.i(TAG, "startScan result:" + mWifiList.size());
			for (int i = 0; i < mWifiList.size(); i++) {
				ScanResult result = mWifiList.get(i);
				QDLog.i(TAG, "startScan result[" + i + "]" + result.SSID + "," + result.BSSID);
			}
			QDLog.i(TAG, "startScan result end.");
		} else {
			QDLog.i(TAG, "startScan result is null.");
		}

	}

	public List<ScanResult> getWifiList() {
		return mWifiList;
	}

	public StringBuilder lookUpScan() {// 查看扫描结果
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < mWifiList.size(); i++) {
			stringBuilder.append("Index_" + new Integer(i + 1).toString() + ":");
			stringBuilder.append((mWifiList.get(i)).toString());
			stringBuilder.append("/n");
		}
		return stringBuilder;
	}

	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	public DhcpInfo getDhcpInfo() {
		return mWifiManager.getDhcpInfo();
	}

	public int getIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	public WifiInfo getWifiInfo() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return mWifiInfo;
	}

	public int addNetwork(WifiConfiguration wcg) {// 添加一个网络配置并连接
		int wcgID = mWifiManager.addNetwork(wcg);
		if(wcgID == -1){
			return -1;
		}
		Method connectMethod = connectWifiByReflectMethod(wcgID);
		if (connectMethod == null) {
			QDLog.d(TAG, "connect wifi by enableNetwork method!");
			// 通用API
			mWifiManager.enableNetwork(wcgID, false);
		}
		return 0;
	}

	public void disconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}

	public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
		QDLog.i(TAG, "SSID:" + SSID + ",password:" + Password);
		WifiConfiguration config = new WifiConfiguration();

		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";

		WifiConfiguration tempConfig = this.IsExist(SSID);

		if (tempConfig != null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		} else {
			QDLog.i(TAG, "IsExist is null.");
		}

		if (Type == 1) // WIFICIPHER_NOPASS
		{
			QDLog.i(TAG, "Type = 1.");
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 2) // WIFICIPHER_WEP
		{
			QDLog.i(TAG, "Type = 2.");
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 3) // WIFICIPHER_WPA
		{

			QDLog.i(TAG, "Type = 3.");
			config.preSharedKey = "\"" + Password + "\"";

			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}

	private WifiConfiguration IsExist(String SSID) {// 查看以前是否已经配置过该SSID &nbsp;
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

	private Method connectWifiByReflectMethod(int netId) {
		Method connectMethod = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			QDLog.d(TAG, "connectWifiByReflectMethod road 1");
			// 反射方法： connect(int, listener) , 4.2 <= phone's android version
			for (Method methodSub : mWifiManager.getClass()
					.getDeclaredMethods()) {
				if ("connect".equalsIgnoreCase(methodSub.getName())) {
					Class<?>[] types = methodSub.getParameterTypes();
					if (types != null && types.length > 0) {
						if ("int".equalsIgnoreCase(types[0].getName())) {
							QDLog.d(TAG,"find connect method By Reflection!");
							connectMethod = methodSub;
						}
					}
				}
			}
			if (connectMethod != null) {
				try {
					connectMethod.invoke(mWifiManager, netId, null);
				} catch (Exception e) {
					e.printStackTrace();
					QDLog.d(TAG, "connectWifiByReflectMethod Android "
							+ Build.VERSION.SDK_INT + " error!");
					return null;
				}
			}
		} else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
			// 反射方法: connect(Channel c, int networkId, ActionListener listener)
			// 暂时不处理4.1的情况 , 4.1 == phone's android version
			QDLog.d(TAG, "connectWifiByReflectMethod road 2");
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
				&& Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			QDLog.d(TAG, "connectWifiByReflectMethod road 3");
			// 反射方法：connectNetwork(int networkId) ,
			// 4.0 <= phone's android version < 4.1
			for (Method methodSub : mWifiManager.getClass()
					.getDeclaredMethods()) {
				if ("connectNetwork".equalsIgnoreCase(methodSub.getName())) {
					Class<?>[] types = methodSub.getParameterTypes();
					if (types != null && types.length > 0) {
						if ("int".equalsIgnoreCase(types[0].getName())) {
							connectMethod = methodSub;
						}
					}
				}
			}
			if (connectMethod != null) {
				try {
					connectMethod.invoke(mWifiManager, netId);
				} catch (Exception e) {
					e.printStackTrace();
					QDLog.d(TAG, "connectWifiByReflectMethod Android "
							+ Build.VERSION.SDK_INT + " error!");
					return null;
				}
			}
		} else {
			// < android 4.0
			return null;
		}
		return connectMethod;
	}
}