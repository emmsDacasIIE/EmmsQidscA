package cn.dacas.emmclient.controller.parser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.dacas.emmclient.business.BusinessListener;
import cn.dacas.emmclient.model.McmContactsModel;
import cn.dacas.emmclient.model.McmDocInfoModel;
import cn.dacas.emmclient.model.McmMessageModel;
import cn.dacas.emmclient.util.QDLog;

/**
 * Created by lenovo on 2015/11/30.
 */
public class McmParser extends BaseParser{
    private static final String TAG = "McmParser";

    /**
     * Doc List的解析
     * @param
     * @param
     * @return
     */
    public static List<McmDocInfoModel> parseDocList(BusinessListener.BusinessType businessType, JSONArray jsonArray) throws Exception{
        List<McmDocInfoModel> itemList = new ArrayList<McmDocInfoModel>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = (JSONObject) jsonArray.get(i);

                McmDocInfoModel item = new McmDocInfoModel();
                item.fileId = json.getString("id");
                item.fileName = json.getString("file_name");
                item.url = json.getString("url");
                item.fileRecvTime =  json.getString("updated_at");//   "updated_at";

                itemList.add(item);

            }

//            Collections.sort(pushAppsArrayList);
//            isNetworkOK = true;
//            getDownloadAppMap();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        QDLog.i(TAG,"parseDocList ===02====end=");
        return itemList;



    }
    /**
     * contacts的解析
     * @param
     * @param
     * @return
     */
    public static List<McmContactsModel> parseContactsList(BusinessListener.BusinessType businessType, JSONArray jsonArray) throws Exception{
        List<McmContactsModel> itemList = new ArrayList<McmContactsModel>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject json = (JSONObject) jsonArray.get(i);
                McmContactsModel item = new McmContactsModel();

                item.id = json.getString("id");
                item.name = json.getString("name");

                itemList.add(item);

            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        QDLog.i(TAG,"parseContactsList ===03====end=");
        return itemList;

    }

    /**
     * Message list的解析
     * @param
     * @param
     * @return
     */
    public static List<McmMessageModel> parseMessageList(BusinessListener.BusinessType businessType, JSONArray jsonArray) throws Exception{
        List<McmMessageModel> itemList = new ArrayList<McmMessageModel>();

        try {
            for (int i = jsonArray.length() - 1; i >= 0; i--) {

                JSONObject json = (JSONObject) jsonArray.get(i);
                McmMessageModel item = new McmMessageModel();

                item.id = json.getInt("id");
                item.title = json.getString("title");
                item.content = json.getString("content");
                item.created_at = json.getString("created_at");
                itemList.add(item);
            }

            QDLog.i(TAG,"parseMessageList========" + itemList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        QDLog.i(TAG,"parseMessageList ===03====end=");
        return itemList;

    }

    /**
     * Message command的解析
     * @param
     * @param
     * @return
     */
    public static List<String> parseMessageCommand(BusinessListener.BusinessType businessType, JSONArray jsonArray) throws Exception{
        List<String> itemList = new ArrayList<String>();

        try {
            for (int i =0 ; i < jsonArray.length(); i++) {

                String item = jsonArray.getJSONObject(i).toString();

                itemList.add(item);
            }

            QDLog.i(TAG,"parseMessageList========" + itemList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        QDLog.i(TAG,"parseMessageList ===03====end=");
        return itemList;
    }

    /**
     * startForwarding 的解析
     * @param
     * @param
     * @return
     */
    public static HashMap<Integer, String> parseForwarding(BusinessListener.BusinessType businessType, JSONArray jsonArray) throws Exception{
        HashMap<Integer, String> map = new HashMap<>();

        try {
            for (int i = 0; i < jsonArray.length() ; i++) {
                JSONObject json = (JSONObject) jsonArray.get(i);
                String accessStr = json.getString("secureAccesses");
                JSONArray accessArr = new JSONArray(accessStr);

                for (int idx = 0; idx < accessArr.length(); idx++) {
                    JSONObject obj = (JSONObject) accessArr.get(idx);
                    String localPort = (String) obj.get("localPort");
                    String remoteIp = (String) obj.get("remoteIp");
                    String remotePort = (String) obj.get("remotePort");
                    map.put(Integer.parseInt(localPort), remoteIp + ":"
                            + remotePort);
                }
            }

            QDLog.i(TAG,"parseForwarding========" + map);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        QDLog.i(TAG,"parseForwarding ===03====end=");
        return map;
    }

}
