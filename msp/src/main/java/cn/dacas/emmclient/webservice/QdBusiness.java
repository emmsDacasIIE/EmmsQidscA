package cn.dacas.emmclient.webservice;

import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.model.DeviceModel;
import cn.dacas.emmclient.model.TokenModel;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.webservice.qdvolley.DeviceforbiddenError;
import de.greenrobot.event.EventBus;

/**
 * Created by lenovo on 2016-1-30.
 */
public class QdBusiness {

    public static void login(final String username,final String password,final Response.Listener<DeviceModel> listener, final Response.ErrorListener errorListener) {
        //向Oauth服务器索取userToken
        QdWebService.fetchUserToken(username, password, new Response.Listener<TokenModel>() {
            @Override
            public void onResponse(TokenModel response) {
                //保存UserToken
                PrefUtils.putUserToken(response);
                //用该Token登陆，如果发现Token过期，将自动触发updateToken
                QdWebService.login(new Response.Listener<DeviceModel>() {
                    @Override
                    public void onResponse(DeviceModel response) {
                        if (!response.isStatus()) {
                             if  (errorListener!=null )
                                errorListener.onErrorResponse(new DeviceforbiddenError());
                            return;
                        }
                        EmmClientApplication.mActivateDevice.setDeviceBinder(response.getOwner_account());
                        EmmClientApplication.mActivateDevice.setBinderName(response.getOwner_name());
                        EmmClientApplication.mActivateDevice.setDeviceType(response.getType());
                        EmmClientApplication.mActivateDevice.setDeviceReported(true);
                        EmmClientApplication.mCheckAccount.setCurrentAccount(username);
                        EmmClientApplication.mCheckAccount.setCurrentName(username);
                        EmmClientApplication.mCheckAccount.setCurrentPassword(password);
                        EmmClientApplication.mDatabaseEngine.setWordsPassword(username, password, username);
                        Bundle p1=new Bundle();
                        p1.putString("owner", EmmClientApplication.mActivateDevice.getDeviceBinder());
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_StartTransaction, p1));
                        Bundle p2=new Bundle();
                        p2.putString("email", username);
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_StartForwarding, p2));
                        if (listener!=null)
                            listener.onResponse(response);
                    }
                }, errorListener);
            }
        }, errorListener);
    }
}
