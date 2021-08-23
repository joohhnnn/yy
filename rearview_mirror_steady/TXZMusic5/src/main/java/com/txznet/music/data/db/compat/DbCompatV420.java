package com.txznet.music.data.db.compat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.txznet.audio.player.entity.Audio;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;
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
 */
public class DbCompatV420 implements IDbCompat {

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
        String sql = "SELECT * FROM HISTORY_DATA";
        try (Cursor cursor = database.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                try {
                    String audioRowId = cursor.getString(cursor.getColumnIndex("AUDIO_ROW_ID"));
                    String[] audioSplit = audioRowId.split("-");
                    int sid = Integer.parseInt(audioSplit[0]);
                    long id = Long.parseLong(audioSplit[1]);
                    int type = cursor.getInt(cursor.getColumnIndex("TYPE"));
                    // 绕过本地音乐
                    if (sid == 0) {
                        continue;
                    }
                    HistoryAudio tmp = exportHistoryAudio(database, sid, id);
                    if (type == 1) {
                        // 历史音乐
                        if (tmp != null) {
                            historyAudioList.add(tmp);
                        }
                    } else {
                        // 历史专辑
                        String albumRowId = cursor.getString(cursor.getColumnIndex("ALBUM_ROW_ID"));
                        String[] albumSplit = albumRowId.split("-");
                        int albumSid = Integer.parseInt(albumSplit[0]);
                        long albumId = Long.parseLong(albumSplit[1]);
                        HistoryAlbum tmpAlbum = exportHistoryAlbum(database, tmp, albumSid, albumId);
                        if (tmpAlbum != null) {
                            historyAlbumList.add(tmpAlbum);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private HistoryAlbum exportHistoryAlbum(SQLiteDatabase database, AudioV5 audio, int albumSid, long albumId) {
        String sql = "SELECT * FROM ALBUM " +
                "WHERE SID = ?" +
                "AND ID = ?";
        try (Cursor s_cursor = database.rawQuery(sql, new String[]{albumSid + "", albumId + ""})) {
            if (s_cursor.moveToNext()) {
                HistoryAlbum historyAlbum = new HistoryAlbum();
                historyAlbum.audio = audio;
                historyAlbum.sid = albumSid;
                historyAlbum.id = albumId;
                historyAlbum.name = s_cursor.getString(s_cursor.getColumnIndex("NAME"));
                historyAlbum.logo = s_cursor.getString(s_cursor.getColumnIndex("LOGO"));
                historyAlbum.report = s_cursor.getString(s_cursor.getColumnIndex("REPORT"));
                if (historyAlbum.sid == 8) { // 当前新闻资源只接了乐听，sid=8
                    historyAlbum.albumType = Album.ALBUM_TYPE_NEWS;
                } else {
                    int albumTypeColIndex = s_cursor.getColumnIndex("ALBUM_TYPE");
                    if (albumTypeColIndex != -1) {
                        historyAlbum.albumType = s_cursor.getInt(albumTypeColIndex);
                    }
                    if (historyAlbum.albumType == 0) {
                        historyAlbum.albumType = Album.ALBUM_TYPE_FM;
                    }
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
                    breakpoint.playEndCount = cursor.getInt(cursor.getColumnIndex("PLAY_END_COUNT"));
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
        List<PushItem> pushItemList = new ArrayList<>();
        String sql = "SELECT * FROM MESSAGE";
        try (Cursor cursor = database.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                try {
                    String audios = cursor.getString(cursor.getColumnIndex("AUDIOS"));
                    JsonArray jArr = new JsonParser().parse(audios).getAsJsonArray();
                    for (int i = 0; i < jArr.size(); i++) {
                        JsonObject jObj = jArr.get(i).getAsJsonObject();
                        PushItem pushItem = new PushItem();
                        pushItem.id = jObj.get("id").getAsLong();
                        pushItem.sid = jObj.get("sid").getAsInt();
                        pushItem.name = jObj.get("name").getAsString();
                        String processUrl = jObj.get("strProcessingUrl").getAsString();
                        String downloadUrl = jObj.get("strDownloadUrl").getAsString();
                        String downloadType = jObj.get("downloadType").getAsString();
                        pushItem.sourceUrl = TXZUri.fromParts(processUrl, downloadUrl, TextUtils.equals("1", downloadType) ? TXZAudio.DOWNLOADTYPE_PROXY : TXZAudio.DOWNLOADTYPE_DIRECT).toString();
                        pushItem.announce = jObj.get("report").getAsString();
                        pushItem.status = cursor.getInt(cursor.getColumnIndex("STATUS"));
                        pushItem.timestamp = cursor.getLong(cursor.getColumnIndex("TIME")) * 1000;
                        pushItemList.add(pushItem);
                    }

                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
        return pushItemList;
    }

    @Override
    public PlayListData exportPlayListData(SQLiteDatabase database) {
        String sql = "SELECT * FROM PLAY_LIST_DATA";
        try (Cursor cursor = database.rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                int dataOri = cursor.getInt(cursor.getColumnIndex("DATA_ORI"));
                PlayListData playListData = new PlayListData();
                /*
                  public static final int DATA_INIT = 0;
                    public static final int DATA_ALBUM = 1;
                    public static final int DATA_ALBUM_MANUFACTORY = 50;//50开始
                    public static final int DATA_HISTORY = 2;
                    public static final int DATA_LOCAL = 3;
                    public static final int DATA_SEARCH = 4;
                    public static final int DATA_FAVOUR = 5;
                    public static final int DATA_SUBSCRIBE = 6;
                    public static final int DATA_PUSH = 7;//推送  后台定义，值为100-200（后台有特殊用处）（客户端可以判断100-200来判断是否是推送，并可以做区别处理）
                    public static final int DATA_MESSAGE = 8;
                    public static final int DATA_CHEZHU_FM = 9;//车主FM,因为拥有特殊操作,播放完毕则跳过.且轮询之后,不跳过.
                    public static final int DATA_CHEZHU_TYPE_FM = 10;//分类FM
                 */
                switch (dataOri) {
                    case 0:
                        playListData.scene = PlayScene.IDLE;
                        break;
                    case 1:
                    case 50:
                    case 9:
                    case 10:
                    case 7:
                    case 4:
                        playListData.scene = PlayScene.ALBUM;
                        break;
                    case 3:
                        playListData.scene = PlayScene.LOCAL_MUSIC;
                        break;
                    case 2:
                        playListData.scene = PlayScene.HISTORY_MUSIC;
                        break;
                    case 5:
                        playListData.scene = PlayScene.FAVOUR_MUSIC;
                        break;
                    case 6:
                        playListData.scene = PlayScene.FAVOUR_ALBUM;
                        break;
                    case 8:
                        playListData.scene = PlayScene.WECHAT_PUSH;
                        break;
                }

                String albumDbId = cursor.getString(cursor.getColumnIndex("ALBUM_DB_ID"));
                String audioDbId = cursor.getString(cursor.getColumnIndex("AUDIO_DB_ID"));

                if (TextUtils.isEmpty(albumDbId) && TextUtils.isEmpty(audioDbId)) {
                    playListData.scene = PlayScene.HISTORY_ALBUM;
                    sql = "SELECT * FROM HISTORY_DATA, ALBUM " +
                            "WHERE HISTORY_DATA.TYPE =2 AND HISTORY_DATA.ALBUM_ROW_ID = ALBUM.ALBUM_DB_ID  ORDER BY _id DESC LIMIT 1";
                    try (Cursor albumCursor = database.rawQuery(sql, null)) {
                        if (albumCursor.moveToNext()) {
                            Album album = new Album();
                            album.sid = albumCursor.getInt(albumCursor.getColumnIndex("SID"));
                            album.id = albumCursor.getLong(albumCursor.getColumnIndex("ID"));
                            album.name = albumCursor.getString(albumCursor.getColumnIndex("NAME"));
                            album.logo = albumCursor.getString(albumCursor.getColumnIndex("LOGO"));
                            album.report = albumCursor.getString(albumCursor.getColumnIndex("REPORT"));
                            if (album.sid == 8) { // 当前新闻资源只接了乐听，sid=8
                                album.albumType = Album.ALBUM_TYPE_NEWS;
                            } else {
                                album.albumType = albumCursor.getInt(albumCursor.getColumnIndex("ALBUM_TYPE"));
                                if (album.albumType == 0) {
                                    album.albumType = Album.ALBUM_TYPE_FM;
                                }
                            }
                            playListData.album = album;
                        }
                    }
                    return playListData;
                }

                if (!TextUtils.isEmpty(albumDbId)) {
                    sql = "SELECT * FROM ALBUM WHERE ALBUM_DB_ID = ?";
                    try (Cursor albumCursor = database.rawQuery(sql, new String[]{albumDbId})) {
                        if (albumCursor.moveToNext()) {
                            Album album = new Album();
                            album.sid = albumCursor.getInt(albumCursor.getColumnIndex("SID"));
                            album.id = albumCursor.getLong(albumCursor.getColumnIndex("ID"));
                            album.name = albumCursor.getString(albumCursor.getColumnIndex("NAME"));
                            album.logo = albumCursor.getString(albumCursor.getColumnIndex("LOGO"));
                            album.report = albumCursor.getString(albumCursor.getColumnIndex("REPORT"));
                            if (album.sid == 8) { // 当前新闻资源只接了乐听，sid=8
                                album.albumType = Album.ALBUM_TYPE_NEWS;
                            } else {
                                album.albumType = albumCursor.getInt(albumCursor.getColumnIndex("ALBUM_TYPE"));
                                if (album.albumType == 0) {
                                    album.albumType = Album.ALBUM_TYPE_FM;
                                }
                            }
                            playListData.album = album;
                        }
                    } catch (Exception e) {
                    }
                }

                if (!TextUtils.isEmpty(audioDbId)) {
                    sql = "SELECT * FROM AUDIO WHERE AUDIO_DB_ID = ?";
                    try (Cursor audioCursor = database.rawQuery(sql, new String[]{audioDbId})) {
                        if (audioCursor.moveToNext()) {
                            Audio audio = new Audio();
                            audio.sid = audioCursor.getInt(audioCursor.getColumnIndex("SID"));
                            audio.albumSid = audio.sid;
                            audio.id = audioCursor.getLong(audioCursor.getColumnIndex("ID"));
                            audio.name = audioCursor.getString(audioCursor.getColumnIndex("NAME"));
                            audio.albumId = audioCursor.getLong(audioCursor.getColumnIndex("ALBUM_ID"));
                            // FIXME: 2019/5/9 该版本没有albumSid记录
                            audio.albumName = audioCursor.getString(audioCursor.getColumnIndex("ALBUM_NAME"));
                            String artistArr = audioCursor.getString(audioCursor.getColumnIndex("ARR_ARTIST_NAME"));
                            if (artistArr != null) {
                                audio.artist = artistArr.split(",");
                            }
                            // FIXME: 2019/5/9 该版本没有Logo
                            String processUrl = audioCursor.getString(audioCursor.getColumnIndex("STR_PROCESSING_URL"));
                            String downloadUrl = audioCursor.getString(audioCursor.getColumnIndex("STR_DOWNLOAD_URL"));
                            String downloadType = audioCursor.getString(audioCursor.getColumnIndex("DOWNLOAD_TYPE"));
                            // FIXME: 2019/5/24 兼容乐听
                            if (!AudioUtils.isNetSong(audio.sid) && TXZAudio.DOWNLOADTYPE_PROXY.equals(downloadType)) {
                                downloadType = TXZAudio.DOWNLOADTYPE_DIRECT;
                            }
                            audio.sourceUrl = TXZUri.fromParts(processUrl, downloadUrl, TextUtils.equals("1", downloadType) ? TXZAudio.DOWNLOADTYPE_PROXY : TXZAudio.DOWNLOADTYPE_DIRECT).toString();
                            String report = audioCursor.getString(audioCursor.getColumnIndex("REPORT"));
                            if (report != null) {
                                audio.setExtraKey(Constant.AudioExtra.REPORT, report);
                            }
                            playListData.audio = audio;
                        }
                    } catch (Exception e) {
                    }
                }

                String audioStr = cursor.getString(cursor.getColumnIndex("AUDIO_STR"));
                if (!TextUtils.isEmpty(audioStr)) {
                    JsonArray jArr = new JsonParser().parse(audioStr).getAsJsonArray();
                    for (int i = 0; i < jArr.size(); i++) {
                        try {
                            JsonObject jObj = jArr.get(i).getAsJsonObject();
                            Audio audio = new Audio();
                            audio.id = jObj.get("id").getAsLong();
                            audio.sid = jObj.get("sid").getAsInt();
                            audio.name = jObj.get("name").getAsString();
                            audio.albumId = jObj.get("albumId").getAsLong();
                            audio.albumName = jObj.get("albumName").getAsString();
                            try {
                                JsonArray arrArtistName = jObj.get("arrArtistName").getAsJsonArray();
                                if (arrArtistName != null && arrArtistName.size() > 0) {
                                    audio.artist = new String[arrArtistName.size()];
                                    for (int j = 0; j < arrArtistName.size(); j++) {
                                        audio.artist[j] = arrArtistName.get(j).getAsString();
                                    }
                                }
                            } catch (Exception e) {

                            }
                            String processUrl = jObj.get("strProcessingUrl").getAsString();
                            String downloadUrl = jObj.get("strDownloadUrl").getAsString();
                            String downloadType = jObj.get("downloadType").getAsString();
                            // FIXME: 2019/5/24 兼容乐听
                            if (!AudioUtils.isNetSong(audio.sid) && TXZAudio.DOWNLOADTYPE_PROXY.equals(downloadType)) {
                                downloadType = TXZAudio.DOWNLOADTYPE_DIRECT;
                            }
                            // 4.x “1”表示需要预加载，其他表示不需要
                            audio.sourceUrl = TXZUri.fromParts(processUrl, downloadUrl, TextUtils.equals("1", downloadType) ? TXZAudio.DOWNLOADTYPE_PROXY : TXZAudio.DOWNLOADTYPE_DIRECT).toString();
                            String report = jObj.get("report").getAsString();
                            if (report != null) {
                                audio.setExtraKey(Constant.AudioExtra.REPORT, report);
                            }
                            if (playListData.audioList == null) {
                                playListData.audioList = new ArrayList<>(jArr.size());
                            }
                            LogUtil.d("export downloadType=" + audio.sourceUrl + ", audio=" + audio);
                            playListData.audioList.add(audio);
                        } catch (Exception e) {

                        }
                    }
                }
                return playListData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        sql = "SELECT * FROM HISTORY_DATA ORDER BY rowid DESC LIMIT 1";
        try (Cursor cursor = database.rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                int type = cursor.getInt(cursor.getColumnIndex("TYPE"));
                PlayListData playListData = new PlayListData();
                if (type == 1) {
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
