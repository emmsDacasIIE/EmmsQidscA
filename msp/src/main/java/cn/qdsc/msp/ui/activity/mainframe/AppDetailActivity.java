package cn.qdsc.msp.ui.activity.mainframe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cn.qdsc.msp.R;
import cn.qdsc.msp.webservice.download.DownLoadFileFromUrl;
import cn.qdsc.msp.webservice.download.DownloadDataInfo;
import cn.qdsc.msp.webservice.download.DownloadFileThread;
import cn.qdsc.msp.webservice.download.MyDownloadListener;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.ui.activity.base.BaseSlidingFragmentActivity;
import cn.qdsc.msp.ui.qdlayout.QdProgressDialog;
import cn.qdsc.msp.core.mam.AppManager;
import cn.qdsc.msp.util.GlobalConsts;
import cn.qdsc.msp.manager.AddressManager;

/**
 * @author Wang
 */
public class AppDetailActivity extends BaseSlidingFragmentActivity {

    private static final String TAG = "AppDetailActivity";


    String pkgNameStr;
    String appStatusStr;
    String appName;
    String appFileName;
    String downloadUrl;

    private TextView mAppNameTextView;
    private TextView mAppTypeTextView;
    private TextView mAppSizeTextView;

    private TextView mAppFuncTextView;

    private TextView mAppInfoTextView;

    private Button mAppStatusButton;

    private CheckBox mCheckBox;

    private ImageView mImageView;

    DisplayImageOptions options;



    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Image_Text_Null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appdetail, "");
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

    private QdProgressDialog progressDialog;
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
                    appStatusStr="安装";
                    mAppStatusButton.setText(appStatusStr);
                    break;
                default:
                    break;
            }
        }
    };

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
                progressDialog.setOnCancleLisener(new View.OnClickListener() {
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

    ////////////////自定义函数////////////
    private void initMyView() {

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);

        mMiddleHeaderView.setText(mContext.getString(R.string.app_detail_title));
//        mMiddleHeaderView.setTextVisibile(true);
        mMiddleHeaderView.setImageVisibile(false);

//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(false);

        mImageView = (ImageView)findViewById(R.id.imageview_left);

        mAppNameTextView = (TextView) findViewById(R.id.textview_title_name);
        mAppTypeTextView = (TextView) findViewById(R.id.textview_mid_title);
        mAppSizeTextView = (TextView) findViewById(R.id.textview_minor_title);

        mAppFuncTextView = (TextView) findViewById(R.id.app_func);
        mAppInfoTextView = (TextView) findViewById(R.id.app_detail_info);

        mCheckBox = (CheckBox) findViewById(R.id.checkbox_fav);

        mAppStatusButton = (Button)findViewById(R.id.right_btn);
        mAppStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(appStatusStr)) {
                    if (appStatusStr.equals("打开")) {
                        PackageManager pm = mContext.getPackageManager();
                        Intent intent = new Intent();
                        try {
                            intent = pm
                                    .getLaunchIntentForPackage(pkgNameStr);
                            mContext.startActivity(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(
                                    mContext, "无法打开" + pkgNameStr, Toast.LENGTH_LONG).show();
                        }
                    }
                    else if (appStatusStr.equals("安装")||appStatusStr.equals("低版本")) {
                        try {
                            final String plainApk = EmmClientApplication.mSecureContainer.decryptToFile(appFileName);
                            AppManager.installApk(AppDetailActivity.this, plainApk);

                            refreshHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    File file = new File(plainApk);
                                    if (file.exists())
                                        file.delete();
                                }
                            }, 30 * 1000);

                        } catch (Exception e) {
                            Toast.makeText(
                                    mContext, appName + "安装文件错误！", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                    if (appStatusStr.equals("下载")||appStatusStr.equals("升级")) {
                        try {
                            //String url = AddressManager.getAddrFile(1) + "/" + URLEncoder.encode(appFileName, "UTF-8");
                            showNoticeDialog(appName,appFileName, downloadUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });

        mCheckBox.setVisibility(View.GONE);

    }

    private void init() {
        initMyView();


        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.ic_stub)
                .showImageForEmptyUri(R.mipmap.ic_empty)
                .showImageOnFail(R.mipmap.ic_error).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).build();


    }

    private void setMsgData() {
        Bundle bundle = this.getIntent().getExtras();
        appName = bundle.getString(GlobalConsts.App_Name);
        downloadUrl = bundle.getString(GlobalConsts.App_Download_Url);
        String appTypeStr = bundle.getString(GlobalConsts.App_Type);
        String appSizeStr = bundle.getString(GlobalConsts.App_Size);

        String appFuncStr = bundle.getString(GlobalConsts.App_Func);
        String appDetailStr = bundle.getString(GlobalConsts.App_Detail);
        appStatusStr = bundle.getString(GlobalConsts.App_Status);
        pkgNameStr = bundle.getString(GlobalConsts.Pkg_Name);
        appFileName=bundle.getString(GlobalConsts.App_File_Name);

        String appIconUrlStr = bundle.getString(GlobalConsts.App_Icon_Url);

        mAppNameTextView.setText(appName);
        mAppTypeTextView.setText(appTypeStr);
        mAppSizeTextView.setText(appSizeStr);
        mAppFuncTextView.setText(appFuncStr);
        mAppInfoTextView.setText(appDetailStr);
        mAppStatusButton.setText(appStatusStr);
        try {
            ImageLoader.getInstance().displayImage(appIconUrlStr,mImageView, options);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
