package com.txznet.music.helper;

import com.txznet.music.Constant;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.BeSendData;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.data.entity.SubscribeAlbum;
import com.txznet.music.data.http.api.txz.entity.TXZAlbum;
import com.txznet.music.report.entity.ReportAlbum;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.TimeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 专辑转换帮助类
 *
 * @author telen
 * @date 2018/12/6,10:26
 */
public class AlbumConverts {
    public static Album convert2Album(TXZAlbum txzAlbum) {
        if (txzAlbum == null) {
            return null;
        }
        Album album = new Album();
        album.id = txzAlbum.id;
        album.sid = txzAlbum.sid;
        album.name = txzAlbum.name;
        album.logo = txzAlbum.logo;
        album.albumType = txzAlbum.albumType;
        album.setExtraKey(Constant.AlbumExtra.SEARCH_REPORT, txzAlbum.report);
        album.setExtraKey(Constant.AlbumExtra.SEARCH_NOVEL_STATUS, txzAlbum.serialize);//小说的连载属性
        if (txzAlbum.lastListen != 0) {
            //上次收听
            album.setExtraKey(Constant.AlbumExtra.SEARCH_LAST_LISTEN, txzAlbum.lastListen);
        }
        album.setExtraKey(Constant.AlbumExtra.SEARCH_SUBTITLE, txzAlbum.tips);
        album.setExtraKey(Constant.AlbumExtra.PAGE_SIZE, txzAlbum.pageSize);

        return album;
    }

    /**
     * 将普通的album转换成订阅的album
     *
     * @param album     待转换的album
     * @param timestamp 时间戳
     * @return 订阅的album
     */
    public static SubscribeAlbum convert2subscribe(Album album, long timestamp) {
        SubscribeAlbum subscribeAlbum = JsonHelper.fromJson(JsonHelper.toJson(album), SubscribeAlbum.class);
        if (timestamp != 0) {
            subscribeAlbum.timestamp = timestamp;
        } else {
            subscribeAlbum.timestamp = TimeManager.getInstance().getTimeMillis();
        }
        subscribeAlbum.extra = album.extra;
        return subscribeAlbum;
    }

    public static BeSendData convertSubscribeAlbum2BeSendData(SubscribeAlbum subscribeAlbum, AudioV5 audioV5, @BeSendData.OperationType int operationType) {
        BeSendData beSendData = new BeSendData();
        beSendData.operation = operationType;
        beSendData.id = subscribeAlbum.id;
        beSendData.sid = subscribeAlbum.sid;
        beSendData.timestamp = subscribeAlbum.timestamp;
        if (audioV5 != null) {
//            beSendData.svrData = audioV5.getExtraKey(Constant.AudioExtra.SVR_DATA);
        }
        return beSendData;
    }


    public static HistoryAlbum convert2HistoryAlbum(Album album) {
        return JsonHelper.fromJson(JsonHelper.toJson(album), HistoryAlbum.class);
    }

    public static Album convert2Album(HistoryAlbum historyAlbum) {
        return JsonHelper.fromJson(JsonHelper.toJson(historyAlbum), Album.class);
    }

    public static ReportAlbum convert2Report(Album album) {
        if (album == null) {
            return new ReportAlbum();
        }
        ReportAlbum reportAlbum = new ReportAlbum();
        reportAlbum.albumId = album.id;
        reportAlbum.albumSid = album.sid;
        return reportAlbum;
    }

    public interface Convert<T, V> {
        V convert(T t);
    }

    public static <T, V> void convert2List(List<T> src, List<V> dest, Convert<T, V> convert) {
        if (dest == null || src == null) {
            return;
        }

        if (src.size() > 0) {
            for (T t : src) {
                dest.add(convert.convert(t));
            }
        }
    }

    public static <T, V> List<V> convert2List(List<T> src, Convert<T, V> convert) {
        if (src == null) {
            return null;
        }
        List<V> dest = new ArrayList<>(src.size());
        convert2List(src, dest, convert);
        return dest;
    }
}