package com.txznet.txz.util.recordcenter;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.recordcenter.cache.TraceCacheBuffer;

public class TXZMonoSourceRecorder extends TXZSourceRecorderBase {
	public TXZMonoSourceRecorder() {
		this(AudioSource.DEFAULT, 16000);
	}

	public TXZMonoSourceRecorder(int audioSource, int sampleRateInHz) {

		mAudioRecord = new SystemRecorder(audioSource, sampleRateInHz,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				getBufferSize(sampleRateInHz));

		mCacheBufferRaw = new TraceCacheBuffer(RecorderCenter.CACHE_BUFFER_SIZE);
	}

	public TXZMonoSourceRecorder(IRecorderFactory factory) {
		this(AudioSource.DEFAULT, 16000, factory);
	}

	public TXZMonoSourceRecorder(int audioSource, int sampleRateInHz,
			IRecorderFactory factory) {

		mAudioRecord = factory.create(audioSource, sampleRateInHz,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				getBufferSize(sampleRateInHz));

		mCacheBufferRaw = new TraceCacheBuffer(RecorderCenter.CACHE_BUFFER_SIZE);
	}

	@Override
	public int getBufferSize(final int sampleRateInHz) {
		int sdkBuffSize = super.getBufferSize(sampleRateInHz);
		int min = AudioRecord.getMinBufferSize(sampleRateInHz,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		if (sdkBuffSize != -1) {
			return  sdkBuffSize > min ? sdkBuffSize : min;
		}
		int need = BUFFER_TIME * sampleRateInHz; // 10s的缓存，防止出现AEC性能太差而丢数据
		return need > min ? need : min;
	}

	@Override
	public int startRecorder(Runnable runIdle) {
		beginWatchRead();

		mRunnableIdle = runIdle;
		int nRet = -1;
		nRet = mAudioRecord.startRecording();
		onStartRecordEnd();
		JNIHelper.logd("startRecorder nRet = " + nRet);
		if (nRet != 0){
			JNIHelper.logd("startRecorder fail : " + nRet);
			onError();
			return 0;
		}

		byte[] buf = new byte[BUFFER_SIZE_READ];
		while (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
			int r = readTry(buf, 0, buf.length);

			if (r < 0) {
				break;
			}

			if (r == 0) {
				continue;
			}

			if (mRawDataWriter != null) {
				try {
					mRawDataWriter.writeData(buf, 0, r);
				} catch (Exception e) {
				}
			}

			if (mReferDataWriter != null) {
				try {
					mReferDataWriter.writeData(RecorderCenter.getQuiteVoice(r),
							0, r);
				} catch (Exception e) {
				}
			}

			if (mAECDataWriter != null) {
				try {
					mAECDataWriter.writeData(buf, 0, r);
				} catch (Exception e) {
				}
			}
		}

		return 0;
	}
}
