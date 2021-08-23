package com.txznet.launcher.domain.fm;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.domain.settings.FmConst;
import com.txznet.launcher.domain.settings.SettingsManager;
import com.txznet.launcher.domain.tts.TtsManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.txz.util.runnables.Runnable1;

/**
 * Created by daviddai on 2018/11/20
 * 控制fm相关操作的类
 * 其实fm操作是很少的，都不需要这个manager。但是因为fm操作有bug，改动后很复杂，所以还是建了个manager
 */
public class FmManager extends BaseManager {

    /////////////////////////////singleton/////////////////////////////
    private static FmManager instance = null;
    private FmManager() {
    }

    public static FmManager getInstance() {
        synchronized (FmManager.class) {
            if (instance == null) {
                instance = new FmManager();
            }
        }

        return instance;
    }
    /////////////////////////////singleton/////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // status
    private int mStatus = FM_IS_CLOSE;
    private static final int FM_IS_OPEN = 0b1;// fixme: 2018/11/20 开和关的状态本来就是互斥的，是不是可以用一个标志位来表示
    private static final int FM_IS_CLOSE = 0b10;
    private static final int FM_IS_TOGGLE_SWITCH = 0b100;
    ///////////////////////////////////////////////////////////////////////////

    private static final String TAG = "FmManager:";

    private float mCurFMFreq = 100;

    private Runnable1<Boolean> closeRecordWinRunnable;
    private Runnable timeoutRunnable;

