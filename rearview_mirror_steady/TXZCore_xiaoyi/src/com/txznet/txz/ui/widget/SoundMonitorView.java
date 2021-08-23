package com.txznet.txz.ui.widget;

import android.content.Context;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.txznet.txz.R;
import com.txznet.txz.util.TXZHandler;

public class SoundMonitorView extends FrameLayout{
	
	private View mSoundMonitorView;
	
	private ImageView mLoadingIv;
	private RippleBackground mAlphaView;
	private VoiceImageView mVoiceImageView;

	static final int STATE_INIT = 0;
	static final int STATE_RECORD = 1;
	static final int STATE_RECOGONIZE = 2;
	
	int state;
	int level;
	
	TXZHandler handler = new TXZHandler(Looper.getMainLooper());
	
	public SoundMonitorView(Context context) {
		this(context,null);
	}
	
	public SoundMonitorView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public SoundMonitorView(Context context, AttributeSet attrs,int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init(){
		if(mSoundMonitorView == null){
			this.mSoundMonitorView = LayoutInflater.from(getContext()).inflate(R.layout.widget_sound_monitor_view, null);
			this.addView(mSoundMonitorView);
			
			this.mLoadingIv = (ImageView)findViewById(R.id.rotate_iv);
			this.mAlphaView = (RippleBackground)findViewById(R.id.scale_view);
			this.mVoiceImageView = (VoiceImageView)findViewById(R.id.voice_monitor_viv);
		}
		
		setState(STATE_INIT);
	}
	
	private void initState(){
		this.mLoadingIv.setVisibility(INVISIBLE);
		this.mAlphaView.setVisibility(INVISIBLE);
	}
	
	private void showListenerAlpha(){
		if(this.mLoadingIv.getVisibility() != GONE || this.mLoadingIv.getVisibility() != INVISIBLE){
			this.mLoadingIv.setAnimation(null);
			this.mLoadingIv.setVisibility(INVISIBLE);
		}
		
		if(this.mAlphaView.getVisibility() == GONE || this.mAlphaView.getVisibility() == INVISIBLE){
			this.mAlphaView.setVisibility(View.VISIBLE);
		}

		mAlphaView.startRippleAnimation();
	}
	
	/**
	 * 显示加载中RotateAnimation
	 */
	public void showLoading(){
		if(mAlphaView.getVisibility() != GONE || mAlphaView.getVisibility() != INVISIBLE){
			mAlphaView.setVisibility(INVISIBLE);
		}
		
		if(mLoadingIv.getVisibility() == GONE || mLoadingIv.getVisibility() == INVISIBLE){
			mLoadingIv.setVisibility(View.VISIBLE);
		}
		startRotateAnimation(mLoadingIv);
	}
	
	public void setState(final int state) {
		this.state = state;
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (state == STATE_INIT) {
					initState();
				} else if (state == STATE_RECORD) {
					showListenerAlpha();
				} else if (state == STATE_RECOGONIZE) {
					showLoading();
				}
				
				mVoiceImageView.setState(state);
			}
		}, 50);
	}
	
	public int getState(){
		return this.state;
	}
	
	public void setVolume(final int volume) {
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mVoiceImageView.setVolume(volume);
			}
		}, 50);
	}
	
	private void startRotateAnimation(final View view){
		RotateAnimation ra = (RotateAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.widget_image_rotate);
		view.startAnimation(ra);
	}
}
