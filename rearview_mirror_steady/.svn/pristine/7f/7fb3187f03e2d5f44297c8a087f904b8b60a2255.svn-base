package com.txznet.webchat.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.squareup.otto.Subscribe;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.StringUtils;
import com.txznet.reserve.activity.ReserveSingleTaskActivity0;
import com.txznet.reserve.activity.ReserveSingleTopActivity4;
import com.txznet.reserve.activity.ReserveSingleTopActivity5;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.webchat.actions.ContactActionCreator;
import com.txznet.webchat.actions.MessageActionCreator;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.helper.TXZNotification;
import com.txznet.webchat.sdk.WxSDKManager;
import com.txznet.webchat.sp.TipManager;
import com.txznet.webchat.sp.WebChatSp;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.TXZBindStore;
import com.txznet.webchat.stores.TXZRecordStore;
import com.txznet.webchat.stores.TXZTtsStore;
import com.txznet.webchat.stores.WxContactFocusStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.stores.WxQrCodeStore;
import com.txznet.webchat.stores.WxResourceStore;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.interfaces.IRecordWin;
import com.txznet.webchat.util.ContactEncryptUtil;

/**
 * 处理界面展示相关逻辑
 * Created by J on 2016/5/3.
 */
public class UIHandler {
    private static final String ACTION_QR_REFRESH = "com.txznet.webchat.action.QR_REFRESH";

    private static UIHandler sInstance;
    private boolean bNotificationEnabled;
    private boolean bUIEnabled;
    private boolean bRecordWindowEnabled;

    private UIHandler() {
        bUIEnabled = WebChatSp.getInstance(GlobalContext.get()).getUIEnabled();
        bNotificationEnabled = WebChatSp.getInstance(GlobalContext.get()).getNotificationEnabled();
        bRecordWindowEnabled = WebChatSp.getInstance(GlobalContext.get()).getRecordWindowEnabled();

        AppStatusStore.get().register(this);
        TXZBindStore.get().register(this);
        TXZRecordStore.get().register(this);
        TXZTtsStore.getInstance().register(this);
        WxContactStore.getInstance().register(this);
        WxLoginStore.get().register(this);
        WxMessageStore.getInstance().register(this);
        WxQrCodeStore.get().register(this);
        WxResourceStore.get().register(this);
    }

    public static UIHandler getInstance() {
        if (null == sInstance) {
            synchronized (UIHandler.class) {
                if (null == sInstance) {
                    sInstance = new UIHandler();
                }
            }
        }

        return sInstance;
    }

    public boolean getWxUIEnabled() {
        return bUIEnabled;
    }

    public void setUIEnabled(boolean enable) {
        this.bUIEnabled = enable;
        WebChatSp.getInstance(GlobalContext.get()).setUIEnabled(enable);
    }

    public void setNotificationEnabled(boolean enable) {
        this.bNotificationEnabled = enable;
        WebChatSp.getInstance(GlobalContext.get()).setNotificationEnabled(enable);
    }

    public boolean getNotificationEnabled() {
        return bNotificationEnabled;
    }

    public void setRecordWindowEnabled(boolean enable) {
        this.bRecordWindowEnabled = enable;
        WebChatSp.getInstance(GlobalContext.get()).setRecordWindowEnabled(enable);
    }

    public boolean getRecordWindowEnabled() {
        return bRecordWindowEnabled;
    }

    public void showAppStart(boolean needTts) {
        if (bUIEnabled) {
            launchUI();
        }

        WxSDKManager.getInstance().notifyLaunch();

        if (needTts) {
            TtsUtil.speakResource("RS_VOICE_WEBCHAT_HINT_NEED_SCAN_QR",
                    TipManager.getTip(TipManager.KEY_TIP_NEED_LOGIN));
        }
    }

