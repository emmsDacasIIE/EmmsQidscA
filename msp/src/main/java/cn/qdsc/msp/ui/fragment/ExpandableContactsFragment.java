package cn.qdsc.msp.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import cn.qdsc.msp.R;
import cn.qdsc.msp.business.BusinessListener;
import cn.qdsc.msp.controller.ControllerListener;
import cn.qdsc.msp.controller.McmController;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.manager.AddressManager;
import cn.qdsc.msp.manager.UrlManager;
import cn.qdsc.msp.model.CheckAccount;
import cn.qdsc.msp.model.McmContactsModel;
import cn.qdsc.msp.ui.contacts.CharacterParser;
import cn.qdsc.msp.ui.contacts.ClearEditText;
import cn.qdsc.msp.ui.contacts.ContactDetailActivity;
import cn.qdsc.msp.ui.contacts.ContactDetailListener;
import cn.qdsc.msp.ui.contacts.ExpandableAdapter;
import cn.qdsc.msp.ui.contacts.PinyinComparator;
import cn.qdsc.msp.ui.contacts.SideBar;
import cn.qdsc.msp.ui.contacts.SortModel;
import cn.qdsc.msp.util.PhoneInfoExtractor;
import cn.qdsc.msp.util.PrefUtils;
import cn.qdsc.msp.util.QDLog;
import cn.qdsc.msp.webservice.download.MyHostnameVerifier;
import cn.qdsc.msp.webservice.download.MyX509TrustManager;


public class ExpandableContactsFragment extends BaseFragment  implements ControllerListener,ContactDetailListener {
	private static final String TAG = "ExpandableContactsFragment";

	private SideBar sideBar;
	private TextView dialog;
	private ExpandableAdapter adapter;
	private ClearEditText mClearEditText;
	private TextView noContacTextView;
	private CharacterParser characterParser;
	private List<SortModel> SourceDateList = new ArrayList<SortModel>();

	private PinyinComparator pinyinComparator;

	private View rootView = null;
	private ListView msgContactsListView = null;

	private Dialog downloadDialog;

	private List<String> mDownloadUrl = new ArrayList<String>();

	McmController mMcmController;


	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	public static Fragment newInstance(int sectionNumber) {
		ExpandableContactsFragment curFragment = new ExpandableContactsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		curFragment.setArguments(args);
		return curFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// create progress
		super.onCreate(savedInstanceState);

		mContext = getActivity();
		mMcmController = new McmController(mContext,this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_contacts,
				container, false);

		//下面这个顺序不能变化
		initViews();

		//for test
//		EmmClientApplication.mDatabaseEngine.deleteContactAllData();
//		setSourceDateListFromDb();
		initAdapter();

		//ui更新
//		updateUI();

		//网络请求
		getContactListFromServer();


//		sendMessage(0);
		return rootView;
	}

	public Handler getHandle() {
		return refreshHandler;
	}

	public View getRootView() {
		return rootView;
	}

	public void setDialog(Dialog dlg) {
		downloadDialog = dlg;
	}

	private void sendMessage(int arg1) {
		Message msg  = new Message();
		msg.arg1 = arg1;
		refreshHandler.sendMessage(msg);
	}

	private void setSourceDateListFromDb() {
		if (SourceDateList!=null && SourceDateList.size()>0) {
			SourceDateList.clear();
		}
		SourceDateList = getDataFromDb();
		Collections.sort(SourceDateList, pinyinComparator);
	}

	private void initViews() {
		characterParser = CharacterParser.getInstance();
		noContacTextView = (TextView)rootView.findViewById(R.id.noText);

		noContacTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				noContacTextView.setText("加载中...");
				getContactListFromServer();

			}
		});


		pinyinComparator = new PinyinComparator();
		sideBar = (SideBar) rootView.findViewById(R.id.sidrbar);
		dialog = (TextView) rootView.findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

			public void onTouchingLetterChanged(String s) {
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					msgContactsListView.setSelection(position);
				}
			}
		});

		msgContactsListView = (ListView) rootView
				.findViewById(R.id.msgContactsListView);

		msgContactsListView.requestFocus();
		msgContactsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				Toast.makeText(
						view.getContext(),
						((SortModel) adapter.getItem(position))
								.getContactName(), Toast.LENGTH_SHORT).show();
			}
		});



