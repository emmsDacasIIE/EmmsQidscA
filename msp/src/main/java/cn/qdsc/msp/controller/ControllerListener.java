package cn.qdsc.msp.controller;

import cn.qdsc.msp.business.BusinessListener;

/**
 * Created by lenovo on 2015/11/30.
 */
public interface ControllerListener {

    public void OnNotify(BusinessListener.BusinessResultCode resCode, BusinessListener.BusinessType type, Object data1, Object data2);
}
