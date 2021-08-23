package com.txznet.launcher.domain.nav;

import com.txz.equipment_manager.EquipmentManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.cfg.DebugCfg;
import com.txznet.launcher.component.nav.NavAppComponent;
import com.txznet.launcher.component.nav.NavGdComponent;
import com.txznet.launcher.component.nav.NavTXZComponent;
import com.txznet.launcher.component.nav.PoiIssuedTool;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.guide.GuideManager;
import com.txznet.launcher.domain.wechat.WechatManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.sdk.TXZStatusManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.sdk.bean.Poi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 * 导航相关业务类。包括了HUD、路况早晚报、地址下发的业务。
 */

public class NavManager extends BaseManager {
    private static final String TAG = NavManager.class.getSimpleName();

    private static NavManager sInstance;

    private NavAppComponent.HUDInfo mCacheInfo;
    private NavAppComponent mCurrNavComponent;
    private NavGdComponent mCurrNavGdComponent;
    private List<NavAppComponent.HUDUpdateListener> mUpdateListeners = new ArrayList<>();

    private boolean hasAnjixingLogin; // 安吉星账号是否已经登录
    private boolean needWaitBootComplete = true;

    public static NavManager getInstance() {
        if (sInstance == null) {
            synchronized (NavManager.class) {
                if (sInstance == null) {
                    sInstance = new NavManager();
                }
            }
        }
        return sInstance;
    }

    private NavAppComponent.HUDUpdateListener updateListener = new NavAppComponent.HUDUpdateListener() {
        @Override
        public void onHudUpdate(final NavAppComponent.HUDInfo info) {
            mCacheInfo = info;
            synchronized (mUpdateListeners) {
                for (NavAppComponent.HUDUpdateListener listener : mUpdateListeners) {
                    listener.onHudUpdate(info);
                }
            }
        }
    };

