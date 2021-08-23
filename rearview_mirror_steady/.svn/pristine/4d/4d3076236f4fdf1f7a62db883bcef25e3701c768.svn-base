package com.txznet.launcher.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by TXZ-METEORLUO on 2018/2/8.
 * 事件分发的被观察者部分的代码
 */

public class EventsObservable {
    private static EventsObservable notificationCenter;
    // 保存监听者的map，每一种监听类型都对应着一列表的观察者
    private final static Map<String, CopyOnWriteArrayList<EventObserver>> EVENT_MAP = new HashMap<>();

    public static EventsObservable getInstance() {
        if (notificationCenter == null) {
            synchronized (EventsObservable.class) {
                if (notificationCenter == null) {
                    notificationCenter = new EventsObservable();
                }
            }
        }
        return notificationCenter;
    }

    /**
     * 注册监听者
     */
    public void registerObserver(String eventType, EventObserver observer) {
        synchronized (EVENT_MAP) {
            CopyOnWriteArrayList<EventObserver> eventObservers = EVENT_MAP.get(eventType);
            if (eventObservers == null) {
                eventObservers = new CopyOnWriteArrayList<>();
                EVENT_MAP.put(eventType, eventObservers);
            }
            if (eventObservers.contains(observer)) {
                return;
            }
            eventObservers.add(observer);
        }
    }

    /**
     * 解除监听者
     */
    public void unRegisterObserver(String eventType, EventObserver observer) {
        if (observer == null) {
            throw new NullPointerException("unRegisterObserver EventObserver is null");
        }

        synchronized (EVENT_MAP) {
            CopyOnWriteArrayList<EventObserver> observers = EVENT_MAP.get(eventType);
            if (observers != null && observers.indexOf(observer) != -1) {
                observers.remove(observer);
            }
        }
    }

    /**
     * 清空所有的监听者
     */
    public void unregisterAll() {
        synchronized (EVENT_MAP) {
            Set<String> keySet = EVENT_MAP.keySet();
            for (String key : keySet) {
                EVENT_MAP.get(key).clear();
            }
            EVENT_MAP.clear();
        }
    }

    /**
     * 分发事件
     */
    public void dispatchEvent(String eventType) {
        CopyOnWriteArrayList<EventObserver> observers = EVENT_MAP.get(eventType);
        if (observers != null) {
            for (EventObserver observer : observers) {
                observer.dispatchChange(eventType);
            }
        }
    }
}