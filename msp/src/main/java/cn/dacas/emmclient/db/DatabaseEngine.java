package cn.dacas.emmclient.db;

import android.content.Context;
import android.database.Cursor;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.security.AESUtil;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.core.mam.MApplicationManager;

/**
 * 将EmmClientDb进行封装，供上层调用
 * Created by lenovo on 2015/12/1.
 */
public class DatabaseEngine {

    private EmmClientDb mEmmClientDb = null;

    public DatabaseEngine(Context context) {
    }

    public void init() {
        mEmmClientDb = new EmmClientDb(EmmClientApplication.getContext());
        mEmmClientDb.open();
    }

    public void finish() {
        mEmmClientDb.closeclose();
    }

    //数据库处理业务

    public boolean setLoginType(String mEmail, int mType) {
        return  mEmmClientDb.updateOrInsertItemByInfo(EmmClientDb.PASSWORD_DATABASE_TABLE,
                new String[]{"email"}, new String[]{mEmail},
                new String[]{"pwdtype"},
                new String[]{String.valueOf(mType)});
    }

    public boolean setCurrentLoginType(int mType) {
        String email= EmmClientApplication.mCheckAccount.getCurrentAccount();
        return setLoginType(email, mType);
    }

    //-1 不存在；0：用户名口令；1：+手势
    public int getLoginType(String mEmail) {
        Cursor mCoursor = mEmmClientDb.getItemByInfo(EmmClientDb.PASSWORD_DATABASE_TABLE,
                new String[]{"email"}, new String[]{mEmail},
                new String[]{"pwdtype"});
        if (mCoursor==null) return -1;
        int r=mCoursor.getInt(0);
        mCoursor.close();
        return r;
    }

    public int getCurrentLoginType() {
        String email= EmmClientApplication.mCheckAccount.getCurrentAccount();
        return getLoginType(email);
    }

    public String getWordsPassword(String mEmail) {
        Cursor mCursor = mEmmClientDb.getItemByInfo(EmmClientDb.PASSWORD_DATABASE_TABLE,
                new String[]{"email"}, new String[]{mEmail},
                new String[]{"wordspwd"});
        String passwordChaos = mCursor.getString(0);
        return AESUtil.decrypt(PrefUtils.PASSWORD_SETTING_WORDS, passwordChaos);
    }

