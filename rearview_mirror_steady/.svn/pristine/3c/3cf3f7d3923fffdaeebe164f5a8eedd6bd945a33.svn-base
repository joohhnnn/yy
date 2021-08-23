package com.txznet.audio.player.focus;

import android.media.AudioManager;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.IMediaPlayer;

/**
 * 默认音频焦点处理逻辑
 *
 * @author zackzhou
 * @date 2018/12/21,18:54
 */

public class DefaultAudioFocusHandler implements IAudioFocusHandler {
    private boolean mPausedByTransientLossOfFocus = false;

    @Override
    public void onAudioFocusChange(AudioPlayer player, int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN: // 重新获得焦点
                player.setVolume(1f, 1f);
                if (!checkPlaying(player.getCurrPlayState()) && mPausedByTransientLossOfFocus) {
                    player.start();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS: // 焦点丢失
                if (checkPlayingUnStrict(player.getCurrPlayState())) {
                    mPausedByTransientLossOfFocus = false;
                }
                player.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: //  短暂焦点丢失
                mPausedByTransientLossOfFocus = checkPlayingUnStrict(player.getCurrPlayState());
                player.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: // 降半播放
                mPausedByTransientLossOfFocus = checkPlayingUnStrict(player.getCurrPlayState());
                player.setVolume(0.5f, 0.5f);
                break;
            default:
                break;
        }
    }

    // 当前是否处于播放中
    private boolean checkPlaying(@IMediaPlayer.PlayState int state) {
        return IMediaPlayer.STATE_ON_PLAYING == state;
    }

    // 当前是否处于播放中
    private boolean checkPlayingUnStrict(@IMediaPlayer.PlayState int state) {
        return state == IMediaPlayer.STATE_ON_PREPARING
                || state == IMediaPlayer.STATE_ON_PREPARED
                || state == IMediaPlayer.STATE_ON_PLAYING
                || state == IMediaPlayer.STATE_ON_BUFFERING;
    }
}
