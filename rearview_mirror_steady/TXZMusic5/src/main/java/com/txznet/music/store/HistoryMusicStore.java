package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.txznet.comm.err.Error;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;
import com.txznet.rxflux.extensions.aac.livedata.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;


/**
 * 历史的ViewModule
 *
 * @author telen
 * @date 2018/12/3,10:56
 */
public class HistoryMusicStore extends Store {

    private List<HistoryAudio> mHistoryAudios = new ArrayList<>();
    private MutableLiveData<List<HistoryAudio>> mHistoryMusicData = new MutableLiveData<>();
    private SingleLiveEvent<Status> mMusicStatus = new SingleLiveEvent<>();

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

    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_GET_HISTORY_MUSIC,
                ActionType.ACTION_GET_HISTORY_ALBUM,
                ActionType.ACTION_GET_ADD_ITEM_HISTORY_MUSIC,
                ActionType.ACTION_GET_DEL_ITEM_HISTORY_MUSIC
        };
    }

    @Override
    protected void onAction(RxAction action) {


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
            default:
                Log.d("HistoryMusicStore", "onAction:" + action.type);
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
            default:
                Log.d("HistoryMusicStore", "onError:" + action.type);
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
