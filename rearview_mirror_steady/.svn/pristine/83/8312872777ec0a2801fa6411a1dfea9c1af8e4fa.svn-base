package com.txznet.music.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.music.Constant;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.RxAction;

import static com.txznet.music.action.ActionType.ACTION_RECORD_WIN_DISMISS;
import static com.txznet.music.action.ActionType.ACTION_RECORD_WIN_SHOW;

/**
 * 声控界面状态改变时的广播接收器
 *
 * @author tongting 4.x 遗留
 */
public class WinListener extends BroadcastReceiver {
    private static final String ACTION_SHOW = "com.txznet.txz.record.show";
    private static final String ACTION_DISMISS = "com.txznet.txz.record.dismiss";
    public static boolean isShowSoundUI = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(Constant.LOG_TAG_APPLICATION, "receiver a message:" + intent.getAction());
        if (intent.getAction().equals(ACTION_SHOW)) {
            isShowSoundUI = true;
            Dispatcher.get().postAction(RxAction.type(ACTION_RECORD_WIN_SHOW).build());
        } else if (intent.getAction().equals(ACTION_DISMISS)) {
            isShowSoundUI = false;
            Dispatcher.get().postAction(RxAction.type(ACTION_RECORD_WIN_DISMISS).build());
        }
    }
}
