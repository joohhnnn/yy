package com.txznet.txz.notification;

import android.content.Context;
import android.util.Log;

import com.txznet.comm.notification.NotificationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.ui.win.record.RecorderWin;

public class NotificationManager {
    private static NotificationManager sInstance;

    private Context mContext;
    private INotification mCurrentNotification;
    private NotificationInfo mLastNotificationInfo;

    private NotificationManager(Context context) {
        mContext = context;
    }

    public static NotificationManager getInstance() {
        if (sInstance == null) {
            synchronized (NotificationManager.class) {
                if (sInstance == null) {
                    sInstance = new NotificationManager(GlobalContext.get());
                }
            }
        }
        return sInstance;
    }

    public void notifyMessage(NotificationInfo notificationInfo) {
        if (mCurrentNotification != null) {
            mCurrentNotification.notifyMessage(notificationInfo);
        }
    }

    public void notify(NotificationInfo notificationInfo) {
    	 RecorderWin.dismiss();
        cancel();
        switch (notificationInfo.type) {
		case NotificationInfo.NOTIFICATION_TYPE_TRAFFIC:
			mCurrentNotification = new TrafficNotification(mContext, notificationInfo);
			break;
		case NotificationInfo.NOTIFICATION_TYPE_WX:
			mCurrentNotification = new Notification(mContext, notificationInfo);
			break;
		default:
			mCurrentNotification = new Notification(mContext, notificationInfo);
			break;
		}
        
       
        mCurrentNotification.show();
    }

    public void cancel() {
        if (mCurrentNotification != null) {
            mCurrentNotification.dismiss();
            mCurrentNotification = null;
        }
    }

    public byte[] invokeTXZNotification(String packageName, final String command, final byte[] data) {
    	AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (command.equals("txz.notification.notify")) {
                    // TODO 增加类似PendingIntent的机制来传递控件点击后的行为
                    try {
                        JSONBuilder doc = new JSONBuilder(data);
                        String msgId = doc.getVal("msgId", String.class);
                        int type = doc.getVal("type", Integer.class, 1);
                        boolean hasSpeak = doc.getVal("hasSpeak", Boolean.class);
                        NotificationInfo info = null;
                        
                        if (type == NotificationInfo.NOTIFICATION_TYPE_WX) {
                            String nick = doc.getVal("nick", String.class);
                            String openId = doc.getVal("openId", String.class);
                            
                            
                            info = new NotificationInfo.WxBuilder().setMsgId(msgId).setNick(nick).setOpenId(openId).setSpeak(hasSpeak).build();
     
                        }
                        
                        if(mLastNotificationInfo != null && mLastNotificationInfo.equals(info)){
                        	NotificationManager.this.notifyMessage(info);
                        	mLastNotificationInfo = info;
                        }else if(hasSpeak){ // 只在播报开始的条件下启动新Notification，防止已取消的Notification被播报结束的消息重新打开
                        	NotificationManager.this.notify(info);
                        	mLastNotificationInfo = info;
                        }
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (command.equals("txz.notification.cancel")) {
                    try {
                        if (data == null) {
                            cancel();
                            mLastNotificationInfo = null;
                        } else {
                            JSONBuilder doc = new JSONBuilder(data);
                            String msgId = doc.getVal("msgId", String.class);
                            if (mLastNotificationInfo != null && mLastNotificationInfo.msgId.equals(msgId)) {
                                cancel();
                                mLastNotificationInfo = null;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0);
        return null;
    }
}
