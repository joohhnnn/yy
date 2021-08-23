package com.txznet.sdk;

import android.media.AudioManager;
import android.os.Parcel;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseArray;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.util.runnables.Runnable2;

import java.util.ArrayList;

import static com.txznet.comm.remote.ServiceManager.TXZ;

/**
 * 类名：支持暂停和继续播放的TTS播报管理器
 * 类描述：正常情况还是使用TXZTtsManager，这个类增加了暂停和继续播放的能力，但未处理音频焦点相关的逻辑，需要实现方自行处理
 */
public class TXZTtsPlayerManager {
    public static final int DEFAULT_STREAM_TYPE = -1;
    public static int DEFAULT_TTS_STREAM = AudioManager.STREAM_ALARM;
    private static TXZTtsPlayerManager sInstance = new TXZTtsPlayerManager();
    public static final String TTS_PLAYER_CMD_PREFIX = "txz.ttsplayer.cmd."; // TXZCore -> SDK
    public static final String TTS_PLAYER_INVOKE_PREFIX = "txz.ttsplayer.invoke."; // SDK -> TXZCore

    public static final String INVOKE_VOICE_SPEED = "voice_speed";//设置播报速度
    public static final String INVOKE_BUFFER_TIME = "buffer_time";//缓冲时间
    public static final String INVOKE_ENABLE_DOWN_VOLUME = "enable_down_volume";//
    public static final String INVOKE_DEFAULT_AUDIO_STREAM = "default_audio_stream";//
    public static final String INVOKE_SPEAK = "speak";//
    public static final String INVOKE_CANCEL = "cancel";//
    public static final String INVOKE_PAUSE = "pause";//
    public static final String INVOKE_RESUME = "resume";//

    public static final String CMD_CALLBACK_SUCCESS = "success";//成功回调
    public static final String CMD_CALLBACK_BEGIN = "begin";//成功回调
    public static final String CMD_CALLBACK_CANCEL = "cancel";//取消回调
    public static final String CMD_CALLBACK_ERROR = "error";//错误回调
    public static final String CMD_CALLBACK_PAUSE = "pause";//暂停回调
    public static final String CMD_CALLBACK_RESUME = "resume";//恢复回调

    public static final int ERROR_CODE_TIEMOUT = 1;

    /**
     * 常量名：无效的tts任务id
     * 变量描述：无效的tts任务使用此ID
     */
    public final static int INVALID_TTS_TASK_ID = TtsUtil.INVALID_TTS_TASK_ID;


    private static SparseArray<RemoteTtsTask> mTaskMap = new SparseArray<RemoteTtsTask>();

    private static class TaskEvent {
        String event;
        Integer error;
        long time;
    }

    private static SparseArray<ArrayList<TaskEvent>> mTaskEventMap =
            new SparseArray<ArrayList<TaskEvent>>(); //存储过早回包的tts事件


    private TXZTtsPlayerManager() {
    }

    /**
     * 获取单例
     *
     * @return 类实例
     */
    public static TXZTtsPlayerManager getInstance() {
        return sInstance;
    }

    /**
     * 重连时需要重新通知同行者的操作放这里
     */
    void onReconnectTXZ() {
        clearTaskMap();

        if (mDefaultAudioStream != null) {
            setDefaultAudioStream(mDefaultAudioStream);
        }
        if (mVoiceSpeed != null) {
            setVoiceSpeed(mVoiceSpeed);
        }

        if (mBufferTime != null) {
            setBufferTime(mBufferTime);
        }
        if (mEnableDownVolume != null) {
            enableDownVolumeWhenNav(mEnableDownVolume);
        }
    }


    /**
     * 类名：TTS逻辑回调类
     * 类描述：回调包含TTS播报状态：开始、结束、完成、取消、出错，详见{@link TtsUtil.ITtsCallback}
     */
    public static abstract class ITtsCallback extends TtsUtil.ITtsCallback {

        /**
         * 暂停回调
         */
        public void onPause() {
        }

