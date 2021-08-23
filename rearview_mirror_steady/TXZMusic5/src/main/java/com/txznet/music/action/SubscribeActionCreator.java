package com.txznet.music.action;

import com.txznet.music.Constant;
import com.txznet.music.data.entity.SubscribeAlbum;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

/**
 * 订阅
 *
 * @author telen
 * @date 2018/12/4,17:17
 */
public class SubscribeActionCreator {

    /**
     * 单例对象
     */
    private volatile static SubscribeActionCreator singleton;

    private SubscribeActionCreator() {
    }

    public static SubscribeActionCreator getInstance() {
        if (singleton == null) {
            synchronized (SubscribeActionCreator.class) {
                if (singleton == null) {
                    singleton = new SubscribeActionCreator();
                }
            }
        }
        return singleton;
    }

    public void unSubscribe(Operation operation, SubscribeAlbum album, String channel) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SUBSCRIBE_EVENT_UNSUBSCRIBE).operation(operation).bundle(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM, album)
                .bundle(Constant.SubscribeConstant.KEY_CHANNEL, channel)
                .build());
//        ArrayList<SubscribeAlbum> subscribeAlbums = new ArrayList<>();
//        subscribeAlbums.add(album);
//        unSubscribe(operation, subscribeAlbums);
    }

//    public void unSubscribe(Operation operation, List<SubscribeAlbum> album) {
//        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SUBSCRIBE_EVENT_UNSUBSCRIBE).operation(operation).bundle(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUMS, album).build());
//    }

    public void subscribe(Operation operation, SubscribeAlbum album, String channel) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SUBSCRIBE_EVENT_SUBSCRIBE).operation(operation).bundle(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM, album)
                .bundle(Constant.SubscribeConstant.KEY_CHANNEL, channel)
                .build());
//        ArrayList<SubscribeAlbum> subscribeAlbums = new ArrayList<>();
//        subscribeAlbums.add(album);
//        subscribe(operation, subscribeAlbums);
    }

//    public void subscribe(Operation operation, List<SubscribeAlbum> album) {
//        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SUBSCRIBE_EVENT_SUBSCRIBE).operation(operation).bundle(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUMS, album).build());
//    }

    public void getSubscribeData(Operation operation, SubscribeAlbum album) {
        RxAction.Builder builder = RxAction.type(ActionType.ACTION_SUBSCRIBE_EVENT_GET).operation(operation);
        if (album != null) {
            builder.bundle(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM, album);
        }
        Dispatcher.get().postAction(builder.build());

    }

}
