package com.txznet.music.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.push.PushManager;
import com.txznet.music.search.SearchEngine;
import com.txznet.music.soundControlModule.asr.AsrManager;

/**
 * Created by ASUS User on 2016/11/19.
 */

public class WinListener extends BroadcastReceiver {
    private static final String ACTION_SHOW = "com.txznet.txz.record.show";
    private static final String ACTION_DISMISS = "com.txznet.txz.record.dismiss";
    public static boolean isShowSoundUI = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.logd("receiver a message:" + intent.getAction());
        if (intent.getAction().equals(ACTION_SHOW)) {
            isShowSoundUI = true;
            AsrManager.getInstance().unregCMD();
            PushManager.getInstance().clickCancel();
        } else if (intent.getAction().equals(ACTION_DISMISS)) {
            isShowSoundUI = false;
            AsrManager.getInstance().regCMD();
            SearchEngine.getInstance().cancelSearch();
            AsrManager.getInstance().cancelRequest();
            PushManager.getInstance().processPushListDelay();
        }
    }
}
