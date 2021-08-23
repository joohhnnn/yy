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
	
	//用于存储主备引擎错误码,ERROR_MAP_SIZE记得要根据ERROR_MAP_XXX的变化而变化
	public static final int ERROR_MAP_MAIN= 0;
	public static final int ERROR_MAP_BAKEUP = 1;
	public static final int ERROR_MAP_SIZE = 2;
	
	//用于存储离、在线、混合以及备用引擎的识别结果,RESULT_MAP_SIZE记得要根据RESULT_MAP_XXX的变化而变化
	public static final int RESULT_MAP_LOCAL = 0;
	public static final int RESULT_MAP_NET = 1;
	public static final int RESULT_MAP_MIX = 2;
	public static final int RESULT_MAP_NET_BAKEUP = 3;
	public static final int RESULT_MAP_SIZE = 4;
	
	private static class Result{
		public EngineType mEngineType = EngineType.ENGINE_NONE;//非NONE,表示该Result已经被修改过
		public int mErrorCode = IAsr.ERROR_SUCCESS;
		public float mScore = -100;
		public VoiceParseData mVoiceData = null;
		public long mRefeshTime = SystemClock.elapsedRealtime();
	}
	
     private SuperEngineBase mNetAsr = null;
     private SuperEngineBase mNetAsr_bak = null;//备用在线识别引擎
     private SuperEngineBase mLocalAsr = null;
	 private IInitCallback mInitCallback = null;
	 private Result[] mResults = null;
	 private int mLocalNetEngineType = AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET;
	 private int[] mAsrErrorCountMap = null;
	 private int[] mAsrSuccessCountMap = null;
	 private AsrEngine mMainEngine = null;
	 private AsrEngine mBakEngine = null;
	 private TXZAudioRecorder mAudioRecorder = null;
	 
	 /** 用户说话结束，识别到结果，等待语义结果返回超时，单位ms，最小值为5000，最大值为20000，超过范围无效 */
	 private int mAsrSemanticTimeout = -1;
	 /** 识别录音结束，等待语义结果返回，等待时间过长时进行提示。bool值，默认为true */
	 private boolean mAsrSemanticHintEnable = true;
	 //录音状态
	private boolean mReady = false;
	private boolean mIsNeedRecording = false;
	private Handler mRecordHandler = null;
	private HandlerThread mRecordThread = null;// 录音工作线程
	private final int DATA_BUFFER_SIZE = 1200; //录音buffer的size
	private final int BUFFER_SIZE = 320; //传入引擎的size
	private byte[] buffer = new byte[BUFFER_SIZE]; //传入引擎的音频
	private byte[] data_buffer = new byte[DATA_BUFFER_SIZE];
	private int mCacheSize = 20; // mCache的大小，以秒为单位
	private long mLastStartRecordTime = 0;

     public AsrContainer(){
    	mAsrSemanticHintEnable = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_ASR_SEMANTIC_HINT_ENABLE, true);
		int timeout = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_ASR_SEMANTIC_TIMEOUT, -1);
		if (timeout >= 5000 && timeout <= 20000) {
			mAsrSemanticTimeout = timeout;
			// 设置了超时取消识别功能，需要把超时优化提示功能关闭
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
    			 mAsrErrorCountMap[resultType] = 0;//做成连续多少次出错才触发备用逻辑
    		 }
    		 if (mAsrSuccessCountMap[resultType] < errCofig.getRestoreCnt()){
    			 mAsrSuccessCountMap[resultType]++;
    		 }
    	 }else{
    		 //忽略断网的时候错误码&忽略非识别引擎本身出错的错误码
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
    	 //连续N次成功识别,则恢复主引擎
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
        			 mAsrErrorCountMap[ERROR_MAP_BAKEUP] = 0;//重置标志位
        			 mAsrSuccessCountMap[ERROR_MAP_MAIN] = 0;//主引擎后续需要连续N次识别成功才可恢复
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
    			 mAsrErrorCountMap[ERROR_MAP_BAKEUP] = 0;//重置标志位
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
		//兼容之前的通过AppId决定主引擎的版本
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
		
		//初始化回调处理器
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
				mLocalAsr = new YZSSuperEngine(AsrMsgConstants.ENGINE_TYPE_YZS_MIX);//yzs混合识别
			} else {
				mLocalAsr = new YZSSuperEngine();//yzs离线识别do{
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
			do{
				if (bSuccess){
					break;
				}
				if (null == mLocalAsr){
					break;
				}
				int engineType = mLocalAsr.getEngineType();
				LogUtil.logi("local asr init failed, engine type = " + engineType);
				if (engineType == AsrMsgConstants.ENGINE_TYPE_YZS_MIX || engineType == AsrMsgConstants.ENGINE_TYPE_YZS_LOCAL){
					break;
				}
				if (engineType == AsrMsgConstants.ENGINE_TYPE_UVOICE_LOCAL){
					reInitLocalEngine();
					return;
				}
			}while(false);
			if (bSuccess){
				int currentType = UiEquipment.AET_DEFAULT;

				//取上次保存在sp里面的引擎类型
				int lastType = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_HISTORICAL_LOCAL_ASR_ENGINE_TYPE, UiEquipment.AET_DEFAULT);

				int localEngineType = mLocalAsr.getEngineType();
				switch (localEngineType){
					case AsrMsgConstants.ENGINE_TYPE_YZS_LOCAL:
					case AsrMsgConstants.ENGINE_TYPE_YZS_MIX:
						currentType = UiEquipment.AET_YZS_FIX; //云知声混合识别和离线识别属于同一类
						break;
					case AsrMsgConstants.ENGINE_TYPE_UVOICE_LOCAL:
						currentType = UiEquipment.AET_UVOICE_FIX;
						break;
					default:
						break;
				}

				//记录当前引擎类型
				LogUtil.d("current engine type = " + currentType);
				PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_HISTORICAL_LOCAL_ASR_ENGINE_TYPE, currentType);

				//与当前的引擎类型比较，如果类型不同，就删除grm文件，重新插词
				do {
					if (lastType == UiEquipment.AET_DEFAULT){
						break;
					}
					if (currentType == UiEquipment.AET_DEFAULT){
						break;
					}
					if (lastType != currentType){
						File grm = new File(AppLogic.getApp().getApplicationInfo().dataDir + "/grm");
						if (grm.exists()) {
							LogUtil.d("delete grm dir");
							FileUtil.removeDirectory(grm);
						}
					}
				}while(false);
			}
			mInitCallback.onInit(bSuccess);
		}
	};

     private void reInitLocalEngine(){
		 if (mLocalNetEngineType == AsrMsgConstants.ENGINE_TYPE_YZS_NET) {
		 	LogUtil.logi("yzs net");
			 mLocalAsr = new YZSSuperEngine(AsrMsgConstants.ENGINE_TYPE_YZS_MIX);//yzs混合识别
		 } else {
		 	LogUtil.logi("yzs local");
			 mLocalAsr = new YZSSuperEngine();//yzs离线识别do{
		 }

		 if (mLocalAsr.capacity() == 0x0011){
			 mNetAsr = null;
		 }
		 mLocalAsr.initialize(mLocalAsrInitCallback);
	 }
	
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
	
	/** 录音结束后，请求语义结果超时监听 */
	private Runnable mRunnableSemanticTimeout = null;
	/** 录音结束后，请求语义时间过长优化*/
	private Runnable mRunnableSemanticTimeoutHint = null;
	
	private boolean mHasSemanticTimeout = false;
	
	private int mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
	
	private void checkSemanticResult() {
		onResults();
	}

	/**
	 * 识别引擎结束录音，开始请求语义时
	 */
	private void beginTimeoutReqSemantic() {
		// 语义请求超时取消
		beginTimeoutSemanticCancel();
		// 语义请求时间过长优化提示
		beginTimeoutSemanticHint();
		// 上面两个任务超时互斥，不能同时存在
	}
	
	private void beginTimeoutSemanticHint() {
		// 配置中是否关闭提示功能
		if (!mAsrSemanticHintEnable) {
			return;
		}
		
		if (mHasSemanticTimeout) {
			JNIHelper.logd("SemanticTimeout added into queue");
			return;
		}
		
		// 识别场景是否支持语义提示
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
			//在线出错, 才使用离线引擎的音量,或者使用非离线引擎的音量
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
				//第一次错误码需要update,因为单个识别实例，在cancel之前有可能多次回调onError
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
				//第一次错误码需要update,因为单个识别实例，在cancel之前有可能多次回调onError
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
			//MIX引擎方式, 如果有任何结果,直接返回
			if (mResults[RESULT_MAP_MIX].mEngineType != EngineType.ENGINE_NONE){
				result = mResults[RESULT_MAP_MIX];
				break;
			}
			
			//未启用在线识别,直接使用离线识别的返回结果
			if (mResults[RESULT_MAP_NET].mErrorCode == IAsr.ERROR_ASR_NO_USE){
				result = mResults[RESULT_MAP_LOCAL];
				break;
			}
			
			//未启用离线线识别,直接使用在线识别的返回结果
			if (mResults[RESULT_MAP_LOCAL].mErrorCode == IAsr.ERROR_ASR_NO_USE){
				result = mResults[RESULT_MAP_NET];
				break;
			}
			
			if (mResults[RESULT_MAP_NET].mScore > ASR_THRESH) {
				result = mResults[RESULT_MAP_NET];
				break;
			}
			
			//在线识别提示没说话,信任度较高,可以直接采纳
			if (mResults[RESULT_MAP_NET].mErrorCode == IAsr.ERROR_NO_SPEECH) {
				result = mResults[RESULT_MAP_NET];
				break;
			}
			
			//离线识别提示没说话,信任度不够，需要结合在线结果
			if (mResults[RESULT_MAP_LOCAL].mErrorCode == IAsr.ERROR_NO_SPEECH) {
				//在线比离线更早返回错误,直接使用离线识别结果
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
		
		//满足以下条件,则使用备用引擎的识别结果
		do{
			//未启用在线识别,直接使用离线识别的返回结果,即不进入备用引擎结果判断
			if (mResults[RESULT_MAP_NET].mErrorCode == IAsr.ERROR_ASR_NO_USE){
				break;
			}
			
			//没有启用备用引擎
			if (mBakEngine == null){
				break;
			}
			//识别结果分数足够
			if (result != null && result.mScore > ASR_THRESH){
				break;
			}
			//备用引擎识别出错
			if (mResults[RESULT_MAP_NET_BAKEUP].mErrorCode != IAsr.ERROR_SUCCESS){
				break;
			}
			//主引擎尚未返回结果, so wait
			if (result == null){
				break;
			}
			//主引擎尚未返回结果, so wait
			if (result != null && result.mEngineType == EngineType.ENGINE_NONE){
				break;
			}
			result = mResults[RESULT_MAP_NET_BAKEUP];
		}while(false);
		
		//执行到此处，如果result.mEngineType等于ENGINE_NONE,代表了需要wait另一种结果
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
		mResults[RESULT_MAP_LOCAL] = new Result();//离线识别引擎结果
		mResults[RESULT_MAP_NET] = new Result();//在线识别引擎结果
		mResults[RESULT_MAP_MIX] = new Result();//混合识别引擎结果
		mResults[RESULT_MAP_NET_BAKEUP] = new Result();//备用识别引擎结果
		
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

		//开始录音
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
	    //因为NetAsr内部使用的是sendMsg的方式，并且已经切换过一次线程。
		//因此，此处不用切线程
		if (mAsrType == AsrType.ASR_MIX
				|| mAsrType == AsrType.ASR_ONLINE) {
				mNetAsrOption = cloneOption(oOption, mNetCallback);
				mNetAsr.start(mNetAsrOption);
		}
        
		//注意:如果NetAsr的start方法是耗时操作, LocalAsr识别时机会被推后。慎重!慎重!
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
				mAudioRecorder.beginSaveCache(20 * 16000 * 2);//保存最多20s的录音数据
			}
			mAudioRecorder.startRecording();

			while (mIsNeedRecording && mReady) {
				if (SystemClock.elapsedRealtime() - mLastStartRecordTime > mCacheSize * 1000) {//超时20s打断
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
