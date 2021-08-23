/**
 * 
 */
package com.txznet.fm.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.R;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.StringUtils;

/**
 * @desc <pre>
 * 数据库操作帮助类
 * </pre>
 * @author Erich Lee
 * @Date Mar 20, 2013
 */
public class DBUtils {
	private static final String TAG = DBUtils.class.getSimpleName();

	public static final String DATABASE_GLOBE_NAME = "txz.sql";// 全局数据库名

	public static final String DATABASE_GLOBE_SCRIPT_FILE_NAME = "txz.sql";

	public static final int DATABASE_VERSION = 14;// 数据库版本

	public static final String DATABASE_SCRIPT_NOTE = "--";

	public static final String DATABASE_SCRIPT_LINE_END = ";";

	private static final DBUtils INSTANCE = new DBUtils();

	private Context ctx;

	// private DataGlobeHelper dataWriteBaseHelper_globe;
	private DataGlobeHelper dataReadBaseHelper_globe;

	private SQLiteDatabase db_globe;

	private List<String> dbScripts_globe;

	private DBUtils() {
		this.ctx = GlobalContext.get();
		this.initDBScript();
		// dataWriteBaseHelper_globe = new DataGlobeHelper(ctx);
		dataReadBaseHelper_globe = new DataGlobeHelper(ctx);
	}

	public SQLiteOpenHelper getSqliteDataBase() {
		return dataReadBaseHelper_globe;
	}

	/**
	 * 从SQL文件中读取文件流，并执行sql语句，建立tb_company表
	 * 
	 * @Description
	 * @author telenewbie
	 * @May 18, 2015 10:53:27 AM
	 */
	private void initDBScript() {

		dbScripts_globe = new ArrayList<String>();

		BufferedReader br1 = null;

		try {
			br1 = new BufferedReader(new InputStreamReader(GlobalContext.get()
					.getResources().openRawResource(R.raw.txz)));
			String lineText;
			StringBuffer sb = new StringBuffer();
			while ((lineText = br1.readLine()) != null) {
				if (lineText.startsWith(DATABASE_SCRIPT_NOTE)) {
					continue;// 跳过注释
				}
				lineText = lineText.trim();
				if (lineText.endsWith(DATABASE_SCRIPT_LINE_END)) {
					lineText = lineText.substring(0, lineText.length() - 1)
							.trim();
					if (StringUtils.isNotEmpty(lineText)) {
						sb.append(lineText);
						dbScripts_globe.add(sb.toString());
						sb.delete(0, sb.length());
						continue;
					}
				}
				sb.append(lineText);
			}

		} catch (Exception e) {
			throw new RuntimeException("database script error："
					+ e.getMessage(), e);
		} finally {
			try {
				if (br1 != null) {
					br1.close();
				}
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}

		}
		if (CollectionUtils.isEmpty(dbScripts_globe)) {
			throw new RuntimeException("no database scripts!");
		}
	}

	public static DBUtils getInstance() {
		return INSTANCE;
	}

	private class DataGlobeHelper extends SQLiteOpenHelper {

		public DataGlobeHelper(Context context) {
			super(context, DATABASE_GLOBE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "start creating tables ...");
			try {
				for (String sql : dbScripts_globe) {
					db.execSQL(sql);
					Log.d(TAG, sql);
				}
			} catch (SQLException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			Log.d(TAG, "end creating tables ...");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			initDBScript();
			onCreate(db);
		}
	}

	public SQLiteDatabase openGlobe() {
		return dataReadBaseHelper_globe.getWritableDatabase();
	}

	/**
	 * 执行sql语句
	 * 
	 * @param sql
	 */
	public void executeSql(String sql) {
		db_globe = openGlobe();
		db_globe.beginTransaction();
		try {
			db_globe.execSQL(sql);
			db_globe.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.loge(e.getMessage());
		} finally {
			db_globe.endTransaction();
			db_globe.close();
		}
	}

	public SQLiteDatabase openReadGlobe() {
		db_globe = dataReadBaseHelper_globe.getReadableDatabase();
		return db_globe;
	}

}
