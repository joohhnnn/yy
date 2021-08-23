package com.txznet.txz.db;

import com.txznet.txz.db.DBInfo.Table.AppInfo;

import android.database.sqlite.SQLiteDatabase;

public class SQLiteRawUtil {

	public static void insertAppInfo(SQLiteDatabase db, String version, String copyright) {
		try {
			db.execSQL("REPLACE INTO " + DBInfo.Table.AppInfo.TABLE_NAME + "(" + AppInfo._ID + ", " + AppInfo.VERSION
					+ ", " + AppInfo.COPYRIGHT + ") VALUES(0, '" + version + "','" + copyright + "')");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
