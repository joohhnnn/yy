package com.txznet.webchat.sdk;

import android.content.Intent;
import android.os.Parcel;
import android.text.TextUtils;

import com.txznet.comm.notification.WxNotificationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.sdk.wechat.InvokeConstants;
import com.txznet.webchat.actions.LoginActionCreator;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.log.L;
import com.txznet.webchat.sdk.base.IWxSDKInvoker;
import com.txznet.webchat.sdk.v1.WxSDKInvokerV1;
import com.txznet.webchat.sdk.v2.WxSDKInvokerV2;
import com.txznet.webchat.sp.WebChatSp;
import com.txznet.webchat.stores.TXZRecordStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.stores.WxQrCodeStore;
import com.txznet.webchat.stores.WxResourceStore;
import com.txznet.webchat.ui.base.UIHandler;
import com.txznet.webchat.util.ContactEncryptUtil;

/**
 * 微信SDK Manager
 *
 * 管理微信SDK相关状态, 提供sdk相关状态转发和处理逻辑
 * NOTE:
 * SDK中的微信联系人id需要做加密处理, 约定SDK Manager通过IWxSDKInvoker发起SDK调用时不关注id加密逻辑, 统一由
 * 对应的IWxSDKInvoker对id进行加密处理; 对于SDK端对车载微信发起的调用, 联系人id已加密处理, 由SDK Manager对
 * id进行解密.
 * in short: SDK Manager只处理id解密, 不处理id加密
 *
 * Created by J on 2018/4/10.
 */

public class WxSDKManager {
    private static final String LOG_TAG = "WxSDKManager";

    private static final String ACTION_IMAGE_READY = "com.txznet.webchat.action" +
            ".SDK_DOWNLOAD_IMG_COMPLETE";
    private static final String ACTION_IMAGE_READY_RAW = "com.txznet.webchat.action" +
            ".DOWNLOAD_IMG_COMPLETE"; // 用于兼容的ImageReady action

    private int mSDKVersion;
    private String mRemotePackageName;
    private IWxSDKInvoker mSDKInvoker;

    /**
     * 为避免在不必要的情况下通知sdk关闭录音界面, 设置一个标志位对录音界面的打开情况进行记录
     */
    private boolean bRecordWinShowing;

    public void setRemotePackageName(String remotePackageName, int version) {
        this.mRemotePackageName = remotePackageName;
        this.mSDKVersion = version;

        if (!TextUtils.isEmpty(mRemotePackageName)) {
            initSDkInvoker();
        }

        WebChatSp sp = WebChatSp.getInstance(GlobalContext.get());
        sp.setRemotePackageName(mRemotePackageName);
        sp.setSDKVersion(version);
    }

    public void clearRemotePackageName() {
        mRemotePackageName = "";
    }

    private void initSDkInvoker() {
        if (1 == mSDKVersion) {
            mSDKInvoker = new WxSDKInvokerV1();
        } else {
            // 早期v2版本的sdk未设置sdk version标志位, 所以可能版本是0
            mSDKInvoker = new WxSDKInvokerV2();
        }

        mSDKInvoker.setRemotePackageName(mRemotePackageName);
        syncLoginStatus();
    }

    /**
     * 与sdk同步当前微信状态
     */
    private void syncLoginStatus() {
        // 设置WechatTool时, 判断是否需要加载二维码
        if (!WxLoginStore.get().isLogin()) {
            notifyLogout();

            // 判断是否需要加载二维码
            if (!WxLoginStore.get().isLogin()) {
                // 未处于登录流程中 && 没有已缓存的二维码 && 没有在请求二维码 的情况下, 主动刷新二维码
                if (!WxQrCodeStore.get().isScanned()
                        && TextUtils.isEmpty(WxQrCodeStore.get().getQrCode())
                        && !WxQrCodeStore.get().isRetrieving()) {
                    LoginActionCreator.get().doWakeup();
                    LoginActionCreator.get().refreshQRCode();
                } else {
                    notifyQRCode();
                }
            }
        } else {
            notifyLogin();
            notifyUserInfo();
        }
    }

    private boolean isSDKEnabled() {
        return !TextUtils.isEmpty(mRemotePackageName);
    }

    /**
     * 加载用户头像
     *
     * @param encryptedId
     */
    public void loadAvatar(String encryptedId) {
        String openId = ContactEncryptUtil.decrypt(encryptedId);
        String imgPath = WxResourceStore.get().getContactHeadImage(openId);

        if (imgPath != null) {
            notifyAvatarReady(encryptedId, imgPath);
        }
    }

    /**
     * 通知sdk指定用户的头像已经下载完成
     *
     * @param id
     * @param path
     */
    public void notifyAvatarReady(String id, String path) {
        Intent intent = new Intent(ACTION_IMAGE_READY);
        intent.putExtra("id", id);
        intent.putExtra("img", path);
        GlobalContext.get().sendBroadcast(intent);

        intent.setAction(ACTION_IMAGE_READY_RAW);
        GlobalContext.get().sendBroadcast(intent);
    }

