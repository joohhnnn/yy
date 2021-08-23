package com.txznet.txz.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class AppInfoProvider extends ContentProvider {
	private static UriMatcher matcher;
	private static final String AUTHORITIES = "com.txznet.txz.provider.AppInfo";
	private static final int APP_INFO = 0;
	private SQLiteHelper mSQLiteHelper;

	public static AppInfoProvider sInstance = null;	
	
	public static void release () {
		if (sInstance != null) {
			sInstance.shutdown();
		}
	}


	@Override
	public boolean onCreate() {
		sInstance = this;
	
		if(matcher == null) {
			matcher = new UriMatcher(UriMatcher.NO_MATCH);
			matcher.addURI(AUTHORITIES, "info", APP_INFO);
		}
		
		mSQLiteHelper = new SQLiteHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mSQLiteHelper.getReadableDatabase();
		switch (matcher.match(uri)) {
		case APP_INFO:
			return db.query(DBInfo.Table.AppInfo.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		default:
			throw new IllegalArgumentException("unknow uri " + uri);
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (matcher.match(uri)) {
		case APP_INFO:
			return "vnd.android.cursor.item/com.txznet.txz.app_info";
		default:
			throw new IllegalArgumentException("unknow uri " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}
