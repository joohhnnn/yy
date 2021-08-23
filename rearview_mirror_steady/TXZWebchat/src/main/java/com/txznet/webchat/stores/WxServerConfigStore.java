package com.txznet.webchat.stores;

import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.model.WxServerConfig;
import com.txznet.webchat.sp.WxServerConfigSp;

/**
 * 存储服务器下发的设置项
 * Created by J on 2017/7/13.
 */

public class WxServerConfigStore extends Store {
    public static final String EVENT_TYPE_ALL = "wx_server_config_store";

    private int mPushLoginEnableStatus;

    private WxServerConfigStore(Dispatcher dispatcher) {
        super(dispatcher);

        refreshPushLoginEnableStatus();
    }

    //  single instance
    private static WxServerConfigStore sInstance;

    public static WxServerConfigStore getInstance() {
        if (null == sInstance) {
            synchronized (WxServerConfigStore.class) {
                if (null == sInstance) {
                    sInstance = new WxServerConfigStore(Dispatcher.get());
                }
            }
        }

        return sInstance;
    }

    // eof single instance

    @Override
    public void onDispatch(Action action) {
        boolean changed = false;
        switch (action.getType()) {
            case ActionType.WX_SERVER_CONFIG_CHANGED:
                changed = doServerConfigChanged((WxServerConfig) action.getData());
                break;

            case ActionType.WX_LOGOUT_REQUEST:
                refreshPushLoginEnableStatus();
                break;

        }

        if (changed) {
            emitChange(EVENT_TYPE_ALL);
        }
    }

    /**
     * 微信免扫码开关改变时暂存到sp, 推迟到微信退出登录时进行响应, 避免微信一次使用过程中出现交互不一致的现象
     *
     * @param newConfig
     * @return
     */
    private boolean doServerConfigChanged(WxServerConfig newConfig) {
        WxServerConfigSp.getInstance().setPushLoginEnabled(newConfig.pushLoginEnabled);

        return false;
    }

    /**
     * 从sp缓存中更新免扫码开关状态
     */
    private void refreshPushLoginEnableStatus() {
        // 读取本地缓存的免扫码开启状态
        mPushLoginEnableStatus = WxServerConfigSp.getInstance().getPushLoginEnabled();
    }

    public boolean isPushLoginEnabled() {
        return mPushLoginEnableStatus != 0;
    }
}
