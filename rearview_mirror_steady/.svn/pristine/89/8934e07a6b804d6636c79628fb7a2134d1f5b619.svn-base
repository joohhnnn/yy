package com.txznet.txz.component.wakeup;

import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IAsrCallback;

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
		public void onError(int errCode){
		}
	}
	
	public static enum WakeupKwType{
		KW_TYPE_DEFAULT,//默认类型
		KW_TYPE_ONESHOT_ONLY,//只包含OneShot关键字的免唤醒识别的唤醒词
		KW_TYPE_DIRECTASR_FRONT,//不是以OneShot关键字开始的免唤醒识别的前置唤醒词
		KW_TYPE_DIRECTASR_REAR, // 不是以OneShot关键字开始的免唤醒识别的后置唤醒词
		KW_TYPE_ONESHOT_DIRECTASR//以OneShot关键字开始的免唤醒识别的唤醒词, 并且长度大于OneShot关键字
	}
	
	public final static int ERROR_CODE_OK = 0;//正常心跳
	public final static int ERROR_CODE_NO_VOL = -1;//规定时间内没有收到音量回调的错误码
	public final static int ERROR_CODE_RECORD_FAIL = -2;//录音出错的错误码
	
	public static class WakeupKw{
		public WakeupKwType mKwType = WakeupKwType.KW_TYPE_DEFAULT;
		public String mOneShotKw = null;
		public String mDirectAsrKw = null;
	}

	public static class WakeupOption{
		public IWakeupCallback wakeupCallback = null; // 唤醒回调
		public long mBeginSpeechTime = 0; // 本次唤醒开始的时间点
		
		public WakeupOption setBeginSpeechTimeout(long mBeginSpeechTime) {
			this.mBeginSpeechTime = mBeginSpeechTime;
			return this;
		}

		public WakeupOption setCallback(IWakeupCallback Callback) {
			wakeupCallback = Callback;
			return this;
		}
	}
	
	public int initialize(String[] cmds, final IInitCallback oRun);

	public int start(WakeupOption oOption);

	public int startWithRecord(IWakeupCallback oCallback, RecordOption options, String[] overTag);
	
	public void stop();
	
	public void stopWithRecord();
	
	public void setWakeupKeywords(String[] keywords);
	
	public void setWakeupThreshold(float val);
	
	public void enableVoiceChannel(boolean enable);
}
