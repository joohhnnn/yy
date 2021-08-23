package com.txznet.music.playerModule.logic.focus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.service.MediaPlaybackService;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import javax.microedition.khronos.opengles.GL;

/**
 * 耳机线控管理助手类 单例
 *
 * @author telenewbi
 */
public class HeadSetHelper {

    private static final String TAG = "Music:HeadSetHelper:";
    private static HeadSetHelper headSetHelper;
    private static boolean isRegister = false;

    Intent intent;

    public static HeadSetHelper getInstance() {
        if (headSetHelper == null) {
            headSetHelper = new HeadSetHelper();
        }
        return headSetHelper;
    }

    MyBroadcast myBroadcast;

    public HeadSetHelper() {

    }

    public boolean getIsRegister() {
        return isRegister;
    }

    public void setIsRegister(boolean isRegister) {
        HeadSetHelper.isRegister = isRegister;
    }

    /**
     * 开启耳机线控监听, 请务必在设置接口监听之后再调用此方法，否则接口无效
     *
     * @param context
     */
    @SuppressLint("NewApi")
    public void open(final Context context) {
        LogUtil.d(TAG, "open:" + FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MEDIA_BUTTON, 1) + ",need registerMediabuttom:" + (!isRegister));
        if (isRegister) {
            return;
        }
        isRegister = true;
        if (FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MEDIA_BUTTON, 1) == 1) {
            if (intent == null) {
                intent = new Intent(context, MediaPlaybackService.class);
                intent.setAction(Intent.ACTION_MEDIA_BUTTON);
                intent.setAction("android.media.browse.MediaBrowserService");
            }
            context.startService(intent);

        } else if (FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MEDIA_BUTTON, 1) == 2) {
            if (myBroadcast == null) {
                myBroadcast = new MyBroadcast();
            }

            IntentFilter mediaButtonFilter = new IntentFilter();
            mediaButtonFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
            GlobalContext.get().registerReceiver(myBroadcast, mediaButtonFilter);
        }

    }

    /**
     * 关闭耳机线控监听
     *
     * @param context
     */
    public void close(Context context) {
        LogUtil.d(TAG, "close" + ",need unregisterMedia:" + isRegister);
        if (!isRegister) {
            return;
        }
        isRegister = false;

        if (FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MEDIA_BUTTON, 1) == 1) {
            if (intent != null) {
                context.stopService(intent);
            }
        } else if (FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MEDIA_BUTTON, 1) == 2) {
            if (myBroadcast != null) {
                GlobalContext.get().unregisterReceiver(myBroadcast);
            }
        }
    }

}