    /**
     * 微信界面显示
     */
    public void notifyLaunch() {
        if (!isSDKEnabled()) {
            return;
        }

        mSDKInvoker.invokeLaunch();
    }

    /**
     * 更新登录二维码
     */
    public void notifyQRCode() {
        if (!isSDKEnabled()) {
            return;
        }

        if (WxQrCodeStore.get().isRetrieving()) {
            L.d(LOG_TAG, "notifyQRCode: retrieving");
            return;
        }

        if (WxQrCodeStore.get().isQrCodeInvalid()) {
            L.d(LOG_TAG, "notifyQRCode: code invalid");
            return;
        }

        if (WxQrCodeStore.get().isScanned()) {
            mSDKInvoker.invokeQrScanned(WxQrCodeStore.get().getScannerPicStr());
        } else {
            mSDKInvoker.invokeQrCode(WxQrCodeStore.get().getQrCode());
        }
    }

    /**
     * 通知登录成功
     */
    public void notifyLogin() {
        if (!isSDKEnabled()) {
            return;
        }

        mSDKInvoker.invokeLogin();
    }

    /**
     * 通知退出登录
     */
    public void notifyLogout() {
        if (!isSDKEnabled()) {
            return;
        }

        mSDKInvoker.invokeLogout();
    }

    /**
     * 更新登录用户信息
     */
    public void notifyUserInfo() {
        if (!isSDKEnabled()) {
            return;
        }

        mSDKInvoker.invokeLoginUserInfo(WxContactStore.getInstance().getLoginUser());
    }

    /**
     * 显示指定会话消息
     *
     * @param openId 对应回话id
     */
    public void notifyShowChat(String openId) {
        if (!isSDKEnabled()) {
            return;
        }

        mSDKInvoker.invokeShowChat(openId);
    }

    /**
     * 更新录音状态
     */
    public void notifyRecordStatus() {
        if (!isSDKEnabled()) {
            return;
        }

        if (TXZRecordStore.get().isReplying()) {
            bRecordWinShowing = true;
            mSDKInvoker.invokeRecordUpdate(TXZRecordStore.get().getCurVoiceToUser(),
                    TXZRecordStore.get().getCountdown());
        } else if (bRecordWinShowing) {
            bRecordWinShowing = false;
            mSDKInvoker.invokeRecordDismiss(TXZRecordStore.get().getCurVoiceToUser(),
                    !TXZRecordStore.get().isSendError(), TXZRecordStore.get().isReplyCancelled());
        }
    }

    private boolean isNotificationShowing;

    /**
     * 更新消息提示窗口
     *
     * @param show 是否显示
     * @param info 对应提示内容
     */
    public void notifyNotificationUpdate(boolean show, WxNotificationInfo info) {
        if (!isSDKEnabled()) {
            return;
        }

        if (show) {
            mSDKInvoker.invokeNotificationUpdate(info);
            isNotificationShowing = true;
        } else if (isNotificationShowing) {
            // 只有Notification确实在显示时才通知dismiss
            mSDKInvoker.invokeNotificationDismiss();
            isNotificationShowing = false;
        }
    }

    /**
     * 同步联系人列表
     *
     * @param count 同步的数目
     */
    public void notifySyncContact(int count) {
        if (!isSDKEnabled()) {
            return;
        }

        mSDKInvoker.invokeSyncContact(count);
    }

    /**
     * 同步消息列表
     *
     * @param encryptId 对应的联系人id(加密后的)
     * @param count     同步的数目
     */
    public void notifySyncMessage(String encryptId, int count) {
        if (!isSDKEnabled()) {
            return;
        }

        String sessionId = ContactEncryptUtil.decrypt(encryptId);

        if (TextUtils.isEmpty(sessionId)) {
            L.e(LOG_TAG, "notifySyncMessage: cannot resolve id for: " + sessionId);
            return;
        }

        mSDKInvoker.invokeSyncMessage(sessionId, count);
    }

    /**
     * 同步消息播报开关状态
     *
     * @param enabled 是否开启自动播报
     */
    public void notifyMsgBroadcastEnabled(boolean enabled) {
        if (!isSDKEnabled()) {
            return;
        }

        mSDKInvoker.invokeMsgBroadcastEnabled(enabled);
    }

    /**
     * 通知联系人信息变更
     * @param contact
     */
    public void notifyModContact(WxContact contact) {
        if (!isSDKEnabled()) {
            return;
        }

        mSDKInvoker.invokeModContact(contact);
    }

    /**
     * 通知联系人删除
     * @param openId
     */
    public void notifyDelContact(String openId) {
        if (!isSDKEnabled()) {
            return;
        }

        mSDKInvoker.invokeDeleteContact(openId);
    }

