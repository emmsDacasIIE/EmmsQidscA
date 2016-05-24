package cn.dacas.emmclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import cn.dacas.emmclient.R;

public class PrivacyStatementActivity extends BaseFA {
	private WebView mWebView;
	private CheckBox mCheckBox;
	
	@Override
	protected TitleBar_Style setMyContentView() {
		setContentView(R.layout.activity_privacy_statement);
		return TitleBar_Style.Text_Text_Text;
	}

	@Override
	protected void initLayout() {
		super.initLayout();
		initView();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
				
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void allowPrivacy(View v) {
		if (!mCheckBox.isChecked()) {
			Toast.makeText(mContext,getString(R.string.privacy_checkbox_prompt),Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(PrivacyStatementActivity.this,
				UserLoginActivity.class);
		startActivity(intent);
		finish();
	}

	private void initView() {

		middleTextView.setText(mContext.getString(R.string.privacy_statement));
		mWebView = (WebView) findViewById(R.id.webView1);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setTextSize(WebSettings.TextSize.LARGER);
		mWebView.loadUrl("file:///android_asset/privacy_context.html");
		mCheckBox = (CheckBox) findViewById(R.id.checkBox1);
		mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				if (isChecked) {
					//mCheckBox.setError(null);
				}
			}
		});
	}
}
