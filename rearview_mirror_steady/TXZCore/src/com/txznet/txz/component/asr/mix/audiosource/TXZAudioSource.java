package com.txznet.txz.component.asr.mix.audiosource;

import java.util.Arrays;

import android.text.TextUtils;

import com.txz.ui.record.UiRecord;
import com.txz.ui.record.UiRecord.RecordData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.component.asr.mix.Arguments;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.UserVoiceConfig;
import com.txznet.txz.util.VoiceGainHelper;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;
import com.unisound.client.IAudioSource;

/**
 * 音频源设置外部实现类
 * 
 * @author unisound Copyright (c) 2015, unisound.com All Rights Reserved
 *         该类用来定义相应的audiosource 操作
 */
public class TXZAudioSource implements IAudioSource {
	private TXZAudioRecorder mAudioRecorder = null;
	private boolean bRecording = false;
	private Config mConfig = null;
	private int nSkipedBytes = 0;
	private boolean bEnableAEC = false;

	public static class Config {
		public final static int DEFAULT_SKIP_BYTES = 0;
		private int mNeedSkipBytes = DEFAULT_SKIP_BYTES;
		private boolean bEnable = true;
		private long mBeginSpeechTime = -1;
		private String mSaveRecordDataPath = null;
		private Integer mUID;
		private Long mServerTime;
		private Boolean bServerTimeConfidence = false;
		
		public Config() {
			bEnable = true;
		}

		public Config(boolean enable) {
			bEnable = enable;
		}

		public void enable(boolean enable) {
			LogUtil.logd("TXZAudioSource enable : " + enable);
			bEnable = enable;
		}

		public boolean voiceEnable() {
			return bEnable;
		}

		public void setSkipBytes(int bytes) {
			mNeedSkipBytes = bytes;
		}

		public void setBeginSpeechTime(long time) {
			mBeginSpeechTime = time;
		}

		public void setSaveDataPath(String strPath){
			mSaveRecordDataPath = strPath;
		}
		
		public void setmUID(Integer mUID) {
			this.mUID = mUID;
		}

		public void setmServerTime(Long mServerTime) {
			this.mServerTime = mServerTime;
		}
		
		public void setbServerTimeConfidence(Boolean bConfidence){
			this.bServerTimeConfidence = bConfidence;
		}

		public static void fillEmptyData(byte[] data, int offset, int len) {
			Arrays.fill(data, offset, len, (byte) 0);
		}
	}

	public TXZAudioSource() {
		this(new Config(true), false);
	}

	public TXZAudioSource(boolean bAEC) {
		this(new Config(true), bAEC);
	}

	public TXZAudioSource(Config config) {
		this(config, false);
	}

	public TXZAudioSource(Config config, boolean bAEC) {
		bEnableAEC = bAEC;
		mAudioRecorder = new TXZAudioRecorder(bEnableAEC);
		if (config != null) {
			mConfig = config;
		} else {
			mConfig = new Config(true);
		}
	    //提前初始化一次用户增益配置,避免第一次用时耗时
		UserVoiceConfig.init();
		VoiceGainHelper.enable();
	}
	
	public TXZAudioSource(int recordType){
		this(new Config(true), recordType);
	}
	
	public TXZAudioSource(Config config, int recordType){
		mAudioRecorder = new TXZAudioRecorder(recordType);
		if(config != null){
			mConfig = config;
		} else {
			mConfig = new Config(true);
		}
	}

	@Override
	public void closeAudioIn() {
		bRecording = false;
		mAudioRecorder.stop();
		if(!TextUtils.isEmpty(mConfig.mSaveRecordDataPath) && Arguments.sIsSaveVoice){
			RecordData mRecordData = new RecordData();
			mRecordData.uint32RecordType = UiRecord.RECORD_TYPE_ASR;
			mRecordData.uint32SampleRate = RecordFile.SAMPLE_RATE_16K;
			mRecordData.uint32Uid = mConfig.mUID;
			mRecordData.uint64RecordTime = mConfig.mServerTime;
			mRecordData.boolRecordTime = true;
			mAudioRecorder.endSaveCache(mConfig.mSaveRecordDataPath, mRecordData, Arguments.sIsSaveRawPCM);
		}
	}

	@Override
	public void closeAudioOut() {

	}

	@Override
	public int openAudioIn() {
		bRecording = true;
		//最多保存20s长的录音
		if(!TextUtils.isEmpty(mConfig.mSaveRecordDataPath)){
			mAudioRecorder.beginSaveCache(20*16000*2);
		}
		
		if (mConfig.mBeginSpeechTime <= 0) {
			nSkipedBytes = 0;

			return mAudioRecorder.startRecording();
		} else {
			// 免唤醒识别已经屏蔽了滴的一声，不需要做截断
			JNIHelper
					.logd("instantAsr::TXZAudioSource openAudioIn, start beginSpeechTime = "
							+ mConfig.mBeginSpeechTime);
			nSkipedBytes = mConfig.mNeedSkipBytes;
			int ret = mAudioRecorder.startRecording(mConfig.mBeginSpeechTime);
			mConfig.mBeginSpeechTime = 0;

			return ret;
		}

	}

	@Override
	public int openAudioOut() {
		return 0;
	}

	@Override
	public int readData(byte[] data, int size) {
		int read = 0;
		if (bRecording) {
			read = mAudioRecorder.read(data, 0, size);
			if (read > 0) {
				// 不停唤醒识别引擎,但是塞静音给引擎,规避不断重启唤醒，不能够马上唤醒的问题
				if (!mConfig.voiceEnable()) {
					Config.fillEmptyData(data, 0, read);
					return 0;
				} else if (nSkipedBytes < mConfig.mNeedSkipBytes) {// 跳过指令字节的录音。主要是避免嘀的一声被录进去。
					int leftNeedSkipBytes = mConfig.mNeedSkipBytes
							- nSkipedBytes;
					if (leftNeedSkipBytes >= read) {
						Config.fillEmptyData(data, 0, size);// all cases : read
															// <= size
						nSkipedBytes += read;
						return 0;
					} else {
						System.arraycopy(data, leftNeedSkipBytes, data, 0, read
								- leftNeedSkipBytes);
						nSkipedBytes += leftNeedSkipBytes;
						
						//调整塞给引擎的声音的增益
						if (VoiceGainHelper.enable() && read - leftNeedSkipBytes > 0){
							VoiceGainHelper.adjustGain(data, 0, read - leftNeedSkipBytes, VoiceGainHelper.getRate());
						}
						
						return read - leftNeedSkipBytes;
					}
				}
			}
		}
		
		//调整塞给引擎的声音的增益
		if (VoiceGainHelper.enable() && read > 0){
			VoiceGainHelper.adjustGain(data, 0, read, VoiceGainHelper.getRate());
		}
		
		return read;
	}

	@Override
	public int writeData(byte[] arg0, int arg1) {
		return 0;
	}
}
