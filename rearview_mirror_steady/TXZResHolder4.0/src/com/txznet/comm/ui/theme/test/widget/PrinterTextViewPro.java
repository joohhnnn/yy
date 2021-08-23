package com.txznet.comm.ui.theme.test.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

/**
 * 说明：打字效果，只有单行
 *
 * 更多的动画效果，每个字出现时有透明度增加，从右逐渐往左平移
 *
 * @author xiaolin
 * create at 2020-10-28 16:17
 */
public class PrinterTextViewPro extends View {

    private final int DEFAULT_PRINT_TIME_DELAY = 100;
    private final int DEFAULT_PRINT_WIDTH_DELAY = 300;


    private Paint mPaint;
    private Rect mTextBoundsRect = new Rect();
    private String mText = "";

    private float mTextSize = 60F;
    private float mTextPadding = mTextSize / 10F;// 文字间距
    private int mTextColor = Color.WHITE;

    private Scroller mViewWidthScroller;
    private Scroller[] mScrollerAry;
    private boolean[] mScrollerStateAry;// true: 已经开始

    public PrinterTextViewPro(Context context) {
        super(context);
        init();
    }

    public PrinterTextViewPro(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PrinterTextViewPro(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTextSize);

        mViewWidthScroller = new Scroller(getContext());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;

        {
            int mode = MeasureSpec.getMode(widthMeasureSpec);
            int size = MeasureSpec.getSize(widthMeasureSpec);
            if (mode == MeasureSpec.EXACTLY) {// 确切的大小
                width = MeasureSpec.getSize(widthMeasureSpec);
            } else if (mode == MeasureSpec.AT_MOST) {// 大小不超过某数值，如：wrap_content
                int w = 0;
                if (mViewWidthScroller.computeScrollOffset()) {
                    w = mViewWidthScroller.getCurrX();
                } else {
                    w = (int) getTextWidth(mText);
                }
                width = Math.min(w, size);
            } else if (mode == MeasureSpec.UNSPECIFIED) {// 不对View大小做限制，如：ListView，ScrollView
                int w = 0;
                if (mViewWidthScroller.computeScrollOffset()) {
                    w = mViewWidthScroller.getCurrX();
                } else {
                    w = (int) getTextWidth(mText);
                }
                width = w;
            }
        }

        {
            int mode = MeasureSpec.getMode(heightMeasureSpec);
            int size = MeasureSpec.getSize(heightMeasureSpec);
            if (mode == MeasureSpec.EXACTLY) {// 确切的大小
                height = MeasureSpec.getSize(heightMeasureSpec);
            } else if (mode == MeasureSpec.AT_MOST) {// 大小不超过某数值，如：wrap_content
                height = (int) Math.min(mTextSize, size);
            } else if (mode == MeasureSpec.UNSPECIFIED) {// 不对View大小做限制，如：ListView，ScrollView
                height = (int) mTextSize;
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (!mViewWidthScroller.isFinished()) {
            requestLayout();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawColor(Color.RED);

        //计算baseline
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float baseline = getHeight() / 2F + distance;

        // 最大偏移量
        float maxOffsetX = mTextSize;

        float left = 0;
        int len = mText.length();
        for (int i = 0; i < len; i++) {
            mPaint.getTextBounds(mText, i, i + 1, mTextBoundsRect);

            int alpha = 255;
            float offsetX = 0;
            if (!mScrollerStateAry[i]) {
                alpha = 0;
            } else if (mScrollerAry[i].computeScrollOffset()) {
                alpha = mScrollerAry[i].getCurrX();
                offsetX = mScrollerAry[i].getCurrY() / 1000F * maxOffsetX;
                postInvalidate();
            }
            mPaint.setAlpha(alpha);
            canvas.drawText(mText, i, i + 1, left + offsetX, baseline, mPaint);

            left += mTextBoundsRect.width();
            left += mTextPadding;
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

    private float getTextWidth(String text) {
        float w = 0;
        int len = text.length();
        for (int i = 0; i < len; i++) {
            mPaint.getTextBounds(text, i, i + 1, mTextBoundsRect);
            w += mTextBoundsRect.width();
            if(i + 1 != len){
                w += mTextPadding;
            }
        }
        return w;
    }

    public void setPrintText(String text) {
        /*文本减少*/
        if (text.length() < mText.length()) {
            for (int i = text.length(); i < mText.length(); i++) {
                mHandler.removeMessages(i);
            }

            int curWidth = getMeasuredWidth();
            if(mViewWidthScroller.computeScrollOffset()){
                curWidth = mViewWidthScroller.getCurrX();
            }
            int toWidth = (int) getTextWidth(text);
            int d = toWidth - curWidth;
            mViewWidthScroller.startScroll(curWidth, 0, d, 0, DEFAULT_PRINT_WIDTH_DELAY);

            this.mText = text;
            requestLayout();
            return;
        }

        /*文本相等，直接替换*/
        if (text.length() == mText.length()) {
            this.mText = text;
            requestLayout();
            return;
        }

        /*文本增加*/
//        for(int i=0; i<mText.length(); i++){
//            mHandler.removeMessages(i);
//        }


        boolean[] scrollerStateAry = new boolean[text.length()];
        Scroller[] scrollerAry = new Scroller[text.length()];

        int oldLen = mText.length();
        int newLen = text.length();

        // 复制旧数据
        for (int i = 0; i < oldLen; i++) {
            scrollerStateAry[i] = mScrollerStateAry[i];
            scrollerAry[i] = mScrollerAry[i];
        }

        mScrollerStateAry = scrollerStateAry;
        mScrollerAry = scrollerAry;

        int stepTime = DEFAULT_PRINT_TIME_DELAY / (newLen - oldLen);
        for (int i = oldLen; i < newLen; i++) {
            scrollerStateAry[i] = false;
            scrollerAry[i] = new Scroller(getContext());

            Message msg = mHandler.obtainMessage();
            msg.what = i;
            msg.arg1 = i;
            mHandler.sendMessageDelayed(msg, stepTime * (i - oldLen));
        }

        int curWidth = getMeasuredWidth();
        if(mViewWidthScroller.computeScrollOffset()){
            curWidth = mViewWidthScroller.getCurrX();
        }
        int toWidth = (int) getTextWidth(text);
        int d = toWidth - curWidth;
        mViewWidthScroller.startScroll(curWidth, 0, d, 0, DEFAULT_PRINT_WIDTH_DELAY);

        this.mText = text;

        requestLayout();
    }

    public String getText() {
        return mText;
    }

    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
    }

    public float geTextSize() {
        return mTextSize;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mScrollerStateAry[msg.arg1] = true;
            mScrollerAry[msg.arg1].startScroll(0, 1000, 255, -1000, 500);
        }
    };
}
