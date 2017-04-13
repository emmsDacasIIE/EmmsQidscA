package cn.dacas.emmsupdatesdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/** check versions of APP
 * Created by Sun RX on 2016-11-9.
 */

public class VersionUpdataChecker {
    String downloadUrl;
    String clientID;
    String clientKey;
    String currentVersionName;
    int currentVersionCode;
    String avaliableVersionName;
    int avaliableVersionCode;
    String serverUrl;

    private TokenModel mTokenModel;

    public VersionUpdataChecker(Context context) {
        currentVersionCode = getPackageVersionCode(context);
        currentVersionName = getPackageVersionName(context);
    }

    /**
     * set params of authentication in Https requests
     *
     * @param serverUrl Web Server Address
     * @param clientID  The ID which the Server assigns to the app;
     * @param clientKey The key which the Server assigns to the app;
     */
    public void authenticationConfigation(String serverUrl, String clientID, String clientKey) {
        this.serverUrl = serverUrl;
        this.clientID = clientID;
        this.clientKey = clientKey;
    }

    /**
     * 获取版本号 Version Name
     *
     * @return 当前应用的版本号
     */
    public static String getPackageVersionName(Context context) {
        try {
            String pkgName = context.getPackageName();
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(pkgName, 0); //PackageManager.GET_CONFIGURATIONS
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param context the context of the app
     *
     * @return get the version code of the app
     */
    public static int getPackageVersionCode(Context context) {
        try {
            String pkgName = context.getPackageName();
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(pkgName, 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private TokenModel getTokenModelFromServer(String serverUrl, HashMap<String, String> headers) {
        TokenModel tokenModel = null;
        HttpsURLConnection connection = null;
        try {
            //?grant_type=client_credentials
            // &client_id=2b5a38705d7b3562655925406a652e65
            // &client_secret=234f523128212d6e70634446224c2a48
            URL url = new URL(serverUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }

            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            JSONObject tokenResponse = new JSONObject(response.toString());
            tokenModel =  new TokenModel(tokenResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return tokenModel;
    }
}
