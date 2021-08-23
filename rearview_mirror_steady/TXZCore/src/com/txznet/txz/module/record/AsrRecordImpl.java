package com.txznet.txz.module.record;

import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.RecorderUtil.RecordCallback;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IAsrCallback;
import com.txznet.txz.component.record.IRecord;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.asr.AsrManager;

public class AsrRecordImpl implements IRecord{
	boolean mWorking = false;
	private RecordCallback mRecordCallback = null;
	private IAsrCallback mAsrCallback = new IAsrCallback(){
		public void onSuccess(AsrOption option,
				VoiceData.VoiceParseData oVoiceParseData) {
			JNIHelper.logd("RecordText");
			if (null != mRecordCallback){
			}
		}

		public void onError(AsrOption option, int error, String desc,
				String speech, int error2) {
			JNIHelper.logd("RecordText");
			if (null != mRecordCallback){
				mRecordCallback.onError(error);
			}
		}

		public void onAbort(AsrOption option, int error) {
			JNIHelper.logd("RecordText");
			if (null != mRecordCallback){
				mRecordCallback.onError(error);
			}
		}

		public void onCancel(AsrOption option) {
			JNIHelper.logd("RecordText");
			if (null != mRecordCallback){
				mRecordCallback.onCancel();
			}
		}

		public void onStart(AsrOption option) {
			JNIHelper.logd("RecordText");
			if (null != mRecordCallback){
				mRecordCallback.onBegin();
			}
		}

		public void onEnd(AsrOption option) {
			JNIHelper.logd("RecordText");
			if (null != mRecordCallback){
				mRecordCallback.onEnd(0);
			}
		}

		public void onVolume(AsrOption option, int volume) {
			JNIHelper.logd("RecordText");
			if (null != mRecordCallback){
				mRecordCallback.onVolume(volume);
			}
		}

	};
	
	@Override
	public void start(RecordCallback callback) {
		start(callback, new RecordOption());
	}
	
	@Override
	public void start(RecordCallback callback, RecordOption option) {
		mRecordCallback = callback;
		Recorder recorder = new Recorder(option.mSavePathPrefix);
		AsrOption oOption = new AsrOption();
		oOption.setEOS(3000);
		oOption.setKeySpeechTimeout(60000);
		oOption.setCallback(mAsrCallback);
		oOption.mRecorder = recorder;
		AsrManager.getInstance().start(oOption);
	}
    
	@Override
	public void stop() {
		
	}
	
	@Override
	public void cancel() {
		
	}

	@Override
	public boolean isBusy() {
		return mWorking;
	}

}
