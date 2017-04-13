package cn.dacas.emmclient.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.business.BusinessListener;
import cn.dacas.emmclient.controller.ControllerListener;
import cn.dacas.emmclient.controller.McmController;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.model.McmMessageModel;
import cn.dacas.emmclient.ui.activity.mainframe.MdmMsgListActivity;
import cn.dacas.emmclient.ui.activity.mainframe.MsgDetailActivity;
import cn.dacas.emmclient.ui.base.CommonAdapter;
import cn.dacas.emmclient.ui.base.CommonViewHolder;
import cn.dacas.emmclient.ui.qdlayout.RefreshableView;
import cn.dacas.emmclient.ui.qdlayout.SearchView;
import cn.dacas.emmclient.util.BroadCastDef;
import cn.dacas.emmclient.util.DateTimeUtil;
import cn.dacas.emmclient.util.GlobalConsts;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.QDLog;
import de.greenrobot.event.EventBus;

/**
 * A placeholder fragment containing a simple view.
 */
public class MsgListFragment extends BaseFragment implements ControllerListener,SearchView.SearchViewListener {
	private static final String TAG = "MsgListFragment";
	// 下载完成修改状态
	public static final String ACTION_REFRESH_DOC = "cn.dacas.emmclient.mam.REFRESH_MSG";

	//////////////////////about search///////////
	/**
	 * 搜索view
	 */
	private SearchView searchView;

	/**
	 * 自动补全列表adapter
	 */
	private ArrayAdapter<String> autoCompleteAdapter;

	/**
	 * 搜索结果列表adapter
	 */
	private SearchAdapter mSearchAdapter;

	//////////////////////end search///////////


	private RefreshableView refreshableView;
	private ListView mListView = null;
	private TextView noTextView = null;

	protected LinearLayout mButtonLayout;
	protected Button mLeftButton;
	protected Button 	  mRightButton;

	private View rootView;

	private ProgressDialog mProgressDlg;

	//全局的数据，搜索时，从这个数据中收索。
	private List<McmMessageModel> allList = new ArrayList<McmMessageModel>();

	//doclist,与view相关连，通过设置adapter
	private ArrayList<McmMessageModel> mResultArrayList = new ArrayList<McmMessageModel>();

	McmController mMcmController = null;

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	public final static String REFRESH_MAIN_ACTIVITY = "refresh_main_activity";

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static Fragment newInstance(int sectionNumber) {
		MsgListFragment curFragment = new MsgListFragment();
		Bundle args = new Bundle();

		// 这个number还有必要么？ lizhongyi
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		curFragment.setArguments(args);
		return curFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity();
		mActivity = getActivity();

		mMcmController = new McmController(mContext,this);

		registerMReceiver();
		getMsgListFromServer();


//		registerBroadcastReceiver();
//		EventBus.getDefault().register(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_msg_list, container, false);

		mListView = (ListView) rootView.findViewById(R.id.pushAppsListView);
		QDLog.i(TAG, "list init======================================");

		searchView = (SearchView)rootView.findViewById(R.id.main_search_layout);
		//设置监听
		searchView.setSearchViewListener(this);


		noTextView = (TextView) rootView.findViewById(R.id.noAppText);
		//noTextView.setVisibility(View.GONE);
		refreshableView = (RefreshableView) rootView.findViewById(R.id.refreshable_view);

		refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {

			@Override
			public void onRefresh() {
				refreshHandler.sendMessage(Message.obtain());
				refreshableView.finishRefreshing();
			}
		}, 0x1003);

		noTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				QDLog.i(TAG, "noTextView.setOnClickListener===========");

				refreshableView.setVisibility(View.GONE);
				//noTextView.setVisibility(View.VISIBLE);
				noTextView.setText("加载中...");

				QDLog.e("MsgListFragment", "refreshableView.setVisibility(View.VISIBLE)");

