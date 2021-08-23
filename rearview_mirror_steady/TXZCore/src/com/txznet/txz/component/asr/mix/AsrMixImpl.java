package com.txznet.txz.component.asr.mix;

import java.io.File;
import java.util.ArrayList;

import android.os.Environment;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;

import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.TTime;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.mix.AsrCallbackFactory.ICallBackNotify;
import com.txznet.txz.component.asr.mix.IAsrCallBackProxy.CallBackOption;
import com.txznet.txz.component.asr.mix.IAsrCallBackProxy.CallBackOption.EngineType;
import com.txznet.txz.component.asr.mix.local.LocalAsrPachiraImpl;
import com.txznet.txz.component.asr.mix.local.LocalAsrYunzhishengImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable2;

public class AsrMixImpl implements IAsr{
     private IAsr mNetAsr = null;
     private IAsr mLocalAsr = null;
	private static final int MAX_BAK_COUNT = 5;
	private static final int MAX_ERR_COUNT = 5;
	private static final int ERR_TIMEOUT = 3 * 60 * 1000;
	private int mCount = 0;
	private boolean bException = false;
	private ArrayList<Long> mExceptionTimes;
	private long mlastExceptionTime = -1;
    private int reInitTimes = 0;
	private IInitCallback mInitCallback = null;
	
     public AsrMixImpl(){
	 }
     
	@Override
	public int initialize(IInitCallback oRun) {
		if (!TextUtils.isEmpty(ProjectCfg.getTencentAppkey()) && !TextUtils.isEmpty(ProjectCfg.getTencentToken())) {
			AsrProxy.sEngineType = AsrMsgConstants.ENGINE_TYPE_TENCENT_NET;
		} else if (!TextUtils.isEmpty(ProjectCfg.getIflyAppId())) {
			AsrProxy.sEngineType = AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET;
		}else{
			AsrProxy.sEngineType = AsrMsgConstants.ENGINE_TYPE_YZS_NET;
		}
		mInitCallback = oRun;
		
		int engineType = ProjectCfg.getVoiceEngineType();
		JNIHelper.logd("engineType="+engineType+",bak="+ProjectCfg.getVoiceBakEngineType());
		if (engineType != 0) {
			if ((engineType & UiEquipment.VET_IFLY) == UiEquipment.VET_IFLY)
				AsrProxy.sEngineType = AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET;
			else if ((engineType & UiEquipment.VET_YUNZHISHENG) == UiEquipment.VET_YUNZHISHENG)
				AsrProxy.sEngineType = AsrMsgConstants.ENGINE_TYPE_YZS_NET;
			else if ((engineType & UiEquipment.VET_TENCENT) == UiEquipment.VET_TENCENT)
				AsrProxy.sEngineType = AsrMsgConstants.ENGINE_TYPE_TENCENT_NET;
		}
		
		//初始化回调处理器
		mAsrCallBackProxy = AsrCallbackFactory.proxy(mCallBackHandler, mCallBackNotify);
		mLocalCallBackOption =new CallBackOption(EngineType.ENGINE_LOCAL);
		mNetCallBackOption =new CallBackOption(EngineType.ENGINE_NET);
		
   	    mNetAsr = AsrProxy.getProxy();
   	    if(usePachiraAsr()){
   	    	mLocalAsr = new LocalAsrPachiraImpl();
   	    }else{
   	    	mLocalAsr = new LocalAsrYunzhishengImpl();
   	    }
   	    if(DebugCfg.PACHIRA_ENGINE_DEBUG){
   	    	mLocalAsr = new LocalAsrPachiraImpl();
   	    }
   	    
   	    
		mLocalAsr.initialize(mLocalAsrInitCallback);
		mNetAsr.initialize(null);
		mExceptionTimes = new ArrayList<Long>(MAX_ERR_COUNT);
		if (DebugCfg.debug_yzs()){
			debug_wk();
		}
		return 0;
	}
	