    public void showChat(String openId, boolean ttsLast) {
        if (bUIEnabled) {
            String theme = WxThemeStore.get().getCurrentTheme();
            ContactActionCreator.get().switchFocusSession(openId);
            if (WxThemeStore.THEME_CAR.equals(theme)
                    || WxThemeStore.THEME_CAR_PORTRAIT.equals(theme)) {
                ReserveSingleTopActivity4.showChat(GlobalContext.get(), openId, ttsLast);
            } else if (WxThemeStore.THEME_CAR_PORTRAIT_T700.equals(theme)) {
                ReserveSingleTopActivity5.showChat(GlobalContext.get(), openId, ttsLast);
            } else {
                ReserveSingleTaskActivity0.show(GlobalContext.get(), openId, ttsLast);
            }
        }

        WxSDKManager.getInstance().notifyShowChat(openId);
    }

    public void showSettings() {
        if (bUIEnabled) {
            // 只有登录了微信才进行响应
            if (WxLoginStore.get().isLogin()) {
                String theme = WxThemeStore.get().getCurrentTheme();

                if (WxThemeStore.THEME_CAR.equals(theme)
                        || WxThemeStore.THEME_CAR_PORTRAIT.equals(theme)) {
                    ReserveSingleTopActivity4.showSettings();
                } else if (WxThemeStore.THEME_CAR_PORTRAIT_T700.equals(theme)) {
                    ReserveSingleTopActivity5.showSettings();
                }
            }
        }
    }

    public void showHelp() {
        if (bUIEnabled) {
            // 只有登录了微信才进行响应
            if (WxLoginStore.get().isLogin()) {
                String theme = WxThemeStore.get().getCurrentTheme();

                if (WxThemeStore.THEME_CAR.equals(theme)
                        || WxThemeStore.THEME_CAR_PORTRAIT.equals(theme)) {
                    ReserveSingleTopActivity4.showHelp();
                } else if (WxThemeStore.THEME_CAR_PORTRAIT_T700.equals(theme)) {
                    ReserveSingleTopActivity5.showSettings();
                }
            }
        }
    }

    private IRecordWin mCurrentRecordWin = null;

    private void updateRecordWin() {
        if (getRecordWindowEnabled()) {
            TXZRecordStore store = TXZRecordStore.get();
            IRecordWin w = WxThemeStore.get().getRecordWin();

            if (mCurrentRecordWin != null && mCurrentRecordWin != w) {
                if (mCurrentRecordWin.isShowing()) {
                    mCurrentRecordWin.dismiss();
                }
            }

            mCurrentRecordWin = w;
            if (store.isReplying()) {
                if (w.isShowing()) {
                    w.refreshTimeRemain(store.getCountdown());
                } else {
                    w.updateTargetInfo(store.getCurVoiceToUser());
                    w.show();
                    w.refreshTimeRemain(store.getCountdown());
                }
            } else {
                if (w.isShowing()) {
                    w.dismiss();
                    mCurrentRecordWin = null;
                }
            }
        }

        WxSDKManager.getInstance().notifyRecordStatus();
    }

    private static final String INTENT_ACTION_RECORD_SHOW = "wx.action.recordwin.show";
    private static final String INTENT_ACTION_RECORD_DISMISS = "wx.action.recordwin.dismiss";
    private boolean mRecordWinShow = false;

    private void sendRecordWinBroadcast() {
        TXZRecordStore store = TXZRecordStore.get();

        if (store.isReplying()) {
            if (!mRecordWinShow) {
                sendBroadCast(INTENT_ACTION_RECORD_SHOW);
            }
            mRecordWinShow = true;
        } else {
            if (mRecordWinShow) {
                sendBroadCast(INTENT_ACTION_RECORD_DISMISS);
            }
            mRecordWinShow = false;
        }
    }

