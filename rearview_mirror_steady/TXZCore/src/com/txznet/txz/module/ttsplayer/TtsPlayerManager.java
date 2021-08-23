package com.txznet.txz.module.ttsplayer;

import android.media.AudioManager;
import android.os.SystemClock;
import android.text.TextUtils;

import com.spreada.utils.chinese.ZHConverter;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZTtsPlayerManager;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.component.ttsplayer.proxy.TtsPlayerProxy;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.bt.BluetoothManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.music.focus.MusicFocusManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.TXZStatisticser;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * 支持播放暂停的tts播放管理类
 * 注意tts播放的内容可能超长
 */
public class TtsPlayerManager extends IModule {
    private static final String MONITOR_TTS_INIT_ERROR = "ttsplayer.init.E.";
    private static final String TAG = "TtsPlayerManager :: ";
    static TtsPlayerManager sTtsPlayerManager = new TtsPlayerManager();
    private ITts mTts = null;
    public static final int DEFAULT_STREAM_TYPE = -1;
    static final TtsUtil.PreemptType DEFAULT_PREEMPT_FLAG = TtsUtil.PreemptType.PREEMPT_TYPE_NONE;
    static final TXZTtsPlayerManager.ITtsCallback DEFAULT_TTS_CALLBACK = null;
    public static final int INVALID_TTS_TASK_ID = 0;
    long ttsDelay = 0;//tts播报延时，只有需要抢焦点时才会进行延时
    private String ttsText;

    static final long DEFAULT_TIMEOUT = 10 * 60 * 1000;// 丢弃TTS的超时时间

    //默认不启用tts暂停播放的功能
    private static boolean enableTtsPlayer = false;

    public static void setEnableTtsPlayer(final boolean enableTtsPlayer) {
        TtsPlayerManager.enableTtsPlayer = enableTtsPlayer;
    }

    public static boolean isEnableTtsPlayer() {
        return enableTtsPlayer;
    }

    private TtsPlayerManager() {
        mInited = false;
        mInitSuccessed = false;
    }

    public static TtsPlayerManager getInstance() {
        return sTtsPlayerManager;
    }

    public void initializeComponent() {
        if (!isEnableTtsPlayer()) {
            JNIHelper.logw(TAG + "TTS player is disable !");
            return;
        }
        if (mTts != null) {
            JNIHelper.logw(TAG + "TTS has been instantiated");
            return;
        }
        // 启动初始化默认的TTS
        mTts = createTtsEngine();
        ITts.IInitCallback callback = new ITts.IInitCallback() {
            @Override
            public void onInit(boolean bSuccess) {
                JNIHelper.logd(TAG + "init tts: " + bSuccess);
                mInited = true;
                mInitSuccessed = bSuccess;
                speakNext();
                if (mInitSuccessed) {
                } else {
                    MonitorUtil.monitorCumulant(MONITOR_TTS_INIT_ERROR + "all");

                }
            }
        };
        mTts.initialize(callback);
    }

    private ITts createTtsEngine() {
        ITts mTts = TtsPlayerProxy.getInstance();
        return mTts;
    }

    int mNextTaskId = 1; // 下一次分配给Tts的任务ID
    List<TtsTask> mTtsTaskQueue = new ArrayList<TtsTask>(); // TTS的等待任务列表
    TtsTask mCurTask = null; // 当前的Tts任务

    Runnable mRunnableSpeakNext = new Runnable() {
        @Override
        public void run() {
            JNIHelper.logd(TAG + "really speak next: queue=" + mTtsTaskQueue.size());
            if (mCurTask != null) {
                JNIHelper.loge(TAG + "speakNext error: current task is not end");
                return;
            }

            if (mTtsTaskQueue.isEmpty()) {
                mCurTask = null;
                //TODO MusicManager.getInstance().onEndTts();
                return;
            }

            if (canSpeakNow()) {
                mCurTask = mTtsTaskQueue.get(0);
                mTtsTaskQueue.remove(0);
                speakText(mCurTask);
            }
        }
    };


    void speakNext() {
        JNIHelper.logd(TAG + "speakNext: queue=" + mTtsTaskQueue.size());
        AppLogic.removeBackGroundCallback(mRunnableSpeakNext);
        AppLogic.runOnBackGround(mRunnableSpeakNext, 0);
    }


