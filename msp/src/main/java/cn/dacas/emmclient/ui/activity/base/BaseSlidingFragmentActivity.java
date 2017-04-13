package cn.dacas.emmclient.ui.activity.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.analytics.MobclickAgent;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.ui.qdlayout.HeaderView;
import cn.dacas.emmclient.manager.ActivityManager;

public abstract class BaseSlidingFragmentActivity extends SlidingFragmentActivity  {

	protected Context mContext;
	protected LayoutInflater mInflater;
	protected LinearLayout layoutDots;
	/**
	 * 当点击左边的功能键时触发的事件，子类实现该接口即可
	 */
	protected OnLeftListener mOnLeftListener;

	/**
	 * 当点击右边的功能键时触发的事件，子类实现该接口即可
	 */
	protected OnRightListener mOnRightListener;

	/**
	 * 当点击右边的全选按钮设置键时触发的事件，子类实现该接口即可
	 */
	protected OnRightCheckedChangeListener mOnRightCheckedChangeListener;

	/**
	 * 当点击左边的返回键时跳转到主页触发的事件，子类实现该接口即可 （一般用于没有正常启动主页的行为动作）
	 */
	protected OnLeft2HomeListener mOnLeft2HomeListener;

    protected  RelativeLayout mParentLayout;
	protected LinearLayout mBodyLayout;

	protected HeaderView mLeftHeaderView, mMiddleHeaderView, mRightHeaderView, mSubRightHeaderView;

	private LinearLayout mBottomLayout;

	protected LinearLayout mDocRelativeLayout, mAppRelativeLayout, mPushRelativeLayout,mContactsRelativeLayout;

	//每个titlebar都有一个风格
	protected enum HearderView_Style{
		Null_Text_Null,
		Text_Text_Null,
		Text_Text_Text,
		Text_Text_Image,
		Image_Text_Null,
		Image_Text_Text,
		Image_Text_Image,
		Image_Text_Image_Image
	}

	HearderView_Style mHearderView_Style;
	
//	private QuickNavigationMenu mQuickNavigationMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mHearderView_Style = setHeaderViewStyle();
		if (mHearderView_Style == HearderView_Style.Image_Text_Image_Image) {
			setContentView(R.layout.activity_base_home);
		}else {
			setContentView(R.layout.activity_base);
		}

		initLayout();

