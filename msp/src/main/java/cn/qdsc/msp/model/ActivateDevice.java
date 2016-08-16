package cn.qdsc.msp.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.webservice.QdParser;
import cn.qdsc.msp.webservice.qdvolley.LicenceError;
import cn.qdsc.msp.event.MessageEvent;
import cn.qdsc.msp.security.AESUtil;
import cn.qdsc.msp.manager.AddressManager;
import cn.qdsc.msp.util.PhoneInfoExtractor;
import cn.qdsc.msp.util.PrefUtils;
import de.greenrobot.event.EventBus;

/** 绑定负责任人逻辑类*/
public class ActivateDevice {
    /*
     * singleton
     */

    static Context mContext;

    private static ActivateDevice mActivateDevice = null;

    public static ActivateDevice getActivateDeviceInstance(Context context) {
        if (mActivateDevice == null) {
            mActivateDevice = new ActivateDevice(context);
        }
        return mActivateDevice;
    }

    private SharedPreferences settings = null;

    public static boolean online = false;
    public static long lastOnlineTime = 0;
    private String deviceType = null;

    private ActivateDevice(Context context) {

        mContext = context.getApplicationContext();
        settings = context.getSharedPreferences(PrefUtils.PREF_NAME, 0);

        this.deviceReported = settings.getBoolean(PrefUtils.DEVICE_REPORTED,
                false);

        if (this.isDeviceReported()) {

            this.deviceBinder = PrefUtils.getAdministrator();

            String nameChaos = settings.getString(PrefUtils.BINDER_NAME, null);
            this.binderName = (nameChaos == null) ? null : AESUtil.decrypt(
                    PrefUtils.BINDER_NAME, nameChaos);

            this.deviceType = settings.getString(PrefUtils.DEVICE_TYPE, null);
        }
        EventBus.getDefault().register(this);
        /*
         * 及时关闭http连接
         */
        System.setProperty("http.keepAlive", "false");
    }

    public void onEvent(MessageEvent event) {
        switch (event.type) {
            case MessageEvent.Event_OnlineState:
                online = event.params.getBoolean("online");
                lastOnlineTime = event.params.getLong("lastOnlineTime");
                break;
            default:
                break;
        }
    }

    private String deviceBinder;
    private String binderName;
    private String binderHash;
    /**标志位，设备是否绑定正确的负责人*/
    private boolean deviceReported = false;
    private boolean deviceAuthorized = false;

    public String getDeviceBinder() {
        return deviceBinder;
    }


    public void setDeviceBinder(String deviceBinder) {
        this.deviceBinder = deviceBinder;
        PrefUtils.putAdministrator(deviceBinder);
    }

    public String getBinderName() {
        return binderName;
    }

