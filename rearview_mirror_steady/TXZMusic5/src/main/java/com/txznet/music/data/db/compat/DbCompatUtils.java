package com.txznet.music.data.db.compat;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.database.sqlite.SQLiteDatabase;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.Constant;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.PlayListDataDao;
import com.txznet.music.data.entity.Breakpoint;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.music.data.entity.PlayListData;
import com.txznet.music.data.entity.PlayMode;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.util.GcTrigger;
import com.txznet.music.util.Logger;

import java.util.List;

import io.reactivex.schedulers.Schedulers;

/**
 * 数据库兼容迁移工具
 *
 * @author zackzhou
 * @date 2019/5/9,14:27
 */

public class DbCompatUtils {

    private static final String TAG = Constant.LOG_TAG_DB + ":Migration";

    private static final String DB_NAME_420_UP = "txz_music_new.db";
    private static final String DB_NAME_410_UP = "txz_music.db";
    private static final String DB_NAME_400 = "txz.sql";

    private static List<HistoryAudio> historyAudioList;
    private static List<HistoryAlbum> historyAlbumList;
    private static List<PushItem> pushItemList;
    private static List<Breakpoint> breakpointList;
    private static PlayListData playListData;

    private DbCompatUtils() {

    }

    /**
     * 执行数据库迁移
     */
    public static void doMigration(SupportSQLiteDatabase database) {
        if (!export(DB_NAME_420_UP, new DbCompatV420())) {
            if (!export(DB_NAME_410_UP, new DbCompatV410())) {
                if (!export(DB_NAME_400, new DbCompatV400())) {
                    Logger.w(TAG, "migration passed, no history db find");
                }
            }
        }
        Schedulers.io().scheduleDirect(() -> {
            Logger.d(TAG, "begin migration");
            if (historyAudioList != null) {
                DBUtils.getDatabase(GlobalContext.get()).getHistoryAudioDao().saveOrUpdate(historyAudioList);
                Logger.d(TAG, "migration historyAudio=" + historyAudioList.size());
                historyAudioList = null;
            }
            if (historyAlbumList != null) {
                DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao().saveOrUpdate(historyAlbumList);
                Logger.d(TAG, "migration historyAlbum=" + historyAlbumList.size());
                historyAlbumList = null;
            }
            if (pushItemList != null) {
                DBUtils.getDatabase(GlobalContext.get()).getPushItemDao().saveOrUpdate(pushItemList);
                Logger.d(TAG, "migration pushItem=" + pushItemList.size());
                pushItemList = null;
            }
            if (breakpointList != null) {
                DBUtils.getDatabase(GlobalContext.get()).getBreakpointDao().saveOrUpdate(breakpointList);
                Logger.d(TAG, "migration breakpoint=" + breakpointList.size());
                pushItemList = null;
            }
            if (playListData != null) {
                PlayListDataDao playListDataDao = DBUtils.getDatabase(GlobalContext.get()).getPlayListDataDao();
                PlayListData dbPlayListData = playListDataDao.getPlayListData();
                if (dbPlayListData == null) {
                    dbPlayListData = new PlayListData();
                }
                dbPlayListData.scene = playListData.scene;
                dbPlayListData.album = playListData.album;
                dbPlayListData.audio = playListData.audio;
                dbPlayListData.audioList = playListData.audioList;
                playListDataDao.saveOrUpdate(dbPlayListData);
                Logger.d(TAG, "migration playListData=" + playListData);
                playListData = null;
            }

            Logger.d(TAG, "finish migration");
            GcTrigger.runGc();
        });

        migrationConfig();
    }

    private static void migrationConfig() {
        int playMode = SharedPreferencesUtils.getPlayMode();
        PlayMode newPlayMode = PlayMode.QUEUE_LOOP;
        switch (playMode) {
            case 0: // 循环
                newPlayMode = PlayMode.QUEUE_LOOP;
                break;
            case 1: // 单曲
                newPlayMode = PlayMode.SINGLE_LOOP;
                break;
            case 2: // 随机
                newPlayMode = PlayMode.RANDOM_PLAY;
                break;
        }
        Logger.d(TAG, "migration config, playMode=" + newPlayMode);
        SharedPreferencesUtils.setMusicPlayMode(newPlayMode);
    }

    // 执行数据库迁移
    private static boolean export(String dbName, IDbCompat dbCompat) {
        Logger.d(TAG, "begin export");
        String dbPath = GlobalContext.get().getDatabasePath(dbName).getAbsolutePath();
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)) {
            if (db == null) {
                Logger.w(TAG, "export target db=" + dbPath + " open failed");
                return false;
            }
            Logger.w(TAG, "export invoke, target db=" + dbPath);

            historyAudioList = dbCompat.exportHistoryAudio(db);
            Logger.w(TAG, "export historyAudioList=" + historyAudioList);

            historyAlbumList = dbCompat.exportHistoryAlbum(db);
            Logger.w(TAG, "export historyAlbumList=" + historyAlbumList);

            pushItemList = dbCompat.exportPushItem(db);
            Logger.w(TAG, "export pushItemList=" + pushItemList);

            breakpointList = dbCompat.exportBreakpoint(db);
            Logger.w(TAG, "export breakpointList=" + breakpointList);

            playListData = dbCompat.exportPlayListData(db);
            Logger.w(TAG, "export playListData=" + playListData);

        } catch (Exception e) {
            e.printStackTrace();
            Logger.w(TAG, "export error, target db=" + dbPath + ", error=" + e.getMessage());
            return false;
        }
        return true;
    }
}
