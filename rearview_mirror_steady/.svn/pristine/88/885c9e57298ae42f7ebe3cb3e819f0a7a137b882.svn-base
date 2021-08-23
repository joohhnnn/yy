package com.txznet.tts.ui;


import java.util.List;

import android.media.AudioManager;

import com.txznet.tts.module.tts.TtsManager;
public class ActionManager {
	public static final String TAG = "ActionManager";

	public static void fillAction(List<AutoAction> actionList) {
		if (actionList == null) {
			return;
		}
		actionList.clear();
		// add action into action list
		actionList.add(new AutoAction() {
			@Override
			public void aciton() {
				TtsManager.getInstance().speak(AudioManager.STREAM_MUSIC, "不喜欢孤独，却又害怕两个人相处");
			}
		}.setName("执行命令"));
	}
	
}