        /**
         * 恢复播放回调
         */
        public void onResume() {
        }
    }

    /**
     * 枚举类名：TTS播报类型
     * 枚举类描述：TTS播报时使用，针对不同类型处理方式不同
     */
    public static enum PreemptType {
        /**
         * 枚举名：不打断模式
         * 枚举描述：TTS播报时设置此模式，TTS任务进入队列，不打断当前TTS
         */
        PREEMPT_TYPE_NONE,
        /**
         * 枚举名：立即打断模式
         * 枚举描述：TTS播报时设置此模式，TTS任务直接打断当前TTS进行播报
         */
        PREEMPT_TYPE_IMMEADIATELY,
        /**
         * 枚举名：下一个插入模式
         * 枚举描述：TTS播报时设置此模式，插入至TTS队列下一个
         */
        PREEMPT_TYPE_NEXT,
        /**
         * 枚举名：清空队列模式
         * 枚举描述：TTS播报时设置此模式，TTS任务进入队列，并清空当前非自身队列任务
         */
        PREEMPT_TYPE_FLUSH,
        /**
         * 枚举名：立即打断模式
         * 枚举描述：TTS播报时设置此模式，立即打断当前TTS，但不取消tts任务，当前TTS任务延后
         */
        PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE;
    }


    /**
     * 方法名：TTS播报
     * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
     * 此方法仅为播报，文本不会显示在语音界面
     *
     * @param text 播报文本
     * @return 返回Tts的任务ID
     */
    public int speakText(String text) {
        return speakText(text, PreemptType.PREEMPT_TYPE_NONE, null);
    }

    /**
     * 方法名：TTS播报
     * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
     * 此方法仅为播报，文本不会显示在语音界面
     *
     * @param text     播报的文本
     * @param callback 播报回调
     * @return 返回Tts的任务ID
     */
    public int speakText(String text, ITtsCallback callback) {
        return speakText(text, PreemptType.PREEMPT_TYPE_NONE, callback);
    }

    /**
     * 方法名：TTS播报
     * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
     * 此方法仅为播报，文本不会显示在语音界面
     *
     * @param text     播报的文本
     * @param type     打断类型
     * @param callback 播报回调
     * @return 返回Tts的任务ID
     */
    public int speakText(String text, PreemptType type, ITtsCallback callback) {
        return speakText(DEFAULT_STREAM_TYPE, text, type, callback);
    }

    /**
     * 方法名：TTS播报
     * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
     * 此方法仅为播报，文本不会显示在语音界面
     *
     * @param streamType 流类型，填写AudioManager.STREAM常量，默认使用AudioManager.STREAM_ALARM
     * @param text       播报的文本
     * @param type       打断类型
     * @param callback   播报回调
     * @return 返回Tts的任务ID
     */
    public int speakText(int streamType, String text, PreemptType type,
            ITtsCallback callback) {
        return speakText(streamType, text, 0, type, callback);
    }


    /**
     * 方法名：TTS播报
     * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
     * 此方法仅为播报，文本不会显示在语音界面
     *
     * @param text     播报的文本
     * @param delay    播报延时，在已经获取到焦点时不会延时
     * @param type     打断类型
     * @param callback 播报回调
     * @return 返回Tts的任务ID
     */
    public int speakText(String text, long delay, PreemptType type, ITtsCallback callback) {
        return speakText(DEFAULT_STREAM_TYPE, text, delay, type, callback);
    }

