package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.txznet.comm.err.Error;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.SearchResult;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;
import com.txznet.rxflux.extensions.aac.livedata.SingleLiveEvent;

public class SearchStore extends Store {

    public enum State {
        NO_NETWORK,
        EMPTY_DATA,
        ERROR,//统一播报网络超时,稍后重试
    }


    //    private MutableLiveData<SearchResult> mResult = new MutableLiveData<>(); // 搜索结果
    private MutableLiveData<State> mErrorState = new SingleLiveEvent<>();// 搜索错误
    private MutableLiveData<Integer> mChoiceIndex = new SingleLiveEvent<>();// 搜索错误


    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_GET_SEARCH_RESULT
        };
    }

    @Override
    protected void onAction(RxAction action) {
    }

    @Override
    protected void onData(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_GET_SEARCH_RESULT:
                if (action.data.get("value") instanceof SearchResult) {
                    SearchResult result = (SearchResult) action.data.get("value");
                    mChoiceIndex.setValue(result.playIndex);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {
        switch (action.type) {
            case ActionType.ACTION_GET_SEARCH_RESULT:

                if (throwable instanceof Error) {
                    int errorCode = ((Error) throwable).errorCode;
                    Logger.d(Constant.LOG_TAG_SEARCH, "onError get errorCode:" + errorCode);

                    if (errorCode == ErrCode.ERROR_CLIENT_NET_EMPTY_DATA) {
                        mErrorState.setValue(State.EMPTY_DATA);//没有数据
                    } else if (errorCode == ErrCode.ERROR_CLIENT_NET_OFFLINE) {
                        mErrorState.setValue(State.NO_NETWORK);//网络错误
                    } else {
                        mErrorState.setValue(State.ERROR);
                    }
                }

                break;
            default:
                break;
        }
    }

    public LiveData<Integer> getChoiceIndex() {
        return mChoiceIndex;
    }

    public LiveData<State> getError() {
        return mErrorState;
    }

}
