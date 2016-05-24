package cn.dacas.emmclient.msgpush;

public class RegMsgPush {
	static{
		System.loadLibrary("MsgPush");
	}
	
	public native void init(String id, MsgPushListener listener, String[] serverIps);
}
