package cn.dacas.emmclient.interception;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

public class ActivityStack {

	
	public static String getForegroundPkg(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		return currentPackageName;
	}
	
	
	
	public static boolean isRunningForeground(Context context,String pkgName) {
		String currentPackageName =  getForegroundPkg(context);
		if (!TextUtils.isEmpty(pkgName)
				&& currentPackageName.equalsIgnoreCase(pkgName)) {
			return true;
		}
		return false;
	}
}
