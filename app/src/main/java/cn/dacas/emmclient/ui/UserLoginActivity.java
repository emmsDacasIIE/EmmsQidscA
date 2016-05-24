package cn.dacas.emmclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.gesturelock.GestureLockActivity;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.ui.dialog.LoadingDialog;
import cn.dacas.emmclient.util.NetworkDef;
import cn.dacas.emmclient.util.QDLog;

/**
 * @author Wang
 */
public class UserLoginActivity extends BaseFA {

    private static final String TAG = "UserLoginActivity";
    public static UserLoginActivity mUserLoginActivity = null;
    private static boolean isLogining = false;

    private LinearLayout mMainLayout = null;
//    private FrameLayout mSubFrameLayout = null;
    private LinearLayout mServerAddressLayout = null;
    private RelativeLayout mlogin_form = null;

    private String mEmail;
    private String mPassword;
    private String mServerAddressStr;
    private String mServerPortStr;

    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mServerAddressView;
    private EditText mServerPortView;
    private TextView mModifyServiceAddrTextView;

    private Button mLoginButton;

    private String userAccount;

    enum UiVisibleType {
        VisibleType_FirstLogin,
        VisibleType_NotFirstLogin,
        VisibleType_ModifyService,
    }

    /**
     * 是否显示server ip 地址，如果已绑定，只不显示
     */
//    private boolean isShowServerAddrPort = true;

    /**
     * 点击修改服务器信息后，设置为true
     */
    private boolean isModifyServiceAddre = false;

    /**
     * 加载进度框
     */
    private LoadingDialog mLoadingDialog;

    @Override
    protected TitleBar_Style setMyContentView() {
        setContentView(R.layout.activity_user_login);
        return TitleBar_Style.Text_Text_Text;
    }

    @Override
    protected void initLayout() {
        super.initLayout();
        setOnClickLeft(new OnLeftListener() {

            @Override
            public void onClick() {
                //相当于关闭服务器设置页面。
                QDLog.i(TAG, "cancel==========================");
                updateUI(UiVisibleType.VisibleType_NotFirstLogin);
                isModifyServiceAddre = false;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
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
        mUserLoginActivity = null;
    }

    ////////////////自定义函数////////////
    private void initMyView() {
        middleTextView.setText(mContext.getString(R.string.login));
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mModifyServiceAddrTextView = (TextView) findViewById(R.id.text_modify_service_addr);
        mLoginButton = (Button) findViewById(R.id.loginBt);

        mMainLayout = (LinearLayout) findViewById(R.id.main_layout);
//        mSubFrameLayout = (FrameLayout) findViewById(R.id.sub_framelayout);
        mServerAddressLayout = (LinearLayout) findViewById(R.id.server_address_layout);
        mlogin_form = (RelativeLayout) findViewById(R.id.login_form);

        mServerAddressView = (EditText) findViewById(R.id.server_address);
        mServerPortView = (EditText) findViewById(R.id.server_port);
    }

    private void init() {
        initMyView();
        initMyData();

        //for test,设置背景为灰色
//        mMainLayout.setBackgroundColor(Color.GRAY);
    }

    private void initMyData() {
        boolean isReported = EmmClientApplication.mActivateDevice.isDeviceReported();
        String ip=NetworkDef.getAddrWebservice();
        if (!isReported || ip==null) {
            //未绑定,意味着首次登录
            updateUI(UiVisibleType.VisibleType_FirstLogin);
        } else {
            //已绑定
            updateUI(UiVisibleType.VisibleType_NotFirstLogin);

        }

        if (ip != null) {
            String[] adds = NetworkDef.getAddrWebservice().split(":");
            mServerAddressView.setText(adds[0]);
            mServerPortView.setText(adds[1]);
        }
        userAccount = EmmClientApplication.mCheckAccount.getCurrentAccount();
        if (userAccount != null) {
            mEmailView.setText(userAccount);
        }
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id,
                                          KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                mPasswordView.setError(null);
                mLoginButton.setError(null);
            }
        });

    }

    /**
     * @return true: 输入数据格式正确；false:输入数据格式不正确
     */

    private boolean checkServerAddressPort() {
        //如果server ip是显示出来的，则进行ip地址和port的check.
        mServerAddressStr = mServerAddressView.getText().toString();
        mServerPortStr = mServerPortView.getText().toString();
        if (TextUtils.isEmpty(mServerAddressStr) || TextUtils.isEmpty(mServerPortStr)) {
            return false;
        } else {
            NetworkDef.setAddrWebservice(mServerAddressStr + ":" + mServerPortStr);
            return true;
        }
    }


