package com.txznet.txz.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.txznet.txz.util.TXZHandler;

public class VoiceImageView extends ImageView{
	
	private static final int STEP_INIT = 0;
	private static final int STEP_RECORD = 1;
	private static final int STEP_HANDLE = 2;
	
	private static final int MIN_VOLUME_VALUE = 0;
	private static final int MAX_VOLUME_VALUE = 30;
	private static final int WAVE_DURATION = 100;
	private static final String XFERMODE_COLOR = "#0E3242";
	
	// 控制遮罩高度
	int state;
	int volume;
	int lastVolume;
	long lastTime;
	
	// 遮罩Bitmap
	Bitmap mXfermodeBitmap;
	Bitmap mSrcBitmap;
	Paint paint;
	Rect rect;
	
	Interpolator mInterpolator;
	WaveAnimation mWaveAnimation;
	TXZHandler mHandler = new TXZHandler(Looper.getMainLooper());

	public VoiceImageView(Context context) {
		this(context,null);
	}
	
	public VoiceImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VoiceImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		if(Build.VERSION.SDK_INT < VERSION_CODES.HONEYCOMB){
			setDrawingCacheEnabled(true);
		}
		setLayerType(LAYER_TYPE_SOFTWARE, null);

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setDither(true);
		paint.setColor(Color.parseColor(XFERMODE_COLOR));

		rect = new Rect();
		rect.left = 0;
		rect.top = 0;
		
		mWaveAnimation = new WaveAnimation();
		mInterpolator = new AccelerateInterpolator();
	}
	
	public void setState(int state){
		this.state = state;
		switch (state) {
		case STEP_INIT:
			setVolume(0);
			break;

		case STEP_RECORD:
			break;
			
		case STEP_HANDLE:
			setVolume(0);
			break;
		}
	}
	
	public void setVolume(int vol){
		this.lastVolume = this.volume;
		this.volume = vol2height(vol);
		if(this.volume == 0){
			this.volume = 1 * getHeight() / 10;
		}
		
		long currentTime = System.currentTimeMillis();
		long duration = currentTime - lastTime;
		if(duration > 5000){
			// 首次
			lastTime = currentTime;
			return;
		}else {
			lastTime = currentTime;
		}
		
		if(duration > WAVE_DURATION){
			duration = WAVE_DURATION;
		}
		
		mWaveAnimation.start(lastVolume, volume, (int)duration, mInterpolator);
		refreshView();
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if(mXfermodeBitmap == null){
			createBitmap(getHeight());
			invalidate();
		}
		
		if(mWaveAnimation.computeOffsetY()){
			this.volume = (int) mWaveAnimation.getOffsetY();
			
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
			canvas.drawBitmap(mXfermodeBitmap, 0, - this.volume, paint);
			paint.setXfermode(null);
			invalidate();
		}
		
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
		canvas.drawBitmap(mXfermodeBitmap, 0, - this.volume, paint);
		paint.setXfermode(null);

	}
	
	private boolean createBitmap(int height){
		if(mXfermodeBitmap != null){
			mXfermodeBitmap.recycle();
		}
		
		mXfermodeBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
		Canvas c = new Canvas(mXfermodeBitmap);
		rect.bottom = height;
		rect.right = getWidth();
		c.drawRect(rect, paint);
		
		return mXfermodeBitmap != null;
	}
	
	private void refreshView(){
		if(Looper.myLooper() == Looper.getMainLooper()){
			invalidate();
		}else {
			postInvalidate();
		}
	}
	
	private int vol2height(int vol){
		float gap = MAX_VOLUME_VALUE - MIN_VOLUME_VALUE;
		if(vol > MAX_VOLUME_VALUE){
			vol = MAX_VOLUME_VALUE;
		}
		
		if(vol < MIN_VOLUME_VALUE){
			vol = MIN_VOLUME_VALUE;
		}
		
		float precent = 1.0f * (vol - MIN_VOLUME_VALUE) / gap;
		return (int) (getHeight() * precent);
	}
	
	/**
	 * 过渡动画
	 */
	class WaveAnimation {
		private float offsetY;
		private float fromOffsetY;
		private float toOffsetY;

		private boolean finished = true;
		private long startTime;
		private int duration;
		private float durationReciprocal;
		private Interpolator interpolator;

		public void start(float fromOffsetY, float toOffsetY, int duration, Interpolator interpolator) {
			this.startTime = AnimationUtils.currentAnimationTimeMillis();
			this.duration = duration;
			this.offsetY = fromOffsetY;
			this.fromOffsetY = fromOffsetY;
			this.toOffsetY = toOffsetY;
			this.durationReciprocal = 1.0f / duration;
			this.finished = false;
			this.interpolator = interpolator;
		}

		public boolean computeOffsetY() {
			if (finished) {
				return false;
			}
			long timePassed = AnimationUtils.currentAnimationTimeMillis() - startTime;
			if (timePassed < duration) {
				float input = timePassed * durationReciprocal;
				float interpolatedTime = interpolator != null ? interpolator.getInterpolation(input) : input;
				offsetY = fromOffsetY + ((toOffsetY - fromOffsetY) * interpolatedTime);
				if (offsetY == toOffsetY) {
					finished = true;
				}
			} else {
				offsetY = toOffsetY;
				finished = true;
			}
			return true;
		}

		public float getOffsetY() {
			return offsetY;
		}

		public final boolean isFinished() {
			return finished;
		}
	}
}
