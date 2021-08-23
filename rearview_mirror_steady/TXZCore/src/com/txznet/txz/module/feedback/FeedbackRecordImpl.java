package com.txznet.txz.module.feedback;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.RecorderUtil.RecordCallback;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.record.IRecord;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.recordcenter.ITXZSourceRecorder;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FeedbackRecordImpl implements IRecord {
    volatile boolean mWorking = false;
    long lastMuteTime = 0;// 连续出现静音段中的第一次出现静音的时间
    long mBeginTime = 0;
    private RecordCallback mRecordCallback;
    private RecordOption mOption;
    private int mTimeOut = 3000;
    private long mSpeechBeginTime = 0;
    private long mSpeechEndTime = 0;
    long mSpeakTagConsume = 0;
    String mLastSpeakTag = null;
    private ExecutorService mExecutorService = Executors.newCachedThreadPool();

    @Override
    public void start(RecordCallback callback) {
        start(callback, new RecordOption());
    }

    private Handler mWorkHandler = null;
    private HandlerThread mWorkThread = null;// 录音工作线程
    private boolean mHasInit = false;

    public void init() {
        mWorkThread = new HandlerThread("feedback_record");
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper());
        mHasInit = true;
    }

    private TXZAudioRecorder mRecorder = null;
    private FileOutputStream mFileOutputStream = null;
    private File rawFile;

    private void startRecording() {
        mWorkHandler.post(new Runnable() {

            @Override
            public void run() {
                rawFile = new File(ProjectCfg.AUDIO_SAVE_PATH + "/feedback_" + System.currentTimeMillis() + RecordFile.SUFFIX_PCM);
                if (rawFile.exists()) {
                    LogUtil.e("file exists");
                    rawFile.delete();
                }
                try {
                    mFileOutputStream = new FileOutputStream(rawFile);
                    final byte[] buffer = new byte[1200];
                    int length = 0;
                    mRecorder.startRecording();
                    mBeginTime = SystemClock.elapsedRealtime();
                    while (mWorking && (length = mRecorder.read(buffer, 0, buffer.length)) > 0 && (SystemClock.elapsedRealtime() - mBeginTime) < mOption.mMaxSpeech)        {
                        mFileOutputStream.write(buffer, 0, length);
                    }
                    mFileOutputStream.flush();
                    stop();
                    LogUtil.d("record status : finish");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void close() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mWorking = false;
                    if (mRecorder != null) {
                        mRecorder.stop();
                    }
                } finally {
                    if (mFileOutputStream != null) {
                        try {
                            mFileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void start(RecordCallback callback, RecordOption option) {
        if (!mHasInit) {
            init();
        }
        JNIHelper.logd("record status: start " + mWorking);
        if (mWorking || callback == null || option == null) {
            return;
        }
        mWorking = true;
        lastMuteTime = 0;
        mRecordCallback = callback;
        mOption = option;
        mTimeOut = option.mMaxMute > 2000 ? option.mMaxMute : 2000;
        mLastSpeechTime = mBeginTime = 0;
        mSpeechBeginTime = mSpeechEndTime = 0;
        mLastMuteDuration = null;
        mSpeakTagConsume = 0;
        LogUtil.d("option mMaxSpeech " + option.mMaxSpeech + " : " + "mTimeout " + mTimeOut);
        mLastSpeakTag = null;
        if (mRecordCallback != null) {
            mRecorder = new TXZAudioRecorder(ITXZSourceRecorder.READER_TYPE_AEC);
            mRecordCallback.onBegin();
        }
        startRecording();
        TtsManager.getInstance().pause();
        useAsrWakeup();
    }

    @Override
    public void stop() {
        onEnd();
        JNIHelper.logd("record status: stop");

    }

    @Override
    public void cancel() {
        doEnd(true);
    }

    @Override
    public boolean isBusy() {
        return mWorking;
    }


    long mLastSpeechTime = 0;
    Integer mLastMuteDuration = null;

    private void notifyMuteDuration(int d) {
        int ds = d / 1000;
        if (mLastMuteDuration == null || mLastMuteDuration != ds) {
            mLastMuteDuration = ds;
            if (mRecordCallback != null) {
                JNIHelper.logd("record_vol mute " + ds);
                mRecordCallback.onMute(ds * 1000);
            }
        }
    }

    private void checkMute(int volume) {
        if (!mWorking) {
            return;
        }
        long currTime = SystemClock.elapsedRealtime();
        // 正真启动录音才开始记时
        if (mBeginTime == 0) {
            mBeginTime = currTime;
        }
        // 正真启动录音才开始记时
        if (mSpeechEndTime == 0) {
            mSpeechEndTime = currTime;
        }

        if (!mWorking || mRecordCallback == null) {
            return;
        }

        mRecordCallback.onVolume(volume);
        // 录音超时
        if (mOption != null
                && currTime - mBeginTime > mOption.mMaxSpeech) {
            JNIHelper.logd("record status: onSpeechTimeout" + mOption.mMaxSpeech);
            mRecordCallback.onSpeechTimeout();
            return;
        }

        long duration = 0;
        if (mSpeechBeginTime < mSpeechEndTime) {
            duration = currTime - mSpeechEndTime;
        }

        if (duration >= mTimeOut) {
            JNIHelper.logd("record status: onMuteTimeout");
            mRecordCallback.onMuteTimeout();

            return;
        }
        notifyMuteDuration((int) duration);
    }

    private void onVolumeOfRecord(final int vol) {
        Runnable oRun = new Runnable() {
            @Override
            public void run() {
                checkMute(vol);
            }
        };
        AppLogic.runOnBackGround(oRun, 0);
    }

    private void onEnd() {
        Runnable oRun = new Runnable() {
            @Override
            public void run() {
                doEnd(false);
            }
        };
        AppLogic.runOnBackGround(oRun, 0);
    }

    private void doEnd(boolean bCanceled) {
        LogUtil.logd("record status: doEnd, working=" + mWorking);

        if (!mWorking) {
            return;
        }
        ReportUtil.doReport(new ReportUtil.Report.Builder().setType("speechLength")
                .putExtra("speechLength", SystemClock.elapsedRealtime() - mBeginTime)
                .buildCommReport());
        close();

        if (mRecordCallback != null) {
            if (bCanceled) {
                mRecordCallback.onCancel();
                if (rawFile.exists()) {
                    rawFile.delete();
                }
            } else {
                mRecordCallback.onEnd((int) (mSpeechBeginTime == 0 ? 0 : SystemClock.elapsedRealtime() - mBeginTime));
                mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        upload(rawFile.getAbsolutePath());
                    }
                });
            }
            mRecordCallback = null;
        }
        mOption = null;
        mRecorder = null;
        cancelAsrWakeup();
        TtsManager.getInstance().resume();
        WakeupManager.getInstance().stop();
        WakeupManager.getInstance().start();
    }

    private final static String TASK_ID = FeedbackManager.RECORD_TASK_ID;

    private void useAsrWakeup() {
        AsrComplexSelectCallback cb = new AsrComplexSelectCallback() {
            @Override
            public boolean needAsrState() {
                return true;
            }

            @Override
            public String getTaskId() {
                return TASK_ID;
            }

            @Override
            public void onSpeechBegin() {
                mSpeechBeginTime = SystemClock.elapsedRealtime();
                if (mRecordCallback instanceof FeedbackRecordCallback && mRecordCallback != null) {
                    ((FeedbackRecordCallback) mRecordCallback).onSpeechBegin();
                }
                LogUtil.d("FeedbackRecordImpl onSpeechBegin " + mSpeechBeginTime);
            }

            @Override
            public void onSpeechEnd() {
                mSpeechEndTime = SystemClock.elapsedRealtime();
                if (mRecordCallback instanceof FeedbackRecordCallback && mRecordCallback != null) {
                    ((FeedbackRecordCallback) mRecordCallback).onSpeechEnd();
                }
                LogUtil.d("FeedbackRecordImpl onSpeechEnd " + (mSpeechEndTime - mSpeechBeginTime));
            }

            @Override
            public void onVolume(int volume) {
                onVolumeOfRecord(volume);
            }

            @Override
            public void onCommandSelected(String type, String command) {
                if ("CMD_OK".equals(type)) {
                    onEnd();
                    ReportUtil.doReport(new ReportUtil.Report.Builder().setType("speakSend")
                            .putExtra("time", System.currentTimeMillis())
                            .buildCommReport());
                } else if ("CMD_CANCEL".equals(type)) {
                    onCancel();
                    ReportUtil.doReport(new ReportUtil.Report.Builder().setType("speakCancel")
                            .putExtra("time", System.currentTimeMillis())
                            .buildCommReport());
                }
            }

        };
        cb.addCommand("CMD_OK", "完毕完毕");
        cb.addCommand("CMD_CANCEL", "取消取消");
        WakeupManager.getInstance().useWakeupAsAsr(cb);
    }

    private void onCancel() {
        Runnable oRun = new Runnable() {
            @Override
            public void run() {
                doEnd(true);
            }
        };
        AppLogic.runOnBackGround(oRun, 0);
    }

    private void upload(String voiceFilePath) {
        LogUtil.d("Feedback upload " + voiceFilePath);
        if (voiceFilePath == null) {
            LogUtil.d("Feedback upload voiceFilePath is null");
            return;
        }
        final File voiceFile = new File(voiceFilePath);
        if (!voiceFile.exists()) {
            LogUtil.e("Feedback upload voiceFile not exist");
            return;
        }
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                LogUtil.d("Feedback upload " + voiceFile.getName());
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("uid", String.valueOf(ProjectCfg.getUid()))
                        .addFormDataPart("voice_id", UUID.randomUUID().toString())
                        .addFormDataPart("channel", "core")
                        .addFormDataPart("voice_file", voiceFile.getName(),
                                RequestBody.create(MediaType.parse("audio/*"), voiceFile))
                        .build();
                Request request = new Request.Builder()
                        .url("http://f.txzing.com/service/feedback/service/Feedback")
                        .post(requestBody)
                        .build();

                Response response = null;
                try {
                    response = client.newCall(request).execute();

                    voiceFile.delete();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    LogUtil.d("Feedback upload" + response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void cancelAsrWakeup() {
        WakeupManager.getInstance().recoverWakeupFromAsr(TASK_ID);
        RecorderWin.setState(RecorderWin.STATE.STATE_END);
    }


}
