package com.txznet.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.util.Logger;

/**
 * 退出应用的广播接收器
 */
public class ExitAppReceiver extends BroadcastReceiver {
    public final String ACTION_CLOSE_APP = "com.txznet.music.close.app";

    @Override
    public void onReceive(Context arg0, Intent intent) {
        Logger.d(Constant.LOG_TAG_APPLICATION, "onReceive " + ACTION_CLOSE_APP);
        if (ACTION_CLOSE_APP.equals(intent.getAction())) {
            AppLogic.getInstance().destroy();
        }
    }
}
