package cn.qdsc.msp.controller;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import cn.qdsc.msp.business.BusinessListener;
import cn.qdsc.msp.business.McmBusiness;
import cn.qdsc.msp.business.PushMsgBusiness;
import cn.qdsc.msp.controller.parser.McmParser;
import cn.qdsc.msp.model.McmContactsModel;
import cn.qdsc.msp.model.McmDocInfoModel;
import cn.qdsc.msp.model.McmMessageModel;
import cn.qdsc.msp.util.QDLog;

/**
 * Created by lenovo on 2015/11/30.
 */
public class McmController extends BaseController  implements BusinessListener {

    private final static String TAG = "McmController";

    public McmController(Context context, ControllerListener listener) {
        super(context, listener);

    }

    /**
     * doc列表
     * @param
     * @param
     */
    public void FetchDocList() {
        McmBusiness business = new McmBusiness(mContext,this);
        business.getDocListFromServer();

    }

    /**
     * contacts列表
     * @param
     * @param
     */
    public void FetchContactsList() {
        McmBusiness business = new McmBusiness(mContext,this);
        business.getContactListFromServer();
    }

    /**
     * maxId: 最大消息id
     * message列表
     */

    public void FetchMessageList(int maxId) {
        PushMsgBusiness business = new PushMsgBusiness(mContext,this);
        business.getMessageFromServer(maxId);
    }

    /**
     * message 命令
     */

    public void FetchMessageCommand() {
        PushMsgBusiness business = new PushMsgBusiness(mContext,this);
        business.getCommands();
    }

    /**
     * 开始转发
     */

    public void startForwarding(final String email) {
        PushMsgBusiness business = new PushMsgBusiness(mContext,this);
        business.startForwarding(email);
    }


    /**
     * 需要实现的接口，来自BusinessListener.
     */

    @Override
    public void onBusinessResultJsonArray(BusinessResultCode resCode, BusinessType type, JSONArray data1, Object obj2) {
        QDLog.i(TAG,"onBusinessResultJsonArray==resCode=" +resCode );
        QDLog.i(TAG,"onBusinessResultJsonArray=BusinessType==" +type );
        QDLog.i(TAG,"onBusinessResultJsonArray==data1=" +data1 );


        //请求结果
        if (type == BusinessType.BusinessType_DocList) {

            if (resCode == BusinessResultCode.ResultCode_Sucess) {
                List<McmDocInfoModel> modelList = null;
                try {
                    modelList = McmParser.parseDocList(type, data1);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof JSONException) {
                        mControllerListener.OnNotify(BusinessResultCode.ResultCode_ParserError,type,null,obj2);
                        return;
                    }
                    if (e instanceof Exception) {
                        mControllerListener.OnNotify(BusinessResultCode.ResultCode_Unknown,type,null,obj2);
                        return;
                    }
                }
                if (null != modelList && modelList.size()>0) {
                    //正常Case,有值
                    mControllerListener.OnNotify(resCode,type,modelList,obj2);
                }else if (null != modelList && modelList.size()==0){
                    //正常Case,列表为空
                    mControllerListener.OnNotify(resCode,type,modelList,obj2);

                }else {
                    //理论上，不会走到这个case
                    mControllerListener.OnNotify(resCode,type,null,obj2);
                }

            }else {
                //请求结果返回的是errorResponse
                mControllerListener.OnNotify(resCode,type,null,obj2);
            }

        }

