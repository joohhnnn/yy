package com.txznet.txz.component.tts.yunzhisheng_3_0;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Properties;

import com.txznet.txz.jni.JNIHelper;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

public class QZAudioRecord implements ITxzAudioRecord {
	private AudioRecord mAudioRecord = null;
	private int mChannel = AudioFormat.CHANNEL_IN_MONO;
	private VoiceDataReceiver mReceiver = null;
    private boolean bSavePcm = false;
    private int mSavedPcmMaxSize = 1024*1024*32;//默认32M
    
	FileOutputStream out = null;
	public QZAudioRecord() {
		mReceiver = new VoiceDataReceiver();
		mReceiver.listen(VoiceDataReceiver.PORT);
		//读取配置参数
		Properties pro = new Properties();
		try{
		    pro.load(new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/" + "qzdebug.properties"));
		    String strSave = pro.getProperty("save", "false");
		    JNIHelper.logd("strSave = " + strSave);
		    if ("true".equals(strSave)){
		    	bSavePcm = true;
		    }else{
		    	bSavePcm = false;
		    }
		    String strMaxSize = pro.getProperty("maxsize", "32");//单位为M
		    JNIHelper.logd("strMaxSize = " + strMaxSize + "(M)");
		    try{
		         int nMaxSize = Integer.parseInt(strMaxSize);
		         mSavedPcmMaxSize = nMaxSize*1024*1024;
		    }catch(Exception e){
		    	
		    }
		    JNIHelper.logd("bSavePcm = " + bSavePcm + ", mSaveMaxSize = " + mSavedPcmMaxSize);
		}catch(Exception e){
			e.printStackTrace();
		}
		if (bSavePcm) {
			try {
				out = new FileOutputStream(Environment
						.getExternalStorageDirectory().getPath()
						+ "/"
						+ String.format("server.pcm"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public int open(int sampleRateInHz, int channelConfig, int audioFormat) {
		channelConfig = mChannel;
		int minBufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		try {
			mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
					sampleRateInHz, channelConfig, audioFormat,
					minBufferSizeInBytes > 6400 ? minBufferSizeInBytes : 6400);
			if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				mAudioRecord.startRecording();
				mReceiver.startReceive();
			}
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	// 16位双声道的话，size应该能被4整除
	//必须加锁，不然多线程去调用AudioRecord的read方法会造成阻塞
	@Override
	public synchronized int read(byte[] data, int size) {
		if (mAudioRecord == null){
			return 0;
		}
		try {
			int newSize = size / 2;
			int nOrigRead = 0;
			byte[] orignal = new byte[newSize];
			byte[] external = new byte[newSize];
			nOrigRead = mAudioRecord.read(orignal, 0, newSize);
			mReceiver.read(external, newSize);
			if (nOrigRead <= 0) {
				return nOrigRead;
			} else {
				for (int i = 0, j = 0; i < nOrigRead && i + 1 < nOrigRead; i = i + 2, j = j + 4) {
					data[j] = orignal[i];
					data[j + 1] = orignal[i + 1];
					data[j + 2] = external[i];
					data[j + 3] = external[i + 1];
				}
				//保存录音
				if (bSavePcm && out != null && mSavedPcmMaxSize > 0) {
					try {
						out.write(data, 0, 2 * nOrigRead);
						mSavedPcmMaxSize -= 2 * nOrigRead;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return 2 * nOrigRead;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void close() {
		if (mAudioRecord != null) {
			if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				mAudioRecord.stop();
			}
			mAudioRecord.release();
			mAudioRecord = null;
			mReceiver.stopReceive();
		}
	}

	private class VoiceDataReceiver {
		private DatagramSocket mSocket;
		public static final int PORT = 10000;
		private static final int BUFFER_SIZE = 4096;
		private byte[] mReceiveBuffer = new byte[BUFFER_SIZE];
		DatagramPacket mPacket = null;
		private boolean mEnable = false;
		private boolean mEnableCache = false;
		AudioDataCache mCache = null;
		public void listen(int port) {
			if (mEnable){
				JNIHelper.logd("I have listened for a long time");
				return;
			}
			try {
				mSocket = new DatagramSocket(PORT);
			} catch (Exception e) {
				mEnable = false;
				return;
			}
			mEnable = true;
			mPacket = new DatagramPacket(mReceiveBuffer, mReceiveBuffer.length);
			mCache = new AudioDataCache();
			start();
		}

		private void start() {
			Runnable oRun = new Runnable() {
				public void run() {
					receive();
				}
			};
			new Thread(oRun).start();

		}

		private void receive() {
			JNIHelper.logd("I am listening for waiting remote udp packet... mEnable = " + mEnable);
			while (mEnable) {
				try {
					//一直接收,避免一下次取数据的时候，取到的上一次缓存的数据
					JNIHelper.logd("receive in...");
					mSocket.receive(mPacket);
					JNIHelper.logd("receive out...");
					//录音中才需要缓存下来
					if (mEnableCache) {
						mCache.write(mPacket.getData(), mPacket.getLength());
					}
				} catch (Exception e) {

				}
			}
			mSocket.close();
		}
        
		public void startReceive(){
			mCache.clear();
			mEnableCache = true;
		}
		
		public void stopReceive(){
			mEnableCache = false;
		}
		
		public int read(byte[] data, int size) {
			int nRet = 0;
			nRet = mCache.read(data, size);
			return nRet;
		}

		public void close() {
			mEnable = false;
		}
	}
	
	/*
	 * 要点：1、循环读写。 2、块读写。 3、已满则覆盖写。4、空则直接返回0。
	 */
	class AudioDataCache{
		private byte[] mBufferCache = new byte[1024*1024];
		private int mHead = 0;
		private int mTail = 0;// 不包括该位
		private int mSize = 0;
		
		public synchronized void clear(){
			mHead = 0;
			mTail = 0;
			mSize = 0;
		}
		
		public synchronized int write(byte[] data){
			int free = mBufferCache.length - mTail;
			if (free >= data.length) {
				System.arraycopy(data, 0, mBufferCache, mTail, data.length);
				mTail += data.length;
				if (mTail == mBufferCache.length) {
					mTail = 0;
				}
			} else {
				System.arraycopy(data, 0, mBufferCache, mTail, free);
				System.arraycopy(data, free, mBufferCache, 0, data.length - free);
				mTail = data.length - free;
				//写满了则覆盖
				if (mTail >= mHead) {
					mHead = mTail;
				}
			}
			if (mTail > mHead){
				mSize = mTail - mHead;
			}else{
				mSize = mBufferCache.length - (mHead - mTail);
			}
			return 0;
		}
		
		public synchronized int write(byte[] data, int length){
			int free = mBufferCache.length - mTail;
			if (free >= length) {
				System.arraycopy(data, 0, mBufferCache, mTail, length);
				mTail += length;
				if (mTail == mBufferCache.length) {
					mTail = 0;
				}
			} else {
				System.arraycopy(data, 0, mBufferCache, mTail, free);
				System.arraycopy(data, free, mBufferCache, 0, length - free);
				mTail = length - free;
				//写满了则覆盖
				if (mTail >= mHead) {
					mHead = mTail;
				}
			}
			if (mTail > mHead){
				mSize = mTail - mHead;
			}else{
				mSize = mBufferCache.length - (mHead - mTail);
			}
			return 0;
		}
		
		public synchronized int read(byte[] data, int size){
			if (mSize <= 0){
				return 0;
			}
			//最多读取mSize个字节
			if (mSize < size) {
				size = mSize;
			}
            int newHead = mHead + size;
			if (newHead <= mBufferCache.length) {
				System.arraycopy(mBufferCache, mHead, data, 0, size);
				if (newHead == mBufferCache.length) {
					mHead = 0;
				}else{
					mHead = newHead;
				}
			} else {
				int free = size - (mBufferCache.length - mHead);
				System.arraycopy(mBufferCache, mHead, data, 0, size - free);
				System.arraycopy(mBufferCache, 0, data, size - free, free);
				mHead = free;
			}
			mSize -= size;
			return size;
		}
		
		public synchronized boolean isEmpty(){
			return mSize == 0;
		}
		
		public synchronized int size(){
			return mSize;
		}
	}
}
