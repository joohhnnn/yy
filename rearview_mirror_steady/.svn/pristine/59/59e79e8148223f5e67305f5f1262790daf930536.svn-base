package com.txznet.txz.component.asr.mix.local;

import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.mix.VoiceRecogitionController;

public class LocalAsrYunzhishengImpl implements IAsr {

	@Override
	public int initialize(IInitCallback oRun) {
		return VoiceRecogitionController.getInstance().initialize(oRun);
	}

	@Override
	public void release() {
		
	}

	@Override
	public int start(AsrOption oOption) {
		VoiceRecogitionController.getInstance().start(oOption);
		return 0;
	}

	@Override
	public void stop() {
		VoiceRecogitionController.getInstance().stop(new AsrOption());
	}

	@Override
	public void cancel() {
		VoiceRecogitionController.getInstance().cancel(new AsrOption());
	}

	@Override
	public boolean isBusy() {
		return false;
	}


	@Override
	public void releaseBuildGrammarData() {
		
	}

	@Override
	public void retryImportOnlineKeywords() {
		
	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
		
		
	}

	@Override
	public boolean buildGrammar(SdkGrammar oGrammarData,
			IBuildGrammarCallback oCallback) {
		return false;
	}

	@Override
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		return VoiceRecogitionController.getInstance().importKeywords(oKeywords, oCallback);
	}
	
}
