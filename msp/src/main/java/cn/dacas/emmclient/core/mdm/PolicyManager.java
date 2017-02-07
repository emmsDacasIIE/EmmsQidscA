package cn.dacas.emmclient.core.mdm;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.Context;

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
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.core.mam.AppManager;
import cn.dacas.emmclient.model.MdmWifiModel;
import cn.dacas.emmclient.util.PhoneInfoExtractor;
import cn.dacas.emmclient.util.RandomUtils;
import cn.dacas.emmclient.util.WifiAdmin;
import cn.dacas.emmclient.webservice.qdvolley.MyJsonObjectRequest;
import cn.dacas.emmclient.webservice.qdvolley.UpdateTokenRequest;

//目前执行的策略被保存在一个配置文件中 policy.last
public class PolicyManager {
    private static PolicyManager mPolicyManager = null;
    private static PolicyContent DEFAULT_POLICY = PolicyContent.getDefaultPolicyContent();


    /*static {
        DEFAULT_POLICY.setPolicyId(0);
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
        DEFAULT_POLICY.setBlackApps(null);
        DEFAULT_POLICY.setWhiteApps(null);
        DEFAULT_POLICY.setMustApps(null);
    }*/

    public static final String PASSWD_POLICY = "锁屏密码";
    public static final String MUST_APP_POLICY = "需要安装下列应用";
    public static final String BLACK_APP_POLICY = "需要卸载下列应用";
    public static final String CAMERA_POLICY = "相机";

//    private PhoneInfoExtractor mPhoneInfoExtractor;
    private Context mContext;

    private String policyFilePath;
    private PolicyContent curPolicy;

    public static PolicyManager getMPolicyManager(Context context) {
        if (mPolicyManager == null) {
            mPolicyManager = new PolicyManager(context);
        }
        return mPolicyManager;
    }

    public PolicyContent getPolicy() {
        return curPolicy;
    }

    public void setPolicy(PolicyContent policyContent){
        this.curPolicy = policyContent;
    }

    private PolicyManager(Context context) {
        this.mContext = context;
        policyFilePath = context.getApplicationContext().getFilesDir()
                .getAbsolutePath()
                +"/policy.last";
        importPolicy();
    }

