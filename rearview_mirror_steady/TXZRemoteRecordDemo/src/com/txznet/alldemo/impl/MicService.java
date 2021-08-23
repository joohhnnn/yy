package com.txznet.alldemo.impl;

import com.txznet.txz.extaudiorecord.IAudioCallback;
import com.txznet.txz.extaudiorecord.ITXZAudioRecord;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class MicService extends Service{
	private static final String TAG = "MICSERVICE";
	private ITXZAudioRecord.Stub mBinder = null;
	IAudioCallback mAudioCallBack = null;
	ITxzAudioRecord mAudioRecord = null;
	@Override
	public void onCreate() {
		super.onCreate();
		mAudioRecord = new SysAudioRecord();
		mBinder = new ITXZAudioRecord.Stub() {		
			@Override
			public void unregisterCallback(IAudioCallback cb) throws RemoteException {
				mAudioCallBack = null;
			}
			
			@Override
			public void registerCallback(IAudioCallback cb) throws RemoteException {
				mAudioCallBack = cb;
			}
			
			@Override
			public void open() throws RemoteException {
				startRecording();
			}
			
			@Override
			public void close() throws RemoteException {
				 bRecording = false;
				
			}
		};
		Log.d(TAG, "onCreate");
	}
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return mBinder;
	}
    
	private boolean bRecording = false;
	public void startRecording(){
		if (bRecording){
			return;
		}
		bRecording = true;
		Runnable oRun = new Runnable(){
			@Override
			public void run() {
				byte[] data = new byte[1200];//最好1200，因為引擎每次read的size就是1200
				int nRet = mAudioRecord.open(SysAudioRecord.FREQUENCY_16K, SysAudioRecord.sChannel_in, SysAudioRecord.sEncoding_in);
				Log.d(TAG, "nRet =  " + nRet);
				while(bRecording){
					int read = mAudioRecord.read(data, data.length);
					//回调数据给TXZCore
					if (mAudioCallBack != null){
						try {
							mAudioCallBack.onCallBack(data);
						} catch (RemoteException e) {
							//android.os.DeadObjectException
							Log.d(TAG, "exception =  " + e.toString());
							mAudioCallBack = null;
						}
					}
				}
			}
		};
		new Thread(oRun, "micThread").start();
	}
}
