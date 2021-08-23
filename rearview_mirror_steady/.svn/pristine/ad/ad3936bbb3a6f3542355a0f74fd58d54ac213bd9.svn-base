package com.txznet.webchat.model;

/**
 * 后台下发的微信插件信息
 * 用于json解析
 * Created by J on 2017/7/14.
 */

public class WxPluginInfo {
    public String name = "";
    public String version = "1.0.0";
    public String url = "";
    public String md5 = "";

    public WxPluginInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public WxPluginInfo() {

    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || !(obj instanceof WxPluginInfo)) {
            return false;
        }

        WxPluginInfo info = (WxPluginInfo) obj;
        return (name.equals(info.name)
                && version.equals(info.version)
                && url.equals(info.url)
                && md5.equals(info.md5));
    }

    @Override
    public String toString() {
        return String.format("WxPluginInfo[%s: %s]", name, version);
    }
}