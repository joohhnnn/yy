package com.txznet.audio.player.focus;

import android.support.annotation.IntDef;

import com.txznet.audio.player.AudioPlayer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

public interface IAudioFocusHandler {
    @IntDef({
            AUDIOFOCUS_GAIN,
            AUDIOFOCUS_LOSS,
            AUDIOFOCUS_LOSS_TRANSIENT,
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface AudioFocus {
    }

    void onAudioFocusChange(AudioPlayer player, @AudioFocus int focusChange);

}
