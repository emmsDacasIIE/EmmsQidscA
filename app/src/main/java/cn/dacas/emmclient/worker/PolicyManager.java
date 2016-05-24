package cn.dacas.emmclient.worker;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.dacas.emmclient.main.CheckAccount;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.util.MyJsonObjectRequest;
import cn.dacas.emmclient.util.NetworkDef;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.UpdateTokenRequest;
import cn.dacas.emmclient.worker.CertificateList.Cert;

//目前执行的策略被保存在一个配置文件中 policy.last
public class PolicyManager {
    private static PolicyManager mPolicyManager = null;
    private static PolicyContent DEFAULT_POLICY = new PolicyContent();


    static {
        DEFAULT_POLICY.setName("默认");
        DEFAULT_POLICY.setType("Organization");
        DEFAULT_POLICY.setEffectTimeStart("00:00");
        DEFAULT_POLICY.setEffectTimeEnd("23:59");

        DEFAULT_POLICY.setAlertLevel("警告");
        DEFAULT_POLICY.setDisableCamera(false);
        DEFAULT_POLICY.setConfigPasswdPolicy(true);
        DEFAULT_POLICY.setMaximumFailedPasswordsForWipe(0);
        DEFAULT_POLICY.setMaximumTimeToLock(0);
        DEFAULT_POLICY.setPasswdQuality("");
        DEFAULT_POLICY.setPasswordExpirationTimeout(0);
        DEFAULT_POLICY.setPasswordHistoryLength(0);
        DEFAULT_POLICY.setPasswordMinimumLength(0);
        DEFAULT_POLICY.setPasswordMinimumSymbols(0);

        DEFAULT_POLICY.setDisableWifi(false);
        DEFAULT_POLICY.setDisableDataNetwork(false);
        DEFAULT_POLICY.setDisableBluetooth(false);

        DEFAULT_POLICY.setDisableBrowser(false);
        DEFAULT_POLICY.setDisableEmail(false);
        DEFAULT_POLICY.setDisableGallery(false);
        DEFAULT_POLICY.setDisableGmail(false);
        DEFAULT_POLICY.setDisableGoogleMap(false);
        DEFAULT_POLICY.setDisableGooglePlay(false);
        DEFAULT_POLICY.setDisableSettings(false);
        DEFAULT_POLICY.setDisableYouTubey(false);
        DEFAULT_POLICY.setBlackApps(new ArrayList<String>());
        DEFAULT_POLICY.setWhiteApps(new ArrayList<String>());
        DEFAULT_POLICY.setMustApps(new ArrayList<String>());

    }

    public static final String PASSWD_POLICY = "锁屏密码";
    public static final String MUST_APP_POLICY = "需要安装下列应用";
    public static final String BLACK_APP_POLICY = "需要卸载下列应用";
    public static final String CAMERA_POLICY = "相机";

    private PhoneInfoExtractor mPhoneInfoExtractor;
    private Context context;
    private DeviceAdminWorker mDeviceAdminWorker;

    private String policyFilePath;
    private SharedPreferences settings;
    private PolicyContent curPolicy;
    private String blackApps;
    private String mustApps;

    public static PolicyManager getMPolicyManager(Context context) {
        if (mPolicyManager == null) {
            mPolicyManager = new PolicyManager(context);
        }
        return mPolicyManager;
    }

    public PolicyContent getPolicy() {
        return curPolicy;
    }

    private PolicyManager(Context context) {
        this.context = context;

        settings = context.getSharedPreferences(PrefUtils.PREF_NAME, 0);
        policyFilePath = context.getApplicationContext().getFilesDir()
                .getAbsolutePath()
                + "/policy.last";
        importPolicy();
    }

