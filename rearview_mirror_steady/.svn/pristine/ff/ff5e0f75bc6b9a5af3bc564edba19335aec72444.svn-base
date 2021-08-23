package com.txznet.webchat.plugin.preset.logic.base;

import com.txznet.txz.plugin.PluginManager;

/**
 * Created by J on 2016/11/17.
 */

public abstract class WxModule {
    private static final String CMD_INVOKE_DISPATCH_EVENT = "wx.cmd.dispatch_event";

    public WxModule() {
    }

    /**
     * 返回Module版本
     * @return
     */
    public abstract int getVersion();

    /**
     * 获取Module Token
     * @return
     */
    public abstract String getToken();

    public abstract void reset();

    protected void dispatchEvent(String action, Object data){
        PluginManager.invoke(CMD_INVOKE_DISPATCH_EVENT, action, data);
    }
}
