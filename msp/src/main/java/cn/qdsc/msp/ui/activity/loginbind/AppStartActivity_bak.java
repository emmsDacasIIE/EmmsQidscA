package cn.qdsc.msp.ui.activity.loginbind;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.umeng.analytics.MobclickAgent;

import cn.qdsc.msp.R;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.util.BitMapUtil;
import cn.qdsc.msp.util.QDLog;
import cn.qdsc.msp.core.mdm.DeviceAdminWorker;


public class AppStartActivity_bak extends Activity {
	private static final String TAG = "AppStartActivity";
	private static final int ACTIVATION_REQUEST = 1;
	LinearLayout mRelativeLayout;
	Drawable mBackgroundDrawable = null;

	ImageView mImageView1,mImageView2,mImageView3 ;

	Context mContext;
	Bitmap bitmap = null;


	private Handler mLoadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			QDLog.i(TAG, "Load finished...........");
			switch (msg.what) {
				case R.mipmap.csr_qd1:
					loadBackgroundThread(mImageView2, R.mipmap.csr_qd1);
					if (bitmap != null) {
						mImageView1.setImageBitmap(bitmap);
						bitmap.recycle();
						bitmap = null;
						Message msg1 = new Message();
						msg1.what = R.mipmap.csr_qd2;
						mLoadHandler.sendMessage(msg);
					}

					break;
				case R.mipmap.csr_qd2:
					loadBackgroundThread(mImageView2, R.mipmap.csr_qd2);
					if (bitmap != null) {
						mImageView2.setImageBitmap(bitmap);

						bitmap.recycle();
						bitmap = null;
						Message msg3 = new Message();
						msg3.what = R.mipmap.csr_qd3;
						mLoadHandler.sendMessage(msg3);
					}

					break;
				case R.mipmap.csr_qd3:

					loadBackgroundThread(mImageView3,R.mipmap.csr_qd3);
					if (bitmap != null) {
						mImageView3.setImageBitmap(bitmap);

						bitmap.recycle();
						bitmap = null;
//						Message msg3 = new Message();
//						msg3.what = R.mipmap.csr_qd3;
//						mLoadHandler.sendMessage(msg3);
					}
					break;

			}
//			if (mBackgroundDrawable != null) {
//				mRelativeLayout.setBackground(mBackgroundDrawable);
//				mBackgroundDrawable = null;
//			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activate_device);
		mRelativeLayout = (LinearLayout)findViewById(R.id.start_activity_layout);

		mImageView1 = (ImageView)findViewById(R.id.top_imageview);
		mImageView2 = (ImageView)findViewById(R.id.center_imageview);
		mImageView3 = (ImageView)findViewById(R.id.bottom_imageview);

		mContext = this;
//		loadBackgroundThread();
		Message msg = new Message();
		msg.what = R.mipmap.csr_qd1;
		mLoadHandler.sendMessage(msg);
		QDLog.i(TAG,"AppStartActivity onCreate========1===========");
	}

	public void onStart() {
		super.onStart();
		QDLog.i(TAG, "AppStartActivity onStart========2===========");
		activateDeviceAdmin();
	}
	
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		QDLog.i(TAG, "AppStartActivity onResume========3===========");
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		QDLog.i(TAG, "AppStartActivity onPause===================");
	}

	private void activateDeviceAdmin() {
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
			new Handler().postDelayed(new Runnable() {
				public void run() {
					if (EmmClientApplication.mActivateDevice.isDeviceReported()) {
						Intent intent = new Intent(AppStartActivity_bak.this, UserLoginActivity.class);
						startActivity(intent);
					}
					else {
						Intent intent = new Intent(AppStartActivity_bak.this,
								PrivacyStatementActivity.class);
						startActivity(intent);
					}
					AppStartActivity_bak.this.finish();
				}
			}, 1500);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVATION_REQUEST && resultCode != RESULT_OK) {
			activateDeviceAdmin();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	//加载背景图片
	private void loadBackgroundThread(final ImageView imageView,final int resId) {

//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				mBackgroundDrawable = BitMapUtil.getDrawableByZoom(AppStartActivity.this, R.mipmap.csr_qd); //  //chat_bg_default
//				if (mBackgroundDrawable != null) {
//					mLoadHandler.sendEmptyMessage(1);
//				}
//
//			}
//		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				bitmap = BitMapUtil.fetchBitmapFromRes(mContext,resId);
				if (bitmap != null) {

//					if (resId == R.mipmap.csr_qd1) {
//						Message msg1 = new Message();
//						msg1.what = R.mipmap.csr_qd1;
//						mLoadHandler.sendMessage(msg1);
//					}
//					else if (resId == R.mipmap.csr_qd2) {
//						Message msg2 = new Message();
//						msg2.what = R.mipmap.csr_qd2;
//						mLoadHandler.sendMessage(msg2);
//					}else if (resId == R.mipmap.csr_qd2) {
//						Message msg3 = new Message();
//						msg3.what = R.mipmap.csr_qd3;
//						mLoadHandler.sendMessage(msg3);
//					}

				}


//				Message msg = new Message();
//				msg.what = resId;
//				mLoadHandler.sendMessage(msg);

			}
		}).start();

	}
}
