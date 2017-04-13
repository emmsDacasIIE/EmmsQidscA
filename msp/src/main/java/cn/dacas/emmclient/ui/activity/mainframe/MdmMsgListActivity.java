package cn.dacas.emmclient.ui.activity.mainframe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import cn.dacas.emmclient.manager.ActivityManager;
import cn.dacas.pushmessagesdk.PushMsgManager;
import cn.dacas.emmclient.R;
import cn.dacas.emmclient.event.MessageEvent;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.ui.fragment.MsgListFragment;
import cn.dacas.emmclient.util.GlobalConsts;
import cn.dacas.emmclient.util.QDLog;
import de.greenrobot.event.EventBus;

//import android.widget.RelativeLayout;

/**
 * 主页的Activity
 * 参考资料：SwitchFragment
 */
//        SlidingFragmentActivity
//BaseFragmentActivity

public class MdmMsgListActivity extends BaseSlidingFragmentActivity {

    private static final String TAG = "MdmMsgListActivity";

    /**
     * 作为页面容器的ViewPager
     */
    ViewPager mViewPager;
    /**
     * 页面集合
     */
    List<Fragment> fragmentList;


    MsgListFragment mFragment = (MsgListFragment) MsgListFragment.newInstance(2);

//    private MsgListFragment mFragment;

//    private McmController mMcmController;
//    private Bundle savedInstanceState;
//
//    protected LinearLayout mButtonLayout;
//    protected Button 	  mLeftButton;
//    protected Button 	  mRightButton;

    @Override
    protected HearderView_Style setHeaderViewStyle() {
        return HearderView_Style.Image_Text_Text;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_message, "");
        setContentView(R.layout.activity_message_list, "");


        //初始化header
        initHeader();

//        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
//        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

        // 初始化SlideMenu
//        initRightMenu();

        // 初始化ViewPager
        initViewPager();

        //初始化底部按键
//        setmBottomLayoutVisibility(View.VISIBLE);

//        mButtonLayout = (LinearLayout) findViewById(R.id.button_layout);

//        mLeftButton = (Button) findViewById(R.id.left_btn);
//        mRightButton = (Button) findViewById(R.id.right_btn);
//        mButtonLayout.setVisibility(View.VISIBLE);

        registerBoradcastReceiver();
        PushMsgManager.cancelNotification(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected  void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(ActivityManager.isLocking)
            ActivityManager.gotoUnlockActivity();
    }

    @Override
    public void onBackPressed() {
        if(getIntent().getBooleanExtra("FromMsg",false)) {
            startActivity(NewMainActivity.getMainActivityIntent(this));
        }
        else
            super.onBackPressed();
    }
    public String getRightText() {
        return mRightHeaderView.getText();
    }

    public void setHeaderText(String rightText) {
        mRightHeaderView.setText(rightText);
    }

    /**
     *
     * @return true: 控件有改变，需要notify到UI
     *   false: 控件无改变，不用更新UI
     */
    public boolean updateHeader() {
        if (mRightHeaderView.getText().equals(mContext.getString(R.string.edit))) {
            if (mFragment.getResultList().size()>0) {
                mFragment.setBottomVisible(true);
                mFragment.showAllItemCheckedView(true);
                mRightHeaderView.setText(mContext.getString(R.string.completed));
            }
//            else {
//                mFragment.setBottomVisible(false);
//                mRightHeaderView.setText("");
//            }
            return true;

        }else if (mRightHeaderView.getText().equals(mContext.getString(R.string.completed))) {
            mFragment.setBottomVisible(false);
            mFragment.showAllItemCheckedView(false);
            mRightHeaderView.setText(mContext.getString(R.string.edit));
            return true;
        }
        return false;

    }



    private void initHeader() {

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.back_advanced);

        mMiddleHeaderView.setText(mContext.getString(R.string.title_security_msg));
//        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);



