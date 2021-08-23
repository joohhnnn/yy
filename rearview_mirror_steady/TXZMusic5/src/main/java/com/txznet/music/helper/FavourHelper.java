package com.txznet.music.helper;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.entity.SubscribeAlbum;

/**
 * 收藏工具
 *
 * @author zackzhou
 * @date 2018/12/26,11:50
 */

public class FavourHelper {

    private FavourHelper() {

    }

    public static boolean isFavour(long id, int sid) {
        FavourAudio favourAudio = DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao().get(id, sid);
        return favourAudio != null;
    }

    public static boolean isSubscribe(long id, int sid) {
        SubscribeAlbum subscribeAlbum = DBUtils.getDatabase(GlobalContext.get()).getSubscribeAlbumDao().get(id, sid);
        return subscribeAlbum != null;
    }
}