    // 从配置文件中导入策略
    private void importPolicy() {
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
                e.printStackTrace();
            } finally {
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 实施策略，policy:null说明重新执行当前策略
    @SuppressLint("InlinedApi")
    private void enforcePolicy() {
        DeviceAdminWorker mDeviceAdminWorker =
                DeviceAdminWorker.getDeviceAdminWorker(mContext);

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

        if (curPolicy.isDisableBluetooth())
            mDeviceAdminWorker.setBluetoothState(false);
        if (curPolicy.isDisableWifi())
            mDeviceAdminWorker.setWifiState(false);
        if (curPolicy.isDisableDataNetwork())
            mDeviceAdminWorker.setDataConnection(false);
        if (!curPolicy.isDisableWifi()) {
            WifiAdmin wifiAdmin=new WifiAdmin(mContext);
            String ssid=wifiAdmin.getWifiInfo().getSSID();
            if (curPolicy.getSsidWhiteList().size()>0) {
                if (!curPolicy.getSsidWhiteList().contains(ssid))
                    mDeviceAdminWorker.setWifiState(false);
            }
            else {
                if (curPolicy.getSsidBlackList().contains(ssid))
                    mDeviceAdminWorker.setWifiState(false);
            }
        }

        for (MdmWifiModel wifi:curPolicy.getWifis())
             mDeviceAdminWorker.configWifi(wifi.getSsid(), wifi.getPassword(), wifi.getEncryptionType());

    }


    public void updatePolicy() {
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET, "/client/devices/" + PhoneInfoExtractor.getIMEI(EmmClientApplication.getContext()) + "/policy",
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
                //未分配策略
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

    public void updatePolicy(JSONObject jsonObject){
        setPolicy(convertPolicy(jsonObject));
        enforcePolicy();
        exportPolicy();
    }

    public void resetPolicy(){
        setPolicy(PolicyContent.getDefaultPolicyContent());
        enforcePolicy();
        exportPolicy();
    }

    // [{"id":10,"name":"New","content":"{\"passcode\":{\"passwordType\":\"图案\",\"allowSimple\":true,\"forcePIN\":true,\"minLength\":16,\"minComplexChars\":1,\"maxPINAgeInDays\":2,\"maxInactivity\":3,\"pinHistory\":4,\"maxGracePeriod\":\"5分钟\",\"maxFailedAttempts\":6}}","type":null,"effectTimeStart":null,"effectTimeEnd":null,"creator":null,"description":null,"priority":null,"created_at":"2014-11-26 06:17:49","updated_at":"2014-11-26 06:17:49"}]
    private PolicyContent convertPolicy(JSONObject jPolicy) {
        PolicyContent policy = new PolicyContent();
        if (jPolicy == null || jPolicy.toString().equals("") || jPolicy.toString().equals("{}")) {
            return PolicyManager.DEFAULT_POLICY;
        }

        try {
            policy.setPolicyId(jPolicy.getLong("id"));
            policy.setName(jPolicy.getString("name"));
            policy.setVersion(jPolicy.getString("version"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return curPolicy;
        }
        try {
            if(jPolicy.has("Organization"))
                policy.setType("Organization");
            if(jPolicy.has("effectTimeStart"))
                policy.setEffectTimeStart(jPolicy.getString("effectTimeStart"));
            if(jPolicy.has("effectTimeEnd"))
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

                //处理wifi黑白名单
                if (!jRestrictions.isNull("blackSSIDs")) {
                    ArrayList<String> bList=new ArrayList<>();
                    JSONArray array=jRestrictions.getJSONArray("blackSSIDs");
                    for (int i=0;i<array.length();i++) {
                        String black = "\"" + array.getString(i) + "\"";
                        bList.add(black);
                    }
                    policy.setSsidBlackList(bList);
                }

                if (!jRestrictions.isNull("whiteSSIDs")) {
                    ArrayList<String> wList=new ArrayList<>();
                    JSONArray array=jRestrictions.getJSONArray("whiteSSIDs");
                    for (int i=0;i<array.length();i++) {
                        String white = "\"" + array.getString(i) + "\"";
                        wList.add(white);
                    }
                    policy.setSsidWhiteList(wList);
                }
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
                ArrayList<String> blackApps = new ArrayList<String>();
                ArrayList<String> whiteApps = new ArrayList<String>();
                ArrayList<String> mustApps = new ArrayList<String>();
                if (!jApps.isNull("blacks")) {
                    JSONArray array = jApps.getJSONArray("blacks");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject json = (JSONObject) array.get(i);
                        if (json.isNull("platform")
                                || !json.getString("platform")
                                .equals("ANDROID"))
                            continue;
                        blackApps.add(json.getString("package_name"));
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
                        mustApps.add(json.getString("package_name"));
                    }
                }

                policy.setBlackApps(blackApps);
                policy.setWhiteApps(whiteApps);
                policy.setMustApps(mustApps);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            policy.setBlackApps(null);
            policy.setWhiteApps(null);
        }

        if (!jContent.isNull("wifis")) {
            JSONArray wifisArray;
            try {
                wifisArray = jContent.getJSONArray("wifis");
                policy.clearWifis();
                for (int i = 0; i < wifisArray.length(); i++) {
                    JSONObject wifi = (JSONObject) wifisArray.get(i);
                    String ssid = wifi.getString("SSID_STR");
                    String passwd = wifi.getString("Password");
                    String encryptionType = wifi.getString("EncryptionType");
                    policy.addWifi(ssid,passwd,encryptionType);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else policy.clearWifis();
        return policy;
    }


    public int getPolicyScore() {
        List<PolicyItemStatus> status = getPolicyStatusDetails();
        int score=0;
        for (PolicyItemStatus item : status) {
           score=score+item.score;
        }
        return  score;
    }

    public List<PolicyItemStatus> getPolicyStatusDetails() {
        List<PolicyItemStatus> details = new ArrayList<PolicyItemStatus>();

        if (curPolicy == null) {
            return details;
        }

        DeviceAdminWorker mDeviceAdminWorker=DeviceAdminWorker.getDeviceAdminWorker(mContext);


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
            passwdItem.score=30;
        } else {
            passwdItem.itemDetail = "请重设密码，要求如下:\n" + "  密码最多失败次数:"
                    + (maxFailTime < 0 ? "未知" : maxFailTime) + "\n"
                    + "  自动锁屏时间:" + (maxLock < 0 ? "未知" : (maxLock + "秒"))
                    + "\n" + "  密码类型:" + quality + "\n" + "  密码有效期:"
                    + (expire < 0 ? "未知" : (expire + "天")) + "\n" + "  密码历史:"
                    + (history < 0 ? "未知" : history) + "\n" + "  密码最小长度:"
                    + (length < 0 ? "未知" : length) + "\n" + "  密码最少包含复杂字符个数:"
                    + (symbols < 0 ? "未知" : symbols) + "\n";
            passwdItem.score= RandomUtils.getRandomInt(0,10);
        }

        details.add(passwdItem);

        PolicyItemStatus blackItem = new PolicyItemStatus();
        blackItem.itemName = PolicyManager.BLACK_APP_POLICY;
        blackItem.score=50;
        StringBuilder blackSb = new StringBuilder("");
        for (String pkgName:curPolicy.getBlackApps()) {
            if (AppManager.checkInstallResult(mContext,pkgName)) {
                blackSb.append("  " + pkgName + "\n");
                if (blackItem.score>=10) blackItem.score=blackItem.score-10;
            }
        }
        blackItem.itemDetail = blackSb.toString();
        blackItem.isAccord = (blackSb.length() <= 0);
        details.add(blackItem);

        PolicyItemStatus mustItem = new PolicyItemStatus();
        mustItem.itemName = PolicyManager.MUST_APP_POLICY;
        mustItem.score=20;
        StringBuilder mustSb = new StringBuilder("");
        for (String pkgName:curPolicy.getMustApps()) {
            if (!AppManager.checkInstallResult(mContext,pkgName)) {
                mustSb.append("  " + pkgName + "\n");
                if (mustItem.score>=10) mustItem.score=mustItem.score-10;
            }
        }
        mustItem.itemDetail = mustSb.toString();
        mustItem.isAccord = mustSb.length() <= 0;
        details.add(mustItem);
        return details;
    }

    public static class PolicyItemStatus {
        public String itemName;
        public String itemDetail;
        public boolean isAccord;
        public int score;
    }
}