package cn.dacas.emmclient.mcm;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.contacts.CharacterParser;
import cn.dacas.emmclient.contacts.ClearEditText;
import cn.dacas.emmclient.contacts.ExpandableAdapter;
import cn.dacas.emmclient.contacts.PinyinComparator;
import cn.dacas.emmclient.contacts.SideBar;
import cn.dacas.emmclient.contacts.SideBar.OnTouchingLetterChangedListener;
import cn.dacas.emmclient.contacts.SortModel;
import cn.dacas.emmclient.db.EmmClientDb;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.security.ssl.IgnoreCertTrustManager;
import cn.dacas.emmclient.util.MyJsonArrayRequest;
import cn.dacas.emmclient.util.NetworkDef;
import cn.dacas.emmclient.util.UpdateTokenRequest;

public class ExpandableContactsFragment extends Fragment {
	private static final String TAG = "ExpandableContactsFragment";

	private Context context;

	private SideBar sideBar;
	private TextView dialog;
	private ExpandableAdapter adapter;
	private ClearEditText mClearEditText;
	private TextView noContacTextView;
	private CharacterParser characterParser;
	private List<SortModel> SourceDateList;

	private PinyinComparator pinyinComparator;

	private View rootView = null;
	private ListView msgContactsListView = null;

	private Dialog downloadDialog;

	private MyJsonArrayRequest jsonArrayRequest;

	private List<String> mDownloadUrl = new ArrayList<String>();

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	public boolean isLoading=false;

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
		this.context = getActivity().getApplicationContext();
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_msg_management_contacts,
				container, false);

		initViews();
		// refreshHandler.sendMessage(Message.obtain());
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

	private void initViews() {
		characterParser = CharacterParser.getInstance();
		noContacTextView = (TextView)rootView.findViewById(R.id.noContactText);
		pinyinComparator = new PinyinComparator();
		sideBar = (SideBar) rootView.findViewById(R.id.sidrbar);
		dialog = (TextView) rootView.findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

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

		SourceDateList = getDataFromDb();
		
		Collections.sort(SourceDateList, pinyinComparator);
		adapter = new ExpandableAdapter(rootView.getContext(), SourceDateList);
		msgContactsListView.setAdapter(adapter);
		if(SourceDateList.size() == 0){
			msgContactsListView.setVisibility(View.GONE);
			sideBar.setVisibility(View.GONE);
			noContacTextView.setVisibility(View.VISIBLE);
		}
		else {
			msgContactsListView.setVisibility(View.VISIBLE);
			sideBar.setVisibility(View.VISIBLE);
			noContacTextView.setVisibility(View.GONE);
		}

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
		getContactListFromServer();
	}

	public void getContactListFromServer() {

        isLoading=true;
		// 创建连接
		String url = "/user/dirs";
//		String url = "https://" + ip + "/api/v1/user/dirs" + "?access_token="
//				+ account.getAccessToken();
	 jsonArrayRequest = new MyJsonArrayRequest(com.android.volley.Request.Method.GET, url, UpdateTokenRequest.TokenType.USER, new Response.Listener<JSONArray>() {
		 int httpRes = -1;

			@Override
			public void onResponse(JSONArray response) {

				EmmClientApplication.mDb.deleteAllDbItem(EmmClientDb.CONTACT_DATABASE_TABLE);
				EmmClientApplication.mDb.getAllItemsOfTable(EmmClientDb.CONTACT_DATABASE_TABLE, null);

				try {
					for (int i = 0; i < 1; i++) {
						JSONObject json = (JSONObject) response.get(i);
						String id = json.getString("id");
//						String name  = json.getString("name");

						String ip = NetworkDef.getAddrWebservice();
						final String url = "https://" + ip + "/api/v1/user/dirs/" + id + "?uuid=" + EmmClientApplication.mPhoneInfo.getIMEI()
								+"&access_token="+ EmmClientApplication.mCheckAccount.getAccessToken();
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                downloadAndSave(url);
                                SourceDateList.clear();
                                SourceDateList = getDataFromDb();
                                Collections.sort(SourceDateList, pinyinComparator);
                                Message message  = new Message();
                                message.arg1 = 1;
                                refreshHandler.sendMessage(message);
                                finishLoading();
                            }
                        });
                        thread.start();

					}  //end for
				}catch (Exception e) {
                    finishLoading();
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
                finishLoading();
                error.printStackTrace();

			}
		});

		EmmClientApplication.mVolleyQueue.add(jsonArrayRequest);

	}

    private void finishLoading() {
        isLoading=false;
        if(downloadDialog!=null && downloadDialog.isShowing())
            downloadDialog.dismiss();
    }
	
	public void updateListView(){
//		adapter = new ExpandableAdapter(rootView.getContext(), SourceDateList);
//		msgContactsListView.setAdapter(adapter);
		adapter.clear();
		adapter.addAll(SourceDateList);
		adapter.notifyDataSetChanged();
		if(SourceDateList.size() == 0){
			msgContactsListView.setVisibility(View.GONE);
			sideBar.setVisibility(View.GONE);
			noContacTextView.setVisibility(View.VISIBLE);
		}
		else {
			msgContactsListView.setVisibility(View.VISIBLE);
			sideBar.setVisibility(View.VISIBLE);
			noContacTextView.setVisibility(View.GONE);
		}
		if(downloadDialog!=null && downloadDialog.isShowing())
			downloadDialog.dismiss();
	}

	class MyHandler extends Handler {
		WeakReference<ExpandableContactsFragment> mFragment;

		MyHandler(ExpandableContactsFragment fragment) {
			mFragment = new WeakReference<ExpandableContactsFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 == 0) {
				final ExpandableContactsFragment theFragment = mFragment.get();
				theFragment.getContactListFromServer();
			}
			if(msg.arg1 == 1){
                updateListView();
			}
		}
	};
	
	

	MyHandler refreshHandler = new MyHandler(this);

	private List<SortModel> getDataFromDb() {
		List<SortModel> mSortList = new ArrayList<SortModel>();
		Cursor mCursor = EmmClientApplication.mDb.getAllItemsOfTable(
				EmmClientDb.CONTACT_DATABASE_TABLE, null);
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
		return ans.replace("%3A", ":").replaceAll("\\+","%20");
	}
	
	private void downloadAndSave(String urlP) {

		String mSavePath = Environment.getExternalStorageDirectory()
				+ "/download";

		// 创建连接
		InputStream is = null;

		try {

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new IgnoreCertTrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
            HttpsURLConnection conn = (HttpsURLConnection) new URL(urlP).openConnection();
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();

			is = conn.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
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
						Cursor mCoursor = EmmClientApplication.mDb.getItemByInfo(
								EmmClientDb.CONTACT_DATABASE_TABLE,
								new String[] {  "_id","name","telephone","cellphone_1","cellphone_2","email_1","email_2","company","address"}, new String[] {
										code,name,telephone, cellphone_1,cellphone_2,email_1,email_2,company, address},
								null);
						if (mCoursor == null || mCoursor.getCount() <= 0) {
							EmmClientApplication.mDb.addDbItem(EmmClientDb.CONTACT_DATABASE_TABLE,new String[] {  "_id","name","telephone","cellphone_1","cellphone_2","email_1","email_2","company","address"}, new String[] {
									code,name,telephone, cellphone_1,cellphone_2,email_1,email_2,company, address});

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



    private class MyHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub
            return true;
        }
    }


}
