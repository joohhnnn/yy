package com.txznet.txz.module.help;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.txz.report_manager.ReportManager;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.sp.CommonSp;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZTongTingManager;
import com.txznet.sdk.TXZWechatManager;
import com.txznet.txz.R;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.tool.NavAppManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.ui.win.help.HelpPreferenceUtil;
import com.txznet.txz.ui.win.help.WinHelpManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class HelpGuideManager {

    public static final String DEFAULT_RESOURCE_FILE_NAME = "float_help_str.json";
    public static final String TEST_ACTION = "com.txznet.txz.help.action";
    public static int AUTO_SHRINK_TIME = 30000;
    public static final int SHRINK_ANIM_DURATION = 200;

    public static final int SCENE_TYPE_UNKNOW = 0;
    public static final int SCENE_TYPE_HOME = 1;
    public static final int SCENE_TYPE_NAV = 2;
    public static final int SCENE_TYPE_MUSIC = 3;
    private Context mContext;
    private HelpFloatWindow mFloatWindow;

    private static HelpGuideManager sHelp = null;

    public static HelpGuideManager getInstance() {
        if (sHelp == null) {
            synchronized (HelpGuideManager.class) {
                if (sHelp == null) {
                    sHelp = new HelpGuideManager();
                }
            }
        }
        return sHelp;
    }

    /////////////////////////// 开机引导动画////////////////////////////////////
    public static final String GUIDE_WAKEUP_TASK_ID = "guideWakeupId";
    public static final String GUIDE_PACKAGE_NAME = "com.txznet.guideanim";

    private boolean hasAnimed;

    public boolean isNeedGuideAnim() {
        if (!isNeedAnim) {
            LogUtil.logd("isNeedGuideAnim isNeedAnim false");
            return false;
        }
        if (hasAnimed) {
            LogUtil.logd("isNeedGuideAnim hasAnimed");
            return false;
        }
        if (!isEnableWakeup()) {
            LogUtil.logd("isNeedGuideAnim isEnableWakeup false");
            return false;
        }
        if (!hasGuideAnim()) {
            return false;
        }

        long time = PreferenceUtil.getInstance().needPlayGuideAnim();
        LogUtil.logd("isNeedGuideAnim time:" + time);
        if (time != 0) {
            return false;
        }
        return true;
    }

    /**
     * 是否有引导动画功能
     *
     * @return
     */
    public boolean hasGuideAnim() {
        if (!PackageManager.getInstance().checkAppExist(GUIDE_PACKAGE_NAME)) {
            LogUtil.logd("hasGuideAnim GUIDE_PACKAGE_NAME false");
            return false;
        }
        // 如果没有安装微信、导航、电话或者音乐中的任意一个应用或者这些应用没有适配语音，则不展示新手引导APK
        boolean hasMusic = true;
//        boolean hasWx = true;
        boolean hasNav = true;
        String disableResaon = MusicManager.getInstance().getDisableResaon();
        if (!TextUtils.isEmpty(disableResaon)) {
            hasMusic = false;
        }
//        if (!PackageManager.getInstance().checkAppExist(ServiceManager.WEBCHAT)) {
//            hasWx = false;
//        }
        if (NavAppManager.getInstance().getCurrNavTool() == null) {
            hasNav = false;
        }

        LogUtil.logd("hasGuideAnim hasMusic:" + hasMusic + /*",hasWx:" + hasWx +*/ ",hasNav:" + hasNav);
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("type", "guideAnim");
        jsonBuilder.put("action", "checkNeedGuide");
        jsonBuilder.put("hasMusic", hasMusic);
//        jsonBuilder.put("hasWx", hasWx);
        jsonBuilder.put("hasNav", hasNav);
        if (SenceManager.getInstance().noneedProcSence("all", jsonBuilder.toBytes())) {
            return true;
        }

        return hasMusic && /*hasWx &&*/ hasNav;
    }

    private boolean mRegisteredOpenCmds;

    /**
     * 检查是否需要注册打开新手引导相关指令
     */
    public void checkToRegisterGuideCmds() {
        if (mRegisteredOpenCmds) {
            return;
        }
        LogUtil.logd("checkToRegisterGuideCmds");
        if (hasGuideAnim()) {
            WinHelpManager.getInstance().regCommand("OPEN_GUIDE_ANIM");
            mRegisteredOpenCmds = true;
        }
    }

    private boolean isNeedAnim = true;

    public void setNeedGuideAnimFlag(boolean isNeedAnim) {
        LogUtil.logd("setNeedGuideAnim:" + isNeedAnim);
        this.isNeedAnim = isNeedAnim;
    }

    public void onLicenseActive() {
        long dTime = System.currentTimeMillis();
        LogUtil.logd("onLicenseActive:" + dTime);
        Long nTime = NativeData.getMilleServerTime().uint64Time;
        if (nTime != null && (nTime - dTime) < 100) { // 一样的时间，设备时间正常
        } else {
            LogUtil.loge("dTime is not equal nTime！");
        }
        PreferenceUtil.getInstance().setActiveTime("" + nTime);
    }

    private static final long THREE_DAY = 3 * 24 * 60 * 60 * 1000L;
    private static final long AMONTH = 30 * 24 * 60 * 60 * 1000L;
//	private static final long THREE_DAY = 5 * 60 * 1000L;
//	private static final long AMONTH = 10 * 60 * 1000L;

    /**
     * 根据激活状态和时间判断是否需要重置引导标记
     */
    public void preCheckNeedGuideAnim() {
        String time = PreferenceUtil.getInstance().getActiveTime();
        if (!TextUtils.isEmpty(time)) {
            Long t = Long.parseLong(time);
            long delay = NativeData.getMilleServerTime().uint64Time - t;
            if (delay >= THREE_DAY) { // 激活三天后设置未读标记
                LogUtil.logd("preCheckNeedGuideAnim setNeedPlayGuideAnim");
                PreferenceUtil.getInstance().setNeedPlayGuideAnim(0);
                PreferenceUtil.getInstance().setActiveTime("");
            }
            return;
        }
        long currTime = NativeData.getMilleServerTime().uint64Time;
        long lastTime = PreferenceUtil.getInstance().getLastUseTime();
        if (lastTime != 0) {
            long delay = currTime - lastTime;
            LogUtil.logd("record time delay:" + delay);
            if (delay >= AMONTH) {// 一个月内没用过语音，设置未读标记
                PreferenceUtil.getInstance().setNeedPlayGuideAnim(0);
            }
        }
    }

    private void prepareState() {
        boolean isMusicFore = TXZTongTingManager.getInstance().isShowUI();
        if (isMusicFore) {
            mState |= STATE_MUSIC_FOCUS_MARK;
        } else {
            mState &= ~STATE_MUSIC_FOCUS_MARK;
        }
        LogUtil.logd("prepareState:" + isMusicFore);
    }

    /**
     * 记录上一次使用语音的时间，用设备时间统计
     */
    public void recordTime() {
        long millis = NativeData.getMilleServerTime().uint64Time;
        PreferenceUtil.getInstance().setLastUseTime(millis);
    }

    public void readGuide(String reason) {
        LogUtil.logd("hasReadGuide:" + reason);
        hasAnimed = true; // 设置为true，判断条件不用每次从Preference中取
        PreferenceUtil.getInstance().setNeedPlayGuideAnim(NativeData.getMilleServerTime().uint64Time);
    }

    public void execGuideAnim() {
        if (isNeedGuideAnim()) {
            startGuideAnim(true);
            hasAnimed = true;
        } else {
            LogUtil.loge("not exist guideanim");
        }
    }

    /////////////////////////// 从语音指令进入引导///////////////////////////
    private boolean isStartFromAsr = false;

    public void startGuideAnimFromVoiceCmd() {
        isStartFromAsr = true;
        startGuideAnim(false);
    }

    private boolean isStartFromOuter;

    public void startGuideAnimFromOuter() {
        LogUtil.logd("startGuideAnimFromOuter");
        isStartFromOuter = true;
        startGuideAnim(false);
    }

    private void onPrepareStartGuideAnim() {
        hideFloatWindow();
        TXZWechatManager.getInstance().skipCurrentMessage();
        WeixinManager.getInstance().cancelDialog();
        isAniming = true;
        if (mAnimingEndRun != null) {
            AppLogic.removeBackGroundCallback(mAnimingEndRun);
            AppLogic.runOnBackGround(mAnimingEndRun, 1 * 1000);
        }
    }

    private void startGuideAnim(boolean isAutoTrigger) {
        onPrepareStartGuideAnim();
        ComponentName cName = new ComponentName(GUIDE_PACKAGE_NAME, "com.txznet.guideanim.ui.SplashActivity");
        Intent intent = new Intent();
        intent.setComponent(cName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("isAutoTrigger", isAutoTrigger);
        // GlobalContext.get().startActivity(intent);
        LogUtil.logd("startGuideAnim...");
        PendingIntent pendingIntent =
                PendingIntent.getActivity(GlobalContext.get(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        try {
            pendingIntent.send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    ///////////////////// 从帮助页面启动，并且回到帮助界面的逻辑/////////////////////
    private boolean isStartFromHelp = false;
    private int selectPage = 0;

    public void startGuideAnimFromHelp(int selectPage) {
        isStartFromHelp = true;
        startGuideAnim(false);
        this.selectPage = selectPage;
    }

    public void onBackHelp() {
        RecorderWin.show();
//		WinHelpDetailTops.getInstance().updateCurPage(selectPage);
        WinHelpManager.getInstance().show(new JSONBuilder()
                .put("type", WinHelpManager.TYPE_OPEN_FROM_BACK)
                .put("selectPage", selectPage)
                .toString());
    }

    private boolean isAniming;

    public boolean isAniming() {
        return isAniming;
    }

    // 第一次跳过新手引导的时候必须显示小红点
    boolean isFirstStart = false;
    boolean isFinishFromSDK = false;

    public byte[] invokeHelp(String command, byte[] data) {
        LogUtil.logd("help command:" + command);
        if ("start".equals(command)) {
            isAniming = true;
            startTime = SystemClock.elapsedRealtime();
            isFirstStart = PreferenceUtil.getInstance().getBoolean(PreferenceUtil.KEY_IS_FIRST_START, true);
            RecorderWin.notifyShow();
            PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_IS_FIRST_START, false);
        } else if ("finish".equals(command)) {
            isAniming = false;
            reportEvent(-1, true);
            onGuideAnimStop(true, false);
        } else if ("never".equals(command)) { // 以后不再提醒
            int idx = -1;
            if (data != null) {
                idx = Integer.parseInt(new String(data));
            }
            reportEvent(idx, false);
            onGuideAnimStop(true, true);
        } else if ("jump".equals(command)) { // 忽略本次，下次继续
            int idx = Integer.parseInt(new String(data));
            reportEvent(idx, false);
            isAniming = false;
            onGuideAnimStop(true, false);
            if (isFirstStart) {
                WinHelpManager.showHelpNewTag(true);
            }
        } else if ("getNicks".equals(command)) {
            return randomWakeupKws().getBytes();
        } else if ("heartbeat".equals(command)) {// 心跳通知，防止引导被Kill导致不能恢复状态
            if (mAnimingEndRun != null) {
                AppLogic.removeBackGroundCallback(mAnimingEndRun);
                AppLogic.runOnBackGround(mAnimingEndRun, 3 * 1000);
            }
            return null;
        } else if ("getTaskId".equals(command)) { // 获取场景唤醒指定ID
            return GUIDE_WAKEUP_TASK_ID.getBytes();
        } else if ("back".equals(command)) {
            int idx = Integer.parseInt(new String(data));
            reportEvent(idx, false);
            isAniming = false;
            onGuideAnimStop(false, false);
        }else if("registerTipsInfo".equals(command)){
            openHelpTipsTask(data);
        }else if("unRegisterTipsInfo".equals(command)){
            JinshouzhiManager manager = JinshouzhiManager.getInstance();
            String msg = new String(data);
            manager.unRegisterTipsInfo(msg);
            LogUtil.d("unRegisterHelpTips: "+msg);
        }else if("helpWindowsClose".equals(command)){
            toggleFloatBtnMode();
            reportClickFloatEven(false,false);
            JinshouzhiManager.getInstance().clearTemporaryForeScene();
            LogUtil.d("registerHelpTips: close windows");
        } else if("finishGuideAnim".endsWith(command)){
            isFinishFromSDK = true;
            //finish在TXZGuide的实现是调用 onAnimJump(COMPLETE_STATE_BACK);
            ServiceManager.getInstance().sendInvoke(GUIDE_PACKAGE_NAME, "guide.anim.finish", null, null);
        } else if("isShowGuide".equals(command)){
            if(RecorderWin.isOpened()){
                return String.valueOf(false).getBytes();
            }
            return String.valueOf(true).getBytes();
        }
        return null;
    }

    int delay = 0;
    volatile boolean canShow = true;
    public void openHelpTipsTask(byte[] data){
        if (!canShow){
            //canShow = false，表示当前正在收缩，不能立即展示，否则会出现按钮和列表同时出现的情况
            delay = 1000;
            LogUtil.d("need delay show");
        }
        openHelpTipsTask.update(data);
        AppLogic.removeBackGroundCallback(openHelpTipsTask);
        AppLogic.runOnBackGround(openHelpTipsTask,delay);
        delay = 500;
    }

    Runnable1<byte[]> openHelpTipsTask = new Runnable1<byte[]>(null) {
        @Override
        public void run() {
            JinshouzhiManager manager = JinshouzhiManager.getInstance();
            String msg = new String(mP1);
            LogUtil.d("registerHelpTips:"+msg);
            TipsInfo tipsInfo= new Gson().fromJson(msg,TipsInfo.class);
            /*if(tipsInfo.isGetWXInfo()){
                manager.registerTemporaryTipsInfo(msg);
            }else{
                manager.registerTipsInfo(msg);
            }*/
            manager.registerTipsInfo(msg);
            boolean isHigh = false;
            if (HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HIGH_TIPS, "0").equals("1")) {
                isHigh = true;
            }
            if (manager.getForeScene() == JinshouzhiManager.TYPE_NAV && isHigh) {
                AUTO_SHRINK_TIME = 20000;
            }else{
                AUTO_SHRINK_TIME = 30000;
            }
            refreshHelpTips();
            if (tipsInfo.isOpenWindows()) {
                reportClickFloatEven(true,false);
                toggleDetailMode();
            }
        }
    };


    private long startTime;

    private void reportEvent(int frame, boolean isFinish) {
        long delayTime = SystemClock.elapsedRealtime() - startTime;
        JSONBuilder json = new JSONBuilder();
        json.put("action", "guideAnim");
        json.put("time", NativeData.getMilleServerTime().uint64Time);
        json.put("finished", isFinish);
        json.put("currFrame", frame);
        json.put("expTime", delayTime);
        ReportUtil.doReport(ReportManager.UAT_COMMON, json.toBytes());
    }

    /**
     * 关闭悬浮窗口
     */
    private void reportCloseFloatEvent(){
        JSONBuilder json = new JSONBuilder();
        json.put("action", "CloseFloat");
        ReportUtil.doReport(ReportManager.UAT_COMMON, json.toBytes());
    }

    /**
     * 上报横条事件
     *
     * @param manual
     * @param barText
     */
    private void reportFloatEvent(boolean manual, String barText) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        if (manual) {
            jsonBuilder.put("action", "helpDetail");
        } else {
            jsonBuilder.put("action", "orizontalBar");
        }
        jsonBuilder.put("time", NativeData.getMilleServerTime().uint64Time);
        if (!TextUtils.isEmpty(barText)) {
            jsonBuilder.put("barText", barText);
        }
        jsonBuilder.put("manual", manual);
        ReportUtil.doReport(ReportManager.UAT_COMMON, jsonBuilder.toBytes());
    }

    private void reportMoveFloatEvent(int x, int y){
        JSONBuilder json = new JSONBuilder();
        json.put("action", "moveFloat");
        json.put("time", NativeData.getMilleServerTime().uint64Time);
        json.put("floatX",x);
        json.put("floatY",y);
        ReportUtil.doReport(ReportManager.UAT_COMMON, json.toBytes());
    }

    /**
     *帮助详情
     * @param isShow true 打开  false隐藏
     * @param manual true手动  false自动
     */
    private void reportClickFloatEven(boolean isShow,boolean manual){
        JSONBuilder json = new JSONBuilder();
        if(manual){
            if(isShow){
                json.put("action", "showHelpDetail");
            }else{
                json.put("action", "hideHelpDetail");
            }
        }else{
            if(isShow){
                json.put("action", "autoShowHelpDetail");
            }else{
                json.put("action", "autoHideHelpDetail");
            }
        }
        json.put("time", NativeData.getMilleServerTime().uint64Time);
        ReportUtil.doReport(ReportManager.UAT_COMMON, json.toBytes());
    }

    private Runnable mAnimingEndRun = new Runnable() {

        @Override
        public void run() {
            LogUtil.logd("mAnimingEndRun");
            onGuideAnimStop(true, false);
        }
    };

    /**
     * 通知结束新手引导
     * 1、休眠前
     * 2、关闭屏幕
     */
    public void finishGuideAnim() {
        readGuide("Power State");
        ServiceManager.getInstance().sendInvoke(GUIDE_PACKAGE_NAME, "guide.anim.finish", null, null);
    }

    /**
     * 电话中
     */
    public void onBeginCall() {
        LogUtil.logd("onBeginCall");
        pauseGuideAnim();
        toggleFloatBtnMode();
    }

    /**
     * 进入倒车
     */
    public void enter_reverse() {
        LogUtil.logd("enter_reverse");
        pauseGuideAnim();
        toggleFloatBtnMode();
    }

    /**
     * 进入倒车，暂停动画
     */
    public void pauseGuideAnim() {
        ServiceManager.getInstance().sendInvoke(GUIDE_PACKAGE_NAME, "guide.anim.pause", null, null);
    }

    /**
     * 退出倒车，恢复动画
     */
    public void resumeGuideAnim() {
        ServiceManager.getInstance().sendInvoke(GUIDE_PACKAGE_NAME, "guide.anim.resume", null, null);
    }

    /**
     * 引导结束
     *
     * @param isNeedRecorderWin 是否启动语音界面
     */
    private void onGuideAnimStop(boolean isNeedRecorderWin, boolean needReadMark) {
        if (needReadMark) {
            readGuide("onGuideAnimStop");
        }
        broadcastGuideEnd();
        AppLogic.removeBackGroundCallback(mAnimingEndRun);
        // 反注册引导的任务，避免引导apk反注册不成功
        WakeupManager.getInstance().recoverWakeupFromAsr(GUIDE_WAKEUP_TASK_ID);
        mAnimingEndRun = null;
        isAniming = false;
        if (isStartFromAsr) {
            // 不打开声控界面
            isStartFromAsr = false;
            isNeedRecorderWin = false;
        } else if (isStartFromHelp) {
            isNeedRecorderWin = false;
            isStartFromHelp = false;
            boolean defOpen = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_BACK_WIN_HELP, false);
            //如果播报完需要返回帮助，则不执行打开窗口等操作
            if (defOpen) {
                onBackHelp();
                return;
            }
        } else if (isStartFromOuter) {
            isStartFromOuter = false;
            isNeedRecorderWin = false;
        }
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("type", "guideAnim");
        jsonBuilder.put("action", "isNeedRecorderWin");
        if (SenceManager.getInstance().noneedProcSence("all", jsonBuilder.toBytes())) {
            isNeedRecorderWin = false;
        }
        // 不需要首次点击语音助手启动
        if (!isNeedAnim) {
            isNeedRecorderWin = false;
        }

        if(isFinishFromSDK){
            isFinishFromSDK = false;
            isNeedRecorderWin = false;
        }


        if (isNeedRecorderWin) {
            RecorderWin.open();
        } else {
            RecorderWin.notifyDismiss();
        }
    }

    private void broadcastGuideEnd() {
        Intent intent = new Intent("com.txznet.txz.action.endGuideAnim");
        GlobalContext.get().sendBroadcast(intent);
    }

    //////////////////////////////////////////////////////////////////////////////
    boolean isFirstPlay = true;
    private static String KEY = "WakeupCMD";
    private List<String> musicWpCMDs = new ArrayList<String>();
    private static final String home = "home";


    public void init(Context context) {
        mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TEST_ACTION);
        mContext.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                int action = intent.getIntExtra("action", -1);
                switch (action) {
                    // 显示和隐藏
                    case 1:
                        int extra = intent.getIntExtra("EXTRA", -1);
                        switch (extra) {
                            case 1:
                                showFloatWindow(false);
                                break;
                            case 2:
                                hideFloatWindow();
                                break;
                        }
                        break;
                    // 控制显示模式
                    case 2:
                        extra = intent.getIntExtra("EXTRA", -1);
                        switch (extra) {
                            case 1:
                                toggleBarMode();
                                break;
                            case 2:
                                toggleFloatBtnMode();
                                break;
                            case 3:
                                toggleDetailMode();
                                break;
                        }
                        break;
                    case 3:
                        extra = intent.getIntExtra("EXTRA", -1);
                        switch (extra) {
                            case 1:
                                MusicManager.getInstance().onBeginCall();
                                break;
                            case 2:
                                MusicManager.getInstance().onEndCall();
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        }, intentFilter);
        AppLogic.removeBackGroundCallback(mInitRunnable);
        AppLogic.runOnBackGround(mInitRunnable);
    }

    Runnable mInitRunnable = new Runnable() {

        @Override
        public void run() {
            parseResource();
            preCheckNeedGuideAnim();
            mInited = true;
            if (mShowDelayRunnable != null) {
                mShowDelayRunnable.run();
            }
        }
    };

    private boolean mInited;
    private Map<String, String[]> mIdStrMap = null;

    private void parseResource() {
        JSONObject jsonObject = null;
        try {
            FileInputStream fis = new FileInputStream(
                    GlobalContext.get().getApplicationInfo().dataDir + "/data/" + DEFAULT_RESOURCE_FILE_NAME);
            FileChannel fileChannel = fis.getChannel();
            if (fileChannel.isOpen()) {
                try {
                    long size = fileChannel.size();
                    ByteBuffer buffer = ByteBuffer.allocate((int) size);
                    fileChannel.read(buffer);
                    try {
                        jsonObject = new JSONObject(new String(buffer.array()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fis.close();
                        fileChannel.close();
                    } catch (IOException e) {
                        LogUtil.loge(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (jsonObject != null) {
            if (mIdStrMap == null) {
                mIdStrMap = new HashMap<String, String[]>();
            }
            mIdStrMap.clear();
            try {
                Iterator<String> iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    Object obj = jsonObject.get(key);
                    if (obj instanceof JSONArray) {
                        JSONArray array = (JSONArray) obj;
                        String[] vals = new String[array.length()];
                        for (int i = 0; i < array.length(); i++) {
                            vals[i] = array.optString(i);
                        }
                        mIdStrMap.put(key, vals);
                    } else if (obj instanceof String) {
                        String val = (String) obj;
                        mIdStrMap.put(key, new String[]{val});
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private int mWakeupCount;

    /**
     * 唤醒的次数
     */
    public void incWakeupCount() {
        if (mWakeupCount <= 0) {
            mWakeupCount = PreferenceUtil.getInstance().getWakeupCount();
        }
        if (mWakeupCount >= 3) {
            mWakeupCount++;
        } else {
            mWakeupCount++;
            PreferenceUtil.getInstance().setWakeupCount(mWakeupCount);
        }
    }

    private int mNavCount;
    private boolean mNavExited = true;
    private boolean mFirstOpenNav = true;
    private boolean mNavPlan = false;//导航是否在规划路线

    /**
     * 开关导航的次数
     */
    public void notifyCloseNav() {
        mNavExited = true;
        mNavPlan = false;
        mFirstOpenNav = true;
        AppLogic.removeBackGroundCallback(mNavRunnable);
        AppLogic.runOnBackGround(mNavRunnable, 3 * 1000);
    }

    Runnable mNavRunnable = new Runnable() {

        @Override
        public void run() {
            nav_inner_count();
        }
    };

    private void nav_inner_count() {
        LogUtil.logd("nav_inner_count");
        if (mNavCount <= 0) {
            mNavCount = PreferenceUtil.getInstance().getOpenNavCount();
        }
        if (mNavCount >= 3) {
            mNavCount++;
        } else {
            mNavCount++;
            PreferenceUtil.getInstance().setOpenNavCount(mNavCount);
        }
    }

    public void showFloatAfterInitSuccess() {
        AppLogic.removeBackGroundCallback(mShowFloatRunnable);
        AppLogic.runOnBackGround(mShowFloatRunnable, 3 * 1000);
    }

    Runnable mShowFloatRunnable = new Runnable() {

        @Override
        public void run() {
            prepareState();

            if (!mInited) {
                LogUtil.loge("has not init！");
                mShowDelayRunnable = new Runnable() {

                    @Override
                    public void run() {
                        mShowDelayRunnable = null;
                        showFloatWindow(true);
                    }
                };
                return;
            }
            if (!isShowHelpFloat()) {
                LogUtil.loge("mShowFloatRunnable not need float！");
                return;
            }
            LogUtil.logd("mShowFloatRunnable");
            if (RecorderWin.isNotifyRecordShow) {
                mShowDelayRunnable = new Runnable() {

                    @Override
                    public void run() {
                        mShowDelayRunnable = null;
                        showFloatWindow(true);
                    }
                };
            } else {
                mShowDelayRunnable = null;
                showFloatWindow(true);
            }
        }
    };

    Runnable mShowDelayRunnable = null;

    private boolean hasShowFirstBar;

    /**
     * 显示帮助浮窗
     */
    public void showFloatWindow(boolean isFirst) {
        if (!isShowHelpFloat()) {
            LogUtil.loge("no need showFloatWindow！");
            return;
        }
        LogUtil.logd("showFloatWindow isFirst:"+isFirst);
        showWindowInner(true);
        refreshHelpFloat(false);
        if(isFirst){
            boolean isSupport = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_HELP_FLOAT_WAKEUP,true);
            if(isSupport){
                RecorderWin.addInterruptJSZKws();
            }
        }
//        if (isFirst) {
//            showFirstHoriBar();
//        }
    }

    /**
     * 是否需要展示浮窗
     *
     * @return
     */
    private boolean isShowHelpFloat() {
        return mNeedHelpFloat;
    }

    private boolean mNeedHelpFloat = false;

    /**
     * 适配程序设置是否需要帮助浮窗
     *
     * @param isNeedFloat
     */
    public void setNeedHelpFloat(String pkn, boolean isNeedFloat) {
        LogUtil.logd(pkn + " setNeedHelpFloat:" + isNeedFloat);
        mNeedHelpFloat = isNeedFloat;
        if (!mNeedHelpFloat) {
            dismiss_Inner();
            reportCloseFloatEvent();
        } else {
            showFloatWindow(true);
        }
    }

    private void showWindowInner(boolean canAutoHide) {
        if (mFloatWindow != null && mFloatWindow.isShowing()) {
            return;
        }

        AppLogic.runOnUiGround(new Runnable() {

            @Override
            public void run() {
                if (mFloatWindow == null) {
                    mFloatWindow = new HelpFloatWindow(GlobalContext.get());
                }
                mFloatWindow.show();
            }
        });
    }

    /**
     * 隐藏帮助浮窗
     */
    public void hideFloatWindow() {
        if (!isShowHelpFloat()) {
            return;
        }
        dismiss_Inner();
    }

    private void dismiss_Inner() {
        if (mFloatWindow != null && mFloatWindow.isShowing()) {
            AppLogic.runOnUiGround(new Runnable() {

                @Override
                public void run() {
                    mFloatWindow.dismiss();
                }
            });
        }
    }

    /**
     * 显示浮窗
     */
    public void resumeFloatWindow() {
        if (!isShowHelpFloat()) {
            return;
        }
        if (mFloatWindow != null && !mFloatWindow.isShowing()) {
            AppLogic.runOnUiGround(new Runnable() {

                @Override
                public void run() {
                    mFloatWindow.resumeShow();
                }
            });
        }
    }

    public void notifyRecordShow() {
        toggleFloatBtnMode();
        hideFloatWindow();
    }

    public void notifyRecordDismiss() {
        if (mShowDelayRunnable != null) {
            mShowDelayRunnable.run();
            return;
        }
        resumeFloatWindow();
    }

    /**
     * 显示横条
     */
    public void toggleBarMode() {
        AppLogic.runOnUiGround(new Runnable() {

            @Override
            public void run() {
                if (mFloatWindow != null && mFloatWindow.isShowing()) {
                    mFloatWindow.switchMode(FloatWindow.MODE_BAR, false);
                }
            }
        });
    }

    /**
     * 切换到按钮模式
     */
    public void toggleFloatBtnMode() {
        if (!isShowHelpFloat()) {
            return;
        }
        AppLogic.runOnUiGround(new Runnable() {

            @Override
            public void run() {
                if (mFloatWindow != null && mFloatWindow.isShowing()) {
                    canShow = false;
                    mFloatWindow.switchMode(FloatWindow.MODE_HIDE, false);
                }
            }
        });
    }

    /**
     * 切换到详细界面
     */
    public void toggleDetailMode() {
        AppLogic.runOnUiGround(new Runnable() {

            @Override
            public void run() {
                if (mFloatWindow != null && mFloatWindow.isShowing()) {
                    mFloatWindow.switchMode(FloatWindow.MODE_DETAIL, false);
                }
            }
        });
    }

    public void delayShowToggleDetailMode() {
        AppLogic.runOnUiGround(new Runnable() {

            @Override
            public void run() {
                if (mFloatWindow != null && mFloatWindow.isShowing()) {
                    mFloatWindow.switchMode(FloatWindow.MODE_DETAIL, false);
                }
            }
        }, 2000);
    }

    /**
     * 当help为空的时候，此时不存在导航场景的唤醒词，相当于后台并无路径
     */
    public void notifyNavHelp(List<String> wps) {
        if(NavAppManager.getInstance().hasRemoteNavTool()){
            return;
        }
        if (wps == null) {
            AUTO_SHRINK_TIME = 30000;
            unRegisterNavHelp();
        } else {
            if(HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HIGH_TIPS,"0").equals("1")){
                AUTO_SHRINK_TIME = 20000;
            }
            mNavPlan = (mState & STATE_NAVING_MARK) == STATE_NAVING_MARK;
            registerNavHelp(mFirstOpenNav,mNavPlan,wps);
        }
    }

    public void registerNavHelp(boolean isOpen, boolean isNav, List<String> wps) {
        TipsInfo tipsInfo = new TipsInfo();
        tipsInfo.setForeScene(JinshouzhiManager.TYPE_NAV);
        tipsInfo.setOpenWindows(isOpen);
        tipsInfo.setNav(isNav);
        tipsInfo.setWpLists(wps);
        Gson gson = new Gson();
        String msg = gson.toJson(tipsInfo);
        JinshouzhiManager.getInstance().registerTipsInfo(msg);
        refreshHelpTips();
        if(isOpen){
            toggleDetailMode();
            reportClickFloatEven(true,false);
        }
    }

    public void unRegisterNavHelp(){
        TipsInfo tipsInfo = new TipsInfo();
        tipsInfo.setForeScene(JinshouzhiManager.TYPE_NAV);
        Gson gson = new Gson();
        String msg = gson.toJson(tipsInfo);
        JinshouzhiManager.getInstance().unRegisterTipsInfo(msg);
    }

    /**
     * 反馈场景注册
     * @param wps 指令
     * @param isOpen 是否展示
     */
    public void registerFeedbackScene(List<String> wps, boolean isOpen) {
        TipsInfo tipsInfo = new TipsInfo();
        tipsInfo.setForeScene(JinshouzhiManager.TYPE_OTHER);
        tipsInfo.setWpLists(wps);
        Gson gson = new Gson();
        String msg = gson.toJson(tipsInfo);
        JinshouzhiManager.getInstance().registerTipsInfo(msg);
        showWindowInner(true);//打开金手指
        refreshHelpTips();
        if (isOpen) {
            toggleDetailMode();
            reportClickFloatEven(true, false);
        }
    }

    /**
     * 反注册反馈场景
     */
    public void unRegisterFeedbackScene(){
        TipsInfo tipsInfo = new TipsInfo();
        tipsInfo.setForeScene(JinshouzhiManager.TYPE_OTHER);
        Gson gson = new Gson();
        String msg = gson.toJson(tipsInfo);
        JinshouzhiManager.getInstance().unRegisterTipsInfo(msg);
        if(!mNeedHelpFloat){
            dismiss_Inner();
        }
    }

    private boolean mShowNavBar = true;

    /**
     * 获取当前生效的唤醒词
     *
     * @return
     */
    private String[] getCurrActivityKws(List<String> otherwise) {
        String[] curKeys = WakeupManager.getInstance().getActiveKeyWords();
        List<String> tmpList = new ArrayList<String>();
        if (curKeys != null && curKeys.length > 0) {
            for (String key : curKeys) {
                boolean found = false;
                if (otherwise != null) {
                    for (String o : otherwise) {
                        if (key.equalsIgnoreCase(o)) {
                            found = true;
                            break;
                        }
                    }
                }
                if (found) {
                    continue;
                }
                if (!tmpList.contains(key)) {
                    tmpList.add(key);
                }
            }
        }
        return tmpList.toArray(new String[tmpList.size()]);
    }

    /**
     * 随机取count条不相同的指令
     *
     * @param container
     * @param sources
     * @param count
     */
    private void randomCmd(List<String> container, List<String> sources, int count) {
        List<String> tmpList = new ArrayList<String>();
        tmpList.addAll(sources);
        if (container.size() > 0) {
            tmpList.removeAll(container);
        }
        if (count > tmpList.size()) {
            count = tmpList.size();
        }
        for (int i = 0; i < count; i++) {
            int idx = new Random().nextInt(tmpList.size());
            String cmd = tmpList.remove(idx);
            container.add(cmd);
        }
    }

    /**
     * 从List 中随机获取一条数据
     *
     * @param cmds
     * @return
     */
    private String randomCmdFromList(List<String> cmds) {
        if (cmds == null || cmds.size() <= 0) {
            return "";
        }
        return cmds.get(new Random().nextInt(cmds.size()));
    }

    /**
     * 从数组中随机获取一条数据
     *
     * @param array
     * @return
     */
    private String randomCmdFromArray(String[] array) {
        if (array == null || array.length <= 0) {
            return "";
        }

        return array[new Random().nextInt(array.length)];
    }

    /**
     * 场景下随机取一条免唤醒指令
     *
     * @return
     */
    private String getRandomCmd(List<String> owner, List<String> third, int senceType) {
        String cmd = "";
        if (third != null) {
            List<String> cmds = third;
            if (cmds != null && cmds.size() > 0) {
                cmd = randomCmdFromList(cmds);
            }
        }
        if (TextUtils.isEmpty(cmd)) {
            String[] kws = getCurrActivityKws(owner);
            if (kws != null && kws.length > 0) {
                int idx = new Random().nextInt(kws.length);
                cmd = kws[idx];
            }
        }
        LogUtil.logd("getRandomCmd cmd:" + cmd);
        return cmd;
    }

    private String mCustomMusicTaskId = "";//"TASK_CTRL_MUSIC";

    public void setCustomMusicTaskId(String taskId) {
        LogUtil.logd("setCustomMusicTaskId:" + taskId);
        mCustomMusicTaskId = taskId;
    }

    private String[] filterIds = new String[]{"ASR_TASK_ID_QUICK_REPORT"};
    private Map<String, String[]> mTaskMap = new HashMap<String, String[]>();

    public void interceptTTWakeupTask(String serviceName, String taskId, String[] cmds) {
        if (TextUtils.isEmpty(mCustomMusicTaskId) || !mCustomMusicTaskId.equals(taskId)) {
            if (!ServiceManager.MUSIC.equals(serviceName)) {
                return;
            }
            for (String fId : filterIds) {
                if (fId.equals(taskId)) {
                    return;
                }
            }
        }
        LogUtil.logd("notifyMusicHelp serviceName:" + serviceName + ",taskId:" + taskId);
        if (mTaskMap.containsKey(taskId)) {
            mTaskMap.remove(taskId);
        }
        mTaskMap.put(taskId, cmds);
    }

    public void recoverTaskId(String serviceName, String taskId) {
        if (TextUtils.isEmpty(mCustomMusicTaskId) || !mCustomMusicTaskId.equals(taskId)) {
            if (!ServiceManager.MUSIC.equals(serviceName)) {
                return;
            }
            for (String fId : filterIds) {
                if (fId.equals(taskId)) {
                    return;
                }
            }
        }
        LogUtil.logd("notifyMusicHelp recoverTaskId:" + serviceName + ",taskId:" + taskId);
        if (mTaskMap.containsKey(taskId)) {
            mTaskMap.remove(taskId);
        }
    }

    private String[] updateMusicKws() {
        Set<String> kwsSet = new HashSet<String>();
        for (String key : mTaskMap.keySet()) {
            String[] kws = mTaskMap.get(key);
            for (String kw : kws) {
                kwsSet.add(kw);
            }
        }
        return kwsSet.toArray(new String[kwsSet.size()]);
    }

    /**
     * 获取音乐场景的展示指令
     *
     * @return
     */
    private List<String> getMusicAsrCmds() {
        List<String> tmpList = new ArrayList<String>();
        putDefaultCmdsToList(tmpList, "RS_FLOAT_HELP_MUSIC_ASR_CMD");
        int idx = new Random().nextInt(tmpList.size());
        String cmd1 = tmpList.get(idx);
        tmpList.clear();
        putDefaultCmdsToList(tmpList, "RS_FLOAT_HELP_MUSIC_ASR_OTHER_CMD");
        List<String> strs = new ArrayList<String>();
        strs.add(cmd1);
        randomCmd(strs, tmpList, 5);
        return strs;
    }

    /**
     * 获取当前需要触发的场景类型
     *
     * @return
     */
    private int getCurrSenceType() {
        if ((mState & STATE_MUSIC_FOCUS_MARK) == STATE_MUSIC_FOCUS_MARK) {
            return SCENE_TYPE_MUSIC;
        }
        if ((mState & STATE_NAV_FOCUS_MARK) == STATE_NAV_FOCUS_MARK) {
            return SCENE_TYPE_NAV;
        }
        return SCENE_TYPE_HOME;
    }

    public void notifyWebchatStatus(boolean isFocus){
        if(isFocus){
            toggleFloatBtnMode();
            mState |= STATE_WEBCHAT_FOCUS;
        }else{
            mState &= ~STATE_WEBCHAT_FOCUS;
        }
    }

    public void notifyNavStatus(boolean isFocus, boolean isInNav) {
        if (isFocus) {
            toggleFloatBtnMode();
            if (mNavExited) {
                mShowNavBar = true;
            }
            mNavExited = false;
            mState |= STATE_NAV_FOCUS_MARK;
        } else {
            mState &= ~STATE_NAV_FOCUS_MARK;
            mFirstOpenNav = false;
        }

        if (isInNav) {
            mState |= STATE_NAVING_MARK;
        } else {
            mState &= ~STATE_NAVING_MARK;
        }
        LogUtil.logd("notifyNavStatus mState:" + mState);
    }

    /**
     * 唤醒是否启用
     *
     * @return
     */
    private boolean isEnableWakeup() {
        return WakeupManager.getInstance().mEnableWakeup;
    }

    private String mDeviceKws;

    private String randomWakeupKws() {
        if (!isEnableWakeup()) { // 如果禁用了唤醒功能
            return "";
        }
        if (!TextUtils.isEmpty(mDeviceKws)) {
            return mDeviceKws;
        }

        String command = "";
        String[] nicks = WakeupManager.getInstance().getWakeupKeywords_User();
        if (nicks == null || nicks.length == 0) {
            nicks = WakeupManager.getInstance().getWakeupKeywords_Sdk();
        }
        if (nicks == null || nicks.length == 0) {
            command = "";
        } else {
            command = nicks[new Random().nextInt(nicks.length)];
        }
        mDeviceKws = command;

        return command;
    }

    public void refreshHelpTips(){
        mWpTxtList.clear();
        mWpTxtList = JinshouzhiManager.getInstance().getWpList();
        LogUtil.d("registerHelpTips  mWpTxtList :"+mWpTxtList.toString());
        refreshHelpFloat(false);
    }

    public void refreshHelpFloat(boolean isNoWp, final boolean isEnableHori) {
        AppLogic.runOnUiGround(new Runnable1<Boolean>(isNoWp) {

            @Override
            public void run() {
                if (mFloatWindow == null) {
                    return;
                }
                if (isEnableHori) {
                    mFloatWindow.switchMode(FloatWindow.MODE_HIDE, false);
                }
                if(mFloatWindow.getCurrMode() == FloatWindow.MODE_HIDE){
                    mFloatWindow.refreshHelpFloat(mP1);
                }
            }
        });
    }

    public void refreshHelpFloat(boolean isNoWp) {
        AppLogic.runOnUiGround(new Runnable1<Boolean>(isNoWp) {

            @Override
            public void run() {
                if (mFloatWindow == null) {
                    return;
                }
                mFloatWindow.refreshHelpFloat(mP1);
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////
    public static final int STATE_MUSIC_PLAYING_MARK = 1 << 0;
    public static final int STATE_NAVING_MARK = 1 << 1;
    public static final int STATE_NAV_FOCUS_MARK = 1 << 2;
    public static final int STATE_MUSIC_FOCUS_MARK = 1 << 3;
    public static final int STATE_WEBCHAT_FOCUS = 1 << 4;

    private int mState;


    private void putDefaultCmdsToList(List<String> list, String keyId) {
        if (mIdStrMap != null && mIdStrMap.containsKey(keyId)) {
            String[] ids = mIdStrMap.get(keyId);
            if (ids != null) {
                for (String id : ids) {
                    list.add(id);
                }
            }
        }
    }

    private void onFloatWindowShow() {
    }

    /**
     * 点中图标
     *
     * @param v
     */
    private void onFloatBtnClick(View v) {
        int curMode = mFloatWindow.getCurrMode();
        // 展开详情
        switch (curMode) {
            case HelpFloatWindow.MODE_BAR:
                toggleFloatBtnMode();
                break;
            case HelpFloatWindow.MODE_HIDE:
                reportFloatEvent(true, null);
                reportClickFloatEven(true,true);
                refreshHelpTips();
                toggleDetailMode();
                break;
        }
    }

    /**
     * 展示指令列表
     */
    private void showDetailFloatWindows(){

    }

    /**
     * 点击详情中的隐藏
     *
     * @param v
     */
    private void onFloatDetailHideClick(View v) {
        //反馈的时候手动点击需要直接关闭悬浮窗
        if(!mNeedHelpFloat){
            dismiss_Inner();
        }
        toggleFloatBtnMode();
        reportClickFloatEven(false,true);
    }

    private int barIndex;
    private List<String> mBarTxtList = new ArrayList<String>();
    private List<String> mCmdTxtList = new ArrayList<String>();
    private List<String> mWpTxtList = new ArrayList<String>();

    /**
     * 更新横条的文本
     *
     * @param switcher
     */
    private void onTextSwitcherUpdate(TextView switcher) {
        if (mBarTxtList == null) {
            switcher.setText("");
            return;
        }
        if (barIndex == mBarTxtList.size()) {
            barIndex = 0;
        }
        if (mBarTxtList.size() > barIndex) {
            switcher.setText(mBarTxtList.get(barIndex));
        } else {
            LogUtil.loge("onTextSwitcherUpdate barIndex to max:" + barIndex);
        }
    }

    /**
     * 更新唤醒帮助文本
     *
     * @param oneTv
     * @param twoTv
     */
    private void onCmdTextViewUpdate(TextView oneTv, TextView twoTv, TextView threeTv, TextView fourTv, TextView fiveTv,
                                     TextView sixTv) {
        if (mCmdTxtList == null) {
            return;
        }
        int len = mCmdTxtList.size();
        if (len > 0) {
            oneTv.setText(mCmdTxtList.get(0));
            oneTv.setVisibility(View.VISIBLE);
        } else {
            oneTv.setText("");
            oneTv.setVisibility(View.GONE);
        }
        if (len > 1) {
            twoTv.setText(mCmdTxtList.get(1));
            twoTv.setVisibility(View.VISIBLE);
        } else {
            twoTv.setText("");
            twoTv.setVisibility(View.GONE);
        }
        if (len > 2) {
            threeTv.setText(mCmdTxtList.get(2));
            threeTv.setVisibility(View.VISIBLE);
        } else {
            threeTv.setText("");
            threeTv.setVisibility(View.GONE);
        }
        if (len > 3) {
            fourTv.setText(mCmdTxtList.get(3));
            fourTv.setVisibility(View.VISIBLE);
        } else {
            fourTv.setText("");
            fourTv.setVisibility(View.GONE);
        }
        if (len > 4) {
            fiveTv.setText(mCmdTxtList.get(4));
            fiveTv.setVisibility(View.VISIBLE);
        } else {
            fiveTv.setText("");
            fiveTv.setVisibility(View.GONE);
        }
    }

    /**
     * 更新免唤醒帮助文本
     *
     * @param oneTv
     * @param twoTv
     * @param threeTv
     * @param fourTv
     */
    private void onWpTextViewUpdate(TextView oneTv, TextView twoTv, TextView threeTv, TextView fourTv) {
        if (mWpTxtList == null) {
            return;
        }
        int len = mWpTxtList.size();
        if (len > 0) {
            oneTv.setText(mWpTxtList.get(0));
            oneTv.setVisibility(View.VISIBLE);
        } else {
            oneTv.setText("");
            oneTv.setVisibility(View.GONE);
        }
        if (len > 1) {
            twoTv.setText(mWpTxtList.get(1));
            twoTv.setVisibility(View.VISIBLE);
        } else {
            twoTv.setText("");
            twoTv.setVisibility(View.GONE);
        }
        if (len > 2) {
            threeTv.setText(mWpTxtList.get(2));
            threeTv.setVisibility(View.VISIBLE);
        } else {
            threeTv.setText("");
            threeTv.setVisibility(View.GONE);
        }
        if (len > 3) {
            fourTv.setText(mWpTxtList.get(3));
            fourTv.setVisibility(View.VISIBLE);
        } else {
            fourTv.setText("");
            fourTv.setVisibility(View.GONE);
        }
    }

    private int mDelayMillis = 200;
    private int mWinType = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 10;

    public void setWinType(int winType) {
        LogUtil.logd("HelpGuideManager setWinType:" + winType);
        mWinType = winType;
        if (mFloatWindow != null) {
            mFloatWindow.updateWinType();
        }
    }

    public void setHelpFloatRefreshDelay(int delay) {
        LogUtil.logd("setHelpFloatRefreshDelay:" + delay);
        mDelayMillis = delay;
    }

    public interface FloatWindow {
        public static final int MODE_BAR = 1;
        public static final int MODE_HIDE = 2;
        public static final int MODE_DETAIL = 3;

        boolean isShowing();

        void show();

        void dismiss();

        void switchMode(int mode, boolean keyEvent);
    }

    private class HelpFloatWindow extends AbsHelpFloatWindow {
        private int mDisplayMode = MODE_HIDE;
        private View mFloatBtnView;
        private ImageView hideDtBtn;
        private TextView mBarTv;
        private View mFloatBtnLy;
        private FrameLayout mDetailLy;
        private View mFloatContentLy;
        private TextView mCmdOneTv;
        private TextView mCmdTwoTv;
        private TextView mCmdThreeTv;
        private TextView mCmdFourTv;
        private TextView mCmdFiveTv;
        private TextView mCmdSixTv;
        private TextView mWpTipTv;
        private TextView mWpOneTv;
        private TextView mWpTwoTv;
        private TextView mWpThreeTv;
        private TextView mWpFourTv;
        private LinearLayout mLayout;

        /////////////////////////////////
        private int mFloatBtnWidth;
        private int mFloatBtnHeight;

        private int mBarLyWidth;
        private int mBarLyHeight;

        private int mDetailLyWidth;
        private int mDetailLyHeight;
        private static final String KEY = "HelpFloatWindowPosition";
        /////////////////////////////////

        public HelpFloatWindow(Context context) {
            super(context);
            initView();
        }

        private void initView() {
            LayoutInflater.from(getContext()).inflate(R.layout.help_float_layout, this);
            mFloatContentLy = findViewById(R.id.help_float_ly);
            mFloatBtnView = findViewById(R.id.float_btn_iv);
            mFloatBtnLy = findViewById(R.id.float_bar_ly);
            hideDtBtn = (ImageView) findViewById(R.id.detail_hide_iv);
            mDetailLy = (FrameLayout) findViewById(R.id.float_detail_ly);
            mBarTv = (TextView) findViewById(R.id.float_bar_ts);
            mCmdOneTv = (TextView) findViewById(R.id.cmd_one_tv);
            mCmdTwoTv = (TextView) findViewById(R.id.cmd_two_tv);
            mCmdThreeTv = (TextView) findViewById(R.id.cmd_three_tv);
            mCmdFourTv = (TextView) findViewById(R.id.cmd_four_tv);
            mCmdFiveTv = (TextView) findViewById(R.id.cmd_five_tv);
            mCmdSixTv = (TextView) findViewById(R.id.cmd_six_tv);
            mWpTipTv = (TextView) findViewById(R.id.wp_tip_tv);
            mWpOneTv = (TextView) findViewById(R.id.wp_one_tv);
            mWpTwoTv = (TextView) findViewById(R.id.wp_two_tv);
            mWpThreeTv = (TextView) findViewById(R.id.wp_three_tv);
            mWpFourTv = (TextView) findViewById(R.id.wp_four_tv);
            mLayout = (LinearLayout) findViewById(R.id.layout_tips);
            hideDtBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFloatDetailHideClick(HelpFloatWindow.this);
                }
            });
            initDetailView();
            setViewVisible(MODE_HIDE, null);
        }

        private boolean mAlreadyLayouted = false;

        protected void onViewOnLayouted() {
            if (mAlreadyLayouted) { // 仅在第一次做计算赋值操作
                return;
            }

            int m = (int) getResources().getDimension(R.dimen.x4);
            mFloatBtnWidth = mFloatBtnView.getWidth() + m * 2;
            mFloatBtnHeight = mFloatBtnView.getHeight();
            mBarLyWidth = mFloatBtnLy.getWidth();
            mBarLyHeight = mFloatBtnLy.getHeight();
            mDetailLyWidth = mDetailLy.getWidth();
            mDetailLyHeight = mDetailLy.getHeight();
            rootWidth = getRootView().getWidth();
            rootHeight = getRootView().getHeight();
            setLayoutStyle();
            LogUtil.logd("onViewPreDraw mFloatBtnWidth:" + mFloatBtnWidth + ",mFloatBtnHeight:" + mFloatBtnHeight
                    + ",mBarLyWidth:" + mBarLyWidth + ",mBarLyHeight:" + mBarLyHeight + ",mDetailLyWidth:"
                    + mDetailLyWidth + ",mDetailLyHeight:" + mDetailLyHeight);
            mAlreadyLayouted = true;
        }

        /**
         * 获取配置文件里的宽高
         */
        public void initDetailView() {
            int width = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_HELP_FLOAT_DETAIL_WIDTH, 0);
            int height = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_HELP_FLOAT_DETAIL_HEIGHT, 0);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if(width != 0){
                layoutParams.width = width;
            }
            if(height != 0){
                layoutParams.height = height;
            }
            mDetailLy.setLayoutParams(layoutParams);
        }

        public void setLayoutStyle() {
            if (mLp.x == 0) {
                int left = (int) getResources().getDimension(R.dimen.x24);
                int right = (int) getResources().getDimension(R.dimen.x40);
                int top = (int) getResources().getDimension(R.dimen.y18);
                mLayout.setPadding(left, top, right, top);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
                hideDtBtn.setLayoutParams(layoutParams);
                int padding = (int) getResources().getDimension(R.dimen.m10);
                hideDtBtn.setPadding(padding, padding, 0, padding);
                hideDtBtn.setImageDrawable(getResources().getDrawable(R.drawable.icon_detail_hide));
                FrameLayout.LayoutParams layoutDetail = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutDetail.height = mDetailLyHeight * (mWpTxtList.size() + 1) / 5;
                mDetailLy.setLayoutParams(layoutDetail);
                mDetailLy.setBackground(getResources().getDrawable(R.drawable.icon_float_detail_bg));
            } else {
                int left = (int) getResources().getDimension(R.dimen.x40);
                int right = (int) getResources().getDimension(R.dimen.x24);
                int top = (int) getResources().getDimension(R.dimen.y18);
                mLayout.setPadding(left, top, right, top);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
                hideDtBtn.setLayoutParams(layoutParams);
                int padding = (int) getResources().getDimension(R.dimen.m10);
                hideDtBtn.setPadding(0, padding, padding, padding);
                hideDtBtn.setImageDrawable(getResources().getDrawable(R.drawable.icon_detail_hide_right));
                FrameLayout.LayoutParams layoutDetail = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutDetail.height = mDetailLyHeight * (mWpTxtList.size() + 1) / 5;
                mDetailLy.setLayoutParams(layoutDetail);
                mDetailLy.setBackground(getResources().getDrawable(R.drawable.icon_float_detail_bg_right));
            }
        }

        /**
         * 获取当前显示的View
         *
         * @return
         */
        private int[] getCurrWH(int mode) {
            int[] wh = new int[2];
            switch (mode) {
                case MODE_BAR:
                    wh[0] = mBarLyWidth;
                    wh[1] = mBarLyHeight;
                    LogUtil.logd("MODE_BAR w:" + wh[0] + ",h:" + wh[1]);
                    break;
                case MODE_DETAIL:
//				wh[0] = mDetailLyWidth;
//				wh[1] = mDetailLyHeight;
                    wh[0] = mDetailLyWidth;
                    wh[1] = mDetailLyHeight * (mWpTxtList.size() + 1) / 5;
                    LogUtil.logd("MODE_DETAIL w:" + wh[0] + ",h:" + wh[1]);
                    break;
                case MODE_HIDE:
                    wh[0] = mFloatBtnWidth;
                    wh[1] = mFloatBtnHeight;
                    LogUtil.logd("MODE_HIDE w:" + wh[0] + ",h:" + wh[1]);
                    break;
                default:
                    wh[0] = mFloatBtnWidth;
                    wh[1] = mFloatBtnHeight;
                    break;
            }
            return wh;
        }

        private int[] getCurrXY(int currMode) {
            int cX = mLp != null ? mLp.x : 0;
            // int cY = mLp != null ? mLp.y : 0;
            int[] xy = new int[2];
            switch (currMode) {
                case MODE_BAR:
                case MODE_HIDE:
                    xy[0] = historyX;
                    xy[1] = historyY;
                    break;
                case MODE_DETAIL:
                    xy[0] = cX;
                    if (ScreenUtil.getScreenHeight() > ScreenUtil.getScreenWidth()) {
                        xy[1] = 0;
                    } else {
                        xy[1] = rootHeight / 2 - mDetailLyHeight / 2;
                    }
                    break;
            }
            return xy;
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    onViewOnLayouted();
                    int[] wh = getCurrWH(mDisplayMode);
                    mWidth = wh[0];
                    mHeight = wh[1];
                    if (!mResumeOpen) {
                        // 初始化居中显示
                        int h = (rootHeight - mHeight) / 2;
                        int position = TXZFileConfigUtil.getIntSingleConfig(KEY, 0);
                        if (position == 1) {
                            mLp.x = getRootView().getWidth();
                            historyX = mLp.x;
                        }
                        setLayoutXY(mLp.x, h, false);
                        historyY = h;
                    }
                    setLayoutWH(mWidth, mHeight, true);
                    LogUtil.logd("onPreDraw rootWidth:" + rootWidth + ",rootHeight:" + rootHeight + ",mWidth:" + mWidth
                            + ",mHeight:" + mHeight);
                    return false;
                }
            });
        }

        public void refreshHelpTips() {
            onTextSwitcherUpdate(mBarTv);
        }

        public void refreshCmdTips() {
            onCmdTextViewUpdate(mCmdOneTv, mCmdTwoTv, mCmdThreeTv, mCmdFourTv, mCmdFiveTv, mCmdSixTv);
        }

        public void refreshWpTips() {
            onWpTextViewUpdate(mWpOneTv, mWpTwoTv, mWpThreeTv, mWpFourTv);
        }

        /**
         * 请求重刷帮助浮窗
         */
        public void refreshHelpFloat(boolean isNoWp) {
            if (isNoWp) {
                mWpTipTv.setVisibility(View.GONE);
            } else {
                mWpTipTv.setVisibility(View.VISIBLE);
            }
            onTextSwitcherUpdate(mBarTv);
            refreshCmdTips();
            refreshWpTips();
        }

        @Override
        protected View dragView() {
            return mFloatBtnView;
        }

        public void scheduleAnim(int sWidth, int eWidth, AnimatorListener listener) {
            LogUtil.logd("scheduleAnim sWidth:" + sWidth + ",eWidth:" + eWidth);
            ValueAnimator animator = ValueAnimator.ofInt(sWidth, eWidth);
            animator.setDuration(SHRINK_ANIM_DURATION);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(new AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    changeLayoutWith(getAnimTargetView(), (Integer) animation.getAnimatedValue());
                }
            });
            if (listener != null) {
                animator.addListener(listener);
            }
            animator.start();
        }

        private View getAnimTargetView() {
            return mBarTv;
        }

        @Override
        protected void onTouchEventUp(MotionEvent event) {
            int left = mFloatContentLy.getLeft();
            int right = mFloatContentLy.getRight();
            LogUtil.logd("left:" + left + ",right:" + right);
        }

        private void changeLayoutWith(View v, int width) {
            if (v != null) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                if (params == null) {
                    params = new FrameLayout.LayoutParams(width, v.getHeight());
                }
                params.width = width;
                v.setLayoutParams(params);
            }
        }
        @Override
        public void switchMode(final int mode, boolean keyEvent) {
            LogUtil.logd("help switchMode:" + mode + ",old mode:" + mDisplayMode + ",keyEvent:" + keyEvent);
            //显示的时候文本条数可能会变，所以窗口大小需要更新
            if (mDisplayMode == mode && mDisplayMode == MODE_HIDE) {
                return;
            }

            // 更新Win的Flag
            updateWinFlag(mode);
            try {
                this.mDisplayMode = mode;
                if (mode != MODE_HIDE) {
                    setViewVisible(mode, null);
                }
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        if (mode != MODE_HIDE) {
                            setLayoutStyle();
                            resetXYWH(mode, true);
                        } else {
                            resetHideMode(mode);
                        }
                    }
                });
                if(mode == MODE_HIDE){
                    setViewVisible(mode, new Runnable() {
                        @Override
                        public void run() {
                            AppLogic.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    resetHideWH(mDisplayMode);
                                }
                            });
                        }
                    });
                }
            } catch (Exception e) {
                LogUtil.logw(e.getMessage());
            }
        }

        public int getCurrMode() {
            return mDisplayMode;
        }

        private void resetXYWH(int mode, boolean resetXY) {
            int[] wh = getCurrWH(mode);
            setLayoutWH(wh[0], wh[1], false);
            if (resetXY) {
                resetXY(mode);
            }
        }

        private void resetXY(int mode) {
            int[] xy = getCurrXY(mode);
            setLayoutXY(xy[0], xy[1], true);
        }

        private void resetHideMode(int mode) {
            int[] xy = getCurrXY(mode);
            setLayoutXY(xy[0], xy[1], false);
        }

        private void resetHideWH(int mode) {
            int[] wh = getCurrWH(mode);
            setLayoutWH(wh[0], wh[1], true);
        }

        private void setViewVisible(int mode, final Runnable animEndRun) {
            switch (mode) {
                case MODE_BAR:
                    if (mBarLyWidth > 0) {
                        scheduleAnim(mBarLyHeight, mBarLyWidth, null);
                    }
                    AppLogic.runOnUiGround(mCancelWeltTask);
                    mDetailLy.setVisibility(INVISIBLE);
                    mBarTv.setVisibility(VISIBLE);
                    mFloatBtnView.setVisibility(VISIBLE);
                    mFloatBtnLy.setVisibility(VISIBLE);
                    checkAutoHideBar();
                    break;

                case MODE_DETAIL:
                    AppLogic.removeUiGroundCallback(mDetailRunnable);
                    AppLogic.removeUiGroundCallback(mDelayVisibleRunnable);
                    AppLogic.runOnUiGround(mDetailRunnable, mDelayMillis);
                    mFloatBtnView.setVisibility(INVISIBLE);
                    mFloatBtnLy.setVisibility(INVISIBLE);
                    checkAutoHideBar();//经过30s(AUTO_SHRINK_TIME )后指令列表自动收缩
                    break;
                case MODE_HIDE:
                    mDetailLy.setVisibility(INVISIBLE);
                    if(animEndRun != null){
                        animEndRun.run();
                    }
                    AppLogic.runOnUiGround(mDelayVisibleRunnable,mDelayMillis);
                    break;
                default:
                    break;
            }
        }

        Runnable mDetailRunnable = new Runnable() {

            @Override
            public void run() {
                mDetailLy.setVisibility(VISIBLE);
            }
        };

        Runnable mDelayVisibleRunnable = new Runnable() {

            @Override
            public void run() {
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        mFloatBtnView.setVisibility(VISIBLE);
                        mFloatBtnLy.setVisibility(VISIBLE);
                        canShow = true;
                    }
                });
            }
        };

        private void checkAutoHideBar() {
            AppLogic.removeUiGroundCallback(mHideBarTask);
            AppLogic.runOnUiGround(mHideBarTask, AUTO_SHRINK_TIME);
        }

        Runnable mHideBarTask = new Runnable() {

            @Override
            public void run() {
                //自动关闭的时候，需要直接关闭悬浮窗
                if(!mNeedHelpFloat){
                    dismiss_Inner();
                }
                if (mDisplayMode != MODE_HIDE) {
                    switchMode(MODE_HIDE, false);
                    reportClickFloatEven(false,false);
                }
            }
        };

        Runnable mWeltTask = new Runnable() {

            @Override
            public void run() {
                if (mDisplayMode == MODE_HIDE) {
                    animatorMethod(mFloatContentLy, mFloatContentLy.getLeft(), mFloatContentLy.getLeft() - 10, 0);
                }
            }
        };

        Runnable mCancelWeltTask = new Runnable() {

            @Override
            public void run() {
                animatorMethod(mFloatContentLy, mFloatContentLy.getLeft(), 0, 0);
            }
        };

        private void animatorMethod(View targetView, int ox, int dx, int duration) {
            if (duration <= 0) {
                targetView.offsetLeftAndRight(dx);
            }
        }
    }

    private abstract class AbsHelpFloatWindow extends LinearLayout implements FloatWindow {
        private WindowManager mWinManager;
        protected WindowManager.LayoutParams mLp;
        private boolean mAlreadyOpen;
        protected boolean mResumeOpen;
        protected int mWidth, mHeight;
        protected int rootWidth, rootHeight;
        private int mTouchSlop;

        public AbsHelpFloatWindow(Context context) {
            super(context);
            mWinManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            switchMode(MODE_HIDE, true);
            return super.dispatchKeyEvent(event);
        }

        protected abstract View dragView();

        @Override
        public boolean isShowing() {
            return mAlreadyOpen;
        }

        /**
         * 更新浮窗的宽高
         *
         * @param w
         * @param h
         * @param refresh
         */
        protected void setLayoutWH(int w, int h, boolean refresh) {
            if (mLp != null) {
                mLp.width = w;
                mLp.height = h;
                mWidth = w;
                mHeight = h;
                LogUtil.logd("lp width:" + w + ",lp height:" + h);
                try {
                    if (refresh) {
                        mWinManager.updateViewLayout(this, mLp);
                    }
                } catch (Exception e) {
                    LogUtil.logw("setLayoutWH window not attach！");
                }
            }
        }

        protected void setLayoutXY(int x, int y, boolean refresh) {
            if (mLp != null) {
                mLp.x = x;
                mLp.y = y;
                LogUtil.logd("lp x:" + x + ",lp y:" + y);
                try {
                    if (refresh) {
                        mWinManager.updateViewLayout(this, mLp);
                    }
                } catch (Exception e) {
                    LogUtil.logw("setLayoutXY window not attach！");
                }
            }
        }

        public void updateWinType() {
            LogUtil.logd("updateWinType:" + mWinType);
            mWinManager.removeView(this);
            mLp.type = mWinType;
            mWinManager.addView(this, mLp);
        }

        @SuppressLint("RtlHardcoded")
        @Override
        public void show() {
            if (mAlreadyOpen) {
                return;
            }
            mLp = new WindowManager.LayoutParams(mWinType, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            mLp.type = mWinType;
            mLp.width = WindowManager.LayoutParams.MATCH_PARENT;
            mLp.height = WindowManager.LayoutParams.MATCH_PARENT;
            mLp.windowAnimations = 0;
            mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
			mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE; /**
            // Window flag: this window can never receive touch events. */
            mLp.format = PixelFormat.RGBA_8888;
            mLp.gravity = Gravity.LEFT | Gravity.TOP | Gravity.START;
            historyX = 0;
            historyY = 0;
            mLp.x = historyX;
            mLp.y = historyY;
            mResumeOpen = false;
            LogUtil.logd("help show x:" + mLp.x + ",y:" + mLp.y);
            mWinManager.addView(this, mLp);
            try {
                getContext().registerReceiver(mHomeReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            } catch (Exception e) {
            }
            onFloatWindowShow();
            mAlreadyOpen = true;
        }

        void updateWinFlag(int mode) {
            switch (mode) {
                case MODE_BAR:
                case MODE_DETAIL:
                    mLp.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                    mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                    mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    // mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    // /**
                    // Window flag: this window can never receive touch events. */
                    break;
                case MODE_HIDE:
                    mLp.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                    mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                    mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    break;
            }
        }

        @Override
        public void dismiss() {
            if (mAlreadyOpen) {
                LogUtil.logd("help float dismiss");
                mWinManager.removeView(this);
                mAlreadyOpen = false;
                try {
                    getContext().unregisterReceiver(mHomeReceiver);
                } catch (Exception e) {
                }
            }
        }

        private BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
            private static final String LOG_TAG = "HomeReceiver";
            private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
            private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
            private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
            private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
            private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                LogUtil.logd("onReceive: action: " + action);
                if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    // android.intent.action.CLOSE_SYSTEM_DIALOGS
                    String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                    if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                        // 短按Home键
                        toggleFloatBtnMode();
                    } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                        // 长按Home键 或者 activity切换键
                    } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                        // 锁屏
                    } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                        // samsung 长按Home键
                    }
                }
            }
        };

        public void resumeShow() {
            if (!mAlreadyOpen) {
                if (mAlreadyOpen) {
                    return;
                }
                mResumeOpen = true;
                mLp = new WindowManager.LayoutParams(mWinType, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
                mLp.type = mWinType;
                mLp.width = WindowManager.LayoutParams.MATCH_PARENT;
                mLp.height = WindowManager.LayoutParams.MATCH_PARENT;
                mLp.windowAnimations = 0;
                mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mLp.format = PixelFormat.RGBA_8888;
                mLp.gravity = Gravity.LEFT | Gravity.TOP | Gravity.START;
                mLp.x = historyX;
                mLp.y = historyY;
                LogUtil.logd("help resumeShow x:" + mLp.x + ",y:" + mLp.y);
                mWinManager.addView(this, mLp);
                try {
                    getContext().registerReceiver(mHomeReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                } catch (Exception e) {
                }
                onFloatWindowShow();
                mAlreadyOpen = true;
            }
        }

        private Rect mVoiceAssistantRect = null;
        protected boolean isTouchVoiceAssistant = false;

        /**
         * 判断点击事件是否点击到指定的view上
         *
         * @param view
         * @param x
         * @param y
         * @return
         */
        private boolean isTouchViewRect(View view, int x, int y) {
            if (view.getVisibility() != View.VISIBLE) {
                return false;
            }
            if (null == mVoiceAssistantRect) {
                mVoiceAssistantRect = new Rect();
            }
            view.getDrawingRect(mVoiceAssistantRect);
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            mVoiceAssistantRect.left = location[0];
            mVoiceAssistantRect.top = location[1];
            mVoiceAssistantRect.right = mVoiceAssistantRect.right + location[0];
            mVoiceAssistantRect.bottom = mVoiceAssistantRect.bottom + location[1];
            return mVoiceAssistantRect.contains(x, y);
        }

        private float downX;
        private float downY;
        private int lastLpX;
        private int lastLpY;
        protected int historyX;
        protected int historyY;
        private boolean shouldMove;
        private Rect mCurVisiableRect = new Rect();
        @Override
        public boolean onTouchEvent(MotionEvent event) {
//            if (dragView().getVisibility() != VISIBLE) {
//                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
//                    LogUtil.logd("onTouchEvent MotionEvent.ACTION_OUTSIDE");
//                    // 关闭浮窗
//                    toggleFloatBtnMode();
//                }
//                return super.onTouchEvent(event);
//            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isTouchVoiceAssistant = false;
                    if (isTouchViewRect(dragView(), (int) event.getRawX(), (int) event.getRawY())) {
                        isTouchVoiceAssistant = true;
                        setPressed(true);
                        downX = event.getRawX();
                        downY = event.getRawY();
                        lastLpX = mLp.x;
                        lastLpY = mLp.y;
                        getWindowVisibleDisplayFrame(mCurVisiableRect);
                        rootWidth = mCurVisiableRect.width();
                        rootHeight = mCurVisiableRect.height();
                        return true;
                    }
                case MotionEvent.ACTION_MOVE:
                    if (isTouchVoiceAssistant) {
                        if (Math.abs(event.getRawX() - downX) >= mTouchSlop
                                || Math.abs(event.getRawY() - downY) >= mTouchSlop) {
                            shouldMove = true;
                        }
                        if (shouldMove) {
                            mLp.x = (int) (lastLpX + event.getRawX() - downX);
                            mLp.y = (int) (lastLpY + event.getRawY() - downY);
                            // X轴不动
                            // if (mLp.x < 0) {
                            // mLp.x = 0;
                            // } else if (mLp.x > rootWidth - mWidth) {
                            // mLp.x = rootWidth - mWidth;
                            // }
                            if (mLp.y < 0) {
                                mLp.y = 0;
                            } else if (mLp.y > rootHeight - mHeight) {
                                mLp.y = rootHeight - mHeight;
                            }
                            if (mLp.x < 0) {
                                mLp.x = 0;
                            } else if (mLp.x > rootWidth - mWidth) {
                                mLp.x = rootWidth - mWidth;
                            }
                            LogUtil.logd("mLp x:" + mLp.x + ",mLp y:" + mLp.y);
                            mWinManager.updateViewLayout(this, mLp);
                            historyX = mLp.x;
                            historyY = mLp.y;
                        }
                        return true;
                    }
                case MotionEvent.ACTION_UP:
                    if (isTouchVoiceAssistant) {
                        setPressed(false);
                        if (!shouldMove) {
                            playSoundEffect(SoundEffectConstants.CLICK);
                            onFloatBtnClick(this);
                            return true;
                        }
                        shouldMove = false;
                        // 复位贴边
                        if (mLp.x < mWidth) {
                            mLp.x = 0;
                        } else if (mLp.x > rootWidth - mWidth - mWidth) {
                            mLp.x = rootWidth - mWidth;
                        } else {
                            if (mLp.x > rootWidth / 2) {
                                mLp.x = rootWidth - mWidth;
                            } else {
                                mLp.x = 0;
                            }
                        }
                        if (mLp.y < mHeight) {
                            mLp.y = 0;
                        } else if (mLp.y > rootHeight - mHeight - mHeight) {
                            mLp.y = rootHeight - mHeight;
                        }
                        mWinManager.updateViewLayout(this, mLp);
                        historyX = mLp.x;
                        historyY = mLp.y;
                        HelpFloatXY.getInstance(getContext()).setX(historyX);
                        HelpFloatXY.getInstance(getContext()).setY(historyY);
                        reportMoveFloatEvent(historyX,historyY);
                        JNIHelper.logd("HelpFloatView SET x=" + mLp.x + ", y=" + mLp.y + ", w=" + mLp.width + ",h = "
                                + mLp.height);
                        onTouchEventUp(event);
                        return true;
                    }
            }
            return super.onTouchEvent(event);
        }

        protected void onTouchEventUp(MotionEvent event) {
        }
    }

    private static class HelpFloatXY extends CommonSp {
        private static final String SP_NAME = "help_float_xy";
        private static final String KEY_X = "x";
        private static final String KEY_Y = "y";

        private static HelpFloatXY sXY;

        private HelpFloatXY(Context context) {
            super(context, SP_NAME);
        }

        public static HelpFloatXY getInstance(Context context) {
            if (sXY == null) {
                synchronized (HelpFloatXY.class) {
                    if (sXY == null) {
                        sXY = new HelpFloatXY(context);
                    }
                }
            }
            return sXY;
        }

        public void setX(int x) {
            setValue(KEY_X, x);
        }

        public void setY(int y) {
            setValue(KEY_Y, y);
        }
    }
}