package cn.dacas.emmclient.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.webservice.download.DownLoadFileFromUrl;
import cn.dacas.emmclient.webservice.download.DownloadDataInfo;
import cn.dacas.emmclient.webservice.download.DownloadFileThread;
import cn.dacas.emmclient.webservice.download.MyDownloadListener;
import cn.dacas.emmclient.core.mdm.PolicyContent;
import cn.dacas.emmclient.core.mdm.PolicyManager;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.model.MamAppInfoModel;
import cn.dacas.emmclient.ui.activity.mainframe.AppDetailActivity;
import cn.dacas.emmclient.ui.base.CommonAdapter;
import cn.dacas.emmclient.ui.base.CommonViewHolder;
import cn.dacas.emmclient.ui.qdlayout.QdProgressDialog;
import cn.dacas.emmclient.ui.qdlayout.RefreshableView;
import cn.dacas.emmclient.ui.qdlayout.SearchView;
import cn.dacas.emmclient.core.mam.AppManager;
import cn.dacas.emmclient.util.GlobalConsts;
import cn.dacas.emmclient.util.PhoneInfoExtractor;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.QDLog;
import cn.dacas.emmclient.webservice.QdWebService;

/**
 * A placeholder fragment containing a simple view.
 */
public class AppListFragment extends BaseFragment implements SearchView.SearchViewListener {
    private static final String TAG = "AppListFragment";
    // 下载完成修改状态
    public static final String ACTION_REFRESH_APPSTORE = "cn.dacas.emmclient.mam.REFRESH_APP_STORE";

    public static final int NECESSARY_APP = 0;
    public static final int OPTIONAL_APP = 1;
    public static final int BLACK_APP = 2;

    private static final int MSG_INSTALL_RESULT = 2;
    private static final int MSG_OPEN_RESULT = 3;

    private SharedPreferences settings = null;

    private BroadcastReceiver mRefreshReceiver;

    DownloadManager mDownloadManager;

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
    private ListView pushAppsListView = null;
    private TextView noAppText = null;

    private View rootView;

//    private ProgressDialog mProgressDlg;

    DisplayImageOptions options;

    //全局的数据，搜索时，从这个数据中收索。
    private List<MamAppInfoModel> allAppList = new ArrayList<MamAppInfoModel>();

    //applist,与adapter对应。
    private ArrayList<MamAppInfoModel> pushAppsArrayList = new ArrayList<MamAppInfoModel>();


    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static Fragment newInstance(int sectionNumber) {
        AppListFragment curFragment = new AppListFragment();
        Bundle args = new Bundle();

        // 这个number还有必要么？ lizhongyi
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        curFragment.setArguments(args);

        return curFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_app_list, container,
                false);

        settings = this.getActivity().getSharedPreferences(PrefUtils.PREF_NAME,
                0);

        pushAppsListView = (ListView) rootView.findViewById(R.id.pushAppsListView);

        searchView = (SearchView)rootView.findViewById(R.id.main_search_layout);
        //设置监听
        searchView.setSearchViewListener(this);


//        pushAppsListView.setAdapter(mAdapter);

        noAppText = (TextView) rootView.findViewById(R.id.noAppText);
        refreshableView = (RefreshableView) rootView.findViewById(R.id.refreshable_view);

        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {

            @Override
            public void onRefresh() {
                refreshHandler.sendMessage(Message.obtain());
//                refreshableView.finishRefreshing();
            }
        }, 0x1003);

        noAppText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                refreshHandler.sendMessage(Message.obtain());
            }
        });

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.ic_stub)
                .showImageForEmptyUri(R.mipmap.ic_empty)
                .showImageOnFail(R.mipmap.ic_error).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).build();