    private NavAppComponent.OnNavStateListener navStateListener = new NavAppComponent.OnNavStateListener() {
        @Override
        public void onForebackGround(boolean isFocus) {
            if (isFocus) {
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_NAV_FOREGROUND);
                if (mCurrNavGdComponent != null) {
                    mCurrNavGdComponent.notifyCancelHud();
                    mCurrNavGdComponent.setGaodeBgNavSpeak(false);
                    mCurrNavGdComponent.setGaodeNavSpeak(false);
                }
            } else {
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_NAV_BACKGROUND);
            }
        }

        @Override
        public void onNavState(boolean isNaving) {
            if (isNaving) {
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_NAV_START_NAVI);
                if (mCurrNavGdComponent != null) {
                    mCurrNavGdComponent.notifyCancelHud();
                }
            } else {
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_NAV_END_NAVI);
            }
        }

        @Override
        public void onNavEnter() {
            LaunchManager.getInstance().launchBackWithStack();
            if (mCurrNavGdComponent != null) {
                mCurrNavGdComponent.resetWakeupAsr();
            }
        }

        @Override
        public void onNavExit() {
            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_NAV_EXIT_NAVI);
        }
    };

    public PoiIssuedTool.OnPoiIssuedListener poiIssuedListener = new PoiIssuedTool.OnPoiIssuedListener() {
        @Override
        public void onPoiIssued(PoiIssuedTool.IssuedPoi item) {
            if (!hasAnjixingLogin) { // 安吉星未登录，则不处理
                LogUtil.logd("onPoiIssued passed, anjixing is logout");
                return;
            }
//            resetNotifyItemList();
//            testPoiIssued();

            synchronized (mIssuedPoiList) {
                mIssuedPoiList.add(0, item);
            }
            processPoiIssuedList();
        }
    };

    public void syncGDMuteIcon (boolean mute){
        if (mCurrNavGdComponent!=null) {
            mCurrNavGdComponent.syncGDMuteIcon(mute);
        }
    }

    private final LinkedList<PoiIssuedTool.IssuedPoi> mIssuedPoiList = new LinkedList<PoiIssuedTool.IssuedPoi>();

    private Runnable mRunnableProcessPoiIssuedList = new Runnable() {
        @Override
        public void run() {
            processPoiIssuedList();
        }
    };

    private void processPoiIssuedList() {
        boolean bDelay = false;
        do {
            if (TXZStatusManager.getInstance().isTtsBusy()
                    || TXZStatusManager.getInstance().isCallBusy()) {
                LogUtil.logd(String.format("onPoiIssued delay, ttsBusy=%s, callBusy=%s",
                        TXZStatusManager.getInstance().isTtsBusy(),
                        TXZStatusManager.getInstance().isCallBusy()));
                bDelay = true;
                break;
            }
            if (LaunchManager.getInstance().isActiveWechatRecord()) { // 录音中延迟下发
                LogUtil.logd("onPoiIssued delay, wechat record active");
                bDelay = true;
                break;
            }
            if (WechatManager.getInstance().isWechatBusy()) { // 微信消息播报中延迟下发
                LogUtil.logd("onPoiIssued delay, wechat busy");
                bDelay = true;
                break;
            }
            if (GuideManager.getInstance().isGuideActive()) { // 开机引导执行中
                LogUtil.logd("onPoiIssued delay, guide busy");
                bDelay = true;
                break;
            }
            if (needWaitBootComplete) {
                LogUtil.logd("onPoiIssued delay, need wait boot complete");
                bDelay = true;
                break;
            }
        } while (false);

        if (bDelay) {
            AppLogic.removeUiGroundCallback(mRunnableProcessPoiIssuedList);
            AppLogic.runOnUiGround(mRunnableProcessPoiIssuedList, 3000);
            return;
        }

        synchronized (mIssuedPoiList) {
            if (mIssuedPoiList.isEmpty()) {
                LogUtil.logd("processPoiIssuedList, list empty");
                return;
            }
            PoiIssuedTool.IssuedPoi issuedPoi = mIssuedPoiList.getFirst();
            NavManager.this.onPoiIssued(issuedPoi);
            mIssuedPoiList.remove(issuedPoi);
        }
        AppLogic.removeUiGroundCallback(mRunnableProcessPoiIssuedList);
        AppLogic.runOnUiGround(mRunnableProcessPoiIssuedList);
    }


    private void testPoiIssued() {
        PoiIssuedTool.IssuedPoi poi = new PoiIssuedTool.IssuedPoi();
        poi.lat = 22.541415;
        poi.lng = 113.980665;
        poi.poiName = "欢乐谷河街道中新社区东北方向南山区沙河街道中";
        poi.poiAddress = "广东省深圳市南山区沙河街道中新社区东北方向";
        poi.source = "模拟";
        poi.message = "您有一条导航消息，目的地：" + poi.poiAddress;
        currNotifyItems.add(poi);
        poi = new PoiIssuedTool.IssuedPoi();
        poi.lat = 22.548074;
        poi.lng = 113.958005;
        poi.poiName = "华润城府河街道中新社区东北方向";
        poi.poiAddress = "广东省深圳市南山区粤海街道大冲社区东北方向";
        poi.source = "同行者";
        currNotifyItems.add(poi);
        poi = new PoiIssuedTool.IssuedPoi();
        poi.lat = 22.5440315;
        poi.lng = 113.930282;
        poi.poiName = "麒麟花园河街道中新社区东北方向";
        poi.poiAddress = "广东省深圳市南山区南头街道莲城社区东南方向";
        poi.source = "同行者";
        currNotifyItems.add(poi);
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                LaunchManager.getInstance().launchPoiIssued();
                // 广播目的地事件
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_POI_ISSUED);
            }
        });
    }

    private void resetNotifyItemList() {
        synchronized (currNotifyItems) {
            currNotifyItems.clear();
        }
        mCurrSelectIdx = 0;
    }

    private final LinkedList<PoiIssuedTool.IssuedPoi> currNotifyItems = new LinkedList<>();

    private void onPoiIssued(PoiIssuedTool.IssuedPoi item) {
        LogUtil.logd(TAG + " onPoiIssued:" + item);
        startCheckTimeout();
        synchronized (currNotifyItems) {
            currNotifyItems.add(0, item);
        }
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                bInterruptNav = true;
                LaunchManager.getInstance().launchPoiIssued();
                // 广播目的地事件
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_POI_ISSUED);
            }
        });
    }

    /**
     * 获取当前通知
     *
     * @return
     */
    public List<PoiIssuedTool.IssuedPoi> getCurrNotifyItems() {
        synchronized (currNotifyItems) {
            return currNotifyItems;
        }
    }

    private int mCurrSelectIdx = 0;

    /**
     * 获取当前处于展示的数据
     *
     * @return
     */
    public PoiIssuedTool.IssuedPoi getCurrActiveItem() {
        if (currNotifyItems.size() < 1) {
            return null;
        }
        synchronized (currNotifyItems) {
            return currNotifyItems.get(mCurrSelectIdx);
        }
    }

    /**
     * 移除通知
     *
     * @param item
     */
    void removeNotifyItem(PoiIssuedTool.IssuedPoi item) {
        synchronized (currNotifyItems) {
            currNotifyItems.remove(item);
        }
    }

    /**
     * 移除缓存的通知
     */
    public void removeAllNotifyItems() {
        synchronized (currNotifyItems) {
            currNotifyItems.clear();
        }
    }

    private static final String POIISSUED_TASK_ID = "PoiIssued_Task_Id";

    public void registerPoiIssuedWakeupAsr(WakeupAsrSelectCallback selectCallback) {
        if (currNotifyItems.size() == 0 || currNotifyItems.size() <= mCurrSelectIdx) {
            LogUtil.logd("registerPoiIssuedWakeupAsr currNotifyItems.size:" + currNotifyItems.size() + ",mCurrSelectIdx:" + mCurrSelectIdx);
            return;
        }
        PoiIssuedTool.IssuedPoi item = currNotifyItems.get(mCurrSelectIdx);
        if (item == null) {
            LogUtil.logd("registerPoiIssuedWakeupAsr item is null！");
            return;
        }

        List<PoiIssuedTool.IssuedPoi> items = getCurrNotifyItems();
        if (items == null || items.size() <= 0) {
            return;
        }
        int size = items.size();

        boolean hasPre = false;
        boolean hasNext = size > 1;
        if (mCurrSelectIdx == (size - 1)) {
            if (selectCallback != null) {
                selectCallback.onLastPage();
            }
            hasNext = false;
        }
        if (mCurrSelectIdx > 0) {
            hasPre = true;
        }

        speakText(item.message, new TXZTtsManager.ITtsCallback() {
            @Override
            public void onSuccess() {
                LogUtil.logd("startCheckTimeout");
                startCheckTimeout();
            }
        });
        registerWakeupKws(hasNext, hasPre, size, item, selectCallback);
        if (selectCallback != null) {
            if (!hasPre) {
                selectCallback.onPrePage();
            }
            if (!hasNext) {
                selectCallback.onLastPage();
            }
        }
    }

    private boolean mHasRegistered;

    private void registerWakeupKws(
            final boolean hasNext,
            final boolean hasPre,
            final int totalSize,
            final PoiIssuedTool.IssuedPoi currItem,
            final WakeupAsrSelectCallback selectCallback) {
        TXZAsrManager.AsrComplexSelectCallback callback = new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public String getTaskId() {
                return POIISSUED_TASK_ID;
            }

            @Override
            public boolean needAsrState() {
                return true;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                startCheckTimeout();
                if ("SURE".equals(type)) {
                    selectSure(currItem);
                } else if ("CANCEL".equals(type)) {
                    selectCancel();
                } else if ("NEXT_PAGE".equals(type)) {
                    mCurrSelectIdx++;
                    boolean hasNext = true;
                    boolean hasPre = false;
                    if (mCurrSelectIdx > 0) {
                        hasPre = true;
                    }
                    if (selectCallback != null) {
                        selectCallback.onNext();
                    }
                    if (mCurrSelectIdx == (totalSize - 1)) {
                        if (selectCallback != null) {
                            selectCallback.onLastPage();
                        }
                        hasNext = false;
                    }
                    registerWakeupKws(hasNext, hasPre, totalSize, getCurrActiveItem(), selectCallback);
                    speakText("已切换为下一页", null);
                } else if ("PRE_PAGE".equals(type)) {
                    mCurrSelectIdx--;
                    boolean hasPre = true;
                    boolean hasNext = false;
                    if (mCurrSelectIdx < (totalSize - 1)) {
                        hasNext = true;
                    }
                    if (selectCallback != null) {
                        selectCallback.onPre();
                    }
                    if (mCurrSelectIdx == 0) {
                        if (selectCallback != null) {
                            selectCallback.onPrePage();
                        }
                        hasPre = false;
                    }
                    registerWakeupKws(hasNext, hasPre, totalSize, getCurrActiveItem(), selectCallback);
                    speakText("已切换为上一页", null);
                }
            }
        };
        callback.addCommand("SURE", "开始导航", "确认");
        callback.addCommand("CANCEL", "取消");
        callback.addCommand("REMOVE_NAME", "小欧小欧");
        if (hasNext) {
            callback.addCommand("NEXT_PAGE", "下一页");
        }
        if (hasPre) {
            callback.addCommand("PRE_PAGE", "上一页");
        }
        if (selectCallback != null) {
            selectCallback.onNotifyKwsUpdate(getNoticeText(hasNext, hasPre));
        }
        TXZAsrManager.getInstance().useWakeupAsAsr(callback);
        mHasRegistered = true;
    }

    Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.logd("timeout...");
            selectCancel();
        }
    };

    /**
     * 开始倒计时
     */
    private void startCheckTimeout() {
        AppLogic.removeBackGroundCallback(mTimeoutRunnable);
        AppLogic.runOnBackGround(mTimeoutRunnable, 2 * 60 * 1000);
    }

    /**
     * 清除超时
     */
    private void removeTimeout() {
        AppLogic.removeBackGroundCallback(mTimeoutRunnable);
    }

    private int mSpeakId = TXZTtsManager.INVALID_TTS_TASK_ID;

    private void speakText(String text, TXZTtsManager.ITtsCallback callback) {
        TXZTtsManager.getInstance().cancelSpeak(mSpeakId);
        mSpeakId = TXZTtsManager.getInstance().speakText(text, callback);
    }

    private String getNoticeText(boolean hasNext, boolean hasPre) {
        if (!hasNext && !hasPre) {
            return "说“<b>开始导航</b>”可进入导航状态，“<b>取消</b>”关闭";
        }
        if (hasNext) {
            return "说“<b>开始导航</b>”可进入导航状态，“<b>取消</b>”关闭";
        }
        if (hasPre) {
            return "说“<b>开始导航</b>”可进入导航状态，“<b>取消</b>”关闭";
        }
        return "";
    }

    private boolean bInterruptNav; // 是否打断导航操作

    private void selectSure(final PoiIssuedTool.IssuedPoi currItem) {
        mCurrSelectIdx = 0;
        removeTimeout();
        removeAllNotifyItems();
        recoverPoiIssuedWakeupAsr();
        TXZTtsManager.getInstance().cancelSpeak(mSpeakId);
        bInterruptNav = false;
        TXZTtsManager.getInstance().speakText("将为您开始导航", new TXZTtsManager.ITtsCallback() {
            @Override
            public void onEnd() {
                if (bInterruptNav) {
                    removeNotifyItem(currItem);
                    LogUtil.logd("selectSure interrupted");
                    return;
                }
                LaunchManager.getInstance().launchBackWithStack();
                selectItem(currItem);
            }
        });
    }

    private void selectCancel() {
        if (currNotifyItems != null && !currNotifyItems.isEmpty()) {
            mCurrSelectIdx = 0;
            removeTimeout();
            removeAllNotifyItems();
            recoverPoiIssuedWakeupAsr();
            speakText("已取消", null);
            LaunchManager.getInstance().launchBackWithStack();
        }
    }

    private void selectItem(PoiIssuedTool.IssuedPoi currItem) {
        if (currItem != null) {
            navIssuedPoi(currItem);
        }
    }

    public interface WakeupAsrSelectCallback {
        void onNext();

        void onPre();

        void onPrePage();

        void onLastPage();

        void onNotifyKwsUpdate(String noticeText);
    }

    public void recoverPoiIssuedWakeupAsr() {
//        if (mHasRegistered) {
        TXZAsrManager.getInstance().recoverWakeupFromAsr(POIISSUED_TASK_ID);
        mHasRegistered = false;
//        }
    }

    private void navIssuedPoi(PoiIssuedTool.IssuedPoi poi) {
        double lat = poi.lat;
        double lng = poi.lng;
        String name = poi.poiName;
        String addr = poi.poiAddress;
        navigate(lat, lng, name, addr);
    }

    public void navigate(double lat, double lng, String targetName, String targetAddress) {
        Poi poi = new Poi();
        poi.setLat(lat);
        poi.setLng(lng);
        poi.setName(targetName);
        poi.setGeoinfo(targetAddress);
        TXZNavManager.getInstance().navToLoc(poi);
    }

    @Override
    public void init() {
        super.init();
        // 同行者相关
        mCurrNavComponent = new NavTXZComponent();
        mCurrNavComponent.init();
        mCurrNavComponent.setHUDInfoListener(updateListener);
        mCurrNavComponent.setNavStateListener(navStateListener);

        if (mCurrNavComponent instanceof PoiIssuedTool) {
            ((PoiIssuedTool) mCurrNavComponent).setPoiIssuedListener(poiIssuedListener);
            ((PoiIssuedTool) mCurrNavComponent).reqPois();
        }

        // 高德相关
        mCurrNavGdComponent = new NavGdComponent();
        mCurrNavGdComponent.init();

        // 设置路况拦截工具
        TXZNavManager.getInstance().setTmcTool(mTmcTool);

        TXZResourceManager.getInstance().setTextResourceString("RS_WX_RECEIVE_NAV_HINT", "收到1条来自%NAME%发来的导航消息，目的地：%TAR%");
        TXZResourceManager.getInstance().setTextResourceString("RS_WX_RECEIVE_NAV", "收到1条来自%NAME%发来的导航消息，目的地%TAR%，导航还是取消");

        hasAnjixingLogin = PreferenceUtil.getInstance().getBoolean(PreferenceUtil.KEY_ANJIXING_LOGIN, false);

        if (DebugCfg.PASS_ANJIXING_DEBUG) {
            hasAnjixingLogin = true;
        }
    }

    private TXZNavManager.TmcTool.OperateListener mTmcOperationListener;
    private boolean hasInvokeSmartTraffic; // 是否执行过SmartTraffic

    private TXZNavManager.TmcTool mTmcTool = new TXZNavManager.TmcTool() {
        @Override
        public void setOperateListener(OperateListener listener) {
            mTmcOperationListener = listener;
        }

        @Override
        public boolean needWait() {
            if (!hasAnjixingLogin || !hasInvokeSmartTraffic) {
                LogUtil.logd("Traffic needWait true");
                return true;
            }
            LogUtil.logd("Traffic needWait false");
            return false;
        }

        @Override
        public boolean isIgnore() {
            return false;
        }

        @Override
        public boolean onSmartTraffic(EquipmentManager.SmartTravel travel) {
            return false;
        }

        @Override
        public boolean onViewDataUpdate(String title, String data) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("title", title);
                jsonObject.put("data", data);
                LaunchManager.getInstance().launchTmc(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_TRAFFIC);
            return true;
        }

        @Override
        public void onDismissDialog() {
            LaunchManager.getInstance().launchBack();
        }
    };

    public void dismissTmc() {
        if (mTmcOperationListener != null) {
            mTmcOperationListener.dismiss();
        }
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{
                EventTypes.EVENT_BOOT_OPERATION_COMPLETE,
                EventTypes.EVENT_LAUNCH_ONRESUME,
                EventTypes.EVENT_ANJIXING_LOGIN,
                EventTypes.EVENT_ANJIXING_LOGOUT,
                EventTypes.EVENT_BOOT_INVALID,
                EventTypes.EVENT_WX_RECORD_SEND_SUCCESS,
                EventTypes.EVENT_WX_RECORD_SEND_FAILED,
                EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP,
                EventTypes.EVENT_WX_MSG_NOTIFY_DISMISS
        };
    }

    @Override
    protected void onEvent(String eventType) {
        switch (eventType) {
            case EventTypes.EVENT_BOOT_OPERATION_COMPLETE:
                startNotifyTraffic();
            case EventTypes.EVENT_WX_MSG_NOTIFY_DISMISS:
            case EventTypes.EVENT_BOOT_INVALID:
                needWaitBootComplete = false;
            case EventTypes.EVENT_WX_RECORD_SEND_SUCCESS:
            case EventTypes.EVENT_WX_RECORD_SEND_FAILED:
                AppLogic.removeUiGroundCallback(mRunnableProcessPoiIssuedList);
                AppLogic.runOnUiGround(mRunnableProcessPoiIssuedList);
                break;
            case EventTypes.EVENT_LAUNCH_ONRESUME:
                if (mCurrNavGdComponent != null) {
                    mCurrNavGdComponent.releaseWakeupAsr();
                }
                break;
            case EventTypes.EVENT_ANJIXING_LOGIN:
                hasAnjixingLogin = true;
                break;
            case EventTypes.EVENT_ANJIXING_LOGOUT:
                hasAnjixingLogin = false;
                break;
            case EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP:
                selectCancel();
                TXZNavManager.getInstance().exitNav(); // 休眠时退出导航
                needWaitBootComplete = true;
                hasInvokeSmartTraffic = false;
                break;
        }
    }

    // 开始响应路况早晚报
    private void startNotifyTraffic() {
        if (mTmcOperationListener != null) {
            LogUtil.logd("startNotifyTraffic");
            mTmcOperationListener.startNotifyTraffic();
            hasInvokeSmartTraffic = true;
        }
    }

    public boolean isFocus() {
        if (mCurrNavComponent != null) {
            return mCurrNavComponent.isFocus();
        }
        return false;
    }

    public boolean isInNav() {
        if (mCurrNavComponent != null) {
            return mCurrNavComponent.isInNav();
        }
        return false;
    }

    public boolean isUseHintShowing (){
        if (mCurrNavGdComponent != null) {
            return mCurrNavGdComponent.isUseHintShowing();
        }
        return false;
    }

    public void enterNav() {
        TXZNavManager.getInstance().enterNav();
    }

    public boolean isBackgroundRunning() {
        if (mCurrNavComponent != null) {
            return mCurrNavComponent.isBackgroundRunning();
        }
        return false;
    }

    public void addHUDUpdateListener(NavAppComponent.HUDUpdateListener listener) {
        LogUtil.logd("addHUDUpdateListener:" + listener + ",mCacheInfo:" + mCacheInfo);
        synchronized (mUpdateListeners) {
            if (mUpdateListeners.contains(listener)) {
                return;
            }
            mUpdateListeners.add(listener);
        }
        listener.onHudUpdate(mCacheInfo);
    }

    public void removeHUDUpdateListener(NavAppComponent.HUDUpdateListener listener) {
        LogUtil.logd("removeHUDUpdateListener:" + listener);
        synchronized (mUpdateListeners) {
            if (mUpdateListeners.contains(listener)) {
                mUpdateListeners.remove(listener);
            }
        }
    }


    public String getRemainTime(int rt) {
        if (rt <= 0) {
            return "";
        }

        if (rt > 60) {
            if (rt >= 3600) {
                int r = (int) (rt % 3600);
                int h = (int) (rt / 3600);
                int m = r / 60;
                return h + "小时" + (m > 0 ? m + "分钟" : "");
            } else {
                return (rt / 60) + "分钟";
            }
        } else {
            return rt + "秒";
        }
    }

    public String getRemainDistance(int distance) {
        if (distance <= 0) {
            return "";
        }

        if (distance > 1000) {
            return (Math.round(distance / 100.0) / 10.0) + "公里";
        } else {
            return distance + "米";
        }
    }

    public boolean isGaodeAlongWayError() {
        return mCurrNavGdComponent != null && mCurrNavGdComponent.isGaodeAlongWayError();
    }
}