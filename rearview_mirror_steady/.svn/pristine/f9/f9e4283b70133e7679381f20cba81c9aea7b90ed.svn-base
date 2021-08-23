package com.txznet.music.receiver;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.receiver.HeadSetHelper.OnHeadSetListener;
import com.txznet.music.service.MusicService;

public class HeadSetReceiver extends BroadcastReceiver {

	Timer timer = null;
	OnHeadSetListener headSetListener = null;
	private static boolean isTimerStart = false;
	private static MyTimer myTimer = null;

	// 重写构造方法，将接口绑定。因为此类的初始化的特殊性。
	public HeadSetReceiver() {
		timer = new Timer(true);
		this.headSetListener = HeadSetHelper.getInstance().getOnHeadSetListener();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		LogUtil.logd("Action::" + intent.getAction());
		String intentAction = intent.getAction();
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			// 获得KeyEvent对象
			KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (null == keyEvent) {
				return;
			}
			int keyCode = keyEvent.getKeyCode();
			LogUtil.logd("keyCode::" + keyCode);
			// if (headSetListener != null) {
			// try {
			if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
				if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == keyCode) {
					if (MusicService.getInstance().isPlaying())
						MusicService.getInstance().pausePlay();
					else
						MusicService.getInstance().resumePlay();
				}
				if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) {
					if (headSetListener != null) {
						if (isTimerStart) {
							myTimer.cancel();
							isTimerStart = false;
							headSetListener.onDoubleClick();
						} else {
							myTimer = new MyTimer();
							timer.schedule(myTimer, 1000);
							isTimerStart = true;
						}
					}
				}
				if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode) {
					MusicService.getInstance().playPrev(true);
				}
				if (KeyEvent.KEYCODE_MEDIA_STOP == keyCode) {
					MusicService.getInstance().pausePlay();
				}
				if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode) {
					MusicService.getInstance().playNext(true);
				}
				if (KeyEvent.KEYCODE_MEDIA_PLAY==keyCode) {
					MusicService.getInstance().startPlayer(true);
				}
				if (KeyEvent.KEYCODE_MEDIA_PAUSE==keyCode) {
					MusicService.getInstance().pausePlay();
				}

			}
			// } catch (Exception e) {
			// // TODO: handle exception
			// }
			// }
		} else if (Intent.ACTION_HEADSET_PLUG.equals(intentAction)) {
			if (intent.getIntExtra("state", 1) == 0) {
				MusicService.getInstance().pausePlay();
			}
		}
		// 终止广播(不让别的程序收到此广播，免受干扰)
		abortBroadcast();
	}

	/*
	 * 定时器，用于延迟1秒，内若无操作则为单击
	 */
	class MyTimer extends TimerTask {

		@Override
		public void run() {
			try {
				myHandle.sendEmptyMessage(0);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	};

	/*
	 * 此handle的目的主要是为了将接口在主线程中触发 ，为了安全起见把接口放到主线程触发
	 */
	Handler myHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			headSetListener.onClick();
			isTimerStart = false;
		}

	};

}
