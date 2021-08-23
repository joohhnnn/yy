package com.txznet.webchat.ui.rearview_mirror.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.txznet.webchat.R;

/**
 * 自带聊天气泡背景的RelativeLayout
 *
 * Created by J on 2018/1/15.
 */

public class BubbleRelativeLayout extends RelativeLayout {
    public static final int ARROW_HEIGHT_DEFAULT = 14;
    public static final int ARROW_POSITION_DEFAULT = ARROW_HEIGHT_DEFAULT / 2;

    private View mViewTop;
    private View mViewArrow;
    private View mViewBottom;

    // backgroud drawables
    private int mResTop;
    private int mResArrow;
    private int mResBottom;

    private int mResTopPressed;
    private int mResArrowPressed;
    private int mResBottomPressed;

    /*
    * 额外添加的padding
    *
    * 由于聊天气泡的切图可能会包含四周的留空区域, 如果不考虑四周的留空区域可能会出现气泡内的其他view显示超出了
    * 气泡边界的现象, 所以提供对四周留空区域大小的设置.
    * */
    private int mExtraPaddingTop;
    private int mExtraPaddingBottom;
    private int mExtraPaddingLeft;
    private int mExtraPaddingRight;

    // arrow position
    private int mArrowPosition;
    // arrow height
    private int mArrowHeight;

    public BubbleRelativeLayout(Context context) {
        super(context);
        init();
    }

