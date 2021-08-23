package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.SubscribeAlbum;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;
import com.txznet.rxflux.extensions.aac.livedata.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 订阅Store
 *
 * @author telen
 * @date 2018/12/5,14:23
 */
public class SubscribeStore extends Store {

    private List<SubscribeAlbum> mAlbums = new ArrayList<>();

    private MutableLiveData<List<SubscribeAlbum>> mSubscribeAlbums = new MutableLiveData<>();


    private SingleLiveEvent<Status> mErrorStatus = new SingleLiveEvent<>();

    /**
     * 错误状态
     */
    public enum Status {
        /**
         * 无网
         */
        NO_NETWORK,
        /**
         * 内部错误
         */
        ERROR
    }

    @Override
    protected void onAction(RxAction action) {
    }

    @Override
    protected void onData(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_SUBSCRIBE_EVENT_GET:
                Collection collection = (Collection) action.data.get(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUMS);
                mAlbums.clear();
                if (collection != null && collection.size() > 0) {
                    mAlbums.addAll(collection);
                }
                mSubscribeAlbums.setValue(mAlbums);

                break;
            case ActionType.ACTION_SUBSCRIBE_EVENT_SUBSCRIBE:
                //执行订阅的逻辑
//                mAlbums.clear();//因为订阅的时候会导致数据都被清空
                SubscribeAlbum e = (SubscribeAlbum) action.data.get(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM);
                mAlbums.remove(e);
                mAlbums.add(0, e);
                mSubscribeAlbums.setValue(mAlbums);
                break;
            case ActionType.ACTION_SUBSCRIBE_EVENT_UNSUBSCRIBE:
                //执行取消订阅的逻辑
                mAlbums.remove(action.data.get(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM));
                mSubscribeAlbums.setValue(mAlbums);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {
        switch (action.type) {
            case ActionType.ACTION_SUBSCRIBE_EVENT_GET:
                mErrorStatus.setValue(Status.ERROR);
                break;
            default:
                break;
        }
    }

    private LiveData<List<SubscribeAlbum>> mSubscribeAlbumsSorted = Transformations.map(mSubscribeAlbums, input -> {
        if (input != null) {
            Collections.sort(input, (o1, o2) -> {
                if (o1.timestamp == o2.timestamp) {
                    return 0;
                }
                return o1.timestamp > o2.timestamp ? -1 : 1;
            });
        }
        return input;
    });

    public LiveData<List<SubscribeAlbum>> getSubscribeAlbums() {
        return mSubscribeAlbumsSorted;
    }

    public LiveData<Status> getErrorStatus() {
        return mErrorStatus;
    }

    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_SUBSCRIBE_EVENT_SUBSCRIBE,
                ActionType.ACTION_SUBSCRIBE_EVENT_GET,
                ActionType.ACTION_SUBSCRIBE_EVENT_UNSUBSCRIBE
        };
    }
}
