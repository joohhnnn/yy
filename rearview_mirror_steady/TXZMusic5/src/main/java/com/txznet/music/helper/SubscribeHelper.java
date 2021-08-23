package com.txznet.music.helper;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.SubscribeAlbum;

/**
 * 订阅的帮助类
 *
 * @author telen
 * @date 2018/12/6,9:55
 */
public class SubscribeHelper {

    /**
     * 数据库中是否订阅
     *
     * @param album 需要判断的对象
     * @return 数据库中的对象
     */
    public static <T extends Album> SubscribeAlbum isSubscribe(T album) {
        return DBUtils.getDatabase(GlobalContext.get()).getSubscribeAlbumDao().get(album.id, album.sid);
    }

    /**
     * 保存数据到数据库中
     *
     * @param album 待保存的album
     * @param type  操作类型
     */
    public static void save2SubscribeDB(SubscribeAlbum album, int type) {
        DBUtils.getDatabase(GlobalContext.get()).getSubscribeAlbumDao().saveOrUpdate(album);

        // TODO: 2018/12/6 删除待发送中的数据?

    }


}
