package com.txznet.txz.component.asr.mix;

import com.txz.ui.voice.VoiceData.VoiceParseData;

public class SimpleAsrCallBackHandler extends AsrCallBackProxy{
	public SimpleAsrCallBackHandler(){
	}
	
	@Override
	public void onVolume(final int vol){
		mAsrOption.mCallback.onVolume(mAsrOption, vol);
	}
	
	@Override
	public void onStart(){
		mAsrOption.mCallback.onStart(mAsrOption);		
	}
	
	@Override
	public void onEnd(){
		mAsrOption.mCallback.onEnd(mAsrOption);
	}
	
	@Override
	public void onBeginOfSpeech(){
		mAsrOption.mCallback.onSpeechBegin(mAsrOption);
	}
	
	@Override
	public void onEndOfSpeech(){
	    mAsrOption.mCallback.onSpeechEnd(mAsrOption);

	}
	
	@Override
	public void onSuccess(VoiceParseData oVoiceParseData){
		mAsrOption.mCallback.onSuccess(mAsrOption, oVoiceParseData);

	}
	
	@Override
	public void onError(final int errCode){
		mAsrOption.mCallback.onError(mAsrOption, 0, null, null, errCode);
			
	}

	@Override
	public void onVolume(CallBackOption oOption, int vol) {
		onVolume(vol);
	}

	@Override
	public void onStart(CallBackOption oOption) {
		onStart();
	}

	@Override
	public void onEnd(CallBackOption oOption) {
		onEnd();
	}

	@Override
	public void onBeginOfSpeech(CallBackOption oOption) {
		onBeginOfSpeech();
	}

	@Override
	public void onEndOfSpeech(CallBackOption oOption) {
		onEndOfSpeech();
	}

	@Override
	public void onSuccess(CallBackOption oOption, VoiceParseData oVoiceParseData) {
		onSuccess(oVoiceParseData);
	}

	@Override
	public void onError(CallBackOption oOption, int errCode) {
		onError(errCode);
	}

	@Override
	public void onMonitor(String attr) {
		mAsrOption.mCallback.onMonitor(attr);
	}

	@Override
	public void onPartialResult(CallBackOption oOption, String partialResult) {
		onPartialResult(partialResult);
	}

	@Override
	public void onPartialResult(String partialResult) {
		mAsrOption.mCallback.onPartialResult(mAsrOption,partialResult);
	}
}
