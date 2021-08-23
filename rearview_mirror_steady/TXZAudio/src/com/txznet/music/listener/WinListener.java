package com.txznet.music.listener;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrManager;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 用来监听同行者Core声控界面的状态
 * 
 * @author telenewbie
 * @version 创建时间：2016年5月12日 下午4:21:15
 * 
 */
public class WinListener extends BroadcastReceiver {
	private static final String ACTION_SHOW = "com.txznet.txz.record.show";
	private static final String ACTION_DISMISS = "com.txznet.txz.record.dismiss";

	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.logd("receiver a message:" + intent.getAction());
		if (intent.getAction().equals(ACTION_SHOW)) {
			TXZAsrManager.getInstance().recoverWakeupFromAsr(
					"SPEAK_MUSIC_PLAYER_TEXT");
		} else if (intent.getAction().equals(ACTION_DISMISS)) {
			AppLogic.regAsrCommand();
		}
	}

}
