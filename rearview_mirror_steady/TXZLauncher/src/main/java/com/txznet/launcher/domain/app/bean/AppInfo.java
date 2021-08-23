package com.txznet.launcher.domain.app.bean;

/**
 * App信息数据类
 */
public class AppInfo {
    public String appName;
    public String pkgName;
    public String versionName;

    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", versionName='" + versionName + '\'' +
                '}';
    }
}
