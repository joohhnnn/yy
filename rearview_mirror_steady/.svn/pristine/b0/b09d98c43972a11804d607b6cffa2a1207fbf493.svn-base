package com.txznet.music.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.albumModule.bean.AlbumDao;
import com.txznet.music.albumModule.bean.AudioDao;
import com.txznet.music.albumModule.bean.BreakpointAudioDao;
import com.txznet.music.albumModule.bean.CategoryDao;
import com.txznet.music.albumModule.bean.DaoMaster;
import com.txznet.music.albumModule.bean.DaoMaster.OpenHelper;
import com.txznet.music.favor.bean.BeSendBeanDao;
import com.txznet.music.favor.bean.FavourBeanDao;
import com.txznet.music.favor.bean.SubscribeBeanDao;
import com.txznet.music.historyModule.bean.HistoryDataDao;
import com.txznet.music.localModule.bean.LocalAudioDao;
import com.txznet.music.message.MessageDao;
import com.txznet.music.playerModule.bean.PlayItemDao;
import com.txznet.music.playerModule.bean.QQTicketTableDao;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.internal.DaoConfig;

import java.util.List;

/**
 * Created by brainBear on 2017/9/8.
 */

public class DaoOpenHelper extends OpenHelper {
    private static final String TAG = "Music:DaoOpenHelper:";

    public DaoOpenHelper(Context context, String name) {
        super(context, name);
    }

    public DaoOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(Database db) {
        LogUtil.d(TAG, "create");
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade " + "upgrade old:" + oldVersion + " new:" + newVersion);
        LogUtil.d(TAG, "upgrade old:" + oldVersion + " new:" + newVersion);
        super.onUpgrade(db, oldVersion, newVersion);

        //跨版本数据迁移，如果从版本1迁移到版本3，会先执行case 1的逻辑，然后执行case 2的逻辑。
        for (int i = oldVersion; i < newVersion; i++) {

            switch (i) {
                case 1:
                    //增加showStyle属性
                    MigrationHelper.addColumn(db, CategoryDao.TABLENAME, CategoryDao.Properties.ShowStyle.columnName, "INTEGER", "0");
                    break;
                case 2://3版本加上表,QQTicketTable
//                    migrationHelper.migrate(db, QQTicketTableDao.class,null);
                    QQTicketTableDao.createTable(db, true);
                    break;
                case 3:
                    //LocalAudio表中audioDbId字段增加Unique注解
                    LocalAudioDao.dropTable(db, true);
                    LocalAudioDao.createTable(db, true);
                    break;
                case 4:
                    PlayItemDao.createTable(db, true);
                    break;
                case 5:
                    FavourBeanDao.createTable(db, true);
                    SubscribeBeanDao.createTable(db, true);
                    BeSendBeanDao.createTable(db, true);
                    break;
                case 6:
                    db.execSQL("alter table AUDIO ADD COLUMN IS_LOCAL INTEGER default 0");
                    break;
                case 7:
                    MessageDao.createTable(db, true);
                    break;
                case 8:
                    MigrationHelper.addColumn(db, BreakpointAudioDao.TABLENAME, BreakpointAudioDao.Properties.Duration.columnName, "INTEGER", "0");
                    MigrationHelper.addColumn(db, BreakpointAudioDao.TABLENAME, BreakpointAudioDao.Properties.PlayEndCount.columnName, "INTEGER", "0");
                    break;
                case 9:
                    HistoryDataDao.createTable(db, true);
                    break;
                case 10:
                    MigrationHelper.addColumn(db, AudioDao.TABLENAME, AudioDao.Properties.OperTime.columnName, "INTEGER", "0");
                    MigrationHelper.addColumn(db, AlbumDao.TABLENAME, AlbumDao.Properties.OperTime.columnName, "INTEGER", "0");
                    break;
                case 11:
                    LocalAudioDao.dropTable(db, true);
                    LocalAudioDao.createTable(db, true);
                    break;
                default:

                    break;
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.e(TAG, "downgrade old:" + oldVersion + " new:" + newVersion);
        Database database = wrap(db);
        DaoMaster.dropAllTables(database, true);
        DaoMaster.createAllTables(database, false);
    }


}
