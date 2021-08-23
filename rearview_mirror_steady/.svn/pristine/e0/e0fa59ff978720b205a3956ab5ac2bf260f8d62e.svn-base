package com.txznet.debugtool.util;
import java.util.ArrayList;

import com.txznet.sdk.TXZTtsManager;

public class TtsUtils {
	private static TtsUtils instance;
	ArrayList<String> speakTexts = new ArrayList<String>();
	int mTaskId = -1;
	int mIndex = 0;
	boolean isCancel = true;
	private TtsUtils() {
	}
	
	public static TtsUtils getInstance() {
		if (instance == null) {
			synchronized (TtsUtils.class) {
				if (instance == null) {
					instance = new TtsUtils();
				}
			}	
		}
		
		return instance;
	}
	
	public synchronized void setTtsTexts(ArrayList<String> speakTexts) {
		mIndex = 0;
		this.speakTexts.clear();
		this.speakTexts.addAll(speakTexts);
	}
	
	public void startPlayTts() {
		if (isCancel) {
			isCancel = false;
			startPlayTtsInner();
		}
	}
	
	private void startPlayTtsInner(){
		if (!isCancel) {
			if (speakTexts.size() > 0) {
				if (mIndex >= speakTexts.size()) {
					mIndex = 0;
				}
				String text;
				try {
					text = speakTexts.get(mIndex);
				} catch (Exception e) {
					text = "正在加载...";
				}	
				
				mTaskId = TXZTtsManager.getInstance().speakText(text, new TXZTtsManager.ITtsCallback() {
					@Override
					public void onBegin() {
						super.onBegin();
					}
					
					@Override
					public void onCancel() {
						super.onCancel();
						mTaskId = -1;
					}
					
					@Override
					public void onSuccess() {
						super.onSuccess();
						mIndex ++;
						startPlayTtsInner();
					}
				});
			}else {
				TXZTtsManager.getInstance().speakText("加载播报文本失败!");
			}
		}
		
	}
	
	public void stopPlayTts() {
		isCancel = true;
		TXZTtsManager.getInstance().cancelSpeak(mTaskId);
	}
	
}
