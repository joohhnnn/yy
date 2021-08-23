package com.txznet.txz.component.tts.mix;

import com.txznet.txz.component.tts.ITts;

public class InnerTtsEngine extends TtsEngine{
	
	@Override
	public ITts getEngine() {
		if (mTtsEngine == null) {
			synchronized (this) {
				if (mTtsEngine == null) {
					try {
						mTtsEngine = (ITts)Class.forName(mClassName).newInstance();
					} catch (Exception e) {
						
					}
				}
			}
		}
		return mTtsEngine;
	}

	@Override
	public TtsType getType() {
		return TtsType.INNER;
	}
}
