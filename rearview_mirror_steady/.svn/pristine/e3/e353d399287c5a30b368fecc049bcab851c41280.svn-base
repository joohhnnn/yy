package com.txznet.launcher.domain.settings;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.R;
import com.txznet.launcher.data.entity.BaseResp;
import com.txznet.launcher.data.http.ApiClient;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.domain.BootStrapManager;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.fm.FmManager;
import com.txznet.launcher.domain.upgrade.UpgradeManager;
import com.txznet.launcher.domain.voip.VoipManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.utils.DeviceUtils;
import com.txznet.launcher.utils.IntentUtils;
import com.txznet.launcher.utils.NetworkUtils;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.launcher.utils.StringUtils;
import com.txznet.launcher.widget.dialog.SysConfirmDialog;
import com.txznet.launcher.widget.dialog.SysInfoDialog;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZCallManager;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.TXZPowerManager;

import java.lang.reflect.Method;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * 设备设置相关操作
 * Created by ASUS User on 2018/5/17.
 */

public class SettingsManager extends BaseManager {

    /**
     * 打开wifi热点
     */
    public static final int TYPE_SETTINGS_AP_CTRL_OPEN = 1;
    /**
     * 关闭wifi热点
     */
    public static final int TYPE_SETTINGS_AP_CTRL_CLOSE = 2;
    /**
     * 刷新wifi热点
     */
    public static final int TYPE_SETTINGS_AP_CTRL_REFRESH = 3;
    /**
     * 刷新wifi热点
     */
    public static final int TYPE_SETTINGS_AP_CTRL_SHOW = 4;

    private static SettingsManager instance;

    public static SettingsManager getInstance() {
        if (instance == null) {
            synchronized (SettingsManager.class) {
                if (instance == null) {
                    instance = new SettingsManager();
                }
            }
        }
        return instance;
    }

    private boolean bCallSos; // 一个标志位，控制是否呼叫客服

