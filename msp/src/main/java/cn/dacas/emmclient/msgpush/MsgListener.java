package cn.dacas.emmclient.msgpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import cn.qdsc.msp.controller.McmController;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.core.mdm.DeviceAdminWorker;
import cn.qdsc.msp.core.mdm.MDMService;
import cn.qdsc.msp.core.mdm.PolicyManager;
import cn.qdsc.msp.event.MessageEvent;
import cn.qdsc.msp.manager.AddressManager;
import cn.qdsc.msp.manager.UrlManager;
import cn.qdsc.msp.model.CommandModel;
import cn.qdsc.msp.util.BroadCastDef;
import cn.qdsc.msp.util.GlobalConsts;
import cn.qdsc.msp.util.PrefUtils;
import cn.qdsc.msp.util.QDLog;
import cn.qdsc.msp.webservice.qdvolley.MyJsonObjectRequest;
import cn.qdsc.msp.webservice.qdvolley.UpdateTokenRequest;
import de.greenrobot.event.EventBus;

/** Worker to deal with Messages.
 * Created by Sun RX on 2016-10-13.
 */
public class MsgListener {
    private Handler hdler = null;
    private Context ctxt = null;
    private McmController mcmController;
    private boolean working = false;

    /**
     * The constructor of MsgListener,
     * in which, a method "registerBroadcastReceiver" is called
     * to register a broadcastReceiver with filter "GET_MESSAGE".
     */
    public MsgListener(Context context, Handler handler, McmController mcmController) {
        setContext(context);
        setHandler(handler);
        this.mcmController = mcmController;
        registerBroadcastReceiver();
    }

    public void setHandler(Handler handler) {
        this.hdler = handler;
    }

    public Handler getHandler() {
        return this.hdler;
    }

    public void setContext(Context context) {
        this.ctxt = context;
    }

    public void onMessage(byte[] byteArr) {
        String msg = new String(byteArr, 0, byteArr.length,
                Charset.forName("utf-8"));
        Log.v("MsgPush", msg);
        QDLog.writeMsgPushLog("receive msg=" + msg);

        try {
            dealMessage(msg);
        } catch (JSONException e) {
            if (hdler != null) {
                // 来自服务器的消息格式错误
                Message message = Message.obtain();
                message.arg1 = MDMService.CommdCode.ERROR_FORMAT;
                hdler.sendMessage(message);
            }
            e.printStackTrace();
        }
    }

