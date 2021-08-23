package com.txznet.txz.component.asr.mix;

import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.txz.component.asr.IAsr.AsrOption;

public interface IAsrCallBackProxy {
	public void setAsrOption(AsrOption asrOption);
	public AsrOption getAsrOption();
	public void onVolume(final int vol);
	public void onStart();
	public void onEnd();
	public void onBeginOfSpeech();
	public void onEndOfSpeech();
	public void onSuccess(VoiceParseData oVoiceParseData);
	public void onError(int errCode);
	public void onPartialResult(String partialResult);
	
	//复杂场景使用以下接口
	public static class CallBackOption{
		public enum EngineType{
			ENGINE_NET, ENGINE_NET_BAK,ENGINE_LOCAL,ENGINE_MIX,ENGINE_NONE
		}
		public EngineType engineType = null;
		public CallBackOption(EngineType engineType){
			this.engineType = engineType;
		}
	}
	public void onVolume(CallBackOption oOption, final int vol);
	public void onStart(CallBackOption oOption);
	public void onEnd(CallBackOption oOption);
	public void onBeginOfSpeech(CallBackOption oOption);
	public void onEndOfSpeech(CallBackOption oOption);
	public void onSuccess(CallBackOption oOption, VoiceParseData oVoiceParseData);
	public void onError(CallBackOption oOption, int errCode);
	public void onMonitor(String attr);
	public void onPartialResult(CallBackOption oOption,String partialResult);
}
