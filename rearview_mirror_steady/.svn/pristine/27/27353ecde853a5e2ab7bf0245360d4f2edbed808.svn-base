package com.txznet.txz.component.media.remote;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.media.constant.InvokeConstants;
import com.txznet.sdk.media.constant.PlayerLoopMode;
import com.txznet.sdk.media.constant.PlayerStatus;
import com.txznet.txz.component.media.MediaPriorityManager;
import com.txznet.txz.component.media.base.AbsMusicTool;
import com.txznet.txz.component.media.base.IRemoteMediaTool;
import com.txznet.txz.component.media.base.MediaToolConstants;
import com.txznet.txz.component.media.model.MediaModel;
import com.txznet.txz.component.media.util.RemoteMediaSearchPresenter;
import com.txznet.txz.component.media.util.RemoteMediaToolInvoker;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.music.util.StringInfoUtils;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONObject;

/**
 * 远程音乐工具
 *
 * 提供与sdk设置的MusicTool的交互逻辑
 * 1. 兼容旧版本TXZMusicManager.MusicTool相关的远程调用处理
 * 2. 兼容通过旧版本sdk设置的MusicTool
 * 3. 兼容旧版本sdk的相关调用命令处理
 *
 * Created by J on 2018/5/2.
 */

public class RemoteMusicTool extends AbsMusicTool implements IRemoteMediaTool {
    private static final String LOG_TAG = "RemoteMusicTool";
    private String mRemotePackageName;
    private int mSDKVersion;
    // 是否拦截声控反馈语
    private boolean bInterceptTts;
    // 是否显示搜索结果列表
    private boolean bShowSearchResult;

