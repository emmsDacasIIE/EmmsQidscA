package cn.dacas.emmclient.ui.activity.loginbind;

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
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.manager.ActivityManager;
import cn.dacas.emmclient.manager.AddressManager;
import cn.dacas.emmclient.model.DeviceModel;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.ui.activity.mainframe.NewMainActivity;
import cn.dacas.emmclient.ui.gesturelock.GestureLockActivity;
import cn.dacas.emmclient.ui.qdlayout.QdLoadingDialog;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.QDLog;
import cn.dacas.emmclient.webservice.QdBusiness;
import cn.dacas.emmclient.webservice.QdWebService;
import cn.dacas.emmclient.webservice.qdvolley.DeviceforbiddenError;

/**
 * @author Wang
 */
public class UserLoginActivity extends BaseSlidingFragmentActivity{

    private static final String TAG = "UserLoginActivity";
    public static UserLoginActivity mUserLoginActivity = null;
    private static boolean isLogining = false;

    private LinearLayout mMainLayout = null;
     //private FrameLayout mSubFrameLayout = null;
    private LinearLayout mServerAddressLayout = null;
    private RelativeLayout mlogin_form = null;
    private static final String defaultPort="8443";

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
//    private LoadingDialog mLoadingDialog;


    @Override
    protected HearderView_Style setHeaderViewStyle() {
        return HearderView_Style.Text_Text_Null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login, "");
        init();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mUserLoginActivity = null;
    }

    ////////////////自定义函数////////////
    private void initMyView() {

        mMiddleHeaderView.setText(mContext.getString(R.string.login));

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mModifyServiceAddrTextView = (TextView) findViewById(R.id.text_modify_service_addr);
        mLoginButton = (Button) findViewById(R.id.loginBt);

        mMainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mServerAddressLayout = (LinearLayout) findViewById(R.id.server_address_layout);
        mlogin_form = (RelativeLayout) findViewById(R.id.login_form);

        mServerAddressView = (EditText) findViewById(R.id.server_address);
        mServerPortView = (EditText) findViewById(R.id.server_port);
//
//        mLeftHeaderView.setVisibility(View.VISIBLE);
//        mLeftHeaderView.setTextVisibile(true);


        setOnClickLeft(R.string.cancel,true, new OnLeftListener() {

            @Override
            public void onClick() {
                //相当于关闭服务器设置页面。
                QDLog.i(TAG, "cancel==========================");
                updateUI(UiVisibleType.VisibleType_NotFirstLogin);
                isModifyServiceAddre = false;
            }
        });
    }

    private void init() {
        initMyView();
        initMyData();

        //for test,设置背景为灰色
//        mMainLayout.setBackgroundColor(Color.GRAY);
    }

    private void initMyData() {
        boolean isReported = EmmClientApplication.mActivateDevice.isDeviceReported();
        String ip= AddressManager.getAddrWebservice();
        if (!isReported || ip==null) {
            //未绑定,意味着首次登录
            updateUI(UiVisibleType.VisibleType_FirstLogin);
        } else {
            //已绑定
            updateUI(UiVisibleType.VisibleType_NotFirstLogin);

        }

        if (ip != null) {
            String[] adds = AddressManager.getAddrWebservice().split(":");
            mServerAddressView.setText(adds[0]);
            mServerPortView.setText(adds[1]);
        }
        else  mServerPortView.setText(defaultPort);
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
        }
        String addr=mServerAddressStr + ":" + mServerPortStr;
        if (!AddressManager.getAddrWebservice().equals(addr)) {
            //更换服务器，重置消息
            PrefUtils.putMsgMaxId(0);
            PrefUtils.putMsgUnReadCount(0);
            AddressManager.setAddrWebservice(mServerAddressStr + ":" + mServerPortStr);
            //获取服务器IP集合
            QdWebService.getIpSettings(new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        //TODO How it works!
                        JSONObject message_broker = response.getJSONObject("message_broker");
                        JSONObject secure_access = response.getJSONObject("secure_access");
                        JSONObject android_app = response.getJSONObject("android_app");
                        String addrMsg = message_broker.getString("host") + ":" + message_broker.getString("port");
                        String addrForward = secure_access.getString("host") + ":" + secure_access.getString("port");
                        String addrUpdate = android_app.getString("app_url");
                        QDLog.writeMsgPushLog("get addr "+addrMsg);
                        AddressManager.setAddrMsg(addrMsg);
                        AddressManager.setAddrForward(addrForward);
                        AddressManager.setAddrUpdate(addrUpdate);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, null);
        }
        return true;
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
//        else if (PhoneInfoExtractor.isRooted()) {
//           Toast.makeText(UserLoginActivity.this,"设备已Root，无法使用平台",Toast.LENGTH_SHORT).show();
//            return;
//        }
        else {
            execLogin();
        }
    }

    private void execLogin() {
        loading();
        QdBusiness.login(mEmail, mPassword, new Response.Listener<DeviceModel>() {
            @Override
            public void onResponse(DeviceModel response) {
                if (mLoadingDialog!=null) {
                    mLoadingDialog.dismiss();
                }
                if (!EmmClientApplication.mActivateDevice.isDeviceReported() && response.getOwner_account().equals(mEmail)) {
                    // go to BinderHitActivity
                    Intent intent = new Intent(mContext, BinderSelectorActivity.class);
                    intent.putExtra("default_bind_flag", true);
                    startActivity(intent);
                    finish();
                } else if (EmmClientApplication.mDatabaseEngine.getPatternPassword(mEmail) == null) {
                    Intent intent = new Intent(mContext, GestureLockActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    if(ActivityManager.isLocking) {
                        ActivityManager.isLocking = false;
                        finish();
                    }else {
                        //goto NewMainActivity
                        Intent intent = new Intent(mContext, NewMainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mLoadingDialog!=null) {
                    mLoadingDialog.dismiss();
                }
                if (error.networkResponse!=null && error.networkResponse.statusCode == 404) {
                    //go to BinderSelectorActivity
                    Intent intent = new Intent(mContext, BinderSelectorActivity.class);
                    intent.putExtra("default_bind_flag", false);
                    intent.putExtra("email", mEmail).putExtra("password", mPassword);
                    startActivity(intent);
                    finish();
                }
                else if (error instanceof ParseError) {
                    Toast.makeText(mContext,"解析错误",Toast.LENGTH_SHORT).show();
                }
                else if (error instanceof AuthFailureError) {
                    Toast.makeText(mContext,"权限不足，无法使用设备",Toast.LENGTH_SHORT).show();
                }
                else if (error instanceof NoConnectionError || error instanceof NetworkError || error instanceof TimeoutError) {
                    Toast.makeText(mContext,"网络连接错误",Toast.LENGTH_SHORT).show();
                } else if( error instanceof DeviceforbiddenError){
                    Toast.makeText(mContext,"该设备被禁用",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(mContext,"登录错误",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

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
                mLeftHeaderView.setVisibility(View.GONE);
                mLoginButton.setText(R.string.login);
                break;
            case VisibleType_NotFirstLogin:
                mServerAddressLayout.setVisibility(View.VISIBLE);
                mServerAddressView.setVisibility(View.GONE);
                mServerPortView.setVisibility(View.GONE);

                mlogin_form.setVisibility(View.VISIBLE);
                mModifyServiceAddrTextView.setVisibility(View.VISIBLE);

//                leftTextView.setVisibility(View.GONE);
                mLeftHeaderView.setVisibility(View.GONE);
                mLoginButton.setText(R.string.login);
                break;
            case VisibleType_ModifyService:
                mServerAddressLayout.setVisibility(View.VISIBLE);
                mServerAddressView.setVisibility(View.VISIBLE);
                mServerPortView.setVisibility(View.VISIBLE);

                mlogin_form.setVisibility(View.GONE);

                mLeftHeaderView.setVisibility(View.VISIBLE);
                mLeftHeaderView.setTextVisibile(true);
                mLeftHeaderView.setText(R.string.cancel);

                mLeftHeaderView.setImageVisibile(false);
                mLoginButton.setText(R.string._confirm_text);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mLoadingDialog != null ) {
                mLoadingDialog.dismiss();
            }
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
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    ////////////doaloading//////////
    private QdLoadingDialog mLoadingDialog;

    private void loading() {
        if (mLoadingDialog==null || !mLoadingDialog.isShowing()) {
            mLoadingDialog = new QdLoadingDialog(this, "加载中...");
            mLoadingDialog.setCancelable(true);
            mLoadingDialog.show();
        }
    }

}
