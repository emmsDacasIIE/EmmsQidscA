package cn.dacas.emmclient.core.mam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.model.MamAppInfoModel;
import cn.dacas.emmclient.ui.activity.mainframe.MamAppListActivity;
import cn.dacas.emmclient.ui.activity.mainframe.NewMainActivity;
import cn.dacas.emmclient.ui.fragment.AppListFragment;
import cn.dacas.emmclient.util.PrefUtils;

public class PkgChangedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		//接收广播：设备上新安装了一个应用程序包后自动启动新安装应用程序。  
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            String packageName = intent.getDataString();
            MApplicationManager myActivityManager = MApplicationManager.getApplicationManager(context);
            if(myActivityManager != null){
            	myActivityManager.appAdded(packageName);
            }
        }  
        //接收广播：设备上删除了一个应用程序包。  
        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {  
        	String deletedPackageName = intent.getDataString().substring(8);
            ArrayList<MamAppInfoModel> canceledAppList = PrefUtils.getCanceledAppList();
            for(MamAppInfoModel canceledApp:canceledAppList){
                if(canceledApp.pkgName.equals(deletedPackageName)) {
                    canceledAppList.remove(canceledApp);
                    Toast.makeText(context,"成功删除失效应用："+canceledApp.appName,Toast.LENGTH_LONG).show();
                }
            }
            PrefUtils.putCancelAppList(canceledAppList);
            context.sendBroadcast(new Intent(AppListFragment.ACTION_REFRESH_APPSTORE));
            //context.startActivity(NewMainActivity.getMainActivityIntent(context));

            MApplicationManager mActivityManager = MApplicationManager.getApplicationManager(context);
            if(mActivityManager != null){
            	mActivityManager.appRemoved(deletedPackageName);
            }
        }
        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
        }
	}
}
