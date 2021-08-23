package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;
import com.txznet.rxflux.extensions.aac.livedata.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 收藏Store
 */
public class FavourStore extends Store {

    List<FavourAudio> mAllFavours = new ArrayList<>();
    //    MutableLiveData<FavourAudio> mFavourAudio = new MutableLiveData<>();
    MutableLiveData<List<FavourAudio>> mFavourAudios = new MutableLiveData<>();
//    MutableLiveData<FavourAudio> mUnFavourAudio = new MutableLiveData<>();

    SingleLiveEvent<Status> mErrorStatus = new SingleLiveEvent<>();

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
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_FAVOUR_EVENT_FAVOUR,
                ActionType.ACTION_FAVOUR_EVENT_UNFAVOUR,
                ActionType.ACTION_FAVOUR_EVENT_GET
        };
    }

    @Override
    protected void onAction(RxAction action) {
//        switch (action.type) {
//            case ActionType.ACTION_FAVOUR_EVENT_FAVOUR:
//                mFavourAudio.setValue((FavourAudio) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO));
//                break;
//            case ActionType.ACTION_FAVOUR_EVENT_UNFAVOUR:
//                mUnFavourAudio.setValue((FavourAudio) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO));
//                break;
//            default:
//                break;
//        }
    }

    @Override
    protected void onData(RxAction action) {
//通知
        switch (action.type) {
            case ActionType.ACTION_FAVOUR_EVENT_GET:
                mAllFavours.clear();
                mAllFavours.addAll((List<FavourAudio>) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIOS));
                mFavourAudios.setValue(mAllFavours);
                break;
            case ActionType.ACTION_FAVOUR_EVENT_FAVOUR:
                FavourAudio favourAudio = (FavourAudio) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO);
                mAllFavours.remove(favourAudio);
                mAllFavours.add(0, favourAudio);
                mFavourAudios.setValue(mAllFavours);
                break;
            case ActionType.ACTION_FAVOUR_EVENT_UNFAVOUR:

                FavourAudio favourAudio1 = (FavourAudio) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO);
                mAllFavours.remove(favourAudio1);
                mFavourAudios.setValue(mAllFavours);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {
        switch (action.type) {
            case ActionType.ACTION_FAVOUR_EVENT_GET:
                mErrorStatus.setValue(Status.ERROR);
                break;
            default:
                break;
        }
    }

//    public LiveData<FavourAudio> getFavourAudio() {
//        return mFavourAudio;
//    }


    private LiveData<List<FavourAudio>> mFavourAudiosSorted = Transformations.map(mFavourAudios, input -> {
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

    public LiveData<List<FavourAudio>> getFavourAudios() {
        return mFavourAudiosSorted;
    }

//    public LiveData<FavourAudio> getUnFavourAudio() {
//        return mUnFavourAudio;
//    }

    public LiveData<Status> getErrorStatus() {
        return mErrorStatus;
    }
}
