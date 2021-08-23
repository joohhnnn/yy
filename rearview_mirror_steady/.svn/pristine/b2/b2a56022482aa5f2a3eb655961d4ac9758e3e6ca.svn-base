package com.txznet.music.data.db.compat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.Breakpoint;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.music.data.entity.PlayListData;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.http.api.txz.entity.TXZAudio;
import com.txznet.music.helper.TXZUri;
import com.txznet.music.util.AudioUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 对同听4.x的数据库兼容
 * 版本=[4.1~4.2)，
 * #该版本不支持微信推送
 * #该版本历史音乐和历史电台在一张表格
 */
public class DbCompatV410 implements IDbCompat {

    private List<HistoryAudio> historyAudioList = new ArrayList<>();
    private List<HistoryAlbum> historyAlbumList = new ArrayList<>();
    private boolean hasExportHistory;

    @Override
    public List<HistoryAudio> exportHistoryAudio(SQLiteDatabase database) {
        if (!hasExportHistory) {
            exportHistory(database);
        }
        return historyAudioList;
    }

    @Override
    public List<HistoryAlbum> exportHistoryAlbum(SQLiteDatabase database) {
        if (!hasExportHistory) {
            exportHistory(database);
        }
        return historyAlbumList;
    }

    private void exportHistory(SQLiteDatabase database) {
        try (Cursor cursor = database.query(true, "HISTORY_AUDIO", new String[]{"AUDIO_DB_ID"},
                null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                try {
                    String audioDbId = cursor.getString(0);
                    String[] idSplitArr = audioDbId.split("-");
                    int sid = Integer.valueOf(idSplitArr[0]);
                    long id = Long.valueOf(idSplitArr[1]);
                    // 绕过本地音乐
                    if (sid == 0) {
                        continue;
                    }
                    HistoryAudio tmp = exportHistoryAudio(database, sid, id);
                    if (AudioUtils.isSong(sid)) {
                        // 历史音乐
                        if (tmp != null) {
                            historyAudioList.add(tmp);
                        }
                    } else {
                        // 历史专辑
                        HistoryAlbum tmpAlbum = exportHistoryAlbum(database, tmp);
                        if (tmpAlbum != null) {
                            historyAlbumList.add(tmpAlbum);
                        }
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
        hasExportHistory = true;
    }

    private HistoryAudio exportHistoryAudio(SQLiteDatabase database, int sid, long id) {
        String sql = "SELECT * FROM AUDIO " +
                "WHERE AUDIO.SID = ? " +
                "AND AUDIO.ID = ?";
        try (Cursor cursor = database.rawQuery(sql, new String[]{sid + "", id + ""})) {
            if (cursor.moveToNext()) {
                HistoryAudio audio = new HistoryAudio();
                audio.sid = cursor.getInt(cursor.getColumnIndex("SID"));
                audio.albumSid = audio.sid;
                audio.id = cursor.getLong(cursor.getColumnIndex("ID"));
                audio.name = cursor.getString(cursor.getColumnIndex("NAME"));
                audio.albumId = cursor.getLong(cursor.getColumnIndex("ALBUM_ID"));
                audio.albumName = cursor.getString(cursor.getColumnIndex("ALBUM_NAME"));
                String artistArr = cursor.getString(cursor.getColumnIndex("ARR_ARTIST_NAME"));
                if (artistArr != null) {
                    audio.artist = artistArr.split(",");
                }
                // FIXME: 2019/5/9 该版本没有Logo
                String processUrl = cursor.getString(cursor.getColumnIndex("STR_PROCESSING_URL"));
                String downloadUrl = cursor.getString(cursor.getColumnIndex("STR_DOWNLOAD_URL"));
                String downloadType = cursor.getString(cursor.getColumnIndex("DOWNLOAD_TYPE"));
                // FIXME: 2019/5/24 兼容乐听
                if (!AudioUtils.isNetSong(audio.sid) && TXZAudio.DOWNLOADTYPE_PROXY.equals(downloadType)) {
                    downloadType = TXZAudio.DOWNLOADTYPE_DIRECT;
                }
                audio.sourceUrl = TXZUri.fromParts(processUrl, downloadUrl, TextUtils.equals("1", downloadType) ? TXZAudio.DOWNLOADTYPE_PROXY : TXZAudio.DOWNLOADTYPE_DIRECT).toString();
                audio.announce = cursor.getString(cursor.getColumnIndex("REPORT"));
                return audio;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private HistoryAlbum exportHistoryAlbum(SQLiteDatabase database, AudioV5 audio) {
        String sql = "SELECT * FROM ALBUM WHERE ID = ?";
        try (Cursor s_cursor = database.rawQuery(sql, new String[]{audio.albumId + ""})) {
            if (s_cursor.moveToNext()) {
                HistoryAlbum historyAlbum = new HistoryAlbum();
                historyAlbum.audio = audio;
                historyAlbum.sid = s_cursor.getInt(s_cursor.getColumnIndex("SID"));
                historyAlbum.id = audio.albumId;
                historyAlbum.name = s_cursor.getString(s_cursor.getColumnIndex("NAME"));
                historyAlbum.logo = s_cursor.getString(s_cursor.getColumnIndex("LOGO"));
                historyAlbum.report = s_cursor.getString(s_cursor.getColumnIndex("REPORT"));
                if (historyAlbum.sid == 8) { // 当前新闻资源只接了乐听，sid=8
                    historyAlbum.albumType = Album.ALBUM_TYPE_NEWS;
                } else {
                    historyAlbum.albumType = Album.ALBUM_TYPE_FM;
                }
                return historyAlbum;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Breakpoint> exportBreakpoint(SQLiteDatabase database) {
        List<Breakpoint> breakpointList = new ArrayList<>();
        String sql = "SELECT * FROM BREAKPOINT_AUDIO, AUDIO WHERE BREAKPOINT_AUDIO.SID = AUDIO.SID AND BREAKPOINT_AUDIO.ID = AUDIO.ID";
        try (Cursor cursor = database.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                try {
                    Breakpoint breakpoint = new Breakpoint();
                    breakpoint.position = cursor.getLong(cursor.getColumnIndex("BREAKPOINT")) * 1000;
                    breakpoint.duration = cursor.getLong(cursor.getColumnIndex("DURATION")) * 1000;
                    breakpoint.sid = cursor.getInt(cursor.getColumnIndex("SID"));
                    breakpoint.id = cursor.getLong(cursor.getColumnIndex("ID"));
                    int albumSid = breakpoint.sid;
                    long albumId = cursor.getLong(cursor.getColumnIndex("ALBUM_ID"));
                    breakpoint.albumId = albumSid + "-" + albumId;
                    breakpointList.add(breakpoint);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
        return breakpointList;
    }

    @Override
    public List<PushItem> exportPushItem(SQLiteDatabase database) {
        // 这个版本没有推送
        return null;
    }

    @Override
    public PlayListData exportPlayListData(SQLiteDatabase database) {
        // 该版本关闭后再次进入，播的都是历史
        String sql = "SELECT AUDIO_DB_ID FROM HISTORY_AUDIO ORDER BY rowid DESC LIMIT 1";
        try (Cursor cursor = database.rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                String audioDbId = cursor.getString(0);
                String[] idSplitArr = audioDbId.split("-");
                int sid = Integer.valueOf(idSplitArr[0]);
                long id = Long.valueOf(idSplitArr[1]);

                PlayListData playListData = new PlayListData();
                if (AudioUtils.isSong(sid)) {
                    playListData.scene = PlayScene.HISTORY_MUSIC;
                } else {
                    playListData.scene = PlayScene.HISTORY_ALBUM;
                }
                return playListData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