    private void sendBroadCast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        GlobalContext.get().sendBroadcast(intent);
    }

    public void repeatSendMessage(String tip) {
        if (!WxLoginStore.get().isLogin()) {
            TtsUtil.speakResource("RS_VOICE_WEBCHAT_HINT_NOT_LOGIN", "您尚未登陆微信助手");
            return;
        }

        if (TXZRecordStore.get().isReplying() || TXZRecordStore.get().isSending()) {
            return;
        }

        final String lastToId = TXZRecordStore.get().getLastVoiceToUser();

        if (StringUtils.isEmpty(lastToId)) {
            TtsUtil.speakResource("RS_VOICE_WEBCHAT_NOT_SENT_MSG", "您还没有发送过微信");
        } else if (null == tip) {
            String nick = WxContactStore.getInstance().getContact(lastToId).mNickName;

            TtsUtil.speakResource("RS_VOICE_WEBCHAT_SEND_MSG_HINT", new String[]{"%TAR%", nick},
                    "即将为您录音，发送给" + nick, new TXZTtsManager.ITtsCallback() {
                        @Override
                        public void onSuccess() {
                            MessageActionCreator.get().replyVoice(lastToId, true);
                        }
                    });
        } else {
            TtsUtil.speakText(tip, new TXZTtsManager.ITtsCallback() {
                @Override
                public void onSuccess() {
                    MessageActionCreator.get().replyVoice(lastToId, true);
                }
            });
        }
    }

    private String mRecentUserId;

    public void updateLastSendToUserId(String id) {
        mRecentUserId = id;
    }

    public void repeatSendMessageToRecent(String tip) {
        if (!WxLoginStore.get().isLogin()) {
            TtsUtil.speakResource("RS_VOICE_WEBCHAT_HINT_NOT_LOGIN", "您尚未登陆微信助手");
            return;
        }

        if (TXZRecordStore.get().isReplying() || TXZRecordStore.get().isSending()) {
            return;
        }

        if (TextUtils.isEmpty(mRecentUserId)) {
            TtsUtil.speakResource("RS_VOICE_WEBCHAT_NO_RECENT_CONTACT", "未找到最近联系人");
        } else if (null == tip) {
            String nick = WxContactStore.getInstance().getContact(mRecentUserId).getDisplayName();

            TtsUtil.speakResource("RS_VOICE_WEBCHAT_SEND_MSG_HINT", new String[]{"%TAR%", nick},
                    "即将为您录音，发送给" + nick, new TXZTtsManager.ITtsCallback() {
                @Override
                public void onSuccess() {
                    MessageActionCreator.get().replyVoice(mRecentUserId, true);
                }
            });
        } else {
            TtsUtil.speakText(tip, new TXZTtsManager.ITtsCallback() {
                @Override
                public void onSuccess() {
                    MessageActionCreator.get().replyVoice(mRecentUserId, true);
                }
            });
        }
    }

    private String mTargetUserId;

    public void sendMessageToCurrentContact(String tip) {
        if (!WxLoginStore.get().isLogin()) {
            TtsUtil.speakResource("RS_VOICE_WEBCHAT_HINT_NOT_LOGIN", "您尚未登陆微信助手");
            return;
        }

        if (TXZRecordStore.get().isReplying() || TXZRecordStore.get().isSending()) {
            return;
        }

        // 寻找当前联系人
        mTargetUserId = null;
        if (TXZNotification.getInstance().isShowing()) {
            mTargetUserId = TXZNotification.getInstance().getCurrentSessionId();
        }

        if (TextUtils.isEmpty(mTargetUserId)) {
            mTargetUserId = WxContactFocusStore.getInstance().getFocusedSession();
        }

        if (TextUtils.isEmpty(mTargetUserId)) {
            return;
        }

        if (null == tip) {
            String nick = WxContactStore.getInstance().getContact(mTargetUserId).getDisplayName();

            TtsUtil.speakResource("RS_VOICE_WEBCHAT_SEND_MSG_HINT", new String[]{"%TAR%", nick},
                    "即将为您录音，发送给" + nick, new TXZTtsManager.ITtsCallback() {
                @Override
                public void onSuccess() {
                    MessageActionCreator.get().replyVoice(mTargetUserId, true);
                }
            });
        } else {
            MessageActionCreator.get().replyVoice(mTargetUserId, false);
        }
    }

    public void replyToRecent(String tip) {
        if (!WxLoginStore.get().isLogin()) {
            TtsUtil.speakResource("RS_VOICE_WEBCHAT_HINT_NOT_LOGIN", "您尚未登陆微信助手");
            return;
        }

        if (TXZRecordStore.get().isReplying() || TXZRecordStore.get().isSending()) {
            return;
        }

        final WxMessage recentMsg = TXZTtsStore.getInstance().getLastBroadMsg();
        if (null == recentMsg || TextUtils.isEmpty(recentMsg.mSenderUserId)) {
            TtsUtil.speakResource("RS_VOICE_WEBCHAT_NO_RECENT_MSG", "您还未收到过微信");
            return;
        }

        final String recentId = recentMsg.mSenderUserId;
        if (null == tip) {
            String nick = WxContactStore.getInstance().getContact(recentId).mNickName;

            TtsUtil.speakResource("RS_VOICE_WEBCHAT_SEND_MSG_HINT", new String[]{"%TAR%", nick},
                    "即将为您录音，发送给" + nick, new TXZTtsManager.ITtsCallback() {
                @Override
                public void onSuccess() {
                    MessageActionCreator.get().replyVoice(recentId, false);
                }
            });
        } else {
            TtsUtil.speakText(tip, new TXZTtsManager.ITtsCallback() {
                @Override
                public void onSuccess() {
                    MessageActionCreator.get().replyVoice(recentId, false);
                }
            });
        }

    }

    /**
     * 显示微信界面
     */
    public void launchUI() {
        if (bUIEnabled) {
            final Intent intent = new Intent();
            if (WxLoginStore.get().isLogin()) {
                intent.setClass(GlobalContext.get(), WxThemeStore.get().getClassForMainActivity());
            } else {
                intent.setClass(GlobalContext.get(), WxThemeStore.get().getClassForQRActivity());
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            // 添加标志位，登录成功后退出微信界面
            intent.putExtra("quitAfterLogin", true);
            GlobalContext.get().startActivity(intent);
        }

        WxSDKManager.getInstance().notifyLaunch();
    }

    /**
     * 隐藏微信界面
     */
    public void hideUI() {
        if (ActivityStack.getInstance().has()) {
            ActivityStack.getInstance().currentActivity().moveTaskToBack(true);
        }
    }

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        switch (event.getType()) {
            case WxQrCodeStore.EVENT_TYPE_ALL:
                updateQRCode();
                break;

            case WxLoginStore.EVENT_TYPE_ALL:
                updateLoginStatus();
                break;

            case TXZRecordStore.EVENT_TYPE_ALL:
                updateRecordWin();
                sendRecordWinBroadcast();
                break;

            case AppStatusStore.EVENT_TYPE_ALL:
                updateNotifyStatus();
                break;

            case WxResourceStore.EVENT_TYPE_ALL:
                notifyImageReady(event.getData());
                break;

        }
    }

    private void updateQRCode() {
        sendQRBroadCast();

        WxSDKManager.getInstance().notifyQRCode();
    }

    private void sendQRBroadCast() {
        WxQrCodeStore store = WxQrCodeStore.get();

        if (!store.isRetrieving() || store.isQrCodeInvalid()) {
            Intent intent = new Intent(ACTION_QR_REFRESH);
            intent.putExtra("code", store.getQrCode());
            GlobalContext.get().sendBroadcast(intent);
        }
    }


    private void updateLoginStatus() {
        if (WxLoginStore.get().isLogin()) {
            WxSDKManager.getInstance().notifyLogin();
        } else {
            WxSDKManager.getInstance().notifyLogout();
        }
    }

    private void updateNotifyStatus() {
        WxSDKManager.getInstance()
                .notifyMsgBroadcastEnabled(AppStatusStore.get().isAutoBroadEnabled());
    }

    private void notifyImageReady(Bundle data) {
        if (null != data) {
            String openId = data.getString("uid");
            String imgPath = WxResourceStore.get().getContactHeadImage(openId);

            if (imgPath != null) {
                WxSDKManager.getInstance()
                        .notifyAvatarReady(ContactEncryptUtil.encrypt(openId), imgPath);
            }
        }
    }
}
