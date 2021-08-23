package com.txznet.music.receiver;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.engine.MediaPlayerActivityEngine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
//				MediaPlayerActivityEngine.getInstance().pause();
			} else {
				LogUtil.logd("microphone come in");
			}
		}
	}

}
