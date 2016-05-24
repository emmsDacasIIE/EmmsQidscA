package dacas.pkgfetcher;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String[] from={"appName","pkgName"};              //这里是ListView显示内容每一列的列名
    int[] to={R.id.app_name,R.id.pkg_name};   //这里是ListView显示每一列对应的list_item中控件的id

    ArrayList<HashMap<String,String>> list=null;
    HashMap<String,String> map=null;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=(ListView)(findViewById(R.id.list));
    }

    @Override
    protected  void onStart(){
        super.onStart();
        //创建ArrayList对象；
        list=new ArrayList<HashMap<String,String>>();
        ArrayList<String> pkgs=getRunningPkgs();
        for(String pkgName:pkgs){
            map=new HashMap<>();
            map.put("appName","app");
            map.put("pkgName", pkgName);
            list.add(map);
        }
        //创建一个SimpleAdapter对象
        SimpleAdapter adapter=new SimpleAdapter(this,list,R.layout.list_item,from,to);
        listView.setAdapter(adapter);
    }

    private ArrayList<String> getRunningPkgs() {
        ArrayList<String> list=new ArrayList<>();
        android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> ss=am.getRunningServices(20);
        for (ActivityManager.RunningServiceInfo s:ss) {
            list.add(s.service.getClassName());
        }
//        List<ActivityManager.RunningTaskInfo> tasks= am.getRunningTasks(10);
//        for (ActivityManager.RunningTaskInfo task:tasks) {
//            list.add(task.baseActivity.getPackageName());
//        }
        return list;
    }

}
