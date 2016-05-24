package cn.dacas.emmclient.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.MenuItem;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.db.EmmClientDb;
import cn.dacas.emmclient.gesturelock.GestureLockActivity;
import cn.dacas.emmclient.main.EmmClientApplication;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings1);

		setTitle("设置");
		getFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new PasswordPreferenceFragment())
				.commit();

	}
	
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	public static class PasswordPreferenceFragment extends PreferenceFragment {
		private Context mContext;
		private SwitchPreference useGesturePasswordPreference;
		String userAccount;
		private final int SETPATTERN = 0;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);

			this.mContext = getActivity().getApplicationContext();
			userAccount = ((EmmClientApplication) mContext).getCheckAccount()
					.getCurrentAccount();
			PreferenceScreen root = getPreferenceManager()
					.createPreferenceScreen(mContext);
			setPreferenceScreen(root);

			PreferenceCategory fakeHeader = new PreferenceCategory(mContext);
			fakeHeader.setTitle("密码");
			getPreferenceScreen().addPreference(fakeHeader);
			addPreferencesFromResource(R.xml.pref_password);

			useGesturePasswordPreference = (SwitchPreference) findPreference("password_use_gesture");
			
			String deviceType = EmmClientApplication.mActivateDevice.getDeviceType();
			if((getLoginType(userAccount) == 0)||deviceType.equals("COPE-PUBLIC"))
				useGesturePasswordPreference.setChecked(false);
			else
				useGesturePasswordPreference.setChecked(true);

			useGesturePasswordPreference
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							// TODO Auto-generated method stub
							if (newValue.toString() == "true") {
								String deviceType = EmmClientApplication.mActivateDevice.getDeviceType();
								if (deviceType.equals("COPE-PUBLIC")) {
									Toast.makeText(mContext,
											"企业设备（员工共用）设备不能设置手势密码。",
											Toast.LENGTH_SHORT).show();
									return false;
								}

								if (EmmClientApplication.mDb.getPatternPassword(userAccount) == null) {
									Intent intent = new Intent(mContext,
											GestureLockActivity.class);
									startActivityForResult(intent, SETPATTERN);
								}
								EmmClientApplication.mDb.setLoginType(userAccount, 1);
							} else {
								EmmClientApplication.mDb.setLoginType(userAccount, 0);
							}
							return true;
						}
					});

			// useGesturePasswordPreference.setChecked(checked);

			Preference resetGesturePasswordPreference = (Preference) findPreference("password_reset_gesture");
			resetGesturePasswordPreference
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {

						@Override
						public boolean onPreferenceClick(Preference preference) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(mContext,
									GestureLockActivity.class);
							startActivity(intent);
							return false;
						}
					});

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// TODO Auto-generated method stub
			return super.onOptionsItemSelected(item);
		}

		private int getLoginType(String mEmail) {
			Cursor mCoursor = EmmClientApplication.mDb.getItemByInfo(
					EmmClientDb.PASSWORD_DATABASE_TABLE,
					new String[] { "email" }, new String[] { mEmail },
					new String[] { "pwdtype" });
			return mCoursor.getInt(0);
		}



		@Override
		public void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			// TODO Auto-generated method stub
			if (requestCode == SETPATTERN) {
				if (resultCode == RESULT_OK) {
					useGesturePasswordPreference.setChecked(true);
					// setLoginType(userAccount, 1);
					super.onActivityResult(requestCode, resultCode, data);
				} else {
					useGesturePasswordPreference.setChecked(false);
				}
			}
		}

	}

}
