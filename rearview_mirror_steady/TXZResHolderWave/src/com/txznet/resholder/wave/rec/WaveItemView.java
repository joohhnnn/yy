package com.txznet.resholder.wave.rec;

import com.txznet.comm.ui.util.LayouUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

public class WaveItemView extends View {
	
	private WaveItemView(Context context) {
		super(context);
	}

	public int minHeight,maxHeight;
	
	public Animation animation;
	private float mScale;
	
	public WaveItemView(Context context,int minHeight,int maxHeight, Drawable src){
		super(context);
		/*setScaleType(ScaleType.FIT_XY);
		setImageResource(R.drawable.src_car_record_sound_item);*/
		//setImageDrawable(src);
		setBackground(LayouUtil.getDrawable("shape_car_record_sound_item"));
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		mScale = minHeight / (float)maxHeight;
		this.setScaleY(mScale);
		animation = new ScaleAnimation(1, 1, mScale, 1, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(260);
		animation.setRepeatMode(Animation.REVERSE);
		animation.setRepeatCount(Animation.INFINITE);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				count++;
				if(count>=Integer.MAX_VALUE){
					count = 0;
				}
				if (count % 2 == 0 && !isWaving) {
					animation.cancel();
					setScaleY(mScale);
					setAnimation(null);
				}
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				
			}
		});
		/*setScaleType(ScaleType.CENTER_INSIDE);
		setImageDrawable(src);*/
	}
	
	class WaveAnimation extends Animation{
		
	}
	
	int count = 0;
	public void start(){
		this.setScaleY(1);
		setAnimation(animation);
		count = 0;
		animation.reset();
		animation.start();
		isWaving = true;
	}
	
	private boolean isWaving = false;
	
	public void stop(){
		stop(false);
	}

	public void stop(boolean reset) {
		isWaving = false;

		if(reset) {
			setScaleY(mScale);
		}
	}

}
