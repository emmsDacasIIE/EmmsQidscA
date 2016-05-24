package cn.dacas.emmclient.mcm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.db.EmmClientDb;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.mam.RefreshableView;
import cn.dacas.emmclient.mam.RefreshableView.PullToRefreshListener;
import cn.dacas.emmclient.util.MyJsonArrayRequest;
import cn.dacas.emmclient.util.NetworkDef;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.UpdateTokenRequest;
import cn.dacas.emmclient.worker.DownLoadFileFromUrl;

/**
 * A placeholder fragment containing a simple view.
 */
public class DocListFragment extends Fragment {
	private static final String TAG = "DocListFragment";

	// 下载完成修改状态
	public static final String ACTION_REFRESH_DOC = "cn.dacas.emmclient.mam.REFRESH_DOC";

	private SharedPreferences settings = null;

	private BroadcastReceiver mRefreshReceiver;

	private RefreshableView refreshableView;
	private ListView docsListView = null;
	DocListAdapter mAdapter = null;
	private TextView noDocText = null;

	private View rootView;

	private boolean isNetworkOK = true;

	private ProgressDialog mProgressDlg;

	private MyJsonArrayRequest jsonArrayRequest;

	// TODO:添加新消息
	private ArrayList<DocListItem> docsArrayList = new ArrayList<DocListItem>();

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static Fragment newInstance(int sectionNumber) {
		DocListFragment curFragment = new DocListFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		curFragment.setArguments(args);
		return curFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_doc_list, container,
				false);

		settings = this.getActivity().getSharedPreferences(PrefUtils.PREF_NAME,
				0);

		docsListView = (ListView) rootView.findViewById(R.id.docsListView);
		noDocText = (TextView) rootView.findViewById(R.id.noDocText);
		refreshableView = (RefreshableView) rootView
				.findViewById(R.id.refreshable_view);

