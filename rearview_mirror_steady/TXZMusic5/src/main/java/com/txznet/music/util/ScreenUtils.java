package com.txznet.music.util;

import android.app.Activity;
import android.graphics.Rect;

import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * TODO : 长屏/短屏/竖屏判断规则
 * <p>
 * <p>
 * Created by Terry on 2017/6/14.
 */

public class ScreenUtils {

    private static final String TAG = "ScreenUtils:";

    /**
     * 后视镜
     */
    public static final int TYPE_HOUSHIJING = 1;
    /**
     * 车机
     */
    public static final int TYPE_CHEJI = 2;
    /**
     * 短屏后视镜
     */
    public static final int TYPE_HOUSHIJING_SHORT = 3;
    /**
     * 长屏后视镜
     */
    public static final int TYPE_HOUSHIJING_LONG = 4;


    private static boolean sInited = false;
    private static int sScreenType = TYPE_HOUSHIJING;

    private ScreenUtils() {
    }

    /**
     * 初始化屏幕类型，该方法会先读取配置文件，如果读取出错则会根据当前activity的大小算出屏幕类型
     *
     * @param activity    如果读取配置文件出错，则会根据这个activity算出屏幕类型
     * @param forceUpdate 是否强制更新屏幕类型
     */
    public static void initScreenType(Activity activity, boolean forceUpdate) {
        if (!sInited || forceUpdate) {
            int screenType = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_SCREEN_TYPE, -1);
            if (screenType < 0) {
                initScreenTypeByRect(activity);
            } else {
                sScreenType = screenType;
            }
            Logger.d(TAG, "screen type:" + sScreenType);
            sInited = true;
        }
    }

    public static void initScreenType(int w, int h, boolean forceUpdate) {
        if (!sInited || forceUpdate) {
            int screenType = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_SCREEN_TYPE, -1);
            if (screenType < 0) {
                initScreenTypeByRect(w, h);
            } else {
                sScreenType = screenType;
            }
            Logger.d(TAG, "screen type:" + sScreenType);
            sInited = true;
        }
    }

    public static int getScreenType() {
        if (!sInited) {
            throw new IllegalStateException("screen type is not init");
        }
        return sScreenType;
    }

    /**
     * 根据activity显示区域大小来得到屏幕类型
     *
     * @param activity
     */
    private static void initScreenTypeByRect(Activity activity) {
        Rect outRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        initScreenTypeByRect(outRect.width(), outRect.height());
    }

    private static void initScreenTypeByRect(int w, int h) {
        if (w > h && w * 1f / h > 2.4) {
            sScreenType = TYPE_HOUSHIJING_LONG;
        } else if (w > h && w * 1f / h < 1.6) {
            sScreenType = TYPE_CHEJI;
        } else if (w < 1024 && h < 500) {
            sScreenType = TYPE_HOUSHIJING_SHORT;
        } else {
            sScreenType = TYPE_HOUSHIJING;
        }
        Logger.d(TAG, "width=" + w + ", height=" + h + ", screenType=" + sScreenType);
    }
}
