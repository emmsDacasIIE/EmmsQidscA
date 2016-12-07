package cn.dacas.emmclient.Job;

import android.content.Context;

import cn.dacas.emmclient.core.mdm.DeviceAdminWorker;
import cn.dacas.emmclient.model.SerializableCMD;
import cn.dacas.emmclient.msgpush.MsgWorker;
import cn.dacas.emmclient.msgpush.PushMsgReceiver;

/**
 * Created by Sun RX on 2016-12-7.
 * A Job to Lock the Device
 * And send the result to the Server;
 */

public class LockDeviceJob extends BasedMDMJobTask {
    public LockDeviceJob(int priority, SerializableCMD cmd) {
        super(priority, "LockDeviceJob", cmd);
    }

    public LockDeviceJob(SerializableCMD cmd) {
        super("LockDeviceJob",cmd);
    }

    @Override
    public void onAdded() {
        super.onAdded();
    }

    @Override
    public void onRun() throws Throwable {
        super.onRun();
        MsgWorker msgWorker=PushMsgReceiver.getMsgWorker();
        if(msgWorker==null)
            throw new Throwable("PushMsgReceiver hasn't set MsgWorker");
        Context context = msgWorker.getContext();
        DeviceAdminWorker.getDeviceAdminWorker(context).lockNow();
        msgWorker.sendStatusToServer(msgWorker.getExeCmdStatus(),
                cmd.cmdUUID,null);
    }
}
