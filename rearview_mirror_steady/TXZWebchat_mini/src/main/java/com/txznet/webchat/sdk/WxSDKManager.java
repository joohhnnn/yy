package com.txznet.webchat.sdk;

import android.os.Parcel;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.sdk.wechat.InvokeConstants;
import com.txznet.webchat.log.L;
import com.txznet.webchat.sdk.base.IWxSDKInvoker;
import com.txznet.webchat.sdk.v1.WxSDKInvokerV1;
import com.txznet.webchat.sdk.v2.WxSDKInvokerV2;
import com.txznet.webchat.sp.WebChatSp;
import com.txznet.webchat.stores.TXZBindStore;
import com.txznet.webchat.ui.base.UIHandler;

/**
 * 微信SDK Manager
 * <p>
 * 管理微信SDK相关状态, 提供sdk相关状态转发和处理逻辑
 * NOTE:
 * SDK中的微信联系人id需要做加密处理, 约定SDK Manager通过IWxSDKInvoker发起SDK调用时不关注id加密逻辑, 统一由
 * 对应的IWxSDKInvoker对id进行加密处理; 对于SDK端对车载微信发起的调用, 联系人id已加密处理, 由SDK Manager对
 * id进行解密.
 * in short: SDK Manager只处理id解密, 不处理id加密
 * <p>
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
        notifyQRCode();
    }

    private boolean isSDKEnabled() {
        return !TextUtils.isEmpty(mRemotePackageName);
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
        mSDKInvoker.invokeQrCode(TXZBindStore.get().getBindUrl());
    }

    /**
     * 处理微信sdk发起的ipc调用
     *
     * @param pkgName 调用端包名
     * @param command 命令
     * @param data    调用数据
     * @return 调用结果
     */
    public byte[] onSdkInvoke(String pkgName, String command, byte[] data) {
        byte[] ret = null;
        L.d(LOG_TAG, "onSdkInvoke:pkg = " + pkgName + ", cmd = " + command);
        if (InvokeConstants.SDK_CMD_DOWNLOAD_AVATAR.equals(command)) {
        } else if (InvokeConstants.SDK_CMD_REFRESH_QR.equals(command)) {
            notifyQRCode();
        } else if (InvokeConstants.SDK_CMD_GET_LOGIN_STATUS.equals(command)) {
            ret = "false".getBytes();
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
        } else if (InvokeConstants.SDK_CMD_SYNC_MESSAGE.equals(command)) {
        } else if (InvokeConstants.SDK_CMD_SET_NOTIFICATION_ENABLED.equals(command)) {
        } else if (InvokeConstants.SDK_CMD_SET_RECORD_WINDOW_ENABLED.equals(command)) {
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
