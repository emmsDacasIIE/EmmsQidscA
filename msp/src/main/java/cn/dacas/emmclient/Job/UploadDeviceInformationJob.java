package cn.dacas.emmclient.Job;

import org.json.JSONObject;

import java.util.HashMap;

import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.core.mdm.MDMService;
import cn.dacas.emmclient.model.SerializableCMD;
import cn.dacas.emmclient.msgpush.MsgWorker;
import cn.dacas.emmclient.util.PrefUtils;

import static cn.dacas.emmclient.core.mdm.MDMService.uploadPrivacySetting;

/**
 * Created by Sun RX on 2016-12-13.
 * A Job to upload the device information to the Server
 * And send the result to the Server;
 */

public class UploadDeviceInformationJob extends BasedMDMJobTask {
    public UploadDeviceInformationJob(int priority, SerializableCMD cmd) {
        super(priority, "UploadDeviceInformationJob", cmd);
    }

    public UploadDeviceInformationJob(SerializableCMD cmd) {
        super("UploadDeviceInformationJob", cmd);
    }

    @Override
    public void onAdded() {
        super.onAdded();
    }

    @Override
    public void onRun() throws Throwable {
        super.onRun();

        String uuid = cmd.cmdUUID;
        MsgWorker msgWorker = MDMService.getMsgWorker();
        if(msgWorker==null)
            throw new Throwable("PushMsgReceiver hasn't set MsgWorker");

        HashMap<String, JSONObject> map = new HashMap<>();
        map.put("query_responses", MDMService.getInstance().getDeviceInfoDetail());
        msgWorker.sendStatusToServer("Acknowledged", uuid, map);
        uploadPrivacySetting(MDMService.getInstance(), PrefUtils.getNetPrivacy());
        EmmClientApplication.mDatabaseEngine.refreshDevice(MDMService.CmdCode.OP_REFRESH);
    }
}
