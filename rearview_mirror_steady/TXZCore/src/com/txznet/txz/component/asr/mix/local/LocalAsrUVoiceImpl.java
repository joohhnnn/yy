package com.txznet.txz.component.asr.mix.local;

import android.os.HandlerThread;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.UVoiceSDK.Params;
import com.txz.UVoiceSDK.TXZRecognizer;
import com.txz.UVoiceSDK.UVoiceActivateParam;
import com.txz.UVoiceSDK.UVoiceCallback;
import com.txz.UVoiceSDK.UnivoiceVadParam;
import com.txz.ui.record.UiRecord;
import com.txz.ui.record.UiRecord.RecordData;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.mix.Arguments;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.util.ExchangeHelper;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.TXZHandler;
import com.txznet.txz.util.recordcenter.QueueBlockingCache;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class LocalAsrUVoiceImpl implements IAsr {
	private TXZRecognizer mEngine;
	private IInitCallback mInitCallback = null;
	private AsrOption mAsrOption = null;
	private TXZAudioRecorder mAudioRecorder = null;
	// 录音线程，通过这个线程获取录音数据
	private TXZHandler mRecordHandler = null;
	private HandlerThread mRecordThread = null;
	// 引擎工作线程
	private TXZHandler mWorkHandler = null;
	private HandlerThread mWorkThread = null;
	private TXZHandler mSlotHandler;
	private HandlerThread mSlotThread;
	private boolean mRecording = false;// 录音状态
	private boolean mReady = false;
	private boolean bInitOk = false;
	private static final String TAG = "LocalAsrUVoiceImpl";
	private final long mSpeechDelay = 0; //start后的延迟

	//引擎参数
	private int mResultType = 0;//设置识别结果类型:0:识别json; 1:识别文本; 2:质量分析文本; 3:质量分析json; 4:识别词网格文本; 5:识别词网格JSON; 6:N个最好的json结果; 7:N个最好的文本结果
	private boolean setDefaultDecoder = false;
	private final static String WORK_PATH = AppLogic.getApp().getApplicationInfo().dataDir + "/uvoice/workpath"; //工作路径
	private final String decoderPath = AppLogic.getApp().getApplicationInfo().dataDir + "/data/"; //Slot和bnf路径
	private boolean bSetPrintFunc = true; //设置打印函数
	private final static String ACOUSTIC_FILE_NAME = "mix_chn_16k_20181128_v2_asrq08_vadq08.bin";
	public final static String POI_NAME = "UVoicePOI";


	private final String slotsPath = AppLogic.getApp().getApplicationInfo().dataDir + "/data/"; //Slot和bnf路径
	//编译词表slot存放路径
	private final static String SLOT_DIR = AppLogic.getApp().getApplicationInfo().dataDir + "/slots/";

	//激活文件名字
	private final static String ACTIVATE_FILE_NAME = "activate.conf";
	//激活文件路径
	private String mActivateFilePath = AppLogic.getApp().getApplicationInfo().dataDir + "/data/" + ACTIVATE_FILE_NAME;
	//appId
	private String mAppId = LicenseManager.getInstance().getAppId() + "UVoice";
	//默认激活文件备份路径
	private final static String DEFAULT_ACTIVATE_FILE_DIR = "/sdcard/txz/";
	//激活文件备份路径
	private String mBackupActivateFilePath;
	
	UVoiceCallback mCallback = new UVoiceCallback() {
		
		@Override
		public void onResult(int type, String result) {
			LogUtil.d(TAG, result);
			if (TextUtils.isEmpty(result)){
				LogUtil.e(TAG, "the result is null");
				LocalAsrUVoiceImpl.this.onError(ERROR_NO_MATCH);
			} else if (type == Params.ASR_RESULT_JSON){
//				LogUtil.logd(TAG+ "the result: "+result);
				saveJsonStr(result);
				parseResult(result);
			} else{
//				LogUtil.logd(TAG+ "the result: "+result);
			}
			
		}
		
		@Override
		public void onEvent(int event, String msg) {
			switch (event) {
			case Params.ASR_EVENT_RESULT:
				LogUtil.d(TAG, "return result");
				onEnd();
				stopProcessAudio();
				stop();
				stopEngine();
				break;
			case Params.ASR_EVENT_BOS:
				LogUtil.d(TAG, "detect the speech!!");
				onStart();
				break;
			case Params.ASR_EVENT_NOSPEECH:
				LogUtil.d(TAG, "speech end");
				LocalAsrUVoiceImpl.this.onError(ERROR_NO_SPEECH);
				break;
			case Params.ASR_EVENT_OK:
//				LogUtil.d(TAG, "OK");
				break;
			case Params.ASR_EVENT_INIT_SUCCESS:
				LogUtil.d(TAG, "init success");
				LogUtil.d(TAG, "UVoice engine version: "+ mEngine.getVersion());
				LogUtil.d(TAG, "UVoice engine info: "+ mEngine.getInfo());

				if (!isSameFile(mActivateFilePath, mBackupActivateFilePath)){
					LogUtil.logi("backup activate file");
					FileUtil.copyFile(mActivateFilePath, mBackupActivateFilePath);
				}
				LogUtil.d(TAG, "start add model");
				addModel();
				LogUtil.d(TAG, "add model successful");
				mEngine.create();
				LogUtil.d(TAG, "create context successful");
				onInit(true);
				break;
			case Params.ASR_EVENT_INIT_FAIL:
				LogUtil.e(TAG, "init fail, msg=" + msg);
				onInit(false);
				break;
			default:
				break;
			}
		}
		
		@Override
		public void onError(int code, String msg) {
			LogUtil.loge("onError::code="+code+"; msg="+msg);
			switch (code) {
            case Params.ASR_ERROR_ACTIVATE_FAILED:
				LogUtil.e(TAG, "activate fail");
				onInit(false);
				break;
			case Params.ASR_ERROR_NO_EXIST:
			case Params.ASR_ERROR_EXIST:
				break;
			default:
                LocalAsrUVoiceImpl.this.onError(IAsr.ERROR_CODE);
                cancel();
				break;
			}
		}
	};
	
	public LocalAsrUVoiceImpl() {
	}
	
	protected void onError(final int errCode) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (mAsrOption != null && mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onError(mAsrOption, 0, null, null, errCode);
				}
			}

		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void doInit() {
		LogUtil.d(TAG, "start doInit");
		//在联网激活的时候，会依赖libssl.so.1.1和libcrypto.so.1.1
		//因为这两个文件不是以.so 结尾的，放在solibs目录下时，并不能解压到apk里面
		//所以，把这两个文件放在assets/data 目录下，在初始化之前load
		String ssl = AppLogic.getApp().getApplicationInfo().dataDir + "/data/libssl.so.1.1";
		String crypto = AppLogic.getApp().getApplicationInfo().dataDir + "/data/libcrypto.so.1.1";
		boolean bRet = false;
		do {
			try {
				if (new File(crypto).exists()) {
					LogUtil.logi("load crypto");
					System.load(crypto);
				} else {
					LogUtil.loge("lib does not exist: " + crypto);
					break;
				}
				if (new File(ssl).exists()) {
					LogUtil.logi("load ssl");
					System.load(ssl);
				} else {
					LogUtil.loge("lib does not exist: " + ssl);
					break;
				}
				bRet = true;
			} catch (Exception e) {
			}
		} while(false);

		if (!bRet){
			LogUtil.loge("load so failed!");
			onInit(false);
			return;
		}

		mBackupActivateFilePath = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_ASR_UVOICE_ACTIVATE_BACKUP_DIR, String.class, DEFAULT_ACTIVATE_FILE_DIR);
		if (!mBackupActivateFilePath.endsWith(File.separator))
			mBackupActivateFilePath += File.separator;
		mBackupActivateFilePath += ACTIVATE_FILE_NAME;
		do{
			if (new File(mActivateFilePath).exists()){
				break;
			}
			if (new File(mBackupActivateFilePath).exists()){
				LogUtil.d(TAG, "use backup file");
				FileUtil.copyFile(mBackupActivateFilePath, mActivateFilePath);
				break;
			}
			LogUtil.e(TAG, "no activate file exists, activate online");
		} while(false);

		File slotDir = new File(SLOT_DIR);
		if (!slotDir.exists()){
			slotDir.mkdir();
		}

