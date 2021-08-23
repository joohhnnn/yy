package com.txznet.music.data.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import com.txznet.music.Constant;
import com.txznet.music.data.db.compat.DbCompatUtils;
import com.txznet.music.data.db.convert.AlbumConvert;
import com.txznet.music.data.db.convert.AudioConvert;
import com.txznet.music.data.db.convert.AudioListConvert;
import com.txznet.music.data.db.convert.AudioV5Convert;
import com.txznet.music.data.db.convert.DateConvert;
import com.txznet.music.data.db.convert.LongArrayConvert;
import com.txznet.music.data.db.convert.PlaySceneConvert;
import com.txznet.music.data.db.convert.StringArrayConvert;
import com.txznet.music.data.db.convert.StringListConvert;
import com.txznet.music.data.db.dao.BeSendDataDao;
import com.txznet.music.data.db.dao.BlackListAudioDao;
import com.txznet.music.data.db.dao.BreakpointDao;
import com.txznet.music.data.db.dao.FavourAudioDao;
import com.txznet.music.data.db.dao.HistoryAlbumDao;
import com.txznet.music.data.db.dao.HistoryAudioDao;
import com.txznet.music.data.db.dao.LocalAudioDao;
import com.txznet.music.data.db.dao.PlayListDataDao;
import com.txznet.music.data.db.dao.PlayUrlInfoDao;
import com.txznet.music.data.db.dao.PushItemDao;
import com.txznet.music.data.db.dao.SubscribeAlbumDao;
import com.txznet.music.data.entity.BeSendData;
import com.txznet.music.data.entity.BlackListAudio;
import com.txznet.music.data.entity.Breakpoint;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.entity.PlayListData;
import com.txznet.music.data.entity.PlayUrlInfo;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.entity.SubscribeAlbum;
import com.txznet.music.util.Logger;


@Database(version = DBInfo.VERSION, entities = {
        HistoryAudio.class,
        LocalAudio.class,
        PlayUrlInfo.class,
        SubscribeAlbum.class,
        FavourAudio.class,
        BeSendData.class,
        HistoryAlbum.class,
        PushItem.class,
        Breakpoint.class,
        PlayListData.class,
        BlackListAudio.class
}, exportSchema = false)
@TypeConverters({StringArrayConvert.class, StringListConvert.class, DateConvert.class, LongArrayConvert.class, AudioListConvert.class, AudioConvert.class, AlbumConvert.class, PlaySceneConvert.class, AudioV5Convert.class})
public abstract class DBUtils extends RoomDatabase {

    private static DBUtils INSTANCE;

    public static DBUtils getDatabase(Context ctx) {
        return INSTANCE == null ? INSTANCE = buildDatabase(ctx.getApplicationContext()) : INSTANCE;
    }

    private static DBUtils buildDatabase(Context ctx) {
        return Room.databaseBuilder(ctx, DBUtils.class, DBInfo.NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Logger.d(Constant.LOG_TAG_DB, "onCreate");
                        DbCompatUtils.doMigration(db);
                    }

                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        super.onOpen(db);
                        Logger.d(Constant.LOG_TAG_DB, "onOpen");
                    }
                })
//                .addMigrations(MIGRATION_1_2)
//                .addMigrations(MIGRATION_2_1)
                .setJournalMode(JournalMode.TRUNCATE)
                .build();
    }

//    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//
//        }
//    };
//    private static final Migration MIGRATION_2_1 = new Migration(2, 1) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//
//        }
//    };

    public abstract LocalAudioDao getLocalAudioDao();

    public abstract HistoryAudioDao getHistoryAudioDao();

    public abstract HistoryAlbumDao getHistoryAlbumDao();

    public abstract SubscribeAlbumDao getSubscribeAlbumDao();

    public abstract FavourAudioDao getFavourAudioDao();

    public abstract BeSendDataDao getBeSendDataDao();

    public abstract PlayUrlInfoDao getPlayUrlInfoDao();

    public abstract PushItemDao getPushItemDao();

    public abstract BreakpointDao getBreakpointDao();

    public abstract PlayListDataDao getPlayListDataDao();

    public abstract BlackListAudioDao getBlackListAudioDao();
}
