package com.txznet.txz.plugin.interfaces;

import com.txz.ui.voice.VoiceData.VoiceParseData;

public interface NlpTransitionToTxz {
	public VoiceParseData TransitionToTxz(VoiceParseData parseData);
}
