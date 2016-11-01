package cn.dacas.emmclient.ui.activity.mainframe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;

/**
 * 我的-设置页面
 * @author Wang
 */
public class MySettingsActivity extends BaseSlidingFragmentActivity {

    private static final String TAG = "MySettingsActivity";

    private LinearLayout mLinearLayout1 = null;
    private LinearLayout mLinearLayout2 = null;
    private LinearLayout mLinearLayout3 = null;
    //private LinearLayout mLinearLayout4 = null;
    private LinearLayout mLinearLayout5=null;

    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Image_Text_Null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_settings, "");
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

        mMiddleHeaderView.setText(mContext.getString(R.string.my_settings));
//        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);

//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(false);

        mLinearLayout1 = (LinearLayout)findViewById(R.id.ll_privacy_settings);
        mLinearLayout2 = (LinearLayout)findViewById(R.id.ll_compliance_info);
        mLinearLayout3 = (LinearLayout)findViewById(R.id.ll_hand_password);
        //mLinearLayout4 = (LinearLayout)findViewById(R.id.ll_float);
        mLinearLayout5= (LinearLayout)findViewById(R.id.ll_backup);


        //隐私设置
        mLinearLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, MyPrivacySettingsActivity.class);
                startActivity(intent);
            }
        });

        //合规详情
        mLinearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, PolicyAttentionActivity.class);
                startActivity(intent);
            }
        });

        mLinearLayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, MyHandPasswordActivity.class);
                startActivity(intent);
            }
        });

        /*mLinearLayout4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, MyFloatActivity.class);
                startActivity(intent);
            }
        });*/

        mLinearLayout5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, BackupAndRestoreActivity.class));
            }
        });
    }


    private void init() {
        initMyView();
       // initMyData();

        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

    }

}
