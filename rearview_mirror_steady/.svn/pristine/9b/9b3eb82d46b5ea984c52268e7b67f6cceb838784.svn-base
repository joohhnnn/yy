
package com.txznet.comm.notification;

public class TrafficNotificationInfo extends NotificationInfo {
    public boolean isRecording;
    public String trafficDetail;

    public TrafficNotificationInfo() {
        super(NOTIFICATION_TYPE_TRAFFIC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrafficNotificationInfo that = (TrafficNotificationInfo) o;
        if(this.trafficDetail==that.trafficDetail)
        	return true;
        if(this.trafficDetail!=null&&that.trafficDetail!=null)
        	return this.trafficDetail.equals(that.trafficDetail);
        return false;
    }


    @Override
    public String toString() {
        return "TrafficNotificationInfo{" +
                "trafficDetail=" + trafficDetail +
                '}';
    }
}