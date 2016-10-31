package cn.qdsc.msp.ui.activity.mainframe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.qdsc.msp.R;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.core.mam.AppManager;
import cn.qdsc.msp.core.update.UpdateManager;
import cn.qdsc.msp.event.MessageEvent;
import cn.qdsc.msp.model.MamAppInfoModel;
import cn.qdsc.msp.model.UserModel;
import cn.qdsc.msp.ui.activity.base.BaseSlidingFragmentActivity;
import cn.qdsc.msp.ui.fragment.HomeFragment;
import cn.qdsc.msp.ui.fragment.MenuLeftFragment;
import cn.qdsc.msp.ui.fragment.OnMainPageChangedListener;
import cn.qdsc.msp.ui.qdlayout.PopMenu;
import cn.qdsc.msp.util.BitMapUtil;
import cn.qdsc.msp.util.NetworkUtils;
import cn.qdsc.msp.util.PrefUtils;
import cn.qdsc.msp.util.QDLog;
import cn.qdsc.msp.util.QdCamera;
import cn.qdsc.msp.webservice.QdWebService;
import de.greenrobot.event.EventBus;

//import android.widget.RelativeLayout;
/**
 * 主页的Activity
 * 参考资料：SwitchFragment
 */
//SlidingFragmentActivity
//BaseFragmentActivity

public class NewMainActivity extends BaseSlidingFragmentActivity implements OnMainPageChangedListener {

    private static final String TAG = "NewMainActivity";

    private static boolean isFirstCreated = true;
    private static final int Handler_Flag_StartActivity = 1;
    private static final int Handler_Flag_Timer = 2;
    private static final int Handler_Flag_ExitActivity = 3;
    private static int UpdateLockTwice = 0;

    LinearLayout layout_info_network;
    /**
     * 作为页面容器的ViewPager
     */
    ViewPager mViewPager;
    /**
     * 页面集合
     */
    List<HomeFragment> fragmentList;

    String emailStr;

    private PopMenu popMenu;

    Timer timer = new Timer( );

    private ArrayList<MamAppInfoModel> buildInApps;


    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Image_Text_Image_Image;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmain, "");

        //初始化header
        initHeader();

        // 初始化SlideMenu
        initRightMenu();

        // 初始化ViewPager
        initViewPager();

        initOnClickEvent();

        //initPopMenu();

        emailStr =  getIntent().getStringExtra("email");

        timer.schedule(mTimerTask, 700, 700);

        final UpdateManager manager = new UpdateManager(NewMainActivity.this);
        // 检查软件更新
        manager.checkUpdate();

        //初次加载展现合规页面
        if (NewMainActivity.isFirstCreated) {
            gotoComplianceActivity();
            isFirstCreated = false;
        }

        layout_info_network=(LinearLayout)findViewById(R.id.layout_info_netwrok);
        if (NetworkUtils.isConnected(mContext))
            layout_info_network.setVisibility(View.GONE);
        else
            layout_info_network.setVisibility(View.VISIBLE);
        EventBus.getDefault().register(this);
        //获取用户信息
        QdWebService.getUserInformation(new Response.Listener<UserModel>() {
            @Override
            public void onResponse(UserModel response) {
                EmmClientApplication.mUserModel=response;
            }
        },null);

        buildInApps=new ArrayList<>();
        ArrayList<MamAppInfoModel> installedList=AppManager.getInstalledApps(mContext,true);
        for (MamAppInfoModel app:installedList) {
            //system app
            if (app.appType==1) {
                if (app.appName.equals("计算器")||app.appName.equals("日历")||
                        app.appName.equals("时钟")||app.appName.equals("相机"))
                    buildInApps.add(app);
            }
        }

