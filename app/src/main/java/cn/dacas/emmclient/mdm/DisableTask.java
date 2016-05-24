package cn.dacas.emmclient.mdm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.WindowManager;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

import cn.dacas.emmclient.main.ActivateDevice;
import cn.dacas.emmclient.ui.AppStartActivity;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.util.MySynRequest;
import cn.dacas.emmclient.util.UpdateTokenRequest;

public class DisableTask extends AsyncTask<Integer, Void, Integer> {
	Context mContext;
	private ActivateDevice activate;
	private AlertDialog.Builder deviceDeleteBuilder;
	private AlertDialog.Builder deviceDisableBuilder;

	public DisableTask(Context ctx) {
		mContext = ctx;
	}

	public static final int DELETE_DEVICE = 1000;
	public static final int AUTH_STATE_CHANGE = 2000;
	private static final int UNAUTH_SUCCESS = 2001;
	private static final int UNAUTH_FAILED = 2002;

	@Override
	protected Integer doInBackground(Integer... params) {
		int flag = params[0];
		activate = ((EmmClientApplication) mContext.getApplicationContext())
				.getActivateDevice();
		if (flag == DELETE_DEVICE) // 服务器端删除设备
			return DELETE_DEVICE;
		else if (flag == AUTH_STATE_CHANGE) {// 授权状态改变
            MySynRequest request=new MySynRequest(Request.Method.GET,"/devices/"+EmmClientApplication.mPhoneInfo.getIMEI()+"/activate", UpdateTokenRequest.TokenType.DEVICE);
            JSONObject result=request.excute();
            try {
                if (result.getBoolean("status"))
                    return UNAUTH_SUCCESS;
                else
                    return UNAUTH_FAILED;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
	}

	@Override
	protected void onPostExecute(final Integer flag) {
		if (flag == DELETE_DEVICE) {
			activate = ((EmmClientApplication) mContext.getApplicationContext())
					.getActivateDevice();
			activate.clearActivateInfo();
			((EmmClientApplication) mContext.getApplicationContext())
					.getCheckAccount().clearCurrentAccount();
			if (deviceDeleteBuilder == null) {
				deviceDeleteBuilder = new AlertDialog.Builder(mContext);
				deviceDeleteBuilder.setTitle("设备不可用");
				deviceDeleteBuilder.setMessage("您的设备已被管理员删除!");

				deviceDeleteBuilder.setPositiveButton("确定",
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(mContext,
										AppStartActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
										| Intent.FLAG_ACTIVITY_CLEAR_TASK);
								mContext.startActivity(intent);
								
							}
						});

				AlertDialog alertDialog = deviceDeleteBuilder.create();
				alertDialog.getWindow().setType(
						(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
				alertDialog.show();
			}
		} else if (flag == UNAUTH_SUCCESS) {
			((EmmClientApplication) mContext.getApplicationContext())
					.getCheckAccount().clearCurrentAccount();
			if (deviceDisableBuilder == null) {
				deviceDisableBuilder = new AlertDialog.Builder(mContext);
				deviceDisableBuilder.setTitle("设备不可用");
				deviceDisableBuilder.setMessage("您的设备未授权或已被管理用禁用!");

				deviceDisableBuilder.setPositiveButton("确定",
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(mContext,
										AppStartActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
										| Intent.FLAG_ACTIVITY_CLEAR_TASK);
								mContext.startActivity(intent);
								
							}
						});

				AlertDialog alertDialog = deviceDisableBuilder.create();
				alertDialog.getWindow().setType(
						(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
				alertDialog.show();
			}
		}
	}

	@Override
	protected void onCancelled() {
	}
}