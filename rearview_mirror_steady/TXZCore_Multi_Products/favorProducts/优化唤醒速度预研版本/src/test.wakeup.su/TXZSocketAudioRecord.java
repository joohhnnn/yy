package com.txznet.test.wakeup.su;

import com.txznet.txz.util.recordcenter.TXZAudioRecorder;

import android.media.AudioRecord;
import android.util.Log;

public class TXZSocketAudioRecord{
	private TXZAudioRecorder audioRecord = null;
	QueueBlockingCache mQueueBlockingCache = null;
	byte[] mDataBuffer = null;
	
	private static enum RecordStatus{
		STATUS_IDEL, STATUS_START, STATUS_PROCCESSING,
	}
	
	private RecordStatus mStatus = RecordStatus.STATUS_IDEL;
	
	public TXZSocketAudioRecord(boolean bAEC)
			throws IllegalArgumentException {
		audioRecord = new TXZAudioRecorder(bAEC);
		
		mQueueBlockingCache = new QueueBlockingCache(32000);
		mDataBuffer = new byte[1200];
	}
	
	public int startRecording() throws IllegalStateException {
		if (mStatus != RecordStatus.STATUS_IDEL){
			return 0;
		}
				
		int nRet = audioRecord.startRecording();
		mStatus = RecordStatus.STATUS_START;
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (RecordStatus.class) {
					mStatus = RecordStatus.STATUS_PROCCESSING;
					RecordStatus.class.notifyAll();
					Log.d("TXZAudioRecord", "loop_proccess");
				}

				try {
					loop();
				} catch (Exception e) {
					Log.w("TXZAudioRecord", "exception : " + e.toString());
				}

				synchronized (RecordStatus.class) {
					Log.d("TXZAudioRecord", "loop_end");
					mStatus = RecordStatus.STATUS_IDEL;
					RecordStatus.class.notifyAll();
				}
			}
		}, "TXZAudioRecordThread").start();
		
		/******等待录音线程完全启动，最多等待2000ms******/
		int cnt = 0;
		synchronized (RecordStatus.class) {
			while (mStatus == RecordStatus.STATUS_START) {
				if (cnt++ > 20) {
					break;
				}
				try {
					RecordStatus.class.wait(100);
				} catch (Exception e) {
				}
			}
		}
		Log.d("TXZAudioRecord", "TXZAudioRecord_startRecording = " + nRet);
		return nRet;
	}
	
	
	public void stop() throws IllegalStateException {
		audioRecord.stop();
		
		/******等待录音线程完全退出，最多等待2000ms******/
		int cnt = 0;
		synchronized (RecordStatus.class) {
			while (mStatus == RecordStatus.STATUS_PROCCESSING) {
				if (cnt++ > 20) {
					break;
				}
				try {
					RecordStatus.class.wait(100);
				} catch (Exception e) {
				}
			}
		}
		
		Log.d("TXZAudioRecord", "TXZAudioRecord_stop");
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
