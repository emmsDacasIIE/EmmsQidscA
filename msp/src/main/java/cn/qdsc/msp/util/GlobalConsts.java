package cn.qdsc.msp.util;

/**
 * Created by lenovo on 2015/12/7.
 */
public class GlobalConsts {


    /** camera used*/
    /**
     * Intent带的参数
     */

    public static final String  Msg_Source = "msg_source";

    public static final int My_MyHead_takePic = 1004;

    public static final int My_MyHead_choicePic = 1005;



    //message相关的定义
    public final static String NEW_MESSAGE = "new_message";

    public final static String GET_MESSAGE = "get_message";

    //my photo path
    public static final String User_HeadPhoto_Path = "head";  //files/head

    //my photo name
    public static final String User_HeadPhoto_Name = "my_head_image";  //files/head

    //保存应用列表的json文件
    public static final String Installed_Apps = "installed_apps.json";
    public static final String Applist = "applist.json";

    //用于msg详情
    public static final int Msg_Detail_Request_Flag = 2001;
    public static final String Msg_Subject = "subject";
    public static final String Msg_Time = "time";
    public static final String Msg_Content = "content";


    //用于app详情
    public static final int App_Detail_Request_Flag = 2002;
    public static final String App_Name = "App_Name";
    public static final String App_Type = "App_Type";
    public static final String App_Size = "App_Size";
    public static final String App_Func = "App_Func";
    public static final String App_Detail = "App_Detail";
    public static final String App_Status = "App_Status";
    public static final String App_Icon_Url = "App_Icon_Url";
    public static final String Pkg_Name = "Pkg_Name";
    public static final String App_File_Name = "App_File_Name";


}
