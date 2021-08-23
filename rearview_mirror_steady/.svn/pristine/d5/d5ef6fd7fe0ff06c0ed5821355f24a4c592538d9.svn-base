package com.txznet.txz.notification;

import com.txznet.comm.notification.NotificationInfo;
import com.txznet.comm.util.ScreenLock;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;

public abstract class INotification extends LinearLayout{
	
	abstract public void initView(Context context);
	abstract public void notifyMessage(NotificationInfo notificationInfo);
    
    protected NotificationInfo mNotificationInfo;
    protected WindowManager mWinManager;
    protected boolean mAlreadyOpen;
    protected int mWidth, mHeight;
    protected ScreenLock mScreenLock;

    public INotification(Context context, NotificationInfo notificationInfo) {
        super(context);
        mWinManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mNotificationInfo = notificationInfo;
        mScreenLock = new ScreenLock(getContext());
    }
    public void dismiss() {
        if (mAlreadyOpen) {
            mWinManager.removeView(this);
            mAlreadyOpen = false;
            mScreenLock.release();
        }
    }
    public void show() {
        if (mAlreadyOpen) {
            return;
        }
        WindowManager.LayoutParams layoutParam = new WindowManager.LayoutParams();
        layoutParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParam.width = mWidth;
        layoutParam.height = mHeight;
        layoutParam.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParam.format = PixelFormat.RGBA_8888;
        layoutParam.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        mWinManager.addView(this, layoutParam);
        mAlreadyOpen = true;
        mScreenLock.lock();
    }
}
