package cn.dacas.emmclient.mdm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.db.EmmClientDb;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.mdm.QQDeleteDialog.IDialogOnclickInterface;

public class MsgListAdapter extends BaseAdapter implements
		IDialogOnclickInterface {

	private List<String> _data;
	private LayoutInflater _inflater;
	private String _query;
	private ForegroundColorSpan _querySpan;

	private QQDeleteDialog myDialog;

	private View currentItemView;
	private int longClickPosition;

	private Context context;

	public MsgListAdapter(Context context, List<String> data) {
		super();
		_data = data;
		_inflater = LayoutInflater.from(context);
		_query = null;
		_querySpan = new ForegroundColorSpan(Color.GREEN);
		this.context = context;

		myDialog = new QQDeleteDialog(this, context, R.style.MyDialogStyleBottom);

		myDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				getCurrentItemView().setBackgroundColor(
						((Activity) MsgListAdapter.this.context).getResources()
								.getColor(android.R.color.white));
			}
		});
	}

	@Override
	public int getCount() {
		return _data.size();
	}

	@Override
	public Object getItem(int position) {
		return _data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = _inflater.inflate(R.layout.list_item_middle, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.subtitle = (TextView) convertView
					.findViewById(R.id.subtitle);
			holder.itemCount = (TextView) convertView
					.findViewById(R.id.itemCount);
			holder.chevron = (ImageView) convertView.findViewById(R.id.chevron);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		String timeMsgItem = _data.get(position);

		try {
			JSONTokener jsonParser = new JSONTokener(timeMsgItem);
			JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
			String time = jsonObject.getString("t");
			String msg = jsonObject.getString("m");
			Date date = new Date();
			DateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss", Locale.CHINA);

				date = sdf.parse(time);
				
			Timestamp timeStamp = new Timestamp(date.getTime());// 获取系统当前时间

			int index = TextUtils.isEmpty(_query) ? -1 : msg.toLowerCase()
					.indexOf(_query.toLowerCase());

			holder.image.setImageResource(android.R.drawable.sym_action_chat);

			holder.subtitle.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(timeStamp));
			holder.itemCount.setVisibility(View.GONE);
			holder.chevron.setVisibility(View.GONE);

			if (index == -1) {
				holder.title.setText(msg);
			} else {
				SpannableStringBuilder ssb = new SpannableStringBuilder(msg);
				ssb.setSpan(_querySpan, index, index + _query.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				holder.title.setText(ssb);
				convertView.setLongClickable(false);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		convertView.setOnClickListener(new OnClickListener() {
			Boolean flag = true;

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TextView tv = (TextView)v.findViewById(R.id.title);
				
				if (flag) {
					flag = false;
					tv.setEllipsize(null); // 展开
					tv.setSingleLine(flag);
				} else {
					flag = true;
					tv.setEllipsize(TextUtils.TruncateAt.END); // 收缩
					tv.setSingleLine(flag);
				}
			}
		});

		convertView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				longClick(position, v);
				return true;
			}

			private void longClick(int index, View view) {
				int[] location = new int[2];
				// 获取当前view在屏幕中的绝对位置
				// ,location[0]表示view的x坐标值,location[1]表示view的坐标值
				view.getLocationOnScreen(location);
				view.setBackgroundColor(_inflater.getContext().getResources()
						.getColor(R.color.blue));
				currentItemView = view;
				longClickPosition = index;
				DisplayMetrics displayMetrics = new DisplayMetrics();
				Display display = ((Activity) _inflater.getContext())
						.getWindowManager().getDefaultDisplay();
				display.getMetrics(displayMetrics);
				WindowManager.LayoutParams params = myDialog.getWindow()
						.getAttributes();
				params.gravity = Gravity.BOTTOM;
				params.y = display.getHeight() - location[1];
				myDialog.getWindow().setAttributes(params);
				myDialog.setCanceledOnTouchOutside(true);
				myDialog.show();
			}
		});

		return convertView;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	// Public methods
	// -----------------------------------------------------------------------------------------
	public void setData(List<String> data) {
		_data = data;
		notifyDataSetChanged();
	}
	
	public String getQuery() {
		return _query;
	}

	public void setQuery(String query) {
		_query = query;
	}

	public List<String> getData() {
		return _data;
	}

	// ViewHolder class
	private class ViewHolder {
		public ImageView image;
		public TextView title;
		public TextView subtitle;
		public TextView itemCount;
		public ImageView chevron;
	}

	public View getCurrentItemView() {
		return currentItemView;
	}

	private void setCurrentItemView(View currentItemView) {
		this.currentItemView = currentItemView;
	}

	public int getLongClickPosition() {
		return longClickPosition;
	}

	private void setLongClickPosition(int longClickPosition) {
		this.longClickPosition = longClickPosition;
	}

	@Override
	public void leftOnclick() {
		// TODO Auto-generated method stub
		myDialog.dismiss();

		try {
			JSONTokener jsonParser = new JSONTokener(_data.get(this
					.getLongClickPosition()));
			JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
			String time = jsonObject.getString("t");
			String msg = jsonObject.getString("m");
			try {
				EmmClientApplication.mDb.deleteDbItemBycolumns(EmmClientDb.DEVICEMSG_DATABASE_TABLE,
						new String[]{"msg", "time"}, new String[]{msg,
								time});
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		getCurrentItemView().setBackgroundColor(
				((Activity) context).getResources().getColor(
						android.R.color.white));

		List<String> data = this.getData();
		data.remove(this.getLongClickPosition());
		this.setData(data);
	}

	@Override
	public void rightOnclick() {
		// TODO Auto-generated method stub
		myDialog.dismiss();
		getCurrentItemView().setBackgroundColor(
				((Activity) context).getResources().getColor(
						android.R.color.white));
	}
}