    boolean canSpeakNow() {
        if (getTtsTool() == null // 组件未构造
                || isInitSuccessed() == false // 初始化未完成
                || mCurTask != null // 当前有TTS任务
                || AsrManager.getInstance().canSpeakTts() // 正在录音中
                || RecordManager.getInstance().isBusy()
                || (!CallManager.getInstance().isIdle() && !CallManager.getInstance().isRinging())
                // 电话忙，并且不是来电响铃
                || TXZPowerControl.isEnterReverse() //在倒车影像状态中
        ) {
            return false;
        }
        return true;
    }

    private ITts getTtsTool() {
        return mTts;
    }


    public int speakText(int iStream, String sText, TtsUtil.PreemptType bPreempt) {
        return speakText(iStream, sText, bPreempt, DEFAULT_TTS_CALLBACK);
    }

    public int speakText(int iStream, String sText, TXZTtsPlayerManager.ITtsCallback oRun) {
        return speakText(iStream, sText, DEFAULT_PREEMPT_FLAG, oRun);
    }

    public int speakText(int iStream, String sText) {
        return speakText(iStream, sText, DEFAULT_PREEMPT_FLAG, DEFAULT_TTS_CALLBACK);
    }

    public int speakText(String sText, TtsUtil.PreemptType bPreempt,
            TXZTtsPlayerManager.ITtsCallback oRun) {
        return speakText(DEFAULT_STREAM_TYPE, sText, bPreempt, oRun);
    }

    public int speakText(String sText, TtsUtil.PreemptType bPreempt) {
        return speakText(DEFAULT_STREAM_TYPE, sText, bPreempt, DEFAULT_TTS_CALLBACK);
    }

    public int speakText(String sText, TXZTtsPlayerManager.ITtsCallback oRun) {
        return speakText(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG, oRun);
    }

    public int speakText(String sText) {
        return speakText(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG, DEFAULT_TTS_CALLBACK);
    }

    public int speakText(int iStream, String sText, TtsUtil.PreemptType bPreempt,
            TXZTtsPlayerManager.ITtsCallback oRun) {
        return speak(iStream, sText, bPreempt, oRun, false);
    }

    public int speak(int iStream, String sText, TtsUtil.PreemptType bPreempt,
            TXZTtsPlayerManager.ITtsCallback oRun, boolean forceStopWakeup) {
        return speak(iStream, sText, bPreempt, oRun, false, forceStopWakeup, ttsDelay);
    }

    public int speak(int iStream, String sText, TtsUtil.PreemptType bPreempt,
            TXZTtsPlayerManager.ITtsCallback oRun, boolean fromRemote, boolean forceStopWakeup,
            long delay) {
        JNIHelper.logd(TAG + "speakText: stream=" + iStream + " ,text=" + sText
                + ",delay=" + delay + ",bPreempt=" + bPreempt
                + ",fromRemote=" + fromRemote + ",forceStopWakeup=" + forceStopWakeup);
        // 创建新的任务
        TtsTask t = new TtsTask();
        t.iStream = iStream;
        t.sText = sText;
        t.oRun = oRun;
        t.fromRemote = fromRemote;
        t.forceStopWakeup = forceStopWakeup;
        t.delay = delay;
        synchronized (mSpeakEndCallback) {
            t.iTaskId = mNextTaskId++;
            if (mNextTaskId <= 0) {
                mNextTaskId = 1;
            }
        }
        if (oRun != null) {
            oRun.setTaskId(t.iTaskId);
        }
        insertSpeakTask(bPreempt, t);
        return t.iTaskId;
    }


