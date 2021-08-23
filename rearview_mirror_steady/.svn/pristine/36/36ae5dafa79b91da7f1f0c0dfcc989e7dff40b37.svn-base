package com.txznet.txz.component.asr.mix;

import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.mix.local.LocalAsrYunzhishengImpl;

public class YZSSuperEngine extends SuperEngineBase{
	private IAsr mAsr = null;
	
	public YZSSuperEngine() {
		this(AsrMsgConstants.ENGINE_TYPE_YZS_LOCAL);
	}
	
	public YZSSuperEngine(int nEngineType){
		super(nEngineType);
		if (getEngineType() == AsrMsgConstants.ENGINE_TYPE_YZS_MIX){
			mAsr = new MixAsrYunzhishengImpl();
			mCapacity = LCOAL_ASR_CAPACITY|NET_ASR_CAPACITY; 
		}else{
			mAsr = new LocalAsrYunzhishengImpl();
			mCapacity = LCOAL_ASR_CAPACITY;
		}
	}
	
	private int mCapacity = NONE_CAPACITY;
	public int capacity(){
		return  mCapacity;
	}
	
	@Override
	public int initialize(IInitCallback oRun) {
		mAsr.initialize(oRun);
		return 0;
	}

	@Override
	public void release() {
		mAsr.release();
	}

	@Override
	public int start(AsrOption oOption) {
		mAsr.start(oOption);
		return 0;
	}

	@Override
	public void stop() {
		mAsr.stop();
	}

	@Override
	public void cancel() {
		mAsr.cancel();
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
		return mAsr.importKeywords(oKeywords, oCallback);
	}

	@Override
	public void releaseBuildGrammarData() {
		
	}

	@Override
	public void retryImportOnlineKeywords() {
		mAsr.retryImportOnlineKeywords();
	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
		mAsr.insertVocab_ext(nGrammar, vocab);
	}
	
	@Override
	public SuperEngineBase getNetEngine(){
		return null;
	}

}
