package com.txznet.music.util;

import android.os.SystemClock;

import com.txznet.music.Constant;
import com.txznet.music.data.source.TXZMusicDataSource;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by brainBear on 2017/11/14.
 * 获取当前时间的工具类
 */

public class TimeManager {

    private static final String TAG = Constant.LOG_TAG_UTILS + ":Time";
    private static final long TIME_DEVIATION = 60;
    private static TimeManager sInstance = new TimeManager();
    private boolean mEffect = false;
    private long mTimeStamp;
    private long mElapsedTime;

    private TimeManager() {
        requestServerTime();
    }

    public static TimeManager getInstance() {
        return sInstance;
    }


    private void saveTime(long time) {
        mEffect = true;
        //后台返回的时间戳单位是秒，客户端需要毫秒值
        mTimeStamp = time * 1000;
        mElapsedTime = SystemClock.elapsedRealtime();
    }


    private void requestServerTime() {
        Disposable disposable = TXZMusicDataSource.get().getServerTime()
                .subscribeOn(Schedulers.io())
                .retryWhen(throwableObservable -> throwableObservable.flatMap(throwable -> {
                    Logger.d(TAG, "request time:" + throwable.toString());
                    return Observable.timer(30, TimeUnit.SECONDS);
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    Logger.d(TAG, "request time:" + aLong);
                    saveTime(aLong);
                }, throwable -> {
                    Logger.d(TAG, "request time:" + throwable.toString());
                });
    }


    /**
     * 获取有效时间 单位秒
     *
     * @return 返回时间戳，如果无效则返回-1
     */
    public long getEffectiveTime() {
        if (mEffect) {
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
        return mTimeStamp + SystemClock.elapsedRealtime() - mElapsedTime;
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
        if (mEffect) {
            return getEffectiveTimeMillis();
        }
        long time = System.currentTimeMillis();
        return time;
    }

    /**
     * 判断当前时间戳是否有效
     *
     * @return true 有效 false 无效
     */
    public boolean isEffect() {
        return mEffect;
    }


    public boolean isTimeout(long time) {
        long effectiveTime = getTime();
        if (effectiveTime < 0) {
            Logger.d(TAG, "isTimeout, effectiveTime=" + effectiveTime);
            return true;
        }
        boolean result = effectiveTime + TIME_DEVIATION > time;
        Logger.d(TAG, "isTimeout=" + result + ", effectiveTime=" + (effectiveTime + TIME_DEVIATION) + ", time=" + time);
        return result;
    }
}