	IInitCallback mLocalAsrInitCallback = new IInitCallback() {
		
		@Override
		public void onInit(boolean bSuccess) {
			if(mInitCallback == null){
				return;
			}
			if(!bSuccess){
				reInitMeasure();
			}else{
				mInitCallback.onInit(bSuccess);
			}
			
		}
	};
	
	private boolean usePachiraAsr() {
		int engineType = ProjectCfg.getVoiceEngineType();
		if (engineType != 0) {
			if ((engineType & UiEquipment.VET_PACHIRA_OFFLINE) == UiEquipment.VET_PACHIRA_OFFLINE){
				if(!hasPachiraResource()){
					JNIHelper.loge("the pachira resource does not exist");
					return false;
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 本地识别引擎初始化失败后的重新初始化措施
	 * 先release掉离线识别引擎，然后delay 3秒*已经初始化的次数 的时间，
	 * 然后重新初始化离线识别引擎，连续初始化失败超过三次，延时3秒重启进程。
	 */
	private void reInitMeasure() {
		reInitTimes++;
		JNIHelper.loge("localAsr reinit times = " + reInitTimes);
		mLocalAsr.release();
		if(reInitTimes > 3){
			AppLogic.runOnBackGround(new Runnable() {
				@Override
				public void run() {
					JNIHelper.loge("txz killed by itself, because localAsr reInit fail 3 times");
					Process.killProcess(Process.myPid());
				}
			}, 3000);
		}
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				mLocalAsr.initialize(mLocalAsrInitCallback);
			}
		}, reInitTimes * 3000);
	}
	
	

	/**
	 * 检查普强资源文件是否存在
	 * @return
	 */
	private boolean hasPachiraResource() {
		boolean bAnalysis = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/pachira/analysis/").exists();
		boolean bPachira = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/pachira/analysis/pachira").exists();
		boolean bResource = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/pachira/resource/").exists();
		boolean bDecoderConf = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/pachira/resource/decoder.conf").exists();
		return bAnalysis & bResource & bDecoderConf & bPachira;
	}

	@Override
	public void release() {
		mNetAsr.release();
		mLocalAsr.release();
	}
    
	private AsrOption mNetAsrOption = null;
	private AsrOption mLocalAsrOption = null;
	private AsrOption mAsrOption = null;
	private int mNetAsrErrorCode = 0;
	private int mLocalAsrErrorCode = 0;
	private AsrType mAsrType = AsrType.ASR_MIX;
	private CallBackOption mLocalCallBackOption = null;
	private CallBackOption mNetCallBackOption = null;
	private VoiceParseData mLocalVoiceParseData = null;
	private boolean isStartRecording = false;
	private boolean isAsrWorking = false;
	private boolean isUseNetAsrVol = true;
	
	private IAsrCallback mNetCallback = new IAsrCallback() {
		public void onStart(AsrOption option){
			mAsrCallBackProxy.onStart(mNetCallBackOption);
		}
		
		public void onEnd(AsrOption option){
			mAsrCallBackProxy.onEnd(mNetCallBackOption);
		}
		
		public void onSpeechBegin(AsrOption option){
			mAsrCallBackProxy.onBeginOfSpeech(mNetCallBackOption);
		}
		
		public void onSpeechEnd(AsrOption option){
			mAsrCallBackProxy.onEndOfSpeech(mNetCallBackOption);
		}
		
		public void onSuccess(AsrOption option, VoiceParseData oVoiceParseData){
			mAsrCallBackProxy.onSuccess(mNetCallBackOption, oVoiceParseData);
		}
		
		public void onAbort(AsrOption option, int error){
			mAsrCallBackProxy.onError(mNetCallBackOption, error);
		}
		
		public void onError(AsrOption option, int error, String desc, String speech, int error2){
			mAsrCallBackProxy.onError(mNetCallBackOption, error2);
		}
		
		public void onCancel(AsrOption option){
			mAsrCallBackProxy.onError(mNetCallBackOption, -1);
		}
		
		public void onVolume(AsrOption option, int volume) {
			if (isUseNetAsrVol) {
				mAsrCallBackProxy.onVolume(mNetCallBackOption, volume);
			}
		};

		@Override
		public void onPartialResult(AsrOption option, String partialResult) {
			super.onPartialResult(option, partialResult);
			mAsrCallBackProxy.onPartialResult(partialResult);
		}
	};
	
	private IAsrCallback mLocalCallback = new IAsrCallback() {
		public void onStart(AsrOption option){
			mAsrCallBackProxy.onStart(mLocalCallBackOption);
		}
		
		public void onEnd(AsrOption option){
			//mAsrCallBackProxy.onEnd(mLocalCallBackOption);
		}
		
		public void onSpeechBegin(AsrOption option){
			mAsrCallBackProxy.onBeginOfSpeech(mLocalCallBackOption);
		}
		
		public void onSpeechEnd(AsrOption option){
			mAsrCallBackProxy.onEndOfSpeech(mLocalCallBackOption);
		}
		
		public void onSuccess(AsrOption option, VoiceParseData oVoiceParseData){
			mAsrCallBackProxy.onSuccess(mLocalCallBackOption, oVoiceParseData);
		}
		
		public void onAbort(AsrOption option, int error){
			mAsrCallBackProxy.onError(mLocalCallBackOption, error);
		}
		
		public void onError(AsrOption option, int error, String desc, String speech, int error2){
			mAsrCallBackProxy.onError(mLocalCallBackOption, error2);
		}
		
		public void onCancel(AsrOption option){
			mAsrCallBackProxy.onError(mLocalCallBackOption, -1);
		}
		
		public void onVolume(AsrOption option, int volume) {
			if (!isUseNetAsrVol) {
				mAsrCallBackProxy.onVolume(mLocalCallBackOption, volume);
			}
		};
	};
	
	private ICallBackNotify mCallBackNotify = new ICallBackNotify() {
		@Override
		public boolean enable() {
			return isAsrWorking;
		}
	};
	private void changeEngine() {
		changeEngine(0);
	}
	private void changeEngine(int type) {
		// just for test
		// mNetAsr.release();
		// AsrProxy.sEngineType = AsrMsgConstants.ENGINE_TYPE_YZS_NET +
		// AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET - AsrProxy.sEngineType;
		if (ProjectCfg.getVoiceBakEngineType() == 0
				|| ProjectCfg.getVoiceEngineType() == 0)
			return;
		int enginType = 0;
		switch (AsrProxy.sEngineType) {
		case AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET:
			enginType = UiEquipment.VET_IFLY;
			break;
		case AsrMsgConstants.ENGINE_TYPE_YZS_NET:
			enginType = UiEquipment.VET_YUNZHISHENG;
			break;
		}
		mNetAsr.release();
		if (type != 0) {
			AsrProxy.sEngineType = getEnginTypeByProtoType(type);
		}
		else if ((ProjectCfg.getVoiceEngineType() & enginType) != 0) {
			AsrProxy.sEngineType = getEnginTypeByProtoType(ProjectCfg
					.getVoiceBakEngineType());
		} else {
			AsrProxy.sEngineType = getEnginTypeByProtoType(ProjectCfg
					.getVoiceEngineType());
		}
	}

	public int getEnginTypeByProtoType(int protoType) {
		if ((protoType & UiEquipment.VET_IFLY) != 0)
			return AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET;
		else if ((protoType & UiEquipment.VET_YUNZHISHENG) != 0)
			return AsrMsgConstants.ENGINE_TYPE_YZS_NET;
		return AsrMsgConstants.ENGINE_TYPE_YZS_NET;
	}

	private ComplexAsrCallBackHandler mCallBackHandler = new ComplexAsrCallBackHandler() {
		
		@Override
		public void onVolume(CallBackOption oOption, int vol) {
			mAsrOption.mCallback.onVolume(mAsrOption, vol);
		}
		
		@Override
		public void onSuccess(CallBackOption oOption, VoiceParseData oVoiceParseData) {
			final IAsrCallback callback = mAsrOption.mCallback;
			if (oOption.engineType == EngineType.ENGINE_LOCAL){
				if (mAsrType == AsrType.ASR_LOCAL || checkResult(oVoiceParseData) || mNetAsrErrorCode < 0) {
					JNIHelper.logd("local : " + oVoiceParseData.strVoiceData + ", score = " + oVoiceParseData.floatResultScore + ", mNetAsrErrorCode = " + mNetAsrErrorCode);
					callback.onSuccess(mAsrOption, oVoiceParseData);
					end();
				}else{
					//在线识别超时或者出错时使用
					mLocalVoiceParseData = oVoiceParseData;
				}
			}else if (oOption.engineType == EngineType.ENGINE_NET){
				if (bException) {
					if (mCount == MAX_BAK_COUNT - 1) {
						bException = false;
						mCount = 0;
						changeEngine(ProjectCfg.getVoiceEngineType());
						if (AsrProxy.sEngineType == AsrMsgConstants.ENGINE_TYPE_YZS_NET)
							MonitorUtil.monitorCumulant("asr.yzs.I.recover");
						else if (AsrProxy.sEngineType == AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET)
							MonitorUtil.monitorCumulant("asr.ifly.I.recover");
					} else if (mCount == MAX_BAK_COUNT - 2) {
						changeEngine();
					}
				} else {
					mCount = 0;
					mlastExceptionTime = -1;
					mExceptionTimes.clear();
				}
				//在线识别直接返回,并且结束本轮识别
				JNIHelper.logd("net : " + oVoiceParseData.strVoiceData);
				callback.onSuccess(mAsrOption, oVoiceParseData);
				end();
			}
		}
		
		@Override
		public void onStart(CallBackOption oOption) {
			if (!isStartRecording){
				mAsrOption.mCallback.onStart(mAsrOption);
				isStartRecording = true;
			}
		}
		
		@Override
		public void onError(CallBackOption oOption, int error2) {
			final IAsrCallback callback = mAsrOption.mCallback;
			if (oOption.engineType == EngineType.ENGINE_NET){
				if (!bException) {
					if (error2 == IAsr.ERROR_ASR_NET_REQUEST) {
						mCount++;
						if (mlastExceptionTime == -1)
							mlastExceptionTime = SystemClock.elapsedRealtime();
						else {
							if (mExceptionTimes.size() >= MAX_ERR_COUNT)
								mExceptionTimes.remove(0);
							mExceptionTimes.add(SystemClock.elapsedRealtime()
									- mlastExceptionTime);
							mlastExceptionTime = SystemClock.elapsedRealtime();
						}
						if (mCount >= MAX_ERR_COUNT) {
							long time = 0;
							for (int i = 0; i < mExceptionTimes.size(); i++)
								time += mExceptionTimes.get(i);
							if (time < ERR_TIMEOUT) {
								mCount = 0;
								mlastExceptionTime = -1;
								bException = true;
								if (AsrProxy.sEngineType == AsrMsgConstants.ENGINE_TYPE_YZS_NET)
									MonitorUtil.monitorCumulant("asr.yzs.E.timeouts");
								else if (AsrProxy.sEngineType == AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET)
									MonitorUtil.monitorCumulant("asr.ifly.E.timeouts");
								changeEngine();
							}
						}
					} else {
						mCount = 0;
						mlastExceptionTime = -1;
						mExceptionTimes.clear();
					}
				} else {
					if (mCount == MAX_BAK_COUNT - 2
							|| mCount == MAX_BAK_COUNT - 1) {
						changeEngine();
					}
				}
				mNetAsrErrorCode = error2;
				if (mAsrType == AsrType.ASR_ONLINE || mNetAsrErrorCode == IAsr.ERROR_NO_SPEECH || mLocalAsrErrorCode != 0){
					callback.onError(mAsrOption, 0, null, null, error2);
					end();
				}else{
					//混合识别,在线识别网络问题时
					//离线结果已出，则返回离线结果
					if (mLocalVoiceParseData != null){
						callback.onSuccess(mAsrOption, mLocalVoiceParseData);
						end();
					}
					//等待离线识别结果
				}
			}else if (oOption.engineType == EngineType.ENGINE_LOCAL){
				if (mAsrType == AsrType.ASR_LOCAL || mNetAsrErrorCode != 0){
					callback.onError(mAsrOption, 0, null, null, error2);
					end();
				}
				mLocalAsrErrorCode = error2;
			}
		}
		
		@Override
		public void onEndOfSpeech(CallBackOption oOption) {
			mAsrOption.mCallback.onSpeechEnd(mAsrOption);
		}
		
		@Override
		public void onEnd(CallBackOption oOption) {
			mAsrOption.mCallback.onEnd(mAsrOption);
		}
		
		@Override
		public void onBeginOfSpeech(CallBackOption oOption) {
			mAsrOption.mCallback.onSpeechBegin(mAsrOption);
		}

		@Override
		public void onPartialResult(CallBackOption oOption, String partialResult) {
			onPartialResult(partialResult);
		}

		@Override
		public void onPartialResult(String partialResult) {
			mAsrOption.mCallback.onPartialResult(mAsrOption,partialResult);
		}
	};
	private IAsrCallBackProxy mAsrCallBackProxy = null;
	private final static float ASR_THRESH_LOCAL = -5.0f; 
	private boolean  checkResult(VoiceParseData oVoiceParseData){
		boolean bRet = false;
		if (oVoiceParseData.floatResultScore != null){
			if (oVoiceParseData.floatResultScore > ASR_THRESH_LOCAL){
				bRet = true;
			}
		}
		return bRet;
	}
	
	private void end(){
		isAsrWorking = false;
		mNetAsr.cancel();
		mLocalAsr.cancel();
	}
	
	private AsrOption cloneOption(AsrOption oOption, IAsrCallback callback){
		AsrOption option = new AsrOption();
		option.setAccent(oOption.mAccent);
		option.setBOS(oOption.mBOS);
		option.setEOS(oOption.mEOS);
		option.setGrammar(oOption.mGrammar);
		option.setKeySpeechTimeout(oOption.mKeySpeechTimeout);
		option.setLanguage(oOption.mLanguage);
		option.setManual(oOption.mManual);
		option.setNeedStopWakeup(oOption.mNeedStopWakeup);
		option.mAsrType = oOption.mAsrType;
		option.setCallback(callback != null ? callback : oOption.mCallback);
		option.mBeginSpeechTime = oOption.mBeginSpeechTime;
		option.mNeedStopWakeup = oOption.mNeedStopWakeup;
		option.mVoiceID = oOption.mVoiceID;
		option.mTtsId = oOption.mTtsId;
		option.mUID = oOption.mUID;
		option.mServerTime = oOption.mServerTime;
		option.mPlayBeepSound = oOption.mPlayBeepSound;
		return option;
	}
	@Override
	public int start(AsrOption oOption) {
		mAsrType = oOption.mAsrType;
		mAsrOption = oOption;
		//生成识别ID
		if (oOption.mVoiceID == 0){
			mAsrOption.mVoiceID = NativeData.getMilleServerTime().uint64Time;
		}
		if(oOption.mUID == null){
			oOption.mUID = NativeData.getUID();
		}
		if(oOption.mServerTime == null){
			TTime tTime = NativeData.getMilleServerTime();
			oOption.mServerTime = tTime.uint64Time;
			oOption.bServerTimeConfidence = tTime.boolConfidence;
		}
		AsrManager.getInstance().mAsrCount ++;
		ReportUtil.doReport(new ReportUtil.Report.Builder().setTaskID(mAsrOption.mVoiceID+"")
				.setSessionId().setType("asr").setAction("start")
				.putExtra("asrCount", AsrManager.getInstance().mAsrCount).buildCommReport());
		mNetAsrErrorCode = 0;
		mLocalAsrErrorCode = 0;
		mLocalVoiceParseData = null;
		isStartRecording = false;
		isAsrWorking = true;
		mAsrCallBackProxy.setAsrOption(mAsrOption);
		if (mAsrType == AsrType.ASR_AUTO) {
			if (NetworkManager.getInstance().hasNet()) {
				mAsrType = AsrType.ASR_ONLINE;
			}else{
				mAsrType = AsrType.ASR_LOCAL;
			}
		}
		JNIHelper.logd("AsrType origin : " + oOption.mAsrType.name() + " now : " + mAsrType.name());
		if (bException) {
			mCount = (mCount + 1) % MAX_BAK_COUNT;
			JNIHelper.logd("exception count="+mCount);
		}
		if (mAsrType == AsrType.ASR_MIX
				|| mAsrType == AsrType.ASR_ONLINE) {
			if (!(ConfigManager.getInstance().getServerConfig().bDataPartner != null
					&& !ConfigManager.getInstance().getServerConfig().bDataPartner
					&& SimManager.getInstance().mLastFlow == 0 && NetworkManager
					.getInstance().getNetType() != UiData.NETWORK_STATUS_WIFI)) {
				mNetAsrOption = cloneOption(oOption, mNetCallback);
				mNetAsr.start(mNetAsrOption);
			}
			else
				mNetAsrErrorCode = IAsr.ERROR_ASR_NET_REQUEST;
		}

		if (mAsrType == AsrType.ASR_MIX
				|| mAsrType == AsrType.ASR_LOCAL) {
			mLocalAsrOption = cloneOption(oOption, mLocalCallback);
			mLocalAsr.start(mLocalAsrOption);
		}
		if (NetworkManager.getInstance().hasNet()) {
			if (mAsrType == AsrType.ASR_LOCAL) {
				isUseNetAsrVol = false;
			}else {
				isUseNetAsrVol = true;
			}
		}else {
			if (mAsrType == AsrType.ASR_ONLINE) {
				isUseNetAsrVol = true;
			}else {
				isUseNetAsrVol = false;
			}
		}
		return 0;
	}

	@Override
	public void stop() {
		mLocalAsr.stop();
		mNetAsr.stop();
	}

	@Override
	public void cancel() {
		end();
	}

	@Override
	public boolean isBusy() {
		return false;
	}

	@Override
	public boolean buildGrammar(SdkGrammar oGrammarData,
			IBuildGrammarCallback oCallback) {
		AppLogic.runOnBackGround(
				new Runnable2<SdkGrammar, IBuildGrammarCallback>(oGrammarData,
						oCallback) {
					@Override
					public void run() {
						if (mP2 != null) {
							mP2.onSuccess(mP1);
						}
					}
				}, 0);
		return true;
	}

	@Override
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		boolean bRet = false;
		if (oKeywords.strType.equals("contact") || oKeywords.strType.startsWith("~")){
			//在线词表
			bRet = mNetAsr.importKeywords(oKeywords, oCallback);
		}else{
			//本地词表
			bRet = mLocalAsr.importKeywords(oKeywords, oCallback);
		}
		return bRet;
	}