    // 从配置文件中导入策略
    private void importPolicy() {
        blackApps = settings.getString(PrefUtils.BLACK_KEY, null);
        mustApps = settings.getString(PrefUtils.NECESSARY_KEY, null);

        // 导入策略
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        File f = new File(policyFilePath);
        if (f.exists()) {
            try {
                fis = new FileInputStream(f);
                ois = new ObjectInputStream(fis);
                curPolicy = (PolicyContent) ois.readObject();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (ois != null)
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }

        if (curPolicy == null) {
            // 读取失败，执行默认策略
            curPolicy = PolicyManager.DEFAULT_POLICY;
        }
    }


    // 向配置文件导出策略
    private void exportPolicy() {
        if (curPolicy != null) {
            File oldFile = new File(policyFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            try {
                fos = new FileOutputStream(policyFilePath);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(curPolicy);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PrefUtils.BLACK_KEY, this.blackApps);
        editor.putString(PrefUtils.NECESSARY_KEY, this.mustApps);
        editor.commit();
    }

    // 实施策略，policy:null说明重新执行当前策略
    @SuppressLint("InlinedApi")
    public void enforcePolicy() {
        // Timestamp timeStamp = new Timestamp(System.currentTimeMillis());//
        // 获取系统当前时间
        // String curTime = new SimpleDateFormat("HH:mm").format(timeStamp);

        // if(curTime.compareTo(policy.effectTimeStart) < 0 ||
        // curTime.compareTo(policy.effectTimeEnd) > 0){
        // 不在有效期内
        // return;
        // }

        if (mDeviceAdminWorker == null) {
            mDeviceAdminWorker = DeviceAdminWorker
                    .getDeviceAdminWorker(context);
        }

        mDeviceAdminWorker.setCameraDisabled(curPolicy.isDisableCamera());

        if (curPolicy.isConfigPasswdPolicy()) {

            mDeviceAdminWorker.setMaximumFailedPasswordsForWipe(curPolicy
                    .getMaximumFailedPasswordsForWipe());
            mDeviceAdminWorker.setMaximumTimeToLock(curPolicy
                    .getMaximumTimeToLock());

            String passwdQuality = curPolicy.getPasswdQuality();
            if (passwdQuality == null) {
                mDeviceAdminWorker
                        .setPasswdQuality(DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
            } else if (passwdQuality.equals("无") || passwdQuality.equals("")) {
                mDeviceAdminWorker
                        .setPasswdQuality(DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
            } else if (passwdQuality.equals("图案")) {
                mDeviceAdminWorker
                        .setPasswdQuality(DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK);
            } else if (passwdQuality.equals("数字")) {
                mDeviceAdminWorker
                        .setPasswdQuality(DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
            } else if (passwdQuality.equals("字母")) {
                mDeviceAdminWorker
                        .setPasswdQuality(DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC);
            } else if (passwdQuality.equals("数字和字母")) {
                mDeviceAdminWorker
                        .setPasswdQuality(DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC);
            } else {
                // 其它
                mDeviceAdminWorker
                        .setPasswdQuality(DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
            }

            mDeviceAdminWorker.setPasswordExpirationTimeout(curPolicy
                    .getPasswordExpirationTimeout());

            mDeviceAdminWorker.setPasswordHistoryLength(curPolicy
                    .getPasswordHistoryLength());

            mDeviceAdminWorker.setPasswordMinimumLength(curPolicy
                    .getPasswordMinimumLength());

            mDeviceAdminWorker.setPasswordMinimumSymbols(curPolicy
                    .getPasswordMinimumSymbols());
        } else {
            mDeviceAdminWorker
                    .setPasswdQuality(DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
            mDeviceAdminWorker.setMaximumFailedPasswordsForWipe(0);
            mDeviceAdminWorker.setMaximumTimeToLock(0);
            mDeviceAdminWorker.setPasswordExpirationTimeout(0);
            mDeviceAdminWorker.setPasswordHistoryLength(0);
            mDeviceAdminWorker.setPasswordMinimumLength(0);
            mDeviceAdminWorker.setPasswordMinimumSymbols(0);
        }

        mDeviceAdminWorker.setDisableWifi(curPolicy.isDisableWifi());
        mDeviceAdminWorker.setSsidWhiteList(curPolicy.getSsidWhiteList());
        mDeviceAdminWorker.setSsidBlackList(curPolicy.getSsidBlackList());
        mDeviceAdminWorker.setDisableDataNetwork(curPolicy
                .isDisableDataNetwork());
        mDeviceAdminWorker.setDisableBluetooth(curPolicy.isDisableBluetooth());

        mDeviceAdminWorker
                .setGooglePlayDisable(curPolicy.isDisableGooglePlay());
        mDeviceAdminWorker.setYouTubeDisable(curPolicy.isDisableYouTube());
        mDeviceAdminWorker.setEmailDisable(curPolicy.isDisableEmail());
        mDeviceAdminWorker.setBrowserDisable(curPolicy.isDisableBrowser());
        mDeviceAdminWorker.setSettingsDisable(curPolicy.isDisableSettings());
        mDeviceAdminWorker.setGalleryDisable(curPolicy.isDisableGallery());
        mDeviceAdminWorker.setGmailDisable(curPolicy.isDisableGmail());
        mDeviceAdminWorker.setGoogleMapDisable(curPolicy.isDisableGoogleMap());
        mDeviceAdminWorker.setBlackApps(curPolicy.getBlackApps());
        mDeviceAdminWorker.setWhiteApps(curPolicy.getWhiteApps());

    }


    public void updatePolicy() {
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET, "/client/devices/" + EmmClientApplication.mPhoneInfo.getIMEI() + "/policy",
                UpdateTokenRequest.TokenType.DEVICE, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                curPolicy = convertPolicy(response);
                enforcePolicy();
                exportPolicy();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error.networkResponse!=null && error.networkResponse.statusCode==404)
                {
                    curPolicy = convertPolicy(null);
                    enforcePolicy();
                    exportPolicy();
                }
            }
        });
        EmmClientApplication.mVolleyQueue.add(request);
    }

    // [{"id":10,"name":"New","content":"{\"passcode\":{\"passwordType\":\"图案\",\"allowSimple\":true,\"forcePIN\":true,\"minLength\":16,\"minComplexChars\":1,\"maxPINAgeInDays\":2,\"maxInactivity\":3,\"pinHistory\":4,\"maxGracePeriod\":\"5分钟\",\"maxFailedAttempts\":6}}","type":null,"effectTimeStart":null,"effectTimeEnd":null,"creator":null,"description":null,"priority":null,"created_at":"2014-11-26 06:17:49","updated_at":"2014-11-26 06:17:49"}]
    private PolicyContent convertPolicy(JSONObject jPolicy) {
        PolicyContent policy = new PolicyContent();
        if (jPolicy == null || jPolicy.toString().equals("") || jPolicy.toString().equals("{}")) {
            policy.setBlackApps(new ArrayList<String>());
            PolicyManager.this.blackApps = null;
            PolicyManager.this.mustApps = null;
            return PolicyManager.DEFAULT_POLICY;
        }

        try {
            policy.setName(jPolicy.getString("name"));
            policy.setVersion(jPolicy.getString("version"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return curPolicy;
        }
        try {
            policy.setType(jPolicy.getString("type"));
            policy.setEffectTimeStart(jPolicy.getString("effectTimeStart"));
            policy.setEffectTimeEnd(jPolicy.getString("effectTimeEnd"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String content;
        JSONObject jContent = null;
        try {
            content = jPolicy.getString("content");
            jContent = new JSONObject(content);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!jContent.isNull("passcode")) {
            try {
                policy.setConfigPasswdPolicy(true);
                String passcode = jContent.getString("passcode");
                JSONObject jPasscode = new JSONObject(passcode);
                if (!jPasscode.isNull("passwordType")) {
                    policy.setPasswdQuality(jPasscode.getString("passwordType"));
                }
                if (!jPasscode.isNull("minLength")) {
                    policy.setPasswordMinimumLength(jPasscode
                            .getInt("minLength"));
                }
                if (!jPasscode.isNull("minComplexChars")) {
                    policy.setPasswordMinimumSymbols(jPasscode
                            .getInt("minComplexChars"));
                }
                if (!jPasscode.isNull("maxFailedAttempts")) {
                    policy.setMaximumFailedPasswordsForWipe(jPasscode
                            .getInt("maxFailedAttempts"));
                }
                if (!jPasscode.isNull("maxInactivity")) {
                    policy.setMaximumTimeToLock(jPasscode
                            .getInt("maxInactivity")*60);
                }
                if (!jPasscode.isNull("maxPINAgeInDays")) {
                    policy.setPasswordExpirationTimeout(jPasscode
                            .getInt("maxPINAgeInDays"));
                }
                if (!jPasscode.isNull("pinHistory")) {
                    policy.setPasswordHistoryLength(jPasscode
                            .getInt("pinHistory"));
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (!jContent.isNull("restrictions")) {
            try {
                String restrictions = jContent.getString("restrictions");
                JSONObject jRestrictions = new JSONObject(restrictions);

                if (!jRestrictions.isNull("allowBluetooth"))
                    policy.setDisableBluetooth(!jRestrictions
                            .getBoolean("allowBluetooth"));
                if (!jRestrictions.isNull("allowCamera"))
                    policy.setDisableCamera(!jRestrictions
                            .getBoolean("allowCamera"));
                if (!jRestrictions.isNull("allowWifi"))
                    policy.setDisableWifi(!jRestrictions
                            .getBoolean("allowWifi"));
                if (!jRestrictions.isNull("allowDataNet"))
                    policy.setDisableDataNetwork(!jRestrictions
                            .getBoolean("allowDataNet"));

                if (!jRestrictions.isNull("allowGooglePlay"))
                    policy.setDisableGooglePlay(!jRestrictions
                            .getBoolean("allowGooglePlay"));
                if (!jRestrictions.isNull("allowAndroidYouTube"))
                    policy.setDisableYouTubey(!jRestrictions
                            .getBoolean("allowAndroidYouTube"));
                if (!jRestrictions.isNull("allowEmail"))
                    policy.setDisableEmail(!jRestrictions
                            .getBoolean("allowEmail"));
                if (!jRestrictions.isNull("allowBrowser"))
                    policy.setDisableBrowser(!jRestrictions
                            .getBoolean("allowBrowser"));
                if (!jRestrictions.isNull("allowConfig"))
                    policy.setDisableSettings(!jRestrictions
                            .getBoolean("allowConfig"));
                if (!jRestrictions.isNull("allowGallery"))
                    policy.setDisableGallery(!jRestrictions
                            .getBoolean("allowGallery"));
                if (!jRestrictions.isNull("allowGmail"))
                    policy.setDisableGmail(!jRestrictions
                            .getBoolean("allowGmail"));
                if (!jRestrictions.isNull("allowGoogleMap"))
                    policy.setDisableGoogleMap(!jRestrictions
                            .getBoolean("allowGoogleMap"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            policy.setDisableBluetooth(false);
            policy.setDisableCamera(false);
            policy.setDisableWifi(false);
            policy.setDisableDataNetwork(false);
            policy.setDisableGooglePlay(false);
            policy.setDisableYouTubey(false);
            policy.setDisableEmail(false);
            policy.setDisableBrowser(false);
            policy.setDisableSettings(false);
            policy.setDisableGallery(false);
            policy.setDisableGmail(false);
            policy.setDisableGoogleMap(false);
        }

        if (!jContent.isNull("app")) {
            try {
                String apps = jContent.getString("app");
                JSONObject jApps = new JSONObject(apps);
                StringBuilder blackAppStr = new StringBuilder("");
                ArrayList<String> blackApps = new ArrayList<String>();
                StringBuilder whiteAppStr = new StringBuilder("");
                ArrayList<String> whiteApps = new ArrayList<String>();
                StringBuilder mustApp = new StringBuilder("");
                ArrayList<String> mustApps = new ArrayList<String>();
                if (!jApps.isNull("blacks")) {
                    JSONArray array = jApps.getJSONArray("blacks");
                    // JSONArray array = new JSONArray(jApps.get("blacks"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject json = (JSONObject) array.get(i);
                        if (json.isNull("platform")
                                || !json.getString("platform")
                                .equals("ANDROID"))
                            continue;
                        blackApps.add(json.getString("package_name"));
                        blackAppStr.append(json.getString("name") + ":"
                                + json.getString("package_name") + ";");
                        // String op=json.getString("operate");
                        // if (op.equals("提示用户卸载应用") || op.equals("0"))
                        // blackAppWarn.append(json.getString("name") + ":" +
                        // json.getString("package") +";");
                        // else if (op.equals("阻止使用")||op.equals("1"))
                        // blackAppError.add(json.getString("package"));
                    }
                }
                if (!jApps.isNull("whites")) {
                    JSONArray array = jApps.getJSONArray("whites");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject json = (JSONObject) array.get(i);
                        if (json.isNull("platform")
                                || !json.getString("platform")
                                .equals("ANDROID"))
                            continue;
                        whiteAppStr.append(json.getString("name") + ":"
                                + json.getString("package_name") + ";");
                        whiteApps.add(json.getString("package_name"));
                    }
                }
                if (!jApps.isNull("musts")) {
                    JSONArray array = jApps.getJSONArray("musts");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject json = (JSONObject) array.get(i);
                        if (json.isNull("platform")
                                || !json.getString("platform")
                                .equals("ANDROID"))
                            continue;
                        mustApp.append(json.getString("name") + ":"
                                + json.getString("package_name") + ";");
                        mustApps.add(json.getString("package_name"));
                    }
                }

                policy.setBlackApps(blackApps);
                policy.setWhiteApps(whiteApps);
                policy.setMustApps(mustApps);
                PolicyManager.this.blackApps = blackAppStr.toString();
                PolicyManager.this.mustApps = mustApp.toString();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            policy.setBlackApps(new ArrayList<String>());
            policy.setWhiteApps(new ArrayList<String>());
            PolicyManager.this.blackApps = null;
            PolicyManager.this.mustApps = null;
        }

//        if (!jContent.isNull("certificates")) {
//            CertificateList certificateList = new CertificateList();
//            try {
//                JSONArray certArray = jContent.getJSONArray("certificates");
//                for (int i = 0; i < certArray.length(); i++) {
//                    JSONObject cert = (JSONObject) certArray.get(i);
//                    String id = cert.getString("id");
//                    String name = cert.getString("payloadCertificateFileName");
//                    certificateList.addCert(id, name);
//
//                }
//                DownloadCertTask downloadCertTask = new DownloadCertTask();
//                downloadCertTask.execute(certificateList);
//            } catch (JSONException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        } else {
//            CertificateList certificateList = new CertificateList();
//            DownloadCertTask downloadCertTask = new DownloadCertTask();
//            downloadCertTask.execute(certificateList);
//        }

        if (!jContent.isNull("wifis")) {
            JSONArray wifisArray;
            try {
                wifisArray = jContent.getJSONArray("wifis");
                for (int i = 0; i < wifisArray.length(); i++) {
                    JSONObject wifi = (JSONObject) wifisArray.get(i);
                    String ssid = wifi.getString("SSID_STR");
                    String passwd = wifi.getString("Password");
                    String encryptionType = wifi.getString("EncryptionType");
                    mDeviceAdminWorker.configWifi(ssid, passwd, encryptionType);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return policy;
    }

    public boolean accordToPolicyRestrictions() {
        boolean isAccord = true;
        List<PolicyItemStatus> status = getPolicyStatusDetails();

        for (PolicyItemStatus item : status) {
            // 黑名单App直接禁用，不要求强制卸载
            if (!item.itemName.equals(PolicyManager.BLACK_APP_POLICY)
                    && !item.isAccord) {
                return false;
            }
        }
        return isAccord;
    }

    public List<PolicyItemStatus> getPolicyStatusDetails() {
        List<PolicyItemStatus> details = new ArrayList<PolicyItemStatus>();

        if (curPolicy == null) {
            return details;
        }

        if (mDeviceAdminWorker == null) {
            mDeviceAdminWorker = DeviceAdminWorker
                    .getDeviceAdminWorker(context);
        }

        // PolicyItemStatus cameraItem = new PolicyItemStatus();
        // cameraItem.itemName = PolicyManager.CAMERA_POLICY;
        // cameraItem.itemDetail = "目前:"
        // + (mDeviceAdminWorker.getCameraDisabled() ? "已禁用" : "可用");
        // cameraItem.isAccord = (mDeviceAdminWorker.getCameraDisabled() ==
        // curPolicy
        // .isDisableCamera());
        // details.add(cameraItem);

        PolicyItemStatus passwdItem = new PolicyItemStatus();
        passwdItem.itemName = PolicyManager.PASSWD_POLICY;

        int maxFailTime = mDeviceAdminWorker.getMaximumFailedPasswordsForWipe();
        long maxLock = mDeviceAdminWorker.getMaximumTimeToLock();
        String quality = mDeviceAdminWorker.getPasswordQuality();
        long expire = mDeviceAdminWorker.getPasswordExpirationTimeout();
        int history = mDeviceAdminWorker.getPasswordHistoryLength();
        int length = mDeviceAdminWorker.getPasswordMinimumLength();
        int symbols = mDeviceAdminWorker.getPasswordMinimumSymbols();

        passwdItem.isAccord = mDeviceAdminWorker.isPasswdSufficient();
        if (passwdItem.isAccord) {
            passwdItem.itemDetail = "锁屏密码满足要求";
        } else {
            passwdItem.itemDetail = "请重设密码，要求如下:\n" + "  密码最多失败次数:"
                    + (maxFailTime < 0 ? "未知" : maxFailTime) + "\n"
                    + "  自动锁屏时间:" + (maxLock < 0 ? "未知" : (maxLock + "秒"))
                    + "\n" + "  密码类型:" + quality + "\n" + "  密码有效期:"
                    + (expire < 0 ? "未知" : (expire + "天")) + "\n" + "  密码历史:"
                    + (history < 0 ? "未知" : history) + "\n" + "  密码最小长度:"
                    + (length < 0 ? "未知" : length) + "\n" + "  密码最少包含复杂字符个数:"
                    + (symbols < 0 ? "未知" : symbols) + "\n";
        }

        details.add(passwdItem);

        PolicyItemStatus blackItem = new PolicyItemStatus();
        blackItem.itemName = PolicyManager.BLACK_APP_POLICY;

        StringBuilder blackSb = new StringBuilder("");
        if (blackApps != null) {
            String[] pkgs = blackApps.split(";");
            for (String pkg : pkgs) {
                String[] tmp = pkg.split(":");
                if (tmp.length >= 2 && getVersionCode(tmp[1]) != -1) {
                    blackSb.append("  " + tmp[0] + "\n");
                }
            }
        }
        blackItem.itemDetail = blackSb.toString();
        blackItem.isAccord = (blackSb.length() <= 0);
        details.add(blackItem);

        PolicyItemStatus mustItem = new PolicyItemStatus();
        mustItem.itemName = PolicyManager.MUST_APP_POLICY;

        StringBuilder mustSb = new StringBuilder("");

        if (mustApps != null) {
            String[] pkgs = mustApps.split(";");
            for (String pkg : pkgs) {
                String[] tmp = pkg.split(":");
                if (tmp.length >= 2 && getVersionCode(tmp[1]) == -1) {
                    mustSb.append("  " + tmp[0] + "\n");
                }
            }
        }
        mustItem.itemDetail = mustSb.toString();
        mustItem.isAccord = mustSb.length() <= 0;
        details.add(mustItem);
        return details;
    }

    private int getVersionCode(String pkgName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(pkgName, 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static class PolicyContent implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private String name;
        private String version;
        public String type;
        public String effectTimeStart;
        public String effectTimeEnd;

        /*
         * all supported operations
         */
        private String alertLevel;
        private boolean disableCamera;
        private boolean isConfigPasswdPolicy;
        private int maximumFailedPasswordsForWipe;
        private int maximumTimeToLock;
        private String passwdQuality;
        private int passwordExpirationTimeout;
        private int passwordHistoryLength;
        private int passwordMinimumLength;
        private int passwordMinimumSymbols;

        private boolean disableWifi;
        private boolean disableDataNetwork;
        private boolean disableBluetooth;
        private List<String> ssidWhiteList = new ArrayList<String>();
        private List<String> ssidBlackList = new ArrayList<String>();

        private boolean disableGooglePlay;
        private boolean disableYouTube;
        private boolean disableEmail;
        private boolean disableBrowser;
        private boolean disableSettings;
        private boolean disableGallery;
        private boolean disableGmail;
        private boolean disableGoogleMap;
        private List<String> blackApps = new ArrayList<String>();
        private List<String> whiteApps = new ArrayList<String>();
        private List<String> mustApps = new ArrayList<String>();

        public String getAlertLevel() {
            return alertLevel;
        }

        public void setAlertLevel(String alertLevel) {
            this.alertLevel = alertLevel;
        }

        public boolean isDisableCamera() {
            return disableCamera;
        }

        public void setDisableCamera(boolean disableCamera) {
            this.disableCamera = disableCamera;
        }

        public boolean isConfigPasswdPolicy() {
            return isConfigPasswdPolicy;
        }

        public void setConfigPasswdPolicy(boolean isConfigPasswdPolicy) {
            this.isConfigPasswdPolicy = isConfigPasswdPolicy;
        }

        public int getMaximumFailedPasswordsForWipe() {
            return maximumFailedPasswordsForWipe;
        }

        public void setMaximumFailedPasswordsForWipe(
                int maximumFailedPasswordsForWipe) {
            this.maximumFailedPasswordsForWipe = maximumFailedPasswordsForWipe;
        }

        public int getMaximumTimeToLock() {
            return maximumTimeToLock;
        }

        public void setMaximumTimeToLock(int maximumTimeToLock) {
            this.maximumTimeToLock = maximumTimeToLock;
        }

        public String getPasswdQuality() {
            return passwdQuality;
        }

        public void setPasswdQuality(String passwdQuality) {
            this.passwdQuality = passwdQuality;
        }

        public int getPasswordExpirationTimeout() {
            return passwordExpirationTimeout;
        }

        public void setPasswordExpirationTimeout(int passwordExpirationTimeout) {
            this.passwordExpirationTimeout = passwordExpirationTimeout;
        }

        public int getPasswordHistoryLength() {
            return passwordHistoryLength;
        }

        public void setPasswordHistoryLength(int passwordHistoryLength) {
            this.passwordHistoryLength = passwordHistoryLength;
        }

        public int getPasswordMinimumLength() {
            return passwordMinimumLength;
        }

        public void setPasswordMinimumLength(int passwordMinimumLength) {
            this.passwordMinimumLength = passwordMinimumLength;
        }

        public int getPasswordMinimumSymbols() {
            return passwordMinimumSymbols;
        }

        public void setPasswordMinimumSymbols(int passwordMinimumSymbols) {
            this.passwordMinimumSymbols = passwordMinimumSymbols;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getNameAndVersion() {
            if (version == null) return name;
            return name + "(version:" + version + ")";
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getEffectTimeStart() {
            return effectTimeStart;
        }

        public void setEffectTimeStart(String effectTimeStart) {
            this.effectTimeStart = effectTimeStart;
        }

        public String getEffectTimeEnd() {
            return effectTimeEnd;
        }

        public void setEffectTimeEnd(String effectTimeEnd) {
            this.effectTimeEnd = effectTimeEnd;
        }

        public boolean isDisableWifi() {
            return disableWifi;
        }

        public void setDisableWifi(boolean disableWifi) {
            this.disableWifi = disableWifi;
        }

        public boolean isDisableDataNetwork() {
            return disableDataNetwork;
        }

        public void setDisableDataNetwork(boolean disableDataNetwork) {
            this.disableDataNetwork = disableDataNetwork;
        }

        public boolean isDisableBluetooth() {
            return disableBluetooth;
        }

        public void setDisableBluetooth(boolean disableBluetooth) {
            this.disableBluetooth = disableBluetooth;
        }

        public List<String> getSsidWhiteList() {
            return ssidWhiteList;
        }

        public void setSsidWhiteList(List<String> ssidWhiteList) {
            this.ssidWhiteList.clear();
            for (String s : ssidWhiteList)
                this.ssidWhiteList.add(s);
        }

        public List<String> getSsidBlackList() {
            return ssidBlackList;
        }

        public void setSsidBlackList(List<String> ssidBlackList) {
            this.ssidBlackList.clear();
            for (String s : ssidBlackList)
                this.ssidBlackList.add(s);
        }

        public boolean isDisableGooglePlay() {
            return disableGooglePlay;
        }

        public void setDisableGooglePlay(boolean disableGooglePlay) {
            this.disableGooglePlay = disableGooglePlay;
        }

        public boolean isDisableYouTube() {
            return disableYouTube;
        }

        public void setDisableYouTubey(boolean disableYouTube) {
            this.disableYouTube = disableYouTube;
        }

        public boolean isDisableEmail() {
            return disableEmail;
        }

        public void setDisableEmail(boolean disableEmail) {
            this.disableEmail = disableEmail;
        }

        public boolean isDisableBrowser() {
            return disableBrowser;
        }

        public void setDisableBrowser(boolean disableBrowser) {
            this.disableBrowser = disableBrowser;
        }

        public boolean isDisableSettings() {
            return disableSettings;
        }

        public void setDisableSettings(boolean disableSettings) {
            this.disableSettings = disableSettings;
        }

        public boolean isDisableGallery() {
            return disableGallery;
        }

        public void setDisableGallery(boolean disableGallery) {
            this.disableGallery = disableGallery;
        }

        public boolean isDisableGmail() {
            return disableGmail;
        }

        public void setDisableGmail(boolean disableGmail) {
            this.disableGmail = disableGmail;
        }

        public boolean isDisableGoogleMap() {
            return disableGoogleMap;
        }

        public void setDisableGoogleMap(boolean disableGoogleMap) {
            this.disableGoogleMap = disableGoogleMap;
        }

        public List<String> getBlackApps() {
            return blackApps;
        }

        public void setBlackApps(List<String> blacks) {
            blackApps.clear();
            for (String b : blacks)
                this.blackApps.add(b);
        }

        public List<String> getWhiteApps() {
            return whiteApps;
        }

        public void setWhiteApps(List<String> whites) {
            whiteApps.clear();
            for (String b : whites)
                this.whiteApps.add(b);
        }

        public List<String> getMustApps() {
            return mustApps;
        }

        public void setMustApps(List<String> musts) {
            mustApps.clear();
            for (String b : musts)
                this.mustApps.add(b);
        }
    }

    class DownloadCertTask extends AsyncTask<CertificateList, Void, Void> {

        @Override
        protected Void doInBackground(CertificateList... params) {
            String ip = NetworkDef.getAddrWebservice();
            if (ip == null)
                return null;
            // 创建连接
            CheckAccount account = CheckAccount
                    .getCheckAccountInstance(context);
            String url = NetworkDef.PROTOCOL + ip
                    + "/api/v1/certificates/assets/";
            CertificateList certificateList = params[0];
            Map<String, String> nameUrl = new HashMap<String, String>();
            for (int i = 0; i < certificateList.length(); i++) {
                Cert cert = certificateList.getCert(i);
                nameUrl.put(cert.name, url + cert.id + "?access_token="
                        + account.getAccessToken());

            }
            DownLoadCertFromUrl.startDownloadCertList(context, nameUrl);
            return null;
        }

    }

    public static class PolicyItemStatus {
        public String itemName;
        public String itemDetail;
        public boolean isAccord;
    }
}