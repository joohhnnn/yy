package com.txznet.loader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Environment;
import android.support.multidex.MultiDex;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.launcher.BuildConfig;
import com.txznet.launcher.data.entity.BaseResp;
import com.txznet.launcher.data.http.ApiClient;
import com.txznet.launcher.domain.BootStrapManager;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.app.PackageManager;
import com.txznet.launcher.domain.fm.FmManager;
import com.txznet.launcher.domain.guide.GuideManager;
import com.txznet.launcher.domain.login.LoginManager;
import com.txznet.launcher.domain.music.MusicManager;
import com.txznet.launcher.domain.nav.NavManager;
import com.txznet.launcher.domain.notification.TodayNoticeManager;
import com.txznet.launcher.domain.settings.FmConst;
import com.txznet.launcher.domain.settings.SettingsConst;
import com.txznet.launcher.domain.settings.SettingsManager;
import com.txznet.launcher.domain.tts.TtsManager;
import com.txznet.launcher.domain.txz.RecordWinManager;
import com.txznet.launcher.domain.upgrade.UpgradeManager;
import com.txznet.launcher.domain.voip.VoipManager;
import com.txznet.launcher.domain.wechat.WechatManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.receiver.TimeReceiver;
import com.txznet.launcher.utils.DeviceUtils;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZBindManager;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.sdk.TXZSysManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.txz.util.runnables.Runnable1;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * Created by brainBear on 2018/2/6.
 */

public class AppLogic extends AppLogicBase {

    public static final String UNSUPPORTED_COMMAND = "抱歉，当前不支持该操作";
    private static final int PAGE_COUNT = 3;
    public static final String NICK = "小欧小欧";

    @Override
    public void onCreate() {
        MultiDex.install(GlobalContext.get());
        super.onCreate();
        initManager();
        initTimeRecv();
        doInit();

        if (BuildConfig.DEBUG) {
            try {
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "txz" + File.separator + "log_enable_file");
                if (!file.exists()) {
                    file.createNewFile();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void doInit() {
        String appId = "932e22de775ab2bec6005bb916837a93";
        String appToken = "cadb00642b71353b17b380249e762002aa071b60";
        // 为了避免开机欢迎语未播报或播报中被打断导致状态不正常。唤醒词延迟到开机欢迎处理完才执行。(位于BootStrapManager)
        TXZConfigManager.getInstance().initialize(getApp(),
                new TXZConfigManager
                        .InitParam(appId, appToken)
                        .setWakeupKeyWordsThreshold("[{\"keyWords\":\"下一首\",\"threshold\":-2.4}, {\"keyWords\":\"继续导航\",\"threshold\":-2.9}, {\"keyWords\":\"开始导航\",\"threshold\":-3.0}, {\"keyWords\":\"远近排序\",\"threshold\":-3.0}, {\"keyWords\":\"距离排序\",\"threshold\":-3.0}, {\"keyWords\":\"上一页\",\"threshold\":-3.0}, {\"keyWords\":\"确定\",\"threshold\":-2.7}, {\"keyWords\":\"取消\",\"threshold\":-2.7}]") // FIXME 工具构造啊，如果有第四个
                        .setWakeupKeywordsNew(NICK)
                        .setTxzStream(AudioManager.STREAM_MUSIC)
                        .setFloatToolType(BuildConfig.DEBUG ? TXZConfigManager.FloatToolType.FLOAT_TOP : TXZConfigManager.FloatToolType.FLOAT_NONE)
                        .setFilterNoiseType(2)//有回音消除的设备，确定了是模式2
                ,
                new TXZConfigManager.InitListener() {
                    @Override
                    public void onSuccess() {
                        onInitSuccess();
                    }

                    @Override
                    public void onError(int errCode, String errDesc) {
//                        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_TXZ_INIT_ERROR);
                    }
                });
        /*
         * 由于core的applogic没有init之前，没法设置分页数。我们在执行init之前设置的TXZConfigManager.getInstance().setPagingBenchmarkCount(PAGE_COUNT);
         * 只会在reconnect中被设置，经过测试，这个总是在core的导航历史分页工具设置pageSize的时机之后执行，而分页工具设置一次后
         * 就不会被改动。另一方面，判断导航历史有多少页的工具是可以被我们设置的pageSize改变。
         * 总的来说，这会导致core的分页工具设置的pageSize为默认的4，而我们设置的pageSiz为3。导致页数正确，但core的判断不正确并且返回的数据不正确。
         * fixme 先用这种方式抢在分页工具设置数量之前将我们的pageSize设置进去。等core修好了这个bug后，用新的core的时候再去掉这段代码吧。
         * 风险点：pageCount不能改动了，总是会被这里设置成3.不过好像不会有这个需求。
         */
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type = intent.getStringExtra("type");
                if ("init".equals(type)) {
                    TXZConfigManager.getInstance().setPagingBenchmarkCount(PAGE_COUNT);
                }
            }
        }, new IntentFilter("com.txznet.txz.power.notify"));
    }


