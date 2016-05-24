package cn.qdsc.msp.ui.activity.mainframe;

import android.os.Bundle;
import android.widget.TextView;

import cn.qdsc.msp.R;
import cn.qdsc.msp.ui.activity.base.BaseSlidingFragmentActivity;
import cn.qdsc.msp.util.GlobalConsts;

/**
 * @author Wang
 */
public class MsgDetailActivity extends BaseSlidingFragmentActivity {

    private static final String TAG = "MsgDetailActivity";

    private TextView mTitleTextView;
    private TextView mTimeTextView;
    private TextView mContentTextView;

    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Image_Text_Null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msgdetail, "");
        init();
        setMsgData();
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

        mMiddleHeaderView.setText(mContext.getString(R.string.msg_detail));
//        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);

//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(false);

        mTitleTextView = (TextView) findViewById(R.id.msgdetail_subject);
        mTimeTextView = (TextView) findViewById(R.id.msgdetail_time);
        mContentTextView = (TextView) findViewById(R.id.msgdetail_content);

//        mContentTextView.setText(contentStr);
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

    private void setMsgData() {
        Bundle bundle = this.getIntent().getExtras();
        String subjectStr = bundle.getString(GlobalConsts.Msg_Subject);
        String timeStr = bundle.getString(GlobalConsts.Msg_Time);
        String contentStr = bundle.getString(GlobalConsts.Msg_Content);

        mSubRightHeaderView.setText(subjectStr);
        mTitleTextView.setText(subjectStr);
        mTimeTextView.setText(timeStr);
        mContentTextView.setText(contentStr);
    }



}
