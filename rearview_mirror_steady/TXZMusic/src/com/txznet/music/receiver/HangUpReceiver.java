package com.txznet.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.service.MusicService;

/**
 * @author telenewbie
 * @version 创建时间：2016年3月19日 下午5:59:26
 * 
 */
public class HangUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.hasExtra("state")) {
			if (intent.getIntExtra("state", 0) == 0) {
				LogUtil.logd("microphone go out");
				MusicService.getInstance().pausePlay();
			} else {
				LogUtil.logd("microphone come in");
			}
		}
	}

}
