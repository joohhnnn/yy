package com.txznet.music.broadcast;

import com.txznet.music.service.MusicService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MediaReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		// 遇到强拔卡的情况
		if(intent.getAction() == Intent.ACTION_MEDIA_EJECT){
			MusicService.getInstance().resetPlayer();
		}
	}
}
