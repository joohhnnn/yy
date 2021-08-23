package com.txznet.launcher.domain.upgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.GsonBuilder;
import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.update.UpdateCenter;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.domain.BootStrapManager;
import com.txznet.launcher.domain.settings.SettingsManager;
import com.txznet.launcher.domain.txz.RecordWinManager;
import com.txznet.launcher.domain.upgrade.bean.UpgradeInfo;
import com.txznet.launcher.domain.voip.VoipManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZCallManager;
import com.txznet.sdkinner.TXZInnerUpgradeManager;
import com.txznet.sdkinner.TXZServiceCommandDispatcher;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by ASUS User on 2018/7/6.
 */

public class UpgradeManager extends BaseManager {
    public static final String UPGRADE_PACKAGE_NAME = "com.ime.otaupdater";
    public static final String UPGRADE_SERVICE_NAME = "com.ime.otaupdater.UpdaterService";

    //主动请求的操作广播
    //(1)请求获取当前状态(一般用于首次进入初始化界面)：
    public static final String UPGRADE_ACTION_START_REFRESH = "com.ime.updater.START_REFRESH";
    //(2)检测最新版本：
    public static final String UPGRADE_ACTION_CHECK_VERSION = "com.ime.updater.CHECK_VERSION";
    //(3)在收到新版本信息后，开始下载
    public static final String UPGRADE_ACTION_START_UPDATE = "com.ime.updater.START_UPDATE";
    //(4)取消下载:
    public static final String UPGRADE_ACTION_CANCEL_UPDATE = "com.ime.updater.CANCEL_UPDATE";
    //(5)下载完毕后，开始安装
    public static final String UPGRADE_ACTION_START_INSTALL = "com.ime.updater.START_INSTALL";

    //监听回调的广播
    public static final String ACTION_UPDATER_INFO = "ime.service.intent.action.ACTION_UPDATER_INFO";

    //(1)code值对应如下：
    public static final int RCODE_UPGRADE_INFO = 1;//更新信息
    public static final int RCODE_UPGRADE_LOGINFO = 2;//登录服务器信息
    public static final int RCODE_UPGRADE_STATE = 3;//更新状态
    //(2)val
    //当code==1时，val有如下值：
    public static final int UPGRADE_INFO_CANT_CONNECT_SERVER = 1;//无法连接服务器
    /**
     * 正在下载的意思。下载中返回，携带的progress就是进度。
     * progress有几率是很大的值或者200，所以使用progress之前要判断progress是否有效
     */
    public static final int UPGRADE_INFO_DOWNLOADING = 2;
    public static final int UPGRADE_INFO_ALREADY_LATEST_VERSION = 3;//当前已是最新版本
    public static final int UPGRADE_INFO_NEW_VERSION_AVAILABLE = 4;// 刷新动作时，发现服务器有新版本
    public static final int UPGRADE_INFO_SERVER_VERSION_GOT = 5;// 不知道什么意思

    //当code==3时，val如下值
    public static final int UPDATOR_STATE_WAIT = -1;
    public static final int UPGRADE_STATE_IDLE = 0;//空闲
    public static final int UPGRADE_STATE_NEED_DOWNLOAD = 1;//待下载
    public static final int UPGRADE_STATE_DOWNLOADING = 2;//正在下载
    public static final int UPDATOR_STATE_DOWLOADING_PAUSE = 3;
    /**
     * 需要安装的意思。只在下载结束后返回，也就是会在我们下载完升级包后返回。
     */
    public static final int UPGRADE_STATE_NEED_INSTALL = 4;
    public static final int UPGRADE_STATE_INSTALLING = 5;//正在安装
    public static final int UPGRADE_STATE_INSTALL_DONE = 6;//安装完毕
    //(3)progress
    //下载进度值0-100;
    //(4)info
    //该信息为字符串直接显示即可，一般为空。

