package cn.dacas.emmclient.webapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import cn.dacas.emmclient.R;

public class CreateShortCutActivity extends Activity {
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        Intent addShortcut;  
        //若是“添加快捷方式”的Action就初始化快捷方式的Intent  
        if (getIntent().getAction()  
                .equals(Intent.ACTION_CREATE_SHORTCUT)) {  
              
            /*初始化添加快捷图标的Intent*/
            addShortcut = new Intent();
            addShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,   
                    "中车企业服务");
              
            Parcelable icon = Intent.ShortcutIconResource.fromContext(  
                    this,R.drawable.home);
            addShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,   
                    icon);
              
            Intent accessWebApp =   
                new Intent("cn.dacas.emmclient.webapp"); 
            accessWebApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            accessWebApp.putExtra("path", "www.baidu.com");
            
            addShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT,  
            		accessWebApp); 
              
            /*设置Result*/  
            //因为Action是由Launcher通过startActivityForResult这个方法发出的。  
            setResult(RESULT_OK,addShortcut);  
        } else {  
            setResult(RESULT_CANCELED);  
        }  
        finish();  
    } 
}
