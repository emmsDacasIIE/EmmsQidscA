package cn.qdsc.msp.ui.activity.mainframe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.List;

import cn.qdsc.msp.R;
import cn.qdsc.msp.core.EmmClientApplication;
import cn.qdsc.msp.ui.activity.base.BaseListActivity;
import cn.qdsc.msp.ui.activity.loginbind.UserLoginActivity;
import cn.qdsc.msp.ui.gesturelock.UnlockActivity;
import cn.qdsc.msp.ui.qdlayout.ListItemData;
import cn.qdsc.msp.util.QDLog;

/**
 * 隐私设置
 * @author QIDSC
 */
public class MyHandPasswordActivity extends BaseListActivity {

    private static final String TAG = "MyPrivacySettingsActivity";

    int[] mMajorResId = {R.string.used_hand_pass,R.string.modify_hand_pass,R.string.forget_hand_pass};
    boolean[] isCheckedArr = {false,false,false};

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

        for(int i = 0; i< mMajorResId.length;i++) {
            ListItemData data = new ListItemData();
            data.mMajorString = this.getString(mMajorResId[i]);
            data.isChecked = isCheckedArr[i];
            mDataArray.add(data);
        }
    }


    private void initMyView() {

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);

        mMiddleHeaderView.setText(mContext.getString(R.string.my_hand_password));
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
            //save data to where???
            data.isChecked = isChecked;
            mAdapter.notifyDataSetChanged();
            int loginType=isChecked?1:0;
            EmmClientApplication.mDatabaseEngine.setCurrentLoginType(loginType);
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

                    QDLog.i(TAG,"MyPrivacySettingsAdaptor getView======" + position);

                    if (position == 0) {
                        holder.mCheckBox.setChecked(data.isChecked);
                        holder.mCheckBox.setVisibility(View.VISIBLE);
                        int loginType=EmmClientApplication.mDatabaseEngine.getCurrentLoginType();
                        holder.mCheckBox.setChecked(loginType==1?true:false);
                        holder.mExpandImage.setVisibility(View.GONE);

                        holder.mCheckBox.setEnabled(true);
                        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean isCheck = holder.mCheckBox.isChecked();
                                holder.mCheckBox.setChecked(!isCheck);
                                onItemExpanClicked(position, isCheck);
                            }
                        });
                    }else if (position == 1) {
                        holder.mExpandImage.setImageResource(ArrowID);
                        holder.mExpandImage.setVisibility(View.VISIBLE);
                        holder.mCheckBox.setVisibility(View.GONE);
                        holder.mCheckBox.setEnabled(false);

//                        holder.mExpandImage
                        holder.mItemLinearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!holder.mCheckBox.isEnabled()) {
                                    Intent it=new Intent(MyHandPasswordActivity.this, UnlockActivity.class);
                                    it.putExtra("type",1);
                                    MyHandPasswordActivity.this.startActivity(it);
                                }
                            }
                        });
                    }
                    else if (position == 2) {
                        holder.mExpandImage.setImageResource(ArrowID);
                        holder.mExpandImage.setVisibility(View.VISIBLE);
                        holder.mCheckBox.setVisibility(View.GONE);
                        holder.mCheckBox.setEnabled(false);

//                        holder.mExpandImage
                        holder.mItemLinearLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!holder.mCheckBox.isEnabled()) {
                                    EmmClientApplication.mDatabaseEngine.setPatternPassword(EmmClientApplication.mCheckAccount.getCurrentAccount(),null);
                                    Intent it=new Intent(MyHandPasswordActivity.this, UserLoginActivity.class);
                                    MyHandPasswordActivity.this.startActivity(it);
                                    finish();
                                }
                            }
                        });
                    }

//                    holder.mExpandImage.setImageResource(data.state > 0?R.drawable.down_expand_gray:  R.drawable.list_item_expand_arrow);


//                    holder.mLeftBtn.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View arg0) {
////                            onItemExpanClicked(position, EQuarantineType.DELETE);
//                        }
//                    });
//                    holder.mRightBtn.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View arg0) {
////                            onItemExpanClicked(position, EQuarantineType.RESTORE);
//                        }
//                    });
                }
            }

            return view;
        }
    }



}
