/**
 * 
 */
package com.txznet.fm.dao.interfase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.utils.ArrayUtils;
import com.txznet.music.utils.BeanUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.StringUtils;

/**
 * 
 * @author telenewbie
 * @version 2016年3月25日
 */
public abstract class BaseDaoImpl implements BaseDao {
	protected static final String TAG = "[music][BaseDao] ";

	public static final String SET = "set";

	public static final String GET = "get";

	public static final String AND = " and ";

	public static final String OR = " or ";

	public static final String LIMIT = " 0,10 ";

	public static final String LIMIT_DEFAULT = LIMIT;

	public static final String LIMIT_ALL = " 0,100000 ";

	public static final String DESC = " desc ";

	public static final String ASC = " asc ";

	public static final String SPACE = " ";

	public static final int BASE_ID_TABLE_ID_INDEX = 0;

	public static final int BASE_NAME_TABLE_ID_INDEX = 1;

	public static final int BASE_TYPE_TABLE_ID_INDEX = 2;

	public static final int BASE_COMPANYID_TABLE_ID_INDEX = 3;

	public static final String TABLE_SID = "sid";
	public static final String TABLE_ID = "id";
	public static final String TABLE_ALBUMID = "albumId";
	public static final String TABLE_NAME = "name";
	public static final String TABLE_ARTISTS = "arrArtistName";
	public static final String TABLE_LASTPLAYTIME = "lastPlayTime";
	public static final String TABLE_STRCATEGORYID = "strCategoryId";
	public static final String TABLE_CURRENTPLAYTIME = "currentPlayTime";
	public static final String TABLE_BNOCACHE = "bNoCache";
	public static final String TABLE_DOWNLOADTYPE = "downloadType";
	public static final String TABLE_STRDOWNLOADURL = "strDownloadUrl";
	public static final String TABLE_STRPROCESSINGURL = "strProcessingUrl";
	public static final String TABLE_BSHOWSOURCE = "bShowSource";
	public static final String TABLE_DURATION = "duration";
	public static final String TABLE_ALBUMNAME = "albumName";
	public static final String TABLE_INDEXID = "indexId";
	public static final String TABLE_PINYIN = "pinyin";
	public static final String TABLE_LOGO = "logo";
	public static final String TABLE_DESC = "desc";
	public static final String TABLE_NUM_LIKED = "likedNum";
	public static final String TABLE_NUM_LISTENED = "listenNum";
	public static final String TABLE_CATEGORYID = "categoryId";
	public static final String TABLE_ARR_CHILD = "arrChild";
	public static final String TABLE_CATEGORY_DRAWABLEID = "drawableId";
	public static final String TABLE_SOURCE = "sourceFrom";
	public static final String TABLE_URLTYPE = "urlType";

	protected String orderIndex = null;

	protected String asc(String... columns) {
		if (ArrayUtils.isEmpty(columns)) {
			return StringUtils.EMPTY;
		}
		StringBuffer sb = new StringBuffer();
		for (String col : columns) {
			sb.append(col).append(SPACE).append(ASC).append(",");
		}
		return sb.substring(0, sb.length() - 1);
	}

	protected String desc(String... columns) {
		if (ArrayUtils.isEmpty(columns)) {
			return StringUtils.EMPTY;
		}
		StringBuffer sb = new StringBuffer();
		for (String col : columns) {
			sb.append(col).append(SPACE).append(DESC).append(",");
		}
		return sb.substring(0, sb.length() - 1);
	}

	protected String[] columns = new String[0];

	/**
	 * @desc <pre>
	 * 获取表名
	 * </pre>
	 * @author Erich Lee
	 * @date Mar 21, 2013
	 * @return
	 */
	@Override
	public abstract String getTableName();

	/**
	 * @desc <pre>
	 * 获取主键字段名
	 * </pre>
	 * @author Erich Lee
	 * @date Mar 21, 2013
	 * @return
	 */
	@Override
	public abstract String[] getPrimaryKey();

	/**
	 * @desc <pre>
	 * 加载关联属性
	 * </pre>
	 * @author Erich Lee
	 * @date Mar 23, 2013
	 */
	public void loadRelativeProp(Object obj) {
	};

	public String[] getColumns() {
		if (columns.length > 0) {
			return columns;
		}
		Cursor cursor = null;
		try {
			cursor = this.getDB().query(getTableName(), null, null, null, null, null, null);
			String[] columnNames = cursor.getColumnNames();
			int len = columnNames.length;
			columns = new String[len];
			System.arraycopy(columnNames, 0, columns, 0, len);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			closeCursor(cursor);
		}
		return columns;
	}

