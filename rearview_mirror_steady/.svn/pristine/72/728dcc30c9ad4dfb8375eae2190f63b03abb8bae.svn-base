package com.txznet.txz.component.wakeup;


public interface ISenceWakeup {
	public interface IInitCallback {
		public void onInit(boolean bSuccess);
	}

	public abstract class ISenceWakeupCallback {
		public void onVolume(int vol){
		}
		public void onSpeechBegin(){
		}
		public void onSpeechEnd(){
		}
		public void onWakeUp(String text,int time){
		}
	}
	
	public static class SenceWakeupOption{
		public long mBeginSpeechTime = 0;//唤醒开始的时间
		
		public SenceWakeupOption setBeginTime(long mBeginTime){
			mBeginSpeechTime = mBeginTime;
			return this;
		}
		
	}
	
	public int initialize( final IInitCallback oRun);

	public int start(ISenceWakeupCallback oCallback, SenceWakeupOption oOption, int recordType, String[] cmds);
	
	public void stop();
	
	public void enableVoiceChannel(boolean enable);
}
