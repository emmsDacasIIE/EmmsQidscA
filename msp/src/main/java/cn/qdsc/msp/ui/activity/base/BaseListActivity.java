/**
 * 这个基类是一个List加一个Button界面,也可以施展为整个界面只是一个List
 */
package cn.qdsc.msp.ui.activity.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.qdsc.msp.R;
import cn.qdsc.msp.ui.qdlayout.ScrollBackListView;

public abstract class BaseListActivity extends BaseSlidingFragmentActivity {
	protected ScrollBackListView mListView;
	protected BaseAdapter mAdapter;

	protected LinearLayout mButtonLayout;

	protected Button 	  mLeftButton;
	protected Button 	  mRightButton;

	protected List<Object> mDataArray;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listview_root_layout, null, null);

		mButtonLayout = (LinearLayout) findViewById(R.id.button_layout);

		mLeftButton = (Button) findViewById(R.id.left_btn);
		mRightButton = (Button) findViewById(R.id.right_btn);

		mDataArray = new ArrayList<Object>();
		
		InitListView();
		initListAdapter();
		initButton();

		//来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
		//就不会显示左侧的menu了。
//		setBehindContentView(R.layout.left_menu_frame);
//		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	protected void setButtonLayoutEnable(boolean state) {
		if (state) {
			mButtonLayout.setVisibility(View.VISIBLE);
			return;
		}
		mButtonLayout.setVisibility(View.GONE);
//		if(state) {
//			mButton.setBackgroundResource(R.drawable.button_red_selector);
//			mButton.setEnabled(true);
//		}
//		else {
//			mButton.setBackgroundResource(R.drawable.button_gray_selector);
//			mButton.setEnabled(false);
//		}
	}
	
	private void InitListView() {
		mListView = (ScrollBackListView) findViewById(R.id.listview);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				onListItemClicked(arg0, arg1, arg2, arg3);
			}
		});
	}

	protected abstract void initListAdapter();
	
	protected abstract void initButton();

	protected abstract void onListItemClicked(AdapterView<?> arg0, View arg1, int arg2, long arg3);
	
	protected class ListAdapter extends BaseAdapter {

		protected List<Object> mDataArray;
		protected Context mContext;
		protected LayoutInflater mInflater;

		protected ViewHolder mHolder;

		public ListAdapter(Context context, List<Object> dataArray) {
			this.mDataArray = dataArray;
			this.mContext = context;
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			reset();
		}

		@Override
		public int getCount() {
			return mDataArray.size();
		}

		@Override
		public Object getItem(int position) {
			return mDataArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;

			if (convertView == null) {
				mHolder = new ViewHolder(position);
				view = mInflater.inflate(R.layout.listitem_privacy_settings, null);
				mHolder.mMajorTextView = (TextView) view.findViewById(R.id.major_txt);
				mHolder.mMinorTextView = (TextView) view.findViewById(R.id.minor_txt);
				mHolder.mExpandImage = (ImageView) view.findViewById(R.id.minor_image);
				mHolder.mCheckBox = (CheckBox) view.findViewById(R.id.checkBox);
				mHolder.mItemLinearLayout = (LinearLayout) view.findViewById(R.id.listLayout);

				mHolder.mExpandImage.setVisibility(View.GONE);
				mHolder.mCheckBox.setVisibility(View.GONE);
				mHolder.mMinorTextView.setVisibility(View.GONE);



//				mHolder.mLeftBtn = (Button) view.findViewById(R.id.left_btn);
//				mHolder.mRightBtn = (Button) view.findViewById(R.id.right_btn);

//				mHolder.mButtonLayout = (LinearLayout) view.findViewById(R.id.bottom_layout);

				view.setTag(mHolder);
			} else {
				view = convertView;
			}


			return view;
		}

		public void reset() {
			for (int i = 0; i < this.mDataArray.size(); i++) {

			}
		}
	}
	
	public class ViewHolder {
		public int			mPosition;
		public ImageView	mImageView, mExpandImage;
		public TextView 	mMajorTextView, mMinorTextView;
		public Button mLeftBtn, mMidBtn, mRightBtn;
		public CheckBox mCheckBox;
		
		public LinearLayout mItemLinearLayout;
		public LinearLayout mButtonLayout;

		
		public ViewHolder(int pos) {
			this.mPosition = pos;
		}
		
		public ViewHolder() {}
		
//		public void enable(Button btn, boolean state) {
//			btn.setBackgroundResource(state?R.drawable.button_red_small_selector:R.drawable.gray_small_button_normal);
//			btn.setTextColor(state?mContext.getResources().getColor(R.color.white):mContext.getResources().getColor(R.color.header_leftAndRight_text));
//			btn.setClickable(state);
//			btn.setEnabled(state);
//		}
	}
}
