package com.txznet.alldemo.impl;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class SysAudioRecord implements ITxzAudioRecord{
    public static final int FREQUENCY_16K = 16000;
	
	public static int sSample_rate_in = FREQUENCY_16K;
	public static int  sChannel_in = AudioFormat.CHANNEL_IN_MONO; // .CHANNEL_CONFIGURATION_MONO;
	public static int sEncoding_in = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord mAudioRecord = null;

	@Override
	public int open(int sampleRateInHz, int channelConfig, int audioFormat) {
		int minBufferSizeInBytes = AudioRecord.getMinBufferSize(
				sampleRateInHz, channelConfig, audioFormat);
		try {
			mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRateInHz,
					channelConfig, audioFormat,
					minBufferSizeInBytes > 6400 ? minBufferSizeInBytes
							: 6400);
			if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				mAudioRecord.startRecording();
			}
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}
    
	//必须加锁，不然多线程去调用AudioRecord的read方法会造成阻塞
	@Override
	public synchronized int read(byte[] data, int size) {
		int read = 0;
		if (mAudioRecord != null) {
			//放置空指针异常
			try {
				read = mAudioRecord.read(data, 0, size);
			} catch (Exception e) {

			}
		}
		return read;
	}

	@Override
	public void close() {
		if (mAudioRecord != null) {
			if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				mAudioRecord.stop();
			}
			mAudioRecord.release();
			mAudioRecord = null;
		}
	}
}
