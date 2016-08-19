package cn.qdsc.msp.core.mdm;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by Sun RX on 2016-8-19.
 * This class is based on BaiDuLocationSDK 7.0
 */
public class LocationService {
    private LocationClient mLocationClient;

    /**
     * 初始化定位服务
     * @param context
     * @param mMyLocationListener 自定义的定位监听函数，定义了当得到新定位之后要进行什么操作
     */
    public LocationService(Context context, BDLocationListener mMyLocationListener){
        mLocationClient = new LocationClient(context);
        mLocationClient.registerLocationListener(mMyLocationListener);
        mLocationClient.setLocOption(getOption());
    }

    /**
     * 设置定位配置模式，并返回Option类型供setLocOption使用。
     * @return LocationClientOption
     */
    LocationClientOption getOption(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(60 * 1000);// 设置发起定位请求的间隔时间为1分钟
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
        return option;
    }

    /**
     * @return a lcationClient which has been set (LocOption and LocationListener)
     */
    public LocationClient getLocationClient(){
        return mLocationClient;
    }
}
