package com.txznet.audio.codec;

public class TXZAudioDecoder implements ITXZAudioDecoder {

	public native  long createDecoder(Object obj);

	@Override
	public int destroyDecoder(long sessionId) {
		return destoryDecoder(sessionId);
	}

	public native  int destoryDecoder(long sessionId);

	public native  int startDecoder(long sessionId,
			String path);

	public native  int stopDecoder(long sessionId);

	public native  int readDecoder(long sessionId, int[] params,
			byte[] data, int offset);

	public native  int seekDecoder(long sessionId, long seekTime,long seekPosition);

	@Override
	public void onGetDuration(long duration) {

	}

	static {
		System.loadLibrary("TXZAudioCodec");
	}
}
