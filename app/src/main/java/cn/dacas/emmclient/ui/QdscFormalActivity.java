package cn.dacas.emmclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.dacas.emmclient.gesturelock.UnlockActivity;
import cn.dacas.emmclient.main.CheckAccount;
import cn.dacas.emmclient.main.EmmClientApplication;

public class QdscFormalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected  void onStart() {
        super.onStart();
        if (EmmClientApplication.runningBackground && EmmClientApplication.intervals>=EmmClientApplication.LockSecs) {
            CheckAccount ac=EmmClientApplication.mCheckAccount;
            if (ac.getCurrentAccount()!=null &&  EmmClientApplication.mDb.getPatternPassword((ac.getCurrentAccount()))!=null &&
                    !EmmClientApplication.mActivateDevice.getDeviceType().equals("COPE-PUBLIC")) {
                Intent intent = new Intent(EmmClientApplication.getContext(), UnlockActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                EmmClientApplication.getContext().startActivity(intent);
                EmmClientApplication.intervals=0;
            }
        }
        EmmClientApplication.runningBackground=false;
    }
}
