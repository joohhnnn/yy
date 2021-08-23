package com.txznet.txz.component.tts.yunzhisheng_3_0;

import com.txznet.txz.jni.JNIHelper;

import android.media.AudioRecord;
import android.media.MediaRecorder;

public class TXZAudioRecord implements ITxzAudioRecord{
	private static final int READ_DATA_SIZE = 1200;
	private static final int READ_LEAST_SLEEP = 20;
	private static TXZAudioRecord sInstance = new TXZAudioRecord();
    private AudioRecord mAudioRecord = null;
    private boolean mStartRecording = false;
    private BlockDataLoopCache mCache = null;
    private boolean mNeedCloseCompleted = false;
    private TXZAudioRecord(){
    }
    
    public static TXZAudioRecord getInstance(){
    	return sInstance;
    }
    
    private void startRecordTask(){
    	mCache = new BlockDataLoopCache();
    	Runnable oRun = new Runnable() {
			@Override
			public void run() {
				readData();
			}
		};
		new Thread(oRun, "txz_record_thread").start();
    }
    
	private void readData() {
		byte[] data = new byte[READ_DATA_SIZE];
		while (mAudioRecord != null) {
			try {
				long begin = System.currentTimeMillis();
				int nRead = mAudioRecord.read(data, 0, READ_DATA_SIZE);
				long end = System.currentTimeMillis();
				if (end - begin < READ_LEAST_SLEEP ){
					long sleep = READ_LEAST_SLEEP - (end - begin);
					try{
					Thread.sleep(sleep);
					}catch(Exception e){
						
					}
				}
				if (mStartRecording) {
					mCache.write(data, nRead);
					JNIHelper.logd("write = " +  nRead);
				}
			} catch (Exception e) {

			}
		}
	}
    
	@Override
	public int open(int sampleRateInHz, int channelConfig, int audioFormat) {
		int nRet = 0;
        if (mAudioRecord == null){
        	nRet =  openAudioRecord(sampleRateInHz, channelConfig, audioFormat);
        }
        if (nRet == 0){
        	 mStartRecording = true;
        }
		return 0;
	}
    
	//必须加锁，不然多线程去调用AudioRecord的read方法会造成阻塞
	@Override
	public synchronized int read(byte[] data, int size) {
		int read = 0;
		if (mCache != null){
			read = mCache.read(data, size);
			JNIHelper.logd("read = " + read);
		}
		return read;
	}

	@Override
	public void close() {
         mStartRecording = false;
         if (mNeedCloseCompleted){
        	 closeAudioRecord();
        	 mNeedCloseCompleted = false;
         }
	}
	
	public void setCloseCompleted(){
		mNeedCloseCompleted = true;
	}
	
	private int openAudioRecord(int sampleRateInHz, int channelConfig, int audioFormat) {
		int minBufferSizeInBytes = AudioRecord.getMinBufferSize(
				sampleRateInHz, channelConfig, audioFormat);
		try {
			mAudioRecord = new AudioRecord(
					MediaRecorder.AudioSource.DEFAULT, sampleRateInHz,
					channelConfig, audioFormat,
					minBufferSizeInBytes > 6400 ? minBufferSizeInBytes
							: 6400);
			if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				mAudioRecord.startRecording();
			}
			startRecordTask();
		} catch (Exception e) {
			if (mAudioRecord != null) {
				mAudioRecord.release();
				mAudioRecord = null;
			}
			return -1;
		}
		return 0;
	}
	
	private void closeAudioRecord() {
		if (mAudioRecord != null) {
			if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				mAudioRecord.stop();
			}
			mAudioRecord.release();
			mAudioRecord = null;
		}
	}
}
