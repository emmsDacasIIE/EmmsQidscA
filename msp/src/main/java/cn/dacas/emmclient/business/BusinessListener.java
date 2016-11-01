package cn.dacas.emmclient.business;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lenovo on 2015/11/30.
 */
public interface BusinessListener {


    /**
     * 业务分类
     */
    public static enum MSP_Category {
        MAM,
        MDM,
        MCM
    }


    /**
     * 业务处理ID
     */
    public static enum BusinessType {
        BusinessType_RequestToken,
        BusinessType_Login,
        BusinessType_bind,

        BusinessType_AppList,
        BusinessType_DocList,
        BusinessType_ContactsList,

        BusinessType_MessageList,

        BusinessType_getCommands,

        BusinessType_startForwarding,

        BusinessType_UserInfo,

        BusinessType_Common_Unknown

    }

    /**
     * 业务处理结果
     */
    public static enum BusinessResultCode {
        ResultCode_Sucess,
        ResultCode_Error,
        ResultCode_ParserError,
        ResultCode_ConnectError,
        ResultCode_AuthFailureError,

        ResultCode_Login_Fail,
        ResultCode_Login_Goto_BinderSelectorActivity,


        ResultCode_Unknown
    }

    /**
     * @param resCode
     * @param type
     * @param data1
     * @param obj2
     */
    void onBusinessResultJsonArray(BusinessResultCode resCode,BusinessType type, JSONArray data1, Object obj2);
//    public void onBusinessResultStr(BusinessResultCode resCode,BusinessType type, String data1, Object obj2);
//    public void onBusinessResultObj(BusinessResultCode resCode,BusinessType type, Object data1, Object obj2);
     void onBusinessResultJsonObj(BusinessResultCode resCode,BusinessType type, JSONObject data1, Object obj2);


    //备用: 由VolleyError抛出的异常，暂时不用。
    //public void onBusinessResultError(BusinessResultCode resCode,BusinessType type, VolleyError data1, Object obj2);


}
