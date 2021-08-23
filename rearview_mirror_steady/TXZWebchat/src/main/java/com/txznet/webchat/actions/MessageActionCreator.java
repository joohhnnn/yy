package com.txznet.webchat.actions;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.RecorderUtil;
import com.txznet.comm.remote.util.StatusUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.sdk.TXZLocationManager;
import com.txznet.sdk.bean.LocationData;
import com.txznet.webchat.AppStatus;
import com.txznet.webchat.Constant;
import com.txznet.webchat.R;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.helper.TXZNotification;
import com.txznet.webchat.plugin.WxPluginManager;
import com.txznet.webchat.stores.TXZRecordStore;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.util.WxMonitorUtil;

public class MessageActionCreator {
    private static MessageActionCreator sInstance;
    private Dispatcher dispatcher;

    MessageActionCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static MessageActionCreator get() {
        if (sInstance == null) {
            synchronized (ResourceActionCreator.class) {
                if (sInstance == null) {
                    sInstance = new MessageActionCreator(Dispatcher.get());
                }
            }
        }
        return sInstance;
    }

    public void repeatMessage(String openId, long msgId) {
        repeatMessage(openId, msgId, true);
    }

    /*
        重复播报消息
     */
    public void repeatMessage(String openId, long msgId, boolean needNotify) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", openId);
        bundle.putLong("msgId", msgId);
        bundle.putBoolean("notify", needNotify);
        //// TODO: 2017/4/14 重播消息内部事件协议调整
        //dispatcher.dispatch(new Action<Bundle>(ActionType.WX_REPEAT_MSG, bundle));
    }

    /*
        重复上一条消息播报
     */
    public void repeatLastMessage() {

        //dispatcher.dispatch(new Action<Bundle>(ActionType.WX_REPEAT_MSG, null));
    }

    /*
        跳过当前消息播报
     */
    public void skipCurrentMessage() {
        cancelNotify();
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_SKIP_MSG, null));
    }


    /**
     * 调起录音发微信给联系人
     *
     * @param openId 联系人id
     * @param beep   是否播放录音前的beep声
     */
    public void replyVoice(final String openId, final boolean beep) {
        // 倒车状态下禁用录音功能
        if (AppStatus.getInstance().isReverseMode()) {
            return;
        }

        if (TXZRecordStore.get().isReplying()) {
            return;
        }
        StatusUtil.getStatus(new StatusUtil.GetStatusCallback() {
            @Override
            public void onGet() {
                if (this.isBusyCall) {
                    return;
                }
                if (this.isBusyAsr) {
                    TtsActionCreator.get().insertTts("录音设备使用中", false, null);
                    return;
                }

                if (beep) {
                    TtsUtil.speakVoice("", "$BEEP", new TtsUtil.ITtsCallback() {
                        @Override
                        public void onEnd() {
                            startRecording(openId);
                        }
                    });
                } else {
                    startRecording(openId);
                }


            }
        });
    }

    private void startRecording(final String openId) {
        final Bundle bundle = new Bundle();
        bundle.putString("uid", openId);
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_REPLY_VOICE_START, bundle));
        RecorderUtil.start(new RecorderUtil.RecordCallback() {
            @Override
            public void onBegin() {
            }


            @Override
            public void onEnd(int speechLength) {
                Bundle data = new Bundle();
                data.putInt("length", speechLength);
                dispatcher.dispatch(new Action<Bundle>(ActionType.WX_REPLY_VOICE_END, data));
            }

            @Override
            public void onParseResult(int voiceLength, String voiceText, String voiceUrl) {
                Bundle data = new Bundle();
                data.putInt("length", voiceLength);
                data.putString("txt", voiceText);
                data.putString("url", voiceUrl);
                dispatcher.dispatch(new Action<Bundle>(ActionType.WX_REPLY_VOICE_PARSE, data));
            }

            @Override
            public void onSpeechTimeout() {

            }

            @Override
            public void onMuteTimeout() {

            }

            @Override
            public void onError(int err) {
                Bundle data = new Bundle();
                data.putInt("err", err);
                dispatcher.dispatch(new Action<Bundle>(ActionType.WX_REPLY_VOICE_ERROR, data));
            }

            @Override
            public void onCancel() {
                Bundle data = new Bundle();
                data.putBoolean("manual", false);
                dispatcher.dispatch(new Action<Bundle>(ActionType.WX_REPLY_VOICE_CANCEL, data));
            }

            @Override
            public void onVolume(int vol) {

            }

            @Override
            public void onMute(int time) {
                Bundle data = new Bundle();
                data.putInt("mute", time);
                dispatcher.dispatch(new Action<Bundle>(ActionType.WX_REPLY_VOICE_MUTE, data));
            }

            @Override
            public void onPCMBuffer(short[] buffer, int len) {

            }

            @Override
            public void onMP3Buffer(byte[] buffer) {

            }
        }, new RecorderUtil.RecordOption()
                .setNeedOnLineParse(true)
                .setEncodeMp3(true).setSkipMute(false).setMaxMute(3000)
                .setMaxSpeech(60000)
                .setSavePathPrefix(Constant.PATH_RECORD_CACHE_PREFIX));

        ContactActionCreator.get().switchFocusSession(openId);
    }

    /*
        立即发送
     */
    public void sendVoice(boolean manual) {
        if (!TextUtils.isEmpty(TXZRecordStore.get().getCurVoiceToUser())) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("manual", manual);
            dispatcher.dispatch(new Action<Bundle>(ActionType.WX_REPLY_VOICE_SEND, bundle));
            RecorderUtil.stop();
        }
    }

    /*
        取消当前播报
     */
    public void cancelCurSpeak() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.TXZ_TTS_CANCEL_CUR, null));
    }

    /*
        取消回复
     */
    public void cancelReply(boolean manual) {
        cancelReply(manual, false);
    }

    public void cancelReply(boolean manual, boolean mute) {
        if (!TXZRecordStore.get().isReplying()) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean("manual", manual);
        bundle.putBoolean("mute", mute);
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_REPLY_VOICE_CANCEL, bundle));
        RecorderUtil.stop();
    }

    public String sendTextMsg(final String toUser, final String content) {
        WxMessage msg = generateRawMsg(toUser);
        msg.mMsgType = WxMessage.MSG_TYPE_TEXT;
        msg.mContent = content;

        return sendMsg(msg);
    }

    public String sendImgMsg(final String toUser, final String imgPath) {
        WxMessage msg = generateRawMsg(toUser);
        msg.mMsgType = WxMessage.MSG_TYPE_IMG;
        msg.mImgCachePath = imgPath;

        return sendMsg(msg);
    }

    public String sendVoiceMsg(final String toUser, final String content, int voiceLength) {
        WxMessage msg = generateRawMsg(toUser);
        msg.mMsgType = WxMessage.MSG_TYPE_VOICE;
        msg.mContent = content;
        msg.mVoiceLength = voiceLength;
        msg.mVoiceCachePath = Environment.getExternalStorageDirectory().getPath() + "/txz/webchat/cache/Self/" + msg.mMsgId;

        return sendMsg(msg);
    }

    public String sendLocationMsg(final String toUser, final double lat, final double lng, final String addr) {
        // 检查当前配置是否启用了位置分享功能
        if (!WxConfigStore.getInstance().getLocShareEnabled()) {
            TtsActionCreator.get().insertTts(GlobalContext.get().getResources().getString(R.string.tip_loc_share_disabled), false, null);
            return "";
        }

        if (0 == lat && 0 == lng) {
            return "";
        }

        WxMessage msg = generateRawMsg(toUser);
        msg.mMsgType = WxMessage.MSG_TYPE_LOCATION;
        msg.mLatitude = lat;
        msg.mLongtitude = lng;
        if (!TextUtils.isEmpty(addr)) {
            msg.mContent = String.format("我在%s http://wx.txzing.com/module/pick_up/web/location.html?lat=%s&lng=%s", addr, lat, lng);
            msg.mAddress = addr;
        } else {
            msg.mContent = String.format("我的位置详情 http://wx.txzing.com/module/pick_up/web/location.html?lat=%s&lng=%s", lat, lng);
            msg.mAddress = "";
        }
        return sendMsg(msg);
    }

    public String sendLocationMsg(final String toUser) {
        // 检查当前配置是否启用了位置分享功能
        if (!WxConfigStore.getInstance().getLocShareEnabled()) {
            TtsActionCreator.get().insertTts(GlobalContext.get().getResources().getString(R.string.tip_loc_share_disabled), false, null);
            return "";
        }

        LocationData data = TXZLocationManager.getInstance().getCurrentLocationInfo();
        if (data != null) {
            return sendLocationMsg(toUser, data.dbl_lat, data.dbl_lng, data.str_addr);
        } else {
            WxMonitorUtil.doMonitor(WxMonitorUtil.WX_LOC_GET_FAILED);
        }

        return "";
    }

    private WxMessage generateRawMsg(final String toUser) {
        WxMessage msg = new WxMessage();
        msg.mMsgId = System.currentTimeMillis();
        msg.mSessionId = toUser;
        msg.mSenderUserId = WxContactStore.getInstance().getLoginUser().mUserOpenId;

        return msg;
    }

    private String sendMsg(WxMessage msg) {
        msg.mMsgId = msg.mMsgId * 10000 + (long) (Math.random() * 10000);
        WxPluginManager.getInstance().invokePlugin(null, PluginInvokeAction.INVOKE_CMD_SEND_MSG, msg);
        ContactActionCreator.get().switchFocusSession(msg.mSessionId);

        return msg.mMsgId + "";
    }

    public void revokeMsg(WxMessage msg) {
        WxPluginManager.getInstance().invokePlugin(null, PluginInvokeAction.INVOKE_CMD_REVOKE_MSG, msg);
    }

    public void revokeLastMsg() {
        WxPluginManager.getInstance().invokePlugin(null, PluginInvokeAction.INVOKE_CMD_REVOKE_MSG,
                WxMessageStore.getInstance().getLastSendMessage());
    }

    public void showNotify(String openId, String msgId) {
        TXZNotification.getInstance().notifyBeginTts(openId, msgId);
    }

    public void dismissNotify(String openId, String msgId) {
        TXZNotification.getInstance().notifyEndTts(openId, msgId);
    }

    public void cancelNotify() {
        cancelNotify(null, null);
    }

    public void cancelNotify(String openId, String msgId) {
        TXZNotification.getInstance().notifyCancelTts(openId, msgId);
    }
}
