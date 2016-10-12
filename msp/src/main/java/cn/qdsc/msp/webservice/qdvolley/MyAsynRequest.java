package cn.qdsc.msp.webservice.qdvolley;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.Map;

import cn.qdsc.msp.manager.UrlManager;
/**
 * Created by lenovo on 2015-11-2.
 */
public abstract  class MyAsynRequest<T> extends Request<T> {
    protected int mMethod;
    protected String mUrl;
    protected Map<String, String> mMap;
    protected Response.Listener<T> mListener;
    protected Response.ErrorListener mErrorListener;
    protected int mType;

    /**
     * 重新定义异步请求的构造器
     * @param method
     * @param url
     * @param type
     * @param listener
     * @param errorListener
     */
    public MyAsynRequest(int method, String url, int type, Response.Listener<T> listener, Response.ErrorListener errorListener)
    {
        super(method, UrlManager.BuildWebServiceUrl(url, type), errorListener);
        mMethod=method;
        mUrl=url;
        mType=type;
        mListener = listener;
        mErrorListener=errorListener;
        mMap = null;
    }


    public MyAsynRequest(int method, String url, int type,  Map<String, String> map, Response.Listener<T> listener, Response.ErrorListener errorListener) {

        // UpdateTokenRequest(url, type)
        super(method, UrlManager.BuildWebServiceUrl(url, type), errorListener);
        mMethod=method;
        mUrl=url;
        mType=type;
        mMap = map;
        mListener = listener;
        mErrorListener=errorListener;
    }

    //mMap是已经按照前面的方式,设置了参数的实例
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mMap;
    }



    @Override
    protected void deliverResponse(T response) {
        if (mListener!=null)
            mListener.onResponse(response);
    }

    abstract  protected void retry();

    //处理token过期的情况
    @Override
    public  void deliverError(VolleyError error) {
        if (error.networkResponse!=null && error.networkResponse.statusCode==401
                && mType!=UpdateTokenRequest.TokenType.NONE)
        {
            UpdateTokenRequest.update(mType, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    retry();
                }
            }, mErrorListener);
        }
        else if (mErrorListener!=null)
            mErrorListener.onErrorResponse(error);
    }
}