    /*
    * 旧版本sdk远程音乐工具只会通知播放状态发生变化一个事件，需要再通过远程调用查询
    * 具体的播放状态， 因此设置此变量对获取到的播放状态进行记录，避免getStatus时再
    * 进行重复的远程调用
    * */
    private PLAYER_STATUS mRemotePlayerStatusCache = PLAYER_STATUS.IDLE;

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
        return InvokeConstants.INVOKE_PREFIX_MUSIC;
    }

    @Override
    public String getInvokePackegeName() {
        return mRemotePackageName;
    }

    public void setPackageName(String packageName, int version, JSONBuilder paramBuilder) {
        mRemotePackageName = packageName;
        mSDKVersion = version;

        if (version >= 3) {
            setShowSearchResult(paramBuilder.getVal(InvokeConstants.PARAM_SHOW_SEARCH_RESULT,
                    boolean.class, false));
            setSearchTimeout(paramBuilder.getVal(InvokeConstants.PARAM_SEARCH_MEDIA_TIMEOUT,
                    int.class, 10000));
        }
    }

    public void clearPackageName() {
        mRemotePackageName = "";

        MediaPriorityManager.getInstance().notifyRemoteMusicToolCleared();
    }

    @Override
    public void setSearchTimeout(final long timeout) {
        mSearchPresenter.setSearchTimeout(timeout);
    }

    @Override
    public void setShowSearchResult(final boolean show) {
        bShowSearchResult = show;
    }

    public byte[] onMusicSdkInvoke(String packageName, String cmd, byte[] data) {
        if (TextUtils.isEmpty(mRemotePackageName) || !mRemotePackageName.equals(packageName)) {
            JNIHelper.loge(String.format("RemoteMusicTool::onMusicSdkInvoke: packageName " +
                    "mismatch: %s | %s", mRemotePackageName, packageName));
            return null;
        }

        byte[] ret = null;
        JSONBuilder param = new JSONBuilder(data);
        if (InvokeConstants.CMD_NOTIFY_PLAYER_STATUS.equals(cmd)) {
            String status = param.getVal("status", String.class);
            notifyPlayerStatusChange(PLAYER_STATUS.fromPlayerStatusStr(status));
        } else if (InvokeConstants.CMD_NOTIFY_PLAYING_MODEL.equals(cmd)) {
            JSONObject jsonModel = param.getVal("model", JSONObject.class);
            this.mRemotePlayingModel = MediaModel.fromJsonObject(jsonModel);
        } else {
            mSearchPresenter.procRemoteInvoke(cmd, param);
        }

        return ret;
    }

    public void updateRemotePlayerStatus(PLAYER_STATUS status) {
        this.mRemotePlayerStatusCache = status;
        notifyPlayerStatusChange(status);
    }

    public void updateRemotePlayingModel(MediaModel model) {
        this.mRemotePlayingModel = model;
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
        if (isOldVersionSdk()) {
            JSONBuilder jsonBuilder = new JSONBuilder();
            jsonBuilder.put("open", true);
            ServiceManager.getInstance().sendInvoke(mRemotePackageName, "tool.music.play",
                    jsonBuilder.toBytes(), null);
            return;
        }

        JSONBuilder builder = new JSONBuilder();
        builder.put("play", play);
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_OPEN, builder.toBytes());
    }

    @Override
    public void exit() {
        if (isOldVersionSdk()) {
            ServiceManager.getInstance().sendInvoke(mRemotePackageName, "tool.music.exit", null,
                    null);
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_EXIT);
    }

    @Override
    public void play(MediaModel model) {
        if (isOldVersionSdk()) {
            playMusic(model.toMusicModel());
            return;
        }

        if (bShowSearchResult) {
            mSearchPresenter.playMusic(model, true);
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_PLAY,
                new JSONBuilder(model.toTXZMediaModel().toJsonObject()).toBytes());
    }

    public void playMusic(TXZMusicManager.MusicModel musicModel) {
        String[] kws = musicModel.getKeywords();
        String kw = "";
        String title = musicModel.getTitle();

        if (!TextUtils.isEmpty(title)) {
            kw = title;
        }
        if (kws != null && kws.length > 0) {
            for (String k : kws) {
                if (TextUtils.isEmpty(k))
                    continue;
                if (!kw.isEmpty()) {
                    kw += "";
                }
                kw += k;
            }
        }
        String title1 = StringInfoUtils.genMediaModelTitle(musicModel.getTitle(),
                musicModel.getAlbum(), musicModel.getArtist(), kw, "歌曲");

        if (bInterceptTts) {
            if (RecorderWin.isOpened()) {
                RecorderWin.close();
            }

            ServiceManager.getInstance().sendInvoke(mRemotePackageName, "tool.music.playMusic",
                    musicModel.toString().getBytes(), null);
        } else {
            String spk = NativeData.getResPlaceholderString("RS_MUSIC_WILL_PLAY", "%MUSIC%",
                    title1);
            RecorderWin.speakTextWithClose(spk,
                    new Runnable1<byte[]>(musicModel.toString().getBytes()) {
                        @Override
                        public void run() {
                            ServiceManager.getInstance().sendInvoke(mRemotePackageName,
                                    "tool.music.playMusic", mP1, null);
                        }
                    });
        }
    }

    @Override
    public void stop() {
        pause();
    }

    @Override
    public void pause() {
        if (isOldVersionSdk()) {
            ServiceManager.getInstance().sendInvoke(mRemotePackageName, "tool.music.pause", null,
                    null);
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_PAUSE);
    }

    @Override
    public void continuePlay() {
        if (isOldVersionSdk()) {
            JSONBuilder jsonBuilder = new JSONBuilder();
            jsonBuilder.put("open", false);
            ServiceManager.getInstance().sendInvoke(mRemotePackageName, "tool.music.play",
                    jsonBuilder.toBytes(), null);
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_CONTINUE_PLAY);
    }

    @Override
    public void next() {
        if (isOldVersionSdk()) {
            ServiceManager.getInstance().sendInvoke(mRemotePackageName, "tool.music.next", null,
                    null);
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_NEXT);
    }

    @Override
    public void prev() {
        if (isOldVersionSdk()) {
            ServiceManager.getInstance().sendInvoke(mRemotePackageName, "tool.music.prev", null,
                    null);
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_PREV);
    }

    @Override
    public void switchLoopMode(LOOP_MODE mode) {
        if (isOldVersionSdk()) {
            switch (mode) {
                case SEQUENTIAL:
                case LIST_LOOP:
                    ServiceManager.getInstance().sendInvoke(mRemotePackageName,
                            "tool.music.switchModeLoopAll", null, null);
                    break;

                case SINGLE_LOOP:
                    ServiceManager.getInstance().sendInvoke(mRemotePackageName,
                            "tool.music.switchModeLoopOne", null, null);
                    break;

                case SHUFFLE:
                    ServiceManager.getInstance().sendInvoke(mRemotePackageName,
                            "tool.music.switchModeRandom", null, null);
                    break;
            }
            return;
        }

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
        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_SWITCH_LOOP_MODE,
                builder.toBytes());
    }

    @Override
    public void collect() {
        if (isOldVersionSdk()) {
            ServiceManager.getInstance().sendInvoke(mRemotePackageName,
                    "tool.music.favourMusic", null, null);
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_COLLECT);
    }

    @Override
    public void unCollect() {
        if (isOldVersionSdk()) {
            ServiceManager.getInstance().sendInvoke(mRemotePackageName,
                    "tool.music.unfavourMusic", null, null);
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_UNCOLLECT);
    }

    @Override
    public void playCollection() {
        if (isOldVersionSdk()) {
            ServiceManager.getInstance().sendInvoke(mRemotePackageName,
                    "tool.music.playFavourMusic", null, null);
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_PLAY_COLLECTION);
    }

    @Override
    public void subscribe() {
        if (isOldVersionSdk()) {
            //MusicRemoteImpl.getInstance().addSubscribe();
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_SUBSCRIBE);
    }

    @Override
    public void unSubscribe() {
        if (isOldVersionSdk()) {
            //MusicRemoteImpl.getInstance().unSubscribe();
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_UNSUBSCRIBE);
    }

    @Override
    public void playSubscribe() {
        if (isOldVersionSdk()) {
            ServiceManager.getInstance().sendInvoke(mRemotePackageName,
                    "tool.music.playFavourMusic", null, null);
            return;
        }

        RemoteMediaToolInvoker.sendInvoke(this, InvokeConstants.INVOKE_PLAY_SUBSCRIBE);
    }

    @Override
    public PLAYER_STATUS getStatus() {
        if (!isEnabled()) {
            return PLAYER_STATUS.IDLE;
        }

        if (isOldVersionSdk()) {
            // 旧版本sdk采用旧版协议
            return mRemotePlayerStatusCache;
        }

        PLAYER_STATUS status = PLAYER_STATUS.IDLE;
        String playerStatusStr = RemoteMediaToolInvoker.sendInvokeSync(this,
                InvokeConstants.INVOKE_GET_PLAYER_STATUS, null, "");
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
        // 旧版本sdk默认按全部支持处理
        if (isOldVersionSdk()) {
            return true;
        }

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
        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_LOOP_MODE,
                builder.toBytes(), false);
    }

    @Override
    public boolean supportCollect() {
        if (isOldVersionSdk()) {
            return true;
        }

        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_COLLECT,
                false);
    }

    @Override
    public boolean supportUnCollect() {
        if (isOldVersionSdk()) {
            return true;
        }

        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_UNCOLLECT,
                false);
    }

    @Override
    public boolean supportPlayCollection() {
        if (isOldVersionSdk()) {
            return true;
        }

        return RemoteMediaToolInvoker.sendInvokeSync(this,
                InvokeConstants.INVOKE_SUPPORT_PLAY_COLLECTION, false);
    }

    @Override
    public boolean supportSubscribe() {
        if (isOldVersionSdk()) {
            return false;
        }

        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_SUPPORT_SUBSCRIBE,
                false);
    }

    @Override
    public boolean supportUnSubscribe() {
        if (isOldVersionSdk()) {
            return false;
        }

        return RemoteMediaToolInvoker.sendInvokeSync(this,
                InvokeConstants.INVOKE_SUPPORT_UNSUBSCRIBE, false);
    }

    @Override
    public boolean supportPlaySubscribe() {
        if (isOldVersionSdk()) {
            return true;
        }
        return RemoteMediaToolInvoker.sendInvokeSync(this,
                InvokeConstants.INVOKE_SUPPORT_PLAY_SUBSCRIBE, false);
    }

    @Override
    public boolean supportSearch() {
        if (isOldVersionSdk()) {
            return true;
        }

        return RemoteMediaToolInvoker.sendInvokeSync(this,
                InvokeConstants.INVOKE_SUPPORT_SEARCH, false);
    }

    @Override
    public boolean hasNext() {
        if (isOldVersionSdk()) {
            return true;
        }

        return RemoteMediaToolInvoker.sendInvokeSync(this,
                InvokeConstants.INVOKE_HAS_NEXT, true);
    }

    @Override
    public boolean hasPrev() {
        if (isOldVersionSdk()) {
            return true;
        }

        return RemoteMediaToolInvoker.sendInvokeSync(this, InvokeConstants.INVOKE_HAS_PREV, true);
    }

    @Override
    public boolean interceptRecordWinControl(final MEDIA_TOOL_OP op) {
        if (MEDIA_TOOL_OP.PLAY == op && bShowSearchResult) {
            return true;
        }

        return false;
    }

    private boolean isOldVersionSdk() {
        return 1 == mSDKVersion;
    }


    //----------- single instance -----------
    private static volatile RemoteMusicTool sInstance;

    public static RemoteMusicTool getInstance() {
        if (null == sInstance) {
            synchronized (RemoteMusicTool.class) {
                if (null == sInstance) {
                    sInstance = new RemoteMusicTool();
                }
            }
        }

        return sInstance;
    }

    private RemoteMusicTool() {

    }
    //----------- single instance -----------

    // logger
    private void log(String msg) {
        JNIHelper.logd(LOG_TAG + "::" + msg);
    }
}
