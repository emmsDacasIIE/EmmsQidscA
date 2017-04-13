package cn.dacas.emmclient.manager;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.File;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.core.mcm.FileOpener;
import cn.dacas.emmclient.ui.activity.loginbind.UserLoginActivity;
import cn.dacas.emmclient.ui.activity.mainframe.PdfViewerActivity;
import cn.dacas.emmclient.ui.activity.mainframe.WebViewerActivity;
import cn.dacas.emmclient.ui.gesturelock.UnlockActivity;

/**
 * Created by lenovo on 2015-12-28.
 * Updated by Sun Rx on 2016-11-28
 * If pwdType == 0, then goto UserLoginActivity
 * else if pwdType == 1, then goto UnlockActivity
 */
public class ActivityManager {
    public static boolean isLocking =false;
    public static void gotoUnlockActivity() {
        isLocking = true;
        String account = EmmClientApplication.mCheckAccount.getCurrentAccount();
        if(account!=null&&
                EmmClientApplication.mDatabaseEngine.getPatternPassword(account)!=null&&
                !EmmClientApplication.mActivateDevice.getDeviceType().equals("COPE-PUBLIC"))
        {
            if(0==EmmClientApplication.mDatabaseEngine.getLoginType(account)){
                // TODO: 2016-11-25 PassWord Login
                Intent intent = new Intent(EmmClientApplication.getContext(), UserLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("type", 0);//0：解锁 1：设置
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

    public static void openPdfFile(Context mContext, String fileFullName, String type) {
        Intent intent = new Intent(mContext, PdfViewerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("fileFullName", fileFullName);
        intent.putExtra("file_type", type);
        mContext.startActivity(intent);
    }

    public static void openWordFile(Context mContext, String fileFullName, String type) {
        Intent intent = new Intent(mContext, WebViewerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("fileFullName", fileFullName);
        intent.putExtra("file_type", type);
        mContext.startActivity(intent);
    }
}
