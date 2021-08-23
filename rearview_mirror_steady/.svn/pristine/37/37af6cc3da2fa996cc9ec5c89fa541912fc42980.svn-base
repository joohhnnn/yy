package com.txznet.feedback.db;

import android.database.sqlite.SQLiteDatabase;

import com.txznet.sqlite.SqliteDao;

public class SqliteDaoImpl implements SqliteDao {

	// 更改数据库版本时应手动修改版本号
	private static final int DATABASE_VERSION = 1;

	private static SqliteDaoImpl instance = new SqliteDaoImpl();

	private SqliteDaoImpl() {

	}

	public static SqliteDaoImpl getInstance() {
		return instance;
	}

	@Override
	public void debug(String arg0, String arg1) {
	}

	@Override
	public int getLastDatabaseVersion() {
		return DATABASE_VERSION;
	}

	@Override
	public void updateGradeDatabase(SQLiteDatabase arg0, int arg1, int arg2) {
	}
}