        //请求结果
        if (type == BusinessType.BusinessType_ContactsList) {

            if (resCode == BusinessResultCode.ResultCode_Sucess) {
                List<McmContactsModel> modelList = null;
                try {
                    modelList = McmParser.parseContactsList(type, data1);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof JSONException) {
                        mControllerListener.OnNotify(BusinessResultCode.ResultCode_ParserError,type,null,obj2);
                        return;
                    }
                    if (e instanceof Exception) {
                        mControllerListener.OnNotify(BusinessResultCode.ResultCode_Unknown,type,null,obj2);
                        return;
                    }
                }
                if (null != modelList && modelList.size()>0) {
                    //正常Case,有值
                    mControllerListener.OnNotify(resCode,type,modelList,obj2);
                }else if (null != modelList && modelList.size()==0){
                    //正常Case,列表为空
                    mControllerListener.OnNotify(resCode,type,modelList,obj2);

                }else {
                    //理论上，不会走到这个case
                    mControllerListener.OnNotify(resCode,type,null,obj2);
                }

            }else {
                //请求结果返回的是errorResponse
                mControllerListener.OnNotify(resCode,type,null,obj2);
            }

        }

        //请求结果
        if (type == BusinessType.BusinessType_MessageList) {

            if (resCode == BusinessResultCode.ResultCode_Sucess) {
                List<McmMessageModel> modelList = null;
                try {
                    QDLog.i(TAG,"onBusinessResultJsonArray====001==will parse modelList=" +data1 );
                    modelList = McmParser.parseMessageList(type, data1);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof JSONException) {
                        mControllerListener.OnNotify(BusinessResultCode.ResultCode_ParserError,type,null,obj2);
                        return;
                    }
                    if (e instanceof Exception) {
                        mControllerListener.OnNotify(BusinessResultCode.ResultCode_Unknown,type,null,obj2);
                        return;
                    }
                }
                if (null != modelList && modelList.size()>0) {
                    //正常Case,有值
                    QDLog.i(TAG,"onBusinessResultJsonArray==modelList===002==" +modelList );
                    mControllerListener.OnNotify(resCode,type,modelList,obj2);
                }else if (null != modelList && modelList.size()==0){
                    //正常Case,列表为空
                    QDLog.i(TAG,"onBusinessResultJsonArray==modelList is null===003==" );
                    mControllerListener.OnNotify(resCode,type,modelList,obj2);

                }else {
                    //理论上，不会走到这个case
                    mControllerListener.OnNotify(resCode,type,null,obj2);
                }

            }else {
                //请求结果返回的是errorResponse
                QDLog.i(TAG,"OnNotify==modelList===resCode==" +resCode );
                mControllerListener.OnNotify(resCode,type,null,obj2);
            }
        }

        //请求结果
        if (type == BusinessType.BusinessType_getCommands) {

            if (resCode == BusinessResultCode.ResultCode_Sucess) {
                List<String> modelList = null;
                try {
                    QDLog.i(TAG,"onBusinessResultJsonArray====002==will parse modelList=" +data1 );
                    modelList = McmParser.parseMessageCommand(type, data1);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof JSONException) {
                        mControllerListener.OnNotify(BusinessResultCode.ResultCode_ParserError,type,null,obj2);
                        return;
                    }
                    if (e instanceof Exception) {
                        mControllerListener.OnNotify(BusinessResultCode.ResultCode_Unknown,type,null,obj2);
                        return;
                    }
                }
                if (null != modelList && modelList.size()>0) {
                    //正常Case,有值
                    QDLog.i(TAG,"onBusinessResultJsonArray==modelList===002==" +modelList );
                    mControllerListener.OnNotify(resCode,type,modelList,obj2);
                }else if (null != modelList && modelList.size()==0){
                    //正常Case,列表为空
                    QDLog.i(TAG,"onBusinessResultJsonArray==modelList is null===003==" );
                    mControllerListener.OnNotify(resCode,type,modelList,obj2);

                }else {
                    //理论上，不会走到这个case
                    mControllerListener.OnNotify(resCode,type,null,obj2);
                }

            }else {
                //请求结果返回的是errorResponse
                QDLog.i(TAG,"OnNotify==modelList===resCode==" +resCode );
                mControllerListener.OnNotify(resCode,type,null,obj2);
            }
        }

        //请求结果
        if (type == BusinessType.BusinessType_startForwarding) {

            if (resCode == BusinessResultCode.ResultCode_Sucess) {
                HashMap<Integer, String> map = null;
                try {
                    QDLog.i(TAG,"onBusinessResultJsonArray====003==will parse modelList=" +data1 );
                    map = McmParser.parseForwarding(type, data1);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof JSONException) {
                        mControllerListener.OnNotify(BusinessResultCode.ResultCode_ParserError,type,null,obj2);
                        return;
                    }
                    if (e instanceof Exception) {
                        mControllerListener.OnNotify(BusinessResultCode.ResultCode_Unknown,type,null,obj2);
                        return;
                    }
                }
                if (null != map && map.size()>0) {
                    //正常Case,有值
                    QDLog.i(TAG,"onBusinessResultJsonArray==modelList===002==" +map );
                    mControllerListener.OnNotify(resCode,type,map,obj2);
                }else if (null != map && map.size()==0){
                    //正常Case,列表为空
                    QDLog.i(TAG,"onBusinessResultJsonArray==modelList is null===003==" );
                    mControllerListener.OnNotify(resCode,type,map,obj2);

                }else {
                    //理论上，不会走到这个case
                    mControllerListener.OnNotify(resCode,type,null,obj2);
                }

            }else {
                //请求结果返回的是errorResponse
                QDLog.i(TAG,"OnNotify==map===resCode==" +resCode );
                mControllerListener.OnNotify(resCode,type,null,obj2);
            }
        }
    }

    @Override
    public void onBusinessResultJsonObj(BusinessResultCode resCode, BusinessType type, JSONObject data1, Object obj2) {

    }

}
