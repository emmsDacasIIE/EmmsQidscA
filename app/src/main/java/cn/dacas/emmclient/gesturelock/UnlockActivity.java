package cn.dacas.emmclient.gesturelock;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.gesturelock.LocusPassWordView.OnCompleteListener;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.ui.QdscGuideActivity;
import cn.dacas.emmclient.worker.DeviceAdminWorker;

public class UnlockActivity extends QdscGuideActivity implements OnCompleteListener {
	private String userAccount;
	private String userName;

	private LocusPassWordView mPwdView;
	private TextView unlockTitle;
	private TextView currentAccoutName;
    private String key;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unlock);
		userAccount = ((EmmClientApplication) this.getApplicationContext())
				.getCheckAccount().getCurrentAccount();
		currentAccoutName = (TextView) findViewById(R.id.current_account);
		userName = ((EmmClientApplication) this.getApplicationContext())
				.getCheckAccount().getCurrentName();
		currentAccoutName.setText("当前用户：" +userName );
		
		unlockTitle = (TextView) findViewById(R.id.draw_pwd_title);
		mPwdView = (LocusPassWordView) this.findViewById(R.id.mPassWordView);
		mPwdView.setOnCompleteListener(this);

		userAccount = ((EmmClientApplication) this.getApplicationContext())
				.getCheckAccount().getCurrentAccount();
        key=getIntent().getStringExtra("key");
	}

	
	public void onResume() {
		super.onResume();
        EmmClientApplication.runningBackground=false;
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}


	/*
	 * (non-Javadoc) 手势输入完成时
	 * 
	 * @see cn.dacas.emmclient.gesturelock.LocusPassWordView.OnCompleteListener#
	 * onComplete(java.lang.String)
	 */
	@Override
	public void onComplete(String password) {
		// TODO Auto-generated method stub

		if (password.equals(EmmClientApplication.mDb.getPatternPassword(userAccount))) {
            DeviceAdminWorker.getDeviceAdminWorker(EmmClientApplication.getContext()).resetVerifyTime(key);
			finish();
		} else {
			mPwdView.error();
			mPwdView.clearPassword();
			unlockTitle.setText(R.string.wrong_pwd_retry);
		}
	}
	


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
