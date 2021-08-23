package com.txznet.txz.component.media.remote;

import android.text.TextUtils;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.media.constant.InvokeConstants;
import com.txznet.sdk.media.constant.PlayerLoopMode;
import com.txznet.sdk.media.constant.PlayerStatus;
import com.txznet.txz.component.media.MediaPriorityManager;
import com.txznet.txz.component.media.base.AbsAudioTool;
import com.txznet.txz.component.media.base.IRemoteMediaTool;
import com.txznet.txz.component.media.base.MediaToolConstants;
import com.txznet.txz.component.media.model.MediaModel;
import com.txznet.txz.component.media.util.RemoteMediaSearchPresenter;
import com.txznet.txz.component.media.util.RemoteMediaToolInvoker;
import com.txznet.txz.jni.JNIHelper;

import org.json.JSONObject;

/**
 * 远程电台工具
 * Created by J on 2018/5/2.
 */

public class RemoteAudioTool extends AbsAudioTool implements IRemoteMediaTool {
    private static final String LOG_TAG = "RemoteAudioTool";
    private String mRemotePackageName;
    private int mSdkVersion;
    // 是否拦截声控反馈语
    private boolean bInterceptTts;
    // 是否显示搜索结果列表
    private boolean bShowSearchResult;

    // 当前播放的节目
    private MediaModel mRemotePlayingModel = null;

    // 电台搜索展示处理工具
    private RemoteMediaSearchPresenter mSearchPresenter = new RemoteMediaSearchPresenter(this);

    @Override
    public boolean isEnabled() {
        return !TextUtils.isEmpty(getPackageName());
    }

    @Override
    public String getInvokePrefix() {
        return InvokeConstants.INVOKE_PREFIX_AUDIO;
    }

    @Override
    public String getInvokePackegeName() {
        return mRemotePackageName;
    }

    public void setPackageName(String packageName, JSONBuilder paramBuilder) {
        if (null == paramBuilder) {
            log("setPackageName: param is null");
            return;
        }

        int sdkVersion = paramBuilder.getVal(InvokeConstants.PARAM_SDK_VERSION, int.class, 0);
        if (0 == sdkVersion) {
            // TODO: 2018/8/6 旧版本远程电台工具处理
            return;
        }

        log(String.format("setPackageName: %s, version = %s", packageName, sdkVersion));
        mRemotePackageName = packageName;
        mSdkVersion = sdkVersion;

        setShowSearchResult(paramBuilder.getVal(InvokeConstants.PARAM_SHOW_SEARCH_RESULT,
                boolean.class, false));
        setSearchTimeout(paramBuilder
                .getVal(InvokeConstants.PARAM_SEARCH_MEDIA_TIMEOUT, int.class, 10000));
    }

    public void clearPackageName() {
        mRemotePackageName = "";
        MediaPriorityManager.getInstance().notifyRemoteAudioToolCleared();
    }

    @Override
    public void setSearchTimeout(final long timeout) {
        mSearchPresenter.setSearchTimeout(timeout);
    }

    @Override
    public void setShowSearchResult(final boolean show) {
        bShowSearchResult = show;
    }

    public void setInterceptTts(boolean intercept) {
        bInterceptTts = intercept;
    }

    public byte[] onAudioSdkInvoke(String packageName, String cmd, byte[] data) {
        if (TextUtils.isEmpty(mRemotePackageName) || !mRemotePackageName.equals(packageName)) {
            JNIHelper.loge(String.format("RemoteAudioTool::onAudioSdkInvoke: packageName " +
                    "mismatch: %s | %s", mRemotePackageName, packageName));
            return null;
        }

        byte[] ret = null;
        JSONBuilder param = new JSONBuilder(data);
        if (InvokeConstants.CMD_NOTIFY_PLAYER_STATUS.equals(cmd)) {
            String status = param.getVal("status", String.class);
            notifyPlayerStatusChange(PLAYER_STATUS.fromPlayerStatusStr(status));
        } else if (InvokeConstants.CMD_NOTIFY_PLAYING_MODEL.equals(cmd)) {
            JSONObject jsonModel =param.getVal("model", JSONObject.class);
            this.mRemotePlayingModel = MediaModel.fromJsonObject(jsonModel);
        } else {
            mSearchPresenter.procRemoteInvoke(cmd, param);
        }

        return ret;
    }

    @Override
    public void cancelRequest() {
        mSearchPresenter.cancelSearch();
    }

    @Override
    public String getPackageName() {
        return mRemotePackageName;
    }

    @Override
    public int getPriority() {
        return MediaToolConstants.PRIORITY_REMOTE_TOOL;
    }

