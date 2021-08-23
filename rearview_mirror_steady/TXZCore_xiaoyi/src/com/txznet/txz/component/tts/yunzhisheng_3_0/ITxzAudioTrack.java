package com.txznet.txz.component.tts.yunzhisheng_3_0;

public interface ITxzAudioTrack {
	public int open(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int mode);

	public int write(byte[] data, int size);

	public void close();
}
