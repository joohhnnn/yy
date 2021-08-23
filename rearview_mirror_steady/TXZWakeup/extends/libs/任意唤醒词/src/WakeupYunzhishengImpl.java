package com.txznet.wakeup.component.wakeup.mix;

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
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.wakeup.audiosource.TXZAudioSource;
import com.txznet.wakeup.wakeup.IWakeup;
import com.unisound.client.IAudioSource;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;
 
public class WakeupYunzhishengImpl implements IWakeup{
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
			case SpeechConstants.ASR_EVENT_ENGINE_INIT_DONE:
				onInit(true);
				break;
			case SpeechConstants.WAKEUP_EVENT_SET_WAKEUPWORD_DONE:
				bSetWkWordDone = true;
			}
		}

		@Override
		public void onError(int type, String errorMSG) {
			LogUtil.logw(" onError type : " + type + ", " + errorMSG);
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
	private enum InitStatus{
		INIT_BEGIN, INIT_END, INIT_IDEL
	}
	
	private InitStatus mInitStatus = InitStatus.INIT_IDEL;
	
	public int initWakeup(IInitCallback oRun) {
		mInitCallback = oRun;
		mSpeechUnderstander = new SpeechUnderstander(GlobalContext.get(),
				ProjectCfg.getYunzhishengAppId()//
				, ProjectCfg.getYunzhishengSecret());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_SERVICE_MODE,
				SpeechConstants.ASR_SERVICE_MODE_LOCAL);
		// 关闭引擎log打印
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		int nRet = mSpeechUnderstander.init("");
		if (nRet < 0){
			onInit(false);
		}
		return 0;
	}
    
	private void onWakeup(){
		Runnable oRun = new Runnable(){
			@Override
			public void run(){
				IWakeupCallback callback = mWakeupCallback;
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
				IWakeupCallback callback = mWakeupCallback;
				if (callback != null){
					callback.onVolume(vol);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onSpeechBegin(){
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				IWakeupCallback callback = mWakeupCallback;
				if (callback != null){
					callback.onSpeechBegin();
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onSpeechEnd(){
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				IWakeupCallback callback = mWakeupCallback;
				if (callback != null){
					callback.onSpeechEnd();
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onInit(final boolean bSuccessed) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				mInitStatus = InitStatus.INIT_END;
				bInitSuccessed = bSuccessed;
				//follow two line codes, should not be placed in main thread
				if (bSuccessed) {
					mConfig = new TXZAudioSource.Config(true);
					mAudioSource = new TXZAudioSource(mConfig, ProjectCfg.mEnableAEC);
					mSpeechUnderstander.setAudioSource(mAudioSource);
				}
				//插入初始化唤醒词
				if (mCacheWkWords != null){
					stop();
					setWakeupKeywords(mCacheWkWords);
					LogUtil.logd("init wk words : " + mCacheWkWords.toString());
					mCacheWkWords = null;
				}
				if (mInitCallback != null) {
					mInitCallback.onInit(bSuccessed);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private String[] mCacheWkWords = null;
	@Override
	public int initialize(String[] cmds, final IInitCallback oRun) {
		LogUtil.logd("init status : " + mInitStatus.name());
		if (mInitStatus == InitStatus.INIT_BEGIN){
			return 0;
		}
		if (mInitStatus == InitStatus.INIT_END){
			AppLogic.runOnBackGround(new Runnable() {
				@Override
				public void run() {
					if (oRun != null){
						oRun.onInit(bInitSuccessed);
					}
				}
			}, 0);
			return 0;
		}
		mInitStatus = InitStatus.INIT_BEGIN;
		LogUtil.logd("init wakeup begin");
		mCacheWkWords = cmds;
		initWakeup(oRun);
		return 0;
	}
    
	private IWakeupCallback mWakeupCallback = null;
	private final float  DEFAULT_THRESHOLD = -3.1f;
	private float mThreshold = DEFAULT_THRESHOLD;
	private boolean bWkStarted = false;
	@Override
	public int start(IWakeupCallback oCallback) {
		if (!bInitSuccessed){
			LogUtil.logw("bInitSuccessed = " + bInitSuccessed);
			return -1;
		}
		
		if (bWkStarted){
			LogUtil.logw("start bWkStarted = " + bWkStarted);
			return 0;
		}
		mWakeupCallback = oCallback;
		bWkStarted = true;
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
		LogUtil.logd("wakeup_model_id : " + wakeup_model_id);
		//mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_WAKEUP_MODEL_ID, wakeup_model_id);
        //mSpeechUnderstander.setOption(SpeechConstants.WAKEUP_WORK_ENGINE, 0);
		mSpeechUnderstander.start(WAKEUPTAG);
		return 0;
	}

	@Override
	public int startWithRecord(IWakeupCallback oCallback, RecordOption options,
			String[] overTag) {
		if (!bInitSuccessed){
			LogUtil.logw("bInitSuccessed = " + bInitSuccessed);
			return -1;
		}
		return 0;
	}

	@Override
	public void stop() {
		if (!bInitSuccessed){
			LogUtil.logw("bInitSuccessed = " + bInitSuccessed);
			return;
		}
		
		if (!bWkStarted){
			LogUtil.logw("stop bWkStarted = " + bWkStarted);
		}
		bWkStarted = false;
		mWakeupCallback = null;
		mSpeechUnderstander.cancel();
	}

	@Override
	public void stopWithRecord() {
		if (!bInitSuccessed){
			LogUtil.logw("bInitSuccessed = " + bInitSuccessed);
			return;
		}
	}
    
	private List<String> mLastWkWordList = new ArrayList<String>();
	private boolean bSetWkWordDone = false;
	@Override
	public void setWakeupKeywords(String[] keywords) {
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
	
	@Override
	public void setWakeupThreshold(float val) {
		if (!bInitSuccessed){
			LogUtil.logw("bInitSuccessed = " + bInitSuccessed);
			return;
		}
		mThreshold = val;
	}

	@Override
	public void enableVoiceChannel(boolean enable) {
		if (!bInitSuccessed){
			LogUtil.logw("bInitSuccessed = " + bInitSuccessed);
			return;
		}
		mConfig.enable(enable);
	}

}