    /**
     * 通知sdk开始发送消息
     * @param msg 发送的消息
     */
    public void notifySendMessageStart(WxMessage msg) {
        if (!isSDKEnabled()) {
            return;
        }

        mSDKInvoker.invokeSendMessageStart(msg);
    }

    /**
     * 通知sdk发送消息完毕
     * @param msg 发送的消息
     * @param success 是否发送成功
     */
    public void notifySendMessageResult(WxMessage msg, boolean success) {
        if (!isSDKEnabled()) {
            return;
        }

        mSDKInvoker.invokeSendMessageResult(msg, success);
    }

    /**
     * 处理微信sdk发起的ipc调用
     * @param pkgName 调用端包名
     * @param command 命令
     * @param data 调用数据
     * @return 调用结果
     */
    public byte[] onSdkInvoke(String pkgName, String command, byte[] data) {
        byte[] ret = null;
        L.d(LOG_TAG, "onSdkInvoke:pkg = " + pkgName + ", cmd = " + command);
        if (InvokeConstants.SDK_CMD_DOWNLOAD_AVATAR.equals(command)) {
            Parcel p = Parcel.obtain();
            p.unmarshall(data, 0, data.length);
            p.setDataPosition(0);
            String id = p.readString();
            loadAvatar(id);
            p.recycle();
        } else if (InvokeConstants.SDK_CMD_REFRESH_QR.equals(command)) {
            if (!WxQrCodeStore.get().isRetrieving()) {
                LoginActionCreator.get().doWakeup();
                LoginActionCreator.get().refreshQRCode();
            }
        } else if (InvokeConstants.SDK_CMD_GET_LOGIN_STATUS.equals(command)) {
            ret = String.valueOf(WxLoginStore.get().isLogin()).getBytes();
        } else if (InvokeConstants.SDK_CMD_SET_TOOL.equals(command)) {
            Parcel p = Parcel.obtain();
            p.unmarshall(data, 0, data.length);
            p.setDataPosition(0);
            boolean blockUI = (1 == p.readInt());
            int sdkVersion = p.readInt();
            setRemotePackageName(pkgName, sdkVersion);
            UIHandler.getInstance().setUIEnabled(!blockUI);
            UIHandler.getInstance().setNotificationEnabled(!blockUI);
            UIHandler.getInstance().setRecordWindowEnabled(!blockUI);
            L.i(LOG_TAG, "sdk set, version = " + sdkVersion + ", blockUI = " + blockUI);
            p.recycle();
        } else if (InvokeConstants.SDK_CMD_CLEAR_TOOL.equals(command)) {
            clearRemotePackageName();
            UIHandler.getInstance().setUIEnabled(true);
            UIHandler.getInstance().setNotificationEnabled(true);
            UIHandler.getInstance().setRecordWindowEnabled(true);
        } else if (InvokeConstants.SDK_CMD_SYNC_CONTACT.equals(command)) {
            Parcel p = Parcel.obtain();
            p.unmarshall(data, 0, data.length);
            p.setDataPosition(0);
            int count = p.readInt();
            notifySyncContact(count);
            p.recycle();
        } else if (InvokeConstants.SDK_CMD_SYNC_MESSAGE.equals(command)) {
            Parcel p = Parcel.obtain();
            p.unmarshall(data, 0, data.length);
            p.setDataPosition(0);
            String session = p.readString();
            int count = p.readInt();
            notifySyncMessage(session, count);
            p.recycle();
        } else if (InvokeConstants.SDK_CMD_SET_NOTIFICATION_ENABLED.equals(command)) {
            Parcel p = Parcel.obtain();
            p.unmarshall(data, 0, data.length);
            p.setDataPosition(0);
            boolean enabled = p.readInt() == 1;
            UIHandler.getInstance().setNotificationEnabled(enabled);
            p.recycle();
        } else if (InvokeConstants.SDK_CMD_SET_RECORD_WINDOW_ENABLED.equals(command)) {
            Parcel p = Parcel.obtain();
            p.unmarshall(data, 0, data.length);
            p.setDataPosition(0);
            boolean enabled = p.readInt() == 1;
            UIHandler.getInstance().setRecordWindowEnabled(enabled);
            p.recycle();
        }

        return ret;
    }

    //----------- single instance -----------
    private static volatile WxSDKManager sInstance;

    public static WxSDKManager getInstance() {
        if (null == sInstance) {
            synchronized (WxSDKManager.class) {
                if (null == sInstance) {
                    sInstance = new WxSDKManager();
                }
            }
        }

        return sInstance;
    }

    private WxSDKManager() {
        // 读取缓存的sdk配置
        mRemotePackageName = WebChatSp.getInstance(GlobalContext.get()).getRemotePackageName();
        L.i(LOG_TAG, "cached remote package name: " + mRemotePackageName);

        if (!TextUtils.isEmpty(mRemotePackageName)) {
            mSDKVersion = WebChatSp.getInstance(GlobalContext.get()).getSDKVersion();
            initSDkInvoker();
        }
    }
    //----------- single instance -----------
}
