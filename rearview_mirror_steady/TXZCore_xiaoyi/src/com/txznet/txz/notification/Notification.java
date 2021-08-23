package com.txznet.txz.notification;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.notification.NotificationInfo;
import com.txznet.comm.notification.WxNotificationInfo;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ScreenLock;
import com.txznet.txz.R;
import com.txznet.txz.util.AnimDrawableUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class Notification extends LinearLayout implements View.OnClickListener {
    private NotificationInfo mNotificationInfo;
    private WindowManager mWinManager;
    private boolean mAlreadyOpen;
    private int mWidth, mHeight;
    private ScreenLock mScreenLock;

    public Notification(Context context, NotificationInfo notificationInfo) {
        super(context);
        mWinManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mNotificationInfo = notificationInfo;
        initView(context);
        mScreenLock = new ScreenLock(getContext());
    }

    private void initView(Context context) {
        View content = null;
        if (mNotificationInfo.type == NotificationInfo.NOTIFICATION_TYPE_WX) {
            WxNotificationInfo info = (WxNotificationInfo) mNotificationInfo;
            LayoutInflater.from(context).inflate(R.layout.win_notification, this);
            content = findViewById(R.id.rlNotification_Content);
            // 头像
            ImageView wxHead = (ImageView) findViewById(R.id.imgNotification_Wx_Head);
            // 昵称
            TextView wxNick = (TextView) findViewById(R.id.txtNotification_Wx_Nick);
            // 语音图标
            ImageView btnRepeat = (ImageView) findViewById(R.id.imgNotification_Wx_Voice);
            // 回复按钮
            View btnReply = findViewById(R.id.llNotification_Wx_Reply);

            Drawable headerImg = null;
            File header = new File(Environment.getExternalStorageDirectory() + "/txz/webchat/cache/Head/" + info.openId);
            if (header.exists()) {
                try {
                    headerImg = new BitmapDrawable(BitmapFactory.decodeStream(new FileInputStream(header)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                headerImg = getResources().getDrawable(R.drawable.wx_default_head);
            }
            wxHead.setImageDrawable(headerImg);

            if (info.hasSpeak) {
                AnimDrawableUtil.start(btnRepeat.getDrawable());
            } else {
                AnimDrawableUtil.reset(btnRepeat.getDrawable());
            }

            wxNick.setText(info.nick);

            content.setOnClickListener(this);
            btnRepeat.setOnClickListener(this);
            btnReply.setOnClickListener(this);
            
            content.setAlpha(0.8f);
        }
        mWidth = content.getLayoutParams().width;
        mHeight = content.getLayoutParams().height;
    }

    public void show() {
        if (mAlreadyOpen) {
            return;
        }
        WindowManager.LayoutParams layoutParam = new WindowManager.LayoutParams();
        layoutParam.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParam.width = mWidth;
        layoutParam.height = mHeight;
        layoutParam.flags = 40;
        layoutParam.format = PixelFormat.RGBA_8888;
        layoutParam.gravity = Gravity.LEFT | Gravity.TOP;
        mWinManager.addView(this, layoutParam);
        mAlreadyOpen = true;
        mScreenLock.lock();
    }

    public void dismiss() {
        if (mAlreadyOpen) {
            mWinManager.removeView(this);
            mAlreadyOpen = false;
            mScreenLock.release();
        }
    }

    public void notifyMessage(NotificationInfo notificationInfo) {
        Log.e(this.getClass().getSimpleName(), "notifyMessage " + notificationInfo.toString());
        if (mNotificationInfo.getClass() != notificationInfo.getClass()) {
            return;
        }
        if (notificationInfo instanceof WxNotificationInfo) {
            WxNotificationInfo info = (WxNotificationInfo) notificationInfo;
            // 语音图标
            ImageView btnRepeat = (ImageView) findViewById(R.id.imgNotification_Wx_Voice);
            if (info.hasSpeak) {
                AnimDrawableUtil.start(btnRepeat.getDrawable());
                if (!mAlreadyOpen) {
                    show();
                }
            } else {
                AnimDrawableUtil.reset(btnRepeat.getDrawable());
            }
            ImageView wxHead = (ImageView) findViewById(R.id.imgNotification_Wx_Head);
            Drawable headerImg = null;
            File header = new File(Environment.getExternalStorageDirectory() + "/txz/webchat/cache/Head/" + info.openId);
            if (header.exists()) {
                try {
                    headerImg = new BitmapDrawable(BitmapFactory.decodeStream(new FileInputStream(header)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                headerImg = getResources().getDrawable(R.drawable.wx_default_head);
            }
            wxHead.setImageDrawable(headerImg);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlNotification_Content:
                if (mNotificationInfo instanceof WxNotificationInfo) {
                    JSONBuilder doc = new JSONBuilder();
                    doc.put("id", ((WxNotificationInfo) mNotificationInfo).openId);
                    ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.session.open", doc.toBytes(), null);
                    dismiss();
                }
                break;
            case R.id.imgNotification_Wx_Voice:
                if (mNotificationInfo instanceof WxNotificationInfo) {
                    JSONBuilder doc = new JSONBuilder();
                    doc.put("id", ((WxNotificationInfo) mNotificationInfo).openId);
                    doc.put("msgId", ((WxNotificationInfo) mNotificationInfo).msgId);
                    ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.msg.repeat", doc.toBytes(), null);
                }
                break;
            case R.id.llNotification_Wx_Reply:
                if (mNotificationInfo instanceof WxNotificationInfo) {
                    JSONBuilder doc = new JSONBuilder();
                    doc.put("id", ((WxNotificationInfo) mNotificationInfo).openId);
                    ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.session.make", doc.toBytes(), null);
                    dismiss();
                }
                break;
        }
    }
}
