package cn.dacas.emmclient.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.umeng.analytics.MobclickAgent;

import cn.dacas.emmclient.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class CertificateSettingsActivity extends Activity {

	private final int ADD_USER_CERT = 0;

	private ListView certListView;
	private SimpleAdapter adapter;
	ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_certificate_settings);
		setTitle("证书管理");
		certListView = (ListView) findViewById(R.id.listView1);
		getCertList();

		adapter = new SimpleAdapter(this, mData,
				android.R.layout.simple_list_item_2, new String[] { "title",
						"installed" }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		certListView.setAdapter(adapter);

		certListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String name = (String) mData.get(position).get("title");
				String installed = (String) mData.get(position)
						.get("installed");

				if (installed.equals("已安装")) {
					return;
				} else {
					Intent intent = new Intent("android.credentials.INSTALL");
					File file = new File(getDir("cert", MODE_PRIVATE)
							.getAbsolutePath() + "/" + name);
					InputStream iStream = null;
					byte[] result = new byte[(int) file.length()];
					try {
						iStream = new FileInputStream(file);
						iStream.read(result);
						iStream.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					intent.putExtra("name", name);
					intent.putExtra("CERT", result);
					startActivityForResult(intent, ADD_USER_CERT);
				}

			}
		});
	}
	
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void getCertList() {
		// 需要安装的cert
		mData.clear();
		File certDirFile = this.getDir("cert", MODE_PRIVATE);
		if (!certDirFile.exists()) {
			certDirFile.mkdir();
			return;
		}
		certDirFile.setReadable(true);
		certDirFile.setWritable(true);
		File[] newCertList = certDirFile.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				if (filename.lastIndexOf(".cer") != -1
						|| filename.lastIndexOf(".crt") != -1)
					return true;
				return false;
			}
		});

		
		if (newCertList == null)
			return;
		// 已安装的cert
		File installedCertDirFile = new File(System.getenv("ANDROID_DATA")
				+ "/misc/keychain/cacerts-added");
		File[] installedCertList = installedCertDirFile.listFiles();

		// 比对获取未安装的cert
		for (File nF : newCertList) {
			Map<String, Object> item = new HashMap<String, Object>();
			String name = nF.getName();

			item.put("title", name);
			item.put("installed", "未安装");

			if (installedCertList != null) {
				try {
					// nF.setReadable(true);
					// nF.setWritable(true);
					InputStream newCert = new FileInputStream(nF);
					byte newCertbytes[] = new byte[(int) nF.length()];
					newCert.read(newCertbytes);
					newCert.close();
					for (File iF : installedCertList) {
						InputStream installedCert = new FileInputStream(iF);
						byte installedCertbytes[] = new byte[(int) iF.length()];
						installedCert.read(installedCertbytes);
						installedCert.close();
						if (Arrays.equals(installedCertbytes, newCertbytes)) {
							item.put("installed", "已安装");
							break;
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			mData.add(item);
		}
	}

	public void fileChannelCopy(File s, File t) {

		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			fi = new FileInputStream(s);
			fo = new FileOutputStream(t);
			in = fi.getChannel();// 得到对应的文件通道
			out = fo.getChannel();// 得到对应的文件通道
			in.transferTo(0, in.size(), out);// 连接两个通道，并且从in通道读取，然后写入out通道
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fi.close();
				in.close();
				fo.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ADD_USER_CERT && resultCode == RESULT_OK) {
			getCertList();
			adapter.notifyDataSetChanged();
		}
	}

}
