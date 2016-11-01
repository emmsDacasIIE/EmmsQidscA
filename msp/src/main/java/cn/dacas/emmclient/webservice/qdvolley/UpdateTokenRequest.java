package cn.dacas.emmclient.webservice.qdvolley;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.manager.UrlManager;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.QDLog;

/**
 * Created by lenovo on 2015-11-10.
 */
public class UpdateTokenRequest {
    private static final String TAG= "MyVolley";

    public interface TokenType {
        int USER = 1;
        int DEVICE =2;
        int NONE=3;
    }

    public static String BuildUrl(String apiUrl,int type)
    {
        String baseUrl= UrlManager.getTokenServiceUrl();
        //"https://"+ AddressManager.getAddrWebservice()+"/api/v1";
        String token=null;
        if (type== UpdateTokenRequest.TokenType.USER)
            token = PrefUtils.getUserToken().getAccessToken();
        else if (type== UpdateTokenRequest.TokenType.DEVICE)
            token = PrefUtils.getDeviceToken().getAccessToken();
        else token=null;
        if (token==null) return baseUrl+apiUrl;

        Pattern p = Pattern.compile("\\?\\S*=");
        Matcher m = p.matcher(apiUrl);
        if (m.find()) {
            QDLog.i(TAG,"Request========" + baseUrl+apiUrl+"&access_token="+token);
            return baseUrl+apiUrl+"&access_token="+token;
        }
        else {
            QDLog.i(TAG,"Request========" + baseUrl+apiUrl+"&access_token="+token);
            return baseUrl + apiUrl + "?access_token=" + token;
        }
    }

    /**
     * 当token过期时，触发更新token
     * @param mType token类型 user or device
     * @param listener
     * @param errorListener
     */
    public static void update(final int mType, final Response.Listener<String> listener, final Response.ErrorListener errorListener)
    {
        //依据不同的类型获取更新token
        String rToken=null;
        if (mType== TokenType.USER)
            rToken= PrefUtils.getUserToken().getRefreshToken();
        else if (mType== TokenType.DEVICE)
            rToken= PrefUtils.getDeviceToken().getRefreshToken();

        //获得更新token要访问的Url
        String refreshUrl=null;
        if (mType== TokenType.DEVICE && rToken==null)
            //其实是重新申请DeviceToken 设备认证没有rToken
            refreshUrl= UrlManager.getTokenServiceUrl() +
                    "?grant_type=client_credentials&client_id=2b5a38705d7b3562655925406a652e65&client_secret=234f523128212d6e70634446224c2a48";
        else {
            //如果当前没有人登陆，则报错
            if (EmmClientApplication.mCheckAccount.getCurrentName()==null && errorListener!=null) {
                errorListener.onErrorResponse(new VolleyError());
                return;
            }
            //其实也是重新申请的Token,没有用到rToken
            refreshUrl = UrlManager.getTokenServiceUrl() +
                    "?grant_type=password&client_id=302a7d556175264c7e5b326827497349&client_secret=4770414c283a20347c7b553650425773&username="+
                    EmmClientApplication.mCheckAccount.getCurrentName()+"&password="+EmmClientApplication.mCheckAccount.getCurrentPassword();
        }
        JsonObjectRequest updateRequest=new JsonObjectRequest(
                Request.Method.POST,
                refreshUrl,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String tokenNew = response.getString("access_token");
                            String rTokenNew = response.has("refresh_token") ? response.getString("refresh_token") : null;
                            QDLog.i(TAG, "update onResponse========response=============" + response);
                            QDLog.i(TAG,"update onResponse========tokenNew=============" + tokenNew);
                            QDLog.i(TAG,"update onResponse========rTokenNew=============" + rTokenNew);
                            if (mType == TokenType.USER) {
                                PrefUtils.putUserToken(tokenNew, rTokenNew);
                            } else if (mType == TokenType.DEVICE) {
                                PrefUtils.putDeviceToken(tokenNew, rTokenNew);
                            }
                            listener.onResponse(tokenNew);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (errorListener != null)
                                errorListener.onErrorResponse(new ParseError());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        QDLog.i(TAG,"update onResponse========error=============" + error.toString());
                        if (errorListener!=null)
                            errorListener.onErrorResponse(error);
                    }
                }
        );
        updateRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        EmmClientApplication.mVolleyQueue.add(updateRequest);
    }
}
