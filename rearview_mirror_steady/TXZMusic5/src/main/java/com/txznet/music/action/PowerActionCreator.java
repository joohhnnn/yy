package com.txznet.music.action;

import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

public class PowerActionCreator {

    /**
     * 单例对象
     */
    private volatile static PowerActionCreator singleton;

    private PowerActionCreator() {
    }

    public static PowerActionCreator getInstance() {
        if (singleton == null) {
            synchronized (PowerActionCreator.class) {
                if (singleton == null) {
                    singleton = new PowerActionCreator();
                }
            }
        }
        return singleton;
    }

    /**
     * 唤醒
     */
    public void notifyWakeup() {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_POWER_WAKEUP).operation(Operation.ACC).build());
    }

    /**
     * 休眠
     */
    public void notifySleep() {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_POWER_SLEEP).operation(Operation.ACC).build());
    }

    /**
     * 进入倒车
     */
    public void notifyEnterReverse() {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_POWER_ENTER_REVERSE).operation(Operation.ACC).build());
    }

    /**
     * 退出倒车
     */
    public void notifyExitReverse() {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_POWER_EXIT_REVERSE).operation(Operation.ACC).build());
    }

}