    public void setBinderName(String binderName) {
        this.binderName = binderName;
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PrefUtils.BINDER_NAME, binderName == null ? null
                : AESUtil.encrypt(PrefUtils.BINDER_NAME, binderName));
        editor.commit();
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PrefUtils.DEVICE_TYPE, deviceType == null ? null
                : deviceType);
        editor.commit();
    }

    public String getDeviceType() {
        if (deviceType==null) return  "UnKnown";
        return deviceType;
    }

    /**
     * 绑定设备责任人/验证当前责任人是否正确
     */
    public void reportDevice(final String email, final String password, final Response.Listener<Void> listener, final Response.ErrorListener errorListener) {
        final String ip = AddressManager.getAddrWebservice();
        JsonObjectRequest requestAppleyDeviceToken=new JsonObjectRequest(Request.Method.POST, "https://" + ip + "/api/v1/oauth/token?" +
                "grant_type=client_credentials&client_id=2b5a38705d7b3562655925406a652e65&client_secret=234f523128212d6e70634446224c2a48",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            PrefUtils.putDeviceToken(QdParser.parseToken(response));
                        } catch (Exception e) {
                            errorListener.onErrorResponse(new ParseError());
                            return;
                        }
                        StringRequest requestApplyUserToken = new StringRequest(Request.Method.POST, "https://" + ip + "/api/v1/oauth/token",
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject resultApplyToken = new JSONObject(response);
                                            if (resultApplyToken.has("access_token")) {
                                                String token = resultApplyToken.getString("access_token");
                                                Map<String, String> map = new HashMap<>();
                                                map.put("name", PhoneInfoExtractor.getDeviceManufacturer()+"-"+PhoneInfoExtractor.getDeviceModel());
                                                JSONObject params=new JSONObject(map);
                                                JsonObjectRequest requestAddDevice = new JsonObjectRequest(Request.Method.POST,
                                                        "https://" + ip + "/api/v1/user/devices/" + PhoneInfoExtractor.getIMEI(mContext) + "?access_token=" + token,params,
                                                        new Response.Listener<JSONObject>() {
                                                            @Override
                                                            public void onResponse(JSONObject resultAddDevice) {
                                                                try {
                                                                    setDeviceReported(true);
                                                                    setDeviceBinder(email);
                                                                    setBinderName(resultAddDevice.getString("owner_name"));
                                                                    if (listener!=null)
                                                                        listener.onResponse(null);
                                                                } catch (JSONException e) {
                                                                    if (errorListener!=null)
                                                                        errorListener.onErrorResponse(new ParseError());
                                                                }
                                                            }
                                                        }, new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                        if (error.networkResponse!=null && error.networkResponse.statusCode==403) {
                                                            if (errorListener!=null)
                                                                errorListener.onErrorResponse(new LicenceError());
                                                        }
                                                        else if (error.networkResponse!=null && error.networkResponse.statusCode==422) {
                                                            setDeviceReported(true);
                                                            if (listener!=null)
                                                                listener.onResponse(null);
                                                        }
                                                        else if (errorListener!=null)
                                                            errorListener.onErrorResponse(error);
                                                    }
                                                }){
                                                    @Override
                                                    public Map getHeaders() {
                                                        HashMap headers = new HashMap();
                                                        headers.put("Accept", "application/json");
                                                        headers.put("Content-Type", "application/json; charset=UTF-8");
                                                        return headers;
                                                    }
                                                } ;
                                                EmmClientApplication.mVolleyQueue.add(requestAddDevice);
                                            }
                                        } catch (JSONException e) {
                                            errorListener.onErrorResponse(new ParseError());
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (errorListener!=null)
                                    errorListener.onErrorResponse(error);
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("username", email);
                                params.put("password", password);
                                params.put("grant_type", "password");
                                params.put("client_id", "302a7d556175264c7e5b326827497349");
                                params.put("client_secret", "4770414c283a20347c7b553650425773");
                                return params;
                            }
                        };
                        EmmClientApplication.mVolleyQueue.add(requestApplyUserToken);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener!=null)
                    errorListener.onErrorResponse(error);
            }
        });
        requestAppleyDeviceToken.setRetryPolicy(new DefaultRetryPolicy(5000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        EmmClientApplication.mVolleyQueue.add(requestAppleyDeviceToken);
    }

    public boolean isDeviceReported() {
        return this.deviceReported;
    }

    public void setDeviceReported(boolean deviceReported) {
        this.deviceReported = deviceReported;

        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PrefUtils.DEVICE_REPORTED, deviceReported);
        editor.commit();
    }

    public boolean isDeviceAuthorized() {
        return deviceAuthorized;
    }

    private void setDeviceAuthorized(boolean deviceAuthorized) {
        this.deviceAuthorized = deviceAuthorized;
    }

    public void clearActivateInfo() {
        // 清除责任人数据
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(PrefUtils.DEVICE_BINDER);
        editor.remove(PrefUtils.DEVICE_REPORTED);
        editor.remove(PrefUtils.BINDER_NAME);
        editor.commit();

        this.setDeviceBinder(null);
        this.setDeviceAuthorized(false);
        this.setDeviceReported(false);
        this.setBinderName(null);
    }

    @Override
    public void finalize() {
        EventBus.getDefault().unregister(this);
        try {
            super.finalize();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
