package com.txznet.comm.ui.theme.test.anim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.loader.AppLogicBase;

public class AnimFullView extends IImageView {
	private static final String TAG = AnimFullView.class.getSimpleName();

	private LruCache mLruCache;

	//private boolean hasHandle;
	private int reportNum;

	private FrameAnimation reportAnim1;//播报,眨眼
	private FrameAnimation reportAnim2;//播报,讲话
	private FrameAnimation listeningAnim1;//倾听,起始动画
	private FrameAnimation listeningAnim2;//倾听，重复动画
	private FrameAnimation handleAnim1;//处理,起始动画
	private FrameAnimation handleAnim2;//处理，重复动画
	private FrameAnimation handleAnim3;//处理，结束动画

	/*public AnimFullView(AbsStepView parentView, Context context) {
		super(context);
		init();
	}*/

	public AnimFullView( Context context) {
		super(context);
		LogUtil.logd(WinLayout.logTag+ "AnimFullView:init " );
		init();
	}

	//初始化资源
	private void init(){
		/*int maxMemory = (int) (Runtime.getRuntime().totalMemory()/1024);
		int cacheSize = maxMemory/8;
		mLruCache = new LruCache<String,Bitmap>(cacheSize){
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount()/1024;
			}
		};*/

		initReportAnim();
		initListeningAnim();
		initHandleAnim();

		//hasHandle = false;
		reportNum = 0;
		//设置默认帧，避免在性能弱的设备上，动画加载有延时
		this.setImageDrawable(LayouUtil.getDrawable("full_report_00000"));
	}