		refreshableView.setOnRefreshListener(new PullToRefreshListener() {

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				refreshHandler.sendMessage(Message.obtain());
				refreshableView.finishRefreshing();
			}
		}, 3);

		noDocText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				refreshHandler.sendMessage(Message.obtain());
			}
		});

		mAdapter = new DocListAdapter(this.getActivity());
		docsListView.setAdapter(mAdapter);
		loadAdapterData();
		mRefreshReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				refreshHandler.sendMessage(Message.obtain());
			}
		};
		this.getActivity().registerReceiver(mRefreshReceiver,
				new IntentFilter(DocListFragment.ACTION_REFRESH_DOC));
		refreshHandler.sendMessage(Message.obtain());
		return rootView;
	}

	private Handler refreshHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgressDlg = ProgressDialog.show(rootView.getContext(), "加载中",
					"正在刷新文档列表...", true, false);

			getDocListFromServer();
		}
	};

	@Override
	public void onDestroyView() {
		this.getActivity().unregisterReceiver(mRefreshReceiver);
		super.onDestroyView();
	}

	/*
	 * 从服务器端获取可展示的应用列表
	 */
	private void getDocListFromServer() {

		if (null == NetworkDef.getAddrWebservice()) {
			isNetworkOK = false;
			mProgressDlg.dismiss();
			return;
		}

		// 创建连接
		String url = "/user/docs";
//		String url = "https://" + ip + "/api/v1/user/"
////				+ app.getCheckAccount().getCurrentAccount()
//				+ "/docs?access_token=" + account.getAccessToken();
//		Log.i(TAG, "getDocListFromServer=================" + url);
		jsonArrayRequest = new MyJsonArrayRequest(com.android.volley.Request.Method.GET, url, UpdateTokenRequest.TokenType.USER, new Response.Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray response) {
//				Log.i(TAG, "getDocListFromServer=================" + response.toString());

				List<String> ids = new ArrayList<String>();
				try {
					for (int i = 0; i < response.length(); i++) {
						JSONObject json = (JSONObject) response.get(i);
						String id = json.getString("id");
						ids.add(id);
						String name = json.getString("file_name");
						String url = json.getString("file_path");
						url = parseAddress(url);
						// cTime = json.getString("created_at");
						String uTime = json.getString("updated_at");

						// 判断文件是否有更新
						Cursor mCoursor = EmmClientApplication.mDb.getItemByInfo(
								EmmClientDb.CORPFILE_DATABASE_TABLE, new String[] {
										"sender", "time", "url" }, new String[] { id,
										uTime, url }, new String[] { "sender" });
						if (mCoursor == null || mCoursor.getCount() <= 0) {
							EmmClientApplication.mDb.updateOrInsertItemByInfo(
									EmmClientDb.CORPFILE_DATABASE_TABLE,
									new String[]{"sender"}, new String[]{id},
									new String[]{"filetag", "isnative", "url",
											"time", "sender"}, new String[]{name,
											"n", url, uTime, id});
						}
					}  //end for

					Cursor cursorId = EmmClientApplication.mDb.getAllItemsOfTable(
							EmmClientDb.CORPFILE_DATABASE_TABLE,
							new String[] { "sender" });
					if (cursorId != null) {
						for (cursorId.moveToFirst(); !cursorId.isAfterLast(); cursorId
								.moveToNext()) {
							String cid = cursorId.getString(cursorId
									.getColumnIndex("sender"));
							if (!ids.contains(cid)) {
								EmmClientApplication.mDb.deleteDbItemBycolumn(
										EmmClientDb.CORPFILE_DATABASE_TABLE, "sender",
										cid);
							}
						}
					}
					loadAdapterData();
					isNetworkOK = true;
//					Log.i(TAG, "getDocListFromServer=======00000000000==========");
				}catch (Exception e) {
//					Log.i(TAG, "getDocListFromServer=======111111111111==========");
					isNetworkOK=false;
					e.printStackTrace();
				}
//				Log.i(TAG, "getDocListFromServer=======22222==========");
				mProgressDlg.dismiss();
				showDocList();
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
//				Log.i(TAG, "getDocListFromServer=======333333333333==========");
				error.printStackTrace();
				if( error instanceof NetworkError) {
					isNetworkOK=false;
				}
				mProgressDlg.dismiss();
				//jsonArrayRequest.deliverError(error);
				showDocList();
			}
		});
		EmmClientApplication.mVolleyQueue.add(jsonArrayRequest);
	}

	private String parseAddress(String url) {
		String ans = "";
		String X[] = url.split("/");
		int size = X.length;
		for (int i = 0; i < size; i++) {
			try {
				ans += URLEncoder.encode(X[i], "UTF-8");
				if (i != size - 1)
					ans += "/";
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ans.replace("%3A", ":").replace("+","%20");
	}

	private void loadAdapterData() {
		// TODO 从数据库初始化数据
		// R.drawable.universal_user
		docsArrayList.clear();

		Cursor mCursor = EmmClientApplication.mDb.getAllItemsOfTable(
				EmmClientDb.CORPFILE_DATABASE_TABLE, null);

		if ((mCursor != null) && (mCursor.getCount() > 0)) {
			mCursor.moveToFirst();
			int fileTagIdx = mCursor.getColumnIndexOrThrow("filetag");
			int pathIdx = mCursor.getColumnIndexOrThrow("path");
			int timeIdx = mCursor.getColumnIndexOrThrow("time");
			int isNativeIdx = mCursor.getColumnIndexOrThrow("isnative");
			int urlIdx = mCursor.getColumnIndexOrThrow("url");
			int senderIdx = mCursor.getColumnIndexOrThrow("sender");
			do {
				String time = mCursor.getString(timeIdx);
				docsArrayList
						.add(new DocListItem(mCursor.getString(senderIdx),
								mCursor.getString(fileTagIdx), time, mCursor
										.getString(isNativeIdx), mCursor
										.getString(urlIdx), mCursor
										.getString(pathIdx)));
			} while (mCursor.moveToNext());
			mCursor.close();
		}
	}

	private void showDocList() {
		int length = docsArrayList.size();
		if (length <= 0) {
			noDocText.setVisibility(View.VISIBLE);
			if (isNetworkOK) {
				noDocText.setText("文档为空，点击刷新！");
			} else {
				noDocText.setText("文档加载失败，点击刷新！");
			}
			refreshableView.setVisibility(View.GONE);
			return;
		} else {
			noDocText.setVisibility(View.GONE);
			refreshableView.setVisibility(View.VISIBLE);
		}
		mAdapter.notifyDataSetChanged();
	}

	FileOpener fop = new FileOpener(DocListFragment.this.getActivity());

	private class DocListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;// 得到一个LayoutInfalter对象用来导入布局

		/** 构造函数 */
		public DocListAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return docsArrayList.size();// 返回数组的长度
		}

		@Override
		public Object getItem(int position) {
			return docsArrayList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			DocListHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_doc, null);
				holder = new DocListHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.image);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.subtitle = (TextView) convertView
						.findViewById(R.id.subtitle);
				holder.itemOperate = (TextView) convertView
						.findViewById(R.id.itemOperate);
				holder.itemDelete = (TextView) convertView
						.findViewById(R.id.itemDelete);
				holder.chevron = (ImageView) convertView
						.findViewById(R.id.chevron);

				convertView.setTag(holder);
			} else {
				holder = (DocListHolder) convertView.getTag();
			}

			DocListItem docItem = docsArrayList.get(position);

			holder.title.setText(docItem.fileName);
			holder.subtitle.setText(docItem.fileRecvTime);
			if (fop.checkEndsWithInStringArray(docItem.url, convertView
					.getResources().getStringArray(R.array.fileEndingImage))) {
				holder.image.setBackgroundResource(R.drawable.doc_img);
			} else if (fop.checkEndsWithInStringArray(docItem.url, convertView
					.getResources().getStringArray(R.array.fileEndingWebText))) {
				holder.image.setBackgroundResource(R.drawable.doc_html);
			} else if (fop.checkEndsWithInStringArray(docItem.url, convertView
					.getResources().getStringArray(R.array.fileEndingPackage))) {
				holder.image.setBackgroundResource(R.drawable.doc_rar);
			} else if (fop.checkEndsWithInStringArray(docItem.url, convertView
					.getResources().getStringArray(R.array.fileEndingAudio))) {
				holder.image.setBackgroundResource(R.drawable.doc_audio);
			} else if (fop.checkEndsWithInStringArray(docItem.url, convertView
					.getResources().getStringArray(R.array.fileEndingVideo))) {
				holder.image.setBackgroundResource(R.drawable.doc_video);
			} else if (fop.checkEndsWithInStringArray(docItem.url, convertView
					.getResources().getStringArray(R.array.fileEndingText))) {
				holder.image.setBackgroundResource(R.drawable.doc_txt);
			} else if (fop.checkEndsWithInStringArray(docItem.url, convertView
					.getResources().getStringArray(R.array.fileEndingPdf))) {
				holder.image.setBackgroundResource(R.drawable.doc_pdf);
			} else if (fop.checkEndsWithInStringArray(docItem.url, convertView
					.getResources().getStringArray(R.array.fileEndingWord))) {
				holder.image.setBackgroundResource(R.drawable.doc_word);
			} else if (fop.checkEndsWithInStringArray(docItem.url, convertView
					.getResources().getStringArray(R.array.fileEndingExcel))) {
				holder.image.setBackgroundResource(R.drawable.doc_excel);
			} else if (fop.checkEndsWithInStringArray(docItem.url, convertView
					.getResources().getStringArray(R.array.fileEndingPPT))) {
				holder.image.setBackgroundResource(R.drawable.doc_ppt);
			} else {
				holder.image.setBackgroundResource(R.drawable.doc_txt);
			}

			if (docItem.isNative.equals("n")) {
				holder.itemOperate.setText("下载");
				holder.itemDelete.setVisibility(View.GONE);
			} else if (docItem.isNative.equals("y")) {
				holder.itemOperate.setText("打开");
				holder.itemDelete.setVisibility(View.VISIBLE);
			}

			holder.itemOperate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View view) {
					// TODO Auto-generated method stub

					final DocListItem listItem = (DocListItem) docsArrayList
							.get(position);
					TextView stateText = (TextView) view
							.findViewById(R.id.itemOperate);
					String state = stateText.getText().toString();
					if (state.equals("下载")) {

						UpdateTokenRequest.update(UpdateTokenRequest.TokenType.USER, new Response.Listener<String>() {
							@Override
							public void onResponse(String response) {
                                Map<String, String> nameUrl = new HashMap<String, String>();
                                String url = "https://" + NetworkDef.getAddrWebservice() + "/api/v1/user/docs/" +
                                        listItem.fileId + "?uuid=" + EmmClientApplication.mPhoneInfo.getIMEI() +
                                        "&access_token="+ response;
                                nameUrl.put(listItem.fileName+"("+listItem.fileRecvTime+")", url);
                                DownLoadFileFromUrl.startDownloadFileList(
                                        rootView.getContext(), nameUrl);
							}
						}, new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								Toast.makeText(DocListFragment.this.getActivity(), "无法连接服务器", Toast.LENGTH_SHORT).show();
							}
						});
					} else if (state.equals("打开")) {
						DownLoadFileFromUrl.openCipherFile(
								DocListFragment.this.getActivity(),
								listItem.path, listItem.url);
					}
				}
			});

			holder.itemDelete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub
					final DocListItem listItem = (DocListItem) docsArrayList
							.get(position);
					AlertDialog.Builder builder = new AlertDialog.Builder(
							DocListFragment.this.getActivity());
					builder.setTitle("删除?");
					builder.setMessage("是否删除该文档?");
					builder.setNegativeButton("取消", null);
					builder.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									docsArrayList.remove(listItem);
									EmmClientApplication.mDb.deleteDbItemBycolumn(
											EmmClientDb.CORPFILE_DATABASE_TABLE,
											"sender", listItem.fileId);
									mAdapter.notifyDataSetChanged();
								}
							});
					builder.create().show();
				}
			});

			convertView.setOnClickListener(new OnClickListener() {
				Boolean flag = true;

				@Override
				public void onClick(View view) {
					TextView stv = (TextView) view.findViewById(R.id.subtitle);
					if (flag) {
						flag = false;
						stv.setEllipsize(null); // 展开
						stv.setSingleLine(flag);
					} else {
						flag = true;
						stv.setEllipsize(TextUtils.TruncateAt.END); // 收缩
						stv.setSingleLine(flag);
					}
				}
			});
			return convertView;
		}
	}

	public class DocListItem implements Comparable {
		public String fileId;
		public String fileName;
		public String fileRecvTime;
		public String isNative;
		public String url;
		public String path;

		public DocListItem(String fileId, String fileName, String fileRecvTime,
				String isNative, String url, String path) {
			this.fileId = fileId;
			this.fileName = fileName;
			this.fileRecvTime = fileRecvTime;
			this.isNative = isNative;
			this.url = url;
			this.path = path;
		}

		@Override
		public int compareTo(Object another) {
			// TODO Auto-generated method stub
			DocListItem tgt = (DocListItem) another;
			return this.fileRecvTime.compareTo(tgt.fileRecvTime);
		}
	}

	private class DocListHolder {
		public ImageView image;
		public TextView title;
		public TextView subtitle;
		public TextView itemOperate;
		public TextView itemDelete;
		public ImageView chevron;
	}
}