package com.txznet.fm.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.dao.interfase.BaseDao;
import com.txznet.fm.dao.interfase.BaseDaoImpl;
import com.txznet.music.bean.HistoryAudio;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.fragment.MusicFragment;
import com.txznet.music.utils.CollectionUtils;

/**
 * operator LocalAudio table
 * 
 * @author telenewbie
 * @version 创建时间：2016年3月25日 下午6:22:26
 * 
 */
public class HistoryAudioDBHelper extends BaseDaoImpl implements BaseDao {


	private final String[] TABLE_COLNAME = new String[] { TABLE_ID, TABLE_NAME, TABLE_ARTISTS, TABLE_STRDOWNLOADURL, TABLE_DURATION, TABLE_ALBUMNAME, TABLE_SID, TABLE_STRCATEGORYID,
			TABLE_STRPROCESSINGURL, TABLE_ALBUMID,TABLE_LASTPLAYTIME,TABLE_DOWNLOADTYPE,TABLE_URLTYPE };
	private static HistoryAudioDBHelper instance;

	private HistoryAudioDBHelper() {
		orderIndex = TABLE_INDEXID + DESC;
	}

	public static HistoryAudioDBHelper getInstance() {
		if (null == instance) {
			synchronized (MusicFragment.class) {
				if (null == instance) {
					instance = new HistoryAudioDBHelper();
				}
			}
		}
		return instance;
	}

	@Override
	public String getTableName() {
		return "HistoryAudio";
	}

	@Override
	public SQLiteDatabase getDB() {
		return DBUtils.getInstance().openGlobe();
	}

//	public boolean remove(long id, int sid) {
//		LogUtil.logd(TAG+"table [" + getTableName() + "] removed data's id : " + id + AND + "sid :" + sid);
//		return this.getDB().delete(getTableName(), getPrimaryKey()[0] + " = ? " + AND + getPrimaryKey()[1] + " = ? ", new String[] { String.valueOf(id), String.valueOf(sid) }) > 0;
//	};

	
	public boolean remove(long id, String name){
		LogUtil.logd(TAG+"table [" + getTableName() + "] removed data's id : " + id + AND + "name :" + name);
		return this.getDB().delete(getTableName(), TABLE_ID + " = ? " + AND + TABLE_NAME + " = ? ", new String[]{String.valueOf(id), name}) > 0;
	}
	
	@Override
	public String[] getPrimaryKey() {
		return new String[] { TABLE_ID, TABLE_SID };
	}

	@Override
	public <T> T fillObject(Cursor cursor, Class<T> clazz) {
		Audio audio = new Audio();
		audio.setId(cursor.getLong(cursor.getColumnIndex(TABLE_COLNAME[0])));
		audio.setName(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[1])));
		List<String> arrtistsNames = new ArrayList<String>();
		arrtistsNames.add(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[2])));
		audio.setArrArtistName(arrtistsNames);
		audio.setStrDownloadUrl(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[3])));
		audio.setDuration(cursor.getLong(cursor.getColumnIndex(TABLE_COLNAME[4])));
		audio.setAlbumName(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[5])));
		audio.setSid(cursor.getInt(cursor.getColumnIndex(TABLE_COLNAME[6])));
		audio.setStrCategoryId(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[7])));
		audio.setStrProcessingUrl(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[8])));
		audio.setAlbumId(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[9])));
		audio.setLastPlayTime(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[10])));
		audio.setDownloadType(cursor.getString(cursor.getColumnIndex(TABLE_DOWNLOADTYPE)));
		audio.setBShowSource(1 == cursor.getInt(cursor
				.getColumnIndex(TABLE_BSHOWSOURCE)));
		audio.setSourceFrom(cursor.getString(cursor.getColumnIndex(TABLE_SOURCE)));
		audio.setUrlType(cursor.getInt(cursor.getColumnIndex(TABLE_URLTYPE)));
		return (T) audio;
	}

	@Override
	public <T> void saveOrUpdate(List<T> objects) {
		if (CollectionUtils.isEmpty(objects)) {
			return;
		}
		SQLiteDatabase db = getDB();
		synchronized (db) {
			try {
				db.beginTransaction();
				for (T o : objects) {
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

}
