package com.txznet.marketing.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.txznet.marketing.MainActivity;
import com.txznet.marketing.R;
import com.txznet.marketing.SDKDemoApp;
import com.txznet.marketing.TipDialog;
import com.txznet.marketing.WakeupCallback;
import com.txznet.marketing.bean.CommandPoint;
import com.txznet.marketing.util.AudioFocusUtil;
import com.txznet.marketing.util.TimerUitl;
import com.txznet.marketing.util.VocieUtil;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZTtsManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MediaPlayerSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback, OnClickListener, Runnable {

	private static MediaPlayerSurfaceView instance;

	public static MediaPlayerSurfaceView getInstance() {
		return instance;
	}

	private SurfaceHolder mHolder;
	MediaPlayer player = null;
	//音频焦点管理
    AudioFocusUtil audioFocusUtil;

	public MediaPlayerSurfaceView(Context context) {
		super(context);
		init();
	}

	public MediaPlayerSurfaceView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MediaPlayerSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		instance = this;
		mHolder = getHolder();
		mHolder.addCallback(this);
		setOnClickListener(this);
		setFocusable(true);
		setFocusableInTouchMode(true);
		this.setKeepScreenOn(true);

        audioFocusUtil = new AudioFocusUtil();

		//根据机器屏幕比例设置画面比例
		setSize();
	}

	//请求音频焦点
    public void requestAudioFocus(){
	    if(audioFocusUtil != null){
	        int requestCode = audioFocusUtil.requestAudioFocus(new AudioFocusUtil.AudioListener() {
                @Override
                public void start() {
                    MediaPlayerSurfaceView.getInstance().start();
                }

                @Override
                public void pause() {

                }
            },MainActivity.getInstance());
	        if (requestCode == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
	            start();
            }
        }
    }

    //释放音频焦点
    private void releaseAudioFocus(){
	    if (audioFocusUtil != null){
	        audioFocusUtil.releaseTheAudioFocus();
        }
    }

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (player == null) {
			player = MediaPlayer.create(MainActivity.getInstance(),
					R.raw.marketing_video);

			// MediaPlayer.create 的官方说明 ：
			// On success, prepare() will already have been called and must not
			// be called again.

			// 根据上面的说明 ，
			// 一旦用create来创建媒体资源，
			// 那么prepare就不再需要调用了，也就是说连下面这句也是不需要的。
			// player.reset();

			try {
				player.setDisplay(holder);
				player.seekTo(0);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}

			player.setOnPreparedListener(new OnPreparedListener() {
				public void onPrepared(MediaPlayer mp) {
                    requestAudioFocus();
                    //mp.start();
					SDKDemoApp.runOnUiGround(
							MediaPlayerSurfaceView.getInstance(), 0);
				}
			});

			//播放完之后显示重播按钮
			player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mediaPlayer) {
					Log.d("jack", "onCompletion: video end");
					TipDialog.getInstance(MainActivity.getInstance()).showButton().show();
				}
			});

		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		release();
	}


	Handler mHandler = new Handler();
	@Override
	public void onClick(View v) {
		if (player != null) {
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.start();
			}
		}
	}

	public static int currIndex = 0;
	WakeupCallback mWakeupCallback;

	@Override
	public void run() {
		if (player != null) {
            Log.d("jack", "run:playerCurrentPosition: "+player.getCurrentPosition()+"--"+CommandPoint.pointArr[currIndex]);

			if ((Math.abs(CommandPoint.pointArr[currIndex]
					- player.getCurrentPosition())) <= 125) { // 到了指令处
				showAndPause();
				return;
			}
			SDKDemoApp.runOnUiGround(this, 250);
		}
	}

	//重播
	public void rePlay(){
		currIndex = 0;
		player.seekTo(0);
		player.start();
		SDKDemoApp.runOnUiGround(this,0);
		/*if (player == null){
			Log.d("jack", "rePlay: null");
		}
		else {
			Log.d("jack", "rePlay: notNull");
		}*/
		/*player.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				SDKDemoApp.runOnUiGround(
						MediaPlayerSurfaceView.getInstance(), 0);
			}
		});*/
	}

	//根据机器屏幕比例设置画面比例
	private void setSize(){
		//获取设备的宽度和高度
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		Log.d("jack", "setSize: "+width+"--"+height);

		int x = width*9 - height*16;

		//宽高比大于16:9,高度不变，设置水平居中
		if (x>0){
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(height*16/9,height);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			MediaPlayerSurfaceView.getInstance().setLayoutParams(layoutParams);
		}
		else {//宽高比小于16:9，宽度不变，设置垂直居中
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width,width*9/16);
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			MediaPlayerSurfaceView.getInstance().setLayoutParams(layoutParams);
		}
	}

	//播放视频
	public void start(){
		if (player != null && !TipDialog.getInstance(MainActivity.getInstance()).getVisibility()) {
			player.start();
		}
	}

	//暂停视频
	public void pause(){
		if (player != null) {
			player.pause();
		}
        releaseAudioFocus();
	}

	//退出视频
	public void release(){
		if (player != null) {
			player.stop();
			player.release();
			player = null;
		}

		currIndex = 0;
		TipDialog.getInstance(MainActivity.getInstance()).dismiss();
		//注销计时器
		TimerUitl.getInstance().destory();
		//注销语音识别
		if (mWakeupCallback != null){
			TXZAsrManager.getInstance().recoverWakeupFromAsr(mWakeupCallback.getTaskId());
		}
        releaseAudioFocus();
	}

	public void showAndPause() {
		pause();
		String tip = CommandPoint.hintArr[currIndex];
		TipDialog.getInstance(MainActivity.getInstance())
				.setText(tip).show();
		mWakeupCallback = new WakeupCallback();
		mWakeupCallback.addCommand("Wake_Type_" + currIndex,
				CommandPoint.commandArr[currIndex]);
		mWakeupCallback.setCommandSelected(false);
		TXZAsrManager.getInstance().useWakeupAsAsr(mWakeupCallback);
		TXZTtsManager.getInstance().speakText("请说"+tip);
		//计时器
		TimerUitl.getInstance().timer(6000,CommandPoint.hintArr[currIndex]);
	}

	public void dismissAndPlay() {
		//在语音界面命中了指令则重新注册指令,不执行操作
		if (MainActivity.getInstance().voiceState){
			mWakeupCallback = new WakeupCallback();
			mWakeupCallback.addCommand("Wake_Type_" + currIndex,
					CommandPoint.commandArr[currIndex]);
			TXZAsrManager.getInstance().useWakeupAsAsr(mWakeupCallback);
			Log.d("jack", "can not wakeup:TXZ_record_show");

			//退出语音界面,防止卡主
            TXZAsrManager.getInstance().cancel();
			return;
		}
		//注销计时器
		TimerUitl.getInstance().destory();

		/*VocieUtil.getInstance().playVoice();
		MainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TipDialog.getInstance(MainActivity.getInstance()).showImageView();
			}
		});

		*//*Random r = new Random();
		String text = CommandPoint.replyArr[r.nextInt(5)];
		TXZTtsManager.getInstance().speakText(text,TXZTtsManager.PreemptType.PREEMPT_TYPE_IMMEADIATELY,null);*//*
		//TXZTtsManager.getInstance().speakText("",TXZTtsManager.PreemptType.PREEMPT_TYPE_IMMEADIATELY,null);

		try {
			Thread.sleep(1920);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		TXZAsrManager.getInstance().recoverWakeupFromAsr(
				mWakeupCallback.getTaskId());
		++currIndex;
		if (currIndex < CommandPoint.pointArr.length)
		{SDKDemoApp.runOnUiGround(this, 0);}
        TipDialog.getInstance(MainActivity.getInstance()).dismiss();
		//start();
        requestAudioFocus();
	}

	//跳过当前指令点
    public void skipCommandPoint() {
	    if (player == null || mWakeupCallback.isCommandSelected()){
            return;
        }
        //注销计时器
        TimerUitl.getInstance().destory();

        TXZAsrManager.getInstance().recoverWakeupFromAsr(
                mWakeupCallback.getTaskId());
        ++currIndex;
        if (currIndex < CommandPoint.pointArr.length)
        {SDKDemoApp.runOnUiGround(this, 0);}
        //start();
        requestAudioFocus();
    }

	//获取当前流媒体播放的位置
	public int getPlayerPoi(){
	    return player.getCurrentPosition();
    }

}
