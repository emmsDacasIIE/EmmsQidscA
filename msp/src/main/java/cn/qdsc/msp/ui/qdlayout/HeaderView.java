package cn.qdsc.msp.ui.qdlayout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.qdsc.msp.R;

public class HeaderView extends LinearLayout {

	private static final String background = "background";

	private Context mContext;

	private View v;

	private TextView mText;

	private LinearLayout mTextBgLayout;


	private ImageView mImageView;

	private CheckBox mAllCheck;


	public HeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		loadView(context, attrs);
	}

	private void loadView(Context context, AttributeSet attrs) {
		LayoutInflater factory = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = factory.inflate(R.layout.header, null);
		setBackground(context, attrs);


		mText = (TextView) v.findViewById(R.id.text);
		mImageView = (ImageView) v.findViewById(R.id.imageview);
		mTextBgLayout = (LinearLayout) v.findViewById(R.id.textBgLayout);

		mAllCheck = (CheckBox) v.findViewById(R.id.allCheck);
		mAllCheck.setVisibility(View.GONE);


//		setBackVisibile(false);
//		setLineFocus(false);

		LayoutParams params = new LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		params.gravity = Gravity.CENTER;
		v.setLayoutParams(params);
		addView(v);
	}

	private void setBackground(Context context, AttributeSet attrs) {
		int resouceId = attrs.getAttributeResourceValue(null, background, 0);

		if (resouceId > 0) {
			Drawable d = context.getResources().getDrawable(resouceId);
			v.setBackgroundDrawable(d);
		}
	}

	public void setText(int resid) {
		mText.setText(resid);
	}

	public void setText(String text) {
		mText.setText(text);
	}

	public void setTextColor(ColorStateList colors) {
		mText.setTextColor(colors);
	}

	public void setTextSize(int size) {
		mText.setTextSize(size);	
	}
	
	public void setImageVisibile(boolean visibile) {
		if (visibile) {
			mImageView.setVisibility(View.VISIBLE);
			return;
		}
		mImageView.setVisibility(View.GONE);
	}

	public void setImageView(int resId) {
		mImageView.setImageResource(resId);
	}

	public void setImageView(Drawable drawable) {
		mImageView.setImageDrawable(drawable);

	}

	public void setTextVisibile(boolean visibile) {
		if (visibile) {
			mText.setVisibility(View.VISIBLE);
			return;
		}
		mText.setVisibility(View.GONE);
	}



	public void setTextBackground(boolean background) {
		if (background) {
//			mTextBgLayout.setBackgroundResource(R.drawable.title_font_bg);
//			mTextBgLayout.setPadding(48, 48, 48, 48);
			mTextBgLayout.setPadding(0, 0, 0, 0);
			return;
		}
		mTextBgLayout.setBackgroundDrawable(null);
	}

	public void setAllCheck(boolean visibile) {
		if (visibile) {
			mAllCheck.setVisibility(View.VISIBLE);
			return;
		}
		mAllCheck.setVisibility(View.GONE);
	}
	
	public CheckBox getAllCheck() {
		return mAllCheck;
	}
	
	public void setChecked(boolean checked) {
		mAllCheck.setChecked(checked);
	}

	public void setOnClickListener(OnClickListener listener) {
		v.setOnClickListener(listener);
	}

	////add ///
	public ImageView getImageView() {
		return mImageView;
	}
	public String getText() {
		if (mText.getText() != null) {
			return mText.getText().toString();
		}
		return "";
	}
}
