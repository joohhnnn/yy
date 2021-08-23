package com.txznet.launcher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;

/**
 * 可以在图片的右下角添加小的圆角图片的类
 */
public class CornerMaskImageView extends ImageView {
    private Drawable mCornerMaskDrawable;
    private int mMaskSize;

    public CornerMaskImageView(Context context) {
        super(context);
    }

    public CornerMaskImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CornerMaskImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CornerMaskImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private int mMaskGravity = Gravity.BOTTOM | Gravity.RIGHT;

    public void showCornerMaskAtRightBottom(@DrawableRes int resId, int size) {
        mCornerMaskDrawable = getResources().getDrawable(resId);
        mMaskSize = size;
        invalidate();
    }

    public void clearCornerMask() {
        if (mCornerMaskDrawable == null) {
            return;
        }
        mCornerMaskDrawable = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCornerMaskDrawable != null) {
            mCornerMaskDrawable.setBounds(canvas.getWidth() - mMaskSize, canvas.getHeight() - mMaskSize, canvas.getWidth(), canvas.getHeight());
            mCornerMaskDrawable.draw(canvas);
        }
    }
}