    private void onInitSuccess() {
        LogUtil.logd("VersionInfo:" + BuildConfig.SVN_VERSION + ";VersionName:" + BuildConfig.VERSION_NAME + ";VersionCode:" + BuildConfig.VERSION_CODE + ";BuildType:" + BuildConfig.BUILD_TYPE);
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_TXZ_INIT_SUCCESS);
        LogUtil.logd("notifyEvent EVENT_TXZ_INIT_SUCCESS");
        TXZAsrManager.getInstance().regCommand(new String[]{"打开今日贴士"}, "OPEN_TODAY_NOTICE");
        TXZAsrManager.getInstance().regCommand(new String[]{"关闭今日贴士"}, "CLOSE_TODAY_NOTICE");
        TXZAsrManager.getInstance().regCommand(new String[]{"打开开机问候"}, "OPEN_WELCOME");
        TXZAsrManager.getInstance().regCommand(new String[]{"关闭开机问候"}, "CLOSE_WELCOME");
//        TXZAsrManager.getInstance().regCommand(new String[]{"打开路况早晚报"}, "··");
//        TXZAsrManager.getInstance().regCommand(new String[]{"关闭路况早晚报"}, "CLOSE_SMART_TRAFFIC");
        //打开wifi和关闭wifi在core里注册了，使用通用的
        TXZAsrManager.getInstance().regCommand(new String[]{"打开FM发射"}, "OPEN_FM");
        TXZAsrManager.getInstance().regCommand(new String[]{"关闭FM发射"}, "CLOSE_FM");
        TXZAsrManager.getInstance().regCommand(new String[]{"打开wifi热点", "打开热点"}, "OPEN_WIFI_AP");
        TXZAsrManager.getInstance().regCommand(new String[]{"关闭wifi热点", "关闭热点"}, "CLOSE_WIFI_AP");
        TXZAsrManager.getInstance().regCommand(new String[]{"查看wifi热点密码", "查看热点"}, "SHOW_WIFI_AP_PW");
        TXZAsrManager.getInstance().regCommand(new String[]{"修改WIFI热点密码", "修改热点", "刷新热点"}, "CHANGE_WIFI_AP_PW");
        TXZAsrManager.getInstance().regCommand(new String[]{"关闭屏幕"}, "CLOSE_SCREEN");
        TXZAsrManager.getInstance().regCommand(new String[]{"打开屏幕"}, "OPEN_SCREEN");

        //VOIP指令
        TXZAsrManager.getInstance().regCommand(new String[]{"联系客服", "联系安吉星客服", "拨打客服电话", "拨打安吉星客服电话"}, "CUSTOMER_SERVICE");
        TXZAsrManager.getInstance().regCommand(new String[]{"联系紧急客服", "联系安吉星紧急客服", "拨打紧急客服电话", "拨打安吉星紧急客服电话"}, "CUSTOMER_SERVICE_SOS");

        // FIXME 产品定义，屏蔽绑定设备的入口，后续改用安吉星后台，已有绑定过公众号的设备不管
//        TXZAsrManager.getInstance().regCommand(new String[]{"绑定设备", "绑定后视镜", "绑定公众号", "绑定微信公众号"}, "BIND_DEVICE");
        TXZAsrManager.getInstance().regCommand(new String[]{"退出登录"}, "QUIT_LOGIN");
        TXZAsrManager.getInstance().regCommand(new String[]{"切换账号", "切换安吉星账号"}, "SWITCH_ACCOUNT");
        TXZAsrManager.getInstance().regCommand(new String[]{"查询系统信息", "查看系统信息"}, "OPEN_SYSTEM_INFO");
        TXZAsrManager.getInstance().regCommand(new String[]{"查询应用信息", "查看应用信息", "查询软件信息", "查看软件信息"}, "OPEN_APP_INFO");

        // 亮度指令
        TXZAsrManager.getInstance().regCommand(new String[]{"最大亮度"}, "LIGHT_MAX");
        TXZAsrManager.getInstance().regCommand(new String[]{"最小亮度"}, "LIGHT_MIN");
        TXZAsrManager.getInstance().regCommand(new String[]{"提高亮度","屏幕亮一点","屏幕太暗了"}, "LIGHT_UP");
        TXZAsrManager.getInstance().regCommand(new String[]{"降低亮度","屏幕暗一点","屏幕太亮了"}, "LIGHT_DOWN");

        // 帮助指令
        TXZAsrManager.getInstance().regCommand(new String[]{"查看帮助","打开帮助"}, "SHOW_HELP");

        // 屏蔽指令
        {
            // 屏蔽新手引导指令
            TXZAsrManager.getInstance().regCommand(new String[]{"查看新手引导", "我要看新手引导", "我想看新手引导", "打开新手引导"}, "INTERRUPT_GUIDE_ANIM");
        }

        TXZAsrManager.getInstance().addCommandListener(commandListener);
        TXZConfigManager.getInstance().enableChangeWakeupKeywords(false);

        TXZAsrManager.getInstance().regCommandForFM(FmConst.min, FmConst.max, "fm");

        TXZConfigManager.getInstance().setSelectListTimeout(PreferenceUtil.getInstance().getLong(PreferenceUtil.KEY_SELECT_LIST_TIMEOUT, PreferenceUtil.DEFAULT_SELECT_LIST_TIMEOUT));

        //启动的时候刷新一下绑定二维码
        TXZBindManager.getInstance().refreshBindStatus();

        TXZSysManager.getInstance().setVolumeMgrTool(mVolumeMgrTool);
        // 设置亮度工具
        TXZSysManager.getInstance().setScreenLightTool(mScreenLightTool);

        //TODO 增加登陆逻辑后，在登录界面需要禁用掉
        TXZAsrManager.getInstance().useWakeupAsAsr(mAsrComplexSelectCallback);
        //覆盖原有帮助界面
        TXZResourceManager.getInstance().setHelpWin(new TXZResourceManager.HelpWin() {
            @Override
            public void show() {
                TXZResourceManager.getInstance().speakTextOnRecordWin("抱歉，当前不支持该操作", true, enableRunnable);
            }

            @Override
            public void close() {
            }
        });

        // 设置高德导航直接开始导航，跳过路径规划选择
        TXZPoiSearchManager.getInstance().setGaoDeAutoPlanningRoute(false);

        //设置强制外放tts主题的界面
        TXZTtsManager.getInstance().forceShowTTSChoiceView(true);

        // tts播报文案修改
        TXZResourceManager.getInstance().setTextResourceString("RS_FM_TO", "");
        String asrWakeupKeywordVoice = "我的名字叫小欧小欧，您可以喊小欧小欧来唤醒我";
        TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_WAKEUP_KEYWORDS_IS", asrWakeupKeywordVoice);
        TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_WAKEUP_KEYWORDS_IS_DISPLAY", asrWakeupKeywordVoice);
        TXZResourceManager.getInstance().setTextResourceString("RS_MAP_OPEN_TRAFFIC", "打开路况");
        TXZResourceManager.getInstance().setTextResourceString("RS_MAP_CLOSE_TRAFFIC", "关闭路况");

