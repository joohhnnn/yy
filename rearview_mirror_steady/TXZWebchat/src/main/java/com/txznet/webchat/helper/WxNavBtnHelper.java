package com.txznet.webchat.helper;

import com.txznet.sdk.TXZWheelControlManager;
import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.webchat.stores.WxConfigStore;

/**
 * 导航按键帮助类，用于管理导航按键相关状态信息
 * Created by J on 2017/2/13.
 */

public class WxNavBtnHelper {
    private static WxNavBtnHelper sInstance = new WxNavBtnHelper();

    private WxNavBtnHelper() {
        initNavBtnDevice();
    }

    public static WxNavBtnHelper getInstance() {
        return sInstance;
    }

    private boolean bNativeNavBtnTriggered = false; // 原生导航按键有没有被使用过
    private boolean bRemoteControllerConnected = false; // 是否有方控设备连接

    /**
     * 获取当前配置是否支持方控按键
     *
     * @return
     */
    public boolean isDpadSupportEnabled() {
        return WxConfigStore.getInstance().getDpadSupportEnabled();
    }

    /**
     * 设置原生导航按键是否被使用过，针对每次应用打开设置有效
     * 即每次应用重新打开时重置
     * @param triggered
     */
    public void setNativeNavBtnTriggered(boolean triggered) {
        bNativeNavBtnTriggered = triggered;
    }

    /**
     * 导航按键是否被触发过，用于确定是否要显示默认的导航按键焦点指示
     * @return
     */
    public boolean isNavBtnTriggered() {
        return bNativeNavBtnTriggered || bRemoteControllerConnected;
    }

    /**
     * 获取当前设备配置的方控交互模式
     *
     * @return 当前设备设置的方控交互模式
     * @see com.txznet.txz.util.focus_supporter.FocusSupporter#NAV_MODE_ONE_WAY
     * @see com.txznet.txz.util.focus_supporter.FocusSupporter#NAV_MODE_TWO_WAY
     */
    public int getNavMode() {
        return FocusSupporter.NAV_MODE_ONE_WAY;
    }

    /**
     * 初始化方控设备监听
     */
    private void initNavBtnDevice() {

        TXZWheelControlManager.getInstance().setConnectionStatusLinstener(new TXZWheelControlManager.OnConnectionStatusLinstener() {
            @Override
            public void isConnected(boolean isConnected) {
                bRemoteControllerConnected = isConnected;
            }
        });

    }
}
