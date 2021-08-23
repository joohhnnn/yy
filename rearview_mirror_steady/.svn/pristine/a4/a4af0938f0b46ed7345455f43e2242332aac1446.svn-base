package com.txznet.launcher.domain.notification;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.domain.notification.data.INoticeData;
import com.txznet.launcher.domain.notification.data.WeatherNoticeData;
import com.txznet.launcher.event.EventTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * 今日贴士业务类
 */
public class TodayNoticeManager extends BaseManager {

    private static TodayNoticeManager sInstance;

    private final List<INoticeData> mNeedPrepareDataList = new ArrayList<>(); // 需要加载的数据项

    private TodayNoticeManager() {
    }

    public static TodayNoticeManager getInstance() {
        if (sInstance == null) {
            sInstance = new TodayNoticeManager();
        }
        return sInstance;
    }

    @Override
    public void init() {
        super.init();
        initNoticeDataList();
    }

    // 初始化需要预加载的数据项
    private void initNoticeDataList() {
        synchronized (mNeedPrepareDataList) {
            if (mNeedPrepareDataList.isEmpty()) {
                mNeedPrepareDataList.add(new WeatherNoticeData()); // 天气
            }
        }
    }


    @Override
    public String[] getObserverEventTypes() {
        return new String[]{
                EventTypes.EVENT_TXZ_INIT_SUCCESS,
                EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP,
                EventTypes.EVENT_DEVICE_POWER_WAKEUP,
        };
    }

    @Override
    protected void onEvent(String eventType) {
        switch (eventType) {
            case EventTypes.EVENT_TXZ_INIT_SUCCESS:
                prepareData(true);
                break;
            case EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP:
                release();
                break;
            case EventTypes.EVENT_DEVICE_POWER_WAKEUP:
                initNoticeDataList();
                break;
        }
    }

    /**
     * 预加载数据
     *
     * @param isTXZInited 同行者是否初始化成功
     */
    public void prepareData(boolean isTXZInited) {
        LogUtil.logd("prepareData, isTXZInited=" + isTXZInited);
        synchronized (mNeedPrepareDataList) {
            for (INoticeData noticeData : mNeedPrepareDataList) {
                if (!noticeData.isLoaded()) {
                    if (!noticeData.isDependOnTXZ()) {
                        noticeData.prepare();
                    }
                    if (noticeData.isDependOnTXZ() && isTXZInited) {
                        noticeData.prepare();
                    }
                }
            }
        }
    }

    /**
     * 获取加载完毕的数据
     */
    public List<INoticeData> getLoadedData() {
        List<INoticeData> preparedDataList = new ArrayList<>();
        synchronized (mNeedPrepareDataList) {
            for (INoticeData iNoticeData : mNeedPrepareDataList) {
                if (iNoticeData.isLoaded()) {
                    preparedDataList.add(iNoticeData);
                }
            }
        }
        LogUtil.logd("getLoadedData, size=" + preparedDataList.size());
        return preparedDataList;
    }

    public void release() {
        LogUtil.logd("release");
        synchronized (mNeedPrepareDataList) {
            for (INoticeData noticeData : mNeedPrepareDataList) {
                if (noticeData.isLoaded()) {
                    noticeData.release();
                }
            }
        }
    }
}
