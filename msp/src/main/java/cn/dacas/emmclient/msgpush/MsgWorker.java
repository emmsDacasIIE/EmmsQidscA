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
import com.birbit.android.jobqueue.JobManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.dacas.emmclient.Job.BasedMDMJobTask;
import cn.dacas.emmclient.Job.EraseDeviceJob;
import cn.dacas.emmclient.Job.EraseEnterpriseData;
import cn.dacas.emmclient.Job.InstallPolicyJob;
import cn.dacas.emmclient.Job.LockAndSetPwdJob;
import cn.dacas.emmclient.Job.LockDeviceJob;
import cn.dacas.emmclient.Job.RemovePolicyJob;
import cn.dacas.emmclient.Job.UploadDeviceInformationJob;
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
public class MsgWorker {
    private Handler hdler = null;
    private McmController mcmController;
    private JobManager mJobManager;
    private volatile boolean working = false;

    /**
     * The constructor of MsgWorker,
     * in which, a method "registerBroadcastReceiver" is called
     * to register a broadcastReceiver with filter "GET_MESSAGE".
     */
    public MsgWorker(Handler handler, McmController mcmController) {
        setHandler(handler);
        this.mcmController = mcmController;
        registerBroadcastReceiver();
        mJobManager = EmmClientApplication.getInstance().getJobManager();
    }

    public void setHandler(Handler handler) {
        this.hdler = handler;
    }

    public Handler getHandler() {
        return this.hdler;
    }

    public void dealMessage(CommandModel commandModel) throws JSONException {
        int reqCode =  commandModel.getCmdCode();
        boolean nowSendStatus = true;

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
                    DeviceAdminWorker
                            .getDeviceAdminWorker(EmmClientApplication.getContext()).setMute(true);
                } else {
                    DeviceAdminWorker
                            .getDeviceAdminWorker(EmmClientApplication.getContext()).setMute(false);
                }
                EmmClientApplication.mDatabaseEngine.setMute(MDMService.CmdCode.OP_SET_MUTE);
                notifyDataChange(BroadCastDef.OP_LOG);
                break;
            case MDMService.CmdCode.OP_LOCK_KEY:
                mJobManager.addJobInBackground(new LockAndSetPwdJob(commandModel.getSerializableCMD()));
                break;
            case MDMService.CmdCode.OP_LOCK:
                mJobManager.addJobInBackground(new LockDeviceJob(commandModel.getSerializableCMD()));
                break;
            case MDMService.CmdCode.OP_FACTORY:
                mJobManager.addJobInBackground(new EraseDeviceJob(commandModel.getSerializableCMD()));
                break;
            case MDMService.CmdCode.OP_ERASE_CORP:
                mJobManager.addJobInBackground(new EraseEnterpriseData(commandModel.getSerializableCMD()));
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
                mJobManager.addJobInBackground(new UploadDeviceInformationJob(commandModel.getSerializableCMD()));
                break;
            case MDMService.CmdCode.OP_POLICY1:
            case MDMService.CmdCode.OP_INSTALL_POLICY2:
                mJobManager.addJobInBackground(new InstallPolicyJob(commandModel.getSerializableCMD()));
                break;
            case MDMService.CmdCode.OP_REMOVE_PROFILE:
                mJobManager.addJobInBackground(new RemovePolicyJob(commandModel.getSerializableCMD()));
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
        EmmClientApplication.getContext()
                .registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    public void getMessages() {
        QDLog.i(MDMService.TAG,"========getMessages function==========");
        final int maxId= PrefUtils.getMsgMaxId();
        mcmController.FetchMessageList(maxId);
        EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_MsgCount_Change));
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
        EmmClientApplication.getContext().sendBroadcast(intent);
    }

    public void onState(boolean state) {
        setWorking(state);
        Bundle params = new Bundle();
        params.putBoolean("online", state);
        QDLog.writeMsgPushLog("state=" + (state?"online":"offline"));
        params.putLong("lastOnlineTime", System.currentTimeMillis());
        EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_OnlineState, params));
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
                        if(response.length()!=0) {
                            commandModel = new CommandModel(response);
                            dealMessage(commandModel);
                        }
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
                    QDLog.e(MDMService.TAG,"sendStatusError",error);
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
