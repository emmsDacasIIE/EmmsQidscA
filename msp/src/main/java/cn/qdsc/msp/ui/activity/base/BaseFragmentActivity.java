package cn.qdsc.msp.ui.activity.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;

import cn.qdsc.msp.R;
import cn.qdsc.msp.ui.qdlayout.HeaderView;

public class BaseFragmentActivity extends FragmentActivity {

    protected Context mContext;

    protected LayoutInflater mInflater;

    /**
     * 当点击左边的功能键时触发的事件，子类实现该接口即可
     */
    protected OnLeftListener mOnLeftListener;

    /**
     * 当点击右边的功能键时触发的事件，子类实现该接口即可
     */
    protected OnRightListener mOnRightListener;

    /**
     * 当点击右边的全选按钮设置键时触发的事件，子类实现该接口即可
     */
    protected OnRightCheckedChangeListener mOnRightCheckedChangeListener;

    /**
     * 当点击左边的返回键时跳转到主页触发的事件，子类实现该接口即可 （一般用于没有正常启动主页的行为动作）
     */
    protected OnLeft2HomeListener mOnLeft2HomeListener;

    protected LinearLayout mHeaderLayout, mBodyLayout;

    protected HeaderView mLeftHeaderView, mMiddleHeaderView, mRightHeaderView;

    protected LinearLayout mBottomLayout;



//	private QuickNavigationMenu mQuickNavigationMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initLayout();
        titleTest();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void titleTest() {
        mMiddleHeaderView.setText("middletest");
        mLeftHeaderView.setText("lefttest");
//		mRightHeaderView.setVisibility(View.VISIBLE);
        mRightHeaderView.setText("righttest");

    }

    private void initLayout() {
        mContext = this;
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mHeaderLayout = (LinearLayout) findViewById(R.id.headerLayout);
        mBodyLayout = (LinearLayout) findViewById(R.id.bodyLayout);
        mLeftHeaderView = (HeaderView) findViewById(R.id.leftHeaderView);
        mMiddleHeaderView = (HeaderView) findViewById(R.id.middleHeaderView);
        mRightHeaderView = (HeaderView) findViewById(R.id.rightHeaderView);

        mBottomLayout = (LinearLayout) findViewById(R.id.bottomLayout);
        mBottomLayout.setVisibility(View.GONE);

        ColorStateList colors = mContext.getResources().getColorStateList(
                R.color.white);
        mMiddleHeaderView.setTextColor(colors);

        colors = mContext.getResources().getColorStateList(
                R.color.white);
        mLeftHeaderView.setTextColor(colors);
        mRightHeaderView.setTextColor(colors);

//		mRightHeaderView.setVisibility(View.GONE);
        mLeftHeaderView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mOnLeftListener != null) {
                    mOnLeftListener.onClick();
                    return;
                }
                if (mOnLeft2HomeListener != null) {
                    mOnLeft2HomeListener.onClick();
                    return;
                }
                finish();
            }
        });
        mRightHeaderView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mOnRightListener != null) {
                    mOnRightListener.onClick();
                }
                if (mOnRightCheckedChangeListener != null) {
                    mRightHeaderView.getAllCheck().toggle();
                    mOnRightCheckedChangeListener
                            .onCheckedChanged(mRightHeaderView.getAllCheck()
                                    .isChecked());
                }
            }
        });

