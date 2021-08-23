package com.txznet.txz.util.recordcenter;

import android.media.AudioRecord;
import android.os.SystemClock;

import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.recordcenter.cache.DataWriter;
import com.txznet.txz.util.recordcenter.cache.TraceCacheBuffer;

public abstract class TXZSourceRecorderBase implements ITXZSourceRecorder {
	public final static int BUFFER_SIZE_READ = 1200;
	public final static int BUFFER_TIME = 10;

	protected ITXZRecorder mAudioRecord = null;
	protected TraceCacheBuffer mCacheBufferRaw = null;
	protected DataWriter mRawDataWriter = null;
	protected DataWriter mAECDataWriter = null;
	protected DataWriter mReferDataWriter = null;
	protected Runnable mRunnableIdle = null;

	protected int errCount = 0;
	protected int emptyCount = 0;

	@Override
	public void setDataWriter(int type, DataWriter writer) {
		switch (type) {
		case READER_TYPE_MIC:
			mRawDataWriter = writer;
			break;
		case READER_TYPE_AEC:
			mAECDataWriter = writer;
			break;
		case READER_TYPE_REFER:
			mReferDataWriter = writer;
			break;
		}
	}

	Runnable mRunnableError = null;

	@Override
	public void setErrorRunnable(Runnable run) {
		mRunnableError = run;
	}

	@Override
	public void stopRecorder() {
		synchronized (mRunnableCheckReadTimeout) {
			AppLogic.removeBackGroundCallback(mRunnableCheckReadTimeout);
		}
		if (mAudioRecord != null) {
			mAudioRecord.stop();
		}
	}

	@Override
	public boolean isRecording() {
		if (mAudioRecord == null) {
			return false;
		}
		return mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
	}

	@Override
	public void releaseRecorder() {
		if (mAudioRecord == null) {
			return;
		}
		mAudioRecord.release();
		//mAudioRecord = null;
	}

	protected void onError() {
		synchronized (mRunnableCheckReadTimeout) {
			AppLogic.removeBackGroundCallback(mRunnableCheckReadTimeout);
		}
		
		Runnable r = mRunnableError;
		mRunnableError = null;
		if (r != null) {
			r.run();
		}
	}

	private long mLastReadTime = 0;
	private Runnable mRunnableCheckReadTimeout = new Runnable() {
		@Override
		public void run() {
			// 读取阻塞超过2s
			if (mLastReadTime + 2000 <= SystemClock.elapsedRealtime()) {
				JNIHelper.loge("recorder read timeout over 2000ms");
				onError();
				return;
			}

			synchronized (mRunnableCheckReadTimeout) {
				AppLogic.removeBackGroundCallback(mRunnableCheckReadTimeout);
				AppLogic.runOnBackGround(mRunnableCheckReadTimeout, 2000);
			}
		}
	};

	protected void beginWatchRead() {
		errCount = 0;
		emptyCount = 0;
		mLastCacheBufferRaw = mCacheBufferRaw;
		mLastReadTime = SystemClock.elapsedRealtime();
		AppLogic.removeBackGroundCallback(mRunnableCheckReadTimeout);
		AppLogic.runOnBackGround(mRunnableCheckReadTimeout, 2000);
	}

	public static TraceCacheBuffer mLastCacheBufferRaw = null;

	public static void saveLastCacheBuffer(DataWriter writer) {
		if (mLastCacheBufferRaw != null) {
			try {
				mLastCacheBufferRaw.readAll(writer);
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * 读取缓冲区中指定长度的数据
	 * @param writer
	 * @param dataLength
	 */
	public static void saveLastCacheBuffer(DataWriter writer, int dataLength){
		if (mLastCacheBufferRaw != null) {
			try {
				mLastCacheBufferRaw.readBySize(writer, dataLength);
			} catch (Exception e) {
			}
		}
	}
	

	public int readTry(byte[] data, int offset, int len) {
		if (mRunnableIdle != null) {
			mRunnableIdle.run();
		}

		mLastReadTime = SystemClock.elapsedRealtime();
		int r = mAudioRecord.read(data, offset, len);
		mLastReadTime = SystemClock.elapsedRealtime();

		if (mRunnableIdle != null) {
			mRunnableIdle.run();
		}

		if (r < 0) {
			JNIHelper.logw("recorder read error ret: " + r);
			errCount++;
			if (errCount > 10) {
				JNIHelper.loge("recorder read error over 10");
				onError();
				return r;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			return 0;
		}

		if (r == 0) {
			emptyCount++;
			if (emptyCount > 50) {
				JNIHelper.loge("recorder read empty over 50");
				onError();
				return -9999;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			return 0;
		}

		emptyCount = errCount = 0;

		if (mRunnableIdle != null) {
			mRunnableIdle.run();
		}

		mCacheBufferRaw.write(data, offset, r);

		return r;
	}
	
	private boolean bLive = true;
	public boolean isLive(){
		return bLive;
	}
	
	@Override
	public void die(){
		clear();
		bLive = false;
	}
	
	@Override
	public void notifyError(int errrorCode){
		onError();
	}
	private Object mPreLocker = new Object();
	private boolean isPreStarting = false;
	private boolean isPreStopWait = false;
	@Override
	final public int preStartRecorder(){
		isPreStarting = true;
		return 0;
	}
	
	@Override
	final public void preStopRecorder(){
		if (!isPreStarting){
			return;
		}
		
		JNIHelper.loge("pre stop begin");
		synchronized (mPreLocker) {
			isPreStopWait = true;
			long begin= SystemClock.elapsedRealtime();
			int count = 0;
			while (isPreStarting) {
				try {
					mPreLocker.wait(100);
				} catch (Exception e) {
				}
				
				if (SystemClock.elapsedRealtime() - begin > 2000){
					JNIHelper.loge("pre stop too long");
					break;
				}
				
				++count;
				if (count > 20){
					JNIHelper.loge("pre stop too many times");
					break;
				}
			}
			isPreStopWait = false;
		}
		JNIHelper.loge("pre stop end");
	}
	
	protected void onStartRecordEnd(){
		JNIHelper.logd("onStartRecordEnd");
		isPreStarting = false;
		if (!isPreStopWait){
			return; 
		}
		JNIHelper.logd("notify pre stop");
		synchronized(mPreLocker){
			isPreStarting = false;
			mPreLocker.notifyAll();
		}
	}
	
	private void clear(){
		if (mAudioRecord != null) {
			try {
				mAudioRecord.release();
			} catch (Exception e) {

			}
		}
		mAudioRecord = null;
		
		AppLogic.removeBackGroundCallback(mRunnableCheckReadTimeout);
		mRunnableCheckReadTimeout = null;

		mCacheBufferRaw = null;
		mRawDataWriter = null;
		mAECDataWriter = null;
		mReferDataWriter = null;
		mRunnableIdle = null;
		mLastCacheBufferRaw = null;
		mRunnableError = null;
		errCount = 0;
		emptyCount = 0;
	}

	/**
	 * 获取缓冲区大小
	 * @param sampleRateInHz
	 * @return
	 */
	public int getBufferSize(int sampleRateInHz) {
		Integer bufferSize = ProjectCfg.getRecorderBufferSize();
		if (bufferSize != null) {
			return bufferSize;
		}
		return -1;
	}
}