				refreshHandler.sendMessage(Message.obtain());
			}
		});

		((MdmMsgListActivity)mActivity).setHeaderText(mContext.getString(R.string.edit));


		//底部button事件
		mButtonLayout = (LinearLayout) rootView.findViewById(R.id.button_layout);

		mButtonLayout.setVisibility(View.GONE);
		mLeftButton = (Button) rootView.findViewById(R.id.left_btn);
		mRightButton = (Button) rootView.findViewById(R.id.right_btn);

		mRightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean findCheck = false;
				for(int i = 0; i< mResultArrayList.size();i++) {
					if (mResultArrayList.get(i).isCheck) {
						findCheck = true;
						break;
					}
				}
				if (findCheck) {
					showDialog();
				}else {
					Toast.makeText(mContext,"请选择要删除的项目!",Toast.LENGTH_SHORT).show();
				}
			}
		});

		mLeftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String btnText = mLeftButton.getText().toString();
				if (btnText.equals(getString(R.string.msg_choice_all))) {
					mLeftButton.setText(R.string.msg_not_choice_all);
					setCheckAll(true);
				}else {
					mLeftButton.setText(R.string.msg_choice_all);
					setCheckAll(false);
				}

				mSearchAdapter.notifyDataSetChanged();
			}
		});


		//非常重要，设置adapter
		mSearchAdapter = new SearchAdapter(mContext, mResultArrayList, R.layout.listitem_message);

		mListView.setAdapter(mSearchAdapter);

		refreshHandler.sendMessage(Message.obtain());
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		QDLog.i(TAG, "onResume=========================");
		if (mResultArrayList!=null && mResultArrayList.size()>=0) {
			mSearchAdapter.notifyDataSetChanged();
		}
//		else
//		if (mResultArrayList.size()==0) {
//			mButtonLayout.setVisibility(View.GONE);
//		}else {
//			mButtonLayout.setVisibility(View.VISIBLE);
//		}
//		refreshHandler.sendMessage(Message.obtain());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

	}

	@Override
	public void onDestroy() {
//		unregisterBoradcastReceiver();
//		EventBus.getDefault().unregister(this);
		super.onDestroy();
		this.getActivity().unregisterReceiver(mBroadcastReciver);
	}

	private Handler refreshHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
