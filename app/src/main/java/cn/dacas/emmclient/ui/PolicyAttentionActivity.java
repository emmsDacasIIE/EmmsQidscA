package cn.dacas.emmclient.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.mam.AppStoreActivity;
import cn.dacas.emmclient.mam.RefreshableView;
import cn.dacas.emmclient.mam.RefreshableView.PullToRefreshListener;
import cn.dacas.emmclient.worker.PolicyManager;
import cn.dacas.emmclient.worker.PolicyManager.PolicyItemStatus;

public class PolicyAttentionActivity extends QdscFormalActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_policy_attention);
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}
	
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.policy_attention, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return PolicyAttentionFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 1 total pages.
			return 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_activity_policy_attention)
						.toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PolicyAttentionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private TextView noPolicyText;
		private RefreshableView refreshableView;
		private ListView policyListView = null;
		private PolicyManager mPolicyManager;
		
		private ProgressDialog mProgressDlg;
		
		private List<PolicyItemStatus> policyArrayList;
		
		private static Map<String, Integer> policy2icon = new HashMap<String, Integer>();
		static{
			policy2icon.put(PolicyManager.BLACK_APP_POLICY, R.drawable.policy_black_list);
			policy2icon.put(PolicyManager.CAMERA_POLICY, R.drawable.policy_camera);
			policy2icon.put(PolicyManager.MUST_APP_POLICY, R.drawable.policy_must_list);
			policy2icon.put(PolicyManager.PASSWD_POLICY, R.drawable.policy_password);
		}

		private static final String ARG_SECTION_NUMBER = "section_number";
		

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PolicyAttentionFragment newInstance(int sectionNumber) {
			PolicyAttentionFragment fragment = new PolicyAttentionFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_policy_attention, container, false);
			
			mPolicyManager = ((EmmClientApplication)this.getActivity().getApplicationContext()).getPolicyManager();

			noPolicyText = (TextView)rootView.findViewById(R.id.noPolicyText);
			refreshableView = (RefreshableView)rootView.findViewById(R.id.refreshable_view);
			policyListView = (ListView)rootView.findViewById(R.id.policyListView);
			
			refreshableView.setOnRefreshListener(new PullToRefreshListener() {

				@Override
				public void onRefresh() {
					// TODO Auto-generated method stub
					refreshHandler.sendMessage(Message.obtain());
					refreshableView.finishRefreshing();
				}
			}, 5);
			return rootView;
		}
		
		@Override
		public void onResume(){
			super.onResume();
			refreshHandler.sendMessage(Message.obtain());
		}
		
		private Handler refreshHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				mProgressDlg = ProgressDialog.show(PolicyAttentionFragment.this.getActivity(), "加载中",
						"正在验证当前策略...", true, false);

				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						policyArrayList = PolicyAttentionFragment.this.mPolicyManager.getPolicyStatusDetails();
						mProgressDlg.dismiss();
					}
				});
				thread.start();
				try {
					thread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				showPolicyList();
			}
		};
		
		private void showPolicyList(){
			//将policyArrayList中的信息显示出来
			int length = policyArrayList.size();
			if (length <= 0) {
				noPolicyText.setVisibility(View.VISIBLE);
				noPolicyText.setText("安全设置内容为空");
				refreshableView.setVisibility(View.GONE);
				return;
			} else {
				noPolicyText.setVisibility(View.GONE);
				refreshableView.setVisibility(View.VISIBLE);
			}

			PolicyItemListAdapter mAdapter = new PolicyItemListAdapter(this.getActivity());
			policyListView.setAdapter(mAdapter);
			
			String policyName = PolicyAttentionFragment.this.mPolicyManager.getPolicy().getNameAndVersion();
			
			if(policyName != null && policyName.length() > 0){
				((PolicyAttentionActivity)PolicyAttentionFragment.this.getActivity()).setTitle("当前策略:"+policyName);
			}
		}

		private class PolicyItemListAdapter extends BaseAdapter {
			private LayoutInflater mInflater;// 得到一个LayoutInfalter对象用来导入布局

			/** 构造函数 */
			public PolicyItemListAdapter(Context context) {
				this.mInflater = LayoutInflater.from(context);
			}

			@Override
			public int getCount() {
				return policyArrayList.size();// 返回数组的长度
			}

			@Override
			public Object getItem(int position) {
				return policyArrayList.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				PolicyHolder holder;
				if (convertView == null) {
					convertView = mInflater.inflate(
							R.layout.list_item_policy_attention, null);
					holder = new PolicyHolder();
					holder.image = (ImageView) convertView
							.findViewById(R.id.image);
					holder.title = (TextView) convertView
							.findViewById(R.id.title);
					holder.subtitle = (TextView) convertView
							.findViewById(R.id.subtitle);
					holder.itemCount = (TextView) convertView
							.findViewById(R.id.itemCount);
					holder.chevron = (ImageView) convertView
							.findViewById(R.id.chevron);

					convertView.setTag(holder);
				} else {
					holder = (PolicyHolder) convertView.getTag();
				}
				PolicyItemStatus policyItem = policyArrayList.get(position);
				
				holder.title.setText(policyItem.itemName);
				holder.subtitle.setText(policyItem.itemDetail);
				holder.itemCount.setVisibility(View.INVISIBLE);
				holder.chevron.setImageResource(policyItem.isAccord?R.drawable.policy_accord:R.drawable.policy_dis_accord);
				
				int imageId = policy2icon.get(policyItem.itemName);
				holder.image.setImageResource(imageId);
				switch(imageId){
				case R.drawable.policy_black_list:
				case R.drawable.policy_must_list:
					if(!policyItem.isAccord){
						//应用的必装或黑名单不满足，跳转到应用管理的界面
						holder.chevron.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								v.getContext().startActivity(new Intent(v.getContext(), AppStoreActivity.class));
							}});
					}
					break;
				case R.drawable.policy_password:
					holder.chevron.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							v.getContext().startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
						}});
					break;
				case R.drawable.policy_camera:
					break;
				default:
				}

				convertView.setOnClickListener(new OnClickListener() {
					Boolean flag = true;

					@Override
					public void onClick(View view) {
						TextView stv = (TextView) view
								.findViewById(R.id.subtitle);
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

		private class PolicyHolder {
			public ImageView image;
			public TextView title;
			public TextView subtitle;
			public TextView itemCount;
			public ImageView chevron;
		}
	}

}
