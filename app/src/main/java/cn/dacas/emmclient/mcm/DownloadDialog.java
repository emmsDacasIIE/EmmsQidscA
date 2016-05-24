package cn.dacas.emmclient.mcm;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import cn.dacas.emmclient.R;

public class DownloadDialog extends Dialog{

	private Context context;

	private View downLoadStatusView;
	private TextView downLoadStatusMessageView;
	
	public DownloadDialog(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dialog_download_contact);
		downLoadStatusView = (View) findViewById(R.id.download_status);
		downLoadStatusMessageView = (TextView) findViewById(R.id.download_status_message);
	}

}
