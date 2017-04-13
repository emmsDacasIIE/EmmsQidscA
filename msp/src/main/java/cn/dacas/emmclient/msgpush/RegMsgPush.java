package cn.dacas.emmclient.msgpush;

/**
 * 加载libMsgPuash.so库，初始化消息推送功能；
 */
@Deprecated
public class RegMsgPush {
	static{
		System.loadLibrary("MsgPush");
	}
	
	public native void init(String id, MsgPushListener listener, String[] serverIps);
}
