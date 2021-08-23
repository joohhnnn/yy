package com.txznet.txz.module.record;

import java.util.LinkedList;

import android.os.SystemClock;

import com.txz.ui.event.UiEvent;
import com.txz.ui.wechat.UiWechat;
import com.txz.ui.wechat.UiWechat.WechatVoiceTask;
import com.txznet.comm.remote.util.RecorderUtil.RecordCallback;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.record.IRecord;
import com.txznet.txz.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;

public class WakeupRecordImpl implements IRecord {
	boolean mWorking = false;
	long lastMuteTime = 0;// 连续出现静音段中的第一次出现静音的时间
	long mBeginTime = 0;
	private String[] mOverTag = { "欧我欧我", "欧稳欧稳", "完毕完毕" };
	private String[] mCancelTag = {"取消取消"};
	private RecordCallback mRecordCallback;
	private RecordOption mOption;
	private int mMuteVol = RECORD_MUTE_VOL;
	private int mTimeOut = 3000;
	private long mSpeechBeginTime = 0;
	private long mSpeechEndTime = 0;
	
	long mSpeakTagConsume = 0;
	String mLastSpeakTag = null;
	
	@Override
	public void start(RecordCallback callback) {
		start(callback, new RecordOption());
	}

	@Override
	public void start(RecordCallback callback, RecordOption option) {
		JNIHelper.logd("record status: start");
		if (mWorking || callback == null || option == null) {
			return;
		}
		mSampleVols.clear();
		mTotalVol = 0;
		WakeupManager.getInstance().stop();
		MusicManager.getInstance().onBeginAsr();
		MusicManager.getInstance().onEndBeep();
		mWorking = true;
		lastMuteTime = 0;
		mRecordCallback = callback;
		mOption = option;
		mTimeOut = option.mMaxMute > 2000 ? option.mMaxMute : 2000;
		mLastSpeechTime = mBeginTime = 0;// SystemClock.elapsedRealtime();
		mSpeechBeginTime = mSpeechEndTime = 0;
		mLastMuteDurnation = null;
		mSpeakTagConsume = 0;
		mLastSpeakTag = null;
		if (mRecordCallback != null) {
			if(mOption.mNeedOnLineParse){
				WechatVoiceTask task = new WechatVoiceTask();
				task.uint64Timestamp = mOption.mOnLineParseTaskId;
				JNIHelper.sendEvent(UiEvent.EVENT_ACTION_WECHAT, UiWechat.SUBEVENT_START_VOICE, task);
			}
			mRecordCallback.onBegin();
		}
		TtsManager.getInstance().pause();// 放这里保证TTS
											// end时，不会再启动普通唤醒因为此时RecordManager
											// is Busy。
		String[] strOverWords = new String[mOverTag.length + mCancelTag.length];
		System.arraycopy(mOverTag, 0, strOverWords, 0, mOverTag.length);
		System.arraycopy(mCancelTag, 0, strOverWords, mOverTag.length, mCancelTag.length);
		
		WakeupManager.getInstance().startWithRecord(new IWakeupCallback() {
			public void onWakeUp(String text) {
				onWakeUp(text, 0);
			}
			
			public void onWakeUp(String text, int time) {
				mSpeakTagConsume = time;
				mLastSpeakTag = text;
				for (int i = 0; i < mOverTag.length; ++i) {
					if (text.equals(mOverTag[i])) {
						JNIHelper.logd("record status: onOver");
						onEnd();
						return;
					}
				}
				for (int i = 0; i < mCancelTag.length; ++i) {
					if (text.equals(mCancelTag[i])) {
						JNIHelper.logd("record status: onCancel");
						onCancel();
						return;
					}
				}
				
			}

			public void onVolume(int vol) {
//				checkVolume(vol);
				checkMute(vol);
			}

			public void onSpeechBegin() {
				JNIHelper.logd("record status: onSpeechBegin");
				mSpeechBeginTime = SystemClock.elapsedRealtime();
			}

			public void onSpeechEnd() {
				JNIHelper.logd("record status: onSpeechEnd");
				mSpeechEndTime = SystemClock.elapsedRealtime();
			}
		}, mOption, strOverWords);
	}

