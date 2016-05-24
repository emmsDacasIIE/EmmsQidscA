package cn.qdsc.msp.core.mdm;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import cn.qdsc.msp.core.mdm.MDMService;
import cn.qdsc.msp.event.MessageEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by lenovo on 2015-11-21.
 */
public class QdscReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("QdscReceiver", "Receive intent action=" + intent.getAction());
        Intent intentMDM = new Intent(context, MDMService.class);
        context.startService(intentMDM);
        if (intent.getAction().equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
            Bundle bundle=new Bundle();
            bundle.putInt("State", intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1));
            EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_Bluetooth_State_Changed,bundle));
        }
        else if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            EventBus.getDefault().post(new MessageEvent(MessageEvent.Event_Network_State_Changed));
        }
    }
}