    /**
     * 方法名：TTS播报
     * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
     * 此方法仅为播报，文本不会显示在语音界面
     *
     * @param streamType 流类型，填写AudioManager.STREAM常量，默认使用AudioManager.STREAM_ALARM
     * @param text       播报的文本
     * @param delay      播报延时，在已经获取到焦点时不会延时
     * @param type       打断类型
     * @param callback   播报回调
     * @return 返回Tts的任务ID
     */
    public int speakText(int streamType, String text, long delay, PreemptType type,
            final ITtsCallback callback) {
        TtsUtil.PreemptType pt = TtsUtil.PreemptType.PREEMPT_TYPE_NONE;
        switch (type) {
            case PREEMPT_TYPE_FLUSH:
                pt = TtsUtil.PreemptType.PREEMPT_TYPE_FLUSH;
                break;
            case PREEMPT_TYPE_IMMEADIATELY:
                pt = TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY;
                break;
            case PREEMPT_TYPE_NEXT:
                pt = TtsUtil.PreemptType.PREEMPT_TYPE_NEXT;
                break;
            case PREEMPT_TYPE_NONE:
                pt = TtsUtil.PreemptType.PREEMPT_TYPE_NONE;
                break;
            case PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE:
                pt = TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE;
                break;
            default:
                break;
        }
        String data = genInvokeData(streamType, text, pt, delay);
        synchronized (mTaskMap) {
            final RemoteTtsTask remoteTtsTask = new RemoteTtsTask();
            final ITtsCallback cb = callback == null ? null : new ITtsCallback() {
                boolean hasBegin = false;
                boolean hasComplete = false;
                boolean hasEnd = false;
                boolean hasPause = false;

                @Override
                public void onBegin() {
                    if (!hasBegin) {
                        hasBegin = true;
                        callback.onBegin();
                    }
                }

                @Override
                public void onEnd() {
                    if (!hasEnd) {
                        hasEnd = true;
                        callback.onEnd();
                    }
                }

                @Override
                public void onCancel() {
                    if (!hasComplete) {
                        hasComplete = true;
                        callback.onCancel();
                        this.onEnd();
                    }
                }

                @Override
                public void onSuccess() {
                    if (!hasComplete) {
                        hasComplete = true;
                        callback.onSuccess();
                        this.onEnd();
                    }
                }

                @Override
                public void onError(int iError) {
                    if (!hasComplete) {
                        hasComplete = true;
                        callback.onError(iError);
                        this.onEnd();
                    }
                }

                @Override
                public void onResume() {
                    if (hasPause && !hasComplete) {
                        remoteTtsTask.isPaused = false;
                        hasPause = false;
                        callback.onResume();
                    }
                }

                @Override
                public void onPause() {
                    if (!hasPause && !hasComplete) {
                        remoteTtsTask.isPaused = true;
                        hasPause = true;
                        callback.onPause();
                    }
                    super.onPause();
                }
            };

            int localTtsId = ServiceManager.getInstance()
                    .sendInvoke(TXZ, TTS_PLAYER_INVOKE_PREFIX + INVOKE_SPEAK, data.getBytes(),
                            new ServiceManager.GetDataCallback() {
                                @Override
                                public void onGetInvokeResponse(ServiceManager.ServiceData data) {
                                    synchronized (mTaskMap) {
                                        RemoteTtsTask remoteTtsTask = mTaskMap
                                                .get(getTaskId());
                                        if (remoteTtsTask == null) {
                                            LogUtil.logw("find local task failed: "
                                                    + getTaskId());
                                            return;
                                        }

                                        if (data != null) {
                                            remoteTtsTask.remoteId = data.getInt();
                                            if (remoteTtsTask.isCanceled) {
                                                ServiceManager.getInstance()
                                                        .sendInvoke(TXZ, TTS_PLAYER_INVOKE_PREFIX +
                                                                        INVOKE_CANCEL,
                                                                ("" + remoteTtsTask.remoteId)
                                                                        .getBytes(), null);
                                                mTaskMap.remove(getTaskId());
                                                return;
                                            }

                                            procSpeakVoiceEvent(remoteTtsTask.remoteId);
                                        } else {
                                            if (isTimeout()) {
                                                if (cb != null) {
                                                    cb.onBegin();
                                                    cb.onError(ERROR_CODE_TIEMOUT);
                                                }
                                                mTaskMap.remove(getTaskId());
                                            }
                                        }
                                    }
                                }
                            });

            if (mTaskMap.get(localTtsId) != null) {
                LogUtil.logw("already exist tts task id: " + localTtsId);
            }

            remoteTtsTask.callback = cb;
            mTaskMap.put(localTtsId, remoteTtsTask);
            return localTtsId;
        }
    }