		setHeaderView();
		titleTest();
		layoutDots = (LinearLayout) findViewById(R.id.llyt_dots);
		//NavigationBar透明化
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = getWindow();
			// Translucent navigation bar
			window.setFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}


	}

    @Override
    protected  void onStart() {
        super.onStart();
            if (EmmClientApplication.runningBackground && EmmClientApplication.intervals >= EmmClientApplication.LockSecs) {
                ActivityManager.gotoUnlockActivity();
                EmmClientApplication.intervals = 0;
            }
        EmmClientApplication.runningBackground=false;
    }
	
	@Override
	protected void onResume() {
		super.onResume();
        MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
        MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        EmmClientApplication.foregroundIntervals=0;
        return  super.dispatchTouchEvent(event);
    }

	private void titleTest() {
		mMiddleHeaderView.setText("middletest");
		mLeftHeaderView.setText("lefttest");
		mRightHeaderView.setText("righttest");

	}

	private void initLayout() {
		mContext = this;
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBodyLayout = (LinearLayout) findViewById(R.id.bodyLayout);
		mLeftHeaderView = (HeaderView) findViewById(R.id.leftHeaderView);
		mMiddleHeaderView = (HeaderView) findViewById(R.id.middleHeaderView);
		mRightHeaderView = (HeaderView) findViewById(R.id.rightHeaderView);
		mRightHeaderView.setVisibility(View.GONE);

		mSubRightHeaderView = (HeaderView) findViewById(R.id.sub_rightHeaderView);
		mSubRightHeaderView.setVisibility(View.GONE);

        mParentLayout=(RelativeLayout)findViewById(R.id.parentLayout);

		initBottomLayout();


		ColorStateList colors = mContext.getResources().getColorStateList(
				R.color.white);
		mMiddleHeaderView.setTextColor(colors);
		mLeftHeaderView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mOnLeftListener != null) {
					mOnLeftListener.onClick();
					return;
				}
				if (mOnLeft2HomeListener != null) {
					mOnLeft2HomeListener.onClick();
					return;
				}
				finish();
			}
		});
		mRightHeaderView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mOnRightListener != null) {
					mOnRightListener.onClick();
				}
				if (mOnRightCheckedChangeListener != null) {
					mRightHeaderView.getAllCheck().toggle();
					mOnRightCheckedChangeListener
							.onCheckedChanged(mRightHeaderView.getAllCheck()
									.isChecked());
				}
			}
		});

	}

	/**
	 * 子类自定义头的右边事件
	 *
	 * @param
	 * @param listener
	 */
	public void setOnClickRight(OnRightListener listener) {
		mRightHeaderView.setVisibility(View.VISIBLE);
		this.mOnRightListener = listener;
	}

	/**
	 * 子类自定义头的右边事件
	 * 
	 * @param resid
	 * @param listener
	 */
	public void setOnClickRight(int resid, OnRightListener listener) {
		setOnClickRight(mContext.getString(resid), listener);
	}

	/**
	 * 子类自定义头的右边事件
	 * 
	 * @param
	 * @param listener
	 */
	public void setOnClickRight(String text, OnRightListener listener) {
		mRightHeaderView.setVisibility(View.VISIBLE);
		mRightHeaderView.setText(text);
		this.mOnRightListener = listener;
	}

	/**
	 * 子类自定义头的右边事件
	 * 
	 * @param
	 * @param listener
	 */
	public void setOnClickRight4Check(OnRightCheckedChangeListener listener) {
		mRightHeaderView.setVisibility(View.VISIBLE);
		mRightHeaderView.setAllCheck(true);
		this.mOnRightCheckedChangeListener = listener;
	}
	

	/**
	 * 子类自定义头的右边事件
	 * 
	 * @param
	 * @param
	 */
	public void setOnClickRight4CheckVisibile(boolean visibile) {
		if (mOnRightCheckedChangeListener != null) {
			mRightHeaderView.setAllCheck(visibile);
		}
	}

	
	/**
	 * 子类自定义头的右边事件
	 * 
	 * @param
	 * @param
	 */
	public void setOnClickRight4Checked(boolean checked) {
		if (mOnRightCheckedChangeListener != null) {
			mRightHeaderView.setChecked(checked);
		}
	}
	
	/**
	 * 子类自定义头的右边事件
	 * 
	 * @param
	 * @param
	 */
	public HeaderView getOnClickRight4Check() {
		if (mOnRightCheckedChangeListener != null) {
			return mRightHeaderView;
		}
		return null;
	}
	
	
	/**
	 * 子类自定义头的左边的事件
	 * 
	 * @param listener
	 */
	public void setOnClickLeft(int resid, boolean backIcon, OnLeftListener listener) {
		setOnClickLeft(mContext.getString(resid), backIcon, listener);
	}

	/**
	 * 子类自定义头的左边的事件
	 * 
	 * @param listener
	 */
	public void setOnClickLeft(String text, boolean backIcon,OnLeftListener listener) {
		mLeftHeaderView.setText(text);
		mLeftHeaderView.setImageVisibile(backIcon);
//		mLeftHeaderView.setLineFocus(lineFocus);
		this.mOnLeftListener = listener;
	}

	/**
	 * 子类自定义头的左边返回主页的事件
	 * 
	 * @param listener
	 */
	public void onClickLeft2Home(OnLeft2HomeListener listener) {
		this.mOnLeft2HomeListener = listener;
	}

	/**
	 * 子类定制自己的body样式
	 * 
	 * @param layout
	 *            样式xml文件
	 * @param middleTitle
	 *            R的string值
	 */
	public void setContentView(int layout, int middleTitle) {
		setContentView(layout, mContext.getString(middleTitle));
	}

	/**
	 * 子类定制自己的body样式
	 * 必须要在子类中调用的，否则，就不会显示head和bottom
	 * @param layout
	 *            样式xml文件
	 * @param
	 *
	 */
	public void setContentView(int layout, String middleTitle) {
		mMiddleHeaderView.setText(middleTitle);
		mInflater.inflate(layout, mBodyLayout);
	}

	/**
	 * 子类定制自己的body样式
	 * 
	 * @param layout
	 * @param middleTitle
	 * @param middleBackground
	 *            标题的背景
	 */
	public void setContentView(int layout, String middleTitle,
			boolean middleBackground) {
		mMiddleHeaderView.setText(middleTitle);
		mMiddleHeaderView.setTextBackground(middleBackground);
		mInflater.inflate(layout, mBodyLayout);
	}

	/**
	 * 子类定制自己的body样式
	 * 
	 * @param layout
	 * @param middleTitle
	 * @param middleBackground
	 *            标题的背景
	 */
	public void setContentView(int layout, int middleTitle,
			boolean middleBackground) {
		setContentView(layout, mContext.getString(middleTitle),
				middleBackground);
	}

	/**
	 * 设置左边标题和中间标题
	 * 
	 * @param layout
	 * @param leftTitle
	 * @param middleTitle
	 */
	public void setContentView(int layout, int leftTitle, int middleTitle) {
		setContentView(layout, mContext.getString(leftTitle),
				mContext.getString(middleTitle));
	}

	/**
	 * 设置左边标题和中间标题
	 * 
	 * @param layout
	 * @param leftTitle
	 * @param middleTitle
	 */
	public void setContentView(int layout, String leftTitle, String middleTitle) {
		if(!TextUtils.isEmpty(leftTitle))
			mLeftHeaderView.setText(leftTitle);
		if(!TextUtils.isEmpty(middleTitle))
			mMiddleHeaderView.setText(middleTitle);
		mInflater.inflate(layout, mBodyLayout);
	}
	
	/**
	 * 设置左边标题和中间标题
	 * 
	 * @param v
	 * @param leftTitle
	 * @param middleTitle
	 */
	public void setContentView(View v, String leftTitle, String middleTitle) {
		if(!TextUtils.isEmpty(leftTitle))
			mLeftHeaderView.setText(leftTitle);
		if(!TextUtils.isEmpty(middleTitle))
			mMiddleHeaderView.setText(middleTitle);
		mBodyLayout.addView(v);
	}

	/**
	 * 设置左边标题和中间标题
	 * 
	 * @param v
	 * @param leftTitle
	 * @param middleTitle
	 */
	public void setContentView(View v, int leftTitle, int middleTitle) {
		setContentView(v, mContext.getString(leftTitle),
				mContext.getString(middleTitle));
	}
	
	/**
	 * 设置左边标题和中间标题
	 * 
	 * @param layout
	 * @param leftTitle
	 * @param middleTitle
	 * @param middleBackground
	 *            中间标题的背景
	 */
	public void setContentView(int layout, int leftTitle, int middleTitle,
			boolean middleBackground) {
		setContentView(layout, mContext.getString(leftTitle),
				mContext.getString(middleTitle), middleBackground);
	}

	/**
	 * 设置左边标题和中间标题
	 * 
	 * @param layout
	 * @param leftTitle
	 * @param middleTitle
	 * @param middleBackground
	 *            中间标题的背景
	 */
	public void setContentView(int layout, String leftTitle,
			String middleTitle, boolean middleBackground) {
		setContentView(layout, leftTitle, middleTitle);
		mMiddleHeaderView.setTextBackground(middleBackground);
	}

	public void setLinesFocus(boolean left, boolean middle, boolean right) {
//		mLeftHeaderView.setLineFocus(left);
//		mMiddleHeaderView.setLineFocus(middle);
//		mRightHeaderView.setLineFocus(right);
	}
	
	public void setMainTitle(int title) {
		mMiddleHeaderView.setText(this.getString(title));
	}
	
	public void setMainTitle(String title) {
		mMiddleHeaderView.setText(title);
	}
	
	/**
	 * 针对删除操作时抽象的统一删除弹框接口
	 * 
	 * @param listener
	 */
	public void onDialogDelete(final OnDialogDeleteListener listener) {
//		CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
//		builder.setTitle(R.string.dialog_prompt_title);
//		builder.setMessage(R.string.dialog_prompt_message);
//		builder.setNegativeButton(getString(R.string.ok),
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//						if (listener != null) {
//							listener.onClick();
//						}
//					}
//				});
//
//		builder.setPositiveButton(getString(R.string.cancel),
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				});
//
//		CustomDialog mCustomDialog = builder.create();
//		mCustomDialog.show();
	}
	
	/**
	 * 弹出一个只有一个确定按钮的对话框
	 * @param msg
	 */
	public void showOnePositiveBtnDialog(String msg) {
//		CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
//		builder.setTitle(R.string.dialog_prompt_title);
//		builder.setMessage(msg);
//		builder.setPositiveButton(getString(R.string.ok),
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				});
//
//		CustomDialog mCustomDialog = builder.create();
//		mCustomDialog.show();
	}
	
	/**
	 * @param
	 */
	public void showTwoBtnDialog(Context context, String title, String prompt, String btnOK, 
			String btnCancel, DialogInterface.OnClickListener okListener) {
//		CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
//		builder.setTitle(title);
//		builder.setMessage(prompt);
//		builder.setNegativeButton(btnOK,okListener);
//		builder.setPositiveButton(btnCancel,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				});
//
//		CustomDialog mCustomDialog = builder.create();
//		mCustomDialog.show();
	}
	
