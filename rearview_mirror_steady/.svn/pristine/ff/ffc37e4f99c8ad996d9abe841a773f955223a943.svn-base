package com.txznet.txz.component.tts.yunzhisheng_3_0;

import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;

import android.media.AudioRecord;

public class SysAudioRecord implements ITxzAudioRecord{
    private AudioRecord mAudioRecord = null;
    private boolean mRecordingStarted = false;
    
	@Override
	public int open(int sampleRateInHz, int channelConfig, int audioFormat) {
		int minBufferSizeInBytes = AudioRecord.getMinBufferSize(
				sampleRateInHz, channelConfig, audioFormat);
		
		//如果上次没有释放，本次就会释放上一次的资源，否则Do Nothing
		//open 和 close方法没有加锁，因为上层调用的地方加锁了
		if (mAudioRecord != null){
			JNIHelper.logd("mAudioRecord is not NULL, it is necessary to release the source not closed last time");
			close();
		}
		
		try {
			mAudioRecord = new AudioRecord(
					ProjectCfg.getAudioSourceForRecord(), sampleRateInHz,
					channelConfig, audioFormat,
					minBufferSizeInBytes > 6400 ? minBufferSizeInBytes
							: 6400);
			if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				mAudioRecord.startRecording();
				mRecordingStarted = true;
				JNIHelper.logd("SysAudioRecord open : " + mRecordingStarted);
				return 0;
			}
		} catch (Exception e) {
			JNIHelper.logd("SysAudioRecord open exception : " + e.toString());
		}
		JNIHelper.logd("SysAudioRecord open fail");
		return -1;
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
			if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
				try {
					mAudioRecord.stop();
					JNIHelper.logd("SysAudioRecord close : stop");
				} catch (Exception e) {
					JNIHelper.logd("SysAudioRecord close : exception : " + e.toString());
				}
			}
			mAudioRecord.release();
			mAudioRecord = null;
			JNIHelper.logd("SysAudioRecord close : release");
		}
	}
}
