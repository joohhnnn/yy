package com.txznet.fm.dao;

import java.lang.reflect.Field;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.reflect.TypeToken;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.dao.interfase.BaseDao;
import com.txznet.fm.dao.interfase.BaseDaoImpl;
import com.txznet.music.bean.response.Category;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.JsonHelper;

/**
 * 数据表：category
 *
 */
public class CategoryDBHelper extends BaseDaoImpl implements BaseDao {
    private static final  String  TAG="[music] [db] ";
    private static String  []TABLE_COLNAME =new String[]{
            TABLE_DESC,TABLE_CATEGORYID,TABLE_LOGO,TABLE_ARR_CHILD
    };


    // 单例

    private static CategoryDBHelper instance;

    private CategoryDBHelper() {
    }

    public static CategoryDBHelper getInstance() {
        if (null == instance) {
            synchronized (CategoryDBHelper.class) {
                if (null == instance) {
                    instance = new CategoryDBHelper();
                }
            }
        }
        return instance;
    }



    @Override
    public String getTableName() {
        return Category.class.getSimpleName();
    }

    @Override
    public SQLiteDatabase getDB() {
        return DBUtils.getInstance().openGlobe();
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
                            //转换成为JSON存储
                            String json=JsonHelper.toJson(declaredField.get(t));
                            buffer.append(json);
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

    @Override
    public String[] getPrimaryKey() {
        return new String[]{TABLE_COLNAME[1]};
    }

    @Override
    public <T> T fillObject(Cursor cursor, Class<T> clazz) {
        Category category=new Category();
        category.setArrChild((List<Category>) JsonHelper.toObject(cursor.getString(cursor.getColumnIndex(TABLE_ARR_CHILD)),new TypeToken<List<Category>>(){
        }.getType()));
        category.setCategoryId(cursor.getInt(cursor.getColumnIndex(TABLE_CATEGORYID)));
        category.setDesc(cursor.getString(cursor.getColumnIndex(TABLE_DESC)));
        category.setLogo(cursor.getString(cursor.getColumnIndex(TABLE_LOGO)));
        return (T) category;
    }
}
