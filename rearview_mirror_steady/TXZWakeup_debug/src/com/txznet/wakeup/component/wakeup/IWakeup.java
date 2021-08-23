package com.txznet.wakeup.component.wakeup;

public interface IWakeup {
	public interface IInitCallback {
		public void onInit(boolean bSuccess);
	}

	public abstract class IWakeupCallback {
		public void onWakeUp(String text) {
		}
		public void onVolume(int vol){
		}
		public void onSetWordsDone(){
		}
		public void onSpeechBegin(){
		}
		public void onSpeechEnd(){
		}
	}

	public int initialize(String[] cmds, final IInitCallback oRun);

	public int start(IWakeupCallback oCallback);
	
	public int startWithRecord(IWakeupCallback oCallback, String savePathPrefix, String[] overTag);
	
	public void stop();
	
	public void stopWithRecord();
	
	public void setWakeupKeywords(String[] keywords);
	
	public void setWakeupThreshold(float val);
}
