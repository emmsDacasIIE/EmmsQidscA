package cn.dacas.emmclient.msgpush;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cn.dacas.pushmessagesdk.BaseMessageReceiver;
import cn.qdsc.msp.R;
import cn.qdsc.msp.core.mdm.MDMService;
import cn.qdsc.msp.ui.activity.mainframe.MdmMsgListActivity;
import cn.qdsc.msp.ui.activity.mainframe.MsgDetailActivity;
import cn.qdsc.msp.util.PhoneInfoExtractor;

/**
 * Created by Sun RX on 2016-10-13.
 */
public class PushMsgReceiver extends BaseMessageReceiver{
    static MsgListener msgListener;
    static public void setMsgListener(MsgListener listener){
        if(listener!=null)
            msgListener = listener;
            msgListener.setWorking(true);
    }

    @Override
    protected void onError(Context context, String msg) {
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
        msgListener.sendHandlerCommend(MDMService.CommdCode.ERROR_MSG_SERVER);
    }

    @Override
    protected void onMsgArrived(Context context, String msg) {
        if(msgListener!=null) {
            try {
                JSONObject jo1 = new JSONObject(msg);
                String jo2 = jo1.getString("content");
                String uuid = new JSONObject(jo2).getString("mdm");
                if(!uuid.equals(PhoneInfoExtractor.getIMEI(context)))
                    return;
                msgListener.sendStatusToServer(msgListener.getExeCmdStatus(),"");
            } catch (JSONException e) {
                msgListener.sendHandlerCommend(MDMService.CommdCode.ERROR_FORMAT);
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onConnectionError(Context context, String msg) {
        if(msgListener!=null)
            msgListener.onState(false);
    }

    @Override
    protected void onNotificationArrived(Context context, String msg) {
        super.onNotificationArrived(context,msg);
        if(msgListener!=null)
            msgListener.getMessages();
    }

    @Override
    public Class getNotificationToActivity() {
        return MdmMsgListActivity.class;
    }

    @Override
    public int getIcon() {
        return R.mipmap.emmclient_small_logo;
    }

    @Override
    protected void onConnectionOk(Context context, String msg) {
        if(msgListener!=null) {
            msgListener.onState(true);
            msgListener.sendStatusToServer(msgListener.getExeCmdStatus(),"");
        }
    }
}
