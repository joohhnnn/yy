package com.txznet.comm.ui.theme.test.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.txznet.comm.ui.theme.test.utils.DimenUtils;

/**
 * 说明：自定义背影色
 *
 * @author xiaolin
 * create at 2020-08-24 14:50
 */
public class BgLinearLayout extends LinearLayout {

    private Paint mPaint;
    private RectF mRectF = new RectF();
    private float mRadius;                  // 圆角

    private int mLastWidth = 0;
    private int mLastHeight = 0;

    public BgLinearLayout(Context context) {
        super(context);
        init();
    }

    public BgLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BgLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if(width == 0 || height == 0){
            return;
        }

        mRectF.set(0, 0, width, height);

        if(mLastWidth != width || mLastHeight != height){
            mLastWidth = width;
            mLastHeight = height;

            // 底色
            Shader shader0 = new LinearGradient(0, 0, mRectF.right, mRectF.bottom,
                    0xF0161D47, 0xF0161D47, Shader.TileMode.CLAMP);
            // 左上角
            Shader shader1 = new RadialGradient(0, 0, mRectF.width(),
                    0xE6652F63, 0x00000000, Shader.TileMode.CLAMP);
            // 右上角
            Shader shader2 = new RadialGradient(mRectF.right, 0, mRectF.height(),
                    0x734295BA, 0x00000000, Shader.TileMode.CLAMP);
            // 左下角
            Shader shader3 = new RadialGradient(0, mRectF.bottom, mRectF.height(),
                    0x73426ABA, 0x00000000, Shader.TileMode.CLAMP);

            // 组合
            Shader composeShader = new ComposeShader(shader0, shader1, PorterDuff.Mode.LIGHTEN);
            composeShader = new ComposeShader(composeShader, shader2, PorterDuff.Mode.LIGHTEN);
            composeShader = new ComposeShader(composeShader, shader3, PorterDuff.Mode.LIGHTEN);
            mPaint.setShader(composeShader);
        }
    }

    private void init() {
        if (getLayerType() != View.LAYER_TYPE_SOFTWARE) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);   // 关闭硬件加速
        }

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mRadius = DimenUtils.dp2px(getContext(), 10F);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.drawRoundRect(mRectF, mRadius, mRadius, mPaint);
        super.dispatchDraw(canvas);
    }
}
