package com.txznet.launcher.event;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 * 通知中心，负责通知所有发生的事件
 */

public class BroadcastingCentre {
    private static BroadcastingCentre sCentre;

    public static BroadcastingCentre getInstance() {
        if (sCentre == null) {
            synchronized (BroadcastingCentre.class) {
                if (sCentre == null) {
                    sCentre = new BroadcastingCentre();
                }
            }
        }
        return sCentre;
    }

    public void notifyEvent(String eventType) {
        LogUtil.logd("notifyEvent:" + eventType);
        EventTypes types = EventTypes.getInstance();
        if (types.isContainType(eventType)) {
            EventsObservable.getInstance().dispatchEvent(eventType);
        } else {
            LogUtil.logd(eventType + " notifyEvent no contain type");
        }
    }

    public void notifyEventWithBackThread(final String eventType) {
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                notifyEvent(eventType);
            }
        });
    }
}