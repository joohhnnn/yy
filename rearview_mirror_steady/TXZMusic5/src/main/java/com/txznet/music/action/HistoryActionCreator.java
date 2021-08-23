package com.txznet.music.action;

import com.txznet.music.Constant;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

import java.util.ArrayList;
import java.util.List;

/**
 * 历史的Action产生的位置
 *
 * @author telen
 * @date 2018/12/3,11:07
 */
public class HistoryActionCreator {


    /**
     * 单例对象
     */
    private volatile static HistoryActionCreator singleton;

    private HistoryActionCreator() {
    }

    public static HistoryActionCreator getInstance() {
        if (singleton == null) {
            synchronized (HistoryActionCreator.class) {
                if (singleton == null) {
                    singleton = new HistoryActionCreator();
                }
            }
        }
        return singleton;
    }

    public void getHistoryMusicData(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_GET_HISTORY_MUSIC).operation(operation).build());
    }


    public void deleteHistoryMusicItem(Operation operation, HistoryAudio historyAuido) {
        List<HistoryAudio> historyAudios = new ArrayList<>();
        historyAudios.add(historyAuido);
        deleteHistoryMusicItem(operation, historyAudios);
    }

    public void deleteHistoryMusicItem(Operation operation, List<HistoryAudio> historyAuidos) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_GET_DEL_ITEM_HISTORY_MUSIC).operation(operation).bundle(Constant.HistoryConstant.KEY_HISTORY_MUSIC_AUDIOS, historyAuidos).build());
    }

    public void getHistoryAlbumData(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_GET_HISTORY_ALBUM).operation(operation).build());
    }


    public void deleteHistoryAlbumItem(Operation operation, List<HistoryAlbum> historyAlbums) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_GET_DEL_ITEM_HISTORY_ALBUM).operation(operation).bundle(Constant.HistoryConstant.KEY_HISTORY_ALBUMS_DELETE, historyAlbums).build());
    }
}
