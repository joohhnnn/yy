package com.txznet.txz.component.wakeup.sence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource;
import com.txznet.txz.component.wakeup.ISenceWakeup;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.recordcenter.ITXZSourceRecorder;
import com.unisound.client.IAudioSource;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;
 
public class WakeupSenceYunzhishengImpl implements ISenceWakeup{
	private final static String WAKEUPTAG = "wakeup";
	private IAudioSource mAudioSource = null;
	private TXZAudioSource.Config mConfig = null;
	private SpeechUnderstander mSpeechUnderstander = null;
	private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {
		@Override
		public void onResult(int type, String jsonResult) {
			switch (type) {
			case SpeechConstants.WAKEUP_RESULT:
				parseWakeupRawText(jsonResult);
				break;
			}
		}

		@Override
		public void onEvent(int type, int timeMs) {
			switch (type) {
			case SpeechConstants.WAKEUP_EVENT_RECOGNITION_SUCCESS:
				onWakeup();
				break;
			case SpeechConstants.ASR_EVENT_CANCEL:
				break;
			case SpeechConstants.ASR_EVENT_SPEECH_DETECTED:
				onSpeechBegin();
				break;
			case SpeechConstants.ASR_EVENT_SPEECH_END:
				onSpeechEnd();
				break;
			case SpeechConstants.ASR_EVENT_VOLUMECHANGE:
				Integer vol = (Integer) mSpeechUnderstander
						.getOption(SpeechConstants.GENERAL_UPDATE_VOLUME);
				if (null == vol) {
					return;
				}
				onVolume(vol);
				break;
			case SpeechConstants.ASR_EVENT_INIT_DONE:
				onInit(true);
				break;
			case SpeechConstants.WAKEUP_EVENT_SET_WAKEUPWORD_DONE:
				bSetWkWordDone = true;
			}
		}

		@Override
		public void onError(int type, String errorMSG) {
		}
	};
	
	private String mWakeupText = "";
	private int mWakeupTime = 0;
	// {"local_asr":[{"result_type":"full","score":-2.39,"recognition_result":"  你好小踢   ","engine_mode":"wakeup"}]}
	public boolean parseWakeupRawText(String jsonResult) {
		LogUtil.logd("jsonResult : " + jsonResult);
		String rawText = "";
		try {
			JSONObject json = new JSONObject(jsonResult);
			JSONArray jsonArray = json.getJSONArray("local_asr");
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			rawText = jsonObject.getString("recognition_result");
			mWakeupText = rawText.trim();
			mWakeupTime = jsonObject.getInt("utteranceTime");
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
			mWakeupTime = 0;
		}
		return false;
	}
	
	private boolean bInitSuccessed = false;
	private IInitCallback mInitCallback = null;
	
