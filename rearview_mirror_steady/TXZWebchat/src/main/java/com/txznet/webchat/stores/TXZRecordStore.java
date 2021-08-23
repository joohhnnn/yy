package com.txznet.webchat.stores;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.StatusUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.txz.util.ThreadManager;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.webchat.Config;
import com.txznet.webchat.Constant;
import com.txznet.webchat.R;
import com.txznet.webchat.RecordStatusObservable;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.actions.ContactActionCreator;
import com.txznet.webchat.actions.MessageActionCreator;
import com.txznet.webchat.actions.TXZReportActionCreator;
import com.txznet.webchat.actions.TtsActionCreator;
import com.txznet.webchat.actions.UploadVoiceActionCreator;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.ReportMessage;
import com.txznet.webchat.ui.base.UIHandler;
import com.txznet.webchat.util.HelpUtil;
import com.txznet.webchat.util.WxMonitorUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TXZRecordStore extends Store {
    private static TXZRecordStore sInstance = new TXZRecordStore(Dispatcher.get());
    private boolean mManuallySend = false; // 是否是手动点击“立即发送按钮”发送的

    /**
     * Constructs and registers an instance of this mProxy with the given dispatcher.
     *
     * @param dispatcher
     */
    TXZRecordStore(Dispatcher dispatcher) {
        super(dispatcher);

        AppLogic.registerRecordStatusObserver(new RecordStatusObservable.StatusObserver() {
            @Override
            public void onStatusChanged(boolean isShowing) {
                if (isShowing && mReplying) {
                    MessageActionCreator.get().cancelReply(true, true);
                }
            }
        });
    }

    public static TXZRecordStore get() {
        return sInstance;
    }

    @Override
    public void onDispatch(Action action) {
        boolean changed = false;
        switch (action.getType()) {
            case ActionType.WX_PLUGIN_LOGIC_RESET:
                reset();
                break;
            case ActionType.WX_REPLY_VOICE_START:
                changed = doWxReplyVoiceStart(action);
                break;
            case ActionType.WX_REPLY_VOICE_END:
                changed = doWxReplyVoiceEnd(action);
                break;
            case ActionType.WX_REPLY_VOICE_PARSE:
                changed = doWxReplyVoiceParse(action);
                break;
            case ActionType.WX_REPLY_VOICE_MUTE:
                changed = doWxReplyVoiceMute(action);
                break;
            case ActionType.WX_REPLY_VOICE_ERROR:
                changed = doWxReplyVoiceError(action);
                break;
            case ActionType.WX_REPLY_VOICE_CANCEL:
                changed = doWxReplyVoiceCancel(action);
                break;
            case ActionType.VOICE_UPLOAD_REQ:
                changed = doVoiceUploadReq(action); // 兼容旧版
                break;
            case ActionType.VOICE_UPLOAD_RESP:
                changed = doVoiceUploadResp(action); // 兼容旧版
                break;
            case ActionType.VOICE_UPLOAD_RESP_ERROR:
                changed = doVoiceUploadError(action); // 兼容旧版
                break;
            case ActionType.WX_SEND_MSG_RESP:
                changed = doWxSendMsgResp(action);
                break;
            case ActionType.WX_SEND_MSG_RESP_ERROR:
                changed = doWxSendMsgErr(action);
                break;
            case ActionType.TXZ_NOTIFY_SHOW:
                mEnableReply = false;
                changed = true;
                break;
            case ActionType.TXZ_NOTIFY_CANCEL:
                mEnableReply = true;
                changed = true;
                break;
            case ActionType.TXZ_NOTIFY_DISMISS:
                //mEnableReply = true;
                changed = false;
                break;
            case ActionType.TXZ_RESTART:
                mEnableReply = true;
                mReplying = false;
                mForeSend = false;
                mSendError = false;
                mSending = false;
                changed = true;
                break;
            case ActionType.WX_REPLY_VOICE_SEND:
                mManuallySend = true;
                break;

            case ActionType.SYSTEM_POWER_REVERSE_ENTER:
                doReverseModeEnter();
                break;
        }
        if (changed) {
            emitChange(TXZRecordStore.EVENT_TYPE_ALL);
        }
    }

    String mVoiceToUserId = "";
    boolean mForeSend = false; // 强制发送模式
    int mCurCountDown; // 倒计时
    private Runnable1<Integer> mForeSendTask = new Runnable1<Integer>(null) {
        @Override
        public void run() {
            if (mP1 <= 0) {
                // 录音时间达到上限时, 强制结束录音, 避免录音端出错导致录音时间无限延长
                MessageActionCreator.get().sendVoice(true);
                L.d("record voice timeout! force stop recording");
                return;
            }
            AppLogic.removeUiGroundCallback(mForeSendTask);
            AppLogic.runOnUiGround(mForeSendTask, 1000);
            mRecorderCountdown = mP1;
            L.d("record voice  Force refreshTimeRemain : " + mP1);
            mP1--;
            mCurCountDown--;
            emitChange(EVENT_TYPE_ALL);
        }
    };

    private Runnable mPrepareForeSendTask = new Runnable() {
        @Override
        public void run() {
            mForeSend = true;
            mCurCountDown = 3; // 60 - 57
            mForeSendTask.update(getCountdown());
            AppLogic.runOnUiGround(mForeSendTask, 0);
        }
    };

    private void doReverseModeEnter() {
        if (isReplying()) {
            MessageActionCreator.get().cancelReply(true, true);
            Toast.makeText(GlobalContext.get(), R.string.lb_toast_record_disable_reverse, Toast.LENGTH_LONG).show();
        }
    }

    private boolean doWxReplyVoiceStart(Action<Bundle> action) {
        mReplying = true;
        mSendError = true;
        mSending = false;
        bReplyCancelled = false;
        mRecorderCountdown = 3;
        Bundle bundle = action.getData();
        mVoiceToUserId = bundle.getString("uid");
        AppLogic.removeUiGroundCallback(mPrepareForeSendTask);
        // 启动录音时将强制刷新task也取消，防止启动新录音任务强制刷新task还在运行导致倒计时被刷新
        AppLogic.removeUiGroundCallback(mForeSendTask);
        AppLogic.runOnUiGround(mPrepareForeSendTask, 60000 - 3000);
        mForeSend = false;

        return true;
    }

    private boolean doWxReplyVoiceEnd(Action<Bundle> action) {
        mForeSend = false;
        TXZConfigManager.getInstance().enableWakeup(true);
        int voiceLength = action.getData().getInt("length");
        if (TextUtils.isEmpty(mVoiceToUserId) && !AppLogic.isRecordWinShowing() && !StatusUtil.isCallBusy()) {
            if (!Config.SupportNewRecord) {
                TtsActionCreator.get().insertTts("RS_VOICE_WEBCHAT_SEND_MSG_CANCEL", "录音已取消", false, null);
                mVoiceToUserId = "";
            }
        } else {
            if (voiceLength == 0) {
                mReplying = false;
                bReplyCancelled = true;
                TtsActionCreator.get().insertTts("RS_VOICE_WEBCHAT_SEND_MSG_NO_SPEAK", "您没有说话，录音已取消", true, null);
            } else if (voiceLength < 1000) {
                mReplying = false;
                bReplyCancelled = true;
                TtsActionCreator.get().insertTts("RS_VOICE_WEBCHAT_SEND_MSG_SPEAK_TOO_SHORT", "说话时间太短", true, null);
                Toast.makeText(AppLogic.getApp(), "说话时间太短！", Toast.LENGTH_SHORT).show();
            } else {
                // 上报发送
                if (!mManuallySend) {
                    TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_VOICE_RECORD_COMPLETE);
                }
                if (Config.SupportNewRecord) {
                    UploadVoiceActionCreator.get().notifyUploadVoice();
                } else {
                    UploadVoiceActionCreator.get().uploadVoice(getRecordCachePath(), mLastSpeedLength);
                }
                AppLogic.removeBackGroundCallback(mResetCountDownTask);
                mRecorderCountdown = 0;
            }
        }
        mManuallySend = false;

        return true;
    }

    private boolean doWxReplyVoiceParse(Action<Bundle> action) {
        int voiceLength = action.getData().getInt("length");
        String text = action.getData().getString("txt");
        final String voiceUrl = action.getData().getString("url");
        UploadVoiceActionCreator.get().notifyUploadVoiceSucc(text, voiceUrl, voiceLength);
        return false;
    }

    private boolean doWxReplyVoiceMute(Action<Bundle> action) {
        int time = action.getData().getInt("mute");
        int seconds = 3 - (time / 1000);
        // 小于临界值时重刷，57秒允许3/2/1，58秒允许2/1，59秒允许1
        if (mForeSend && seconds <= mCurCountDown) {
            L.d("TXZRecordStore", "record voice on Mute Force : " + time + ", " + seconds);
            AppLogic.removeUiGroundCallback(mForeSendTask);
            mForeSendTask.update(seconds);
            AppLogic.runOnUiGround(mForeSendTask, 0);
        } else {
            L.d("TXZRecordStore", "record voice on Mute: " + time + ", " + seconds);
            mRecorderCountdown = seconds;
            AppLogic.removeBackGroundCallback(mResetCountDownTask);
            AppLogic.runOnBackGround(mResetCountDownTask, 1700);
        }
        return true;
    }

    // 重置倒计时Task， 防止静音回调丢失导致倒计时卡在某个位置
    private Runnable mResetCountDownTask = new Runnable() {
        @Override
        public void run() {
            if (mReplying && !mSending) {
                L.i("TXZRecordStore", "record voice count down stucked! reset to 3");
                mRecorderCountdown = 3;
                emitChange(EVENT_TYPE_ALL);
            }
        }
    };

    private boolean doWxReplyVoiceError(Action<Bundle> action) {
        int code = action.getData().getInt("err");
        if (code == 500) {
            UploadVoiceActionCreator.get().notifyUploadVoiceError("server error");
            return false;
        }
        mReplying = false;
        //mSending = false;
        mForeSend = false;
        mVoiceToUserId = "";
        if (!TextUtils.isEmpty(mVoiceToUserId) && Config.SupportNewRecord) {
            TtsActionCreator.get().insertTts("RS_VOICE_WEBCHAT_SEND_MSG_FAILED_NEW", "消息发送失败", false, null);
        }
        return true;
    }

    private boolean doWxReplyVoiceCancel(Action<Bundle> action) {
        mReplying = false;
        bReplyCancelled = true;
        boolean manual = action.getData().getBoolean("manual");
        boolean mute = action.getData().getBoolean("mute", false);
        mVoiceToUserId = "";
        if (manual) {
            mLastUploadSessionToken = mCurUploadSessionToken;
        } else {
            mForeSend = false;
            if (!Config.SupportNewRecord && !mute) {
                procRecordCancel();
            }
            TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_VOICE_RECORD_CANCEL);
        }
        if (Config.SupportNewRecord && !mute) {
            procRecordCancel();
        }
        return true;
    }

    private void procRecordCancel() {
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                if (!AppLogic.isRecordWinShowing()) {
                    TtsActionCreator.get().insertTts("RS_VOICE_WEBCHAT_SEND_MSG_CANCEL", "录音已取消", false, null);
                }
            }
        }, 200);
    }

    Runnable mUploadRespErrorDelay = new Runnable() {
        @Override
        public void run() {
            UploadVoiceActionCreator.get().notifyUploadVoiceError("REQ_TIMEOUT");
        }
    };

    private boolean doVoiceUploadReq(Action<Bundle> action) {
        mCurUploadSessionToken = action.getData().getLong("id");
        mSending = true;
        mSendError = true;
        if (Config.SupportNewRecord) {
            AppLogic.removeUiGroundCallback(mUploadRespErrorDelay);
            AppLogic.runOnUiGround(mUploadRespErrorDelay, 1000 * 15);
        }
        return true;
    }

    private String mLastSendMsgToken; // 上次发送的消息token
    private String mLastSendMsgUrl; // 上次发送的语音url

    private boolean doVoiceUploadResp(Action<Bundle> action) {
        if (Config.SupportNewRecord) {
            AppLogic.removeUiGroundCallback(mUploadRespErrorDelay);
        }

        mLastSendMsgUrl = action.getData().getString("url");
        final String txt = action.getData().getString("txt");
        int voiceLength = action.getData().getInt("length");
        if (TextUtils.isEmpty(mLastSendMsgUrl)) {
            UploadVoiceActionCreator.get().notifyUploadVoiceError("服务器异常请稍后再试");
            return true;
        }
        if (mLastUploadSessionToken == mCurUploadSessionToken) {
            L.i("TXZRecordStore", "doVoiceUploadResp mLastUploadSessionToken == mCurUploadSessionToken");
            return false;
        }
        mSending = false;
        mLastUploadSessionToken = mCurUploadSessionToken;
        if (WxContactStore.getInstance().getLoginUser() != null) {
            String content;
            if (Config.SupportNewRecord && !TextUtils.isEmpty(txt)) {
                content = txt.replaceAll("完毕，完毕。", "").replaceAll("完毕完毕", "");
            } else {
                content = "我正在使用微信助手给你发送语音: " + mLastSendMsgUrl;
            }

            mLastSendMsgToken = MessageActionCreator.get().sendVoiceMsg(mVoiceToUserId, content,
                    voiceLength);
        }
        mRecorderCountdown = 0;
        return true;
    }

    private boolean doVoiceUploadError(Action<String> action) {
        WxMonitorUtil.doMonitor(WxMonitorUtil.WX_VOICE_UPLOAD_FAILED);
        if (Config.SupportNewRecord) {
            AppLogic.removeUiGroundCallback(mUploadRespErrorDelay);
        }

        if (mLastUploadSessionToken == mCurUploadSessionToken) {
            L.i("TXZRecordStore", "doVoiceUploadError mLastUploadSessionToken == mCurUploadSessionToken");
            return false;
        }
        if (!mFirstTimeUploadError || Config.SupportNewRecord) {
            mLastUploadSessionToken = mCurUploadSessionToken;
        }
        String desc = action.getData();
        L.d("TXZRecordStore", "onUploadVoiceError " + desc);
        if (mFirstTimeUploadError && !Config.SupportNewRecord) {
            mFirstTimeUploadError = false;
            TtsUtil.speakResource("RS_VOICE_WEBCHAT_SEND_MSG_FAILED", "消息发送失败，即将为您重试", new TtsUtil.ITtsCallback() {
                @Override
                public void onEnd() {
                    JSONObject req = new JSONObject();
                    try {
                        req.put("path", getRecordCachePath());
                        req.put("length", mLastSpeedLength);
                        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "wx.upload.voice",
                                req.toString().getBytes(), null);
                    } catch (JSONException e) {
                        L.e("TXZRecordStore", "retry send msg encountered error: " + e.toString());
                    }
                    mRecorderCountdown = 0;
                    emitChange(EVENT_TYPE_ALL);
                }
            });
        } else {
            if (!Config.SupportNewRecord) {
                mFirstTimeUploadError = true;
            }
            TtsUtil.speakResource("RS_VOICE_WEBCHAT_SEND_MSG_FAILED_NEW", "消息发送失败，请稍后再试", new TtsUtil.ITtsCallback() {
                @Override
                public void onEnd() {
                    emitChange(EVENT_TYPE_ALL);
                }
            });

            mReplying = false;
            mVoiceToUserId = "";
        }
        mSending = false;
        mSendError = true;
        return true;
    }

    private boolean doWxSendMsgResp(Action<WxMessage> action) {
        mSendError = false;

        final WxMessage msg = action.getData();
        final String token = msg.mMsgId + "";

        if (!TextUtils.isEmpty(mLastSendMsgToken) && token.equals(mLastSendMsgToken)) {
            if (!TextUtils.isEmpty(mVoiceToUserId)) {
                if (mVoiceToUserId.equals(mLastToUserId) && HelpUtil.REPEAT_TIP_ENABLED) {
                    TtsActionCreator.get().insertTts("RS_VOICE_WEBCHAT_SEND_MSG_SUCCESS_WITH_HINT", "消息发送成功，如需再发一条，请说继续发送", false, mTtsCallback);
                    HelpUtil.procRepeatTipPlayed();
                } else {
                    TtsActionCreator.get().insertTts("RS_VOICE_WEBCHAT_SEND_MSG_SUCCESS", "消息发送成功", false, mTtsCallback);
                }
            }

            mLastToUserId = mVoiceToUserId;
            // 记录最近交互联系人
            UIHandler.getInstance().updateLastSendToUserId(mVoiceToUserId);
            mReplying = false;
            mVoiceToUserId = "";
            // 拷贝缓存文件
            ThreadManager.getPool().execute(new Runnable() {
                @Override
                public void run() {
                    FileInputStream is = null;
                    FileOutputStream os = null;
                    try {
                        File from = new File(getRecordCachePath());
                        File out = new File(Constant.PATH_MSG_VOICE_CACHE_SELF + token);
                        out.getParentFile().mkdir();
                        if (from.exists()) {
                            is = new FileInputStream(from);
                            os = new FileOutputStream(out);
                            byte[] buff = new byte[1024];
                            int hasRead = -1;
                            while ((hasRead = is.read(buff)) > 0) {
                                os.write(buff, 0, hasRead);
                            }

                            msg.mVoiceCachePath = out.getPath();

                            // 删除录音缓存文件
                            from.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }

        ContactActionCreator.get().topSession(msg.mSessionId);

        return true;
    }

    private boolean doWxSendMsgErr(Action<WxMessage> action) {
        WxMessage msg = action.getData();
        final String token = msg.mMsgId + "";

        if (!TextUtils.isEmpty(mLastSendMsgToken) && token.equals(mLastSendMsgToken)) {
            TtsUtil.speakResource("RS_VOICE_WEBCHAT_SEND_MSG_FAILED_NEW", "消息发送失败，请稍后再试", new TtsUtil.ITtsCallback() {
                @Override
                public void onEnd() {
                    mReplying = false;
                    mVoiceToUserId = "";
                    emitChange(EVENT_TYPE_ALL);
                }
            });

            mSending = false;
            mSendError = true;

            return true;
        }

        return false;
    }

    TtsUtil.ITtsCallback mTtsCallback = new TtsUtil.ITtsCallback() {
        @Override
        public void onSuccess() {
            regWakeupReply(mLastToUserId);
        }
    };

    private Runnable mUnregisterWakeupReplyTask = new Runnable() {
        @Override
        public void run() {
            // 反注册“继续发送”的唤醒词
            L.d("unregistering wakeup reply");
            AsrUtil.recoverWakeupFromAsr("REPEAT_REPLY_WX");
        }
    };

    public void regWakeupReply(final String toUser) {
        L.d("registering wakeup reply");
        AsrUtil.useWakeupAsAsr(new AsrUtil.AsrComplexSelectCallback() {
            @Override
            public void onCommandSelected(String type, final String command) {
                if (type.equals("CMD_REPEAT_REPLY")) {

                    TXZTtsStore.getInstance().procWakeupReply(toUser);

                    TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_VOICE_CONTINUE_SEND);

                    // 用户学会“继续发送” 关闭提示
                    if (HelpUtil.REPEAT_TIP_ENABLED) {
                        HelpUtil.disableRepeatTip();
                    }
                }
            }

            @Override
            public boolean needAsrState() {
                return true;
            }

            @Override
            public String getTaskId() {
                return "REPEAT_REPLY_WX";
            }
        }.addCommand("CMD_REPEAT_REPLY", "继续发送", "再发一条"));

        AppLogic.removeUiGroundCallback(mUnregisterWakeupReplyTask);
        AppLogic.runOnUiGround(mUnregisterWakeupReplyTask, 6000);
    }

    private String getRecordCachePath() {
        return Config.SupportNewRecord ? Constant.PATH_RECORD_CACHE : Constant.PATH_RECORD_CACHE_OLD;
    }

    private boolean mReplying = false;
    private boolean mSending = false;
    private boolean mSendError = false;
    private int mRecorderCountdown = 3;
    private String mLastToUserId = ""; // 上次发送的目标用户id
    private boolean mEnableReply = true;
    private boolean bReplyCancelled = false;

    private boolean mFirstTimeUploadError = true;
    private long mCurUploadSessionToken = -1;
    private long mLastUploadSessionToken = -1;
    private int mLastSpeedLength = 0;

    private void reset() {
        mReplying = false;
        mSending = false;
        mSendError = false;
        mRecorderCountdown = 3;
        mLastToUserId = "";
        mEnableReply = true;
        bReplyCancelled = false;
    }

    /*
        是否处于回复状态中
     */
    public boolean isReplying() {
        return mReplying;
    }

    /*
        是否处于发送中
     */
    public boolean isSending() {
        return mSending;
    }

    /*
        是否处于发送失败
     */
    public boolean isSendError() {
        return mSendError;
    }

    /**
     * 是否已取消发送
     * @return
     */
    public boolean isReplyCancelled() {
        return bReplyCancelled;
    }

    /*
        获取发送倒计时
     */
    public int getCountdown() {
        return mRecorderCountdown;
    }

    /*
        获取当前的发送目标
     */
    public String getCurVoiceToUser() {
        return mVoiceToUserId;
    }

    /**
     * 获取上次的发送目标
     *
     * @return
     */
    public String getLastVoiceToUser() {
        return mLastToUserId;
    }

    public static final String EVENT_TYPE_ALL = "txz_record_store";
}
