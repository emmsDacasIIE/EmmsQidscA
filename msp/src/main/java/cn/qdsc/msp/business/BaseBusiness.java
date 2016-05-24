package cn.qdsc.msp.business;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

/**
 * Created by lenovo on 2015/11/30.
 */
public class BaseBusiness {

    Context mContext;
    BusinessListener mBusinessListener;

//    public static RequestQueue mVolleyQueue = Volley.newRequestQueue(EmmClientApplication.getContext(),new SslHttpStack(false));

    public BaseBusiness(Context context, BusinessListener bListener) {
        mContext = context;
        mBusinessListener =bListener;

    }

    //network request error, not include json parser error
    public BusinessListener.BusinessResultCode setErrorMsgCode(VolleyError error,BusinessListener.BusinessType businessType) {

        //error prosess,from login auth error

        //token timeout when login, //go to BinderSelectorActivity
        if (error.networkResponse!=null && error.networkResponse.statusCode == 404 && businessType == BusinessListener.BusinessType.BusinessType_Login) {

            return BusinessListener.BusinessResultCode.ResultCode_Login_Goto_BinderSelectorActivity;
        }

        //loginRequest have this case,AuthFailureError error
        if (error instanceof AuthFailureError && businessType == BusinessListener.BusinessType.BusinessType_Login) {

            return BusinessListener.BusinessResultCode.ResultCode_AuthFailureError;
        }

        //VolleyError,通用错误
        if (error instanceof NoConnectionError || error instanceof NetworkError || error instanceof TimeoutError) {
            return BusinessListener.BusinessResultCode.ResultCode_ConnectError;
        }

        return BusinessListener.BusinessResultCode.ResultCode_Unknown;

    }

}
