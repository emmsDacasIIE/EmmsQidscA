package cn.dacas.emmclient.msgpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import cn.dacas.emmclient.controller.McmController;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.core.mdm.DeviceAdminWorker;
import cn.dacas.emmclient.core.mdm.MDMService;
import cn.dacas.emmclient.core.mdm.PolicyManager;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.manager.UrlManager;
import cn.dacas.emmclient.model.CommandModel;
import cn.dacas.emmclient.util.BroadCastDef;
import cn.dacas.emmclient.util.GlobalConsts;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.QDLog;
import cn.dacas.emmclient.webservice.qdvolley.MyJsonObjectRequest;
import cn.dacas.emmclient.webservice.qdvolley.UpdateTokenRequest;
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

    public void dealMessage(CommandModel commandModel) throws JSONException {
        int reqCode =  commandModel.getCmdCode();
        boolean nowSendStatus = true;

        int ret;

        switch (reqCode) {
            case MDMService.CmdCode.NEW_COMMANDS:
                getCommands();
                break;
            case MDMService.CmdCode.OP_PUSH_MSG:
                getMessages();
                break;
            case MDMService.CmdCode.OP_SET_MUTE:
                String stateMute = commandModel.getCommandMap().get("state");
                if (stateMute.equals("true")) {
                    DeviceAdminWorker.getDeviceAdminWorker(ctxt).setMute(true);
                } else {
                    DeviceAdminWorker.getDeviceAdminWorker(ctxt).setMute(false);
                }
                EmmClientApplication.mDatabaseEngine.setMute(MDMService.CmdCode.OP_SET_MUTE);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CmdCode.OP_LOCK_KEY:
                String passwdLock = commandModel.getCommandMap().get("passcode");
                ret = DeviceAdminWorker.getDeviceAdminWorker(ctxt).resetPasswd(passwdLock);
                DeviceAdminWorker.getDeviceAdminWorker(ctxt).lockNow();
                EmmClientApplication.mDatabaseEngine.setLockScreenCode(ret, MDMService.CmdCode.OP_LOCK_KEY);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CmdCode.OP_LOCK:
                DeviceAdminWorker.getDeviceAdminWorker(ctxt).lockNow();
                break;
            case MDMService.CmdCode.OP_FACTORY:
                if (hdler != null) {
                    String option = commandModel.getCommandMap().get("option");
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CmdCode.FACTORY;
                    if(option == null)
                        handlerMsg.arg2 = MDMService.CmdCode.WARN;
                    else
                        handlerMsg.arg2 = option.equals("enforce") ?
                                MDMService.CmdCode.ENFORCE : MDMService.CmdCode.WARN;
                    hdler.sendMessage(handlerMsg);
                    EmmClientApplication.mDatabaseEngine.reFactory(MDMService.CmdCode.OP_FACTORY);

                }
//                    notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CmdCode.OP_ERASE_CORP:
                if (hdler != null) {
                    String option = commandModel.getCommandMap().get("option");

                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CmdCode.ERASE_CORP;
                    if(option == null)
                        handlerMsg.arg2 = MDMService.CmdCode.WARN;
                    else
                        handlerMsg.arg2 = option.equals("enforce") ?
                                MDMService.CmdCode.ENFORCE : MDMService.CmdCode.WARN;
                    hdler.sendMessage(handlerMsg);
                    EmmClientApplication.mDatabaseEngine.eraseCorp(MDMService.CmdCode.OP_ERASE_CORP);
                }
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CmdCode.OP_ERASE_ALL:
                if (hdler != null) {
                    String option = commandModel.getCommandMap().get("option");

                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CmdCode.ERASE_ALL;
                    if(option == null)
                        handlerMsg.arg2 = MDMService.CmdCode.WARN;
                    else
                        handlerMsg.arg2 = option.equals("enforce") ?
                                        MDMService.CmdCode.ENFORCE : MDMService.CmdCode.WARN;
                    hdler.sendMessage(handlerMsg);
                    EmmClientApplication.mDatabaseEngine.eraseDeviceAllData(MDMService.CmdCode.OP_ERASE_ALL);
                }
//                    notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CmdCode.OP_REFRESH:
                nowSendStatus = false;
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CmdCode.REFRESH_ALL;
                    handlerMsg.obj = commandModel.getCommandUUID();
                    hdler.sendMessage(handlerMsg);
                }
                EmmClientApplication.mDatabaseEngine.refreshDevice(MDMService.CmdCode.OP_REFRESH);
                break;
            case MDMService.CmdCode.OP_POLICY1:
            case MDMService.CmdCode.OP_INSTALL_POLICY2:
                QDLog.d("POLICY", "Receive new policy notification");
                EmmClientApplication.mDatabaseEngine.pushPolicy(MDMService.CmdCode.OP_POLICY1);
                String payload = commandModel.getCommandMap().get("payload");
                JSONObject policyJson = new JSONObject(payload);
                PolicyManager.getMPolicyManager(ctxt).updatePolicy(policyJson);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CmdCode.OP_REMOVE_PROFILE:
                EmmClientApplication.mDatabaseEngine.removePolicy(MDMService.CmdCode.OP_REMOVE_PROFILE);
                String id = commandModel.getCommandMap().get("identifier");
                QDLog.d("POLICY", "Remove policy: "+ id +", and reset to be the Default one.");
                PolicyManager.getMPolicyManager(ctxt).resetPolicy();
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CmdCode.AUTH_STATE_CHANGE:
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CmdCode.AUTH_STATE_CHANGE;
                    hdler.sendMessage(handlerMsg);
                }
                EmmClientApplication.mDatabaseEngine.changeDeviceState(MDMService.CmdCode.AUTH_STATE_CHANGE);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CmdCode.DELETE_DEVICE:
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CmdCode.DELETE_DEVICE;
                    hdler.sendMessage(handlerMsg);
                }
                EmmClientApplication.mDatabaseEngine.deleteDeviceFromService(MDMService.CmdCode.DELETE_DEVICE);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CmdCode.OP_CHANGE_DEVICE_TYPE:
                if (hdler != null) {
                    Message handlerMsg = Message.obtain();
                    handlerMsg.arg1 = MDMService.CmdCode.DEVICE_INFO_CHANGE;
                    hdler.sendMessage(handlerMsg);
                }
            default:
                break;
        }
        if(nowSendStatus)
            sendStatusToServer("Acknowledged",commandModel.getCommandUUID(),null);

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
    /*public void sendMsg(String msg) {
        Handler handler = getHandler();
        QDLog.i(MDMService.TAG,"sendMsg =========="+msg);
        if (handler != null) {
            QDLog.i(MDMService.TAG,"sendMsg ======111===="+msg);
            Bundle bundle = new Bundle();
            bundle.putCharSequence("msg", msg);
            Message handlerMsg = Message.obtain();
            handlerMsg.arg1 = MDMService.CmdCode.PUSH_MSG;
            handlerMsg.setData(bundle);
            handler.sendMessageAtFrontOfQueue(handlerMsg);
        }
    }*/

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
    public void sendStatusToServer(final String status, final String cmdUUID, final Map<String,JSONObject> params) {
        QDLog.d(CommandModel.TAG,status+":"+cmdUUID);
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
                        QDLog.d(CommandModel.TAG,response.toString());
                        commandModel = new CommandModel(response);
                        dealMessage(commandModel);
                    } catch (Exception e) {
                        if(commandModel!=null)
                            sendStatusToServer("Error",commandModel.getCommandUUID(),null);
                        e.printStackTrace();
                    }
                }
            };
            errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //sendHandlerCommend(MDMService.CmdCode.ERROR_MSG_SERVER);
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
                    if(params!=null && params.size()>0){
                        for (String key:params.keySet()) {
                            jsonObject.put(key,params.get(key));
                        }
                    }
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