//		mLeftHeaderView.setBackVisibile(true);
    }

    /**
     * 子类自定义头的右边事件
     *
     * @param resid
     * @param listener
     */
    public void setOnClickRight(int resid, OnRightListener listener) {
        setOnClickRight(mContext.getString(resid), listener);
    }

    /**
     * 子类自定义头的右边事件
     *
     * @param
     * @param listener
     */
    public void setOnClickRight(String text, OnRightListener listener) {
        mRightHeaderView.setVisibility(View.VISIBLE);
        mRightHeaderView.setText(text);
        this.mOnRightListener = listener;
    }

    /**
     * 子类自定义头的右边事件
     *
     * @param
     * @param listener
     */
    public void setOnClickRight4Check(OnRightCheckedChangeListener listener) {
        mRightHeaderView.setVisibility(View.VISIBLE);
        mRightHeaderView.setAllCheck(true);
        this.mOnRightCheckedChangeListener = listener;
    }


    /**
     * 子类自定义头的右边事件
     *
     * @param
     * @param
     */
    public void setOnClickRight4CheckVisibile(boolean visibile) {
        if (mOnRightCheckedChangeListener != null) {
            mRightHeaderView.setAllCheck(visibile);
        }
    }


    /**
     * 子类自定义头的右边事件
     *
     * @param
     * @param
     */
    public void setOnClickRight4Checked(boolean checked) {
        if (mOnRightCheckedChangeListener != null) {
            mRightHeaderView.setChecked(checked);
        }
    }

    /**
     * 子类自定义头的右边事件
     *
     * @param
     * @param
     */
    public HeaderView getOnClickRight4Check() {
        if (mOnRightCheckedChangeListener != null) {
            return mRightHeaderView;
        }
        return null;
    }


    /**
     * 子类自定义头的左边的事件
     *
     * @param listener
     */
    public void setOnClickLeft(int resid, boolean backIcon, OnLeftListener listener) {
        setOnClickLeft(mContext.getString(resid), backIcon, listener);
    }

    /**
     * 子类自定义头的左边的事件
     *
     * @param listener
     */
    public void setOnClickLeft(String text, boolean backIcon,OnLeftListener listener) {
        mLeftHeaderView.setText(text);
        mLeftHeaderView.setImageVisibile(backIcon);
//		mLeftHeaderView.setLineFocus(lineFocus);
        this.mOnLeftListener = listener;
    }

    /**
     * 子类自定义头的左边返回主页的事件
     *
     * @param listener
     */
    public void onClickLeft2Home(OnLeft2HomeListener listener) {
        this.mOnLeft2HomeListener = listener;
    }

    /**
     * 子类定制自己的body样式
     *
     * @param layout
     *            样式xml文件
     * @param middleTitle
     *            R的string值
     */
    public void setContentView(int layout, int middleTitle) {
        setContentView(layout, mContext.getString(middleTitle));
    }

    /**
     * 子类定制自己的body样式
     * 必须要在子类中调用的，否则，就不会显示head和bottom
     * @param layout
     *            样式xml文件
     * @param
     *
     */
    public void setContentView(int layout, String middleTitle) {
        mMiddleHeaderView.setText(middleTitle);
        mInflater.inflate(layout, mBodyLayout);
    }

    /**
     * 子类定制自己的body样式
     *
     * @param layout
     * @param middleTitle
     * @param middleBackground
     *            标题的背景
     */
    public void setContentView(int layout, String middleTitle,
                               boolean middleBackground) {
        mMiddleHeaderView.setText(middleTitle);
        mMiddleHeaderView.setTextBackground(middleBackground);
        mInflater.inflate(layout, mBodyLayout);
    }

    /**
     * 子类定制自己的body样式
     *
     * @param layout
     * @param middleTitle
     * @param middleBackground
     *            标题的背景
     */
    public void setContentView(int layout, int middleTitle,
                               boolean middleBackground) {
        setContentView(layout, mContext.getString(middleTitle),
                middleBackground);
    }

    /**
     * 设置左边标题和中间标题
     *
     * @param layout
     * @param leftTitle
     * @param middleTitle
     */
    public void setContentView(int layout, int leftTitle, int middleTitle) {
        setContentView(layout, mContext.getString(leftTitle),
                mContext.getString(middleTitle));
    }

    /**
     * 设置左边标题和中间标题
     *
     * @param layout
     * @param leftTitle
     * @param middleTitle
     */
    public void setContentView(int layout, String leftTitle, String middleTitle) {
        if(!TextUtils.isEmpty(leftTitle))
            mLeftHeaderView.setText(leftTitle);
        if(!TextUtils.isEmpty(middleTitle))
            mMiddleHeaderView.setText(middleTitle);
        mInflater.inflate(layout, mBodyLayout);
    }

    /**
     * 设置左边标题和中间标题
     *
     * @param v
     * @param leftTitle
     * @param middleTitle
     */
    public void setContentView(View v, String leftTitle, String middleTitle) {
        if(!TextUtils.isEmpty(leftTitle))
            mLeftHeaderView.setText(leftTitle);
        if(!TextUtils.isEmpty(middleTitle))
            mMiddleHeaderView.setText(middleTitle);
        mBodyLayout.addView(v);
    }

    /**
     * 设置左边标题和中间标题
     *
     * @param v
     * @param leftTitle
     * @param middleTitle
     */
    public void setContentView(View v, int leftTitle, int middleTitle) {
        setContentView(v, mContext.getString(leftTitle),
                mContext.getString(middleTitle));
    }

    /**
     * 设置左边标题和中间标题
     *
     * @param layout
     * @param leftTitle
     * @param middleTitle
     * @param middleBackground
     *            中间标题的背景
     */
    public void setContentView(int layout, int leftTitle, int middleTitle,
                               boolean middleBackground) {
        setContentView(layout, mContext.getString(leftTitle),
                mContext.getString(middleTitle), middleBackground);
    }

    /**
     * 设置左边标题和中间标题
     *
     * @param layout
     * @param leftTitle
     * @param middleTitle
     * @param middleBackground
     *            中间标题的背景
     */
    public void setContentView(int layout, String leftTitle,
                               String middleTitle, boolean middleBackground) {
        setContentView(layout, leftTitle, middleTitle);
        mMiddleHeaderView.setTextBackground(middleBackground);
    }

    public void setLinesFocus(boolean left, boolean middle, boolean right) {
//		mLeftHeaderView.setLineFocus(left);
//		mMiddleHeaderView.setLineFocus(middle);
//		mRightHeaderView.setLineFocus(right);
    }

    public void setMainTitle(int title) {
        mMiddleHeaderView.setText(this.getString(title));
    }

    public void setMainTitle(String title) {
        mMiddleHeaderView.setText(title);
    }

    /**
     * 针对删除操作时抽象的统一删除弹框接口
     *
     * @param listener
     */
    public void onDialogDelete(final OnDialogDeleteListener listener) {
//		CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
//		builder.setTitle(R.string.dialog_prompt_title);
//		builder.setMessage(R.string.dialog_prompt_message);
//		builder.setNegativeButton(getString(R.string.ok),
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//						if (listener != null) {
//							listener.onClick();
//						}
//					}
//				});
//
//		builder.setPositiveButton(getString(R.string.cancel),
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				});
//
//		CustomDialog mCustomDialog = builder.create();
//		mCustomDialog.show();
    }

    /**
     * 弹出一个只有一个确定按钮的对话框
     * @param msg
     */
    public void showOnePositiveBtnDialog(String msg) {
//		CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
//		builder.setTitle(R.string.dialog_prompt_title);
//		builder.setMessage(msg);
//		builder.setPositiveButton(getString(R.string.ok),
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				});
//
//		CustomDialog mCustomDialog = builder.create();
//		mCustomDialog.show();
    }

    /**
     * @param
     */
    public void showTwoBtnDialog(Context context, String title, String prompt, String btnOK,
                                 String btnCancel, DialogInterface.OnClickListener okListener) {
//		CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
//		builder.setTitle(title);
//		builder.setMessage(prompt);
//		builder.setNegativeButton(btnOK,okListener);
//		builder.setPositiveButton(btnCancel,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				});
//
//		CustomDialog mCustomDialog = builder.create();
//		mCustomDialog.show();
    }

//	public void setQuickNavigationMenu(List<MenuData> menuArray) {
//		mQuickNavigationMenu = new QuickNavigationMenu(mContext, menuArray);// 出现与消失的动画
//		mQuickNavigationMenu.update();
//	}

    @Override
    /**
     * 创建MENU
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        //menu.add("menu");// 必须创建一项
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    /**
     * 拦截MENU
     */
    public boolean onMenuOpened(int featureId, Menu menu) {
//		if (mQuickNavigationMenu != null) {
//			if (mQuickNavigationMenu.isShowing())
//				mQuickNavigationMenu.dismiss();
//			else {
//				mQuickNavigationMenu.showAtLocation(findViewById(R.id.parentLayout),
//						Gravity.BOTTOM, 0, 0);
//			}
//		}
        return true;// 返回为true 则显示系统menu
    }

    public interface OnLeftListener {

        public void onClick();
    }

    public interface OnRightListener {

        public void onClick();
    }

    public interface OnRightCheckedChangeListener {

        public void onCheckedChanged(boolean checked);
    }

    public interface OnLeft2HomeListener {

        public void onClick();
    }

    public interface OnDialogDeleteListener {

        public void onClick();
    }

}
