package cn.dacas.emmclient.util;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.dacas.emmclient.main.EmmClientApplication;

/**
 * Created by lenovo on 2015-11-4.
 */
public class MySynRequest extends JsonObjectRequest {
    private static RequestFuture<JSONObject> future=RequestFuture.newFuture();
    private int mMethod;
    private String mUrl;
    private int mType;

    public  MySynRequest(int method,String url,int type) {
        super(method,UpdateTokenRequest.BuildUrl(url,type),future,future);
        mMethod=method;
        mUrl=url;
        mType=type;
    }

    public JSONObject excute() {
        EmmClientApplication.mVolleyQueue.add(this);
        try {

            JSONObject result = future.get(2, TimeUnit.SECONDS);
            return  result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return  null;
        }
        catch (TimeoutException e) {
            return null;
        }
        catch (ExecutionException e) {
            e.printStackTrace();
            String rToken=null;
            if (mType==1) rToken=EmmClientApplication.mCheckAccount.getRefreshToken();
            else if (mType==2) rToken=EmmClientApplication.mActivateDevice.getRefreshToken();
            String refreshUrl=null;
            if (mType==2 && rToken==null)
                refreshUrl= "https://" + NetworkDef.getAddrWebservice() + "/api/v1/oauth/token?" +
                        "grant_type=client_credentials&client_id=2b5a38705d7b3562655925406a652e65&client_secret=234f523128212d6e70634446224c2a48";
            else
                refreshUrl="https://" + NetworkDef.getAddrWebservice() + "/api/v1/oauth/token/"+
                    "?grant_type=refresh_token&refresh_token="+rToken;
            JsonObjectRequest refreshRequest=new JsonObjectRequest(Method.POST,refreshUrl,future,future);
            EmmClientApplication.mVolleyQueue.add(refreshRequest);
            try {
                JSONObject refreshResult=future.get(2, TimeUnit.SECONDS);
                String tokenNew=refreshResult.getString("access_token");
                String rTokenNew=refreshResult.has("refresh_token")?refreshResult.getString("refresh_token"):null;
                if (mType==1) {
                    EmmClientApplication.mCheckAccount.setAccesstoken(tokenNew);
                    EmmClientApplication.mCheckAccount.setRefreshToken(rTokenNew);
                }
                else if (mType==2) {
                    EmmClientApplication.mActivateDevice.setAccesstoken(tokenNew);
                    EmmClientApplication.mActivateDevice.setRefreshToken(rTokenNew);
                }
                JsonObjectRequest newRequest=new JsonObjectRequest(mMethod,UpdateTokenRequest.BuildUrl(mUrl,mType),future,future);
                EmmClientApplication.mVolleyQueue.add(newRequest);
                JSONObject result=future.get(2, TimeUnit.SECONDS);
                return result;
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return null;
            } catch (ExecutionException e1) {
                e1.printStackTrace();
                return null;
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
            catch (TimeoutException e1) {
                return null;
            }
        }
    }
}
