package com.txznet.txz.component.tts.yunzhisheng_3_0;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.os.IBinder;
import android.os.RemoteException;

import com.mesada.cardvr.service.IAudioCallback;
import com.mesada.cardvr.service.IMediaRecorderService;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
/*
 * 问题点1：read频率低可能导致数据延时严重，比如导致唤醒极慢。
 * 问题点2：read频率高会导致CPU占用高。
 */
public class MSDAudioRecord implements ITxzAudioRecord {
	private IMediaRecorderService mService = null;
	private boolean bStarted = false;
	private PipedInputStream in = null;
	private PipedOutputStream out = null;
    private AudioDataCache mCache = null;
    private int mWaitDataSize = 0;
    private int mChannel = AudioFormat.CHANNEL_IN_MONO;
	private IAudioCallback mAudioCallBack = new IAudioCallback.Stub() {
		@Override
		public void onCallback(byte[] data) throws RemoteException {
			writeInner(data);
//透传数据即可
//			if (mChannel == AudioFormat.CHANNEL_IN_MONO) {
//				byte[] buffer = split(data, data.length, 2);
//				writeInner(buffer);
//			} else {
//				writeInner(data);
//			}
		}

	};
    
	private byte[] split(byte[] data, int len, int channel) {
		byte[] buf = null;
		int bufSize = 0;
		if (channel == 1 && len > 0){
			bufSize = len;
			buf = new byte[bufSize];
			for(int i = 0; i < bufSize; i++){
				buf[i] = data[i];
			}
		}else if (channel == 2 && len > 1){
			bufSize = len/2;
			buf = new byte[bufSize];
			int j = 0;
			int k = 0;
			for(j = 0; j < bufSize; j = j + 2, k = k + 4){
				buf[j] = data[k];
				buf[j + 1] = data[k + 1];
			}
		}else{
			return buf;
		}
		return buf;
	}

	private synchronized void writeInner(byte[] data) {
		if (!bStarted) {
			return;
		}
	    mCache.write(data);
	    if (mCache.size() >= mWaitDataSize){
	    	try{
	    	out.write(1);
	    	}catch(Exception e){
	    		
	    	}
	    }
	}

	private ServiceConnection mSvrConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			JNIHelper.logd("disconnect : " + name.getPackageName());
			mService = null;
			//reConnectService
			JNIHelper.logd("restart to connect to " + name.getPackageName());
			connectService();
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			JNIHelper.logd("connect : " + name.getPackageName());
			mService = IMediaRecorderService.Stub.asInterface(service);
			try {
				mService.openAudioDevices(true);
				mService.setAudioCallback(mAudioCallBack);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public MSDAudioRecord() {
		in = new PipedInputStream();
		out = new PipedOutputStream();
		try {
			in.connect(out);
		} catch (Exception e) {

		}
		mCache = new AudioDataCache();
		connectService();
	}

	private void connectService() {
		JNIHelper.logd("connect...");
		Intent intent = new Intent();
		ComponentName cn = new ComponentName("com.mesada.cardvr",
				"com.mesada.cardvr.service.MediaRecorderService");
		intent.setComponent(cn);
		try {
			AppLogic.getApp().bindService(intent, mSvrConn,
					Context.BIND_AUTO_CREATE);
		} catch (Exception e) {
			JNIHelper.logd("connect exception");
		}
	}

	@Override
	public synchronized int open(int sampleRateInHz, int channelConfig,
			int audioFormat) {
		JNIHelper.logd("open");
		mChannel = channelConfig;
		bStarted = true;
		mCache.clear();
		return 0;
	}

	@Override
	public int read(byte[] data, int size) {
		long begin = System.currentTimeMillis();
		int read = 0;
		do {
			if (mCache.size() >= size){
				break;
			}
			//waiting......
			mWaitDataSize = size;
			try{
				int nRet = in.read();
				if (nRet == 0){
					return read;
				}
			}catch(Exception e){
				
			}
		}while(false);
		long end = System.currentTimeMillis();
		long needSleep = 15 - (end - begin);
		if (needSleep >  0){
			try{
			Thread.sleep(needSleep);
			}catch(Exception e){
				
			}
		}
		read = mCache.read(data, size);
		mWaitDataSize = Integer.MAX_VALUE;
		return read;
	}

	@Override
	public void close() {
		bStarted = false;
		try {
			out.write(0);
		} catch (Exception e) {

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
