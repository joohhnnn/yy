package com.txznet.music.model;

import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.MusicPageData;
import com.txznet.music.data.entity.RadioPageData;
import com.txznet.music.data.entity.RecommendPageData;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.ToastUtils;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 主页业务
 *
 * @author zackzhou
 * @date 2018/12/30,11:41
 */

public class HomePageModel extends RxWorkflow {

    private boolean hasRecTabTip, hasMusicTabTip, hasRadioTabTip;

    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_HOME_GET_RECOMMEND_PAGE_DATA:
                requestRecommend(action);
                break;
            case ActionType.ACTION_HOME_GET_MUSIC_PAGE_DATA:
                requestMusicPage(action);
                break;
            case ActionType.ACTION_HOME_GET_RADIO_PAGE_DATA:
                requestRadioPage(action);
                break;
        }
    }

    private void requestRecommend(RxAction action) {
        String cache = SharedPreferencesUtils.getRecPageCache();
        if (cache != null) {
            action.data.put(Constant.HomeConstant.KEY_PAGE_DATA, JsonHelper.fromJson(cache, RecommendPageData.class));
            postRxData(action);
        }
        if (!hasRecTabTip && !NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
            hasRecTabTip = true;
        }
        Disposable disposable = TXZMusicDataSource.get().getRecommendPageData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(pageData -> {
                    action.data.put(Constant.HomeConstant.KEY_PAGE_DATA, pageData);
                    postRxData(action);
                }, throwable -> {
                    postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG, throwable));
                });
        addRxAction(action, disposable);
    }

    private void requestMusicPage(RxAction action) {
        String cache = SharedPreferencesUtils.getMusicPageCache();
        if (cache != null) {
            action.data.put(Constant.HomeConstant.KEY_PAGE_DATA, JsonHelper.fromJson(cache, MusicPageData.class));
            postRxData(action);
        }
        if (!hasMusicTabTip && !NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
            hasMusicTabTip = true;
        }
        Disposable disposable = TXZMusicDataSource.get().getMusicPageData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(musicPageData -> {
                    action.data.put(Constant.HomeConstant.KEY_PAGE_DATA, musicPageData);
                    postRxData(action);
                }, throwable -> {
                    postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG));
                });
        addRxAction(action, disposable);
    }

    private void requestRadioPage(RxAction action) {
        String cache = SharedPreferencesUtils.getRadioPageCache();
        if (cache != null) {
            action.data.put(Constant.HomeConstant.KEY_PAGE_DATA, JsonHelper.fromJson(cache, RadioPageData.class));
            postRxData(action);
        }
        if (!hasRadioTabTip && !NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
            hasRadioTabTip = true;
        }
        Disposable disposable = TXZMusicDataSource.get().getRadioPageData()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(radioPageData -> {
                    action.data.put(Constant.HomeConstant.KEY_PAGE_DATA, radioPageData);
                    postRxData(action);
                }, throwable -> {
                    postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG));
                });
        addRxAction(action, disposable);
    }
}
