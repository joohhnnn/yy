package com.txznet.txz.component.wakeup.mix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.service.TXZContext;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.mix.Arguments;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.ExchangeHelper;
import com.txznet.txz.util.ThreshHoldAdapter;
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
			case SpeechConstants.ASR_EVENT_INIT_DONE:
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
	private float mWakeupScore = 0f;
	// {"local_asr":[{"result_type":"full","score":-2.39,"recognition_result":"  你好小踢   ","engine_mode":"wakeup"}]}
	public boolean parseWakeupRawText(String jsonResult) {
		LogUtil.logd("jsonResult : " + jsonResult);
		String rawText = "";
		float score = 0.0f;
		try {
			JSONObject json = new JSONObject(jsonResult);
			JSONArray jsonArray = json.getJSONArray("local_asr");
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			rawText = jsonObject.getString("recognition_result");
			score = (float) jsonObject.getDouble("score");
			mWakeupText = rawText.replace(" ", "");
			mWakeupTime = jsonObject.getInt("utteranceTime");
			//转换与V2引擎一致的分数标准
			mWakeupScore = ThreshHoldAdapter.getThreshValueFromV3(score);
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
	private final static String PRIV_FILESDIR_NAME = "svr1";
	public int initWakeup(IInitCallback oRun) {
		mInitCallback = oRun;
		if (Looper.myLooper() == null) {
			Looper.prepare();
		}
		mSpeechUnderstander = new SpeechUnderstander(new TXZContext(GlobalContext.get(), PRIV_FILESDIR_NAME),
				ProjectCfg.getYunzhishengAppId()//
				, ProjectCfg.getYunzhishengSecret());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_SERVICE_MODE,
				SpeechConstants.ASR_SERVICE_MODE_LOCAL);
		// 关闭引擎log打印
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_DEBUG_SAVELOG, false);
		mSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		LogUtil.logd("init wk begin wk");
		int nRet = 0;
		mSpeechUnderstander.init("");
		LogUtil.logd("init wk end nRet : " + nRet);
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
					callback.onWakeUp(mWakeupText, mWakeupTime, mWakeupScore);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onVolume(final int vol){
		mLastVolTime = SystemClock.elapsedRealtime();
		
        /*******去掉音量回调, 减少AIDL调用次数****************/
		/*******AndyZhao 2017/9/11 for 意图YunOS**********/
		
		/********微信录音需要音量回调触发说话的检测********/
		/*******修改微信录音模块，风险稍大一点*************/
		/*******AndroidZhao 2017/9/18********************/
		if (!bNeedVolCallBack){
			return;
		}
		
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
	
	private void onError(final int errCode){
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				IWakeupCallback callback = mWakeupCallback;
				if (callback != null){
					callback.onError(errCode);
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
	private boolean bNeedVolCallBack = false;//是否需要音量回调
	@Override
	public int start(WakeupOption oOption) {
		if (!bInitSuccessed){
			LogUtil.logw("bInitSuccessed = " + bInitSuccessed);
			return -1;
		}
		
		if (bWkStarted){
			LogUtil.logw("start bWkStarted = " + bWkStarted);
			return 0;
		}
		if(oOption != null){
			mWakeupCallback = oOption.wakeupCallback;
			mConfig.setBeginSpeechTime(oOption.mBeginSpeechTime);
			JNIHelper.logd("start wakeup beginTime = "+oOption.mBeginSpeechTime);
		}
		bWkStarted = true;
		// 设置唤醒结果不以json的格式返回
		mSpeechUnderstander.setOption(
				SpeechConstants.ASR_OPT_RESULT_JSON, false);
		mSpeechUnderstander.setOption(
				SpeechConstants.WAKEUP_OPT_THRESHOLD_VALUE,
				-5.8f);// 阈值设置需要为float类型
		mSpeechUnderstander.setOption(
				SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, 100);
		int wakeup_model_id = SpeechConstants.AUTO_128_MODEL;
		if (ProjectCfg.mUseHQualityWakeupModel  || Arguments.sIsAsrWakeup){
			wakeup_model_id = SpeechConstants.AUTO_320_MODEL;
		}
		String printLog = String.format("WakeupArgment HOuality = %b, isAsrWakeup = %b, wakeup_model_id = %d",  
				ProjectCfg.mUseHQualityWakeupModel, Arguments.sIsAsrWakeup, wakeup_model_id);
		LogUtil.logd(printLog);
		
		bNeedVolCallBack = false;//默认不回调音量
		//云知声新的SDK,唤醒默认不回调音量, 但是微信录音时需要回调音量
		boolean bWeChatRecording = false;
		List<String> wxWorsd = new ArrayList<String>();
		wxWorsd.add("完毕完毕");
		wxWorsd.add("取消取消");
		wxWorsd.add("欧我欧我");
		wxWorsd.add("欧稳欧稳");

		List<String> feedbackWords = new ArrayList<String>();
		feedbackWords.add("完毕完毕");
		feedbackWords.add("取消取消");

		bWeChatRecording = checkWakeupWords(mLastWkWordList, wxWorsd) || checkWakeupWords(mLastWkWordList, feedbackWords);
		bNeedVolCallBack = bWeChatRecording;
		LogUtil.logd("WakeupArgment bWeChatRecording = " + bWeChatRecording + ", bNeedVolCallBack = " + bNeedVolCallBack);
		
		boolean bVadEnable = false;//V3引擎默认不开启VAD
		//云知声新的SDK,唤醒默认不使用VAD,但是微信录音时，需要打开VAD
		bVadEnable = bWeChatRecording;
		
		//mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_WAKEUP_VAD_ENABLED, bVadEnable);
		/********需要VAD事件但是又不影响识别******/
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_WAKEUP_VAD_ENABLED, true);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_VAD_AFFECT_ASR, false);
		
		//唤醒词前后有其他语音也可以唤醒
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_INHIBIT_BACK_WAKEUP, false);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_INHIBIT_FRONT_WAKEUP, false);
		mSpeechUnderstander.start(WAKEUPTAG);
		//检测音量回调
		AppLogic.removeBackGroundCallback(oCheckTask);
		AppLogic.runOnBackGround(oCheckTask, 6*1000);
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
			return;
		}
		LogUtil.logd("stopWakeup");
		bWkStarted = false;
		mWakeupCallback = null;
		AppLogic.removeBackGroundCallback(oCheckTask);
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
			return;//唤醒已启动的话，设置唤醒词，会阻塞该调用
		}
		
		//防止调用方设置了空唤醒词
		if (keywords == null || keywords.length == 0){
			LogUtil.logw("setWkKeywords keywords is empty!!!");
			return;
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
		//enable录音通道时, 可以认为当即有音量回调, 避免两个问题：
		//1、TTS较长, 大于3秒, 且长度约为3的整数倍的时候，容易出现， enable为true的瞬间， 尚未拿到音量回调，但是检测线程正在执行检测。
		//2、enable为true后, 音量回调有延时,检测线程稍后立马执行检测。
		if (enable){
			mLastVolTime = SystemClock.elapsedRealtime();
		}
		bVoiceEnable = enable;
		mConfig.enable(enable);
	}
	private boolean bVoiceEnable = true;
	private long mLastVolTime = SystemClock.elapsedRealtime();
	private Runnable oCheckTask = new Runnable() {
		@Override
		public void run() {
			//已经停止唤醒, 结束检测
			if (!bWkStarted){
				LogUtil.logw("checkVol bWkStarted  : " + bWkStarted);
				return;
			}
			
			// 唤醒录音被拦截时，延时检测
			if (!bVoiceEnable) {
				//LogUtil.logw("checkVol bVoiceEnable : " + bVoiceEnable);
				AppLogic.removeBackGroundCallback(oCheckTask);
				AppLogic.runOnBackGround(oCheckTask, 3 * 1000);
				return;
			}

			//2秒内有音量回调
			if (mLastVolTime + 2000 > SystemClock.elapsedRealtime()) {
				//LogUtil.logw("checkVol mLastVolTime : " + mLastVolTime + ", now : " + SystemClock.elapsedRealtime());
				AppLogic.removeBackGroundCallback(oCheckTask);
				AppLogic.runOnBackGround(oCheckTask, 3 * 1000);
				return;
			}
			
			LogUtil.loge("checkVol have no volume data for long time");
			onError(IWakeup.ERROR_CODE_NO_VOL);
		}
	};
	
	
}
