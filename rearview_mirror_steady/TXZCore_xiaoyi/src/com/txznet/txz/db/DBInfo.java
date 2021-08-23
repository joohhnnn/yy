package com.txznet.txz.db;

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
	}
}