	@Override
	public void stop() {
		JNIHelper.logd("record status: stop");
		onEnd();
	}
	
	@Override
	public void cancel() {
		stop();
	}

	@Override
	public boolean isBusy() {
		return mWorking;
	}

	final int SAMPLE_COUNT = 27;
	final double MUTE_RATE = 0.22;
	final int MIN_SPEECH_COUNT = 4;
	public static int MAX_VOLUME = 0;
	public static int MIN_VOLUME = 65535;

	static class VolumeRecord {
		int volume;
		long time;

		public VolumeRecord(int v) {
			volume = v;
			time = SystemClock.elapsedRealtime();
		}
	}

	LinkedList<VolumeRecord> mSampleVols = new LinkedList<VolumeRecord>();
	int mTotalVol = 0;
	long mLastSpeechTime = 0;
	Integer mLastMuteDurnation = null;

	private void notifyMuteDurnation(int d) {
		int ds = d / 1000;
		if (mLastMuteDurnation == null || mLastMuteDurnation != ds) {
			mLastMuteDurnation = ds;
			if (mRecordCallback != null) {
				JNIHelper.logd("record_vol mute " + ds);
				mRecordCallback.onMute(ds * 1000);
			}
		}
	}
	
	double mTotalVoiceVolume = 0;
	int mTotalVoiceCount = 0;

	private synchronized void checkVolume(int volume) {
		// 正真启动录音才开始记时
		if (mBeginTime == 0) {
			mLastSpeechTime = mBeginTime = SystemClock.elapsedRealtime();
			mTotalVoiceVolume = 0;
			mTotalVoiceCount = 0;
		}
		mSampleVols.add(new VolumeRecord(volume));
		mTotalVol += volume;
		if (mSampleVols.size() > SAMPLE_COUNT) {
			mTotalVol -= mSampleVols.get(0).volume;
			mSampleVols.remove(0);
			int n = 0;
			double avg = mTotalVol * 1.0 / SAMPLE_COUNT;
			if (volume > MAX_VOLUME) {
				MAX_VOLUME = volume;
			}
			if (volume < MIN_VOLUME) {
				MIN_VOLUME = volume;
			}
			double limit = (MAX_VOLUME - MIN_VOLUME) * MUTE_RATE;
			boolean highVol = false;
			if (mTotalVoiceCount > 3) {
				highVol = (avg > (mTotalVoiceVolume/mTotalVoiceCount) -  (MAX_VOLUME - MIN_VOLUME) * MUTE_RATE/3);
			}
			for (int i = 0; i < mSampleVols.size(); ++i) {
				double inc = mSampleVols.get(i).volume - avg;
				if (inc > limit || inc < -limit) {
					++n;
					if (n >= MIN_SPEECH_COUNT) {
						mLastSpeechTime = mSampleVols.get(i).time;
						notifyMuteDurnation(0);
					}
				}
				if (highVol) {
					mLastSpeechTime = mSampleVols.get(i).time;
					notifyMuteDurnation(0);
				}
			}
			if (n >= MIN_SPEECH_COUNT) {
				mTotalVoiceVolume+=avg;
				mTotalVoiceCount++;
			}
//			JNIHelper
//					.logd("record_vol = " + n + ":" + volume + "/" + (int) avg
//							+ "/" + (int) (limit) + "/" + MIN_VOLUME + "/"
//							+ MAX_VOLUME);
		}

		if (!mWorking || mRecordCallback == null) {
			return;
		}

		mRecordCallback.onVolume(volume);
		// 录音超时
		if (mOption != null
				&& SystemClock.elapsedRealtime() - mBeginTime > mOption.mMaxSpeech) {
			JNIHelper.logd("record status: onSpeechTimeout");
			mRecordCallback.onSpeechTimeout();
			onEnd();
			return;
		}

		// 连续出现静音的时长超过3s
		long duration = SystemClock.elapsedRealtime() - mLastSpeechTime;

		if (duration >= mTimeOut) {
			JNIHelper.logd("record status: onMuteTimeout");
			mRecordCallback.onMuteTimeout();
			onEnd();
			return;
		}
		notifyMuteDurnation((int) duration);
	}
    
