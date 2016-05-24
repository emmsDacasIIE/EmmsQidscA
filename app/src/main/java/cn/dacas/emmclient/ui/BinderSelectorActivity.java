package cn.dacas.emmclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.gesturelock.GestureLockActivity;
import cn.dacas.emmclient.main.EmmClientApplication;

public class BinderSelectorActivity extends BaseFA {

    private Boolean defaultBindFlag;
    private String email, password;
    private TextView mPromptTextView;
    private Button mYesButton;
    private Button mNobutton;

    @Override
    protected TitleBar_Style setMyContentView() {
        setContentView(R.layout.activity_binder_selector);
        return TitleBar_Style.Text_Text_Text;
    }

    @Override
    protected void initLayout() {
        super.initLayout();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        defaultBindFlag = getIntent().getBooleanExtra("default_bind_flag", false);
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        initView();

        mYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defaultBindFlag) {
                    if (EmmClientApplication.mDb.getPatternPassword(email) == null) {
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
                            EmmClientApplication.mCheckAccount.userLogin(email, password, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    if (EmmClientApplication.mDb.getPatternPassword(email) == null) {
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
                            Toast.makeText(BinderSelectorActivity.this, "绑定失败", Toast.LENGTH_SHORT).show();
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
        middleTextView.setText(mContext.getString(R.string.bind_user));

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

    }


}
