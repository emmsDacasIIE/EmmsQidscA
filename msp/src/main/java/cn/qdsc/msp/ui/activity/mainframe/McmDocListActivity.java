package cn.qdsc.msp.ui.activity.mainframe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.qdsc.msp.R;
import cn.qdsc.msp.business.BusinessListener;
import cn.qdsc.msp.controller.ControllerListener;
import cn.qdsc.msp.controller.McmController;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.core.mcm.FileOpener;
import cn.qdsc.msp.manager.AddressManager;
import cn.qdsc.msp.model.McmDocInfoModel;
import cn.qdsc.msp.ui.activity.base.BaseSlidingFragmentActivity;
import cn.qdsc.msp.ui.base.CommonAdapter;
import cn.qdsc.msp.ui.base.CommonViewHolder;
import cn.qdsc.msp.ui.qdlayout.PopMenu;
import cn.qdsc.msp.ui.qdlayout.QdProgressDialog;
import cn.qdsc.msp.ui.qdlayout.RefreshableView;
import cn.qdsc.msp.ui.qdlayout.SearchView;
import cn.qdsc.msp.util.BitMapUtil;
import cn.qdsc.msp.util.FileEngine;
import cn.qdsc.msp.util.QDLog;
import cn.qdsc.msp.util.SdcardManager;
import cn.qdsc.msp.util.SimpleDateUtil;
import cn.qdsc.msp.webservice.download.DownLoadFileFromUrl;
import cn.qdsc.msp.webservice.download.DownloadDataInfo;
import cn.qdsc.msp.webservice.download.DownloadFileThread;
import cn.qdsc.msp.webservice.download.MyDownloadListener;

//import android.widget.RelativeLayout;

/**
 * 主页的Activity
 * 参考资料：SwitchFragment
 */
//        SlidingFragmentActivity
//BaseFragmentActivity

public class McmDocListActivity extends BaseSlidingFragmentActivity implements ControllerListener, SearchView.SearchViewListener {
    private static final String TAG = "DocListFragment";

    private PopMenu popMenu;



    //////////////////////about search///////////
    /**
     * 搜索view
     */
    private SearchView searchView;

    /**
     * 搜索结果列表adapter
     */
    private SearchAdapter mSearchAdapter;

    //////////////////////end search///////////

    private RefreshableView refreshableView;
    private SwipeMenuListView docsListView = null;
    private TextView noTextView = null;

    DisplayImageOptions options;

    //全局的数据，搜索时，从这个数据中收索。
    private List<McmDocInfoModel> allDocsList = new ArrayList<McmDocInfoModel>();

    //doclist,与view相关连，通过设置adapter
    private ArrayList<McmDocInfoModel> docsArrayList = new ArrayList<McmDocInfoModel>();

    McmController mMcmController = null;

    //下载用
    DownLoadFileFromUrl mDownLoadFileFromUrl = null;

    public static enum EDocFileStatus {
        Downloaded,
        No_Download,
        Fav,
        All
    }



    @Override
    public void OnNotify(BusinessListener.BusinessResultCode resCode, BusinessListener.BusinessType type, Object data1, Object data2) {
        refreshableView.finishRefreshing();
        noTextView.setVisibility(View.GONE);
        switch (resCode) {
            //请求OK
            case ResultCode_Sucess:
                switch (type) {
                    case BusinessType_DocList:
                        if (data1 != null) {
                            List<McmDocInfoModel> currentList = (List<McmDocInfoModel>) data1;

                            //考虑在线程中执行，因为全是数据库操作lizy
                            if (currentList == null || currentList.size() <= 0) {
                                docsArrayList.clear();
                                allDocsList.clear();
                                showDocList(true);
                            }else {
                                UpdateDocList2DB(currentList);
                                docsArrayList.clear();
                                allDocsList.clear();
                                List<McmDocInfoModel> allDbList =  loadAdapterData();
                                filterDataFromDb(allDbList,currentList);
                                showDocList(true);
                            }


                            return;
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                //response错误
                showDocList(false);
        }
    }

    @Override
    public void onRefreshAutoComplete(String text) {
        QDLog.i(TAG, "onRefreshAutoComplete===========" + text);
    }

    @Override
    public void onSearch(String text) {
        QDLog.i(TAG, "onSearch===========" + text);
        getResultData(text);
    }

    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Image_Text_Image;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_doc_list, "");

        //初始化header
        initHeader();
        initView();
        initPopMenu();

        initAdapter();

        mMcmController = new McmController(this, this);
//        mFileOpener = new FileOpener(mContext);
//        mDownLoadFileFromUrl = new DownLoadFileFromUrl(mContext, MyDownloadListener.Download_Type.Type_Doc, this);
        mDownLoadFileFromUrl = new DownLoadFileFromUrl(mContext);


        refreshHandler.sendMessage(Message.obtain());

    }

