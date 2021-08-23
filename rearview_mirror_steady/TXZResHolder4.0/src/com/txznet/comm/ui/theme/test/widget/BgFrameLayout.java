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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.txznet.comm.ui.theme.test.utils.DimenUtils;

/**
 * 说明：自定义背影色
 *
 * @author xiaolin
 * create at 2020-08-24 14:48
 */
public class BgFrameLayout extends FrameLayout {

    private Paint mPaint;
    private RectF rectF = new RectF();
    private float radius;

    private int lastWidth = 0;
    private int lastHeight = 0;

    public BgFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public BgFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BgFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (getLayerType() != View.LAYER_TYPE_SOFTWARE) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);   // 关闭硬件加速
        }

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        radius = DimenUtils.dp2px(getContext(), 10F);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if(width == 0 || height == 0){
            return;
        }

        rectF.set(0, 0, width, height);

        if(lastWidth != width || lastHeight != height){
            lastWidth = width;
            lastHeight = height;

            // 底色
            Shader shader0 = new LinearGradient(0, 0, rectF.right, rectF.bottom,
                    0xF0161D47, 0xF0161D47, Shader.TileMode.CLAMP);
            // 左上角
            Shader shader1 = new RadialGradient(0, 0, rectF.width(),
                    0xE6652F63, 0x00000000, Shader.TileMode.CLAMP);
            // 右上角
            Shader shader2 = new RadialGradient(rectF.right, 0, rectF.height(),
                    0x734295BA, 0x00000000, Shader.TileMode.CLAMP);
            // 左下角
            Shader shader3 = new RadialGradient(0, rectF.bottom, rectF.height(),
                    0x73426ABA, 0x00000000, Shader.TileMode.CLAMP);

            // 组合
            Shader composeShader = new ComposeShader(shader0, shader1, PorterDuff.Mode.LIGHTEN);
            composeShader = new ComposeShader(composeShader, shader2, PorterDuff.Mode.LIGHTEN);
            composeShader = new ComposeShader(composeShader, shader3, PorterDuff.Mode.LIGHTEN);
            mPaint.setShader(composeShader);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.drawRoundRect(rectF, radius, radius, mPaint);
        super.dispatchDraw(canvas);
    }
}
