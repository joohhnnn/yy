package com.txznet.music.helper;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;

/**
 * 播放的逻辑，关于面唤醒声控的逻辑
 */
public class PlayerAboutAsr {


    //##创建一个单例类##
    private volatile static PlayerAboutAsr singleton;

    private PlayerAboutAsr() {
    }

    public static PlayerAboutAsr getInstance() {
        if (singleton == null) {
            synchronized (PlayerAboutAsr.class) {
                if (singleton == null) {
                    singleton = new PlayerAboutAsr();
                }
            }
        }
        return singleton;
    }

    int speakTTsId = 0;
    private Runnable runnable = () -> {
        stopBroadcastTTs();
        speakTTsId = TtsUtil.speakResource("RS_VOICE_SPEAK_ASR_NET_POOR", Constant.RS_VOICE_SPEAK_ASR_NET_POOR);
    };

    /**
     * 播报tts
     */
    public void broadcastTTS() {
        //详情请看，同听需求4.4.0---->2.2，产品只需要，免唤醒底下的播报，其他都不考虑，劝谏无用！（是啊，明明文档只写了免唤醒底下的处理逻辑，何必考虑其他情况呢）
        AppLogic.runOnBackGround(runnable, 3000);
    }

    public void stopBroadcastTTs() {
        TtsUtil.cancelSpeak(speakTTsId);
        AppLogic.removeBackGroundCallback(runnable);
    }


}
