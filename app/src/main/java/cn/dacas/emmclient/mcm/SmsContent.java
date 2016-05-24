package cn.dacas.emmclient.mcm;

// 一条短信内容
public class SmsContent implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String address;
	String body;
	String date;
	String type;
}
