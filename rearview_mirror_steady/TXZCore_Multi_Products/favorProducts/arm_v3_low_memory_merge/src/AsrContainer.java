package com.txznet.txz.component.asr.mix;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.TextUtils;

import com.txz.ui.data.UiData.TTime;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.record.UiRecord;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.mix.AsrCallbackFactory.ICallBackNotify;
import com.txznet.txz.component.asr.mix.AsrEngineController.AsrEngine;
import com.txznet.txz.component.asr.mix.AsrEngineController.ErrorConfig;
import com.txznet.txz.component.asr.mix.AsrEngineController.ExtraVoiceResult;
import com.txznet.txz.component.asr.mix.AsrEngineController.NetResultType;
import com.txznet.txz.component.asr.mix.IAsrCallBackProxy.CallBackOption;
import com.txznet.txz.component.asr.mix.IAsrCallBackProxy.CallBackOption.EngineType;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;
import com.txznet.txz.util.runnables.Runnable2;

import java.io.File;

public class AsrContainer implements IAsr{
	public static final String MONITOR_BACKUP_START = "asr.backup.start.";
	public static final String MONITOR_BACKUP_NEXT = "asr.backup.next.";
	public static final String MONITOR_BACKUP_RESTORE = "asr.backup.restore.";
	
	//?????????????????????????????????,ERROR_MAP_SIZE???????????????ERROR_MAP_XXX??????????????????
	public static final int ERROR_MAP_MAIN= 0;
	public static final int ERROR_MAP_BAKEUP = 1;
	public static final int ERROR_MAP_SIZE = 2;
	
	//??????????????????????????????????????????????????????????????????,RESULT_MAP_SIZE???????????????RESULT_MAP_XXX??????????????????
	public static final int RESULT_MAP_LOCAL = 0;
	public static final int RESULT_MAP_NET = 1;
	public static final int RESULT_MAP_MIX = 2;
	public static final int RESULT_MAP_NET_BAKEUP = 3;
	public static final int RESULT_MAP_SIZE = 4;
	
	private static class Result{
		public EngineType mEngineType = EngineType.ENGINE_NONE;//???NONE,?????????Result??????????????????
		public int mErrorCode = IAsr.ERROR_SUCCESS;
		public float mScore = -100;
		public VoiceParseData mVoiceData = null;
		public long mRefeshTime = SystemClock.elapsedRealtime();
	}
	
     private SuperEngineBase mNetAsr = null;
     private SuperEngineBase mNetAsr_bak = null;//????????????????????????
     private SuperEngineBase mLocalAsr = null;
	 private IInitCallback mInitCallback = null;
	 private Result[] mResults = null;
	 private int mLocalNetEngineType = AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET;
	 private int[] mAsrErrorCountMap = null;
	 private int[] mAsrSuccessCountMap = null;
	 private AsrEngine mMainEngine = null;
	 private AsrEngine mBakEngine = null;
	 private TXZAudioRecorder mAudioRecorder = null;
	 
	 /** ??????????????????????????????????????????????????????????????????????????????ms???????????????5000???????????????20000????????????????????? */
	 private int mAsrSemanticTimeout = -1;
	 /** ????????????????????????????????????????????????????????????????????????????????????bool???????????????true */
	 private boolean mAsrSemanticHintEnable = true;
	 //????????????
	private boolean mReady = false;
	private boolean mIsNeedRecording = false;
	private Handler mRecordHandler = null;
	private HandlerThread mRecordThread = null;// ??????????????????
	private final int DATA_BUFFER_SIZE = 1200; //??????buffer???size
	private final int BUFFER_SIZE = 320; //???????????????size
	private byte[] buffer = new byte[BUFFER_SIZE]; //?????????????????????
	private byte[] data_buffer = new byte[DATA_BUFFER_SIZE];
	private int mCacheSize = 20; // mCache???????????????????????????
	private long mLastStartRecordTime = 0;

