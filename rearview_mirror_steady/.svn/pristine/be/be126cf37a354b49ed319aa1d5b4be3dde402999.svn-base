package com.txznet.music.action;

import com.txznet.music.Constant;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

/**
 * 声控指令统一处理的位置
 *
 * @author telen
 * @date 2018/12/7,11:48
 */
public class SoundCommandActionCreator {


    /**
     * 单例对象
     */
    private volatile static SoundCommandActionCreator singleton;

    private SoundCommandActionCreator() {
    }

    public static SoundCommandActionCreator getInstance() {
        if (singleton == null) {
            synchronized (SoundCommandActionCreator.class) {
                if (singleton == null) {
                    singleton = new SoundCommandActionCreator();
                }
            }
        }
        return singleton;
    }

    /**
     * 收藏或订阅当前播放的内容，可能是电台，可能是音乐哦
     *
     * @param operation
     */
    public void favourOrSubscribe(Operation operation, boolean isWakeup) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_COMMAND_FAVOUR_SUBSCRIBE)
                .bundle(Constant.FavourConstant.KEY_FAVOUR_IS_WAKEUP, isWakeup)
                .operation(operation).build());
    }


    /**
     * 取消收藏或取消订阅当前播放的内容，可能是电台，可能是音乐哦
     *
     * @param operation
     */
    public void unfavourOrUnSubscribe(Operation operation, boolean isWakeup) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_COMMAND_UNFAVOUR_UNSUBSCRIBE)
                .bundle(Constant.FavourConstant.KEY_FAVOUR_IS_WAKEUP, isWakeup)
                .operation(operation).build());
    }


}
