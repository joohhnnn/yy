package com.txznet.music.action;

import com.txznet.music.Constant;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

/**
 * 播放队列
 *
 * @author zackzhou
 * @date 2018/12/13,16:01
 */

public class PlayQueueActionCreator {
    private static PlayQueueActionCreator sInstance = new PlayQueueActionCreator();

    private PlayQueueActionCreator() {
    }

    public static PlayQueueActionCreator get() {
        return sInstance;
    }

    public void getQueue() {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_QUEUE_GET).build());
    }

    /**
     * 加载更多
     *
     * @param isUp 是否向上加载
     */
    public void loadMore(Operation operation, boolean isUp) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_QUEUE_LOAD_MORE)
                .operation(operation)
                .bundle(Constant.PlayQueueConstant.KEY_IS_UP, isUp)
                .build());
    }
}
