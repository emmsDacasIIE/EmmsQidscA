package cn.qdsc.msp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/*
 * 为了方便，在查询或更新时采用的是String[]，所以定义表项时采用text
 */
/*
 * 1: 执行操作历史
 *  _id integer, code text not null,content text not null, state text not null, time text not null 
 *  
 *  code表示操作代码，以字符串的形式表达
 *  content表示操作附带的数据，如应用卸载操作
 *  state表示当前操作的状态，暂定义STATE_READY、STATE_SUCCESS两种
 *  time表示接收到操作指令的时间
 *  
 *  2: 应用程序黑名单
 *  _id integer, pkgname text not null, time text not null
 *  
 *  appname表示待删除的应用
 *  time表示接收到黑名单的时间
 *  
 *  一旦黑名单中的应用卸载，删除该表中的条目
 *  
 *  3: 设备消息
 *  _id integer, msg text not null, time text not null
 *  
 *  msg 消息内容
 *  time 消息时间
 *  
 *  4: 企业文档管理
 *  _id integer, filetag text not null, path text not null, time text not null, sender text not null
 *  
 *  filetag表示在发送企业文档时的附加说明
 *  path为url表示只是接收到了文档通知，并没有真正获取
 *  path为文件系统路径表示该文件确实已经接收
 *  
 *  5:企业应用推送
 *  _id integer, apptag text not null, path text not null, time text not null, sender text not null
 *  
 *  apptag在应用推送时添加的附件说明
 *  path为url或者app存放的地址
 *  
 */

public class EmmClientDb {
	public static final String KEY_ROWID = "_id";

	public static final String STATE_READY = "r";
	public static final String STATE_SUCCESS = "s";

	private static int currentVersion = 1;

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String ACTIONLOG_DATABASE_CREATE = "create table actionlogtable (_id integer primary key autoincrement, "
			+ "code text not null, content text not null, state text not null, time text not null);";

	private static final String APPBLACK_DATABASE_CREATE = "create table appblacktable (_id integer primary key autoincrement, "
			+ "pkgname text not null, time text not null);";

	/**
	 * readed: 0: 未读；1：已读
	 */
	private static final String DEVICEMSG_DATABASE_CREATE = "create table devicemsgtable (_id integer primary key autoincrement, "
			+ "title text not null,msg text not null, time text not null,readed integer not null default 0);";
	private static final String CORPFILE_DATABASE_CREATE = "create table corpfiletable (_id integer primary key autoincrement, "
			+ "filetag text not null, isnative text not null, url text, path text, time text not null, sender text not null,fav integer not null default 0,len integer not null default 0);";
	private static final String CONTACT_DATABASE_CREATE = "create table contacttable (_id integer primary key ,"
			+ "name text not null, telephone text, cellphone_1 text, cellphone_2 text, email_1 text, email_2 text, company text, address text);";
//	private static final String CONTACT_DATABASE_CREATE = "create table contacttable ("
//			+ "name text not null, tel text primary key not null, email text, company text, address text);";

	//在安全工作区显示的app
	//time:安装或者更新的时间
	private static final String APPLIST_DATABASE_CREATE = "create table applisttable (_id integer primary key autoincrement, "
			+ "package text not null,label text not null,sender text not null,version_name text,version_code integer,page integer,position integer);";

	private static final String PASSWORD_DATABASE_CREATE = "create table passwordtable (_id integer primary key autoincrement, email text not null,"
			+ "name text not null,wordspwd text not null, patternpwd text, pwdtype integer not null default 0);";
	private static final String DATABASE_NAME = "EmmClientDb";

	public static final String ACTIONLOG_DATABASE_TABLE = "actionlogtable";
	public static final String APPBLACK_DATABASE_TABLE = "appblacktable";
	public static final String DEVICEMSG_DATABASE_TABLE = "devicemsgtable";
	public static final String CORPFILE_DATABASE_TABLE = "corpfiletable";
	public static final String CONTACT_DATABASE_TABLE = "contacttable";
	public static final String PASSWORD_DATABASE_TABLE = "passwordtable";
	public static final String APPLIST_DATABASE_TABLE = "applisttable";

