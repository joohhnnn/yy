package com.txznet.comm.notification;

public abstract class NotificationInfo {
    public static final int NOTIFICATION_TYPE_BASE = 0;
    public static final int NOTIFICATION_TYPE_WX = 1;
    public static final int NOTIFICATION_TYPE_TRAFFIC= 2;
    public String msgId;
    public int type;

    public NotificationInfo(int type) {
        this.type = type;
    }

    abstract static class Builder {
        protected NotificationInfo mNotificationInfo;

        public Builder(int type) {
            switch (type) {
                case NOTIFICATION_TYPE_WX:
                    mNotificationInfo = new WxNotificationInfo();
                    break;
                case NOTIFICATION_TYPE_TRAFFIC:
                	mNotificationInfo = new TrafficNotificationInfo();
                	break;
                default:
                    mNotificationInfo = new BaseNotificationInfo();
                    break;
            }
        }

        public abstract Builder setMsgId(String msgId);

        public NotificationInfo build() {
            return mNotificationInfo;
        }
    }

    public static class WxBuilder extends NotificationInfo.Builder {

        public WxBuilder() {
            super(NOTIFICATION_TYPE_WX);
        }

        public WxBuilder setMsgId(String msgId) {
            mNotificationInfo.msgId = msgId;
            return this;
        }

        public WxBuilder setOpenId(String openId) {
            if (mNotificationInfo instanceof WxNotificationInfo) {
                ((WxNotificationInfo) mNotificationInfo).openId = openId;
            }
            return this;
        }

        public WxBuilder setNick(String nick) {
            if (mNotificationInfo instanceof WxNotificationInfo) {
                ((WxNotificationInfo) mNotificationInfo).nick = nick;
            }
            return this;
        }

        public WxBuilder setSpeak(boolean hasSpeak) {
            if (mNotificationInfo instanceof WxNotificationInfo) {
                ((WxNotificationInfo) mNotificationInfo).hasSpeak = hasSpeak;
            }
            return this;
        }
    }
    
    public static class TrafficBuilder extends NotificationInfo.Builder {

        public TrafficBuilder() {
            super(NOTIFICATION_TYPE_TRAFFIC);
        }

		@Override
		public TrafficBuilder setMsgId(String msgId) {
			mNotificationInfo.msgId = msgId;
			return this;
		}
		
		public TrafficBuilder setRecorderStatus(boolean isRecording){
			if(mNotificationInfo instanceof TrafficNotificationInfo){
				((TrafficNotificationInfo)mNotificationInfo).isRecording=isRecording;
			}
			return this;
		}
		public TrafficBuilder setTrafficInfo(String trafficDetail){
			if(mNotificationInfo instanceof TrafficNotificationInfo){
				((TrafficNotificationInfo)mNotificationInfo).trafficDetail=trafficDetail;
			}
			return this;
		}
    }
}
