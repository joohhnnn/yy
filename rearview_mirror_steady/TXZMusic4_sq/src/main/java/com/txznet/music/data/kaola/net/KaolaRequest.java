package com.txznet.music.data.kaola.net;

import com.txznet.music.data.kaola.net.bean.KaolaAlbum;
import com.txznet.music.data.kaola.net.bean.KaolaCategory;
import com.txznet.music.data.kaola.net.bean.RespActive;
import com.txznet.music.data.kaola.net.bean.KaolaAudio;
import com.txznet.music.data.kaola.net.bean.RespParent;
import com.txznet.music.data.kaola.net.bean.RespItem;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by telenewbie on 2018/2/5.
 */

public interface KaolaRequest {
    //考拉后台不支持,post在body里面传递参数的形式,特此作为备注
//    @POST("app/active")
//    Call<RespTestActive> active(@Body ReqActive reqActive);

    @POST("app/active")
    Observable<RespParent<RespActive>> active(@QueryMap Map<String, String> param);


    @GET("app/init")
    Observable<RespParent<RespActive>> init(
            @QueryMap Map<String, String> param
    );

//    @GET("resource/searchall")
//    Observable<RespParent<List<RespItem>>> searchAll(@QueryMap Map<String, String> param);

    @GET("resource/searchtype")
    Observable<RespParent<RespItem<KaolaAlbum>>> searchByType(@QueryMap Map<String, String> param);

    @GET("audio/list")
    Observable<RespParent<RespItem<KaolaAudio>>> getAudios(@QueryMap Map<String, String> param);

    @GET("category")
    Observable<RespParent<List<KaolaCategory>>> getCategorys(@QueryMap Map<String, String> param);

    @GET("category/sublist")
    Observable<RespParent<List<KaolaCategory>>> getSubCategorys(@QueryMap Map<String, String> param);

    @GET("album/list")
    Observable<RespParent<RespItem<KaolaAlbum>>> getAlbums(@QueryMap Map<String, String> param);


}