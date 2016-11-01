package cn.dacas.emmclient.ui.activity.loginbind;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.model.ActivateDevice;
import cn.dacas.emmclient.model.DeviceModel;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.ui.activity.mainframe.NewMainActivity;
import cn.dacas.emmclient.ui.gesturelock.GestureLockActivity;
import cn.dacas.emmclient.ui.qdlayout.QdLoadingDialog;
import cn.dacas.emmclient.util.PhoneInfoExtractor;
import cn.dacas.emmclient.webservice.QdBusiness;
import cn.dacas.emmclient.webservice.qdvolley.LicenceError;


public class BindUserActivity extends BaseSlidingFragmentActivity {
    private static final String FORGET_PW = "http://emms.wangpj.net:8080/EMMS";

    private boolean isDeviceReported = false;

    private ActivateDevice activate;

    // Values for email and password at the time of the login attempt.
    private String mEmail;
    private String mPassword;
    private String loginEmail, loginPassword;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mConfirmButton;

    private LinearLayout mMainLayout = null;
//    private FrameLayout mSubFrameLayout = null;

    /**
     * 加载进度框
     */
//    private LoadingDialog mLoadingDialog;

    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Null_Text_Null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_edit_user, "");

        activate = EmmClientApplication.mActivateDevice;

        loginEmail = getIntent().getStringExtra("email");
        loginPassword = getIntent().getStringExtra("password");

        initView();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
//        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
//        MobclickAgent.onPause(this);
    }

    /** 在xml进行了事件onClick的注册*/
    public void bind(View v) {
        String imei = PhoneInfoExtractor.getIMEI(mContext);
        if (null == imei)
            Toast.makeText(BindUserActivity.this, "无法获取设备IMEI号", Toast.LENGTH_SHORT).show();
        else
            attemptBind();
    }

    /////////////自定义的函数/////////
    private void initView() {
        mMiddleHeaderView.setText(mContext.getString(R.string.bind_user));
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mConfirmButton = (Button) findViewById(R.id.button_bind);

//        mConfirmButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String imei = PhoneInfoExtractor.getPhoneInfoExtractor(mContext).getIMEI();
//                if (null == imei)
//                    Toast.makeText(BindUserActivity.this, "无法获取设备IMEI号", Toast.LENGTH_SHORT).show();
//                else
//                    attemptBind();
//            }
//        });
        mMainLayout = (LinearLayout) findViewById(R.id.main_layout);
//        mSubFrameLayout = (FrameLayout) findViewById(R.id.sub_framelayout);

//        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
//        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
    }

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
     * @return true: 参数不正确； false:参数正确
     */
    private boolean checkLoginParam() {
        return checkEmailPassword();
    }

    public void attemptBind() {
        boolean isCheckOK = checkLoginParam();
        if (!isCheckOK) {
			Toast.makeText(mContext,"账户或密码输入错误",Toast.LENGTH_SHORT).show();
            return;
        } else {
            execBind();
        }
    }

    public void execBind() {
//        mSubFrameLayout.setVisibility(View.VISIBLE);
//        mMainLayout.setVisibility(View.GONE);
//        mLoadingDialog = new LoadingDialog(this, R.layout.view_tips_loading2);
//        mLoadingDialog.show();

        loading();
        activate.reportDevice(mEmail, mPassword, new Response.Listener<Void>() {
            @Override
            public void onResponse(Void response) {
//                mLoadingDialog.dismiss();

                if (mLoadingDialog != null){
                    mLoadingDialog.dismiss();
                }
                QdBusiness.login(loginEmail, loginPassword, new Response.Listener<DeviceModel>() {
                    @Override
                    public void onResponse(DeviceModel response) {
                        String str = EmmClientApplication.mDatabaseEngine.getPatternPassword(mEmail);
                        if (str == null) {
                            Intent intent = new Intent(BindUserActivity.this, GestureLockActivity.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(BindUserActivity.this, NewMainActivity.class);
                            startActivity(intent);
                        }
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BindUserActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(BindUserActivity.this, UserLoginActivity.class);
                        intent.putExtra("email", loginEmail).putExtra("password", loginPassword);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                mSubFrameLayout.setVisibility(View.GONE);
//                mMainLayout.setVisibility(View.VISIBLE);
//                mLoadingDialog.dismiss();

                if (mLoadingDialog != null){
                    mLoadingDialog.dismiss();
                }
                if (error instanceof NoConnectionError || error instanceof NetworkError || error instanceof TimeoutError)
                    Toast.makeText(mContext,"无法连接服务器",Toast.LENGTH_SHORT).show();
                else if (error instanceof AuthFailureError)
                    Toast.makeText(mContext,"用户验证失败",Toast.LENGTH_SHORT).show();
                else if (error instanceof LicenceError)
                    Toast.makeText(mContext,"终端授权已不足，请联系管理员",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext,"绑定责任人失败",Toast.LENGTH_SHORT).show();
            }
        });
    }


    ////////////doaloading//////////
    private QdLoadingDialog mLoadingDialog;

    private void loading() {

        mLoadingDialog = new QdLoadingDialog(this, "加载中...");
        mLoadingDialog.setCancelable(true);
        mLoadingDialog.show();
    }

}
