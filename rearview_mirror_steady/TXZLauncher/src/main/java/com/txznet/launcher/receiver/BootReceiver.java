package com.txznet.launcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.loader.AppLogic;

/**
 * 监听开机广播
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            LogUtil.logd("receive:" + intent.getAction());
            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_COMPLETE);
        }
    }
}
