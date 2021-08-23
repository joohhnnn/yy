package com.txznet.music.data.netease.net;

import com.txznet.music.data.netease.net.bean.NeteaseUrl;
import com.txznet.music.data.netease.net.bean.RespRecommandSong;
import com.txznet.music.data.netease.net.bean.RespSearch;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by telenewbie on 2018/2/8.
 */

public interface NeteaseRequest {

    @Headers({
            "channel: gm"
    })
    @POST("search/v2")
    Observable<RespSearch> search(@QueryMap Map<String, String> search);

    @Headers({
            "channel: gm"
    })
    @POST("song/playurl")
    Observable<NeteaseUrl> getUrl(@QueryMap Map<String, String> param);

    /**
     * limit	Integer	获得推荐歌曲数量，默认20
     * withMediaUrl	Boolean	是否返回播放地址，默认false
     * 需要用户信息
     */
    @Headers({
            "channel: gm"
    })
    @POST("recommend/everyday/songs")
    Observable<RespRecommandSong> getRecommand(@QueryMap Map<String, String> param);
}
