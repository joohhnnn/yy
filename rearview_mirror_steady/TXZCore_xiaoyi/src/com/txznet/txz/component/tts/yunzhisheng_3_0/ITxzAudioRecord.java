package com.txznet.txz.component.tts.yunzhisheng_3_0;

public interface ITxzAudioRecord {
	public int open(int sampleRateInHz, int channelConfig, int audioFormat);

	public int read(byte[] data, int size);

	public void close();
}
