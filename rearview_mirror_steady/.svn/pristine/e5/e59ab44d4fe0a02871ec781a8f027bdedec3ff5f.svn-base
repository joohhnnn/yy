package com.txznet.webchat.model;

/**
 * 后台下发配置json bean
 * Created by J on 2017/7/14.
 */
public class WxServerConfig {
    public int pushLoginEnabled;
    public WxPluginInfo[] plugin;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WxServerConfig {pushLoginEnabled = ");
        sb.append(pushLoginEnabled);
        sb.append(", pluginlist = ");

        if (null != plugin) {
            for (WxPluginInfo pi : plugin) {
                sb.append("[");
                sb.append(pi.name);
                sb.append(": ");
                sb.append(pi.version);
                sb.append("] ");
            }
        }

        sb.append("}");

        return sb.toString();
    }
}