    @Override
    public void onPause() {
        QDLog.i(TAG, "onPause===============================111======");

        //中断下载线程
        DownLoadFileFromUrl.removeAllDownThread();
        QDLog.i(TAG, "onPause===============================1112======");

        super.onPause();
    }

    private Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {


            switch (msg.what) {
                case DownLoadFileFromUrl.DOWNLOADING:
                    if (progressDialog!=null) {
                        if (msg.arg1>=100) msg.arg1=99;
                        progressDialog.setProgress(msg.arg1);
                    }
                    break;
                case DownLoadFileFromUrl.DOWNLOAD_STOP:
                    if (progressDialog!=null) progressDialog.dismiss();
                    Toast.makeText(mContext,"下载失败",Toast.LENGTH_SHORT).show();
                    break;
                case DownLoadFileFromUrl.DOWNLOAD_FINISH:
                    progressDialog.setMessage("下载完成");
                    progressDialog.setProgress(100);
                    mSearchAdapter.notifyDataSetChanged();
                    break;
                default:
                    getDocListFromServer();
                    break;
            }

        }
    };

    private void initHeader() {

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);

        mMiddleHeaderView.setText(mContext.getString(R.string.title_security_doc));
//        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);

//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(true);
        mRightHeaderView.setImageView(R.mipmap.msp_titlebar_doc_icon);
    }

    void initView() {

        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

        //真正用到的layout的定义

        searchView = (SearchView)findViewById(R.id.main_search_layout);

        noTextView = (TextView) findViewById(R.id.noAppText);

        //左滑动menu
//        refreshableView = (PullToRefreshSwipMenuListView) findViewById(R.id.pullToRefresh);
//        docsListView = (SwipeMenuListView) refreshableView.getRefreshableView();

        refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
        docsListView = (SwipeMenuListView) findViewById(R.id.pullDocsListView);


        //注册左滑动menu
        registerForContextMenu(docsListView);

        //设置事件监听
        searchView.setSearchViewListener(this);

        noTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                refreshHandler.sendMessage(Message.obtain());
            }
        });

        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {

            @Override
            public void onRefresh() {
                refreshHandler.sendMessage(Message.obtain());
            }
        }, 0x1003);

        noTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                refreshHandler.sendMessage(Message.obtain());
            }
        });

        /** 设置为false， 下拉加载就不显示了*/
//        refreshableView.setPullToRefreshEnabled(true);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.ic_stub)
                .showImageForEmptyUri(R.mipmap.ic_empty)
                .showImageOnFail(R.mipmap.ic_error).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).build();

    }

    void initAdapter() {

        //非常重要，设置adapter
        mSearchAdapter = new SearchAdapter(this, docsArrayList, R.layout.msp_list_item_app1);
        docsListView.setAdapter(mSearchAdapter);

//        setPllupListener();
        createSwipMenu();

    }

    /**
     * 用最新的接口,获取应用列表
     */
    private void getDocListFromServer() {

        if (null == AddressManager.getAddrWebservice()) {
//            mProgressDlg.dismiss();
            return;
        }
        mMcmController.FetchDocList();
    }

    /**
     * @param isNetworkOK 网络是否OK
     */
    private void showDocList(boolean isNetworkOK) {
        int length = docsArrayList.size();
        if (length <= 0) {
            noTextView.setVisibility(View.VISIBLE);
            if (isNetworkOK) {
                noTextView.setText(getString(R.string.file_list_nodata));
            }
            else {
                noTextView.setText(getString(R.string.file_list_load_fail));
            }
            refreshableView.setVisibility(View.GONE);
            return;
        } else {
            noTextView.setVisibility(View.GONE);
            refreshableView.setVisibility(View.VISIBLE);
        }
        mSearchAdapter.notifyDataSetChanged();
    }

    private void initPopMenu() {
        String[] arr = this.getResources().getStringArray(R.array.doc_menu);
        popMenu = new PopMenu(this,arr);

        //pop menu event
        popMenu.setOnItemClickListener(new PopMenu.OnItemClickListener() {

            @Override
            public void onItemClick(int index) {

                switch (index) {
                    case 0:
                        //All
//                        Toast.makeText(mContext,"all",Toast.LENGTH_SHORT).show();
                        getResultDataByStatus(index);

                        break;
                    case 1:
                        //
//                        Toast.makeText(mContext,"已下载",Toast.LENGTH_SHORT).show();
                        getResultDataByStatus(index);
                        break;
                    case 2:
//                        Toast.makeText(mContext, "未下载", Toast.LENGTH_SHORT).show();
                        getResultDataByStatus(index);
                        break;
                    case 3:
//                        Toast.makeText(mContext,"已收藏",Toast.LENGTH_SHORT).show();
                        getResultDataByStatus(index);
                        break;
                    default:
                        break;
                }
            }

        });

        setOnClickRight("", new OnRightListener() {
            @Override
            public void onClick() {
                popMenu.showAsDropDown(mRightHeaderView);
            }
        });
    }
