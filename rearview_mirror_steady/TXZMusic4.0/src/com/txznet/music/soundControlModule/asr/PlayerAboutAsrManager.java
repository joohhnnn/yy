package com.txznet.music.soundControlModule.asr;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.baseModule.Constant;

/**
 * 播放的逻辑，关于面唤醒声控的逻辑
 */
public class PlayerAboutAsrManager {


    //##创建一个单例类##
    private volatile static PlayerAboutAsrManager singleton;

    private boolean isFreeWakeUp;

    private PlayerAboutAsrManager() {
    }

    public static PlayerAboutAsrManager getInstance() {
        if (singleton == null) {
            synchronized (PlayerAboutAsrManager.class) {
                if (singleton == null) {
                    singleton = new PlayerAboutAsrManager();
                }
            }
        }
        return singleton;
    }

    int speakTTsId = 0;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            stopBroadcastTTs();
            speakTTsId = TtsUtil.speakResource("RS_VOICE_SPEAK_ASR_NET_POOR", Constant.RS_VOICE_SPEAK_ASR_NET_POOR);
        }
    };

    /**
     * 播报tts
     */
    public void broadcastTTS() {
        //详情请看，同听需求4.4.0---->2.2，产品只需要，免唤醒底下的播报，其他都不考虑，劝谏无用！（是啊，明明文档只写了免唤醒底下的处理逻辑，何必考虑其他情况呢）
        isFreeWakeUp = true;
        AppLogic.runOnBackGround(runnable, 3000);
    }

    public void stopBroadcastTTs() {
        isFreeWakeUp = false;
        TtsUtil.cancelSpeak(speakTTsId);
        AppLogic.removeBackGroundCallback(runnable);
    }

    public boolean isFreeWakeUp() {
        return isFreeWakeUp;
    }
}
