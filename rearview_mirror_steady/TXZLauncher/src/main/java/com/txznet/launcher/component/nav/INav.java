package com.txznet.launcher.component.nav;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 * 导航相关接口，定义了导航是否被focus、是否在导航中、是否在后台等状态判断方法。
 */

public interface INav {
    void init();

    boolean isFocus();

    boolean isInNav();

    boolean isBackgroundRunning();
}