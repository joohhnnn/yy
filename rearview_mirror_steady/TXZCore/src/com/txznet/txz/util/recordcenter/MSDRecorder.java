package com.txznet.txz.util.recordcenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.media.AudioRecord;
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
public class MSDRecorder implements ITXZRecorder {
	private IMediaRecorderService mService = null;
	private boolean bStarted = false;
    private QueueBlockingCache mCache = null;
    private int mRecordingState = AudioRecord.RECORDSTATE_STOPPED;
	private IAudioCallback mAudioCallBack = new IAudioCallback.Stub() {
		@Override
		public void onCallback(byte[] data) throws RemoteException {
			writeInner(data);
		}

	};

	private synchronized void writeInner(byte[] data) {
		if (!bStarted) {
			return;
		}
		if (mCache != null && data != null) {
			mCache.write(data, 0, data.length);
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
			mService = IMediaRecorderService.Stub.asInterface(service);
			try {
				mService.openAudioDevices(true);
				mService.setAudioCallback(mAudioCallBack);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public MSDRecorder(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes) {
		int cacheSize = 5 * 32 * 1000;//缓存5秒的数据
		if (channelConfig == AudioFormat.CHANNEL_IN_STEREO){
			cacheSize = + cacheSize;//缓存10秒的数据
		}
	   JNIHelper.logd("RemoteRecorder channelConfig : "  + channelConfig + ", cacheSize : " + cacheSize);
		mCache = new QueueBlockingCache(cacheSize);
		connectService();
	}

	private void connectService() {
		Intent intent = new Intent();
		ComponentName cn = new ComponentName("com.mesada.cardvr",
				"com.mesada.cardvr.service.MediaRecorderService");
		intent.setComponent(cn);
		try {
			//必須先start然後bind, 不然客戶端進程掛掉后，會另起一個Service對象。
			AppLogic.getApp().startService(intent);
			AppLogic.getApp().bindService(intent, mSvrConn,
					Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
		} catch (Exception e) {
			JNIHelper.logd("connect exception");
		}
	}

	@Override
	public int startRecording() {
		bStarted = true;
	    mRecordingState = AudioRecord.RECORDSTATE_RECORDING;
		mCache.enable();
		return 0;
	}

	@Override
	public void stop() {
		bStarted = false;
		mRecordingState = AudioRecord.RECORDSTATE_STOPPED;
		mCache.disable();
		mCache.interrupt();
	}

	@Override
	public int getRecordingState() {
		return mRecordingState;
	}

	@Override
	public void release() {
		if (bStarted){
			stop();
		}
	}

	@Override
	public int read(byte[] data, int offset, int len) {
		int nRet = 0;
		nRet = mCache.read(data, offset, len);
		return nRet;
	}

	@Override
	public int getState() {
		return AudioRecord.STATE_INITIALIZED;
	}
	
	@Override
	public void rebuild() {
		// TODO Auto-generated method stub
	}

}
