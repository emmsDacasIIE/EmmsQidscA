package cn.dacas.emmclient.core.mam;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cn.dacas.emmclient.model.MamAppInfoModel;
import cn.dacas.emmclient.ui.activity.mainframe.NewMainActivity;
import cn.dacas.emmclient.util.PhoneInfoExtractor;
import cn.dacas.emmclient.util.PrefUtils;

import static com.baidu.location.h.i.A;

/**
 * Created by lizhongyi on 2015/12/16.
 * Updated by Sun RX on 2017/1/22
 */
public class AppManager {

    /**
     * 安装APK文件
     */
    public static void installApk(Context context, File apkFile) {
        installApk(context, apkFile.toString());
    }

    /**
     * 安装apk
     */
    public static void installApk(final Context context, final String filePath) {
        // 修改apk权限
        try {
            String command = "chmod " + "777" + " " + filePath;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // install the apk.
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + filePath),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 安装apk, 先不用这个。
     * @param context
     * @return
     */

    public static void installPackage(Context context, File file, int requestCode) {
        // 安装之前先记录预安装APK文件的名字，以用于返回结果时根据requestCode获取安装的apk packageName
//        MainActivity.m_mapInstallApkData.put(requestCode, file.getName());
//
//        //gxj: 如果主界面没有显示，需要调出主界面到前台
//        Intent it1 = new Intent(context, MainActivity.class);
//        it1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        context.startActivity(it1);

        //gxj: <span style="color:#ff0000;">不使用FLAG_ACTIVITY_NEW_TASK是因为无法获取result</span>，同时又要保持多个apk(task)的安装(FLAG_ACTIVITY_SINGLE_TOP / FLAG_ACTIVITY_PREVIOUS_IS_TOP)
        //<span style="color:#ff0000;">对于使用FLAG_ACTIVITY_SINGLE_TOP / FLAG_ACTIVITY_PREVIOUS_IS_TOP的情况，startActivityForResult只有在所有的安装界面消失的时候才会有返回，且返回的resultCode都是0，疑惑，所以在返回后需要检查否安装成功的判断</span>
        //不能使用Intent.FLAG_ACTIVITY_NEW_TASK是因为Intent.FLAG_ACTIVITY_NEW_TASK无法获得返回的结果；不能使用Intent.FLAG_ACTIVITY_CLEAR_TOP是因为可能会有多个apk同时安装
        Intent it2 = new Intent();
        it2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);	//或FLAG_ACTIVITY_PREVIOUS_IS_TOP
        it2.setAction(android.content.Intent.ACTION_VIEW);
        Uri path = Uri.fromFile(file);
        it2.setDataAndType(path, "application/vnd.android.package-archive");
//        context.startActivityForResult(it2, requestCode);
    }

    /**
     * 安装成功与否check,也可以用于判断app是否存在
     */
    public static boolean checkInstallResult(Context mContext, String pkgName) {
        int code = PhoneInfoExtractor.getPackageVersionCode(mContext, pkgName);
        return code >= 0;

    }


    // =============== packages apps ===============//
    /*
     * 最基本的PackageInfo List的获取
     */
    public static List<PackageInfo> getInstalledPackages(Context context) {
        return context.getPackageManager().getInstalledPackages(0);
    }

    /*
     * 最基本的ApplicationInfo List的获取
     */
    public static List<ApplicationInfo> getInstalledApplications(Context context) {
        return context.getPackageManager().getInstalledApplications(0);
    }



