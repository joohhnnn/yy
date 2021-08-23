package com.txznet.launcher.cfg;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;

/**
 * Created by ASUS User on 2018/7/12.
 */

public class DebugUtils {
    //测试广播，应远峰要求，将声控唤醒的结果和识别的结果发广播给远峰
    private static final String ACTION_ASR_RESULT = "com.txznet.txz.intent.action.ACTION_ASR_DEBUG";
    private static final int TYPE_ASR = 0;
    private static final int TYPE_WAKEUP = 1;

    public static void sendAsrResult(String asrResult) {
        if (!DebugCfg.YF_ASR_RESULT_DEBUG && !TextUtils.isEmpty(asrResult)) {
            Intent intent = new Intent(ACTION_ASR_RESULT);
            intent.putExtra("type", TYPE_ASR);
            intent.putExtra("data", asrResult);
            GlobalContext.get().sendBroadcast(intent);
        }
    }

    public static void sendWakeupResult(String wakeupResult) {
        if (!DebugCfg.YF_ASR_RESULT_DEBUG && !TextUtils.isEmpty(wakeupResult)) {
            Intent intent = new Intent(ACTION_ASR_RESULT);
            intent.putExtra("type", TYPE_WAKEUP);
            intent.putExtra("data", wakeupResult);
            GlobalContext.get().sendBroadcast(intent);
        }
    }
}
