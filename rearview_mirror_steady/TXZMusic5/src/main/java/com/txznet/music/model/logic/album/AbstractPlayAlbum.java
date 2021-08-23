package com.txznet.music.model.logic.album;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.IMediaPlayer;
import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.ProxyHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.PlayInfoEvent;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.DisposableManager;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.util.TtsHelper;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

/**
 * 播放专辑的逻辑
 *
 * @author zackzhou
 * @date 2018/12/17,9:49
 */

public abstract class AbstractPlayAlbum {
    public static final String TAG = Constant.LOG_TAG_LOGIC + ":Album";
    protected final Album album;
    private boolean hasNotifyError; // 是否已经提示过用户

    protected AbstractPlayAlbum(Album album) {
        this.album = album;
    }

    /**
     * 播放专辑的处理
     */
    public void onPlayAlbum(Operation operation, PlayScene scene) {
        if (!album.isPlayEnd
                && PlayHelper.get().getCurrPlayScene() != null && PlayHelper.get().getCurrPlayScene() == scene
                && PlayHelper.get().getCurrAlbum() != null && PlayHelper.get().getCurrAlbum().equals(album)
                && PlayHelper.get().getCurrAudio() != null) {
            if (IMediaPlayer.STATE_ON_PAUSED == PlayHelper.get().getCurrPlayState()) {
                AudioPlayer.getDefault().start();
            }
            return;
        }
        if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            if (Operation.SOUND == operation) {
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_NET_OFFLINE", Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
            } else {
                ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
            }
            return;
        }

        // 防止加载更多互串
        DisposableManager.get().remove("loadMore");
        AudioV5 audio = PlayHelper.get().getCurrAudio();
        if (audio != null) {
            ProxyHelper.releaseProxyRequest(audio.sid, audio.id);

            ReportEvent.reportAudioPlayEnd(audio,
                    PlayInfoEvent.MANUAL_TYPE_MANUAL,
                    AudioUtils.isLocalSong(audio.sid) ? PlayInfoEvent.ONLINE_TYPE_OFFLINE : PlayInfoEvent.ONLINE_TYPE_ONLINE, PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(),
                    Operation.SOUND == operation ? PlayInfoEvent.EXIT_TYPE_SOUND : Operation.MANUAL == operation ? PlayInfoEvent.EXIT_TYPE_MANUAL : PlayInfoEvent.EXIT_TYPE_OTHER, false);
        }

        // 清空当前播放内容
        PlayHelper.get().clearNotAlbum();
        PlayHelper.get().setPlayScene(scene);
        PlayHelper.get().setCurrAlbum(album);

        onPlayAlbumInner(operation);
    }

    /**
     * 播放专辑的处理
     */
    protected abstract void onPlayAlbumInner(Operation operation);

    /**
     * 通知错误
     */
    protected void notifyRequestError(Operation operation) {
        if (hasNotifyError) {
            return;
        }
        if ((album == null && PlayHelper.get().getCurrAlbum() == null)
                || (album != null && PlayHelper.get().getCurrAlbum() != null && album.equals(PlayHelper.get().getCurrAlbum()))) {
            Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_STATE_CHANGE)
                    .bundle(Constant.PlayConstant.KEY_AUDIO, PlayHelper.get().getCurrAudio())
                    .bundle(Constant.PlayConstant.KEY_PLAY_STATE, IMediaPlayer.STATE_ON_PAUSED).build());
            TtsHelper.speakNetworkError();
            hasNotifyError = true;
        }
    }

    /**
     * 通知错误
     */
    protected void notifyAlbumEmpty(Operation operation) {
        if ((album == null && PlayHelper.get().getCurrAlbum() == null)
                || (album != null && PlayHelper.get().getCurrAlbum() != null && album.equals(PlayHelper.get().getCurrAlbum()))) {
            Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_INFO_CHANGE)
                    .bundle(Constant.PlayConstant.KEY_AUDIO, null)
                    .bundle(Constant.PlayConstant.KEY_PLAY_WILL_BE_END, false).build());
            Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_STATE_CHANGE)
                    .bundle(Constant.PlayConstant.KEY_AUDIO, null)
                    .bundle(Constant.PlayConstant.KEY_PLAY_STATE, IMediaPlayer.STATE_ON_PAUSED).build());
            if (Operation.SOUND == operation) {
                TtsHelper.speakResourceUnCancel("RS_VOICE_SPEAK_NO_AUDIOS_TIPS", Constant.RS_VOICE_SPEAK_NO_AUDIOS_TIPS);
            } else {
                ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_NO_AUDIOS_TIPS);
            }
            PlayHelper.get().clearNotAlbum();
        }
    }

    // 检测是否需要重新请求
    protected void check2DoRetry(Operation operation, Throwable throwable) {
        Error error = null;
        if (throwable instanceof Error) {
            error = (Error) throwable;
        }
        if (error == null && throwable.getCause() != null && throwable.getCause() instanceof Error) {
            error = (Error) throwable.getCause();
        }

        if (error != null) {
            if (error.errorCode == ErrCode.ERROR_CLIENT_NET_TIMEOUT
                    || error.errorCode == ErrCode.ERROR_CORE_RESP_WRONG
                    || error.errorCode == ErrCode.ERROR_CLIENT_NET_OFFLINE) {
                Logger.d(TAG, "req album wrong, error code =" + error.errorCode + ", retry later");
                AppLogic.runOnUiGround(() -> {
                    Album currAlbum = PlayHelper.get().getCurrAlbum();
                    Logger.d(TAG, "req album check, album=" + album + ", curAlbum=" + currAlbum);
                    if (currAlbum != null && currAlbum.equals(album)) {
                        onPlayAlbumInner(operation);
                    }
                }, 3000);
            } else {
                Logger.d(TAG, "req album error, msg=" + throwable);
            }
        } else {
            Logger.d(TAG, "req album error, msg=" + throwable);
        }
    }
}
