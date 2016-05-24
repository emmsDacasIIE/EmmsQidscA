package cn.dacas.emmclient.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import cn.dacas.emmclient.R;

public class LoadingCommentDialog {
	private static Dialog mDialog = null;

	/**
	 * 
	 * @param context
	 *        null: use layout text (Loading...)
	 *        else: use input text
	 */
	public static void showCommentDialog(Context context) {
		closeCommentDialog(context);
		
		WindowManager m = ((Activity) context).getWindowManager();
		Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
		d.getHeight();
		d.getWidth();

		mDialog = new Dialog(context, R.style.Dialog);

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View layout = inflater.inflate(R.layout.prograss_dialog, null);
		mDialog.setContentView(layout);


		/*
		Window dialogWindow = mDialog.getWindow();//
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.gravity = Gravity.CENTER;
		// dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);

		// lp.x = 20; // 新位置X坐标
		// lp.y = 60; // 新位置Y坐标
		lp.width = d.getWidth() - 20; // 宽度
		// lp.height = (int) (d.getHeight()*0.4); // 高度
		
		dialogWindow.setAttributes(lp);
*/
		mDialog.show();
	}
	
	public static void closeCommentDialog(Context context) {
		if (mDialog!=null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}
	

}
