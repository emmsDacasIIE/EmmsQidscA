package cn.dacas.emmclient.core.mcm;

import android.os.Environment;
import android.os.FileObserver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.dacas.emmclient.util.QDLog;

public class SDCardFileObserver extends FileObserver {
	private Timer mTimer;
	private Map<String,DeleteFileTimerTask> tasks;
	
	 //mask:指定要监听的事件类型，默认为FileObserver.ALL_EVENTS  
    public SDCardFileObserver(String path, int mask) {  
        super(path, mask);  
        mTimer=new Timer();
        tasks=new HashMap<String,DeleteFileTimerTask>();
    }  

    public SDCardFileObserver(String path) {  
        super(path);  
        mTimer=new Timer();
        tasks=new HashMap<String,DeleteFileTimerTask>();
    }  

    @Override  
    public void onEvent(int event, String path) {  
        final int action = event & FileObserver.ALL_EVENTS;  
        switch (action) {  
        case FileObserver.OPEN:
        	QDLog.d("FileObserver","file open; path="+path);
        	break;
        case FileObserver.CLOSE_NOWRITE:
        	QDLog.d("FileObserver","file close; path: " + path);
        	if (tasks.containsKey(path)) {
        		DeleteFileTimerTask task=tasks.get(path);
        		if (task.cancel()) {
		        	task=new DeleteFileTimerTask(path);
		        	mTimer.schedule(task, 30000);
        		}
        	}
        	else {
        		DeleteFileTimerTask task=new DeleteFileTimerTask(path);
        		tasks.put(path, task);
	        	mTimer.schedule(task, 30000);
        	}
            break;               
       default:
            break;  
        }  
    }  
    
    class DeleteFileTimerTask extends TimerTask{
    	
    	private String path;
    	
		public DeleteFileTimerTask(String path) {
			this.path=path;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String sdpath = Environment.getExternalStorageDirectory().getPath()
					+ "/";
			String tpath = sdpath + "tmp/" + path;
			File f = new File(tpath);
			if (f.exists() && f.isFile()) {
				QDLog.d("FileObserver", "delete tmp file " + path);
				f.delete();
			}
			tasks.remove(path);
		}
    }
	
}
