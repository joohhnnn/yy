package com.txznet.txz.util.focus_supporter.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * root view for drawing focus rect on screen
 * Created by J on 2016/10/24.
 */

public class FocusView extends FrameLayout{

    private View mViewFocusRect;

    public FocusView(Context context) {
        this(context, null);
    }

    public FocusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mViewFocusRect = new View(getContext());
        addView(mViewFocusRect, 0, 0);
    }

    public void setFocusResource(int resId) {
        setFocusDrawable(getResources().getDrawable(resId));
    }

    @SuppressLint("NewApi")
	public void setFocusDrawable(Drawable drawable) {
        mViewFocusRect.setBackground(drawable);
    }



    private int[] mArrLocationOnScreen = new int[2];
    public void setFocusPosition(int left, int top, int width, int height) {

        if(width < 0 || height < 0) {
            //LogUtil.loge("FocusSupporter: focus width & height must be positive int!");
            width = 0;
            height = 0;
            //throw new IllegalArgumentException("focus width & height must be positive int!");
        }

        LayoutParams params = (LayoutParams) mViewFocusRect.getLayoutParams();

        if(null ==  params) {
            params = new LayoutParams(width, height);
        }else{
            params.width = width;
            params.height = height;
        }

        this.getLocationOnScreen(mArrLocationOnScreen);
        params.setMargins(left - mArrLocationOnScreen[0], top - mArrLocationOnScreen[1], 0, 0);
        mViewFocusRect.setLayoutParams(params);
    }

}
