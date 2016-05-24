package cn.dacas.emmclient.msgpush;

public interface MsgPushListener {
	public void onMessage(byte[] msg);
	public void onState(boolean state);
}
