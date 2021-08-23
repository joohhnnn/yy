package com.txznet.audio.player.playback;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.KeyEvent;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.IMediaPlayer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogicBase;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackController {
    private static final String TAG = "MediaPlaybackController";
    private static final SparseArray<List<Integer>> EXTRA_KEY = new SparseArray<>();
    private IMediaButtonHandler mMediaButtonHandler;

    private MediaPlaybackController() {
    }

    private static class Holder {
        private static final MediaPlaybackController INSTANCE = new MediaPlaybackController();
    }

    public static MediaPlaybackController get() {
        return Holder.INSTANCE;
    }

    /**
     * 扩展音频按键
     *
     * @param oriKeyCode 原始keyCode
     * @param extraKey   扩展keyCode
     */
    public void setExtraKeyCode(int oriKeyCode, int... extraKey) {
        List<Integer> keys = new ArrayList<>(extraKey.length);
        for (int key : extraKey) {
            keys.add(key);
        }
        EXTRA_KEY.put(oriKeyCode, keys);
    }

    // 反查原始keyCode
    private int getOriKeyCode(int keyCode) {
        for (int i = 0; i < EXTRA_KEY.size(); i++) {
            int key = EXTRA_KEY.keyAt(i);
            if (key == keyCode) {
                return key;
            }
            List<Integer> val = EXTRA_KEY.valueAt(i);
            if (val != null && val.contains(keyCode)) {
                return key;
            }
        }
        return keyCode;
    }

    /**
     * 启动，抢占MediaSession
     * 应用应该在每次音频播放时抢占一次
     */
    public void enable() {
        AppLogicBase.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                MediaButtonRegister.getInstance().registerMediaButtonEventReceiver(GlobalContext.get());
            }
        });
    }

    /**
     * 关闭，释放MediaSession
     * 应用应该在音频焦点时调用该方法
     */
    public void disable() {
        AppLogicBase.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                MediaButtonRegister.getInstance().unRegisterMediaButtonEventReceiver(GlobalContext.get());
            }
        });
    }

    /**
     * 设置按键监听
     */
    public void setMediaButtonHandler(IMediaButtonHandler handler) {
        mMediaButtonHandler = handler;
    }

    // 内部处理音频按键
    static void handleIntent(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null)
                return;

            if (event.getKeyCode() != KeyEvent.KEYCODE_HEADSETHOOK
                    && event.getKeyCode() != KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    && event.getAction() != KeyEvent.ACTION_DOWN)
                return;
            LogUtil.d(TAG, "handle media button key down, key_code=" + event.getKeyCode());

            int oriKey = MediaPlaybackController.get().getOriKeyCode(event.getKeyCode());

            if (MediaPlaybackController.get().mMediaButtonHandler != null) {
                boolean intercept = MediaPlaybackController.get().mMediaButtonHandler.onMediaButtonKeyDown(oriKey);
                if (intercept) {
                    return;
                }
            }
            switch (oriKey) {
                case KeyEvent.KEYCODE_MEDIA_PLAY: // 播放
                    AudioPlayer.getDefault().start();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE: // 暂停
                    AudioPlayer.getDefault().pause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP: // 停止
                    AudioPlayer.getDefault().stop();
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT: // 下一首
                    AudioPlayer.getDefault().next(true);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS: // 上一首
                    AudioPlayer.getDefault().prev();
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: // 播放/暂停
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (AudioPlayer.getDefault().getCurrPlayState() == IMediaPlayer.STATE_ON_PLAYING) {
                            AudioPlayer.getDefault().pause();
                        } else if (AudioPlayer.getDefault().getCurrPlayState() == IMediaPlayer.STATE_ON_PAUSED) {
                            AudioPlayer.getDefault().start();
                        }
                    }
                    break;
            }
        }
    }
}
