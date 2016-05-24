package cn.dacas.emmclient.mam;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.main.EmmClientApplication;
import cn.dacas.emmclient.mam.RefreshableView.PullToRefreshListener;
import cn.dacas.emmclient.util.MyJsonArrayRequest;
import cn.dacas.emmclient.util.NetworkDef;
import cn.dacas.emmclient.util.PrefUtils;
import cn.dacas.emmclient.util.UpdateTokenRequest;
import cn.dacas.emmclient.worker.DownLoadAppFromUrl;
import cn.dacas.emmclient.worker.PhoneInfoExtractor;
import cn.dacas.providers.DownloadManager;
import cn.dacas.providers.DownloadManager.Query;
import cn.dacas.providers.downloads.DownloadService;
import cn.dacas.providers.downloads.ui.DownloadList;

/**
 * A placeholder fragment containing a simple view.
 */
public class AppListFragment extends Fragment {
    private static final String TAG = "AppListFragment";
    // 下载完成修改状态
    public static final String ACTION_REFRESH_APPSTORE = "cn.dacas.emmclient.mam.REFRESH_APP_STORE";

    public static final int NECESSARY_APP = 0;
    public static final int OPTIONAL_APP = 1;
    public static final int BLACK_APP = 2;

    private SharedPreferences settings = null;

    private BroadcastReceiver mRefreshReceiver;

    DownloadManager mDownloadManager;

    private RefreshableView refreshableView;
    private ListView pushAppsListView = null;
    private TextView noAppText = null;

    private View rootView;

    private boolean isNetworkOK = true;

    private ProgressDialog mProgressDlg;

    DisplayImageOptions options;

    //	private JsonArrayRequest jsonArrayRequest;
    MyJsonArrayRequest jsonArrayRequest;
    // TODO:添加新消息
    private ArrayList<PushAppsListItem> pushAppsArrayList = new ArrayList<PushAppsListItem>();

    private Map<String, Integer> url2status = new HashMap<String, Integer>();