//			mProgressDlg = ProgressDialog.show(rootView.getContext(), "加载中",
//					"正在刷新消息列表...", true, false);
			//getMsgListFromServer();
			getMsgListFromDB();
			showMessageList();
		}
	};

	/**
	 * 接收OP_MSG消息（来自mdmserver）
	 */
	private void registerMReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BroadCastDef.OP_MSG);
		this.getActivity().registerReceiver(mBroadcastReciver, intentFilter);
	}

	private BroadcastReceiver mBroadcastReciver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BroadCastDef.OP_MSG.equals(intent.getAction())) {
				QDLog.i(TAG,"registerMReceiver=====================");
				getMsgListFromDB();
				showMessageList();
				//MsgListFragment.this.getLoaderManager().restartLoader(0, null, MsgListFragment.this);
			}
		}
	};

	//////需要接口调用的函数////

	private void loadAdapterData() {
		mResultArrayList.clear();
		allList.clear();

		Cursor mCursor = EmmClientApplication.mDatabaseEngine.getAllMessageData();
		if ((mCursor != null) && (mCursor.getCount() > 0)) {
			mCursor.moveToFirst();
			int contentIdx = mCursor.getColumnIndexOrThrow("msg");
			int timeIdx = mCursor.getColumnIndexOrThrow("time");
			int idIdx = mCursor.getColumnIndexOrThrow("_id");
			int subjectIdx = mCursor.getColumnIndexOrThrow("title");
			int readedIdx = mCursor.getColumnIndexOrThrow("readed");
			do {
				//需要格式化time
				String time = mCursor.getString(timeIdx);
				mResultArrayList
						.add(new McmMessageModel(mCursor.getInt(idIdx),
								mCursor.getString(subjectIdx),
								mCursor.getString(contentIdx),
								time,
								mCursor.getInt(readedIdx),
								false
						));
			} while (mCursor.moveToNext());
			Collections.reverse(mResultArrayList);
			allList.addAll(mResultArrayList);
			mCursor.close();
		}
	}

	/*** 获取搜索结果data和adapter*/
	private void getResultData(String text) {
		if (allList.size() <= 0) {
			return;
		}
		if (TextUtils.isEmpty(text)) {
			mResultArrayList.clear();
			mResultArrayList.addAll(allList);
		}else {
			mResultArrayList.clear();
			for (int i = 0; i< allList.size(); i++) {
				if (allList.get(i).content.toLowerCase().contains(text.trim().toLowerCase()) ||
						allList.get(i).title.toLowerCase().contains(text.trim().toLowerCase())
						) {
					mResultArrayList.add(allList.get(i));
				}else {
					//do nothing
				}
			}
		}
		mSearchAdapter.notifyDataSetChanged();

	}

	/**
	 * 当搜索框 文本改变时 触发的回调 ,更新自动补全数据
	 * @param text
	 */
	@Override
	public void onRefreshAutoComplete(String text) {

		QDLog.i(TAG,"onRefreshAutoComplete===========" + text);
		//更新数据
//        getAutoCompleteData(text);
	}
	/**
	 * 点击搜索键时edit text触发的回调
	 *
	 * @param text
	 */
	@Override
	public void onSearch(String text) {
		QDLog.i(TAG, "onSearch===========" + text);
		getResultData(text);
	}

	public class SearchAdapter extends CommonAdapter<McmMessageModel> {

		public SearchAdapter(Context context, List<McmMessageModel> data, int layoutId) {
			super(context, data, layoutId);
		}

		//需要将model中的check等ui的值也写上。
		@Override
		public void convert(final CommonViewHolder holder, final int position) {
			final McmMessageModel model = mDatas.get(position);

			holder.setText(R.id.major_txt,model.title); //title
			holder.setText(R.id.minor_txt, model.content); //content

//			String resTime = DateTimeUtil.Time2Date(model.created_at);
			final String resTime = DateTimeUtil.formatDateTime(model.created_at);
			if (resTime == null) {
				holder.setText(R.id.textview_time, ""); //time
			}else {
				holder.setText(R.id.textview_time, resTime); //time
			}

			holder.setChecked(R.id.checkBox1, model.isCheck); // check box
			if (model.readed == 0) { //未读，显示左边的红点
				holder.setVisible(R.id.major_image,true);
			}else{
				holder.setVisible(R.id.major_image,false);
			}

			holder.setVisible(R.id.checkBox1, model.needShowCheck);

//			if (((MdmMsgListActivity)mActivity).getRightText().equals(mContext.getString(R.string.edit)) ) {
//				holder.setVisible(R.id.checkBox1, false);
//			}else {
//				holder.setVisible(R.id.checkBox1, true);
//			}


			final CheckBox checkBox = holder.getView(R.id.checkBox1);
			checkBox.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					model.isCheck = !model.isCheck;
					checkBox.setChecked(model.isCheck);

				}
			});


			LinearLayout mainLinearLayout = holder.getView(R.id.msg_main_layout);
//			LinearLayout mainLinearLayout = holder.getView(holder.getLayoutId());

//			View mainLinearLayout = holder.getConvertView();

