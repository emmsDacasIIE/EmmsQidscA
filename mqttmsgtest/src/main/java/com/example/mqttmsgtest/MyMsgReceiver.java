package com.example.mqttmsgtest;

import android.content.Context;
import android.os.Message;
import android.widget.Toast;

import cn.dacas.pushmessagesdk.BaseMessageReceiver;

/**
 * Created by Administrator on 2016-9-8.
 */
public class MyMsgReceiver extends BaseMessageReceiver {
    @Override
    protected void onError(Context context, String msg) {
        //Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
        Message message = new Message();
        message.obj = msg;
        message.what = 0;
        MainActivity.showHandler.sendMessage(message);
    }

    @Override
    protected void onMsgArrived(Context context, String msg) {
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
        Message message = new Message();
        message.obj = msg;
        message.what = 0;
        MainActivity.showHandler.sendMessage(message);
    }

    @Override
    public Class getNotificationToActivity() {
        return MainActivity.class;
    }

    @Override
    public int getIcon() {
        return R.drawable.push_notification;
    }
}
