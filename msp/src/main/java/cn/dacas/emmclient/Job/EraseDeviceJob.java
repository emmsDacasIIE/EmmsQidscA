package cn.dacas.emmclient.Job;

import android.os.Bundle;

import cn.dacas.emmclient.core.mdm.DeviceAdminWorker;
import cn.dacas.emmclient.core.mdm.MDMService;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.model.SerializableCMD;
import cn.dacas.emmclient.msgpush.MsgWorker;
import de.greenrobot.event.EventBus;

/**
 * Created by Sun RX on 2016-12-14.
 */

public class EraseDeviceJob extends BasedMDMJobTask {
    public EraseDeviceJob(int priority, SerializableCMD cmd) {
        super(priority, "EraseDeviceJob", cmd);
    }

    public EraseDeviceJob( SerializableCMD cmd) {
        super("EraseDeviceJob", cmd);
    }

    @Override
    public void onRun() throws Throwable {
        super.onRun();
        MsgWorker msgWorker= MDMService.getMsgWorker();
        if(msgWorker==null)
            throw new Throwable("PushMsgReceiver hasn't set MsgWorker");

        Bundle params = new Bundle();
        params.putString("title", "恢复出厂设置");
        params.putString("message", "该设备恢复出厂设置中");

        EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_Show_alertDialog,params));
        msgWorker.sendStatusToServer(msgWorker.getExeCmdStatus(),
                cmd.cmdUUID,null);

        DeviceAdminWorker.getDeviceAdminWorker(MDMService.getInstance()).wipeData(false);
    }
}