	@Override
	public <T> void save(T o) {
		if (o != null) {
			ContentValues cv = createContentValues(o);
			this.getDB().insert(getTableName(), null, cv);
			LogUtil.logd(TAG+"插入到table [" + getTableName() + "] 插入数据为 : " + cv.toString());
		}
	}

	protected String getPrimaryKeySelection() {
		String[] pkNames = getPrimaryKey();
		StringBuffer sb = new StringBuffer();
		for (String pkName : pkNames) {
			sb.append(pkName).append(" = ? ").append(AND);
		}
		return sb.substring(0, sb.length() - AND.length());
	}

	protected String getPrimaryKeySelection(String... cs) {
		if (null == cs) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (String pkName : cs) {
			sb.append(pkName).append(",");
		}
		return sb.substring(0, sb.length() - ",".length());
	}

	@Override
	public <T> void saveOrUpdate(T o) {
		if (o != null) {
			ContentValues cv = createContentValues(o);
			int len = getPrimaryKey().length;
			String[] pkvs = new String[len];
			for (int i = 0; i < len; i++) {
				pkvs[i] = cv.getAsString(getPrimaryKey()[i]);
			}
			if (pkvs!=null&&pkvs[0]!=null&&exists(getPrimaryKeySelection(), pkvs)) {
				this.getDB().update(getTableName(), cv, getPrimaryKeySelection(), pkvs);
			} else {
				save(o);
			}
		}
	}

	@Override
	public <T> void saveOrUpdate(T o, ContentValues c) {
		if (o != null) {
			int len = getPrimaryKey().length;
			String[] pkvs = new String[len];
			for (int i = 0; i < len; i++) {
				pkvs[i] = c.getAsString(getPrimaryKey()[i]);
			}
			if (exists(getPrimaryKeySelection(), pkvs)) {
				this.getDB().update(getTableName(), c, getPrimaryKeySelection(), pkvs);
			} else {
				save(o);
			}
		}

	}

	@Override
	public boolean remove(long id) {
		LogUtil.logd(TAG+"table [" + getTableName() + "] removed data's id : " + id);
		return this.getDB().delete(getTableName(), getPrimaryKey()[0] + " = ?", new String[] { String.valueOf(id) }) > 0;
	}

	@Override
	public boolean remove(String selection, String[] selectionArgs) {
		LogUtil.logd(TAG+"table [" + getTableName() + "] removed");
		return this.getDB().delete(getTableName(), selection, selectionArgs) > 0;
	}

