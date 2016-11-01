package cn.dacas.emmclient.webservice;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.manager.UrlManager;
import cn.dacas.emmclient.model.DeviceModel;
import cn.dacas.emmclient.model.MamAppInfoModel;
import cn.dacas.emmclient.model.McmMessageModel;
import cn.dacas.emmclient.model.TokenModel;
import cn.dacas.emmclient.model.UserModel;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.webservice.qdvolley.MyJsonArrayRequest;
import cn.dacas.emmclient.webservice.qdvolley.MyJsonObjectRequest;
import cn.dacas.emmclient.webservice.qdvolley.UpdateTokenRequest;

/**
 * Created by lenovo on 2016-1-6.
 */
public class QdWebService {

    public static void fetchUserToken(final String username,final String password,final Response.Listener<TokenModel> listener,Response.ErrorListener errorListener) {
        //final String url = "https://" + AddressManager.getAddrWebservice() + "/api/v1/oauth/token";
        final String url = UrlManager.getTokenServiceUrl();
        StringRequest requestApplyToken = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        TokenModel token=QdParser.parseToken(response);
                        listener.onResponse(token);
                    }
                }, errorListener)
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                params.put("grant_type", "password");
                params.put("client_id", "302a7d556175264c7e5b326827497349");
                params.put("client_secret", "4770414c283a20347c7b553650425773");
                return params;
            }
        };
        requestApplyToken.setRetryPolicy(new DefaultRetryPolicy(5000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        EmmClientApplication.mVolleyQueue.add(requestApplyToken);
    }

    /**
     * 使用用户的Token登陆，如果Token过期，将自动更新token
     * @param listener
     * @param errorListener
     */
    public static void login(final Response.Listener<DeviceModel> listener,Response.ErrorListener errorListener) {
        MyJsonObjectRequest requestLogin = new MyJsonObjectRequest(
                Request.Method.POST,
                "/user/devices/" + EmmClientApplication.mPhoneInfoExtractor.getIMEI()+"/login",
                UpdateTokenRequest.TokenType.USER,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        DeviceModel device=QdParser.parseDevice(response);
                        EmmClientApplication.mDeviceModel = device;
                        if (listener!=null)
                            listener.onResponse(device);
                    }
                },
                errorListener);
        EmmClientApplication.mVolleyQueue.add(requestLogin);
    }

    public static void submitFeedback(final String username,final String content, Response.Listener<JSONObject> listener,Response.ErrorListener errorListener) {
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.POST, "/user/feedback",
                UpdateTokenRequest.TokenType.USER, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }

            @Override
            public byte[] getBody() {
                Map<String, String> map = new HashMap<>();
                map.put("username",username);
                map.put("content",content);
                String ms = new JSONObject(map).toString();
                return ms.getBytes();
            }
        };
        EmmClientApplication.mVolleyQueue.add(request);
    }

    public static void getIpSettings(Response.Listener<JSONObject> listener,Response.ErrorListener errorListener) {
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET, "/client/settings/android",
                UpdateTokenRequest.TokenType.NONE, listener, errorListener) {
        };
        EmmClientApplication.mVolleyQueue.add(request);
    }

    public static void getUserInformation(final Response.Listener<UserModel> listener,Response.ErrorListener errorListener) {
        MyJsonObjectRequest jsonObjectRequest=new MyJsonObjectRequest(Request.Method.GET, "/user", UpdateTokenRequest.TokenType.USER,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        UserModel user = QdParser.parseUserInfo(response);
                        if (listener!=null)
                            listener.onResponse(user);
                    }
                },errorListener);
        EmmClientApplication.mVolleyQueue.add(jsonObjectRequest);
    }

    public static void submitUserInformation(final String name,final String email,final String telephone,
                                             final Response.Listener<UserModel> listener,Response.ErrorListener errorListener) {
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.PUT, "/users/" + EmmClientApplication.mCheckAccount.getCurrentAccount(),
                UpdateTokenRequest.TokenType.USER, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                UserModel user = QdParser.parseUserInfo(response);
                if (listener!=null)
                    listener.onResponse(user);
            }
        }, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }

            @Override
            public byte[] getBody() {
                Map<String, String> map = new HashMap<>();
                map.put("name",name);
                map.put("email",email);
                map.put("telephone_number",telephone);
                String ms = new JSONObject(map).toString();
                return ms.getBytes();
            }
        };
        EmmClientApplication.mVolleyQueue.add(request);
    }

    public static void getAppList(final Response.Listener<ArrayList<MamAppInfoModel>> listener, final Response.ErrorListener errorListener) {
        String url = "/user/apps?platforms=ANDROID";
        MyJsonArrayRequest jsonArrayRequest = new MyJsonArrayRequest(Request.Method.GET, url, UpdateTokenRequest.TokenType.USER,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            ArrayList<MamAppInfoModel> appList=QdParser.parseAppList(response);
                            if (listener!=null)
                                listener.onResponse(appList);
                        } catch (JSONException e) {
                            if (errorListener!=null)
                             errorListener.onErrorResponse(new ParseError());
                        }
                    }
                    },errorListener);
        EmmClientApplication.mVolleyQueue.add(jsonArrayRequest);
    }

    public static void getMessageList(final Response.Listener<ArrayList<McmMessageModel>> listener,Response.ErrorListener errorListener) {
        int maxId= PrefUtils.getMsgMaxId();
        String url = "/client/devices/" + EmmClientApplication.mPhoneInfoExtractor.getIMEI() +"/messages?message_id=" + maxId;
        MyJsonArrayRequest jsonArrayRequest = new MyJsonArrayRequest(Request.Method.GET, url, UpdateTokenRequest.TokenType.DEVICE,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<McmMessageModel> msgList=QdParser.parseMsgList(response);
                        if (listener!=null)
                            listener.onResponse(msgList);
                    }
                },errorListener);
        EmmClientApplication.mVolleyQueue.add(jsonArrayRequest);
    }

}
