package com.txznet.webchat.stores;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.StatusUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZCameraManager;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.bean.Poi;
import com.txznet.webchat.AppStatus;
import com.txznet.webchat.Constant;
import com.txznet.webchat.R;
import com.txznet.webchat.RecordStatusObservable;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.actions.AppStatusActionCreator;
import com.txznet.webchat.actions.ContactActionCreator;
import com.txznet.webchat.actions.MessageActionCreator;
import com.txznet.webchat.actions.ResourceActionCreator;
import com.txznet.webchat.actions.TXZReportActionCreator;
import com.txznet.webchat.actions.TtsActionCreator;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.helper.WxStatusHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.ReportMessage;
import com.txznet.webchat.model.TtsModel;
import com.txznet.webchat.ui.common.widget.FileDownloadDialog;
import com.txznet.webchat.util.FileUtil;
import com.txznet.webchat.util.HelpUtil;
import com.txznet.webchat.util.SmileyParser;
import com.txznet.webchat.util.SoundManager;
import com.txznet.webchat.util.WxHelpGuideManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责Tts播报和队列管理
 * Created by J on 2016/11/30.
 */

public class TXZTtsStore extends Store {
    public static final String EVENT_TYPE_ALL = "txz_tts_store";
    private static final String LOG_TAG = "TxzTtsStore";

    private static final int LONG_MSG_LENGTH_THRESHOLD = 100; // 长文本消息长度阈值
    private static final int LONG_MSG_CUT_THRESHOLD = 50; // 长文本消息截断长度

    private static TXZTtsStore sInstance = new TXZTtsStore();

    private LinkedList<TtsModel> mListTtsQueue = new LinkedList<>(); // tts队列
    private LinkedList<TtsModel> mListTtsInsertQueue = new LinkedList<>(); // tts插队队列

    private int mCurTtsId;
    private TtsModel mCurTtsModel;
    private TtsModel mProcessingModel; // 正在下载音频的model
    private WxMessage mLastBroadMsg; // 最后播报的消息

    private long mBroadcastingMsg = -1;

    private long mLastTtsBeginTime = 0;

    // tts状态标记
    private boolean bBusyTts; // 正在播报tts
    private boolean bBusyWxTts; // 正在播报微信内部Tts
    private boolean bRecordWinShowing; // 声控界面打开
    private boolean bBusyDownloadingVoice; // 等待语音下载完成
    private boolean bBusyAsr; // 等待唤醒任务结束
    private boolean bBusyCall; // 等待电话结束
    private boolean bSleepMode; // 休眠中不进行消息播报
    private boolean bWaitingRecordEnd = false;
    private boolean bWaitingReplyAsr;

    private String mOpenedChatId; // 当前打开的聊天界面对应的联系人id, 未打开聊天界面时为空

    // 媒体焦点flag
    private boolean mIsMediaFocusGained; // 媒体焦点是否被占用
    private static final int MEDIA_FOCUS_TIMEOUT = 10000; // 媒体焦点占用超时
    private long mLastMediaFocusTime; // 上次媒体焦点被占用时间

    private Runnable mProcTtsQueueTask = new Runnable() {
        @Override
        public void run() {
            TtsActionCreator.get().procTtsQueue();
        }
    };

    private Runnable mReleaseSessionLockTask = new Runnable() {
        @Override
        public void run() {
            bBusyWxTts = false;
            bWaitingReplyAsr = false;
            MessageActionCreator.get().cancelNotify();
            // 反注册“回复微信”的唤醒词
            AsrUtil.recoverWakeupFromAsr("REPLY_WX");
            AsrUtil.recoverWakeupFromAsr("REPLY_LONG_MSG");
            WxStatusHelper.getInstance().notifyChatModeStatusChanged(false);
            WxHelpGuideManager.getInstance()
                    .hideHelpGuide(WxHelpGuideManager.GuideScene.RECEIVE_MESSAGE);
            TtsActionCreator.get().procTtsQueue();
        }
    };

    private Runnable mDownloadVoiceTimeoutTask = new Runnable() {
        @Override
        public void run() {
            bBusyDownloadingVoice = false;
            mListTtsInsertQueue.addFirst(mProcessingModel);
            mProcessingModel = null;
            TtsActionCreator.get().procTtsQueue();
        }
    };

    @Override
    public void onDispatch(Action action) {
        boolean changed = false;
        switch (action.getType()) {
            case ActionType.WX_PLUGIN_LOGIC_RESET:
                reset();
                break;

            case ActionType.TXZ_TTS_QUEUE_ADD:
                changed = doTtsQueueAdd((TtsModel) action.getData());
                break;

            case ActionType.TXZ_TTS_QUEUE_INSERT:
                changed = doTtsQueueInsert((TtsModel) action.getData());
                break;

            case ActionType.TXZ_TTS_QUEUE_PROC:
                changed = doTtsQueueProc();
                break;

            case ActionType.WX_REPEAT_MSG:
                changed = doRepeatMessage((WxMessage) action.getData());
                break;

            case ActionType.WX_SKIP_MSG:
                changed = doSkipMessage();
                break;

            case ActionType.WX_MEDIA_FOCUS_CHANGED:
                mIsMediaFocusGained = (boolean) action.getData();

                if (mIsMediaFocusGained) {
                    mLastMediaFocusTime = System.currentTimeMillis();
                } else {
                    TtsActionCreator.get().procTtsQueue();
                }

                break;

            case ActionType.WX_DOWNLOAD_VOICE_RESP:
                changed = doVoiceDownloadResp((WxMessage) action.getData());
                break;

            case ActionType.WX_DOWNLOAD_VOICE_RESP_ERROR:
                changed = doVoiceDownloadError((WxMessage) action.getData());
                break;

            case ActionType.WX_SEND_MSG_RESP:
                doWxSendMsgResp(action);
                break;

            case ActionType.WX_REPLY_VOICE_START:
                mBroadcastingMsg = -1;
                bWaitingRecordEnd = true;
                break;

            case ActionType.WX_REPLY_VOICE_CANCEL:
            case ActionType.WX_REPLY_VOICE_ERROR:
            case ActionType.VOICE_UPLOAD_RESP_ERROR:
            case ActionType.VOICE_UPLOAD_RESP:
                bWaitingRecordEnd = false;
                AppLogic.removeBackGroundCallback(mProcTtsQueueTask);
                AppLogic.runOnBackGround(mProcTtsQueueTask, 500);
                break;

            case ActionType.SYSTEM_POWER_SLEEP:
                doSleep();
                break;

            case ActionType.SYSTEM_POWER_WAKEUP:
                doWakeUp();
                break;

            case ActionType.WX_SKIP_REPEAT_MSG:
                changed = doSKipRepeatMessage();
                break;

            case ActionType.SYSTEM_POWER_REVERSE_ENTER:
                doReverseEnter();
                break;

            case ActionType.SYSTEM_POWER_REVERSE_EXIT:
                doReverseExit();
                break;

            case ActionType.WX_OPEN_CHAT:
                doOpenChat((Bundle) action.getData());
                break;

            case ActionType.WX_CLOSE_CHAT:
                doCloseChat((Bundle) action.getData());
                break;
        }

        if (changed) {
            emitChange(EVENT_TYPE_ALL);
        }
    }

