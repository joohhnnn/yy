package com.txznet.music.data.netease;

import android.util.TimeUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.data.netease.net.NeteaseRequest;
import com.txznet.music.data.netease.net.bean.NeteaseUrl;
import com.txznet.music.data.netease.net.bean.RespRecommandSong;
import com.txznet.music.data.netease.net.bean.RespSearch;
import com.txznet.music.data.utils.DeviceInfo;
import com.txznet.music.data.utils.OnGetData;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by telenewbie on 2018/2/8.
 */

public class NeteaseSDK {


    //##创建一个单例类##
    private volatile static NeteaseSDK singleton;
    //    private final static String TEST_URL = "http://api.igame.163.com/openapi/";
    private final static String NORMAL_URL = "http://api.music.163.com/openapi/";
    Retrofit retrofit = null;
    NeteaseRequest neteaseRequest = null;

    private int bitrate = 128;//使用

    private NeteaseSDK() {


        //声明日志类
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//设定日志级别
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


//自定义OkHttpClient
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
//添加拦截器
        okHttpClient.addInterceptor(httpLoggingInterceptor);
        okHttpClient.connectTimeout(1000 * 3, TimeUnit.SECONDS);
        okHttpClient.readTimeout(1000 * 3, TimeUnit.SECONDS);

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.build())
                .baseUrl(NORMAL_URL).build();

        neteaseRequest = retrofit.create(NeteaseRequest.class);
    }

    public static NeteaseSDK getInstance() {
        if (singleton == null) {
            synchronized (NeteaseSDK.class) {
                if (singleton == null) {
                    singleton = new NeteaseSDK();
                }
            }
        }
        return singleton;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public void getSearch(String keyword, final OnGetData<RespSearch> onGetData) {


        int type = 1;
        int limit = 20;
        int offset = 0;

        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("type", String.valueOf(type));
        params.put("limit", String.valueOf(limit));
        params.put("offset", String.valueOf(offset));


        neteaseRequest.search(params).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<RespSearch>() {
            @Override
            public void accept(RespSearch respSearch) throws Exception {
                if (null != onGetData) {
                    onGetData.success(respSearch);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (null != onGetData) {
                    onGetData.failed(-2);
                }
            }
        });

    }


    /**
     * 切忌,所有的网易请求数据的url都需要带上header
     *
     * @param songId
     * @param bitrate
     * @return
     * @ channel: gm
     */
    public void getUrl(final String songId, final int bitrate, final OnGetData<NeteaseUrl> onGetData) {


        Map<String, String> params = new HashMap<>();
        params.put("songId", songId);
        params.put("bitrate", String.valueOf(bitrate));
        neteaseRequest.getUrl(params).retry(3, new Predicate<Throwable>() {
            @Override
            public boolean test(Throwable throwable) throws Exception {
                Thread.sleep(3000);
                return (throwable instanceof UnknownHostException);
            }
        }).subscribeOn(Schedulers.io())/*.observeOn(AndroidSchedulers.mainThread())*/.subscribe(new Consumer<NeteaseUrl>() {
            @Override
            public void accept(NeteaseUrl neteaseUrl) throws Exception {
                if (neteaseUrl.getCode() != 200) {
                    if (onGetData != null) {
                        onGetData.failed(-1);
                    }
                    return;
                }
                if (onGetData != null) {
                    onGetData.success(neteaseUrl);
                }


            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (onGetData != null) {
                    onGetData.failed(-2);
                }

            }
        });
    }


    public void getRecommandSongs(int limit, boolean withMediaUrl, final OnGetData<RespRecommandSong> onGetData) {
        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(limit));
        params.put("withMediaUrl", String.valueOf(withMediaUrl));
        params.put("deviceId", DeviceInfo.getDeviceSerialNumber(GlobalContext.get()));

        neteaseRequest.getRecommand(params).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<RespRecommandSong>() {
            @Override
            public void accept(RespRecommandSong respRecommandSong) throws Exception {
                if (respRecommandSong.getCode() != 200) {
                    if (onGetData != null) {
                        onGetData.failed(respRecommandSong.getCode());
                    }
                    return;
                }
                if (onGetData != null) {
                    onGetData.success(respRecommandSong);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (onGetData != null) {
                    onGetData.failed(-2);
                }
            }
        });
    }
}
