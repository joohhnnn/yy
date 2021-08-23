package com.txznet.music.Time;

import android.os.SystemClock;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestCallBack;

/**
 * Created by brainBear on 2017/11/14.
 * 获取当前时间的工具类
 */

public class TimeManager {

    private static final String TAG = "TimeManager:";
    private static TimeManager sInstance;
    private boolean mEffect = false;
    private long mTimeStamp;
    private long mElapsedTime;

    private TimeManager() {
        requestServerTime();
    }

    public static TimeManager getInstance() {
        if (null == sInstance) {
            synchronized (TimeManager.class) {
                if (null == sInstance) {
                    sInstance = new TimeManager();
                }
            }
        }
        return sInstance;
    }


    private void saveTime(long time) {
        mEffect = true;
        //后台返回的时间戳单位是秒，客户端需要毫秒值
        mTimeStamp = time * 1000;
        mElapsedTime = SystemClock.elapsedRealtime();
    }


    private void requestServerTime() {
        NetManager.getInstance().requestTime(new RequestCallBack<TimeResponse>(TimeResponse.class) {
            @Override
            public void onResponse(TimeResponse data) {
                LogUtil.d(TAG, "request time:" + data.getTime());
                saveTime(data.getTime());
            }

            @Override
            public void onError(String cmd, Error error) {
                LogUtil.d(TAG, "request time:" + error.toString());
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        requestServerTime();
                    }
                }, 30 * 1000);
            }
        });
    }


    /**
     * 获取有效时间 单位秒
     *
     * @return 返回时间戳，如果无效则返回-1
     */
    public long getEffectiveTime() {
        if (isEffect()) {
            return getEffectiveTimeMillis() / 1000;
        }
        return -1;
    }

    /**
     * 获取有效时间 单位毫秒
     *
     * @return 返回时间戳，如果无效则返回-1
     */
    public long getEffectiveTimeMillis() {
        if (isEffect()) {
            return mTimeStamp + SystemClock.elapsedRealtime() - mElapsedTime;
        }
        return -1;
    }

    /**
     * 获取时间戳，如果当前时间没有同步过则返回当前的系统时间戳，单位秒
     *
     * @return 返回时间戳
     */
    public long getTime() {
        return getTimeMillis() / 1000;
    }

    /**
     * 获取时间戳，如果当前时间没有同步过则返回当前的系统时间戳，单位毫秒
     *
     * @return 返回时间戳
     */
    public long getTimeMillis() {
        if (isEffect()) {
            return getEffectiveTimeMillis();
        }
        return System.currentTimeMillis();
    }

    /**
     * 判断当前时间戳是否有效
     *
     * @return true 有效 false 无效
     */
    public boolean isEffect() {
        return mEffect;
    }

}

