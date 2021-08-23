package com.txznet.txz.voice.aec;

import com.txznet.txz.voice.IVoiceProcessor;

public class YZSAecImpl implements IVoiceProcessor {
	private com.unisound.jni.AEC mEngine;
	private IEvent mCallback = null;
	
	public YZSAecImpl(boolean rightCompare) {
		mEngine = new com.unisound.jni.AEC(16000, 1);
		mEngine.setOptionInt(2, 1);
		mEngine.setOptionInt(3, rightCompare ? 0 : 1);
	}

	@Override
	public byte[] process(byte[] audioIn, byte[] bytes1) {
		return mEngine.process(audioIn, bytes1);
	}

	@Override
	public void release() {
		mEngine.release();
		mEngine = null;
	}

	@Override
	public void setCallback(IEvent callback) {
		mCallback = callback;
	}

	@Override
	public int getType() {
		return 0;
	}
}