    private Map<String, String> url2path = new HashMap<String, String>();

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
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        curFragment.setArguments(args);
        return curFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_app_list, container,
                false);

        settings = this.getActivity().getSharedPreferences(PrefUtils.PREF_NAME,
                0);

        pushAppsListView = (ListView) rootView
                .findViewById(R.id.pushAppsListView);
        noAppText = (TextView) rootView.findViewById(R.id.noAppText);
        refreshableView = (RefreshableView) rootView
                .findViewById(R.id.refreshable_view);

        refreshableView.setOnRefreshListener(new PullToRefreshListener() {

            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                refreshHandler.sendMessage(Message.obtain());
                refreshableView.finishRefreshing();
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
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).build();

        mDownloadManager = new DownloadManager(this.getActivity()
                .getContentResolver(), this.getActivity().getPackageName());
        startDownloadService();
        mRefreshReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                refreshHandler.sendMessage(Message.obtain());
            }
        };
        this.getActivity().registerReceiver(mRefreshReceiver,
                new IntentFilter(AppListFragment.ACTION_REFRESH_APPSTORE));

        refreshHandler.sendMessage(Message.obtain());
        return rootView;
    }

    private Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mProgressDlg = ProgressDialog.show(rootView.getContext(), "加载中",
                    "正在刷新应用列表...", true, false);
            getAppListFromServer();
        }
    };

    @Override
    public void onDestroyView() {
        this.getActivity().unregisterReceiver(mRefreshReceiver);
        super.onDestroyView();
    }

    /**
     * 用最新的接口
     */
    private void getAppListFromServer() {
        pushAppsArrayList.clear();
        String url = "/user/apps?platforms=ANDROID";
        jsonArrayRequest = new MyJsonArrayRequest(com.android.volley.Request.Method.GET, url, UpdateTokenRequest.TokenType.USER,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        String blackApps = settings.getString(PrefUtils.BLACK_KEY, null);
                        String mustApps = settings.getString(PrefUtils.NECESSARY_KEY, null);
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject json = (JSONObject) response.get(i);
                                String type = json.getString("type");
                                if (!type.equals("APK"))
                                    continue;
                                String des = json.getString("description");
                                if (des != null && des.equals("undefined"))
                                    des = null;
                                int versionCode = 0;
                                if (json.has("version_code") && !json.getString("version_code").trim().equals(""))
                                    versionCode = Integer.parseInt(json.getString("version_code"));
                                PushAppsListItem item = new PushAppsListItem(
                                        json.getString("name"),
                                        des,
                                        versionCode,
                                        json.getString("version_name"),
                                        json.getString("package_name"),
                                        "",
//								json.getString("icon_name"),
                                        AppListFragment.OPTIONAL_APP, json.getString("id"));
                                if (blackApps != null && blackApps.contains(item.pkgName)) {
                                    if (getVersionCode(item.pkgName) != -1) {
                                        item.appType = AppListFragment.BLACK_APP;
                                        pushAppsArrayList.add(item);
                                    }
                                } else if (mustApps != null
                                        && mustApps.contains(item.pkgName)) {
                                    item.appType = AppListFragment.NECESSARY_APP;
                                    pushAppsArrayList.add(item);
                                } else
                                    pushAppsArrayList.add(item);
                            }
                            Collections.sort(pushAppsArrayList);
                            isNetworkOK = true;
                            getDownloadAppMap();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mProgressDlg.dismiss();
                        showAppList();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    isNetworkOK = false;
                }
                mProgressDlg.dismiss();
                //showAppList();
            }
        });
        EmmClientApplication.mVolleyQueue.add(jsonArrayRequest);
    }


    /*
     * 从DownloadManager获取当前正在下载或完成的<url, status>信息
     */
    private void getDownloadAppMap() {
        // TODO 更新url2status
        url2status.clear();
        url2path.clear();

        Cursor mCursor = mDownloadManager.query(new Query());
        int urlColumn = mCursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_URI);
        int statusColumn = mCursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS);

        int pathColumn = mCursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI);

        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();
            do {
                url2status.put(mCursor.getString(urlColumn),
                        mCursor.getInt(statusColumn));
                url2path.put(mCursor.getString(urlColumn),
                        mCursor.getString(pathColumn));
            } while (mCursor.moveToNext());
        }

        try {
            mCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDownloadService() {
        Intent intent = new Intent();
        intent.setClass(this.getActivity(), DownloadService.class);
        this.getActivity().startService(intent);
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    private String getVersion(String pkgName) {
        try {
            PackageManager manager = this.getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(pkgName, 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getVersionCode(String pkgName) {
        try {
            PackageManager manager = this.getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(pkgName, 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    protected void showDownloadList() {
        Intent intent = new Intent();
        intent.setClass(this.getActivity(), DownloadList.class);
        startActivity(intent);
    }

    private void showAppList() {
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

        AppListAdapter mAdapter = new AppListAdapter(this.getActivity());
        pushAppsListView.setAdapter(mAdapter);
    }

    private class AppListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;// 得到一个LayoutInfalter对象用来导入布局

        /**
         * 构造函数
         */
        public AppListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return pushAppsArrayList.size();// 返回数组的长度
        }

        @Override
        public Object getItem(int position) {
            return pushAppsArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private String encodeUrl(String appUrl) {
            String netUrl = appUrl;
            int index = appUrl.lastIndexOf('/');
            if (index != -1) {
                try {
                    String str = appUrl
                            .substring(index + 1);
                    str = URLEncoder.encode(str, "utf-8");
                    str = str.replaceAll("\\+", "%20");
                    netUrl = appUrl.substring(0, index + 1)
                            + str;
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return netUrl;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            AppListHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_app_store,
                        null);
                holder = new AppListHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.subtitle = (TextView) convertView
                        .findViewById(R.id.subtitle);
                holder.itemCount = (TextView) convertView
                        .findViewById(R.id.itemCount);
                holder.chevron = (ImageView) convertView
                        .findViewById(R.id.chevron);
                holder.type = (TextView) convertView.findViewById(R.id.type);

                convertView.setTag(holder);
            } else {
                holder = (AppListHolder) convertView.getTag();
            }

            PushAppsListItem appItem = pushAppsArrayList.get(position);
            final String netUrl = "https://" + NetworkDef.getAddrWebservice() +
                    "/api/v1/user/apps/" + appItem.id + "?uuid=" + EmmClientApplication.mPhoneInfo.getIMEI();
            holder.title.setText(appItem.appName);
            holder.subtitle.setText(appItem.appDesc != null ? appItem.appDesc
                    + " 版本" + appItem.appVersion : "版本" + appItem.appVersion);
            holder.itemCount.setVisibility(View.VISIBLE);
            holder.chevron.setVisibility(View.INVISIBLE);

            switch (appItem.appType) {
                case AppListFragment.NECESSARY_APP:
                    holder.type
                            .setBackgroundResource(R.drawable.background_necessary_app);
                    holder.type.setText("必");
                    break;
                case AppListFragment.OPTIONAL_APP:
                    holder.type
                            .setBackgroundResource(R.drawable.background_optional_app);
                    holder.type.setText("选");
                    break;
                case AppListFragment.BLACK_APP:
                    holder.type
                            .setBackgroundResource(R.drawable.background_black_app);
                    holder.type.setText("黑");
                    break;
            }

            try {
                ImageLoader.getInstance().displayImage(appItem.iconUrl,
                        holder.image, options);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String curVersion = getVersion(appItem.pkgName);
            int curVersionCode = getVersionCode(appItem.pkgName);

            if (appItem.appType == AppListFragment.BLACK_APP) {
                // 黑名单应用
                holder.subtitle
                        .setText(appItem.appDesc != null ? appItem.appDesc
                                + "黑名单应用" : "黑名单应用");
                holder.itemCount.setText("卸载");
            } else {
                // 必装或者选装应用
                if (curVersion != null
                        && curVersionCode == appItem.appVersionCode) {
                    holder.itemCount.setText("打开");
                } else if (curVersion != null
                        && curVersionCode < appItem.appVersionCode) {
                    holder.subtitle
                            .setText(appItem.appDesc != null ? appItem.appDesc
                                    + " 版本" + appItem.appVersion + " 当前版本"
                                    + curVersion : "版本" + appItem.appVersion
                                    + " 当前版本" + curVersion);
                    holder.itemCount.setText("升级");
                } else if (curVersion != null
                        && curVersionCode > appItem.appVersionCode) {
                    holder.subtitle.setText("服务器版本" + appItem.appVersion
                            + "低于已安装版本" + curVersion);
                    holder.itemCount.setText("低版本");
                } else {
                    Integer status = url2status.get(netUrl);
                    if (status == null) {
                        holder.itemCount.setText("下载");
                    } else {
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            holder.itemCount.setText("安装");
                        } else {
                            holder.itemCount.setText("下载中");
                        }
                    }
                }
            }

            holder.itemCount.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View view) {
                    // TODO Auto-generated method stub

                    final PushAppsListItem listItem = (PushAppsListItem) pushAppsArrayList
                            .get(position);
                    final TextView stateText = (TextView) view
                            .findViewById(R.id.itemCount);
                    String state = stateText.getText().toString();
                    if (state.equals("卸载")) {
                        Uri packageURI = Uri.parse("package:"
                                + listItem.pkgName);
                        Intent uninstallIntent = new Intent(
                                Intent.ACTION_DELETE, packageURI);
                        startActivity(uninstallIntent);
                    } else if (state.equals("打开") || state.equals("低版本")) {
                        PackageManager pm = view.getContext()
                                .getPackageManager();
                        Intent intent = new Intent();
                        try {
                            intent = pm
                                    .getLaunchIntentForPackage(listItem.pkgName);
                            view.getContext().startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(
                                    view.getContext().getApplicationContext(),
                                    "无法打开" + listItem.appName,
                                    Toast.LENGTH_LONG).show();
                        }
                    } else if (state.equals("下载中")) {
                        showDownloadList();
                    } else if (state.equals("升级")) {
                        UpdateTokenRequest.update(UpdateTokenRequest.TokenType.USER, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                new DownLoadAppFromUrl(view.getContext()).startDownload(listItem.appName, netUrl + "&access_token=" + response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(AppListFragment.this.getActivity(), "无法连接服务器", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (state.equals("下载")) {
                        //更新Token，确保Token可用
                        UpdateTokenRequest.update(UpdateTokenRequest.TokenType.USER, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                new DownLoadAppFromUrl(view.getContext()).startDownload(listItem.appName, netUrl + "&access_token=" + response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(AppListFragment.this.getActivity(),"无法连接服务器",Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // 通过Intent安装APK文件
                        try {
                            Intent toOpenApk = new Intent(Intent.ACTION_VIEW);
                            toOpenApk.setDataAndType(
                                    Uri.parse(url2path.get(netUrl)),
                                    "application/vnd.android.package-archive");
                            toOpenApk.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            view.getContext().startActivity(toOpenApk);
                        } catch (Exception e) {
                            Toast.makeText(
                                    view.getContext().getApplicationContext(),
                                    listItem.appName + "安装文件错误！",
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }


            });

            convertView.setOnClickListener(new OnClickListener() {
                Boolean flag = true;

                @Override
                public void onClick(View view) {
                    TextView stv = (TextView) view.findViewById(R.id.subtitle);
                    if (flag) {
                        flag = false;
                        stv.setEllipsize(null); // 展开
                        stv.setSingleLine(flag);
                    } else {
                        flag = true;
                        stv.setEllipsize(TextUtils.TruncateAt.END); // 收缩
                        stv.setSingleLine(flag);
                    }
                }
            });
            return convertView;
        }

        private String getAddress(PushAppsListItem listItem) {
            EmmClientApplication app = (EmmClientApplication) getActivity().getApplicationContext();
            String ip = NetworkDef.getAvailableUpdateIp();
            String getStr = "https://" + ip +
                    "/api/v1/user/apps/" + listItem.id + "?uuid=" + PhoneInfoExtractor.getPhoneInfoExtractor(app).getIMEI() + "&access_token="
                    + EmmClientApplication.mCheckAccount.getAccessToken();
            return getStr;
        }
    }

    public static class PushAppsListItem implements Comparable {
        public String appName;
        public String appDesc;
        public int appVersionCode;
        public String appVersion;
        public String pkgName;
        public String iconUrl;
        public int appType;
        public String type;
        public String created_time;
        public String url;
        public String id;

        public PushAppsListItem(String appName, String appDesc,
                                int appVersionCode, String appVersion, String pkgName,
                                String iconUrl, int appType, String id
        ) {
            this.appName = appName;
            this.appDesc = appDesc;
            this.appVersionCode = appVersionCode;
            this.appVersion = appVersion;
            this.pkgName = pkgName;
            this.iconUrl = iconUrl;
            this.appType = appType;
            this.id = id;

        }

        public PushAppsListItem(String appName, String iconUrl, String created_time, int appType, String id) {
            // TODO Auto-generated constructor stub
            this.appName = appName;
            this.iconUrl = iconUrl;
            this.created_time = created_time;
            this.appType = appType;
            this.id = id;
        }

        @Override
        public int compareTo(Object another) {
            // TODO Auto-generated method stub
            PushAppsListItem tgt = (PushAppsListItem) another;
            return (this.appType < tgt.appType ? -1
                    : (this.appType == tgt.appType ? 0 : 1));
        }
    }

    private class AppListHolder {
        public ImageView image;
        public TextView title;
        public TextView type;
        public TextView subtitle;
        public TextView itemCount;
        public ImageView chevron;
    }

}