    private static class RemoteTtsTask {
        int remoteId = -1;
        boolean isCanceled;
        ITtsCallback callback;
        boolean isPaused;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + remoteId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RemoteTtsTask other = (RemoteTtsTask) obj;
            if (remoteId != other.remoteId) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "RemoteTtsTask [remoteId=" + remoteId + ", isCanceled=" + isCanceled +
                    ", callback=" + callback + "]";
        }
    }

    private static void procSpeakVoiceEvent(Integer remotetaskId) {
        synchronized (mTaskMap) {
            for (int i = 0; i < mTaskEventMap.size(); ) {
                final int remoteId = mTaskEventMap.keyAt(i);
                final ArrayList<TaskEvent> taskEventList = mTaskEventMap.valueAt(i);
                // 处理空任务事件
                if (taskEventList.isEmpty()) {
                    mTaskEventMap.removeAt(i);
                    continue;
                }
                // 处理超时任务事件
                if (SystemClock.elapsedRealtime() -
                        taskEventList.get(taskEventList.size() - 1).time > 5 * 60 * 1000) {
                    mTaskEventMap.removeAt(i);
                    continue;
                }
                // 处理先到的事件
                if (remotetaskId == null || remoteId == remotetaskId) {
                    ServiceManager.getInstance().runOnServiceThread(new Runnable() {
                        @Override
                        public void run() {
                            for (TaskEvent taskEvent : taskEventList) {
                                LogUtil.logw("process tts old event " + taskEvent.event + " for :" +
                                        remoteId);
                                notifyTtsCallback(taskEvent.event, remoteId, taskEvent.error);
                            }
                        }
                    }, 0);
                    mTaskEventMap.removeAt(i);
                    continue;
                }
                ++i;
            }
        }
    }


    public static void notifyTtsCallback(final String event, int remoteTtsId,
            final Integer iError) {
        synchronized (mTaskMap) {

            for (int i = 0; i < mTaskMap.size(); ++i) {
                final RemoteTtsTask task = mTaskMap.valueAt(i);
                if (task.remoteId == remoteTtsId) {
                    if (task.callback != null) {
                        ServiceManager.getInstance().runOnServiceThread(new Runnable() {
                            @Override
                            public void run() {
                                if (CMD_CALLBACK_BEGIN.equals(event)) {
                                    task.callback.onBegin();
                                } else if (CMD_CALLBACK_SUCCESS.equals(event)) {
                                    task.callback.onSuccess();
                                } else if (CMD_CALLBACK_CANCEL.equals(event)) {
                                    task.callback.onCancel();
                                } else if (CMD_CALLBACK_ERROR.equals(event)) {
                                    task.callback.onError(iError);
                                } else if (CMD_CALLBACK_PAUSE.equals(event)) {
                                    task.callback.onPause();
                                } else if (CMD_CALLBACK_RESUME.equals(event)) {
                                    task.callback.onResume();
                                }
                            }
                        }, 0);
                    }
                    if (CMD_CALLBACK_SUCCESS.equals(event) || CMD_CALLBACK_CANCEL.equals(event)
                            || CMD_CALLBACK_ERROR.equals(event)) {
                        mTaskMap.removeAt(i);
                    }
                    return;
                }
            }

            LogUtil.logw("can not found task: " + remoteTtsId + " for event " + event);


            ArrayList<TaskEvent> taskEventList = mTaskEventMap.get(remoteTtsId);
            if (taskEventList == null) {
                taskEventList = new ArrayList<TaskEvent>();
                mTaskEventMap.put(remoteTtsId, taskEventList);
            }
            TaskEvent taskEvent = new TaskEvent();
            taskEvent.event = event;
            taskEvent.time = SystemClock.elapsedRealtime();
            taskEvent.error = iError;
            taskEventList.add(taskEvent);
        }

    }

    public static void clearTaskMap() {
        synchronized (mTaskMap) {
            for (int i = 0; i < mTaskMap.size(); i++) {
                RemoteTtsTask task = mTaskMap.valueAt(i);
                if (task != null && task.callback != null) {
                    task.callback.onError(-1024);
                }
            }
            mTaskMap.clear();
        }
    }


    private static String genInvokeData(int iStream, String sText, TtsUtil.PreemptType bPreempt,
            long delay) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("iStream", iStream);
        builder.put("bPreempt", bPreempt.toString());
        builder.put("sText", sText);
        // builder.put("voiceUrls", voiceUrls);
        builder.put("delay", delay);
        return builder.toString();
    }


    /**
     * 方法名：取消播报
     * 方法描述：根据TTS任务ID，强制取消TTS任务
     *
     * @param taskId 需要取消的TTS任务ID
     */
    public void cancelSpeak(int taskId) {
        synchronized (mTaskMap) {
            RemoteTtsTask task = mTaskMap.get(taskId);
            if (task != null) {
                if (task.remoteId == -1) {
                    task.isCanceled = true;
                } else {
                    ServiceManager.getInstance()
                            .sendInvoke(TXZ, TTS_PLAYER_INVOKE_PREFIX + INVOKE_CANCEL,
                                    ("" + task.remoteId).getBytes(), null);
                    if (task.callback == null) {
                        mTaskMap.remove(taskId);
                    }
                }
                // 优先回调
                if (task.callback != null) {
                    ServiceManager.getInstance().runOnServiceThread(
                            new Runnable2<RemoteTtsTask, Integer>(task, taskId) {
                                @Override
                                public void run() {
                                    if (mP1.callback != null) {
                                        mP1.callback.onCancel();
                                    }
                                }
                            }, 0);
                }
            }
        }
    }

    public void pauseSpeak(int taskId) {
        synchronized (mTaskMap) {
            RemoteTtsTask task = mTaskMap.get(taskId);
            if (task != null) {
                if (task.remoteId == -1) {
                    task.isPaused = true;
                } else if (!task.isPaused) {
                    ServiceManager.getInstance()
                            .sendInvoke(TXZ, TTS_PLAYER_INVOKE_PREFIX + INVOKE_PAUSE,
                                    ("" + task.remoteId).getBytes(), null);
                }

                // 优先回调
                if (task.callback != null) {
                    ServiceManager.getInstance().runOnServiceThread(
                            new Runnable2<RemoteTtsTask, Integer>(task, taskId) {
                                @Override
                                public void run() {
                                    if (mP1.callback != null) {
                                        mP1.callback.onPause();
                                    }
                                }
                            }, 0);
                }

            }
        }
    }

    public void resumeSpeak(int taskId) {
        synchronized (mTaskMap) {
            RemoteTtsTask task = mTaskMap.get(taskId);
            if (task != null) {
                if (task.remoteId == -1) {
                    task.isPaused = false;
                } else if (task.isPaused) {
                    ServiceManager.getInstance()
                            .sendInvoke(TXZ, TTS_PLAYER_INVOKE_PREFIX + INVOKE_RESUME,
                                    ("" + task.remoteId).getBytes(), null);
                }
                // 优先回调
                if (task.callback != null) {
                    ServiceManager.getInstance().runOnServiceThread(
                            new Runnable2<RemoteTtsTask, Integer>(task, taskId) {
                                @Override
                                public void run() {
                                    if (mP1.callback != null) {
                                        mP1.callback.onResume();
                                    }
                                }
                            }, 0);
                }

            }
        }
    }

    public static byte[] preInvokeTtsPlayerEvent(String packageName, String command, byte[] data) {
        JSONBuilder jsonDoc = new JSONBuilder(new String(data));
        int ttsId = jsonDoc.getVal("ttsId", Integer.class);
        if (command.equals(CMD_CALLBACK_BEGIN)) {
            notifyTtsCallback(CMD_CALLBACK_BEGIN, ttsId, null);
        } else if (command.equals(CMD_CALLBACK_SUCCESS)) {
            notifyTtsCallback(CMD_CALLBACK_SUCCESS, ttsId, null);
        } else if (command.equals(CMD_CALLBACK_CANCEL)) {
            notifyTtsCallback(CMD_CALLBACK_CANCEL, ttsId, null);
        } else if (command.equals(CMD_CALLBACK_ERROR)) {
            int error = jsonDoc.getVal("error", Integer.class);
            notifyTtsCallback(CMD_CALLBACK_ERROR, ttsId, error);
        } else if (command.equals(CMD_CALLBACK_PAUSE)) {
            notifyTtsCallback(CMD_CALLBACK_PAUSE, ttsId, null);
        } else if (command.equals(CMD_CALLBACK_RESUME)) {
            notifyTtsCallback(CMD_CALLBACK_RESUME, ttsId, null);
        }
        return null;
    }

    private Integer mDefaultAudioStream = null;
    private Integer mVoiceSpeed = null;
    private Integer mBufferTime = null;
    private Boolean mEnableDownVolume = null;

    /**
     * 方法名：设置TTS播报流
     * 方法描述：设置默认的TTS播报音频流，使用AudioManager的STREAM常量，默认使用AudioManager.STREAM_ALARM
     *
     * @param stream 播报流
     */
    public void setDefaultAudioStream(int stream) {
        mDefaultAudioStream = stream;
        DEFAULT_TTS_STREAM = stream;
        ServiceManager.getInstance()
                .sendInvoke(ServiceManager.TXZ,
                        TTS_PLAYER_INVOKE_PREFIX + INVOKE_DEFAULT_AUDIO_STREAM,
                        ("" + stream).getBytes(), null);
    }

    /**
     * 方法名：设置合成的TTS播放语速
     * 方法描述：设置合成的TTS播放语速，取值范围1-100,数值越大，语速越快,同行者默认值是70
     *
     * @param speed 合成的TTS播放语速
     */
    public void setVoiceSpeed(int speed) {
        if (speed < 1) {
            speed = 1;
        } else if (speed > 100) {
            speed = 100;
        }

        mVoiceSpeed = speed;
        ServiceManager.getInstance()
                .sendInvoke(ServiceManager.TXZ, TTS_PLAYER_INVOKE_PREFIX + INVOKE_VOICE_SPEED,
                        ("" + speed).toString().getBytes(), null);
    }

    /**
     * 方法名：设置TTS播报的缓冲时间
     * 方法描述：设置TTS播报的缓冲时间, 取值范围 0 ~ 15000, 默认值为300, 单位毫秒
     * 由于TTS是合成音，实际应用中系统会因为防POP音等问题造成TTS掉字，通过此方法规避问题
     *
     * @param nTime 缓冲时间，单位ms
     */
    public void setBufferTime(int nTime) {
        mBufferTime = nTime;
        ServiceManager.getInstance()
                .sendInvoke(ServiceManager.TXZ, TTS_PLAYER_INVOKE_PREFIX + INVOKE_BUFFER_TIME,
                        ("" + nTime).toString().getBytes(), null);
    }

    /**
     * 方法名：设置导航播报语音时TTS是否降低音量
     * 方法描述：设置导航播报语音时TTS是否降低音量，默认语音与导航共存，根据需求修改，一般推荐修改导航降低音量
     *
     * @param enable 是否降低音量
     */
    public void enableDownVolumeWhenNav(boolean enable) {
        mEnableDownVolume = enable;
        ServiceManager.getInstance()
                .sendInvoke(ServiceManager.TXZ,
                        TTS_PLAYER_INVOKE_PREFIX + INVOKE_ENABLE_DOWN_VOLUME,
                        ("" + enable).toString().getBytes(), null);
    }
}
