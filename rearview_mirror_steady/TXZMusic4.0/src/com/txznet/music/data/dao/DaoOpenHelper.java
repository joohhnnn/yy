package com.txznet.music.data.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.albumModule.bean.AlbumDao;
import com.txznet.music.albumModule.bean.AudioDao;
import com.txznet.music.albumModule.bean.BreakpointAudioDao;
import com.txznet.music.albumModule.bean.DaoMaster;
import com.txznet.music.albumModule.bean.DaoMaster.OpenHelper;
import com.txznet.music.baseModule.bean.PlayListDataDao;
import com.txznet.music.dao.MigrationHelper;
import com.txznet.music.data.entity.CategoryDao;
import com.txznet.music.favor.bean.BeSendBeanDao;
import com.txznet.music.favor.bean.FavourBeanDao;
import com.txznet.music.favor.bean.SubscribeBeanDao;
import com.txznet.music.historyModule.bean.HistoryDataDao;
import com.txznet.music.localModule.bean.LocalAudioDao;
import com.txznet.music.message.MessageDao;
import com.txznet.music.playerModule.bean.PlayItemDao;
import com.txznet.music.playerModule.bean.QQTicketTableDao;

import org.greenrobot.greendao.database.Database;

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
                    db.execSQL("alter table AUDIO ADD COLUMN IS_LOCAL INTEGER default 0");//这个字段没有多大的意义
//                    MigrationHelper.addColumn(db, AudioDao.TABLENAME, AudioDao.Properties.OperTime.columnName, "INTEGER", "0");
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
                case 12:
                    MigrationHelper.addTextColumn(db, BreakpointAudioDao.TABLENAME, BreakpointAudioDao.Properties.AlbumId.columnName, "");
                    break;
                case 13:
                    MigrationHelper.addColumn(db, AlbumDao.TABLENAME, AlbumDao.Properties.AlbumType.columnName, "INTEGER", "0");
                    break;
                case 14:
                case 15:
                    //为了解决bug：android.database.sqlite.SQLiteConstraintException: NOT NULL constraint failed: AUDIO.IS_LOCAL (code 1299)
                    // 解决的方式是：讲@Transient去掉，就是，还让保存到数据库里面
//                    DaoMaster.dropAllTables(db, true);
//                    DaoMaster.createAllTables(db, false);

                    MigrationHelper.addColumn(db, AudioDao.TABLENAME, AudioDao.Properties.SrcAlbumId.columnName, "INTEGER", "0");
                    MigrationHelper.addColumn(db, AlbumDao.TABLENAME, AlbumDao.Properties.Pid.columnName, "INTEGER", "0");
                    MigrationHelper.addColumn(db, AlbumDao.TABLENAME, AlbumDao.Properties.PSid.columnName, "INTEGER", "0");
                    //因为之前这里将Audio表中的isLocal设置为@Transient，导致出现问题，现在补上
                    MigrationHelper.addColumn(db, AudioDao.TABLENAME, AudioDao.Properties.IsLocal.columnName, "INTEGER", "0");
                    MigrationHelper.addColumn(db, AudioDao.TABLENAME, AudioDao.Properties.IsInsert.columnName, "INTEGER", "0");

                    PlayListDataDao.createTable(db, true);

                    break;
                case 16:
                    MigrationHelper.addColumn(db, AudioDao.TABLENAME, AudioDao.Properties.ProcessIsPost.columnName, "INTEGER", "0");
                    MigrationHelper.addTextColumn(db, AudioDao.TABLENAME, AudioDao.Properties.ProcessHeader.columnName, "");
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


    //把表的数据复制出来一份，然后删除原有的表，再将数据导入回新的表
    private void copyToNewDB(SQLiteDatabase db, int oldVersion, int newVersion) {
        Database database = wrap(db);

    }

}
