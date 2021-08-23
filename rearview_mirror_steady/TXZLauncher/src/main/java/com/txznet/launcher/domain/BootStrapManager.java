package com.txznet.launcher.domain;

import android.os.SystemClock;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.launcher.cfg.DebugCfg;
import com.txznet.launcher.data.entity.BindInfoResp;
import com.txznet.launcher.data.http.ApiClient;
import com.txznet.launcher.domain.app.PackageManager;
import com.txznet.launcher.domain.guide.GuideManager;
import com.txznet.launcher.domain.login.LoginManager;
import com.txznet.launcher.domain.nav.NavManager;
import com.txznet.launcher.domain.notification.TodayNoticeManager;
import com.txznet.launcher.domain.settings.SettingsManager;
import com.txznet.launcher.domain.upgrade.UpgradeManager;
import com.txznet.launcher.domain.voip.VoipManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.utils.DeviceUtils;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.launcher.utils.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZTtsManager;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * Created by meteorluo on 2018/2/14.
 */

public class BootStrapManager extends BaseManager {
    private static BootStrapManager sInstance = new BootStrapManager();
    private boolean isStartUnInvoke = true; // 防止多次触发start
    private boolean isFirstLaunchToday; // 是否当天自然日首次启动
    private boolean bDisableRefreshTime; // 禁止刷新时间
    private int mLastTtsId = TtsUtil.INVALID_TTS_TASK_ID;
    private boolean mBootOperationComplete = false; // 是否开机流程已结束

    private BootStrapManager() {
    }

    public static BootStrapManager getInstance() {
        return sInstance;
    }

    @Override
    public void init() {
        super.init();

        /*
            启动时间戳小于上次的时间戳或两次偏差在一定的范围之内<10)，则认为是开机重启
            反之则，认为是kill的。
            短时间连续crash - 当开机重启
         */
        boolean isValidBoot; // 是否正常启动
        long lastElapsedRealtime = PreferenceUtil.getInstance().getLong(PreferenceUtil.KEY_ELAPSED_REAL_TIME, 0L);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        // TODO: 2018/8/6 这个判断是否有问题？ 重复了吧。
        if (elapsedRealtime < lastElapsedRealtime ||
                (lastElapsedRealtime - elapsedRealtime > 0 && lastElapsedRealtime - elapsedRealtime < 10 * 1000)) {
            isValidBoot = true;
        } else {
            isValidBoot = false;
        }
        PreferenceUtil.getInstance().setLong(PreferenceUtil.KEY_ELAPSED_REAL_TIME, elapsedRealtime);
        LogUtil.logd("isValidBoot=" + isValidBoot);

        TXZAsrManager.getInstance().useWakeupAsAsr(new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public String getTaskId() {
                return "GLOBAL_REMOVE_WAKEUP";
            }

            @Override
            public boolean needAsrState() {
                return false;
            }
        }.addCommand("REMOVE", "小欧小欧"));

