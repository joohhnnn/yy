package com.txznet.webchat.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.comm.base.ActivityStack;
import com.txznet.loader.AppLogic;

public class WxStatusReceiver extends BroadcastReceiver {
    private static final String ACTION_CLOSE = "com.txznet.webchat.action.CLOSE_APP";

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getAction();
        if (msg.equals(ACTION_CLOSE)) {
            ActivityStack.getInstance().exit();
        }
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }, 1500);
    }
}
