package com.txznet.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.comm.remote.util.LogUtil;

/**
 * Created by telenewbie on 2017/6/3.
 */

public class ShowAppReceiver extends BroadcastReceiver {
    public static final String INTENT_SET_MOVE = "com.txznet.music.move";
    public static final String INTENT_SET_SHOW = "com.txznet.music.show";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.logd("music:receiver:"+intent.getAction());
        if (INTENT_SET_SHOW.equals(intent.getAction())) {
//            GlobalContext.get().startActivity(new Intent(GlobalContext.get(), SplashActivity.class));
        } else if (INTENT_SET_MOVE.equals(intent.getAction())) {
//            ScreenUtils.updateScreenSize(ShowSize.getInstance().width,ShowSize.getInstance().height,true);
        }
    }
}
