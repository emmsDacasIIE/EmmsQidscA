package cn.qdsc.msp.ui.qdlayout;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import cn.qdsc.msp.R;


/**
 * @author xiechunlei
 */
public class QdLoadingDialog extends Dialog {

	private String msg;

	private LoadingView mLoadingView;

	private TextView tvMsg;

	/**
	 * @param context
	 * @param loadTip
	 */
	public QdLoadingDialog(Context context, String loadTip) {
		super(context);
		msg = loadTip;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//		this.getWindow().getDecorView()
//				.setBackgroundColor(Color.TRANSPARENT);
		setContentView(R.layout.load_data);
		initView();
	}

	public void initView() {
		tvMsg = (TextView) findViewById(R.id.msg);
		tvMsg.setText(msg);
		mLoadingView = (LoadingView)findViewById(R.id.loadingImage);
//		mLoadingView.setBackgroundColor(Color.TRANSPARENT);
	}

	public LoadingView getLoadingView() {
		return mLoadingView;
	}

	public void setMessage(String msg) {
		//tvMsg.setText(msg);
	}
}
