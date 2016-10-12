package com.example.mqttmsgtest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;

import cn.dacas.pushmessagesdk.PushMsgManager;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MQTT";
    private Button pubTopic, pubTopic2,getJson, deleteAlias;

    static private TextView textView;
    final String serverUri = "tcp://192.168.151.175:1883";//iot.eclipse.org 192.168.151.175

    PushMsgManager pushMsgManager;

    static Handler showHandler = new Handler(){
        public void handleMessage(Message msg){
            textView.setText(textView.getText()+"//n"+msg.obj.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pubTopic = (Button) findViewById(R.id.publishToTopic);
        pubTopic2 = (Button) findViewById(R.id.publishToTopic2);
        getJson = (Button) findViewById(R.id.getJson);
        textView = (TextView) findViewById(R.id.textView);
        deleteAlias = (Button) findViewById(R.id.deleteAliases);

        pubTopic.setOnClickListener(this);
        pubTopic2.setOnClickListener(this);
        getJson.setOnClickListener(this);
        deleteAlias.setOnClickListener(this);

        PushMsgManager.refleshMsgNotification(this,getIntent());

        pushMsgManager = new PushMsgManager(this,serverUri);
        try {
            //1. 注册APP，如果发现已经有reg_id则直接请求要关注的主题
            pushMsgManager.registerPush(
                    "http://192.168.151.137:8000/client/devices",// Web adder
                    "046e2930-7cc2-4398-9b1c-65852317de29",// client_id
                    "6668b6a3-8486-4165-a418-374194ad47d3");// client_secret
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.publishToTopic:
                String topic = pushMsgManager.getNotifyTopicLists().get(0);
                pushMsgManager.publishToTopic(topic,"{\"title\":\"New Message\",\"content\":\"This is a test message!\",\"status\":\"DEV\"}");
                break;
            case R.id.publishToTopic2:
                String tpc = pushMsgManager.getMsgTopicLists().get(0);
                pushMsgManager.publishToTopic(tpc, "{\"title\":\"test123\",\"content\":\"test123\",\"status\":\"DEV\"}");
                break;
            case R.id.getJson:
                //pushMsgManager.getJsonArrayFormServer(PushMsgManager.CommCodeType.NET_GetAliase);
                try {
                    JSONArray jsonArray = new JSONArray("[\"AD\",\"srx\"]");
                    //pushMsgManager.deleteHttpRequest(PushMsgManager.CommCodeType.NET_GetAliase,jsonArray);
                    pushMsgManager.sendJsonArrayToServer(Request.Method.POST,PushMsgManager.CommCodeType.NET_GetAliase,jsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.deleteAliases:
                try {
                    pushMsgManager.deleteHttpRequest(PushMsgManager.CommCodeType.NET_GetAliase,new JSONArray("[\"AD\"]"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
