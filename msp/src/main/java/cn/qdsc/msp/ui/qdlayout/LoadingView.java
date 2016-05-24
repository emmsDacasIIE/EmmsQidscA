package cn.qdsc.msp.ui.qdlayout;


import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.qdsc.msp.R;

public class LoadingView extends LinearLayout {

	private Context mContext;

	private LayoutInflater mFactory;

	private ImageView mAnimationIV;

	private AnimationDrawable mAnimationDrawable;
	
    private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			mAnimationIV.setImageResource(R.drawable.loading);
			mAnimationDrawable = (AnimationDrawable) mAnimationIV.getDrawable();
			mAnimationDrawable.start();
			super.handleMessage(msg);
		}
    	
    };

	public LoadingView(Context context) {
		super(context);
		mContext = context;
		mFactory = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		initLayout();
		start();
	}

	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mFactory = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		initLayout();
		start();
	}

	private void initLayout() {
		LayoutParams params = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		mAnimationIV = new ImageView(mContext);
		addView(mAnimationIV, params);

		mAnimationIV.setImageResource(R.drawable.loading);
	}

	public void start() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(500);
					mHandler.sendEmptyMessage(0);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}).start();
	}

	public void stop() {
		mAnimationDrawable = (AnimationDrawable) mAnimationIV.getDrawable();
		mAnimationDrawable.stop();
	}
}
