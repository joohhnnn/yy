package com.txznet.music.receiver;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.engine.MediaPlayerActivityEngine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 桌面控件按钮控制电台之家
 * 
 * @author telenewbie
 *
 */
public class WidgetListener extends BroadcastReceiver {

	
	public static final String TAG = "[Listener] ";
	public static final String LISTENER = "com.txznet.music.operator";
	public static final String OPERATOR = "operator";
	public static final String PLAY = "PLAY";
	public static final String PAUSE = "PAUSE";
	public static final String NEXT = "NEXT";
	public static final String PREV = "PREV";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (LISTENER.equals(intent.getAction())) {// 过滤自己需要的
			String operator = intent.getStringExtra(OPERATOR);
			LogUtil.logd(TAG+"receiver=" + intent.getAction() + ":" + operator);
			if (PLAY.equals(operator)) {
				MediaPlayerActivityEngine.getInstance().play();
			} else if (PAUSE.equals(operator)) {
				MediaPlayerActivityEngine.getInstance().pause();
			} else if (NEXT.equals(operator)) {
				MediaPlayerActivityEngine.getInstance().next();
			} else if (PREV.equals(operator)) {
				MediaPlayerActivityEngine.getInstance().last();
			}
		}
	}

}