    public BubbleRelativeLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        init();
    }

    public BubbleRelativeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        init();
    }

    public void setBubbleResource(int topRes, int arrowRes, int bottomRes) {
        mResTop = topRes;
        mResArrow = arrowRes;
        mResBottom = bottomRes;

        if (!bPressed) {
            invalidateBubbleResource(mResTop, mResArrow, mResBottom);
        }
    }

    public void setBubblePressedResource(int topRes, int arrowRes, int bottomRes) {
        mResTopPressed = topRes;
        mResArrowPressed = arrowRes;
        mResBottomPressed = bottomRes;

        if (bPressed) {
            invalidateBubbleResource(mResTopPressed, mResArrowPressed, mResBottomPressed);
        }
    }

    public void setEnablePushEffect(boolean enable) {
        bEnablePushEffect = enable;
    }

    private boolean invalidateBubbleResource(int resRop, int resArrow, int resBottom) {
        boolean changed = false;
        if (invalidateViewRes(mViewTop, resRop)) {
            changed = true;
        }

        if (invalidateViewRes(mViewArrow, resArrow)) {
            changed = true;
        }

        if (invalidateViewRes(mViewBottom, resBottom)) {
            changed = true;
        }

        return changed;
    }

    public void setArrowHeight(int height) {
        if (mArrowHeight != height) {
            mArrowHeight = height;
            layoutBubbleViews(mHorizontalSpaceTotal, mVerticalSpaceTotal);
        }
    }

    public void setArrowPosition(int position) {
        if (mArrowPosition != position) {
            mArrowPosition = position;
            layoutBubbleViews(mHorizontalSpaceTotal, mVerticalSpaceTotal);
        }
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable
                .BubbleRelativeLayout);
        mResTop = ta.getResourceId(R.styleable.BubbleRelativeLayout_cbv_top_src, 0);
        mResArrow = ta.getResourceId(R.styleable.BubbleRelativeLayout_cbv_arrow_src, 0);
        mResBottom = ta.getResourceId(R.styleable.BubbleRelativeLayout_cbv_bottom_src, 0);
        mResTopPressed = ta.getResourceId(R.styleable.BubbleRelativeLayout_cbv_top_src_pressed, 0);
        mResArrowPressed = ta.getResourceId(R.styleable
                .BubbleRelativeLayout_cbv_arrow_src_pressed, 0);
        mResBottomPressed = ta.getResourceId(R.styleable
                .BubbleRelativeLayout_cbv_bottom_src_pressed, 0);
        mArrowHeight = ta.getDimensionPixelSize(R.styleable
                .BubbleRelativeLayout_cbv_arrow_height, ARROW_HEIGHT_DEFAULT);
        mArrowPosition = ta.getDimensionPixelSize(R.styleable
                .BubbleRelativeLayout_cbv_arrow_position, ARROW_POSITION_DEFAULT);
        mExtraPaddingTop = ta.getDimensionPixelSize(R.styleable
                .BubbleRelativeLayout_cbv_extra_padding_top, 0);
        mExtraPaddingBottom = ta.getDimensionPixelSize(R.styleable
                .BubbleRelativeLayout_cbv_extra_padding_bottom, 0);
        mExtraPaddingLeft = ta.getDimensionPixelSize(R.styleable
                .BubbleRelativeLayout_cbv_extra_padding_left, 0);
        mExtraPaddingRight = ta.getDimensionPixelSize(R.styleable
                .BubbleRelativeLayout_cbv_extra_padding_right, 0);
        ta.recycle();
    }

    private void init() {
        // set clip padding to false in case of BubbleViews be clipped
        setClipToPadding(false);

        // set extra padding
        setPadding(
                getPaddingLeft() + mExtraPaddingLeft,
                getPaddingTop() + mExtraPaddingTop,
                getPaddingRight() + mExtraPaddingRight,
                getPaddingBottom() + mExtraPaddingBottom
        );

        // init Views
        mViewTop = new BubbleContainerView(getContext());
        mViewArrow = new BubbleContainerView(getContext());
        mViewBottom = new BubbleContainerView(getContext());

        // init res
        invalidateViewRes(mViewTop, mResTop);
        invalidateViewRes(mViewArrow, mResArrow);
        invalidateViewRes(mViewBottom, mResBottom);

        addView(mViewTop);
        addView(mViewArrow);
        addView(mViewBottom);
    }

    private boolean invalidateViewRes(View target, int resId) {
        target.setBackgroundResource(resId);

        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mHorizontalSpaceTotal = r - l;
        mVerticalSpaceTotal = b - t;
        layoutBubbleViews(mHorizontalSpaceTotal, mVerticalSpaceTotal);
    }

    private void layoutBubbleViews(int widthTotal, int heightTotal) {
        int line1 = mArrowPosition - mArrowHeight / 2 + mExtraPaddingTop;
        int line2 = mArrowPosition + mArrowHeight / 2;

        mViewTop.layout(0, 0, widthTotal, line1);
        mViewArrow.layout(0, line1, widthTotal, line2);
        mViewBottom.layout(0, line2, widthTotal, heightTotal);
    }

    private boolean bPressed;
    private boolean bEnablePushEffect = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                log("down");
                bPressed = true;
                updateBubbleResByState();
                return true;

            case MotionEvent.ACTION_UP:
                log("up");
                bPressed = false;
                updateBubbleResByState();
                judgeClick(event.getX(), event.getY());
                return true;

            case MotionEvent.ACTION_MOVE:
                //log("move");
                return bPressed;

            case MotionEvent.ACTION_CANCEL:
                bPressed = false;
                updateBubbleResByState();
                return true;

            default:
                return false;
        }
    }

    private void updateBubbleResByState() {
        if (!bEnablePushEffect) {
            return;
        }

        if (bPressed) {
            invalidateBubbleResource(mResTopPressed, mResArrowPressed, mResBottomPressed);
        } else {
            invalidateBubbleResource(mResTop, mResArrow, mResBottom);
        }
    }

    private int mHorizontalSpaceTotal;
    private int mVerticalSpaceTotal;

    private void judgeClick(float upX, float upY) {
        if (upX >= 0 && upX <= mHorizontalSpaceTotal
                && upY >= 0 && upY <= mVerticalSpaceTotal) {
            this.callOnClick();
        }
    }

    private static final boolean DEBUG = false;

    private void log(String msg) {
        if (DEBUG) {
            Log.i("ChatBubbleView", msg);
        }
    }

    private static class BubbleContainerView extends View {

        private LayoutParams mFakeParam = new LayoutParams(0, 0);

        public BubbleContainerView(Context context) {
            super(context);
        }

        @Override
        public LayoutParams getLayoutParams() {
            // 返回一个0,0的LayoutParam, 避免Parent在measure时将自己的LayoutParam考虑在内
            return mFakeParam;
        }
    }
}
