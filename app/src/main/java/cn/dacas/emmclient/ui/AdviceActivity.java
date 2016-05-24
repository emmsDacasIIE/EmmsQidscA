package cn.dacas.emmclient.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.util.MyJsonObjectRequest;
import cn.dacas.emmclient.util.UpdateTokenRequest;
import cn.dacas.emmclient.worker.ConnectionDetector;
import cn.dacas.emmclient.worker.PhoneInfoExtractor;

public class AdviceActivity extends ActionBarActivity {

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
	
	private EmmClientApplication app;
	private PhoneInfoExtractor extractor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advice);
		app = (EmmClientApplication) AdviceActivity.this.getApplicationContext();
		extractor = PhoneInfoExtractor.getPhoneInfoExtractor(this);

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
		getMenuInflater().inflate(R.menu.advice, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_advice) {
			final PlaceholderFragment fragment = (PlaceholderFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());
			final String advice = fragment.getAdvice();
			if(advice != null && advice.length() > 0){
				if(!ConnectionDetector.isNetworkConnected(this)){
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("网络错误").setMessage("网络异常，请重新配置网络！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
					        Intent intent =  new Intent(Settings.ACTION_SETTINGS);  
					        startActivity(intent);
							dialog.dismiss();
						}
					}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
					
					builder.create().show();
				}else{
                    HashMap<String,String> map=new HashMap<>();
                    map.put("assignee",EmmClientApplication.mCheckAccount.getCurrentAccount());
                    map.put("title", EmmClientApplication.mPhoneInfo.getIMEI());
                    map.put("body", advice);
                    MyJsonObjectRequest request=new MyJsonObjectRequest(Request.Method.POST, "/user/feedback", UpdateTokenRequest.TokenType.USER, map,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Toast.makeText(getApplicationContext(), "反馈成功", Toast.LENGTH_LONG).show();
                                    fragment.clearAdvice();
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(getApplicationContext(), "反馈失败，请稍候重试！", Toast.LENGTH_LONG).show();
                        }
                    });
                    EmmClientApplication.mVolleyQueue.add(request);
				}
			}else{
				fragment.setAdviceError();
			}
			
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
			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_advice).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		
		private EditText adviceText;
		
		private static PlaceholderFragment single = null;

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			if(single == null){
				single = new PlaceholderFragment();
				Bundle args = new Bundle();
				args.putInt(ARG_SECTION_NUMBER, sectionNumber);
				single.setArguments(args);
			}
			return single;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_advice,
					container, false);
			adviceText = (EditText)rootView.findViewById(R.id.adviceText);
			
			adviceText.setOnKeyListener(new OnKeyListener(){

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					((EditText)v).setError(null);
					return false;
				}});	
			
			return rootView;
		}
		
		public String getAdvice(){
			return adviceText.getText().toString();
		}
		
		public void setAdviceError(){
			adviceText.setError("不能为空");
			adviceText.requestFocus();
		}
		
		public void clearAdvice(){
			adviceText.setText(null);
		}
	}
}