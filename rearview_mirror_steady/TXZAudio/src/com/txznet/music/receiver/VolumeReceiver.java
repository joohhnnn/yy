package com.txznet.music.receiver;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

/**
 * 系统按键的监听
 * 
 * @author telenewbie
 * @version 创建时间：2016年6月22日 下午9:52:24
 * 
 */
public class VolumeReceiver extends BroadcastReceiver {
	static AudioManager mAudioManager = (AudioManager) GlobalContext.get()
			.getSystemService(Context.AUDIO_SERVICE);

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
			Constant.currentSound = mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC)
					* 1.0f
					/ mAudioManager
							.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			LogUtil.logd("currentSound:" + Constant.currentSound);
		}
	}
}
