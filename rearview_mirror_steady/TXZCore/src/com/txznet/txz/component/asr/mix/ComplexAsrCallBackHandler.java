package com.txznet.txz.component.asr.mix;

import com.txz.ui.voice.VoiceData.VoiceParseData;

public abstract class ComplexAsrCallBackHandler extends AsrCallBackProxy{
	@Override
	public final void onVolume(int vol) {
		
	}

	@Override
	public final void onStart() {
		
	}

	@Override
	public final void onEnd() {
	}

	@Override
	public final void onBeginOfSpeech() {
		
	}

	@Override
	public final void onEndOfSpeech() {
		
	}

	@Override
	public final void onSuccess(VoiceParseData oVoiceParseData) {
		
	}

	@Override
	public final void onError(int errCode) {
		
	}

	@Override
	public void onPartialResult(CallBackOption oOption,String partialResult) {

	}

	@Override
	public void onPartialResult(String partialResult) {

	}
}
