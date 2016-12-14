package cn.dacas.emmclient.Job;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.core.mdm.DeviceAdminWorker;
import cn.dacas.emmclient.core.mdm.MDMService;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.model.SerializableCMD;
import cn.dacas.emmclient.msgpush.MsgWorker;
import cn.dacas.emmclient.util.BroadCastDef;
import cn.dacas.emmclient.util.QDLog;
import de.greenrobot.event.EventBus;

import static android.R.id.message;

/**
 * Created by Administrator on 2016-12-13.
 */

public class EraseEnterpriseData extends BasedMDMJobTask {
    public EraseEnterpriseData(int priority, SerializableCMD cmd) {
        super(priority, "EraseEnterpriseData", cmd);
    }

    public EraseEnterpriseData(SerializableCMD cmd) {
        super("EraseEnterpriseData", cmd);
    }

    @Override
    public void onAdded() {
        super.onAdded();
    }

    @Override
    public void onRun() throws Throwable {
        super.onRun();
        MsgWorker msgWorker= MDMService.getMsgWorker();
        if(msgWorker==null)
            throw new Throwable("PushMsgReceiver hasn't set MsgWorker");

        EmmClientApplication.mDatabaseEngine.eraseCorp(MDMService.CmdCode.OP_ERASE_CORP);

        EmmClientApplication.mSecureContainer.deletAllFiles();
        EmmClientApplication.mDatabaseEngine.clearCorpData();

        Bundle params = new Bundle();
        params.putString("title", "擦除企业数据");
        params.putString("message", "该设备已被擦除企业数据");

        //Toast.makeText(MDMService.getInstance(),"擦除企业数据"+"\n"+"该设备已被擦除企业数据",Toast.LENGTH_LONG).show();
        EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_Show_alertDialog,params));

        msgWorker.notifyDataChange(BroadCastDef.OP_LOG);
        msgWorker.sendStatusToServer(msgWorker.getExeCmdStatus(),
                cmd.cmdUUID,null);
    }
}
