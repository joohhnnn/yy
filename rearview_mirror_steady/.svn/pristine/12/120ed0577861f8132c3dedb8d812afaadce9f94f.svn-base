package com.txznet.music.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.R;

import java.util.ArrayList;
import java.util.List;

public class CustomSeekBar extends AppCompatSeekBar {
    public final static int PAINT_HEIGHT = 6;
    public static int PAINT_LEFT_PADDING = 0;
    public static int BITMAP_LEFT_PADDING = 0;
    public static int PAINT_RIGHT_PADDING = PAINT_LEFT_PADDING;
    private Bitmap baseBitmap;
    private Bitmap bmp;
    private Canvas pCanvas;
    private List<LocalBuffer> buffers;
    private int mBackgroundColor = 0xFF393939;
    private int mProgressColor = 0xFF1cc859;
    private int mBufferColor = 0xFF5d5a5a;
    private int mSeekBarHeadId = R.drawable.fm_player_drag;
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

    public void setPadding(int padding) {
        PAINT_LEFT_PADDING = PAINT_RIGHT_PADDING = padding;
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        buffers = new ArrayList<LocalBuffer>();
        baseBitmap = BitmapFactory.decodeResource(context.getResources(), mSeekBarHeadId);

        bmp = Bitmap.createBitmap(baseBitmap.getWidth(),
                baseBitmap.getHeight(), baseBitmap.getConfig());
//        BITMAP_LEFT_PADDING = PAINT_LEFT_PADDING = PAINT_RIGHT_PADDING = (bmp.getWidth() + 1) / 2;

        initbm();
    }

    private void initbm() {
        Paint paint = new Paint();
        pCanvas = new Canvas(bmp);
        pCanvas.drawBitmap(baseBitmap, new Matrix(), paint);

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

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        int PAINT_WIDTH = getWidth()
                - (PAINT_LEFT_PADDING + PAINT_RIGHT_PADDING);
        // 底色
        canvas.drawRect(new Rect(PAINT_LEFT_PADDING, 0, getWidth() - PAINT_RIGHT_PADDING, getHeight()), mPaintBg);
        // ///////////////////////////////////////////////////////////////
        // 缓冲条
        if (CollectionUtils.isNotEmpty(buffers)) {
            for (LocalBuffer buffer : buffers) {
                if (buffer == null) {
                    break;
                }
                canvas.drawRect(new Rect(PAINT_LEFT_PADDING
                        + (int) (buffer.getFromP() * PAINT_WIDTH), 0, PAINT_LEFT_PADDING
                        + (int) (buffer.getToP() * PAINT_WIDTH), getHeight()), mPaintBuffer);
            }
        }
        // 进度条
        canvas.drawRect(new Rect(PAINT_LEFT_PADDING, 0, PAINT_LEFT_PADDING
                + (getProgress() * PAINT_WIDTH / getMax()), getHeight()), mPaintProgress);
    }

    public void setSeekBarBackgroundColor(int color) {
        mBackgroundColor = color;
        mPaintBg.setColor(mBackgroundColor);
        postInvalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setSeekBarProgressColor(int color) {
        mProgressColor = color;
        mPaintProgress.setColor(mProgressColor);
        postInvalidate();
    }

    public void setSeekBarBufferColor(int color) {
        mBufferColor = color;
        mPaintBuffer.setColor(mBufferColor);
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