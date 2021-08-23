package com.txznet.txz.component.wakeup.mix;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.txznet.txz.module.wakeup.WakeupCmdTask;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.ThreshHoldAdapter;
import com.unisound.client.ErrorCode;
import com.unisound.client.IAudioSource;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;
 
public class WakeupPrebuiltImpl implements IWakeup{
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
				// 识别成功
				LogUtil.logd("WAKEUP_EVENT_RECOGNITION_SUCCESS");
				onWakeup();
				break;
			case SpeechConstants.ASR_EVENT_CANCEL:
				LogUtil.logd("ASR_EVENT_CANCEL");
				break;
			case SpeechConstants.ASR_EVENT_RECORDING_PREPARED:
				// 录音准备
				LogUtil.logd("ASR_EVENT_RECORDING_PREPARED");
				break;
			case SpeechConstants.ASR_EVENT_RECORDING_START:
				LogUtil.logd("ASR_EVENT_RECORDING_START");
				break;
			case SpeechConstants.ASR_EVENT_RECORDING_STOP:
				LogUtil.logd("ASR_EVENT_RECORDING_STOP");
				break;
			case SpeechConstants.ASR_EVENT_SPEECH_DETECTED:
				// 检测到说话
				LogUtil.logd("ASR_EVENT_SPEECH_DETECTED");
				onSpeechBegin();
				break;
			case SpeechConstants.ASR_EVENT_SPEECH_END:
				// 说话停止
				LogUtil.logd("ASR_EVENT_SPEECH_END");
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
			case SpeechConstants.ASR_EVENT_VAD_TIMEOUT:
				// VAD 超时
//				LogUtil.logd("ASR_EVENT_VAD_TIMEOUT");
				break;
			case SpeechConstants.ASR_EVENT_INIT_DONE:
				// SDK初始化完成
				LogUtil.logd("ASR_EVENT_INIT_DONE");
				onInit(true);
				break;
			case SpeechConstants.ASR_EVENT_COMPILER_INIT_DONE:
				// 离线编译初始化完成
				LogUtil.logd("ASR_EVENT_COMPILER_INIT_DONE");
				onCompilerInitDone();
				break;
			case SpeechConstants.ASR_EVENT_COMPILE_WAKEUP_WORD_DONE:
				// 唤醒词编译完成
				LogUtil.logd("ASR_EVENT_COMPILE_WAKEUP_WORD_DONE");
				onCompileWakeupWordDone(true);
				break;
			case SpeechConstants.WAKEUP_EVENT_SET_WAKEUPWORD_DONE:
				// 唤醒词设置完毕
				LogUtil.logd("WAKEUP_EVENT_SET_WAKEUPWORD_DONE");
				break;
			case SpeechConstants.ASR_EVENT_LOADGRAMMAR_DONE:
				// 用户模型加载完成
				LogUtil.logd("ASR_EVENT_LOADGRAMMAR_DONE");
				onLoadGrammarDone();
				break;
			}
		}

		@Override
		public void onError(int type, String errorMSG) {
			LogUtil.logw(" onError type : " + type + ", " + errorMSG);
			parseErrorCode(errorMSG);
		}
	};
	
	// {"errorCode":-91002,"errorMsg":"请求初始化错误"}
	private int parseErrorCode(String errorMsg) {
		JSONObject json = null;
		int errorCode = IWakeup.ERROR_CODE_OK;
		try {
			json = new JSONObject(errorMsg);
			errorCode = json.getInt("errorCode");
			switch (errorCode) {
			case ErrorCode.ASR_SDK_FIX_COMPILE_ERROR:
			case ErrorCode.ASR_SDK_FIX_COMPILE_NO_INIT:
				LogUtil.loge("ASR_SDK_FIX_COMPILE_ERROR");
				onCompileWakeupWordDone(false);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return errorCode;
	}
	
	private String mWakeupText = "";
	private int mWakeupTime = 0;
	private float mWakeupScore = 0f;
	
	public WakeupPrebuiltImpl() {
		File file = new File(GlobalContext.get().getApplicationInfo().dataDir + "/grammar/");
		if (!file.exists()) {
			file.mkdirs();
		}
	}
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
	
	private enum CompilerInitStatus{
		INIT_BEGIN, INIT_END, INIT_IDEL
	}
	
	private InitStatus mInitStatus = InitStatus.INIT_IDEL;
	private final static String PRIV_FILESDIR_NAME = "svr1";
	public int initWakeup(IInitCallback oRun) {
		mInitCallback = oRun;
		mSpeechUnderstander = new SpeechUnderstander(new TXZContext(GlobalContext.get(), PRIV_FILESDIR_NAME),
				ProjectCfg.getYunzhishengAppId()//
				, ProjectCfg.getYunzhishengSecret());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_SERVICE_MODE,
				SpeechConstants.ASR_SERVICE_MODE_LOCAL);
		// 关闭引擎log打印
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_DEBUG_SAVELOG, false);
		// 离线编译引擎
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_ADVANCE_INIT_COMPILER, false);
		mSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		mSpeechUnderstander.init("");
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
//				if (mCacheWkWords != null){
//					stop();
//					setWakeupKeywords(mCacheWkWords);
//					LogUtil.logd("init wk words : " + Arrays.toString(mCacheWkWords));
//					mCacheWkWords = null;
//				}
				if (mInitCallback != null) {
					mInitCallback.onInit(bSuccessed);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
//	private String[] mCacheWkWords = null;
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
		checkCacheLimit();
//		mCacheWkWords = cmds;
		initWakeup(oRun);
		return 0;
	}
    
	private IWakeupCallback mWakeupCallback = null;
	private boolean bWkStarted = false;
	private boolean bNeedVolCallBack = false;//是否需要音量回调
	@Override
	public synchronized int start(WakeupOption oOption) {
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
		
		String grammarPath = getWakeupGrammarPath(mLastCompileTask);
		if (grammarPath != null) {
			LogUtil.logd("load wakeupPath = " + grammarPath);
			mSpeechUnderstander.loadGrammar(WAKEUPTAG, grammarPath);
			try {
				this.wait(2000);//start/stop/setWakeupWord运行同一线程里面,不用担心stop/setWakeupWord操作会抢占该同步锁
			} catch (Exception e) {

			}
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
		bWeChatRecording = checkWakeupWords(mLastWkWordList, wxWorsd);
		bNeedVolCallBack = bWeChatRecording;
		LogUtil.logd("WakeupArgment bWeChatRecording = " + bWeChatRecording + ", bNeedVolCallBack = " + bNeedVolCallBack);
		
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
	public synchronized void stop() {
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
	private CompilerInitStatus mCompilerInitStatus = CompilerInitStatus.INIT_IDEL;
	private static final String DEFAULT_WAKEUP_PATH = GlobalContext.get().getApplicationInfo().dataDir + "/" + PRIV_FILESDIR_NAME + "/YunZhiSheng/asrfix/wakeup.dat";
	//private Map<String, List<String>> mWakeupKeywordsMap = new HashMap<String, List<String>>();
	private CmdCompileTask mCurrentCompileTask = null;
	private CmdCompileTask mLastCompileTask = null;
	private List<CmdCompileTask> mWaitCompileTasks = new LinkedList<CmdCompileTask>();
	public final static String WK_STATIC_DIR = "wk_static";
	public final static String WK_DYNAMIC_DIR = "wk_dynamic";
	public final static String WK_EXCLUSIVE_DIR = "wk_exclusive";
	private final static int WK_STATIC_CACHE_MAX_CNT = 200;//200*5K
	private final static int WK_EXCLUSIVE_CACHE_MAX_CNT = 100;//100*1K
	private final static int WK_DYNAMIC_CACHE_MAX_CNT = 50;//50*10K
	
	private void checkCacheLimit(){
		final String rootDir = GlobalContext.get().getApplicationInfo().dataDir;
		final String[] dirs = new String[]{WK_STATIC_DIR, WK_EXCLUSIVE_DIR, WK_DYNAMIC_DIR};
		for (String dir : dirs) {
			String fullPath = String.format("%s/%s", rootDir,dir);
			File f = new File(fullPath);
			if (!f.isDirectory()){
				continue;
			}
			
			int limitCnt = WK_STATIC_CACHE_MAX_CNT;
			if (TextUtils.equals(dir, WK_DYNAMIC_DIR)){
				limitCnt = WK_DYNAMIC_CACHE_MAX_CNT;
			}else if (TextUtils.equals(dir, WK_EXCLUSIVE_DIR)){
				limitCnt = WK_EXCLUSIVE_CACHE_MAX_CNT;
			}else if (TextUtils.equals(dir, WK_STATIC_DIR)){
				limitCnt = WK_STATIC_CACHE_MAX_CNT;
			}
			LogUtil.logd("checkCacheLimit , " + dir + ":" + limitCnt);
			delectRedundantFile(f.listFiles(), limitCnt);
		}
	}
	
	private void delectRedundantFile(File[] files, final int limitCnt){
		if (files == null){
			return;
		}
		
		int fileCnt = files.length;
		if (fileCnt < limitCnt){
			return;
		}
		
		for(File f : files){
			if (f == null){
				continue;
			}
			
			boolean ret = f.delete();
			if (!ret){
				LogUtil.logw("del fail:" +  f.getPath());
			}
		}
	}
	
	private String genKwsGrammar(CmdCompileTask task) {
		if (task == null){
			return "";
		}
		
		final int mask = task.getTaskKwsType();
		String grammardir = WK_STATIC_DIR;
		do {
			if ((mask & WakeupCmdTask.TYPE_EXCLUSIVE_MASK) != 0) {
				grammardir = WK_EXCLUSIVE_DIR;
				break;
			}
			
			if ((mask & WakeupCmdTask.TYPE_DYNAMIC_MASK) != 0) {
				grammardir = WK_DYNAMIC_DIR;
				break;
			}
			grammardir = WK_STATIC_DIR;
		} while (false);
		
		final String rootDir = GlobalContext.get().getApplicationInfo().dataDir;
		String fullPath = String.format("%s/%s/%s.dat", rootDir, grammardir, task.getTaskId());
		LogUtil.logd("wk_fullPath:" + fullPath);
		return fullPath;
	}
	
	private String findKwsGrammarInAllDir(String sTaskId) {
		if (TextUtils.isEmpty(sTaskId)){
			return "";
		}
		String grammarPath = "";
		final String rootDir = GlobalContext.get().getApplicationInfo().dataDir;
		final String[] dirs = new String[]{WK_STATIC_DIR, WK_EXCLUSIVE_DIR, WK_DYNAMIC_DIR};
		for (String dir : dirs) {
			String fullPath = String.format("%s/%s/%s.dat", rootDir,dir, sTaskId);
			File f = new File(fullPath);
			if (f.exists()) {
				grammarPath = fullPath;
				break;
			}
		}
		
		LogUtil.logd("wk_grammarPath:" + grammarPath);
		return grammarPath;
	}
	
	private String getWakeupGrammarPath(CmdCompileTask task) {
		if (task == null) {
			return null;
		}
		
		String grammarPath = null;
		do{
			{
				String wakeupDatPath = genKwsGrammar(task);
				if (!TextUtils.isEmpty(wakeupDatPath)){
					File file = new File(wakeupDatPath);
					if (file.exists()) {
						grammarPath = wakeupDatPath;
						break;
					}
				}
			}
			
			// 查找失败,再全路径搜索一遍
			{
				String wakeupDatPath = findKwsGrammarInAllDir(task.getTaskId());
				if (!TextUtils.isEmpty(wakeupDatPath)) {
					File file = new File(wakeupDatPath);
					if (file.exists()) {
						grammarPath = wakeupDatPath;
						break;
					}
				}
			}
			
		}while(false);
		
		return grammarPath;
	}
	
	private boolean saveWakeupKeywords(CmdCompileTask task) {
		//保存唤醒词语法
		String grmPath = genKwsGrammar(task);
		boolean bRet = false;
		do {
			if (TextUtils.isEmpty(grmPath)){
				break;
			}
			
			bRet = FileUtil.copyFile(DEFAULT_WAKEUP_PATH, grmPath);
			if (bRet) {
				LogUtil.logd("saveWakeupKeywords success : " + task.getTaskId());
			} else {
				//保存过程中出错，需要清除临时生成的文件
				File f = new File(grmPath);
				if (f.exists()){
					f.delete();
					LogUtil.loge("saveWakeupKeywords fail delect tmp file,  " + task.getTaskId());
				}
			}
		} while (false);
		
		return true;
	}
	
	private synchronized void onCompilerInitDone(){
		mCompilerInitStatus = CompilerInitStatus.INIT_END;
		execCompileTask();
	}
	
	private synchronized void onCompileWakeupWordDone(boolean bSuccessed) {
		if (mCurrentCompileTask == null) {
			return;
		}
		//成功才需要保存
		if (bSuccessed){
			saveWakeupKeywords(mCurrentCompileTask);
		}
		
		boolean needRestart = false;
		needRestart = mWaitCompileTasks.isEmpty();//编译任务结束后再重启唤醒
		
		mCurrentCompileTask = null;
		execCompileTask();
		//重启唤醒操作放到compiler释放之后，因为compiler释放中，load不了grammar
		if (needRestart) 
		{
			LogUtil.logd("need restart");
			onError(IWakeup.ERROR_CODE_RECORD_FAIL);//每次编译完都需要重启一次唤醒，不然grammar可能load不进去
		}
	}
	
	private synchronized void onLoadGrammarDone() {
		try {
			this.notifyAll();
		} catch (Exception e) {

		}
	}
	
	private void execCompileTask() {
		if (mCompilerInitStatus == CompilerInitStatus.INIT_IDEL) {
			// 未初始化
			LogUtil.logd("Initialization  compiler begin");
			mSpeechUnderstander.initCompiler();//异步接口
			LogUtil.logd("Initialization  compiler end");
			mCompilerInitStatus = CompilerInitStatus.INIT_BEGIN;
			return;
		}
		if (mCompilerInitStatus != CompilerInitStatus.INIT_END) {
			LogUtil.logd("Initializing compiler");
			return;
		}
		if (mCurrentCompileTask != null) {
			// 当前已有编译任务进行需要等待
			return;
		}
		if (mWaitCompileTasks.size() == 0) {
			// 任务已完成
			LogUtil.logd("Destory compiler begin");
			mSpeechUnderstander.destoryCompiler();//同步接口，耗时400ms左右
			LogUtil.logd("Destory compiler end");
			mCompilerInitStatus = CompilerInitStatus.INIT_IDEL;
			return;
		}
		
		mCurrentCompileTask = mWaitCompileTasks.remove(0);
		LogUtil.logw("set wakeup keywords begin");
		mSpeechUnderstander.setWakeupWord(mCurrentCompileTask.getKws());
	}
	
	
	@Override
	public synchronized void setWakeupKeywords(String[] keywords) {
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
		LogUtil.logd(Arrays.toString(keywords));
		// { "type" : "WAKEUP_TYPE_COMMON" }
		// 将需要添加的扩展信息放置到数组第一个位置
		int offset = 0;
		int kwsType = WakeupCmdTask.TYPE_NONE_MASK;
		
		try {
			JSONObject json = new JSONObject(keywords[0]);
			kwsType = json.getInt("type");
			offset = 1;
			LogUtil.logd("type:" + kwsType);
		} catch (Exception e) {
			LogUtil.logw("exception:" + e.toString());
		}
		
		List<String> WkWordList = new ArrayList<String>(keywords.length - offset);
		for (int i = offset; i < keywords.length; i++) {
			//空串SDK不会回调事件
			if (TextUtils.isEmpty(keywords[i])){
				continue;
			}
			WkWordList.add(keywords[i]);
		}
		
		//空SDK不会回调事件
		if (WkWordList.isEmpty()){
			LogUtil.logw("wakeup_words is empty");
			return;
		}
		
		mLastWkWordList = WkWordList;
		Set<String> set = new HashSet<String>();
		for (String string : WkWordList) {
			if (!TextUtils.isEmpty(string)) {
				set.add(string);
			}
		}
		// 判断唤醒词是否改变
		String sTaskId = MD5Util.generateMD5(set.toString());
		CmdCompileTask oTask = new WakeupCmdCompileTask(sTaskId, WkWordList, kwsType);
		//预编译类型的唤醒词，只参与编译，不影响下一次唤醒使用的唤醒词
		if (!WakeupCmdTask.isPreBuildType(kwsType)){
			mLastCompileTask = oTask;//下一次启动唤醒时,load这一次编译任务的唤醒词
		}
		addCompileTask(oTask);
	}
	
	private void addCompileTask(CmdCompileTask oTask){
		if (oTask == null){
			return;
		}
		
		if (getWakeupGrammarPath(oTask) != null) {
			LogUtil.logw("Wakeup keywords compiled");
			return;
		}
		
		final CmdCompileTask currTask = mCurrentCompileTask;
		if (currTask != null) {
			if (currTask.equals(oTask)) {
				LogUtil.logw("The task is compiling");
				return;
			}
		}
		
		for (int i = 0; i < mWaitCompileTasks.size(); i++) {
			if (oTask.equals(mWaitCompileTasks.get(i))) {
				LogUtil.logw("The task has already existed");
				return;
			}
		}
		
		mWaitCompileTasks.add(oTask);
		execCompileTask();
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
