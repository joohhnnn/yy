package com.txznet.music.model;

import com.txznet.audio.player.IMediaPlayer;
import com.txznet.audio.player.core.ijk.IjkMediaPlayer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.Logger;
import com.txznet.music.util.TtsHelper;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

/**
 * 网络状态检测
 *
 * @author zackzhou
 * @date 2019/1/17,10:03
 */

public class NetworkCheckModel extends RxWorkflow {

    // 相同音频，播放进度相同的次数
    private AudioV5 mLastAudio;
    private long mLastPosition;
    private boolean isRecordWinShowing;
    private boolean isTtsSpeaking;

    private Runnable mNetworkWarnTipsTask = new Runnable() {
        @Override
        public void run() {
            AudioV5 audioV5 = PlayHelper.get().getCurrAudio();
            long position = PlayHelper.get().getLastPosition();
            if (audioV5 == mLastAudio && mLastPosition == position) {
                if (isRecordWinShowing || TtsHelper.isNotifySeek) {
                    AppLogic.removeBackGroundCallback(mNetworkWarnTipsTask);
                    AppLogic.runOnBackGround(mNetworkWarnTipsTask, 5000);
                    return;
                }
                String resId;
                String defaultText;

                if (IMediaPlayer.STATE_ON_ERROR == PlayHelper.get().getCurrPlayState()) {
                    return;
                }
                if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                    resId = "RS_VOICE_SPEAK_ASR_NET_POOR_LOADING";
                    defaultText = Constant.RS_VOICE_SPEAK_ASR_NET_POOR_LOADING;
                } else {
                    resId = "RS_VOICE_SPEAK_ASR_NET_OFFLINE";
                    defaultText = Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE;
                }
                isTtsSpeaking = true;
                TtsHelper.speakResource(resId, defaultText, new TtsUtil.ITtsCallback() {
                    @Override
                    public void onEnd() {
                        isTtsSpeaking = false;
                    }
                });
            }
        }
    };

    private Runnable mCheckAlbumLoadedTask = () -> {
        if (PlayHelper.get().getCurrAudio() == null
                && PlayHelper.get().getCurrAlbum() != null
                && PlayHelper.get().getCurrPlayState() != IMediaPlayer.STATE_ON_PAUSED) {
            Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_STATE_CHANGE)
                    .bundle(Constant.PlayConstant.KEY_AUDIO, PlayHelper.get().getCurrAudio())
                    .bundle(Constant.PlayConstant.KEY_PLAY_STATE, IMediaPlayer.STATE_ON_BUFFERING).build());
        }
    };

    private Runnable mCheckBuffTask = () -> {
        if (PlayHelper.get().getCurrAudio() != null) {
            if (IMediaPlayer.STATE_ON_BUFFERING == PlayHelper.get().getCurrPlayState()
                    && PlayHelper.get().getLastPosition() == 0) {
                Logger.w(Constant.LOG_TAG_LOGIC, "continue buff, replay now");
                PlayHelper.get().replay();
            }
        }
    };

    @Override
    public void onAction(RxAction action) {
        AudioV5 audioV5;
        switch (action.type) {
            case ActionType.ACTION_PLAYER_ON_STATE_CHANGE: //  播放状态改变
                int state = PlayHelper.get().getCurrPlayState();
                long position = PlayHelper.get().getLastPosition();
                audioV5 = PlayHelper.get().getCurrAudio();
                if (audioV5 != null && !AudioUtils.isLocalSong(audioV5.sid) && IMediaPlayer.STATE_ON_BUFFERING == state && position != mLastPosition && !isTtsSpeaking) {
                    mLastAudio = PlayHelper.get().getCurrAudio();
                    mLastPosition = PlayHelper.get().getLastPosition();
                    AppLogic.removeBackGroundCallback(mNetworkWarnTipsTask);
                    AppLogic.runOnBackGround(mNetworkWarnTipsTask, 5000);
                } else if (IMediaPlayer.STATE_ON_PLAYING == state) {
                    AppLogic.removeBackGroundCallback(mNetworkWarnTipsTask);
                } else if (IMediaPlayer.STATE_ON_PAUSED == state) {
                    AppLogic.removeBackGroundCallback(mNetworkWarnTipsTask);
                } else if (IjkMediaPlayer.STATE_ON_STOPPED == state) {
                    AppLogic.removeBackGroundCallback(mNetworkWarnTipsTask);
                }
                if (IMediaPlayer.STATE_ON_BUFFERING == state) {
                    AppLogic.removeBackGroundCallback(mCheckBuffTask);
                    AppLogic.runOnBackGround(mCheckBuffTask, 3000);
                }
                break;
            case ActionType.ACTION_PLAYER_ON_INFO_CHANGE: // 播放内容改变
                AppLogic.removeUiGroundCallback(mCheckAlbumLoadedTask);
                AppLogic.removeUiGroundCallback(mNetworkWarnTipsTask);
                audioV5 = PlayHelper.get().getCurrAudio();
                if (audioV5 != null) {
                    mLastAudio = PlayHelper.get().getCurrAudio();
                    mLastPosition = PlayHelper.get().getLastPosition();
                    AppLogic.removeBackGroundCallback(mNetworkWarnTipsTask);
                    AppLogic.runOnBackGround(mNetworkWarnTipsTask, 5000);
                }
                AppLogic.removeBackGroundCallback(mCheckBuffTask);
                break;
            case ActionType.ACTION_PLAYER_ON_ALBUM_CHANGE:
                AppLogic.removeUiGroundCallback(mCheckAlbumLoadedTask);
                AppLogic.runOnUiGround(mCheckAlbumLoadedTask, 0);
                break;
            case ActionType.ACTION_RECORD_WIN_SHOW:
                isRecordWinShowing = true;
                break;
            case ActionType.ACTION_RECORD_WIN_DISMISS:
                isRecordWinShowing = false;
                break;
            default:
                break;
        }
    }
}
