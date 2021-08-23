package com.txznet.music.utils;

import com.txznet.comm.remote.util.TtsUtil;

public class TtsUtilWrapper {
    private static final boolean INTERCEPT_TTS = true; // 上汽通用专版禁用tts

    public static int speakText(int iStream, String sText, TtsUtil.PreemptType bPreempt) {
        if (INTERCEPT_TTS) {
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakText(iStream, sText, bPreempt);
    }

    public static int speakText(int iStream, String sText, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakText(iStream, sText, oRun);
    }

    public static int speakText(int iStream, String sText) {
        if (INTERCEPT_TTS) {
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakText(iStream, sText);
    }

    public static int speakText(String sText, TtsUtil.PreemptType bPreempt, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakText(sText, bPreempt, oRun);
    }

    public static int speakText(String sText, TtsUtil.PreemptType bPreempt) {
        if (INTERCEPT_TTS) {
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakText(sText, bPreempt);
    }

    public static int speakText(String sText, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakText(sText, oRun);
    }

    public static int speakText(String sText) {
        if (INTERCEPT_TTS) {
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakText(sText);
    }

    public static int speakText(int iStream, String sText, TtsUtil.PreemptType bPreempt, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakText(iStream, sText, bPreempt, oRun);
    }

    public static int speakText(int iStream, String sText, long delay, TtsUtil.PreemptType bPreempt, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakText(iStream, sText, delay, bPreempt, oRun);
    }

    /////////////////////////

    public static int speakResource(String resId, String defaultText) {
        if (INTERCEPT_TTS) {
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakResource(resId, defaultText);
    }

    public static int speakResource(String resId, String[] resArgs, String defaultText) {
        if (INTERCEPT_TTS) {
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakResource(resId, resArgs, defaultText);
    }

    public static int speakResource(String resId, String defaultText, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakResource(resId, defaultText, oRun);
    }

    public static int speakResource(String resId, String[] resArgs, String defaultText, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakResource(resId, resArgs, defaultText, oRun);
    }

    public static int speakResource(int iStream, String resId, String defaultText, TtsUtil.PreemptType bPreempt, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakResource(iStream, resId, defaultText, bPreempt, oRun);
    }

    public static int speakResource(int iStream, String resId, String[] resArgs, String defaultText, TtsUtil.PreemptType bPreempt, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakResource(iStream, resId, resArgs, defaultText, bPreempt, oRun);
    }

    public static int speakResource(int iStream, String resId, String[] resArgs, String defaultText, String[] voiceUrls, TtsUtil.PreemptType preempt, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(iStream, resId, resArgs, defaultText, voiceUrls, preempt, oRun);
    }

    ///////////////////////////

    public static int speakVoice(String voiceUrl, TtsUtil.PreemptType bPreempt, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(voiceUrl, bPreempt, oRun);
    }

    public static int speakVoice(String voiceUrl, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(voiceUrl, oRun);
    }

    public static int speakVoice(String sText, String voiceUrl, TtsUtil.PreemptType bPreempt) {
        if (INTERCEPT_TTS) {
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(sText, voiceUrl, bPreempt);
    }

    public static int speakVoice(String sText, String voiceUrl) {
        if (INTERCEPT_TTS) {
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(sText, voiceUrl);
    }

    public static int speakVoice(String voiceUrl, TtsUtil.PreemptType bPreempt) {
        if (INTERCEPT_TTS) {
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(voiceUrl, bPreempt);
    }

    public static int speakVoice(String voiceUrl) {
        if (INTERCEPT_TTS) {
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(voiceUrl);
    }

    public static int speakVoice(String sText, String voiceUrl, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(sText, voiceUrl, oRun);
    }

    public static int speakVoice(String sText, String voiceUrl, TtsUtil.PreemptType bPreempt, TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(sText, voiceUrl, bPreempt, oRun);
    }

    public static int speakVoice(int iStream, String sText, String[] voiceUrls, TtsUtil.PreemptType bPreempt, final TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(iStream, sText, voiceUrls, bPreempt, oRun);
    }

    public static int speakVoice(int iStream, String sText, String[] voiceUrls, long delay, TtsUtil.PreemptType bPreempt, final TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(iStream, sText, voiceUrls, delay, bPreempt, oRun);
    }

    public static int speakVoice(int iStream, String resId, String[] resArgs, String sText, String[] voiceUrls, TtsUtil.PreemptType bPreempt, final TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(iStream, resId, resArgs, sText, voiceUrls, bPreempt, oRun);
    }

    public static int speakVoiceTask(TtsUtil.VoiceTask[] voiceTasks, final TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoiceTask(voiceTasks, oRun);
    }

    public static int speakVoiceTask(int iStream, TtsUtil.VoiceTask[] voiceTasks, final TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoiceTask(iStream, voiceTasks, oRun);
    }

    public static int speakVoiceTask(TtsUtil.PreemptType bPreempt, TtsUtil.VoiceTask[] voiceTasks, final TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoiceTask(bPreempt, voiceTasks, oRun);
    }

    public static int speakVoiceTask(int iStream, TtsUtil.PreemptType bPreempt, TtsUtil.VoiceTask[] voiceTasks, final TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoiceTask(iStream, bPreempt, voiceTasks, oRun);
    }

    public static void refreshFeatures() {
        TtsUtil.refreshFeatures();
    }

    public static int speakVoice(int iStream, String resId, String[] resArgs, String sText, String[] voiceUrls, long delay, TtsUtil.PreemptType bPreempt, final TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(iStream, resId, resArgs, sText, voiceUrls, delay, bPreempt, oRun);
    }

    public static int speakVoice(int iStream, String resId, String[] resArgs, String sText, String[] voiceUrls, long delay, TtsUtil.PreemptType bPreempt, TtsUtil.VoiceTask[] voiceTasks, final TtsUtil.ITtsCallback oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.onEnd();
            }
            return TtsUtil.INVALID_TTS_TASK_ID;
        }
        return TtsUtil.speakVoice(iStream, resId, resArgs, sText, voiceUrls, delay, bPreempt, voiceTasks, oRun);
    }

    public static void cancelSpeak(int iTaskId) {
        TtsUtil.cancelSpeak(iTaskId);
    }

    public static void speakTextOnRecordWin(String sText, boolean close,
                                            Runnable oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.run();
            }
            return;
        }
        TtsUtil.speakTextOnRecordWin(sText, close, oRun);
    }

    public static void speakTextOnRecordWin(String resId, String sText, boolean close,
                                            Runnable oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.run();
            }
            return;
        }
        TtsUtil.speakTextOnRecordWin(resId, sText, close, oRun);
    }

    public static void speakTextOnRecordWin(String resId, String sText, String[] resArgs, boolean close,
                                            Runnable oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.run();
            }
            return;
        }
        TtsUtil.speakTextOnRecordWin(resId, sText, resArgs, close, oRun);
    }

    public static void speakTextOnRecordWinWithCancle(String resId, String sText, String[] resArgs, boolean isCancleExecute,
                                                      Runnable oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.run();
            }
            return;
        }
        TtsUtil.speakTextOnRecordWin(resId, sText, resArgs, isCancleExecute, oRun);
    }


    /**
     * @param sText   播报的内容
     * @param close   播报完毕后是否关闭界面
     * @param needAsr 是否开启识别的状态
     * @param oRun    播报完毕执行的逻辑
     */
    public static void speakTextOnRecordWin(String sText, boolean close,
                                            boolean needAsr, Runnable oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.run();
            }
            return;
        }
        TtsUtil.speakTextOnRecordWin(sText, close, needAsr, oRun);
    }

    public static void speakTextOnRecordWin(String resId, String sText, String[] resArgs, boolean close,
                                            boolean needAsr, Runnable oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.run();
            }
            return;
        }
        TtsUtil.speakTextOnRecordWin(resId, sText, resArgs, close, needAsr, oRun);
    }

    public static void speakTextOnRecordWin(String resId, String sText, String[] resArgs, boolean close,
                                            boolean needAsr, boolean isCancleExecute, Runnable oRun) {
        if (INTERCEPT_TTS) {
            if (oRun != null) {
                oRun.run();
            }
            return;
        }
        TtsUtil.speakTextOnRecordWin(resId, sText, resArgs, close, needAsr, isCancleExecute, oRun);
    }
}
