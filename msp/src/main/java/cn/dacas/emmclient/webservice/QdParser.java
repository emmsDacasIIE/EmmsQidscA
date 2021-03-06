package cn.dacas.emmclient.webservice;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.dacas.emmclient.model.DeviceModel;
import cn.dacas.emmclient.model.MamAppInfoModel;
import cn.dacas.emmclient.model.McmMessageModel;
import cn.dacas.emmclient.model.TokenModel;
import cn.dacas.emmclient.model.UserModel;

/**
 * Created by lenovo on 2016-1-15.
 */
public class QdParser {
    public static ArrayList<MamAppInfoModel> parseAppList(JSONArray array) throws JSONException {
        Gson gson = new Gson();
        ArrayList<MamAppInfoModel>  appList = gson.fromJson(array.toString(), new TypeToken<ArrayList<MamAppInfoModel>>(){}.getType());
        try {
            for (int i = appList.size()-1; i>=0 ;i--) {
                MamAppInfoModel app = appList.get(i);
                JSONObject obj = (JSONObject)array.get(i);
                if(app.isWeb() && obj.has("url"))
                    app.file_name=obj.getString("url");

                //clear unavailable data
                if(app.isApk() && app.pkgName ==null)
                    appList.remove(i);
                if(app.isWeb() && app.file_name == null)
                    appList.remove(i);
            }
        } catch (JSONException e) {
            throw  e;
        }
        return appList;
    }

    public static ArrayList<McmMessageModel> parseMsgList(JSONArray array) {
        Gson gson = new Gson();
        ArrayList<McmMessageModel>  msgList = gson.fromJson(array.toString(), new TypeToken<ArrayList<McmMessageModel>>(){}.getType());
        return msgList;
    }

    public static UserModel parseUserInfo(JSONObject object) {
        Gson gson=new Gson();
        UserModel user = gson.fromJson(object.toString(), UserModel.class);
        return user;
    }

    public static TokenModel parseToken(String str) {
        Gson gson=new Gson();
        TokenModel token = gson.fromJson(str, TokenModel.class);
        return token;
    }

    public static TokenModel parseToken(JSONObject object) {
        Gson gson=new Gson();
        TokenModel token = gson.fromJson(object.toString(), TokenModel.class);
        return token;
    }

    public static DeviceModel parseDevice(JSONObject object ) {
        Gson gson=new Gson();
        DeviceModel device = gson.fromJson(object.toString(), DeviceModel.class);
        return device;
    }
}
