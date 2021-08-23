package com.txznet.music.action;

import com.txznet.music.Constant;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

/**
 * @author telen
 * @date 2018/12/17,17:37
 */
public class SettingActionCreator {

    /**
     * 单例对象
     */
    private volatile static SettingActionCreator singleton;

    private SettingActionCreator() {
    }

    public static SettingActionCreator getInstance() {
        if (singleton == null) {
            synchronized (SettingActionCreator.class) {
                if (singleton == null) {
                    singleton = new SettingActionCreator();
                }
            }
        }
        return singleton;
    }


    /**
     * 点击智能播放按钮
     */
    public void clickBootPlay(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SETTING_CLICK_BOOT_PLAY).operation(operation).build());
    }

    /**
     * 改变播放悬浮窗的设置
     *
     * @param type 一共有三类(需求文档上面)
     */
    public void clickChangeFloatSetting(Operation operation, int type) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SETTING_CLICK_CHANGE_FLOAT_PLAYER).operation(operation).bundle(Constant.SettingConstant.KEY_CHANGE_FLOAT_STYPE, type).build());
    }


    /**
     * 点击"开启免唤醒指令"按钮
     */
    public void clickAsr(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SETTING_CLICK_OPEN_ASR).operation(operation).build());

    }

    /**
     * 点击清空缓存
     */
    public void clickClearMemory(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SETTING_CLICK_CLEAR_MEMORY).operation(operation).build());

    }

    /**
     * 点击帮助
     */
    public void clickHelp(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SETTING_CLICK_HELP).operation(operation).build());

    }


}