    BroadcastReceiver mSettingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.logd("receive:" + intent.getAction());
            switch (intent.getAction()) {
                case SettingsConst.ACTION_SETTINGS_ACC_STATE:
                    boolean accOn = intent.getBooleanExtra("state", true);
                    if (accOn) {
                        TXZPowerManager.getInstance().reinitTXZ(new Runnable() {
                            @Override
                            public void run() {
                                TXZPowerManager
                                        .getInstance()
                                        .notifyPowerAction(
                                                TXZPowerManager.PowerAction.POWER_ACTION_WAKEUP);
                                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_DEVICE_POWER_WAKEUP);
                                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_TXZ_INIT_SUCCESS);
                                // fixme 这样子写代码不好，但是没有时间了，讲道理这个应该在comm中实现。
                                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.selector.poi.useDefaultCoexistAsrAndWakeup", (true+"").getBytes(), null);
                            }
                        });
                    } else {
                        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP);

                        TXZPowerManager.getInstance().notifyPowerAction(
                                TXZPowerManager.PowerAction.POWER_ACTION_BEFORE_SLEEP);
                        TXZPowerManager.getInstance().releaseTXZ();
                    }
                    break;
                case SettingsConst.ACTION_SETTINGS_KEY_EVENT:
                    /*
                        code = 0 power, code = 1 红键 code = 2 蓝键
                        event = 0 key down, event = 1 key up
                        调整为松开时才触发，避免跟恢复出厂设置冲突(7/3,zackzhou已同步产品)
                     */
                    int code = intent.getIntExtra("code", -1);
                    int event = intent.getIntExtra("event", -1);
                    if (code == 1) { // red
                        if (event == 0) { // down
                            bCallSos = true;
                            if (VoipManager.getInstance().getCallStatus()== TXZCallManager.CallTool.CallStatus.CALL_STATUS_IDLE) {
                                ctrlScreen(true);
                            }
                        } else if (event == 1) { // up
                            if (bCallSos) {
                                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_DEVICE_RED_BUTTON_PRESSED);
                            }
                        }
                    } else if (code == 2) { // blue
                        if (event == 0) {// down
                            if (VoipManager.getInstance().getCallStatus()== TXZCallManager.CallTool.CallStatus.CALL_STATUS_IDLE) {
                                ctrlScreen(true);
                            }
                        }
                        if (event == 1) { // up
                            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_DEVICE_BLUE_BUTTON_PRESSED);
                        }
                    }
                    break;
                case SettingsConst.ACTION_WIFI_AP_STATE_CHANGED:
                    int cstate = intent.getIntExtra(SettingsConst.EXTRA_WIFI_AP_STATE, -1);
                    if (cstate == SettingsConst.WIFI_AP_STATE_ENABLED) {
                        LaunchManager.getInstance().refreshAP(true);
                    } else {
                        LaunchManager.getInstance().refreshAP(false);
                    }
                    break;
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    LaunchManager.getInstance().refreshLocation(isGPSEnabled());
                    break;
                case SettingsConst.ACTION_GET_FM_TX:
                    float curFMFreq = intent.getFloatExtra("frequency", FmManager.getInstance().getCurFMFreq());
                    boolean isFmOpen=intent.getBooleanExtra("power", false);
                    FmManager.getInstance().setCurFMFreq(curFMFreq);
                    FmManager.getInstance().setFmIsOpen(isFmOpen);
                    LaunchManager.getInstance().refreshFM(isFmOpen);
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_FM_RECEIVE_STATE);
                    break;
                case SettingsConst.ACTION_RECORY_FACTORY:
                    LogUtil.e("isBootOperationComplete: " + BootStrapManager.getInstance().isBootOperationComplete() +
                            " ,isSystemUpgrading: " + UpgradeManager.getInstance().isSystemUpgrading());
                    if (!BootStrapManager.getInstance().isBootOperationComplete() ||UpgradeManager.getInstance().isSystemUpgrading()) {
                        /*
                         * 如果开机流程没有结束，不让执行恢复出厂设置。
                         * 如果系统升级正在下载中，不执行恢复出厂设置。
                         */
                    }else {
                        bCallSos = false;
                        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_DEVICE_RECORY_FACTORY);
                        ctrlScreen(true);
                        masterClear();
                    }
                    break;
                case SettingsConst.ACTION_SIM_STATE_CHANGED:
                    hasSimCard = hasSimCardInner();
                    if (!hasSimCard) {
                        LaunchManager.getInstance().refreshSignalIcon(-1);
                        LaunchManager.getInstance().refreshSignalText("");
                        // 按需求，当sim卡不在的时候关闭热点。sim如果卡的不够紧，出现非人为拔出导致的识别不到sim卡也会导致热点关闭。概率应该不高
                        ctrlAPType(TYPE_SETTINGS_AP_CTRL_CLOSE);
                        // 如果当时正好是wifi热点界面，将界面改成已关闭。
                        if (LaunchManager.getInstance().isCurrModule(LaunchManager.ViewModuleType.TYPE_SETTINGS_AP)) {
                            JSONBuilder jsonBuilder = new JSONBuilder();
                            jsonBuilder.put("cmd", "ctrl");
                            jsonBuilder.put("type", TYPE_SETTINGS_AP_CTRL_CLOSE);
                            LaunchManager.getInstance().launchAPModule(jsonBuilder.toString());
                        }
                    }
                    break;
                case SettingsConst.ACTION_MCU_UPDATE_STATE:
                    mMcuVersion = intent.getStringExtra("mcuVersion");
                    break;
            }
        }
    };


    private String mMcuVersion;

    /*
       获取Mcu版本信息
     */
    public String getMcuVersion() {
        return mMcuVersion;
    }

    /*
        同步Mcu版本信息
     */
    public void syncMcuVersion() {
        GlobalContext.get().sendBroadcast(new Intent(SettingsConst.ACTION_MCU_OTA_NOTIFY));
    }


    private SysConfirmDialog mMasterClearDialog;
    private SysInfoDialog mCleanInvokeDialog;

    // 恢复出厂设置
    private void masterClear() {
        if (mMasterClearDialog == null) {
            SysConfirmDialog.SysDialogBuildData buildData = new SysConfirmDialog.SysDialogBuildData();
            buildData.setContent("您确定要恢复出厂设置吗");
            buildData.setIcon(R.drawable.ic_master_clear);
            buildData.setHintTts("您确定要恢复出厂设置吗，您可以说确定或取消");
            buildData.setSureText("确定", new String[]{"确定", "是"});
            buildData.setSureText("取消", new String[]{"取消", "否"});
            mMasterClearDialog = new SysConfirmDialog(buildData) {
                @Override
                public void onClickOk() {
                    invokeMasterClear();
                }

                @Override
                public String getReportDialogId() {
                    return "MASTER_CLEAR";
                }

                @Override
                public void onClickCancel() {
                    super.onClickCancel();
                    TtsUtil.speakText("已取消");
                }

                @Override
                public void onShow() {
                    super.onShow();
                    TXZNavManager.getInstance().enableWakeupNavCmds(false);
                }

                @Override
                public void onDismiss() {
                    super.onDismiss();
                    TXZNavManager.getInstance().enableWakeupNavCmds(true);
                }
            };
        }
        mMasterClearDialog.show();
    }

    private void invokeMasterClear() {
        String txt = "正在为您恢复出厂设置，\n请稍等...";
        String tts = "将为您恢复出厂设置";
        SysInfoDialog.SysDialogBuildData buildData = new SysInfoDialog.SysDialogBuildData();
        buildData.setContent(txt);
        buildData.setHintTts(tts);
        if (mCleanInvokeDialog == null) {
            mCleanInvokeDialog = new SysInfoDialog(buildData) {
                @Override
                public String getReportDialogId() {
                    return "MASTER_CLEAR_INVOKE";
                }

                @Override
                protected void onEndTts() {
                    super.onEndTts();
                    ApiClient.getInstance().getApiService().unbind(DeviceUtils.getDeviceID())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<BaseResp>() {
                                @Override
                                public void accept(BaseResp baseResp) throws Exception {
                                    if (baseResp != null && baseResp.errorCode.equals("E0000")) {
                                        LogUtil.logd("master_clear#send android.intent.action.TXZ_MASTER_CLEAR");
                                        Intent intent = new Intent("android.intent.action.TXZ_MASTER_CLEAR");
                                        GlobalContext.get().sendBroadcast(intent);
                                    } else {
                                        mCleanInvokeDialog.dismiss("auto dismiss");
                                        showErrorInfo();
                                    }
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    if (throwable instanceof HttpException) { // 本身就未绑定的情况下，会返回404
                                        if (((HttpException) throwable).code() == 404) {
                                            ResponseBody resp = ((HttpException) throwable).response().errorBody();
                                            if (resp != null && resp.string().contains("E4004")) {
                                                Intent intent = new Intent("android.intent.action.TXZ_MASTER_CLEAR");
                                                GlobalContext.get().sendBroadcast(intent);
                                                return;
                                            }
                                        }
                                    }
                                    mCleanInvokeDialog.dismiss("auto dismiss");
                                    showErrorInfo();
                                }
                            });
                }
            };
        }
        mCleanInvokeDialog.show();
    }

    private void showErrorInfo() {
        LogUtil.logd("master_clear#unbind failed");
        String txt = "网络异常，请稍后再试！";
        SysInfoDialog.SysDialogBuildData buildData = new SysInfoDialog.SysDialogBuildData();
        buildData.setContent(txt);
        buildData.setHintTts(txt);
        buildData.setAutoClose(true);
        new SysInfoDialog(buildData) {
            @Override
            public String getReportDialogId() {
                return "MASTER_CLEAR_ERROR";
            }
        }.show();
    }

    //监听wifi热点状态变化
    @Override
    public void init() {
        super.init();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SettingsConst.ACTION_SETTINGS_ACC_STATE);
        filter.addAction(SettingsConst.ACTION_SETTINGS_KEY_EVENT);
        filter.addAction(SettingsConst.ACTION_WIFI_AP_STATE_CHANGED);
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(SettingsConst.ACTION_GET_FM_TX);
        filter.addAction(SettingsConst.ACTION_RECORY_FACTORY);
        filter.addAction(SettingsConst.ACTION_SIM_STATE_CHANGED);
        filter.addAction(SettingsConst.ACTION_MCU_UPDATE_STATE);
        GlobalContext.get().registerReceiver(mSettingsReceiver, filter);

        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                LaunchManager.getInstance().refreshAP(isWifiApEnabled());
                LaunchManager.getInstance().refreshLocation(isGPSEnabled());
                if (!hasSimCard()) {
                    LaunchManager.getInstance().refreshSignalIcon(-1);
                    LaunchManager.getInstance().refreshSignalText("");
                }
                ((TelephonyManager) GlobalContext.get().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).listen(mPhoneStateListener,
                        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }
        }, 5000);


        syncMcuVersion(); // 启动触发一次mcu版本同步
    }

    /**
     * 打开或关闭wifi热点
     *
     * @param type
     */
    public void ctrlAPType(int type) {
        String name=StringUtils.getWifiName();
        switch (type) {
            case TYPE_SETTINGS_AP_CTRL_OPEN:
                ctrlWifiAp(true, name, PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_WIFI_AP_PSD, PreferenceUtil.DEFAULT_WIFI_AP_PSD));
                break;
            case TYPE_SETTINGS_AP_CTRL_CLOSE:
                ctrlWifiAp(false, name, PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_WIFI_AP_PSD, PreferenceUtil.DEFAULT_WIFI_AP_PSD));
                break;
            case TYPE_SETTINGS_AP_CTRL_SHOW:
                ctrlWifiAp(true, name,PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_WIFI_AP_PSD, PreferenceUtil.DEFAULT_WIFI_AP_PSD));
                break;
            case TYPE_SETTINGS_AP_CTRL_REFRESH:
                String psd = StringUtils.getRandomString(8);
                PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_WIFI_AP_PSD, psd);
                ctrlWifiAp(true, name, psd);
                break;
        }
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("cmd", "ctrl");
        jsonBuilder.put("type", type);
        if (TYPE_SETTINGS_AP_CTRL_CLOSE != type) {
            LaunchManager.getInstance().launchAPModule(jsonBuilder.toString());
        }
    }


    /**
     * 设置FM
     *
     * @param power
     * @param freq
     */
    public void ctrlFM(boolean power, float freq) {
        Intent intent = new Intent(SettingsConst.ACTION_SETTINGS_SET_FM);
        intent.putExtra("save", true);
        intent.putExtra("power", power);
        intent.putExtra("freq", freq);
        sendBroadcast(intent);
    }

    /**
     * wifi开关
     *
     * @param power
     */
    public void ctrlWifi(boolean power) {
//        Intent intent = new Intent(SettingsConst.ACTION_SETTINGS_SET_WIFI);
//        intent.putExtra("power", power);
//        sendBroadcast(intent);
        //使用标准的接口
        try {
            WifiManager mWifiManager = (WifiManager) GlobalContext.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            mWifiManager.setWifiEnabled(power);
        } catch (Exception e) {

        }

    }

    /**
     * 设置wifi热点
     *
     * @param power
     * @param name
     * @param psd
     */
