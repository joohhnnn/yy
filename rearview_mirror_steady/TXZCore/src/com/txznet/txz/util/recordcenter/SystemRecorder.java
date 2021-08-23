package com.txznet.txz.util.recordcenter;

import android.media.AudioRecord;
import com.txznet.txz.jni.JNIHelper;

/*
 * 问题点1：read频率低可能导致数据延时严重，比如导致唤醒极慢。
 * 问题点2：read频率高会导致CPU占用高。
 */
public class SystemRecorder implements ITXZRecorder {
	private AudioRecord mAudioRecord = null;
    private boolean mRecordingStarted = false;
    private int mAudioSource = 0;
    private int mSampleRateInHz = 0;
    private int mChannelConfig = 0;
    private int mAudioFormat = 0;
    private int mBufferSizeInBytes = 0;
    
	public SystemRecorder(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes){
		mAudioSource = audioSource;
		mSampleRateInHz = sampleRateInHz;
		mChannelConfig = channelConfig;
		mAudioFormat = audioFormat;
		mBufferSizeInBytes = bufferSizeInBytes;
		JNIHelper.logd(String.format("mAudioSource = %d, "
				+ "mSampleRateInHz = %d, mChannelConfig = %d,"
				+ " mAudioFormat = %d, mBufferSizeInBytes = %d", mAudioSource,
				mSampleRateInHz, mChannelConfig, mAudioFormat,
				mBufferSizeInBytes));
	}
	
	@Override
	public int startRecording() {
		int nRet = 0;
		if (mAudioRecord != null){
			JNIHelper.logd("release last AudioRecord");//防止上次未释放录音机
			release();
		}
		try{
			mAudioRecord = new AudioRecord(mAudioSource, mSampleRateInHz, mChannelConfig, mAudioFormat, mBufferSizeInBytes);
		}catch(Exception e){
			JNIHelper.logd("exception : " + e.toString());
			mRecordingStarted = false;
			return -1;
		}
		
		try {
			mAudioRecord.startRecording();
		} catch (Exception e) {
			JNIHelper.logd("exception : " + e.toString());
			nRet = -1;
		}
		mRecordingStarted = nRet == 0;
		JNIHelper.logd("SystemAudioRecord startRecording mRecodingStarted : " + mRecordingStarted);
		return nRet;
	}

	@Override
	public void stop() {
		mRecordingStarted = false;
		if (mAudioRecord != null) {
			try {
				if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
					mAudioRecord.stop();
					JNIHelper.logd("SystemAudioRecord stop mRecordingStared : " + mRecordingStarted);
				}
			} catch (Exception e) {
				JNIHelper.logd("SystemAudioRecord  stop exception : " + e.toString());
			}
		}
	}

	@Override
	public int getRecordingState() {
		return mAudioRecord == null ? AudioRecord.RECORDSTATE_STOPPED : mAudioRecord.getRecordingState();
	}

	@Override
	public void release() {
		mRecordingStarted = false;
		if (mAudioRecord != null) {
			//停止录音
			try {
				if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
					mAudioRecord.stop();
					JNIHelper.logd("SystemAudioRecord stop mRecordingStared : " + mRecordingStarted);
				}
			} catch (Exception e) {
				JNIHelper.logd("SystemAudioRecord  stop exception : " + e.toString());
			}
			//释放录音机
			mAudioRecord.release();
			mAudioRecord = null;
			JNIHelper.logd("SystemAudioRecord release  : " + mRecordingStarted);
		}
	}
    
	@Override
	public int read(byte[] data, int offset, int size) {
		int read = 0;
		if (mRecordingStarted) {
			if (mAudioRecord != null) {
				try {
					read = mAudioRecord.read(data, 0, size);
				} catch (Exception e) {
					JNIHelper.logd("SystemAudioRecord read exception : " + e.toString());
				}
			}
		}
		return read;
	}

	@Override
	public int getState() {
		return mAudioRecord == null ? AudioRecord.STATE_UNINITIALIZED : mAudioRecord.getState();
	}
	
	@Override
	public void rebuild() {
		// TODO Auto-generated method stub
		
	}
}
