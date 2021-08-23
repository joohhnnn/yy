package com.txznet.comm.ui.theme.test.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * 解决ellipsize="start" 前面不显示[...]的BUG
 *
 * @author xiaolin
 * 2020-08-25 15:49
 */
public class EllipsizePrinterTextView extends PrinterTextView {

    private Paint mPaint;
    private Rect mTextBoundsRect = new Rect();

    public EllipsizePrinterTextView(Context context) {
        super(context);
        init();
    }

    public EllipsizePrinterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getCurrentTextColor());
        mPaint.setTextSize(getTextSize());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();

        String text = getText().toString();
        int len = text.length();

        mPaint.getTextBounds(text, 0, len, mTextBoundsRect);
        if (mTextBoundsRect.width() <= width) {
            super.onDraw(canvas);
        } else {
            /*
             * 文字超出控制宽度
             *
             * 测量过程
             *
             * 一二三四五六七分八九十
             *
             * ...二三四五六七分八九十
             * ...三四五六七分八九十
             * ...四五六七分八九十
             * ...五六七分八九十
             * ...六七分八九十
             * ...七分八九十
             *
             */

            for (int i = 1; i < len; i++) {
                String newText = "..." + text.substring(i);
                float w = mPaint.measureText(newText);
                if (w < width) {
                    drawTextCenter(canvas, newText, 0);// 左对齐
                    break;
                }
            }
        }
    }

    /**
     * 垂直居中绘制文字
     *
     * @param x 文字左边的x坐标
     */
    private void drawTextCenter(Canvas canvas, String text, float x) {
        //计算baseline
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float baseline = getHeight() / 2F + distance;

        canvas.drawText(text, x, baseline, mPaint);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        mPaint.setColor(color);
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        mPaint.setTextSize(size);
    }
}
