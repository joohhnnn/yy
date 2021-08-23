package com.txznet.launcher.data.http;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.data.http.api.AnjixingService;
import com.txznet.launcher.data.http.interceptor.AuthInterceptor;
import com.txznet.launcher.utils.Conditions;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络请求通用基础配置和获取网络请求对象的封装
 */
public final class ApiClient {
    private static ApiClient sInstance;
    private AnjixingService mApiService;

    private ApiClient() {

    }

    public static ApiClient getInstance() {
        if (sInstance == null) {
            sInstance = new ApiClient();
        }
        return sInstance;
    }

    private OkHttpClient buildOkHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(ApiConst.CONNECT_TIMEOUT, TimeUnit.SECONDS);
        // 这里面添加了header
        builder.addInterceptor(new AuthInterceptor());
//        if (BuildConfig.DEBUG) { // 打开日志
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    LogUtil.logi(message);
                }
            });
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(httpLoggingInterceptor);
//        }
        // FIXME 忽略https证书验证
        builder.sslSocketFactory(SSLSocketClient.getSSLSocketFactory());
        builder.hostnameVerifier(SSLSocketClient.getHostnameVerifier());
        return builder.build();
    }

    /**
     * 获取安吉星的Retrofit对象，用来执行网络操作
     */
    public AnjixingService getApiService() {
        if (mApiService == null) {
            mApiService = new Retrofit.Builder()
                    .baseUrl(Conditions.useAnjixingTestEnvironment() ? ApiConst.HOST_ANJIXING_TEST : ApiConst.HOST_ANJIXING_PRODUCT)
                    .client(buildOkHttp())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(AnjixingService.class);
        }
        return mApiService;
    }
}
