package cn.dacas.emmclient.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.util.PhoneInfoExtractor;

/**
 * @author Wang
 */
public class ContactDetailActivity extends BaseSlidingFragmentActivity {

    private static final String TAG = "ContactDetailActivity";


    //sub view
    private TextView mNameTextView;
    private TextView mCompanyTextView;

    private TextView mTelTextViewLable1;
    private TextView mTelTextViewLable2;
    private TextView mTelTextView1Value1;
    private TextView mTelTextView1Value2;

    private TextView mEmailTextViewLable1;
    private TextView mEmailTextViewLable2;
    private TextView mEmailTextViewValue1;
    private TextView mEmailTextViewValue2;

    private TextView mAddressTextViewLable;
    private TextView mAddressTextViewValue;



    ImageView mMsgImageView1;
    ImageView mMsgImageView2;
    ImageView mTelImageView1;
    ImageView mTelImageView2;


    //group layout
    LinearLayout mLinearLayoutGroup1,
        mLinearLayoutGroup2,
        mLinearLayoutGroup3,
        mLinearLayoutGroup4;

    //sub layout
    RelativeLayout mPhotoLinearLayout;
    LinearLayout mTelLinearLayout1,
            mTelLinearLayout2,
            mEmailLinearLayout1,
            mEmailLinearLayout2,
            mAddressLinearLayout;

    //仅仅是右边的两个图，为了隐藏
    LinearLayout mEmailSubLinearLayout1;
    LinearLayout mEmailSubLinearLayout2;
    LinearLayout mAddressSubLinearLayout;


    SortModel sortModel;

    @Override
    protected HearderView_Style setHeaderViewStyle() {
        return HearderView_Style.Image_Text_Null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail, "");
        init();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    ////////////////自定义函数////////////
    private void initMyView() {
        mMiddleHeaderView.setText("contact detail");

//        mLeftHeaderView.setTextVisibile(false);
//        mLeftHeaderView.setImageVisibile(true);
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);

        mMiddleHeaderView.setText("Unkonwn");
//        mMiddleHeaderView.setTextVisibile(true);
//        mMiddleHeaderView.setImageVisibile(false);

//        mRightHeaderView.setText(mContext.getString(R.string.edit));
//        mRightHeaderView.setTextVisibile(false);
//        mRightHeaderView.setImageVisibile(false);

