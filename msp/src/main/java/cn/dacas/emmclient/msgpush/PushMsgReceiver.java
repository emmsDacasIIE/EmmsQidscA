package cn.dacas.emmclient.msgpush;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONObject;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.pushmessagesdk.BaseMessageReceiver;
import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.mdm.MDMService;
import cn.dacas.emmclient.ui.activity.mainframe.MdmMsgListActivity;
import cn.dacas.emmclient.util.PhoneInfoExtractor;

/**
 * Created by Sun RX on 2016-10-13.
 * Based on BaseMessageReceiver
 */
public class PushMsgReceiver extends BaseMessageReceiver{
    static private MsgWorker sMsgWorker;
    static public void setMsgWorker(MsgWorker listener){
        if(listener!=null)
            sMsgWorker = listener;
            sMsgWorker.setWorking(true);
    }
    static public MsgWorker getMsgWorker(){
        return sMsgWorker;
    }

    @Override
    protected void onError(Context context, String msg) {
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
        sMsgWorker.sendHandlerCommend(MDMService.CmdCode.ERROR_MSG_SERVER);
    }

    @Override
    protected void onMsgArrived(Context context, String msg) {
        if(sMsgWorker !=null) {
            try {
                String jo2 = new JSONObject(msg).getString("content");
                JSONObject contJsn = new JSONObject(jo2);
                if(contJsn.has("message")){
                    String cmd = contJsn.getString("message");
                    switch (cmd){
                        case "DeviceUpdated":
                            sMsgWorker.sendHandlerCommend(MDMService.CmdCode.DEVICE_INFO_CHANGE);
                            break;
                        default:
                            break;
                    }
                }else if(contJsn.has("mdm")) {
                    String uuid = contJsn.getString("mdm");
                    if (!uuid.equals(PhoneInfoExtractor.getIMEI(context)))
                        return;
                    sMsgWorker.sendStatusToServer(sMsgWorker.getExeCmdStatus(), "", null);
                }
            } catch (Exception e) {
                sMsgWorker.sendHandlerCommend(MDMService.CmdCode.ERROR_FORMAT);
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onConnectionError(Context context, String msg) {
        if(sMsgWorker !=null)
            sMsgWorker.onState(false);
    }

    @Override
    protected void onNotificationArrived(Context context, String msg) {
        if(!EmmClientApplication.mDeviceModel.isStatus())
            return;
        super.onNotificationArrived(context,msg);
        if(sMsgWorker !=null)
            sMsgWorker.getMessages();
    }

    @Override
    public Class getNotificationToActivity() {
        return MdmMsgListActivity.class;
    }

    @Override
    public int getIcon() {
        return R.mipmap.emm_red_28_logo;
    }

    @Override
    protected void onConnectionOk(Context context, String msg) {
        if(sMsgWorker !=null) {
            sMsgWorker.onState(true);
            sMsgWorker.sendStatusToServer(sMsgWorker.getExeCmdStatus(),"",null);
        }
    }
}
