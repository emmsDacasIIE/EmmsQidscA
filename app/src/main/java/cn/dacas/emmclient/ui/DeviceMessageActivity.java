package cn.dacas.emmclient.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.main.CheckAccount;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.mdm.MsgListFragment;
import cn.dacas.emmclient.util.PrefUtils;

public class DeviceMessageActivity extends QdscFormalActivity {
	private EmmClientApplication app;
	private MsgListFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_message);
		
		app = (EmmClientApplication)this.getApplicationContext();

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragment = new MsgListFragment();
		fragmentTransaction.add(R.id.fragment_container, fragment);
		fragmentTransaction.commit();
		SharedPreferences unreadMsgCount = getApplicationContext().getSharedPreferences(PrefUtils.MSG_COUNT, 0);
		SharedPreferences.Editor editor = unreadMsgCount.edit();
		editor.putInt("unread_count", 0);
		editor.commit();
	}
	
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		Intent intent = this.getIntent();

		if (intent != null && intent.getBooleanExtra("FromMsg", false)) {
			CheckAccount checkAccount = app.getCheckAccount();
			
			if(checkAccount != null && checkAccount.getCurrentAccount() == null){
//				new LogInDlg(this).showLogInDlg();
			}
		}
	}
}