        for (int i = 0; i< 4;i++) {
            switch (i) {
                case 0:
                    mLinearLayoutGroup1 = (LinearLayout)findViewById(R.id.layout_group1);
                    mPhotoLinearLayout = (RelativeLayout)findViewById(R.id.myphoto_sublayout);

                    mNameTextView = (TextView)findViewById(R.id.textview_myname_lable);
                    mCompanyTextView = (TextView)findViewById(R.id.textview_mycompany_lable);
                    break;
                case 1:
                    mLinearLayoutGroup2 = (LinearLayout)findViewById(R.id.layout_group2);

                    //tel1
                    mTelLinearLayout1 = (LinearLayout)findViewById(R.id.ll_tel_cell_number1);

                    mTelTextViewLable1 = (TextView)mTelLinearLayout1.findViewById(R.id.textview_major);
                    mTelTextViewLable1.setText(R.string.telphone_number1);
                    mTelTextView1Value1 = (TextView)mTelLinearLayout1.findViewById(R.id.textview_minor);

                    mTelImageView1 = (ImageView)mTelLinearLayout1.findViewById(R.id.imageview_mid);
                    mMsgImageView1 = (ImageView)mTelLinearLayout1.findViewById(R.id.imageview_right);

                    //tel2
                    mTelLinearLayout2 = (LinearLayout)findViewById(R.id.ll_tel_cell_number2);

                    mTelTextViewLable2 = (TextView)mTelLinearLayout2.findViewById(R.id.textview_major);
                    mTelTextViewLable2.setText(R.string.telphone_number2);
                    mTelTextView1Value2 = (TextView)mTelLinearLayout2.findViewById(R.id.textview_minor);

                    mTelImageView2  = (ImageView)mTelLinearLayout2.findViewById(R.id.imageview_mid);
                    mMsgImageView2 = (ImageView)mTelLinearLayout2.findViewById(R.id.imageview_right);


                    break;
                case 2:
                    mLinearLayoutGroup3 = (LinearLayout)findViewById(R.id.layout_group3);

                    //email 1
                    mEmailLinearLayout1 = (LinearLayout)findViewById(R.id.ll_emailid1);

                    mEmailTextViewLable1 = (TextView)mEmailLinearLayout1.findViewById(R.id.textview_major);
                    mEmailTextViewLable1.setText(R.string.email1);
                    mEmailTextViewValue1 = (TextView)mEmailLinearLayout1.findViewById(R.id.textview_minor);

                    mEmailSubLinearLayout1 = (LinearLayout)mEmailLinearLayout1.findViewById(R.id.layout21);
                    mEmailSubLinearLayout1.setVisibility(View.GONE);

                    //email 2
                    mEmailLinearLayout2 = (LinearLayout)findViewById(R.id.ll_emailid2);

                    mEmailTextViewLable2 = (TextView)mEmailLinearLayout2.findViewById(R.id.textview_major);
                    mEmailTextViewLable2.setText(R.string.email2);
                    mEmailTextViewValue2 = (TextView)mEmailLinearLayout2.findViewById(R.id.textview_minor);

                    mEmailSubLinearLayout2 = (LinearLayout)mEmailLinearLayout2.findViewById(R.id.layout21);
                    mEmailSubLinearLayout2.setVisibility(View.GONE);


                    break;
                case 3:
                    mLinearLayoutGroup4 = (LinearLayout)findViewById(R.id.layout_group4);
                    //address
                    mAddressLinearLayout = (LinearLayout)findViewById(R.id.ll_address);
                    mAddressTextViewLable = (TextView)mAddressLinearLayout.findViewById(R.id.textview_major);
                    mAddressTextViewLable.setText(R.string.address);
                    mAddressTextViewValue = (TextView)mAddressLinearLayout.findViewById(R.id.textview_minor);
                    mAddressSubLinearLayout = (LinearLayout)mAddressLinearLayout.findViewById(R.id.layout21);

                    mAddressSubLinearLayout.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }

        }
    }


    private void init() {
        initMyView();
        setData();

        setClickEvent();

       // initMyData();

        //来自slidemenuActivity，必须调用，调用后，会出现左边的菜单。同时，调用setTouchModeAbove，这样，在滑动的时候，
        //就不会显示左侧的menu了。
//        setBehindContentView(R.layout.left_menu_frame);
//        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
    }
    private void setData() {
        Intent intent=this.getIntent();
        Bundle bundle=intent.getExtras();

        sortModel=(SortModel)bundle.getSerializable("sortModel");
        mNameTextView.setText(sortModel.getContactName());
        mCompanyTextView.setText(sortModel.getContactCompany());
        mTelTextView1Value1.setText(sortModel.getCellphone_1());
        mTelTextView1Value2.setText(sortModel.getCellphone_2());
        mEmailTextViewValue1.setText(sortModel.getEmail_1());
        mEmailTextViewValue1.setText(sortModel.getEmail_2());

        mAddressTextViewValue.setText(sortModel.getContactAddress());
        mMiddleHeaderView.setText(sortModel.getContactName());
    }

    private void setClickEvent() {
        mTelImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkTelPhoneNumber(sortModel.getCellphone_1())) {
                    PhoneInfoExtractor.dialing(mContext, sortModel.getCellphone_1());
                }

            }
        });

        mTelImageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkTelPhoneNumber(sortModel.getCellphone_2())) {
                    PhoneInfoExtractor.dialing(mContext, sortModel.getCellphone_2());
                }
            }
        });

        mMsgImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkTelPhoneNumber(sortModel.getCellphone_1())) {
                    PhoneInfoExtractor.sendMsg(mContext, sortModel.getCellphone_1());
                }

            }
        });

        mMsgImageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkTelPhoneNumber(sortModel.getCellphone_2())) {
                    PhoneInfoExtractor.sendMsg(mContext, sortModel.getCellphone_2());
                }
            }
        });


    }

    private boolean checkTelPhoneNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            Toast.makeText(mContext,"号码为空!",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }



}