	private synchronized void checkMute(int volume) {
		long currTime = SystemClock.elapsedRealtime();
		// 正真启动录音才开始记时
		if (mBeginTime == 0) {
		    mBeginTime = currTime;
		}
		// 正真启动录音才开始记时
		if (mSpeechEndTime == 0){
		    mSpeechEndTime = currTime;	
		}
		
		if (!mWorking || mRecordCallback == null) {
			return;
		}

		mRecordCallback.onVolume(volume);
		// 录音超时
		if (mOption != null
				&& currTime - mBeginTime > mOption.mMaxSpeech) {
			JNIHelper.logd("record status: onSpeechTimeout");
			mRecordCallback.onSpeechTimeout();
			onEnd();
			return;
		}

		long duration = 0;
		if (mSpeechBeginTime < mSpeechEndTime){
		    duration = currTime - mSpeechEndTime;
		}

		if (duration >= mTimeOut) {
			JNIHelper.logd("record status: onMuteTimeout");
			mRecordCallback.onMuteTimeout();
			onEnd();
			return;
		}
		notifyMuteDurnation((int) duration);
	}
	
	private void onEnd() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				doEnd(false);
			}
		};
		AppLogic.runOnUiGround(oRun, 0);
	}
    
	private void onCancel(){
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				doEnd(true);
			}
		};
		AppLogic.runOnUiGround(oRun, 0);
	}
	
	private synchronized void doEnd(boolean bCanceled) {
		JNIHelper.logd("record status: doEnd, working=" + mWorking);

		if (!mWorking) {
			return;
		}
		mWorking = false;

		if (mRecordCallback != null) {
			if (bCanceled){
				if(mOption.mNeedOnLineParse){
					WechatVoiceTask task = new WechatVoiceTask();
					task.uint64Timestamp = mOption.mOnLineParseTaskId;
					task.uint32TailLen = mSpeakTagConsume == 0 ? null : Integer.valueOf("" + mSpeakTagConsume);
					task.strTailText = mLastSpeakTag == null ? null : mLastSpeakTag.getBytes();
					JNIHelper.sendEvent(UiEvent.EVENT_ACTION_WECHAT, UiWechat.SUBEVENT_CANCEL_VOICE, task);
				}
				mRecordCallback.onCancel();
			}else{
				mRecordCallback.onEnd((int) (mSpeechBeginTime == 0 ? 0 : SystemClock.elapsedRealtime() - mBeginTime));
				if (mOption.mNeedOnLineParse) {
					WechatVoiceTask task = new WechatVoiceTask();
					task.uint64Timestamp = mOption.mOnLineParseTaskId;
					task.uint32TailLen = mSpeakTagConsume == 0 ? null : Integer.valueOf("" + mSpeakTagConsume);
					task.strTailText = mLastSpeakTag == null ? null : mLastSpeakTag.getBytes();
					JNIHelper.sendEvent(UiEvent.EVENT_ACTION_WECHAT, UiWechat.SUBEVENT_END_VOICE, task);
				}
			}
			mRecordCallback = null;
		}
		WakeupManager.getInstance().stopWithRecord();
		mOption = null;
		MusicManager.getInstance().onEndAsr();
		WakeupManager.getInstance().start();
		TtsManager.getInstance().resume();
	}
}
