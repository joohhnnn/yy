package com.txznet.launcher.component.nav;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.txz.RecordWinManager;
import com.txznet.launcher.utils.IntentUtils;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZResourceManager;

/**
 * 封装了与高德交互的业务。如首次打开高德时的提示，高德回到前台时通知关闭HUD。
 */
public class NavGdComponent {
    private static final String PACKAGE_NAME_GAODE = "com.autonavi.amapautolite";
    private static final String TASK_ID_GD_HINTS = "task_id_hints";
    private static final String RECV_BROADCAST = "AUTONAVI_STANDARD_BROADCAST_SEND";// 字段中的send是对于高德来说的，而名字的recv是对我们来说。
    private static final String SEND_BROADCAST = "AUTONAVI_STANDARD_BROADCAST_RECV";

    private int mLastTtsId;
    private boolean isUseHintShowing; // 当前使用提示是否处于显示状态
    private boolean isAlreadySetWakeupAsr; // 是否已经设置了免唤醒词
    private int mLastGdPid;
    private boolean isGaodeAlongWayError;

    public void init() {
        /*
            首次进入会触发
            如果触发返回了home，再次进入不会触发
            如果执行了，退出，下次进入会触发
         */
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int KEY_TYPE = intent.getIntExtra("KEY_TYPE", -1);
                int EXTRA_STATE = intent.getIntExtra("EXTRA_STATE", -1);
                LogUtil.logd("receive:" + intent.getAction() + ", KEY_TYPE=" + KEY_TYPE + ", EXTRA_STATE=" + EXTRA_STATE);
                if (10019 == KEY_TYPE) { // 高德地图使用提示
                    if (60 == EXTRA_STATE) { // 使用提示打开
                        isUseHintShowing = true;
                        if (!isAlreadySetWakeupAsr) {
                            TtsUtil.cancelSpeak(mLastTtsId);
                            mLastTtsId = TtsUtil.speakText("请认真阅读高德地图使用提示，确定请说“同意”放弃请说“退出”", new TtsUtil.ITtsCallback() {
                                @Override
                                public void onEnd() {
                                    super.onEnd();
                                    registerWakeupAsr();
                                }
                            });
                        }
                    } else if (2 == EXTRA_STATE) { // FIXME extra_state = 2 不可靠
                        isUseHintShowing = false;
                        releaseWakeupAsr();
                    } else if (1 == EXTRA_STATE) { // 地图模块初始化完成，创建完成
                        notifyCancelHud();
                    }
                } else if (12105 == KEY_TYPE) { // 躲避拥堵弹窗信息
                    ctrlAvoidTrafficJam(false);
                } else if (13028 == KEY_TYPE) { // 高德错误弹框处理
                    /*
                     * 1. 监听9月20日新版本增加的广播协议：
                     * 作用:导航显示弹窗的时候对外发送该对话框的信息
                     * 参数说明：
                     * Action：“AUTONAVI_STANDARD_BROADCAST_SEND”
                     * KEY_TYPE: 13028
                     * DIALOG_ID: 对话框ID，区分不同场景的对话框
                     * DIALOG_TITILE: 对话框标题
                     * DIALOG_MSG: 对话框消息内容
                     * DIALOG_LEFT: 对话框左侧按钮文本
                     * DIALOG_MIDDLE: 对话框中间按钮文本
                     * DIALOG_RIGHT: 对话框右侧按钮文本
                     *
                     * DIALOG_ID目前定义了如下对话框：
                     * 1. 路线规划失败，退出导航 1001
                     * 2. 在线路线规划失败，重试或者退出导航 1002
                     * 3. 离线路线规划失败，使用在线或者退出导航 1003
                     * 4. 沿途搜索无结果，是否周边搜 1004
                     * 5. 途径点超过三个，是否清除之前的途径点 1005
                     *
                     * 2.监听到该协议后，等待四秒在屏幕上展示弹窗。期间可以调用语音引擎播报语料“执行失败，将退出导航。”
                     *
                     * 3.调用如下广播协议推出导航：
                     * 作用:通知导航结束引导，退出导航状态，回到主图界面。
                     * 参数说明：
                     * Action："AUTONAVI_STANDARD_BROADCAST_RECV"
                     * KEY_TYPE:10010
                     */
                    int dialogId = intent.getIntExtra("DIALOG_ID", -1);
                    LogUtil.logd("receive:" + intent.getAction() + ", DIALOG_ID=" + dialogId);
                    if (dialogId == 1001 || dialogId == 1002 || dialogId == 1003 || dialogId == 1004 || dialogId == 1005) {
                        isGaodeAlongWayError = true;
                        AppLogicBase.runOnUiGround(new Runnable() {
                            @Override
                            public void run() {
                                closeNavGaode();
                                // 这里一定要用speakTextOnRecordWin方法。因为要求我们不展示poi界面，需要关闭搜索。搜索没有关闭的方法提供给我们，不过关闭窗口就可以清空搜索了。
                                TXZResourceManager.getInstance().speakTextOnRecordWin("执行失败，将退出导航。", true, new Runnable() {
                                    @Override
                                    public void run() {
                                        backToDesktop();
                                        isGaodeAlongWayError =false;
                                    }
                                });
                            }
                        });
                    }
                }
            }
        }, new IntentFilter(RECV_BROADCAST));

        setGaodeBgNavSpeak(false);
        setGaodeNavSpeak(false);
    }


    private void registerWakeupAsr() {
        TXZAsrManager.AsrComplexSelectCallback asrComplexSelectCallback = new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public String getTaskId() {
                return TASK_ID_GD_HINTS;
            }

            @Override
            public boolean needAsrState() {
                return true;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                if ("NICK".equals(type)) {
                    return;
                }
                TtsUtil.cancelSpeak(mLastTtsId);
                isUseHintShowing = false;
                releaseWakeupAsr();
                // 唤醒声控之后，是可以同时识别到command和免唤醒词的，且优先免唤醒词。所以如果声控默认是不关闭窗口的，这里不关闭窗口的话就会出现窗口没有关闭的情况。
                if (!RecordWinManager.getInstance().isRecordWinClosed()) {
                    TXZResourceManager.getInstance().speakTextOnRecordWin("",true,null);
                }
                Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent.putExtra("KEY_TYPE", 12203);
                switch (type) {
                    case "SURE":
                        intent.putExtra("EXTRA_OPTION", 2);
                        break;
                    case "CANCEL":
                        intent.putExtra("EXTRA_OPTION", 1);
                        break;
                }
                GlobalContext.get().sendBroadcast(intent);
            }

        };
        asrComplexSelectCallback.addCommand("SURE", "同意", "确定")
                .addCommand("CANCEL", "退出", "取消")
                .addCommand("NICK", AppLogic.NICK);
        TXZAsrManager.getInstance().useWakeupAsAsr(asrComplexSelectCallback);
        isAlreadySetWakeupAsr = true;
    }

    public void releaseWakeupAsr() {
        TtsUtil.cancelSpeak(mLastTtsId);
        TXZAsrManager.getInstance().recoverWakeupFromAsr(TASK_ID_GD_HINTS);
        isAlreadySetWakeupAsr = false;

    }

    public void resetWakeupAsr() {
        if (!isAlreadySetWakeupAsr && isUseHintShowing) { // onNavEnter会多次触发
            registerWakeupAsr();
            TtsUtil.cancelSpeak(mLastTtsId);
            mLastTtsId = TtsUtil.speakText("请认真阅读高德地图使用提示，确定请说“同意”放弃请说“退出”");
        }
    }

    public static int getGaodePId() {
        int pid = -1;
        ActivityManager manager = (ActivityManager) GlobalContext.get()
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager
                .getRunningAppProcesses()) {
            if (processInfo.processName != null && processInfo.processName.equals(PACKAGE_NAME_GAODE)) {
                pid = processInfo.pid;
                break;
            }
        }
        return pid;
    }

    public void notifyCancelHud() {
        LogUtil.logd("notifyCancelHud");
        Intent intent = new Intent(SEND_BROADCAST);
        intent.putExtra("KEY_TYPE", 12107);
        intent.putExtra("HUD_IS_OPEN", 1);
        GlobalContext.get().sendBroadcast(intent);
    }

    /**
     * 设置后台巡航播报开关
     *
     * @param enable 开启/关闭
     */
    public void setGaodeBgNavSpeak(boolean enable) {
        Intent intent = new Intent(SEND_BROADCAST);
        intent.putExtra("KEY_TYPE", 12006);
        intent.putExtra("EXTRA_SETTING_TYPE", 1); // 1：后台巡航播报
        intent.putExtra(" EXTRA_SETTING_RESULT", enable);
        GlobalContext.get().sendBroadcast(intent);
    }

    public void setGaodeNavSpeak(boolean enable) {
        Intent intent = new Intent(SEND_BROADCAST);
        intent.putExtra("KEY_TYPE", 10064);
        intent.putExtra("EXTRA_TYPE", 0);// 0:所有 1:路况播报；2:电子眼播报； 3:警示播报
        intent.putExtra("EXTRA_OPERA", 1);  // 0:打开 ；1:关闭
        GlobalContext.get().sendBroadcast(intent);
    }

    /**
     * 躲避拥堵弹窗控制
     * Action:"AUTONAVI_STANDARD_BROADCAST_RECV"
     * KEY_TYPE:12106
     * EXTRA_AVOID_TRAFFIC_JAM_CONTROL:true：避开；false：忽略
     * Intent intent = new Intent();
     * intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
     * intent.putExtra("KEY_TYPE", 12106);
     * 使用场景：用户手动点击更优路线（躲避拥堵）弹窗的控制（避开、忽略），对外发送通知告知第三方，
     * 第三方以此退出语音流程
     * 说明：auto给第三方发送用户手动对更优路线（躲避拥堵）弹窗的控制的状态通知
     * 版本信息：车镜2.1以上适配渠道版本支持
     */
    public void ctrlAvoidTrafficJam(boolean bAvoid) {
        Intent intent = new Intent(SEND_BROADCAST);
        intent.putExtra("KEY_TYPE", 12106);
        intent.putExtra("EXTRA_AVOID_TRAFFIC_JAM_CONTROL", bAvoid);
        GlobalContext.get().sendBroadcast(intent);
    }

    public boolean isUseHintShowing() {
        return isUseHintShowing;
    }

    public boolean isGaodeAlongWayError() {
        return isGaodeAlongWayError;
    }

    /**
     * 同步一下高德地图的声音图标。
     * 高德地图的声音图标在静音的时候不会变化，高德不改，让我们在静音的时候通知他们改变图标。
     *
     * 抄的高德说明：
     * 设置地图是否播报
     * 说明：Auto启动后，第三方发送设置地图是否播报即是否静音（永久静音/临时静音），auto响应。
     * 版本信息：auto1.4.2以上适配渠道版本支持。
     * 参数说明：
     * Action："AUTONAVI_STANDARD_BROADCAST_RECV"
     * KEY_TYPE:10047
     * EXTRA_MUTE:（可选）是否永久静音(int)mute
     * EXTRA_CASUAL_MUTE:（可选）是否临时静音(int)casualmute
     * mute 永久静音：静音后，需要取消静音或者auto内部设置操作取消静音才能恢复。0:取消静音；1：静音，未传默认0；
     * casualmute 临时静音：设置的单次有效，如退出地图后再重启，地图声音就会恢复。0:取消临时静音；1:临时禁音， 未传默认0
     * 注：临时静音与永久静音需单独分开发送
     *
     * @param mute 是否要静音
     */
    public void syncGDMuteIcon(boolean mute) {
        Intent intent = new Intent(SEND_BROADCAST);
        intent.putExtra("KEY_TYPE", 10047);
        intent.putExtra("EXTRA_MUTE", mute ? 1 : 0);
        IntentUtils.sendBroadcast(intent);
    }

    /**
     * 关闭导航状态
     * note：这个没有退出导航，我们需要自己返回桌面
     */
    private void closeNavGaode(){
        // 关闭导航状态
        Intent closeIntent = new Intent();
        closeIntent.setAction(SEND_BROADCAST);
        closeIntent.putExtra("KEY_TYPE", 10010);
        GlobalContext.get().sendBroadcast(closeIntent);
    }

    private void backToDesktop(){
        // 返回桌面
        if (LaunchManager.getInstance().isLaunchResume()) {
            LaunchManager.getInstance().launchDesktop();
        } else {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                GlobalContext.get().startActivity(homeIntent);
            } catch (Exception e) {
                LogUtil.loge("返回桌面错误！");
            }
        }
    }
}