	public int initWakeup(IInitCallback oRun) {
		mInitCallback = oRun;
		mSpeechUnderstander = new SpeechUnderstander(GlobalContext.get(),
				ProjectCfg.getYunzhishengAppId()//
				, ProjectCfg.getYunzhishengSecret());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_SERVICE_MODE,
				SpeechConstants.ASR_SERVICE_MODE_LOCAL);
		mSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		int nRet = 0;
		mSpeechUnderstander.init("");
		if (nRet < 0){
			onInit(false);
		}
		return 0;
	}
    
	private void onWakeup(){
		JNIHelper.logd("WakeupSenceYunzhishengImpl onWakeup mWakeupText:"+mWakeupText+" ,mWakeupTime:"+mWakeupTime);
		Runnable oRun = new Runnable(){
			@Override
			public void run(){
				ISenceWakeupCallback callback = mWakeupCallback;
				if (callback != null){
					callback.onWakeUp(mWakeupText, mWakeupTime);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onVolume(final int vol){
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				ISenceWakeupCallback callback = mWakeupCallback;
				if (callback != null){
					callback.onVolume(vol);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onSpeechBegin(){
		JNIHelper.logd("WakeupSenceYunzhishengImpl onSpeechBegin");
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				ISenceWakeupCallback callback = mWakeupCallback;
				if (callback != null){
					callback.onSpeechBegin();
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onSpeechEnd(){
		JNIHelper.logd("WakeupSenceYunzhishengImpl onSpeechEnd");
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				ISenceWakeupCallback callback = mWakeupCallback;
				if (callback != null){
					callback.onSpeechEnd();
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onInit(final boolean bSuccessed) {
		JNIHelper.logd("WakeupSenceYunzhishengImpl onInit");
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				bInitSuccessed = bSuccessed;
				//follow two line codes, should not be placed in main thread
				if (bSuccessed) {
					mConfig = new TXZAudioSource.Config(true);
//					mAudioSource = new TXZAudioSource(mConfig, ITXZSourceRecorder.READER_TYPE_REFER);
					mAudioSource = new TXZAudioSource(mConfig, ITXZSourceRecorder.READER_TYPE_INNER);
					mSpeechUnderstander.setAudioSource(mAudioSource);
				}
				if (mInitCallback != null) {
					mInitCallback.onInit(bSuccessed);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	@Override
	public int initialize(IInitCallback oRun) {
		initWakeup(oRun);
		return 0;
	}
    
	private ISenceWakeupCallback mWakeupCallback = null;
	private final float  DEFAULT_THRESHOLD = -50f;//保护引擎的阀值，设的很低
	private float mThreshold = DEFAULT_THRESHOLD;
	private boolean bWkStarted = false;
	
	public int startWithSafe(ISenceWakeupCallback oCallback, SenceWakeupOption option, int recordType, String[] cmds){
		
		return start(oCallback, option, recordType, cmds);
	}
	
	@Override
	public int start(ISenceWakeupCallback oCallback, SenceWakeupOption option, int recordType, String[] cmds) {
		JNIHelper.logd("WakeupSenceYunzhishengImpl start");
		if (!bInitSuccessed){
			LogUtil.logw("WakeupSenceYunzhishengImpl bInitSuccessed = " + bInitSuccessed);
			return -1;
		}
		if (bWkStarted){//正在识别中
			LogUtil.logw("WakeupSenceYunzhishengImpl start bWkStarted = " + bWkStarted);
			stop();
			startLogic(oCallback, option, recordType, cmds);
			return 0;
		}
		startLogic(oCallback, option,recordType, cmds);
		return 0;
	}

	private void startLogic(ISenceWakeupCallback oCallback,  SenceWakeupOption option, int recordType,
			String[] cmds) {
		setWakeupKeywords(cmds);
		mWakeupCallback = oCallback;
		bWkStarted = true;
		if(recordType != -1){
			mAudioSource = new TXZAudioSource(recordType);
			mSpeechUnderstander.setAudioSource(mAudioSource);
		}
		
		// 设置唤醒结果不以json的格式返回
		mSpeechUnderstander.setOption(
				SpeechConstants.ASR_OPT_RESULT_JSON, false);
		mSpeechUnderstander.setOption(
				SpeechConstants.WAKEUP_OPT_THRESHOLD_VALUE,
				mThreshold);// 阈值设置需要为float类型
		mSpeechUnderstander.setOption(
				SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, 100);
		int wakeup_model_id = SpeechConstants.AUTO_128_MODEL;
		if (ProjectCfg.mUseHQualityWakeupModel){
			wakeup_model_id = SpeechConstants.AUTO_320_MODEL;
		}
		if(mConfig != null){
			JNIHelper.logd("prowakeup mBeginSpeechTime = "+option.mBeginSpeechTime);
			mConfig.setBeginSpeechTime(option.mBeginSpeechTime);
		}else{
			JNIHelper.logd("mConfig = null");
		}
		LogUtil.logd("wakeup_model_id : " + wakeup_model_id);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_WAKEUP_MODEL_ID, wakeup_model_id);
//		mSpeechUnderstander.setOption(
//				SpeechConstants.ASR_OPT_SAVE_RECORDING_DATA,
//				Environment.getExternalStorageDirectory().getPath()+"/referP.pcm");
		mSpeechUnderstander.start(WAKEUPTAG);
	}


	@Override
	public void stop() {
		JNIHelper.logd("WakeupSenceYunzhishengImpl stop");
		if (!bInitSuccessed){
			LogUtil.logw("bInitSuccessed = " + bInitSuccessed);
			return;
		}
		
		if (!bWkStarted){
//			LogUtil.logd("stop bWkStarted = " + bWkStarted);
			return;
		}
		bWkStarted = false;
		mWakeupCallback = null;
		mSpeechUnderstander.cancel();
	}

    
	private List<String> mLastWkWordList = new ArrayList<String>();
	private boolean bSetWkWordDone = false;

	private void setWakeupKeywords(String[] keywords) {
		if (!bInitSuccessed){
			LogUtil.logw("bInitSuccessed = " + bInitSuccessed);
			return;
		}
		
		if (bWkStarted){
			LogUtil.logw("setWkKeywords bWkStarted = " + bWkStarted);
		}
		
		List<String>WkWordList = Arrays.asList(keywords);
		if (checkWakeupWords(mLastWkWordList, WkWordList)) {
			LogUtil.logw("wakeup keywords not change");
			return;
		}
		bSetWkWordDone = false;
		mSpeechUnderstander.setWakeupWord(WkWordList);
		int nCount = 0;
		while(!bSetWkWordDone && nCount < 1000){
			try {
				Thread.sleep(10);
				nCount++;
			} catch (InterruptedException e) {
			}
		}
		if (bSetWkWordDone){
			mLastWkWordList = WkWordList;
		}
		LogUtil.logd("bSetWkWordDone : " + bSetWkWordDone);
	}
    
	private boolean checkWakeupWords(List<String> lastKws, List<String> kws) {
		if (lastKws == null || kws == null || lastKws.size() != kws.size()) {
			return false;
		}
		Set<String> setKw = new HashSet<String>();
		Set<String> setRemove  = new HashSet<String>();
		setKw.addAll(lastKws);
		for (String s : kws) {
			if (setKw.remove(s) == false && setRemove.contains(s) == false) {
				return false;
			}
			setRemove.add(s);
		}
		return setKw.isEmpty();
	}
	
	// @Override
	// public void setWakeupThreshold(float val) {
	// 	if (!bInitSuccessed){
	// 		LogUtil.logw("bInitSuccessed = " + bInitSuccessed);
	// 		return;
	// 	}
	// 	mThreshold = val;
	// }

	@Override
	public void enableVoiceChannel(boolean enable) {
		if (!bInitSuccessed){
			LogUtil.logw("bInitSuccessed = " + bInitSuccessed);
			return;
		}
		mConfig.enable(enable);
	}

}
