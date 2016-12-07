package cn.dacas.emmclient.Job;

import android.content.Context;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.core.mdm.DeviceAdminWorker;
import cn.dacas.emmclient.core.mdm.MDMService;
import cn.dacas.emmclient.model.SerializableCMD;
import cn.dacas.emmclient.msgpush.MsgWorker;
import cn.dacas.emmclient.msgpush.PushMsgReceiver;
import cn.dacas.emmclient.util.BroadCastDef;

/**
 * Created by Sun RX on 2016-12-7.
 * Lock the Device and Reset Password.
 */

public class LockAndSetPwdJob extends BasedMDMJobTask {
    public LockAndSetPwdJob(int priority, SerializableCMD cmd) {
        super(priority, "LockAndSetPwdJob", cmd);
    }

    public LockAndSetPwdJob(SerializableCMD cmd) {
        super("LockAndSetPwdJob", cmd);
    }

    @Override
    public void onAdded() {
        super.onAdded();
    }

    @Override
    public void onRun() throws Throwable {
        super.onRun();
        MsgWorker msgWorker= PushMsgReceiver.getMsgWorker();
        if(msgWorker==null)
            throw new Throwable("PushMsgReceiver hasn't set MsgWorker");
        Context context = msgWorker.getContext();

        String passwdLock = cmd.paramMap.get("passcode");
        if(passwdLock == null)
            msgWorker.sendStatusToServer("Error",
                    cmd.cmdUUID,null);
        int ret = DeviceAdminWorker.getDeviceAdminWorker(context).resetPasswd(passwdLock);
        DeviceAdminWorker.getDeviceAdminWorker(context).lockNow();

        EmmClientApplication.mDatabaseEngine.setLockScreenCode(ret, MDMService.CmdCode.OP_LOCK_KEY);
        msgWorker.notifyDataChange(BroadCastDef.OP_LOG);

        msgWorker.sendStatusToServer(msgWorker.getExeCmdStatus(),
                cmd.cmdUUID,null);
    }
}