    @Override
    public void open(boolean play) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("play", play);
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_OPEN, builder.toBytes());
    }

    @Override
    public void exit() {
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_EXIT);
    }

    @Override
    public void play(MediaModel model) {
        if (bShowSearchResult) {
            mSearchPresenter.playMusic(model, true);
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_PLAY,
                new JSONBuilder(model.toTXZMediaModel().toJsonObject()).toBytes());
    }

    @Override
    public void stop() {
        pause();
    }

    @Override
    public void pause() {
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_PAUSE);
    }

    @Override
    public void continuePlay() {
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_CONTINUE_PLAY);
    }

    @Override
    public void next() {
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_NEXT);
    }

    @Override
    public void prev() {
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_PREV);
    }

    @Override
    public void switchLoopMode(LOOP_MODE mode) {
        String modeStr = "";
        switch (mode) {
            case SEQUENTIAL:
                modeStr = PlayerLoopMode.SEQUENTIAL.toModeStr();
                break;

            case LIST_LOOP:
                modeStr = PlayerLoopMode.LIST_LOOP.toModeStr();
                break;

            case SINGLE_LOOP:
                modeStr = PlayerLoopMode.SINGLE_LOOP.toModeStr();
                break;

            case SHUFFLE:
                modeStr = PlayerLoopMode.SHUFFLE.toModeStr();
                break;
        }

        JSONBuilder builder = new JSONBuilder();
        builder.put("mode", modeStr);
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_SWITCH_LOOP_MODE, builder.toBytes());
    }

    @Override
    public void collect() {
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_COLLECT);
    }

    @Override
    public void unCollect() {
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_UNCOLLECT);
    }

    @Override
    public void playCollection() {
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_PLAY_COLLECTION);
    }

    @Override
    public void subscribe() {
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_SUBSCRIBE);
    }

    @Override
    public void unSubscribe() {
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_UNSUBSCRIBE);
    }

    @Override
    public void playSubscribe() {
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_PLAY_SUBSCRIBE);
    }

    @Override
    public PLAYER_STATUS getStatus() {
        if (!isEnabled()) {
            return PLAYER_STATUS.IDLE;
        }

        PLAYER_STATUS status = PLAYER_STATUS.IDLE;
        String playerStatusStr = RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_GET_PLAYER_STATUS, null, "");
        if (PlayerStatus.BUFFERING.toStatusString().equals(playerStatusStr)) {
            status = PLAYER_STATUS.BUFFERING;
        } else if (PlayerStatus.PLAYING.toStatusString().equals(playerStatusStr)) {
            status = PLAYER_STATUS.PLAYING;
        } else if (PlayerStatus.PAUSED.toStatusString().equals(playerStatusStr)) {
            status = PLAYER_STATUS.PAUSED;
        } else if (PlayerStatus.STOPPED.toStatusString().equals(playerStatusStr)) {
            status = PLAYER_STATUS.STOPPED;
        }

        return status;
    }

    @Override
    public MediaModel getPlayingModel() {
        return mRemotePlayingModel;
    }

    @Override
    public boolean supportLoopMode(LOOP_MODE mode) {
        String loopModeStr = "";
        switch (mode) {
            case SEQUENTIAL:
                loopModeStr = PlayerLoopMode.SEQUENTIAL.toModeStr();
                break;

            case LIST_LOOP:
                loopModeStr = PlayerLoopMode.LIST_LOOP.toModeStr();
                break;

            case SINGLE_LOOP:
                loopModeStr = PlayerLoopMode.SINGLE_LOOP.toModeStr();
                break;

            case SHUFFLE:
                loopModeStr = PlayerLoopMode.SHUFFLE.toModeStr();
                break;
        }

        JSONBuilder builder = new JSONBuilder();
        builder.put("mode", loopModeStr);
        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_LOOP_MODE, builder.toBytes(), false);
    }

    @Override
    public boolean supportCollect() {
        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_COLLECT, false);
    }

    @Override
    public boolean supportUnCollect() {
        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_UNCOLLECT, false);
    }

    @Override
    public boolean supportPlayCollection() {
        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_PLAY_COLLECTION, false);
    }

    @Override
    public boolean supportSubscribe() {
        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_SUBSCRIBE, false);
    }

    @Override
    public boolean supportUnSubscribe() {
        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_UNSUBSCRIBE, false);
    }

    @Override
    public boolean supportPlaySubscribe() {
        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_PLAY_SUBSCRIBE, false);
    }

    @Override
    public boolean supportSearch() {
        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_SEARCH, false);
    }

    @Override
    public boolean hasNext() {
        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_HAS_NEXT, true);
    }

    @Override
    public boolean hasPrev() {
        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_HAS_PREV, true);
    }

    @Override
    public boolean interceptRecordWinControl(final MEDIA_TOOL_OP op) {
        if (MEDIA_TOOL_OP.PLAY == op && bShowSearchResult) {
            return true;
        }

        return false;
    }

    //----------- single instance -----------
    private static volatile RemoteAudioTool sInstance;

    public static RemoteAudioTool getInstance() {
        if (null == sInstance) {
            synchronized (RemoteAudioTool.class) {
                if (null == sInstance) {
                    sInstance = new RemoteAudioTool();
                }
            }
        }

        return sInstance;
    }

    private RemoteAudioTool() {

    }
    //----------- single instance -----------

    // loggger
    private void log(String msg) {
        JNIHelper.logd(LOG_TAG + "::" + msg);
    }
}
