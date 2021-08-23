package com.txznet.launcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 * 监听时间变化的广播，每分钟一次，广播在整分钟的时候发送。
 */

public class TimeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_TIME_CHANGE);
    }
}