    @Override
    public void init() {
        super.init();
        // 默认这个方法是什么都不做的。
        closeRecordWinRunnable = new Runnable1<Boolean>(false) {
            @Override
            public void run() {
                boolean canRun = mP1 && TXZRecordWinManager.getInstance().isOpened();
                LogUtil.d(TAG, "can close record win");
                if (canRun) {
                    TXZResourceManager.getInstance().dissmissRecordWin();
                    mP1 = false;
                }
            }
        };
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                TXZResourceManager.getInstance().dissmissRecordWin();
            }
        };
    }

    /**
     * 开关fm的处理方法
     * 通过{@link #complexTts(boolean, float, String)}播报tts并调整fm
     *
     * @param power 是否打开fm
     * @param tts   反馈语
     */
    public void ctrlFM(boolean power, String tts) {
        complexTts(power, getCurFMFreq(), tts);
    }

    /**
     * 设置，调整fm的频率。
     * <p>
     * 一定有以下这些操作。
     * 1. 先判断频率是否在有效范围内
     * 2. 部分频率会影响gps，这部分频率会被修改提示语。
     * 3. 通过{@link #complexTts(boolean, float, String)}播报tts并调整fm
     * <p>
     * note：该方法没有处理多次调用的情况。应该没有这种情况吧。
     *
     * @param hz 要设置fm的频率，单位是hz
     */
    public void toFmFreq(float hz) {
        try {
            // 判断是否有效范围，无效直接中断操作
            if (hz > FmConst.max || hz < FmConst.min) {
                String spk = "抱歉，有效调频范围是" + FmConst.min + "至" + FmConst.max;
                TXZResourceManager.getInstance().speakTextOnRecordWin(spk, true, null);
                return;
            }
            // 上报数据
            ReportUtil.doReport(new ReportUtil.Report.Builder().setType("fm").setAction("fm")
                    .setSessionId().buildCommReport());

            // 根据是否是不好的频率来选择tts内容
            String notice;
            if (FmConst.BAD_FREQUENCY_SET.contains(hz)) {
                notice = FmConst.BAD_FREQUENCY_NOTICE;
            } else {
                notice = FmConst.NORMAL_NOTICE;
            }
            notice = notice.replace("%CHANNEL%", hz + "赫兹");
            complexTts(true, hz, notice);
        } catch (Exception e) {
            LogUtil.loge("toFmFreq error");
            TXZResourceManager.getInstance().speakTextOnRecordWin("暂不支持此频点", true, null);
        }
    }

    /**
     * 由于音乐播放中的时候，打开fm会出现音乐先播放一下子然后fm才生效的情况。
     * 目前通过先不关闭recordWin的方法来解决。
     * <p>
     * 逻辑如下：
     * 1. 播报完tts后不关闭窗口，但也不继续识别
     * 2. 在fm设置生效后，调用关闭窗口的方法。这个方法是否会执行，取决于中途是否有窗口状态变化、是否有唤醒、tts是否success
     *
     * @param open   是否打开fm
     * @param hz     fm频率
     * @param notice 反馈语
     */
    private void complexTts(final boolean open, final float hz, String notice) {
        // 保存状态
        setToggleSwitch(open);

        // 取消超时任务
        AppLogicBase.removeBackGroundCallback(timeoutRunnable);

        // 开始播报tts
        final boolean close = !isToggleSwitch();
//        TtsUtil.speakTextOnRecordWin(null,notice, null,close, false,false,new Runnable() {
//            @Override
//            public void run() {
//                // 这里才实际开始fm的设置。
//                SettingsManager.getInstance().ctrlFM(open, hz);
//                LogUtil.d(TAG, "update close runnable's state to true after finish tts");
//                closeRecordWinRunnable.update(true);
//                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_FM_START_NOTIFY_ADJUST);
//                /*
//                 * 一段时间之后如果系统没有返回fm状态,关闭窗口.避免由于系统的bug导致我们窗口没有关闭.
//                 */
//                if (close) {
//                    AppLogicBase.runOnBackGround(timeoutRunnable,1000);
//                }
//            }
//        });
        TXZResourceManager.getInstance().cancelCloseRecordWin();
        TtsManager.TtsBuilder.create()
                .setText(notice)
                .setClose(close)/*只有当切换fm开关的时候才会有漏音的问题，调频不会。所以这里只处理切换fm开关的情况*/
                .setIsRemoteCommand(true)
                .setTtsCallback(new TtsUtil.ITtsCallback() {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        // 这里才实际开始fm的设置。
                        SettingsManager.getInstance().ctrlFM(open, hz);
                        LogUtil.d(TAG, "update close runnable's state to true after finish tts");
                        closeRecordWinRunnable.update(true);
                        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_FM_START_NOTIFY_ADJUST);
                        /*
                         * 一段时间之后如果系统没有返回fm状态,关闭窗口.避免由于系统的bug导致我们窗口没有关闭.
                         */
                        if (close) {
                            AppLogicBase.runOnBackGround(timeoutRunnable,1000);
                        }
                    }

                    @Override
                    public void onCancel() {
                        super.onCancel();
                        // 这里才实际开始fm的设置。
                        SettingsManager.getInstance().ctrlFM(open, hz);
                        /*
                         * 由于取消tts的onCancel是异步的，所以我们没法知道对方已经做了什么。
                         * 又由于我们不支持打断，所以识别到的一定是免唤醒词和昵称。这些是一定要自己去处理窗口的。
                         * 所以这里不管窗口的状态了。
                         */
                        LogUtil.d(TAG, "update close runnable's state to false after cancel tts");
                        closeRecordWinRunnable.update(false);
                        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_FM_START_NOTIFY_ADJUST);
                    }

                    @Override
                    public void onError(int iError) {
                        super.onError(iError);
                        // 这里才实际开始fm的设置。
                        SettingsManager.getInstance().ctrlFM(open, hz);

                        // 出现莫名其妙的异常后关闭窗口,避免窗口没有人去关闭.
                        if (TXZRecordWinManager.getInstance().isOpened()) {
                            TXZResourceManager.getInstance().dissmissRecordWin();
                        }
                        closeRecordWinRunnable.update(false);
                        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_FM_START_NOTIFY_ADJUST);
                    }
                })
                .speak();
    }

    public void setCurFMFreq(float curFMFreq) {
        mCurFMFreq = curFMFreq;
    }

    public float getCurFMFreq() {
        return mCurFMFreq;
    }

    public void setFmIsOpen(boolean isOpen) {
        // 当设置了开关，即表示切换开关已经结束。
        mStatus &= ~FM_IS_TOGGLE_SWITCH;
        LogUtil.logd("setToggleSwitch: false");

        // 保存状态
        if (isOpen) {
            mStatus |= FM_IS_OPEN;
            mStatus &= ~FM_IS_CLOSE;
        } else {
            mStatus |= FM_IS_CLOSE;
            mStatus &= ~FM_IS_OPEN;
        }
    }

    /**
     * fm是否已经打开了
     *
     * @return true fm已经打开
     */
    public boolean isFmOpen() {
        return (mStatus & FM_IS_OPEN) == FM_IS_OPEN;
    }

    /**
     * 是否在切换状态中。
     *
     * @return true 切换过程中
     */
    public boolean isToggleSwitch() {
        return (mStatus & FM_IS_TOGGLE_SWITCH) == FM_IS_TOGGLE_SWITCH;
    }

    /**
     * 根据要切换的状态来保存是否切换了fm的开关
     *
     * @param needOpen 要切换成的状态
     */
    public void setToggleSwitch(boolean needOpen) {
        if (needOpen != isFmOpen()) {
            LogUtil.logd("setToggleSwitch: true");
            mStatus |= FM_IS_TOGGLE_SWITCH;
        }
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{EventTypes.EVENT_VOICE_OPEN,
                EventTypes.EVENT_VOICE_DISMISS,
                EventTypes.EVENT_WAKE_UP,
                EventTypes.EVENT_FM_RECEIVE_STATE,};
    }

    @Override
    protected void onEvent(String eventType) {
        super.onEvent(eventType);
        switch (eventType) {
            case EventTypes.EVENT_VOICE_OPEN:
            case EventTypes.EVENT_VOICE_DISMISS:
            case EventTypes.EVENT_WAKE_UP:
                // 当窗口状态发生变化或者识别到唤醒词了，阻止runnable的执行。
                if (isToggleSwitch()) {
                    LogUtil.d(TAG, "update close runnable's state to false because win state or wakeup");
                    closeRecordWinRunnable.update(false);
                    AppLogicBase.removeBackGroundCallback(timeoutRunnable);
                }
                break;
            case EventTypes.EVENT_FM_RECEIVE_STATE:
                // 当fm状态变化的时候就执行runnable。由于runnable内部还有一个标志位，所以这里就直接执行runnable了。
                closeRecordWinRunnable.run();
                AppLogicBase.removeBackGroundCallback(timeoutRunnable);
                break;
        }
    }
}
