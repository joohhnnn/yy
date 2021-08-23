package com.txznet.test.wakeup.su;

import java.util.Arrays;

import android.util.Log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.UserVoiceConfig;
import com.txznet.txz.util.VoiceGainHelper;
import com.unisound.client.IAudioSource;

/**
 * 音频源设置外部实现类
 * 
 * @author unisound Copyright (c) 2015, unisound.com All Rights Reserved
 *         该类用来定义相应的audiosource 操作
 */
public class TXZAudioSource implements IAudioSource, IConfInterface {
	private TXZSocketAudioRecord mAudioRecorder = null;
	private boolean bRecording = false;
	private Config mConfig = null;
	private boolean bEnableAEC = false;

	public static class Config {
		public final static int DEFAULT_SKIP_BYTES = 0;
		private boolean bEnable = true;
		private long mBeginSpeechTime = -1;
		
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
		
		public void setBeginSpeechTime(long time) {
			mBeginSpeechTime = time;
		}
		
		public static void fillEmptyData(byte[] data, int offset, int len) {
			Arrays.fill(data, offset, len, (byte) 0);
		}
		
		public int mSilenceLength = 0;
		public int mSilenceCacheCount = 0;
		private IConfInterface mConfInterface = null;
		
		public void setConfInterface(IConfInterface confInterface){
			mConfInterface = confInterface;
		}
		
		public void setSilenceLength(int len) {
			if (mSilenceLength <= 0) {
				mSilenceLength = len;
				mSilenceCacheCount = 10;

				if (mConfInterface != null) {
					mConfInterface.setConfig(
							IConfInterface.CONF_SILENCE_LENGHT, null);
				}

			}
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
		mAudioRecorder = new TXZSocketAudioRecord(bEnableAEC);
		if (config != null) {
			mConfig = config;
		} else {
			mConfig = new Config(true);
		}
		mConfig.setConfInterface(this);
		
	    //提前初始化一次用户增益配置,避免第一次用时耗时
		UserVoiceConfig.init();
		VoiceGainHelper.enable();
	}

	@Override
	public void closeAudioIn() {
		bRecording = false;
		mAudioRecorder.stop();
	}

	@Override
	public void closeAudioOut() {

	}

	@Override
	public int openAudioIn() {
		bRecording = true;
		return mAudioRecorder.startRecording();
	}

	@Override
	public int openAudioOut() {
		return 0;
	}

	@Override
	public int readData(byte[] data, int size) {
		int read = 0;
		if (mConfig != null && mConfig.mSilenceLength > 0){
			int len = mConfig.mSilenceLength;
			if (len >= size){
				read = size;
			}else{
				read = len;
			}
			Arrays.fill(data, 0, size, (byte)0);
			mConfig.mSilenceLength -= read;
			Log.d("vad", "len : " + mConfig.mSilenceLength + ", size : " + size);
			return read;
		}
		
		if (mConfig != null && mConfig.mSilenceCacheCount > 0){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Arrays.fill(data, 0, size, (byte)0);
			mConfig.mSilenceCacheCount--;
			Log.d("vad", "mSilenceCacheCount : " + mConfig.mSilenceCacheCount);
			return size;
		}
		
		if (bRecording) {
			read = mAudioRecorder.read(data, 0, size);
			if (read > 0) {
				// 不停唤醒识别引擎,但是塞静音给引擎,规避不断重启唤醒，不能够马上唤醒的问题
				if (!mConfig.voiceEnable()) {
					Config.fillEmptyData(data, 0, read);
					return 0;
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

	@Override
	public boolean setConfig(String strConfig, byte[] data) {
		if (IConfInterface.CONF_SILENCE_LENGHT.equals(strConfig)){
			mAudioRecorder.interrupt();
		}
		return false;
	}
}
