package com.txznet.launcher.module.notification;

import com.txznet.launcher.module.IModule;

/**
 * Created by zackzhou on 2018/3/24.
 */

public interface INoticeModule extends IModule {

    // 播报描述tts
    void playNotice(int order, Runnable ttsCallback);
}
