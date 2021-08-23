package com.txznet.music.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.txznet.music.Constant;
import com.txznet.music.ui.MediaPlayerActivity;

/**
 * Created by ASUS User on 2016/9/20.
 */

public class JumpUtils {
    /**
     * 从哪个界面跳转到该界面
     *
     * @param source
     *            进入的来源
     * @param ctx
     */
    public static void jumpFrom(Context ctx, Class<? extends  Activity> toClass , int source) {
        Intent intent = new Intent(ctx, toClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.SOURCE, source);
        ctx.startActivity(intent);// 跳转到播放器页面
    }
}
