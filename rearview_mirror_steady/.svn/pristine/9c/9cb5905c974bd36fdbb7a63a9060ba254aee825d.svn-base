package com.txznet.sdk;

import android.content.Intent;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogicBase;

/**
 * 第三方媒体播放音频焦点管理
 * 
 * @author J
 *
 */
public class TXZMediaFocusManager {
	public static final String INTENT_FOCUS_GAINED = "com.txz.media.focus.gained";
	public static final String INTENT_FOCUS_RELEASED = "com.txz.media.focus.released";

	private static TXZMediaFocusManager sInstance = new TXZMediaFocusManager();
	private static final int MAX_GAIN_FOCUS_TIME = 8000; // 获取音频焦点最大时间

	private boolean mOnAudioFocus = false; // 音频焦点是否被占用
	private long mLastGainFocusTime = 0; // 记录上次音频焦点请求时间

	private TXZMediaFocusManager() {
	}

	public static TXZMediaFocusManager getInstance() {
		return sInstance;
	}

	/**
	 * 请求音频焦点，每次请求焦点最多持续8秒, 超时焦点会被释放
	 */
	public void requestFocus() {
		boolean needNotify = !isFocusGained(); // 判断是否需要发送焦点改变通知
		
		mOnAudioFocus = true;
		
		if(needNotify){
			notifyMediaFocusChanged();
		}
		
		AppLogicBase.removeBackGroundCallback(mReleaseFocusTask);
		AppLogicBase.runOnBackGround(mReleaseFocusTask, MAX_GAIN_FOCUS_TIME);
	}

	/**
	 * 释放音频焦点
	 */
	public void releaseFocus() {
		if (!isFocusGained()) {
			return;
		}

		mOnAudioFocus = false;
		notifyMediaFocusChanged();

		AppLogicBase.removeBackGroundCallback(mReleaseFocusTask);
	}

	/**
	 * 获取当前是否有第三方正在占用音频焦点
	 * 
	 * @return 占用情况
	 */
	public boolean isFocusGained() {
		return mOnAudioFocus;
	}

	Runnable mReleaseFocusTask = new Runnable() {

		@Override
		public void run() {
			if(isFocusGained()){
				// 超时释放焦点
				mOnAudioFocus = false;
				notifyMediaFocusChanged();
			}
		}
	};

	private void notifyMediaFocusChanged() {
		Log.d("asd", "media focus changed: "
				+ (isFocusGained() ? "true" : "false"));
		Intent intent;

		if (isFocusGained()) {
			intent = new Intent(INTENT_FOCUS_GAINED);
		} else {
			intent = new Intent(INTENT_FOCUS_RELEASED);
		}

		GlobalContext.get().sendBroadcast(intent);
	}

}