     public AsrContainer(){
    	mAsrSemanticHintEnable = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_ASR_SEMANTIC_HINT_ENABLE, true);
		int timeout = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_ASR_SEMANTIC_TIMEOUT, -1);
		if (timeout >= 5000 && timeout <= 20000) {
			mAsrSemanticTimeout = timeout;
			// ???????????????????????????????????????????????????????????????????????????
			mAsrSemanticHintEnable = false;
		}
	 }
     
     private void zero(int[] data){
    	 if (data == null){
    		 return;
    	 }
    	 
    	 for (int i = 0; i < data.length; ++i){
    		 data[i] = 0;
    	 }
     }
     
     private void updateAsrErrorMap(int resultType, int errorCode, boolean successed){
    	 ErrorConfig errCofig = AsrEngineController.getIntance().getErrorConfig();
    	 if (successed){
    		 if (mAsrErrorCountMap[resultType] > 0){
    			 mAsrErrorCountMap[resultType] = 0;//????????????????????????????????????????????????
    		 }
    		 if (mAsrSuccessCountMap[resultType] < errCofig.getRestoreCnt()){
    			 mAsrSuccessCountMap[resultType]++;
    		 }
    	 }else{
    		 //??????????????????????????????&?????????????????????????????????????????????
			if (NetworkManager.getInstance().hasNet() && AsrEngineController.isValidAsrError(errorCode)) {
				if (mAsrErrorCountMap[resultType] < errCofig.getErrorMaxCnt()) {
					mAsrErrorCountMap[resultType]++;
				}
				if (mAsrSuccessCountMap[resultType] > 0){
					mAsrSuccessCountMap[resultType] = 0;
				}
			}
    	 }
     }
     
     private void checkAsrErrors(){
    	 ErrorConfig errCofig = AsrEngineController.getIntance().getErrorConfig();
    	 JNIHelper.logd("mainRestoreCnt : " + errCofig.getRestoreCnt());
    	 //??????N???????????????,??????????????????
    	 if (mAsrSuccessCountMap[ERROR_MAP_MAIN] >= errCofig.getRestoreCnt()){
    		 //stop bakeup online engine
    		 if (mBakEngine != null){
    			 monitor(MONITOR_BACKUP_RESTORE + mMainEngine.getName());
    			 String strLog = "stop bakeup engine";
    			 JNIHelper.logd(strLog);
    			 DebugCfg.showDebugToast(strLog);
    			 AsrEngineController.getIntance().push_top(mBakEngine);
    			 mBakEngine = null;
    			 mNetAsr_bak.release(true);
    		 }
    	 }
    	 JNIHelper.logd("mainMaxErrorCnt : " + errCofig.getErrorMaxCnt());
    	 if (mAsrErrorCountMap[ERROR_MAP_MAIN] >= errCofig.getErrorMaxCnt()){
    		 //start bakeup online engine
    		 if (mBakEngine == null){
    			 mBakEngine = AsrEngineController.getIntance().top();
        		 if (mBakEngine != null){
        			 monitor(MONITOR_BACKUP_START + mMainEngine.getName());
        			 String strLog = "start bakeup engine : " + mBakEngine.getName();
        			 JNIHelper.logd(strLog);
        			 DebugCfg.showDebugToast(strLog);
        			 mAsrErrorCountMap[ERROR_MAP_BAKEUP] = 0;//???????????????
        			 mAsrSuccessCountMap[ERROR_MAP_MAIN] = 0;//???????????????????????????N???????????????????????????
        			 mNetAsr_bak = SuperEngineProxy.getProxy();
        			 mNetAsr_bak.setEngineType(mBakEngine.getType());
        			 mNetAsr_bak.initialize(null);
        		 }
    		 }
    	 }
    	 
    	 if (mAsrErrorCountMap[ERROR_MAP_BAKEUP] >= errCofig.getErrorMaxCnt()){
    		 // switch to next bakeup online engine
    		 if (mBakEngine != null){
    			 monitor(MONITOR_BACKUP_NEXT + mBakEngine.getName());
    			 mAsrErrorCountMap[ERROR_MAP_BAKEUP] = 0;//???????????????
    			 AsrEngineController.getIntance().push_back(mBakEngine);
        		 mBakEngine = AsrEngineController.getIntance().top();
				if (mBakEngine != null) {
	    			 String strLog = "switch to next bakeup engine : " + mBakEngine.getName();
	    			 JNIHelper.logd(strLog);
	    			 DebugCfg.showDebugToast(strLog);
					mNetAsr_bak.release();
					mNetAsr_bak.setEngineType(mBakEngine.getType());
					mNetAsr_bak.initialize(null);
				}
    		 }
    	 }
     }
     
	@Override
	public int initialize(IInitCallback oRun) {
		mMainEngine = AsrEngineController.getIntance().getMainEngine();
		//?????????????????????AppId????????????????????????
		if (mMainEngine == null){
			int oldMainEngine =AsrEngineController.getIntance().getOldMainEngineType();
			AsrEngineController.getIntance().setMainEngine(oldMainEngine);
			mMainEngine = AsrEngineController.getIntance().getMainEngine();
		}
		
		LogUtil.logd("init_asr  mainEngineType:" + mMainEngine.getType() + ",mainEngineName:" + mMainEngine.getName());
		
		mLocalNetEngineType = mMainEngine.getType();
		mInitCallback = oRun;
		
		mAsrErrorCountMap = new int[ERROR_MAP_SIZE];
		mAsrSuccessCountMap = new int[ERROR_MAP_SIZE];
		zero(mAsrErrorCountMap);
		zero(mAsrSuccessCountMap);
		
		//????????????????????????
		mAsrCallBackProxy = AsrCallbackFactory.proxy(mCallBackHandler, mCallBackNotify);
		mLocalCallBackOption =new CallBackOption(EngineType.ENGINE_LOCAL);
		mNetCallBackOption =new CallBackOption(EngineType.ENGINE_NET);
		mNetCallBackOption_bak =new CallBackOption(EngineType.ENGINE_NET_BAK);
		mMixCallBackOption =new CallBackOption(EngineType.ENGINE_MIX);
		mLocalCallback = SuperEngineBase.createAsrCallBack(mAsrCallBackProxy, mLocalCallBackOption);
		mNetCallback = SuperEngineBase.createAsrCallBack(mAsrCallBackProxy, mNetCallBackOption);
		mNetCallback_bak = SuperEngineBase.createAsrCallBack(mAsrCallBackProxy, mNetCallBackOption_bak);
		mMixCallback = SuperEngineBase.createAsrCallBack(mAsrCallBackProxy, mMixCallBackOption);
		mAudioRecorder = new TXZAudioRecorder(false);
		mRecordThread = new HandlerThread("asr_container_record_thread");
		mRecordThread.start();
		mRecordHandler = new Handler(mRecordThread.getLooper());
		//mLocalNetEngineType = AsrMsgConstants.ENGINE_TYPE_YZS_NET;
		do {
			if (ImplCfg.useUVoiceAsr()) {
				mLocalAsr = new YZSSuperEngine(AsrMsgConstants.ENGINE_TYPE_UVOICE_LOCAL);
				LogUtil.logd("UVoice engine enable true");
				break;
			}
			LogUtil.d("AsrContainer", "not use uvoice");
			if (mLocalNetEngineType == AsrMsgConstants.ENGINE_TYPE_YZS_NET) {
				mLocalAsr = new YZSSuperEngine(AsrMsgConstants.ENGINE_TYPE_YZS_MIX);//yzs????????????
			} else {
				mLocalAsr = new YZSSuperEngine();//yzs????????????do{
			}
		} while (false);
		
		if (mLocalAsr.capacity() == 0x0011){
			mNetAsr = null; 
		}else{
			mNetAsr = SuperEngineProxy.getProxy(true);
			mNetAsr.setEngineType(mLocalNetEngineType);
			mNetAsr.initialize(null);
		}
   	    
   	    mLocalAsr.initialize(mLocalAsrInitCallback);
		return 0;
	}
	
	IInitCallback mLocalAsrInitCallback = new IInitCallback() {
		
		@Override
		public void onInit(boolean bSuccess) {
			if(mInitCallback == null){
				return;
			}
			mInitCallback.onInit(bSuccess);
		}
	};
	
	@Override
	public void release() {
		if (mNetAsr != null){
			mNetAsr.release();
		}
		mLocalAsr.release();
	}
    
	private AsrOption mNetAsrOption = null;
	private AsrOption mNetAsrOption_bak = null;
	private AsrOption mLocalAsrOption = null;
	private AsrOption mAsrOption = null;
	private AsrType mAsrType = AsrType.ASR_MIX;
	private CallBackOption mLocalCallBackOption = null;
	private CallBackOption mNetCallBackOption = null;
	private CallBackOption mNetCallBackOption_bak = null;
	private CallBackOption mMixCallBackOption = null;
	private boolean isStartRecording = false;
	private boolean isAsrWorking = false;
	
	private IAsrCallback mLocalCallback = null;
	private IAsrCallback mNetCallback = null;
	private IAsrCallback mNetCallback_bak = null;
	private IAsrCallback mMixCallback = null;
	
	private Runnable mSpeechTimeOutRun = new Runnable() {
		@Override
		public void run() {
			if (isAsrWorking){
				JNIHelper.logd("speechTimeout!!!");
				stop();
			}
		}
	};
	
	private void checkSpeechTimeout(long timeout){
		AppLogic.removeBackGroundCallback(mSpeechTimeOutRun);
		AppLogic.runOnBackGround(mSpeechTimeOutRun, timeout);
	}
	
	private void cancelSpeechTimeoutCheck(){
		AppLogic.removeBackGroundCallback(mSpeechTimeOutRun);
	}
	
	/** ???????????????????????????????????????????????? */
	private Runnable mRunnableSemanticTimeout = null;
	/** ????????????????????????????????????????????????*/
	private Runnable mRunnableSemanticTimeoutHint = null;
	
	private boolean mHasSemanticTimeout = false;
	
	private int mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
	
	private void checkSemanticResult() {
		onResults();
	}

	/**
	 * ????????????????????????????????????????????????
	 */
	private void beginTimeoutReqSemantic() {
		// ????????????????????????
		beginTimeoutSemanticCancel();
		// ????????????????????????????????????
		beginTimeoutSemanticHint();
		// ???????????????????????????????????????????????????
	}
	
	private void beginTimeoutSemanticHint() {
		// ?????????????????????????????????
		if (!mAsrSemanticHintEnable) {
			return;
		}
		
		if (mHasSemanticTimeout) {
			JNIHelper.logd("SemanticTimeout added into queue");
			return;
		}
		
		// ????????????????????????????????????
		if (mAsrOption == null || mAsrOption.mEnableSemanticHint == null
				|| mAsrOption.mEnableSemanticHint == false) {
			return;
		}
		if (null == mRunnableSemanticTimeoutHint) {
			mRunnableSemanticTimeoutHint = new Runnable() {
				
				@Override
				public void run() {
					JNIHelper.logd("SemanticTimeout: hint");
					String text = NativeData.getResString("RS_VOICE_ASR_SEMANTIC_WAIT");
					if (TextUtils.isEmpty(text)) {
						return;
					}
					mSpeechTaskId = TtsManager.getInstance().speakText(text, PreemptType.PREEMPT_TYPE_NEXT, new ITtsCallback() {
						@Override
						public void onSuccess() {
							mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
							RecorderWin.setState(RecorderWin.STATE.STATE_RECOGONIZE);
							AsrContainer.this.checkSemanticResult();
						}
						
						@Override
						public void onError(int iError) {
							mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
							RecorderWin.setState(RecorderWin.STATE.STATE_RECOGONIZE);
							AsrContainer.this.checkSemanticResult();
						}
					});
				}
			};
		}
		AppLogic.removeBackGroundCallback(mRunnableSemanticTimeoutHint);
		AppLogic.runOnBackGround(mRunnableSemanticTimeoutHint, 5000);
		mHasSemanticTimeout = true;
	}

	private void beginTimeoutSemanticCancel() {
		if (mAsrSemanticTimeout < 0) {
			return;
		}
		if (null == mRunnableSemanticTimeout) {
			mRunnableSemanticTimeout =  new Runnable() {
				@Override
				public void run() {
					JNIHelper.logd("SemanticTimeout: cancel");
					AsrContainer.this.cancel();
					mAsrOption.mCallback.onError(mAsrOption, 0, null, null, IAsr.ERROR_ASR_NET_REQUEST);
				}
			};
		}
		AppLogic.removeBackGroundCallback(mRunnableSemanticTimeout);
		AppLogic.runOnBackGround(mRunnableSemanticTimeout, mAsrSemanticTimeout);
	}
	
	private void cancelTimeoutReqSemantic() {
		canceltimeoutSemanticCancel();
		cancelTimeoutSemanticHint();
	}

	private void cancelTimeoutSemanticHint() {
		if (null == mRunnableSemanticTimeoutHint) {
			return;
		}
		AppLogic.removeBackGroundCallback(mRunnableSemanticTimeoutHint);
		mHasSemanticTimeout = false;
	}

	private void canceltimeoutSemanticCancel() {
		if (null == mRunnableSemanticTimeout) {
			return;
		}
		AppLogic.removeBackGroundCallback(mRunnableSemanticTimeout);
	}
	
	private ICallBackNotify mCallBackNotify = new ICallBackNotify() {
		@Override
		public boolean enable() {
			return isAsrWorking;
		}
	};
	
	private ComplexAsrCallBackHandler mCallBackHandler = new ComplexAsrCallBackHandler() {
		
		@Override
		public void onVolume(CallBackOption oOption, int vol) {
			//????????????, ??????????????????????????????,????????????????????????????????????
			if (oOption.engineType == EngineType.ENGINE_LOCAL){
				if (mResults[RESULT_MAP_NET].mErrorCode != IAsr.ERROR_SUCCESS){
					mAsrOption.mCallback.onVolume(mAsrOption, vol);
					return;
				}
				return;
			}
			mAsrOption.mCallback.onVolume(mAsrOption, vol);
		}
		
		@Override
		public void onSuccess(CallBackOption oOption, VoiceParseData oVoiceParseData) {
			if (oOption.engineType == EngineType.ENGINE_LOCAL){
				mResults[RESULT_MAP_LOCAL].mEngineType = EngineType.ENGINE_LOCAL;
				mResults[RESULT_MAP_LOCAL].mScore = oVoiceParseData.floatResultScore;
				mResults[RESULT_MAP_LOCAL].mVoiceData = oVoiceParseData;
				mResults[RESULT_MAP_LOCAL].mRefeshTime = SystemClock.elapsedRealtime();
			}else if (oOption.engineType == EngineType.ENGINE_NET){
				mResults[RESULT_MAP_NET].mEngineType = EngineType.ENGINE_NET;
				mResults[RESULT_MAP_NET].mScore = 100;
				mResults[RESULT_MAP_NET].mVoiceData = oVoiceParseData;
				mResults[RESULT_MAP_NET].mRefeshTime = SystemClock.elapsedRealtime();
				updateAsrErrorMap(ERROR_MAP_MAIN, IAsr.ERROR_SUCCESS, true);
			}else if (oOption.engineType == EngineType.ENGINE_NET_BAK){
				mResults[RESULT_MAP_NET_BAKEUP].mEngineType = EngineType.ENGINE_NET_BAK;
				mResults[RESULT_MAP_NET_BAKEUP].mScore = 100;
				mResults[RESULT_MAP_NET_BAKEUP].mVoiceData = oVoiceParseData;
				mResults[RESULT_MAP_NET_BAKEUP].mRefeshTime = SystemClock.elapsedRealtime();
				updateAsrErrorMap(ERROR_MAP_BAKEUP, IAsr.ERROR_SUCCESS, true);
			}else{
				mResults[RESULT_MAP_MIX].mEngineType = EngineType.ENGINE_MIX;
				mResults[RESULT_MAP_MIX].mScore = oVoiceParseData.floatResultScore;
				mResults[RESULT_MAP_MIX].mVoiceData = oVoiceParseData;
				mResults[RESULT_MAP_MIX].mRefeshTime = SystemClock.elapsedRealtime();
				ExtraVoiceResult oExtraVoiceResult = AsrEngineController.parseExtraVoiceResult(oVoiceParseData); 
				if (oExtraVoiceResult.mNetResultType == NetResultType.NETRESULT_TYPE_SUCCESS){
					updateAsrErrorMap(ERROR_MAP_MAIN, IAsr.ERROR_SUCCESS, true);
				}else if  (oExtraVoiceResult.mNetResultType == NetResultType.NETRESULT_TYPE_FAIL){
					updateAsrErrorMap(ERROR_MAP_MAIN, oExtraVoiceResult.mNetErrorCode, false);
				}
			}
			onResults();
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
			boolean needUpdate = false;
			if (oOption.engineType == EngineType.ENGINE_LOCAL){
				mResults[RESULT_MAP_LOCAL].mEngineType = EngineType.ENGINE_LOCAL;
				mResults[RESULT_MAP_LOCAL].mErrorCode = error2;
				mResults[RESULT_MAP_LOCAL].mRefeshTime = SystemClock.elapsedRealtime();
			}else if (oOption.engineType == EngineType.ENGINE_NET){
				//????????????????????????update,??????????????????????????????cancel???????????????????????????onError
				if (mResults[RESULT_MAP_NET].mErrorCode == IAsr.ERROR_SUCCESS){
					needUpdate = true;
				}
				
				mResults[RESULT_MAP_NET].mEngineType = EngineType.ENGINE_NET;
				mResults[RESULT_MAP_NET].mErrorCode = error2;
				mResults[RESULT_MAP_NET].mRefeshTime = SystemClock.elapsedRealtime();
				
				if (needUpdate && isAsrWorking){
					updateAsrErrorMap(ERROR_MAP_MAIN, error2, false);
				}
				
			}else if (oOption.engineType == EngineType.ENGINE_NET_BAK){
				//????????????????????????update,??????????????????????????????cancel???????????????????????????onError
				if (mResults[RESULT_MAP_NET_BAKEUP].mErrorCode == IAsr.ERROR_SUCCESS){
					needUpdate = true;
				}
				
				mResults[RESULT_MAP_NET_BAKEUP].mEngineType = EngineType.ENGINE_NET_BAK;
				mResults[RESULT_MAP_NET_BAKEUP].mErrorCode = error2;
				mResults[RESULT_MAP_NET_BAKEUP].mRefeshTime = SystemClock.elapsedRealtime();
				
				if (needUpdate && isAsrWorking){
					updateAsrErrorMap(ERROR_MAP_BAKEUP, error2, false);
				}
				
			}else{
				mResults[RESULT_MAP_MIX].mEngineType = EngineType.ENGINE_MIX;
				mResults[RESULT_MAP_MIX].mErrorCode = error2;
				mResults[RESULT_MAP_MIX].mRefeshTime = SystemClock.elapsedRealtime();
				updateAsrErrorMap(ERROR_MAP_MAIN, error2, false);
			}
			onResults();
		}
		
		@Override
		public void onEndOfSpeech(CallBackOption oOption) {
			mAsrOption.mCallback.onSpeechEnd(mAsrOption);
		}
		
		@Override
		public void onEnd(CallBackOption oOption) {
			mIsNeedRecording = false;
			if (oOption.engineType != EngineType.ENGINE_LOCAL){
				beginTimeoutReqSemantic();
				cancelSpeechTimeoutCheck();
				mAsrOption.mCallback.onEnd(mAsrOption);
			}
		}
		
		@Override
		public void onBeginOfSpeech(CallBackOption oOption) {
			mAsrOption.mCallback.onSpeechBegin(mAsrOption);
		}

		@Override
		public void onPartialResult(CallBackOption oOption,String partialResult) {
			onPartialResult(partialResult);
		}

		@Override
		public void onPartialResult(String partialResult) {
			mAsrOption.mCallback.onPartialResult(mAsrOption,partialResult);
		}
	};
	
	private void onResults(){
		if (mSpeechTaskId != TtsManager.INVALID_TTS_TASK_ID) {
			return;
		}
		if (mResults == null) {
			return;
		}
		
		Result result = null;
		do {
			//MIX????????????, ?????????????????????,????????????
			if (mResults[RESULT_MAP_MIX].mEngineType != EngineType.ENGINE_NONE){
				result = mResults[RESULT_MAP_MIX];
				break;
			}
			
			//?????????????????????,???????????????????????????????????????
			if (mResults[RESULT_MAP_NET].mErrorCode == IAsr.ERROR_ASR_NO_USE){
				result = mResults[RESULT_MAP_LOCAL];
				break;
			}
			
			//????????????????????????,???????????????????????????????????????
			if (mResults[RESULT_MAP_LOCAL].mErrorCode == IAsr.ERROR_ASR_NO_USE){
				result = mResults[RESULT_MAP_NET];
				break;
			}
			
			if (mResults[RESULT_MAP_NET].mScore > ASR_THRESH) {
				result = mResults[RESULT_MAP_NET];
				break;
			}
			
			//???????????????????????????,???????????????,??????????????????
			if (mResults[RESULT_MAP_NET].mErrorCode == IAsr.ERROR_NO_SPEECH) {
				result = mResults[RESULT_MAP_NET];
				break;
			}
			
			//???????????????????????????,??????????????????????????????????????????
			if (mResults[RESULT_MAP_LOCAL].mErrorCode == IAsr.ERROR_NO_SPEECH) {
				//?????????????????????????????????,??????????????????????????????
				if (mResults[RESULT_MAP_NET].mErrorCode != IAsr.ERROR_SUCCESS  
						&& mResults[RESULT_MAP_LOCAL].mRefeshTime > mResults[RESULT_MAP_NET].mRefeshTime){
					result = mResults[RESULT_MAP_LOCAL];
				}else{
					result = mResults[RESULT_MAP_NET];
				}
				break;
			}
			
			if (mResults[RESULT_MAP_LOCAL].mErrorCode != IAsr.ERROR_SUCCESS){
				result = mResults[RESULT_MAP_NET];
				break;
			}
			
			if (mResults[RESULT_MAP_LOCAL].mScore > ASR_THRESH) {
				result = mResults[RESULT_MAP_LOCAL];
				break;
			}
			
			if (mResults[RESULT_MAP_NET].mErrorCode != IAsr.ERROR_SUCCESS){
				result = mResults[RESULT_MAP_LOCAL];
				break;
			}
			
		} while (false);
		
		//??????????????????,????????????????????????????????????
		do{
			//?????????????????????,???????????????????????????????????????,????????????????????????????????????
			if (mResults[RESULT_MAP_NET].mErrorCode == IAsr.ERROR_ASR_NO_USE){
				break;
			}
			
			//????????????????????????
			if (mBakEngine == null){
				break;
			}
			//????????????????????????
			if (result != null && result.mScore > ASR_THRESH){
				break;
			}
			//????????????????????????
			if (mResults[RESULT_MAP_NET_BAKEUP].mErrorCode != IAsr.ERROR_SUCCESS){
				break;
			}
			//???????????????????????????, so wait
			if (result == null){
				break;
			}
			//???????????????????????????, so wait
			if (result != null && result.mEngineType == EngineType.ENGINE_NONE){
				break;
			}
			result = mResults[RESULT_MAP_NET_BAKEUP];
		}while(false);
		
		//????????????????????????result.mEngineType??????ENGINE_NONE,???????????????wait???????????????
		if (result != null && result.mEngineType != EngineType.ENGINE_NONE){
			onResult(result);
		}
	}
	
	private void onResult(Result result){
		JNIHelper.logd("engineType : " + result.mEngineType);
		do {
			
			if (result.mErrorCode != IAsr.ERROR_SUCCESS){
				mAsrOption.mCallback.onError(mAsrOption, 0, null, null, result.mErrorCode);
				break;
			}
			
			mAsrOption.mCallback.onSuccess(mAsrOption, result.mVoiceData);
		} while (false);
		end();
	}
	
	private IAsrCallBackProxy mAsrCallBackProxy = null;
	
	private void end(){
		cancelTimeoutReqSemantic();
		isAsrWorking = false;
		mReady = false;
		cancelSpeechTimeoutCheck();
		if (mNetAsr != null){
			mNetAsr.cancel();
		}
		
		if (mBakEngine != null){
			mNetAsr_bak.cancel();
		}
		mLocalAsr.cancel();
		mResults = null;
		checkAsrErrors();
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
		mResults = new Result[RESULT_MAP_SIZE];
		mResults[RESULT_MAP_LOCAL] = new Result();//????????????????????????
		mResults[RESULT_MAP_NET] = new Result();//????????????????????????
		mResults[RESULT_MAP_MIX] = new Result();//????????????????????????
		mResults[RESULT_MAP_NET_BAKEUP] = new Result();//????????????????????????
		
		mAsrType = oOption.mAsrType;
		mAsrOption = oOption;
		//????????????ID
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

		Arguments.sIsSaveVoice = ProjectCfg.enableSaveVoice();
		Arguments.sIsSaveRawPCM = ProjectCfg.enableSaveRawPCM();
		
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
		
		Integer timeout = oOption.mKeySpeechTimeout; 
		JNIHelper.logd("SpeechTimeout : " + timeout);
		if (timeout != null){
			checkSpeechTimeout(timeout);
		}

		//????????????
		mRecordHandler.removeCallbacks(mRecordingTask);
		mRecordHandler.post(mRecordingTask);

		if (mBakEngine != null && mAsrType != AsrType.ASR_LOCAL){
			mNetAsrOption_bak = cloneOption(oOption, mNetCallback_bak);
			mNetAsr_bak.start(mNetAsrOption_bak);
		}
		
		if (mLocalAsr.capacity() == 0x0011){
			mLocalAsrOption = cloneOption(oOption, mMixCallback);
			mLocalAsrOption.mAsrType = mAsrType;
			mLocalAsr.start(mLocalAsrOption);
			return 0;
		}
		
		if (mAsrType == AsrType.ASR_ONLINE){
			mResults[RESULT_MAP_LOCAL].mEngineType = EngineType.ENGINE_LOCAL;
			mResults[RESULT_MAP_LOCAL].mErrorCode = IAsr.ERROR_ASR_NO_USE;
			mResults[RESULT_MAP_LOCAL].mRefeshTime = SystemClock.elapsedRealtime();
		}else if (mAsrType == AsrType.ASR_LOCAL){
			mResults[RESULT_MAP_NET].mEngineType = EngineType.ENGINE_NET;
			mResults[RESULT_MAP_NET].mErrorCode = IAsr.ERROR_ASR_NO_USE;
			mResults[RESULT_MAP_NET].mRefeshTime = SystemClock.elapsedRealtime();
		}
	    //??????NetAsr??????????????????sendMsg????????????????????????????????????????????????
		//??????????????????????????????
		if (mAsrType == AsrType.ASR_MIX
				|| mAsrType == AsrType.ASR_ONLINE) {
				mNetAsrOption = cloneOption(oOption, mNetCallback);
				mNetAsr.start(mNetAsrOption);
		}
        
		//??????:??????NetAsr???start?????????????????????, LocalAsr?????????????????????????????????!??????!
		if (mAsrType == AsrType.ASR_MIX
				|| mAsrType == AsrType.ASR_LOCAL) {
			mLocalAsrOption = cloneOption(oOption, mLocalCallback);
			mLocalAsr.start(mLocalAsrOption);
		}
		return 0;
	}

	private Runnable mRecordingTask = new Runnable() {
		@Override
		public void run() {
			mReady = true;
			mIsNeedRecording = true;
			long nVoiceId = 0;
			mLastStartRecordTime = SystemClock.elapsedRealtime();
			if (mAsrOption != null) {
				nVoiceId = mAsrOption.mVoiceID;
			}
			if (nVoiceId != 0) {
				mAudioRecorder.beginSaveCache(20 * 16000 * 2);//????????????20s???????????????
			}
			mAudioRecorder.startRecording();

			while (mIsNeedRecording && mReady) {
				if (SystemClock.elapsedRealtime() - mLastStartRecordTime > mCacheSize * 1000) {//??????20s??????
					LogUtil.logd("AsrContainer:" + "mRecordingTask timeout");
					break;
				}
				if (mAudioRecorder != null) {
					mAudioRecorder.read(data_buffer, 0,
							data_buffer.length);
				} else {
					break;
				}
			}
			LogUtil.logd("AsrContainer:" + "mRecording end");
			mIsNeedRecording = false;
			mAudioRecorder.stop();
			if (nVoiceId != 0 && Arguments.sIsSaveVoice) {
				UiRecord.RecordData mRecordData = new UiRecord.RecordData();
				mRecordData.uint32SampleRate = RecordFile.SAMPLE_RATE_16K;
				mRecordData.uint32RecordType = UiRecord.RECORD_TYPE_ASR;
				mRecordData.uint64RecordTime = mAsrOption.mServerTime;
				mRecordData.boolRecordTime = mAsrOption.bServerTimeConfidence;
				mRecordData.uint32Uid = mAsrOption.mUID;
				mRecordData.boolRecordTime = true;
				mRecordData.uint32FilterNoiseType = ProjectCfg.getFilterNoiseType();
				mAudioRecorder.endSaveCache("new_#" + nVoiceId, mRecordData, Arguments.sIsSaveRawPCM);
			}
		}
	};

	@Override
	public void stop() {
		cancelSpeechTimeoutCheck();
		mLocalAsr.stop();
		if (mNetAsr != null){
			mNetAsr.stop();
		}
		if (mBakEngine != null){
			mNetAsr_bak.stop();
		}
	}

	@Override
	public void cancel() {
		if (mSpeechTaskId != TtsManager.INVALID_TTS_TASK_ID) {
			TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		}
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
		if (mLocalAsr.capacity() == 0x0011){
			return mLocalAsr.importKeywords(oKeywords, oCallback);
		}
		
		if (oKeywords.strType.equals("contact") || oKeywords.strType.startsWith("~")){
			//????????????
			bRet = mNetAsr.importKeywords(oKeywords, oCallback);
		}else{
			//????????????
			bRet = mLocalAsr.importKeywords(oKeywords, oCallback);
		}
		return bRet;
	}

	@Override
	public void releaseBuildGrammarData() {
	}

	@Override
	public void retryImportOnlineKeywords() {
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_RETRY_ONLINE_KEYWORDS);
	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
		
	}
	
	private void monitor(String strAttr){
		if (NetworkManager.getInstance().hasNet()){
			MonitorUtil.monitorCumulant(strAttr);
		}
	}
}
