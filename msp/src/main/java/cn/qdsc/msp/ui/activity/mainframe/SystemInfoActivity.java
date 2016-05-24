package cn.qdsc.msp.ui.activity.mainframe;

import android.os.Bundle;
import android.widget.TextView;

import cn.qdsc.msp.R;
import cn.qdsc.msp.ui.activity.base.BaseSlidingFragmentActivity;
import cn.qdsc.msp.util.PhoneInfoExtractor;

public class SystemInfoActivity extends BaseSlidingFragmentActivity {

    TextView text_info_mac;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_info, "");
        initView();
    }

    @Override
    protected HearderView_Style setHeaderViewSyle() {
        return HearderView_Style.Image_Text_Null;
    }

    private void initView() {
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);
        mMiddleHeaderView.setText(mContext.getString(R.string.my_system_info));
        mMiddleHeaderView.setImageVisibile(false);
        text_info_mac=(TextView)findViewById(R.id.text_info_mac);
        String mac= PhoneInfoExtractor.getPhoneInfoExtractor(mContext).getMacAddress();
        text_info_mac.setText(mac);
    }

}
