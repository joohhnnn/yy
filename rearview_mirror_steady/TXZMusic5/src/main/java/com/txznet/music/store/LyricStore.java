package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.txznet.comm.err.Error;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;
import com.txznet.rxflux.extensions.aac.livedata.SingleLiveEvent;

import java.io.File;

/**
 * @author telen
 * @date 2018/12/13,15:28
 */
public class LyricStore extends Store {

    MutableLiveData<File> mLyricData = new MutableLiveData<>();
    SingleLiveEvent<Status> mStatus = new SingleLiveEvent<>();

    /**
     *
     */
    public enum Status {
        /**
         * 没有找到数据
         */
        LYRIC_EMPTY,
        /**
         * 没有网络
         */
        LYRIC_NO_NET,
        /**
         * 内部错误
         */
        LYRIC_ERROR
    }

    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_LYRIC_GET
        };
    }

    @Override
    protected void onAction(RxAction action) {

    }

    @Override
    protected void onData(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_LYRIC_GET:
                mLyricData.postValue((File) action.data.get(Constant.LyricConstant.KEY_LYRIC_FILE));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {
        switch (action.type) {
            case ActionType.ACTION_LYRIC_GET:
                if (throwable instanceof Error) {
                    if (((Error) throwable).errorCode == ErrCode.ERROR_CLIENT_NET_EMPTY_DATA) {
                        mStatus.setValue(Status.LYRIC_EMPTY);
                        return;
                    } else if (((Error) throwable).errorCode == ErrCode.ERROR_CLIENT_NET_OFFLINE) {
                        mStatus.setValue(Status.LYRIC_NO_NET);
                        return;
                    }
                }
                mStatus.setValue(Status.LYRIC_ERROR);
                break;
            default:
                break;
        }
    }

    public LiveData<File> getLyricData() {
        return mLyricData;
    }

    public LiveData<Status> getStatus() {
        return mStatus;
    }
}
