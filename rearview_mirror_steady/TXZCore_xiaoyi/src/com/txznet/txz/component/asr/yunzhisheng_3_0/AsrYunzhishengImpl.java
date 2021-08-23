package com.txznet.txz.component.asr.yunzhisheng_3_0;

import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrEngineFactory.AsrEngineAdapter;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine.AsrAndWakeupIIintCallback;
import com.txznet.txz.util.runnables.Runnable2;

public class AsrYunzhishengImpl implements IAsr {
	AsrEngineAdapter mEngine = null;
	
	public AsrYunzhishengImpl() {
		mEngine = AsrEngineFactory.getAdapter();
	}

	@Override
	public int initialize(final IInitCallback oRun) {
		mEngine.initialize(new AsrAndWakeupIIintCallback() {
			@Override
			public void onInit(boolean bSuccessed) {
				oRun.onInit(bSuccessed);
			}
		});
		return 0;
	}

	@Override
	public void release() {

	}

	@Override
	public int start(final AsrOption oOption){
		mEngine.startAsr(oOption);
		return 0;
	}

	@Override
	public void stop() {
		mEngine.stopAsr();
	}

	@Override
	public void cancel() {
		mEngine.cancelAsr();
	}

	@Override
	public boolean isBusy() {
		return AsrWakeupEngine.getEngine().isBusy();
	}

	@Override
	public void releaseBuildGrammarData() {

	}
	
	@Override
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		return AsrWakeupEngine.getEngine().importKeywords(oKeywords, oCallback);
	}

	@Override
	public boolean buildGrammar(SdkGrammar oGrammarData,
			IBuildGrammarCallback oCallback) {
		AppLogic.runOnBackGround(
				new Runnable2<SdkGrammar, IBuildGrammarCallback>(oGrammarData,
						oCallback) {
					@Override
					public void run() {
						if (mP2 != null) {
							mP2.onSuccess(mP1);
						}
					}
				}, 0);
		return true;
	}
	
	@Override
	public void retryImportOnlineKeywords() {
		AsrWakeupEngine.getEngine().retryImportOnlineKeywords();
	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
		AsrWakeupEngine.getEngine().insertVocab_ext(nGrammar, vocab);
	}

}