//        mDownloadManager = new DownloadManager(this.getActivity()
//                .getContentResolver(), this.getActivity().getPackageName());
//        startDownloadService();
        mRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                refreshHandler.sendMessage(Message.obtain());
            }
        };
        this.getActivity().registerReceiver(mRefreshReceiver,
                new IntentFilter(AppListFragment.ACTION_REFRESH_APPSTORE));

        //非常重要，设置adapter
        mSearchAdapter = new SearchAdapter(this.getActivity(), pushAppsArrayList, R.layout.msp_list_item_app1);
        pushAppsListView.setAdapter(mSearchAdapter);

        refreshHandler.sendMessage(Message.obtain());
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        QDLog.i(TAG,"onDestroyView===============================888======");
        this.getActivity().unregisterReceiver(mRefreshReceiver);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        QDLog.i(TAG,"onDestroyView===============================9999======");
        super.onDestroy();
    }

    @Override
    public void onPause() {
        QDLog.i(TAG,"onPause===============================111======");
        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        QDLog.i(TAG, "onPause===============================222======");
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        QDLog.i(TAG, "onPause===============================333======");
        super.onDetach();
    }

    public Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DownLoadFileFromUrl.DOWNLOADING:
                    if (progressDialog != null) {
                        if (msg.arg1 >= 100) msg.arg1 = 99;
                        progressDialog.setProgress(msg.arg1);
                    }
                    break;
                case DownLoadFileFromUrl.DOWNLOAD_STOP:
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
                    break;
                case DownLoadFileFromUrl.DOWNLOAD_FINISH:
                    progressDialog.setMessage("下载完成");
                    progressDialog.setProgress(100);
                    mSearchAdapter.notifyDataSetChanged();
                    break;
                default:
                    noAppText.setVisibility(View.VISIBLE);
                    noAppText.setText("正在刷新应用列表...");
                    getAppListFromServer();
                    break;
            }
        }
    };

    /**
     * 用最新的接口,获取应用列表
     */
    private void getAppListFromServer() {
        pushAppsArrayList.clear();
        allAppList.clear();
        refreshableView.finishRefreshing();
        noAppText.setVisibility(View.GONE);
        QdWebService.getAppList(new Response.Listener<ArrayList<MamAppInfoModel>>() {
            @Override
            public void onResponse(ArrayList<MamAppInfoModel> response) {
                PolicyContent policy= PolicyManager.getMPolicyManager(AppListFragment.this.getActivity()).getPolicy();

                for (int i = 0; i< response.size(); i++) {
                    MamAppInfoModel m = response.get(i);
                    m.appType = AppListFragment.OPTIONAL_APP;
                    if (m.type.equalsIgnoreCase("WEB")) continue;
                    //设置appType
                    if (policy.getBlackApps().contains(m.pkgName)) {
                            m.appType = AppListFragment.BLACK_APP;
                    } else if (policy.getMustApps().contains(m.pkgName)) {
                        m.appType = AppListFragment.NECESSARY_APP;
                    }
                    pushAppsArrayList.add(m);
                    allAppList.add(m);
                }
                Collections.sort(pushAppsArrayList);
                Collections.sort(allAppList);
                showAppList(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showAppList(false);
            }
        });
    }

    public void showAppList(boolean isNetworkOK) {
        int length = pushAppsArrayList.size();
        if (length <= 0) {
            noAppText.setVisibility(View.VISIBLE);
            if (isNetworkOK) {
                noAppText.setText("应用商店为空，点击刷新！");
            } else {
                noAppText.setText("应用列表加载失败，点击刷新！");
            }
            refreshableView.setVisibility(View.GONE);
            return;
        } else {
            noAppText.setVisibility(View.GONE);
            refreshableView.setVisibility(View.VISIBLE);
        }

        //下面这个，不用也可以lizhongyi
        mSearchAdapter.notifyDataSetChanged();
    }




    ////////////////////////为了搜索//////////////////////////////

    //    /**
//     * 获取搜索结果data和adapter
//     */
    private void getResultData(String text) {
        if (pushAppsArrayList.size() <= 0) {
            return;
        }
        if (TextUtils.isEmpty(text)) {
            pushAppsArrayList.clear();
            pushAppsArrayList.addAll(allAppList);
        }else {
            pushAppsArrayList.clear();
            for (int i = 0; i< allAppList.size(); i++) {
                if (allAppList.get(i).appName.toLowerCase().contains(text.trim().toLowerCase())) {
                    pushAppsArrayList.add(allAppList.get(i));
                }else {
                    //do nothing
                }
            }
        }

        mSearchAdapter.notifyDataSetChanged();
    }

    //只在activity中调用，
    public void getResultDataByStatus(int status) {
        if (allAppList.size() <= 0) {
            return;
        }

        switch (status) {
            case 0:
                pushAppsArrayList.clear();
                pushAppsArrayList.addAll(allAppList);
                break;
            case 1:


            case 2:
            case 3:
            case 4:
                pushAppsArrayList.clear();
                for (int i = 0; i < allAppList.size(); i++) {
                    int res = EmmClientApplication.mSecureContainer.getFileState(allAppList.get(i).appName);

                    if ((status ==1 &&  res == 0 ) || (status ==1 &&  res == 2)){
                        boolean isExist = AppManager.checkInstallResult(mContext,allAppList.get(i).pkgName);
                        if (!isExist) {
                            pushAppsArrayList.add(allAppList.get(i));
                        }
                        continue;

                    }else
                    if ((status ==2 &&  res == 0) && !AppManager.checkInstallResult(mContext,allAppList.get(i).pkgName)) {
                        pushAppsArrayList.add(allAppList.get(i));
                        continue;
                    }else if ((status ==3 &&  res == 0) && AppManager.checkInstallResult(mContext,allAppList.get(i).pkgName)) {
                        pushAppsArrayList.add(allAppList.get(i));
                        continue;
                    }else if ((status ==4 &&  res == 0) && AppManager.checkApkVersion(mContext,allAppList.get(i).pkgName,allAppList.get(i).appVersionCode) == 1) {
                        pushAppsArrayList.add(allAppList.get(i));
                        continue;
                    }
                }
                break;
        }

        mSearchAdapter.notifyDataSetChanged();
    }


    /**
     * 当搜索框 文本改变时 触发的回调 ,更新自动补全数据
     * @param text
     */
    @Override
    public void onRefreshAutoComplete(String text) {

        QDLog.i(TAG,"onRefreshAutoComplete===========" + text);
        //更新数据
//        getAutoCompleteData(text);
    }
    /**
     * 点击搜索键时edit text触发的回调
     *
     * @param text
     */
    @Override
    public void onSearch(String text) {
        QDLog.i(TAG, "onSearch===========" + text);
//        Toast.makeText(mContext, "begin search...", Toast.LENGTH_SHORT).show();
        getResultData(text);
//        Toast.makeText(mContext, "完成搜索", Toast.LENGTH_SHORT).show();

    }

    private QdProgressDialog progressDialog;
    private DownloadDataInfo mDownloadDataInfo;
    private void showNoticeDialog(final String appName,final String fileName,final String url)
    {
        // 构造对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.app_download_title);
        builder.setMessage(mContext.getString(R.string.app_download_info) + ":" + appName);
        // 更新
        builder.setPositiveButton(R.string.app_donwload_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                progressDialog=new QdProgressDialog(mContext);
                progressDialog.show();
                progressDialog.setTitle("下载");
                progressDialog.setMessage("正在下载" + appName);
                progressDialog.setOnCancleLisener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressDialog.dismiss();
                        mDownloadDataInfo.cancle = true;
                    }
                });
                mDownloadDataInfo=new DownloadDataInfo(MyDownloadListener.Download_Type.Type_App,fileName,url);
                DownloadFileThread thread = new DownloadFileThread(mContext, mDownloadDataInfo,refreshHandler);
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

    /**
     * delete APK
     * @param type 0: all; 1: installed
     */
    public void deleteAPK(int type){
        for (MamAppInfoModel model :allAppList) {
            try {
                if(type == 0)
                    EmmClientApplication.mSecureContainer.delete(model.file_name);
                else if ((type == 1) && AppManager.checkInstallResult(mContext,model.pkgName))
                    EmmClientApplication.mSecureContainer.delete(model.file_name);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    //////adapter///////////////
     public class SearchAdapter extends CommonAdapter<MamAppInfoModel> {

        public SearchAdapter(Context context, List<MamAppInfoModel> data, int layoutId) {
            super(context, data, layoutId);
        }

        //需要将model中的check等ui的值也写上。
        @Override
        public void convert(final CommonViewHolder holder, int position) {
            QDLog.i(TAG, "SearchAdapter convert==============000000000000====position=======" + position);

            final MamAppInfoModel model = mDatas.get(position);

                setHolderValue(holder, model, false);
                setRightButtonText(holder, holder.status);

            //set onClickEvent
            holder.setOnClickListener(R.id.right_btn, new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final TextView stateText = (TextView) v.findViewById(R.id.right_btn);
                    switch (holder.status) {
                        case Setup:
                            try {
                                final String plainApk = EmmClientApplication.mSecureContainer.decryptToFile(model.file_name);
                                AppManager.installApk(AppListFragment.this.getActivity(), plainApk);

                                refreshHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        File file = new File(plainApk);
                                        if (file.exists())
                                            file.delete();
                                    }
                                }, 30 * 1000);
                                mSearchAdapter.notifyDataSetChanged();

                            } catch (Exception e) {
                                Toast.makeText(
                                        mContext, model.appName + "安装文件错误！", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                            break;
                        case Open:{
                            try {
                                EmmClientApplication.mSecureContainer.delete(model.file_name);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        case LowVersion:
                            PackageManager pm = mContext.getPackageManager();
                            Intent intent = new Intent();
                            try {
                                intent = pm
                                        .getLaunchIntentForPackage(model.pkgName);
                                mContext.startActivity(intent);

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(
                                        mContext, "无法打开" + model.appName, Toast.LENGTH_LONG).show();
                            }
                            break;

//                        case Downloading: //
//                            DownLoadFileFromUrl.removeDownThread(model.file_name);
////                            holder.status = CommonViewHolder.AppItemStatus.Continue;
//                            stateText.setText("继续");
//                            break;
                        case Update:
                        case StartDownload:
                            try {
                                //String url = AddressManager.getAddrFile(1) + "/" + URLEncoder.encode(model.file_name, "UTF-8");
                                showNoticeDialog(model.appName,model.file_name, model.url);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            });

            LinearLayout mainLinearLayout = holder.getView(R.id.itemContainer);
            mainLinearLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mContext,AppDetailActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putString(GlobalConsts.App_Name,model.appName);
                    bundle.putString(GlobalConsts.App_Type, "个人应用");
//                    bundle.putString(GlobalConsts.App_Size, model.);
                    String detailStr = "";
                    if (!TextUtils.isEmpty(model.appVersion)){
                        detailStr = "版本 : ";
                        detailStr += model.appVersion + "\n";
                    }
                    if (!TextUtils.isEmpty(model.updated_time)){
                        detailStr += "更新时间 : ";
                        detailStr += model.updated_time + "\n";
                    }
                    detailStr += "系统要求 : ";
                    detailStr += "需要Android4.0或更高以上版本";

                    Button btnRight = (Button)holder.getView(R.id.right_btn);
                    String btnTextStr = btnRight.getText().toString();

                    bundle.putString(GlobalConsts.App_Detail, detailStr);
                    bundle.putString(GlobalConsts.App_Func, model.appDesc);
                    bundle.putString(GlobalConsts.App_Status, btnTextStr);
                    bundle.putString(GlobalConsts.App_Icon_Url, model.iconUrl);
                    bundle.putString(GlobalConsts.Pkg_Name, model.pkgName);
                    bundle.putString(GlobalConsts.App_File_Name,model.file_name);
                    bundle.putString(GlobalConsts.App_Download_Url,model.url);

                    QDLog.println(TAG, model.iconUrl);

                    intent.putExtras(bundle);

                    startActivity(intent);

                }
            });
        }

        private void setRightButtonText(CommonViewHolder holder, CommonViewHolder.AppItemStatus status) {
            switch (status) {
                case Open:
                    holder.setButtonText(R.id.right_btn, "打开");
                    break;
                case StartDownload:
                    holder.setButtonText(R.id.right_btn, "下载");
                    break;
                case Update:
                    holder.setButtonText(R.id.right_btn, "升级");
                    break;
                case Downloading:
                    holder.setButtonText(R.id.right_btn, "暂停");

                    break;
                case LowVersion:
                    holder.setButtonText(R.id.right_btn, "低版本");
                    break;
                case Continue:
                    holder.setButtonText(R.id.right_btn, "继续");
                    break;
                case Setup:
                    holder.setButtonText(R.id.right_btn, "安装");
                    break;
                default:
                    holder.setButtonText(R.id.right_btn, "下载");
                    break;
            }

        }

        public void setHolderValue(CommonViewHolder holder, MamAppInfoModel model, boolean isClick) {


            holder.setImageResource(R.id.imageview_left, R.mipmap.doc_audio); //left image
            holder.setText(R.id.textview_title_name, model.appName); //title
//            holder.setText(R.id.textview_mid_title, "" + model.appType); //sub title
            holder.setText(R.id.textview_minor_title, model.appVersion); //thirdtitle
//            holder.setText(R.id.itemCount, ""); //gone
            holder.setButtonText(R.id.right_btn, model.appDesc); //right button
            holder.setVisible(R.id.checkbox_fav, false);

            QDLog.i(TAG, "setHolderValue===========" + model.iconUrl);

            try {
                ImageLoader.getInstance().displayImage(model.iconUrl,
                        (ImageView) holder.getView(R.id.imageview_left), options);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String curVersion = PhoneInfoExtractor.getPackageVersionName(mContext, model.pkgName);
            int curVersionCode = PhoneInfoExtractor.getPackageVersionCode(mContext, model.pkgName);
            int res = EmmClientApplication.mSecureContainer.getFileState(model.file_name);
            if (curVersion!=null && curVersionCode>=0) {
                if ( curVersionCode == model.appVersionCode) {
                    holder.status = CommonViewHolder.AppItemStatus.Open;
                } else if (curVersionCode < model.appVersionCode) {
                    if (res==1)
                        holder.status = CommonViewHolder.AppItemStatus.Setup;
                    else holder.status = CommonViewHolder.AppItemStatus.Update;
                } else if (curVersionCode > model.appVersionCode) {
                    //服务器版本比本地安装的版本低
                    holder.status = CommonViewHolder.AppItemStatus.LowVersion;
                }
            }
            else {
                if (res == 1)
                    holder.status = CommonViewHolder.AppItemStatus.Setup;
                else
                    holder.status = CommonViewHolder.AppItemStatus.StartDownload;
            }
        }
    }
}
