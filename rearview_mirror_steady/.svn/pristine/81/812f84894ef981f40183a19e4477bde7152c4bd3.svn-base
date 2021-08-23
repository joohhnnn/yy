package com.txznet.music.power;

import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.action.PowerActionCreator;
import com.txznet.music.util.Logger;

/**
 * 用于管理休眠和倒车相关的命令字
 *
 * @author telen
 * @date 2018/12/28,11:46
 */
public class PowerManager {

    private static final String TAG = Constant.LOG_TAG_POWER;
    private static PowerManager sInstance;
    private boolean isReversing = false;
    private boolean isSleeping = false;

    private PowerManager() {

    }

    public static PowerManager getInstance() {
        if (null == sInstance) {
            synchronized (PowerManager.class) {
                if (null == sInstance) {
                    sInstance = new PowerManager();
                }
            }
        }
        return sInstance;
    }


    public void notifyWakeUp() {
        AppLogic.runOnUiGround(() -> {
            isSleeping = false;
            Logger.d(TAG, "notify wakeup");
            PowerActionCreator.getInstance().notifyWakeup();
        });
    }


    public void notifySleep() {
        AppLogic.runOnUiGround(() -> {
            isSleeping = true;
            Logger.d(TAG, "notify sleep");
            PowerActionCreator.getInstance().notifySleep();
        });
    }

    public void notifyExit() {
        AppLogic.runOnUiGround(() -> Logger.d(TAG, "notify exit"));
    }

    public void notifyReverseStart() {
        AppLogic.runOnUiGround(() -> {
            isReversing = true;
            Logger.d(TAG, "reverse start");
            PowerActionCreator.getInstance().notifyEnterReverse();
        });
    }

    public void notifyReverseEnd() {
        AppLogic.runOnUiGround(() -> {
            isReversing = false;
            Logger.d(TAG, "notify reverse end");
            PowerActionCreator.getInstance().notifyExitReverse();
        });
    }

    public boolean isReversing() {
        return isReversing;
    }

    public boolean isSleeping() {
        return isSleeping;
    }
}
