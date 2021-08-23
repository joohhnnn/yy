package com.txznet.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.Operation;

/**
 * 桌面控件按钮控制电台之家
 *
 * @author telenewbie
 */
public class WidgetListener extends BroadcastReceiver {
    public static final String TAG = "[Listener] ";
    public static final String LISTENER = "com.txznet.music.operator";
    public static final String OPERATOR = "operator";
    public static final String PLAY = "PLAY";
    public static final String PAUSE = "PAUSE";
    public static final String NEXT = "NEXT";
    public static final String PREV = "PREV";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LISTENER.equals(intent.getAction())) {// 过滤自己需要的
            String operator = intent.getStringExtra(OPERATOR);
            Logger.d(TAG, "receiver=" + intent.getAction() + ":" + operator);
            if (PLAY.equals(operator)) {
                PlayerActionCreator.get().play(Operation.SDK);
            } else if (PAUSE.equals(operator)) {
                PlayerActionCreator.get().pause(Operation.SDK);
            } else if (NEXT.equals(operator)) {
                PlayerActionCreator.get().next(Operation.SDK);
            } else if (PREV.equals(operator)) {
                PlayerActionCreator.get().prev(Operation.SDK);
            }
        }
    }
}