    /**
     * @return true: 输入数据格式正确；false:输入数据格式不正确
     */
    private boolean checkEmailPassword() {

        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(mEmail) || TextUtils.isEmpty(mPassword)) {
            return false;
        }
        return true;
    }

    /**
     * 参数校验
     *
     * @return true: 输入数据格式正确；false:输入数据格式不正确
     */
    private boolean checkLoginParam() {
        return checkServerAddressPort() && checkEmailPassword();
    }


    public void attemptLogin() {

        if (isLogining) {
            return;
        }
        boolean isCheckOK = checkLoginParam();

        if (!isCheckOK) {
            Toast.makeText(mContext, "输入有错误", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (EmmClientApplication.mPhoneInfo.isRooted()) {
           Toast.makeText(UserLoginActivity.this,"设备已Root，无法使用平台",Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            isLogining=true;
            execLogin();
        }
    }

    private void execLogin() {
        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        //showProgress(true);
//        mSubFrameLayout.setVisibility(View.VISIBLE);
//        mMainLayout.setVisibility(View.GONE);
        mLoadingDialog = new LoadingDialog(this, R.layout.view_tips_loading2);
        mLoadingDialog.show();

        EmmClientApplication.mCheckAccount.userLogin(mEmail, mPassword, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //showProgress(false);
                mLoadingDialog.dismiss();
                try {
                    String owner = response.getString("owner_name");
                    if (!EmmClientApplication.mActivateDevice.isDeviceReported() && owner.equals(mEmail)) {
                            // go to BinderHitActivity
                            Intent intent = new Intent(mContext, BinderSelectorActivity.class);
                            intent.putExtra("default_bind_flag", true);
                            startActivity(intent);
                            finish();
                    }
                    else  if (EmmClientApplication.mDb.getPatternPassword(mEmail) == null) {
                        Intent intent = new Intent(mContext, GestureLockActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        gotoNewMainActivity();
                    }
                } catch (JSONException e) {
                    Toast.makeText(mContext, "解析错误", Toast.LENGTH_SHORT).show();
//                    mSubFrameLayout.setVisibility(View.GONE);
//                    mMainLayout.setVisibility(View.VISIBLE);
//                    mLoadingDialog.dismiss();
                    e.printStackTrace();
                }
                isLogining = false;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //showProgress(false);
                mLoadingDialog.dismiss();
               if (error.networkResponse!=null && error.networkResponse.statusCode == 404) {
                    //go to BinderSelectorActivity
                    Intent intent = new Intent(mContext, BinderSelectorActivity.class);
                    intent.putExtra("default_bind_flag", false);
                    intent.putExtra("email", mEmail).putExtra("password", mPassword);
                    startActivity(intent);
                    finish();
                }
                else {
//                   mSubFrameLayout.setVisibility(View.GONE);
//                   mMainLayout.setVisibility(View.VISIBLE);

                   if (error instanceof NoConnectionError || error instanceof NetworkError || error instanceof TimeoutError)
                       Toast.makeText(mContext, "无法连接服务器", Toast.LENGTH_SHORT).show();
                   else if (error instanceof AuthFailureError)
                       Toast.makeText(mContext, "用户验证失败", Toast.LENGTH_SHORT).show();
                   else
                       Toast.makeText(mContext, "登录失败", Toast.LENGTH_SHORT).show();
               }
                isLogining=false;
            }
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    private void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = mContext.getResources().getInteger(
//                    android.R.integer.config_shortAnimTime);
//
//            mLoginStatusView.setVisibility(View.VISIBLE);
//            mLoginStatusView.animate().setDuration(shortAnimTime)
//                    .alpha(show ? 1 : 0)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            mLoginStatusView.setVisibility(show ? View.VISIBLE
//                                    : View.GONE);
//                        }
//                    });
//
//            mLoginFormView.setVisibility(View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime)
//                    .alpha(show ? 0 : 1)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            mLoginFormView.setVisibility(show ? View.GONE
//                                    : View.VISIBLE);
//                        }
//                    });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }
    public void login(View v) {
        if (isModifyServiceAddre) {
            //修改服务器信息
            confirmModifyAddr();
            isModifyServiceAddre=false;
        } else {
            //login
            attemptLogin();
        }

    }

    /**
     * 点击"修改服务器信息"时的操作
     *
     * @param v
     */
    public void modifyServiceAddr(View v) {
        isModifyServiceAddre = true;
        updateUI(UiVisibleType.VisibleType_ModifyService);
    }

    /**
     * 在"修改服务器信息"页面点击”确认“时的操作
     *
     * @param
     */
    private void confirmModifyAddr() {
        if (isModifyServiceAddre) {
            boolean checkOK = checkServerAddressPort();
            if (checkOK) {
                updateUI(UiVisibleType.VisibleType_NotFirstLogin);
            }
        }

    }


    private void updateUI(UiVisibleType type) {
        switch (type) {
            case VisibleType_FirstLogin:
                mServerAddressLayout.setVisibility(View.VISIBLE);
                mServerAddressView.setVisibility(View.VISIBLE);
                mServerPortView.setVisibility(View.VISIBLE);

                mlogin_form.setVisibility(View.VISIBLE);
                mModifyServiceAddrTextView.setVisibility(View.GONE);
                leftTextView.setVisibility(View.GONE);
                mLoginButton.setText(R.string.login);
                break;
            case VisibleType_NotFirstLogin:
                mServerAddressLayout.setVisibility(View.VISIBLE);
                mServerAddressView.setVisibility(View.GONE);
                mServerPortView.setVisibility(View.GONE);
//                mEmailView.setVisibility(View.VISIBLE);
//                mPasswordView.setVisibility(View.VISIBLE);

                mlogin_form.setVisibility(View.VISIBLE);
                mModifyServiceAddrTextView.setVisibility(View.VISIBLE);

                leftTextView.setVisibility(View.GONE);
                mLoginButton.setText(R.string.login);
                break;
            case VisibleType_ModifyService:
                mServerAddressLayout.setVisibility(View.VISIBLE);
                mServerAddressView.setVisibility(View.VISIBLE);
                mServerPortView.setVisibility(View.VISIBLE);

                mlogin_form.setVisibility(View.GONE);

                leftTextView.setVisibility(View.VISIBLE);
                leftTextView.setText(R.string.cancel);
                mLoginButton.setText(R.string._confirm_text);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (isModifyServiceAddre) {
                //相当于关闭服务器设置页面。
                updateUI(UiVisibleType.VisibleType_NotFirstLogin);
                isModifyServiceAddre = false;
                return true;
            }else {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(startMain);
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void gotoNewMainActivity() {
        Intent intent = new Intent(mContext, NewMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

}
