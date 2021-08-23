package com.txznet.launcher.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.launcher.R;
import com.txznet.launcher.bean.AppInfo;

public class AppIcon extends LinearLayout {
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_SMALL = 1;

    private int mStyle = STYLE_NORMAL;
    private ImageView mIcon;
    private TextView mText;
    private AppInfo mAppInfo;

    public AppIcon(Context context) {
        super(context);
        initView();
    }

    public AppIcon(Context context, AppInfo appInfo) {
        super(context);
        mAppInfo = appInfo;
        initView();
    }

    public AppIcon(Context context, AppInfo appInfo, int style) {
        super(context);
        mAppInfo = appInfo;
        mStyle = style;
        initView();
    }

    private void initView() {
        switch (mStyle) {
            case STYLE_SMALL:
                LayoutInflater.from(getContext()).inflate(R.layout.launcher_grid_item_small, this);
                break;
            default:
                LayoutInflater.from(getContext()).inflate(R.layout.launcher_grid_item, this);
                break;
        }
        mIcon = (ImageView) findViewById(R.id.img_Item_Icon);
        mText = (TextView) findViewById(R.id.txt_Item_Text);
        setMask(mIcon);
        if (mAppInfo != null) {
            if (mAppInfo.getIcon() != null) {
                setImageDrawable(mAppInfo.getIcon());
            }
            if (mAppInfo.getAppName() != null) {
                setText(mAppInfo.getAppName());
            }
        }
    }

    private void setMask(ImageView view) {
        view.setOnTouchListener(new OnTouchListener() {
            private float offsetX;
            private float offsetY;
            private long down;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                        offsetX = event.getX();
                        offsetY = event.getY();
                        down = SystemClock.currentThreadTimeMillis();
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        mIcon.clearColorFilter();
                        break;
                    case MotionEvent.ACTION_UP:
                        mIcon.clearColorFilter();
                        if (mOnClickListener != null && SystemClock.currentThreadTimeMillis() - down < ViewConfiguration.getLongPressTimeout()) {
                            if (Math.abs(event.getX() - offsetX) < ViewConfiguration.getTouchSlop() && Math.abs(event.getY() - offsetY) < ViewConfiguration.getTouchSlop()) {
                                playSoundEffect(SoundEffectConstants.CLICK);
                                mOnClickListener.onClick(AppIcon.this);
                            }
                        }
                        break;
                }

                return false;
            }
        });
    }

    public void setText(int res) {
        mText.setText(res);
    }

    public void setText(String text) {
        mText.setText(text);
    }

    public void setImageDrawable(Drawable drawable) {
        mIcon.setImageDrawable(drawable);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
        super.setOnClickListener(l);
    }

    private OnClickListener mOnClickListener;

    public AppInfo getAppInfo() {
        return mAppInfo;
    }
}
