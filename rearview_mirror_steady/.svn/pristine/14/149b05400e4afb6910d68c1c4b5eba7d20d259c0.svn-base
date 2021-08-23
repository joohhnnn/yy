package com.txznet.launcher.module;

import com.txznet.launcher.event.BaseEventTool;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 * 做一层包装
 * Module的基类，将事件分发包装包module里面
 */

public abstract class BaseModule extends BaseEventTool implements IModule {

    protected boolean isNavInFocusBeforeDisplay; // 显示前是否处于导航中

    @Override
    public void onCreate(String data) {
        register();
    }

    @Override
    public void refreshView(String data) {
    }

    @Override
    public void onDestroy() {
        unRegister();
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPreRemove() {
    }

    @Override
    public void notifyIsNavInFocusBeforeDisplay(boolean isInFocus) {
        isNavInFocusBeforeDisplay = isInFocus;
    }
}