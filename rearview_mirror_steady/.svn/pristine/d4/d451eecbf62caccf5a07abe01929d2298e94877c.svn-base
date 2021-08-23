package com.txznet.music.action;

import com.txznet.music.Constant;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

/**
 * 收藏
 *
 * @author telen
 * @date 2018/12/4,17:17
 */
public class FavourActionCreator {


    /**
     * 单例对象
     */
    private volatile static FavourActionCreator singleton;

    private FavourActionCreator() {
    }

    public static FavourActionCreator getInstance() {
        if (singleton == null) {
            synchronized (FavourActionCreator.class) {
                if (singleton == null) {
                    singleton = new FavourActionCreator();
                }
            }
        }
        return singleton;
    }

    public void unFavour(Operation operation, FavourAudio audio, String channel) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_FAVOUR_EVENT_UNFAVOUR)
                .bundle(Constant.FavourConstant.KEY_FAVOUR_AUDIO, audio)
                .bundle(Constant.FavourConstant.KEY_FAVOUR_CHANNEL, channel)
                .operation(operation).build());
    }

    public void favour(Operation operation, FavourAudio audio, String channel) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_FAVOUR_EVENT_FAVOUR)
                .bundle(Constant.FavourConstant.KEY_FAVOUR_AUDIO, audio)
                .bundle(Constant.FavourConstant.KEY_FAVOUR_CHANNEL, channel)
                .operation(operation).build());
    }

    public void getData(Operation operation, FavourAudio audio) {

        RxAction.Builder builder = RxAction.type(ActionType.ACTION_FAVOUR_EVENT_GET).operation(operation);
        if (audio != null) {
            builder.bundle(Constant.FavourConstant.KEY_FAVOUR_AUDIO, audio);
        }
        Dispatcher.get().postAction(builder.build());
    }
}
