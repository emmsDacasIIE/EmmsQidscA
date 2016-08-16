package cn.qdsc.msp.business;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.qdsc.msp.webservice.qdvolley.MyJsonArrayRequest;
import cn.qdsc.msp.webservice.qdvolley.MyJsonObjectRequest;
import cn.qdsc.msp.webservice.qdvolley.UpdateTokenRequest;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.util.PhoneInfoExtractor;
import cn.qdsc.msp.util.QDLog;

/**
 * Created by lizhongyi on 2015/12/22.
 */
public class PushMsgBusiness extends BaseBusiness {
    private final String TAG = "PushMsgBusiness";

    public PushMsgBusiness(Context context, BusinessListener bListener) {
        super(context, bListener);
    }

    /**
     *
     * from MDMService,获取消息列表
     */


    public void getMessageFromServer(int maxId) {
        final BusinessListener.BusinessType businessType = BusinessListener.BusinessType.BusinessType_MessageList;
        //String url = "/user/apps?platforms=ANDROID";
        String url = "/client/devices/" + PhoneInfoExtractor.getIMEI(mContext) +"/messages?message_id=" + maxId;
        QDLog.i(TAG,"getMessageFromServer========url=====" + url);
        MyJsonArrayRequest jsonArrayRequest = new MyJsonArrayRequest(Request.Method.GET, url, UpdateTokenRequest.TokenType.DEVICE,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        QDLog.i(TAG, "getMessageFromServer: " + response.toString());
                        mBusinessListener.onBusinessResultJsonArray(BusinessListener.BusinessResultCode.ResultCode_Sucess, businessType, response, null);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                QDLog.i(TAG,"getMessageFromServer========VolleyError=====" + error);
                BusinessListener.BusinessResultCode errorCode = setErrorMsgCode(error, businessType);
                mBusinessListener.onBusinessResultJsonArray(errorCode, businessType, null, null);
            }
        });
        EmmClientApplication.mVolleyQueue.add(jsonArrayRequest);
    }

    // from MDMService
    public void getCommands() {
        final BusinessListener.BusinessType businessType = BusinessListener.BusinessType.BusinessType_getCommands;
        String url = "/client/devices/" + PhoneInfoExtractor.getIMEI(mContext) +
                "/commands";
        MyJsonArrayRequest request = new MyJsonArrayRequest(Request.Method.GET, url, UpdateTokenRequest.TokenType.DEVICE, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                QDLog.i(TAG, "getCommands: " + response.toString());
                mBusinessListener.onBusinessResultJsonArray(BusinessListener.BusinessResultCode.ResultCode_Sucess, businessType, response, null);
            }
        }, null);
        EmmClientApplication.mVolleyQueue.add(request);
    }

    /**
     * from MDMService, 暂时没用到，而是用的MDMService的downloadDeviceInfo
     */
    //
    public void downloadDeviceInfo() {
        final BusinessListener.BusinessType businessType = BusinessListener.BusinessType.BusinessType_getCommands;
        String url = "/client/devices/" + PhoneInfoExtractor.getIMEI(mContext);
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET, url, UpdateTokenRequest.TokenType.DEVICE, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                QDLog.i(TAG, "downloadDeviceInfo: " + response);
                try {
                    EmmClientApplication.mActivateDevice.setDeviceType(response.getString("type"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, null);
        EmmClientApplication.mVolleyQueue.add(request);
    }

    /**
     * 开始转发
     * @param email
     */
    public void startForwarding(final String email) {
        final BusinessListener.BusinessType businessType = BusinessListener.BusinessType.BusinessType_startForwarding;
        String url = "/user/apps?platforms=ANDROID";
        MyJsonArrayRequest request = new MyJsonArrayRequest(Request.Method.GET, url, UpdateTokenRequest.TokenType.USER, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                QDLog.i(TAG, "startForwarding: " + response.toString());
                mBusinessListener.onBusinessResultJsonArray(BusinessListener.BusinessResultCode.ResultCode_Sucess, businessType, response, null);
            }
        }, null);
        EmmClientApplication.mVolleyQueue.add(request);

    }



}
