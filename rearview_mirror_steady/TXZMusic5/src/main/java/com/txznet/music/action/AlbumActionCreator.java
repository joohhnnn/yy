package com.txznet.music.action;

import com.txznet.music.Constant;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

/**
 * @author telen
 * @date 2019/1/14,14:16
 */
public class AlbumActionCreator {

    /**
     * 单例对象
     */
    private volatile static AlbumActionCreator singleton;

    private AlbumActionCreator() {
    }

    public static AlbumActionCreator getInstance() {
        if (singleton == null) {
            synchronized (AlbumActionCreator.class) {
                if (singleton == null) {
                    singleton = new AlbumActionCreator();
                }
            }
        }
        return singleton;
    }


    /**
     * 获取专辑数据
     *
     * @param pageIndex  上下拉需要
     * @param categoryId 分类id
     */
    public void getAlbumByCategory(Operation operation, int pageIndex, int sid, long categoryId) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_ALBUM_EVENT_GET_FROM_CATEGORY)
                .bundle(Constant.AlbumConstant.KEY_ALBUM_SID, sid)
                .bundle(Constant.AlbumConstant.KEY_ALBUM_CATEGORY_ID, categoryId)
                .bundle(Constant.AlbumConstant.KEY_ALBUM_PAGE_INDEX, pageIndex).operation(operation).build());
    }

    /**
     * 获取专辑数据
     *
     * @param pageIndex  上下拉需要
     * @param categoryId 分类id
     */
    public void getAlbumByCategory(Operation operation, int pageIndex, int sid, long categoryId, int offset) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_ALBUM_EVENT_GET_FROM_CATEGORY)
                .bundle(Constant.AlbumConstant.KEY_ALBUM_SID, sid)
                .bundle(Constant.AlbumConstant.KEY_ALBUM_CATEGORY_ID, categoryId)
                .bundle(Constant.AlbumConstant.KEY_ALBUM_PAGE_OFFSET, offset)
                .bundle(Constant.AlbumConstant.KEY_ALBUM_PAGE_INDEX, pageIndex).operation(operation).build());
    }
}
