package cn.dacas.emmclient.event;

import android.os.Bundle;

public class MessageEvent {
	
	public static final int Event_StartTransaction=0x0101;
	public static final int Event_StartForwarding=0x0103;
	public static final int Event_StopForwarding=0x0104;
	public static final int Event_UploadLocation=0x0102;

	public static final int Event_OnlineState=0x0201;

	public static final int Event_StartMsgPush=0x0301;

	public static final int Event_Bluetooth_State_Changed=0x0401;
	public static final int Event_Network_State_Changed=0x0402;

	public static final int Event_FILEOPEN_FAILED=0x0501;

	public static final int Event_Show_alertDialog = 0x0601;

	public static final int Event_NO_UpdateApk = 0x0701;
	public static final int Event_UpdateApk = 0x0702;
	public static final int Event_Error_UpdateApk = 0x0702;

	public static final int Event_MsgCount_Change = 0x801;

	public static final int Event_APP_Deleted = 0x901;
	// 0x0101-0x01FF for MDMService
	public int type;
	public Bundle params;
	public MessageEvent(int type) {
		this.type=type;
	}
	
	public MessageEvent(int type, Bundle params) {
		this.type=type;
		this.params=params;
	}
}
