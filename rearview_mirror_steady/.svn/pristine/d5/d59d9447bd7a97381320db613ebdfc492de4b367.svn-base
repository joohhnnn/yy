package com.txznet.music.model.logic.queue;

import android.support.annotation.CallSuper;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.audio.player.queue.PlayQueue;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.music.Constant;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.ProxyHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.PlayInfoEvent;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.util.TtsHelper;
import com.txznet.rxflux.Operation;

/**
 * 音频上下首选择器
 *
 * @author zackzhou
 * @date 2018/12/18,15:21
 */

public abstract class AbstractQueueItemPicker implements AudioPlayer.AudioPlayerQueueInterceptor {
    public static final String TAG = Constant.LOG_TAG_LOGIC;
    protected final Album album;

    protected AbstractQueueItemPicker(Album album) {
        this.album = album;
    }

    @Override
    @CallSuper
    public void pickNextItem(PlayQueue queue, Audio oriAudio, boolean fromUser, Callback callback) {
        // 打断tts播报
        TtsHelper.cancel();
        AudioV5 audio = PlayHelper.get().getCurrAudio();
        if (audio != null) {
            ProxyHelper.releaseProxyRequest(audio.sid, audio.id);

            @PlayInfoEvent.ExitType int exitType;
            if (fromUser) {
                Operation operation = PlayHelper.get().getLastSwitchAudioOperation();
                if (Operation.SOUND == operation) {
                    exitType = PlayInfoEvent.EXIT_TYPE_SOUND;
                } else if (Operation.MANUAL == operation) {
                    exitType = PlayInfoEvent.EXIT_TYPE_MANUAL;
                } else {
                    exitType = PlayInfoEvent.EXIT_TYPE_OTHER;
                }
            } else {
                exitType = PlayInfoEvent.EXIT_TYPE_AUTO;
            }
            ReportEvent.reportAudioPlayEnd(audio,
                    fromUser ? PlayInfoEvent.MANUAL_TYPE_MANUAL : PlayInfoEvent.MANUAL_TYPE_AUTO,
                    AudioUtils.isLocalSong(audio.sid) ? PlayInfoEvent.ONLINE_TYPE_OFFLINE : PlayInfoEvent.ONLINE_TYPE_ONLINE,
                    PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(), exitType, PlayHelper.get().getLastSwitchAudioOperation() == Operation.ERROR);
        }
    }

    @Override
    @CallSuper
    public void pickPrevItem(PlayQueue queue, Audio oriAudio, Callback callback) {
        // 打断tts播报
        TtsHelper.cancel();
        AudioV5 audio = PlayHelper.get().getCurrAudio();
        if (audio != null) {
            ProxyHelper.releaseProxyRequest(audio.sid, audio.id);

            @PlayInfoEvent.ExitType int exitType;
            Operation operation = PlayHelper.get().getLastSwitchAudioOperation();
            if (Operation.SOUND == operation) {
                exitType = PlayInfoEvent.EXIT_TYPE_SOUND;
            } else if (Operation.MANUAL == operation) {
                exitType = PlayInfoEvent.EXIT_TYPE_MANUAL;
            } else {
                exitType = PlayInfoEvent.EXIT_TYPE_OTHER;
            }
            ReportEvent.reportAudioPlayEnd(audio,
                    PlayInfoEvent.MANUAL_TYPE_MANUAL,
                    AudioUtils.isLocalSong(audio.sid) ? PlayInfoEvent.ONLINE_TYPE_OFFLINE : PlayInfoEvent.ONLINE_TYPE_ONLINE, PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(), exitType,
                    PlayHelper.get().getLastSwitchAudioOperation() == Operation.ERROR);
        }
    }

    /**
     * 通知错误
     */
    protected void notifyRequestError(Operation operation) {
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_NET_POOR", Constant.RS_VOICE_SPEAK_ASR_NET_POOR);
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_POOR);
        } else {
            TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_NET_OFFLINE", Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
        }
    }

    private static int mFirstTtsId;

    /**
     * 通知已经是第一个
     */
    protected void notifyAlreadyFirst(Operation operation) {
        TtsUtil.cancelSpeak(mFirstTtsId);
        if (Operation.SOUND == operation) {
            mFirstTtsId = TtsHelper.speakResourceUnCancel("RS_VOICE_ALREADY_FIRST", Constant.RS_VOICE_ALREADY_FIRST);
        } else {
            ToastUtils.showShortOnUI(Constant.RS_VOICE_ALREADY_FIRST);
        }
    }
}
