package cn.dacas.emmclient.event;

import android.os.Bundle;

public class MessageEvent {
	
	public static final int Event_StartTransaction=0x0101;
	public static final int Event_StartForwarding=0x0103;
	public static final int Event_StopForwarding=0x0104;
	public static final int Event_UploadLocation=0x0102;
	public static final int Event_OnlineState=0x0201;
	// 0x0101-0x01FF for MDMService
	public int type;
	public Bundle params;
	public MessageEvent(int type) {
		this.type=type;
	}
	
	public MessageEvent(int type,Bundle params) {
		this.type=type;
		this.params=params;
	}
}
