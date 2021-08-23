package com.txznet.wakeup.wakeup;

import com.txznet.comm.remote.util.RecorderUtil.RecordOption;

public interface IWakeup {
	public interface IInitCallback {
		public void onInit(boolean bSuccess);
	}

	public abstract class IWakeupCallback {
		public void onWakeUp(String text, float score) {
		}
		public void onVolume(int vol){
		}
		public void onSetWordsDone(){
		}
		public void onSpeechBegin(){
		}
		public void onSpeechEnd(){
		}
		public void onWakeUp(String text,int time, float score){
			onWakeUp(text, score);
		}
	}

	public int initialize(String[] cmds, final IInitCallback oRun);

	public int start(IWakeupCallback oCallback);
	
	public int startWithRecord(IWakeupCallback oCallback, RecordOption options, String[] overTag);
	
	public void stop();
	
	public void stopWithRecord();
	
	public void setWakeupKeywords(String[] keywords);
	
	public void setWakeupThreshold(float val);
	
	public void enableVoiceChannel(boolean enable);
}
