package com.txznet.webchat.helper;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.view.Window;
import android.view.WindowManager;

import com.txznet.webchat.log.L;
import com.txznet.webchat.model.WxUIConfig;

/**
 * 微信窗口位移动画Helper
 * Created by J on 2017/6/22.
 */

public class WindowMoveAnimateHelper {
    private static final String LOG_TAG = "WindowMoveAnimateHelper";
    private Window mWinTarget;
    private PointF mStartPoint;
    private PointF mEndPoint;
    private int mDuration;
    private BesselEvaluator mEvaluator;
    private ValueAnimator mAnimator;

    // 用于
    private static BesselEvaluator mDownBessel = new BesselEvaluator(new PointF(-400, 300), new PointF(-400, 800));
    private static BesselEvaluator mUpBessel = new BesselEvaluator(new PointF(400, -300), new PointF(400, -800));

    /**
     * invoke entrance
     *
     * @param win target window
     * @return WindowMoveHelper
     */
    public static Builder of(Window win) {
        return new Builder(win);
    }

    /**
     * ::constructor
     *
     * @param win 目标窗口
     */
    private WindowMoveAnimateHelper(Window win) {
        this.mWinTarget = win;
    }

    public void start() {
        mAnimator.setDuration(mDuration);
        mAnimator.addUpdateListener(new BesselListener(mWinTarget));
        mAnimator.start();
    }


    /////////    Builder
    public static class Builder {
        private Window mWinTarget;
        private PointF mStartPoint;
        private PointF mEndPoint;
        private int mDuration;

        private ValueAnimator mAnimation;

        private Builder(Window win) {
            this.mWinTarget = win;
        }

        public Builder setKeyPoint(PointF start, PointF end) {
            this.mStartPoint = start;
            this.mEndPoint = end;

            return this;
        }

        public Builder setKeyPoint(WxUIConfig start, WxUIConfig end) {
            PointF _start = new PointF(start.x, start.y);
            PointF _end = new PointF(end.x, end.y);

            return setKeyPoint(_start, _end);
        }

        public Builder setDuration(int duration) {
            this.mDuration = duration;

            return this;
        }

        public WindowMoveAnimateHelper build() {
            // data evaluate
            if (null == mWinTarget) {
                throw new IllegalArgumentException("target Window is null");
            } else if (null == mStartPoint) {
                throw new IllegalArgumentException("start point is null");
            } else if (null == mEndPoint) {
                throw new IllegalArgumentException("end point is null");
            } else if (mDuration == 0) {
                L.e(LOG_TAG, "duration cannot be 0, set to 1000 by default");
                mDuration = 1000;
            }

            WindowMoveAnimateHelper helper = new WindowMoveAnimateHelper(mWinTarget);
            helper.mStartPoint = mStartPoint;
            helper.mEndPoint = mEndPoint;
            helper.mDuration = mDuration;

            helper.mEvaluator = (mEndPoint.y > mStartPoint.y) ? mDownBessel : mUpBessel;
            helper.mAnimator = ValueAnimator.ofObject(helper.mEvaluator, mStartPoint, mEndPoint);

            return helper;
        }
    }


    /////////    value evaluators
    private static class BesselEvaluator implements TypeEvaluator<PointF> {

        private PointF point1;
        private PointF point2;
        private PointF pointF;

        public BesselEvaluator(PointF point1, PointF point2) {
            this.point1 = point1;
            this.point2 = point2;
        }

        @Override
        public PointF evaluate(float time, PointF start, PointF end) {
            float timeLeft = 1.0f - time;
            pointF = new PointF();//结果

            PointF point0 = start;//起点

            PointF point3 = end;//终点
            pointF.x = timeLeft * timeLeft * timeLeft * (point0.x)
                    + 3 * timeLeft * timeLeft * time * (point1.x)
                    + 3 * timeLeft * time * time * (point2.x)
                    + time * time * time * (point3.x);

            pointF.y = timeLeft * timeLeft * timeLeft * (point0.y)
                    + 3 * timeLeft * timeLeft * time * (point1.y)
                    + 3 * timeLeft * time * time * (point2.y)
                    + time * time * time * (point3.y);
            return pointF;
        }
    }

    private class BesselListener implements ValueAnimator.AnimatorUpdateListener {

        private Window target;
        private int rawX;
        private int rawY;
        private WindowManager.LayoutParams mLpRaw;

        public BesselListener(Window target) {
            this.target = target;

            rawX = target.getAttributes().x;
            rawY = target.getAttributes().y;
            mLpRaw = target.getAttributes();
        }


        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (target != null) {
                PointF pointF = (PointF) animation.getAnimatedValue();
                mLpRaw.x = rawX + (int) pointF.x;
                mLpRaw.y = rawY + (int) pointF.y;
                target.setAttributes(mLpRaw);
            }
        }
    }
}
