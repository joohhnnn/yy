package com.txznet.webchat.util;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

import com.txznet.webchat.R;
import com.txznet.webchat.stores.WxConfigStore;

public class SoundManager {

    /* 声音池 */
    private static SoundPool mSoundPool;

    /* 声音 */
    private SparseIntArray mSoundMap;

    private static AudioManager am;
    private static SoundManager mInstance;
    private Context mContext;

    private int mCurrentSize;

    private float mMaxVolume;
    private float mCurrentVolume;

    private SoundManager(Context context) {
        this.mContext = context;

        am = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
        mMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        mCurrentVolume = am.getStreamVolume(AudioManager.STREAM_ALARM);

        mSoundPool = new SoundPool(2, AudioManager.STREAM_ALARM, 1);
        mSoundMap = new SparseIntArray();

        addSound(R.raw.after_upload_voice);
        addSound(R.raw.play_completed);
        addSound(R.raw.play_long_text);
    }

    public static SoundManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (SoundManager.class) {
                mInstance = new SoundManager(context.getApplicationContext());
            }
        }

        return mInstance;
    }

    public void addSound(int rawId) {
        mSoundMap.put(mCurrentSize, mSoundPool.load(mContext, rawId, 1));
        mCurrentSize++;
    }

    /**
     * 播放第几个声音
     *
     * @param sound
     */
    public void play(int sound) {
        if (!isPlayTone()) {
            return;
        }

        float configvolume = WxConfigStore.getInstance().getBeepVolume();
        float ratioVolume;
        if (configvolume < 0) {
            // 使用当前音量
            ratioVolume = mCurrentVolume / mMaxVolume;
        } else if (configvolume <= 1f) {
            // 使用配置音量
            ratioVolume = configvolume;
        } else {
            // 音量最大1.0
            ratioVolume = 1f;
        }

        mSoundPool.play(mSoundMap.get(sound), ratioVolume, ratioVolume, 1, 0, 1);
    }

    /**
     * 当响铃模式为静音或者震动时，不发出声音
     *
     * @return
     */
    private boolean isPlayTone() {
        if ((am.getRingerMode() == AudioManager.RINGER_MODE_SILENT) || (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE)) {
            return false;
        }
        return true;
    }
}
