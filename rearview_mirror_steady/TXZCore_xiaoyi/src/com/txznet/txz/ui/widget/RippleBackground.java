package com.txznet.txz.ui.widget;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.txznet.txz.R;

public class RippleBackground extends RelativeLayout{
	private static final String DEFAULT_RIPPLE_COLOR = "#0099CC";
	private static final float DEFAULT_SCALE=6.0f;

	private static final int DEFAULT_STROKEWIDTH = 2;
	private static final int DEFAULT_RIPPLE_RADIUS = 64;
	
    private static final int DEFAULT_DURATION_TIME=800;
    private static final int OBJECTANIMATOR_INFINITE = -1;
    private static final int OBJECTANIMATOR_RESTART = 1;

    private int rippleColor;
    private float rippleStrokeWidth;
    private float rippleRadius;
    private int rippleDurationTime;
    private float rippleScale;
    private Paint paint;
    private boolean animationRunning=false;
    private AnimatorSet animatorSet;
    private ArrayList<Animator> animatorList;
    private LayoutParams rippleParams;
    
    private RippleView mRippleView;

    public RippleBackground(Context context) {
        super(context);
    }

    public RippleBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RippleBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (isInEditMode())
            return;

        if (null == attrs) {
            throw new IllegalArgumentException("Attributes should be provided to this view");
        }

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleBackground);
        rippleColor=typedArray.getColor(R.styleable.RippleBackground_rb_color, Color.parseColor(DEFAULT_RIPPLE_COLOR));
        rippleStrokeWidth=typedArray.getDimension(R.styleable.RippleBackground_rb_strokeWidth, DEFAULT_STROKEWIDTH);
        rippleRadius=typedArray.getDimension(R.styleable.RippleBackground_rb_radius,DEFAULT_RIPPLE_RADIUS);
        rippleDurationTime=typedArray.getInt(R.styleable.RippleBackground_rb_duration,DEFAULT_DURATION_TIME);
        rippleScale=typedArray.getFloat(R.styleable.RippleBackground_rb_scale,DEFAULT_SCALE);
        typedArray.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(rippleStrokeWidth);
        paint.setColor(rippleColor);

        rippleParams=new LayoutParams((int)(2*(rippleRadius+rippleStrokeWidth)),(int)(2*(rippleRadius+rippleStrokeWidth)));
        rippleParams.addRule(CENTER_IN_PARENT, TRUE);

        animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorList=new ArrayList<Animator>();

        mRippleView = new RippleView(getContext());
        addView(mRippleView,rippleParams);
        
        ObjectAnimator scaleX = createAnimator(mRippleView, "ScaleX", 1.0f, rippleScale);
        ObjectAnimator scaleY = createAnimator(mRippleView, "ScaleY", 1.0f, rippleScale);
        ObjectAnimator alpha = createAnimator(mRippleView, "Alpha", 1.0f,0.0f);
        animatorList.add(scaleX);
        animatorList.add(scaleY);
        animatorList.add(alpha);

        animatorSet.playTogether(animatorList);
    }
    
    private ObjectAnimator createAnimator(View target,String propertyName,float...values){
    	ObjectAnimator animator = ObjectAnimator.ofFloat(target, propertyName, values);
    	animator.setRepeatMode(OBJECTANIMATOR_RESTART);
    	animator.setRepeatCount(OBJECTANIMATOR_INFINITE);
    	animator.setDuration(rippleDurationTime);
    	return animator;
    }

    private class RippleView extends View{

        public RippleView(Context context) {
            super(context);
            this.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int radius=(Math.min(getWidth(),getHeight()))/2;
            canvas.drawCircle(radius,radius,radius-rippleStrokeWidth,paint);
        }
    }

    public void startRippleAnimation(){
        if(!isRippleAnimationRunning()){
            mRippleView.setVisibility(VISIBLE);
            animatorSet.start();
            animationRunning=true;
        }
    }

    public void stopRippleAnimation(){
        if(isRippleAnimationRunning()){
            animatorSet.end();
            animationRunning=false;
        }
    }

    public boolean isRippleAnimationRunning(){
        return animationRunning;
    }
}
