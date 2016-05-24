package cn.dacas.emmclient.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.main.EmmClientApplication;

public class ExitFromSettings extends Activity {
	private TextView aboutInfoText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exit_dialog_from_settings);
		aboutInfoText = (TextView) this.findViewById(R.id.about_info);
		aboutInfoText.setText("设备责任人："
				+ EmmClientApplication.mActivateDevice.getBinderName() + "\n当前登录用户："
				+ EmmClientApplication.mCheckAccount.getCurrentName());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}

	public void exitbutton1(View v) {
		this.finish();
	}

	public void exitbutton0(View v) {
		((EmmClientApplication) this.getApplicationContext()).getCheckAccount()
				.clearCurrentAccount();
		Intent intent = new Intent(this, UserLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}
}
