package com.txznet.txz.component.tts.yunzhisheng_3_0;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.txznet.loader.AppLogic;
import com.txznet.txz.extaudiorecord.IAudioCallback;
import com.txznet.txz.extaudiorecord.ITXZAudioRecord;
import com.txznet.txz.jni.JNIHelper;
/*
 * 问题点1：read频率低可能导致数据延时严重，比如导致唤醒极慢。
 * 问题点2：read频率高会导致CPU占用高。
 */
public class RemoteAudioRecord implements ITxzAudioRecord {
	private ITXZAudioRecord mService = null;
	private boolean bStarted = false;
	private PipedInputStream in = null;
	private PipedOutputStream out = null;
    private AudioDataCache mCache = null;
    private int mWaitDataSize = 0;
	private IAudioCallback mAudioCallBack = new IAudioCallback.Stub() {
		@Override
		public void onCallBack(byte[] data) throws RemoteException {
			writeInner(data);
		}

	};

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
			JNIHelper.logd("restart to connect to " + name.getPackageName());
			connectService();
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			JNIHelper.logd("connect : " + name.getPackageName());
			mService = ITXZAudioRecord.Stub.asInterface(service);
			try {
				mService.open();
				mService.registerCallback(mAudioCallBack);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public RemoteAudioRecord() {
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
		Intent intent = new Intent();
		intent.setAction("com.txznet.txz.intent.action.REMOTERECORD");
		try {
			//必須先start然後bind, 不然客戶端進程掛掉后，會另起一個Service對象。
			AppLogic.getApp().startService(intent);
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