	private static final int DATABASE_VERSION = 2;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(ACTIONLOG_DATABASE_CREATE);
			db.execSQL(APPBLACK_DATABASE_CREATE);
			db.execSQL(DEVICEMSG_DATABASE_CREATE);
			db.execSQL(CORPFILE_DATABASE_CREATE);
			db.execSQL(CONTACT_DATABASE_CREATE);
			db.execSQL(PASSWORD_DATABASE_CREATE);
			db.execSQL(APPLIST_DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//			db.execSQL("DROP TABLE IF EXISTS " + ACTIONLOG_DATABASE_TABLE);
//			db.execSQL("DROP TABLE IF EXISTS " + APPBLACK_DATABASE_TABLE);
//			db.execSQL("DROP TABLE IF EXISTS " + DEVICEMSG_DATABASE_TABLE);
//			db.execSQL("DROP TABLE IF EXISTS " + CORPFILE_DATABASE_TABLE);
//			db.execSQL("DROP TABLE IF EXISTS " + CONTACT_DATABASE_TABLE);
//			db.execSQL("DROP TABLE IF EXISTS " + PASSWORD_DATABASE_CREATE);
			
			db.execSQL("DELETE FROM "+ACTIONLOG_DATABASE_TABLE);
			db.execSQL("DELETE FROM "+DEVICEMSG_DATABASE_TABLE);
			db.execSQL("DELETE FROM "+CORPFILE_DATABASE_TABLE);
			db.execSQL("DELETE FROM "+CONTACT_DATABASE_TABLE);
			db.execSQL("DELETE FROM "+APPLIST_DATABASE_TABLE);
			
//			onCreate(db);
		}
	}

	public EmmClientDb(Context ctx) {
		this.mCtx = ctx;
	}

	public EmmClientDb open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void closeclose() {
		mDbHelper.close();
	}

	public void upgrade() {
		mDbHelper.onUpgrade(mDb, currentVersion, ++currentVersion);
	}

	
	public long addDbItem(String tablename, String[] columnnames,
			String[] columnvalues) {
		if (columnnames.length != columnvalues.length) {
			return -1;
		}
		if ((columnvalues == null) || (columnnames == null)
				|| (columnnames.length == 0)) {
			return -1;
		}
		ContentValues initialValues = new ContentValues();
		for (int idx = 0; idx < columnnames.length; idx++) {
			initialValues.put(columnnames[idx], columnvalues[idx]);
		}
		return mDb.insert(tablename, null, initialValues);
	}

	public boolean deleteDbItemById(String tablename, long rowId) {
		return mDb.delete(tablename, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean deleteAllDbItem(String tablename) {
		int count = mDb.delete(tablename, null, null);
		return count > 0;
	}

	public boolean deleteDbItemBycolumn(String tablename, String columnname,
			String value) {
		return mDb.delete(tablename, columnname + "=" + value, null) > 0;
	}

	public boolean deleteDbItemBycolumns(String tablename,
			String[] columnnames, String[] values) {
		StringBuilder sb = new StringBuilder(columnnames[0] + "=?");
		for (int idx = 1; idx < columnnames.length; idx++) {
			sb.append(" and " + columnnames[idx] + "=?");
		}

		return mDb.delete(tablename, sb.toString(), values) > 0;
	}

	// 返回值为null，或者至少包含一条记录
	public Cursor getAllItemsOfTable(String tablename, String[] requestColumns) {
		Cursor mCursor = mDb.query(tablename, requestColumns, null, null, null,
				null, null);
		if ((mCursor == null) || (mCursor.getCount() <= 0)) {
			return null;
		} else {
			mCursor.moveToFirst();
			return mCursor;
		}
	}

	// 返回值为null，或者至少包含一条数据
	public Cursor getItemsOfTableById(String tablename, long id,
			String[] requestColumns) {
		Cursor mCursor = mDb.query(true, tablename, requestColumns, KEY_ROWID
				+ "=" + id, null, null, null, null, null);
		if ((mCursor == null) || (mCursor.getCount() <= 0)) {
			return null;
		} else {
			mCursor.moveToFirst();
			return mCursor;
		}
	}
	

	// 返回值为null，或者至少包含一条数据
	public Cursor getItemByInfo(String tablename, String[] columnnames,
			String[] columnvalues, String[] requestcolumns) {
		if ((columnvalues == null) || (columnnames == null)
				|| (columnnames.length == 0)) {
			return null;
		}
		if (columnnames.length != columnvalues.length) {
			return null;
		}

		StringBuilder sb = new StringBuilder(columnnames[0] + "=?");
		for (int idx = 1; idx < columnnames.length; idx++) {
			sb.append(" and " + columnnames[idx] + "=?");
		}

		Cursor mCursor = mDb.query(true, tablename, requestcolumns,
				sb.toString(), columnvalues, null, null, null, null);
		if ((mCursor == null) || (mCursor.getCount() <= 0)) {
			return null;
		} else {
			mCursor.moveToFirst();
			return mCursor;
		}
	}

	public int getItemCountByInfo(String tablename, String[] columnnames,
			String[] columnvalues, String[] requestcolumns) {
		Cursor mCursor = getItemByInfo(tablename, columnnames, columnvalues,
                requestcolumns);
		if (mCursor == null) {
			return 0;
		} else {
			return mCursor.getCount();
		}
	}
	

	// 此处如果多条记录满足查询条件，则只更新第一条记录
	public boolean updateOrInsertItemByInfo(String tablename,
			String[] querynames, String[] queryvalues, String[] columnnames,
			String[] columnvalues) {
		Cursor mCursor = getItemByInfo(tablename, querynames, queryvalues, null);

		if (mCursor == null) {
			if (addDbItem(tablename, columnnames, columnvalues) == -1) {
				return false;
			} else {
				return true;
			}
		} else {
			int rowId = mCursor
					.getInt(mCursor.getColumnIndexOrThrow(KEY_ROWID));
			return updateDbItemById(tablename, rowId, columnnames, columnvalues);
		}
	}

	public boolean updateDbItemById(String tablename, int rowId,
			String[] columnnames, String[] columnvalues) {
		if ((columnvalues == null) || (columnnames == null)
				|| (columnnames.length == 0)) {
			return false;
		}
		if (columnnames.length != columnvalues.length) {
			return false;
		}
		ContentValues args = new ContentValues();
		for (int idx = 0; idx < columnnames.length; idx++) {
			args.put(columnnames[idx], columnvalues[idx]);
		}
		return mDb.update(tablename, args, KEY_ROWID + "=" + rowId, null) > 0;
	}


    public void clearCorpData() {
        upgrade();
    }
	
}
