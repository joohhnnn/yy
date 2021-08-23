package com.txznet.music.data.db.compat;

import android.database.sqlite.SQLiteDatabase;

import com.txznet.music.data.entity.Breakpoint;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.music.data.entity.PlayListData;
import com.txznet.music.data.entity.PushItem;

import java.util.List;

/**
 * 数据库兼容处理
 *
 * @author zackzhou
 * @date 2019/5/5,11:15
 */

public interface IDbCompat {

    /**
     * 导出历史音频
     */
    List<HistoryAudio> exportHistoryAudio(SQLiteDatabase database);

    /**
     * 导出历史专辑
     */
    List<HistoryAlbum> exportHistoryAlbum(SQLiteDatabase database);

    /**
     * 导出断点数据
     */
    List<Breakpoint> exportBreakpoint(SQLiteDatabase database);

    /**
     * 导出微信推送
     */
    List<PushItem> exportPushItem(SQLiteDatabase database);

    /**
     * 导出最后播放
     */
    PlayListData exportPlayListData(SQLiteDatabase database);
}
