package cn.dacas.emmclient.util;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import cn.dacas.emmclient.main.EmmClientApplication;

import static cn.dacas.emmclient.util.UpdateTokenRequest.BuildUrl;

/**
 * Created by lenovo on 2015-11-6.
 */
public class MyJsonArrayRequest extends  MyAsynRequest<JSONArray> {

    public MyJsonArrayRequest(int method, String url, int type, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener)
    {
        super(method,url,type, listener,errorListener);
    }

    public MyJsonArrayRequest(int method, String url, int type, Map<String,String> map, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener)
    {
        super(method,url,type, map, listener,errorListener);
    }

    @Override
    protected void retry() {
        JsonArrayRequest newRequest = new JsonArrayRequest(mMethod, BuildUrl(mUrl, mType), mListener, mErrorListener);
        EmmClientApplication.mVolleyQueue.add(newRequest);
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONArray(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}
