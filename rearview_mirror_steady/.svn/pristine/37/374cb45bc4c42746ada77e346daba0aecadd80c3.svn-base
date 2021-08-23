package com.txznet.launcher.domain.notification.data;

import com.google.gson.Gson;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.utils.NetworkUtils;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZNetDataProvider;
import com.txznet.sdk.bean.WeatherData;

/**
 * 今日提示天气类型的数据类
 */
public class WeatherNoticeData implements INoticeData {
    private String mData;

    @Override
    public void prepare() {
        if (!NetworkUtils.isNetworkConnected(AppLogic.getApp())) {
            return;
        }

        // 获取数据
        TXZNetDataProvider.getInstance().getWeatherInfo(new TXZNetDataProvider.NetDataCallback<WeatherData>() {
            @Override
            public void onResult(WeatherData data) {
                try {
                    mData = new Gson().toJson(data);
                    LogUtil.logd("prepare weather data success");
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.logd("prepare weather data failed");
                }
            }

            @Override
            public void onError(int errorCode) {
                LogUtil.logd("prepare weather data failed");
            }
        });
    }

    @Override
    public boolean isLoaded() {
        return mData != null;
    }

    @Override
    public int getType() {
        return INoticeData.DATA_TYPE_WEATHER;
    }

    @Override
    public String getData() {
        return mData;
    }

    @Override
    public boolean isDependOnTXZ() {
        return true;
    }

    @Override
    public void release() {
        mData = null;
    }
}