        setOnClickRight(new OnRightListener() {

            @Override
            public void onClick() {
                boolean bRes = updateHeader();
                if (bRes) {

                    mFragment.notifyDataChanged();
                }

            }
        });
    }

    private void initViewPager() {

        mViewPager=(ViewPager) findViewById(R.id.viewPager);

        fragmentList=new ArrayList<Fragment>();
//        mAppListFragment=new AppListFragment();


        fragmentList.add(mFragment);

        mViewPager.setAdapter(new MyFrageStatePagerAdapter(getSupportFragmentManager()));

//        android.app.FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager
//                .beginTransaction();
//        mFragment = new MsgListFragment();
//        fragmentTransaction.add(R.id.fragment_container, mFragment);
//        fragmentTransaction.commit();

    }

    /**
     * 定义自己的ViewPager适配器。
     * 也可以使用FragmentPagerAdapter。关于这两者之间的区别，可以自己去搜一下。
     */
    class MyFrageStatePagerAdapter extends FragmentStatePagerAdapter
    {

        public MyFrageStatePagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        /**
         * 每次更新完成ViewPager的内容后，调用该接口，此处复写主要是为了让导航按钮上层的覆盖层能够动态的移动
         */
//        @Override
//        public void finishUpdate(ViewGroup container)
//        {
//            super.finishUpdate(container);//这句话要放在最前面，否则会报错
//            //获取当前的视图是位于ViewGroup的第几个位置，用来更新对应的覆盖层所在的位置
//            int currentItem=mViewPager.getCurrentItem();
//            if (currentItem==currenttab)
//            {
//                return ;
//            }
//            imageMove(mViewPager.getCurrentItem());
//            currenttab=mViewPager.getCurrentItem();
//        }

    }

    ////关于事件的， 貌似没有用呀？
    public void onEventMainThread(MessageEvent event)
    {
        switch (event.type)
        {
            case MessageEvent.Event_OnlineState:
                boolean online=event.params.getBoolean("online");
                if (!online) {
//                    Toast.makeText(mContext, "onEventMainThread not online", Toast.LENGTH_SHORT).show();
//					sbOnline.setImageResource(R.drawable.rootblock_icon_online_selected);
//					tvOnline.setText("离线");
                }
                else {
//                    Toast.makeText(mContext,"onEventMainThread online",Toast.LENGTH_SHORT).show();
//					sbOnline.setImageResource(R.drawable.rootblock_icon_online);
//					tvOnline.setText("在线");
                }
            default:
                break;
        }
    }

    //注册广播
    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(REFRESH_MAIN_ACTIVITY);
        myIntentFilter.addAction(GlobalConsts.NEW_MESSAGE);
        // 注册广播
        getApplicationContext().registerReceiver(
                mBroadcastReceiver, myIntentFilter);
    }

    public void unregisterBoradcastReceiver() {
        // 反注册广播
        getApplicationContext().unregisterReceiver(
                mBroadcastReceiver);
    }

    public final static String REFRESH_MAIN_ACTIVITY = "refresh_main_activity";
    //广播接收者
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            QDLog.i(TAG,"mBroadcastReceiver ==========" + action);
            if (action.equals(REFRESH_MAIN_ACTIVITY)) {
//                appListSharedPreferences = app.getSharedPreferences(app
//                                .getCheckAccount().getCurrentAccount() + "appList",
//                        0);
//                String pageNo = "";
//                pageNo += String.valueOf(vpMain.getCurrentItem() + 1);
//                pageNo += "/";
//                pageNo += String.valueOf(views.size());
//                tvPage.setText(pageNo);
//                if (getActivity() instanceof NewMainActivity) {
//                    ((NewMainActivity) getActivity()).checkPolicy();
//                }
            }
            else if(action.equals(GlobalConsts.NEW_MESSAGE)){
//                refreshHandler.sendMessage(Message.obtain());

//				refreshFragment();
//                adapter.notifyDataSetChanged();
//                String pageNo = "";
//                pageNo += String.valueOf(vpMain.getCurrentItem() + 1);
//                pageNo += "/";
//                pageNo += String.valueOf(views.size());
//                tvPage.setText(pageNo);
            }
        }

    };
}