        // 屏蔽高德途径点功能
        TXZResourceManager.getInstance().setTextResourceString("RS_NAV_CMD_NAV_WAY_POI_CMD_GASTATION", "");
        TXZResourceManager.getInstance().setTextResourceString("RS_NAV_CMD_NAV_WAY_POI_CMD_TOILET", "");
        TXZResourceManager.getInstance().setTextResourceString("RS_NAV_CMD_NAV_WAY_POI_CMD_REPAIR", "");
        TXZResourceManager.getInstance().setTextResourceString("RS_NAV_CMD_NAV_WAY_POI_CMD_ATM", "");

        // 修改亮度指令的反馈语，适用于增加亮度等core自带的指令
        TXZResourceManager.getInstance().setTextResourceString("RS_CMD_LIGHT_MAX", new String[]{"当前亮度已最大"});
        TXZResourceManager.getInstance().setTextResourceString("RS_CMD_LIGHT_MIN", new String[]{"当前亮度已最小"});
        TXZResourceManager.getInstance().setTextResourceString("RS_CMD_LIGHT_UP", new String[]{"亮度已增加"});
        TXZResourceManager.getInstance().setTextResourceString("RS_CMD_LIGHT_DOWN", new String[]{"亮度已降低"});
        TXZResourceManager.getInstance().setTextResourceString("RS_CMD_LIGHT_MAX_SETTING", new String[]{"亮度已最大"});
        TXZResourceManager.getInstance().setTextResourceString("RS_CMD_LIGHT_MIN_SETTING", new String[]{"亮度已最小"});

        // 修改网络异常的反馈语
        TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_UNKNOW_LOCAL_AFTER", new String[]{"网络不好，请稍后再试", "访问不到网络，请稍后再试", "网络出错，请稍后再试"});
        TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_UNKNOW_LOCAL_NEED_WIFI", new String[]{"网络不好，请稍后再试", "访问不到网络，请稍后再试", "网络出错，请稍后再试"});
        TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_UNKNOW_LOCAL_CHECK_NET", new String[]{"网络不好，请稍后再试", "访问不到网络，请稍后再试", "网络出错，请稍后再试"});
        TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_UNKNOW_LOCAL_NO_NET", new String[]{"网络不好，请稍后再试", "访问不到网络，请稍后再试", "网络出错，请稍后再试"});

        // 目前开机引导，我们用的是定制的一套。所以不再提示core是没有帮我们保存，会导致每次唤醒都执行开机引导。这里将开机引导设置成不可执行。
        TXZConfigManager.getInstance().setIsNeedGuideAnim(false);

