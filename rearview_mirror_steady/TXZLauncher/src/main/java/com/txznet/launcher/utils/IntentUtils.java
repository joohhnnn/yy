package com.txznet.launcher.utils;

import android.content.Intent;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;

/**
 * Created by daviddai on 2018/10/19
 * 发送Intent的工具类，主要是将发送的内容打印出来。
 */
public class IntentUtils {

    public static void sendBroadcast (Intent intent){
        LogUtil.logi("send broadcast intent: "+intent);
        LogUtil.logi("send broadcast intent extras: "+intent.getExtras());
        GlobalContext.get().sendBroadcast(intent);
    }
}
