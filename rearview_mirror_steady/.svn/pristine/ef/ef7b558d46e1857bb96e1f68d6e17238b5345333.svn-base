package com.txznet.txz.component.media;

import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.sp.CommonSp;

/**
 * 用于持久化存储媒体工具优先级的sp
 * Created by J on 2018/5/14.
 */

public class MediaPrioritySp extends CommonSp {
    public static final String SP_NAME = "media_priority";

    public static final String KEY_PRIORITY_AUDIO = "priority_audio";
    public static final String KEY_PRIORITY_MUSIC = "priority_music";
    //public static final String KEY_PRIORITY_MEDIA = "priority_media";

    public String getPriorityMusic() {
        return getValue(KEY_PRIORITY_MUSIC, "");
    }

    public String getPriorityAudio() {
        return getValue(KEY_PRIORITY_AUDIO, "");
    }

    public void updatePriorityMusic(String packageName) {
        setValue(KEY_PRIORITY_MUSIC, TextUtils.isEmpty(packageName) ? "" : packageName);
    }

    public void updatePriorityAudio(String packageName) {
        setValue(KEY_PRIORITY_AUDIO, TextUtils.isEmpty(packageName) ? "" : packageName);
    }

    //----------- single instance -----------
    private static volatile MediaPrioritySp sInstance;

    public static MediaPrioritySp getInstance() {
        if (null == sInstance) {
            synchronized (MediaPrioritySp.class) {
                if (null == sInstance) {
                    sInstance = new MediaPrioritySp();
                }
            }
        }

        return sInstance;
    }

    private MediaPrioritySp() {
        super(GlobalContext.get(), SP_NAME);
    }
    //----------- single instance -----------
}
