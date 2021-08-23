package com.txznet.webchat.comm.plugin.base;

import com.txznet.txz.plugin.PluginManager;

/**
 * 微信插件公共基类
 * Created by J on 2016/8/15.
 */
public abstract class WxPlugin {
    protected static final String CMD_INVOKE_DISPATCH_EVENT = "wx.cmd.dispatch_event";
    protected static final String CMD_INVOKE_INVOKE_PLUGIN = "wx.cmd.invoke_plugin";

    /**
     * 返回插件版本
     *
     * @return
     */
    public abstract int getVersionCode();

    /**
     * 返回插件版本名称
     *
     * @return
     */
    public abstract String getVersionName();

    /**
     * 获取插件Token
     *
     * Token用于作为区分不同插件的唯一识别码, 需要与其他插件区分
     *
     * @return 插件唯一标识
     */
    public abstract String getToken();

    /**
     * 备份数据，用于插件热替换或进程重启需要数据持久化的场景
     *
     * @return
     */
    public abstract Object backup();

    /**
     * 恢复数据，用于插件热替换或进程重启需要数据持久化的场景
     *
     * @param data 用于恢复的数据
     * @return 恢复结果
     */
    public abstract boolean restore(Object data);

    /**
     * 外部调用接口，执行指定操作
     *
     * @param cmd  操作码
     * @param args 参数
     */
    public abstract void invoke(String cmd, Object... args);

    protected void dispatchEvent(String action, Object data) {
        PluginManager.invoke(CMD_INVOKE_DISPATCH_EVENT, action, data);
    }

    protected void invokePlugin(String cmd, Object data) {
        PluginManager.invoke(CMD_INVOKE_INVOKE_PLUGIN, "", cmd, data);
    }
}