//==============adapter=================


    public class SearchAdapter extends CommonAdapter<McmDocInfoModel> {

        public SearchAdapter(Context context, List<McmDocInfoModel> data, int layoutId) {
            super(context, data, layoutId);
        }

        //需要将model中的check等ui的值也写上。
        @Override
        public void convert(final CommonViewHolder holder, int position) {
            QDLog.i(TAG, "SearchAdapter convert==============000000000000===position========" + position);

            final McmDocInfoModel docItem = mDatas.get(position);

            holder.setText(R.id.textview_title_name, docItem.fileName); //title
//            holder.setText(R.id.textview_minor_title, docItem.fileRecvTime); //thirdtitle
//            holder.setImageResource(R.id.imageview_left, R.mipmap.doc_audio);
            int resId = getImageviewResId(docItem.fileName);
            holder.setImageResource(R.id.imageview_left, resId); // left icon
//            holder.setBackgroundResource(R.id.imageview_left, resId); // left icon

            long recvTime = SimpleDateUtil.convert2long(docItem.fileRecvTime,
                    SimpleDateUtil.DATE_FORMAT);
            String timeStr = resetUpdateAtValue(updatedDocAtValue(recvTime));
            holder.setText(R.id.textview_minor_title, timeStr);
            int status = EmmClientApplication.mSecureContainer.getFileState(docItem.fileName);

            holder.setText(R.id.textview_minor_title, "");
            if (status==1) {
                holder.setButtonText(R.id.right_btn, "打开");
                String sdcardPath = SdcardManager.getSdcardPath();
                if (sdcardPath!=null) {
                    String filePath = sdcardPath + "/msp/" + mContext.getApplicationContext().getPackageName() + File.separator + docItem.fileName;
                    File file = new File(filePath);
                    if (file.exists()) {
                        long fileLen = file.length();
                        String fileLenStr = FileEngine.formatLen(fileLen);
                        holder.setText(R.id.textview_minor_title, ""+fileLenStr);
                    }
                }
//                holder.setVisible(R.id.btn_listitem_delete,true);
            }
            else {
                holder.setButtonText(R.id.right_btn, "下载");
//                holder.setVisible(R.id.btn_listitem_delete, false);
            }

            //是否收藏
            if (docItem.fav == 0) {
                //未收藏
                holder.setChecked(R.id.checkbox_fav, false);
//
            } else {
                //收藏
                holder.setChecked(R.id.checkbox_fav, true);
            }

            //gone
            holder.setVisible(R.id.textview_mid_title, false); //subtitle
            holder.setVisible(R.id.itemCount, false); //itemCount
//            holder.setVisible(R.id.chevron, false); //subtitle
/*
            holder.setOnClickListener(R.id.btn_listitem_delete, new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    // 构造对话框
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("删除");
                    builder.setMessage("确定删除?");
                    // 更新
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                EmmClientApplication.mSecureContainer.delete(docItem.fileName);
                                mSearchAdapter.notifyDataSetChanged();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            finally {
                                dialog.dismiss();
                            }

                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    Dialog noticeDialog = builder.create();

                    noticeDialog.getWindow().setType(
                            (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    noticeDialog.show();
                }
            });
*/
            //set onClickEvent
            holder.setOnClickListener(R.id.right_btn, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Button stateText = (Button) v.findViewById(R.id.right_btn);
                    String state = stateText.getText().toString();
                    QDLog.i(TAG, "convert=============state =" + state);

                    if (state.equals("下载") ) {

                        try {
                            //String url = AddressManager.getAddrFile(2)+"/" + URLEncoder.encode(docItem.fileName, "UTF-8");
                            showNoticeDialog(docItem.fileName,docItem.url);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (state.equals("打开")) {
                        QDLog.i(TAG, "===========fileName=====" + docItem.fileName);
                        FileEngine.openCipherFile(mContext, docItem.fileName);
                    }
                }
            });

            //收藏事件

            final CheckBox checkBox = holder.getView(R.id.checkbox_fav);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //考虑： 数据库如果更新成功，则UI才改变，否则，UI不变；
                    //从体验来讲，可以是，UI更新，即使数据库不变。

                    String favStr = "0";
                    if (isChecked) {
                        favStr = "1";
                    }
                    //更新数据库
//                    boolean bRes = EmmClientApplication.mDatabaseEngine.updateDocList(docItem.fileId, docItem.fileName, docItem.url, docItem.fileRecvTime, favStr);
                    boolean bRes = EmmClientApplication.mDatabaseEngine.updateDocItem(docItem.fileName, "y", docItem.fileRecvTime, docItem.fileId, favStr);
                    QDLog.i(TAG, "========onCheckedChanged====bRes======" + bRes);
                    if (bRes) {
                        //checkBox.setChecked(isChecked);
                        for (McmDocInfoModel model : docsArrayList) {
                            if (model.fileId == docItem.fileId) {
                                model.fav = Integer.parseInt(favStr);
                                break;
                            }
                        }
                        mSearchAdapter.notifyDataSetChanged();
                    }else {
                        Toast.makeText(mContext,"收藏失败!",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        public int getImageviewResId(String filename) {
            if (FileOpener.checkEndsWithInStringArray(filename, mContext
                    .getResources().getStringArray(R.array.fileEndingImage))) {
                return R.mipmap.image;
//                holder.image.setBackgroundResource(R.drawable.doc_img);
            } else if (FileOpener.checkEndsWithInStringArray(filename, mContext
                    .getResources().getStringArray(R.array.fileEndingWebText))) {
                return R.mipmap.txt;
//                holder.image.setBackgroundResource(R.mipmap.doc_html);
            } else if (FileOpener.checkEndsWithInStringArray(filename, mContext
                    .getResources().getStringArray(R.array.fileEndingPackage))) {
                return R.mipmap.zip;

//                holder.image.setBackgroundResource(R.mipmap.doc_rar);
            } else if (FileOpener.checkEndsWithInStringArray(filename, mContext
                    .getResources().getStringArray(R.array.fileEndingAudio))) {
                return R.mipmap.audio;
//                holder.image.setBackgroundResource(R.mipmap.doc_audio);
            } else if (FileOpener.checkEndsWithInStringArray(filename, mContext
                    .getResources().getStringArray(R.array.fileEndingVideo))) {
                return R.mipmap.video;
//                holder.image.setBackgroundResource(R.mipmap.doc_video);
            } else if (FileOpener.checkEndsWithInStringArray(filename, mContext
                    .getResources().getStringArray(R.array.fileEndingText))) {
                return R.mipmap.txt;
//                holder.image.setBackgroundResource(R.mipmap.doc_txt);
            } else if (FileOpener.checkEndsWithInStringArray(filename, mContext
                    .getResources().getStringArray(R.array.fileEndingPdf))) {
                return R.mipmap.pdf;
//                holder.image.setBackgroundResource(R.mipmap.doc_pdf);
            } else if (FileOpener.checkEndsWithInStringArray(filename, mContext
                    .getResources().getStringArray(R.array.fileEndingWord))) {
                return R.mipmap.word;
//                holder.image.setBackgroundResource(R.mipmap.doc_word);
            } else if (FileOpener.checkEndsWithInStringArray(filename, mContext
                    .getResources().getStringArray(R.array.fileEndingExcel))) {
                return R.mipmap.xls;
//                holder.image.setBackgroundResource(R.mipmap.doc_excel);
            } else if (FileOpener.checkEndsWithInStringArray(filename, mContext
                    .getResources().getStringArray(R.array.fileEndingPPT))) {
                return R.mipmap.ppt;
//                holder.image.setBackgroundResource(R.mipmap.doc_ppt);
            }else if (FileOpener.checkEndsWithInStringArray(filename, mContext
                    .getResources().getStringArray(R.array.fileEndingApk))) {
                return R.mipmap.apk;
//                holder.image.setBackgroundResource(R.mipmap.doc_ppt);
            } else {
                return R.mipmap.txt;
//                holder.image.setBackgroundResource(R.mipmap.doc_txt);
            }
        }
    }

    ///////////时间的显示///////////
    /**
     * 一分钟的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_MINUTE = 60 * 1000;

    /**
     * 一小时的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_HOUR = 60 * ONE_MINUTE;

    /**
     * 一天的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_DAY = 24 * ONE_HOUR;

    /**
     * 一月的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_MONTH = 30 * ONE_DAY;

    /**
     * 一年的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_YEAR = 12 * ONE_MONTH;

    /**
     * 上次更新时间的字符串常量，用于作为SharedPreferences的键值
     */

    /**
     * 刷新下拉头中上次更新时间的文字描述。
     */
    private String updatedDocAtValue(long lastTime) {
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastTime;
        long timeIntoFormat;
        String updateAtValue;
        if (timePassed < 0) {
            updateAtValue = getResources().getString(R.string.time_unknow_error);
        } else if (timePassed < ONE_HOUR) {
            timeIntoFormat = timePassed / ONE_MINUTE;
            String value = timeIntoFormat + mContext.getString(R.string.time_minute);
            updateAtValue = String.format(getResources().getString(R.string.time_befor), value);
        } else if (timePassed < ONE_DAY) {
            timeIntoFormat = timePassed / ONE_HOUR;
            String value = timeIntoFormat + mContext.getString(R.string.time_hour);
            updateAtValue = String.format(getResources().getString(R.string.time_befor), value);
        } else if (timePassed < ONE_MONTH) {
            timeIntoFormat = timePassed / ONE_DAY;
            String value = timeIntoFormat + mContext.getString(R.string.time_day);
            updateAtValue = String.format(getResources().getString(R.string.time_befor), value);
        } else if (timePassed < ONE_YEAR) {
            timeIntoFormat = timePassed / ONE_MONTH;
            String value = timeIntoFormat + mContext.getString(R.string.time_month);
            updateAtValue = String.format(getResources().getString(R.string.time_befor), value);
        } else {
            timeIntoFormat = timePassed / ONE_YEAR;
            String value = timeIntoFormat + mContext.getString(R.string.time_year);
            updateAtValue = String.format(getResources().getString(R.string.time_befor), value);
        }
        return updateAtValue;
    }

    //格式化
    private String resetUpdateAtValue(String updateAtValue) {
        int len = updateAtValue.length();
        String s = updateAtValue;
        if (len <12) {
            for (;len <12;len++) {
                s = s+" ";
            }
        }
        return s;
    }

    class MyDownloadListenerImp implements MyDownloadListener {
        String fileName;

        public MyDownloadListenerImp(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void onDownloadStatus(Download_Status status, int progress, DownloadDataInfo dataInfo) {
            QDLog.i(TAG, "DownloadDataInfo-==000000===m.fileName=====" + dataInfo.fileName);
            QDLog.i(TAG, "DownloadDataInfo-==000000===progress=====" + progress);
            QDLog.i(TAG, "DownloadDataInfo-==000000===status=====" + status);

            boolean needUpdateList = false;
            for (int i = 0; i < docsArrayList.size(); i++) {
                McmDocInfoModel m = docsArrayList.get(i);
                QDLog.i(TAG, "DownloadDataInfo-=m.fileName========11111111==m.fileName=====" + m.fileName);
                QDLog.i(TAG, "DownloadDataInfo-=dataInfo.fileName=1111111===dataInfo.fileName=====" + dataInfo.fileName);
                if (dataInfo.fileName.contains(m.fileName) ) {
                    QDLog.i(TAG, "==============================================================have file name=====");
                    switch (status) {
                        case Downloading:
                            m.progress = progress;
                            needUpdateList = true;
                            break;
                        case Stop:
                            if (progress == -1) {
                                Toast.makeText(mContext, "网络连接失败!", Toast.LENGTH_SHORT).show();
                                m.progress = progress;
                                needUpdateList = true;
                            }else  if (progress == -2){
                                Toast.makeText(mContext,"找不到要下载的文件!",Toast.LENGTH_SHORT).show();
                                m.progress = progress;
                                needUpdateList = true;
                            }else  if (progress == -3){
                                Toast.makeText(mContext,"下载失败！!",Toast.LENGTH_SHORT).show();
                                m.progress = progress;
                                needUpdateList = true;
                            }else {
                                needUpdateList = true;
                            }

                            break;
                        case Finished:
                            boolean bRes = EmmClientApplication.mDatabaseEngine.updateDocItem(m.fileName, "y", m.fileRecvTime, m.fileId, ""+m.fav);
                            m.progress = progress;
                            needUpdateList = true;
                            break;
                        case FirstDownload:
                            //写长度到数据库中
                            if (m.len<=0) { //说明len在数据库中对应的行中的值为0
                                m.len = progress;
                                boolean updateRes = EmmClientApplication.mDatabaseEngine.updateDocItemLenth(m.fileName, "y", m.fileRecvTime, m.fileId, "" + m.len);
                                needUpdateList = true;
                            }


                        default:
                            break;
                    }
                    break;
                }

            }//end for

            if (needUpdateList) {

                mSearchAdapter.notifyDataSetChanged();
//            notifyDataChange(DocListFragment.ACTION_REFRESH_DOC);
            }

            return;

        }

        @Override
        public String getFileName() {
            return fileName;
        }
    }

    /////////////////
//    private void setPllupListener() {
//        refreshableView
//                .setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
//                    @Override
//                    public void onRefresh(
//                            PullToRefreshBase<ListView> refreshView) {
//                        // TODO Auto-generated method stub
//                        refreshableView.onRefreshComplete();
//                        QDLog.i(TAG, "URL+刷新==================");
//                        refreshHandler.sendMessage(Message.obtain());
//
//                    }
//                });
//
//        refreshableView
//                .setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
//                    @Override
//                    public void onLastItemVisible() {
//                        QDLog.i(TAG, "setOnLastItemVisibleListener==xxxxx======");
////                        Toast.makeText(mContext, "上拉刷新！", Toast.LENGTH_SHORT).show();
////
//                    }
//                });
//    }


    //左滑动menu的创建
    ///////////////////////swip menu /////
    private void createSwipMenu() {
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
//                SwipeMenuItem openItem = new SwipeMenuItem(mContext.getApplicationContext());
//                // set item background
//                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
//                        0xCE)));
//                // set item width
//
//                openItem.setWidth(BitMapUtil.dp2px(mContext, 70));
//                // set item title
//                openItem.setTitle("Open");
//                // set item title fontsize
//                openItem.setTitleSize(18);
//                // set item title font color
//                openItem.setTitleColor(Color.WHITE);
//                // add to menu
//                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        mContext.getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(BitMapUtil.dp2px(mContext, 70));
                // set a icon
                deleteItem.setTitle("删 除");
                deleteItem.setTitleSize(22);
                deleteItem.setTitleColor(Color.WHITE);
//                deleteItem.setIcon(R.mipmap.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        docsListView.setMenuCreator(creator);

        // step 2. listener item click event
        docsListView
                .setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(int position,
                                                   SwipeMenu menu, int index) {
                        McmDocInfoModel item = docsArrayList.get(position);
                        switch (index) {
                            case 0:
                                // delete
//                                Toast.makeText(mContext,"open",Toast.LENGTH_SHORT).show();
                                int res = EmmClientApplication.mSecureContainer.getFileState(allDocsList.get(position).fileName);
                                if (res == 1 ) {
                                    showDeleteDialog(item, position);
                                }else {
                                    Toast.makeText(mContext,"该文档还没有下载！",Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 1:
                                // delete
//                                showDeleteDialog(item,position);
//                                docsArrayList.remove(position);
//                                mSearchAdapter.notifyDataSetChanged();

                                break;
                        }
                        return false;
                    }
                });

        // set SwipeListener
        docsListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        // other setting
        // listView.setCloseInterpolator(new BounceInterpolator());

        docsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
//				Toast.makeText(getApplicationContext(),
//						position + " onItemClick 2:", Toast.LENGTH_LONG).show();
                String item = (String)arg0.getItemAtPosition(position);
                //FishResultModel item = modelFishList.get(position);
//                GetFishDetailByPid(item);
//                Toast.makeText(mContext.getApplicationContext(),	position + "  click", Toast.LENGTH_SHORT).show();
            }
        });

        // test item long click
        docsListView
                .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent,
                                                   View view, int position, long id) {
//                        Toast.makeText(mContext.getApplicationContext(),
//                                position + " long click", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

    } //end menu


    //////需要接口调用的函数////

    //from DocListFragment的getDocListFromServer
    private void UpdateDocList2DB(List<McmDocInfoModel> modelList) {
//
//        if (modelList != null && modelList.size() <= 0) {
//            return;
//        }

        List<String> idList = new ArrayList<String>();

        for (int i = 0; i < modelList.size(); i++) {
            idList.add(modelList.get(i).fileId);
            McmDocInfoModel m = modelList.get(i);

            // 判断文件是否有更新
            Cursor cursor = EmmClientApplication.mDatabaseEngine.getDocListCursor(m.fileId, m.fileRecvTime);
            if (cursor == null || cursor.getCount() <= 0) {
                //没有更新，其实这里执行的是插入
                EmmClientApplication.mDatabaseEngine.updateDocList(m.fileId, m.fileName, m.url, m.fileRecvTime);
            }
        }

        String[] columns = new String[]{"sender"};
        Cursor cursor = EmmClientApplication.mDatabaseEngine.getDocAllItem(columns);
        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                String cid = cursor.getString(cursor.getColumnIndex("sender"));
                if (!idList.contains(cid)) {
                    //数据库中有数据，但是，本地获取的列表没有，就从数据库中删除掉这个数据。
                    EmmClientApplication.mDatabaseEngine.deleteDocItem(
                            cid);
                }
            }
        }
    }

    private List<McmDocInfoModel> loadAdapterData() {
        // TODO 从数据库初始化数据
        // R.drawable.universal_user
        List<McmDocInfoModel> allDbList = new ArrayList<>();

        Cursor mCursor = EmmClientApplication.mDatabaseEngine.getDocAllItem(null);

        if ((mCursor != null) && (mCursor.getCount() > 0)) {
            mCursor.moveToFirst();
            int fileTagIdx = mCursor.getColumnIndexOrThrow("filetag");
            int pathIdx = mCursor.getColumnIndexOrThrow("path");
            int timeIdx = mCursor.getColumnIndexOrThrow("time");
            int isNativeIdx = mCursor.getColumnIndexOrThrow("isnative");
            int urlIdx = mCursor.getColumnIndexOrThrow("url");
            int senderIdx = mCursor.getColumnIndexOrThrow("sender");
            int favIdx = mCursor.getColumnIndexOrThrow("fav");
            int lenIdx = mCursor.getColumnIndexOrThrow("len");
            do {
                String time = mCursor.getString(timeIdx);
                allDbList
                        .add(new McmDocInfoModel(mCursor.getString(senderIdx),
                                mCursor.getString(fileTagIdx), time,
                                mCursor.getString(isNativeIdx),
                                mCursor.getString(urlIdx),
                                mCursor.getString(pathIdx),
                                mCursor.getInt(favIdx),
                                mCursor.getInt(lenIdx)));

            } while (mCursor.moveToNext());
            mCursor.close();
        }
        return allDbList;
    }

    private void filterDataFromDb(List<McmDocInfoModel> allDbList,List<McmDocInfoModel> tempList) {
        if (allDbList == null || allDbList.size() <= 0) {
            allDocsList.addAll(tempList);
            docsArrayList.addAll(tempList);
            return;
        }
        for (int i = 0; i< allDbList.size();i++ ){
            for(McmDocInfoModel tempModel:tempList ) {
                if (allDbList.get(i).fileId.equals(tempModel.fileId)) {
                    docsArrayList.add(allDbList.get(i));
                    allDocsList.add(allDbList.get(i));
                    break;
                }
            }
        }

    }

    ////////////////////////为了搜索//////////////////////////////
    private void getResultData(String text) {
        if (allDocsList.size() <= 0) {
            return;
        }
        if (TextUtils.isEmpty(text)) {
            docsArrayList.clear();
            docsArrayList.addAll(allDocsList);
        } else {
            docsArrayList.clear();
            for (int i = 0; i < allDocsList.size(); i++) {
                if (allDocsList.get(i).fileName.toLowerCase().contains(text.trim().toLowerCase())) {
                    docsArrayList.add(allDocsList.get(i));
                } else {
                    //do nothing
                }
            }
        }

        mSearchAdapter.notifyDataSetChanged();
    }

    public void getResultDataByStatus(int status) {
        if (allDocsList.size() <= 0) {
            return;
        }
        switch (status) {
            case 0:
                docsArrayList.clear();
                docsArrayList.addAll(allDocsList);
                break;
            case 1:
            case 2:
            case 3:
                docsArrayList.clear();
                for (int i = 0; i < allDocsList.size(); i++) {
                    int res = EmmClientApplication.mSecureContainer.getFileState(allDocsList.get(i).fileName);
                    if (status ==1 &&  res == 1) {
                        docsArrayList.add(allDocsList.get(i));
                        continue;
                    }
                    if (status ==2 &&  res == 0) {
                        docsArrayList.add(allDocsList.get(i));
                        continue;
                    }
                    if (status ==3 &&  allDocsList.get(i).fav == 1) {
                        docsArrayList.add(allDocsList.get(i));
                        continue;
                    }
                }
                break;
        }
        mSearchAdapter.notifyDataSetChanged();
    }

    ///////////////下载////////////////////
    private QdProgressDialog progressDialog;
    private DownloadDataInfo  mDownloadDataInfo;

    /**
     * fileNameWithTime:
     *    为了显示文件名
     * 显示软件更新对话框
     */
    private void showNoticeDialog(final String fileName,final String url)
    {
        // 构造对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.file_download_title);
        builder.setMessage(mContext.getString(R.string.file_download_info)+":"+fileName);
        // 更新
        builder.setPositiveButton(R.string.app_donwload_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                progressDialog=new QdProgressDialog(mContext);
                progressDialog.show();
                progressDialog.setOnCancleLisener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressDialog.dismiss();
                        mDownloadDataInfo.cancle=true;
                    }
                });
                progressDialog.setTitle("下载");
                progressDialog.setMessage("正在下载" + fileName);
                mDownloadDataInfo=new DownloadDataInfo(MyDownloadListener.Download_Type.Type_Doc,fileName,url);
                DownloadFileThread thread = new DownloadFileThread(mContext,mDownloadDataInfo,refreshHandler);
                thread.start();
            }
        });
        // 稍后更新
        builder.setNegativeButton(R.string.app_download_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();

        noticeDialog.getWindow().setType(
                (WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        noticeDialog.show();
    }


    ////底部菜单的实现

    private Button mPromptTitleButton;
    private Button mConfirmButton;
    private Button mCancelButton;

    private Dialog mPopupMenuDialog;

    private void showDeleteDialog(final McmDocInfoModel model, final int pos) {
        View view = this.getLayoutInflater().inflate(R.layout.photo_choose_dialog, null);
        initBottomMenu(view,pos);
        mPopupMenuDialog = new Dialog(mContext, R.style.transparentFrameWindowStyle);
        mPopupMenuDialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = mPopupMenuDialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = this.getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // 设置显示位置
        mPopupMenuDialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        mPopupMenuDialog.setCanceledOnTouchOutside(true);
        mPopupMenuDialog.show();
    }



    private void  initBottomMenu(View v,final int pos) {
        mPromptTitleButton = (Button) v.findViewById(R.id.button_bottom_menu_takePic);
        mConfirmButton = (Button) v.findViewById(R.id.button_bottom_menu_choicePic);
        mCancelButton = (Button) v.findViewById(R.id.button_bottom_menu_cancel);

        mPromptTitleButton.setText(R.string.doc_delete_confirm_prompt);
        mConfirmButton.setText(R.string.msg_delete_confirm);
        mCancelButton.setText(R.string.msg_delete_cancel);

        mPromptTitleButton.setEnabled(false);
        mPromptTitleButton.setTextColor(mContext.getResources().getColor(R.color.msg_prompt_title_color));

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    EmmClientApplication.mSecureContainer.delete(docsArrayList.get(pos).fileName);
                    docsArrayList.remove(pos);
                    mSearchAdapter.notifyDataSetChanged();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext,"删除文件失败！",Toast.LENGTH_SHORT).show();
                }


                if (mPopupMenuDialog != null && mPopupMenuDialog.isShowing()) {
                    mPopupMenuDialog.dismiss();
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupMenuDialog != null && mPopupMenuDialog.isShowing()) {
                    mPopupMenuDialog.dismiss();
                }
            }
        });
    }



}
