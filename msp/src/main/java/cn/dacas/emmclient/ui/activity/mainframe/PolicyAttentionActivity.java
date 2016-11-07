package cn.dacas.emmclient.ui.activity.mainframe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.mdm.PolicyManager;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.ui.qdlayout.RefreshableView;

/**
 * 合规详情
 */
public class PolicyAttentionActivity extends BaseSlidingFragmentActivity {

	private RefreshableView refreshableView;
	private ListView policyListView = null;

	private List<PolicyManager.PolicyItemStatus> policyArrayList;

	private static Map<String, Integer> policy2icon = new HashMap<String, Integer>();
	static{
		policy2icon.put(PolicyManager.BLACK_APP_POLICY, R.mipmap.msp_need_uninstall_app);
		policy2icon.put(PolicyManager.CAMERA_POLICY, R.mipmap.policy_camera);
		policy2icon.put(PolicyManager.MUST_APP_POLICY, R.mipmap.msp_must_install);
		policy2icon.put(PolicyManager.PASSWD_POLICY, R.mipmap.msp_lock_password);
	}


	@Override
	protected HearderView_Style setHeaderViewStyle() {
		return HearderView_Style.Image_Text_Null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_policy_attention, R.string.my_privacy_detail_info);
		initMyView();
		//来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
		//就不会显示左侧的menu了。
		setBehindContentView(R.layout.left_menu_frame);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		PolicyManager.getMPolicyManager(mContext).updatePolicy();
		showPolicyList();
	}

	private Handler refreshHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			showPolicyList();
		}
	};

	private void initMyView() {

		//mLeftHeaderView.setTextVisibile(false);
		//mLeftHeaderView.setImageVisibile(true);
		mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);

		String policyName = PolicyManager.getMPolicyManager(mContext).getPolicy().getName();
		mMiddleHeaderView.setText(mContext.getString(R.string.my_privacy_detail_info)+":"+policyName);
		//mMiddleHeaderView.setTextVisibile(true);
		//mMiddleHeaderView.setImageVisibile(false);

		//mRightHeaderView.setTextVisibile(false);
		//mRightHeaderView.setImageVisibile(false);

		refreshableView = (RefreshableView)findViewById(R.id.refreshable_view);
		policyListView = (ListView)findViewById(R.id.policyListView);

		refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				refreshHandler.sendMessage(Message.obtain());
				refreshableView.finishRefreshing();
			}
		}, 5);
	}

	private void showPolicyList(){
		policyArrayList = PolicyManager.getMPolicyManager(PolicyAttentionActivity.this).getPolicyStatusDetails();
		PolicyItemListAdapter mAdapter = new PolicyItemListAdapter(mContext);
		policyListView.setAdapter(mAdapter);
		String policyName = PolicyManager.getMPolicyManager(mContext).getPolicy().getName();
		if(policyName != null && policyName.length() > 0){
			setTitle("当前策略:"+policyName);
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
			PolicyManager.PolicyItemStatus policyItem = policyArrayList.get(position);

			holder.title.setText(policyItem.itemName);
			holder.subtitle.setText(policyItem.itemDetail);
			holder.itemCount.setVisibility(View.INVISIBLE);
			holder.chevron.setImageResource(policyItem.isAccord ? R.mipmap.msp_policy_check_ok:R.mipmap.msp_policy_check_ng);

			int imageId = policy2icon.get(policyItem.itemName);
			holder.image.setImageResource(imageId);
			switch(imageId){
				case R.mipmap.policy_black_list:
				case R.mipmap.policy_must_list:
					if(!policyItem.isAccord){
						//应用的必装或黑名单不满足，跳转到应用管理的界面
						holder.chevron.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
//								Toast.makeText(v.getContext(),"start appStoryAct==========",Toast.LENGTH_SHORT).show();
//								v.getContext().startActivity(new Intent(v.getContext(), AppStoreActivity.class));
							}});
					}
					break;
				case R.mipmap.policy_password:
					holder.chevron.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							v.getContext().startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
						}});
					break;
				case R.mipmap.policy_camera:
					break;
				default:
			}

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					TextView stv = (TextView) view
							.findViewById(R.id.subtitle);
					if (TextUtils.isEmpty(stv.getText().toString())) return;
					if (stv.getVisibility()==View.GONE)
						stv.setVisibility(View.VISIBLE);
					else if (stv.getVisibility()==View.VISIBLE)
						stv.setVisibility(View.GONE);
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
