package com.txznet.txz.util.recordcenter;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.recordcenter.cache.TraceCacheBuffer;
import com.txznet.txz.voice.IVoiceProcessor;
import com.txznet.txz.voice.aec.HobotAecImpl;
import com.txznet.txz.voice.aec.YZSAecImpl;

public class TXZStereoAECSourceRecorder extends TXZSourceRecorderBase {
	private boolean mRightCompare = true;

	public TXZStereoAECSourceRecorder(boolean rightCompare) {
		this(AudioSource.DEFAULT, 16000, rightCompare);
	}

	public TXZStereoAECSourceRecorder(int audioSource, int sampleRateInHz,
			boolean rightCompare) {
		mRightCompare = rightCompare;

		mAudioRecord = new SystemRecorder(audioSource, sampleRateInHz,
				AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT,
				getBufferSize(sampleRateInHz));

		mCacheBufferRaw = new TraceCacheBuffer(
				2 * RecorderCenter.CACHE_BUFFER_SIZE);
	}

	public TXZStereoAECSourceRecorder(boolean rightCompare,
			IRecorderFactory factory) {
		this(AudioSource.DEFAULT, 16000, rightCompare, factory);
	}

	public TXZStereoAECSourceRecorder(int audioSource, int sampleRateInHz,
			boolean rightCompare, IRecorderFactory factory) {
		mRightCompare = rightCompare;

		mAudioRecord = factory.create(audioSource, sampleRateInHz,
				AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT,
				getBufferSize(sampleRateInHz));

		mCacheBufferRaw = new TraceCacheBuffer(
				2 * RecorderCenter.CACHE_BUFFER_SIZE);
	}

	@Override
	public int getBufferSize(final int sampleRateInHz) {
		int sdkBuffSize = super.getBufferSize(sampleRateInHz);
		int min = AudioRecord.getMinBufferSize(sampleRateInHz,
				AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		if (sdkBuffSize != -1) {
			return  sdkBuffSize > min ? sdkBuffSize : min;
		}
		int need = BUFFER_TIME * 2 * sampleRateInHz; // 10s的缓存，防止出现AEC性能太差而丢数据
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
		IVoiceProcessor aec = null;

		byte[] buf = new byte[BUFFER_SIZE_READ];
		byte[] bufRaw = new byte[BUFFER_SIZE_READ / 2];
		byte[] data = bufRaw;
		byte[] dataAEC = bufRaw;

		while (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
			int r = readTry(buf, 0, buf.length);

			if (r < 0) {
				break;
			}

			if (r == 0) {
				continue;
			}

			if (r % 2 != 0) {
				LogUtil.logw("read data error: mod 2 error " + r);
			}

			if (mRawDataWriter != null) {
				if (data.length < (r + 1) / 2) {
					data = new byte[(r + 1) / 2];
				}
				int n = 0;
				for (int i = mRightCompare ? 0 : 2; n < data.length
						&& i + 1 < r; i += 4) {
					data[n++] = buf[i];
					data[n++] = buf[i + 1];
				}
				try {
					mRawDataWriter.writeData(data, 0, n);
				} catch (Exception e) {
				}
			}

			if (mReferDataWriter != null) {
				if (data.length < (r + 1) / 2) {
					data = new byte[(r + 1) / 2];
				}
				int n = 0;
				for (int i = mRightCompare ? 2 : 0; n < data.length
						&& i + 1 < r; i += 4) {
					data[n++] = buf[i];
					data[n++] = buf[i + 1];
				}
				try {
					mReferDataWriter.writeData(data, 0, n);
				} catch (Exception e) {
				}
			}

			if (mAECDataWriter != null) {
				if (r != buf.length) {
					if (r != dataAEC.length) {
						dataAEC = new byte[r];
					}
					System.arraycopy(buf, 0, dataAEC, 0, r);
				} else {
					dataAEC = buf;
				}
				if (bNeedRebuild){ //如果授权失败就切换成云知声的
					bNeedRebuild = false;
					aec.release();
					aec = null;
				}
				if (aec == null) {
					aec = createAEC();
				}
				byte[] ret = aec.process(dataAEC, null);
				try {
					mAECDataWriter.writeData(ret, 0, ret.length);
				} catch (Exception e) {
				}
			} else {
				if (aec != null) {
					aec.release();
					aec = null;
				}
			}
		}

		if (aec != null) {
			aec.release();
		}

		return 0;
	}


	private boolean useYZS = false;
	private boolean bNeedRebuild = false;
	private IVoiceProcessor createAEC(){
		IVoiceProcessor aec = null;
		if (ImplCfg.useHobotAec() && (!useYZS)) { // use Hobot aec
			LogUtil.d("use Hobot aec");
			try {
				aec = new HobotAecImpl(12000, mRightCompare, new IVoiceProcessor.IEvent() {
					@Override
					public void onEvent(int code) {
						switch (code){
							case IVoiceProcessor.AUTH_FAIL:
								useYZS = true;
								bNeedRebuild = true;
								break;
							case IVoiceProcessor.AUTH_SUCCESS:
								break;
							default:
								break;
						}
					}
				}); //地平线aec
			} catch (Exception e) {
				LogUtil.logd("hobot init fail, use yzs aec");
				e.printStackTrace();
				useYZS = true;
				aec = new YZSAecImpl(mRightCompare);
			}
		}else{
			LogUtil.d("use YZS aec");
			aec = new YZSAecImpl(mRightCompare); //云知声aec
		}
		return aec;
	}
}
