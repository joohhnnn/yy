package com.txznet.music.dao;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.internal.DaoConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by brainBear on 2017/9/26.
 */

public class MigrationHelper {
    private static final String CONVERSION_CLASS_NOT_FOUND_EXCEPTION = "MIGRATION HELPER - CLASS DOESN'T MATCH WITH THE CURRENT PARAMETERS";

    public static void addColumn(Database db, String tableName, String columnName, String columnType, String defaultValue) {
        db.execSQL(String.format(Locale.getDefault(), "ALTER table %s ADD COLUMN %s %s default %s", tableName, columnName, columnType, defaultValue));
    }

    public void migrate(Database db, Class<? extends AbstractDao<?, ?>> daoClasses, OnDataRestoreListener listener) {

        generateTempTables(db, daoClasses);
        dropTable(db, daoClasses, true);
        createTable(db, daoClasses, false);
        restoreData(db, daoClasses, listener);

//        try{
//            generateTempTables(db, daoClasses);
//            dropTable(db, daoClasses, true);
//            createTable(db, daoClasses, false);
//            restoreData(db, daoClasses, listener);
//        }catch (Exception e){
//            Log.e("!@#", "migrate: " + e.toString());
//        }

    }

    /**
     * 生成临时列表
     *
     * @param db
     * @param daoClasses
     */
    private void generateTempTables(Database db, Class<? extends AbstractDao<?, ?>> daoClasses) {
        DaoConfig daoConfig = new DaoConfig(db, daoClasses);

        String divider = "";
        String tableName = daoConfig.tablename;
        String tempTableName = daoConfig.tablename.concat("_TEMP");
        ArrayList<String> properties = new ArrayList<>();

        StringBuilder createTableStringBuilder = new StringBuilder();

        createTableStringBuilder.append("CREATE TABLE ").append(tempTableName).append(" (");

        for (int j = 0; j < daoConfig.properties.length; j++) {
            String columnName = daoConfig.properties[j].columnName;

            if (getColumns(db, tableName).contains(columnName)) {
                properties.add(columnName);

                String type = null;

                try {
                    type = getTypeByClass(daoConfig.properties[j].type);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                createTableStringBuilder.append(divider).append(columnName).append(" ").append(type);

                if (daoConfig.properties[j].primaryKey) {
                    createTableStringBuilder.append(" PRIMARY KEY");
                }

                divider = ",";
            }
        }
        createTableStringBuilder.append(");");

        db.execSQL(createTableStringBuilder.toString());

        StringBuilder insertTableStringBuilder = new StringBuilder();

        insertTableStringBuilder.append("INSERT INTO ").append(tempTableName).append(" (");
        insertTableStringBuilder.append(TextUtils.join(",", properties));
        insertTableStringBuilder.append(") SELECT ");
        insertTableStringBuilder.append(TextUtils.join(",", properties));
        insertTableStringBuilder.append(" FROM ").append(tableName).append(";");

        db.execSQL(insertTableStringBuilder.toString());

    }

    /**
     * 存储新的数据库表 以及数据
     *
     * @param db
     * @param daoClasses
     */
    private void restoreData(Database db, Class<? extends AbstractDao<?, ?>> daoClasses, OnDataRestoreListener listener) {

        DaoConfig daoConfig = new DaoConfig(db, daoClasses);
        String tableName = daoConfig.tablename;
        String tempTableName = daoConfig.tablename.concat("_TEMP");

        List<String> columns = getColumns(db, tempTableName);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tempTableName, null);
            while (cursor.moveToNext()) {
                List<DataEntry> dataEntries = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    DataEntry dataEntry = new DataEntry(columns.get(i), cursor.getString(i), cursor.getType(i));
                    dataEntries.add(dataEntry);
                }
                if (null != listener) {
                    List<DataEntry> newDataEntries = listener.onDataRestore(daoConfig, dataEntries);
                    String sqlSt = createDataRestoreSqlStr(tableName, newDataEntries);
                    Log.e("!@#", "restoreData: " + sqlSt);
                    db.execSQL(sqlSt);
                }
                dataEntries.clear();
            }

        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }

        StringBuilder dropTableStringBuilder = new StringBuilder();
        dropTableStringBuilder.append("DROP TABLE ").append(tempTableName);
        db.execSQL(dropTableStringBuilder.toString());

    }

    private String getTypeByClass(Class<?> type) throws Exception {
        if (type.equals(String.class)) {
            return "TEXT";
        }
        if (type.equals(Long.class) || type.equals(Integer.class) || type.equals(long.class)) {
            return "INTEGER";
        }
        if (type.equals(Boolean.class)) {
            return "BOOLEAN";
        }

        Exception exception = new Exception(CONVERSION_CLASS_NOT_FOUND_EXCEPTION.concat(" - Class: ").concat(type.toString()));
        exception.printStackTrace();
        throw exception;
    }

    private List<String> getColumns(Database db, String tableName) {
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " limit 1", null);
            if (cursor != null) {
                columns = new ArrayList<>(Arrays.asList(cursor.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return columns;
    }

    private void dropTable(Database db, Class<? extends AbstractDao<?, ?>> daoClasses, boolean ifExists) {
        try {
            Method dropTableMethod = daoClasses.getMethod("dropTable", Database.class, boolean.class);
            dropTableMethod.invoke(null, db, ifExists);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void createTable(Database db, Class<? extends AbstractDao<?, ?>> daoClasses, boolean ifExists) {
        try {
            Method createTableMethod = daoClasses.getMethod("createTable", Database.class, boolean.class);
            createTableMethod.invoke(null, db, ifExists);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private String createDataRestoreSqlStr(String tableName, List<DataEntry> dataEntries) {
        ArrayList<String> columns = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        for (DataEntry entry : dataEntries) {
            columns.add(entry.getKey());
            if (entry.getType() != Cursor.FIELD_TYPE_STRING) {
                values.add(entry.getValue());
            } else {
                values.add("'" + entry.getValue() + "'");
            }

        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append(" (");
        sb.append(TextUtils.join(",", columns));
        sb.append(") VALUES (");
        sb.append(TextUtils.join(",", values));
        sb.append(")");

        return sb.toString();
    }


    public interface OnDataRestoreListener {

        List<DataEntry> onDataRestore(DaoConfig conf, List<DataEntry> dataEntries);

    }

}
