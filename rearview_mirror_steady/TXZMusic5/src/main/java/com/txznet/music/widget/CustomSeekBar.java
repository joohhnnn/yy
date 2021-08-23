package com.txznet.music.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.R;
import com.txznet.proxy.cache.LocalBuffer;

import java.util.ArrayList;
import java.util.List;

public class CustomSeekBar extends AppCompatSeekBar {
    public final static int PAINT_HEIGHT = 4;
    public static int PAINT_LEFT_PADDING = 0;
    public static int PAINT_TOP_PADDING = 0;
    public static int PAINT_BOTTOM_PADDING = 0;
    public static int PAINT_RIGHT_PADDING = PAINT_LEFT_PADDING;
    private BitmapDrawable baseBitmapDrawable;
    //    private Bitmap bmp;
    //    private Canvas pCanvas;
    private List<LocalBuffer> buffers;
    private int mBackgroundColor = 0x33FFFFFF;
    private int mProgressColor = 0xFFDC321E;
    private int mBufferColor = 0x7FFFFFFF;
    private int mSeekBarHeadId = R.drawable.player_progress_dot_icon;
    private boolean isEnableSeek = true;//是否可以拖动
    private boolean isDrag = false;//是否拖动中
    private Paint mPaintBg;
    private Paint mPaintBuffer;
    private Paint mPaintProgress;

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public CustomSeekBar(Context context) {
        this(context, null);
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        buffers = new ArrayList<>();
        baseBitmapDrawable = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(context.getResources(), mSeekBarHeadId));

        PAINT_TOP_PADDING = getPaddingTop();
        PAINT_LEFT_PADDING = getPaddingLeft();
        PAINT_RIGHT_PADDING = getPaddingRight();

        initbm();

    }

    Paint mDotPaint;

    private void initbm() {
        mDotPaint = new Paint();
        mDotPaint.setAntiAlias(true);
//        pCanvas = new Canvas(bmp);
//        pCanvas.drawBitmap(baseBitmap, new Matrix(), paint);

        mPaintBg = new Paint();
        mPaintBg.setColor(mBackgroundColor);
        mPaintBuffer = new Paint();
        mPaintBuffer.setColor(mBufferColor);
        mPaintProgress = new Paint();
        mPaintProgress.setColor(mProgressColor);
    }

    public synchronized void setBufferRange(List<LocalBuffer> buffers) {
        this.buffers.clear();
        if (buffers != null) {
            this.buffers.addAll(buffers);
        }
        postInvalidate();
    }

    @Override
    public synchronized void setProgress(int progress) {
        if (isDrag) {//拖动中
            return;
        }
        super.setProgress(progress);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        measure(widthMeasureSpec,);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height1 = getPaddingTop() + getPaddingBottom() + height;
        int width1 = getPaddingLeft() + getPaddingRight() + width;
        setMeasuredDimension(width1, height1);
//        Logger.d("test:::", "onMeasure>>>" + height + "," + width + "," + height1 + "," + width1);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        int PAINT_WIDTH = getWidth() - (PAINT_LEFT_PADDING + PAINT_RIGHT_PADDING);
        // 底色
        Rect r = new Rect(PAINT_LEFT_PADDING, getHeight() / 2 - PAINT_HEIGHT
                / 2, getWidth() - PAINT_RIGHT_PADDING, getHeight() / 2
                + PAINT_HEIGHT / 2);
        canvas.drawRect(r, mPaintBg);
        // ///////////////////////////////////////////////////////////////
        // 缓冲条
        if (CollectionUtils.isNotEmpty(buffers)) {
            for (LocalBuffer buffer : buffers) {
                if (buffer == null) {
                    break;
                }
                Rect r3 = new Rect(PAINT_LEFT_PADDING
                        + (int) (buffer.getFromP() * PAINT_WIDTH), getHeight()
                        / 2 - PAINT_HEIGHT / 2, PAINT_LEFT_PADDING
                        + (int) (buffer.getToP() * PAINT_WIDTH), getHeight()
                        / 2 + PAINT_HEIGHT / 2);
                canvas.drawRect(r3, mPaintBuffer);
            }
        }
        // 进度条
        Rect r2 = new Rect(PAINT_LEFT_PADDING, getHeight() / 2 - PAINT_HEIGHT
                / 2, PAINT_LEFT_PADDING
                + (getProgress() * PAINT_WIDTH / getMax()), getHeight() / 2
                + PAINT_HEIGHT / 2);
        canvas.drawRect(r2, mPaintProgress);

        // 光标
        left = getProgress() * PAINT_WIDTH / getMax() - getHeight() / 3;
        top = 0;
        right = left + getHeight();
        bottom = getHeight();
        bound.set(left, top, right, bottom);
        baseBitmapDrawable.setBounds(bound);
        baseBitmapDrawable.draw(canvas);
    }

    int left, top, right, bottom;
    Rect bound = new Rect();

    public void setSeekBarBackgroundColor(int color) {
        mBackgroundColor = color;
        postInvalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setSeekBarProgressColor(int color) {
        mProgressColor = color;
        postInvalidate();
    }

    public void setSeekBarBufferColor(int color) {
        mBufferColor = color;
        postInvalidate();
    }

    public void setSeekBarHeadId(int id) {
        mSeekBarHeadId = id;
        postInvalidate();
    }

    public boolean isEnableSeek() {
        return isEnableSeek;
    }

    public void setEnableSeek(boolean enableSeek) {
        isEnableSeek = enableSeek;
    }

    public boolean isDrag() {
        return isDrag;
    }

    public void setDrag(boolean drag) {
        isDrag = drag;
    }
}