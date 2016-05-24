package cn.qdsc.msp.core.forward;
import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class IForward {
	//转发服务器的地址和端口号
	public static final int FORWARD_PORT = 3546;
	
	private HashMap<Integer, String> localPort2Addr = null;
	private List<ATrans> transactions = new ArrayList<ATrans>();
	
	@SuppressLint("UseSparseArrays")
	public IForward(){
		localPort2Addr = new HashMap<Integer, String>(); 
	}
	
	public void addMapping(HashMap<Integer, String> map){
		Set<Integer> keys = map.keySet();
		
		if(keys.size() <= 0){
			return;
		}
		
		for(Integer key:keys){
			if(!localPort2Addr.containsKey(key)){
				//start listening
				String[] server = map.get(key).split(":");
				if(server.length == 2){
					localPort2Addr.put(key, map.get(key));
					ATrans trans = new ATrans(key, server[0], Integer.parseInt(server[1]));
					transactions.add(trans);
					trans.execute();
				}
			}
		}
	}
	
	public void stopMapping(){
		for(ATrans trans:transactions){
			try{
				trans.stopExecution();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		transactions.clear();
		localPort2Addr.clear();
	}
}
