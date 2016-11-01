package cn.dacas.emmclient.webservice.qdvolley;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.manager.UrlManager;
import cn.dacas.emmclient.util.QDLog;


/**
 * Created by lenovo on 2015-11-6.
 */
public class MyJsonObjectRequest extends  MyAsynRequest<JSONObject> {
    private static final String TAG= "MyVolley";

    public MyJsonObjectRequest(int method, String url, int type, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener)
    {
        super(method,url,type, listener,errorListener);
    }

    public MyJsonObjectRequest(int method, String url, int type, Map<String,String> map, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener)
    {
        super(method,url,type, map, listener,errorListener);
    }


    @Override
    protected void retry() {
        //更新Token后重新请求服务
        JsonObjectRequest newRequest = new JsonObjectRequest(mMethod, UrlManager.BuildWebServiceUrl(mUrl, mType), mListener, mErrorListener);
        EmmClientApplication.mVolleyQueue.add(newRequest);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            QDLog.i(TAG, "Response========" + jsonString);
            QDLog.println(TAG, jsonString);
            return Response.success(new JSONObject(jsonString),HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}
