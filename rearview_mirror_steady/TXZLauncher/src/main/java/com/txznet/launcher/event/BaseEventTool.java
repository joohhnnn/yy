package com.txznet.launcher.event;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by meteorluo on 2018/2/14.
 * 封装了事件的监听和撤销。对外提供了要监听的事件和监听事件的处理两个方法。方便其他类使用事件分发
 */

public class BaseEventTool {
    private static final int MSG_EVENT = 1;

//    private static class ListenerHandler extends Handler {
//        private WeakReference<BaseEventTool> mModule;
//
//        public ListenerHandler(BaseEventTool module) {
//            mModule = new WeakReference<BaseEventTool>(module);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (MSG_EVENT == msg.what) {
//                mModule.get().onEvent((String) msg.obj);
//            }
//        }
//    }

    private class ModuleEventObserver extends EventObserver {

        @Override
        protected void onChange(String eventType) {
//            mHandler.removeMessages(MSG_EVENT);
//            Message.obtain(mHandler, MSG_EVENT, eventType).sendToTarget();
            onEvent(eventType);
        }
    }

    private ModuleEventObserver mObserver = new ModuleEventObserver();
//    private ListenerHandler mHandler = new ListenerHandler(this);

    public void register() {
        registerEventObserver(mObserver);
    }

    public void unRegister() {
        unregisterEventObserver(mObserver);
    }

    /**
     * 注册观察者
     *
     * @param observer
     */
    private void registerEventObserver(EventObserver observer) {
        final String[] eventTypes = getObserverEventTypes();
        if (eventTypes == null || eventTypes.length < 1) {
            return;
        }
        final EventsObservable eventsObservable = EventsObservable.getInstance();
        for (String event : eventTypes) {
            eventsObservable.registerObserver(event, observer);
        }
    }

    /**
     * 反注册观察者
     *
     * @param observer
     */
    private void unregisterEventObserver(EventObserver observer) {
        final String[] eventTypes = getObserverEventTypes();
        if (eventTypes == null || eventTypes.length < 1) {
            return;
        }
        final EventsObservable eventsObservable = EventsObservable.getInstance();
        for (String event : eventTypes) {
            eventsObservable.unRegisterObserver(event, observer);
        }
    }

    /**
     * 需要添加事件重写该方法
     *
     * @return
     */
    public String[] getObserverEventTypes() {
        return null;
    }

    /**
     * 需要监听消息重写该方法
     *
     * @param eventType
     */
    protected void onEvent(String eventType) {
    }
}