//	public void setQuickNavigationMenu(List<MenuData> menuArray) {
//		mQuickNavigationMenu = new QuickNavigationMenu(mContext, menuArray);// 出现与消失的动画
//		mQuickNavigationMenu.update();
//	}
	
	@Override
	/** 
	 * 创建MENU 
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		//menu.add("menu");// 必须创建一项
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	/** 
	 * 拦截MENU 
	 */
	public boolean onMenuOpened(int featureId, Menu menu) {
//		if (mQuickNavigationMenu != null) {
//			if (mQuickNavigationMenu.isShowing())
//				mQuickNavigationMenu.dismiss();
//			else {
//				mQuickNavigationMenu.showAtLocation(findViewById(R.id.parentLayout),
//						Gravity.BOTTOM, 0, 0);
//			}
//		}
		return true;// 返回为true 则显示系统menu
	}


    public interface OnLeftListener {

		public void onClick();
	}

	public interface OnRightListener {

		public void onClick();
	}

	public interface OnRightCheckedChangeListener {

		public void onCheckedChanged(boolean checked);
	}

	public interface OnLeft2HomeListener {

		public void onClick();
	}

	public interface OnDialogDeleteListener {

		public void onClick();
	}

	private void setHeaderView() {
		switch (mHearderView_Style) {
			case Null_Text_Null:
				//set visibile
				mLeftHeaderView.setVisibility(View.GONE);
				mMiddleHeaderView.setVisibility(View.VISIBLE);
				mRightHeaderView.setVisibility(View.GONE);
				mSubRightHeaderView.setVisibility(View.GONE);

				mMiddleHeaderView.setTextVisibile(true);
				mMiddleHeaderView.setImageVisibile(false);

				break;

			case Text_Text_Null:
				//set visibile
				mLeftHeaderView.setVisibility(View.VISIBLE);
				mMiddleHeaderView.setVisibility(View.VISIBLE);
				mRightHeaderView.setVisibility(View.GONE);
				mSubRightHeaderView.setVisibility(View.GONE);

				mLeftHeaderView.setTextVisibile(true);
				mMiddleHeaderView.setTextVisibile(true);

				mLeftHeaderView.setImageVisibile(false);
				mMiddleHeaderView.setImageVisibile(false);

				break;

			case Text_Text_Image:
				//set visibile
				mLeftHeaderView.setVisibility(View.VISIBLE);
				mMiddleHeaderView.setVisibility(View.VISIBLE);
				mRightHeaderView.setVisibility(View.VISIBLE);
				mSubRightHeaderView.setVisibility(View.GONE);

				mLeftHeaderView.setTextVisibile(true);
				mMiddleHeaderView.setTextVisibile(true);
				mRightHeaderView.setTextVisibile(true);

				mLeftHeaderView.setImageVisibile(false);
				mMiddleHeaderView.setImageVisibile(false);
				mRightHeaderView.setImageVisibile(false);
				break;


			case Image_Text_Null:
				mLeftHeaderView.setVisibility(View.VISIBLE);
				mMiddleHeaderView.setVisibility(View.VISIBLE);
				mRightHeaderView.setVisibility(View.GONE);
				mSubRightHeaderView.setVisibility(View.GONE);

				mLeftHeaderView.setTextVisibile(false);
				mMiddleHeaderView.setTextVisibile(true);
				mRightHeaderView.setTextVisibile(false);

				mLeftHeaderView.setImageVisibile(true);
				mMiddleHeaderView.setImageVisibile(false);
				mRightHeaderView.setImageVisibile(false);
				break;


			case Image_Text_Text:
				mLeftHeaderView.setVisibility(View.VISIBLE);
				mMiddleHeaderView.setVisibility(View.VISIBLE);
				mRightHeaderView.setVisibility(View.VISIBLE);
				mSubRightHeaderView.setVisibility(View.GONE);

				mLeftHeaderView.setTextVisibile(false);
				mMiddleHeaderView.setTextVisibile(true);
				mRightHeaderView.setTextVisibile(true);

				mLeftHeaderView.setImageVisibile(true);
				mMiddleHeaderView.setImageVisibile(false);
				mRightHeaderView.setImageVisibile(false);
				break;

			case Image_Text_Image:
				mLeftHeaderView.setVisibility(View.VISIBLE);
				mMiddleHeaderView.setVisibility(View.VISIBLE);
				mRightHeaderView.setVisibility(View.VISIBLE);
				mSubRightHeaderView.setVisibility(View.GONE);

				mLeftHeaderView.setTextVisibile(false);
				mMiddleHeaderView.setTextVisibile(true);
				mRightHeaderView.setTextVisibile(false);

				mLeftHeaderView.setImageVisibile(true);
				mMiddleHeaderView.setImageVisibile(false);
				mRightHeaderView.setImageVisibile(true);
				break;

			case Image_Text_Image_Image:
				mLeftHeaderView.setVisibility(View.VISIBLE);
				mMiddleHeaderView.setVisibility(View.VISIBLE);
				mRightHeaderView.setVisibility(View.VISIBLE);
				mSubRightHeaderView.setVisibility(View.VISIBLE);

				mLeftHeaderView.setTextVisibile(false);
				mMiddleHeaderView.setTextVisibile(true);
				mRightHeaderView.setTextVisibile(false);

				mLeftHeaderView.setImageVisibile(true);
				mMiddleHeaderView.setImageVisibile(false);
				mRightHeaderView.setImageVisibile(true);

				break;

		}

		if (mHearderView_Style != HearderView_Style.Image_Text_Image_Image) {
			//来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
			//就不会显示左侧的menu了。
			setBehindContentView(R.layout.left_menu_frame);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

	}

	abstract protected HearderView_Style setHeaderViewStyle();

	//////////////////bottom控件事件的处理////////////

	private void initBottomLayout() {

		mBottomLayout = (LinearLayout) findViewById(R.id.bottomLayout);
		mBottomLayout.setVisibility(View.GONE);

		mAppRelativeLayout = (LinearLayout)findViewById(R.id.ly_bottomItem1);
		mDocRelativeLayout = (LinearLayout)findViewById(R.id.ly_bottomItem2);
		mContactsRelativeLayout = (LinearLayout)findViewById(R.id.ly_bottomItem3);
		mPushRelativeLayout = (LinearLayout)findViewById(R.id.ly_bottomItem4);
	}

	protected void setmBottomLayoutVisibility(int visibility) {
		mBottomLayout.setVisibility(visibility);
	}


	protected void SetOnClickBottomItemListener(final OnClickBottomItemListener listener) {



		mAppRelativeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClickBottomItem(OnClickBottomItemListener.Enum_ItemId.Item_App);
			}
		});

		mDocRelativeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClickBottomItem(OnClickBottomItemListener.Enum_ItemId.Item_Doc);
			}
		});

		mContactsRelativeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClickBottomItem(OnClickBottomItemListener.Enum_ItemId.Item_Contacts);
			}
		});

		mPushRelativeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClickBottomItem(OnClickBottomItemListener.Enum_ItemId.Item_Push);
			}
		});

	}

	public interface OnClickBottomItemListener {
		public static enum Enum_ItemId {
			Item_App,
			Item_Doc,
			Item_Contacts,
			Item_Push
		}
		public void onClickBottomItem(Enum_ItemId itemId);


	}

}
