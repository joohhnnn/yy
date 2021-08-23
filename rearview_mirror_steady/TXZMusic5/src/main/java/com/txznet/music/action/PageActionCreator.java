package com.txznet.music.action;

import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.RxAction;

/**
 * 主页行为创建
 *
 * @author zackzhou
 * @date 2018/12/30,11:39
 */

public class PageActionCreator {
    private static PageActionCreator sInstance = new PageActionCreator();

    private PageActionCreator() {
    }

    public static PageActionCreator get() {
        return sInstance;
    }

    /**
     * 获取推荐页面数据
     */
    public void getRecommendPage() {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_HOME_GET_RECOMMEND_PAGE_DATA).build());
    }

    /**
     * 获取音乐页面数据
     */
    public void getMusicPage() {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_HOME_GET_MUSIC_PAGE_DATA).build());
    }

    /**
     * 获取电台页面数据
     */
    public void getRadioPage() {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_HOME_GET_RADIO_PAGE_DATA).build());
    }
}