    private void doOpenChat(Bundle bundle) {
        String uid = bundle.getString("uid");

        if (TextUtils.isEmpty(uid)) {
            mOpenedChatId = "";
            return;
        }

        mOpenedChatId = uid;
    }

    private void doCloseChat(Bundle bundle) {
        String uid = bundle.getString("uid");
        mOpenedChatId = "";
    }

    private void doReverseEnter() {
        if (cancelCurrentTts()) {
            Toast.makeText(GlobalContext.get(), R.string.lb_toast_tts_disable_reverse,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void doReverseExit() {
        // 取消可能存在的静音唤醒
        // 避免插了静音唤醒的情况下进入倒车, 某些版本的Core内逻辑保护导致取消唤醒的远程调用被block, 退出倒车时
        // 未成功取消的静音唤醒又被恢复从而导致唤醒任务一直持续的问题
        AsrUtil.recoverWakeupFromAsr("REPLY_WX");
        AsrUtil.recoverWakeupFromAsr("REPLY_LONG_MSG");

        // 退出倒车状态时先更新下StatusUtil的相关状态, 避免因倒车状态变化导致的部分状态位过期, tts播报被卡住
        // 的问题
        StatusUtil.getStatus(new StatusUtil.GetStatusCallback() {
            @Override
            public void onGet() {
                bBusyAsr = StatusUtil.isAsrBusy();
                bBusyCall = StatusUtil.isCallBusy();
                bBusyTts = StatusUtil.isTtsBusy();

                L.d(LOG_TAG, String.format("status after reverse exit: isBusyAsr = %s, isBusyCall" +
                        " = %s, isBusyTts = %s", isBusyAsr, isBusyCall, isBusyTts));

                // 倒车退出后延迟3s再次触发下消息播报, 避免某些状态恢复延迟导致的问题
                AppLogic.runOnBackGround(new Runnable() {
                    @Override
                    public void run() {
                        TtsActionCreator.get().procTtsQueue();
                    }
                }, 3000);
            }
        });
    }

    public WxMessage getLastBroadMsg() {
        return mLastBroadMsg;
    }

    private void doSleep() {
        bSleepMode = true;

        cancelCurrentTts();
        AppLogic.removeBackGroundCallback(mProcTtsQueueTask);
        MessageActionCreator.get().cancelNotify();
    }

    private void doWakeUp() {
        bSleepMode = false;

        AppLogic.removeBackGroundCallback(mProcTtsQueueTask);
        AppLogic.runOnBackGround(mProcTtsQueueTask, 1000);
    }

    public static TXZTtsStore getInstance() {
        return sInstance;
    }

    private TXZTtsStore() {
        super(Dispatcher.get());

        StatusUtil.addStatusListener(new StatusUtil.StatusListener() {
            @Override
            public void onBeginAsr() {
                L.d(LOG_TAG, "StatusListener::onBeginAsr");
                bBusyAsr = true;
            }

            @Override
            public void onEndAsr() {
                L.d(LOG_TAG, "StatusListener::onEndAsr");
                bBusyAsr = false;
                AppLogic.removeBackGroundCallback(mProcTtsQueueTask);
                AppLogic.runOnBackGround(mProcTtsQueueTask, 500);
            }

            @Override
            public void onBeginTts() {
                L.d(LOG_TAG, "StatusListener::onBeginTts");
                bBusyTts = true;
            }

            @Override
            public void onEndTts() {
                L.d(LOG_TAG, "StatusListener::onEndTts");
                bBusyTts = false;
                AppLogic.removeBackGroundCallback(mProcTtsQueueTask);
                AppLogic.runOnBackGround(mProcTtsQueueTask, 500);
            }

            @Override
            public void onBeginCall() {
                L.d(LOG_TAG, "StatusListener::onBeginCall");
                bBusyCall = true;
                MessageActionCreator.get().cancelReply(true, true);
            }

            @Override
            public void onEndCall() {
                L.d(LOG_TAG, "StatusListener::onEndCall");
                bBusyCall = false;
                AppLogic.removeBackGroundCallback(mProcTtsQueueTask);
                AppLogic.runOnBackGround(mProcTtsQueueTask, 500);
            }

            @Override
            public void onMusicPlay() {
                L.d(LOG_TAG, "StatusListener::onMusicPlay");
            }

            @Override
            public void onMusicPause() {
                L.d(LOG_TAG, "StatusListener::onMusicPause");
            }

            @Override
            public void onBeepEnd() {
                L.d(LOG_TAG, "StatusListener::onBeepEnd");
            }
        });

        AppLogic.registerRecordStatusObserver(new RecordStatusObservable.StatusObserver() {
            @Override
            public void onStatusChanged(boolean isShowing) {
                if (isShowing) {
                    AppLogic.removeBackGroundCallback(mReleaseRecordWinLockTask);
                    bRecordWinShowing = true;
                    //MessageActionCreator.get().cancelReply(true, true);
                    TXZAsrManager.getInstance().recoverWakeupFromAsr("SPEAK_LONG_TEXT");
                    TXZAsrManager.getInstance().recoverWakeupFromAsr("REPLY_WX");
                    MessageActionCreator.get().cancelNotify();
                } else {
                    AppLogic.runOnBackGround(mReleaseRecordWinLockTask, 1000);
                }
            }
        });
    }

    private Runnable mReleaseRecordWinLockTask = new Runnable() {
        @Override
        public void run() {
            bRecordWinShowing = false;
            TtsActionCreator.get().procTtsQueue();
        }
    };

    public long getBroadcastingMessage() {
        return mBroadcastingMsg;
    }

    private void reset() {
        bBusyTts = false;
        bBusyWxTts = false;
        bRecordWinShowing = false;
        bBusyDownloadingVoice = false;
        bBusyAsr = false;
        bBusyCall = false;
        bWaitingReplyAsr = false;
        bWaitingRecordEnd = false;

        mListTtsInsertQueue.clear();
        mListTtsQueue.clear();

        mCurTtsId = TtsUtil.INVALID_TTS_TASK_ID;
        mCurTtsModel = null;
        mProcessingModel = null;
        mLastBroadMsg = null;

        MessageActionCreator.get().cancelNotify();
    }

    private boolean doTtsQueueAdd(TtsModel model) {
        mListTtsQueue.add(model);
        return false;
    }

    private boolean doTtsQueueInsert(TtsModel model) {
        // 插队tts消息直接进行播报,不进行入队操作
        if (null == model.message) {
            if (model.force) {
                mCurTtsId = TtsUtil.speakResource(TtsUtil.DEFAULT_STREAM_TYPE, model.resId,
                        model.resArgs, model.text, TtsUtil.PreemptType.PREEMPT_TYPE_NEXT,
                        model.callback);
            } else {
                mCurTtsId = TtsUtil.speakResource(model.resId, model.resArgs, model.text,
                        model.callback);
            }
        } else {
            // 消息播报插到insertQueue
            mListTtsInsertQueue.addFirst(model);
            doTtsQueueProc();
        }


        return false;
    }

    private boolean doTtsQueueProc() {
        // 判断插队队列中是否有待播报消息
        while (!mListTtsInsertQueue.isEmpty()) {
            if (checkShoudProc(mListTtsInsertQueue.get(0))) {
                procTtsModel(mListTtsInsertQueue.remove(0), true);
                return true;
            }

            mListTtsInsertQueue.remove(0);
        }

        if (null != mCurTtsModel && null != mCurTtsModel.message) {
            // 上一条tts是消息播报，优先播报消息队列中来自同一会话的其他消息
            for (int i = 0; i < mListTtsQueue.size(); i++) {
                TtsModel model = mListTtsQueue.get(i);
                WxMessage msg = model.message;

                if (null != msg && msg.mSessionId.equals(mCurTtsModel.message.mSessionId)) {

                    if (checkShoudProc(model)) {
                        procTtsModel(mListTtsQueue.remove(i), false);
                        return true;
                    }

                    mListTtsQueue.remove(i);

                }
            }
        }

        // 处理tts队列
        while (!mListTtsQueue.isEmpty()) {
            if (checkShoudProc(mListTtsQueue.get(0))) {
                procTtsModel(mListTtsQueue.remove(0), false);
                return true;
            }

            mListTtsQueue.remove(0);
        }

        return false;
    }

    private boolean doVoiceDownloadResp(WxMessage message) {
        if (null != mProcessingModel && message.mMsgId == mProcessingModel.message.mMsgId) {
            bBusyDownloadingVoice = false;
            mProcessingModel.message.mVoiceCachePath = message.mVoiceCachePath;
            procTtsModel(mProcessingModel, true);
            mProcessingModel = null;

            return true;
        }

        return false;
    }

    private boolean doVoiceDownloadError(WxMessage message) {
        if (null != mProcessingModel && message.mMsgId == mProcessingModel.message.mMsgId) {
            bBusyDownloadingVoice = false;
            procTtsModel(mProcessingModel, true);
            mProcessingModel = null;

            return true;
        }

        return false;
    }

    /**
     * 判断指定ttsmodel是否需要播报
     *
     * @param model
     * @return
     */
    private boolean checkShoudProc(TtsModel model) {
        if (null == model) {
            return false;
        }

        // 非消息语音直接播报
        if (null == model.message) {
            return true;
        }

        // 自动播报关闭不播报消息
        if (!AppStatusStore.get().isAutoBroadEnabled()) {
            return false;
        }

        // 联系人被屏蔽,不进行播报
        if (WxContactStore.getInstance().isContactBlocked(model.message.mSessionId)) {
            return false;
        }

        return true;
    }

    private void procTtsModel(TtsModel model, boolean fromInsertQueue) {
        // 检查是否需要下载语音文件
        if (null != model.message && WxMessage.MSG_TYPE_VOICE == model.message.mMsgType) {
            WxMessage msg = model.message;

            // 本地缓存路径为空, 若消息来自插队队列说明语音下载已失败,来自tts队列说明语音还未下载
            if (TextUtils.isEmpty(msg.mVoiceCachePath) && !fromInsertQueue) {
                // 启动下载语音任务, 3秒未下载成功视为超时
                if (null == mProcessingModel) {
                    bBusyDownloadingVoice = true;
                    mProcessingModel = model;
                    AppLogic.removeBackGroundCallback(mDownloadVoiceTimeoutTask);
                    AppLogic.runOnBackGround(mDownloadVoiceTimeoutTask, 4000);
                }

                ResourceActionCreator.get().downloadVoice(msg);
                return;
            }
        }

        // 判断媒体焦点是否被占用
        if (mIsMediaFocusGained) {
            // 若媒体焦点被占用时间超过超时限制，强制进行播报
            if ((System.currentTimeMillis() - mLastMediaFocusTime) < MEDIA_FOCUS_TIMEOUT) {
                L.d("procTtsQueue busy, waiting for media focus");
                if (fromInsertQueue) {
                    mListTtsInsertQueue.addFirst(model);
                } else {
                    mListTtsQueue.addFirst(model);
                }
                //mListTtsInsertQueue.add(model);

                return;
            } else {
                mIsMediaFocusGained = false;
            }
        }

        if (checkProcStatus()) {
            if (fromInsertQueue) {
                mListTtsInsertQueue.addFirst(model);
            } else {
                mListTtsQueue.addFirst(model);
            }

            return;
        }

        // 判断是否是已下载完成的文件消息
        if (null != model.message && WxMessage.MSG_TYPE_FILE == model.message.mMsgType) {
            // 播报下载成功的和失败的文件消息
            if (ResourceActionCreator.get().isFileDownloaded(model.message)
                    || ResourceActionCreator.get().isFileDownloadFailed(model.message)) {
                FileDownloadDialog.getInstance().updateMessage(model.message).show();
                return;
            }
        }

        model.text = getTtsText(model);
        mCurTtsModel = model;
        bBusyWxTts = true;
        mLastTtsBeginTime = System.currentTimeMillis();

        // 若是语音消息且语音已下载成功, 对语音进行播报
        if (null != model.message
                && WxMessage.MSG_TYPE_VOICE == model.message.mMsgType
                && !TextUtils.isEmpty(model.message.mVoiceCachePath)) {
            mCurTtsId = TtsUtil.speakVoice(model.text, model.message.mVoiceCachePath, mTtsCallback);
        } else {
            // 判断是否需要按资源播报
            if (!TextUtils.isEmpty(model.resId)) {
                mCurTtsId = TtsUtil.speakResource(model.resId, model.resArgs, model.text,
                        mTtsCallback);
            } else {
                mCurTtsId = TtsUtil.speakText(model.text, mTtsCallback);
            }
        }

        if (null != model.message) {
            mBroadcastingMsg = model.message.mMsgId;
            mLastBroadMsg = model.message;
            emitChange(EVENT_TYPE_ALL);
        }

        // 判断是否需要显示Notification
        if (null != mCurTtsModel && null != mCurTtsModel.message) {
            MessageActionCreator.get().showNotify(mCurTtsModel.message.mSessionId,
                    mCurTtsModel.message.mMsgId + "");
        }
    }

    private boolean checkProcStatus() {
        boolean stat = false;
        StringBuilder sb = new StringBuilder("procTtsQueue busy: ");
        if (bBusyAsr) {
            stat = true;
            sb.append("bBusyAsr, ");
        }

        if (bBusyTts) {
            stat = true;
            sb.append("bBusyTts, ");
        }

        if (bBusyCall) {
            stat = true;
            sb.append("bBusyCall, ");
        }

        if (bRecordWinShowing) {
            stat = true;
            sb.append("bRecordWinShowing, ");
        }

        if (bWaitingReplyAsr) {
            stat = true;
            sb.append("bWaitingReplyAsr, ");
        }

        if (bBusyDownloadingVoice) {
            stat = true;
            sb.append("bBusyDownloadingVoice, ");
        }

        if (bWaitingRecordEnd) {
            stat = true;
            sb.append("bWaitingRecordEnd, ");
        }

        if (bSleepMode) {
            stat = true;
            sb.append("bSleepMode, ");
        }

        if (AppStatus.getInstance().isReverseMode()) {
            stat = true;
            sb.append("ReverseMode, ");
        }

        if (FileDownloadDialog.getInstance().isShowing()) {
            stat = true;
            sb.append("fileDownloadDialogShowing, ");
        }

        if (stat) {
            L.d(LOG_TAG, sb.toString());
        }

        return stat;
    }

    private void procLongMsg(TtsModel model) {
        MessageActionCreator.get().showNotify(mCurTtsModel.message.mSessionId,
                mCurTtsModel.message.mMsgId + "");
        mCurTtsId = TtsUtil.speakText(model.message.mContent.substring(50), mReplyCallback);

        mBroadcastingMsg = model.message.mMsgId;
        emitChange(EVENT_TYPE_ALL);
    }

    private TtsUtil.ITtsCallback mTtsCallback = new TtsUtil.ITtsCallback() {
        @Override
        public void onCancel() {
            super.onCancel();
            bBusyWxTts = false;
            MessageActionCreator.get().cancelNotify();
        }

        @Override
        public void onSuccess() {
            super.onSuccess();

            // dismiss notify
            if (null != mCurTtsModel && null != mCurTtsModel.message) {
                MessageActionCreator.get().dismissNotify(mCurTtsModel.message.mSessionId,
                        mCurTtsModel.message.mMsgId + "");
            }

            if (handleMsgByType()) {
                return;
            }

            if (AppStatusStore.get().isAutoBroadEnabled()
                    && !WxContactStore.getInstance().isContactBlocked(mCurTtsModel.message
                    .mSessionId)
                    && isMsgTypeSupportContinuesProc(mCurTtsModel.message.mMsgType)) {
                // 判断tts队列中是否有来自同一联系人的其他消息
                for (int i = 0; i < mListTtsQueue.size(); i++) {
                    TtsModel model = mListTtsQueue.get(i);

                    if (null != model && null != model.message
                            && model.message.mSessionId.equals(mCurTtsModel.message.mSessionId)) {

                        AppLogic.runOnBackGround(new Runnable() {
                            @Override
                            public void run() {
                                TtsActionCreator.get().procTtsQueue();
                            }
                        }, 200);
                        return;
                    }
                }
            }

            // 播报消息提示
            bWaitingReplyAsr = true;
            mCurTtsId = procHelpTip();
        }

        @Override
        public void onEnd() {
            // 清空正在播报的消息
            if (!bSkipNextRepeatMsgCancel) {
                mBroadcastingMsg = -1;
                emitChange(EVENT_TYPE_ALL);
            }
            bSkipNextRepeatMsgCancel = false;
        }
    };

    /**
     * 处理特殊类型的消息播报结束回调
     *
     * @return 是否中断当前回调处理
     */
    private boolean handleMsgByType() {
        // 普通文本tts播报结束直接继续播报其他消息
        if (null == mCurTtsModel || null == mCurTtsModel.message) {
            AppLogic.runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    TtsActionCreator.get().procTtsQueue();
                }
            }, 500);
            return true;
        }

        // 判断是否需要播报长文本提示
        if (isLongTextMsg(mCurTtsModel)) {
            SoundManager.getInstance(GlobalContext.get()).play(2);
            AppLogic.runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    bWaitingReplyAsr = true;
                    mCurTtsId = TtsUtil.speakResource("RS_VOICE_WEBCHAT_HINT_LONG_MSG",
                            "本消息文字过长，将为您自动跳过，如需继续播报请说确定", mLongMsgCallback);
                }
            }, 500);
            return true;
        }

        // 判断是否需要处理文件消息播报
        if (isFileMsg(mCurTtsModel) && WxConfigStore.getInstance().isFileMsgEnabled()) {
            procFileMsgTip(mCurTtsModel.message);
            return true;
        }

        return false;
    }

    /**
     * 判断当前msgType是否支持继续处理来自同一联系人的其他消息
     * <p>
     * 当前唯一不支持连续处理的消息类型是位置分享, 因为需要处理导航相关的指令
     *
     * @return
     */
    private boolean isMsgTypeSupportContinuesProc(int msgType) {
        // 判断当前消息是否是需要中断处理的类型
        if (WxMessage.MSG_TYPE_LOCATION == msgType
                || WxMessage.MSG_TYPE_FILE == msgType) {
            return false;
        }

        return true;
    }

    private boolean isLongTextMsg(TtsModel model) {
        if (null == model || null == model.message) {
            return false;
        }

        if (WxMessage.MSG_TYPE_TEXT != model.message.mMsgType) {
            return false;
        }

        if (!isLongMsgText(model.message.mContent)) {
            return false;
        }

        // 考虑特殊情况, 文字消息可能全部由表情组成, 此时的tts播报为: xxx对你发来一连串表情
        // 这种情况下可能虽然消息内容很长, 但不应该被当做长文本播报处理, 所以此处应排除纯表情
        // 的情况
        return !isFullEmotionContent(model.message.mContent);
    }

    private boolean isLongMsgText(String text) {
        return !TextUtils.isEmpty(text) && text.length() > LONG_MSG_LENGTH_THRESHOLD;
    }

    private boolean isFileMsg(TtsModel model) {
        if (null == model || null == model.message) {
            return false;
        }

        if (WxMessage.MSG_TYPE_FILE == model.message.mMsgType) {
            return true;
        }

        return false;
    }

    private void procFileMsgTip(WxMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(", 文件名为, ");
        sb.append(FileUtil.getFileNameForTts(msg.mFileName));

        // 判断文件大小是否超出限制
        if (WxConfigStore.getInstance().isFileSizeSupported(msg.mFileSize)) {
            // 判断是否是支持的文件类型
            String fileSuffix = msg.mFileName.substring(msg.mFileName.lastIndexOf(".") + 1);
            if (!WxConfigStore.getInstance().isFileSuffixSupported(fileSuffix)) {
                sb.append(", 本设备暂不支持此文件类型, 您可以在手机端查看");
            } else {
                sb.append(String.format(", 大小为%s, 查看文件您可以说, 打开文件",
                        FileUtil.getBroadFileSize(msg.mFileSize)));
            }
        } else {
            sb.append(String.format(", 大小为%s, 文件大小超出限制, 您可以在手机端查看",
                    FileUtil.getBroadFileSize(msg.mFileSize)));
        }


        mCurTtsId = TtsUtil.speakText(sb.toString(), mReplyCallback);
    }

    private int mContinuesMsgCount = 0;

    private int procHelpTip() {
        // 如果设置了强制播报导航消息的语音提示, 优先进行处理
        if (WxConfigStore.getInstance().getLocMsgEnabled()
                && WxConfigStore.getInstance().forceBroadNavTip()
                && WxMessage.MSG_TYPE_LOCATION == mCurTtsModel.message.mMsgType) {
            HelpUtil.procNavTipPlayed();
            return TtsUtil.speakResource("RS_VOICE_WEBCHAT_HELP_TIP_NAV", "您可以说“导航过去”进行导航",
                    mReplyCallback);
        }

        // 优先播报回复微信提示
        if (HelpUtil.REPLY_TIP_ENABLED) {
            HelpUtil.procReplyTipPlayed();
            return TtsUtil.speakResource("RS_VOICE_WEBCHAT_HELP_TIP_REPLY",
                    "您可以说“回复微信”进行回复", mReplyCallback);
        }

        // 若是位置信息，播报导航提示
        if (WxConfigStore.getInstance().getLocMsgEnabled()
                && HelpUtil.NAV_TIP_ENABLED && WxMessage.MSG_TYPE_LOCATION == mCurTtsModel
                .message.mMsgType) {
            HelpUtil.procNavTipPlayed();
            return TtsUtil.speakResource("RS_VOICE_WEBCHAT_HELP_TIP_NAV", "您可以说“导航过去”进行导航",
                    mReplyCallback);
        }

        // 判断是否需要播报屏蔽消息提示
        if (mContinuesMsgCount >= 2 && HelpUtil.MASK_TIP_ENABLED) {
            HelpUtil.procMaskTipPlayed();
            if (WxContact.isGroupOpenId(mCurTtsModel.message.mSessionId)) {
                return TtsUtil.speakResource("RS_VOICE_WEBCHAT_HELP_TIP_FILTER_GROUP",
                        "此群消息过多，您可以说“屏蔽消息”进行屏蔽", mReplyCallback);
            } else {
                return TtsUtil.speakResource("RS_VOICE_WEBCHAT_HELP_TIP_FILTER",
                        "此联系人消息过多，您可以说“屏蔽消息”进行屏蔽", mReplyCallback);
            }
        }

        return TtsUtil.speakText("", mReplyCallback);
    }

    private TtsUtil.ITtsCallback mReplyCallback = new TtsUtil.ITtsCallback() {

        @Override
        public void onSuccess() {
            AppLogic.runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    L.d("mReplyCallback: onSuccess");
                    regWakeupReply();
                    SoundManager.getInstance(AppLogic.getApp()).play(1);
                    AppLogic.removeBackGroundCallback(mReleaseSessionLockTask);
                    AppLogic.runOnBackGround(mReleaseSessionLockTask, 6000);
                }
            }, 0);
        }

        public void onCancel() {
            L.d("mReplyCallback: onCancel");
            bWaitingReplyAsr = false;
            MessageActionCreator.get().cancelNotify();
        }
    };

    // 通知唤醒回复
    private void regWakeupReply() {
        // 长文本消息此处停止下消息播报的动画，避免喊确定继续播报结束后还在播放播报动画
        if (isLongTextMsg(mCurTtsModel)) {
            MessageActionCreator.get().dismissNotify(mCurTtsModel.message.mSessionId,
                    mCurTtsModel.message.mMsgId + "");
        }

        AsrUtil.AsrComplexSelectCallback callback = new AsrUtil.AsrComplexSelectCallback() {
            @Override
            public void onCommandSelected(final String type, final String command) {
                WxStatusHelper.getInstance().notifyChatModeStatusChanged(false);
                // 命中消息相关命令时取消金手指显示
                WxHelpGuideManager.getInstance()
                        .hideHelpGuide(WxHelpGuideManager.GuideScene.RECEIVE_MESSAGE);
                AppLogic.runOnBackGround(new Runnable() {
                    @Override
                    public void run() {
                        if ("CMD_REPLY_WX".equals(type)) {
                            TXZReportActionCreator.getInstance()
                                    .report(ReportMessage.REPORT_VOICE_REPLY);
                            bWaitingReplyAsr = false;
                            procWakeupReply(mCurTtsModel.message.mSessionId);

                            AppLogic.removeBackGroundCallback(mReleaseSessionLockTask);
                            MessageActionCreator.get().cancelNotify();

                            // 用户学会“回复微信”关键词， 关闭提示
                            if (HelpUtil.REPLY_TIP_ENABLED) {
                                HelpUtil.disableReplyTip();
                            }

                        } else if ("CMD_FILTER".equals(type)) {
                            TXZReportActionCreator.getInstance()
                                    .report(ReportMessage.REPORT_VOICE_SHIELD);
                            if (null != mCurTtsModel && null != mCurTtsModel.message) {
                                ContactActionCreator.get()
                                        .filterSpeak(mCurTtsModel.message.mSessionId, false);
                            }

                            bWaitingReplyAsr = false;
                            AppLogic.removeBackGroundCallback(mReleaseSessionLockTask);
                            MessageActionCreator.get().cancelNotify();

                            // 用户学会“屏蔽消息”关键词， 关闭提示
                            if (HelpUtil.MASK_TIP_ENABLED) {
                                HelpUtil.disableMaskTip();
                            }
                        } else if ("CMD_SHARE_LOCATION".equals(type)) {
                            MessageActionCreator.get()
                                    .sendLocationMsg(mCurTtsModel.message.mSessionId);
                            bWaitingReplyAsr = false;
                            AppLogic.removeBackGroundCallback(mReleaseSessionLockTask);
                            MessageActionCreator.get().cancelNotify();
                        } else if ("CMD_NAV_TO".equals(type)) {
                            if (null == mCurTtsModel || null == mCurTtsModel.message) {
                                L.w(LOG_TAG, "nav to with null message");
                                return;
                            }

                            if (WxMessage.MSG_TYPE_LOCATION != mCurTtsModel.message.mMsgType) {
                                L.w(LOG_TAG, "nav to msgType not match");
                                return;
                            }

                            WxMessage message = mCurTtsModel.message;

                            if (TextUtils.isEmpty(message.mAddress)) {
                                L.w(LOG_TAG, "nav to empty address");
                            }

                            final Poi poi = new Poi();
                            poi.setName("地图选点");
                            poi.setGeoinfo(message.mAddress);
                            poi.setLat(message.mLatitude);
                            poi.setLng(message.mLongtitude);
                            TtsUtil.speakResource("RS_VOICE_WEBCHAT_HINT_NAV",
                                    new String[]{"%TAR%", message.mAddress},
                                    "将为您导航到" + message.mAddress, new TtsUtil.ITtsCallback() {
                                        @Override
                                        public void onSuccess() {
                                            TXZNavManager.getInstance().navToLoc(poi);
                                        }
                                    });

                            bWaitingReplyAsr = false;
                            AppLogic.removeBackGroundCallback(mReleaseSessionLockTask);
                            MessageActionCreator.get().cancelNotify();

                            // 用户学会“导航过去”关键词， 关闭提示
                            if (HelpUtil.NAV_TIP_ENABLED) {
                                HelpUtil.disableNavTip();
                            }
                        } else if ("CMD_SHARE_PHOTO".equals(type)) {
                            TXZCameraManager.getInstance().capturePhoto();
                        } else if ("CMD_FILTER_GROUP".equals(type)) {
                            AppStatusActionCreator.get().disableGroupMsgSpeak();
                            TtsUtil.speakResource("RS_VOICE_WEBCHAT_GROUP_SPEAK_DISABLE",
                                    "群消息播报已关闭");

                            bWaitingReplyAsr = false;
                            AppLogic.removeBackGroundCallback(mReleaseSessionLockTask);
                            MessageActionCreator.get().cancelNotify();
                        } else if ("CMD_DOWNLOAD_FILE".equals(type)) {
                            if (null != mCurTtsModel
                                    && null != mCurTtsModel.message
                                    && isFileDownloadSupported(mCurTtsModel.message)) {
                                MessageActionCreator.get().cancelNotify();
                                TtsUtil.speakText("正在为您下载");
                                ResourceActionCreator.get().downloadFile(mCurTtsModel.message);
                                AppLogic.removeBackGroundCallback(mReleaseSessionLockTask);
                            }
                        }
                        // 反注册“回复微信”的唤醒词
                        AsrUtil.recoverWakeupFromAsr("REPLY_WX");
                        AsrUtil.recoverWakeupFromAsr("REPLY_LONG_MSG");
                    }
                }, 0);
            }

            @Override
            public boolean needAsrState() {
                return true;
            }

            @Override
            public int getPriority() {
                return AsrUtil.WKASR_PRIORITY_NO_ASR;
            }

            @Override
            public String getTaskId() {
                return "REPLY_WX";
            }
        };

        // 构造金手指指令列表
        ArrayList<String> guideList = new ArrayList<>();
        guideList.add("回复微信");
        guideList.add("屏蔽消息");

        callback.addCommand("CMD_REPLY_WX", "回复微信")
                .addCommand("CMD_FILTER", "屏蔽消息", "屏蔽屏蔽")
                .addCommand("CMD_SHARE_PHOTO", "分享当前照片", "分享照片")
                .addCommand("CMD_FILTER_GROUP", "屏蔽群消息", "关闭群消息")
                .addCommand("CMD_DOWNLOAD_FILE", "打开文件");

        // 仅在当前是位置消息且启用了位置消息处理的情况下注册导航过去相关指令
        if (WxConfigStore.getInstance().getLocMsgEnabled()
                && WxMessage.MSG_TYPE_LOCATION == mCurTtsModel.message.mMsgType) {
            callback.addCommand("CMD_NAV_TO", "导航过去", "帮我导航", "导航去那里", "导航到那里去",
                    "帮我导航到那里");
            guideList.add(0, "导航过去");
        }

        // 仅在启用了位置分享功能的情况下注册分享位置相关指令
        if (WxConfigStore.getInstance().getLocShareEnabled()) {
            callback.addCommand("CMD_SHARE_LOCATION", "分享当前位置", "分享我的位置", "分享位置");
            guideList.add("分享当前位置");
        }

        AsrUtil.useWakeupAsAsr(callback);

        WxStatusHelper.getInstance().notifyChatModeStatusChanged(true);
        WxHelpGuideManager.getInstance()
                .showHelpGuide(WxHelpGuideManager.GuideScene.RECEIVE_MESSAGE, guideList);
    }

    private boolean isFileDownloadSupported(WxMessage msg) {
        if (!(WxMessage.MSG_TYPE_FILE == msg.mMsgType)) {
            L.d("TXZTtsStore", "file download not supported: message type mismatch");
            return false;
        }

        if (!WxConfigStore.getInstance().isFileSizeSupported(msg.mFileSize)) {
            L.d("TXZTtsStore", "file download not supported: file too large: " + msg.mFileSize);
            return false;
        }

        String suffix = FileUtil.getFileSuffix(msg.mFileName);
        if (!WxConfigStore.getInstance().isFileSuffixSupported(suffix)) {
            L.d("TXZTtsStore", "file download not supported: file suffix not supported: " + suffix);
            return false;
        }

        return true;
    }


    private TtsUtil.ITtsCallback mLongMsgCallback = new TtsUtil.ITtsCallback() {
        @Override
        public void onSuccess() {
            AppLogic.runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    L.d("mLongMsgCallback: onSuccess");
                    AppLogic.removeBackGroundCallback(mReleaseSessionLockTask);
                    AppLogic.runOnBackGround(mReleaseSessionLockTask, 6000);
                    regWakeupReply();
                    regLongMsgReply();
                    SoundManager.getInstance(AppLogic.getApp()).play(1);

                }
            }, 0);
        }

        @Override
        public void onCancel() {
            bWaitingReplyAsr = false;
            super.onCancel();
        }
    };

    private void regLongMsgReply() {
        AsrUtil.useWakeupAsAsr(new AsrUtil.AsrComplexSelectCallback() {
            @Override
            public void onCommandSelected(final String type, final String command) {
                AppLogic.runOnBackGround(new Runnable() {
                    @Override
                    public void run() {
                        if (type.equals("CMD_PROC_LONG_MSG")) {
                            procLongMsg(mCurTtsModel);
                        }

                        AppLogic.removeBackGroundCallback(mReleaseSessionLockTask);
                        // 反注册“回复微信”的唤醒词
                        AsrUtil.recoverWakeupFromAsr("REPLY_LONG_MSG");
                        AsrUtil.recoverWakeupFromAsr("REPLY_WX");
                        WxHelpGuideManager.getInstance()
                                .hideHelpGuide(WxHelpGuideManager.GuideScene.RECEIVE_MESSAGE);
                    }
                }, 0);
            }

            @Override
            public boolean needAsrState() {
                return true;
            }

            @Override
            public int getPriority() {
                return AsrUtil.WKASR_PRIORITY_NO_ASR;
            }

            @Override
            public String getTaskId() {
                return "REPLY_LONG_MSG";
            }
        }.addCommand("CMD_PROC_LONG_MSG", "确定"));
    }

    void procWakeupReply(final String sessionId) {
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                if (bBusyTts
                        || bBusyCall
                        || bRecordWinShowing
                        || bWaitingReplyAsr
                        || bBusyDownloadingVoice) {
                    L.d(LOG_TAG, "procWakeupReply busy:"
                            + ", bBusyTts = " + bBusyTts
                            + ", bBusyCall = " + bBusyCall
                            + ", bRecordWinShowing = " + bRecordWinShowing
                            + ", bWaitingReplyAsr = " + bWaitingReplyAsr
                            + ", bBusyDownloadingVoice = " + bBusyDownloadingVoice);
                    return;
                }

                final WxContact contact = WxContactStore.getInstance().getContact(sessionId);

                if (null != contact) {
                    String displayName = SmileyParser.removeEmoji(contact.getDisplayName());
                    MessageActionCreator.get().cancelNotify();
                    bWaitingRecordEnd = true;

                    String text = "即将为您录音，发微信给" + displayName;

                    TtsActionCreator.get().insertTts("RS_VOICE_WEBCHAT_SEND_MSG_HINT", new
                            String[]{"%TAR%", displayName}, text, true, new TtsUtil.ITtsCallback() {
                        @Override
                        public void onSuccess() {
                            MessageActionCreator.get().replyVoice(contact.mUserOpenId, true);
                        }

                        @Override
                        public void onCancel() {
                            bWaitingRecordEnd = false;
                        }

                        @Override
                        public void onError(int iError) {
                            bWaitingRecordEnd = false;
                        }
                    });
                }
            }
        }, 0);

    }

    private boolean cancelCurrentTts() {
        L.d(LOG_TAG, "cancelCurrentTts, bBusyWxTts = " + bBusyWxTts);

        if (bBusyWxTts) {
            mBroadcastingMsg = -1;
            TtsUtil.cancelSpeak(mCurTtsId);
            bBusyWxTts = false;

            AppLogic.removeBackGroundCallback(mReleaseSessionLockTask);
            AppLogic.runOnBackGround(mReleaseSessionLockTask, 0);
            return true;
        }

        return false;
    }

    private String getTtsText(TtsModel model) {
        if (null == model) {
            L.e(LOG_TAG, "getTtsText::model is null");
            return "";
        }

        // message字段为空当作普通文字消息播报
        if (null == model.message) {
            return model.text;
        }

        if (!isValidMessageTts(model)) {
            L.e(LOG_TAG, "getTtsText::model is invalid: " + model.toString());
            return "";
        }

        if (TextUtils.isEmpty(model.text)) {
            model.text = getMsgTts(model.message, false);
        }

        return model.text;
    }

    private String getMsgTts(WxMessage message, boolean isRepeat) {
        WxContact sender = WxContactStore.getInstance().getContact(message.mSenderUserId);
        WxContact session = WxContactStore.getInstance().getContact(message.mSessionId);

        boolean isSelf = message.mSenderUserId
                .equals(WxContactStore.getInstance().getLoginUser().mUserOpenId);
        boolean isGroupSession = WxContact.isGroupOpenId(message.mSessionId);
        boolean isTimeout = (System.currentTimeMillis() - mLastTtsBeginTime)
                > Constant.TTS_SAME_SPEAKER_DELAY;

        StringBuilder textBuilder = new StringBuilder();

        if (isSelf) {
            if (isGroupSession) {
                textBuilder.append("我在").append(session.getDisplayName());
            } else {
                textBuilder.append("我对").append(session.getDisplayName());
            }
        } else {
            WxMessage lastMsg = (null == mCurTtsModel) ? null : mCurTtsModel.message;

            // 会话未发生变化，设置连续播报标志位
            if (null != lastMsg && lastMsg.mSessionId.equals(message.mSessionId)) {
                mContinuesMsgCount++;
            } else {
                mContinuesMsgCount = 0;
            }

            // 若发送者和会话都没有变化，且没有超出连续播报阈值
            if (null != lastMsg
                    && lastMsg.mSenderUserId.equals(message.mSenderUserId)
                    && lastMsg.mSessionId.equals(message.mSessionId)
                    && !isTimeout) {
                /*if ((lastMsg.mMsgType == WxMessage.MSG_TYPE_TEXT) && (message.mMsgType ==
                WxMessage.MSG_TYPE_TEXT)) {
                    textBuilder.append("");
                    isContinuesText = true;
                } else {
                    isContinuesText = false;
                    textBuilder.append("又对你");
                }*/
                textBuilder.append("又");
            } else {
                if (isGroupSession) {
                    // 消息来自当前打开的会话
                    if (message.mSessionId.equals(mOpenedChatId)) {
                        textBuilder.append(getContactNickForTts(sender));
                    } else {
                        textBuilder.append(getContactNickForTts(sender)).append("在").append
                                (session.getDisplayName());
                    }
                } else {
                    // 当前打开会话为群聊时, 需要补充"单独对你说"提示, 避免消息发送方也在当前打开的群内引起的
                    // 歧义(无法分辨是发送方在当前群内发送了消息还是对你单独发送了消息)
                    if (WxContact.isGroupOpenId(mOpenedChatId)) {
                        textBuilder.append(getContactNickForTts(sender)).append("单独对你");
                    } else {
                        textBuilder.append(getContactNickForTts(sender)).append("对你");
                    }
                }
            }
        }

        if (WxMessage.MSG_TYPE_TEXT == message.mMsgType) {
            textBuilder.append(getTextMessageTts(message.mContent, isSelf, !isRepeat));
        } else {
            if (isSelf) {
                textBuilder.append("发送了");
            } else {
                textBuilder.append("发来");
            }
        }

        do {
            if (WxMessage.MSG_TYPE_IMG == message.mMsgType) {
                textBuilder.append("一张图片");
                break;
            }

            if (WxMessage.MSG_TYPE_ANIM == message.mMsgType) {
                textBuilder.append("一个动态表情");
                break;
            }

            if (WxMessage.MSG_TYPE_URL == message.mMsgType) {
                textBuilder.append("一条链接");
                break;
            }

            if (WxMessage.MSG_TYPE_FILE == message.mMsgType) {
                textBuilder.append("一个文件");

                break;
            }

            if (WxMessage.MSG_TYPE_LOCATION == message.mMsgType) {
                textBuilder.append("一条位置分享");

                if (!TextUtils.isEmpty(message.mAddress)) {
                    textBuilder.append("，地址是").append(message.mAddress);
                }

                break;
            }

            if (WxMessage.MSG_TYPE_VOICE == message.mMsgType) {
                textBuilder.append("一条语音消息");
                break;
            }
        } while (false);

        return SmileyParser.removeEmoji(textBuilder.toString());
    }

    private String getTextMessageTts(String msgContent, boolean isSelf, boolean cutLongText) {
        String prefix = isSelf ? "发送了" : "发来";
        // 表情播报
        if (isFullEmotionContent(msgContent)) {
            String emotion = msgContent.substring(1, msgContent.indexOf(']'));
            int n = 1;
            boolean same = true;
            try {
                for (int i = emotion.length() + 2; i < msgContent.length();
                     i += emotion.length() + 2) {
                    if (!msgContent.substring(i + 1, i + 1 + emotion.length()).equals(emotion)) {
                        same = false;
                        break;
                    }
                    ++n;
                }
            } catch (Exception e) {
                same = false;
            }


            // 处理emoji表情, 不进行播报
            if (emotion.startsWith("emoji")) {
                emotion = "";
            }

            if (!same) {
                return prefix + "一连串表情";
            }
            if (n > 3) {
                return prefix + "一连串" + SmileyParser.getEmotionTts(emotion) + "表情";
            }
            return prefix + n + "个" + SmileyParser.getEmotionTts(emotion) + "表情";
        }
        // 字符表情
        if (msgContent.matches("^(\\s*<span class=\"[\\w ]+\"></span>\\s*)+$")) {
            return "发来特殊表情";
        }
        // 特殊符号
        if (msgContent.matches("^[^0-9A-Za-z\u4e00-\u9fa5]+$")) {
            String regex = "[^0-9A-Za-z\u4e00-\u9fa5]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(msgContent);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            if (count < 4) {
                return prefix + count + "个特殊符号";
            } else {
                return prefix + "一连串特殊符号";
            }
        }

        if (cutLongText && isLongMsgText(msgContent)) {
            msgContent = msgContent.substring(0, LONG_MSG_CUT_THRESHOLD);
        }

        return "说: " + msgContent;
    }

    private String getContactNickForTts(WxContact contact) {
        if (null == contact || TextUtils.isEmpty(contact.getDisplayName())) {
            return "某人";
        }

        return contact.getDisplayName();
    }

    private boolean isValidMessageTts(TtsModel model) {
        return !((null == model)
                || (null == model.message)
                || TextUtils.isEmpty(model.message.mSenderUserId)
                || TextUtils.isEmpty(model.message.mSessionId));
    }

    /**
     * 返回消息是否是由纯表情组成的
     *
     * @param content
     * @return
     */
    private boolean isFullEmotionContent(String content) {
        return content.matches("^(?:\\[[^\\[\\]]+\\])+$");
    }

    private void doWxSendMsgResp(Action<WxMessage> action) {
        WxMessage msg = action.getData();
        if (WxMessage.MSG_TYPE_LOCATION == msg.mMsgType) {
            WxContact contact = WxContactStore.getInstance().getContact(msg.mSessionId);
            if (contact != null) {
                TXZRecordStore.get().regWakeupReply(msg.mSessionId);
                String nick = SmileyParser.removeEmoji(contact.getDisplayName());
                TtsActionCreator.get().insertTts("RS_VOICE_WEBCHAT_SEND_LOC_SUCCESS",
                        new String[]{"%TAR%", nick}, "已为您分享位置给" + nick, false,
                        new TtsUtil.ITtsCallback() {
                            @Override
                            public void onCancel() {
                                super.onCancel();
                                TXZAsrManager.getInstance().recoverWakeupFromAsr("REPEAT_REPLY_WX");
                            }
                        });
            }
        } else if (WxMessage.MSG_TYPE_IMG == msg.mMsgType) {
            WxContact contact = WxContactStore.getInstance().getContact(msg.mSessionId);
            if (contact != null) {
                TXZRecordStore.get().regWakeupReply(msg.mSessionId);
                String nick = SmileyParser.removeEmoji(contact.getDisplayName());
                TtsActionCreator.get().insertTts("RS_VOICE_WEBCHAT_SEND_IMG_SUCCESS", new
                                String[]{"%TAR%", nick}, "已为您分享图片给" + nick, false,
                        new TtsUtil.ITtsCallback() {
                            @Override
                            public void onCancel() {
                                super.onCancel();
                                TXZAsrManager.getInstance().recoverWakeupFromAsr("REPEAT_REPLY_WX");
                            }
                        });
            }
        }
    }

    private int mLastRepeatMsgTtsId = TtsUtil.INVALID_TTS_TASK_ID;
    private boolean bSkipNextRepeatMsgCancel = false;

    private boolean doRepeatMessage(WxMessage msg) {
        if (WxMessage.MSG_TYPE_VOICE == msg.mMsgType && !TextUtils.isEmpty(msg.mVoiceCachePath)) {
            bBusyWxTts = true;
            mCurTtsId = TtsUtil.speakVoice(getMsgTts(msg, true), msg.mVoiceCachePath, TtsUtil
                    .PreemptType.PREEMPT_TYPE_IMMEADIATELY, mRepeatMsgCallback);
        } else {
            bBusyWxTts = true;
            mCurTtsId = TtsUtil.speakText(getMsgTts(msg, true), TtsUtil.PreemptType
                    .PREEMPT_TYPE_IMMEADIATELY, mRepeatMsgCallback);
        }

        if (mBroadcastingMsg != -1) {
            bSkipNextRepeatMsgCancel = true;
        }
        mLastRepeatMsgTtsId = mCurTtsId;

        mBroadcastingMsg = msg.mMsgId;
        emitChange(EVENT_TYPE_ALL);

        return true;
    }

    private boolean doSKipRepeatMessage() {
        // 判断当前正在播报的消息是否是重播的
        if (mCurTtsId == mLastRepeatMsgTtsId) {
            cancelCurrentTts();
            return true;
        }

        return false;
    }

    private TtsUtil.ITtsCallback mRepeatMsgCallback = new TtsUtil.ITtsCallback() {
        @Override
        public void onEnd() {
            if (!bSkipNextRepeatMsgCancel) {
                bBusyWxTts = false;
                mBroadcastingMsg = -1;
                emitChange(EVENT_TYPE_ALL);
            }
            bSkipNextRepeatMsgCancel = false;
        }
    };

    private boolean doSkipMessage() {
        cancelCurrentTts();

        return true;
    }

}
