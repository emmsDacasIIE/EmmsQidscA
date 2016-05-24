package cn.dacas.emmclient.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.dacas.emmclient.mdm.MDMService;

/**
 * Created by lenovo on 2015-11-21.
 */
public class QdscReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("QdscReceiver","Receive intent action="+intent.getAction());
        Intent intentMDM = new Intent(context, MDMService.class);
        context.startService(intentMDM);
    }
}
