package com.txznet.audio.player.playback;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.MediaSession.Callback;
import android.media.session.PlaybackState;
import android.media.session.PlaybackState.Builder;
import android.os.Build;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.txznet.comm.remote.util.LogUtil;

/**
 * 音频按键注册工具
 */
public class MediaButtonRegister {
    private static MediaButtonRegister INSTANCE = new MediaButtonRegister();
    private static MediaSession session;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    final static class AnonymousClass1 extends Callback {
        final Context ctx;

        AnonymousClass1(Context context) {
            this.ctx = context;
        }

        @Override
        public boolean onMediaButtonEvent(@NonNull Intent intent) {
            MediaPlaybackController.handleIntent(this.ctx, intent);
            return true;
        }
    }

    private MediaButtonRegister() {
    }

    public static MediaButtonRegister getInstance() {
        return INSTANCE;
    }

    @TargetApi(21)
    private static void setMediaButtonEvent(Context context) {
        String str = context.getPackageName();
        LogUtil.d("MediaButtonRegister setMediaButtonEvent " + str);
        if (session != null) {
            session.setActive(false);
            session = null;
        }
        session = new MediaSession(context, str);
        session.setPlaybackState(new Builder().setState(PlaybackState.STATE_PLAYING, -1, 10.0f).build());
        session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);
        session.setCallback(new AnonymousClass1(context));
        session.setActive(true);
    }

    public void registerMediaButtonEventReceiver(Context context) {
        LogUtil.d("MediaButtonRegister registerMediaButtonEventReceiver");
        if (context != null) {
            if (VERSION.SDK_INT >= 21) {
                try {
                    setMediaButtonEvent(context);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).registerMediaButtonEventReceiver(new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName()));
                } catch (Exception e2) {
                }
            }
        }
    }

    public void unRegisterMediaButtonEventReceiver(Context context) {
        LogUtil.d("MediaButtonRegister unRegisterMediaButtonEventReceiver");
        if (VERSION.SDK_INT < 21) {
            ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).unregisterMediaButtonEventReceiver(new ComponentName(context.getPackageName(), MediaButtonReceiver.class.getName()));
        } else if (session != null) {
            session.setActive(false);
            session.release();
            session = null;
        }
    }
}