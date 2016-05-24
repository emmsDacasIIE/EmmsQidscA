package cn.qdsc.msp.ui.activity.loginbind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import cn.qdsc.msp.R;
import cn.qdsc.msp.ui.activity.base.BaseSlidingFragmentActivity;

/**
 * 隐私协议--登录绑定时用
 */
public class PrivacyStatementActivity extends BaseSlidingFragmentActivity {
	private WebView mWebView;
	private CheckBox mCheckBox;

	@Override
	protected HearderView_Style setHeaderViewSyle() {
		return HearderView_Style.Null_Text_Null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_privacy_statement,"");
		initView();
				
	}
	
	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
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

		mMiddleHeaderView.setText(mContext.getString(R.string.privacy_statement));

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
