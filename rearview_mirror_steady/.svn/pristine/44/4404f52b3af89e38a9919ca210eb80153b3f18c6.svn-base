package com.txznet.feedback.dtool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DBNAME = "feedback.db";

	public DBHelper(Context context) {
		super(context, DBNAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table t_msg("
				+ "_id LONG PRIMARY KEY,"
				+ "_type integer not null,"
				+ "_msg varchar(1024),"
				+ "_note varchar(10),"
				+ "_time varchar(32),"
				+ "_read integer)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
