package com.txznet.music.data.db.compat;

import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库信息
 *
 * @author zackzhou
 * @date 2019/5/9,14:42
 */

public class DbCompatData {
    public int appVersion; // 版本
    public SQLiteDatabase sqLiteDatabase; // 数据库对象
}
