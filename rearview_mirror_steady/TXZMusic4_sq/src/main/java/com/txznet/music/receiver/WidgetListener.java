package com.txznet.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;

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
				PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
			} else if (PAUSE.equals(operator)) {
				PlayEngineFactory.getEngine().pause(EnumState.Operation.manual);
			} else if (NEXT.equals(operator)) {
				PlayEngineFactory.getEngine().next(EnumState.Operation.manual);
			} else if (PREV.equals(operator)) {
				PlayEngineFactory.getEngine().last(EnumState.Operation.manual);
			}
		}
	}

}
