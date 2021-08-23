package com.txznet.comm.ui.theme.test.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.txznet.comm.ui.theme.test.utils.DimenUtils;
import com.txznet.resholder.R;

/**
 * 带翻页进度的滑动布局
 * <p>
 * 2020-08-13 15:45
 *
 * @author xiaolin
 */
public class ProgressBarScrollView extends ScrollView {

    private Paint mPaint;
    private float marginTop, marginRight, marginBottom;
    private float barWidth;
    private float radius;
    private int barColor;

    private int maxPage = 1;
    private int curPage = 0;

    public ProgressBarScrollView(Context context) {
        this(context, null);
    }

    public ProgressBarScrollView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressBarScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressBarScrollView);
        marginTop = ta.getDimension(R.styleable.ProgressBarScrollView_bar_margin_top, DimenUtils.dp2px(context, 4F));
        marginRight = ta.getDimension(R.styleable.ProgressBarScrollView_bar_margin_right, DimenUtils.dp2px(context, 8F));
        marginBottom = ta.getDimension(R.styleable.ProgressBarScrollView_bar_margin_bottom, DimenUtils.dp2px(context, 4F));
        barWidth = ta.getDimension(R.styleable.ProgressBarScrollView_bar_width, DimenUtils.dp2px(context, 4F));
        radius = ta.getDimension(R.styleable.ProgressBarScrollView_bar_radius, DimenUtils.dp2px(context, 2F));
        barColor = ta.getColor(R.styleable.ProgressBarScrollView_bar_color, 0x30FFFFFF);
        ta.recycle();

        mPaint = new Paint();
        mPaint.setColor(barColor);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int y = getScrollY();
        canvas.save();
        canvas.translate(0, y);

        float drawHeight = getHeight() - marginTop - marginBottom;
        float width = getWidth();
        float stepH = drawHeight / maxPage;

        RectF rectF = new RectF();
        rectF.left = width - marginRight - barWidth;
        rectF.right = rectF.left + barWidth;
        rectF.top = stepH * curPage + marginTop;
        rectF.bottom = rectF.top + stepH;

        canvas.drawRoundRect(rectF, radius, radius, mPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public void setBarColor(int barColor) {
        this.barColor = barColor;
    }
}
