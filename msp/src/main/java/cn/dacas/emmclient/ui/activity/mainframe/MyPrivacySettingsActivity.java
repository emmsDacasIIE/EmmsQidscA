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
import cn.dacas.emmclient.util.PrefUtils;

import static cn.dacas.emmclient.core.mdm.MDMService.updatePrivacySetting;

/**
 * 隐私设置
 * @author QIDSC
 */
public class MyPrivacySettingsActivity extends BaseListActivity {

    private static final String TAG = "MyPrivacySettingsActivity";


    int[] MajorResId = {R.string.hardware_info,R.string.system_info,R.string.location_info,R.string.network_info,R.string.app_service_info};
    int[] MinorResId = {R.string.hardware_info_detail,R.string.system_info_detail,R.string.location_info_detail,R.string.network_info_detail,R.string.app_service_info_detail};

    String device_type;


    @Override
    protected HearderView_Style setHeaderViewStyle() {
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
        device_type= EmmClientApplication.mActivateDevice.getDeviceType();
        for(int i = 0; i< MajorResId.length;i++) {
            ListItemData data = new ListItemData();
            data.mMajorString = mContext.getString(MajorResId[i]);
            data.mMinorString = mContext.getString(MinorResId[i]);
            if (device_type.equalsIgnoreCase("BYOD") || (device_type.equalsIgnoreCase("UNKNOWN"))) {
                switch (i) {
                    case 0:
                        data.isChecked = PrefUtils.getHardPrivacy();
                        break;
                    case 1:
                        data.isChecked = PrefUtils.getSysPrivacy();
                        break;
                    case 2:
                        data.isChecked = PrefUtils.getLockPrivacy();
                        break;
                    case 3:
                        data.isChecked = PrefUtils.getNetPrivacy();
                        break;
                    case 4:
                        data.isChecked = PrefUtils.getAppPrivacy();
                        break;
                    default:
                        data.isChecked = true;
                        break;
                }
            }
            else
                data.isChecked=true;
            mDataArray.add(data);
        }
    }

    private void initMyView() {

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);

        mMiddleHeaderView.setText(mContext.getString(R.string.my_private_settings));
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
            data.isChecked = isChecked;
            switch ( pos) {
                case 0:
                    PrefUtils.putHardPrivacy(isChecked);
                    break;
                case 1:
                    PrefUtils.putSysPrivacy(isChecked);
                    break;
                case 2:
                    PrefUtils.putLockPrivacy(isChecked);
                    break;
                case 3:
                    PrefUtils.putNetPrivacy(isChecked);
                    updatePrivacySetting(isChecked);
                    break;
                case 4:
                    PrefUtils.putAppPrivacy(isChecked);
                    break;
            }
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

                    holder.mMinorTextView.setVisibility(View.VISIBLE);
                    holder.mCheckBox.setVisibility(View.VISIBLE);

                    holder.mMajorTextView.setText(data.mMajorString);

                    holder.mMinorTextView.setText(data.mMinorString);
                    holder.mCheckBox.setChecked(data.isChecked);
                    if ((device_type.equalsIgnoreCase("BYOD") || (device_type.equalsIgnoreCase("UNKNOWN")))
                            && (position == 2 || position == 3)) {
                        holder.mCheckBox.setEnabled(true);
                    }
                    else {
                        holder.mCheckBox.setEnabled(false);
                        holder.mCheckBox.setBackground(getResources().getDrawable(R.mipmap.msp_checkbox_yes_icon_disable));
                    }
                    holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean isCheck = holder.mCheckBox.isChecked();
                            holder.mCheckBox.setChecked(!isCheck);
                            onItemExpanClicked(position,isCheck);

                        }
                    });

                }
            }

            return view;
        }
    }


}
