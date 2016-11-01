package cn.dacas.emmclient.ui.activity.mainframe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.webservice.QdWebService;

/**
 * @author Wang
 */
public class MyFeedbackActivity extends BaseSlidingFragmentActivity {

    private static final String TAG = "MyFeedbackActivity";

    private LinearLayout mMainLayout = null;
//    private FrameLayout mSubFrameLayout = null;
    private LinearLayout mServerAddressLayout = null;
    private RelativeLayout mlogin_form = null;

    private String contentStr;

    private EditText mContentEditText;


    private Button mConfirmButton;


    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Image_Text_Null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback, "");
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
        super.onDestroy();
    }

    ////////////////自定义函数////////////
    private void initMyView() {

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);

        mMiddleHeaderView.setText(mContext.getString(R.string.my_feedback));
//        mMiddleHeaderView.setTextVisibile(true);
        mMiddleHeaderView.setImageVisibile(false);

//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(false);


        mContentEditText = (EditText) findViewById(R.id.feedback_content);

        mConfirmButton = (Button) findViewById(R.id.confirm_button);

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QdWebService.submitFeedback(EmmClientApplication.mCheckAccount.getCurrentName(), mContentEditText.getText().toString(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Toast.makeText(mContext, "反馈成功", Toast.LENGTH_SHORT);
                                MyFeedbackActivity.this.finish();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(mContext, "反馈失败", Toast.LENGTH_SHORT);
                            }
                        });
            }
        });




    }

    private void init() {
        initMyView();

        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

        //for test,设置背景为灰色
//        mMainLayout.setBackgroundColor(Color.GRAY);
    }




}