        // fixme 这样子写代码不好，但是没有时间了，讲道理这个应该在comm中实现。
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.selector.poi.useDefaultCoexistAsrAndWakeup", (true+"").getBytes(), null);
    }

    TXZAsrManager.AsrComplexSelectCallback mAsrComplexSelectCallback = new TXZAsrManager.AsrComplexSelectCallback() {
        @Override
        public String getTaskId() {
            return "TASK_ID_LAUNCHER_GLOBAL";
        }

        @Override
        public boolean needAsrState() {
            return false;
        }

        @Override
        public void onCommandSelected(String type, String command) {
            super.onCommandSelected(type, command);
            // 开机流程中屏蔽掉免唤醒词
            if (!BootStrapManager.getInstance().isBootOperationComplete()) {
                LogUtil.logd("asr wakeup, isBootOperationComplete=false");
                return;
            }
            // 登录界面屏蔽掉免唤醒词
            if (LaunchManager.getInstance().isActiveModule(LaunchManager.ViewModuleType.TYPE_LOGIN)) {
                LogUtil.logd("asr wakeup, isLoginModuleActive=true");
                return;
            }
            // 微信消息播报中则屏蔽免唤醒词
            if (WechatManager.getInstance().isWechatBusy()) {
                LogUtil.logd("asr wakeup, isWechatBusy=true");
                return;
            }
            // 开机引导页屏蔽免唤醒词
            if (GuideManager.getInstance().isGuideActive()) {
                LogUtil.logd("asr wakeup, isGuideActive=true");
                return;
            }
            // ota升级中屏蔽免唤醒词
            if (UpgradeManager.getInstance().isSystemUpgradeActive()||UpgradeManager.getInstance().isSystemUpgrading()) {
                LogUtil.logd("asr wakeup, isSystemUpgradeActive=true");
                return;
            }

            // 高德地图提示界面时屏蔽免唤醒词
            if (NavManager.getInstance().isUseHintShowing() && !LaunchManager.getInstance().isLaunchResume()) {
                LogUtil.logd("asr wakeup, isGaodeUseHintShowing=true");
                return;
            }

            switch (type) {
                case "CLOSE_SCREEN":
                    SettingsManager.getInstance().ctrlScreen(false);
                    if (!(isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened())) {
//                        TXZResourceManager.getInstance().speakTextOnRecordWin("已为您关闭屏幕", true, null);
                        TtsManager.TtsBuilder.create().setText("已为您关闭屏幕").setClose(true).setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY).speak();
                    }
                    break;
                case "OPEN_SCREEN":
                    SettingsManager.getInstance().ctrlScreen(true);
                    if (!(isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened())) {
//                        TXZResourceManager.getInstance().speakTextOnRecordWin("已为您打开屏幕", true, null);
                        TtsManager.TtsBuilder.create().setText("已为您打开屏幕").setClose(true).setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY).speak();
                    }
                    break;
                case "BACK_LAUNCHER":
                    if (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) {
                        if (!RecordWinManager.getInstance().isRecordWinClosed()) {
                            RecordWinManager.getInstance().ctrlRecordWinDismiss();
                        }
                        homeRunnable.run();
                    } else {
//                        TXZResourceManager.getInstance().speakTextOnRecordWin("已为您返回桌面", true, homeRunnable);
                        TtsManager.TtsBuilder.create().setText("已为您返回桌面").setClose(true).setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY).setTtsCallback(new TtsUtil.ITtsCallback() {
                            @Override
                            public void onEnd() {
                                super.onEnd();
                                homeRunnable.run();
                            }
                        }).speak();
                    }

                    break;
                case "INC_VOLUME":
                    if (mVolumeMgrTool.isMaxVolume()) {
//                        TXZResourceManager.getInstance().speakTextOnRecordWin("当前音量已经最大", true, (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) ? enableRunnable : null);
                        final Runnable runnable = (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) ? enableRunnable : null;
                        TtsManager.TtsBuilder.create().setText("当前音量已经最大")
                                .setShowText(TXZRecordWinManager.getInstance().isOpened())
                                .setClose(true)
                                .setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY)
                                .setTtsCallback(new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        if (runnable != null) {
                                            runnable.run();
                                        }
                                    }
                                }).speak();
                    } else {
                        /*
                         * 当是免唤醒，并且音乐在播放中的时候。声音如果是最小音量或者静音的话，会先将音乐播放出来，然后才是Tts播报。
                         * 所以这里修改成tts前才设置音量。
                         */
//                        mVolumeMgrTool.incVolume();
////                        TXZResourceManager.getInstance().speakTextOnRecordWin("已为您增大音量", true, (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) ? enableRunnable : null);
                        final Runnable runnable = (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) ? enableRunnable : null;
                        TtsManager.TtsBuilder.create().setText("已为您增大音量")
                                .setShowText(TXZRecordWinManager.getInstance().isOpened())
                                .setClose(true)
                                .setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY)
                                .setTtsCallback(new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onBegin() {
                                        super.onBegin();
                                        mVolumeMgrTool.incVolume();
                                    }

                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        if (runnable != null) {
                                            runnable.run();
                                        }
                                    }
                                })
                                .speak();
                    }
                    break;
                case "DEC_VOLUME":
                    if (mVolumeMgrTool.isMinVolume()) {
//                        TXZResourceManager.getInstance().speakTextOnRecordWin("当前音量已经最小", true, (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) ? enableRunnable : null);
                        final Runnable runnable = (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) ? enableRunnable : null;
                        TtsManager.TtsBuilder.create().setText("当前音量已经最小")
                                .setShowText(TXZRecordWinManager.getInstance().isOpened())
                                .setClose(true)
                                .setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY)
                                .setTtsCallback(new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        if (runnable != null) {
                                            runnable.run();
                                        }
                                    }
                                }).speak();
                    } else {
//                        mVolumeMgrTool.decVolume();
////                        TXZResourceManager.getInstance().speakTextOnRecordWin("已为您减小音量", true, (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) ? enableRunnable : null);
                        final Runnable runnable = (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) ? enableRunnable : null;
                        TtsManager.TtsBuilder.create().setText("已为您减小音量")
                                .setShowText(TXZRecordWinManager.getInstance().isOpened())
                                .setClose(true)
                                .setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY)
                                .setTtsCallback(new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onBegin() {
                                        super.onBegin();
                                        mVolumeMgrTool.decVolume();
                                    }

                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        if (runnable != null) {
                                            runnable.run();
                                        }
                                    }
                                })
                                .speak();
                    }
                    break;
                case "BACK_NAV":
                    LogUtil.e( "onCommandSelected: isWakeupResult()="+isWakeupResult());
                    if (BootStrapManager.getInstance().isBootOperationComplete()) {
                        if (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) {
                            LogUtil.e("onCommandSelected: RecordWinManager.getInstance().isRecordWinClosed()="+RecordWinManager.getInstance().isRecordWinClosed());
                            if (!RecordWinManager.getInstance().isRecordWinClosed()) {
                                RecordWinManager.getInstance().ctrlRecordWinDismiss();
                            }
                            LaunchManager.getInstance().launchBackWithStack();
                            openNavRunnable.run();
                        } else {
                            /*
                             * 这里需要加这个launchBackWithStack是因为有时序问题：
                             * 1.打开导航是异步方法，而RecordWin的close方法中launchBack是一个切换两次主线程的异步方法，并
                             * 且该方法中判断如果不在前台会通过startActivity将launcher拉回前台。
                             * 当导航先出现然后返回桌面的方法才走到判断是否在前台的逻辑，这时候会触发
                             * startActivity，将launcher拉回前台。这样子返回导航就相当于没有起作用了。
                             * 2.MainActivity的onPause方法也会触发launchBack来返回桌面，如果当前不是桌面也会触发
                             * startActivity将launcher拉回前台。
                             * 3.bug触发的时候，顺序是launchBack先切换一次到主线程
                             *  --> 导航进入前台，触发onPause方法
                             *  --> launchBack再切换一次到主线程并真正执行返回桌面
                             *  --> 执行返回桌面时由于launcher不在前台，将launcher拉回前台。
                             * launchBackWittStack的原理：
                             * 1.将返回桌面的方法先执行了，然后才打开导航，这样子就不存在时序问题了。
                             * 2.onPause等生命周期方法是在主线程中执行的，launchBackWithStack的返回桌面也会在主线程中
                             * 执行，那么按照handler的顺序，一定是launchBackWithStack先执行，然后才是onPause等方法。
                             *
                             * fixme 这种方法很不好，意味着每次跳转界面都必须先执行一次返回桌面。感觉相关的几个地方都有问题。没时间看先这样。
                             */
//                            TXZResourceManager.getInstance().speakTextOnRecordWin("将为您打开导航", true, new Runnable() {
//                                @Override
//                                public void run() {
//                                    LaunchManager.getInstance().launchBackWithStack();
//                                    openNavRunnable.run();
//                                }
//                            });
                            TtsManager.TtsBuilder.create()
                                    .setText("将为您打开导航")
                                    .setClose(true)
                                    .setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY)
                                    .setTtsCallback(new TtsUtil.ITtsCallback() {
                                        @Override
                                        public void onEnd() {
                                            super.onEnd();
                                            LaunchManager.getInstance().launchBackWithStack();
                                            openNavRunnable.run();
                                        }
                                    })
                                    .speak();
                        }
                    }
                    break;
                case "MUSIC_CONTINUE":
                    if (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) {
                        if (LaunchManager.getInstance().isMusicWorking()) {
                            if (!MusicManager.getInstance().isPlaying()) {
                                MusicManager.getInstance().play();
                            }
                        }
                    } else {
                        if (LaunchManager.getInstance().isMusicWorking()) {
                            if (!MusicManager.getInstance().isPlaying()) {
//                                TXZResourceManager.getInstance().speakTextOnRecordWin("即将继续播放", true, new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        MusicManager.getInstance().play();
//                                    }
//                                });
                                TtsManager.TtsBuilder.create().setText("即将继续播放").setClose(true).setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY).setTtsCallback(new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        MusicManager.getInstance().play();
                                    }
                                }).speak();
                            } else {
//                                TXZResourceManager.getInstance().speakTextOnRecordWin("即将继续播放", true, null);
                                TtsManager.TtsBuilder.create().setText("即将继续播放").setClose(true).setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY).speak();

                            }
                        } else {
//                            TXZResourceManager.getInstance().speakTextOnRecordWin("当前未启动播放器", true, null);
                            TtsManager.TtsBuilder.create().setText("当前未启动播放器").setClose(true).setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY).speak();
                        }
                    }
                    break;
                case "MUSIC_PAUSE":
                    if (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) {
                        if (LaunchManager.getInstance().isMusicWorking()) {
                            if (MusicManager.getInstance().isPlaying()) {
                                MusicManager.getInstance().pause();
                            }
                        }
                    } else {
                        if (LaunchManager.getInstance().isMusicWorking()) {
//                            if (MusicManager.getInstance().isPlaying()) {  // 声控处理的那一刻，音乐是暂停状态
//                            TXZResourceManager.getInstance().speakTextOnRecordWin("即将暂停播放", true, new Runnable() {
//                                @Override
//                                public void run() {
//                                    MusicManager.getInstance().pause();
//                                }
//                            });
                            TtsManager.TtsBuilder.create().setText("即将暂停播放").setClose(true).setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY).setTtsCallback(new TtsUtil.ITtsCallback() {
                                @Override
                                public void onEnd() {
                                    super.onEnd();
                                    MusicManager.getInstance().pause();
                                }
                            }).speak();
//                            } else {
//                                TXZResourceManager.getInstance().speakTextOnRecordWin("即将暂停播放", true, null);
//                            }
                        } else {
//                            TXZResourceManager.getInstance().speakTextOnRecordWin("当前未启动播放器", true, null);
                            TtsManager.TtsBuilder.create().setText("当前未启动播放器").setClose(true).setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY).speak();
                        }
                    }
                    break;
                case "MUSIC_PRE":
                    if (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) {
                        if (LaunchManager.getInstance().isMusicWorking()) {
                            MusicManager.getInstance().playPrevious();
                        }
                    } else {
                        if (LaunchManager.getInstance().isMusicWorking()) {
//                            TXZResourceManager.getInstance().speakTextOnRecordWin("即将播放上一首", true, new Runnable() {
//                                @Override
//                                public void run() {
//                                    MusicManager.getInstance().playPrevious();
//                                }
//                            });
                            TtsManager.TtsBuilder.create()
                                    .setText("即将播放上一首")
                                    .setClose(true)
                                    .setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY)
                                    .setTtsCallback(new TtsUtil.ITtsCallback() {
                                        @Override
                                        public void onEnd() {
                                            super.onEnd();
                                            MusicManager.getInstance().playPrevious();
                                        }
                                    })
                                    .speak();
                        } else {
//                            TXZResourceManager.getInstance().speakTextOnRecordWin("当前未启动播放器", true, null);
                            TtsManager.TtsBuilder.create().setText("当前未启动播放器").setClose(true).setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY).speak();
                        }
                    }
                    break;
                case "MUSIC_NEXT":
                    if (isWakeupResult() && !TXZRecordWinManager.getInstance().isOpened()) {
                        if (LaunchManager.getInstance().isMusicWorking()) {
                            MusicManager.getInstance().playNext();
                        }
                    } else {
                        if (LaunchManager.getInstance().isMusicWorking()) {
//                            TXZResourceManager.getInstance().speakTextOnRecordWin("即将播放下一首", true, new Runnable() {
//                                @Override
//                                public void run() {
//                                    MusicManager.getInstance().playNext();
//                                }
//                            });
                            TtsManager.TtsBuilder.create().setText("即将播放下一首").setClose(true).setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY).setTtsCallback(new TtsUtil.ITtsCallback() {
                                        @Override
                                        public void onEnd() {
                                            super.onEnd();
                                            MusicManager.getInstance().playNext();
                                        }
                                    })
                            .speak();
                        } else {
//                            TXZResourceManager.getInstance().speakTextOnRecordWin("当前未启动播放器", true, null);
                            TtsManager.TtsBuilder.create().setText("当前未启动播放器").setClose(true).setPreemptType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY).speak();
                        }
                    }
                    break;
            }
        }
    }.addCommand("CLOSE_SCREEN", "关闭屏幕")
            .addCommand("OPEN_SCREEN", "打开屏幕")
            .addCommand("BACK_LAUNCHER", "返回桌面")
            .addCommand("INC_VOLUME", "增大音量")
            .addCommand("DEC_VOLUME", "减小音量")
            .addCommand("BACK_NAV", "返回导航")
            .addCommand("MUSIC_CONTINUE", "继续播放")
            .addCommand("MUSIC_PRE", "上一首")
            .addCommand("MUSIC_NEXT", "下一首")
            .addCommand("MUSIC_PAUSE", "暂停播放");

    Runnable openNavRunnable = new Runnable() {
        @Override
        public void run() {
            //包名是com.autonavi.amapautolite
            Intent in = GlobalContext.get().getPackageManager()
                    .getLaunchIntentForPackage("com.autonavi.amapautolite");
            if (in != null) {
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                GlobalContext.get().startActivity(in);
            }
        }
    };

    Runnable homeRunnable = new Runnable() {
        @Override
        public void run() {
            if (LaunchManager.getInstance().isLaunchResume()) {
                LaunchManager.getInstance().launchDesktop();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    GlobalContext.get().startActivity(intent);
                } catch (Exception e) {
                    LogUtil.loge("返回桌面错误！");
                }
            }
        }
    };

    Runnable enableRunnable = new Runnable() {
        @Override
        public void run() {
            if (!RecordWinManager.getInstance().isRecordWinClosed()) {
                RecordWinManager.getInstance().ctrlRecordWinDismiss();
            }
            if (LaunchManager.getInstance().isLaunchResume()) {
                LaunchManager.getInstance().launchBack();
            }
        }
    };

    abstract class MyScreenLightTool implements TXZSysManager.ScreenLightTool {

        static final int LEVEL = 5;
        int currentLight;
        int maxLight = 5;
        int minLight = 1;
        private final BroadcastReceiver mReceiver;

        public MyScreenLightTool() {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (SettingsConst.ACTION_BACKLIGHT_LEVEL_STATE.equals(intent.getAction())) {
                        /*
                         *  min 最小亮度等级 int
                         *  max 最高亮度等级 int
                         *  current 当前亮度等级 int
                         */
                        minLight = intent.getIntExtra("min", 1);
                        maxLight = intent.getIntExtra("max", 5);
                        currentLight = intent.getIntExtra("current", -1);
                        LogUtil.e("onReceive: min=" + minLight + " max=" + maxLight + " current=" + currentLight);
                    }
                }
            };
            GlobalContext.get().registerReceiver(mReceiver, new IntentFilter(SettingsConst.ACTION_BACKLIGHT_LEVEL_STATE));
        }
    }

    private MyScreenLightTool mScreenLightTool = new MyScreenLightTool() {

        @Override
        public void incLight() {
            Intent intent = new Intent(SettingsConst.ACTION_ADJUST_BACKLIGHT_LEVEL);
            intent.putExtra("level", Math.min(currentLight+1,maxLight));
            GlobalContext.get().sendBroadcast(intent);
        }

        @Override
        public void decLight() {
            Intent intent = new Intent(SettingsConst.ACTION_ADJUST_BACKLIGHT_LEVEL);
            intent.putExtra("level", Math.max(currentLight-1,minLight));
            GlobalContext.get().sendBroadcast(intent);
        }

        @Override
        public void maxLight() {
            Intent intent = new Intent(SettingsConst.ACTION_ADJUST_BACKLIGHT_LEVEL);
            intent.putExtra("level", maxLight);
            GlobalContext.get().sendBroadcast(intent);
        }

        @Override
        public void minLight() {
            Intent intent = new Intent(SettingsConst.ACTION_ADJUST_BACKLIGHT_LEVEL);
            intent.putExtra("level", minLight);
            GlobalContext.get().sendBroadcast(intent);
        }

        @Override
        public boolean isMaxLight() {
            return maxLight == currentLight;
        }

        @Override
        public boolean isMinLight() {
            return minLight == currentLight;
        }
    };

    abstract class MyVolumeMgrTool implements TXZSysManager.VolumeMgrTool {
        public int mMaxVolume = 100;
        public int mAdjustRate = 20;
        public float mVolumeRate = 0.2f;
        public AudioManager am = null;
        public int mAlarmMaxVolume = 100;
        public int mNotificationMaxVolume = 100;
        public int mRingMaxVolume = 100;
        public int mVoiceCallMaxVolume = 100;
        public int mSystemMaxVolume = 100;
        public int mDtmfMaxVolume = 100;
        public int mGisMaxVolume = 100;

        public MyVolumeMgrTool() {
            am = (AudioManager) GlobalContext.get().getSystemService(Context.AUDIO_SERVICE);
            mMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAdjustRate = Math.round(mMaxVolume * mVolumeRate);
            mAlarmMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            mNotificationMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            mRingMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_RING);
            mVoiceCallMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            mSystemMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            mDtmfMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_DTMF);
            mGisMaxVolume = am.getStreamMaxVolume(10);
        }
    }

    MyVolumeMgrTool mVolumeMgrTool = new MyVolumeMgrTool() {

        private int getVolume(int volume, int maxVolume) {
            int ret = volume;
            ret = Math.round(volume / (float) mMaxVolume * maxVolume);
            if (ret > maxVolume) {
                ret = maxVolume;
            } else if (ret < 0) {
                ret = 0;
            }
            return ret;
        }

        private void setVolumeInner(int volume, boolean needSave) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_ALARM, getVolume(volume, mAlarmMaxVolume), AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, getVolume(volume, mNotificationMaxVolume), AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_RING, getVolume(volume, mRingMaxVolume), AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_SYSTEM, getVolume(volume, mSystemMaxVolume), AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, getVolume(volume, mVoiceCallMaxVolume), AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_DTMF, getVolume(volume, mDtmfMaxVolume), AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(10, getVolume(volume, mGisMaxVolume), AudioManager.FLAG_PLAY_SOUND);
            if (needSave) {
                PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_SETTINGS_VOLUME, volume);
            }
            // 将声音的状态同步给高德。让高德知道是不是静音。这里把声音为0作为静音。
            NavManager.getInstance().syncGDMuteIcon(volume == 0);
        }

        @Override
        public void incVolume() {
            int volume = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_SETTINGS_VOLUME, am.getStreamVolume(AudioManager.STREAM_MUSIC));
            volume += mAdjustRate;
            if (volume > mMaxVolume) {
                volume = mMaxVolume;
            }
            setVolumeInner(volume, true);
        }

        @Override
        public void decVolume() {
            int volume = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_SETTINGS_VOLUME, am.getStreamVolume(AudioManager.STREAM_MUSIC));
            volume -= mAdjustRate;
            if (volume < 0) {
                volume = 0;
            }
            setVolumeInner(volume, true);
        }

        @Override
        public void maxVolume() {
            setVolumeInner(mMaxVolume, true);
        }

        @Override
        public void minVolume() {
            setVolumeInner(0, true);
        }

        @Override
        public void mute(boolean enable) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, enable ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE, 0);
