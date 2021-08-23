package com.txznet.fm.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.dao.interfase.BaseDao;
import com.txznet.fm.dao.interfase.BaseDaoImpl;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.fragment.MusicFragment;
import com.txznet.music.utils.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS User on 2016/9/12.
 */

public class AudioDBHelper extends BaseDaoImpl implements BaseDao {

	private final String[] TABLE_COLNAME = new String[] { TABLE_ID, TABLE_NAME,
			TABLE_ARTISTS, TABLE_STRDOWNLOADURL, TABLE_DURATION,
			TABLE_ALBUMNAME, TABLE_SID, TABLE_STRCATEGORYID,
			TABLE_STRPROCESSINGURL, TABLE_ALBUMID, TABLE_LASTPLAYTIME,TABLE_DOWNLOADTYPE,TABLE_BSHOWSOURCE, TABLE_SOURCE ,TABLE_URLTYPE};

	// 单例

	private static AudioDBHelper instance;

	private AudioDBHelper() {
	}

	public static AudioDBHelper getInstance() {
		if (null == instance) {
			synchronized (AudioDBHelper.class) {
				if (null == instance) {
					instance = new AudioDBHelper();
				}
			}
		}
		return instance;
	}

	@Override
	public String getTableName() {
		return Audio.class.getSimpleName();
	}

	@Override
	public String[] getPrimaryKey() {
		return new String[] { TABLE_ID, TABLE_SID };
	}

	@Override
	public SQLiteDatabase getDB() {
		return DBUtils.getInstance().openGlobe();
	}

	@Override
	public <T> T fillObject(Cursor cursor, Class<T> clazz) {
		Audio audio = new Audio();
		audio.setId(cursor.getLong(cursor.getColumnIndex(TABLE_COLNAME[0])));
		audio.setName(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[1])));
		List<String> arrtistsNames = new ArrayList<String>();
		arrtistsNames.add(cursor.getString(cursor
				.getColumnIndex(TABLE_COLNAME[2])));
		audio.setArrArtistName(arrtistsNames);
		audio.setStrDownloadUrl(cursor.getString(cursor
				.getColumnIndex(TABLE_COLNAME[3])));
		audio.setDuration(cursor.getLong(cursor
				.getColumnIndex(TABLE_COLNAME[4])));
		audio.setAlbumName(cursor.getString(cursor
				.getColumnIndex(TABLE_COLNAME[5])));
		audio.setSid(cursor.getInt(cursor.getColumnIndex(TABLE_COLNAME[6])));
		audio.setStrCategoryId(cursor.getString(cursor
				.getColumnIndex(TABLE_COLNAME[7])));
		audio.setStrProcessingUrl(cursor.getString(cursor
				.getColumnIndex(TABLE_COLNAME[8])));
		audio.setAlbumId(cursor.getString(cursor
				.getColumnIndex(TABLE_COLNAME[9])));
		audio.setLastPlayTime(cursor.getString(cursor
				.getColumnIndex(TABLE_COLNAME[10])));
		audio.setDownloadType(cursor.getString(cursor
				.getColumnIndex(TABLE_DOWNLOADTYPE)));
		audio.setSourceFrom(cursor.getString(cursor
				.getColumnIndex(TABLE_SOURCE)));
		audio.setBShowSource(1 == cursor.getInt(cursor
				.getColumnIndex(TABLE_BSHOWSOURCE)));
		audio.setUrlType(cursor.getInt(cursor.getColumnIndex(TABLE_URLTYPE)));
		return (T) audio;
	}

	@Override
	public <T> void saveOrUpdate(List<T> objects) {
		if (CollectionUtils.isEmpty(objects)) {
			return;
		}
		StringBuffer buffer = new StringBuffer();
		try {
			objects.getClass().getTypeParameters();
			buffer.append("INSERT OR REPLACE INTO " + getTableName() + "(");

			for (int i = 0; i < TABLE_COLNAME.length; i++) {
				buffer.append(TABLE_COLNAME[i]);
				if (i != TABLE_COLNAME.length - 1) {
					buffer.append(",");
				} else {
					buffer.append(")");
				}
			}
			buffer.append(" values ");
			for (int i = 0; i < objects.size(); i++) {
				if (i != 0) {
					buffer.append(",");
				}
				T t = objects.get(i);
				buffer.append(" (");
				for (int j = 0; j < TABLE_COLNAME.length; j++) {
					if (j != 0) {
						buffer.append(",");
					}
					Field declaredField = t.getClass().getDeclaredField(
							TABLE_COLNAME[j]);
					declaredField.setAccessible(true);
					buffer.append("'");
					if (null != declaredField.get(t)) {
						if (List.class
								.isAssignableFrom(declaredField.getType())) {
							buffer.append(CollectionUtils.toString(
									(List) declaredField.get(t)).replaceAll(
									"'", "&#39;"));
						} else {
							buffer.append(declaredField.get(t).toString()
									.replaceAll("'", "&#39;"));
						}
					}
					buffer.append("'");
				}
				buffer.append(" )");
			}
			this.execSql(buffer.toString());
		} catch (Exception e) {
			LogUtil.loge("  error ::" + e.getMessage() + ","
					+ buffer.toString());
		}
	}

	public void updateAudioLastPlayTime(Audio audio) {
		if (audio != null) {
			StringBuffer sb = new StringBuffer();
			sb.append("update Audio set lastPlayTime='"
					+ audio.getLastPlayTime() + "' where id ='" + audio.getId()
					+ "'and sid='" + audio.getSid() + "'");
			this.execSql(sb.toString());
		}
	}
}
