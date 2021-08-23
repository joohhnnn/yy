package com.txznet.webchat.util;

import android.support.annotation.Nullable;

import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZJinshouzhiManager;

import java.util.List;

/**
 * 金手指Manager
 *
 * 处理微信需要和金手指联动的场景
 *
 * Created by J on 2018/10/15.
 */

public class WxHelpGuideManager {
    /**
     * 金手指场景
     */
    public enum GuideScene {
        /**
         * 收到消息
         */
        RECEIVE_MESSAGE,
        /**
         * 录音
         */
        RECORD,
        /**
         * 微信切换到前台
         */
        FOREGROUND,
    }

    public void showHelpGuide(GuideScene scene, @Nullable List<String> kws) {
        switch (scene) {
            case FOREGROUND:
                TXZJinshouzhiManager.getInstance().wxForeSceneShowTips(true);
                break;

            case RECEIVE_MESSAGE:
                TXZJinshouzhiManager.getInstance().wxGetWXInfoSceneShowTips(kws);
                break;

            case RECORD:
                TXZJinshouzhiManager.getInstance().wxRecordingSceneShowTips(kws);
                break;
        }
    }

    public void hideHelpGuide(GuideScene scene) {
        TXZJinshouzhiManager.getInstance().closeWindow();

        // 取消显示时, 若微信在前台, 重新刷新下默认的前台提示
        if (AppLogic.isForeground()) {
            TXZJinshouzhiManager.getInstance().wxForeSceneShowTips(false);
        }
    }

    //----------- single instance -----------
    private static volatile WxHelpGuideManager sInstance;

    public static WxHelpGuideManager getInstance() {
        if (null == sInstance) {
            synchronized (WxHelpGuideManager.class) {
                if (null == sInstance) {
                    sInstance = new WxHelpGuideManager();
                }
            }
        }

        return sInstance;
    }

    private WxHelpGuideManager() {

    }
    //----------- single instance -----------
}
