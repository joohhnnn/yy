package com.txznet.music.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 按下透明度减半的CheckBox
 *
 * @author zackzhou
 * @date 2018/12/24,10:59
 */

public class AlphaCheckBox extends android.support.v7.widget.AppCompatCheckBox {

    public AlphaCheckBox(Context context) {
        super(context);
    }

    public AlphaCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean intercept = super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setAlpha(0.5f);
                intercept = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setAlpha(1f);
                break;
            default:
                break;
        }
        return intercept;
    }
}
