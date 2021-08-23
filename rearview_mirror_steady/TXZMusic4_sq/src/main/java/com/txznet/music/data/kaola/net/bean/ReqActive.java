package com.txznet.music.data.kaola.net.bean;

/**
 * Created by telenewbie on 2018/2/5.
 */

public class ReqActive {
    private String appid;//是	应用id
    private String sign;//是	签名
    private String deviceid;//是	设备标识
    private String packagename;//是	包名
    private String os;//是	android,ios,web,other

    private String version;//是	应用版本号
    private String osversion;//是	系统版本号
    private int devicetype;//是	设备类型 0-Andriod，1-IOS，2-其他

    public ReqActive(String appid, String sign, String deviceid, String packagename, String os) {
        this.appid = appid;
        this.sign = sign;
        this.deviceid = deviceid;
        this.packagename = packagename;
        this.os = os;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getPackagename() {
        return packagename;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOsversion() {
        return osversion;
    }

    public void setOsversion(String osversion) {
        this.osversion = osversion;
    }

    public int getDevicetype() {
        return devicetype;
    }

    public void setDevicetype(int devicetype) {
        this.devicetype = devicetype;
    }

    @Override
    public String toString() {
        return "ReqActive{" +
                "appid='" + appid + '\'' +
                ", sign='" + sign + '\'' +
                ", deviceid='" + deviceid + '\'' +
                ", packagename='" + packagename + '\'' +
                ", os='" + os + '\'' +
                ", version='" + version + '\'' +
                ", osversion='" + osversion + '\'' +
                ", devicetype=" + devicetype +
                '}';
    }
}
