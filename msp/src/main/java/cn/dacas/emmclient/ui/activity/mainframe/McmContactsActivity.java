package cn.dacas.emmclient.ui.activity.mainframe;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.ui.fragment.AppListFragment;
import cn.dacas.emmclient.ui.fragment.ExpandableContactsFragment;

//import android.widget.RelativeLayout;

/**
 * 主页的Activity
 * 参考资料：SwitchFragment
 */
//        SlidingFragmentActivity
//BaseFragmentActivity

public class McmContactsActivity extends BaseSlidingFragmentActivity {


    /**
     * 作为页面容器的ViewPager
     */
    ViewPager mViewPager;
    /**
     * 页面集合
     */
    List<Fragment> fragmentList;

    Fragment fragment = ExpandableContactsFragment.newInstance(2);

    AppListFragment mAppListFragment = (AppListFragment) AppListFragment.newInstance(2);

    Handler handler;



    //覆盖层
//    ImageView imageviewOvertab;

    //屏幕宽度
//    int screenWidth;
    //当前选中的项
//    int currenttab=-1;

    @Override
    protected HearderView_Style setHeaderViewStyle() {
        return HearderView_Style.Image_Text_Null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applist, "");

        //初始化header
        initHeader();

        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

        // 初始化SlideMenu
//        initRightMenu();

        // 初始化ViewPager
        initViewPager();

//        initOnClickEvent();

    }

    private void initHeader() {

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.back_advanced);


        mMiddleHeaderView.setText(mContext.getString(R.string.title_security_contacts));
//        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);

//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(false);

//        setOnClickLeft("", true, new OnLeftListener() {
//            @Override
//            public void onClick() {
//                showLeftMenu();
//            }
//        });
    }

    private void initViewPager() {
        mViewPager=(ViewPager) findViewById(R.id.viewPager);

        fragmentList=new ArrayList<Fragment>();
//        mAppListFragment=new AppListFragment();


        fragmentList.add(mAppListFragment);



        mViewPager.setAdapter(new ContactSectionsPagerAdapter(getSupportFragmentManager()));

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initOnClickEvent() {
        setmBottomLayoutVisibility(View.VISIBLE);
        SetOnClickBottomItemListener(new OnClickBottomItemListener() {
            @Override
            public void onClickBottomItem(Enum_ItemId itemId) {
                switch (itemId) {
                    case Item_App:
                        Toast.makeText(mContext,"app item====1======", Toast.LENGTH_SHORT).show();
                        break;
                    case Item_Doc:
                        Toast.makeText(mContext,"Item_Doc item===2=======", Toast.LENGTH_SHORT).show();
                        break;
                    case Item_Contacts:
                        Toast.makeText(mContext,"Item_Contacts item===3=======", Toast.LENGTH_SHORT).show();
                        break;
                    case Item_Push:
                        Toast.makeText(mContext,"Item_Push item=====4=====", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    /**
     * 定义自己的ViewPager适配器。
     * 也可以使用FragmentPagerAdapter。关于这两者之间的区别，可以自己去搜一下。
     */

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class ContactSectionsPagerAdapter extends FragmentPagerAdapter {

        public ContactSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class
            // below).
            handler = ((ExpandableContactsFragment)fragment).getHandle();
//			Message msg = new Message();
//			msg.arg1 = 0;
//			handler.sendMessage(msg);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_corp_contacts_section)
                            .toUpperCase(l);
            }
            return null;
        }


    }
//
//    class MyFrageStatePagerAdapter extends FragmentStatePagerAdapter
//    {
//
//        public MyFrageStatePagerAdapter(FragmentManager fm)
//        {
//            super(fm);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            return fragmentList.get(position);
//        }
//
//        @Override
//        public int getCount() {
//            return fragmentList.size();
//        }
//
//        /**
//         * 每次更新完成ViewPager的内容后，调用该接口，此处复写主要是为了让导航按钮上层的覆盖层能够动态的移动
//         */
////        @Override
////        public void finishUpdate(ViewGroup container)
////        {
////            super.finishUpdate(container);//这句话要放在最前面，否则会报错
////            //获取当前的视图是位于ViewGroup的第几个位置，用来更新对应的覆盖层所在的位置
////            int currentItem=mViewPager.getCurrentItem();
////            if (currentItem==currenttab)
////            {
////                return ;
////            }
////            imageMove(mViewPager.getCurrentItem());
////            currenttab=mViewPager.getCurrentItem();
////        }
//
//    }
//
////    /**
////     * 移动覆盖层
////     * @param moveToTab 目标Tab，也就是要移动到的导航选项按钮的位置
////     * 第一个导航按钮对应0，第二个对应1，以此类推
////     */
////    private void imageMove(int moveToTab)
////    {
////        int startPosition=0;
////        int movetoPosition=0;
////
////        startPosition=currenttab*(screenWidth/4);
////        movetoPosition=moveToTab*(screenWidth/4);
////        //平移动画
//////        TranslateAnimation translateAnimation=new TranslateAnimation(startPosition,movetoPosition, 0, 0);
//////        translateAnimation.setFillAfter(true);
//////        translateAnimation.setDuration(200);
//////        imageviewOvertab.startAnimation(translateAnimation);
////    }
//
////    //手动设置ViewPager要显示的视图
////    private void changeView(int desTab)
////    {
////        mViewPager.setCurrentItem(desTab, true);
////    }

}
