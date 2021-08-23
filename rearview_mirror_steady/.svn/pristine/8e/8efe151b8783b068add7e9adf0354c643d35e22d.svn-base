package com.txznet.txz.component.asr.yunzhisheng_3_0;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import android.os.Environment;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.component.asr.txzasr.IEngine;
import com.txznet.txz.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.TXZHandler;

public class AsrWakeupEngine {
	public static final String AUDIO_SAVE_FILE_PREFIX = Environment
			.getExternalStorageDirectory().getPath() + "/txz/voice";
	public final static String NULL_FILE_PARENT_DIR = Environment
			.getExternalStorageDirectory().getPath() + "/txz/null";
	public static String NULL_FILE = "";//默认
	
	private HandlerThread backThread = null;
	private TXZHandler workHandler = null;

	private HandlerThread compileThread = null;
	private TXZHandler compileHandler = null;
    private String strEngine = STR_YZS_ENGINE;
    public static final String STR_YZS_ENGINE = "com.txznet.txz.component.asr.txzasr.YzsEngine";
    public static final String STR_TXZ_ENGINE = "com.txznet.txz.component.asr.txzasr.TxzEngine";
    
	static AsrWakeupEngine s_engine = new AsrWakeupEngine();
	public static float WAKEUP_OPT_THRESHOLD = -3.1f;
    private IEngine mEngine = null;
	public static enum AsrState {
		ASR_RECOGNIZE, ASR_WAKEUP, ASR_RECORDING, ASR_IDLE
	};
	
	private boolean checkDebugFlag() {
		String debug = Environment.getExternalStorageDirectory().getPath()
				+ "/txz/debug_engine.properties";
		boolean bRet = false;
		File f = new File(debug);
		if (f.exists()) {
			bRet = true;
			try {
				Properties proerties = new Properties();
				proerties.load(new FileInputStream(debug));
				String engine = proerties.getProperty("engine", "");
				JNIHelper.logd("engine = " + engine);
				if (engine.equals("yzs")){
					strEngine = STR_YZS_ENGINE;
				}else if (engine.equals("txz")){
					strEngine = STR_TXZ_ENGINE;
				}
				JNIHelper.logd("strEngine = " + strEngine);
			} catch (Exception e) {

			}
		}
		return bRet;
	}
	
	private boolean  hasCreatedForHole = false;
	private synchronized void createBlackHole() {
		if (hasCreatedForHole){
			JNIHelper.logd("hasCreateForHole = " + hasCreatedForHole);
			return;
		}
		JNIHelper.logd("NULL_FILE_PARENT_DIR = " + NULL_FILE_PARENT_DIR);
		File f = new File(NULL_FILE_PARENT_DIR);
		if (!f.exists()) {
			boolean bRet = f.mkdirs();
			if (bRet) {
				NULL_FILE = NULL_FILE_PARENT_DIR + "/wakeup.pcm";
				JNIHelper.logd("create blackHole " + NULL_FILE);
			}else{
				JNIHelper.logd("makedirs fail : " + NULL_FILE_PARENT_DIR);
				return;
			}
		} else {
			NULL_FILE = NULL_FILE_PARENT_DIR + "/wakeup.pcm";
			JNIHelper.logd("create blackHole " + NULL_FILE);
		}
		hasCreatedForHole = true;
		AppLogic.removeBackGroundCallback(oDelBlackHoleRun);
		AppLogic.runOnBackGround(oDelBlackHoleRun, DelBlackHoleFreq);
    }
    
    private final int DelBlackHoleFreq = 10000*6;
    private Runnable oDelBlackHoleRun = new Runnable(){
		@Override
		public void run() {
			File f = new File(NULL_FILE);
			if (f.exists()){
				f.delete();
			}
			AppLogic.removeBackGroundCallback(oDelBlackHoleRun);
			AppLogic.runOnBackGround(oDelBlackHoleRun,  DelBlackHoleFreq);
		}
    };
    
