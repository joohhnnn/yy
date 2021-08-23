package com.txznet.txz.module.record;

import android.os.SystemClock;

import com.txz.ui.event.UiEvent;
import com.txz.ui.wechat.UiWechat;
import com.txz.ui.wechat.UiWechat.WechatVoiceTask;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.RecorderUtil.RecordCallback;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.mix.audiosource.DataDistributer;
import com.txznet.txz.component.record.IRecord;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;

public class WXRecordImpl implements IRecord {
	boolean mWorking = false;
	long lastMuteTime = 0;// 连续出现静音段中的第一次出现静音的时间
	long mBeginTime = 0;
	private RecordCallback mRecordCallback;
	private RecordOption mOption;
	private int mTimeOut = 3000;
	private long mSpeechBeginTime = 0;
	private long mSpeechEndTime = 0;
	
	long mSpeakTagConsume = 0;
	String mLastSpeakTag = null;
	
	@Override
	public void start(RecordCallback callback) {
		start(callback, new RecordOption());
	}
    
	private Recorder mRecorder = null;
	@Override
	public void start(RecordCallback callback, RecordOption option) {
		JNIHelper.logd("record status: start");
		if (mWorking || callback == null || option == null) {
			return;
		}
		mWorking = true;
		lastMuteTime = 0;
		mRecordCallback = callback;
		mOption = option;
		mTimeOut = option.mMaxMute > 2000 ? option.mMaxMute : 2000;
		mLastSpeechTime = mBeginTime = 0;
		mSpeechBeginTime = mSpeechEndTime = 0;
		mLastMuteDurnation = null;
		mSpeakTagConsume = 0;
		mLastSpeakTag = null;
		if (mRecordCallback != null) {
			if(mOption.mNeedOnLineParse){
				WechatVoiceTask task = new WechatVoiceTask();
				task.uint64Timestamp = mOption.mOnLineParseTaskId;
				JNIHelper.sendEvent(UiEvent.EVENT_ACTION_WECHAT, UiWechat.SUBEVENT_START_VOICE, task);
				mRecorder = new OnlineParseRecorder(mOption.mOnLineParseTaskId);
			}else{
				mRecorder = new Recorder(mOption.mSavePathPrefix);
			}
			DataDistributer.getIntance().addRecorder(mRecorder);
			mRecordCallback.onBegin();
		}
		TtsManager.getInstance().pause();
		useAsrWakeup();
	}

	@Override
	public void stop() {
		JNIHelper.logd("record status: stop");
		onEnd();
	}
	
	@Override
	public void cancel() {
		doEnd(true);
	}

	@Override
	public boolean isBusy() {
		return mWorking;
	}
	

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

    
	private void checkMute(int volume) {
		if (!mWorking){
			return;
		}
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
	
	private void onVolumeOfRecord(final int vol){
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				checkMute(vol);
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onEnd() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				doEnd(false);
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
    
	private void onCancel(){
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				doEnd(true);
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void doEnd(boolean bCanceled) {
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
		mOption = null;
		DataDistributer.getIntance().delRecorder(mRecorder);
		mRecorder = null;
		cancelAsrWakeup();
		TtsManager.getInstance().resume();
	}
	
	private final static String TASKID = RecordManager.RECORD_TASK_ID;
	private void useAsrWakeup(){
		AsrComplexSelectCallback cb = new AsrComplexSelectCallback() {
			@Override
			public boolean needAsrState() {
				return true;
			}
			
			@Override
			public String getTaskId() {
				return TASKID;
			}
			
			@Override
			public void onSpeechBegin() {
				mSpeechBeginTime = SystemClock.elapsedRealtime();
			}
			
			@Override
			public void onSpeechEnd() {
				mSpeechEndTime = SystemClock.elapsedRealtime();
			}
			
			@Override
			public void onVolume(int volume) {
				onVolumeOfRecord(volume);
			}
			
			@Override
			public void onCommandSelected(String type, String command) {
				if ("CMD_OK".equals(type)){
					onEnd();
				}else if ("CMD_CANCEL".equals(type)){
					onCancel();
				}
			}
			
		};
		cb.addCommand("CMD_OK", "欧我欧我", "欧稳欧稳", "完毕完毕" );
		cb.addCommand("CMD_CANCEL", "取消取消");
		WakeupManager.getInstance().useWakeupAsAsr(cb);
	}
	
	private void cancelAsrWakeup(){
		WakeupManager.getInstance().recoverWakeupFromAsr(TASKID);
	}
	
}
