package com.txznet.test.wakeup.su;

import android.media.AudioRecord;
import android.util.Log;

public class TXZSystemAudioRecord{
	private AudioRecord audioRecord = null;
	QueueBlockingCache mQueueBlockingCache = null;
	byte[] mDataBuffer = null;
	
	public TXZSystemAudioRecord(int audioSource, int sampleRateInHz,
			int channelConfig, int audioFormat, int bufferSizeInBytes)
			throws IllegalArgumentException {
		audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat,
				bufferSizeInBytes);
		
		mQueueBlockingCache = new QueueBlockingCache(32000);
		mDataBuffer = new byte[1200];
	}
	
	
	public int startRecording() throws IllegalStateException {
		Log.d("TXZAudioRecord", "startRecording");
		audioRecord.startRecording();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					loop();
				} catch (Exception e) {
					Log.w("TXZAudioRecord", "exception : " + e.toString());
				}
			}
		}, "TXZAudioRecordThread").start();
		
		return 0;
	}
	
	
	public void stop() throws IllegalStateException {
		audioRecord.stop();
	}
	
	
	public void release() {
		audioRecord.release();
		audioRecord = null;
	}
	
	
	public int read(byte[] audioData, int offsetInBytes, int sizeInBytes) {
		return mQueueBlockingCache.read(audioData, offsetInBytes, sizeInBytes);
	}
	
	
	public int getRecordingState() {
		return audioRecord.getRecordingState();
	}
	
	
	public int getState() {
		return audioRecord.getState();
	}
	
	public void interrupt(){
		Log.d("TXZAudioRecord", "interrupt");
		mQueueBlockingCache.interrupt();
	}
	
	private void loop(){
		int read = 0;
		while(audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
			read = audioRecord.read(mDataBuffer, 0, mDataBuffer.length);
			if (read < 0){
				break;
			}
			
			mQueueBlockingCache.write(mDataBuffer, 0, read);
		}
	}
}
