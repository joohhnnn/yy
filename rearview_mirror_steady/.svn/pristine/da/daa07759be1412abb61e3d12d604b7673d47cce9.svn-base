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
import com.txznet.txz.module.wakeup.WakeupPcmHelper;
import com.txznet.txz.util.recordcenter.TXZAudioTrack;
import com.unisound.client.IAudioSource;

/**
 * 音频源设置外部实现类
 * 
 * @author unisound Copyright (c) 2015, unisound.com All Rights Reserved
 *         该类用来定义相应的audiosource 操作
 */
public class TxzAudioSourceImpl implements IAudioSource {
	public static final int FREQUENCY_16K = 16000;
	public static final int FREQUENCY_22K = 22050;
	
	private static int sSample_rate_in = FREQUENCY_16K;
	private static int  sChannel_in = AudioFormat.CHANNEL_IN_MONO; // .CHANNEL_CONFIGURATION_MONO;
	private static int sEncoding_in = AudioFormat.ENCODING_PCM_16BIT;

	private static int sStream_type_out = AudioManager.STREAM_MUSIC;
	private static int sSample_rate_out = FREQUENCY_22K;//云知声SDK51版本TTS默认使用22050的速率。
	private static int sChannel_out = AudioFormat.CHANNEL_OUT_MONO;
	private static int sMode_out = TXZAudioTrack.MODE_STREAM;
	private static ITxzAudioRecord sExternalAudioRecord = null;
	private static ITxzAudioRecord sDefaultAudioRecord = new SysAudioRecord();
	private static ITxzAudioTrack sExternalAudioTrack = null;
	private static ITxzAudioTrack sDefaultAudioTrack = new SysAudioTrack();
	
	private ITxzAudioRecord mAudioRecord = null;
	
	private ITxzAudioTrack mAudioTrack = null;

	public TxzAudioSourceImpl() {
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
			int nRet = 0;
			nRet = mAudioTrack.open(sStream_type_out, sSample_rate_out, sChannel_out, sEncoding_in, sMode_out);
			JNIHelper.logd("TxzAudioSoouce open audio out nRet =  " + nRet);
			return nRet;
		}
		return -1;
	}

	/**
	 * 读取数据
	 */
	@Override
	public int readData(byte[] buffer, int size) {
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
			AudioSourceDistributer.getIntance().distribute(buffer, read, 1);
			return read;
		}
		return 0;
	}

	/**
	 * 向声音播放源写入数据
	 */
	@Override
	public int writeData(byte[] buffer, int size) {
		if (mAudioTrack != null){
			return mAudioTrack.write(buffer, size);
		}
		return 0;
	}

	/**
	 * 关闭音频输入
	 */
	@Override
	public synchronized void closeAudioIn() {
		if (mAudioRecord != null){
			if (SelfRecordHelper.needWait()){
				SelfRecordHelper.start(this);
			}else{
				mAudioRecord.close();
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
			JNIHelper.logd("TxzAudioSoouce close audio out");
		}

	}
    
	/*
	 * 暂时打桩
	 */
	public static void setAudioFilePath(String audioPath, boolean isPlay) {
	}
}