//    public void ctrlWifiAp(boolean power, String name, String psd) {
//        Intent intent = new Intent(SettingsConst.ACTION_SETTINGS_SET_WIFIAP);
//        intent.putExtra("power", power);
//        intent.putExtra("name", name);
//        intent.putExtra("psd", psd);
//        sendBroadcast(intent);
//    }

    /**
     * 设备重启
     */
    public void ctrlReboot() {
        Intent intent = new Intent(SettingsConst.ACTION_SETTINGS_REBOOT_SYSTEM);
        sendBroadcast(intent);
    }

    /**
     * 打开屏幕、关闭屏幕
     *
     * @param power
     */
    public void ctrlScreen(boolean power) {
        LogUtil.e( "ctrlScreen: power=="+power);
        Intent intent = new Intent(SettingsConst.ACTION_SETTINGS_SCREEN_CONTROL_FINAL);
        intent.putExtra("on", power);
        sendBroadcast(intent);
    }

    /**
     * 睡眠命令
     */
    public void ctrlGoToSleep() {
        Intent intent = new Intent(SettingsConst.ACTION_SETTINGS_GOTOSLEEP);
        sendBroadcast(intent);
    }


    /**
     * NONE
     * WEP
     * WPA/WPA2
     * EAP
     * 设置wifi加密方式
     *
     * @param type
     */
    public void ctrlWifiWep(String type) {
        Intent intent = new Intent(SettingsConst.ACTION_SETTINGS_SET_WIFI_WEP);
        intent.putExtra("type", type);
        sendBroadcast(intent);
    }

    private void sendBroadcast(Intent intent) {
        IntentUtils.sendBroadcast(intent);
    }


    /**
     * 检查是否开启Wifi热点
     *
     * @return
     */
    public boolean isWifiApEnabled() {
        try {
            WifiManager mWifiManager = (WifiManager) GlobalContext.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (boolean) method.invoke(mWifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) GlobalContext.get().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean on = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return on;
    }

    Boolean hasSimCard = null;
    Boolean isSimCardReady = null; // SIM卡模块是否准备完毕，可以ping通网络，该变量只有开机的时候有用，中途拔卡没有处理

    public boolean hasSimCard() {
        if (hasSimCard == null) {
            hasSimCard = hasSimCardInner();
        }
        return hasSimCard;

    }

    private boolean hasSimCardInner() {
        boolean hasSimCard = false;
        TelephonyManager tm = (TelephonyManager) GlobalContext.get().getSystemService(Context.TELEPHONY_SERVICE);
        int state = tm.getSimState();
        switch (state) {
            case TelephonyManager.SIM_STATE_UNKNOWN:
            case TelephonyManager.SIM_STATE_ABSENT:
                hasSimCard = false;
                break;
            default:
                hasSimCard = true;
                break;
        }
        return hasSimCard;
    }

    /**
     * 设置wifi热点
     *
     * @param enabled
     * @param name
     * @param password
     */
    private void ctrlWifiAp(boolean enabled, String name, String password) {
        WifiManager mWifiManager = (WifiManager) GlobalContext.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int channel = 3;
        boolean currentState = false;
        Class<? extends WifiManager> mClass = mWifiManager.getClass();
        Method method = null;
        try {
            method = mClass.getMethod("isWifiApEnabled");
            method.setAccessible(true);
            currentState = (boolean) method.invoke(mWifiManager);

            method = mClass.getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration configuration = (WifiConfiguration) method.invoke(mWifiManager);

            if (!enabled) {
                if (currentState) {
                    ctrlWifi(true);
                    method = mClass.getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                    method.invoke(mWifiManager, configuration, false);

                }
            } else {
                if (!currentState) {
                    ctrlWifi(false);
                    configuration.SSID = name;
                    configuration.preSharedKey = password;
                    method = mClass.getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                    method.invoke(mWifiManager, configuration, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @SuppressLint("NewApi")
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
//            LogUtil.logd("onSignalStrengthChanged: SignalStrength=" + signalStrength);
            //getLevel 返回0-4，我们的信号条在0-5

            if (hasSimCard) {
                LaunchManager.getInstance().refreshSignalIcon(Math.round(signalStrength.getLevel() * 5 / 4));

                String netType = "";
                boolean hasConn = false;
                switch (NetworkUtils.getNetWorkType(GlobalContext.get().getApplicationContext())) {
                    case NetworkUtils.NETWORK_WIFI:
                        hasConn = true;
                        break;
                    case NetworkUtils.NETWORK_4G:
                        netType = "4G";
                        hasConn = true;
                        break;
                    case NetworkUtils.NETWORK_3G:
                        netType = "3G";
                        hasConn = true;
                        break;
                    case NetworkUtils.NETWORK_2G:
                        netType = "2G";
                        hasConn = true;
                        break;
                    case NetworkUtils.NETWORK_UNKNOWN:
                        break;
                    case NetworkUtils.NETWORK_NO:
                        break;
                }
                if (isSimCardReady == null && hasConn) {
                    isSimCardReady = true;
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_DEVICE_SIM_READY);
                }
                LaunchManager.getInstance().refreshSignalText(netType);
            } else {
                LaunchManager.getInstance().refreshSignalIcon(-1);
                LaunchManager.getInstance().refreshSignalText("");
            }
        }

    };

    @Override
    protected void onEvent(String eventType) {
        switch (eventType) {
            case EventTypes.EVENT_DEVICE_POWER_WAKEUP:
                hasSimCard = hasSimCardInner();
                if (hasSimCard) { // 重新检测
                    isSimCardReady = null;
                }
                break;
            case EventTypes.EVENT_WAKE_UP:
            case EventTypes.EVENT_POI_ISSUED:
            case EventTypes.EVENT_TRAFFIC:
                hideResetDialog("voice interrupt");
                break;
            case EventTypes.EVENT_DEVICE_RED_BUTTON_PRESSED:
            case EventTypes.EVENT_DEVICE_BLUE_BUTTON_PRESSED:
                hideResetDialog("voip interrupt");
                break;
            case EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP:
                hideResetDialog("device sleep");
                break;
            case EventTypes.EVENT_UPGRADE_APP_SHOW:
                hideResetDialog("show upgrade");
                break;
        }
    }

    /**
     * 隐藏出厂设置确认dialog和正在恢复出厂设置的dialog，并上报原因。
     * 有两个地方要有这个就抽出来了。
     * @param reason dialog隐藏的原因
     */
    private void hideResetDialog(final String reason) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (mMasterClearDialog != null && mMasterClearDialog.isShowing()) {
                    mMasterClearDialog.dismiss(reason);
                }
                if (mCleanInvokeDialog != null && mCleanInvokeDialog.isShowing()) {
                    mCleanInvokeDialog.dismiss(reason);
                }
            }
        });
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{
                EventTypes.EVENT_WAKE_UP,
                EventTypes.EVENT_POI_ISSUED,
                EventTypes.EVENT_TRAFFIC,
                EventTypes.EVENT_DEVICE_POWER_WAKEUP,
                EventTypes.EVENT_DEVICE_RED_BUTTON_PRESSED,
                EventTypes.EVENT_DEVICE_BLUE_BUTTON_PRESSED,
                EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP,
                EventTypes.EVENT_UPGRADE_APP_SHOW
        };
    }
}