	//初始化播报状态动画资源
	private void initReportAnim(){
		String name = "";
		reportAnim1 = new FrameAnimation();
		reportAnim2= new FrameAnimation();
		for(int i =0;i < 13;i+=2){
			if (i < 10){
				name = "full_report_0000" + i;
			}else {
				name = "full_report_000" + i;
			}
			Drawable drawable = LayouUtil.getDrawable(name);

			Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();
			if (mBitmap.isRecycled()){drawable.mutate();}

			reportAnim1.addFrame(drawable,60);
		}
		for(int i =13;i < 16;i+=2){
			name = "full_report_000" + i;
			Drawable drawable = LayouUtil.getDrawable(name);
			reportAnim2.addFrame(drawable,120);
			//reportAnim.addFrame(drawable,60);

			Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();
			if (mBitmap.isRecycled()){drawable.mutate();}
		}

		reportAnim1.setOnFrameAnimationListener(new FrameAnimation.OnFrameAnimationListener() {
			@Override
			public void onStart() {

			}

			@Override
			public void onEnd() {
				AppLogicBase.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						showReportAnim2();
					}
				});
			}
		});
		reportAnim2.setOnFrameAnimationListener(new FrameAnimation.OnFrameAnimationListener() {
			@Override
			public void onStart() {}

			@Override
			public void onEnd() {
				//if (!reportStop){
				if (++reportNum == 5) {
					reportNum = 0;
					AppLogicBase.runOnUiGround(new Runnable() {
						@Override
						public void run() {
							showReportAnim1();
						}
					});
				} else if(reportNum > 0) {
					AppLogicBase.runOnUiGround(new Runnable() {
						@Override
						public void run() {
							showReportAnim2();
						}
					});
				}
				//}
			}
		});
	}

	//初始化倾听状态动画资源
	private void initListeningAnim(){
		String name = "";
		listeningAnim1 = new FrameAnimation();
		listeningAnim2 = new FrameAnimation();
		for(int i =0;i < 15;i+=2){
			if (i < 10){
				name = "full_listening_0000" + i;
			}else {
				name = "full_listening_000" + i;
			}
			Drawable drawable = LayouUtil.getDrawable(name);

            Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();
            if (mBitmap.isRecycled()){drawable.mutate();}
			listeningAnim1.addFrame(drawable,60);
		}
		for(int i = 15;i < 29;i+=2){
			name = "full_listening_000" + i;
			Drawable drawable = LayouUtil.getDrawable(name);

			Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();
			if (mBitmap.isRecycled()){drawable.mutate();}
			listeningAnim2.addFrame(drawable,60);
		}

		listeningAnim1.setOnFrameAnimationListener(new FrameAnimation.OnFrameAnimationListener() {
			@Override
			public void onStart() {

			}

			@Override
			public void onEnd() {
				AppLogicBase.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						showListeningAnim2();
					}
				});
			}
		});
	}
	//初始化处理状态动画资源
	private void initHandleAnim(){
		String name = "";
		handleAnim1 = new FrameAnimation();
		handleAnim2 = new FrameAnimation();
		handleAnim3 = new FrameAnimation();
		for(int i = 0;i < 6;i++){
			name = "full_handle_0000" + i;
			Drawable drawable = LayouUtil.getDrawable(name);


            Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();
            if (mBitmap.isRecycled()){drawable.mutate();}
			handleAnim1.addFrame(drawable,40);
		}
		for(int i = 6;i < 18;i+=2){
			if (i < 10){
				name = "full_handle_0000" + i;
			}else {
				name = "full_handle_000" + i;
			}
			Drawable drawable = LayouUtil.getDrawable(name);
			handleAnim2.addFrame(drawable,60);

            Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();
            if (mBitmap.isRecycled()){drawable.mutate();}
		}
		for(int i = 18;i < 24;i++){
			name = "full_handle_000" + i;
			Drawable drawable = LayouUtil.getDrawable(name);


			Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();
			if (mBitmap.isRecycled()){drawable.mutate();}
			handleAnim3.addFrame(drawable,40);
		}

		handleAnim1.setOnFrameAnimationListener(new FrameAnimation.OnFrameAnimationListener() {
			@Override
			public void onStart() {

			}

			@Override
			public void onEnd() {
				AppLogicBase.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						showHandleAnim2();
					}
				});
			}
		});
	}

	private void showListeningAnim2(){
		if (listeningAnim2 != null){
			this.setImageDrawable(listeningAnim2);
			listeningAnim2.setOneShot(false);
			listeningAnim2.stop();
			listeningAnim2.start();
		}

	}

	private void showHandleAnim2(){
		if (handleAnim2 != null){
			this.setImageDrawable(handleAnim2);
			handleAnim2.setOneShot(false);
			handleAnim2.stop();
			handleAnim2.start();
		}

	}

	private void showHandleAnim3(){
		if (handleAnim3 != null){
			this.setImageDrawable(handleAnim3);
			handleAnim3.setOneShot(true);
			handleAnim3.stop();
			handleAnim3.start();
		}

	}

	private void showReportAnim1(){
		if (reportAnim1 != null){
			this.setImageDrawable(reportAnim1);
			reportAnim1.setOneShot(true);
			reportAnim1.stop();
			reportAnim1.start();
		}

	}

	private void showReportAnim2(){
		if (reportAnim2 != null){
			this.setImageDrawable(reportAnim2);
			reportAnim2.setOneShot(true);
			reportAnim2.stop();
			reportAnim2.start();
		}

	}

	//开始动画
	@Override
	public void playStartAnim(int State) {
		switch (State){
			case 0:
				reportNum = 0;
				/*if (hasHandle){
					AppLogicBase.runOnUiGround(new Runnable() {
						@Override
						public void run() {
							showHandleAnim3();
						}
					});
					AppLogicBase.runOnUiGround(new Runnable() {
						@Override
						public void run() {
							showReportAnim1();
						}
					},240);
				}else {
					reportAnim1.setOneShot(false);
					this.setImageDrawable(reportAnim1);
					reportAnim1.stop();
					reportAnim1.start();
				}*/
				reportAnim1.setOneShot(false);
				this.setImageDrawable(reportAnim1);
				reportAnim1.stop();
				reportAnim1.start();
				//hasHandle = false;
				break;
			case 1:
				listeningAnim1.setOneShot(true);
				this.setImageDrawable(listeningAnim1);
				listeningAnim1.stop();
				listeningAnim1.start();
				break;
			case 2:
				//hasHandle = true;
				handleAnim1.setOneShot(true);
				this.setImageDrawable(handleAnim1);
				handleAnim1.stop();
				handleAnim1.start();
				break;
		}
	}

	//停止动画
	@Override
	public void playEndAnim(int State) {
		switch (State){
			case 0:
				reportNum = -1;
				this.setImageDrawable(reportAnim1);
				reportAnim1.stop();
				reportAnim2.stop();
				break;
			case 1:
				this.setImageDrawable(listeningAnim1);
				listeningAnim1.stop();
				listeningAnim2.stop();
				break;
			case 2:
				this.setImageDrawable(handleAnim1);
				handleAnim1.stop();
				handleAnim2.stop();
                handleAnim3.stop();
				break;
		}
	}

	//停止所有动画
    @Override
    public void stopAllAnim() {
        reportNum = -1;
        reportAnim1.stop();
        reportAnim2.stop();
        listeningAnim1.stop();
        listeningAnim2.stop();
        handleAnim1.stop();
        handleAnim2.stop();
        handleAnim3.stop();
    }

    //回收帧动画资源
	@Override
	public void destory(){
		if (reportAnim1 != null){
			reportAnim1.destory();
			reportAnim1 = null;
		}
		if (reportAnim2 != null){
			reportAnim2.destory();
			reportAnim2 = null;
		}
		if (listeningAnim1 != null){
			listeningAnim1.destory();
			listeningAnim1= null;
		}
		if (listeningAnim2 != null){
			listeningAnim2.destory();
			listeningAnim2 = null;
		}
		if (handleAnim1 != null){
			handleAnim1.destory();
			handleAnim1 = null;
		}
		if (handleAnim2 != null){
			handleAnim2.destory();
			handleAnim2 = null;
		}
		if (handleAnim3 != null){
			handleAnim3.destory();
			handleAnim3 = null;
		}

		this.setImageBitmap(null);
		this.setImageDrawable(null);
	}
}