package com.txznet.music.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.ViewConfiguration;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by telenewbie on 2017/9/26.
 */

public class CircleShadeImageView extends CircleImageView {

    public CircleShadeImageView(Context context) {
        super(context);
    }

    public CircleShadeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleShadeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    private float offsetX;
    private float offsetY;
    private String name;// 标志该组件的名称，用于首页的显示
    private long down;


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                offsetX = event.getX();
                offsetY = event.getY();
                down = SystemClock.currentThreadTimeMillis();
                return true;
            case MotionEvent.ACTION_CANCEL:
                this.clearColorFilter();
                break;
            case MotionEvent.ACTION_UP:
                this.clearColorFilter();
                if (mOnClickListener != null && SystemClock.currentThreadTimeMillis() - down < ViewConfiguration.getLongPressTimeout()) {
                    if (Math.abs(event.getX() - offsetX) < ViewConfiguration.getTouchSlop() && Math.abs(event.getY() - offsetY) < ViewConfiguration.getTouchSlop()) {
                        playSoundEffect(SoundEffectConstants.CLICK);
                        mOnClickListener.onClick(this);
                    }
                    return true;
                }
                break;
        }

        return false;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
        super.setOnClickListener(l);
    }

    private OnClickListener mOnClickListener;

    /**
     * 设置该组件的名称
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 获得该组件的名称
     *
     * @return
     */
    public void setName(String name) {
        this.name = name;
    }
}
