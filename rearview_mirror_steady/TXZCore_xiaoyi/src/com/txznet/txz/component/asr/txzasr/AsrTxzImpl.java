package com.txznet.txz.component.asr.txzasr;

import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txznet.txz.component.asr.IAsr;

public class AsrTxzImpl implements IAsr{

	@Override
	public int initialize(IInitCallback oRun) {
		return 0;
	}

	@Override
	public void release() {
		
	}

	@Override
	public int start(AsrOption oOption) {
		return 0;
	}

	@Override
	public void stop() {
	}

	@Override
	public void cancel() {
	}

	@Override
	public boolean isBusy() {
		return false;
	}

	@Override
	public boolean buildGrammar(SdkGrammar oGrammarData,
			IBuildGrammarCallback oCallback) {
		return false;
	}

	@Override
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
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

}
