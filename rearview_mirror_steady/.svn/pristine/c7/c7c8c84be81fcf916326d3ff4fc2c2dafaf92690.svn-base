package com.txznet.txz.module.feedback;

import android.os.SystemClock;
import android.widget.Toast;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.RecorderUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZStatusManager;
import com.txznet.txz.component.record.IRecord;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.music.focus.MusicFocusManager;
import com.txznet.txz.module.qrcode.QrCodeManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.TXZFileConfigUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class FeedbackManager extends IModule implements IRecord {

    public static final String RECORD_TASK_ID = "FeedbackManager";
    private static FeedbackManager sFeedbackManager;
    private IRecord mRecorder = null;

    private FeedbackManager() {
        try {
            mRecorder = (IRecord) Class.forName("com.txznet.txz.module.feedback.FeedbackRecordImpl")
                    .newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            mRecorder = null;
        }
    }

    @Override
    public int initialize_AfterInitSuccess() {
        if (TXZFileConfigUtil
                .getBooleanSingleConfig(TXZFileConfigUtil.KEY_FEEDBACK_FEATURE_ENABLE, true)) {
            regCommand("CMD_FEEDBACK");
        } else {
            LogUtil.d("feedback feature is disable");
        }
        return super.initialize_AfterInitSuccess();
    }

    public static FeedbackManager getInstance() {
        if (sFeedbackManager == null) {
            synchronized (RecordManager.class) {
                if (sFeedbackManager == null) {
                    sFeedbackManager = new FeedbackManager();
                }
            }
        }
        return sFeedbackManager;
    }


    private FeedbackWindowManager mFeedbackWindowManager;
    public static final String CHANNEL_NO_SUCCESS_BIND_URL = "txzFeedbackSuccess";
    public static final String CHANNEL_NO_FAIL_BIND_URL = "txzFeedbackFail";

    private long mStartRecordingTime;

    public boolean isCanShowHelpTips() {
        return mCanShowHelpTips;
    }

    public void setCanShowHelpTips(boolean canShowHelpTips) {
        mCanShowHelpTips = canShowHelpTips;
    }

    private boolean mCanShowHelpTips = true;


    private Runnable mUpdateFeedbackRunnable = new Runnable() {
        @Override
        public void run() {
            String text = "发送反馈(3s)";
            if (mFeedbackWindowManager != null &&
                    !text.equals(mFeedbackWindowManager.getFeedbackText())) {
                mFeedbackWindowManager.updateFeedback(text);
            }
        }
    };


    private boolean mNoSpeak = false;
    private int mTtsTaskId;
    private boolean mCanShowFeedbackQrCode = TXZFileConfigUtil
            .getBooleanSingleConfig(TXZFileConfigUtil.KEY_CAN_SHOW_FEEDBACK_QRCODE,
                    true);

    @Override
    public int onCommand(String cmd) {

        ReportUtil.doReport(new ReportUtil.Report.Builder().setType("command")
                .putExtra("time", System.currentTimeMillis())
                .putExtra("command", cmd).setSessionId().buildCommReport());
        if ("CMD_FEEDBACK".equals(cmd)) {
            if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                org.json.JSONObject json = new org.json.JSONObject();
                try {
                    json.put("type", 12);
                    json.put("tips", "关注\"车车互联\"微信公众号，\n" +
                            "发送“我要反馈”，立即反馈您的问题");
                    json.put("vTips", "我要反馈，我要吐槽");
                    json.put("qrCode",
                            QrCodeManager.getInstance().getQrCodeByScene(CHANNEL_NO_FAIL_BIND_URL));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (mCanShowFeedbackQrCode) {
                    RecorderWin.showData(json.toString());
                    AsrManager.getInstance().setNeedCloseRecord(false);
                    RecorderWin.speakTextNotEqualsDisplay(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NO_NET"), null);
                } else {
                    RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NO_NET"), null);
                }

            } else {
                ReportUtil.doReport(new ReportUtil.Report.Builder().setType("feedback")
                        .putExtra("time", System.currentTimeMillis())
                        .putExtra("command", cmd).setSessionId().buildCommReport());
                if (mFeedbackWindowManager == null) {
                    mFeedbackWindowManager = new FeedbackWindowManager(GlobalContext.get());
                }
                mFeedbackWindowManager.setFeedbackWindowDismissListener(
                        new FeedbackWindowManager.FeedbackWindowDismissListener() {
                            @Override
                            public void onDismiss(int dismissReason) {
                                MusicFocusManager.getInstance().releaseAudioFocusImmediately();
                                HelpGuideManager.getInstance().unRegisterFeedbackScene();

                                // 3s内点击反馈按钮，但是没有说话
                                if (mNoSpeak && dismissReason ==
                                        FeedbackWindowManager.DismissReason.DISMISS_NORMAL
                                                .ordinal()) {
                                    mRecorder.cancel();
                                    return;
                                }


                                if (dismissReason ==
                                        FeedbackWindowManager.DismissReason.DISMISS_CLICK_CANCEL
                                                .ordinal()) {
                                    mRecorder.cancel();
                                } else if (dismissReason ==
                                        FeedbackWindowManager.DismissReason.DISMISS_NORMAL
                                                .ordinal()) {
                                    mRecorder.stop();
                                    mCanShowHelpTips = false;
                                    org.json.JSONObject json = new org.json.JSONObject();
                                    String tts = "";
                                    try {
                                        json.put("type", 12);

                                        if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                                            json.put("qrCode", QrCodeManager.getInstance()
                                                    .getQrCodeByScene(CHANNEL_NO_FAIL_BIND_URL));
                                            tts = "网络不佳，试试在公众号反馈吧";

                                        } else {
                                            tts = "反馈结束，感谢您的宝贵意见";
                                            json.put("qrCode", QrCodeManager.getInstance()
                                                    .getQrCodeByScene(CHANNEL_NO_SUCCESS_BIND_URL));
                                        }
                                        json.put("vTips", "我要反馈，我要吐槽");
                                        json.put("tips", "关注\"车车互联\"微信公众号，\n" +
                                                "第一时间获取反馈结果！");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if (!RecorderWin.isOpened()) {
                                        RecorderWin.show();
                                    }

                                    if (!ChoiceManager.getInstance().isSelecting()) {
                                        if (!mCanShowFeedbackQrCode) {
                                            RecorderWin.speakText(tts, null);
                                            return;
                                        }
                                        RecorderWin.addCloseRunnable(new Runnable() {
                                            @Override
                                            public void run() {
                                                TtsManager.getInstance().cancelSpeak(mTtsTaskId);
                                            }
                                        });
                                        RecorderWin.showData(json.toString());
                                        mTtsTaskId = TtsManager.getInstance()
                                                .speakText(tts, new TtsUtil.ITtsCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        super.onSuccess();
                                                        AsrManager.getInstance().start();
                                                    }
                                                });
                                    }
                                }
                            }
                        });
                mNoSpeak = true;
                RecorderWin.addSystemMsg("主人请说");
                TtsManager.getInstance().speakText("主人请说", new TtsUtil.ITtsCallback() {
                    @Override
                    public void onSuccess() {
                        RecorderWin.close();
                        start(new FeedbackRecordCallback() {
                            @Override
                            void onSpeechBegin() {
                                mNoSpeak = false;
                                LogUtil.e("FeedbackManager onSpeechBegin ");
                                if (mFeedbackWindowManager != null) {
                                    mFeedbackWindowManager.startAnimator();
                                }

                            }

                            @Override
                            void onSpeechEnd() {
                                LogUtil.e("FeedbackManager onSpeechEnd ");
                                if (mFeedbackWindowManager != null) {
                                    mFeedbackWindowManager.cancelAnimator();
                                }
                            }

                            @Override
                            public void onBegin() {
                                MusicFocusManager.getInstance().requestAudioFocus(
                                        TXZStatusManager.AudioLogicType.AUDIO_LOGIC_PAUSE);
                                //                                mNeedRequestFocus = true;
                                //                                requestAudioFocus
                                //                                (TXZStatusManager
                                //                                .AudioLogicType
                                //                                .AUDIO_LOGIC_PAUSE,
                                //                                mOnAudioFocusChangeListener,
                                //                                AudioManager.STREAM_MUSIC);
                                mStartRecordingTime = SystemClock.elapsedRealtime();
                                LogUtil.e("FeedbackManager onBegin" + mStartRecordingTime);
                                mFeedbackWindowManager.show();
                                List list = new ArrayList();
                                list.add("完毕完毕");
                                list.add("取消取消");
                                HelpGuideManager.getInstance().registerFeedbackScene(list, true);
                            }

                            @Override
                            public void onEnd(int speechLength) {
                                LogUtil.e("FeedbackManager onEnd" + speechLength);
                                mFeedbackWindowManager.dismiss(
                                        FeedbackWindowManager.DismissReason.DISMISS_NORMAL
                                                .ordinal());
                            }

                            @Override
                            public void onParseResult(int voiceLength, String voiceText,
                                    String voiceUrl) {
                                LogUtil.e("FeedbackManager onParseResult");
                            }

                            @Override
                            public void onSpeechTimeout() {
                                LogUtil.e("FeedbackManager onSpeechTimeout" +
                                        (SystemClock.elapsedRealtime() - mStartRecordingTime));
                                // 结束录音，上传文件
                                mRecorder.stop();
                                ReportUtil.doReport(
                                        new ReportUtil.Report.Builder().setType("autoSendTimeout")
                                                .putExtra("time", System.currentTimeMillis())
                                                .buildCommReport());
                            }

                            @Override
                            public void onMuteTimeout() {
                                if (mNoSpeak) {
                                    ReportUtil.doReport(
                                            new ReportUtil.Report.Builder().setType("autoCancel")
                                                    .putExtra("time", System.currentTimeMillis())
                                                    .buildCommReport());
                                    mRecorder.cancel();
                                } else {
                                    ReportUtil.doReport(
                                            new ReportUtil.Report.Builder().setType("autoSend")
                                                    .putExtra("time", System.currentTimeMillis())
                                                    .buildCommReport());
                                    // 结束录音，上传文件
                                    mRecorder.stop();
                                }
                                LogUtil.d("FeedbackManager onMuteTimeout" +
                                        (SystemClock.elapsedRealtime() - mStartRecordingTime));
                            }

                            @Override
                            public void onError(int err) {
                                LogUtil.e("FeedbackManager onError");
                            }

                            @Override
                            public void onCancel() {
                                mFeedbackWindowManager.dismiss(
                                        FeedbackWindowManager.DismissReason.DISMISS_RECORD_CANCEL
                                                .ordinal());
                                if (mNoSpeak) {
                                    TtsManager.getInstance().speakText("你没有说话，已取消反馈");
                                    Toast.makeText(GlobalContext.get(), "你没有说话，已取消反馈",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    TtsManager.getInstance().speakText("已取消反馈");
                                }

                            }

                            @Override
                            public void onVolume(final int vol) {

                            }

                            @Override
                            public void onMute(int time) {
                                LogUtil.e("FeedbackManager onMute" + time);
                                AppLogic.removeUiGroundCallback(mUpdateFeedbackRunnable);
                                AppLogic.runOnUiGround(mUpdateFeedbackRunnable, 3000);
                                if (mFeedbackWindowManager != null) {
                                    mFeedbackWindowManager
                                            .updateFeedback("发送反馈(" + (3 - time / 1000) + "s)");
                                }
                            }

                            @Override
                            public void onPCMBuffer(short[] buffer, int len) {
                                LogUtil.e("FeedbackManager onPCMBuffer");
                            }

                            @Override
                            public void onMP3Buffer(byte[] buffer) {
                                LogUtil.e("FeedbackManager onMP3Buffer");
                            }
                        });
                    }
                });
            }
        }
        return super.onCommand(cmd);
    }


    @Override
    public void start(RecorderUtil.RecordCallback callback) {
        if (mRecorder != null) {
            RecorderUtil.RecordOption option = new RecorderUtil.RecordOption();
            option.mMaxMute = 3000;
            option.mMaxSpeech = 60 * 1000;
            mRecorder.start(callback, option);
        }
    }

    @Override
    public void start(RecorderUtil.RecordCallback callback, RecorderUtil.RecordOption option) {
        if (mRecorder != null) {
            mRecorder.start(callback, option);
        }
    }

    @Override
    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
        }
    }

    @Override
    public boolean isBusy() {
        if (mRecorder != null) {
            return mRecorder.isBusy();
        }
        return false;
    }

    @Override
    public void cancel() {
        if (mRecorder != null) {
            mRecorder.cancel();
        }
    }
}
