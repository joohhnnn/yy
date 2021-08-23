package com.txznet.launcher.data.http.interceptor;

import android.support.annotation.NonNull;
import android.util.Base64;

import com.txznet.launcher.utils.DeviceUtils;
import com.txznet.txz.util.EncodeUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 安吉星的OkHttp拦截器，用来添加header
 */
public class AuthInterceptor implements Interceptor {
    private String mAuthKeyCache;
    private String mCacheDeviceId;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder reqBuilder = chain.request().newBuilder();
        reqBuilder.addHeader("Authorization", "Basic " + doSign(DeviceUtils.getDeviceID()));
        reqBuilder.addHeader("Channel", "rvmirror");
        return chain.proceed(reqBuilder.build());
    }

    // 签名
    private String doSign(@NonNull String deviceID) {
        if (mAuthKeyCache == null || !deviceID.equals(mCacheDeviceId)) {
            mAuthKeyCache = Base64.encodeToString((deviceID + ":" + EncodeUtil.Sha256Str(deviceID)).getBytes(), Base64.DEFAULT);
            mAuthKeyCache = mAuthKeyCache.replaceAll("\n", "");
            mCacheDeviceId = deviceID;
        }
        return mAuthKeyCache;
    }
}