    int speakText(TtsTask t) {
        if (t == null) {
            speakNext();
            return ERROR_SUCCESS;
        }
        if (t.oRun != null) {
            t.oRun.onBegin();
        }
        if (TextUtils.isEmpty(ImplCfg.getTtsImplClass())) {
            if (t.oRun != null) {
                AppLogic.runOnBackGround(new Runnable1<TtsTask>(t) {
                    @Override
                    public void run() {
                        JNIHelper.logd(TAG + "speakText end onError: id=" + mP1.iTaskId);
                        mP1.oRun.onError(ERROR_ABORT);
                        mP1.oRun.onEnd();
                    }
                }, 0);
            }
            return ERROR_SUCCESS;
        }

        mCurTask = t;
        if (mCurTask.iStream == DEFAULT_STREAM_TYPE) {
            if (BluetoothManager.getInstance().isScoStateOn()) {
                mCurTask.iStream = AudioManager.STREAM_VOICE_CALL;
            } else {
                mCurTask.iStream = TXZTtsPlayerManager.DEFAULT_TTS_STREAM; // 默认使用通知的通道
            }
        }
        // 校验通道的音量，太小给出震动提示
        VolumeManager.getInstance().checkVolume(mCurTask.iStream, true, true);

        if (SystemClock.elapsedRealtime() - t.createdTime > DEFAULT_TIMEOUT) {
            JNIHelper.loge(TAG + "speakText createTime:" + t.createdTime +
                    ",drop overtimed tts task!");
            cancelCurTask();
            return ERROR_SUCCESS;
        }

        JNIHelper
                .logd(TAG + "speakText begin: id=" + t.iTaskId + ",stream=" + t.iStream + ",text=" +
                        t.sText + ",sco="
                        + BluetoothManager.getInstance().isScoStateOn());
        // 上报数据
        ReportUtil.doReport(new ReportUtil.Report.Builder().setType("ttsplayer").setAction("begin")
                .setSessionId().buildCommReport());

        // 判断条件提权，先播报文本是否为空
        if (TextUtils.isEmpty(mCurTask.sText)) {
            speakNextVoice();
            return ERROR_SUCCESS;
        }
        if (!TextUtils.isEmpty(t.sText)) {
            // 不去过滤多余空格，只有去搜索录音文件时才会过滤
            t.sText = ZHConverter.convert(t.sText, ZHConverter.SIMPLIFIED);
        }

        int filterIndex = -1;

        if (mCurTask == null || TextUtils.isEmpty(mCurTask.sText)) {
            // 文本为空时强制跳过文本播报
            filterIndex = 0;
        }
        ttsText = mCurTask.sText;

        // 统计TTS说法 andyzhao 2016-06-01
        TXZStatisticser.append(mCurTask.sText);

        long mDelay = 0;
        if (!MusicFocusManager.getInstance().hasAudioFocus()) {//需要抢焦点
            mDelay = mCurTask.delay;
        }
        // 将TTS开始播报监听提前开始执行
        // MusicManager.getInstance().onBeginTts(mCurTask.iStream, mCurTask);


        if (filterIndex == 0 && mCurTask.sText != null) {
            // 等于0时跳过 TTS 文本播报
            mCurTask.textOffset = mCurTask.sText.length();
        }

        AppLogic.removeBackGroundCallback(mRunnableSpeakTextDelay);
        if (mDelay > 0) {
            AppLogic.runOnBackGround(mRunnableSpeakTextDelay, mDelay);
        } else {
            mRunnableSpeakTextDelay.run();
        }
        return ERROR_SUCCESS;
    }

    private Runnable mRunnableSpeakTextDelay = new Runnable() {
        public void run() {
            speakNextVoice();
            if (mCurTask != null) {
                //TTS打断使用
                InterruptTts.getInstance()
                        .startAsr(mCurTask.oRun, mCurTask.sText, mCurTask.iTaskId);
            }
        }
    };

    void speakNextVoice() {
        AppLogic.removeBackGroundCallback(mRunnableSpeakNextVoice);
        AppLogic.runOnBackGround(mRunnableSpeakNextVoice, 0);
    }

    Runnable mRunnableSpeakNextVoice = new Runnable() {
        @Override
        public void run() {
            if (mCurTask == null || (mCurTask.sText == null || mCurTask.textOffset >= mCurTask.sText
                    .length()) ) {
                onSuccessCallback();
                return;
            }

            if (speakTextWithSplit(mCurTask)) {
                return;
            }
        }
    };

    private static final int TEXT_SPLIT_LEN = 200;

