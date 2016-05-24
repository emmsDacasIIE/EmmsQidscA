package cn.qdsc.msp.ui.qdlayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import cn.qdsc.msp.R;

/**
 * Created by lizhongyi on 2015/12/21.
 */
public class PopMenu implements OnItemClickListener {
	public interface OnItemClickListener {
		 void onItemClick(int index);
	}
	
	private ArrayList<String> itemList;
	private Context mContext;
	private PopupWindow popupWindow;
	private ListView listView;
	private OnItemClickListener listener;
	private LayoutInflater inflater;

	String[] from={"text"};
	int[] to={R.id.text_main_record};

	private int measureContentWidth(ListAdapter listAdapter) {
		ViewGroup mMeasureParent = null;
		int maxWidth = 0;
		View itemView = null;
		int itemType = 0;

		final ListAdapter adapter = listAdapter;
		final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final int positionType = adapter.getItemViewType(i);
			if (positionType != itemType) {
				itemType = positionType;
				itemView = null;
			}

			if (mMeasureParent == null) {
				mMeasureParent = new FrameLayout(mContext);
			}

			itemView = adapter.getView(i, itemView, mMeasureParent);
			itemView.measure(widthMeasureSpec, heightMeasureSpec);

			final int itemWidth = itemView.getMeasuredWidth();

			if (itemWidth > maxWidth) {
				maxWidth = itemWidth;
			}
		}
		return maxWidth;
	}

	public PopMenu(Context mContext,String[] strs) {
		this.mContext = mContext;

		itemList = new ArrayList<>();

		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.popmenu, null);

		listView = (ListView) view.findViewById(R.id.listView);
		ArrayList<HashMap<String,String>> list=new ArrayList<>();
		for(String str:strs){
			HashMap<String,String> map=new HashMap<>();
			map.put("text",str);
			list.add(map);
		}
		SimpleAdapter adapter=new SimpleAdapter(mContext,list,R.layout.pomenu_item,from,to);
		listView.setAdapter(adapter);
		//listView.setAdapter(new PopAdapter());
		listView.setOnItemClickListener(this);


		popupWindow = new PopupWindow(view,
				measureContentWidth(adapter),
				LayoutParams.WRAP_CONTENT);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景（很神奇的）
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	}

	public PopMenu(Context mContext,ArrayList<String> list) {
		this(mContext,(String[])list.toArray(new String[list.size()]));
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (listener != null) {
			listener.onItemClick(position);
		}
		dismiss();
	}

	//设置菜单项点击监听器
	public void setOnItemClickListener(OnItemClickListener listener) {
		 this.listener = listener;
	}


	// 下拉式 弹出 pop菜单 parent 右下角
	public void showAsDropDown(View parent) {
		//保证尺寸是根据屏幕像素密度来的
		popupWindow.showAsDropDown(parent);

		//使其聚集
		popupWindow.setFocusable(true);
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(true);
		//刷新状态
		popupWindow.update();
	}

	//
	public void dismiss() {
		popupWindow.dismiss();
	}
}
