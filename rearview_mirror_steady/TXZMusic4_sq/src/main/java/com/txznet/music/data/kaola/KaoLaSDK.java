package com.txznet.music.data.kaola;

import android.content.Context;
import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.data.kaola.net.KaolaRequest;
import com.txznet.music.data.kaola.net.bean.KaolaAlbum;
import com.txznet.music.data.kaola.net.bean.KaolaAudio;
import com.txznet.music.data.kaola.net.bean.KaolaCategory;
import com.txznet.music.data.kaola.net.bean.RespActive;
import com.txznet.music.data.kaola.net.bean.RespItem;
import com.txznet.music.data.kaola.net.bean.RespParent;
import com.txznet.music.data.utils.DeviceInfo;
import com.txznet.music.data.utils.KaolaHelper;
import com.txznet.music.data.utils.OnGetData;
import com.txznet.music.service.ThirdHelper;
import com.txznet.music.utils.SharedPreferencesUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by telenewbie on 2018/2/5.
 */

public class KaoLaSDK {
    public static String openid = "";

    public final static String BASE_URL = "http://open.kaolafm.com/v2/";
    public final static String TAG = "com.txznet.test:kaola";

    private static Context mCtx;

    KaolaRequest kaolaRequest = null;
    String appid = "";
    String deviceid = "";
    String sign = "";
    String packagename = "";
    String os = "";
    String version = "";
    String osversion = "";
    int devicetype = 0;
    private String rtype = "20000";//请求的类别
    private String pagesize = "20";//请求的条数
    private String pagenum = "1";//请求的页数


    //##创建一个单例类##
    private volatile static KaoLaSDK singleton;

    private KaoLaSDK() {
        if (mCtx == null) {
            throw new RuntimeException("you should invoke initData(Context ctx) before any method");
        }

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

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.build())
                .baseUrl(BASE_URL).build();

        kaolaRequest = retrofit.create(KaolaRequest.class);

        appid = "wt2713";
        deviceid = DeviceInfo.getDeviceSerialNumber(mCtx);
        sign = KaolaHelper.getActiveSign(deviceid);
        packagename = "com.sgm.carlink";
        os = "web";
        version = "1.0";
        osversion = "5.1";
        devicetype = 0;
    }

    public static void initData(Context ctx) {
        mCtx = ctx;
    }

    public static KaoLaSDK getInstance() {
        if (singleton == null) {
            synchronized (KaoLaSDK.class) {
                if (singleton == null) {
                    singleton = new KaoLaSDK();
                }
            }
        }
        return singleton;
    }

    private Map<String, String> getCommMap() {
        Map<String, String> params = new HashMap<>();
        params.put("appid", appid);
        params.put("deviceid", deviceid);
        params.put("sign", TextUtils.equals("", openid) ? sign : KaolaHelper.getOtherSign(deviceid, openid));
        params.put("packagename", packagename);
        params.put("os", os);
        return params;
    }


