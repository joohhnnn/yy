package com.txznet.record.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.txznet.record.lib.R;

/**
 * 圆角头像
 */
public class CircleImageView extends ImageView {

    private static final int DEFAULT_BORDER_WIDTH = 2;
    private static final int COLORDRAWABLE_DIMENSION = 1;
    private static final int DEFAULT_BORDER_COLOR = Color.TRANSPARENT;
    private static final int DEFAULT_BG_COLOR = Color.TRANSPARENT;
    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;
    private static final Config DEFAULT_DRAWABLE_CONFIG = Config.ARGB_8888;

    private int mBgColor;
    private int mBorderWidth;
    private int mBorderColor;

    private float mBitmapRadius;
    private float mBorderRadius;

    private int mBitmapWidth;
    private int mBitmapHeight;

    private Paint mBgPaint = new Paint();
    private Paint mBitmapPaint = new Paint();
    private Paint mBorderPaint = new Paint();
    private Matrix mShaderMatrix = new Matrix();

    private RectF mBitmapRectF = new RectF();
    private RectF mBorderRectF = new RectF();

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;

    private boolean isResourceReady;
    private boolean isSetup;

    private Bitmap mLoadingBm;
    private RotateAnimation mLoadingAnim;

    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public CircleImageView(Context context, AttributeSet attr, int defValue) {
        super(context, attr, defValue);
        super.setScaleType(SCALE_TYPE);

        TypedArray ta = context.obtainStyledAttributes(attr, R.styleable.CircleImageView, defValue, 0);

        mBorderWidth = ta.getDimensionPixelOffset(R.styleable.CircleImageView_border_width, DEFAULT_BORDER_WIDTH);
        mBorderColor = ta.getColor(R.styleable.CircleImageView_border_color, DEFAULT_BORDER_COLOR);
        mBgColor = ta.getColor(R.styleable.CircleImageView_bg_color, DEFAULT_BG_COLOR);

        ta.recycle();
        isResourceReady = true;
        if (isSetup) {
            init();
            isSetup = false;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mBorderRadius, mBgPaint);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mBitmapRadius, mBitmapPaint);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mBorderRadius, mBorderPaint);
    }

    int degrees;

    /**
     * 设置边框的宽度
     *
     * @param borderWidth
     */
    public void setBorderWidth(int borderWidth) {
        if (mBorderWidth == borderWidth) {
            return;
        }

        mBorderWidth = borderWidth;
        init();
    }

    /**
     * 设置边框的颜色值
     *
     * @param color
     */
    public void setBorderColor(int color) {
        if (mBorderColor == color) {
            return;
        }

        mBorderColor = color;
        mBorderPaint.setColor(color);
        invalidate();
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = drawable2bitmap(drawable);
        init();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        init();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mBitmap = drawable2bitmap(getDrawable());
        init();
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException("scaleType is not support!");
        }

        super.setScaleType(scaleType);
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        throw new IllegalStateException("background should not be seted");
    }

    @Override
    public void setBackground(Drawable background) {
        throw new IllegalStateException("background should not be seted");
    }

    @Override
    public void setBackgroundColor(int color) {
        throw new IllegalStateException("background should not be seted");
    }

    @Override
    public void setBackgroundResource(int resid) {
        throw new IllegalStateException("background should not be seted");
    }

    private Bitmap drawable2bitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;
            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, DEFAULT_DRAWABLE_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), DEFAULT_DRAWABLE_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private void init() {
        if (!isResourceReady) {
            isSetup = true;
            return;
        }

        if (mBitmap == null) {
            return;
        }

        if (mLoadingBm == null) {
//			mLoadingBm = drawable2bitmap(getResources().getDrawable(R.drawable.loading));
        }

        mBitmapShader = new BitmapShader(mBitmap, TileMode.CLAMP, TileMode.CLAMP);
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mBgPaint.setAntiAlias(true);
        mBgPaint.setStyle(Style.FILL);
        mBgPaint.setColor(mBgColor);

        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Style.STROKE);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();

        mBorderRectF.set(0, 0, getWidth(), getHeight());
        mBorderRadius = Math.min((mBorderRectF.width() - mBorderWidth) / 2, (mBorderRectF.height() - mBorderWidth) / 2);

        mBitmapRectF.set(mBorderWidth, mBorderWidth, mBorderRectF.width() - mBorderWidth, mBorderRectF.height() - mBorderWidth);
        mBitmapRadius = Math.min(mBitmapRectF.width() / 2, mBitmapRectF.height() / 2);

        updateShaderMatrix();
        invalidate();
    }

    private void updateShaderMatrix() {
        float scale = 0;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);
        if (mBitmapWidth * mBitmapRectF.width() > mBitmapHeight * mBitmapRectF.height()) {
            scale = mBitmapRectF.height() / (float) mBitmapHeight;
            dx = (mBitmapRectF.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mBitmapRectF.width() / (float) mBitmapWidth;
            dy = (mBitmapRectF.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((dx + 0.5f) + mBorderWidth, (dy + 0.5f) + mBorderWidth);
        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

    public void invalidate() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            super.invalidate();
        } else {
            postInvalidate();
        }
    }
}
