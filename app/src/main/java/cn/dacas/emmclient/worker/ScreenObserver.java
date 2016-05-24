package cn.dacas.emmclient.worker;

import java.lang.reflect.Method;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;

public class ScreenObserver{
	public static ScreenObserver mScreenObserver;
	private static String TAG = "ScreenObserver";
	private Context mContext;
	private ScreenBroadcastReceiver mScreenReceiver;
    private ScreenStateListener mScreenStateListener;
    private static Method mReflectScreenState;
    
	public ScreenObserver(Context context){
		mContext = context;
		mScreenReceiver = new ScreenBroadcastReceiver();
		try {
			mReflectScreenState = PowerManager.class.getMethod("isScreenOn",
					new Class[] {});
		} catch (NoSuchMethodException nsme) {
			Log.d(TAG, "API < 7," + nsme);
		}
		
	}
	
	public static ScreenObserver getScreenObserverInstance(Context context) {
		if (mScreenObserver == null) {
			mScreenObserver = new ScreenObserver(context);
		}
		
		return mScreenObserver;
	}
	
	/**
     * screen状态广播接收者
     *
     */
    private class ScreenBroadcastReceiver extends BroadcastReceiver{
    	private String action = null;
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		action = intent.getAction();
    		if(Intent.ACTION_SCREEN_ON.equals(action)){
    			mScreenStateListener.onScreenOn();
    		}else if(Intent.ACTION_SCREEN_OFF.equals(action)){
    			mScreenStateListener.onScreenOff();
    		}
    	}
    }
    
	
	/**
	 * 请求screen状态更新
	 * @param listener
	 */
	public void setScreenStateListener(ScreenStateListener listener) {
		mScreenStateListener = listener;
		startScreenBroadcastReceiver();	
		firstGetScreenState();
	}
	
	/**
	 * 第一次请求screen状态
	 */
	private void firstGetScreenState(){
		PowerManager manager = (PowerManager) mContext
				.getSystemService(Activity.POWER_SERVICE);
		if (isScreenOn(manager)) {
			if (mScreenStateListener != null) {
				mScreenStateListener.onScreenOn();
			}
		} else {
			if (mScreenStateListener != null) {
				mScreenStateListener.onScreenOff();
			}
		}
	}
	
	/**
	 * 停止screen状态更新
	 */
	public void stopScreenStateUpdate(){
		mContext.getApplicationContext().unregisterReceiver(mScreenReceiver);
	}
	
	/**
	 * 启动screen状态广播接收器
	 */
    private void startScreenBroadcastReceiver(){
    	IntentFilter filter = new IntentFilter();
    	filter.addAction(Intent.ACTION_SCREEN_ON);
    	filter.addAction(Intent.ACTION_SCREEN_OFF);
    	mContext.getApplicationContext().registerReceiver(mScreenReceiver, filter);
    }
	
    /**
     * screen是否打开状态
     * @param pm
     * @return
     */
	private static boolean isScreenOn(PowerManager pm) {
		boolean screenState;
		try {
			screenState = (Boolean) mReflectScreenState.invoke(pm);
		} catch (Exception e) {
			screenState = false;
		}
		return screenState;
	}
	
	public interface ScreenStateListener {
		public void onScreenOn();
		public void onScreenOff();
	}
}