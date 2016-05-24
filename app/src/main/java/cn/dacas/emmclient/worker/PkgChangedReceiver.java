package cn.dacas.emmclient.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PkgChangedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//接收广播：设备上新安装了一个应用程序包后自动启动新安装应用程序。  
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            String packageName = intent.getDataString();
            MApplicationManager myActivityManager = MApplicationManager.getMyActivityManager(context);
            if(myActivityManager != null){
            	myActivityManager.appAdded(packageName);
            }
        }  
        //接收广播：设备上删除了一个应用程序包。  
        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {  
        	String packageName = intent.getDataString();
            MApplicationManager mActivityManager = MApplicationManager.getMyActivityManager(context);
            if(mActivityManager != null){
            	mActivityManager.appRemoved(packageName);
            }
        }
        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
        }
	}
}
