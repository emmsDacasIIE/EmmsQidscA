package cn.dacas.emmclient.gesturelock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.db.EmmClientDb;
import cn.dacas.emmclient.gesturelock.LocusPassWordView.OnCompleteListener;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.ui.NewMainActivity;
import cn.dacas.emmclient.ui.QdscGuideActivity;

public class GestureLockActivity extends QdscGuideActivity implements
		OnCompleteListener, OnClickListener {

	private LocusPassWordView mPwdView;
	private Context mContext;
	private TextView drawPwdTextView;
	private Button leftButton;
	private Button rightButton;

	private EmmClientDb db;
	private String firstPwd = "";
	private int setPwdPage = 1;

	private int setPwdCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draw_pwd);
		mContext = getApplicationContext();
		db = EmmClientApplication.mDb;
		drawPwdTextView = (TextView) findViewById(R.id.draw_pwd_title);
		mPwdView = (LocusPassWordView) this.findViewById(R.id.mPassWordView);
		mPwdView.setOnCompleteListener(this);

		leftButton = (Button) findViewById(R.id.button_left);
		leftButton.setOnClickListener(this);
		rightButton = (Button) findViewById(R.id.button_right);
		rightButton.setOnClickListener(this);
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
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
	public void onComplete(String mPassword) {
		// TODO Auto-generated method stub
		setPwdCount++;
		if (setPwdCount == 1) {
			leftButton.setVisibility(View.VISIBLE);
            rightButton.setVisibility(View.VISIBLE);
			leftButton.setText(R.string.retry);
			rightButton.setText(R.string.go_on);
			firstPwd = mPassword;
		} else if (setPwdCount == 2) {
			if (!mPassword.equals(firstPwd)) {
				drawPwdTextView.setText(R.string.please_retry);
				mPwdView.error();
				mPwdView.clearPassword();
				setPwdCount--;
			} else {
				drawPwdTextView.setText(R.string.new_pattern);
				rightButton.setText(R.string.allow);
				rightButton.setVisibility(View.VISIBLE);
			}
		} else {

		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (setPwdCount == 1) {
			if (1 == setPwdPage) {
				if (id == leftButton.getId()) {
					mPwdView.clearPassword(0);
					firstPwd = "";
					setPwdCount--;
					leftButton.setVisibility(View.INVISIBLE);
				} else if (id == rightButton.getId()) {
					mPwdView.clearPassword(0);
					drawPwdTextView.setText(R.string.please_set_pattern_2nd);
					setPwdPage++;
                    leftButton.setVisibility(View.INVISIBLE);
				}
			} else if (2 == setPwdPage) {
			}
		} else if (setPwdCount == 2) {
			 if (id == rightButton.getId()) {
				db.setPatternPassword(EmmClientApplication.mCheckAccount.getCurrentAccount(), firstPwd);
				Toast.makeText(mContext, mContext.getString(R.string.pwd_setted), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, NewMainActivity.class);
                startActivity(intent);
                finish();;
			}
		}

	}

}
