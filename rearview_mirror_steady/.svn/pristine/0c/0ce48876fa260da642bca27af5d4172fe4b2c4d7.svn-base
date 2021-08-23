package com.txznet.launcher.domain.guide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;

/**
 * Created by daviddai on 2018/8/16
 * 开机引导管理模块，接收引导app状态的receiver
 */
public class GuideManager extends BaseManager {

    public static final String GUIDE_PKG_NAME = "com.txznet.guideanim";

    // 和引导app协商好的，0是完毕，1是被打断。
    private static final int GUIDE_COMPLETE = 0;
    private static final int GUIDE_INTERRUPT = 1;

    private static final String RECEIVE_PARAMS_STATE = "state";
    private static final String SEND_PARAMS_FORCE = "force";

    private static final String ACTION_GUIDE_FOREGROUND = "com.txznet.txz.intent.action.GUIDE_FOREGROUND";
    private static final String ACTION_GUIDE_BACKGROUND = "com.txznet.txz.intent.action.GUIDE_BACKGROUND";

    private boolean isGuideActive; // 开机引导是否处于活跃状态;

    private static GuideManager sInstance;

    private GuideManager() {
    }

    public static GuideManager getInstance() {
        if (sInstance == null) {
            sInstance = new GuideManager();
        }
        return sInstance;
    }

    @Override
    public void init() {
        super.init();


        /**
         * 开机引导状态
         * ACTION  com.txznet.txz.intent.action.GUIDE_STATE_NOTIFY
         * EXTRA   state	int  状态 0 - complete 完毕 ， 1 - interrupt 被打断
         */
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(RECEIVE_PARAMS_STATE, GUIDE_COMPLETE);
                LogUtil.e( "onReceive: state=" + state);
                if (state == GUIDE_COMPLETE) {
                    // TODO: 2018/8/17 引导正常结束
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_GUIDE_COMPLETE);
                } else if (state == GUIDE_INTERRUPT) {
                    // TODO: 2018/8/17  引导被关闭
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_GUIDE_INTERRUPT);
                }
            }
        }, new IntentFilter("com.txznet.txz.intent.action.GUIDE_STATE_NOTIFY"));


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GUIDE_FOREGROUND);
        intentFilter.addAction(ACTION_GUIDE_BACKGROUND);
        /*
            开机引导处于前台
                ACTION	com.txznet.txz.intent.action.GUIDE_FOREGROUND
                EXTRA	暂无
            开机引导处于后台 - Application启动的时候先同步下
                ACTION 	com.txznet.txz.intent.action.GUIDE_BACKGROUND
                EXTRA 	暂无
         */
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtil.logd("receive, " + intent.getAction());
                if (ACTION_GUIDE_FOREGROUND.equals(intent.getAction())) {
                    isGuideActive = true;
                } else if (ACTION_GUIDE_BACKGROUND.equals(intent.getAction())) {
                    isGuideActive = false;
                }
            }
        }, intentFilter);
    }

    /**
     * 启动开机引导
     * ACTION	com.txznet.txz.intent.action.BOOT_GUIDE
     * EXTRA	force	boolean 是否强制启动，若强制启动则无视本地是否执行过的记录
     */
    public void sendStartGuideBroadcast(Context context, boolean force) {
        LogUtil.logd("sendGuideBroadcast, isForce=" + force);
        Intent intent = new Intent();
        intent.setAction("com.txznet.txz.intent.action.BOOT_GUIDE");
        intent.putExtra(SEND_PARAMS_FORCE, force);
        context.sendBroadcast(intent);
    }

    /**
     * 关闭/打断开机引导
     * ACTION 	com.txznet.txz.intent.action.INTERRUPT_GUIDE
     * EXTRA 	暂无
     */
    public void sendInterruptGuideBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction("com.txznet.txz.intent.action.INTERRUPT_GUIDE");
        context.sendBroadcast(intent);
    }

    public boolean isGuideActive() {
        return isGuideActive;
    }

    @Override
    protected void onEvent(String eventType) {
        switch (eventType) {
            case EventTypes.EVENT_DEVICE_BLUE_BUTTON_PRESSED:
            case EventTypes.EVENT_DEVICE_RED_BUTTON_PRESSED:
            case EventTypes.EVENT_DEVICE_RECORY_FACTORY:
            case EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP:
                if (isGuideActive) {
                    sendInterruptGuideBroadcast(GlobalContext.get());
                }
                break;
        }
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[] {
                EventTypes.EVENT_DEVICE_BLUE_BUTTON_PRESSED,
                EventTypes.EVENT_DEVICE_RED_BUTTON_PRESSED,
                EventTypes.EVENT_DEVICE_RECORY_FACTORY,
                EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP
        };
    }
}
