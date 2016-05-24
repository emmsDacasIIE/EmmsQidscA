package cn.qdsc.msp.manager;

import android.content.Intent;

import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.ui.gesturelock.UnlockActivity;

/**
 * Created by lenovo on 2015-12-28.
 */
public class ActivityManager {
    public static void gotoUnlockActivity() {
        String account = EmmClientApplication.mCheckAccount.getCurrentAccount();
        if(account!=null&&EmmClientApplication.mDatabaseEngine.getPatternPassword(account)!=null&&
                !EmmClientApplication.mActivateDevice.getDeviceType().equals("COPE-PUBLIC"))
        {
                Intent intent = new Intent(EmmClientApplication.getContext(), UnlockActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("type", 0);
                EmmClientApplication.getContext().startActivity(intent);
        }
    }
}
