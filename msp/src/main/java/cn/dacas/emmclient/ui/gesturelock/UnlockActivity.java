package cn.dacas.emmclient.ui.gesturelock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.manager.ActivityManager;

public class UnlockActivity extends Activity implements LocusPassWordView.OnCompleteListener {
	private String userAccount;
	private String userName;

	private LocusPassWordView mPwdView;
	private TextView unlockTitle;
	private TextView currentAccoutName;
    private int type;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_unlock);
		userAccount = EmmClientApplication.mCheckAccount.getCurrentAccount();
		currentAccoutName = (TextView) findViewById(R.id.current_account);
		userName =EmmClientApplication.mCheckAccount.getCurrentName();
		currentAccoutName.setText("当前用户：" +userName );
		
		unlockTitle = (TextView) findViewById(R.id.draw_pwd_title);
		mPwdView = (LocusPassWordView) this.findViewById(R.id.mPassWordView);
		mPwdView.setOnCompleteListener(this);

		userAccount = EmmClientApplication.mCheckAccount.getCurrentAccount();
		//0：解锁 1：设置
		type=getIntent().getIntExtra("type",0);
		if (type==0) {
			unlockTitle.setText("请解锁");
		}
		else if (type==1) {
			unlockTitle.setText("请输入原来的手势密码");
			unlockTitle.setTextColor(getResources().getColor(R.color.white));
		}
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
	protected  void onStart() {
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

		if (password.equals(EmmClientApplication.mDatabaseEngine.getPatternPassword(userAccount))) {
            if (type==1) {
                Intent intent = new Intent(UnlockActivity.this, GestureLockActivity.class);
                startActivity(intent);
            }
			if(ActivityManager.isLocking)
				ActivityManager.isLocking = false;
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
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && type==0) {
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
