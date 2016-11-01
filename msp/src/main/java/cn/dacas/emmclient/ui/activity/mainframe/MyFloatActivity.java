package cn.dacas.emmclient.ui.activity.mainframe;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.List;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.ui.activity.base.BaseListActivity;
import cn.dacas.emmclient.ui.qdlayout.ListItemData;

/**
 * 隐私设置
 * @author QIDSC
 */
public class MyFloatActivity extends BaseListActivity {

    private static final String TAG = "MyPrivacySettingsActivity";

    int[] mMajorStrResId = {R.string.my_is_float};
    boolean[] isCheckedArr = {false};

    //向右的箭头
    private int ArrowID = R.mipmap.msp_right_arrow_gray;

    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Image_Text_Null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
    }

    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initListAdapter() {
        mAdapter = new MyPrivacySettingsAdaptor(mContext, mDataArray);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void initButton() {
        setButtonLayoutEnable(false);

    }

    @Override
    protected void onListItemClicked(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

    }

    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }




    ////////////////自定义函数////////////
    private void iniDataArrays() {

        for(int i = 0; i< mMajorStrResId.length;i++) {
            ListItemData data = new ListItemData();
            data.mMajorString = getString( mMajorStrResId[i]);
            data.isChecked = EmmClientApplication.isFloating;
            mDataArray.add(data);
        }
    }


    private void initMyView() {

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);

        mMiddleHeaderView.setText(mContext.getString(R.string.my_float));
//        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);

//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(false);


    }

    private void init() {
        initMyView();
        iniDataArrays();

    }

    //////////////自定义的adaptor 以及与adaptor相关的函数定义//////////
    private void onItemExpanClicked(int pos,boolean isChecked) {
        Object object = mDataArray.get(pos);
        if(object instanceof ListItemData) {
            ListItemData data = (ListItemData)object;
            EmmClientApplication.isFloating=isChecked;
            data.isChecked = isChecked;
            mAdapter.notifyDataSetChanged();
        }
    }
    private class MyPrivacySettingsAdaptor extends ListAdapter {

        public MyPrivacySettingsAdaptor(Context context, List<Object> dataArray) {
            super(context, dataArray);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if(view.getTag() instanceof ViewHolder) {
                final ViewHolder holder = (ViewHolder)view.getTag();

                Object obj = mDataArray.get(position);
                if(null != obj && obj instanceof ListItemData) {
                    ListItemData data = (ListItemData)obj;

                    holder.mMajorTextView.setText(data.mMajorString);
                    holder.mCheckBox.setChecked(data.isChecked);
                    holder.mCheckBox.setVisibility(View.VISIBLE);
                    holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean isCheck = holder.mCheckBox.isChecked();
                            holder.mCheckBox.setChecked(!isCheck);
                            onItemExpanClicked(position, isCheck);
                        }
                    });
                }
            }

            return view;
        }
    }

}
