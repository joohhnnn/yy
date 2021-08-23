package com.txznet.comm.notification;

public class WxNotificationInfo extends NotificationInfo {
    public String openId;
    public String nick;
    public boolean hasSpeak;

    public WxNotificationInfo() {
        super(NOTIFICATION_TYPE_WX);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WxNotificationInfo that = (WxNotificationInfo) o;
        return this.msgId.equals(that.msgId) || this.openId.equals(that.openId);
    }

    @Override
    public int hashCode() {
        return openId.hashCode();
    }

    @Override
    public String toString() {
        return "WxNotificationInfo{" +
                "type=" + type +
                ", openId='" + openId + '\'' +
                ", nick='" + nick + '\'' +
                ", hasSpeak=" + hasSpeak +
                '}';
    }
}
