package dacas.sangforvpntest;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 数据库操作工具类
 * 
 * 
 */
public class DBHelper {
	private static final String TAG = DBHelper.class.getName();// 调试标签
	private static final String DATABASE_NAME = "/sdcard/dbdemo.db";// 数据库名
	
	private SQLiteDatabase mDB; // sqlite数据库
	private Context mContext;// 应用环境上下文 Activity 是其子类

	public DBHelper(Context _context) {
		mContext = _context;
		mDB = mContext.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
		Log.i(TAG, "db path=" + mDB.getPath());
	}

	/**
	 * 建表 列名 区分大小写？ 都有什么数据类型？ SQLite 3 TEXT 文本 NUMERIC 数值 INTEGER 整型 REAL 小数
	 * NONE 无类型 查询可否发送select ?
	 */
	public void CreateTable() {
		try {
			mDB.execSQL("CREATE TABLE t_user (" + "_ID INTEGER PRIMARY KEY autoincrement,"
					+ "NAME TEXT," + "PASSWORD VARCHAR" + ");");
			Log.i(TAG, "Create Table t_user ok");
		} catch (Exception e) {
			Log.e(TAG, "Create Table t_user err, table exists.");
		}
	}

	/**
	 * 增加数据
	 * 
	 * @param id
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean save(String username, String password) {
		String sql = "";
		try {
			sql = "insert into t_user values(null,'" + username + "','" + password + "')";
			mDB.execSQL(sql);
			Log.i(TAG, "insert Table t_user ok");
		} catch (Exception e) {
			Log.e(TAG, "insert Table t_user err ,sql: " + sql);
			return false;
		}
		
		return true;
	}

	/**
	 * 查询所有记录
	 * 
	 * @return Cursor 指向结果记录的指针，类似于JDBC 的 ResultSet
	 */
	public String dumpAllUser() {
		Cursor cur = mDB.query("t_user", new String[] { "_ID", "NAME", "PASSWORD" }, null, null, null, null,
				null);
		
		StringBuffer sb = new StringBuffer();
		final int idIndex = cur.getColumnIndex("_ID");
		final int userIndex = cur.getColumnIndex("NAME");
		final int pwdIndex  = cur.getColumnIndex("PASSWORD");
		sb.append("[ ");
		for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			int id = cur.getInt(idIndex);
			String username = cur.getString(userIndex);
			String password = cur.getString(pwdIndex);
			sb.append("id:" + id + ", ");
			sb.append("username:" + username + ", ");
			sb.append("password:" + password + " ");
		}
		sb.append("]");
		
		Log.i(TAG, "load result is : " + sb.toString());
		return sb.toString();
	}

	public void close() {
		mDB.close();
	}
}
