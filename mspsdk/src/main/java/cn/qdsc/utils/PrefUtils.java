package cn.qdsc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

/**
 * Created by lenovo on 2015-12-11.
 */
public class PrefUtils {
        private static final String QdPref="QdscPreference";
        private SharedPreferences pref;

        public PrefUtils(Context ctx) {
            pref=ctx.getSharedPreferences(QdPref,Context.MODE_PRIVATE);
        }

        public String getRootKeyRandom() {
            return pref.getString("RootKey_Random",null);
        }

        public void setRootKeyRandom(String key) {
            SharedPreferences.Editor editor=pref.edit();
            editor.putString("RootKey_Random",key);
            editor.commit();
        }

        public String getChaosKeyInfo(String fileName) {
            return pref.getString(fileName, null);
        }

        public boolean containsKey(String fileName) {
            return pref.contains(fileName);
        }

        public void setChaosKeyInfo(String fileName,String chaosKey) {
            SharedPreferences.Editor editor=pref.edit();
            editor.putString(fileName,chaosKey);
            editor.commit();
        }

        public void deleteChaosKeyInfo(String fileName) {
            SharedPreferences.Editor editor=pref.edit();
            editor.remove(fileName);
            editor.commit();
        }

        private static final String MSP_SHARE_SETTING = "MSPSHARESETTING";

        public static  String getVpnSettings(Context mContext) {
            try {
                Context otherAppContext = mContext.createPackageContext("cn.qdsc.msp", Context.CONTEXT_IGNORE_SECURITY);
                SharedPreferences share = otherAppContext.getSharedPreferences(MSP_SHARE_SETTING,Context.MODE_WORLD_READABLE|Context.MODE_MULTI_PROCESS);
                return share.getString("VPN",null);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
}
