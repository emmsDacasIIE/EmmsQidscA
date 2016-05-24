package com.iie.bean;

public class DeviceInfo implements java.io.Serializable{
	private String text;
	private String email;
	private String device_id;
	private String device_type;
	private String imei;
	private String platform;
	private String register_time;
	private String status;
	
	public DeviceInfo() {
	}
	
	public DeviceInfo(String email,String device_id, String device_type, String imei,
			String platform, String register_time, String status) {
		super();
		this.email = email;
		this.device_id = device_id;
		this.device_type = device_type;
		this.imei = imei;
		this.platform = platform;
		this.register_time = register_time;
		this.status = status;
	}
	
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	public String getDevice_type() {
		return device_type;
	}
	public void setDevice_type(String device_type) {
		this.device_type = device_type;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getRegister_time() {
		return register_time;
	}
	public void setRegister_time(String register_time) {
		this.register_time = register_time;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
