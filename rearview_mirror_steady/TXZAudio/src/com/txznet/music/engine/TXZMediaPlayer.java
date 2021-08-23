package com.txznet.music.engine;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant.PlayMode;
import com.txznet.music.bean.response.Audio;

public class TXZMediaPlayer extends MediaPlayer {
	private int serverPort;

	// private IMediaPlayer listener;
	private AudioManager audioManager;

	private MyFocusListener focusListener;

	private PlayMode currentMode;

	private List<Audio> songInfos;// 播放器类自身保存一份歌单列表

	private Random random = new Random();

	private int songNum;

	private MyObserver myObserver;

	/**
	 * 当端口被占用的时候，更改端口号
	 * 
	 * @param port
	 *            可用端口号
	 */
	public void setServerPort(int port) {
		serverPort = port;
	}

	// public void setListener(IMediaPlayer listener) {
	// this.listener = listener;
	// }

	public class MyFocusListener implements OnAudioFocusChangeListener {
		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				TXZMediaPlayer.super.setVolume(1.0f, 1.0f);
				if (!TXZMediaPlayer.super.isPlaying())
					TXZMediaPlayer.this.start();
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				if (TXZMediaPlayer.super.isPlaying())
					TXZMediaPlayer.super.pause();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				if (TXZMediaPlayer.super.isPlaying())
					TXZMediaPlayer.super.setVolume(0.1f, 0.1f);
				break;
			}
		}
	}

	public static class MyObserver extends Observable {

		public void setPlay(boolean isPlay) {
			setChanged();
			notifyObservers(isPlay);
		}

	}

	public TXZMediaPlayer() {
		super();
		audioManager = (AudioManager) AppLogic.getApp().getSystemService(Context.AUDIO_SERVICE);
		focusListener = new MyFocusListener();
		myObserver = new MyObserver();
	}

	@Override
	public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {

//		if (serverPort != 0 && path.startsWith("media://")) {
//			String substring = path.substring(path.indexOf("media://") + 8);
//			path = "http://127.0.0.1:" + serverPort + "/play_music.mp3?id=" + substring;
//			LogUtil.logd("请求路径：" + path);
//		} else {
//			if (serverPort == 0) {// 没有获取到端口号过，再次请求
//				// 去获取最新的端口号，避免端口号的占用
//				MediaPlayerActivityEngine.getInstance().refreshCategoryList();
//			}
//			LogUtil.logd("不符合条件：端口：" + serverPort + "路径" + path);
//		}

		super.setDataSource(path);
	}

	@Override
	public void start() throws IllegalStateException {

		audioManager.requestAudioFocus(focusListener, // Use the musicstream.
				AudioManager.STREAM_MUSIC,
				// Request permanent focus.
				AudioManager.AUDIOFOCUS_GAIN);
		super.start();
		myObserver.setPlay(true);
	}

	@Override
	public void stop() throws IllegalStateException {
		super.stop();
		audioManager.abandonAudioFocus(focusListener);
		myObserver.setPlay(false);
	}

	@Override
	public void pause() throws IllegalStateException {
		super.pause();
		audioManager.abandonAudioFocus(focusListener);
		myObserver.setPlay(false);
	}

	public void setMode(PlayMode mode) {
		currentMode = mode;
	}

	public void setWatcher(Observer observer) {
		myObserver.addObserver(observer);
	}

}
