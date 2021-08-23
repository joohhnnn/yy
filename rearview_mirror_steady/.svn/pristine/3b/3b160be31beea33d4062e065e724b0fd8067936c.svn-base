package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.MusicPageData;
import com.txznet.music.data.entity.RadioPageData;
import com.txznet.music.data.entity.RecommendPageData;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;
import com.txznet.rxflux.extensions.aac.livedata.SingleLiveEvent;

/**
 * @author zackzhou
 * @date 2018/12/30,11:56
 */

public class HomePageStore extends Store {
    private MutableLiveData<RecommendPageData> mRecPage = new MutableLiveData<>();
    private MutableLiveData<MusicPageData> mMusicPage = new MutableLiveData<>();
    private MutableLiveData<RadioPageData> mRadioPage = new MutableLiveData<>();
    private SingleLiveEvent<Status> mStatus = new SingleLiveEvent<>();

    /**
     *
     */
    public enum Status {
        /**
         * 没有找到数据
         */
        LOAD_FAILED
    }

    /**
     * 获取推荐页
     */
    public LiveData<RecommendPageData> getRecPageData() {
        return mRecPage;
    }

    /**
     * 获取音乐
     */
    public LiveData<MusicPageData> getMusicPageData() {
        return mMusicPage;
    }

    /**
     * 获取电台
     */
    public LiveData<RadioPageData> getRadioPageData() {
        return mRadioPage;
    }

    public LiveData<Status> getStatus() {
        return mStatus;
    }

    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_HOME_GET_RECOMMEND_PAGE_DATA,
                ActionType.ACTION_HOME_GET_MUSIC_PAGE_DATA,
                ActionType.ACTION_HOME_GET_RADIO_PAGE_DATA
        };
    }

    @Override
    protected void onAction(RxAction action) {

    }

    @Override
    protected void onData(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_HOME_GET_RECOMMEND_PAGE_DATA:
                mRecPage.setValue((RecommendPageData) action.data.get(Constant.HomeConstant.KEY_PAGE_DATA));
                break;
            case ActionType.ACTION_HOME_GET_MUSIC_PAGE_DATA:
                mMusicPage.setValue((MusicPageData) action.data.get(Constant.HomeConstant.KEY_PAGE_DATA));
                break;
            case ActionType.ACTION_HOME_GET_RADIO_PAGE_DATA:
                mRadioPage.setValue((RadioPageData) action.data.get(Constant.HomeConstant.KEY_PAGE_DATA));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {
        mStatus.setValue(Status.LOAD_FAILED);
    }
}
