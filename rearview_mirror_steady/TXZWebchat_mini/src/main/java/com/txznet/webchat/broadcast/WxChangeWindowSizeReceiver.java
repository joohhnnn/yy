package com.txznet.webchat.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.webchat.actions.ConfigActionCreator;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.WxUIConfig;

/**
 * 更改窗口大小和显示位置的BroadcastReceiver
 * Created by J on 2017/5/26.
 */

public class WxChangeWindowSizeReceiver extends BroadcastReceiver {
    /**
     * 窗口显示参数变化
     */
    public static final String ACTION_CHANGE_WINDOW_SIZE = "com.txznet.webchat.show.size";
    /**
     * 隐藏界面
     */
    public static final String ACTION_HIDE = "com.txznet.webchat.hide";

    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_GRAVITY = "gravity";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_CHANGE_WINDOW_SIZE.equals(intent.getAction())) {
            WxUIConfig config = new WxUIConfig();
            config.x = intent.getIntExtra(KEY_X, 0);
            config.y = intent.getIntExtra(KEY_Y, 0);
            config.width = intent.getIntExtra(KEY_WIDTH, 0);
            config.height = intent.getIntExtra(KEY_HEIGHT, 0);
            config.gravity = intent.getIntExtra(KEY_GRAVITY, Gravity.TOP | Gravity.LEFT);

            L.d("WxChangeWindowSizeReceiver", "show.size triggered: " + config.toString());
            ScreenUtils.updateScreenSize(config.width, config.height, true);
            ConfigActionCreator.getInstance().changeUILayoutConfig(config);
        } else if (ACTION_HIDE.equals(intent.getAction())) {
            L.d("WxChangeWindowSizeReceiver", "hide triggered");
            ActivityStack.getInstance().currentActivity().moveTaskToBack(true);
        }
    }
}
