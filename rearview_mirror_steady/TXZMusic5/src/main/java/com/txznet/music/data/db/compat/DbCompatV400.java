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
 * 对应版本4.0.1-
 * #该版本不支持微信推送
 * #该版本历史音乐和历史电台在一张表格
 */
public class DbCompatV400 implements IDbCompat {

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
        String sql = "SELECT * FROM historyaudio";
        try (Cursor cursor = database.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                try {
                    int sid = cursor.getInt(cursor.getColumnIndex("sid"));
                    long id = cursor.getLong(cursor.getColumnIndex("id"));
                    // 绕过本地音乐
                    if (sid == 0) {
                        continue;
                    }
                    // 历史音乐
                    HistoryAudio tmp = exportHistoryAudio(cursor, sid, id);
                    if (AudioUtils.isSong(sid)) {
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

    private HistoryAudio exportHistoryAudio(Cursor cursor, int sid, long id) {
        try {
            HistoryAudio audio = new HistoryAudio();
            audio.sid = sid;
            audio.id = id;
            audio.name = cursor.getString(cursor.getColumnIndex("name"));
            audio.albumId = cursor.getLong(cursor.getColumnIndex("albumId"));
            audio.albumSid = audio.sid;
            audio.albumName = cursor.getString(cursor.getColumnIndex("albumName"));
            String artistArr = cursor.getString(cursor.getColumnIndex("arrArtistName"));
            if (artistArr != null) {
                audio.artist = artistArr.split(",");
            }
            // FIXME: 2019/5/9 该版本没有Logo
            String processUrl = cursor.getString(cursor.getColumnIndex("strProcessingUrl"));
            String downloadUrl = cursor.getString(cursor.getColumnIndex("strDownloadUrl"));
            String downloadType = cursor.getString(cursor.getColumnIndex("downloadType"));
            // FIXME: 2019/5/24 兼容乐听
            if (!AudioUtils.isNetSong(audio.sid) && TXZAudio.DOWNLOADTYPE_PROXY.equals(downloadType)) {
                downloadType = TXZAudio.DOWNLOADTYPE_DIRECT;
            }
            audio.sourceUrl = TXZUri.fromParts(processUrl, downloadUrl, TextUtils.equals("1", downloadType) ? TXZAudio.DOWNLOADTYPE_PROXY : TXZAudio.DOWNLOADTYPE_DIRECT).toString();
            audio.announce = cursor.getString(cursor.getColumnIndex("report"));
            return audio;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private HistoryAlbum exportHistoryAlbum(SQLiteDatabase database, AudioV5 audio) {
        String sql = "SELECT * FROM album WHERE id = ?";
        try (Cursor s_cursor = database.rawQuery(sql, new String[]{audio.albumId + ""})) {
            if (s_cursor.moveToNext()) {
                HistoryAlbum historyAlbum = new HistoryAlbum();
                historyAlbum.audio = audio;
                historyAlbum.sid = s_cursor.getInt(s_cursor.getColumnIndex("sid"));
                historyAlbum.id = audio.albumId;
                historyAlbum.name = s_cursor.getString(s_cursor.getColumnIndex("name"));
                historyAlbum.logo = s_cursor.getString(s_cursor.getColumnIndex("logo"));
                historyAlbum.report = s_cursor.getString(s_cursor.getColumnIndex("report"));
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
        String sql = "SELECT *, breakpoint.lastPlayTime as b_lastPlayTime  FROM breakpoint, audio WHERE breakpoint.sid = audio.sid AND breakpoint.id = audio.id";
        try (Cursor cursor = database.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                try {
                    Breakpoint breakpoint = new Breakpoint();
                    breakpoint.position = cursor.getLong(cursor.getColumnIndex("b_lastPlayTime")) * 1000;
                    // FIXME: 2019/5/10 4.0可能没有duration记录
                    breakpoint.duration = cursor.getLong(cursor.getColumnIndex("duration")) * 1000;
                    breakpoint.sid = cursor.getInt(cursor.getColumnIndex("sid"));
                    breakpoint.id = cursor.getLong(cursor.getColumnIndex("id"));
                    int albumSid = breakpoint.sid;
                    long albumId = cursor.getLong(cursor.getColumnIndex("albumId"));
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
        String sql = "SELECT * FROM historyaudio ORDER BY rowid DESC LIMIT 1";
        try (Cursor cursor = database.rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                PlayListData playListData = new PlayListData();
                int sid = cursor.getInt(cursor.getColumnIndex("sid"));
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
