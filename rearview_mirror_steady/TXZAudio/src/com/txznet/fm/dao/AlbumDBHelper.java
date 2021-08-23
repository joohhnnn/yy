package com.txznet.fm.dao;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.txznet.fm.dao.interfase.BaseDao;
import com.txznet.fm.dao.interfase.BaseDaoImpl;
import com.txznet.music.bean.response.Album;

/**
 * @author telenewbie
 * @version 创建时间：2016年5月5日 下午7:35:28
 * 
 */
public class AlbumDBHelper extends BaseDaoImpl implements BaseDao {
	private final String[] TABLE_COLNAME = new String[] { TABLE_ID, TABLE_NAME,
			TABLE_ARTISTS, TABLE_LOGO, TABLE_DESC, TABLE_NUM_LIKED,
			TABLE_NUM_LISTENED, TABLE_SID, TABLE_CATEGORYID };
	private static AlbumDBHelper instance = null;

	public static AlbumDBHelper getInstance() {
		if (instance == null) {
			synchronized (AlbumDBHelper.class) {
				if (instance == null) {
					instance = new AlbumDBHelper();
				}
			}
		}
		return instance;
	}

	@Override
	public <T> T fillObject(Cursor cursor, Class<T> clazz) {
		Album album = new Album();
		album.setId(cursor.getLong(cursor.getColumnIndex(TABLE_COLNAME[0])));
		album.setName(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[1])));
		List<String> arrtistsNames = new ArrayList<String>();
		arrtistsNames.add(cursor.getString(cursor
				.getColumnIndex(TABLE_COLNAME[2])));
		album.setArrArtistName(arrtistsNames);
		album.setLogo(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[3])));
		album.setDesc(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[4])));
		album.setLikedNum(cursor.getInt(cursor.getColumnIndex(TABLE_COLNAME[5])));
		album.setListenNum(cursor.getInt(cursor
				.getColumnIndex(TABLE_COLNAME[6])));
		album.setSid(cursor.getInt(cursor.getColumnIndex(TABLE_COLNAME[7])));
		album.setCategoryID(cursor.getInt(cursor
				.getColumnIndex(TABLE_COLNAME[8])));
		return (T) album;
	}

	@Override
	public SQLiteDatabase getDB() {
		return DBUtils.getInstance().openGlobe();
	}

	@Override
	public <T> void saveOrUpdate(List<T> t) {
		SQLiteDatabase db = getDB();
		synchronized (db) {
			try {
				db.beginTransaction();
				for (T o : t) {
					saveOrUpdate(o);
				}
				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
			}
		}
	}

	@Override
	public String getTableName() {
		return Album.class.getSimpleName();
	}

	@Override
	public String[] getPrimaryKey() {
		// 设置唯一键：sid,id,categoryID,这三个值
		return new String[] { TABLE_COLNAME[0], TABLE_COLNAME[7],
				TABLE_COLNAME[8] };
		// return new String[] { "_index" };
	}

}