//            } else {
//                am.setStreamMute(AudioManager.STREAM_MUSIC, enable);
//            }
            int volume = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_SETTINGS_VOLUME, am.getStreamVolume(AudioManager.STREAM_MUSIC));
            if (enable) {
                if (volume > 0) {
                    PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_SETTINGS_VOLUME, volume);
                }
                setVolumeInner(0, false);
            } else {
                setVolumeInner(volume, false);
            }
        }

        // TODO: 2018/11/23 这里的判断有问题，如果当前已经是最大音量，然后唤醒小欧说静音，然后说增大音量。反馈语会是已为您增大音量。
        @Override
        public boolean isMaxVolume() {
            return am.getStreamVolume(AudioManager.STREAM_MUSIC) == mMaxVolume;
        }

        @Override
        public boolean isMinVolume() {
            return am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
        }

        @Override
        public boolean decVolume(int decValue) {
            return false;
        }

        @Override
        public boolean incVolume(int incValue) {
            return false;
        }

        @Override
        public boolean setVolume(int value) {
            return false;
        }
    };

    TXZAsrManager.CommandListener commandListener = new TXZAsrManager.CommandListener() {
        @Override
        public void onCommand(String cmd, String data) {
            //TODO 实现指令功能
            String tts = "抱歉，该操作我还没学会";
            switch (data) {
                case "OPEN_SYSTEM_INFO":
                    tts = "已为您打开系统信息";
                    break;
                case "OPEN_APP_INFO":
                    tts = "已为您打开应用信息";
                    break;
                case "OPEN_TODAY_NOTICE":
                    PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_TODAY_NOTICE_STATE, true);
                    tts = "已为您打开今日贴士";
                    break;
                case "CLOSE_TODAY_NOTICE":
                    PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_TODAY_NOTICE_STATE, false);
                    tts = "已为您关闭今日贴士";
                    break;
                case "OPEN_WELCOME":
                    PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_WELCOME_STATE, true);
                    tts = "已为您打开开机问候";
                    break;
                case "CLOSE_WELCOME":
                    PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_WELCOME_STATE, false);
                    tts = "已为您关闭开机问候";
                    break;
                case "OPEN_SMART_TRAFFIC":
                    PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_SMART_TRAFFIC_STATE, true);
                    tts = "已为您打开路况早晚报";
                    break;
                case "CLOSE_SMART_TRAFFIC":
                    PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_SMART_TRAFFIC_STATE, false);
                    tts = "已为您关闭路况早晚报";
                    break;
                case "OPEN_FM":
                    tts = "好的，当前频率为" + FmManager.getInstance().getCurFMFreq() + "，请把车载收音机调到相同频率";
                    FmManager.getInstance().ctrlFM(true,tts);
                    return;
                case "CLOSE_FM":
                    tts = "将为您关闭FM发射";
                    FmManager.getInstance().ctrlFM(false,tts);
                    return;
                case "BIND_DEVICE":
                    tts = "";
                    break;
                case "QUIT_LOGIN":
                    tts = "";
                    reqUnbindDevice("将为您退出登录","退出失败，请稍后再试");
                    return;
                case "SWITCH_ACCOUNT":
                    tts = "";
                    reqUnbindDevice("将为您返回登录设备界面","切换失败，请稍后再试");
                    return;
                case "OPEN_WIFI_AP":
                    if (SettingsManager.getInstance().hasSimCard()) {
                        tts = "将为您打开WIFI热点";
                    } else {
                        TXZResourceManager.getInstance().speakTextOnRecordWin("没有Sim卡，打开WIFI热点失败。", true, null);
                        return;
                    }
                    break;
                case "CLOSE_WIFI_AP":
                    if (SettingsManager.getInstance().isWifiApEnabled()) {
                        tts = "将为您关闭WIFI热点";
                    } else {
                        TXZResourceManager.getInstance().speakTextOnRecordWin("WIFI热点已关闭。", true, null);
                        return;
                    }
                    break;
                case "SHOW_WIFI_AP_PW":
                    if (SettingsManager.getInstance().hasSimCard()) {
                        tts = "将为您查看WIFI热点密码";
                    } else {
                        TXZResourceManager.getInstance().speakTextOnRecordWin("没有Sim卡，打开WIFI热点失败。", true, null);
                        return;
                    }
                    break;
                case "CHANGE_WIFI_AP_PW":
                    if (SettingsManager.getInstance().hasSimCard()) {
                        tts = "已为您修改WIFI热点密码";
                    } else {
                        TXZResourceManager.getInstance().speakTextOnRecordWin("没有Sim卡，打开WIFI热点失败。", true, null);
                        return;
                    }
                    break;
                case "OPEN_SCREEN":
                    tts = "已为您打开屏幕";
                    break;
                case "CLOSE_SCREEN":
                    tts = "已为您关闭屏幕";
                    break;
                case "CUSTOMER_SERVICE":
                    tts = "正在为您呼叫安吉星客服中心";
                    break;
                case "CUSTOMER_SERVICE_SOS":
                    tts = "正在为您呼叫安吉星客服中心";
                    break;
                case "LIGHT_MAX":
                    mScreenLightTool.maxLight();
                    tts = "亮度已最大";
                    break;
                case "LIGHT_MIN":
                    mScreenLightTool.minLight();
                    tts = "亮度已最小";
                    break;
                case "LIGHT_UP":
                    if (mScreenLightTool.isMaxLight()) {
                        tts = "当前亮度已最大";
                    }else {
                        mScreenLightTool.incLight();
                        tts = "亮度已增加";
                    }
                    break;
                case "LIGHT_DOWN":
                    if (mScreenLightTool.isMinLight()) {
                        tts = "当前亮度已最小";
                    }else {
                        mScreenLightTool.decLight();
                        tts = "亮度已降低";
                    }
                    break;
                case "SHOW_HELP":
                    tts = "将为您打开帮助";
                    break;
                case "INTERRUPT_GUIDE_ANIM":
                    tts= UNSUPPORTED_COMMAND;
                default:
                    // 注释原因：现在的fm由我们自己处理，所以不需要这个了。
