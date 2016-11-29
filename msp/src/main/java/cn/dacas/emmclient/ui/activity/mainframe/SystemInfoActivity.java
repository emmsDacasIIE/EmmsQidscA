package cn.dacas.emmclient.ui.activity.mainframe;

import android.os.Bundle;
import android.widget.TextView;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.core.EmmClientApplication;
import cn.dacas.emmclient.ui.activity.base.BaseSlidingFragmentActivity;
import cn.dacas.emmclient.util.PhoneInfoExtractor;

public class SystemInfoActivity extends BaseSlidingFragmentActivity {

    TextView text_info_mac, text_device_name, text_device_type;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_info, "");
        initView();
    }

    @Override
    protected HearderView_Style setHeaderViewStyle() {
        return HearderView_Style.Image_Text_Null;
    }

    private void initView() {
        mLeftHeaderView.setImageView(R.mipmap.msp_titlebar_leftarrow_icon);
        mMiddleHeaderView.setText(mContext.getString(R.string.my_system_info));
        mMiddleHeaderView.setImageVisibile(false);
        text_info_mac=(TextView)findViewById(R.id.text_info_mac);
        text_device_name = (TextView)findViewById(R.id.text_info_device_name);
        text_device_type = (TextView) findViewById(R.id.text_info_device_type);
        String mac= PhoneInfoExtractor.getPhoneInfoExtractor(mContext).getMacAddress();

        text_info_mac.setText(mac);
        if(EmmClientApplication.mDeviceModel!=null) {
            text_device_name.setText(EmmClientApplication.mDeviceModel.getName());
            text_device_type.setText(EmmClientApplication.mDeviceModel.getType());
        }

    }

}
