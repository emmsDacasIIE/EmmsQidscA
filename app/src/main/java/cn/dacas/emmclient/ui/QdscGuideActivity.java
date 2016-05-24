package cn.dacas.emmclient.ui;

import android.app.Activity;
import android.os.Bundle;

import cn.dacas.emmclient.main.EmmClientApplication;

public class QdscGuideActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected  void onStart() {
        super.onStart();
        EmmClientApplication.runningBackground=false;
    }
}
