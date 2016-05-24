package cn.qdsc.msp.ui.activity.loginbind;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.RelativeLayout;

import com.umeng.analytics.MobclickAgent;

import cn.qdsc.msp.R;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.util.BitMapUtil;
import cn.qdsc.msp.util.QDLog;
import cn.qdsc.msp.core.mdm.DeviceAdminWorker;


public class AppStartActivity extends Activity {
	private static final String TAG = "AppStartActivity";
	private static final int ACTIVATION_REQUEST = 1;
	private boolean isDeviceReported;
	RelativeLayout mRelativeLayout;
	Drawable mBackgroundDrawable = null;

	private Handler mLoadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			QDLog.i(TAG, "Load finished...........");
			if (msg.what == 2 ) {
				if (mBackgroundDrawable != null) {
					mRelativeLayout.setBackground(mBackgroundDrawable);
					mBackgroundDrawable = null;
				}
//				mLoadHandler.sendEmptyMessage(1);
			}else {

			}
			activateDeviceAdmin();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activate_device);
		mRelativeLayout = (RelativeLayout)findViewById(R.id.start_activity_layout);
		loadBackgroundThread();
		QDLog.i(TAG,"AppStartActivity onCreate===================");
	}

	public void onStart() {
		super.onStart();
		QDLog.i(TAG, "AppStartActivity onStart===================");
//		activateDeviceAdmin();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		QDLog.i(TAG, "AppStartActivity onResume===================");
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		QDLog.i(TAG, "AppStartActivity onPause===================");
	}

	private void activateDeviceAdmin() {
		QDLog.i(TAG, "activateDeviceAdmin is call from appStartActivity===================");
		ComponentName mDeviceAdminSample = new ComponentName(
				this.getApplicationContext(), DeviceAdminWorker.DeviceAdminSampleReceiver.class);
		DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		if (!mDPM.isAdminActive(mDeviceAdminSample)) {
			Intent intent = new Intent(
					DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
					mDeviceAdminSample);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					this.getString(R.string.add_admin_extra_app_text));
			startActivityForResult(intent, ACTIVATION_REQUEST);
		} else {
			startOtherActivity();
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVATION_REQUEST && resultCode == RESULT_OK) {
			startOtherActivity();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void startOtherActivity() {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				if (EmmClientApplication.mActivateDevice.isDeviceReported()) {
					Intent intent = new Intent(AppStartActivity.this, UserLoginActivity.class);
					startActivity(intent);
				}
				else {
					Intent intent = new Intent(AppStartActivity.this,
							PrivacyStatementActivity.class);
					startActivity(intent);
				}
				AppStartActivity.this.finish();
			}
		}, 10);
	}


	//加载背景图片
	private void loadBackgroundThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				mBackgroundDrawable = BitMapUtil.getDrawableByZoom(AppStartActivity.this, R.mipmap.csr_qd);
//				mRelativeLayout.setBackground(mBackgroundDrawable);
				Message msg = new Message();
				msg.what = 2;
				mLoadHandler.sendMessage(msg);

			}
		}).start();

	}
}
