package cn.dacas.emmclient.ui.activity.mainframe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.update.UpdateManager;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.util.PhoneInfoExtractor;

/**
 * @author Wang
 */
public class MyAboutActivity extends BaseSlidingFragmentActivity {

    private static final String TAG = "MyInformationActivity";

    private LinearLayout mSystemInfoLayout = null;
    private LinearLayout mUpdateLinearLayout = null;
    private LinearLayout mFeedbackLinearLayout = null;
    private TextView mVersionTextView;


    @Override
    protected HearderView_Style setHeaderViewStyle() {
        return HearderView_Style.Image_Text_Null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_about, "");
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
    }

    ////////////////自定义函数////////////
    private void initMyView() {

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);

        mMiddleHeaderView.setText(mContext.getString(R.string.my_about));
//        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);

//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(false);

        mVersionTextView = (TextView)findViewById(R.id.textview_version);

        mSystemInfoLayout=(LinearLayout)findViewById(R.id.ll_info);
        mUpdateLinearLayout = (LinearLayout)findViewById(R.id.ll_version);
        mFeedbackLinearLayout = (LinearLayout)findViewById(R.id.ll_feedback);

        mSystemInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, SystemInfoActivity.class);
                startActivity(intent);
            }
        });

        mUpdateLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"已经是最新版本!",Toast.LENGTH_SHORT).show();
            }
        });

        //feedback
        mFeedbackLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent();
                intent2.setClass(mContext, MyFeedbackActivity.class);
                startActivity(intent2);
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

        //下面这两个函数为什么不起作用？
        String curVersion = PhoneInfoExtractor.getPackageVersionName(mContext, mContext.getPackageName());
        //int curVersionCode = PhoneInfoExtractor.getPackageVersionCode(mContext, mContext.getPackageName());

        mVersionTextView.setText(mVersionTextView.getText()+ ": " + curVersion);

        checkSwVersion();

    }

    private void checkSwVersion() {
        final UpdateManager manager = new UpdateManager(this);
        // 检查软件更新
        manager.checkUpdate();
    }


}