//        WindowManager windowManager = getWindowManager();
//        Display display = windowManager.getDefaultDisplay();
//        int screenWidth = display.getWidth();
//        int screenHeight =  display.getHeight();
//        Toast.makeText(mContext,screenHeight+"*"+screenWidth,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        setHeadImage();
        showApppList();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveAppList();
    }

    private void showApppList() {
        QdWebService.getAppList(new Response.Listener<ArrayList<MamAppInfoModel>>() {
            @Override
            public void onResponse(ArrayList<MamAppInfoModel> response) {
                //重新排序appList
                if (response == null)
                    response = new ArrayList<>();
                for (MamAppInfoModel app:buildInApps)
                    response.add(0,app);
                ArrayList<MamAppInfoModel> savedList = PrefUtils.getApplist();
                ArrayList<MamAppInfoModel> newList = reorderAppList(response, savedList);
                //将重排列的list存储到本地
                PrefUtils.putAppList(newList);
                refreshMainPage(newList);
                mViewPager.setAdapter(new MyFrageStatePagerAdapter(getSupportFragmentManager()));
            }
        }, null);
    }

    private void saveAppList() {
        //获取重新排列的showList
        ArrayList<MamAppInfoModel> showList=new ArrayList<>();
        for (HomeFragment f:fragmentList) {
            for (Object obj: f.getAdapter().getItems()) {
                MamAppInfoModel app=(MamAppInfoModel)obj;
                showList.add(app);
            }
        }
        //重新排列list
        ArrayList<MamAppInfoModel> savedList =PrefUtils.getApplist();
        ArrayList<MamAppInfoModel> newList=reorderAppList(savedList, showList);
        PrefUtils.putAppList(newList);
    }

    private void refreshMainPage(ArrayList<MamAppInfoModel> newList) {
        fragmentList.clear();
        //获取list中并且已经安装的应用 && Web应用
        ArrayList<MamAppInfoModel> showList = new ArrayList<>();

        for (MamAppInfoModel app : newList) {
            if (app.isWeb())
                showList.add(app);
            else if (AppManager.checkInstallResult(NewMainActivity.this, app.pkgName)) {
                showList.add(app);
            }
        }
        HomeFragment fragment=null;
        ArrayList<MamAppInfoModel> pageList=null;
        int idx=0;
        for (int i=0;i<showList.size();i++) {
            if (i%HomeFragment.Max_Apps_Count==0) {
                if (fragment!=null && pageList!=null)
                    fragment.setContent(idx,pageList,NewMainActivity.this);
                fragment=new HomeFragment();
                pageList=new ArrayList<>();
                fragmentList.add(fragment);
            }
            pageList.add(showList.get(i));
        }
        if (fragment!=null) {
            fragment.setContent(idx, pageList, NewMainActivity.this);
        }
    }

    private ArrayList<MamAppInfoModel> reorderAppList(ArrayList<MamAppInfoModel> sourceList,ArrayList<MamAppInfoModel> orderList) {
        ArrayList<MamAppInfoModel> newList=new ArrayList<>();
        for (MamAppInfoModel app : orderList) {
            if (sourceList.contains(app)) {
                newList.add(app);
                sourceList.remove(app);
            }
        }
        for (MamAppInfoModel app:sourceList) {
                newList.add(app);
        }
        return newList;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void onDestroy ( ) {

        if (timer != null) {

            timer.cancel( );

            timer = null;
        }
        super.onDestroy( );

    }

    private void gotoComplianceActivity() {
        //mContext.startActivity(new Intent(mContext, ComplianceActivity.class));
    }

    ////handler///
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Handler_Flag_StartActivity) {
                gotoComplianceActivity();
            }else if (Handler_Flag_ExitActivity == msg.what) {
                List<String> homeList = AppManager.getHomes(mContext);
                for (int i = 0; i< homeList.size();i++) {
                    String pkg = homeList.get(i);
                    AppManager.startAPP(mContext,pkg);
                    break;
                }
                finish();
            }

            else {
                //update right image
                //updateRightImage();
            }

        }
    };

    ////timer///
    TimerTask mTimerTask = new TimerTask(){
        public void run() {
            Message message = new Message();
            message.what = Handler_Flag_Timer;
            myHandler.sendMessage(message);
        }
    };

    private void updateRightImage() {
        if (UpdateLockTwice <=10000000)  {
            if (UpdateLockTwice %2 == 0) {
                mRightHeaderView.setImageView(R.mipmap.msp_lock_icon);
            }else {
                mRightHeaderView.setImageView(R.mipmap.msp_not_lock_icon);
            }
            UpdateLockTwice++;
        }else {
            UpdateLockTwice = 0;
        }
    }

    /////head image////
    private void  setHeadImage() {
        String headphotoName = QdCamera.GetHeadFullPathName(mContext);
        if (!TextUtils.isEmpty(headphotoName)) {
            File f = new File(headphotoName);
            if ( f.exists())
                setImageView(headphotoName);
        }
    }

    private void setImageView(String realPath) {
        QDLog.i(TAG, "setImageView ===================path======" + realPath);
        BitMapUtil.setRoundImageSrc(mContext,mLeftHeaderView.getImageView(), realPath);
//        BitMapUtil.setImageSrc(mLeftHeaderView.getImageView(), realPath);
    }


    private void initHeader() {
        mLeftHeaderView.setImageView(R.mipmap.msp_user);

        mMiddleHeaderView.setText(mContext.getString(R.string.security_work_area));
        mRightHeaderView.setImageView(R.mipmap.msp_titlebar_right_icon);
        mRightHeaderView.setVisibility(View.GONE);

        mSubRightHeaderView.setVisibility(View.GONE);
        mSubRightHeaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoComplianceActivity();
            }
        });
        setOnClickLeft("", true, new OnLeftListener() {
            @Override
            public void onClick() {
                showLeftMenu();
            }
        });
    }

    private void initRightMenu()
    {
        Fragment leftMenuFragment = new MenuLeftFragment();

        //非常重要
        setBehindContentView(R.layout.left_menu_frame);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_left_menu_frame, leftMenuFragment).commit();
        SlidingMenu menu = getSlidingMenu();
        menu.setMode(SlidingMenu.LEFT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
//		menu.setBehindWidth()
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);
        // menu.setBehindScrollScale(1.0f);
        menu.setSecondaryShadowDrawable(R.drawable.shadow);
        //设置右边（二级）侧滑菜单
        // menu.setSecondaryMenu(R.layout.right_menu_frame);

    }

    public void showLeftMenu()
    {
        getSlidingMenu().showMenu();
    }

    public void showRightMenu(View view)
    {
        getSlidingMenu().showSecondaryMenu();
    }

    private void initViewPager() {
        mViewPager=(ViewPager) findViewById(R.id.viewPager);
        fragmentList=new ArrayList<HomeFragment>();
    }

    /**
     * 底部按键的事件处理
     */
    private void initOnClickEvent() {
        setmBottomLayoutVisibility(View.VISIBLE);
        SetOnClickBottomItemListener(new OnClickBottomItemListener() {
            @Override
            public void onClickBottomItem(Enum_ItemId itemId) {
                switch (itemId) {
                    case Item_App:
                        Intent intent = new Intent(NewMainActivity.this,
                                MamAppListActivity.class);
                        startActivity(intent);

                        break;
                    case Item_Doc:
                        Intent intent2 = new Intent(NewMainActivity.this,
                                McmDocListActivity.class);  //    //XfuncListFragment


                        startActivity(intent2);

                        break;
                    case Item_Contacts:
                        Intent intent3 = new Intent(NewMainActivity.this,
                                McmContactsActivity.class);
                        startActivity(intent3);

                        break;
                    case Item_Push:
                        Intent intent4 = new Intent(NewMainActivity.this,
                                MdmMsgListActivity.class);
                        startActivity(intent4);
                        break;
                }
            }
        });
    }

    @Override
    public void onLeftPage(int idx, MamAppInfoModel model) {
        if (idx<=0) return;
        try {
            MamAppInfoModel modelToExchange=(MamAppInfoModel)(fragmentList.get(idx-1).getAdapter().getItem(0));
            fragmentList.get(idx).getAdapter().remove(model);
            fragmentList.get(idx).getAdapter().add(modelToExchange);
            fragmentList.get(idx-1).getAdapter().remove(modelToExchange);
            fragmentList.get(idx-1).getAdapter().add(model);
            mViewPager.setCurrentItem(idx-1,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRightPage(int idx, MamAppInfoModel model) {
        if (idx>=fragmentList.size()-1) return;
        try {
            MamAppInfoModel modelToExchange=(MamAppInfoModel)(fragmentList.get(idx+1).getAdapter().getItem(0));
            fragmentList.get(idx).getAdapter().remove(model);
            fragmentList.get(idx).getAdapter().add(modelToExchange);
            fragmentList.get(idx+1).getAdapter().remove(modelToExchange);
            fragmentList.get(idx+1).getAdapter().add(model);
            mViewPager.setCurrentItem(idx+1,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

//    /**
//     * 移动覆盖层
//     * @param moveToTab 目标Tab，也就是要移动到的导航选项按钮的位置
//     * 第一个导航按钮对应0，第二个对应1，以此类推
//     */
//    private void imageMove(int moveToTab)
//    {
//        int startPosition=0;
//        int movetoPosition=0;
//
//        startPosition=currenttab*(screenWidth/4);
//        movetoPosition=moveToTab*(screenWidth/4);
//        //平移动画
////        TranslateAnimation translateAnimation=new TranslateAnimation(startPosition,movetoPosition, 0, 0);
////        translateAnimation.setFillAfter(true);
////        translateAnimation.setDuration(200);
////        imageviewOvertab.startAnimation(translateAnimation);
//    }

//    //手动设置ViewPager要显示的视图
//    private void changeView(int desTab)
//    {
//        mViewPager.setCurrentItem(desTab, true);
//    }

    /**
     * 双击back退出
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // this.moveTaskToBack(true);
//            exitBy2Click(); // 调用双击退出函数
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private static Boolean isExit = false;

    private void exitBy2Click() {
        // TODO Auto-generated method stub
        Timer tExit = null;
        if (!isExit) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    System.gc();

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = new Message();
                    msg.what = Handler_Flag_ExitActivity;
                    myHandler.sendMessage(msg);
                }
            }).start();

            //System.exit(0);
        }
    }

    //////popup menu /////
    private void initPopMenu() {
        setOnClickRight("", new OnRightListener() {
            @Override
            public void onClick() {
                popMenu = new PopMenu(NewMainActivity.this,PrefUtils.getSecurityRecords());
                popMenu.showAsDropDown(mRightHeaderView);
            }
        });
    }


    ////网络状态
    public void onEventMainThread(MessageEvent event)
    {
        switch (event.type)
        {
            case MessageEvent.Event_OnlineState:
                boolean online=event.params.getBoolean("online");
                if (online) {
                    layout_info_network.setVisibility(View.GONE);
                }
                else {
                    layout_info_network.setVisibility(View.VISIBLE);
                }
            default:
                break;
        }
    }

}
