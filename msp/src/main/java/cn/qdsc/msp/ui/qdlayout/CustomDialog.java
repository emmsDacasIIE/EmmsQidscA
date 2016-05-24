package cn.qdsc.msp.ui.qdlayout;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.qdsc.msp.R;

/**
 * <p>
 * Title: CustomDialog
 * </p>
 * <p>
 * Description:自定义Dialog（参数传入Dialog样式文件，Dialog布局文件）
 * </p>
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author archie
 * @version 1.0
 */
public class CustomDialog extends Dialog implements View.OnClickListener {
	int layoutRes;// 布局文件
	Context context;
	/** title **/
	private TextView textview_title;
	/** 确定按钮 **/
	private Button confirmBtn;
	/** 取消按钮 **/
	private Button cancelBtn;
	
	/** text **/
	private TextView textview_content;
	
	private CustomDialogListener mCustomDialogListener;

	public CustomDialog(Context context, CustomDialogListener listener) {
		super(context);
		this.context = context;
		mCustomDialogListener = listener;
	}

	/**
	 * 自定义布局的构造方法
	 * 
	 * @param context
	 * @param resLayout
	 */
	public CustomDialog(Context context, int resLayout, CustomDialogListener listener) {
		super(context);
		this.context = context;
		this.layoutRes = resLayout;
		mCustomDialogListener = listener;
	}

	/**
	 * 自定义主题及布局的构造方法
	 * 
	 * @param context
	 * @param theme
	 * @param resLayout
	 */
	public CustomDialog(Context context, int theme, int resLayout, CustomDialogListener listener) {
		super(context, theme);
		this.context = context;
		this.layoutRes = resLayout;
		mCustomDialogListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(layoutRes);
		
		// 根据id在布局中找到控件对象
		textview_title = (TextView) findViewById(R.id.textview_title);
		confirmBtn = (Button) findViewById(R.id.confirm_btn);
		cancelBtn = (Button) findViewById(R.id.cancel_btn);
		textview_content = (TextView) findViewById(R.id.textview_content);
		
		
		// 设置按钮的文本颜色
		confirmBtn.setTextColor(0xff1E90FF);
		cancelBtn.setTextColor(0xff1E90FF);
		
		textview_content.setText(mCustomDialogListener.setTextviewText());
		confirmBtn.setText(mCustomDialogListener.setConfirmText());
		cancelBtn.setText(mCustomDialogListener.setCancelText());
		
		// 为按钮绑定点击事件监听器
		confirmBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.confirm_btn:
			
			mCustomDialogListener.confirm();
			break;
		case R.id.cancel_btn:
			mCustomDialogListener.cancel();
			break;
		}
	}
	public interface CustomDialogListener {
		
		
		public void confirm();
		public void cancel();
		public String setTextviewText();
		public String setConfirmText();
		public String setCancelText();
	}
}