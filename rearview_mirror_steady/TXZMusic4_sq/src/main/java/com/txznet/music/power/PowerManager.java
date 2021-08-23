package com.txznet.music.power;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brainBear on 2017/10/31.
 * 用于管理休眠和倒车相关的命令字
 */

public class PowerManager {

    private static final String TAG = "PowerManager:";
    private static PowerManager sInstance;
    private List<PowerChangedListener> mListeners;
    private boolean isReversing = false;

    private PowerManager() {
        mListeners = new ArrayList<>();
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


    public void addPowerChangedListener(final PowerChangedListener listener) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (!mListeners.contains(listener)) {
                    mListeners.add(listener);
                }
            }
        });
    }

    public void removePowerChangedListener(final PowerChangedListener listener) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                mListeners.remove(listener);
            }
        });
    }


    public void notifyWakeUp() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "notify wake up");
                for (PowerChangedListener listener : mListeners) {
                    listener.onWakeUp();
                }
            }
        });
    }


    public void notifySleep() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "notify sleep");
                for (PowerChangedListener listener : mListeners) {
                    listener.onSleep();
                }
            }
        });
    }

    public void notifyExit() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "notify exit");
                for (PowerChangedListener listener : mListeners) {
                    listener.onExit();
                }
            }
        });
    }

    public void notifyReverseStart() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                isReversing = true;
                LogUtil.d(TAG, "reverse start");
                for (PowerChangedListener listener : mListeners) {
                    listener.onReverseStart();
                }
            }
        });
    }

    public void notifyReverseEnd() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                isReversing = false;
                LogUtil.d(TAG, "notify reverse end");
                for (PowerChangedListener listener : mListeners) {
                    listener.onReverseEnd();
                }
            }
        });
    }

    public boolean isReversing() {
        return isReversing;
    }

}
