package cn.qdsc.msp.business;

import android.content.Context;

import com.android.volley.NetworkError;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;

import cn.qdsc.msp.webservice.qdvolley.MyJsonArrayRequest;
import cn.qdsc.msp.webservice.qdvolley.UpdateTokenRequest;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.util.QDLog;

/**
 * Created by lenovo on 2015/11/30.
 */
public class McmBusiness extends BaseBusiness {
    private final String TAG = "McmBusiness";

    public McmBusiness(Context context, BusinessListener bListener) {
        super(context, bListener);
    }

    /**
     * 获取文档列表
     */

    public void getDocListFromServer() {
        final BusinessListener.BusinessType businessType = BusinessListener.BusinessType.BusinessType_DocList;
        String url = "/user/docs";
        MyJsonArrayRequest jsonArrayRequest = new MyJsonArrayRequest(com.android.volley.Request.Method.GET, url, UpdateTokenRequest.TokenType.USER,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        QDLog.i(TAG, "getDocListFromServer: " + response.toString());
                        mBusinessListener.onBusinessResultJsonArray(BusinessListener.BusinessResultCode.ResultCode_Sucess, businessType, response, null);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof NetworkError) {
//                    isNetworkOK = false;
                }
                BusinessListener.BusinessResultCode errorCode = setErrorMsgCode(error, businessType);
                mBusinessListener.onBusinessResultJsonArray(errorCode, businessType, null, null);
            }
        });
        EmmClientApplication.mVolleyQueue.add(jsonArrayRequest);
    }

    /**
     * get contacts list
     */


    public void getContactListFromServer() {
        final BusinessListener.BusinessType businessType = BusinessListener.BusinessType.BusinessType_ContactsList;
        String url = "/user/dirs";
        MyJsonArrayRequest jsonArrayRequest = new MyJsonArrayRequest(com.android.volley.Request.Method.GET, url, UpdateTokenRequest.TokenType.USER,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        QDLog.i(TAG, "getContactListFromServer: " + response.toString());
                        mBusinessListener.onBusinessResultJsonArray(BusinessListener.BusinessResultCode.ResultCode_Sucess, businessType, response, null);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof NetworkError) {
//                    isNetworkOK = false;
                }
                BusinessListener.BusinessResultCode errorCode = setErrorMsgCode(error, businessType);
                mBusinessListener.onBusinessResultJsonArray(errorCode, businessType, null, null);
            }
        });
        EmmClientApplication.mVolleyQueue.add(jsonArrayRequest);
    }





}
