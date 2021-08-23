package com.txznet.txz.component.wakeup.yunzhisheng_3_0;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrEngineFactory;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrEngineFactory.AsrEngineAdapter;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine.AsrAndWakeupIIintCallback;
import com.txznet.txz.component.tts.yunzhisheng_3_0.AudioSourceDistributer;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.record.OnlineParseRecorder;
import com.txznet.txz.module.record.Recorder;
import com.txznet.txz.module.wakeup.WakeupPcmHelper;

import android.R.bool;

public class WakeupYunzhishengImpl implements IWakeup {

	IInitCallback mInitCallback;
	boolean bInited = false;
	private String[] defaultWords = { "你好小踢" };
	private String[] wakeupWords;
	AsrEngineAdapter mEngine = null;
	
	public WakeupYunzhishengImpl() {
		mEngine = AsrEngineFactory.getAdapter();
	}

	@Override
	public int initialize(final String[] cmds, IInitCallback oRun) {
		mInitCallback = oRun;
		wakeupWords = cmds;
		
		mEngine.initialize(new AsrAndWakeupIIintCallback() {
			@Override
			public void onInit(boolean bSuccessed) {
				if (bSuccessed) {
				  JNIHelper.logd("WakeupImplInit:" + bSuccessed);
				  doInit();
				}
			}
		});
		return 0;
	}
   
	private void doInit(){
		Runnable run = new Runnable() {
			@Override
			public void run() {
				bInited = true;
				setWakeupKeywords(wakeupWords);
				if (mInitCallback != null) {
					mInitCallback.onInit(true);
					mInitCallback = null;
				}
			}
		};
		AppLogic.runOnBackGround(run, 0);
	}
	
	@Override
	public int start(WakeupOption oOption) {
		mEngine.startWakeup(oOption.wakeupCallback);
		return 0;
	}

	@Override
	public void stop() {
		mEngine.stopWakeup();
	}

	@Override
	public void setWakeupKeywords(String[] keywords) {
		//避免未初始化完，就去set。
		if (!bInited){
			wakeupWords = keywords;
			return;
		}
		
		if (keywords == null) {
			keywords = defaultWords;
		}
 
		List<String> keyWordList = new ArrayList<String>();
		for (int i = 0; i < keywords.length; ++i) {
			keyWordList.add(keywords[i]);
		}

		mEngine.setWakeupWords(keyWordList);
	}
    
	private Recorder mRecorder;
	@Override
	public int startWithRecord(IWakeupCallback oCallback, RecordOption options, String[] overTag) {
		if(options.mNeedOnLineParse){
			mRecorder = new OnlineParseRecorder(options.mOnLineParseTaskId);
		} else {
			mRecorder = new Recorder(options.mSavePathPrefix);
		}
 		AudioSourceDistributer.getIntance().addRecorder(mRecorder);
 		if (overTag != null && overTag.length > 0){
 			setWakeupKeywords(overTag);
 		}
 		mEngine.startWithRecord(oCallback);
		return 0;
	}

	@Override
	public void stopWithRecord() {
		AudioSourceDistributer.getIntance().delRecorder(mRecorder);
		if (mRecorder != null){
			mRecorder.close();
			mRecorder = null;
		}
 		mEngine.stopWithRecord();
	}


	@Override
	public void setWakeupThreshold(float val) {
		JNIHelper.logd("setWakeupThreshold: " + val);
		AsrWakeupEngine.WAKEUP_OPT_THRESHOLD = val;
	}

	@Override
	public void enableVoiceChannel(boolean enable) {
		JNIHelper.logd("enable = " + enable);
		WakeupPcmHelper.enableVoiceChannel(enable);
	}
}
