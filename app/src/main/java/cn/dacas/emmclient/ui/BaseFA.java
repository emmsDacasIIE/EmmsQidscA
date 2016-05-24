package cn.dacas.emmclient.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.dacas.emmclient.R;


public abstract class BaseFA extends QdscGuideActivity {
	private static String TAG = "BaseFA";

	protected ImageView leftImageView = null;
	protected ImageView rightImageView = null;
	protected TextView leftTextView = null;
	protected TextView middleTextView = null;
	protected TextView rightTextView = null;

	protected RelativeLayout mRelativeLayout = null;

	protected Context mContext;
	protected Activity mActivity;

	/**
	 * 当点击左边的功能返回键时触发的事件，子类实现该接口即可
	 */
	protected OnLeftListener mOnLeftListener;

	/**
	 * 当点击右边的设置键时触发的事件，子类实现该接口即可
	 */
	protected OnRightListener mOnRightListener;
	
	public static enum TitleBar_Style {
		Image_Text_Image,
		Image_Text_Text,
		Text_Text_Text
	}
	
	protected TitleBar_Style mTitleBar_Style;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mTitleBar_Style = TitleBar_Style.Image_Text_Text;

		super.onCreate(savedInstanceState);
		mContext = this;
		mActivity = this;

		initTitlebar();
		initLayout();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void finish() {
		super.finish();
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	////////////////自定义函数/////////////
	/**InitTitlebar和initlayout
	 * 
	 * 1. 包括背景色的设置,必须是先调用setMyContentView，才能再调用setFeatureInt；
	 * 2. 为了统一，需要在子类的initlayout函数中对titlebar进行操作，比如隐藏右边的；设置中间text值
	 */
	private void initTitlebar() {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		mTitleBar_Style = setMyContentView();

		// 通用titlebar
		switch(mTitleBar_Style) {
		case Image_Text_Image:
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.titlebar_image_text_image);
			mRelativeLayout = (RelativeLayout)findViewById(R.id.titlebar_layout);
			leftImageView = (ImageView)findViewById(R.id.imageview_left);
			rightImageView = (ImageView)findViewById(R.id.imageview_right);
			break;
			
		case Image_Text_Text:
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.titlebar_image_text_text);
			mRelativeLayout = (RelativeLayout)findViewById(R.id.titlebar_layout);
			leftImageView = (ImageView)findViewById(R.id.imageview_left);
			rightTextView = (TextView)findViewById(R.id.textview_right);
			break;
		case Text_Text_Text:
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.titlebar_text_text_text);
			mRelativeLayout = (RelativeLayout)findViewById(R.id.titlebar_layout);
			leftTextView = (TextView) findViewById(R.id.textview_left);
			middleTextView = (TextView) findViewById(R.id.textview_mid);
			rightTextView = (TextView) findViewById(R.id.textview_right);
			break;
		}

	}

	private void setLeftOnclickListener() {
		View view = null;
		if (leftImageView != null) {
			view = leftImageView;
		}else if (leftTextView != null){
			view = leftTextView;
		}
		if (view != null) {
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (mOnLeftListener != null) {
						mOnLeftListener.onClick();
					}
				}
			});
		}

	}
	private void setRightOnclickListener() {
		View view = null;
		if (rightImageView != null) {
			view = rightImageView;
		}else if (rightTextView != null){
			view = rightTextView;
		}
		if (view != null) {
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (mOnRightListener != null) {
						mOnRightListener.onClick();
					}
				}
			});
		}
	}

	//////////事件相关///////
	/**
	 * 子类自定义头的左边事件
	 *
	 * @param listener
	 */
	public void setOnClickLeft(OnLeftListener listener) {
		this.mOnLeftListener = listener;
	}

	/**
	 * 子类自定义头的右边事件
	 *
	 * @param listener
	 */
	public void setOnClickRight(OnRightListener listener) {
		this.mOnRightListener = listener;
	}

	/**
	 * 右边的点击事件的接口
	 */
	public interface OnRightListener {

		public void onClick();
	}

	/**
	 * 左边的点击事件的接口
	 */
	public interface OnLeftListener {

		public void onClick();
	}

	////////子类必须实现的方法///////////
	/**
	 * 初始化view，必须由子类实现，并且在子类中，首先以super.initLayout的方式调用此函数
	 */
	protected void initLayout() {
		setLeftOnclickListener();
		setRightOnclickListener();

	}

	/**
	 * 在子类中实现时，只调用setMyContentView方法。
	 * @return titlebar的风格，
	 */
	protected abstract TitleBar_Style setMyContentView();

}