    private boolean speakTextWithSplit(TtsTask task) {
        if (task == null || task.sText == null) {
            return false;
        }
        int len = task.sText.length();
        int offset = task.textOffset;
        if (len <= offset) {
            return false;
        }
        int end = offset + TEXT_SPLIT_LEN;
        if (len <= end) {
            end = len;
        } else {
            int last = task.sText.lastIndexOf('。', end);
            if (offset < last) {
                end = last + 1;
            }
        }
        getTtsTool().start(task.iStream, replaceSpeakText(task.sText.substring(offset, end)),
                mSpeakEndCallback);
        task.textOffset = end;
        return true;
    }

    private String[] mOriginals;
    private String[] mReplaces;

    /**
     * 替换TTS播报文本
     */
    private String replaceSpeakText(String text) {
        //.replace("同行者", "同形者") // 云知声已经处理改多音字
        String result = text.replace("星期一", "星期1").replace("空调", "空条").replace("调到", "条到")
                .replace("冇", "卯");
        result = result.replace("4S", "四S").replace("4s", "四S").replace("12月", "十二月")
                .replace("2月", "二月").replace("月2号", "月二号");
        if (null == mOriginals) {
            return result;
        }
        for (int i = 0; i < mOriginals.length; i++) {
            // 替换的原始文本不允许为空，替换文本允许是空串
            if (TextUtils.isEmpty(mOriginals[i]) || null == mReplaces[i]) {
                continue;
            }
            result = result.replace(mOriginals[i], mReplaces[i]);
        }
        return result;
    }

