package com.txznet.sdk;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.WechatBindInfo;

/**
 * 微信绑定状态Manager
 *
 * 提供绑定状态相关接口, 允许获取当前绑定用户信息及绑定二维码等状态,
 * 同时允许设置listener监听绑定状态变化
 *
 * Created by J on 2018/4/24.
 */

public final class TXZBindManager {
    private static final String LOG_TAG = "TXZBindManager::";
    private static final String BIND_STATUS_INVOKE_PREFIX = "wx.info.";

    // 绑定状态相关的调用命令
    private static final String INVOKE_CMD_QRCODE = "qrcode";
    private static final String INVOKE_CMD_NICK = "nick";
    // 绑定用户信息
    private String mBindQrUrl = "";
    private String mBindUserNick = "";
    private boolean bHasBind;
    private String mHeadUrl = "";

    // listener
    private OnBindStatusChangeListener mBindStatusChangeListener;

    /**
     * 设置绑定状态变化listener
     *
     * @param listener
     */
    public void setOnBindStatusChangeListener(OnBindStatusChangeListener listener) {
        this.mBindStatusChangeListener = listener;
    }

    /**
     * 移除绑定状态变化listener
     */
    public void clearOnBindStatusChangeListener() {
        this.mBindStatusChangeListener = null;
    }

    /**
     * 获取当前绑定状态
     *
     * @return
     */
    public WechatBindInfo getBindStatus() {
        return new WechatBindInfo(bHasBind, mBindQrUrl, mBindUserNick, mHeadUrl);
    }

    /**
     * 强制刷新绑定状态
     */
    public void refreshBindStatus() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "wx.subscribe.qrcode", null,
                null);
    }

    protected void updateBindStatus(boolean hasBind, String bindUserNick, String qrUrl, String headUrl) {
        this.bHasBind = hasBind;
        this.mBindUserNick = bindUserNick;
        this.mHeadUrl = headUrl;

        if (!TextUtils.isEmpty(qrUrl)) {
            this.mBindQrUrl = qrUrl;
        }

        // 通知listener绑定信息变化
        if (null != mBindStatusChangeListener) {
            mBindStatusChangeListener.onBindStatusChanged(new WechatBindInfo(hasBind,
                    qrUrl, bindUserNick, headUrl));
        }
    }

    public interface OnBindStatusChangeListener {
        void onBindStatusChanged(WechatBindInfo newBindInfo);
    }

    private TXZService.CommandProcessor mCommandProcessor = new TXZService.CommandProcessor() {
        @Override
        public byte[] process(final String packageName, final String command, final byte[] data) {
            if (INVOKE_CMD_QRCODE.equals(command)) {
                resolveBindStatus(new JSONBuilder(data));
            } else if (INVOKE_CMD_NICK.equals(command)) {
                resolveBindNick(data);
            }

            return null;
        }

        private void resolveBindStatus(JSONBuilder builder) {
            try {
                String qrUrl = builder.getVal("qrcode", String.class);
                boolean hasBind = builder.getVal("isbind", boolean.class);
                String userNick = hasBind ? builder.getVal("nick", String.class) : "";
                String headUrl = hasBind ? builder.getVal("headurl", String.class) : "";

                updateBindStatus(hasBind, userNick, qrUrl, headUrl);
            } catch (Exception e) {
                LogUtil.loge(LOG_TAG + "resolveBindStatus encountered error: " + e.toString());
                e.printStackTrace();
            }
        }

        private void resolveBindNick(byte[] data) {
            String userNick = new String(data);
            LogUtil.loge(LOG_TAG + "resolveBindNick nick = " + userNick);

            updateBindStatus(true, userNick, "", "");
        }
    };

    //----------- single instance -----------
    private static volatile TXZBindManager sInstance;

    public static TXZBindManager getInstance() {
        if (null == sInstance) {
            synchronized (TXZBindManager.class) {
                if (null == sInstance) {
                    sInstance = new TXZBindManager();
                }
            }
        }

        return sInstance;
    }

    private TXZBindManager() {
        TXZService.setCommandProcessor(BIND_STATUS_INVOKE_PREFIX, mCommandProcessor);

        refreshBindStatus();
    }
    //----------- single instance -----------
}
