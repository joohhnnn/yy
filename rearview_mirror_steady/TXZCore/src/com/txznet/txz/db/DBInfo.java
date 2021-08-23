package com.txznet.txz.db;

import com.txz.ui.map.UiMap;

import android.provider.BaseColumns;

public final class DBInfo {
	public static final class Table {
		public static final class AppInfo implements BaseColumns{
			public static final String TABLE_NAME = "app_info";
			public static final String VERSION = "version";
			public static final String COPYRIGHT = "copyright";
			
			public static final String CREATE_TABLE = 
					"CREATE TABLE IF NOT EXISTS "
                            + TABLE_NAME + " ( "
                            + _ID + " integer PRIMARY KEY AUTOINCREMENT, "
                            + VERSION + " varchar(255) NOT NULL, "
                            + COPYRIGHT + " varchar(255))";
			
			public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
		}
		
		public static final class NavHistory /*implements BaseColumns*/{
			public static final String TABLE_NAME = "nav_history";
			public static final String KEY_OPERA_TIME = "Opera_Time";
			public static final String KEY_OPERA_STATUS = "Opera_Status";
//			public static final String KEY_ACTIVE_STATUS = "Active_Status";
			public static final String KEY_SAME_FIELD_BEGIN = "beginCompareField";
			public static final String KEY_SAME_FIELD_END = "endCompareField";
			public static final String KEY_START_INFO = "Start_Info";
			public static final String KEY_END_INFO = "End_Info";
			
			public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
					+ TABLE_NAME + " ( "
					+ KEY_OPERA_TIME + " integer PRIMARY KEY NOT NULL,"
					+ KEY_OPERA_STATUS + " integer,"
					+ KEY_SAME_FIELD_BEGIN + " varchar(255),"
					+ KEY_SAME_FIELD_END + " varchar(255),"
					+ KEY_START_INFO + " varchar(255) NOT NULL,"
					+ KEY_END_INFO + " varchar(255) NOT NULL)";
			public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
			
			public static final String AUTO_LIMIT_TRIGGER_NAME = "AUTO_LIMIT_TRIGGER";

			public static final String TRIGGER_SQL = "DELETE FROM " + TABLE_NAME + " WHERE " + KEY_OPERA_TIME + "=(SELECT MIN("
					+ KEY_OPERA_TIME + ") FROM " + TABLE_NAME + ");";

			public static final String QUERY_SQL_CONDITION = "";

			public static final String INNSERT_LIMIT_TRIGGER = "CREATE TRIGGER " + AUTO_LIMIT_TRIGGER_NAME + " BEFORE INSERT ON "
					+ TABLE_NAME + " WHEN (select count(*) from " + TABLE_NAME + QUERY_SQL_CONDITION + ")>%SIZE% BEGIN "
					+ TRIGGER_SQL + " END;";

			public static final String QUERY_TRIGGER = "SELECT name FROM sqlite_master WHERE type = 'trigger'";
			
			public static final String QUERY_SQL = "SELECT " + KEY_OPERA_TIME + "," + KEY_OPERA_STATUS + ","
					+ KEY_SAME_FIELD_BEGIN + "," + KEY_SAME_FIELD_END + "," + KEY_START_INFO + "," + KEY_END_INFO
					+ " FROM " + TABLE_NAME + " WHERE  whereArgs  ORDER BY " + KEY_OPERA_TIME + " DESC";

			///////////////////////////////////////////////////////////////////////////////////////////////////
			public static final String AUTO_DELETE_TRIGGER_NAME = "AUTO_DELETE_TRIGGER";

			public static final String AUTO_DELETE_TRIGGER_SQL = "DELETE FROM " + TABLE_NAME + " WHERE "
					+ KEY_OPERA_STATUS + "&" + UiMap.HIDE_BEGIN_ADDRESS + "=" + UiMap.HIDE_BEGIN_ADDRESS + " AND "
					+ KEY_OPERA_STATUS + "&" + UiMap.HIDE_END_ADDRESS + "=" + UiMap.HIDE_END_ADDRESS + " AND "
					+ KEY_OPERA_STATUS + "&" + UiMap.ADDED + "!=" + UiMap.ADDED + ";";

			public static final String AUTO_DELETE_TRIGGER = "CREATE TRIGGER " + AUTO_DELETE_TRIGGER_NAME
					+ " AFTER UPDATE ON " + TABLE_NAME + " BEGIN " + AUTO_DELETE_TRIGGER_SQL
					+ " END";
		}
		}
	}
