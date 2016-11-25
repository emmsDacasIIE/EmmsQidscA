package cn.dacas.emmclient.manager;

import android.content.Intent;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.ui.activity.loginbind.AppStartActivity;
import cn.dacas.emmclient.ui.activity.loginbind.UserLoginActivity;
import cn.dacas.emmclient.ui.gesturelock.UnlockActivity;

/**
 * Created by lenovo on 2015-12-28.
 */
public class ActivityManager {
    public static void gotoUnlockActivity() {
        String account = EmmClientApplication.mCheckAccount.getCurrentAccount();
        if(account!=null&&
                EmmClientApplication.mDatabaseEngine.getPatternPassword(account)!=null&&
                !EmmClientApplication.mActivateDevice.getDeviceType().equals("COPE-PUBLIC"))
        {
            if(0==EmmClientApplication.mDatabaseEngine.getLoginType(EmmClientApplication.mCheckAccount.getCurrentAccount())){
                // TODO: 2016-11-25 PassWord Login
                Intent intent = new Intent(EmmClientApplication.getContext(), UserLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("type", 0);
                EmmClientApplication.getContext().startActivity(intent);
            }
            else {
                Intent intent = new Intent(EmmClientApplication.getContext(), UnlockActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("type", 0);
                EmmClientApplication.getContext().startActivity(intent);
            }
        }
    }
}
