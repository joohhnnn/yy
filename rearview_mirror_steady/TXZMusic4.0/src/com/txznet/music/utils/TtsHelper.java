package com.txznet.music.utils;

import com.txznet.comm.remote.util.TtsUtil;

/**
 * Created by telenewbie on 2017/9/28.
 */

public class TtsHelper {

    private static int ttsId = -1;

    public static void speakResource(String resId, String defaultText, TtsUtil.ITtsCallback oRun) {
        cancle();
        ttsId = TtsUtil.speakResource(resId, defaultText, oRun);
    }

    public static void speakResource(String resId, String defaultText) {
        speakResource(resId, defaultText, null);
    }


    public static void cancle() {
        if (ttsId != -1) {
            TtsUtil.cancelSpeak(ttsId);
        }
    }
}
