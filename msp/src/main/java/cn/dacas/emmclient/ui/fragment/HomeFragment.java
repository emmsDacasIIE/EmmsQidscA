package cn.dacas.emmclient.ui.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import cn.qdsc.dynamicgrid.BaseDynamicGridAdapter;
import cn.qdsc.dynamicgrid.DynamicGridView;
import cn.dacas.emmclient.R;
import cn.dacas.emmclient.model.MamAppInfoModel;
import cn.dacas.emmclient.ui.activity.mainframe.AppDynamicGridAdapter;
import cn.dacas.emmclient.util.QDLog;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    public static final int Max_Apps_Count = 16;

    private View mView;
    private DynamicGridView gridView;
    private List<MamAppInfoModel> mAppList;
    private int idx;
    private OnMainPageChangedListener mMainPageChagedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
//		System.out.println("OneFragment  onCreate");
        super.onCreate(savedInstanceState);
    }

    public void setContent(int idx, List<MamAppInfoModel> list, OnMainPageChangedListener listener) {
        this.idx = idx;
        mAppList = list;
        mMainPageChagedListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//		System.out.println("OneFragment  onCreateView");
        if (mView == null) {
            initView(inflater, container);
        }
        return mView;
    }

    public AppDynamicGridAdapter getAdapter() {
        if (gridView != null)
            return (AppDynamicGridAdapter) (gridView.getAdapter());
        return null;
    }

    private void initView(LayoutInflater inflater, ViewGroup container) {
        mView = inflater.inflate(R.layout.fragment_grid, container, false);
        gridView = (DynamicGridView) mView.findViewById(R.id.dynamic_grid);
        int column_count = gridView.getNumColumns();
        QDLog.i(TAG, "initView====column_count======" + column_count);
        if (mAppList==null) mAppList=new ArrayList<>();
        gridView.setAdapter(new AppDynamicGridAdapter(this.getActivity(),
                mAppList, getResources().getInteger(R.integer.column_count)));
        gridView.setOnDropListener(new DynamicGridView.OnDropListener() {
            @Override
            public void onActionDrop() {
                gridView.stopEditMode();
            }
        });
        gridView.setOnDragListener(new DynamicGridView.OnDragListener() {
            @Override
            public void onDragStarted(int position) {
            }

            @Override
            public void onDragPositionsChanged(int oldPosition, int newPosition) {
            }

        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                gridView.startEditMode(position);
                return true;
            }
        });


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MamAppInfoModel model = (MamAppInfoModel)(getAdapter().getItems().get(position));
                QDLog.i(TAG, "=====gridView.setOnItemClickListener====" + model.toString());

                // open app
                if (model.isApk()) {
                    PackageManager pm = getActivity().getPackageManager();
                    try {
                        //TODO
                        Intent intent = pm.getLaunchIntentForPackage(model.pkgName);
                        getActivity().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (model.isWeb()) {
                    try {
                        Uri uri = Uri.parse(model.file_name);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        gridView.setOnPageChangedListener(new DynamicGridView.OnPageChangedListener() {
            @Override
            public void onLeftPage(int position) {
                MamAppInfoModel model = (MamAppInfoModel) ((BaseDynamicGridAdapter) gridView.getAdapter()).getItem(position);
                mMainPageChagedListener.onLeftPage(idx, model);
            }

            @Override
            public void onRightPage(int position) {
                MamAppInfoModel model = (MamAppInfoModel) ((BaseDynamicGridAdapter) gridView.getAdapter()).getItem(position);
                mMainPageChagedListener.onRightPage(idx, model);
            }
        });

    }

    @Override
    public void onPause() {
//		System.out.println("OneFragment  onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
//		System.out.println("OneFragment  onResume");
        super.onResume();
    }

    @Override
    public void onDestroy() {
//		System.out.println("OneFragment  onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
//		System.out.println("OneFragment  onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onStop() {
//		System.out.println("OneFragment  onStop");
        super.onStop();
    }

    @Override
    public void onStart() {
//		System.out.println("OneFragment  onStart");
        super.onStart();
    }
}
