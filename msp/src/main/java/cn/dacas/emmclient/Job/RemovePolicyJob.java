package cn.dacas.emmclient.Job;

import org.json.JSONObject;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.core.mdm.MDMService;
import cn.dacas.emmclient.core.mdm.PolicyManager;
import cn.dacas.emmclient.model.SerializableCMD;
import cn.dacas.emmclient.msgpush.MsgWorker;
import cn.dacas.emmclient.util.BroadCastDef;
import cn.dacas.emmclient.util.QDLog;

/**
 * Created by Sun RX on 2016-12-14.
 * A Job task to remove current policy and reset to the default one.
 */

public class RemovePolicyJob extends BasedMDMJobTask {
    public RemovePolicyJob(int priority, SerializableCMD cmd) {
        super(priority, "RemovePolicyJob", cmd);
    }

    public RemovePolicyJob(SerializableCMD cmd) {
        super("RemovePolicyJob", cmd);
    }

    @Override
    public void onRun() throws Throwable {
        super.onRun();
        MsgWorker msgWorker= MDMService.getMsgWorker();
        if(msgWorker==null)
            throw new Throwable("PushMsgReceiver hasn't set MsgWorker");

        if(cmd.paramMap.containsKey("identifier")) {
            EmmClientApplication.mDatabaseEngine.removePolicy(MDMService.CmdCode.OP_REMOVE_PROFILE);
            String id = cmd.paramMap.get("identifier");
            QDLog.d("POLICY", "Remove policy: " + id + ", and reset to be the Default one.");
            PolicyManager.getMPolicyManager(EmmClientApplication.getContext())
                    .resetPolicy();
            msgWorker.notifyDataChange(BroadCastDef.OP_LOG);
        }
        msgWorker.sendStatusToServer(msgWorker.getExeCmdStatus(),
                cmd.cmdUUID,null);

    }
}