    /**
     * 获取已安装的App
     *
     * @param context
     * @param includeSysApps:
     *            true, 查找全部app； false,查找系统自带之外的app
     *
     * @return ArrayList<AppModel>
     */
    public static ArrayList<MamAppInfoModel> getInstalledApps(Context context, boolean includeSysApps) {
        ArrayList<MamAppInfoModel> res = new ArrayList<>();
        List<PackageInfo> packs = context.getPackageManager()
                .getInstalledPackages(0);

        for (PackageInfo pkInfo : packs) {
            ApplicationInfo p = pkInfo.applicationInfo;
            // Non-system app
            if (includeSysApps || (p.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                MamAppInfoModel model = new MamAppInfoModel();
                model.appName = p.loadLabel(context.getPackageManager()).toString();
                model.pkgName = p.packageName;
                model.appVersionCode = pkInfo.versionCode;
                model.appVersion = pkInfo.versionName;
                model.appType = ((p.flags & ApplicationInfo.FLAG_SYSTEM) == 0) ? 0 : 1;
                model.type="APK";
                res.add(model);
            }
        }

        return res;
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *  @return 返回包含所有包名的字符串列表
     */
    public static List<String> getHomes(Context ctx) {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = ctx.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    public static Drawable getApplicationInfo(Context context,String pkgName) {
        try {
            return context.getPackageManager().getApplicationIcon(pkgName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param context
     * @param pkgName
     * @return -1: 没有安装; 0:最新版本； 1：本地的比服务器的低；2：本地的比服务器的高。
     * 99: 未知版本
     */
    public static int checkApkVersion(Context context,String pkgName,int newVersionCode) {
        if (!checkInstallResult(context, pkgName)) {
            return -1;
        }
        String curVersion = PhoneInfoExtractor.getPackageVersionName(context, pkgName);
        int curVersionCode = PhoneInfoExtractor.getPackageVersionCode(context, pkgName);

        if (curVersion != null
                && curVersionCode == newVersionCode) {
            //是最新版本
            return 0;
        } else if (curVersion != null
                && curVersionCode < newVersionCode) {
            //不是最新版本
            return 1;
//                holder.rightBtn.setText("更新");
        } else if (curVersion != null
                && curVersionCode > newVersionCode) {
            //服务器版本比本地安装的版本低
            return 2;
        }
        return 99;
    }

    public static String getForegroundPkg(Context context) {
        android.app.ActivityManager am = (android.app.ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        return currentPackageName;
    }

    /*
	 * 启动一个app
	 */
    public static void startAPP(Context context,String appPackageName){
        try{
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
            context.startActivity(intent);
        }catch(Exception e){
            Toast.makeText(context, "没有安装", Toast.LENGTH_LONG).show();

        }
    }

    static public ArrayList<MamAppInfoModel> updatedCanceledAppList(Context context, ArrayList<MamAppInfoModel> newAppList){
        ArrayList<MamAppInfoModel> currentCanceledList = PrefUtils.getCanceledAppList();
        ArrayList<MamAppInfoModel> currentAppList = PrefUtils.getAppList();
        ArrayList<MamAppInfoModel> newCanceledList = new ArrayList<>();

        for(Iterator it = newAppList.iterator(); it.hasNext();){
            MamAppInfoModel newApp = (MamAppInfoModel)it.next();
            if (currentCanceledList.contains(newApp))
                currentCanceledList.remove(newApp);
        }

        for(MamAppInfoModel oldApp : currentAppList){
            if((!newAppList.contains(oldApp))
                    &&(!currentCanceledList.contains(oldApp))
                    && oldApp.isApk()
                    && AppManager.checkInstallResult(context, oldApp.pkgName)) {
                oldApp.isCanceled = true;
                newCanceledList.add(oldApp);
            }
        }
        newCanceledList.addAll(currentCanceledList);
        PrefUtils.putCancelAppList(newCanceledList);
        return newCanceledList;
    }

    public List<ResolveInfo> queryUrlSchemeApp(Context context){
        PackageManager pm = context.getPackageManager(); // 获得PackageManager对象
        //Intent mainIntent = new Intent(Intent.ACTION_MAIN, Uri.parse("emms://auth_activity/*"));
        Intent mainIntent = new Intent("emms.intent.action.check_authorization");
        mainIntent.addCategory("emms.intent.category.authorization");
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm
                .queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);
        // 调用系统排序 ， 根据name排序
        // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
        Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
        return resolveInfos;
    }
    static public void uninstallApp(Context context, String pkgName){
        Intent uninstall_intent = new Intent();
        uninstall_intent.setAction(Intent.ACTION_DELETE);
        uninstall_intent.setData(Uri.parse("package:"+pkgName));
        context.startActivity(uninstall_intent);
    }
}
