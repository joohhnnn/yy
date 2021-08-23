package com.txznet.txz.component.tts.yunzhisheng_3_0;

import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;

import android.media.AudioRecord;
import android.media.MediaRecorder;

public class SysAudioRecord implements ITxzAudioRecord{
    private AudioRecord mAudioRecord = null;
    private boolean mRecordingStarted = false;
    
	@Override
	public int open(int sampleRateInHz, int channelConfig, int audioFormat) {
		int minBufferSizeInBytes = AudioRecord.getMinBufferSize(
				sampleRateInHz, channelConfig, audioFormat);
		try {
			mAudioRecord = new AudioRecord(
					ProjectCfg.getAudioSourceForRecord(), sampleRateInHz,
					channelConfig, audioFormat,
					minBufferSizeInBytes > 6400 ? minBufferSizeInBytes
							: 6400);
			if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				mAudioRecord.startRecording();
			}
		} catch (Exception e) {
			return -1;
		}
		mRecordingStarted = true;
		JNIHelper.logd("AudioRecord open : " + mRecordingStarted);
		return 0;
	}
    
	// 必须加锁，不然多线程去调用AudioRecord的read方法会造成阻塞
	@Override
	public int read(byte[] data, int size) {
		int read = 0;
		if (mRecordingStarted) {
			if (mAudioRecord != null) {
				// avoid null pointer exception
				// lock null object will throw exception
				try {
					synchronized (mAudioRecord) {
						read = mAudioRecord.read(data, 0, size);
					}
				} catch (Exception e) {
					JNIHelper.logd("exception : " +  e.toString());
				}
			}
		}
		return read;
	}

	@Override
	public void close() {
		mRecordingStarted = false;
		if (mAudioRecord != null) {
			if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				mAudioRecord.stop();
			}
			mAudioRecord.release();
			mAudioRecord = null;
			JNIHelper.logd("AudioRecord close : " + mRecordingStarted);
		}
	}
}
