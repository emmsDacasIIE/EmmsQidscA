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
 * Created by Sun RX on 2016-12-13.
 * A Job to install the new policy on the device
 * And send the result to the Server;
 */

public class InstallPolicyJob extends BasedMDMJobTask {
    public InstallPolicyJob(int priority, SerializableCMD cmd) {
        super(priority, "InstallPolicyJob", cmd);
    }

    public InstallPolicyJob(SerializableCMD cmd) {
        super("InstallPolicyJob", cmd);
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

        QDLog.d("POLICY", "Receive new policy notification");
        EmmClientApplication.mDatabaseEngine.pushPolicy(MDMService.CmdCode.OP_POLICY1);
        if(cmd.paramMap.containsKey("payload")) {
            String payload = cmd.paramMap.get("payload");
            JSONObject policyJson = new JSONObject(payload);
            PolicyManager.getMPolicyManager(EmmClientApplication.getContext())
                    .updatePolicy(policyJson);
            msgWorker.notifyDataChange(BroadCastDef.OP_LOG);
        }
        msgWorker.sendStatusToServer(msgWorker.getExeCmdStatus(),
                cmd.cmdUUID,null);
    }
}
