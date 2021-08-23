package com.txznet.nav.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.txznet.loader.AppLogic;
import com.txznet.nav.R;
import com.txznet.txz.util.runnables.Runnable1;

public class DirectionCiv extends ViewGroup{

	private int mCircleImageRadius;
	private Point mCenterPoint;
	
	private CircleImageView mCiv;
	private ImageView mCarImageView;
	
	private String mUserId;
	
	public DirectionCiv(Context context) {
		super(context);
		init();
	}

	public DirectionCiv(Context context,AttributeSet attr) {
		super(context,attr);
		init();
	}
	
	public DirectionCiv(Context context,AttributeSet attr,int defValue) {
		super(context,attr,defValue);
		init();
	}
	
	public void initalize(String userId){
		this.mUserId = userId;
	}
	
	public String getUserId(){
		if(mUserId == null || "".equals(mUserId)){
			throw new NullPointerException("userId should not null,you should run initalize to set userId first!");
		}
		
		return mUserId;
	}
	
	private void init(){
		setDrawingCacheEnabled(true);
		mCircleImageRadius = 40;
		createImageView();
	}
	
	private void createImageView(){
		removeAllViewsInLayout();
		if(mCiv == null){
			mCiv = (CircleImageView) LayoutInflater.from(getContext()).inflate(R.layout.civ_layout, null);
		}
		
		LayoutParams lp = mCiv.getLayoutParams();
		if(lp == null){
			lp = new LayoutParams(mCircleImageRadius * 2,mCircleImageRadius * 2);
		}
		
		lp.width = mCircleImageRadius * 2;
		lp.height = lp.width;
		mCiv.setLayoutParams(lp);
		
		mCarImageView = new ImageView(getContext());
		mCarImageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_car));
		LayoutParams params = new LayoutParams(lp.width / 2, lp.width);
		mCarImageView.setLayoutParams(params);
		
		addView(mCiv);
		addView(mCarImageView, 0);
	}
	
	@SuppressLint("NewApi")
	public void rotateDirect(float degree){
		if(getChildCount() < 2){
			return;
		}
		final View view = getChildAt(1);
		if(view != null){
			view.setRotation(degree);
		}
		invalidateView();
	}
	
	public void setCircleImageSize(int radius){
		mCircleImageRadius = radius;
		createImageView();
	}
	
	public void setUserDrawable(Drawable drawable){
		AppLogic.runOnUiGround(new Runnable1<Drawable>(drawable) {
			
			@Override
			public void run() {
				mCiv.setImageDrawable(mP1);
			}
		}, 0);
	}
	
	public void setUserBitmap(Bitmap bitmap){
		AppLogic.runOnUiGround(new Runnable1<Bitmap>(bitmap) {
			
			@Override
			public void run() {
				mCiv.setImageBitmap(mP1);
			}
		}, 0);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChild(mCiv, widthMeasureSpec, heightMeasureSpec);
		measureChild(mCarImageView, widthMeasureSpec, heightMeasureSpec);
		
		int imageWidth = mCiv.getMeasuredWidth();
		
		// 计算三角形的高
		int parentWidthSize = imageWidth;
		int parentHeightSize = parentWidthSize + (imageWidth / 2);
		int parentWidthSpec = MeasureSpec.makeMeasureSpec(parentWidthSize, MeasureSpec.EXACTLY);
		int parentHeightSpec = MeasureSpec.makeMeasureSpec(parentHeightSize, MeasureSpec.EXACTLY);
		setMeasuredDimension(parentWidthSpec, parentHeightSpec);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// 居中向上排放
		int left = (r - l - mCiv.getMeasuredWidth()) / 2;
		int right = left + mCiv.getMeasuredWidth();
		int top = 0;
		int bottom = mCiv.getMeasuredHeight();
		for(int i = 0;i<getChildCount();i++){
			final View view = getChildAt(i);
			if(view instanceof CircleImageView){
				view.layout(left, top, right, bottom);
			}else {
				int lf = (getWidth() - view.getMeasuredWidth()) / 2;
				int tp = getHeight() / 3;
				int rh = lf + view.getMeasuredWidth();
				int bo = tp + view.getMeasuredHeight();
				view.layout(lf, tp, rh, bo);
			}
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
	}
	
	/**
	 * 得到位于父控件的点坐标
	 * @return
	 */
	public Point getAbsCenterPoint(){
		if(mCenterPoint == null){
			mCenterPoint = new Point();
		}
		
		mCenterPoint.x = getLeft() + getWidth() / 2;
		mCenterPoint.y = getTop() + getHeight() / 2;
		return mCenterPoint;
	}
	
	public CircleImageView getCiv(){
		return mCiv;
	}
	
	public void invalidateView(){
		if(Looper.myLooper() == Looper.getMainLooper()){
			super.invalidate();
		}else {
			postInvalidate();
		}
	}
}