	private AsrWakeupEngine() {
		backThread = new HandlerThread("AsrWakeupEngineThread"); 
		backThread.start();
		workHandler = new TXZHandler(backThread.getLooper());

		compileThread = new HandlerThread("AsrWakeupEngineCompileThread");
		compileThread.start();
		compileHandler = new TXZHandler(compileThread.getLooper());

		File f = new File(AUDIO_SAVE_FILE_PREFIX);
		if (!f.exists()) {
			f.mkdirs();
		}
	}
    
	public void runOnBackGround(Runnable oRun, int delay) {
		if (workHandler != null) {
			if (delay <= 0) {
				workHandler.post(oRun);
			} else {
				workHandler.postDelayed(oRun, delay);
			}
		}
	}

	public void delOnBackGround(Runnable oRun) {
		if (workHandler != null) {
			workHandler.removeCallbacks(oRun);
		}
	}

	public void runOnCompileBackGround(Runnable oRun, int delay) {
		if (compileHandler != null) {
			if (delay <= 0) {
				compileHandler.post(oRun);
			} else {
				compileHandler.postDelayed(oRun, delay);
			}
		}
	}

	public void delOnCompileBackGround(Runnable oRun) {
		if (compileHandler != null) {
			compileHandler.removeCallbacks(oRun);
		}
	}

	public static AsrWakeupEngine getEngine() {
		return s_engine;
	}

	public  void setWakeupWords(List<String> keyWordList) {
		if (mEngine != null){
			mEngine.setWakeupWords(keyWordList);
		}
	}

	public void startWakeup(IWakeupCallback oCallback) {
		if (mEngine != null){
			mEngine.startWakeup(oCallback);
		}
	}

	public void stopWakeup() {
		if (mEngine != null){
			mEngine.stopWakeup();
		}
	}

	public int startWithRecord(IWakeupCallback oCallback) {
		if (mEngine != null){
			return mEngine.startWithRecord(oCallback);
		}
		return 0;
	}

	public void stopWithRecord() {
		if (mEngine != null){
			mEngine.stopWithRecord();
		}
	}

	public static interface AsrAndWakeupIIintCallback {
		public void onInit(boolean bSuccessed);
	}

	public synchronized int initialize(AsrAndWakeupIIintCallback oRun) {
		if (mEngine == null){
			if (!TextUtils.isEmpty(ProjectCfg.getIflyAppId()) && !TextUtils.isEmpty(ProjectCfg.getYunzhishengAppId())){
				strEngine = STR_TXZ_ENGINE;
			}else{
				strEngine = STR_YZS_ENGINE;
			}
			checkDebugFlag();
			try {
				mEngine = (IEngine) Class
						.forName(strEngine)
						.newInstance();
			} catch (Exception e) {

			}
		}
		
		if (mEngine != null) {
			if (ProjectCfg.needBlackHole()) {
				createBlackHole();
			}
			return mEngine.initialize(oRun);
		}
		return 0;
	}

	public int startAsr(AsrOption oOption) {
		if (mEngine != null){
			return mEngine.startAsr(oOption);
		}
		return 0;
	}

	public void stopAsr() {
		if (mEngine != null){
			mEngine.stopAsr();
		}
	}
	
	public void cancelAsr() {
		if (mEngine != null){
			mEngine.cancelAsr();
		}
	}

	public boolean isBusy() {
		if (mEngine != null){
			return mEngine.isBusy();
		}
		return false;
	}

	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		if (mEngine != null){
			return mEngine.importKeywords(oKeywords, oCallback);
		}
		return false;
	}
	
	public void retryImportOnlineKeywords(){
	}
	
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
	}
	
	public void enableAutoRun(boolean enable){
		if (mEngine != null){
			mEngine.enableAutoRun(enable);
		}
	}

	public AsrState getAsrState() {
		if (mEngine != null){
			return mEngine.getAsrState();
		}
		return AsrState.ASR_IDLE;
	}
	
}
