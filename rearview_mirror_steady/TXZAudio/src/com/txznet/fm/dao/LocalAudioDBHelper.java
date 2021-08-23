package com.txznet.fm.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.dao.interfase.BaseDao;
import com.txznet.fm.dao.interfase.BaseDaoImpl;
import com.txznet.music.bean.LocalAudio;
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
public class LocalAudioDBHelper extends BaseDaoImpl implements BaseDao {

	private final String[] TABLE_COLNAME = new String[] { TABLE_ID, TABLE_NAME,
			TABLE_ARTISTS, TABLE_STRDOWNLOADURL, TABLE_DURATION, TABLE_SID,
			TABLE_PINYIN, TABLE_DESC, TABLE_BSHOWSOURCE, TABLE_SOURCE };

	// 单例

	private static LocalAudioDBHelper instance;

	private LocalAudioDBHelper() {
	}

	public static LocalAudioDBHelper getInstance() {
		if (null == instance) {
			synchronized (MusicFragment.class) {
				if (null == instance) {
					instance = new LocalAudioDBHelper();
				}
			}
		}
		return instance;
	}

	public boolean remove(String url) {
		LogUtil.logd("table [" + getTableName() + "] removed data's id : "
				+ url);
		return this.getDB().delete(getTableName(), TABLE_COLNAME[3] + " = ?",
				new String[] { url }) > 0;
	}

	@Override
	public String getTableName() {
		return LocalAudio.class.getSimpleName();
	}

	@Override
	public SQLiteDatabase getDB() {
		return DBUtils.getInstance().openGlobe();
	}

	@Override
	public String[] getPrimaryKey() {
		return new String[] {  TABLE_STRDOWNLOADURL };
	}

	@Override
	public <T> T fillObject(Cursor cursor, Class<T> clazz) {
		Audio audio = new Audio();
		audio.setId(cursor.getLong(cursor.getColumnIndex(TABLE_COLNAME[0])));
		audio.setName(cursor.getString(cursor.getColumnIndex(TABLE_COLNAME[1])));
		// audio.setSid(cursor.getInt(cursor.getColumnIndex(TABLE_COLNAME[5])));
		audio.setSid(0);
		List<String> arrtistsNames = new ArrayList<String>();
		arrtistsNames.add(cursor.getString(cursor
				.getColumnIndex(TABLE_COLNAME[2])));
		audio.setArrArtistName(arrtistsNames);
		audio.setStrDownloadUrl(cursor.getString(cursor
				.getColumnIndex(TABLE_COLNAME[3])));
		audio.setDuration(cursor.getLong(cursor
				.getColumnIndex(TABLE_COLNAME[4])));
		audio.setDownloadType("0");
		audio.setStrCategoryId("");
		audio.setBShowSource(1 == cursor.getInt(cursor
				.getColumnIndex(TABLE_BSHOWSOURCE)));
		audio.setSourceFrom("本地音乐");
		return (T) audio;
	}

	private static final int ONCE_SAVE_INT = 100; //一次性存储太多数据 sqlite会报错
	
	@Override
	public <T> void saveOrUpdate(List<T> objects) {
		if (CollectionUtils.isEmpty(objects)) {
			return;
		}
		for (int index = 0; index < objects.size(); index += ONCE_SAVE_INT) {
			List<T> subList = objects.subList(index, index + ONCE_SAVE_INT > objects.size() ? objects.size() : index + ONCE_SAVE_INT);
			StringBuffer buffer = new StringBuffer();
			try {
				subList.getClass().getTypeParameters();
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
				for (int i = 0; i < subList.size(); i++) {
					if (i != 0) {
						buffer.append(",");
					}
					T t = subList.get(i);
					buffer.append(" (");
					for (int j = 0; j < TABLE_COLNAME.length; j++) {
						if (j != 0) {
							buffer.append(",");
						}
						Field declaredField = t.getClass().getDeclaredField(TABLE_COLNAME[j]);
						declaredField.setAccessible(true);
						buffer.append("'");
						if (null != declaredField.get(t)) {
							if (List.class.isAssignableFrom(declaredField.getType())) {
								buffer.append(
										CollectionUtils.toString((List) declaredField.get(t)).replaceAll("'", "&#39;"));
							} else {
								buffer.append(declaredField.get(t).toString().replaceAll("'", "&#39;"));
							}
						}
						buffer.append("'");
					}
					buffer.append(" )");
				}
				this.execSql(buffer.toString());
			} catch (Exception e) {
				LogUtil.loge("  error ::" + e.getMessage() + "," + buffer.toString());
			}
		}

	}

	@Override
	public boolean remove(long id) {
		LogUtil.logd("table [" + getTableName() + "] removed data's id : " + id);
		return this.getDB().delete(getTableName(), TABLE_COLNAME[0] + " = ?",
				new String[] { String.valueOf(id) }) > 0;
	}

	@Override
	protected String getDefaultOrderIndex() {
		return TABLE_PINYIN;
	}

}