    public void setWordsPassword(String user,String password,String email) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.PASSWORD_DATABASE_TABLE,
                new String[]{"email"}, new String[]{email},
                new String[]{"email", "name", "wordspwd"},
                new String[]{
                        email, user, AESUtil.encrypt(PrefUtils.PASSWORD_SETTING_WORDS,
                        password)});
    }



    public void setPatternPassword(String mEmail, String mPassword) {
        mEmmClientDb.updateOrInsertItemByInfo(EmmClientDb.PASSWORD_DATABASE_TABLE,
                new String[]{"email"}, new String[]{mEmail},
                new String[]{"patternpwd", "pwdtype"}, new String[]{
                        AESUtil.encrypt(PrefUtils.PASSWORD_SETTING_WORDS,
                                mPassword), "1"});
    }
    /**
     *
     */
    public String getPatternPassword(String email) {
        Cursor mCursor = mEmmClientDb.getItemByInfo(EmmClientDb.PASSWORD_DATABASE_TABLE,
                new String[] { "email" }, new String[] { email },
                new String[] { "patternpwd" });
        if (mCursor==null) return null;
        String passwordChaos = mCursor.getString(0);
        mCursor.close();
        return passwordChaos==null?null:AESUtil.decrypt(
                PrefUtils.PASSWORD_SETTING_WORDS, passwordChaos);
    }

    ///////////////////2. contact about
    /**
     * delete all contacts
     */
    public void deleteContactAllData() {
        mEmmClientDb.deleteAllDbItem(EmmClientDb.CONTACT_DATABASE_TABLE);
    }

    /**
     * get contact all data
     * @param
     * @param
     */
    public Cursor getContactAllData() {
        Cursor cursor = mEmmClientDb.getAllItemsOfTable(EmmClientDb.CONTACT_DATABASE_TABLE, null);
        return cursor;
    }

    /**
     * get contact item data, from getItemByInfo
     * @param
     * @param
     */
    public Cursor getContactItemData(String[] fields, String[] values) {

        Cursor cursor = mEmmClientDb.getItemByInfo(EmmClientDb.CONTACT_DATABASE_TABLE,
                fields,
                values,
                null);
        return cursor;
    }

    /**
     * add item data
     */

    public void addContactItemData(String[] fields, String[] values) {

        mEmmClientDb.addDbItem(EmmClientDb.CONTACT_DATABASE_TABLE, fields, values);


    }


    ////////////////////3. app about
    /**
     * get black list
     * @return
     */
    public Cursor getBlackList() {
        Cursor mCursor = mEmmClientDb.getAllItemsOfTable(
                EmmClientDb.APPBLACK_DATABASE_TABLE, null);
        return mCursor;
    }

    /**
     * update appinfo
     * @param app
     */
    public void updateAppinfo(MApplicationManager.AppInfo app) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.APPBLACK_DATABASE_TABLE,
                null,
                null,
                new String[]{"pkgname", "time"},
                new String[]{app.getPkgInfo().packageName,
                        String.valueOf(System.currentTimeMillis())});
    }

    //////////////////4. doc about
    /**
     * 从doclistfragment而来
     *
     */
    public Cursor getDocListCursor(String id,String uTime) {
        Cursor cursor = mEmmClientDb.getItemByInfo(
                EmmClientDb.CORPFILE_DATABASE_TABLE,
                new String[] {
                        "sender", "time" },
                new String[] { id,
                        uTime },
                new String[] { "sender" }
        );
        return cursor;
    }

    /**
     * 从doclistfragment而来
     *
     */
    public boolean updateDocList(String id,String name,String url,String uTime) {
        return mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.CORPFILE_DATABASE_TABLE,
                new String[]{"sender"},
                new String[]{id},
                new String[]{"filetag", "isnative", "url",
                        "time", "sender"},
                new String[]{name, "n", url, uTime, id}
        );
    }


    /**
     * 更新数据库,用非空值
     */
    public boolean updateDocItem(String fileName,String isNative,String uTime,String sender,String favStatus) {
        return mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.CORPFILE_DATABASE_TABLE,
                new String[]{"filetag"},
                new String[]{fileName},
                new String[]{"filetag", "isnative","time","sender","fav"},
                new String[]{fileName, isNative,uTime,sender,favStatus});

    }

    /**
     * 更新数据库中文件长度,用非空值
     */
    public boolean updateDocItemLenth(String fileName,String isNative,String uTime,String sender,String len) {
        return mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.CORPFILE_DATABASE_TABLE,
                new String[]{"filetag"},
                new String[]{fileName},
                new String[]{"filetag", "isnative","time","sender","len"},
                new String[]{fileName, isNative,uTime,sender,len});

    }

    /**
     * 从doclistfragment而来
     *
     */
    public Cursor getDocAllItem(String[] requestColumns) {
        Cursor cursor = mEmmClientDb.getAllItemsOfTable(
                EmmClientDb.CORPFILE_DATABASE_TABLE,
                requestColumns
                );
        return cursor;
    }

    public boolean deleteDocItem(String cid) {
        return mEmmClientDb.deleteDbItemBycolumn(
                EmmClientDb.CORPFILE_DATABASE_TABLE, "sender",
                cid);
    }

    /////////////5.message

    public Cursor getAllMessageData() {
        return mEmmClientDb.getAllItemsOfTableInOrder(
                EmmClientDb.DEVICEMSG_DATABASE_TABLE, null,"time");
    }

    public void deleteDbItemByColumns(String msg,String time) {
        mEmmClientDb.deleteDbItemBycolumns(EmmClientDb.DEVICEMSG_DATABASE_TABLE,
                new String[]{"msg", "time"}, new String[]{msg,
                        time});
    }

    public boolean updateMessageData(String title,String msg, String created_at,String read) {
        return mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.DEVICEMSG_DATABASE_TABLE, new String[]{"title"},
                new String[]{title}, new String[]{"title", "msg", "time", "readed"},
                new String[]{title, msg, created_at, read});
    }


    /////////////10.策略相关

    /**
     * 设置静音
     */
    public void setMute( int OP_SET_MUTE) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                null,
                null,
                new String[]{"code", "content", "state", "time"},
                new String[]{Integer.toString(OP_SET_MUTE), "设置静音",
                        "s", Long.toString(System.currentTimeMillis())});

    }


    /**
     * 设置锁屏码
     */
    public void setLockScreenCode( int ret,int OP_LOCK_KEY) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                null,
                null,
                new String[]{"code", "content", "state", "time"},
                new String[]{Integer.toString(OP_LOCK_KEY), "设置锁屏密码",
                        (ret == 0) ? "s" : "r",
                        Long.toString(System.currentTimeMillis())});
    }

    /**
     * 恢复出厂设置
     */
    public void reFactory( int opId) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                null,
                null,
                new String[]{"code", "content", "state", "time"},
                new String[]{Integer.toString(opId), "恢复出厂设置",
                        "s",
                        Long.toString(System.currentTimeMillis())});
    }

    /**
     * 擦除企业数据
     */
    public void eraseCorp( int opId) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                null,
                null,
                new String[]{"code", "content", "state", "time"},
                new String[]{Integer.toString(opId), "擦除企业数据",
                        "s",
                        Long.toString(System.currentTimeMillis())});
    }

    /**
     * 擦除设备上的所有数据
     */
    public void eraseDeviceAllData( int opId) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                null,
                null,
                new String[]{"code", "content", "state", "time"},
                new String[]{Integer.toString(opId), "擦除设备上的所有数据",
                        "s",
                        Long.toString(System.currentTimeMillis())});
    }

    /**
     * 重新上传设备信息---刷新设备
     */
    public void refreshDevice( int opId) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                null,
                null,
                new String[]{"code", "content", "state", "time"},
                new String[]{Integer.toString(opId), "刷新设备",
                        "s",
                        Long.toString(System.currentTimeMillis())});
    }

    /**
     * 企业策略推送
     */
    public void pushPolicy( int opId) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                null,
                null,
                new String[]{"code", "content", "state", "time"},
                new String[]{Integer.toString(opId), "企业策略推送",
                        "s",
                        Long.toString(System.currentTimeMillis())});
    }

    public void removePolicy(int opId) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                null,
                null,
                new String[]{"code", "content", "state", "time"},
                new String[]{Integer.toString(opId), "企业策略移除",
                        "s",
                        Long.toString(System.currentTimeMillis())});
    }

    /**
     * 重新上传设备信息--改变设备状态
     */
    public void changeDeviceState( int opId) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                null,
                null,
                new String[]{"code", "content", "state", "time"},
                new String[]{Integer.toString(opId), "改变设备状态",
                        "s",
                        Long.toString(System.currentTimeMillis())});
    }

    /**
     * 服务器端删除设备
     */
    public void deleteDeviceFromService( int opId) {
        mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.ACTIONLOG_DATABASE_TABLE,
                null,
                null,
                new String[]{"code", "content", "state", "time"},
                new String[]{Integer.toString(opId), "服务器端删除设备",
                        "s",
                        Long.toString(System.currentTimeMillis())});
    }

    public void clearCorpData() {
        mEmmClientDb.clearData();
    }

    ///////applist//////////////
    public Cursor getAppListAllData() {
        Cursor cursor = mEmmClientDb.getAllItemsOfTable(EmmClientDb.APPLIST_DATABASE_TABLE, null);
        return cursor;
    }

    /**
     * add item data to applist
     */

    public void addAppItemData(String[] fields, String[] values) {
        mEmmClientDb.addDbItem(EmmClientDb.APPLIST_DATABASE_TABLE, fields, values);
    }

    /**
     * delete all contacts
     */
    public void deleteAppAllData() {
        mEmmClientDb.deleteAllDbItem(EmmClientDb.APPLIST_DATABASE_TABLE);
    }

    /**
     * 更新应用列表
     *
     */
    public boolean updateAppList(String id,String pkgName,String labelName,String versionName,
            String versionCode,String page,String position) {
        return mEmmClientDb.updateOrInsertItemByInfo(
                EmmClientDb.APPLIST_DATABASE_TABLE,
                new String[]{"sender"},
                new String[]{id},
                new String[]{"package", "label", "sender", "version_name",
                        "version_code", "page","position"},
                new String[]{pkgName, labelName, id,versionName, versionCode, page,position}
        );
    }

    /**
     * 获取applist的cursor
     *
     */
    public Cursor getAppListCursor(String id,String pkgName) {
        Cursor cursor = mEmmClientDb.getItemByInfo(
                EmmClientDb.CORPFILE_DATABASE_TABLE,
                new String[] {"sender", "package" },
                new String[] { id,pkgName },
                new String[] { "sender" }
        );
        return cursor;
    }

//    /**
//     * 根据id进行数据库的插入或更新
//     */
//    public void updateApplist( int id) {
//        mEmmClientDb.updateOrInsertItemByInfo(
//                EmmClientDb.APPLIST_DATABASE_TABLE,
//                "_id",
//                "id",
//                new String[]{"_id", "content", "state", "time"},
//                new String[]{Integer.toString(id), "改变设备状态",
//                        "s",
//                        Long.toString(System.currentTimeMillis())});
//    }


}