//		if(SourceDateList.size() == 0){
//			msgContactsListView.setVisibility(View.GONE);
//			sideBar.setVisibility(View.GONE);
//			noContacTextView.setVisibility(View.VISIBLE);
//		}
//		else {
//			msgContactsListView.setVisibility(View.VISIBLE);
//			sideBar.setVisibility(View.VISIBLE);
//			noContacTextView.setVisibility(View.GONE);
//		}

		mClearEditText = (ClearEditText) rootView
				.findViewById(R.id.filter_edit);

		mClearEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});





	}

	private void initAdapter() {
		adapter = new ExpandableAdapter(rootView.getContext(), SourceDateList,this);
		msgContactsListView.setAdapter(adapter);
	}

	public void getContactListFromServer() {

		if (null == AddressManager.getAddrWebservice()) {
			return;
		}

		//列表请求
		if (mMcmController != null) {
			mMcmController.FetchContactsList();
		}

//		sendMessage(1);


	}
	
	private void updateUI(){
		if(downloadDialog!=null && downloadDialog.isShowing()) {
			downloadDialog.dismiss();
		}


		if(SourceDateList.size() == 0){
			msgContactsListView.setVisibility(View.GONE);
			sideBar.setVisibility(View.GONE);
			noContacTextView.setVisibility(View.VISIBLE);
			SystemClock.sleep(300);
			noContacTextView.setText("通讯录为空，点击刷新！");
		}
		else {
			msgContactsListView.setVisibility(View.VISIBLE);
			sideBar.setVisibility(View.VISIBLE);
			noContacTextView.setVisibility(View.GONE);
		}
		adapter.updateListView(SourceDateList);
	}

	class MyHandler extends Handler {
		WeakReference<ExpandableContactsFragment> mFragment;

		MyHandler(ExpandableContactsFragment fragment) {
			mFragment = new WeakReference<ExpandableContactsFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
//			if (msg.arg1 == 0) {
//				final ExpandableContactsFragment theFragment = mFragment.get();
//				theFragment.getContactListFromServer();
//			}
//			if(msg.arg1 == 1){
//				updateUI();
//			}

			if(msg.arg1 == 2){
				//download thread
				if (mDownloadUrl!=null && mDownloadUrl.size()> 0 ) {
					String url = mDownloadUrl.get(0);
					downloadContactThread(url,0);
				}else {
					//全部数据下载完成，更新UI
					QDLog.i(TAG, "MyHandler============ download finished!");
					PrefUtils.addSecurityRecord("加密通讯录");
					setSourceDateListFromDb();
					updateUI();
					adapter.updateListView(SourceDateList);
//					adapter.clear();
//					adapter.addAll(SourceDateList);
//					adapter.notifyDataSetChanged();

				}

			}
		}
	}

	MyHandler refreshHandler = new MyHandler(this);

	private List<SortModel> getDataFromDb() {
		List<SortModel> mSortList = new ArrayList<SortModel>();
		Cursor mCursor = EmmClientApplication.mDatabaseEngine.getContactAllData();
		if ((mCursor != null) && (mCursor.getCount() > 0)) {
			mCursor.moveToFirst();
			int nameIdx = mCursor.getColumnIndexOrThrow("name");
			int telephoneIdx = mCursor.getColumnIndexOrThrow("telephone");
			int cellphone_1Idx = mCursor.getColumnIndexOrThrow("cellphone_1");
			int cellphone_2Idx = mCursor.getColumnIndexOrThrow("cellphone_2");
			int email_1Idx = mCursor.getColumnIndexOrThrow("email_1");
			int email_2Idx = mCursor.getColumnIndexOrThrow("email_2");
			int companyIdx = mCursor.getColumnIndexOrThrow("company");
			int addressIdx = mCursor.getColumnIndexOrThrow("address");
			do {
				SortModel sortModel = new SortModel();
				sortModel.setContactName(mCursor.getString(nameIdx));	
				sortModel.setTelephone(mCursor.getString(telephoneIdx));
				sortModel.setCellphone_1(mCursor.getString(cellphone_1Idx));
				sortModel.setCellphone_2(mCursor.getString(cellphone_2Idx));
				sortModel.setEmail_1(mCursor.getString(email_1Idx));
				sortModel.setEmail_2(mCursor.getString(email_2Idx));
				sortModel.setContactCompany(mCursor.getString(companyIdx));
				sortModel.setContactAddress(mCursor.getString(addressIdx));
				mSortList.add(sortModel);
			} while (mCursor.moveToNext());
			mCursor.close();
		}

		for (SortModel m : mSortList) {
			String pinyin = characterParser.getSelling(m.getContactName());
			String sortString = pinyin.substring(0, 1).toUpperCase();
			if (sortString.matches("[A-Z]")) {
				m.setSortLetters(sortString.toUpperCase());
			} else {
				m.setSortLetters("#");
			}
		}
		return mSortList;
	}

	private void filterData(String filterStr) {
		List<SortModel> filterDateList = new ArrayList<SortModel>();

		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = SourceDateList;
		} else {
			filterDateList.clear();
			for (SortModel sortModel : SourceDateList) {
				String name = sortModel.getContactName();
				if (name.indexOf(filterStr.toString()) != -1
						|| characterParser.getSelling(name).startsWith(
								filterStr.toString())) {
					filterDateList.add(sortModel);
				}
			}
		}

		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}

	private void downloadAndSave(String urlP) {
		QDLog.i(TAG,"downloadAndSave============urlp==" + urlP);
		URL url = null;
		
		try {
			url = new URL(urlP);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String mSavePath = Environment.getExternalStorageDirectory()
				+ "/download";

		// 创建连接
		HttpURLConnection conn = null;
		InputStream is = null;



		// Create a trust manager that does not validate certificate chains

		try {

			HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
			SSLContext sc = null;
			try {
				sc = SSLContext.getInstance("TLS");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			try {
//				sc.init(null, trustAllCerts, new SecureRandom());
				sc.init(null, new TrustManager[]{new MyX509TrustManager()}, new SecureRandom());
			} catch (KeyManagementException e) {
				e.printStackTrace();
			}
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Content-Type",
					"application/octet-stream");
			conn.connect();

			is = conn.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (is == null) {
			return;
		}

		File file = new File(mSavePath);
		// 判断文件目录是否存在
		if (!file.exists())
			file.mkdir();
		File contactFile = new File(mSavePath, "tmp");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(contactFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		// 缓存

		// 写入到文件中
		try {
			byte buf[] = new byte[8192];
			int numread = 0;
			do {
				numread = is.read(buf);
				if (numread <= 0)
					break;
				fos.write(buf, 0, numread);
			} while (true);// 点击取消就停止下载.
			fos.close();
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 读文件
		try {
			byte buf[] = new byte[1024 * 1024];
			FileInputStream fin = new FileInputStream(contactFile);
			int numread = 0;
			String str = "";
			do {
				numread = fin.read(buf);
				if (numread <= 0)
					break;
				str += new String(buf, 0, numread, "UTF-8");
				int left = str.indexOf('{');
				int right = str.lastIndexOf('}');
				JSONArray array;
				String name, company, address,cellphone_1,cellphone_2,email_1,email_2,code,telephone;
				try {
					array = new JSONArray("[" + str.substring(left, right + 1)
							+ "]");
					for (int i = 0; i < array.length(); i++) {
						JSONObject peopleJson = array.getJSONObject(i);
						code = peopleJson.getString("code");
						telephone = peopleJson.has("telephone")?peopleJson.getString("telephone"):"";
						name = peopleJson.has("name")?peopleJson.getString("name"):"";
						cellphone_1 = peopleJson.has("cellphone_1")?peopleJson.getString("cellphone_1"):"";
						cellphone_2 = peopleJson.has("cellphone_2")?peopleJson.getString("cellphone_2"):"";
						email_1 = peopleJson.has("email_1")?peopleJson.getString("email_1"):"";
						email_2 = peopleJson.has("email_2")?peopleJson.getString("email_2"):"";
						company = peopleJson.has("company")? peopleJson.getString("company") : "";
						address = peopleJson.has("address")?peopleJson.getString("address"): "";

						String[] fields = new String[] {  "_id","name","telephone","cellphone_1","cellphone_2","email_1","email_2","company","address"};
						String[] values = new String[] {  code,name,telephone, cellphone_1,cellphone_2,email_1,email_2,company, address};
						Cursor mCoursor = EmmClientApplication.mDatabaseEngine.getContactItemData(fields, values);

						if (mCoursor == null || mCoursor.getCount() <= 0) {
							EmmClientApplication.mDatabaseEngine.addContactItemData(fields,values);
						} else
							mCoursor.close();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				str = str.substring(right + 1);
			} while (true);
			fin.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private void downloadContactThread(final String url, final int index) {

		//考虑用Download通用模块去下载lizhongyi1214
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				QDLog.i(TAG, "downloadContactThread============url==" + url);
				QDLog.i(TAG, "downloadContactThread============index==" + index);
				downloadAndSave(url);
				mDownloadUrl.remove(index);
				sendMessage(2);
			}
		});
		thread.start();

	}



	@Override
	public void OnNotify(BusinessListener.BusinessResultCode resCode, BusinessListener.BusinessType type, Object data1, Object data2) {
//		mProgressDlg.dismiss();
//		refreshableView.finishRefreshing();

		QDLog.i(TAG, "OnNotify=================" + resCode);
		mDownloadUrl.clear();

		//以前，要进行删除，个人认为不能删除lizhongyi
//		EmmClientApplication.mDatabaseEngine.deleteContactAllData();

		switch (resCode) {
			//请求OK
			case  ResultCode_Sucess:
				switch (type) {
					case BusinessType_ContactsList:
						if (data1 != null) {
							List<McmContactsModel> currentList = (List<McmContactsModel>) data1;

							if (currentList == null || currentList.size() <=0 ) {
								SourceDateList.clear();
								updateUI();

								return;
							}else {
								startDownloadThread(currentList);
								return;
							}

						}
						break;
					default:
						break;
				}
				break;
			default:
				//response错误
				setSourceDateListFromDb();

                updateUI();

		}
	}

	/**
	 * 只在onNotify中调用
	 */
	private void startDownloadThread(List<McmContactsModel> currentList) {

		for (int i =0; i< currentList.size();i++) {
			McmContactsModel m = currentList.get(i);
			String ip = AddressManager.getAddrWebservice();
			//String url = "https://" + ip + "/api/v1/user/dirs/" + m.id + "?uuid=" + PhoneInfoExtractor.getIMEI(mContext);
//			CheckAccount account = CheckAccount.getCheckAccountInstance(mContext.getApplicationContext());
//			final String urlP = NetworkDef.parseAddress(url) + "&access_token="+ account.getAccessToken();
			String url = UrlManager.getWebServiceUrl()+"/user/dirs/" + m.id; // + "?access_token=" + PhoneInfoExtractor.getIMEI(mContext);


			CheckAccount account = CheckAccount.getCheckAccountInstance(mContext.getApplicationContext());

			final String urlP = url + "?access_token="+ PrefUtils.getUserToken().getAccessToken() + "&uuid=" + PhoneInfoExtractor.getIMEI(mContext);

			mDownloadUrl.add(urlP);
		}
//		SourceDateList.clear();
//		SourceDateList = getDataFromDb();
//		Collections.sort(SourceDateList, pinyinComparator);
		if (mDownloadUrl!= null && mDownloadUrl.size() > 0) {
			//发送下载的handler
			sendMessage(2);
		}


	}

	//////////////需要实现的接口
	@Override
	public void onDeliveData(SortModel sortModel) {
		//start information
		Intent intent = new Intent(mContext, ContactDetailActivity.class);

		Bundle bundle=new Bundle();
		bundle.putSerializable("sortModel", sortModel);
		intent.putExtras(bundle);
		startActivity(intent);
	}

}
