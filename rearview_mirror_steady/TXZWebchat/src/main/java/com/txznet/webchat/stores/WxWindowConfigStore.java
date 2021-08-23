package com.txznet.webchat.stores;

import com.txznet.comm.util.ScreenUtils;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.WxUIConfig;

/**
 * 微信窗口管理Store
 * 储存界面显示相关的Window参数, 对应WindowManager.LayoutParams
 * <p>
 * Created by J on 2017/5/26.
 */

public class WxWindowConfigStore extends Store {
    public static final String EVENT_TYPE_ALL = "wx_window_config_store";
    private static final String LOG_TAG = "wx_window_config_store";

    private WxUIConfig mUIConfig = null;

    // ----------SingleInstance
    public static WxWindowConfigStore sInstance;

    public static WxWindowConfigStore getInstance() {
        if (null == sInstance) {
            synchronized (WxWindowConfigStore.class) {
                if (null == sInstance) {
                    sInstance = new WxWindowConfigStore();
                }
            }
        }

        return sInstance;
    }

    private WxWindowConfigStore() {
        super(Dispatcher.get());
    }
    // ----------eof SingleInstance

    /**
     * 获取当前界面显示参数
     * @return 显示参数, null代表默认显示(全屏)
     */
    public WxUIConfig getUIConfig() {
        return mUIConfig;
    }

    //// TODO: 2017/6/15 Store不应该有公有的set方法, 此处是为了解决界面启动时事件总线还未将uiConfig更新事件传递到Store导致的时序问题, 待解决

    /**
     * 更新ui设置
     *
     * @param config
     */
    public void updateUIConfig(WxUIConfig config) {
        doWindowParamChanged(config);
        emitChange(EVENT_TYPE_ALL);
    }


    @Override
    public void onDispatch(Action action) {
        boolean changed = false;

        switch (action.getType()) {
            case ActionType.WX_WINDOW_PARAM_CHANGED:
                changed = doWindowParamChanged((WxUIConfig) action.getData());
                break;
        }

        if (changed) {
            emitChange(EVENT_TYPE_ALL);
        }
    }

    private boolean doWindowParamChanged(WxUIConfig newConfig) {
        if (null == newConfig || !newConfig.isInValid()) {
            return false;
        }

        if (null == mUIConfig || !mUIConfig.equals(newConfig)) {
            L.i(LOG_TAG, "change UI config to: " + newConfig.toString());
            mUIConfig = newConfig;
            ScreenUtils.updateScreenSize(mUIConfig.width, mUIConfig.height, true);
            return true;
        }

        return false;
    }
}
