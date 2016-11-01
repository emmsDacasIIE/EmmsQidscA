//package cn.qdsc.msp.ui.fragment;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.app.ProgressDialog;
//import android.content.*;
//import android.database.Cursor;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler;
//import android.os.Message;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.WindowManager;
//import android.widget.*;
//import cn.qdsc.msp.R;
//import cn.qdsc.msp.webservice.download.DownLoadFileFromUrl;
//import cn.qdsc.msp.webservice.download.DownloadDataInfo;
//import cn.qdsc.msp.webservice.download.MyDownloadListener;
//import cn.qdsc.msp.https.BusinessListener;
//import cn.qdsc.msp.webservice.qdvolley.UpdateTokenRequest;
//import cn.qdsc.msp.controller.ControllerListener;
//import cn.qdsc.msp.controller.McmController;
//import cn.qdsc.msp.core.mcm.FileOpener;
//import cn.qdsc.msp.core.EmmClientApplication;
//import cn.qdsc.msp.model.McmDocInfoModel;
//import cn.qdsc.msp.ui.activity.base.BaseSlidingFragmentActivity;
//import cn.qdsc.msp.ui.base.CommonAdapter;
//import cn.qdsc.msp.ui.base.CommonViewHolder;
//import cn.qdsc.msp.ui.qdlayout.CustomDialog;
//import cn.qdsc.msp.ui.qdlayout.PopMenu;
//import cn.qdsc.msp.ui.qdlayout.SearchView;
//import cn.qdsc.msp.util.*;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.baoyz.swipemenulistview.SwipeMenu;
//import com.baoyz.swipemenulistview.SwipeMenuCreator;
//import com.baoyz.swipemenulistview.SwipeMenuItem;
//import com.baoyz.swipemenulistview.SwipeMenuListView;
//import com.handmark.pulltorefresh.library.PullToRefreshBase;
//import com.handmark.pulltorefresh.library.PullToRefreshSwipMenuListView;
//import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * A placeholder fragment containing a simple view.
// */
//public class XfuncListFragment extends BaseSlidingFragmentActivity implements ControllerListener, MyDownloadListener, SearchView.SearchViewListener {
//    private static final String TAG = "XfuncListFragment";
//    // 下载完成修改状态
//    public static final String ACTION_REFRESH_DOC = "cn.qdsc.msp.mam.REFRESH_DOC";
//
////    private SharedPreferences settings = null;
//
//    private BroadcastReceiver mRefreshReceiver;
//
//    //////////////////////about search///////////
//    /**
//     * 搜索view
//     */
//    private SearchView searchView;
//
//    /**
//     * 搜索结果列表adapter
//     */
//    private SearchAdapter mSearchAdapter;
//
//    //////////////////////end search///////////
//
//
////    private RefreshableView refreshableView;
//    private PullToRefreshSwipMenuListView refreshableView;
//    private SwipeMenuListView docsListView;
//
////    private ListView docsListView = null;
//    private TextView noTextView = null;
//
//    private View rootView;
//
//    private ProgressDialog mProgressDlg;
//
//    private PopMenu popMenu;
//
////    DisplayImageOptions options;
//
//    //全局的数据，搜索时，从这个数据中收索。
//    private List<McmDocInfoModel> allDocsList = new ArrayList<McmDocInfoModel>();
//
//    //doclist,与view相关连，通过设置adapter
//    private ArrayList<McmDocInfoModel> docsArrayList = new ArrayList<McmDocInfoModel>();
//
//    McmController mMcmController = null;
//
//    FileOpener mFileOpener;
//
//    //下载用
//    DownLoadFileFromUrl mDownLoadFileFromUrl = null;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_doclist, "");
////        mContext = getActivity();
//
//        initView();
//
//        mContext.registerReceiver(mRefreshReceiver,
//                new IntentFilter(XfuncListFragment.ACTION_REFRESH_DOC));
//
//        mMcmController = new McmController(mContext, this);
//        mFileOpener = new FileOpener(mContext);
//        mDownLoadFileFromUrl = new DownLoadFileFromUrl(mContext, Download_Type.Type_Doc, this);
//    }
//
////    @Override
////    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
////        rootView = inflater.inflate(R.layout.fragment_xfunc_list, container,
////                false);
////
////
////
////
//////        docsListView = (ListView) rootView.findViewById(R.id.pushAppsListView);
////
////		searchView = (SearchView)rootView.findViewById(R.id.main_search_layout);
////        //设置监听
////        searchView.setSearchViewListener(this);
////
////        noTextView = (TextView) rootView.findViewById(R.id.noAppText);
////
////
////        //下拉刷新
////        refreshableView = (PullToRefreshSwipMenuListView)rootView.findViewById(R.id.pullToRefresh);
////        docsListView = (SwipeMenuListView)refreshableView
////                .getRefreshableView();
////
////        registerForContextMenu(docsListView);
////
////        refreshableView.setPullToRefreshEnabled(true);
////
////
//////        refreshableView = (RefreshableView) rootView.findViewById(R.id.refreshable_view);
////
//////        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
//////
//////            @Override
//////            public void onRefresh() {
//////                refreshHandler.sendMessage(Message.obtain());
////////                refreshableView.finishRefreshing();
//////            }
//////        }, 0x1003);
////
////        noTextView.setOnClickListener(new OnClickListener() {
////
////            @Override
////            public void onClick(View v) {
////                // TODO Auto-generated method stub
////                refreshHandler.sendMessage(Message.obtain());
////            }
////        });
//////
//////        options = new DisplayImageOptions.Builder()
//////                .showImageOnLoading(R.mipmap.ic_stub)
//////                .showImageForEmptyUri(R.mipmap.ic_empty)
//////                .showImageOnFail(R.mipmap.ic_error).cacheInMemory(true)
//////                .cacheOnDisk(true).considerExifParams(true).build();
////
////        mRefreshReceiver = new BroadcastReceiver() {
////
////            @Override
////            public void onReceive(Context context, Intent intent) {
////                // TODO Auto-generated method stub
////                refreshHandler.sendMessage(Message.obtain());
////            }
////        };
////        this.getActivity().registerReceiver(mRefreshReceiver,
////                new IntentFilter(XfuncListFragment.ACTION_REFRESH_DOC));
////
////        //非常重要，设置adapter
////        mSearchAdapter = new SearchAdapter(this.getActivity(), docsArrayList, R.layout.msp_list_item_app1);
//////        docsListView.setAdapter(mSearchAdapter);
////        docsListView.setAdapter(mSearchAdapter);
////        setPllupListener();
////        createSwipMenu();
////
////
////        refreshHandler.sendMessage(Message.obtain());
////        return rootView;
////    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        mContext.unregisterReceiver(mRefreshReceiver);
//        super.onDestroy();
//    }
//
//
//
//    //////////init view/////////////////////
//
//    private void initPopMenu() {
//        popMenu = new PopMenu(this);
//        String[] arr = this.getResources().getStringArray(R.array.about_menu);
//        popMenu.addItems(arr);
//
//        //pop menu event
//        popMenu.setOnItemClickListener(new PopMenu.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(int index) {
//
//                switch (index) {
//                    case 0:
//                        //All
//
//                        Toast.makeText(mContext,"all",Toast.LENGTH_SHORT).show();
//                        break;
//                    case 1:
//                        //
//                        Toast.makeText(mContext,"已下载",Toast.LENGTH_SHORT).show();
//                        break;
//                    case 2:
//                        Toast.makeText(mContext, "未下载", Toast.LENGTH_SHORT).show();
//                        break;
//                    case 3:
//                        Toast.makeText(mContext,"已收藏",Toast.LENGTH_SHORT).show();
//                        break;
////                    case 4:
////                        //login logout
////                        //LoginOrOut();
////
////                        //for test shouchang
////                        //GetMyfavourites();
////                        startMyfavouritesActivity();
////                        break;
////
////                    case 5:
////                        //login logout
////                        LoginOrOut();
////                        break;
//
//                    default:
//                        break;
//                }
//
//
//            }
//
//        });
//
//        setOnClickRight("", new OnRightListener() {
//            @Override
//            public void onClick() {
//                popMenu.showAsDropDown(mRightHeaderView);
//            }
//        });
//    }
//
//    private void initHeader() {
//
//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
//        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);
//
//        mMiddleHeaderView.setText(mContext.getString(R.string.security_doc));
//        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);
//
//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(true);
//        mRightHeaderView.setImageView(R.mipmap.msp_titlebar_doc_icon);
//
//
//
////        setOnClickLeft("", true, new OnLeftListener() {
////            @Override
////            public void onClick() {
////                showLeftMenu();
////            }
////        });
//    }
//
//    private void initBody() {
//        searchView = (SearchView)findViewById(R.id.main_search_layout);
//        //设置监听
//        searchView.setSearchViewListener(this);
//
//        noTextView = (TextView) findViewById(R.id.noAppText);
//
//
//        //下拉刷新
//        refreshableView = (PullToRefreshSwipMenuListView)findViewById(R.id.pullToRefresh);
//        docsListView = (SwipeMenuListView)refreshableView
//                .getRefreshableView();
//
//        registerForContextMenu(docsListView);
//
//        refreshableView.setPullToRefreshEnabled(true);
//
//        noTextView.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                refreshHandler.sendMessage(Message.obtain());
//            }
//        });
//
//        mRefreshReceiver = new BroadcastReceiver() {
//
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                // TODO Auto-generated method stub
//                refreshHandler.sendMessage(Message.obtain());
//            }
//        };
//
//        //非常重要，设置adapter
//        mSearchAdapter = new SearchAdapter(mContext,docsArrayList, R.layout.msp_list_item_app1);
////        docsListView.setAdapter(mSearchAdapter);
//        docsListView.setAdapter(mSearchAdapter);
//        setPllupListener();
//        createSwipMenu();
//
//        refreshHandler.sendMessage(Message.obtain());
//
//    }
//    private void initView() {
//        //初始化header
//        initHeader();
//
//        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
//        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
//
//        // 初始化SlideMenu
////        initRightMenu();
//
//        initPopMenu();
//
//        initBody();
//
//    }
//
//    private Handler refreshHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            mProgressDlg = ProgressDialog.show(mContext, "加载中",
//                    "正在刷新文档列表...", true, false);
////            getAppListFromServer();
//
//            getDocListFromServer();
//        }
//    };
//
//    /**
//     * 用最新的接口,获取应用列表
//     */
//    private void getDocListFromServer() {
//
//        if (null == NetworkDef.getAddrWebservice()) {
//            mProgressDlg.dismiss();
//            return;
//        }
//        mMcmController.FetchDocList();
//    }
//
//    /**
//     * @param isNetworkOK 网络是否OK
//     */
//    private void showDocList(boolean isNetworkOK) {
//        int length = docsArrayList.size();
//        if (length <= 0) {
//            noTextView.setVisibility(View.VISIBLE);
//            if (isNetworkOK) {
//                noTextView.setText("文档为空，点击刷新！");
//            } else {
//                noTextView.setText("文档加载失败，点击刷新！");
//            }
////            refreshableView.setVisibility(View.GONE);
//            return;
//        } else {
//            noTextView.setVisibility(View.GONE);
////            refreshableView.setVisibility(View.VISIBLE);
//        }
//        mSearchAdapter.notifyDataSetChanged();
//    }
//
//    ////////////////////controller请求返回的结果////////
//    @Override
//    public void OnNotify(BusinessListener.BusinessResultCode resCode, BusinessListener.BusinessType type, Object data1, Object data2) {
//
//        mProgressDlg.dismiss();
////        refreshableView.finishRefreshing();
//        refreshableView.onRefreshComplete();
//        switch (resCode) {
//            //请求OK
//            case ResultCode_Sucess:
//                switch (type) {
//                    case BusinessType_DocList:
//                        if (data1 != null) {
//                            List<McmDocInfoModel> currentList = (List<McmDocInfoModel>) data1;
//
//                            //考虑在线程中执行，因为全是数据库操作lizhongyi
//                            UpdateDocList(currentList);
//                            loadAdapterData();
//                            showDocList(true);
//                            return;
//                        }
//                        break;
//                    default:
//                        break;
//                }
//                break;
//            default:
//                //response错误
////                showToastMsg(resCode,type);
//                showDocList(false);
//        }
//
//    }
//
//    //////需要接口调用的函数////
//
//    //from DocListFragment的getDocListFromServer
//    private void UpdateDocList(List<McmDocInfoModel> modelList) {
//
//        if (modelList != null && modelList.size() <= 0) {
//            return;
//        }
//
//        List<String> idList = new ArrayList<String>();
//
//        for (int i = 0; i < modelList.size(); i++) {
//            idList.add(modelList.get(i).fileId);
//            McmDocInfoModel m = modelList.get(i);
//
//            // 判断文件是否有更新
//            Cursor cursor = EmmClientApplication.mDatabaseEngine.getDocListCursor(m.fileId, m.fileRecvTime);
//            if (cursor == null || cursor.getCount() <= 0) {
//                //需要改，lizy
//                String fav = "0";//未收藏
//                EmmClientApplication.mDatabaseEngine.updateDocList(m.fileId, m.fileName, m.url, m.fileRecvTime, fav);
//            }
//        }
//
//        String[] columns = new String[]{"sender"};
//        Cursor cursor = EmmClientApplication.mDatabaseEngine.getDocAllItem(columns);
//        if (cursor != null) {
//            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
//                    .moveToNext()) {
//                String cid = cursor.getString(cursor.getColumnIndex("sender"));
//                if (!idList.contains(cid)) {
//                    EmmClientApplication.mDatabaseEngine.deleteDocItem(
//                            cid);
//                }
//            }
//        }
//    }
//
//    private void loadAdapterData() {
//        // TODO 从数据库初始化数据
//        // R.drawable.universal_user
//        docsArrayList.clear();
//        allDocsList.clear();
//
//        Cursor mCursor = EmmClientApplication.mDatabaseEngine.getDocAllItem(null);
//
//        if ((mCursor != null) && (mCursor.getCount() > 0)) {
//            mCursor.moveToFirst();
//            int fileTagIdx = mCursor.getColumnIndexOrThrow("filetag");
//            int pathIdx = mCursor.getColumnIndexOrThrow("path");
//            int timeIdx = mCursor.getColumnIndexOrThrow("time");
//            int isNativeIdx = mCursor.getColumnIndexOrThrow("isnative");
//            int urlIdx = mCursor.getColumnIndexOrThrow("url");
//            int senderIdx = mCursor.getColumnIndexOrThrow("sender");
//            int favIdx = mCursor.getColumnIndexOrThrow("fav");
//            do {
//                String time = mCursor.getString(timeIdx);
//                docsArrayList
//                        .add(new McmDocInfoModel(mCursor.getString(senderIdx),
//                                mCursor.getString(fileTagIdx), time,
//                                mCursor.getString(isNativeIdx),
//                                mCursor.getString(urlIdx),
//                                mCursor.getString(pathIdx),
//                                mCursor.getString(favIdx)));
//                allDocsList
//                        .add(new McmDocInfoModel(mCursor.getString(senderIdx),
//                                mCursor.getString(fileTagIdx), time,
//                                mCursor.getString(isNativeIdx),
//                                mCursor.getString(urlIdx),
//                                mCursor.getString(pathIdx),
//                                mCursor.getString(favIdx)));
//            } while (mCursor.moveToNext());
//            mCursor.close();
//        }
//    }
//
//    ////由Download实现的接口///////
//
//    @Override
//    public void onDownloadStatus(Download_Status status, int progress, DownloadDataInfo dataInfo) {
//        switch (status) {
//            case Downloading:
//                // 显示下载对话框
//                //showDownloadDialog();
//                mProgress.setProgress(progress);
//                progressText.setText(progress + "%");
//                break;
//            case Stop:
//                mDownloadDialog.dismiss();
//                break;
//            case Finished:
//                QDLog.i(TAG, "onDownloadStatus -==================Finished");
//                mDownloadDialog.dismiss();
//                //更新数据库
//                QDLog.i(TAG, "DownloadDataInfo-==========" + dataInfo.toString());
//                QDLog.i(TAG, "DownloadDataInfo-===dataInfo.fileName=======" + dataInfo.fileName);
//
//                for (int i = 0; i < docsArrayList.size(); i++) {
//
//                    McmDocInfoModel m = docsArrayList.get(i);
//                    QDLog.i(TAG, "DownloadDataInfo-=====m.fileName=====" + m.fileName);
//                    if (dataInfo.fileName.contains(m.fileName)) {
//                        boolean bRes = EmmClientApplication.mDatabaseEngine.updateDocItem(m.fileName, "y", m.fileRecvTime, m.fileId, m.fav);
//                        QDLog.i(TAG, "DownloadDataInfo-=====222222222222222222222222222 bRes==" + bRes);
//                        break;
//                    }
//
//                }
//                notifyDataChange(XfuncListFragment.ACTION_REFRESH_DOC);
//                break;
//            default:
//                break;
//        }
//    }
//
//    //发送intent，重新从网络获取数据。
//    private void notifyDataChange(String action) {
//        Intent intent = new Intent();
//        intent.setAction(action);
//        this.mContext.sendBroadcast(intent);
//    }
//
//    /**
//     * 显示软件下载对话框
//     */
//
//    ProgressBar mProgress;
//    TextView progressText;
//    private Dialog mDownloadDialog;
//
//    private void showDownloadDialog(String fileName, String url) {
//        // 构造软件下载对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setTitle(mContext.getString(R.string.app_downloading) + ":" + fileName);
//        // 给下载对话框增加进度条
//        final LayoutInflater inflater = LayoutInflater.from(mContext);
//        View v = inflater.inflate(R.layout.appdownload_progress, null);
//        mProgress = (ProgressBar) v.findViewById(R.id.app_download_progress);
//        progressText = (TextView) v.findViewById(R.id.progressText);
//        builder.setView(v);
//        // 取消更新
//        builder.setNegativeButton(R.string.app_download_cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                // 设置取消状态
//                mDownLoadFileFromUrl.stopDownloadFile();
////                cancelDownload = true;
//            }
//        });
//        mDownloadDialog = builder.create();
//        mDownloadDialog.setCancelable(false);
//        mDownloadDialog.getWindow().setType(
//                (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
//        mDownloadDialog.show();
//
//        mDownLoadFileFromUrl.startDownloadFile(fileName, url);
//
//    }
//
//    /**
//     * fileNameWithTime:
//     * 为了显示文件名
//     * 显示软件更新对话框
//     */
//    private void showNoticeDialog(final String fileName, final String url) {
//        // 构造对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setTitle(R.string.file_download_title);
//        builder.setMessage(mContext.getString(R.string.file_download_info) + ":" + fileName);
//        // 更新
//        builder.setPositiveButton(R.string.app_donwload_btn, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                // 显示下载对话框
//                showDownloadDialog(fileName, url);
//            }
//        });
//        // 稍后更新
//        builder.setNegativeButton(R.string.app_download_later, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        Dialog noticeDialog = builder.create();
//
//        noticeDialog.getWindow().setType(
//                (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
//        noticeDialog.show();
//    }
//
//    ////////////////////////为了搜索//////////////////////////////
//
//    //    /**
////     * 获取搜索结果data和adapter
////     */
//    private void getResultData(String text) {
//        if (allDocsList.size() <= 0) {
//            return;
//        }
//        if (TextUtils.isEmpty(text)) {
//            docsArrayList.clear();
//            docsArrayList.addAll(allDocsList);
//        } else {
//            docsArrayList.clear();
//            for (int i = 0; i < allDocsList.size(); i++) {
//                if (allDocsList.get(i).fileName.contains(text.trim())) {
//                    docsArrayList.add(allDocsList.get(i));
//                } else {
//                    //do nothing
//                }
//            }
//        }
//
//        mSearchAdapter.notifyDataSetChanged();
//    }
//
//    /**
//     * 当搜索框 文本改变时 触发的回调 ,更新自动补全数据
//     *
//     * @param text
//     */
//    @Override
//    public void onRefreshAutoComplete(String text) {
//
//        QDLog.i(TAG, "onRefreshAutoComplete===========" + text);
//        //更新数据
////        getAutoCompleteData(text);
//    }
//
//    /**
//     * 点击搜索键时edit text触发的回调
//     *
//     * @param text
//     */
//    @Override
//    public void onSearch(String text) {
//        QDLog.i(TAG, "onSearch===========" + text);
//        Toast.makeText(mContext, "begin search...", Toast.LENGTH_SHORT).show();
//        getResultData(text);
//        Toast.makeText(mContext, "完成搜素", Toast.LENGTH_SHORT).show();
//
//    }
//
//    public class SearchAdapter extends CommonAdapter<McmDocInfoModel> {
//
//        public SearchAdapter(Context context, List<McmDocInfoModel> data, int layoutId) {
//            super(context, data, layoutId);
//        }
//
//        //需要将model中的check等ui的值也写上。
//        @Override
//        public void convert(final CommonViewHolder holder, int position) {
//            QDLog.i(TAG, "SearchAdapter convert==============000000000000===========");
//
//            final McmDocInfoModel docItem = mDatas.get(position);
//
//            holder.setText(R.id.textview_title_name, docItem.fileName); //title
//            holder.setText(R.id.textview_minor_title, docItem.fileRecvTime); //thirdtitle
//            holder.setImageResource(R.id.imageview_left, R.mipmap.doc_audio); // left icon
//            holder.setButtonText(R.id.right_btn, docItem.fileName); //right button
//            //holder.setImageResource(R.id.imageview_minor_fav, R.id.imageview_minor_fav); //right button
//
//            QDLog.i(TAG, "SearchAdapter convert=========111===" + docItem.fileName);
//            //right button
//            if (EmmClientApplication.mSecureContainer.exists(docItem.fileName)) {
//                holder.setButtonText(R.id.right_btn, "打开");
//            } else {
//                holder.setButtonText(R.id.right_btn, "下载");
//            }
//
//            //是否收藏
//            if (docItem.fav.equals("0")) {
//                //未收藏
//                holder.setChecked(R.id.checkbox_fav, false);
////
//            } else {
//                //收藏
//                holder.setChecked(R.id.checkbox_fav, true);
//            }
//
//            //gone
//            holder.setVisible(R.id.textview_mid_title, false); //subtitle
//            holder.setVisible(R.id.itemCount, false); //itemCount
//            holder.setVisible(R.id.chevron, false); //subtitle
//
//            //set onClickEvent
//            holder.setOnClickListener(R.id.right_btn, new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    TextView stateText = (TextView) v.findViewById(R.id.right_btn);
//                    String state = stateText.getText().toString();
//
//                    if (state.equals("下载")) {
//                        UpdateTokenRequest.update(UpdateTokenRequest.TokenType.USER, new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
//                                // 判断SD卡是否存在，并且是否具有读写权限
//                                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                                    // 获得存储卡的路径
//                                    String url = "https://" + NetworkDef.getAddrWebservice() + "/api/v1/user/docs/" +
//                                            docItem.fileId + "?uuid=" + PhoneInfoExtractor.getIMEI(mContext) +
//                                            "&access_token=" + response;
//                                    showNoticeDialog(docItem.fileName, url);
//                                } else {
//                                    Toast.makeText(mContext, "无sdcard", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        }, new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                Toast.makeText(mContext, "无法连接服务器", Toast.LENGTH_SHORT).show();
//                            }
//                        });
////                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//////                                        String url = "https://" + NetworkDef.getAddrWebservice() + "/api/v1/user/docs/" +
//////                                                docItem.fileId + "?uuid=" + PhoneInfoExtractor.getIMEI(mContext) +
//////                                                "&access_token=" + response;
////                            String url = "http://192.168.0.22:80/api/v1/user/docs/";
////                            showNoticeDialog(docItem.fileName, url);
////                        } else {
////                            Toast.makeText(mContext, "无sdcard", Toast.LENGTH_SHORT).show();
////                        }
//                    } else if (state.equals("打开")) {
//                        QDLog.i(TAG, "===========fileName=====" + docItem.fileName);
//                        FileEngine.openCipherFile(mContext, docItem.fileName);
//                    }
//                }
//
//            });
//
//            //收藏事件
//
//            final CheckBox checkBox = holder.getView(R.id.checkbox_fav);
//            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    //考虑： 数据库如果更新成功，则UI才改变，否则，UI不变；
//                    //从体验来讲，可以是，UI更新，即使数据库不变。
//
//                    String favStr = "0";
//                    if (isChecked) {
//                        favStr = "1";
//                    }
//                    //更新数据库
//                    boolean bRes = EmmClientApplication.mDatabaseEngine.updateDocList(docItem.fileId, docItem.fileName, docItem.url, docItem.fileRecvTime, favStr);
//                    QDLog.i(TAG, "========onCheckedChanged====bRes======" + bRes);
//                    if (bRes) {
//                        checkBox.setChecked(isChecked);
//                    }
//                }
//            });
//        }
//    }
//
//    ///////////////////////swip menu /////
//    private void createSwipMenu() {
//        // step 1. create a MenuCreator
//        SwipeMenuCreator creator = new SwipeMenuCreator() {
//
//            @Override
//            public void create(SwipeMenu menu) {
//                // create "open" item
//                SwipeMenuItem openItem = new SwipeMenuItem(
//                        mContext.getApplicationContext());
//                // set item background
//                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
//                        0xCE)));
//                // set item width
//                openItem.setWidth(BitMapUtil.dp2px(mContext, 90));
//                // set item title
//                openItem.setTitle("Open");
//                // set item title fontsize
//                openItem.setTitleSize(18);
//                // set item title font color
//                openItem.setTitleColor(Color.WHITE);
//                // add to menu
//                menu.addMenuItem(openItem);
//
//                // create "delete" item
//                SwipeMenuItem deleteItem = new SwipeMenuItem(
//                        mContext.getApplicationContext());
//                // set item background
//                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
//                        0x3F, 0x25)));
//                // set item width
//                deleteItem.setWidth(BitMapUtil.dp2px(mContext, 90));
//                // set a icon
//                deleteItem.setIcon(R.mipmap.ic_delete);
//                // add to menu
//                menu.addMenuItem(deleteItem);
//            }
//        };
//        // set creator
//        docsListView.setMenuCreator(creator);
//
//        // step 2. listener item click event
//        docsListView
//                .setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(int position,
//                                                   SwipeMenu menu, int index) {
//                        McmDocInfoModel item = docsArrayList.get(position);
//                        switch (index) {
//                            case 0:
//                                // open
////                                GetFishDetailByPid(item);
//                                break;
//                            case 1:
//                                // delete
//                                showDeleteDialog(item,position);
//                                // mAppList.remove(position);
//                                // mAdapter.notifyDataSetChanged();
//                                break;
//                        }
//                        return false;
//                    }
//                });
//
//        // set SwipeListener
//
//        docsListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
//
//            @Override
//            public void onSwipeStart(int position) {
//                // swipe start
//            }
//
//            @Override
//            public void onSwipeEnd(int position) {
//                // swipe end
//            }
//        });
//
//        // other setting
//        // listView.setCloseInterpolator(new BounceInterpolator());
//
//        docsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
//                                    long arg3) {
//                QDLog.i(TAG,"setOnItemClickListener=============");
////				Toast.makeText(getApplicationContext(),
////						position + " onItemClick 2:", Toast.LENGTH_LONG).show();
//                McmDocInfoModel item = (McmDocInfoModel)arg0.getItemAtPosition(position);
//                //FishResultModel item = modelFishList.get(position);
////                GetFishDetailByPid(item);
//            }
//        });
//
//
//
//        // test item long click
//        docsListView
//                .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//
//                    @Override
//                    public boolean onItemLongClick(AdapterView<?> parent,
//                                                   View view, int position, long id) {
////						Toast.makeText(getApplicationContext(),
////								position + " long click", 0).show();
//                        return false;
//                    }
//                });
//
//    }
//
//    ////////////////set refrensh event////
//    private void setPllupListener() {
//        refreshableView
//                .setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
//                    @Override
//                    public void onRefresh(
//                            PullToRefreshBase<ListView> refreshView) {
//                        // TODO Auto-generated method stub
//                        refreshableView.onRefreshComplete();
////                        morePrg.setVisibility(View.GONE);
//                        // GetMyFish();
//                        QDLog.i(TAG, "URL+刷新==================");
//                        QDLog.i(TAG, "URL+刷新==================");
//                        QDLog.i(TAG, "URL+刷新==================");
//                        refreshHandler.sendMessage(Message.obtain());
//                        // adapter = new NewsAdapter(MyFishListFA.this,
//                        // list);
//                        // newsListView.setAdapter(adapter);
//                        // i = 1;
//                        // mPullToRefreshListView.onRefreshComplete();
//
//                        // handler.post(new Runnable() {
//                        //
//                        // @Override
//                        // public void run() {
//                        // // TODO Auto-generated method stub
//                        // list = getNews(url);
//                        // Log.i("URL+刷新", url);
//                        // adapter = new NewsAdapter(MainActivity.this,
//                        // list);
//                        // newsListView.setAdapter(adapter);
//                        // i = 1;
//                        // mPullToRefreshListView.onRefreshComplete();
//                        //
//                        // }
//                        // });
//
//                    }
//                });
//
//        refreshableView
//                .setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
//                    @Override
//                    public void onLastItemVisible() {
//                        QDLog.i(TAG, "setOnLastItemVisibleListener========");
//
//
////                        if (pageIndex <= modelTotalPage) {
////                            morePrg.setVisibility(View.VISIBLE);
////                            GetMyFish(pageIndex,mType); // 加载更多数据
////                            //getfishComment();
////                        } else {
////                            morePrg.setVisibility(View.INVISIBLE);
////                            Toast.makeText(mContext, "数据全部加载完成！", Toast.LENGTH_SHORT).show();
////                        }
//                    }
//                });
//    }
//
//
//    ///////////show dialog//////
//    ////dialog for delete
//    CustomDialog dialog;
//    private void showDeleteDialog(final McmDocInfoModel model, final int pos) {
//
//        //将mLoginUser设置为null，意味着退出登录
//        //creatDialog(mContext);
//        dialog=new CustomDialog(mContext, R.style.mydeletestyle, R.layout.customdialog, new CustomDialog.CustomDialogListener() {
//
//            @Override
//            public void confirm() {
//                deleteDocItem(model, pos);
//                dialog.dismiss();
//                dialog = null;
//
//            }
//
//            @Override
//            public void cancel() {
//                dialog.dismiss();
//                dialog = null;
//            }
//
//            @Override
//            public String setTextviewText() {
//                return mContext.getString(R.string.delete_prompt_content);
//
//            }
//
//            @Override
//            public String setConfirmText() {
//                return mContext.getString(R.string._confirm_text);
//
//            }
//
//            @Override
//            public String setCancelText() {
//                return mContext.getString(R.string.cancel);
//
//            }
//
//        });
//        dialog.show();
//
//    }
//
//    private void deleteDocItem(final McmDocInfoModel model, final int pos) {
//
//        boolean bRes = EmmClientApplication.mDatabaseEngine.deleteDocItem(model.fileId);
//        if (bRes) {
//            docsArrayList.remove(pos);
//            loadAdapterData();
//        }
//
//    }
//}

package cn.dacas.emmclient.ui.fragment;
public class XfuncListFragment {
}