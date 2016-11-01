package cn.dacas.emmclient.ui.activity.loginbind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.model.DeviceModel;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.ui.activity.mainframe.NewMainActivity;
import cn.dacas.emmclient.ui.gesturelock.GestureLockActivity;
import cn.dacas.emmclient.util.QDLog;
import cn.dacas.emmclient.webservice.QdBusiness;
import cn.dacas.emmclient.webservice.qdvolley.LicenceError;

//import cn.dacas.emmclient.R;
//import cn.dacas.emmclient.gesturelock.GestureLockActivity;
//import cn.dacas.emmclient.main.EmmClientApplication;

public class BinderSelectorActivity extends BaseSlidingFragmentActivity {

    private final static String TAG ="BinderSelectorActivity";

    private Boolean defaultBindFlag;
    private String email, password;
    private TextView mPromptTextView;
    private Button mYesButton;
    private Button mNobutton;

    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Null_Text_Null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binder_selector,"");
        defaultBindFlag = getIntent().getBooleanExtra("default_bind_flag", false);
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        initView();

        mYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QDLog.i(TAG,"mYesButton=======" + defaultBindFlag);
                if (defaultBindFlag) {

                    String patternPass = EmmClientApplication.mDatabaseEngine.getPatternPassword(email);
                    if (patternPass == null) {
                        Intent intent = new Intent(BinderSelectorActivity.this, GestureLockActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(BinderSelectorActivity.this, NewMainActivity.class);
                        startActivity(intent);
                    }
                    finish();
                } else {
                    EmmClientApplication.mActivateDevice.reportDevice(email, password, new Response.Listener<Void>() {
                        @Override
                        public void onResponse(Void response) {
                            QDLog.i(TAG,"mYesButton==onResponse ok=====" );
                            QdBusiness.login(email, password, new Response.Listener<DeviceModel>() {
                                @Override
                                public void onResponse(DeviceModel device) {
                                    String patternPass = EmmClientApplication.mDatabaseEngine.getPatternPassword(email);
                                    if (patternPass == null) {
                                        Intent intent = new Intent(BinderSelectorActivity.this, GestureLockActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(BinderSelectorActivity.this, NewMainActivity.class);
                                        startActivity(intent);
                                    }
                                    finish();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    QDLog.i(TAG, "mYesButton==onErrorResponse =====");
                                    Toast.makeText(BinderSelectorActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(BinderSelectorActivity.this, UserLoginActivity.class);
                                    intent.putExtra("email", email).putExtra("password", password);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
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
            }
        });
            mNobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BinderSelectorActivity.this, BindUserActivity.class);
                intent.putExtra("email", email).putExtra("password", password);
                startActivity(intent);
                finish();
            }
        });
        }

    /////////////自定义的函数/////////
    private void initView() {

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(false);

        mMiddleHeaderView.setText(mContext.getString(R.string.bind_user));
//        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);
//
//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(false);

        mPromptTextView = (TextView) findViewById(R.id.textview_prompt);
        mYesButton = (Button) findViewById(R.id.button_yes);
        mNobutton = (Button) findViewById(R.id.button_no);

        if (defaultBindFlag) {
            mPromptTextView.setText(getString(R.string.prompt_default_setting_text1, email));
            mYesButton.setText(R.string._confirm_text);
            mNobutton.setVisibility(View.GONE);
        } else {
            mPromptTextView.setText(R.string.prompt_setting_device_user);
            mYesButton.setText(R.string.prompt_btn_yes);
            mNobutton.setVisibility(View.VISIBLE);
        }

//        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
//        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

    }


}
