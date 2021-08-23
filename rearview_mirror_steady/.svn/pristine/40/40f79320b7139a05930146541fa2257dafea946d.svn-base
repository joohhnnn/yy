package com.txznet.music.utils;

import android.content.Context;
import android.media.AudioManager;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;
import com.txznet.music.engine.MediaPlayerActivityEngine;

/**
 * @author telenewbie
 * @version 创建时间：2016年5月17日 下午10:18:27
 * 
 */
public class AudioManagerHelper {
	static int maxVolume;

	/**
	 * 增加全局音量
	 */
	public static void addWholeSound() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				(int) (Constant.currentSound * maxVolume),
				AudioManager.FLAG_PLAY_SOUND);
		// mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
		// AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND
		// | AudioManager.FLAG_SHOW_UI
		// | AudioManager.FX_FOCUS_NAVIGATION_UP
		// );
	}

	static AudioManager mAudioManager = (AudioManager) GlobalContext.get()
			.getSystemService(Context.AUDIO_SERVICE);

	static {
		maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		LogUtil.logd("max Volume:" + maxVolume);
	}

	/**
	 * 减低全局音量
	 */
	public static void reduceWholeSound() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				(int) (Constant.currentSound * maxVolume),
				AudioManager.FLAG_PLAY_SOUND);
		// mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
		// AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND
		// | AudioManager.FX_FOCUS_NAVIGATION_UP
		// );
	}

	public static void addSound() {
		Constant.currentSound += 0.1;
		if (Constant.currentSound >= 1) {
			Constant.currentSound = 1;
		}
		if (Constant.currentSound == 1) {
			// 最大音量
			maxVolume();
		} else {
			addWholeSound();
		}
		// setMediaVolum(Constant.currentSound);
	}

	public static void reduceSound() {

		Constant.currentSound -= 0.1;
		if (Constant.currentSound < 0) {
			Constant.currentSound = 0;
		}
		if (Constant.currentSound == 0) {
			minVolume();
		} else {
			reduceWholeSound();
		}
		// setMediaVolum(Constant.currentSound);
	}

	public static void maxVolume() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
				AudioManager.FLAG_PLAY_SOUND);
	}

	public static void minVolume() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
				AudioManager.FLAG_PLAY_SOUND);
	}

}
