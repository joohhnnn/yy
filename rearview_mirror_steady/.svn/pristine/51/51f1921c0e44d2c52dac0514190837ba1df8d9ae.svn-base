package com.txznet.music.service.impl;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.support.annotation.NonNull;
import android.util.Log;

import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.action.SearchActionCreator;
import com.txznet.music.data.entity.SearchResult;
import com.txznet.music.store.SearchStore;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.Operation;

/**
 * 搜索模块
 */
public class SearchCommand extends BaseCommand implements LifecycleOwner {
    private static final String TAG = Constant.LOG_TAG_SEARCH;

    private SearchResult mSearchResult; // 搜索结果
    private SearchStore mSearchStore = new SearchStore();
    private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);


    public SearchCommand() {
        //Core选择的结果回来
        addCmd("playmusiclist.index", (pkgName, cmd, data) -> {
            if (isChoiceHistory(data)) {//如果是历史记录的选择
                // TODO: 2018/11/21 对于声控查看历史的选择
                Logger.d(Constant.LOG_TAG_SEARCH, "SearchCommand:" + "choiceHistory");
            } else if (isChoiceSearchResult(data)) {// 如果是搜索结果的选择
                choiceIndex(Operation.SOUND, getChoiceIndex(data));
            } else {
                Logger.d(Constant.LOG_TAG_SEARCH, "SearchCommand:choiceElse");
            }
            //如果是其他的选择
            return new byte[0];
        });
        //发起声控搜索
        addCmd("sound.find", (pkgName, cmd, data) -> {
            search(new String(data), false);
            return new byte[0];
        });
        //发起声控搜索
        addCmd("sound.find.v2", (pkgName, cmd, data) -> {
            search(new String(data), true);
            return new byte[0];
        });
        //取消
        addCmd("sound.cancelfind", (pkgName, cmd, data) -> {
            cancel();
            return new byte[0];
        });

        mLifecycleRegistry.markState(Lifecycle.State.INITIALIZED);
    }

    /**
     * 根据关键字搜索
     * <p>
     * 1. 播报正在为你搜索.. -> 播完
     * -> 结果回来，执行播报
     * -> 结果未回来，等结果回来的时候在播放
     *
     * @param keyword    搜索关键字
     * @param justInvoke 是否只执行，同听4.x需要自己处理播报交互，同听5.+把交互交给core
     */
    private void search(String keyword, boolean justInvoke) {
        initSearchResultObs();
        SearchActionCreator.getInstance().getSearchData(Operation.SOUND, keyword, justInvoke);
    }

    // 初始化搜索结果监听
    private void initSearchResultObs() {
        mLifecycleRegistry.markState(Lifecycle.State.CREATED);
        mLifecycleRegistry.markState(Lifecycle.State.STARTED);
        mLifecycleRegistry.markState(Lifecycle.State.RESUMED);
        mSearchStore.getChoiceIndex().observe(this, choiceIndex -> {
            if (choiceIndex != null) {
                Log.d(TAG, "search: " + "choiceIndex:auto::" + choiceIndex);
                choiceIndex(Operation.AUTO, choiceIndex);
            }
        });

        mSearchStore.getError().observe(this, state -> {
            if (state == null) {
                return;
            }
            invokeErrorReport(state);
        });
    }

    private void invokeErrorReport(SearchStore.State state) {
        switch (state) {
            case ERROR:
                speakTTSSearchException();
                break;
            case EMPTY_DATA:
                speakTTSNotFound();
                break;
            case NO_NETWORK:
                sepakTTSNotNet();
                break;
            default:
                break;
        }
    }


    // 取消搜索
    private void cancel() {
//        if (mSearchStore.isRegistered()) {
//            mSearchStore.unRegister();
//        }
        AppLogic.runOnUiGround(() -> {
            //java.lang.IllegalStateException: Cannot invoke removeObserver on a background thread
            mLifecycleRegistry.markState(Lifecycle.State.DESTROYED);
        }, 1000);
        SearchActionCreator.getInstance().cancelSearch(Operation.SDK);
    }

    // 声控选择
    private void choiceIndex(Operation operation, int choiceIndex) {
        SearchActionCreator.getInstance().choiceSearchData(operation, choiceIndex);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    private int getChoiceIndex(byte[] data) {
        int index = -1;
        try {
            // 新版本传的是json
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            index = jsonBuilder.getVal("index", Integer.class, -1);
        } catch (Exception e) {
            Log.i(TAG, "getChoiceIndex: " + e.getMessage());
        }
        if (index == -1) {
            index = Integer.parseInt(new String(data));
        }
        return index;
    }

    private boolean isChoiceSearchResult(byte[] data) {
        return getChoiceIndex(data) != -1;
    }

    private boolean isChoiceHistory(byte[] data) {
        boolean viewDetail = false;
        try {
            // 新版本传的是json
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            viewDetail = jsonBuilder.getVal("showDetail", Boolean.class, false);
        } catch (Exception e) {
            Log.i(TAG, "isChoiceHistory: " + e.getMessage());
        }

        return viewDetail;
    }


    private void speakTTSSearchException() {
        String tips = Constant.RS_VOICE_SPEAK_SEARCH_EXCEPTION;
        TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_SEARCH_EXCEPTION", tips, false, null);
    }

    private void speakTTSNotFound() {
        Logger.d(TAG, "speak not found");
        String tips = Constant.RS_VOICE_SPEAK_NODATAFOUND_TIPS;
        MonitorUtil.monitorCumulant(Constant.M_EMPTY_SOUND);
        TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NODATAFOUND_TIPS",
                tips, false, null);
    }

    private void sepakTTSNotNet() {
        String tips = Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS;
        TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NETNOTCON_TIPS", tips, false, null);
    }

}
