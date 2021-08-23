package com.txznet.tts.ui;

import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.text.TextUtils;
import com.txznet.loader.AppLogic;
import com.txznet.tts.module.tts.TTSConfig;
import com.txznet.tts.module.tts.TtsManager;
import com.txznet.tts.ui.DialogInput.IResultCallBack;
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
				speakText();
			}
		}.setName("播报TTS"));
		actionList.add(new AutoAction() {
			@Override
			public void aciton() {
				setTts(TTSConfig.ENGINE_IFLYTECK);
			}
		}.setName("使用语记"));
		actionList.add(new AutoAction() {
			@Override
			public void aciton() {
				setTts(TTSConfig.ENGINE_LELE);
			}
		}.setName("使用乐乐"));
	}
	
	private static void speakText(){
		DialogInput.showDialog(new IResultCallBack() {
			@Override
			public void onResult(String strText) {
				String strSpeakText = TextUtils.isEmpty(strText) ? "不喜欢孤独，却又害怕两个人相处" : strText.trim();
				TtsManager.getInstance().speak(AudioManager.STREAM_MUSIC, strSpeakText);
			}
		});
	}
	
	private static void setTts(int  tts){
		SharedPreferences preferences = AppLogic.getApp().getSharedPreferences(TTSConfig.CONFIG, Context.MODE_PRIVATE);
		Editor editor=preferences.edit();
		editor.putInt(TTSConfig.ENGINE_TYPE, tts);
		editor.commit();
		System.exit(0);
	}
}
