package com.txznet.launcher.domain.wechat;

import android.graphics.Bitmap;
import android.os.Environment;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.BuildConfig;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.settings.SettingsManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.module.wechat.bean.WechatMsgData;
import com.txznet.launcher.utils.Base64Converter;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZWechatManager;
import com.txznet.sdk.TXZWechatManagerV2;
import com.txznet.sdk.bean.WechatContactV2;
import com.txznet.sdk.bean.WechatMessageV2;
import com.txznet.sdk.wechat.AbsTXZWechatTool;
import com.txznet.txz.util.runnables.Runnable1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by TXZ-METEORLUO on 2018/2/24.
 * 微信相关业务类，包括了微信界面的处理（view调用这个类的方法）和微信发送过来信息的处理（设置了wechatTool，收到消息会自动回调）
 */

public class WechatManager extends BaseManager {

    private static WechatManager instance;

    public static final int TYPE_WECHAT_SHOW_QRCODE = 1;
    public static final int TYPE_WECHAT_UPDATE_QRCODE = 2;
    public static final int TYPE_WECHAT_SCAN_QRCODE = 3;
    public static final int TYPE_WECHAT_LOGIN = 4;
    public static final int TYPE_WECHAT_RECORD = 5;
    public static final int TYPE_WECHAT_SEND_BEGIN = 6;
    public static final int TYPE_WECHAT_SEND_SUCCESS = 7;
    public static final int TYPE_WECHAT_SEND_FAIL = 8;
    public static final int TYPE_WECHAT_UPDATE_SELF = 9;
    public static final int TYPE_WECHAT_LOGOUT = 10;
    public static final int TYPE_WECHAT_MSG_RECEIVE = 11;
    public static final int TYPE_WECHAT_SEND_CANCEL = 12;
    public static final String WECHAT_USER_ICON_FILE = Environment.getExternalStorageDirectory() + File.separator + "txz" + File.separator + "webchat" + File.separator + "user.jpg";

    private CopyOnWriteArrayList<String> mContactsId = new CopyOnWriteArrayList<String>();
    private final HashMap<String, Boolean> mContactNotify = new HashMap<>();

    private HashMap<String, String> mUserIconCache = new HashMap<String, String>();

    public static WechatManager getInstance() {
        if (instance == null) {
            synchronized (WechatManager.class) {
                if (instance == null) {
                    instance = new WechatManager();
                }
            }
        }
        return instance;
    }


    private AbsTXZWechatTool mTXZWechatTool = new AbsTXZWechatTool() {
        @Override
        protected void launch(WechatContactV2 loginUser, String qr) {
            LogUtil.logd("launch wechat");
            if (loginUser != null) {
//            LaunchManager.getInstance().launchWechatQr(new JSONBuilder()
//                    .put("type", TYPE_WECHAT_LOGIN)
//                    .toString());
            } else {
//            if (RecordWinManager.getInstance().isRecordWinClosed()) {
//                LaunchManager.getInstance().launchWechatQr(new JSONBuilder()
//                        .put("type", TYPE_WECHAT_SHOW_QRCODE)
//                        .toString());
//            } else {
                isLaunchWechatQr = true;
                showWechatQr();
//            }
            }
        }

        @Override
        protected void updateQR(String qr) {
            LogUtil.logd("updateQR:" + qr);
            if (LaunchManager.getInstance().isActiveWechatQr()) {
                LaunchManager.getInstance().launchWechatQr(new JSONBuilder()
                        .put("type", TYPE_WECHAT_UPDATE_QRCODE)
                        .put("data", qr).toString());
            }
        }

        @Override
        protected void QRScanned(String userIcon) {
            LogUtil.logd("QRScanned:" + userIcon);
            AppLogic.runOnBackGround(new Runnable1<String>(userIcon) {
                @Override
                public void run() {
                    try {
                        Bitmap bitmap = Base64Converter.string2Bitmap(mP1);
                        File file = new File(WECHAT_USER_ICON_FILE);
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        if (file.exists()) {
                            file.delete();
                        }
                        file.createNewFile();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, new FileOutputStream(WECHAT_USER_ICON_FILE));
                        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_WX_SCANNED);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        protected void login() {
            LogUtil.logd("login");

            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_WX_LOGIN);
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    TXZWechatManagerV2.getInstance().syncContactList(SHOW_WECHAT_CONTRACTS_COUNT);
                }
            }, 1000);
            //        AppLogic.runOnUiGround(new Runnable() {
//            @Override
//            public void run() {
//                LaunchManager.getInstance().launchDesktop();
//            }
//        });
        }

