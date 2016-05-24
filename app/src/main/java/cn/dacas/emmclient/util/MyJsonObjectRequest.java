package cn.dacas.emmclient.util;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import cn.dacas.emmclient.main.EmmClientApplication;

import static cn.dacas.emmclient.util.UpdateTokenRequest.BuildUrl;

/**
 * Created by lenovo on 2015-11-6.
 */
public class MyJsonObjectRequest extends  MyAsynRequest<JSONObject> {

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
        JsonObjectRequest newRequest = new JsonObjectRequest(mMethod, BuildUrl(mUrl, mType), mListener, mErrorListener);
        EmmClientApplication.mVolleyQueue.add(newRequest);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}
