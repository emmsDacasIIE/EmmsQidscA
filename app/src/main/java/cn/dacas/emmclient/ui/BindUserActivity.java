package cn.dacas.emmclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.gesturelock.GestureLockActivity;
import cn.dacas.emmclient.main.ActivateDevice;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.ui.dialog.LoadingDialog;
import cn.dacas.emmclient.worker.PhoneInfoExtractor;

public class BindUserActivity extends BaseFA {
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
    private LoadingDialog mLoadingDialog;

    @Override
    protected TitleBar_Style setMyContentView() {
        setContentView(R.layout.activity_bind_edit_user);
        return TitleBar_Style.Text_Text_Text;
    }

    @Override
    protected void initLayout() {
        super.initLayout();
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_edit_user);

        activate = ((EmmClientApplication) BindUserActivity.this
                .getApplicationContext()).getActivateDevice();

        loginEmail = getIntent().getStringExtra("email");
        loginPassword = getIntent().getStringExtra("password");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    /** 在xml进行了事件onClick的注册*/
    public void bind(View v) {
        String imei = PhoneInfoExtractor.getPhoneInfoExtractor(mContext).getIMEI();
        if (null == imei)
            Toast.makeText(BindUserActivity.this, "无法获取设备IMEI号", Toast.LENGTH_SHORT).show();
        else
            attemptBind();
    }

    /////////////自定义的函数/////////
    private void initView() {
        middleTextView.setText(mContext.getString(R.string.bind_user));
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
        mLoadingDialog = new LoadingDialog(this, R.layout.view_tips_loading2);
        mLoadingDialog.show();
        activate.reportDevice(mEmail, mPassword, new Response.Listener<Void>() {
            @Override
            public void onResponse(Void response) {
                mLoadingDialog.dismiss();
                EmmClientApplication.mCheckAccount.userLogin(loginEmail, loginPassword, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (EmmClientApplication.mDb.getPatternPassword(mEmail) == null) {
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
                mLoadingDialog.dismiss();
                if (error instanceof NoConnectionError || error instanceof NetworkError || error instanceof TimeoutError)
                    Toast.makeText(mContext,"无法连接服务器",Toast.LENGTH_SHORT).show();
                else if (error instanceof AuthFailureError)
                    Toast.makeText(mContext,"用户验证失败",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext,"绑定责任人失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
