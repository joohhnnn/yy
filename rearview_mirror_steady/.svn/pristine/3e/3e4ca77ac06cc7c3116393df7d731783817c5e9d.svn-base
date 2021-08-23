package com.txznet.music.data.http.api.txz.entity.req;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 获取收藏或订阅的数据
 *
 * @author telen
 * @date 2018/12/4,20:40
 */
public class TXZReqFavour extends TXZReqBase {
    public final static int AUDIO_TYPE = 1; //音乐， 收藏
    public final static int ALBUM_TYPE = 2;  //专辑，订阅

    @IntDef({
            AUDIO_TYPE,
            ALBUM_TYPE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface StoreType {
    }

    public TXZReqFavour(@StoreType int storeType) {
        this.storeType = storeType;
    }


    public int storeType;  //1.
    public int sid;
    public long id;    //音频id, 或专辑id， 0：从头开始
    public int count;  //拉取数据(默认十条)
    public long operTime;//请求的时间戳(第一次为0,其他的时候先是本地最后一条的时间戳)


}