    public void dealMessage(String message) throws JSONException {
        JSONTokener jsonParser = new JSONTokener(message);
        JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
        String code = jsonObject.getString("command");

        int reqCode = Integer.parseInt(code);
        int ret;

        switch (reqCode) {
            case MDMService.CommdCode.NEW_COMMANDS:
                getCommands();
                break;
            case MDMService.CommdCode.OP_PUSH_MSG:
                getMessages();
                break;
            case MDMService.CommdCode.OP_SET_MUTE:
                String stateMute = jsonObject.getString("state");
                if (stateMute.equals("true")) {
                    DeviceAdminWorker.getDeviceAdminWorker(ctxt).setMute(true);
                } else {
                    DeviceAdminWorker.getDeviceAdminWorker(ctxt).setMute(false);
                }
                EmmClientApplication.mDatabaseEngine.setMute(MDMService.CommdCode.OP_SET_MUTE);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CommdCode.OP_LOCK_KEY:
                String passwdLock = jsonObject.getString("passwd");
                ret = DeviceAdminWorker.getDeviceAdminWorker(ctxt).resetPasswd(passwdLock);
                DeviceAdminWorker.getDeviceAdminWorker(ctxt).lockNow();
                EmmClientApplication.mDatabaseEngine.setLockScreenCode(ret, MDMService.CommdCode.OP_LOCK_KEY);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CommdCode.OP_LOCK:
                DeviceAdminWorker.getDeviceAdminWorker(ctxt).lockNow();
                break;
            case MDMService.CommdCode.OP_FACTORY:
                if (hdler != null) {
                    String option = jsonObject.getString("option");

                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CommdCode.FACTORY;
                    handlerMsg.arg2 = option.equals("enforce") ?  MDMService.CommdCode.ENFORCE :  MDMService.CommdCode.WARN;
                    hdler.sendMessage(handlerMsg);
                    EmmClientApplication.mDatabaseEngine.reFactory(MDMService.CommdCode.OP_FACTORY);

                }
//                    notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CommdCode.OP_ERASE_CORP:
                if (hdler != null) {
                    String option = jsonObject.getString("option");

                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CommdCode.ERASE_CORP;
                    handlerMsg.arg2 = option.equals("enforce") ?  MDMService.CommdCode.ENFORCE :  MDMService.CommdCode.WARN;
                    hdler.sendMessage(handlerMsg);
                    EmmClientApplication.mDatabaseEngine.eraseCorp(MDMService.CommdCode.OP_ERASE_CORP);
                }
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case  MDMService.CommdCode.OP_ERASE_ALL:
                if (hdler != null) {
                    String option = jsonObject.getString("option");

                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 =  MDMService.CommdCode.ERASE_ALL;
                    handlerMsg.arg2 = option.equals("enforce") ?  MDMService.CommdCode.ENFORCE : MDMService.CommdCode. WARN;
                    hdler.sendMessage(handlerMsg);
                    EmmClientApplication.mDatabaseEngine.eraseDeviceAllData( MDMService.CommdCode.OP_ERASE_ALL);
                }
//                    notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case  MDMService.CommdCode.OP_REFRESH:
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 =  MDMService.CommdCode.REFRESH_ALL;
                    hdler.sendMessage(handlerMsg);
                }
                EmmClientApplication.mDatabaseEngine.refreshDevice( MDMService.CommdCode.OP_REFRESH);
//                    notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case  MDMService.CommdCode.OP_POLICY1:
            case  MDMService.CommdCode.OP_POLICY2:
                Log.d("POLICY", "Receive new policy notification");
                EmmClientApplication.mDatabaseEngine.pushPolicy( MDMService.CommdCode.OP_POLICY1);

                PolicyManager.getMPolicyManager(ctxt).updatePolicy();
                notifyDataChange(BroadCastDef.OP_LOG);
                break;

            case  MDMService.CommdCode.AUTH_STATE_CHANGE:
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 =  MDMService.CommdCode.AUTH_STATE_CHANGE;
                    hdler.sendMessage(handlerMsg);
                }
                EmmClientApplication.mDatabaseEngine.changeDeviceState( MDMService.CommdCode.AUTH_STATE_CHANGE);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case  MDMService.CommdCode.DELETE_DEVICE:
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 =  MDMService.CommdCode.DELETE_DEVICE;
                    hdler.sendMessage(handlerMsg);
                }
                EmmClientApplication.mDatabaseEngine.deleteDeviceFromService( MDMService.CommdCode.DELETE_DEVICE);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case  MDMService.CommdCode.OP_CHANGE_DEVICE_TYPE:
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 =  MDMService.CommdCode.DEVICE_TYPE;
                    hdler.sendMessage(handlerMsg);
                }
            default:
                break;
        }
    }

    public void dealMessage(CommandModel commandModel) throws JSONException {
        int reqCode =  commandModel.getCmdCode();
        JSONObject jsonObject = new JSONObject();

        int ret;

        switch (reqCode) {
            case MDMService.CommdCode.NEW_COMMANDS:
                getCommands();
                break;
            case MDMService.CommdCode.OP_PUSH_MSG:
                getMessages();
                break;
            case MDMService.CommdCode.OP_SET_MUTE:
                String stateMute = jsonObject.getString("state");
                if (stateMute.equals("true")) {
                    DeviceAdminWorker.getDeviceAdminWorker(ctxt).setMute(true);
                } else {
                    DeviceAdminWorker.getDeviceAdminWorker(ctxt).setMute(false);
                }
                EmmClientApplication.mDatabaseEngine.setMute(MDMService.CommdCode.OP_SET_MUTE);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CommdCode.OP_LOCK_KEY:
                String passwdLock = commandModel.getCommandMap().get("passcode");
                ret = DeviceAdminWorker.getDeviceAdminWorker(ctxt).resetPasswd(passwdLock);
                DeviceAdminWorker.getDeviceAdminWorker(ctxt).lockNow();
                EmmClientApplication.mDatabaseEngine.setLockScreenCode(ret, MDMService.CommdCode.OP_LOCK_KEY);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CommdCode.OP_LOCK:
                DeviceAdminWorker.getDeviceAdminWorker(ctxt).lockNow();
                break;
            case MDMService.CommdCode.OP_FACTORY:
                if (hdler != null) {
                    String option = jsonObject.getString("option");

                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CommdCode.FACTORY;
                    handlerMsg.arg2 = option.equals("enforce") ? MDMService.CommdCode.ENFORCE : MDMService.CommdCode.WARN;
                    hdler.sendMessage(handlerMsg);
                    EmmClientApplication.mDatabaseEngine.reFactory(MDMService.CommdCode.OP_FACTORY);

                }
//                    notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CommdCode.OP_ERASE_CORP:
                if (hdler != null) {
                    String option = jsonObject.getString("option");

                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CommdCode.ERASE_CORP;
                    handlerMsg.arg2 = option.equals("enforce") ? MDMService.CommdCode.ENFORCE : MDMService.CommdCode.WARN;
                    hdler.sendMessage(handlerMsg);
                    EmmClientApplication.mDatabaseEngine.eraseCorp(MDMService.CommdCode.OP_ERASE_CORP);
                }
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CommdCode.OP_ERASE_ALL:
                if (hdler != null) {
                    String option = jsonObject.getString("option");

                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CommdCode.ERASE_ALL;
                    handlerMsg.arg2 = option.equals("enforce") ? MDMService.CommdCode.ENFORCE : MDMService.CommdCode.WARN;
                    hdler.sendMessage(handlerMsg);
                    EmmClientApplication.mDatabaseEngine.eraseDeviceAllData(MDMService.CommdCode.OP_ERASE_ALL);
                }
//                    notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CommdCode.OP_REFRESH:
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CommdCode.REFRESH_ALL;
                    hdler.sendMessage(handlerMsg);
                }
                EmmClientApplication.mDatabaseEngine.refreshDevice(MDMService.CommdCode.OP_REFRESH);
                break;
            case MDMService.CommdCode.OP_POLICY1:
            case MDMService.CommdCode.OP_POLICY2:
                Log.d("POLICY", "Receive new policy notification");
                EmmClientApplication.mDatabaseEngine.pushPolicy(MDMService.CommdCode.OP_POLICY1);

                PolicyManager.getMPolicyManager(ctxt).updatePolicy();
                notifyDataChange(BroadCastDef.OP_LOG);
                break;

            case MDMService.CommdCode.AUTH_STATE_CHANGE:
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CommdCode.AUTH_STATE_CHANGE;
                    hdler.sendMessage(handlerMsg);
                }
                EmmClientApplication.mDatabaseEngine.changeDeviceState(MDMService.CommdCode.AUTH_STATE_CHANGE);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CommdCode.DELETE_DEVICE:
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CommdCode.DELETE_DEVICE;
                    hdler.sendMessage(handlerMsg);
                }
                EmmClientApplication.mDatabaseEngine.deleteDeviceFromService(MDMService.CommdCode.DELETE_DEVICE);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CommdCode.OP_CHANGE_DEVICE_TYPE:
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CommdCode.DEVICE_TYPE;
                    hdler.sendMessage(handlerMsg);
                }
            default:
                break;
        }
        sendStatusToServer("Acknowledged",commandModel.getCommandUUID());

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GlobalConsts.GET_MESSAGE)) {
                QDLog.i(MDMService.TAG,"get_message==========");
                getMessages();
            }
        }
    };

    public void registerBroadcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(GlobalConsts.GET_MESSAGE);
        // 注册广播
        ctxt.registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    public void getMessages() {
        QDLog.i(MDMService.TAG,"========getMessages function==========");
        final int maxId= PrefUtils.getMsgMaxId();
        mcmController.FetchMessageList(maxId);
        PrefUtils.addSecurityRecord("加密一条消息");

    }

    private void getCommands() {
        QDLog.i(MDMService.TAG,"========getCommands function==========");
        mcmController.FetchMessageCommand();

    }

    public void notifyDataChange(String action) {
        QDLog.i(MDMService.TAG,"========notifyDataChange action==========" + action);
        Intent intent = new Intent();
        intent.setAction(action);
        ctxt.sendBroadcast(intent);
    }

    public void onState(boolean state) {
        setWorking(state);
        Bundle params = new Bundle();
        params.putBoolean("online", state);
        QDLog.writeMsgPushLog("state=" + (state?"online":"offline"));
        params.putLong("lastOnlineTime", System.currentTimeMillis());
        EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_OnlineState, params));
    }




    //send handler msg
    public void sendMsg(String msg) {
        Handler handler = getHandler();
        QDLog.i(MDMService.TAG,"sendMsg =========="+msg);
        if (handler != null) {
            QDLog.i(MDMService.TAG,"sendMsg ======111===="+msg);
            Bundle bundle = new Bundle();
            bundle.putCharSequence("msg", msg);
            Message handlerMsg = Message.obtain();
            handlerMsg.arg1 = MDMService.CommdCode.PUSH_MSG;
            handlerMsg.setData(bundle);
            handler.sendMessageAtFrontOfQueue(handlerMsg);
        }
    }

    public boolean isWorking(){
        return working;
    }

    public void setWorking(boolean flag) {
        this.working = flag;
    }

    public String getExeCmdStatus() {
        return "Idle";
    }
    public void sendHandlerCommend(int actionType){
        if (this.getHandler() != null) {
            // 来自服务器的消息格式错误
            Message message = Message.obtain();
            message.arg1 = actionType;
            this.getHandler().sendMessage(message);
        }
    }

    /**
     * send Cmd Status to Server with http post method,
     * where there are params:
     * 1.status // Acknowledged, Error, CommandFormatError, Idle ,NotNow
     * 2.CommandUUID
     * if status = Acknowledged, it means CommandUUID cmd has been executed;
     * if status = Idle, it means the Device is ready to execute new CMD;
     * if status = NotNow
     */
    public void sendStatusToServer(final String status, final String cmdUUID) {
        Response.Listener<JSONObject> rspListener = null;
        Response.ErrorListener errorListener = null;
        //1. sent the result of cmd (cmdUUID != ""), and then not deal with any response;
        //2. sent the status "idle" to Server to get cmd (cmdUUID == "");
        if(cmdUUID.equals("")) {
            rspListener = new Response.Listener<JSONObject>() {
                CommandModel commandModel;
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        commandModel = new CommandModel(response);
                        dealMessage(commandModel);
                    } catch (Exception e) {
                        if(commandModel!=null)
                            sendStatusToServer("Error",commandModel.getCommandUUID());
                        e.printStackTrace();
                    }
                }
            };
            errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //sendHandlerCommend(MDMService.CommdCode.ERROR_MSG_SERVER);
                }
            };
        }

        MyJsonObjectRequest cmdRequest = new MyJsonObjectRequest(
                Request.Method.PUT,
                UrlManager.cmdServerPath + "/" + EmmClientApplication.imei,
                UpdateTokenRequest.TokenType.DEVICE,
                rspListener
                , errorListener
        ){
            @Override
            public byte[] getBody() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("status",status);
                    jsonObject.put("command_uuid",cmdUUID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonObject.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                header.put("Content-Type", "application/json; charset=UTF-8");
                return header;
            }
        };
        EmmClientApplication.mVolleyQueue.add(cmdRequest);
    }
}