    private static final String UPGRADE_TIPS = "检测到系统新版本%1$s（共%2$s），您确定要升级吗？";
    private static final String UPGRADE_RESUME_TIPS = "系统新版本%1$s升级已中断，您确定要继续升级吗";
    private static final String UPGRADE_DOWNLOAD_ERROR_TTS_TIPS = "升级失败！";
    private static final String UPGRADE_INSTALL_ERROR_TTS_TIPS = "安装失败！";
    private static final String UPGRADE_SUCCESS_TTS_TIPS = "升级成功，即将重启设备。";

    private static final long UPGRADE_RESULT_DISMISS_TIME = 3 * 1000;// 隐藏结果对话框用的时间 3秒

    private static UpgradeManager mInstance = null;
    private UpgradeFloatView mUpgradeFloatView;
    private IPushOTAStrategy mPushOTAStrategy;
    private String mUpgradeTips="";
    private UpgradeInfo mUpgradeInfo;

    public static UpgradeManager getInstance() {
        if (mInstance == null) {
            synchronized (UpgradeManager.class) {
                if (mInstance == null) {
                    mInstance = new UpgradeManager();
                }
            }
        }
        return mInstance;
    }

    private UpgradeInnerDialog mInnerUpgradeDialog = null;

    @Override
    public void init() {
        super.init();
        // 避免升级过程中crash导致小欧小欧唤不醒。因为升级过程中，屏蔽了小欧小欧，crash时没有机会解除屏蔽会被下一次重启的launcher使用。
//        unlockWakeupNick();
//        cancelUpgrade();// 保证crash时会关闭下载，不然获取是否有提示的action没有作用。
        //注册升级监听广播
        GlobalContext.get().registerReceiver(new UpdateBaseReceiver(), new IntentFilter(ACTION_UPDATER_INFO));

        TXZServiceCommandDispatcher.setCommandProcessor("comm.update.upgrade", new TXZServiceCommandDispatcher.CommandProcessor() {
            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                JSONBuilder json = new JSONBuilder(data);
                final String newApk = json.getVal(BaseApplication.SP_KEY_APK,
                        String.class);
                File fApk = new File(newApk);
                if (!fApk.exists()) {
                    LogUtil.logw("upgrade file[" + newApk + "] not exist");
                    return null;
                }
                LogUtil.logw("upgrade silence restart");
                UpdateCenter.processUpdateApk(newApk);

                //AppLogicBase.restartProcess();

                SettingsManager.getInstance().ctrlReboot();

                return null;
            }
        });

        //先通知core当前处于繁忙状态，防止还没有进入主界面的是个弹出升级提示
        TXZInnerUpgradeManager.getInstance().notifyStatusToUpgrade(true);

        TXZInnerUpgradeManager.getInstance().setUpgradeDialogTool(new TXZInnerUpgradeManager.UpgradeDialogTool() {
            @Override
            public void showConfirmDialog(@NonNull String title, @NonNull String content, @NonNull String detailInfo, @NonNull final DialogListener listener) {

                if (!RecordWinManager.getInstance().isRecordWinClosed()) {
                    //先将声控界面关闭
                    RecordWinManager.getInstance().ctrlRecordWinDismiss();
                }

                JSONBuilder jsonBuilder = new JSONBuilder(detailInfo);
                String pkgName = jsonBuilder.getVal("pkgName", String.class, "");
                String version = jsonBuilder.getVal("version", String.class, "");
                String appName = jsonBuilder.getVal("appName", String.class, "");
                String totalSize = jsonBuilder.getVal("totalSize", String.class, "0M");
                int type = jsonBuilder.getVal("type", Integer.class, TXZInnerUpgradeManager.UpgradeDialogTool.TYPE_UPGRADE);
                String hintTts = jsonBuilder.getVal("hintTts", String.class, "");
                String upgradeInfo = jsonBuilder.getVal("upgradeInfo", String.class, "");


                UpgradeInnerDialog.UpgradeDialogBuildDate buildData = new UpgradeInnerDialog.UpgradeDialogBuildDate();
                buildData.setSureText("", new String[]{"升级", "确定", "我要升级"})
                        .setTips(content)
                        .setTitle(title)
                        .setHintTts(hintTts)
                        .setCancelOutside(false);
                buildData.setCancelText("", new String[]{"取消"});

                if (!TextUtils.isEmpty(upgradeInfo)) {
                    buildData.setContent("更新内容:\r\n" + upgradeInfo);
                }

                mInnerUpgradeDialog = new UpgradeInnerDialog(buildData) {
                    @Override
                    public void onClickOk() {
                        listener.onClickOk();
                        listener.onDismiss();
                    }

                    @Override
                    public void onClickCancel() {
                        listener.onClickCancel();
                        listener.onDismiss();
                    }

                    @Override
                    public String getReportDialogId() {
                        return "upgrade_notify_launcher";
                    }
                };
                mInnerUpgradeDialog.show();
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_UPGRADE_APP_SHOW);
            }

            @Override
            public void cancelAll() {
                if (mInnerUpgradeDialog != null && mInnerUpgradeDialog.isShowing()) {
                    mInnerUpgradeDialog.dismiss("cancelAll");
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_UPGRADE_APP_DISMISS);
                }
            }