//			LinearLayout mainLinearLayout = (LinearLayout)holder.getLayout();

			mainLinearLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (model.readed == 0) {
						EmmClientApplication.mDatabaseEngine.updateMessageData(model.title, model.content, model.created_at, "" + 1);
						for (McmMessageModel m: mResultArrayList) {
							if (m.id == model.id) {
								m.readed = 1;
							}
						}
					}

					Intent intent = new Intent(mContext,MsgDetailActivity.class);

					Bundle bundle = new Bundle();
					bundle.putString(GlobalConsts.Msg_Subject,model.title);
					bundle.putString(GlobalConsts.Msg_Time, resTime);
					bundle.putString(GlobalConsts.Msg_Content, model.content);
					intent.putExtras(bundle);


					startActivity(intent);

				}
			});
		}
	}

	/////////////////
	//for test
	private void testData() {
//		mResultArrayList.clear();
//		allList.clear();
//
//		for (int i = 0; i< 16; i++ ) {
//			String s = "" +i;
//			McmMessageModel m = new McmMessageModel(i,"主题123" + s,"内容456"+ s,"时间789" + s,0);
//			m.isCheck = false;
//			mResultArrayList.add(m);
//			allList.add(m);
//		}
//		refreshableView.finishRefreshing();
	}

	/**
	 * 从数据库获得消息列表
	 * @return 消息列表
     */
	private List<McmMessageModel> getMsgListFromDB() {
//		if (mProgressDlg!=null) {
//			mProgressDlg.dismiss();
//		}

//		testData();
		loadAdapterData();
		return null;

		/** this part doesn't work!
		List<McmMessageModel> modelList = new ArrayList<McmMessageModel>();
		Cursor mCursor = EmmClientApplication.mDatabaseEngine.getAllMessageData();
		if ((mCursor != null) && (mCursor.getCount() > 0)) {
			mCursor.moveToFirst();
			int titleIdx = mCursor.getColumnIndexOrThrow("title");
			int contentIdx = mCursor.getColumnIndexOrThrow("content");
			int timeIdx = mCursor.getColumnIndexOrThrow("created_at");

			do {
				McmMessageModel model = new McmMessageModel();

				model.title = (mCursor.getString(titleIdx));
				model.content = (mCursor.getString(contentIdx));
				model.created_at = (mCursor.getString(timeIdx));


				modelList.add(model);
			} while (mCursor.moveToNext());
			mCursor.close();
		}
		return modelList;*/
	}

	/**
	 * 从服务器获取消息列表
	 */
	private void getMsgListFromServer() {
		final int maxId = PrefUtils.getMsgMaxId();

		if (mMcmController == null) {
			//////初始化Controller
			mMcmController = new McmController(mContext,this);
		}
		QDLog.i(TAG, "getMsgListFromServer==============" + maxId);
		mMcmController.FetchMessageList(maxId);
	}

	@Override
	public void OnNotify(BusinessListener.BusinessResultCode resCode, BusinessListener.BusinessType type, Object data1, Object data2) {

//        mProgressDlg.dismiss();
        refreshableView.finishRefreshing();
		switch (resCode) {
			//请求OK
			case  ResultCode_Sucess:
				switch (type) {
					case BusinessType_MessageList:
						if (data1 != null) {
							List<McmMessageModel> currentList = (List<McmMessageModel>) data1;

							//考虑在线程中执行，因为全是数据库操作lizy
							UpdateMessageList(currentList);
							return;
						}
						break;
					default:
						break;
				}
				break;
			default:
				//response错误
//                showToastMsg(resCode,type);
//                showDocList(false);
		}
	}

	//////需要接口调用的函数////

	private void showMessageList() {
		QDLog.i(TAG,"mResultArrayList==============" + mResultArrayList.size());
		int length = mResultArrayList.size();
		if (length <= 0) {
			noTextView.setVisibility(View.VISIBLE);
			try {
				Thread.sleep(300);
			}catch (Exception e) {
			}
			noTextView.setText("消息为空，点击刷新！");
			refreshableView.setVisibility(View.GONE);
			mButtonLayout.setVisibility(View.GONE);
//			((MdmMsgListActivity)mActivity).setHeaderText("");

			return;
		} else {
			((MdmMsgListActivity)mActivity).setHeaderText(mContext.getString(R.string.edit));
			noTextView.setVisibility(View.GONE);
			refreshableView.setVisibility(View.VISIBLE);

		}
		mSearchAdapter.notifyDataSetChanged();
	}


	//from MDMService
	private void UpdateMessageList(List<McmMessageModel> modelList) {

		if (modelList != null && modelList.size() <= 0) {
			return;
		}

		for (int i = modelList.size()-1; i >=0;i--) {

			McmMessageModel m = modelList.get(i);
			EmmClientApplication.mDatabaseEngine.updateMessageData(m.title,m.content, m.created_at,""+m.readed);
			//save data
			saveUnreadCount(m.id);

			//send action
			notifyDataChange(GlobalConsts.NEW_MESSAGE);

			//send msg 没有实际用处
			sendMsg(m.content);
		}

//        send OP_MSG action
		notifyDataChange(BroadCastDef.OP_MSG);

		//update list
		mResultArrayList.clear();
		mResultArrayList.addAll(modelList);
	}

	// 存储未读条数
	private void saveUnreadCount(int modelId) {
		final int maxId =PrefUtils.getMsgMaxId();
		int mId = Math.max(modelId, maxId);
		int unreadCount = PrefUtils.getMsgUnReadCount();
		PrefUtils.putMsgMaxId(mId);
		PrefUtils.putMsgUnReadCount(unreadCount+1);
	}



	//send handler msg
	private void sendMsg(String msg) {
//		Handler handler = listener.getHandler();
//		if (handler != null) {
//			//sendBroadcast
//			Intent intent = new Intent();
//			intent.setAction(GlobalConsts.NEW_MESSAGE);
//			context.sendBroadcast(intent);
//
//			//send message
//			Bundle bundle = new Bundle();
//			bundle.putCharSequence("msg", msg);
//			Message handlerMsg = Message.obtain();
//			handlerMsg.arg1 = PUSH_MSG;
//			handlerMsg.setData(bundle);
//			handler.sendMessageAtFrontOfQueue(handlerMsg);
//		}
	}

	private void notifyDataChange(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		this.getActivity().sendBroadcast(intent);
	}


	private void setCheckAll(boolean isCheck) {
		for (int i = 0; i<mResultArrayList.size();i++) {
			McmMessageModel m = mResultArrayList.get(i);
			m.isCheck = isCheck;
		}
	}

	private void deleteItemFromDb() {
		int size = mResultArrayList.size();
		for (int i = size-1; i>=0;i--) {
			McmMessageModel m = mResultArrayList.get(i);
			if (m.isCheck) {
				EmmClientApplication.mDatabaseEngine.deleteDbItemByColumns(m.content,m.created_at);
			}
		}
	}
	private void deleteCheckItem() {
		deleteItemFromDb();
		int size = mResultArrayList.size();
		for (int i = size-1; i>=0;i--) {
			McmMessageModel m = mResultArrayList.get(i);
			if (m.isCheck) {
				mResultArrayList.remove(i);
			}
		}


	}


	////底部菜单的实现

	private Button mPromptTitleButton;
	private Button mConfirmButton;
	private Button mCancelButton;

	private Dialog mPopupMenuDialog;

	private void showDialog() {
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

		mPromptTitleButton.setText(R.string.msg_delete_confirm_prompt);
		mConfirmButton.setText(R.string.msg_delete_confirm);
		mCancelButton.setText(R.string.msg_delete_cancel);

		mPromptTitleButton.setEnabled(false);
		mPromptTitleButton.setTextColor(mContext.getResources().getColor(R.color.msg_prompt_title_color));

		mConfirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteCheckItem();
				mSearchAdapter.notifyDataSetChanged();
				if (mResultArrayList.size() <= 0) {
					setBottomVisible(false);
					((MdmMsgListActivity) mActivity).setHeaderText("");
				}

//				((MdmMsgListActivity) mActivity).updateHeader();


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

	public void setBottomVisible(boolean visible) {
		if (visible) {
			mButtonLayout.setVisibility(View.VISIBLE);
		}else {
			mButtonLayout.setVisibility(View.GONE);
		}
	}

	public ArrayList<McmMessageModel> getResultList() {
		return mResultArrayList;
	}

	public void notifyDataChanged() {
		mSearchAdapter.notifyDataSetChanged();
	}

	public void showAllItemCheckedView(boolean show) {
		for (int i = 0;i<mResultArrayList.size();i++) {
			mResultArrayList.get(i).needShowCheck = show;
		}
	}




}