    public void setReplaceSpeakWord(String replaceJson) {
        JNIHelper.logd(TAG + "tts: replace word: " + replaceJson);
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(replaceJson);
        } catch (JSONException e) {
            e.printStackTrace();
            JNIHelper.loge(TAG + "tts: replace word: " + e.getMessage());
            return;
        }
        if (null == jsonArray || jsonArray.length() == 0) {
            return;
        }
        mOriginals = new String[jsonArray.length()];
        mReplaces = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            String original = null;
            String replace = null;
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                original = object.getString("original");
                replace = object.getString("replace");
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            // 替换的原始文本不允许为空，替换文本允许是空串
            if (TextUtils.isEmpty(original) || null == replace) {
                continue;
            }
            mOriginals[i] = original;
            mReplaces[i] = replace;
        }
    }

    /**
     * TTS onError 回调处理逻辑, 需要保证在 TTS 线程调用
     */
    private void onErrorCallback(int iError) {
        if (mCurTask != null) {
            JNIHelper.loge(TAG + "speakText end: id=" + mCurTask.iTaskId + ",stream=" +
                    mCurTask.iStream +
                    ",text="
                    + mCurTask.sText + ",error=" + iError);
        }
        // 切换线程防止阻塞当前播报
        AppLogic.runOnBackGround(new Runnable2<TtsTask, Integer>(mCurTask, iError) {
            @Override
            public void run() {
                if (mP1 != null && mP1.oRun != null) {
                    JNIHelper.logd(TAG + "speakText end onError: id=" + mP1.iTaskId);
                    mP1.oRun.onError(mP2);
                    mP1.oRun.onEnd();
                    mP1.oRun = null; // 防止二次回调异常
                }
            }
        }, 0);
        mCurTask = null;
        speakNext();
    }

    /**
     * TTS onCancel 回调处理逻辑, 需要保证在 TTS 线程调用
     */
    private void onCancelCallback() {
        do {
            if (mCurTask != null) {
                JNIHelper.logd(TAG + "speakText end: id=" + mCurTask.iTaskId + ",stream=" +
                        mCurTask.iStream + ",text="
                        + mCurTask.sText);
            } else {
                break;
            }

            if (cancelTaskId != mCurTask.iTaskId) {
                JNIHelper.loge(TAG + "speakText : mCurTask.iTaskId=" + mCurTask.iTaskId + ",id=" +
                        cancelTaskId);
                break; // 防止多次cancel TTS时一个任务多个回调
            }

            // 上报数据
            ReportUtil
                    .doReport(new ReportUtil.Report.Builder().setType("ttsplayer").setAction("end")
                            .setSessionId().buildCommReport());

            AppLogic.runOnBackGround(new Runnable1<TtsTask>(mCurTask) {
                @Override
                public void run() {
                    if (mP1 != null && mP1.oRun != null) {
                        // callback不为空时，isRealCancel值才有价值
                        if (mP1.isRealCancel) {
                            JNIHelper.logd(TAG + "speakText end onCancel: id=" + mP1.iTaskId);
                            mP1.oRun.onCancel();
                            mP1.oRun.onEnd();
                            mP1.oRun = null; // 防止二次回调异常
                        } else {
                            // 下次取消还是要回调的
                            mP1.isRealCancel = true;
                        }
                    }
                }
            }, 0);
            mCurTask = null;
        } while (false);
        speakNext();
    }

    /**
     * TTS onSuccess 回调处理逻辑, 需要保证在 TTS 线程调用
     */
    private void onSuccessCallback() {
        if (mCurTask != null) {
            JNIHelper.logd(TAG + "speakText end: id=" + mCurTask.iTaskId + ",stream=" +
                    mCurTask.iStream +
                    ",text="
                    + mCurTask.sText);
        }
        // 上报数据
        ReportUtil.doReport(new ReportUtil.Report.Builder().setType("tts").setAction("end")
                .setSessionId().buildCommReport());

        AppLogic.runOnBackGround(new Runnable1<TtsTask>(mCurTask) {
            @Override
            public void run() {
                if (mP1 != null && mP1.oRun != null) {
                    JNIHelper.logd(TAG + "speakText end onSuccess: id=" + mP1.iTaskId);
                    mP1.oRun.onSuccess();
                    mP1.oRun.onEnd();
                    mP1.oRun = null; // 防止二次回调异常
                }
            }
        }, 0);
        mCurTask = null;
        speakNext();
    }

    private int cancelTaskId; // 当前取消任务ID

    /**
     * TTS 播报回调，需要在回调中切换到 TTS 线程中
     */
    TXZTtsPlayerManager.ITtsCallback mSpeakEndCallback = new TXZTtsPlayerManager.ITtsCallback() {
        @Override
        public void onError(int iError) {
            JNIHelper.loge(TAG + "speakText : onError");
            AppLogic.runOnBackGround(new Runnable1<Integer>(iError) {
                @Override
                public void run() {
                    onErrorCallback(mP1);
                }
            }, 0);
        }

        @Override
        public void onCancel() {
            JNIHelper.logd(TAG + "speakText : onCancel");
            AppLogic.runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    onCancelCallback();
                }
            }, 0);
        }

        @Override
        public void onSuccess() {
            JNIHelper.logd(TAG + "speakText : next text");
            speakNextVoice();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    };


    public class TtsTask {
        int iTaskId = INVALID_TTS_TASK_ID;
        int iStream = DEFAULT_STREAM_TYPE;
        String sText = "";
        /**
         * tts播报偏移量
         */
        int textOffset = 0;
        int iVoiceIndex = 0;
        TXZTtsPlayerManager.ITtsCallback oRun;
        long createdTime = 0;
        boolean fromRemote = false; // 标识任务是否来自第三方
        boolean forceStopWakeup = false; // 播放时即使开启了回音消除也不允许打断
        long delay = ttsDelay;

        /**
         * 取消语音播报时，是否回调callback事件，
         * true 表示调用callback，默认值；
         * false 表示不调用回调，可以插入下次播报
         */
        boolean isRealCancel = true;

        public void enableForceStopWakeup(boolean froceStopWakeUp) {
            this.forceStopWakeup = froceStopWakeUp;
        }

        public boolean isForceStopWakeup() {
            return forceStopWakeup;
        }
    }


    public void insertSpeakTask(TtsUtil.PreemptType bPreempt, TtsTask t) {
        t.createdTime = SystemClock.elapsedRealtime();
        AppLogic.runOnBackGround(new Runnable2<TtsUtil.PreemptType, TtsTask>(bPreempt, t) {
            @Override
            public void run() {
                TtsUtil.PreemptType bPreempt = mP1;
                TtsTask t = mP2;
                JNIHelper.logd(TAG + "really begin play tts: " + t.iTaskId);
                if (bPreempt == TtsUtil.PreemptType.PREEMPT_TYPE_FLUSH) {
                    clearSpeak(t);
                    return;
                }
                // 添加到队列
                if (!canSpeakNow()) {
                    JNIHelper.logd(TAG + "push in tts queue: bPreempt=" + bPreempt);
                    if (bPreempt != TtsUtil.PreemptType.PREEMPT_TYPE_NONE) {
                        mTtsTaskQueue.add(0, t);
                    } else {
                        mTtsTaskQueue.add(t);
                    }

                    if (bPreempt == TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY) {
                        if (mCurTask == null) {
                            speakNext();
                        } else {
                            cancelCurTask();
                        }
                    }
                    if (bPreempt == TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE) {
                        if (mCurTask == null) {
                            speakNext();
                        } else {
                            mCurTask.isRealCancel = false;
                            final TtsTask mOldCurTask = mCurTask;
                            cancelCurTask();
                            AppLogic.runOnBackGround(new Runnable() {

                                @Override
                                public void run() {
                                    insertSpeakTask(TtsUtil.PreemptType.PREEMPT_TYPE_NEXT,
                                            mOldCurTask);
                                }
                            }, 200);
                        }
                    }
                    return;
                }
                // 直接合成
                if (ITts.ERROR_SUCCESS != speakText(t)) {
                    if (t.oRun != null) {
                        JNIHelper.logd(TAG + "speakText end onError: id=" + t.iTaskId);
                        t.oRun.onError(ITts.ERROR_UNKNOW);
                        t.oRun.onEnd();
                    }
                }
            }
        }, 0);
    }

    // /////////////////////////////////////////////////////////////////

    void cancelCurTask() {
        // 不用处理当前mCurTask，stop后会回调speakEnd，里面会进行处理
        if (null != mCurTask) {
            // 防止多次cancel TTS时一个任务多个回调
            if (cancelTaskId == mCurTask.iTaskId) {
                JNIHelper.logw(TAG + "cancelCurTask : mCurTask.iTaskId=" + mCurTask.iTaskId);
                return;
            }
            cancelTaskId = mCurTask.iTaskId; //TODO 保存当前cancel task id，存在风险问题，不再TTS线程
            if (getTtsTool() != null && getTtsTool().isBusy()) {
                getTtsTool().stop();
            } else {
                // 移除tts延时播报任务
                AppLogic.removeBackGroundCallback(mRunnableSpeakTextDelay);
                mSpeakEndCallback.onCancel();
            }
        }
    }

    public void errorCurTask() {
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                if (null != mCurTask) {
                    JNIHelper.logd(TAG + "speakText end errorCurTask : id=" + mCurTask.iTaskId);
                    final TXZTtsPlayerManager.ITtsCallback cb = mCurTask.oRun;
                    if (cb != null) {
                        mCurTask.oRun = new TXZTtsPlayerManager.ITtsCallback() {
                            @Override
                            public void onCancel() {
                                cb.onError(ERROR_ABORT);
                            }

                            @Override
                            public void onEnd() {
                                cb.onEnd();
                            }

                            @Override
                            public void onSuccess() {
                                cb.onSuccess();
                            }

                            @Override
                            public void onError(int iError) {
                                cb.onError(iError);
                            }
                        };
                    }
                    cancelCurTask();
                }
            }
        }, 0);
    }

    public void cancelSpeak(int iTaskId) {
        if (iTaskId == INVALID_TTS_TASK_ID) {
            return;
        }
        TtsTask t = mCurTask;
        if (t == null) {
            t = new TtsTask();
        }
        JNIHelper.recordLogStack(1);
        JNIHelper.logd(TAG + "cancelSpeak[" + iTaskId + "]: curTask=" + t.iTaskId + ",text=" +
                t.sText);
        AppLogic.runOnBackGround(new Runnable1<Integer>(iTaskId) {
            @Override
            public void run() {
                int iTaskId = mP1;
                JNIHelper.logd(TAG + "really begin cancel tts: " + iTaskId);
                if (mCurTask != null && mCurTask.iTaskId == iTaskId) {
                    cancelCurTask();
                    return;
                }
                for (int i = 0; i < mTtsTaskQueue.size(); ++i) {
                    if (mTtsTaskQueue.get(i).iTaskId == iTaskId) {
                        if (mTtsTaskQueue.get(i).oRun != null) {
                            AppLogic.runOnBackGround(new Runnable1<TtsTask>(mTtsTaskQueue.get(i)) {
                                @Override
                                public void run() {
                                    JNIHelper.logd(TAG + "speakText end onCancel: id=" +
                                            mP1.iTaskId);
                                    mP1.oRun.onCancel();
                                    mP1.oRun.onEnd();
                                }
                            }, 0);
                        }
                        mTtsTaskQueue.remove(i);
                        return;
                    }
                }
            }
        }, 0);
    }


    /**
     * 暂停TTS，暂时直接停掉当前任务
     */
    public void pauseSpeak(int taskId) {
        if (taskId == INVALID_TTS_TASK_ID) {
            return;
        }
        JNIHelper.logd(TAG + "pause: " + taskId);
        AppLogic.runOnBackGround(new Runnable1<Integer>(taskId) {
            @Override
            public void run() {
                int iTaskId = mP1;
                JNIHelper.logd("really begin pause tts: " + iTaskId);
                if (mCurTask != null && mCurTask.iTaskId == iTaskId) {
                    if (getTtsTool() != null && getTtsTool().isBusy()) {
                        getTtsTool().pause();
                    }
                }
            }
        }, 0);
    }

    /**
     * 恢复TTS
     */
    public void resumeSpeak(int taskId) {
        if (taskId == INVALID_TTS_TASK_ID) {
            return;
        }
        JNIHelper.logd(TAG + "resume: " + taskId);
        AppLogic.runOnBackGround(new Runnable1<Integer>(taskId) {
            @Override
            public void run() {
                int iTaskId = mP1;
                JNIHelper.logd("really begin resume tts: " + iTaskId);
                if (mCurTask != null && mCurTask.iTaskId == iTaskId) {
                    if (getTtsTool() != null && getTtsTool().isBusy()) {
                        getTtsTool().resume();
                    }
                }
            }
        }, 0);
    }

    // /////////////////////////////////////////////////////////////////

    /**
     * 清理所有tts语音，暂时不开放，业务不应该有该调用
     */
    protected void clearSpeak(TtsTask newTask) {
        AppLogic.runOnBackGround(new Runnable1<TtsTask>(newTask) {
            @Override
            public void run() {
                TtsTask newTask = mP1;
                List<TtsTask> q = mTtsTaskQueue;
                mTtsTaskQueue = new ArrayList<TtsTask>();// 必须先清空列表
                TtsTask old = mCurTask;
                mCurTask = null;
                for (int i = 0; i < q.size(); ++i) {
                    TtsTask t = q.get(i);
                    if (t.oRun != null) {
                        AppLogic.runOnBackGround(new Runnable1<TtsTask>(t) {
                            @Override
                            public void run() {
                                JNIHelper.logd(TAG + "speakText end onCancel: id=" + mP1.iTaskId);
                                mP1.oRun.onCancel();
                                mP1.oRun.onEnd();
                            }
                        }, 0);
                    }
                }
                if (newTask != null) {
                    mTtsTaskQueue.add(newTask);
                }
                mCurTask = old;
                if (mCurTask != null) {
                    cancelCurTask();
                } else {
                    speakNext();
                }
            }
        }, 0);
    }

    // /////////////////////////////////////////////////////////////////
    public boolean isBusy() {
        return mCurTask != null || mTtsTaskQueue.isEmpty() == false;
    }

    public void setVoiceSpeed(int speed) {
        JNIHelper.logd("setVoiceSpeed speed=" + speed);
        if (mInitSuccessed && getTtsTool() != null) {
            getTtsTool().setVoiceSpeed(speed);
        }
    }

    public int getVoiceSpeed() {
        if (mInitSuccessed && getTtsTool() != null) {
            return getTtsTool().getVoiceSpeed();
        }
        return 0;
    }

    public int getCurTaskId() {
        if (mCurTask != null) {
            return mCurTask.iTaskId;
        }
        return INVALID_TTS_TASK_ID;
    }

    public byte[] invokeCommTts(final String packageName, final String command, final byte[] data) {
        String cmd = command.substring(TXZTtsPlayerManager.TTS_PLAYER_INVOKE_PREFIX.length());
        if (cmd.equals(TXZTtsPlayerManager.INVOKE_SPEAK)) {
            JSONBuilder jsonDoc = new JSONBuilder(new String(data));
            int iStream = jsonDoc.getVal("iStream", Integer.class);
            String sText = jsonDoc.getVal("sText", String.class);
            long delay = jsonDoc.getVal("delay", Long.class, 0l);
            TtsUtil.PreemptType bPreempt =
                    TtsUtil.PreemptType.valueOf(jsonDoc.getVal("bPreempt", String.class));
            int remoteTtsId =
                    speakText(iStream, sText, bPreempt, new TXZTtsPlayerManager.ITtsCallback() {
                        @Override
                        public void onPause() {
                            String data = new JSONBuilder().put("ttsId", mTaskId).toString();
                            ServiceManager.getInstance().sendInvoke(packageName,
                                    TXZTtsPlayerManager.TTS_PLAYER_CMD_PREFIX +
                                            TXZTtsPlayerManager.CMD_CALLBACK_PAUSE, data.getBytes(),
                                    null);
                        }

                        @Override
                        public void onResume() {
                            String data = new JSONBuilder().put("ttsId", mTaskId).toString();
                            ServiceManager.getInstance().sendInvoke(packageName,
                                    TXZTtsPlayerManager.TTS_PLAYER_CMD_PREFIX +
                                            TXZTtsPlayerManager.CMD_CALLBACK_RESUME,
                                    data.getBytes(),
                                    null);
                        }

                        @Override
                        public void onBegin() {
                            String data = new JSONBuilder().put("ttsId", mTaskId).toString();
                            ServiceManager.getInstance().sendInvoke(packageName,
                                    TXZTtsPlayerManager.TTS_PLAYER_CMD_PREFIX +
                                            TXZTtsPlayerManager.CMD_CALLBACK_BEGIN, data.getBytes(),
                                    null);
                        }

                        @Override
                        public void onCancel() {
                            String data = new JSONBuilder().put("ttsId", mTaskId).toString();
                            ServiceManager.getInstance().sendInvoke(packageName,
                                    TXZTtsPlayerManager.TTS_PLAYER_CMD_PREFIX +
                                            TXZTtsPlayerManager.CMD_CALLBACK_CANCEL,
                                    data.getBytes(),
                                    null);
                        }

                        @Override
                        public void onSuccess() {
                            String data = new JSONBuilder().put("ttsId", mTaskId).toString();
                            ServiceManager.getInstance().sendInvoke(packageName,
                                    TXZTtsPlayerManager.TTS_PLAYER_CMD_PREFIX +
                                            TXZTtsPlayerManager.CMD_CALLBACK_SUCCESS,
                                    data.getBytes(),
                                    null);
                        }

                        @Override
                        public void onError(final int iError) {
                            String data =
                                    new JSONBuilder().put("ttsId", mTaskId).put("error", iError)
                                            .toString();
                            ServiceManager.getInstance().sendInvoke(packageName,
                                    TXZTtsPlayerManager.TTS_PLAYER_CMD_PREFIX +
                                            TXZTtsPlayerManager.CMD_CALLBACK_ERROR, data.getBytes(),
                                    null);
                        }
                    });
            return (remoteTtsId + "").getBytes();
        } else if (cmd.equals(TXZTtsPlayerManager.INVOKE_CANCEL)) {
            cancelSpeak(Integer.parseInt(new String(data)));
            return null;
        } else if (cmd.equals(TXZTtsPlayerManager.INVOKE_RESUME)) {
            resumeSpeak(Integer.parseInt(new String(data)));
            return null;
        } else if (cmd.equals(TXZTtsPlayerManager.INVOKE_PAUSE)) {
            pauseSpeak(Integer.parseInt(new String(data)));
            return null;
        } else if (cmd.equals(TXZTtsPlayerManager.INVOKE_VOICE_SPEED)) {
            if (data == null) {
                return null;
            }
            int speed = Integer.parseInt(new String(data));
            setVoiceSpeed(speed);
            return null;
        }


        return null;
    }
}