	@Override
	public void removeAll() {
		try {
			this.getDB().delete(getTableName(), null, null);
			LogUtil.logd(TAG+"table [" + getTableName() + "] deleted all.");
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	@Override
	public void execSql(String sql) {
		this.getDB().execSQL(sql);
	}

	@Override
	public <T> List<T> execSql(String sql, String[] args, Class<T> clazz) {
		Cursor cursor = null;
		try {
			cursor = this.getDB().rawQuery(sql, args);
			return fillList(cursor, clazz);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			closeCursor(cursor);
		}
		return new ArrayList<T>();
	}

	@Override
	public <T> T load(long id, Class<T> clazz) {
		Cursor cursor = null;
		T obj = null;
		try {
			cursor = this.getDB().query(getTableName(), null, getPrimaryKey()[0] + " = ? ", new String[] { String.valueOf(id) }, null, null, null);

			if (cursor.moveToNext()) {
				obj = fillObject(cursor, clazz);
				loadRelativeProp(obj);
			}

		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			closeCursor(cursor);
		}
		return obj;
	}

	/**
	 * @desc <pre>
	 * 根据帖子ID查询关联的数据
	 * </pre>
	 * @author Erich Lee
	 * @date Mar 23, 2013
	 * @param clazz
	 * @param postId
	 * @return
	 */
	@Override
	public <T> T findByPostId(Class<T> clazz, long postId) {
		List<T> list = find(clazz, "postId = ?", new String[] { String.valueOf(postId) });
		T obj = null;
		if (!CollectionUtils.isEmpty(list)) {
			obj = list.get(0);
		}
		return obj;
	}

	@Override
	public <T> T findOne(Class<T> clazz, String selection, String[] selectionArgs) {
		List<T> list = find(clazz, selection, selectionArgs);
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public <T> List<T> find(Class<T> clazz, String selection, String[] selectionArgs) {
		return find(clazz, selection, selectionArgs, null);
	}

	@Override
	public <T> List<T> find(Class<T> clazz, String selection, String[] selectionArgs, String orderBy) {
		return find(clazz, selection, selectionArgs, orderBy, LIMIT);
	}

	@Override
	public <T> List<T> find(Class<T> clazz, String selection, String[] selectionArgs, String orderBy, String limit) {
		String select = null;
		String[] selectArgs = null;
		String order = null;
		if (!StringUtils.isEmpty(selection)) {
			select = selection;
		}
		if (!ArrayUtils.isEmpty(selectionArgs)) {
			selectArgs = selectionArgs;
		}
		order = getDefaultOrderIndex();// 默认
		if (!StringUtils.isEmpty(orderBy)) {
			order = orderBy;
		}
		Cursor cursor = null;
		try {
			cursor = this.getDB().query(getTableName(), null, select, selectArgs, null, null, order, limit);
			return fillList(cursor, clazz);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			closeCursor(cursor);
		}
		return new ArrayList<T>();
	}
	
	protected  String  getDefaultOrderIndex(){
		return orderIndex;
	}

	@Override
	public <T> List<T> findAll(Class<T> clazz) {
		return findAll(clazz, null);
	}

	@Override
	public <T> List<T> findAll(Class<T> clazz, String orderBy) {
		return findAll(clazz, null, null, orderBy);
	}

	@Override
	public <T> List<T> findAll(Class<T> clazz, String selection, String[] selectionArgs, String orderBy) {
		return find(clazz, selection, selectionArgs, orderBy, null);
	}

	@Override
	public boolean exists(long id) {
		return exists(getPrimaryKey()[0] + " = ? ", new String[] { String.valueOf(id) });
	}

	@Override
	public boolean exists(String selection, String[] selectionArgs) {
		Cursor cursor = null;
		try {
			cursor = this.getDB().query(getTableName(), null, selection, selectionArgs, null, null, null);
			if (cursor.getCount() > 0) {
				return true;
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			closeCursor(cursor);
		}
		return false;
	}

	protected void closeCursor(Cursor cursor) {
		if (cursor != null) {
			cursor.close();
		}
	}

	protected ContentValues createContentValues(Object o) {
		if (o == null) {
			return null;
		}
		ContentValues cv = new ContentValues();
		Class clazz = o.getClass();
		String[] columnNames = getColumns();
//		Method method = null;
		String fieldTypeName;
		try {
			for (String columnName : columnNames) {
				Field declaredField = null;
				try {
					declaredField = clazz.getDeclaredField(columnName);
				} catch (Exception e) {
				}
//				cv.put(columnName, declaredField.getType());
//				method = BeanUtils.getMethod(clazz, columnName, GET);
				if (declaredField == null) {
					continue;
				}
				declaredField.setAccessible(true);
				fieldTypeName = declaredField.getType().getSimpleName();
				if (fieldTypeName.equals("String")) {
					cv.put(columnName, (String) declaredField.get(o));
				} else if (fieldTypeName.equals("int")) {
					cv.put(columnName, (Integer)  declaredField.get(o));
				} else if (fieldTypeName.equals("boolean")) {
					cv.put(columnName, (Boolean)  declaredField.get(o) ? 1 : 0);
				} else if (fieldTypeName.equals("long")) {
					cv.put(columnName, (Long)  declaredField.get(o));
				} else if (fieldTypeName.equals("double")) {
					cv.put(columnName, (Double)  declaredField.get(o));
				} else if (fieldTypeName.equals("float")) {
					cv.put(columnName, (Float)  declaredField.get(o));
				} else if (fieldTypeName.equals("short")) {
					cv.put(columnName, (Short)  declaredField.get(o));
				} else if (fieldTypeName.equals("byte[]")) {
					cv.put(columnName, (byte[])  declaredField.get(o));
				} else if (fieldTypeName.equals("List")) {
					cv.put(columnName, CollectionUtils.toString((List)  declaredField.get(o)));
				} else {
					Log.i(TAG, "The field type [" + fieldTypeName + "] not supported!");
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return cv;
	}

	/**
	 * @desc <pre>
	 * 将数据库数据转换为java对象
	 * </pre>
	 * @author Erich Lee
	 * @date Mar 21, 2013
	 * @param cursor
	 * @param clazz
	 * @return
	 */
	@Override
	public abstract <T> T fillObject(Cursor cursor, Class<T> clazz);

	/**
	 * @desc <pre>
	 * 将数据库数据转换为集合
	 * </pre>
	 * @author Erich Lee
	 * @date Mar 21, 2013
	 * @param cursor
	 * @param clazz
	 * @return
	 */
	protected <T> List<T> fillList(Cursor cursor, Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		T o = null;
		while (cursor.moveToNext()) {

			o = fillObject(cursor, clazz);

			if (o != null) {
				loadRelativeProp(o);
				list.add(o);
			}

		}
		return list;
	}

	protected String makeArgs(String[] ids) {
		StringBuffer sb = new StringBuffer();
		sb.append("?");
		for (int i = 1; i < ids.length; i++) {
			sb.append(",?");
		}
		return sb.toString();
	}

}