//		AppLogic.runOnBackGround(initTimeoutRunnable, 1000 * 30);
		mEngine = TXZRecognizer.getInstance(GlobalContext.get(), mCallback);
		mEngine.setOption(Params.ASR_SETTING_RESULT_TYPE, mResultType);
		mEngine.setOption(Params.ASR_SETTING_PRINT_FUNCTION, bSetPrintFunc);
		File workPath = new File(WORK_PATH);
		if (!workPath.exists()){
			workPath.mkdirs();
		}
		mEngine.setOption(Params.ASR_SETTING_WORK_PATH, WORK_PATH);

		//设置激活参数
		//(pc_client_id, pc_product_id, pc_uuid, pc_activate_file)
		//id 需要传入32位的
		String appId = MD5Util.generateMD5(mAppId);
		String silentId = String.format(Locale.CHINA, "%032d", 0);
		String uuid = String.format(Locale.CHINA, "%032d", ProjectCfg.getUid());
		LogUtil.d(TAG, "appId = " + appId + "; silentId = " + silentId + "; uuid = " + uuid);
		UVoiceActivateParam param = new UVoiceActivateParam(silentId, appId, uuid, mActivateFilePath);
		mEngine.setOption(Params.ASR_SETTING_ACTIVATE_PARAM, param);
		
		do {
			String strAcousticPath = AppLogic.getApp().getApplicationInfo().dataDir + "/data/" + ACOUSTIC_FILE_NAME;
			File file = new File(strAcousticPath);
			if (!file.exists()){
				LogUtil.loge(TAG + "acoustic model does not exist");
				break;
			}
			LogUtil.logd(TAG + "acoustic model path: "+strAcousticPath);
			mEngine.setOption(Params.ASR_SETTING_ACOUSTIC_PATH, strAcousticPath);
		} while (false);

		//初始化添加slot的handler
		mSlotThread = new HandlerThread("UVoice.slot.thread");
		mSlotThread.start();
		mSlotHandler = new TXZHandler(mSlotThread.getLooper());

		loadSlotMap();
		mEngine.init();
	}

	private void setVAD(){
		//设置vad参数
		int vad_ms_end_cont_sie = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_ASR_UVOICE_VAD_SIL_TIMEOUT, 1000);
		LogUtil.d(TAG, "set vad eos : " + vad_ms_end_cont_sie + " ms");
		float vad_threshold = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_ASR_UVOICE_VAD_THRESHOLD, Float.class, 0.7f);
		LogUtil.d(TAG, "set vad threshold : " + vad_threshold);
		UnivoiceVadParam VADParam = new UnivoiceVadParam(1, vad_threshold, 5000, 100, 600, vad_ms_end_cont_sie, 0.5f, 0);
		mEngine.setOption(Params.ASR_SETTING_VAD_PARAM, VADParam);
	}
	
	/**
	 * 添加模型
	 */
	private void addModel(){
		//设置vad参数
		setVAD();

		//离线POI
		addPOI();

		//编译词表
		addBnf();
	}

	//离线POI
	private void addPOI(){
		String modelPath = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_ASR_UVOICE_POI_DECODER_PATH);
		if (TextUtils.isEmpty(modelPath)){
			LogUtil.loge("the decoder path is empty");
			return;
		}
		if (new File(modelPath).exists()) {
			addDecoder(POI_NAME, modelPath);
			addRescore();
		}
	}

	//编译词表
	private void addBnf() {
		LogUtil.logi("start add slot");
		String decoderName = "txz";
		addDecoder(decoderName, decoderPath+"UVoice_"+decoderName+".dat");
		File slotDir = new File(SLOT_DIR);
		File[] slots = null;
		do {
			if (!slotDir.exists()) {
				LogUtil.loge("slot dir does not exist");
				break;
			}
			slots = slotDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".dat");
				}
			});
			if (null == slots) {
				LogUtil.loge("slot list is null");
				break;
			}
			for (File slot : slots) {
				String slotName = slot.getName();
				slotName = slotName.substring(0, slotName.length() - 4);
				SlotType slotType = new SlotType(null, slotName, SLOT_DIR + slotName + ".dat", null, SlotType.TYPE_DAT);
				mSlots.add(slotType);
			}
		}while(false);

		//添加data目录下的slots
		String slotListPath = slotsPath + decoderName + "_slot_list";
		String[] otherSlots = getContents(slotListPath);
		if (null == otherSlots) {
			return;
		}
		for (String slotName : otherSlots) {
			if (containSlot(slots, decoderName + "#" + slotName)) {
//				LogUtil.logi(slotName + " slot is exist");
				continue;
			}
			if (!new File(slotsPath + slotName + ".txt").exists()){
				continue;
			}
			SlotType slot = new SlotType(null, decoderName + "#" + slotName, slotsPath + slotName + ".txt", null, SlotType.TYPE_TXT);
			mSlots.add(slot);
		}
		importSlot();
	}

	private boolean containSlot(File[] slotList, String slot){
		boolean bRet = false;
		slot += ".dat";
		do {
			if (null == slotList || null == slot) {
				break;
			}
			for (File s : slotList) {
				if (s.getName().equals(slot)){
					bRet = true;
					break;
				}
			}
		}while (false);
		return bRet;
	}

	private void addRescore(){
		String rescorePath = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_ASR_UVOICE_POI_RESCORE_PATH);
		if (TextUtils.isEmpty(rescorePath)){
			LogUtil.loge("the rescore path is empty");
			return;
		}
		if (new File(rescorePath).exists()) {
			addRescore("rescore", "wfst-compress", rescorePath, POI_NAME);
		}
	}

	private String[] getContents(String fileName) {
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				LogUtil.loge(TAG + "the file does not exist: " + fileName);
				throw new FileNotFoundException();
			}
			BufferedReader br = new BufferedReader(new FileReader(file));
			String result;
			ArrayList<String> results = new ArrayList<String>();
			result = br.readLine();
			while (result != null) {
				results.add(result);
				result = br.readLine();
			}
			br.close();
			String[] res = new String[results.size()];
			return results.toArray(res);
		} catch (Exception e){
			e.printStackTrace();
		}

		return null;
	}

	private boolean addDecoder(String name, String path){
		File file = new File(path);
		if (!file.exists()){
			return false;
		}
		if (mEngine == null){
			return false;
		}
		return mEngine.addDecoder(name, path, null);
	}
	
	private boolean addRescore(String name, String type, String path, String decoderName){
		File file = new File(path);
		if (!file.exists()){
			return false;
		}
		return mEngine.addRescore(name, type, path, decoderName);
	}
	
	/*Runnable initTimeoutRunnable = new Runnable() {

		@Override
		public void run() {
			if (mInitCallback != null) {
				LogUtil.loge(TAG + "uvoice init failed");
				mInitCallback.onInit(false);
			}
		}
	};*/

	private boolean bInitFst = false;
	/**
	 * 初始化fst模型，fst用于离线编译词表。需要在动态插词之前执行
	 */
	public void initFst(){
		if (bInitFst){
			return;
		}
		//编译词表线程，initFst的操作比较耗时，可能会超过5s，所以不使用死锁检测
		mSlotHandler.resetTime();
		mEngine.initFst();
		mSlotHandler.heartbeat();
		bInitFst = true;
	}

	@Override
	public int initialize(IInitCallback oRun) {
		LogUtil.d(TAG, "initialize function");
		mInitCallback = oRun;
		mRecordThread = new HandlerThread("uvoice_record_thread");
		mRecordThread.start();
		mRecordHandler = new TXZHandler(mRecordThread.getLooper());
		mWorkThread = new HandlerThread("uvoice_process_thread");
		mWorkThread.start();
		mWorkHandler = new TXZHandler(mWorkThread.getLooper());
		mAudioRecorder = new TXZAudioRecorder(ProjectCfg.mEnableAEC);
		mCache = new QueueBlockingCache(32000*mCacheSize); //mCacheSize 秒
		doInit();
		return 0;
	}
	
	protected void onVolume(final Integer vol) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (mAsrOption != null && mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onVolume(mAsrOption, vol);
				}
			}

		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private boolean bNeedEnd;
	protected void onEnd() {
		LogUtil.d(TAG, "onEnd, bNeedEnd=" + bNeedEnd);
		if (bNeedEnd) {
			bNeedEnd = false;
            mRecording = false;
            Runnable oRun = new Runnable() {
                @Override
                public void run() {
                    if (mAsrOption != null && mAsrOption.mCallback != null) {
                        mAsrOption.mCallback.onEnd(mAsrOption);
                    }
                }

            };
            AppLogic.runOnBackGround(oRun, 0);
        }
	}

	protected void onStart() {
		LogUtil.d(TAG, "onStart");
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (mAsrOption != null && mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onStart(mAsrOption);
				}
			}

		};
		AppLogic.runOnBackGround(oRun, 0);
	}


	protected void onInit(final boolean state) {
		LogUtil.d(TAG, "onInit, state="+state);
//		AppLogic.removeBackGroundCallback(initTimeoutRunnable);
		bInitOk = state;
		Runnable oRun = new Runnable() {
			public void run() {
				if (mInitCallback != null) {
					mInitCallback.onInit(state);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);

	}

	@Override
	public void release() {
		LogUtil.logi(TAG + "release");
		if (mReady && mEngine != null) {
			mRecording = false;
			mReady = false;
			bInitOk = false;
			LogUtil.logd(TAG + "destroy");
			mEngine.release();
		}
	}

	private boolean bStart;
	@Override
	public int start(AsrOption oOption) {
		if (!bInitOk){
			LogUtil.loge("uvoice engine was not init");
			if (null!=oOption && null!=oOption.mCallback) {
				oOption.mCallback.onError(oOption, 0, null, null, IAsr.ERROR_CODE);
			}
			return 0;
		}

		LogUtil.i(TAG, "start");
		mAsrOption = oOption;

		mRecordHandler.postDelayed(recordingTask, mSpeechDelay);
		return 0;
	}

	private void startEngine() {
		mWorkHandler.post(new Runnable() {
			@Override
			public void run() {
//				if (bImportSlot) {
//					LogUtil.loge("uvoice engine is importing slots");
//					if (null != mAsrOption && null != mAsrOption.mCallback) {
//						mAsrOption.mCallback.onError(mAsrOption, 0, null, null, IAsr.ERROR_CODE);
//					}
//					return;
//				}
				bNeedEnd = true;
				bStart = true;
				LogUtil.d(TAG, "start engine");
				mEngine.start();
			}
		});
	}

	private void stopEngine() {
		mWorkHandler.post(new Runnable() {
			@Override
			public void run() {
				mEngine.stop();
				bStart = false;
				importSlot();
				attachSlot();
			}
		});
	}

	private final int DATA_BUFFER_SIZE = 1200; //录音buffer的size
	private final int BUFFER_SIZE = 320; //传入引擎的size
	private final byte[] buffer = new byte[BUFFER_SIZE]; //传入引擎的音频
	private final byte[] data_buffer = new byte[DATA_BUFFER_SIZE];
	private QueueBlockingCache mCache; //储存音频
	private int mCacheSize = 20; // mCache的大小，以秒为单位

	/**
	 * 设置buffer大小
	 * @param time - 秒
	 */
	public void setBufferTime(int time){
		mCacheSize = time;
	}

	private final Runnable recordingTask = new Runnable() {
		@Override
		public void run() {
			if (!bInitOk){
				return;
			}
			mCache.enable();
			startEngine();
			mRecording = true;
			mReady = true;
			LogUtil.d(TAG, "start process");
			mWorkHandler.postDelayed(processAudioTask, mSpeechDelay);

			long nVoiceId = 0;
			if(ProjectCfg.getOfflineAsrSaveData()) {
				if (mAsrOption != null) {
					nVoiceId = mAsrOption.mVoiceID;
				}
				if (nVoiceId != 0) {
					mAudioRecorder.beginSaveCache(20 * 16000 * 2);//保存最多20s的录音数据
				}
			}

			long beginSpeechTime = mAsrOption.mBeginSpeechTime;
			LogUtil.logd(TAG + "::mRecording start beginSpeechTime = "
					+ beginSpeechTime);
			if (0 == beginSpeechTime) {
				mAudioRecorder.startRecording();
			} else {
				mAudioRecorder.startRecording(beginSpeechTime);
			}

			while (mRecording && mReady) {
				if (mAudioRecorder != null) {
					int read = mAudioRecorder.read(data_buffer, 0,
							data_buffer.length);
					if (read > 0) {
                        mCache.write(data_buffer, 0, read);
					}
					mRecordHandler.heartbeat();
				} else {
					break;
				}
			}
			byte[] empty = new byte[BUFFER_SIZE];
			mCache.write(empty, 0, BUFFER_SIZE);
			LogUtil.logd(TAG + "::mRecording end");
			mRecording = false;
			mAudioRecorder.stop();

			if(ProjectCfg.getOfflineAsrSaveData()) {
				if (nVoiceId != 0 && Arguments.sIsSaveVoice) {
					RecordData mRecordData = new RecordData();
					mRecordData.uint32SampleRate = RecordFile.SAMPLE_RATE_16K;
					mRecordData.uint32RecordType = UiRecord.RECORD_TYPE_ASR;
					mRecordData.uint64RecordTime = mAsrOption.mServerTime;
					mRecordData.boolRecordTime = mAsrOption.bServerTimeConfidence;
					mRecordData.uint32Uid = mAsrOption.mUID;
					mRecordData.boolRecordTime = true;
					mAudioRecorder.endSaveCache("#" + nVoiceId, mRecordData, Arguments.sIsSaveRawPCM);
				}
			}
		}
	};

	private boolean bProcessAudio = false;
	private final Runnable processAudioTask = new Runnable() {
		@Override
		public void run() {
			bProcessAudio = true;
			while(bProcessAudio){
				mCache.read(buffer, 0, buffer.length, BUFFER_SIZE);
				processAudio(buffer);
				if (!mRecording){
					if (mCache.size()<BUFFER_SIZE){
						break;
					}
				}
				mWorkHandler.heartbeat();
			}
			LogUtil.logd(TAG + "::audio task stop");
			stopEngine();
		}
	};

	private void stopProcessAudio(){
		bProcessAudio = false;
		mCache.interrupt();
		mCache.disable();
	}

	private void processAudio(byte[] buffer){
		if (bProcessAudio){
			mEngine.writeAudio(buffer, 0, buffer.length);
		}
	}

	@Override
	public void stop() {
		if (!bInitOk){
			LogUtil.loge("uvoice engine was not init");
			return;
		}
		if (mReady) {
			mRecording = false;
			LogUtil.logd(TAG + "stop");
//			stopEngine();
			mReady = false;
//			mCache.disable();
		}
	}

	@Override
	public void cancel() {
		if (!bInitOk){
			LogUtil.loge("uvoice engine was not init");
			return;
		}
		LogUtil.logd(TAG + "cancel, mReady=" + mReady);
		if (mReady) {
			mRecording = false;
			mReady = false;
			cancelEngine();
		}
		stopProcessAudio();
		cancelEngine();
	}

	private void cancelEngine() {
		mWorkHandler.post(new Runnable() {
			@Override
			public void run() {
				mEngine.cancel();
				bStart = false;
			}
		});
	}

	@Override
	public boolean isBusy() {
		return false;
	}

	@Override
	public boolean buildGrammar(SdkGrammar oGrammarData,
			IBuildGrammarCallback oCallback) {
		return false;
	}

	private static class SlotType{
		IImportKeywordsCallback keywordsCallback;
		String[] slotName;
		String[] slotPath;
		SdkKeywords oKeywords;
		int type;

		static final int TYPE_TXT = 0;
		static final int TYPE_DAT = 1;

		public SlotType(IImportKeywordsCallback keywordsCallback, String slotName, String slotPath, SdkKeywords oKeywords, int type) {
			this.keywordsCallback = keywordsCallback;
			this.slotName = new String[]{slotName};
			this.slotPath = new String[]{slotPath};
			this.oKeywords = oKeywords;
			this.type = type;
		}

		public SlotType(IImportKeywordsCallback keywordsCallback, String[] slotName, String[] slotPath, SdkKeywords oKeywords, int type) {
			this.keywordsCallback = keywordsCallback;
			this.slotName = slotName;
			this.slotPath = slotPath;
			this.oKeywords = oKeywords;
			this.type = type;
		}
	}
	private final LinkedList<SlotType> mSlots = new LinkedList<SlotType>(); //待插词
	private static final Object sCompiledSlotLock = new Object();
	private final LinkedList<String> mCompiledSlotList = new LinkedList<String>(); //已编译，待插词

	//处理前后slot内容的映射。<slot名字,<处理后的slot内容,原slot内容>>， <cmdKeywords, <温度调到十八度, 温度调到18度>>
	public static HashMap<String, HashMap<String, String>> mSlotMap =
			new HashMap<String, HashMap<String, String>>();
	private static final String MAP_FILE_SUFFIX = ".map";
	private static final String SLOT_MAP_SPLIT_SYMBOL = "=";
	private void saveSlotMap(String slotName) {
		HashMap<String, String> slotMap = mSlotMap.get(slotName);
		if (slotMap == null)
			return;
		try {
			FileWriter writer = new FileWriter(SLOT_DIR + slotName + MAP_FILE_SUFFIX);
			Set<Map.Entry<String, String>> entries = slotMap.entrySet();
			for (Map.Entry<String, String> entry : entries) {
				writer.write(entry.getKey() + SLOT_MAP_SPLIT_SYMBOL + entry.getValue() + "\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadSlotMap() {
		File dir = new File(SLOT_DIR);
		if (!dir.exists())
			return;
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(MAP_FILE_SUFFIX);
			}
		});
		if (files == null)
			return;
		for (File file : files) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line;
				HashMap<String, String> slotMap = new HashMap<String, String>();
				while((line = reader.readLine()) != null) {
					if (TextUtils.isEmpty(line))
						continue;
					String[] split = line.split(SLOT_MAP_SPLIT_SYMBOL);
					if (split.length != 2)
						continue;
					slotMap.put(split[0], split[1]);
				}
				reader.close();
				String fileName = file.getName();
				mSlotMap.put(fileName.substring(0, fileName.length() - MAP_FILE_SUFFIX.length()), slotMap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ImportKeywordsRunnable extends Runnable2<SdkKeywords, IImportKeywordsCallback> {
		String type;
		String[] aContents;

		class SlotData{
			public SlotData(HashMap<String, String> slotMap, String filePath, String type) {
				this.slotMap = slotMap;
				this.filePath = filePath;
				this.type = type;
			}

			HashMap<String, String> slotMap;
			String filePath, type;
		}

		public ImportKeywordsRunnable(SdkKeywords p1, IImportKeywordsCallback p2) {
			super(p1, p2);
		}

		@Override
		public void run() {
			int error = 0;
			do {
				String key;
				SdkKeywords oKeywords = mP1;
				LogUtil.d(TAG, "import keywords, strType=" + oKeywords.strType
						+ "length=" + oKeywords.rptStrKw.length);

				if (oKeywords.strType.startsWith("<")) {
					key = oKeywords.strType.substring(1,
							oKeywords.strType.length() - 1);
					type = key;
				} else {
					break;// type不是正确的类型
				}

				LogUtil.d(TAG, "type="+type);

				aContents = oKeywords.rptStrKw;
				if (aContents == null || aContents.length == 0) {
					LogUtil.w(TAG, "the rptStrKw is empty");
					break;
				}

				//判断slot文件的目录是否存在，不存在就创建一个
				File slotDir = new File(SLOT_DIR);
				if (!slotDir.exists()) {
					slotDir.mkdir();
				}

				if ("cmdKeywords".equals(type)) {
					// 声瀚最多支持插500个词，所以对于命令字这种槽位较多需要特殊处理，命令字的槽位分为十个，
					// 每个槽位插500个，如果不够，就全部放在最后一个槽位中
					String[] slotNameList = new String[] {"cmdKeywords1", "cmdKeywords2", "cmdKeywords3", "cmdKeywords4", "cmdKeywords5", "cmdKeywords6", "cmdKeywords7", "cmdKeywords8", "cmdKeywords9", "cmdKeywords10"};
					int slotIndex = 0;
					int slotNum = 0, maxSlotNum = 500;
					FileWriter writer;
					String filePath = SLOT_DIR + slotNameList[0] + ".txt";
					ArrayList<SlotData> successSlotList = new ArrayList<SlotData>(slotNameList.length - 1);
					try {
						writer = new FileWriter(filePath);
					} catch (IOException e) {
						e.printStackTrace();
						LogUtil.e(TAG, "importKeywords: create writer [ " + filePath + " ] failed:" + e.toString());
						mP2.onError(-1, mP1);
						return;
					}
					HashMap<String, String> slotMap = new HashMap<String, String>();
					for (String aContent : aContents) {
						if (aContent == null
								|| aContent.trim().isEmpty()
								|| aContent.equals("<unknown>")) {
							LogUtil.w(TAG, aContent + " is not available");
							continue;
						}

						if (slotNum >= maxSlotNum // 已写入500个词，换下一个槽位
								&& slotIndex <= slotNameList.length) { // 没有多的槽位了，就使用最后一个
							//保存结果
							successSlotList.add(new SlotData(slotMap, filePath, slotNameList[slotIndex]));

							//更新值
							slotIndex++;
							filePath = SLOT_DIR + slotNameList[slotIndex] + ".txt";
							try {
								writer.close();
								writer = new FileWriter(filePath);
							} catch (IOException e) {
								e.printStackTrace();
								LogUtil.e(TAG, "importKeywords: create writer [ " + filePath + " ] failed:" + e.toString());
								mP2.onError(-1, mP1);
								return;
							}

							//重置值
							slotNum = 0;
							slotMap = new HashMap<String, String>();
						}

						try {
//									LogUtil.d(TAG, "content = " + aContents[i]);
							String processedString = processNumSlot(aContent);
							if (!TextUtils.equals(processedString, aContent)) {
								writer.write(processedString + "\n");
								slotMap.put(processedString, aContent);
							}
							writer.write(aContent + "\n");
							writer.flush();
							slotNum++;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					successSlotList.add(new SlotData(slotMap, filePath, slotNameList[slotIndex])); //添加最后一个

					String[] slotNames = new String[successSlotList.size()];
					String[] slotPaths = new String[successSlotList.size()];
					for (int i = 0; i < successSlotList.size(); i++) {
						SlotData slotData = successSlotList.get(i);
						slotNames[i] = "txz#" + slotData.type;
						slotPaths[i] = slotData.filePath;
						mSlotMap.put(slotData.type, slotData.slotMap);
						saveSlotMap(slotData.type);
					}

					SlotType slot = new SlotType(mP2, slotNames, slotPaths, mP1, SlotType.TYPE_TXT);
					mSlots.add(slot);
					importSlot();
				} else {
					File file = new File(SLOT_DIR + type + ".txt");
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
					FileWriter writer = null;
					try {
						writer = new FileWriter(file);
					} catch (IOException e) {
						e.printStackTrace();
						LogUtil.e(TAG, "importKeywords: create writer [ " + file.getAbsolutePath() + " ] failed:" + e.toString());
						mP2.onError(-1, mP1);
						return;
					}

					HashMap<String, String> slotMap = new HashMap<String, String>();

					for (String aContent : aContents) {
						if (aContent == null
								|| aContent.trim().isEmpty()
								|| aContent.equals("<unknown>")) {
							LogUtil.w(TAG, aContent + " is not available");
							continue;
						}
						try {
//									LogUtil.d(TAG, "content = " + aContents[i]);
							if (type.equals("fmFreqValue")) {
								String[] nums = aContent.split("点");
								String chineseContent = ExchangeHelper.numberToChinese(Integer.parseInt(nums[0]));
								if (nums.length == 2) {
									chineseContent += "点" + nums[1];
								}
								writer.write(chineseContent + "\n");
								writer.write(aContent + "\n");
								slotMap.put(chineseContent, aContent);
							} else if (type.equals("amValue")) {
								String chineseContent = ExchangeHelper.numberToChinese(Integer.parseInt(aContent));
								writer.write(chineseContent + "\n");
								writer.write(aContent + "\n");
								slotMap.put(chineseContent, aContent);
							} else {
								String processedString = processNumSlot(aContent);
								if (!TextUtils.equals(processedString, aContent)) {
									writer.write(processedString + "\n");
									slotMap.put(processedString, aContent);
								}
								writer.write(aContent + "\n");
							}
							writer.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					//添加slot
					enQueueSlot(type, slotMap, file.getPath());
				}
				return;
			} while(false);

			if (error != 0) {
				if (mP2 != null) {
					mP2.onError(-1, mP1);
				}
			} else {
				if (mP2 != null) {
					mP2.onSuccess(mP1);
				}
			}
		}

		/**
		 * 将slot添加到slot列表中
		 * @param slotMap slot内容的映射，有的包含了数字的词，经过了特殊处理，在解析时，需要还原
		 * @param filePath 待编译slot文件的绝对路径
		 */
		private void enQueueSlot(String type, HashMap<String, String> slotMap, String filePath) {
			SlotType slot = new SlotType(mP2, "txz#"+type, filePath, mP1, SlotType.TYPE_TXT);
			mSlotMap.put(type, slotMap);
			saveSlotMap(type);
			mSlots.add(slot);
			importSlot();
		}
	}

	@Override
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		if (!bInitOk){
			LogUtil.loge("uvoice engine was not init");
			oCallback.onError(-1, oKeywords);
			return true;
		}
		mSlotHandler.post(new ImportKeywordsRunnable(oKeywords, oCallback));

		return true;
	}

	/**
	 * 处理二位数的阿拉伯数字
	 * 因为12说十二不能识别，只有说一二才行
	 */
	private String processNumSlot(String text) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c > '9' || c < '0') {
				sb.append(c);
				continue;
			}

			//前一个不是数字，后一个是数字，后两个不是数字
			if (i == text.length() - 1) { //最后一位
				sb.append(c);
				continue;
			}
			if (i > 0) {
				char before = text.charAt(i - 1);
				if (before >= '0' && before <= '9') {
					sb.append(c);
					continue;
				}
			}
			char after = text.charAt(i + 1);
			if (after < '0' || after > '9') {
				sb.append(c);
				continue;
			}
			if (i < text.length() - 2) {
				char after2 = text.charAt(i + 2);
				if (after2 >= '0' && after2 <= '9') {
					sb.append(c);
					continue;
				}
			}

			String nums = c + Character.toString(after);
			nums = processNum(nums);
			sb.append(nums);
			i++;
		}

		return sb.toString();
	}

	private String processNum(String sNum) {
		int num = Integer.parseInt(sNum);
		if (num < 10 || num > 19)
			return ExchangeHelper.numberToChinese(num);
		return ExchangeHelper.numberToChinese(num).substring(1); //去掉一字
	}

	/**
	 * 插词需要放在引擎的工作线程，因为在识别的时候，不能插词
	 */
	private final Runnable mImportSlotRunnable = new Runnable() {
		@Override
		public void run() {
			if (mSlots.isEmpty()){
				return;
			}

			if (bStart) {
				LogUtil.w("audio is processing, stop import slot");
				return;
			}

			SlotType slot = mSlots.removeFirst();
			LogUtil.d(TAG, "add slot, name=" + Arrays.toString(slot.slotName) + ", path="
					+ Arrays.toString(slot.slotPath) + ", remain:" + mSlots.size());
			Boolean success = null;
			if (slot.type == SlotType.TYPE_TXT) {
				// 因为目前没有单独的编译接口，是使用addDecoder的方式编译的，在add后保存中间文件当作编译好的
				// 模型文件所以需要先判断是否attach过这个slot，如果attach过，需要先在引擎的工作线程删除这个slot
				synchronized (sCompiledSlotLock) {
					for (int i = 0; i < slot.slotName.length; i++) {
						String slotName = slot.slotName[i];
						if (attachedSlotList.contains(slotName)) {
							boolean ret = mEngine.delSlot(slotName);
							attachedSlotList.remove(slotName);
							if (!ret) {
								LogUtil.e(TAG, "delSlot failed:" + slotName);
							}
						}
					}
				}
				compileSlot(slot);
			} else {
				for (int i = 0; i < slot.slotName.length; i++) {
					String slotName = slot.slotName[i];
					String slotPath = slot.slotPath[i];
					synchronized (sCompiledSlotLock) {
						attachedSlotList.add(slotName);
						success = mEngine.addDatSlot(slotName, slotPath);
					}
					if (!success) {
						LogUtil.e(TAG, "addDatSlot failed:[" + slotName + ":" + slotPath + ":" + success + "]");
						break;
					}
				}
			}
			if (success != null) {
				if (success) {
					if (slot.keywordsCallback != null) {
						slot.keywordsCallback.onSuccess(slot.oKeywords);
					}
				} else {
					if (slot.keywordsCallback != null) {
						slot.keywordsCallback.onError(-1, slot.oKeywords);
					}
				}
			}
			attachSlot();
			importSlot();
		}
	};

	private void importSlot(){
		mWorkHandler.removeCallbacks(mImportSlotRunnable);
		mWorkHandler.post(mImportSlotRunnable);
	}

	private void compileSlot(SlotType slot) {
		mSlotHandler.post(new Runnable1<SlotType>(slot) {
			@Override
			public void run() {
				initFst(); //未初始化fst会执行，如果已经初始化了，就不会执行
				//如果已attach过，就不编译，调用importSlot，先在工作线程将这个slot移除掉
				synchronized (sCompiledSlotLock) {
					for (String slotName : mP1.slotName) {
						if (attachedSlotList.contains(slotName)) {
							mSlots.addLast(mP1);
							importSlot();
							return;
						}
					}
					// 这里需要移除掉需要重新编译的词，防止之前编译过这个词，在释放锁后，attach方法获取锁后
					// 马上attach这个slot，后面的编译（compileSlot方法）会发生异常
					for (String slotName : mP1.slotName) {
						mCompiledSlotList.remove(slotName);
					}
				}
				for (int i = 0; i < mP1.slotName.length; i++) {
					String slotName = mP1.slotName[i];
					String slotPath = mP1.slotPath[i];
					boolean success = mEngine.compileSlot(slotName, slotPath);
					if (!success) {
						LogUtil.e(TAG, "compileSlot: failed, slotName=" + slotName + ", slotPath=" + slotPath);
					}
					if (success) { //编译成功后，加入
						synchronized (sCompiledSlotLock) {
							mCompiledSlotList.remove(slotName);
							mCompiledSlotList.addLast(slotName);
							if (mP1.type == SlotType.TYPE_TXT) {
								FileUtil.copyFile(WORK_PATH + "/list.dat", SLOT_DIR + "/" + slotName + ".dat");
							}
						}
					} else {
						if (mP1.keywordsCallback != null)
							mP1.keywordsCallback.onError(-1, mP1.oKeywords);
						return;
					}
					mSlotHandler.heartbeat();
				}
				if (mP1.keywordsCallback != null)
					mP1.keywordsCallback.onSuccess(mP1.oKeywords);
			}
		});
	}

	private final List<String> attachedSlotList = new ArrayList<String>();
	private void attachSlot() { //引擎工作线程
		if (bStart) {
			LogUtil.w("audio is processing, stop attach slot");
			return;
		}
		synchronized (sCompiledSlotLock) {
			if (mCompiledSlotList.isEmpty())
				return;
			while (!mCompiledSlotList.isEmpty()) {
				String slotName = mCompiledSlotList.removeFirst();
				boolean ret;
				if (attachedSlotList.contains(slotName)) {
					ret = mEngine.delSlot(slotName);
					if (!ret) {
						LogUtil.e(TAG, "delSlot failed:" + slotName);
					}
					mEngine.addDatSlot(slotName, SLOT_DIR + slotName + ".dat");
				}
				ret = mEngine.attachSlot(slotName);
				attachedSlotList.add(slotName);
				LogUtil.d(TAG, "attachSlot: slotName=" + slotName + ", ret=" + ret);
			}
		}
	}

	@Override
	public void releaseBuildGrammarData() {
		
	}

	@Override
	public void retryImportOnlineKeywords() {
		
	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
		// TODO Auto-generated method stub

	}
	
	protected void parseResult(String jsonResult) {
		if (TextUtils.isEmpty(jsonResult)) {
			return;
		}
		VoiceData.VoiceParseData oVoiceParseData = new VoiceData.VoiceParseData();
		oVoiceParseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_UVOICE_LOCAL_JSON;
		oVoiceParseData.strVoiceData = jsonResult;
		oVoiceParseData.uint32Sence = mAsrOption.mGrammar;
		oVoiceParseData.floatResultScore = getScore(jsonResult);
		LogUtil.d("oVoiceParseData.floatResultScore="+oVoiceParseData.floatResultScore);
		AsrOption oOption = mAsrOption;
		if (oOption != null) {
			if (oOption.mManual) {
				oVoiceParseData.boolManual = 1;
			} else {
				oVoiceParseData.boolManual = 0;
			}
			oVoiceParseData.uint32Sence = oOption.mGrammar;
			if (!oOption.mNeedStopWakeup) {
				oVoiceParseData.uint32AsrWakeupType = VoiceData.VOICE_ASR_WAKEUP_TYPE_MIX;
			}
		}
		mAsrOption.mCallback.onSuccess(mAsrOption, oVoiceParseData);
	}
	
	public static VoiceParseData uvoiceDataToTxzScene(VoiceParseData parseData) {
		if (parseData == null) {
			return parseData;
		}
		if (TextUtils.isEmpty(parseData.strText)) {
			parseData.strText = "";
		}
		VoiceParseData newData = new VoiceParseData();
		try {
			newData = VoiceParseData.parseFrom(VoiceParseData
					.toByteArray(parseData));
		} catch (InvalidProtocolBufferNanoException e) {
			LogUtil.loge(TAG + "uvoice VoiceParseData parse error = "
					+ e.getMessage());
		}
		UVoiceLocalJsonConver conver = new UVoiceLocalJsonConver(parseData);
		newData.strVoiceData = conver.getJson();
		newData.floatTextScore = conver.getScore();
		newData.strText = conver.getRawText();
		newData.uint32TextMask = conver.getTextMask();
		LogUtil.logd(TAG + "newData strVoiceData = " + newData.strVoiceData
				+ " floatTextScore = " + newData.floatTextScore + " strText = "
				+ newData.strText + ", uint32TextMask = " + newData.uint32TextMask);
		return newData;
	}


	private float getScore(String text){
		float score = UVoiceLocalJsonConver.CONFIG_MID;
		do {
			JSONObject json = null;
			try {
				json = JSONObject.parseObject(text);
			} catch (Exception e) {
				break;
			}
			JSONArray hypotheses = null;
			if (json.containsKey("_hypotheses")) {
				hypotheses = json.getJSONArray("_hypotheses");
			} else {
				break;
			}
			if (null==hypotheses||hypotheses.isEmpty()) {
				break;
			}
			int conf = getMaxScore(hypotheses);
			if (conf >= UVoiceLocalJsonConver.CONF_HIGH_SCORE){
				score = UVoiceLocalJsonConver.CONFIG_HIGH;
			} else {
				score = UVoiceLocalJsonConver.CONFIG_MID;
			}
		}while(false);
		return score;
	}

	private int getMaxScore(JSONArray jArray) {
		int score = 0;
		String modelName;
		JSONObject model = null;
		if (jArray == null){
			return score;
		}
		for (int i = 0; i < jArray.size(); i++) {
			int num = 0;
			try {
				model = jArray.getJSONObject(i);
				if (null == model){
					continue;
				}
				modelName = model.getString("_decoder");

				if (POI_NAME.equals(modelName)){
					continue;
				}

				JSONArray items = model.getJSONArray("_items");
				if (items.size() < 3){
					continue;
				}
				JSONObject lastWord = items.getJSONObject(items.size() - 1);
				if (null == lastWord || !lastWord.containsKey("_orthography")){
					continue;
				}
				String endSymbol = lastWord.getString("_orthography");
				if (!"</s>".equals(endSymbol)){
					continue;
				}
				num = model.getIntValue("_conf");

			} catch (Exception e){
				continue;
			}
			if (num > score) {
				score = num;
			}
		}
		return score;
	}

	private boolean isSameFile(String file1, String file2){
		boolean bRet = false;
		do{
			try{
				File f1 = new File(file1);
				File f2 = new File(file2);
				if (!f1.exists()||!f2.exists()){
					break;
				}

				if (f1.length()!=f2.length()){
					break;
				}

				StringBuilder sb1 = new StringBuilder();
				StringBuilder sb2 = new StringBuilder();
				String s = "";
				BufferedReader bReader1 = new BufferedReader(new FileReader(f1));
				BufferedReader bReader2 = new BufferedReader(new FileReader(f2));
				while ((s =bReader1.readLine()) != null) {
					sb1.append(s);
				}
				while ((s =bReader2.readLine()) != null) {
					sb2.append(s);
				}
				if (TextUtils.equals(sb1.toString(), sb2.toString())){
					bRet = true;
				}
				try {
					bReader1.close();
				} catch(Exception e){}
				bReader2.close();
			} catch (Exception e){
			}
		}while(false);
		return bRet;
	}

	private void saveJsonStr(String json){
		if (!DebugCfg.ENABLE_LOG) {
			return;
		}
		File file = new File("/sdcard/txz/uvoice/result.txt");
		try {
			String dir = file.getParent();
			File d = new File(dir);
			if (!d.exists()){
				d.mkdirs();
			}
			file.createNewFile();
			FileWriter write = new FileWriter(file);
			write.write(json);
			write.flush();
			write.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
