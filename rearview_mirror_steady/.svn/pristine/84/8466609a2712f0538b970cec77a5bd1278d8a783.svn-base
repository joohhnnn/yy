package com.txznet.nav.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.txznet.nav.MyApplication;
import com.txznet.nav.R;
import com.txznet.txz.util.runnables.Runnable1;

public class DirectionCiv extends ViewGroup{

	private int mSquareColor = Color.parseColor("#34bfff");
	private int mLength;
	private int mRectangleHeight;
	private int mCircleImageRadius;
	private int mMaskRadius;
	private int mRadiusDelay = 2;
	
	private float mRotateDegree = 145;
	private boolean reDraw;
	
	private Paint mSquarePaint;
	private Paint mBitmapPaint;
	private Point mCenterPoint;
	private Xfermode mXfermode = new PorterDuffXfermode(Mode.DST_OUT);
	
	private Bitmap mSrcBitmap;
	private Bitmap mMaskBitmap;
	
	private Bitmap mCarBitmap;
	
	private CircleImageView mCiv;
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
		mCircleImageRadius = 32;
		createImageView();
		
		mInterpolator = new LinearInterpolator();
	}
	
	private Interpolator mInterpolator;
	private volatile boolean isPlayAnim = false;
	
	public void playAnim(boolean play){
		isPlayAnim = play;
	}
	
	public boolean isPlayAnim(){
		return isPlayAnim;
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
		addView(mCiv);
	}
	
	public void rotateDirect(float degree){
		mRotateDegree = degree + 180;
		invalidateView();
	}
	
	public void setCircleImageSize(int radius){
		mCircleImageRadius = radius;
		createImageView();
	}
	
	public void setUserDrawable(Drawable drawable){
		MyApplication.getApp().runOnUiGround(new Runnable1<Drawable>(drawable) {
			
			@Override
			public void run() {
				mCiv.setImageDrawable(mP1);
			}
		}, 0);
	}
	
	public void setUserBitmap(Bitmap bitmap){
		MyApplication.getApp().runOnUiGround(new Runnable1<Bitmap>(bitmap) {
			
			@Override
			public void run() {
				mCiv.setImageBitmap(mP1);
			}
		}, 0);
	}
	
	public void setBorderColor(int color){
		MyApplication.getApp().runOnUiGround(new Runnable1<Integer>(color) {
			
			@Override
			public void run() {
				mCiv.setBorderColor(mP1);
				mSquareColor = mP1;
				reDraw = true;
			}
		}, 0);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChild(mCiv, widthMeasureSpec, heightMeasureSpec);
		
		int imageWidth = mCiv.getMeasuredWidth();
		mMaskRadius = (imageWidth / 2) + mRadiusDelay;
		mLength = imageWidth;
		
		// 计算三角形的高
		int rectangleHeight = (int) Math.sqrt(Math.pow(mLength, 2) - Math.pow(mLength / 2, 2));
		int parentWidthSize = rectangleHeight * 2;
		int parentHeightSize = parentWidthSize;
		int parentWidthSpec = MeasureSpec.makeMeasureSpec(parentWidthSize, MeasureSpec.EXACTLY);
		int parentHeightSpec = MeasureSpec.makeMeasureSpec(parentHeightSize, MeasureSpec.EXACTLY);
		setMeasuredDimension(parentWidthSpec, parentHeightSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int left = (r - l - mCiv.getMeasuredWidth()) / 2;
		int right = left + mCiv.getMeasuredWidth();
		int top = (b - t - mCiv.getMeasuredHeight()) / 2;
		int bottom = top + mCiv.getMeasuredHeight();
		mCiv.layout(left, top, right, bottom);
	}
	
	private float DURATION = 10.0f;
	private int ratio = 0;
	private float mMill = 0;
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		Log.d("DirectionCiv", "DirectionCiv dispatchDraw -- >");

		if(isPlayAnim){
			if(mMill == DURATION){
				ratio = -1;
			}else if(mMill == 0){
				ratio = 1;
			}
			
			mMill += ratio;
			float input = mMill / DURATION;
			float alpha = mInterpolator.getInterpolation(input);
			setAlpha(alpha);
		}else {
			setAlpha(1.0f);
		}
		
		if(mBitmapPaint == null){
			mBitmapPaint = new Paint();
			mBitmapPaint.setDither(true);
			mBitmapPaint.setFilterBitmap(false);
		}
		
//		int src = canvas.saveLayer(0, 0, getWidth(), getHeight(), null,0);
//		drawRectangle(canvas);
//		drawMask(canvas);
//		canvas.restoreToCount(src);
		makeCarBitmap();
		int save = canvas.save();
		canvas.rotate(mRotateDegree,getWidth() / 2,getHeight() / 2);
//		canvas.translate((getWidth() / 2) - mLength / 2, 0);
		canvas.translate((getWidth() / 2) - mLength / 2, 0);
//		canvas.drawBitmap(mCarBitmap, (getWidth() - mCarBitmap.getWidth()) / 2, (getHeight() / 2) - 5, mBitmapPaint);
		canvas.drawBitmap(mCarBitmap, 0, 0, mBitmapPaint);
		canvas.restoreToCount(save);
		
		mCiv.setRotate(mRotateDegree);
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
	
	public void updateFinger(int fingerX,int fingerY){
		final Point point = getAbsCenterPoint();
		int delayX = fingerX - point.x;
		int delayY = fingerY - point.y;
		
		Log.d("Finger", "delayX -->"+delayX + ",delayY -->"+delayY);
		if(delayX == 0 && delayY == 0){
			return;
		}
		
		float ratio;
		if(delayX >= 0 && delayY <= 0){         											// 0~90°
			if(delayX == 0){
				mRotateDegree = 0;
			}else if(delayY == 0){
				mRotateDegree = 90;
			}else {
				ratio = (float) Math.atan2(Math.abs(delayX) , Math.abs(delayY));
				mRotateDegree = (float) Math.toDegrees(ratio);
			}
		}else if(delayX >= 0 && delayY >= 0){  												// 90~180°
			if(delayX == 0){
				mRotateDegree = 180;
			}else if(delayY == 0){
				mRotateDegree = 90;
			}else {
				ratio = (float) Math.atan2(Math.abs(delayY) , Math.abs(delayX));
				mRotateDegree = (float) Math.toDegrees(ratio) + 90;
			}
		}else if(delayX <= 0 && delayY >= 0){  												// 180~270°
			if(delayX == 0){
				mRotateDegree = 180;
			}else if(delayY == 0){
				mRotateDegree = 270;
			}else {
				ratio = (float) Math.atan2(Math.abs(delayX) , Math.abs(delayY));
				mRotateDegree = (float) Math.toDegrees(ratio) + 180;
			}
		}else if(delayX <= 0 && delayY <= 0){  												// 270~360°
			if(delayX == 0){
				mRotateDegree = 0;
			}else if(delayY == 0){
				mRotateDegree = 270;
			}else {
				ratio = (float) Math.atan2(Math.abs(delayY) , Math.abs(delayX));
				mRotateDegree = (float) Math.toDegrees(ratio) + 270;
			}
		}
		
		invalidateView();
	}
	
	public void setRotateDegree(float degress){
		mRotateDegree = degress;
		invalidateView();
	}
	
	private void drawRectangle(Canvas canvas){
		if(mSrcBitmap == null || reDraw){
			reDraw = false;
			makeRectangleBitmap();
		}

		if(mSrcBitmap != null){
			int save = canvas.save();
			canvas.rotate(mRotateDegree,getWidth() / 2,getHeight() / 2);
			canvas.translate((getWidth() / 2) - mLength / 2, 0);
			canvas.drawBitmap(mSrcBitmap, 0, 0, mBitmapPaint);
			canvas.restoreToCount(save);
		}
	}
	
	private void drawMask(Canvas canvas){
//		Bitmap bitmap = makeMaskBitmap();
//		if(bitmap != null){
//			canvas.save();
//			canvas.translate((getWidth() / 2) - mMaskRadius, (getHeight() / 2) - mMaskRadius);
//			mBitmapPaint.setXfermode(mXfermode);
//			canvas.drawBitmap(makeMaskBitmap(), 0, 0, mBitmapPaint);
//			mBitmapPaint.setXfermode(null);
//			canvas.restore();
//		}
		
		if(mMaskBitmap == null || reDraw){
			reDraw = false;
			makeMaskBitmap();
		}
		
		if(mMaskBitmap != null){
			canvas.save();
			canvas.translate((getWidth() / 2) - mMaskRadius, (getHeight() / 2) - mMaskRadius);
			mBitmapPaint.setXfermode(mXfermode);
			canvas.drawBitmap(mMaskBitmap, 0, 0, mBitmapPaint);
			mBitmapPaint.setXfermode(null);
			canvas.restore();
		}
	}
	
	private void makeCarBitmap(){
		if(mCarBitmap == null){
			if(mSquarePaint == null){
				mSquarePaint = new Paint();
				mSquarePaint.setAntiAlias(true);
				mSquarePaint.setDither(true);
			}
			
			Drawable drawable = getResources().getDrawable(R.drawable.icon_car);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			mRectangleHeight = (int) Math.sqrt(Math.pow(mLength, 2) - Math.pow(mLength / 2, 2));
			mCarBitmap = Bitmap.createBitmap(mLength, mRectangleHeight, Config.ARGB_8888);
			Canvas canvas = new Canvas(mCarBitmap);
			drawable.draw(canvas);
		}
	}
	
	private Bitmap makeRectangleBitmap(){
		if(mSrcBitmap != null){
			mSrcBitmap.recycle();
			mSrcBitmap = null;
		}
		
		if(mSquarePaint == null){
			mSquarePaint = new Paint();
			mSquarePaint.setAntiAlias(true);
			mSquarePaint.setDither(true);
		}
		mSquarePaint.setColor(mSquareColor);
		
		mRectangleHeight = (int) Math.sqrt(Math.pow(mLength, 2) - Math.pow(mLength / 2, 2));
		mSrcBitmap = Bitmap.createBitmap(mLength, mRectangleHeight, Config.ARGB_8888);
		
		Canvas canvas = new Canvas(mSrcBitmap);
		Path path = new Path();  
        path.moveTo(mLength / 2, 0);			// 此点为多边形的起点  
        path.lineTo(mLength, mRectangleHeight);  
        path.lineTo(0, mRectangleHeight);  
        path.close(); 							// 使这些点构成封闭的多边形
        canvas.drawPath(path, mSquarePaint);
        
        return mSrcBitmap;
	}
	
	private Bitmap makeMaskBitmap(){
		if(mMaskBitmap != null){
			mMaskBitmap.recycle();
			mMaskBitmap = null;
		}
		
		float radius = mMaskRadius;
		if(mSquarePaint == null){
			mSquarePaint = new Paint();
			mSquarePaint.setAntiAlias(true);
			mSquarePaint.setDither(true);
		}
		
		mMaskBitmap = Bitmap.createBitmap((int)radius * 2, (int)radius * 2, Config.ARGB_8888);
		Canvas canvas = new Canvas(mMaskBitmap);
		canvas.drawCircle(radius, radius, radius, mSquarePaint);
		
		return mMaskBitmap;
	}
	
	public void invalidateView(){
		if(Looper.myLooper() == Looper.getMainLooper()){
			super.invalidate();
		}else {
			postInvalidate();
		}
	}
}