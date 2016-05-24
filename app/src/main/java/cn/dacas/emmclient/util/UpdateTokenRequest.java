package cn.dacas.emmclient.util;

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

import cn.dacas.emmclient.main.EmmClientApplication;

/**
 * Created by lenovo on 2015-11-10.
 */
public class UpdateTokenRequest {

    public interface TokenType {
        int USER = 1;
        int DEVICE =2;
    }

    public static String BuildUrl(String apiUrl,int type)
    {
        String baseUrl="https://"+NetworkDef.getAddrWebservice()+"/api/v1";
        String token=null;
        if (type== UpdateTokenRequest.TokenType.USER)
            token=EmmClientApplication.mCheckAccount.getAccessToken();
        else if (type== UpdateTokenRequest.TokenType.DEVICE)
            token=EmmClientApplication.mActivateDevice.getAccessToken();
        if (token==null) return baseUrl+apiUrl;
        Pattern p = Pattern.compile("\\?\\S*=");
        Matcher m = p.matcher(apiUrl);
        if (m.find()) {
            return baseUrl+apiUrl+"&access_token="+token;
        }
        else
            return baseUrl+apiUrl+"?access_token="+token;
    }

    public static void update(final int mType, final Response.Listener<String> listener, final Response.ErrorListener errorListener)
    {
        String rToken=null;
        if (mType==TokenType.USER) rToken= EmmClientApplication.mCheckAccount.getRefreshToken();
        else if (mType== TokenType.DEVICE) rToken=EmmClientApplication.mActivateDevice.getRefreshToken();
        String refreshUrl=null;
        if (mType== TokenType.DEVICE && rToken==null)
            refreshUrl= "https://" + NetworkDef.getAddrWebservice() + "/api/v1/oauth/token?" +
                    "grant_type=client_credentials&client_id=2b5a38705d7b3562655925406a652e65&client_secret=234f523128212d6e70634446224c2a48";
        else {
//            refreshUrl="https://" + NetworkDef.getAvailableWebServiceIp() + "/api/v1/oauth/token?"+
//                    "grant_type=refresh_token&refresh_token="+rToken;
            if (EmmClientApplication.mCheckAccount.getCurrentName()==null && errorListener!=null) {
                errorListener.onErrorResponse(new VolleyError());
                return;
            }
            refreshUrl = "https://" + NetworkDef.getAddrWebservice() + "/api/v1/oauth/token?"+
                    "grant_type=password&client_id=302a7d556175264c7e5b326827497349&client_secret=4770414c283a20347c7b553650425773&username="+
                    EmmClientApplication.mCheckAccount.getCurrentName()+"&password="+EmmClientApplication.mCheckAccount.getCurrentPassword();
        }
        JsonObjectRequest updateRequest=new JsonObjectRequest(Request.Method.POST, refreshUrl, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String tokenNew = response.getString("access_token");
                    String rTokenNew = response.has("refresh_token") ? response.getString("refresh_token") : null;
                    if (mType == TokenType.USER) {
                        EmmClientApplication.mCheckAccount.setAccesstoken(tokenNew);
                        EmmClientApplication.mCheckAccount.setRefreshToken(rTokenNew);
                    } else if (mType == TokenType.DEVICE) {
                        EmmClientApplication.mActivateDevice.setAccesstoken(tokenNew);
                        EmmClientApplication.mActivateDevice.setRefreshToken(rTokenNew);
                    }
                    listener.onResponse(tokenNew);
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (errorListener != null)
                        errorListener.onErrorResponse(new ParseError());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener!=null)
                    errorListener.onErrorResponse(error);
            }
        });
        updateRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        EmmClientApplication.mVolleyQueue.add(updateRequest);
    }
}
