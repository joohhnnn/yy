package com.txznet.txz.component.tts.mix;

import java.util.Locale;

import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.txz.component.tts.ITts;

public class TtsMix implements ITts {

	public TtsMix() {
	}

	@Override
	public int initialize(IInitCallback oRun) {
		return TtsEngineManager.getInstance().initInnerEngine(oRun);
	}

	@Override
	public void release() {
		TtsEngineManager.getInstance().release();
	}

	@Override
	public int start(int iStream, String sText, ITtsCallback oRun) {
		return TtsEngineManager.getInstance().speakText(iStream, sText, oRun);
	}

	@Override
	public int pause() {
		return TtsEngineManager.getInstance().pause();
	}

	@Override
	public int resume() {
		return TtsEngineManager.getInstance().resume();
	}

	@Override
	public void stop() {
		TtsEngineManager.getInstance().stop();
	}

	@Override
	public boolean isBusy() {
		return TtsEngineManager.getInstance().isBusy();
	}

	@Override
	public void setTtsModel(String ttsModel) {
		TtsEngineManager.getInstance().setTtsModel(ttsModel);
	}

	@Override
	public int setLanguage(Locale loc) {
		return TtsEngineManager.getInstance().setLanguage(loc);
	}

	@Override
	public void setVoiceSpeed(int speed) {
		TtsEngineManager.getInstance().setVoiceSpeed(speed);
		return;
	}

	@Override
	public int getVoiceSpeed() {
		return TtsEngineManager.getInstance().getVoiceSpeed();
	}

	@Override
	public void setOption(TTSOption oOption) {
		TtsEngineManager.getInstance().setOption(oOption);
		return;
	}

}
