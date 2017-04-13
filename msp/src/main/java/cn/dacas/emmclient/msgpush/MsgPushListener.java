package cn.dacas.emmclient.msgpush;

@Deprecated
public interface MsgPushListener {
	public void onMessage(byte[] msg);
	public void onState(boolean state);
}
