package cn.dacas.emmclient.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.ui.activity.loginbind.UserLoginActivity;
import cn.dacas.emmclient.ui.activity.mainframe.MyAboutActivity;
import cn.dacas.emmclient.ui.activity.mainframe.MySettingsActivity;
import cn.dacas.emmclient.util.BitMapUtil;
import cn.dacas.emmclient.util.QDLog;
import cn.dacas.emmclient.util.QdCamera;

/**
 * �󻬲˵������ҡ�
 */
public class MenuLeftFragment extends BaseFragment
{

	private static String TAG = "MenuLeftFragment";

	private View mView;

	private LinearLayout mSettingsLinearLayout;
	private LinearLayout mAboutLinearLayout;


	private ImageView mMyPhotoImageView;
	private TextView mUserNameTextView;
	private TextView mPhoneTextView;

	private ImageView mSettingsArrowImageView;
	private ImageView mAboutArrowImageView;

	private Button mExitLoginButton;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = mActivity = getActivity();

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		if (mView == null)
		{
			initView(inflater, container);
		}
		return mView;
	}

	@Override
	public void onResume() {
		QDLog.i(TAG,"onResume=========000=======================");
		super.onResume();
		setDataForUi();
	}

	private void initView(LayoutInflater inflater, ViewGroup container)
	{
		mView = inflater.inflate(R.layout.left_menu, container, false);
		mMyPhotoImageView = (ImageView)mView.findViewById(R.id.imageview_myphoto);
		mSettingsArrowImageView =  (ImageView)mView.findViewById(R.id.imageView_settings_arrow);
		mAboutArrowImageView =  (ImageView)mView.findViewById(R.id.imageView_about_arrow);
		mExitLoginButton = (Button)mView.findViewById(R.id.button_exitLogin);

		mSettingsLinearLayout = (LinearLayout)mView.findViewById(R.id.ll_settings);

		mAboutLinearLayout = (LinearLayout)mView.findViewById(R.id.ll_about);

		mUserNameTextView = (TextView)mView.findViewById(R.id.textview_username);
		mPhoneTextView = (TextView)mView.findViewById(R.id.textview_phone_number);

		setClickEvent();

	}

	private void setClickEvent() {
		mMyPhotoImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Intent intent = new Intent();
				//intent.setClass(mContext, MyInformationActivity.class);
				//QDLog.i(TAG, "onNotify ============MyInformationActivity===");
				//startActivity(intent);

//				Bundle bundle = new Bundle();
//				bundle.putString(IntentConts.Fish_Detail_Pid, pidString);
//				bundle.putInt(IntentConts.Msg_Source, IntentConts.Home_FishDetail);
//				bundle.putSerializable(IntentConts.Fish_Detail, (Serializable) fishPic);
//				intent.putExtras(bundle);
			}
		});


		mSettingsLinearLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(mContext, MySettingsActivity.class);

				QDLog.i(TAG, "onNotify ===============");
//				Bundle bundle = new Bundle();
//				bundle.putString(IntentConts.Fish_Detail_Pid, pidString);
//				bundle.putInt(IntentConts.Msg_Source, IntentConts.Home_FishDetail);
//				bundle.putSerializable(IntentConts.Fish_Detail, (Serializable) fishPic);

//				intent.putExtras(bundle);
				startActivity(intent);

			}
		});


		mAboutLinearLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(mContext, MyAboutActivity.class);

				QDLog.i(TAG, " ===============");
//				Bundle bundle = new Bundle();
//				bundle.putString(IntentConts.Fish_Detail_Pid, pidString);
//				bundle.putInt(IntentConts.Msg_Source, IntentConts.Home_FishDetail);
//				bundle.putSerializable(IntentConts.Fish_Detail, (Serializable) fishPic);

//				intent.putExtras(bundle);
				startActivity(intent);

			}
		});


		mExitLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				QDLog.i(TAG, "onNotify =======mExitLoginButton========");
				showDeleteDialog();

//				View view = mActivity.getLayoutInflater().inflate(R.layout.photo_choose_dialog, null);
//				initBottomMenu(view);

//				EmmClientApplication.mCheckAccount.clearCurrentAccount();
//				Intent intent = new Intent(mContext, UserLoginActivity.class);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//				startActivity(intent);

			}
		});

	}

	private void setDataForUi() {
		QDLog.i(TAG, "setDataForUi 0000begin===========");
		String headphotoName = QdCamera.GetHeadFullPathName(mContext);
		if (!TextUtils.isEmpty(headphotoName)) {
			File f = new File(headphotoName);
			if (f != null && f.exists())
				setImageView(headphotoName);
		}

		if (EmmClientApplication.mUserModel!= null) {
			mUserNameTextView.setText(EmmClientApplication.mUserModel.getName());
			mPhoneTextView.setText(EmmClientApplication.mUserModel.getTelephone_number());
		}
	}

	private void setImageView(String realPath) {
		QDLog.i(TAG, "setImageView ===================path======" + realPath);
		BitMapUtil.setImageSrc(mMyPhotoImageView, realPath);
	}

	////底部菜单的实现

	private Button mPromptTitleButton;
	private Button mConfirmButton;
	private Button mCancelButton;

	private Dialog mPopupMenuDialog;

	private void showDeleteDialog() {
		View view = mActivity.getLayoutInflater().inflate(R.layout.photo_choose_dialog, null);
		initBottomMenu(view);
		mPopupMenuDialog = new Dialog(mContext, R.style.transparentFrameWindowStyle);
		mPopupMenuDialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		Window window = mPopupMenuDialog.getWindow();
		// 设置显示动画
		window.setWindowAnimations(R.style.main_menu_animstyle);
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = mActivity.getWindowManager().getDefaultDisplay().getHeight();
		// 以下这两句是为了保证按钮可以水平满屏
		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
		wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

		// 设置显示位置
		mPopupMenuDialog.onWindowAttributesChanged(wl);
		// 设置点击外围解散
		mPopupMenuDialog.setCanceledOnTouchOutside(true);
		mPopupMenuDialog.show();
	}



	private void  initBottomMenu(View v) {
		mPromptTitleButton = (Button) v.findViewById(R.id.button_bottom_menu_takePic);
		mConfirmButton = (Button) v.findViewById(R.id.button_bottom_menu_choicePic);
		mCancelButton = (Button) v.findViewById(R.id.button_bottom_menu_cancel);

		mPromptTitleButton.setText(R.string.home_exit_login_prompt);
		mConfirmButton.setText(R.string.msg_delete_confirm);
		mCancelButton.setText(R.string.msg_delete_cancel);

		mPromptTitleButton.setEnabled(false);
		mPromptTitleButton.setTextColor(mContext.getResources().getColor(R.color.msg_prompt_title_color));

		mConfirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				QDLog.i(TAG, "onNotify =======mExitLoginButton========");
				EmmClientApplication.mCheckAccount.clearCurrentAccount();
				Intent intent = new Intent(mContext, UserLoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);


				if (mPopupMenuDialog != null && mPopupMenuDialog.isShowing()) {
					mPopupMenuDialog.dismiss();
				}
			}
		});

		mCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPopupMenuDialog != null && mPopupMenuDialog.isShowing()) {
					mPopupMenuDialog.dismiss();
				}
			}
		});
	}
}
