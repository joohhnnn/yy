package com.txznet.music.util;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.music.Constant;

/**
 * Created by telenewbie on 2017/9/28.
 */

public class TtsHelper {

    private static int ttsId = -1;

    public static int speakResource(String resId, String defaultText, TtsUtil.ITtsCallback oRun) {
        cancel();
        return ttsId = TtsUtil.speakResource(resId, defaultText, oRun);
    }

    public static int speakResourceUnCancel(String resId, String defaultText) {
        cancel();
        return TtsUtil.speakResource(resId, defaultText, null);
    }

    public static int speakResource(String resId, String defaultText) {
        return speakResource(resId, defaultText, null);
    }

    public static int speakResource(String resId, String[] resArgs, String defaultText) {
        cancel();
        return ttsId = TtsUtil.speakResource(resId, resArgs, defaultText, null);
    }

    public static void speakNetworkError() {
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_NET_POOR", Constant.RS_VOICE_SPEAK_ASR_NET_POOR);
        } else {
            TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_NET_OFFLINE", Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
        }
    }

    // 即将从上次停止处播放的提示语，是否处于播放中
    public static boolean isNotifySeek;

    public static void cancel() {
        if (ttsId != -1) {
            TtsUtil.cancelSpeak(ttsId);
        }
    }
}
