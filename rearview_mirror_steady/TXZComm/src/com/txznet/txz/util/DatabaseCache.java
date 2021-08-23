package com.txznet.txz.util;

import java.io.IOException;
import java.util.List;

import com.txznet.comm.remote.util.LogUtil;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseCache<E> {

	private class SQLiteHelper extends SQLiteOpenHelper {

		public SQLiteHelper(final Context context) {
			super(context, sqLiteCallback.getName(), null, sqLiteCallback.getVersion());
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			sqLiteCallback.onCreate(db);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			sqLiteCallback.onUpgrade(db, oldVersion, newVersion);
		}
	}

	public static interface PersistableResource<E> {

		Cursor getCursor(SQLiteDatabase readableDatabase, String selection, String[] selectArgs);

		E loadFrom(Cursor cursor);

		void store(SQLiteDatabase writableDatabase, SQLiteDatabase readableDatabase, List<E> items);

		boolean delete(SQLiteDatabase writableDatabase, String where, String[] whereArgs);
	}

	public static interface SQLiteCallback {
		int getVersion();

		String getName();

		void onCreate(final SQLiteDatabase db);

		void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion);
	}

	private boolean isAlreadyInit;
	protected SQLiteHelper sqLiteHelper;
	protected SQLiteCallback sqLiteCallback;
	protected PersistableResource<E> mPersistableResource;

	public DatabaseCache(SQLiteCallback callback, PersistableResource<E> resource) {
		this.sqLiteCallback = callback;
		this.mPersistableResource = resource;
	}

	public void init(Context context) {
		if (isAlreadyInit) {
			LogUtil.logd("databaseCache has already init！");
			return;
		}

		isAlreadyInit = true;
		sqLiteHelper = new SQLiteHelper(context);
	}

	protected SQLiteDatabase getWritable() {
		try {
			return sqLiteHelper.getWritableDatabase();
		} catch (SQLiteException e1) {
			try {
				return sqLiteHelper.getWritableDatabase();
			} catch (SQLiteException e2) {
				return null;
			}
		}
	}

	protected SQLiteDatabase getReadable() {
		try {
			return sqLiteHelper.getReadableDatabase();
		} catch (SQLiteException e1) {
			try {
				return sqLiteHelper.getReadableDatabase();
			} catch (SQLiteException e2) {
				return null;
			}
		}
	}

	public boolean delete(String where, String[] whereArgs) {
		SQLiteOpenHelper helper = sqLiteHelper;
		final SQLiteDatabase writeDb = getWritable();
		if (writeDb == null) {
			return false;
		}
		writeDb.beginTransaction();
		try {
			boolean b = mPersistableResource.delete(writeDb, where, whereArgs);
			writeDb.setTransactionSuccessful();
			return b;
		} finally {
			writeDb.endTransaction();
			helper.close();
		}
	}
}
