package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.util.Log;

import com.txznet.comm.err.Error;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;
import com.txznet.rxflux.extensions.aac.livedata.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * 历史的ViewModule
 *
 * @author telen
 * @date 2018/12/3,10:56
 */
public class HistoryAlbumStore extends Store {
    private List<HistoryAlbum> mHistoryAlbums = new ArrayList<>();
    private MutableLiveData<List<HistoryAlbum>> mHistoryAlbumData = new MutableLiveData<>();
    private LiveData<List<HistoryAlbum>> mHistoryAlbumHidden = Transformations.map(mHistoryAlbumData, input -> {
        if (input != null) {
            Iterator<HistoryAlbum> iterator = input.iterator();
            while (iterator.hasNext()) {
                HistoryAlbum historyAlbum = iterator.next();
                if (historyAlbum.flag == HistoryAlbum.FLAG_HIDDEN) {
                    iterator.remove();
                }
            }
        }
        return input;
    });
    private SingleLiveEvent<Status> mAlbumStatus = new SingleLiveEvent<>();

    /**
     *
     */
    public enum Status {
        /**
         * 没有数据返回
         */
        HISTORY_GET_EMPTY_DATA,
        /**
         * 统一播报网络超时,稍后重试
         */
        HISTORY_NET_ERROR,
        /**
         * 删除失败
         */
        HISTORY_DELETE_FAIL,
    }

    public LiveData<List<HistoryAlbum>> getHistoryAlbumData() {
        return mHistoryAlbumHidden;
    }


    public LiveData<HistoryAlbumStore.Status> getAlbumStatus() {
        return mAlbumStatus;
    }

    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_GET_HISTORY_ALBUM,
                ActionType.ACTION_GET_ADD_ITEM_HISTORY_ALBUM,
                ActionType.ACTION_GET_DEL_ITEM_HISTORY_ALBUM
        };
    }

    @Override
    protected void onAction(RxAction action) {
    }

    @Override
    protected void onData(RxAction action) {
        switch (action.type) {
            //电台相关
            case ActionType.ACTION_GET_HISTORY_ALBUM:
                Object albums = action.data.get(Constant.HistoryConstant.KEY_HISTORY_MUSIC_AUDIOS);
                mHistoryAlbums.clear();
                mHistoryAlbums.addAll((Collection<? extends HistoryAlbum>) albums);
                mHistoryAlbumData.setValue(mHistoryAlbums);
                break;
            case ActionType.ACTION_GET_DEL_ITEM_HISTORY_ALBUM:
                List<HistoryAlbum> historyAlbums = (List<HistoryAlbum>) action.data.get(Constant.HistoryConstant.KEY_HISTORY_ALBUMS_DELETE);
                mHistoryAlbums.removeAll(historyAlbums);
                mHistoryAlbumData.setValue(mHistoryAlbums);
                break;

            case ActionType.ACTION_GET_ADD_ITEM_HISTORY_ALBUM:
                HistoryAlbum historyAlbum = (HistoryAlbum) action.data.get(Constant.HistoryConstant.KEY_HISTORY_ALBUM);
                mHistoryAlbums.remove(historyAlbum);
                mHistoryAlbums.add(0, historyAlbum);
                mHistoryAlbumData.setValue(mHistoryAlbums);
                break;
            default:
                Log.d("HistoryAlbumStore", "onAction:" + action.type);
                break;

        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {
        switch (action.type) {
            case ActionType.ACTION_GET_HISTORY_ALBUM:
                setStatus(mAlbumStatus, throwable);
                break;
            case ActionType.ACTION_GET_DEL_ITEM_HISTORY_ALBUM:
                if (throwable instanceof Error) {
                    mAlbumStatus.setValue(Status.HISTORY_DELETE_FAIL);
                }
                break;
            default:
                Log.d("HistoryAlbumStore", "onError:" + action.type);
                break;

        }
    }

    private void setStatus(SingleLiveEvent<Status> event, Throwable throwable) {
        if (throwable instanceof Error) {
            int errorCode = ((Error) throwable).errorCode;
            if (errorCode == ErrCode.ERROR_CLIENT_NET_EMPTY_DATA) {
                event.setValue(Status.HISTORY_GET_EMPTY_DATA);
            } else if (errorCode == ErrCode.ERROR_INNER_WRONG) {
                event.setValue(Status.HISTORY_NET_ERROR);
            } else {
                event.setValue(Status.HISTORY_NET_ERROR);
            }
        }
    }
}