//                    if (data.startsWith("fm#")) {
//                        //调频到XXX,core中有播报，跳过
//                        try {
//                            data = data.substring("fm#".length());
//                            SettingsManager.getInstance().ctrlFM(true, Float.parseFloat(data));
//                        } catch (Exception e) {
//
//                        }
//                        return;
//                    }
                    break;
            }
            TXZResourceManager.getInstance().speakTextOnRecordWin(tts, true, new Runnable1<String>(data) {
                @Override
                public void run() {
                    switch (mP1) {
                        case "OPEN_SYSTEM_INFO":
                            LaunchManager.getInstance().launchSystemInfoModule();
                            break;
                        case "OPEN_APP_INFO":
                            LaunchManager.getInstance().launchAppInfoModule();
                            break;
                        case "BIND_DEVICE":
                            LaunchManager.getInstance().launchWechatBindModule();
                            break;
                        case "OPEN_WIFI_AP":
                            SettingsManager.getInstance().ctrlAPType(SettingsManager.TYPE_SETTINGS_AP_CTRL_OPEN);
                            break;
                        case "CLOSE_WIFI_AP":
                            SettingsManager.getInstance().ctrlAPType(SettingsManager.TYPE_SETTINGS_AP_CTRL_CLOSE);
                            break;
                        case "SHOW_WIFI_AP_PW":
                            SettingsManager.getInstance().ctrlAPType(SettingsManager.TYPE_SETTINGS_AP_CTRL_SHOW);
                            break;
                        case "CHANGE_WIFI_AP_PW":
                            SettingsManager.getInstance().ctrlAPType(SettingsManager.TYPE_SETTINGS_AP_CTRL_REFRESH);
                            break;

//                        case "OPEN_FM":
//                            FmManager.getInstance().ctrlFM(true, FmManager.getInstance().getCurFMFreq(), tts);
//                            break;
//                        case "CLOSE_FM":
//                            FmManager.getInstance().ctrlFM(false, FmManager.getInstance().getCurFMFreq(), tts);
//                            break;

                        case "OPEN_SCREEN":
                            SettingsManager.getInstance().ctrlScreen(true);
                            break;
                        case "CLOSE_SCREEN":
                            SettingsManager.getInstance().ctrlScreen(false);
                            break;

                        case "CUSTOMER_SERVICE":
                            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_DEVICE_BLUE_BUTTON_PRESSED);
                            break;
                        case "CUSTOMER_SERVICE_SOS":
                            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_DEVICE_RED_BUTTON_PRESSED);
                            break;
                        case "SHOW_HELP":
                            LaunchManager.getInstance().launchHelpModule();
                            break;
                    }
                }
            });
        }
    };


    private Disposable unbindDisposable;

    private void reqUnbindDevice(final String successText, final String failureText) {
        if (unbindDisposable != null && !unbindDisposable.isDisposed()) {
            unbindDisposable.dispose();
            unbindDisposable = null;
        }
        unbindDisposable = ApiClient.getInstance().getApiService().unbind(DeviceUtils.getDeviceID())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseResp>() {
                    @Override
                    public void accept(BaseResp baseResp) throws Exception {
                        if (baseResp != null && baseResp.errorCode.equals("E0000")) {
                            // 这里将解绑操作放在tts播报结束后执行，不要同时执行多个UI操作，这样可能会导致冲突。
                            TtsManager.TtsBuilder.create().setText(successText).setClose(true).setIsRemoteCommand(true).setTtsCallback(new TtsUtil.ITtsCallback() {
                                @Override
                                public void onEnd() {
                                    super.onEnd();
                                    doOnUnbind();
                                }
                            });
                        } else {
                            TXZResourceManager.getInstance().speakTextOnRecordWin(failureText, true, null);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.e("anjixing unbind throwable: "+throwable);
                        if (throwable instanceof HttpException) { // 本身就未绑定的情况下，会返回404
                            if (((HttpException) throwable).code() == 404) {
                                ResponseBody resp = ((HttpException) throwable).response().errorBody();
                                if (resp != null && resp.string().contains("E4004")) {
                                    TtsManager.TtsBuilder.create().setText(successText).setClose(true).setIsRemoteCommand(true).setTtsCallback(new TtsUtil.ITtsCallback() {
                                        @Override
                                        public void onEnd() {
                                            super.onEnd();
                                            doOnUnbind();
                                        }
                                    }).speak();
                                    return;
                                }
                            }
                            TXZResourceManager.getInstance().speakTextOnRecordWin(failureText, true, null);
                        } else {
                            // TODO: 2018/11/10 tapd-1007368 文字不显示的原因
                            // speakTextOnRecordWin只会在recordwin打开的情况下将文本发给launcher，如果不是recordwin就不会发送文本
                            TXZResourceManager.getInstance().speakTextOnRecordWin("网络异常，请稍后再试", true, null);
                        }
                    }
                });
    }

    private void doOnUnbind() {
        LoginManager.getInstance().clearSaveLoginData();
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_ANJIXING_LOGOUT);
    }

    private void initTimeRecv() {
        // 监听时间的变化，每分钟发送一次。这个不能在manifest中注册，需要动态注册。
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        getApp().registerReceiver(new TimeReceiver(), filter);
    }

    /**
     * 顺序影响观察者接收消息的时序
     */
    private void initManager() {
        TXZConfigManager.getInstance().setPagingBenchmarkCount(PAGE_COUNT);
        TXZSysManager.getInstance().syncAppInfoList(new TXZSysManager.AppInfo[]{});
        LaunchManager.getInstance().init();
        NavManager.getInstance().init();
        UpgradeManager.getInstance().init();
        BootStrapManager.getInstance().init();
        RecordWinManager.getInstance().init();
        MusicManager.getInstance().init();
        WechatManager.getInstance().init();
        SettingsManager.getInstance().init();
        VoipManager.getInstance().init();
        TodayNoticeManager.getInstance().init();
        PackageManager.getInstance().init();
        GuideManager.getInstance().init();
        LoginManager.getInstance().init();
        FmManager.getInstance().init();
    }
}
