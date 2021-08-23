package com.txznet.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.RxAction;

/**
 * 监听设备TF插拔事件
 *
 * @author zackzhou
 * @date 2018/12/25,10:36
 */

public class MediaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        Logger.d(Constant.LOG_TAG_APPLICATION, "MediaReceiver receive action=" + intent.getAction());
        switch (intent.getAction()) {
            case Intent.ACTION_MEDIA_MOUNTED: // 插入，且挂载完毕
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_MEDIA_SCANNER_STARTED).build());
                break;
            case Intent.ACTION_MEDIA_EJECT: // 拔出
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_MEDIA_EJECT).build());
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_MEDIA_SCANNER_STARTED).build());
                break;
            case Intent.ACTION_MEDIA_SCANNER_STARTED:
                break;
            default:
                break;
        }
    }


    public static void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_NOFS);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction("android.intent.action.MEDIA_UNSHARED");
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        filter.addDataScheme("file");
        GlobalContext.get().registerReceiver(new MediaReceiver(), filter);
        Logger.d(Constant.LOG_TAG_APPLICATION, "register MediaReceiver");
    }
}
