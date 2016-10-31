package cn.qdsc.msp.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * LOG的类, 可以控制代码的是否打印
 * @author lenovo
 */
public class QDLog {
	private static boolean isOpenLog = true;


	public static void e(String tag, String msg) {
		if (isOpenLog)
			Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable e) {
		if (isOpenLog)
			Log.e(tag, msg, e);
	}

	public static void i(String tag, String msg) {
		if (isOpenLog)
			Log.i(tag, msg);
	}

	public static void d(String tag, String msg) {
		if (isOpenLog)
			Log.d(tag, msg);
	}

	public static void v(String tag, String msg) {
		if (isOpenLog)
			Log.v(tag, msg);
	}
	
	public static void w(String tag, String msg) {
		if (isOpenLog)
			Log.w(tag, msg);
	}

	public static void f(String msg) {
		if (isOpenLog) {
			try {
				//String file = mContext.getCacheDir() + File.separator + "log.txt";
				String file = "/sdcard/" + "log.txt";
				FileWriter writer = new FileWriter(file, true);

				SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				String log = String.format("%s\t%s\r\n", formatter.format(new Date()), msg);
				writer.write(log);
				writer.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void print(String msg) {
		try {
			String file = "/sdcard/qdlog.out";
			FileWriter writer = new FileWriter(file, true);

			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy/MM/dd HH:mm:ss");
			String log = String.format("%s\t%s\r\n",
					formatter.format(new Date()), msg);
			writer.write(log);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void println(String tag, String msg) {
		try {
//			String file = "/sdcard/qdlog.out";

			String file = SdcardManager.getSdcardPath();
			String logPath = file + File.separator+ "qdlog.out";


			FileWriter writer = new FileWriter(logPath, true);

			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy/MM/dd HH:mm:ss");
			String log = String.format("%s\t%s\r\n",
					formatter.format(new Date()), msg);
			writer.write(tag + " "+ log);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeMsgPushLog(String msg) {
		try {
			String logPath = Environment.getExternalStorageDirectory().getPath() + "/msp/MsgPushLog.txt";
			File dir=new File(Environment.getExternalStorageDirectory().getPath() + "/msp");
			if (!dir.exists())
				dir.mkdirs();
			FileWriter writer = new FileWriter(logPath, true);
			DateFormat df = new SimpleDateFormat("HH:mm:ss");
			String time=df.format(new Date());
			writer.write(time+" === " + msg+"\r\n\r\n");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
