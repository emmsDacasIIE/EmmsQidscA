package cn.dacas.emmclient.ui.activity.loginbind;

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

import com.umeng.analytics.MobclickAgent;//友盟 静态统计类

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.util.BitMapUtil;
import cn.dacas.emmclient.util.QDLog;
import cn.dacas.emmclient.core.mdm.DeviceAdminWorker;


public class AppStartActivity extends Activity {
	private static final String TAG = "AppStartActivity";
	private static final int ACTIVATION_REQUEST = 1;
	private boolean isDeviceReported;
	RelativeLayout mRelativeLayout;
	Drawable mBackgroundDrawable = null;

	/**处理加载图片*/
	private Handler mLoadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			QDLog.i(TAG, "Load finished...........");
			if (msg.what == 2 ) {
				if (mBackgroundDrawable != null) {
					mRelativeLayout.setBackground(mBackgroundDrawable);
					mBackgroundDrawable = null;
				}
			}else {

			}
			//加载完图片，然后确定是否已经激活设备管理员权限
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

	/**激活该应用的需要的设备管理员权限*/
	private void activateDeviceAdmin() {
		QDLog.i(TAG, "activateDeviceAdmin is call from appStartActivity===================");
		//DeviceAdminSampleReceiver: 这个类是继承了DeviceAdminReceiver
		//设备管理接收者，该类提供了系统发出的意图动作。
		//你的设备管理应用程序必须包含一个DeviceAdminReceiver 的子类。代表着手机上的设备管理器。
		ComponentName mDeviceAdminSample = new ComponentName(
				this.getApplicationContext(), DeviceAdminWorker.DeviceAdminSampleReceiver.class);
		//getSystemService获得系统服务:设备管理接收者
		DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

		//判断该组件是否有系统管理员的权限
		if (!mDPM.isAdminActive(mDeviceAdminSample)) {
			//没有管理员的权限，则获取管理员的权限
			Intent intent = new Intent(
					DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
					mDeviceAdminSample);
			//会在激活界面中显示的额外内容
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					this.getString(R.string.add_admin_extra_app_text));
			startActivityForResult(intent, ACTIVATION_REQUEST);
		} else {
			startOtherActivity();
		}
	}


	/**
	 * 接收 activateDeviceAdmin()中返回的结果 startActivityForResult(intent, ACTIVATION_REQUEST);
	 * @param requestCode ACTIVATION_REQUEST
	 * @param resultCode
	 * @param data
     */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVATION_REQUEST && resultCode == RESULT_OK) {
			startOtherActivity();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**根据设备绑定责任人的情况，进入不同Activity*/
	private void startOtherActivity() {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				if (EmmClientApplication.mActivateDevice.isDeviceReported()) {
					// 如果设备已经绑定了责任人 ，进入登录画面：UserLoginActivity
					Intent intent = new Intent(AppStartActivity.this, UserLoginActivity.class);
					startActivity(intent);
				}
				else {
					// 没有绑定责任人，则进入隐私声明：PrivacyStatementActivity
					Intent intent = new Intent(AppStartActivity.this,
							PrivacyStatementActivity.class);
					startActivity(intent);
				}
				AppStartActivity.this.finish();
			}
		}, 10);
	}


	/**	加载背景图片*/
	private void loadBackgroundThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				mBackgroundDrawable = BitMapUtil.getDrawableByZoom(AppStartActivity.this, R.mipmap.csr_zc);
//				mRelativeLayout.setBackground(mBackgroundDrawable);
				Message msg = new Message();
				msg.what = 2;
				mLoadHandler.sendMessage(msg);

			}
		}).start();

	}
}
