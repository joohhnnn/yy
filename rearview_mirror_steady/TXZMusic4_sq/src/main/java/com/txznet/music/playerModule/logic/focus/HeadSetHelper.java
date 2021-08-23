package com.txznet.music.playerModule.logic.focus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.service.MediaPlaybackService;

/**
 * 耳机线控管理助手类 单例
 *
 * @author telenewbi
 */
public class HeadSetHelper {

    private static final String TAG = "Music:HeadSetHelper:";
    private static HeadSetHelper headSetHelper;

    Intent intent;

    public static HeadSetHelper getInstance() {
        if (headSetHelper == null) {
            headSetHelper = new HeadSetHelper();
        }
        return headSetHelper;
    }


    /**
     * 开启耳机线控监听, 请务必在设置接口监听之后再调用此方法，否则接口无效
     *
     * @param context
     */
    @SuppressLint("NewApi")
    public void open(final Context context) {
        LogUtil.d(TAG, "open");
        intent = new Intent(context, MediaPlaybackService.class);
        context.startService(intent);

    }

    /**
     * 关闭耳机线控监听
     *
     * @param context
     */
    public void close(Context context) {
        LogUtil.d(TAG, "close");
        if (intent != null) {
            context.stopService(intent);
            intent = null;
        }
    }

}