//    public void activate(final OnGetData<String> openIdData) {
//
//
//        Map<String, String> params = getCommMap();
//        params.put("version", version);
//        params.put("osversion", osversion);
//        params.put("devicetype", "" + devicetype);
//
//
//        kaolaRequest.active(params).subscribeOn(Schedulers.io())
//                .map(new Function<RespParent<RespActive>, String>() {
//                    @Override
//                    public String apply(RespParent<RespActive> respActiveRespParent) throws Exception {
//                        if ("50500".equals(respActiveRespParent.getErrcode())) {
//                            return "-1";
//                        }
//
//                        return respActiveRespParent.getResult().getOpenid();
//
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<String>() {
//                    @Override
//                    public void accept(String s) throws Exception {
//                        if ("-1".equals(s)) {
//                            init(openIdData);
//                        } else {
//                            openid = s;
//                            openIdData.success(openid);
//                        }
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        openIdData.failed(-1);
//                    }
//                });
//    }

    public void init(final OnGetData<String> openIdData) {
        Map<String, String> params = getCommMap();
        kaolaRequest.init(params).subscribeOn(Schedulers.io())
                .map(new Function<RespParent<RespActive>, String>() {
                    @Override
                    public String apply(RespParent<RespActive> respActiveRespParent) throws Exception {
                        return respActiveRespParent.getResult().getOpenid();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        openid = s;
                        openIdData.success(openid);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        openIdData.failed(-1);
                    }
                });
    }


    Disposable mGetOpenIdDisposable;

    /**
     * "openid":"wt27132018020710002825"
     * rtype	String	是	类型：专辑：20000，碎片：30000，传统广播：50000
     * pagenum	Int	否	页数，默认=1
     * pagesize	Int	否	条数，默认=10
     *
     * @param keywords
     */
    public void search(final String keywords, final OnGetData<RespItem<KaolaAlbum>> onGetData) {

        if ("".equals(openid)) {
            if (mGetOpenIdDisposable != null && !mGetOpenIdDisposable.isDisposed()) {
                mGetOpenIdDisposable.dispose();
                mGetOpenIdDisposable = null;
            }
             mGetOpenIdDisposable = getOpenIdObservable().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            search(keywords, onGetData);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            onGetData.failed(-1);
                        }
                    });
            return;
        }


        Map<String, String> params = getCommMap();
        params.put("openid", openid);
        params.put("q", keywords);
        params.put("rtype", rtype);
        params.put("pagenum", pagenum);
        params.put("pagesize", pagesize);
        kaolaRequest.searchByType(params).subscribeOn(Schedulers.io())
                .map(new Function<RespParent<RespItem<KaolaAlbum>>, RespItem<KaolaAlbum>>() {
                    @Override
                    public RespItem<KaolaAlbum> apply(RespParent<RespItem<KaolaAlbum>> respItemRespParent) throws Exception {
                        LogUtil.d(ThirdHelper.TAG, respItemRespParent.toString());
                        if ("40300".equals(respItemRespParent.getErrcode())) {
                            clearOpenId();
                        }
                        return respItemRespParent.getResult();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespItem<KaolaAlbum>>() {
                    @Override
                    public void accept(RespItem<KaolaAlbum> kaolaAlbumRespItem) throws Exception {
                        LogUtil.d(ThirdHelper.TAG, kaolaAlbumRespItem.toString());
                        if (onGetData != null) {
                            onGetData.success(kaolaAlbumRespItem);
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (onGetData != null) {
                            onGetData.failed(-1);
                        }
                    }
                });
    }


    /**
     * 断点续播,!!
     *
     * @param albumid
     */
    public void getAudios(long albumid, long audioid, final OnGetData<RespItem<KaolaAudio>> onGetData) {
        Map<String, String> params = getCommMap();
        params.put("openid", openid);
        params.put("albumid", String.valueOf(albumid));
        params.put("pagenum", pagenum);
        params.put("pagesize", pagesize);
        if (audioid != 0) {
            params.put("audioid", String.valueOf(audioid));
        }


        kaolaRequest.getAudios(params).subscribeOn(Schedulers.io())
                .map(new Function<RespParent<RespItem<KaolaAudio>>, RespItem<KaolaAudio>>() {
                    @Override
                    public RespItem<KaolaAudio> apply(RespParent<RespItem<KaolaAudio>> respItemRespParent) throws Exception {
                        return respItemRespParent.getResult();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespItem<KaolaAudio>>() {
                    @Override
                    public void accept(RespItem<KaolaAudio> kaolaAudioRespItem) throws Exception {
                        if (onGetData != null) {
                            onGetData.success(kaolaAudioRespItem);
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

    public Observable<RespParent<RespItem<KaolaAudio>>> getAudiosObservable(final long albumid, final long audioid, int page) {
        if (page <= 0) {
            page = 1;
        }
        final int finalPage = page;
        return getOpenIdObservable().flatMap(new Function<String, ObservableSource<RespParent<RespItem<KaolaAudio>>>>() {
            @Override
            public ObservableSource<RespParent<RespItem<KaolaAudio>>> apply(String s) throws Exception {
                Map<String, String> params = getCommMap();
                params.put("openid", openid);
                params.put("albumid", String.valueOf(albumid));
                params.put("pagenum", String.valueOf(finalPage));
                params.put("pagesize", pagesize);
                if (audioid != 0) {
                    params.put("audioid", String.valueOf(audioid));
                }
                return kaolaRequest.getAudios(params);
            }
        });

    }

    public Observable<RespParent<List<KaolaCategory>>> getCategoryObservable() {
        return getOpenIdObservable().flatMap(new Function<String, ObservableSource<RespParent<List<KaolaCategory>>>>() {
            @Override
            public ObservableSource<RespParent<List<KaolaCategory>>> apply(String s) throws Exception {
                Map<String, String> params = getCommMap();
                params.put("openid", openid);
                return kaolaRequest.getCategorys(params);
            }
        });

    }

    public Observable<RespParent<List<KaolaCategory>>> getSubCategoryObservable(final long cid) {
        return getOpenIdObservable().flatMap(new Function<String, ObservableSource<RespParent<List<KaolaCategory>>>>() {
            @Override
            public ObservableSource<RespParent<List<KaolaCategory>>> apply(String s) throws Exception {
                Map<String, String> params = getCommMap();
                params.put("openid", openid);
                params.put("cid", String.valueOf(cid));
                return kaolaRequest.getSubCategorys(params);
            }
        });
    }


    public Observable<RespParent<RespItem<KaolaAlbum>>> getAlbumsObservable(final long cid) {
        return getOpenIdObservable().flatMap(new Function<String, ObservableSource<RespParent<RespItem<KaolaAlbum>>>>() {
            @Override
            public ObservableSource<RespParent<RespItem<KaolaAlbum>>> apply(String s) throws Exception {
                Map<String, String> params = getCommMap();
                params.put("openid", openid);
                params.put("cid", String.valueOf(cid));
                params.put("pagenum", "1");
                params.put("pagesize", "10");
                params.put("sorttype", "0");
                return kaolaRequest.getAlbums(params);
            }
        });
    }

    private Observable<RespParent<RespActive>> getActiveObservable() {
        Map<String, String> params = getCommMap();
        params.put("version", version);
        params.put("osversion", osversion);
        params.put("devicetype", "" + devicetype);
        return kaolaRequest.active(params).doOnNext(setOpenidConsumer);
    }

    Consumer<RespParent<RespActive>> setOpenidConsumer = new Consumer<RespParent<RespActive>>() {
        @Override
        public void accept(RespParent<RespActive> respActiveRespParent) throws Exception {
            if (respActiveRespParent.getErrcode() == null || TextUtils.equals(respActiveRespParent.getErrcode(), "")) {
                openid = respActiveRespParent.getResult().getOpenid();
                SharedPreferencesUtils.setKaolaOpenId(openid);
            }
        }
    };

    private Observable<RespParent<RespActive>> getInitObservable() {
        Map<String, String> params = getCommMap();
        kaolaRequest.init(params);

        return kaolaRequest.init(params).doOnNext(setOpenidConsumer);
    }

    private Observable<String> getOpenIdObservable() {

        if (TextUtils.equals("", openid) && TextUtils.equals("", SharedPreferencesUtils.getKaolaOpenId())) {
            //没有激活成功
            return getActiveObservable().flatMap(new Function<RespParent<RespActive>, ObservableSource<RespParent<RespActive>>>() {
                @Override
                public ObservableSource<RespParent<RespActive>> apply(final RespParent<RespActive> respActiveRespParent) throws Exception {


                    if (TextUtils.equals(respActiveRespParent.getErrcode(), "")) {
                        return Observable.just(respActiveRespParent);
                    } else {
                        if ("50500".equals(respActiveRespParent.getErrcode())) {
                            //重复激活
                            return getInitObservable();
                        }
                        throw new IllegalStateException("考拉接口出现异常：(" + respActiveRespParent.getErrcode() + ")");
                    }
                }
            }).flatMap(new Function<RespParent<RespActive>, ObservableSource<String>>() {
                @Override
                public ObservableSource<String> apply(RespParent<RespActive> respActiveRespParent) throws Exception {
                    return Observable.just(respActiveRespParent.getResult().getOpenid());
                }

            });
        } else {
            //激活成功
            if (TextUtils.equals("", openid)) {
                openid = SharedPreferencesUtils.getKaolaOpenId();
            } else if (TextUtils.equals("", SharedPreferencesUtils.getKaolaOpenId())) {
                SharedPreferencesUtils.setKaolaOpenId(openid);
            }

            return Observable.just(openid);
        }
    }

    public void clearOpenId() {
        SharedPreferencesUtils.setKaolaOpenId("");
        openid = "";
    }

}
