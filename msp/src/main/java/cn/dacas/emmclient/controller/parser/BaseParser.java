package cn.dacas.emmclient.controller.parser;

import cn.dacas.emmclient.business.BusinessListener;

/**
 * Created by lenovo on 2015/11/30.
 */
public class BaseParser {

    private static BusinessListener.BusinessResultCode mBusinessResultCode;

    public static void setBusinessResultCode(BusinessListener.BusinessResultCode resultCode) {
        mBusinessResultCode = resultCode;
    }

    public static BusinessListener.BusinessResultCode getBusinessResultCode(BusinessListener.BusinessResultCode resultCode) {
        return mBusinessResultCode;
    }


}
