package com.iie.bean;

public class SoftInfo implements java.io.Serializable{
	
	public SoftInfo(String appName, int appId, String appVersion, long appSize,
			long dataSize, String appType) {
		super();
		this.appName = appName;
		this.appId = appId;
		this.appVersion = appVersion;
		this.appSize = appSize;
		this.dataSize = dataSize;
		this.appType = appType;
	}
	private String appName;
	private int appId;
	private String appVersion;
	private long appSize;
	private long dataSize;
	private String appType;
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public int getAppId() {
		return appId;
	}
	public void setAppId(int appId) {
		this.appId = appId;
	}
	public String getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	public long getAppSize() {
		return appSize;
	}
	public void setAppSize(long appSize) {
		this.appSize = appSize;
	}
	public long getDataSize() {
		return dataSize;
	}
	public void setDataSize(long dataSize) {
		this.dataSize = dataSize;
	}
	public String getAppType() {
		return appType;
	}
	public void setAppType(String appType) {
		this.appType = appType;
	}
}
