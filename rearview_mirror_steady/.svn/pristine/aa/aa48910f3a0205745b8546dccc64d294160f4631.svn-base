package com.txznet.txz.component.tts.yunzhisheng_3_0;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.txzasr.SelfRecordHelper;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine.AsrState;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.wakeup.WakeupPcmHelper;
import com.txznet.txz.util.recordcenter.TXZAudioTrack;
import com.unisound.client.IAudioSourceAEC;

/**
 * 音频源设置外部实现类
 * 
 * @author unisound Copyright (c) 2015, unisound.com All Rights Reserved
 *         该类用来定义相应的audiosource 操作
 */
public class TxzAudioSourceImplAEC extends IAudioSourceAEC {
	public static final int FREQUENCY_16K = 16000;
	
	private static int sSample_rate_in = FREQUENCY_16K;
	private static int  sChannel_in = AudioFormat.CHANNEL_IN_STEREO;
	private static int sEncoding_in = AudioFormat.ENCODING_PCM_16BIT;

	private static int sStream_type_out = AudioManager.STREAM_MUSIC;
	private static int sSample_rate_out = FREQUENCY_16K;
	private static int sChannel_out = AudioFormat.CHANNEL_OUT_MONO;
	private static int sMode_out = TXZAudioTrack.MODE_STREAM;
	private static ITxzAudioRecord sExternalAudioRecord = null;
	private static ITxzAudioRecord sDefaultAudioRecord = new SysAudioRecord();
	private static ITxzAudioTrack sExternalAudioTrack = null;
	private static ITxzAudioTrack sDefaultAudioTrack = new SysAudioTrack();
	
	private ITxzAudioRecord mAudioRecord = null;
	
	private ITxzAudioTrack mAudioTrack = null;
    
	private boolean mBompareWithRightChannel = true;
	public TxzAudioSourceImplAEC(boolean compareWithRightChannel) {
		super();
		if (compareWithRightChannel) {
			this.setMicChannel(0);
		} else {
			this.setMicChannel(1);
		}
		mBompareWithRightChannel = compareWithRightChannel;
	}
	
	public static void setStreamType(int type) {
		sStream_type_out = type;
	}
	
	public static void setAudioRecord(ITxzAudioRecord audioRecord){
		sExternalAudioRecord = audioRecord;
	}
	
	public static void setAudioTrack(ITxzAudioTrack audioTrack){
		sExternalAudioTrack = audioTrack;
	}
	
	/**
	 * 打开音频输入
	 */
	@Override
	public synchronized int openAudioIn() {
		if (AsrWakeupEngine.getEngine().getAsrState() == AsrState.ASR_RECOGNIZE) {
			AsrManager.getInstance().stopWav();
		}
		//
		if (sExternalAudioRecord != null){
			mAudioRecord = sExternalAudioRecord;
		}else{
			mAudioRecord = sDefaultAudioRecord;
		}
		
		if (mAudioRecord != null){
			int nRet = mAudioRecord.open(sSample_rate_in, sChannel_in, sEncoding_in);
			if (nRet == 0){
				setRecordingStart(true);
				setFirstStartRecording(true);
			}
			JNIHelper.logd("startRecording nRet = " + nRet);
			return nRet;
			
		}
		return -1;
	}

	/**
	 * 打开音频输出
	 */
	@Override
	public synchronized int openAudioOut() {
		if (sExternalAudioTrack != null){
			mAudioTrack = sExternalAudioTrack;
		}else{
			mAudioTrack = sDefaultAudioTrack;
		}
		if (mAudioTrack != null){
			return mAudioTrack.open(sStream_type_out, sSample_rate_out, sChannel_out, sEncoding_in, sMode_out);
		}
		return -1;
	}

	/**
	 * 关闭音频输入
	 */
	@Override
	public synchronized void closeAudioIn() {
		if (mAudioRecord != null){
			if (SelfRecordHelper.needWait()){
				//必须置位,否则父类对象会继续readDataPro
				setRecordingStart(false);
				SelfRecordHelper.start(this);
			}else{
				mAudioRecord.close();
				//必须置位,否则父类对象会继续readDataPro
				setRecordingStart(false);
			}
		}
	}

	/**
	 * 声音播放关闭
	 */
	@Override
	public synchronized void closeAudioOut() {
		if (mAudioTrack != null){
			mAudioTrack.close();
		}

	}
    
	/*
	 * 暂时打桩
	 */
	public static void setAudioFilePath(String audioPath, boolean isPlay) {
	}
	private int mErrCount = 0;
	@Override
	public int readDataPro(byte[] buffer, int size) {
		if (mAudioRecord != null){
			int read = 0;
			read =  mAudioRecord.read(buffer, size);
			if (read > 0 && AppLogic.isMainProcess() && AsrWakeupEngine.getEngine().getAsrState() == AsrState.ASR_WAKEUP) {
				//不停唤醒识别引擎,但是塞静音给引擎,规避不断重启唤醒，不能够马上唤醒的问题
				if (!WakeupPcmHelper.channelEnable()){
					WakeupPcmHelper.fillEmptyData(buffer, 0, read);
					return 0;
				}
				WakeupPcmHelper.pushPcm(buffer, 0, read);
			}
			AudioSourceDistributer.getIntance().distribute(buffer, read, mBompareWithRightChannel ? 2 : 3);
			//云知声回音消除的代码中, 没有把读取录音失败的事件抛出来
			//路盛出现第一次read就返回-3、导致引擎后面就不去识别了的问题
			if (read < 0){
				if (mErrCount == 0)
					JNIHelper.logd("AudioRecord_Read_Error = " + read);
				read = 0;
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
				mErrCount++;
				if (mErrCount >= 10) {
					WakeupManager.getInstance().stop();
					WakeupManager.getInstance().startDelay(0);
					mErrCount = 0;
					JNIHelper.logd("AudioRecord_Read_Error = " + read);
				}
			}
			else
				mErrCount = 0;
			return read;
		}
		return 0;
	}

	@Override
	public int writeDataPro(byte[] buffer, int size) {
		if (mAudioTrack != null){
			return mAudioTrack.write(buffer, size);
		}
		return 0;
	}

}
