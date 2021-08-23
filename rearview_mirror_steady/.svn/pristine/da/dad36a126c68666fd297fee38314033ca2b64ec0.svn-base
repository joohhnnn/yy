package com.txznet.txz.component.asr.txzasr;

import com.txznet.txz.jni.JNIHelper;
import com.unisound.client.IAudioSource;
import com.unisound.client.IAudioSourceAEC;

public class SelfRecordHelper {
	private static final int BUFFER_SIZE = 1200;
	private static boolean sRecording = false;
	private static IAudioSource sAudioSource = null;
	private static boolean sNeedWait = false;
	
	public static int start(IAudioSource audioSource){
		if (sRecording){
			return 0;
		}
		JNIHelper.logd("startSelf...");
		sAudioSource = audioSource;
		sRecording = true;
		readBySelf();
		return 0;
	}
	
	public static void stop(){
		if (!sRecording){
			return;
		}
		JNIHelper.logd("stopSelf...");
		sNeedWait = false;
		sRecording = false;
		if (sAudioSource != null){
			sAudioSource.closeAudioIn();
		}
		sAudioSource = null;

	}
	
	private static void readBySelf() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				JNIHelper.logd("startRecordingBySelf...");
				byte[] buffer = new byte[BUFFER_SIZE];
				while (sRecording) {
					if (sAudioSource != null) {
						//使用捕获异常的方式，而不是锁的方式的好处是：stop能立刻被执行。
						try {
							if (sAudioSource instanceof IAudioSourceAEC) {
								((IAudioSourceAEC) sAudioSource).readDataPro(
										buffer, buffer.length);
							} else {
								sAudioSource.readData(buffer, buffer.length);
							}
						} catch (Exception e) {

						}
					}
				}
			}
		};
		new Thread(oRun).start();
	}
	
	public static boolean needWait(){
		return sNeedWait;
	}
	
	public static void setNeedWait(boolean bNeed){
		sNeedWait = bNeed;
	}
}
