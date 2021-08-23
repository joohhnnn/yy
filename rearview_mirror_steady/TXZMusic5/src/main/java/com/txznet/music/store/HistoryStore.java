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
import com.txznet.music.data.entity.HistoryAudio;
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
public class HistoryStore extends Store {

    private List<HistoryAudio> mHistoryAudios = new ArrayList<>();
    private MutableLiveData<List<HistoryAudio>> mHistoryMusicData = new MutableLiveData<>();
    private SingleLiveEvent<Status> mMusicStatus = new SingleLiveEvent<>();

    private List<HistoryAlbum> mHistoryAlbums = new ArrayList<>();
    private MutableLiveData<List<HistoryAlbum>> mHistoryAlbumData = new MutableLiveData<>();
    private LiveData<List<HistoryAlbum>> mHistoryMusicDataHidden = Transformations.map(mHistoryAlbumData, input -> {
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

    public LiveData<List<HistoryAudio>> getHistoryMusicData() {
        return mHistoryMusicData;
    }

    public LiveData<Status> getMusicStatus() {
        return mMusicStatus;
    }

    public LiveData<List<HistoryAlbum>> getHistoryAlbumData() {
        return mHistoryMusicDataHidden;
    }

    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_GET_HISTORY_MUSIC,
                ActionType.ACTION_GET_HISTORY_ALBUM,
                ActionType.ACTION_GET_ADD_ITEM_HISTORY_MUSIC,
                ActionType.ACTION_GET_DEL_ITEM_HISTORY_MUSIC,
                ActionType.ACTION_GET_ADD_ITEM_HISTORY_ALBUM,
                ActionType.ACTION_GET_DEL_ITEM_HISTORY_ALBUM
        };
    }

    @Override
    protected void onAction(RxAction action) {


    }

    /**
     * @param src  源
     * @param data 添加的数据
     * @param <T>  添加的类型
     * @return 是否出错
     */
    @SuppressWarnings("unchecked")
    private <T extends Collection> boolean setDataError(MutableLiveData<T> src, Object data) {
        if (data != null) {
            if (data instanceof Collection) {
                if (((Collection) data).size() > 0) {
                    src.setValue((T) data);
                    return false;
                }
            }
        }
        return true;
    }

    private <T extends Collection> boolean removeDataError(MutableLiveData<T> src, List data) {
        if (data == null || src.getValue() == null || !src.getValue().removeAll(data)) {
            return false;
        }
        src.setValue(src.getValue());
        return false;
    }

    @Override
    protected void onData(RxAction action) {
        switch (action.type) {
            //音乐相关
            case ActionType.ACTION_GET_HISTORY_MUSIC:
                Object audios = action.data.get(Constant.HistoryConstant.KEY_HISTORY_MUSIC_AUDIOS);
                mHistoryAudios.clear();
                mHistoryAudios.addAll((List<HistoryAudio>) audios);
                mHistoryMusicData.setValue(mHistoryAudios);
                break;
            case ActionType.ACTION_GET_DEL_ITEM_HISTORY_MUSIC:
                List<HistoryAudio> historyAudios = (List<HistoryAudio>) action.data.get(Constant.HistoryConstant.KEY_HISTORY_MUSICS_DELETE);
                mHistoryAudios.removeAll(historyAudios);
                mHistoryMusicData.setValue(mHistoryAudios);
                break;
            case ActionType.ACTION_GET_ADD_ITEM_HISTORY_MUSIC:
                HistoryAudio audio = (HistoryAudio) action.data.get(Constant.HistoryConstant.KEY_HISTORY_MUSIC_AUDIO);
                mHistoryAudios.remove(audio);
                mHistoryAudios.add(0, audio);
                mHistoryMusicData.setValue(mHistoryAudios);
                break;
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
                Log.d("HistoryStore", "onAction:" + action.type);
                break;

        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {

        switch (action.type) {
            case ActionType.ACTION_GET_HISTORY_MUSIC:
                setStatus(mMusicStatus, throwable);
                break;
            case ActionType.ACTION_GET_DEL_ITEM_HISTORY_MUSIC:
                if (throwable instanceof Error) {
                    mMusicStatus.setValue(Status.HISTORY_DELETE_FAIL);
                }
                break;
            case ActionType.ACTION_GET_HISTORY_ALBUM:
                setStatus(mAlbumStatus, throwable);
                break;
            case ActionType.ACTION_GET_DEL_ITEM_HISTORY_ALBUM:
                if (throwable instanceof Error) {
                    mAlbumStatus.setValue(Status.HISTORY_DELETE_FAIL);
                }
                break;
            default:
                Log.d("HistoryStore", "onError:" + action.type);
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