	@Override
	public void releaseBuildGrammarData() {
	}

	@Override
	public void retryImportOnlineKeywords() {
		if (mNetAsr != null) {
			mNetAsr.retryImportOnlineKeywords();
		}
	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
		
	}
	
	private final static String TASKID = "AsrMixImpl";
	private void debug_wk(){
		WakeupManager.getInstance().useWakeupAsAsr(new AsrComplexSelectCallback(){
			@Override
			public boolean needAsrState() {
				return false;
			}
			
			@Override
			public String getTaskId() {
				return TASKID;
			}
			
			public void onCommandSelected(String type, String command) {
				String switchEngine = NativeData.getResString("RS_VOICE_SWITCH_ENGINE");
				String text = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", switchEngine);
				if (isWakeupResult()){
					select_engine();
					TtsManager.getInstance().speakText(text);
				}else{
					select_engine();
					RecorderWin.speakTextWithClose(text, new Runnable() {
						@Override
						public void run() {
						}
					});
				}
			};
		}.addCommand("CMD_SELECT_ENGINE", "切换引擎"));
	}
	
	private void select_engine() {
		if (AsrProxy.sEngineType == AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET) {
			AsrProxy.sEngineType = AsrMsgConstants.ENGINE_TYPE_YZS_NET;
		} else {
			AsrProxy.sEngineType = AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET;
		}
		mNetAsr.release();
	}

}