        if (!isValidBoot) {
            NavManager.getInstance().recoverPoiIssuedWakeupAsr();
            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_INVALID);
        }

        // 调试用
        if (DebugCfg.TODAY_NOTICE_DEBUG) {
            isFirstLaunchToday = true;
            mPrivateFlag |= FLAG_IS_BOOT_COMPLETE;
        }
        if (DebugCfg.PASS_ANJIXING_DEBUG) {
            mPrivateFlag |= FLAG_IS_ANJIXING_LOGIN;
        }
    }

    private boolean hasCheckBindStatus; // 是否已经检测过安吉星的绑定状态
    private boolean shouldInvokeStartAfterCheck; // 是否需要在同步完绑定状态后执行start()方法

    // 刷新安吉星状态
    private void refreshAnjixingBindStatus() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext(DeviceUtils.getDeviceID());
                e.onComplete();
            }
        }).flatMap(new Function<String, ObservableSource<BindInfoResp>>() {
            @Override
            public ObservableSource<BindInfoResp> apply(String s) throws Exception {
                return ApiClient.getInstance().getApiService().getBindInfo(s).toObservable();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BindInfoResp>() {
                    @Override
                    public void accept(BindInfoResp bindInfoResp) throws Exception {
                        if (bindInfoResp.errorCode == null || "E0000".equals(bindInfoResp.errorCode)) {
                            PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_ANJIXING_LOGIN, true);
                            if (bindInfoResp.userInfo != null) {
                                PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_ANJIXING_ACC_NAME, bindInfoResp.userInfo.name);
                                PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_ANJIXING_ACC_BIRTHDAY, bindInfoResp.userInfo.birthday);
                            }
                            if (bindInfoResp.vehicleInfo != null) {
                                PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_ANJIXING_ACC_VEHICLE_LICENSE, bindInfoResp.vehicleInfo.vehicleLicense);
                            }
                            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_ANJIXING_LOGIN);
                        }else {
                            mPrivateFlag &= ~FLAG_IS_ANJIXING_LOGIN;
                            LoginManager.getInstance().clearSaveLoginData();
                        }
                        hasCheckBindStatus = true;
                        if (shouldInvokeStartAfterCheck) {
                            shouldInvokeStartAfterCheck = false;
                            AppLogic.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    start();
                                }
                            }, 3000);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.e("anjixing getBindInfo throwable: " + throwable);
                        if (throwable instanceof HttpException) { // 未绑定
                            mPrivateFlag &= ~FLAG_IS_ANJIXING_LOGIN;
                            LoginManager.getInstance().clearSaveLoginData();
                        }
                        hasCheckBindStatus = true;
                        if (shouldInvokeStartAfterCheck) {
                            shouldInvokeStartAfterCheck = false;
                            AppLogic.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    start();
                                }
                            }, 3000);
                        }
                    }
                });
    }

    /**
     * 执行开机首次启动的逻辑
     */
    public void start() {
        // 1. 开机欢迎语
        if (!isStartUnInvoke) { // 因与事件框架的时序问题，挪到MainActivity的onResume执行，可多次触发
            return;
        }
        LogUtil.logd("hasSim=" + SettingsManager.getInstance().hasSimCard());
        if (SettingsManager.getInstance().hasSimCard() && !hasCheckBindStatus) { // 有插SIM卡，则先等待安吉星状态同步
            // 刷新安吉星绑定状态
            shouldInvokeStartAfterCheck = true;
            return;
        }

        isStartUnInvoke = false;

        String todayTxt = StringUtils.formatDate(System.currentTimeMillis());
        String launchTime = PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_LAUNCH_TIME, null);
        if (launchTime == null || !todayTxt.equals(launchTime)) { // 当天自然日首次启动
            isFirstLaunchToday = true;
        }
        PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_LAUNCH_TIME, todayTxt);

        LaunchManager.getInstance().enableTime(false);
        if (PreferenceUtil.getInstance().getBoolean(PreferenceUtil.KEY_WELCOME_STATE, true)) {
            sayWelcome();
        } else {
            doLoginOrTodayNotice();
        }
    }

    /**
     * 开机流程结束后要执行的操作
     * 由于有多个地方要用这个，就提取出来。
     */
    public void notifyBootOperationComplete(){
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_OPERATION_COMPLETE);
        LaunchManager.getInstance().enableTime(true, true);
    }

    public boolean isBootOperationComplete() {
        return mBootOperationComplete;
    }

    public void setIsBootOperationComplete(boolean bootOperationComplete) {
        mBootOperationComplete = bootOperationComplete;
    }

    public boolean isDisableRefreshTime(){
        return bDisableRefreshTime;
    }

    // 进行开机欢迎语提示
    private void sayWelcome() {
        String welcomeTxt;
        String welcomeTts; // tts播报的欢迎语
        // 判断是否登录了安吉星账号
        boolean hasLogin = PreferenceUtil.getInstance().getBoolean(PreferenceUtil.KEY_ANJIXING_LOGIN, false);
        long curTimeMill = System.currentTimeMillis();
        if (hasLogin) {
            mPrivateFlag |= FLAG_IS_ANJIXING_LOGIN;
            String nick = PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_ANJIXING_ACC_NAME, ""); // 获取账号信息
            welcomeTxt = StringUtils.getWelcomeText(curTimeMill, nick);
            if (TextUtils.isEmpty(nick) || StringUtils.isFullChinese(nick)) {
                welcomeTts = welcomeTxt;
            } else { // 若账号昵称里含有除中文之外的字符，则仅播报前半段问候
                welcomeTts = welcomeTxt.substring(0, welcomeTxt.indexOf(","));
            }
        } else {
            welcomeTxt = StringUtils.getWelcomeText(curTimeMill, null);
            welcomeTts = welcomeTxt;
        }
        welcomeTts = welcomeTts.replaceAll("Hello", "哈喽");
        if (isFirstLaunchToday) {
            String holidayTips = StringUtils.getHolidayTips(curTimeMill);
            welcomeTts += holidayTips;
        }

        if (VoipManager.getInstance().hasCallAfterBoot() // 呼叫过安吉星跳过欢迎语播报流程
                || (mPrivateFlag & FLAG_IS_FOREGROUND) == 0) { // 非前台
            LaunchManager.getInstance().enableTime(true, true);
            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_OPERATION_COMPLETE);
            // 这里如果是非前台的情况下，会跳过登录流程。不过好像是在onResume的事件中检查了是否有登录。所以这个不影响
            return;
        }

        bDisableRefreshTime = true;
        LaunchManager.getInstance().launchHello(welcomeTxt);
        mLastTtsId = TXZTtsManager.getInstance().speakText(welcomeTts, new TXZTtsManager.ITtsCallback() {
            @Override
            public void onEnd() {
                bDisableRefreshTime = false;
            }

            @Override
            public void onSuccess() {
                doLoginOrTodayNotice();
            }

            @Override
            public void onCancel() {
                LaunchManager.getInstance().enableTime(true, true);
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_OPERATION_COMPLETE);
            }

            @Override
            public void onError(int iError) {
                LaunchManager.getInstance().enableTime(true, true);
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_OPERATION_COMPLETE);
            }
        });
    }

    private void requestTodayNotice() {
        if (!PreferenceUtil.getInstance().getBoolean(PreferenceUtil.KEY_TODAY_NOTICE_STATE, true)  // 关闭配置
                || (mPrivateFlag & FLAG_IS_FOREGROUND) == 0 // 非前台
                || !isFirstLaunchToday  // 非今日首次启动
                || TodayNoticeManager.getInstance().getLoadedData().isEmpty() // 没有数据
                || VoipManager.getInstance().hasCallAfterBoot()) { // 欢迎语播报前播报过安吉星
//            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_OPERATION_COMPLETE);
//            LaunchManager.getInstance().enableTime(true, true);
            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_TODAY_NOTICE_COMPLETE);
            return;
        }
        LaunchManager.getInstance().launchNotification();
    }

    /**
     * 系统升级的逻辑
     */
    private void checkSystemUpgrade(){
        // 欢迎语播报前播报过安吉星,不展示升级框
        if (VoipManager.getInstance().hasCallAfterBoot()) {
            notifyBootOperationComplete();
            return;
        }
        // 需要升级提示
        if (UpgradeManager.getInstance().checkAndUpgrade()) {
            // 什么都不做，由UpgradeManager去处理
        }else {// 不需要升级提示
            notifyBootOperationComplete();
        }
    }

    // 登录操作
    private void showLoginQr() {
        LaunchManager.getInstance().launchLoginModule(null);
    }

    private void doLoginOrTodayNotice() {
        // FIXME 是否会与离线时收到的推送内容踩时点
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (checkShowLogin()) {
                    return;
                }
                LaunchManager.getInstance().enableTime(true, true);
                LaunchManager.getInstance().launchDesktop();
                // 2. 请求今日数据
                requestTodayNotice();
            }
        }, 1000);
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{
                EventTypes.EVENT_TXZ_INIT_SUCCESS,
                EventTypes.EVENT_LAUNCH_ONRESUME,
                EventTypes.EVENT_ANJIXING_LOGIN,
                EventTypes.EVENT_ANJIXING_LOGOUT,
                EventTypes.EVENT_BOOT_COMPLETE,
                EventTypes.EVENT_BOOT_OPERATION_COMPLETE,
                EventTypes.EVENT_DEVICE_SIM_READY,
                EventTypes.EVENT_GUIDE_COMPLETE,
                EventTypes.EVENT_DEVICE_POWER_WAKEUP,
                EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP,
                EventTypes.EVENT_GUIDE_INTERRUPT,
                EventTypes.EVENT_BOOT_TODAY_NOTICE_COMPLETE,
        };
    }

    @Override
    protected void onEvent(String eventType) {
        LogUtil.logd("onEvent eventType=" + eventType);
        switch (eventType) {
            case EventTypes.EVENT_TXZ_INIT_SUCCESS:
                if ((mPrivateFlag & FLAG_IS_TXZ_INIT_SUCCESS) != 0) {
                    break;
                }
                mPrivateFlag |= FLAG_IS_TXZ_INIT_SUCCESS;
                checkStatus();
                break;
            case EventTypes.EVENT_LAUNCH_ONRESUME:
                mPrivateFlag |= FLAG_IS_FOREGROUND;
                if (isStartUnInvoke) {
                    checkStatus();
                } else if (!checkShowLogin() && !isDisableRefreshTime()) {
                    LaunchManager.getInstance().enableTime(true, false);
                    LaunchManager.getInstance().dismissDialogWin();
                }
                break;
            case EventTypes.EVENT_ANJIXING_LOGIN:
                if ((mPrivateFlag & FLAG_IS_ANJIXING_LOGIN) != 0) {
                    break;
                }
                mPrivateFlag |= FLAG_IS_ANJIXING_LOGIN;
                if (!checkShowLogin()) {
                    TXZAsrManager.getInstance().recoverWakeupFromAsr("task_id_login");
                    LaunchManager.getInstance().enableTime(true, true);
                    LaunchManager.getInstance().launchDesktop();
                }
                break;
            case EventTypes.EVENT_ANJIXING_LOGOUT:
                mPrivateFlag &= ~FLAG_IS_ANJIXING_LOGIN;
                checkShowLogin();
                break;
            case EventTypes.EVENT_BOOT_COMPLETE:
                mPrivateFlag |= FLAG_IS_BOOT_COMPLETE;
                checkStatus();
                break;
            case EventTypes.EVENT_BOOT_OPERATION_COMPLETE:
                TXZAsrManager.getInstance().recoverWakeupFromAsr("GLOBAL_REMOVE_WAKEUP");
                setIsBootOperationComplete(true);
                break;
            case EventTypes.EVENT_DEVICE_SIM_READY: // 有网之后开始请求数据
                if (!isStartUnInvoke) {
                    return;
                }
                if (!VoipManager.getInstance().hasCallAfterBoot()) { // 安吉星呼叫过会打断欢迎语播报
                    TodayNoticeManager.getInstance().prepareData(TXZConfigManager.getInstance().isInitedSuccess());
                }
                refreshAnjixingBindStatus();
                break;
            case EventTypes.EVENT_GUIDE_COMPLETE:
                mPrivateFlag |= FLAG_IS_GUIDE_COMPLETE;
                checkStatus();
                break;
            case EventTypes.EVENT_DEVICE_POWER_WAKEUP:
                init();
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_COMPLETE);
                LaunchManager.getInstance().enableTime(false);
                if ((mPrivateFlag & FLAG_IS_FOREGROUND) != 0) {
                    boolean appInstalled = PackageManager.getInstance().checkAppInstalled(GuideManager.GUIDE_PKG_NAME);
                    LogUtil.logi(GuideManager.GUIDE_PKG_NAME+" isInstalled: "+appInstalled);
                    if (appInstalled) {
                        GuideManager.getInstance().sendStartGuideBroadcast(GlobalContext.get(), false);
                    }else {
                        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_GUIDE_COMPLETE);
                    }
                } else {
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_GUIDE_COMPLETE);
                }
                break;
            case EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP:
                TXZTtsManager.getInstance().cancelSpeak(mLastTtsId);
                mPrivateFlag &= ~FLAG_IS_GUIDE_COMPLETE;
                mPrivateFlag &= ~FLAG_IS_TXZ_INIT_SUCCESS;
                hasCheckBindStatus = false;
                isStartUnInvoke = true;
                setIsBootOperationComplete(false);
                isFirstLaunchToday = false; // 复位首次开机的状态
                break;
            case EventTypes.EVENT_GUIDE_INTERRUPT: // 开机引导被打断的情况
                LaunchManager.getInstance().enableTime(true, false);
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_OPERATION_COMPLETE);
                break;
            case EventTypes.EVENT_BOOT_TODAY_NOTICE_COMPLETE:
                checkSystemUpgrade();
                break;
        }
    }

    /**
     * Launcher是否在桌面
     */
    public static final int FLAG_IS_FOREGROUND = 0x0001;
    /**
     * TXZ是否初始化成功
     */
    public static final int FLAG_IS_TXZ_INIT_SUCCESS = 0x0002;

    /**
     * 安吉星是否登录
     */
    public static final int FLAG_IS_ANJIXING_LOGIN = 0x0004;

    /**
     * 是否收到开机广播
     */
    public static final int FLAG_IS_BOOT_COMPLETE = 0x0008;

    /**
     * 开机引导是否结束
     */
    public static final int FLAG_IS_GUIDE_COMPLETE = 0x0010;

    private int mPrivateFlag;

    /**
     * 当app在前台、core初始化成功和接受到过开机广播时，执行start方法
     */
    private void checkStatus() {
        LogUtil.logd("checkStatus, _a=" + ((mPrivateFlag & FLAG_IS_FOREGROUND) != 0)
                + ", b=" + ((mPrivateFlag & FLAG_IS_TXZ_INIT_SUCCESS) != 0)
                + ", c=" + ((mPrivateFlag & FLAG_IS_BOOT_COMPLETE) != 0)
                + ", d=" + ((mPrivateFlag & FLAG_IS_GUIDE_COMPLETE) != 0));
        if ((mPrivateFlag & FLAG_IS_TXZ_INIT_SUCCESS) != 0
                        && (mPrivateFlag & FLAG_IS_BOOT_COMPLETE) != 0
                        && (mPrivateFlag & FLAG_IS_GUIDE_COMPLETE) != 0) {
            start();
        }
    }

    private boolean checkShowLogin() {
        if ((mPrivateFlag & FLAG_IS_ANJIXING_LOGIN) == 0) {
            showLoginQr();
            return true;
        }
        return false;
    }
}