        @Override
        protected void updateLoginUser(WechatContactV2 user) {
            LogUtil.logd("updateSelf:" + user.getId() + ";nick:" + user.getNickName());
            PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_WECHAT_USER_ID, user.getId());
            PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_WECHAT_USER_NICK, user.getNickName());
        }

        @Override
        protected void logout() {
            mUserIconCache.clear();
            mContactsId.clear();
            mContactNotify.clear();
            LogUtil.logd("logout");
            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_WX_LOGOUT);

            // 微信注销的时候将发送中界面关闭，不然会出现微信已注销但是发送界面还在展示中的问题。
            if (LaunchManager.getInstance().isCurrModule(LaunchManager.ViewModuleType.TYPE_WECHAT_RECORD_MODULE)) {
                LaunchManager.getInstance().launchBack();
            }
        }

        /**
         * 接受到微信消息的回调，开始播报后会回调一次，结束后会回调一次。
         * @param message 对应的消息
         * @param casting 是否正在播报
         */
        @Override
        protected void updateNotify(WechatMessageV2 message, boolean casting) {
            //TODO 收到消息可能会显示在多个地方，需要处理
            if (BuildConfig.LOG_DEBUG) {
                LogUtil.logd("updateNotify:" + message.getMsgId() + ";senderId:" + message.getSenderId() + ";senderNick:" + message.getSenderNick() + ";msgType:" + message.getMsgType() + ";casting:" + casting);
            } else {
                LogUtil.logd("updateNotify");
            }
            if (!isBusy) {
                isBusy = true;
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_WX_MSG_NOTIFY);
            }

            if (casting) {
                updateContactsId(message.getSessionId());
                WechatMsgData wechatMsgData = new WechatMsgData(message);
                SettingsManager.getInstance().ctrlScreen(true);
                if (!LaunchManager.getInstance().launchWechatDialog(wechatMsgData)) {
                    for (WechatStateListener listener :
                            listeners) {
                        listener.updateNotify(wechatMsgData);
                    }
                }
            }
        }

        @Override
        protected void dismissNotify() {
            LogUtil.logd("dismissNotify");
            if (!LaunchManager.getInstance().dismissNotify()) {
                for (WechatStateListener listener :
                        listeners) {
                    listener.dismissNotify();
                }
            }
            isBusy = false;
            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_WX_MSG_NOTIFY_DISMISS);
        }

        @Override
        protected void updateRecordWin(int timeRemain, WechatContactV2 targetUser) {
            if (BuildConfig.LOG_DEBUG) {
                LogUtil.logd("updateRecordWin:" + timeRemain + ";id:" + targetUser.getId() + ";nick:" + targetUser.getNickName());
            } else {
                LogUtil.logd("updateRecordWin");
            }
            if (timeRemain > 0) {
                LaunchManager.getInstance().launchWechatRecord(new JSONBuilder()
                        .put("type", TYPE_WECHAT_RECORD)
                        .put("id", targetUser.getId())
                        .put("timeRemain", timeRemain)
                        .put("nick", targetUser.getNickName()).toString());
            } else {
                LaunchManager.getInstance().launchWechatRecord(new JSONBuilder()
                        .put("type", TYPE_WECHAT_SEND_BEGIN)
                        .toString());
            }
        }

        /**
         *
         * @param isSuccess   录音是否发送成功
         * @param isCancelled 录音是否被取消
         */
        @Override
        protected void dismissRecordWin(boolean isSuccess, boolean isCancelled) {
            LogUtil.logd("dismissRecordWin: isSuccess=" + isSuccess + ", isCancel:" + isCancelled);
            if (LaunchManager.getInstance().isActiveWechatRecord()) {
                if (isSuccess) {
                    LaunchManager.getInstance().launchWechatRecord(new JSONBuilder()
                            .put("type", TYPE_WECHAT_SEND_SUCCESS).toString());
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_WX_RECORD_SEND_SUCCESS);
                } else if (isCancelled) {
                    /*
                     * updateRecordWin和dismissRecordWin调用的间隔可能是很短的，如果在200ms之内，由于
                     * 加载录音中的界面是延时操作的，而返回桌面是马上执行的，这会导致先返回桌面然后才
                     * 展示录音中界面。
                     */
                    LaunchManager.getInstance().launchWechatRecord(new JSONBuilder()
                            .put("type", TYPE_WECHAT_SEND_CANCEL).toString());
//                    LaunchManager.getInstance().launchBack();
                } else {
                    LaunchManager.getInstance().launchWechatRecord(new JSONBuilder()
                            .put("type", TYPE_WECHAT_SEND_FAIL).toString());
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_WX_RECORD_SEND_FAILED);
                }
            }
        }

        @Override
        protected void showChat(WechatContactV2 session, List<WechatMessageV2> msgList) {
            if (BuildConfig.LOG_DEBUG) {
                LogUtil.logd("showChat:" + session.getId() + "；name:" + session.getNickName() + "msg:" + msgList.size());
            } else {
                LogUtil.logd("showChat");
            }
        }

        @Override
        protected void updateNotifyStatus(boolean enabled) {
            LogUtil.logd("updateNotifyStatus:" + enabled);
        }

        @Override
        protected void updateContactList(List<WechatContactV2> list) {
            LogUtil.logd("updateContactList:" + list.size());
            mContactsId.clear();
            mContactNotify.clear();
            for (WechatContactV2 contract : list) {
                if (contract != null) {
                    updateContactsId(contract.getId());
                    synchronized (mContactNotify) {
                        mContactNotify.put(contract.getId(), contract.notifyMsg());
                    }
                }
            }
            if (LaunchManager.getInstance().isLaunchResume()) {
                for (WechatStateListener listener :
                        listeners) {
                    listener.updateContacts();
                }
            }
        }

        @Override
        protected void updateMessageList(WechatContactV2 session, List<WechatMessageV2> msgList) {
            LogUtil.logd("updateMessageList");
        }

        @Override
        protected void modContact(WechatContactV2 contact) {

        }

        @Override
        protected void delContact(String id) {

        }
    };


    public CopyOnWriteArrayList<String> getContactsId() {
        return mContactsId;
    }

    public boolean isContactNotify(String contactId) {
        synchronized (mContactNotify) {
            Boolean notify = mContactNotify.get(contactId);
            if (notify != null && !notify) {
                return false;
            }
        }
        return true;
    }

    public static final int SHOW_WECHAT_CONTRACTS_COUNT = 6;

    private void updateContactsId(String senderId) {

        mContactsId.remove(senderId);
        mContactsId.add(0, senderId);

        if (mContactsId.size() > SHOW_WECHAT_CONTRACTS_COUNT) {
            for (int i = SHOW_WECHAT_CONTRACTS_COUNT; i < mContactsId.size(); i++) {
                mContactsId.remove(i);
            }
        }
    }

    /**
     * 获取头像的简单缓存
     *
     * @param userId
     * @param listener
     */
    public void getUsericon(String userId, final TXZWechatManagerV2.ImageListener listener) {
        String imgPath = mUserIconCache.get(userId);
        if (imgPath != null) {
            listener.onImageReady(userId, imgPath);
        } else {
            LogUtil.logd("getUsericon userId=" + userId);
            TXZWechatManagerV2.getInstance().getUsericon(userId, new TXZWechatManagerV2.ImageListener() {
                @Override
                public void onImageReady(String id, String imgPath) {
                    LogUtil.logd("getUsericon ready, id=" + id + ", imgPath=" + imgPath);
                    mUserIconCache.put(id, imgPath);
                    listener.onImageReady(id, imgPath);
                }
            });
        }

    }

    private CopyOnWriteArrayList<WechatStateListener> listeners = new CopyOnWriteArrayList<WechatStateListener>();

    public interface WechatStateListener {
        void dismissNotify();

        void updateNotify(WechatMsgData msgData);

        void updateContacts();
    }

    public boolean addWechatStateListener(WechatStateListener listener) {
        if (listeners.contains(listener)) {
            return false;
        }
        listeners.add(listener);
        return true;
    }

    public boolean removeWechatStateListener(WechatStateListener listener) {
        if (!listeners.contains(listener)) {
            return false;
        }
        listeners.remove(listener);
        return true;
    }

    @Override
    public void init() {
        super.init();
        TXZWechatManagerV2.getInstance().setWechatTool(mTXZWechatTool, true);
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (TXZWechatManagerV2.getInstance().getLoginStatus()) {
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_WX_LOGIN);
                    TXZWechatManagerV2.getInstance().syncContactList(SHOW_WECHAT_CONTRACTS_COUNT);
                }
            }
        }, 1000);
        //替换掉登陆成功的播报
        TXZWechatManagerV2.getInstance().setVoiceText("WECHAT_TIP_LOGIN_SUCCESS_TIP", "微信助手登陆成功");
        TXZWechatManagerV2.getInstance().setVoiceText("KEY_TIP_LOGIN_SUCCESS_INTRO_AUTO_LOGIN", "微信助手登陆成功");
    }

    private boolean isLaunchWechatQr;
    private boolean isVoiceDismiss;

    private void showWechatQr(){
        if (isLaunchWechatQr && isVoiceDismiss) {
            isLaunchWechatQr = false;
            isVoiceDismiss = false;
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    LaunchManager.getInstance().launchWechatQr(new JSONBuilder()
                            .put("type", TYPE_WECHAT_SHOW_QRCODE)
                            .toString());
                }
            }, 500);
        }
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{
                EventTypes.EVENT_VOICE_DISMISS,
                EventTypes.EVENT_VOICE_OPEN,
                EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP
        };
    }

    @Override
    protected void onEvent(String eventType) {
        switch (eventType) {
            case EventTypes.EVENT_VOICE_DISMISS:
                isVoiceDismiss = true;
                showWechatQr();
                break;
            case EventTypes.EVENT_VOICE_OPEN:
                isVoiceDismiss = false;
                break;
            case EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP:
                TXZWechatManager.getInstance().exit(); // 休眠时退出微信
                break;
        }
    }

    //TODO 新的接口去掉了showQR的回调，只会回调updateQR
//    @Override
//    public void showQR(String url) {
//        LogUtil.logd("showQR:" + url);
//        LaunchManager.getInstance().launchWechatQr(new JSONBuilder()
//                .put("type", TYPE_WECHAT_SHOW_QRCODE)
//                .put("data", url).toString());
//    }

    private boolean isBusy; // 是否播报繁忙(处于播报中，接收回复指令中)

    public boolean isWechatBusy() {
        return isBusy;
    }
}