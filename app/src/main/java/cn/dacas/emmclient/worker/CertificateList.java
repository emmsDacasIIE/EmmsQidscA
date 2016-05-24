package cn.dacas.emmclient.worker;

import java.util.ArrayList;
import java.util.List;

public class CertificateList {
	
	private List<Cert> certList = new ArrayList<CertificateList.Cert>();
	
	public void addCert(String id,String name){
		certList.add(new Cert(id, name));
	}
	
	public int length(){
		return certList.size();
	}
	
	public Cert getCert(int location){
		return certList.get(location);
	}
	
	public class Cert{
		String id;
		String name;
		Cert(String id,String name){
			this.id = id;
			this.name = name;
		}
	}
}
