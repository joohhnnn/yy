/**
 * 
 */
package com.txznet.fm.dao.interfase;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * @author telenewbie
 * @version 2016年3月25日
 */
public interface BaseDao {
	public abstract String[] getPrimaryKey();

	public abstract String getTableName();

	public abstract SQLiteDatabase getDB();

	public abstract void loadRelativeProp(Object obj);

	public abstract void removeAll();

	public abstract boolean remove(long id);

	public abstract void execSql(String sql);

	public abstract <T> T load(long id, Class<T> clazz);

	public abstract boolean exists(long id);

	public abstract <T> void saveOrUpdate(T o);

	public abstract <T> void saveOrUpdate(T o, ContentValues c);

	/**
	 * 批量插入
	 * 
	 * @param t
	 */
	public abstract <T> void saveOrUpdate(List<T> t);

	public abstract <T> void save(T o);

	public abstract <T> List<T> findAll(Class<T> clazz, String selection, String[] selectionArgs, String orderBy);

	public abstract <T> List<T> findAll(Class<T> clazz, String orderBy);

	public abstract <T> List<T> findAll(Class<T> clazz);

	public abstract <T> List<T> find(Class<T> clazz, String selection, String[] selectionArgs, String orderBy, String limit);

	public abstract <T> List<T> find(Class<T> clazz, String selection, String[] selectionArgs, String orderBy);

	public abstract <T> List<T> find(Class<T> clazz, String selection, String[] selectionArgs);

	public abstract <T> T findByPostId(Class<T> clazz, long postId);

	public abstract <T> T findOne(Class<T> clazz, String selection, String[] selectionArgs);

	public abstract boolean exists(String selection, String[] selectionArgs);

	public abstract boolean remove(String selection, String[] selectionArgs);

	public abstract <T> List<T> execSql(String sql, String[] args, Class<T> clazz);

	public abstract <T> T fillObject(Cursor cursor, Class<T> clazz);
}
