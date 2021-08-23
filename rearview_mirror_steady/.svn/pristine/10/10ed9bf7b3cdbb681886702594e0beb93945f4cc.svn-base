package com.txznet.txz.component.asr.txzasr;

import java.util.List;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine.AsrAndWakeupIIintCallback;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine.AsrState;
import com.txznet.txz.component.wakeup.IWakeup.IWakeupCallback;

public interface IEngine {
	public int initialize(AsrAndWakeupIIintCallback oRun);
	public int startAsr(AsrOption oOption);
	public void stopAsr();
	public void cancelAsr();
	public void startWakeup(IWakeupCallback oCallback);
	public void stopWakeup();
	public void setWakeupWords(List<String> keyWordList);
	public int startWithRecord(IWakeupCallback oCallback);
	public void stopWithRecord();
	public boolean isBusy();
	public boolean importKeywords(SdkKeywords oKeywords, IImportKeywordsCallback oCallback);
	public void retryImportOnlineKeywords();
	public void insertVocab_ext(int nGrammar, StringBuffer vocab);
    public void enableAutoRun(boolean enable);
	public AsrState getAsrState();
}