            @Override
            public void dismissConfirmDialog(@NonNull String title, @NonNull String content, @NonNull String detailInfo) {
                if (mInnerUpgradeDialog != null) {
                    mInnerUpgradeDialog.dismiss("dismissConfirmDialog");
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_UPGRADE_APP_DISMISS);
                }
            }
        });

        TXZInnerUpgradeManager.getInstance().setNotificationTool(new TXZInnerUpgradeManager.NotificationTool() {
            @Override
            public void notify(@NonNull String pkgName, @NonNull String version, @Nullable String apkName, int event, @Nullable String data) {
                switch (event) {
                    case TXZInnerUpgradeManager.NotificationTool.EVENT_BEGIN_DOWNLOAD:
                        if (mUpgradeFloatView == null) {
                            mUpgradeFloatView = new UpgradeFloatView(GlobalContext.get());
                        }
                        mUpgradeFloatView.open();
                        break;
                    case TXZInnerUpgradeManager.NotificationTool.EVENT_PROGRESS_CHANGE:
                        if (mUpgradeFloatView != null) {
                            if (!mUpgradeFloatView.isOpening()) {
                                mUpgradeFloatView.open();
                            }
                        } else {
                            mUpgradeFloatView = new UpgradeFloatView(GlobalContext.get());
                        }
                        JSONBuilder jsonBuilder = new JSONBuilder(data);
                        mUpgradeFloatView.notifyProgressChanged(jsonBuilder.getVal("progress", Integer.class, 0), apkName);
                        break;
                    case TXZInnerUpgradeManager.NotificationTool.EVENT_END_DOWNLOAD:
                        if (mUpgradeFloatView != null) {
                            mUpgradeFloatView.close();
                        }
                        break;
                }
            }

            @Override
            public void cancelAll() {
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        if (mUpgradeFloatView != null) {
                            mUpgradeFloatView.close();
                        }
                    }
                });
            }
        });

        // 设置升级推送的策略，管理是否通知用户升级
        mPushOTAStrategy = new MostThreePushOTAStrategy();

    }

    /**
     * ota升级回调广播
     */
    public class UpdateBaseReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_UPDATER_INFO)) {
                int code = intent.getIntExtra("code", -1);
                int val = intent.getIntExtra("val", -1);
                int progress = intent.getIntExtra("progress", -1);
                String info = intent.getStringExtra("info");

                LogUtil.e("onReceive: code=" + code+" val=" + val+" progress=" + progress+" info=" + info);
                switch (code) {
                    case RCODE_UPGRADE_INFO:
                        if (val == UPGRADE_INFO_CANT_CONNECT_SERVER) {
                            // 链接不上服务器，check version会触发
                        } else if (val == UPGRADE_INFO_DOWNLOADING) {
                            // 这里获取下载并展示的progress
                            if (progress <= 100) {// 这里会返回大于100的progress，只要100以内的值。
                                updateDownloadProgress(progress);
                            }
                        } else if (val == UPGRADE_INFO_SERVER_VERSION_GOT) {
                            // check version时都会返回这个值。没啥用。
                        }
                        break;
                    case RCODE_UPGRADE_LOGINFO:
                        // 保存info，有数据就是有新版本。
                        if (val==0) {
                            UpgradeInfo[] upgradeInfos = new GsonBuilder().create().fromJson(info, UpgradeInfo[].class);
                            if (upgradeInfos!=null) {
                                for (UpgradeInfo upgradeInfo : upgradeInfos) {
                                    if (upgradeInfo != null && upgradeInfo.getSize() != 0 && !TextUtils.isEmpty(upgradeInfo.getVersion())) {
                                        mUpgradeInfo = upgradeInfo;
                                    }
                                }
                            }
                        }
                        break;
                    case RCODE_UPGRADE_STATE:
                        if (val == UPGRADE_STATE_NEED_DOWNLOAD) {
                            // check version或 refresh有新版本时返回
                        } else if (val == UPGRADE_STATE_DOWNLOADING) {
                            // 这里展示progress
                            showUpgradeDownloadingDialog(progress);
                        } else if (val == UPGRADE_STATE_NEED_INSTALL) {
                            // 下载结束后，通知远峰系统开始安装
                            startInstall();
                            if (mPushOTAStrategy instanceof MostThreePushOTAStrategy) {
                                ((MostThreePushOTAStrategy) mPushOTAStrategy).setHadDownload(false);
                            }
                        }
                        break;
                }
            }
        }
    }

    /**
     * 检查是否要提示升级，如果要的话执行升级操作。
     */
    public boolean checkAndUpgrade(){
        LogUtil.e("checkAndUpgrade: mUpgradeInfo="+mUpgradeInfo);
        if (mUpgradeInfo==null) {
            return false;
        }
        String version = mUpgradeInfo.getVersion().toUpperCase();
        // 将version同步到推送策略中。
        mPushOTAStrategy.onReceiveOTAPush(version);

        // 判断是否需要提示升级
        int upgradeType = mPushOTAStrategy.isNotifyOTAUpgrade();
        LogUtil.e("checkAndUpgrade: upgradeType="+upgradeType);
        if (upgradeType == IPushOTAStrategy.NO_UPDATE) {
            return false;
        }

        // 返回的size是int类型，单位是b
        DecimalFormat df = new DecimalFormat("#.#");
        String size = df.format(mUpgradeInfo.getSize() / (1024 * 1024f)) + "M";

        // 根据是否是首次下载还是继续下载，选择不同的文案。
        if (upgradeType == IPushOTAStrategy.FIRST_UPDATE) {
            mUpgradeTips = String.format(UPGRADE_TIPS, version, size);
        } else if (upgradeType == IPushOTAStrategy.RESUME_UPDATE) {
            mUpgradeTips = String.format(UPGRADE_RESUME_TIPS, version);
        } else {
            mUpgradeTips = "";
        }

        // 展示对话框
        // 延时1s展示，不然今日贴士后小欧还在移动中就展示升级提示了，有点突兀。
        AppLogicBase.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                // 由于延时展示，要避免延时过程中拨打了电话。这里检查下电话状态。
                if (VoipManager.getInstance().getCallStatus()== TXZCallManager.CallTool.CallStatus.CALL_STATUS_IDLE) {
                    showUpgradeInfoDialog();
                }
            }
        },1000);
        return true;
    }
    // TODO: 2018/10/17 写一个测试的方法，需要时可以通过debug和标志文件调用。 然后看效果。

    /**
     * 请求获取当前状态(一般用于首次进入初始化界面)：
     */
    public void refreshUpgradeState() {
        startService(UPGRADE_ACTION_START_REFRESH);
    }

    /**
     * 检测最新版本：
     */
    private void checkUpgradeVersion() {
        startService(UPGRADE_ACTION_CHECK_VERSION);
    }

    /**
     * 开始下载
     */
    private void startUpgrade() {
        startService(UPGRADE_ACTION_START_UPDATE);
    }

    /**
     * 取消下载
     */
    private void cancelUpgrade() {
        startService(UPGRADE_ACTION_CANCEL_UPDATE);
    }

    /**
     * 开始安装
     */
    private void startInstall() {
        startService(UPGRADE_ACTION_START_INSTALL);
    }

    /**
     * 是否在升级中
     * @return 在升级中
     */
    public boolean isSystemUpgradeActive() {
        return mUpgradeInfoDialog != null && mUpgradeInfoDialog.isShowing();
    }

    public boolean isSystemUpgrading(){
        return mUpgradeProgressDialog != null && mUpgradeProgressDialog.isShowing();
    }

    private void startService(String action) {
        Intent i = new Intent();
        i.setClassName(UPGRADE_PACKAGE_NAME, UPGRADE_SERVICE_NAME);
        i.setAction(action);
        GlobalContext.get().startService(i);
        LogUtil.e("start " + UPGRADE_SERVICE_NAME + ": action=" + action);
    }

    UpgradeDialog mUpgradeInfoDialog;

    private void showUpgradeInfoDialog() {
        if (mUpgradeInfoDialog == null) {
            LogUtil.e("mUpgradeTips: "+mUpgradeTips);
            UpgradeDialog.UpgradeDialogBuildDate buildData = new UpgradeDialog.UpgradeDialogBuildDate();
            buildData.setSureText("", new String[]{"确定"})
                    .setCancelText("", new String[]{"取消"})
                    .setTips(mUpgradeTips)
                    .setHintTts("您有一则系统升级消息，升级请说确定，放弃请说取消。");
            mUpgradeInfoDialog = new UpgradeDialog(buildData) {
                @Override
                public void onClickOk() {
                    startUpgrade();
                    showUpgradeDownloadingDialog(0);
                    mPushOTAStrategy.onSelectDownload();
                }

                @Override
                public void onClickCancel() {
                    cancelUpgrade();
                    BootStrapManager.getInstance().notifyBootOperationComplete();
                }

                @Override
                public void onBackPressed() {
                    this.dismissInner();
                    cancelUpgrade();
                    BootStrapManager.getInstance().notifyBootOperationComplete();
                }

                @Override
                public String getReportDialogId() {
                    return "upgrade_launcher_notify";
                }
            };
        }
        if (!mUpgradeInfoDialog.isShowing()) {
            mUpgradeInfoDialog.show();
            // 统计升级的次数
            // 一开始设计时是根据是失败还是取消会有不同的处理，但现在的没有这么多区别，只要是dialog出现就记一次。
            mPushOTAStrategy.onShow();
            SettingsManager.getInstance().ctrlScreen(true);
        }
    }

    UpgradeProgressDialog mUpgradeProgressDialog;

    private void showUpgradeDownloadingDialog(int progress) {
        if (mUpgradeProgressDialog == null) {
            UpgradeProgressDialog.WinProgressBuildData winProgressBuildData = new UpgradeProgressDialog.WinProgressBuildData()
                    .setMaxProgress(100)
                    .setmProgress(progress)
                    .setMessageText("升级中，请勿关机")
                    .setStyle(0);
            winProgressBuildData.setCancelable(false);
            winProgressBuildData.setCancelOutside(false);

            mUpgradeProgressDialog = new UpgradeProgressDialog(winProgressBuildData) {
                @Override
                public String getReportDialogId() {
                    return "upgrade_launcher_downloading";
                }
            };
        }
        mUpgradeProgressDialog.updateProgress(progress);
        if (!mUpgradeProgressDialog.isShowing()) {
            mUpgradeProgressDialog.show();
            SettingsManager.getInstance().ctrlScreen(true);
            lockWakeupNick();
        }
    }

    private void updateDownloadProgress(int progress){
        if (mUpgradeProgressDialog != null) {
            mUpgradeProgressDialog.updateProgress(progress);
        }
    }

    /**
     * 屏蔽昵称免唤醒词
     * 这里不适用bootStrapManager中对小欧的屏蔽，是因为开机流程结束的event有点乱，总是在我们想不到的时候触发，导致没有了对小欧小欧的屏蔽。
     * 所以这里专门弄一个屏蔽，自己用。
     */
    private void lockWakeupNick(){
        TXZAsrManager.getInstance().useWakeupAsAsr(new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public String getTaskId() {
                return "SYSTEM_UPGRADE_REMOVE_WAKEUP";
            }

            @Override
            public boolean needAsrState() {
                return false;
            }
        }.addCommand("REMOVE", "小欧小欧"));
    }

    /**
     * 取消对昵称免唤醒词的屏蔽
     */
    private void unlockWakeupNick(){
        TXZAsrManager.getInstance().recoverWakeupFromAsr("SYSTEM_UPGRADE_REMOVE_WAKEUP");
    }

    @Override
    protected void onEvent(String eventType) {
        super.onEvent(eventType);

        switch (eventType) {
            case EventTypes.EVENT_BOOT_OPERATION_COMPLETE:
                TXZInnerUpgradeManager.getInstance().notifyStatusToUpgrade(false);
                break;
            case EventTypes.EVENT_DEVICE_RED_BUTTON_PRESSED:
            case EventTypes.EVENT_DEVICE_BLUE_BUTTON_PRESSED:
                if (mInnerUpgradeDialog != null && mInnerUpgradeDialog.isShowing()) {
                    mInnerUpgradeDialog.dismiss("on call");
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_UPGRADE_APP_DISMISS);
                }
                // 关闭OTA升级确认框
                if (mUpgradeInfoDialog!=null && mUpgradeInfoDialog.isShowing()) {
                    mUpgradeInfoDialog.dismiss("on call");
                    BootStrapManager.getInstance().notifyBootOperationComplete();
                }
                break;
            case EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP:
                if (mInnerUpgradeDialog != null && mInnerUpgradeDialog.isShowing()) {
                    mInnerUpgradeDialog.dismiss("acc off");
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_UPGRADE_APP_DISMISS);
                }
                // 关闭OTA升级确认框
                if (mUpgradeInfoDialog!=null && mUpgradeInfoDialog.isShowing()) {
                    mUpgradeInfoDialog.dismiss("acc off");
                    // 这里不可以让开机流程结束，不然在可能会执行路况早晚报。
//                    BootStrapManager.getInstance().notifyBootOperationComplete();
                }
                // 关闭OTA升级进度框
                if (mUpgradeProgressDialog!=null && mUpgradeProgressDialog.isShowing()) {
                    mUpgradeProgressDialog.dismiss("acc off");
                    // 启动升级后要关闭，不让otaUpdater那边会一直下载
                    cancelUpgrade();
                    unlockWakeupNick();
                }

                // 清除保存的升级信息，避免acc on之后即使没有网络也会执行ota升级。
                mUpgradeInfo = null;
                // 清除dialog，避免acc on之后使用的dialog是上一次的dialog。
                mUpgradeInfoDialog = null;
                mUpgradeProgressDialog = null;
                break;
            case EventTypes.EVENT_VOICE_OPEN:
                if (mInnerUpgradeDialog != null && mInnerUpgradeDialog.isShowing()) {
                    mInnerUpgradeDialog.dismiss("record win open");
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_UPGRADE_APP_DISMISS);
                }
                break;
            case EventTypes.EVENT_DEVICE_SIM_READY:// 有网后检测是否有新版本。
                checkUpgradeVersion();
                break;
            case EventTypes.EVENT_DEVICE_RECORY_FACTORY:
                // 关闭OTA升级确认框
                if (mUpgradeInfoDialog!=null && mUpgradeInfoDialog.isShowing()) {
                    mUpgradeInfoDialog.dismiss("recovery factory");
                    BootStrapManager.getInstance().notifyBootOperationComplete();
                }

                //关闭应用升级对话框
                if (mInnerUpgradeDialog != null && mInnerUpgradeDialog.isShowing()) {
                    mInnerUpgradeDialog.dismiss("recovery factory");
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_UPGRADE_APP_DISMISS);
                }
                break;
        }
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{EventTypes.EVENT_BOOT_OPERATION_COMPLETE/*,EventTypes.EVENT_BOOT_INVALID*/,
                EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP,EventTypes.EVENT_DEVICE_RED_BUTTON_PRESSED,
                EventTypes.EVENT_DEVICE_BLUE_BUTTON_PRESSED,EventTypes.EVENT_VOICE_OPEN,
                EventTypes.EVENT_DEVICE_SIM_READY,
                EventTypes.EVENT_DEVICE_RECORY_FACTORY,
        };
    }
}
