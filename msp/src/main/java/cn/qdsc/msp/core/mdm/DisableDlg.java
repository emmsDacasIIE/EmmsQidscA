package cn.qdsc.msp.core.mdm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.WindowManager;


public class DisableDlg {
	
	private static boolean isShowing = false;
	AlertDialog alertDialog;
	

	public DisableDlg(final Context context) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("应用禁用");
		builder.setMessage("该应用已经被禁用!");
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent startMain = new Intent(Intent.ACTION_MAIN); 
                startMain.addCategory(Intent.CATEGORY_HOME); 
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                context.startActivity(startMain); 
				dialog.dismiss();
				isShowing=false;
			}
		});
		alertDialog = builder.setCancelable(false).create();
		alertDialog.getWindow().setType(
				(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
//		alertDialog.getWindow().setType(
//				(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY));
	}
	

	public void showDisableDlg() {	
		if(!isShowing){			
			alertDialog.show();
			isShowing = true;
		}		
	}
	
	public void dismissDisableDlg() {
		alertDialog.dismiss();
		isShowing = false;
	}
	
	public boolean isShowing() {
		return isShowing;
	}

}