package com.txznet.webchat;

/**
 * App状态缓存
 * Created by J on 2017/10/31.
 */

public class AppStatus {
    private boolean bReverseMode = false;

    private static AppStatus sInstance;

    public static AppStatus getInstance() {
        if (null == sInstance) {
            synchronized (AppStatus.class) {
                if (null == sInstance) {
                    sInstance = new AppStatus();
                }
            }
        }

        return sInstance;
    }

    private AppStatus() {

    }

    public boolean isReverseMode() {
        return bReverseMode;
    }

    public void setReverseMode(boolean bReverseMode) {
        this.bReverseMode = bReverseMode;
    }
}
