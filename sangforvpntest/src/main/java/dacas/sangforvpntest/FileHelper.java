package dacas.sangforvpntest;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileHelper {
	private final static String TAG = FileHelper.class.getName();
	private final static String TEST_DATA = "hello file hook!";

	public void testFileHook() {
		File file1 = new File("/sdcard/csh.txt");
		try {
			if (!file1.exists()) {
				file1.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file1);
			fos.write(TEST_DATA.getBytes());
			fos.flush();
			fos.close();
			Log.i(TAG, "success output data to file");
		} catch (Exception e1) {
			Log.e(TAG, e1.getMessage());
			return;
		}

		File file2 = new File("/sdcard/csh.txt");
		StringBuffer sb = new StringBuffer();
		try {
			FileInputStream fis = new FileInputStream(file2);
			int b = 0;
			while ((b = fis.read()) != -1) {
				sb.append((char) b);
			}
			fis.close();
			Log.i(TAG, "read data is :" + sb.toString());
		} catch (Exception e2) {
			Log.e(TAG, e2.getMessage());
		}
	}
}
