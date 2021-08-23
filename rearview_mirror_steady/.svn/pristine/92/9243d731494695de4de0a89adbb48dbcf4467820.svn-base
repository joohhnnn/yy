package com.txznet.txz.component.asr.mix.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.HashMap;

import android.os.SystemClock;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.record.UiRecord;
import com.txz.ui.record.UiRecord.RecordData;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.mix.Arguments;
import com.txznet.txz.component.asr.mix.AsrCallbackFactory;
import com.txznet.txz.component.asr.mix.IAsrCallBackProxy;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;

public class NetAsrBaiduImpl implements IAsr{
	public static final String MONITOR_INFO = "asr.baidu.I.";
	public static final String MONITOR_ERROR = "asr.baidu.E.";
	public static final String MONITOR_WARNING = "asr.baidu.W.";
	private static final int STATUS_IDLE = 0;
	private static final int STATUS_BUSY = 1;
	private boolean mInitOk = false;
	private int mStatus = STATUS_IDLE;
	private IAsrCallBackProxy mAsrCallBackProxy = null;
	private AsrOption mAsrOption = null;
	protected boolean mRecording;
	protected boolean mReady;
	private static Config mConfig = null;
	private AsrCallBackListener mAsrCallBackListener;
    private EventManager asr;
	private HashMap<String, Object> config;

	private void fixResult(String result) {
		mStatus = STATUS_IDLE;
		mRecording = false;
		LogUtil.logd("Asr:Result="+result);
		JSONObject object = JSONObject.parseObject(result);
		if(!object.containsKey("error")) {
			mAsrCallBackProxy.onError(IAsr.ERROR_ASR_NET_NLU_EMTPY);
			return;
		}
		String resultText;
		int error = object.getIntValue("error");
		LogUtil.logd("Asr:Error="+error);
		if (error != 0) {
			switch (error) {
			case 1://网络超时
			case 2://网络错误
				mAsrCallBackProxy.onError(IAsr.ERROR_ASR_NET_REQUEST);
				mAsrCallBackProxy.onMonitor(MONITOR_ERROR + MONITOR_NO_REQUEST);
				break;
			case 3://录音错误
			case 6://前端超时 默认8S
				mAsrCallBackProxy.onError(IAsr.ERROR_NO_SPEECH);
				mAsrCallBackProxy.onMonitor(MONITOR_ERROR + MONITOR_NO_SPEECH);
				break;
			case 4://服务端错误
			case 7://没有识别结果
				mAsrCallBackProxy.onError(IAsr.ERROR_CODE);
				mAsrCallBackProxy.onMonitor(MONITOR_ERROR + MONITOR_NO_MATCH);
				break;
			case 8://引擎忙
				mAsrCallBackProxy.onError(IAsr.ERROR_ASR_ISBUSY);
				mAsrCallBackProxy.onMonitor(MONITOR_ERROR + error);
				break;
			case 5://客户端调用错误
			case 9://缺少权限
			default:
				mAsrCallBackProxy.onError(IAsr.ERROR_CODE);
				mAsrCallBackProxy.onMonitor(MONITOR_ERROR + error);
				break;
			}
			return ;
		}
		if (object.containsKey("results_recognition")) {
			JSONArray results = object.getJSONArray("results_recognition");
			resultText = results.getString(0);
		}
		else {
			resultText = "";
		}
		
		VoiceData.VoiceParseData oVoiceParseData = new VoiceData.VoiceParseData();
		oVoiceParseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_BAIDU_SCENE_JSON;
		oVoiceParseData.strVoiceData = object.getString("origin_result");
		oVoiceParseData.strText = resultText;
		oVoiceParseData.uint32Sence = mAsrOption.mGrammar;
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
		LogUtil.logd("Asr:text=" + oVoiceParseData.strText);
		mAsrCallBackProxy.onSuccess(oVoiceParseData);
	}	
	class AsrCallBackListener implements EventListener {
	    private long sReady = 0;
	    private long sEnd = 0;
	    private long sBegin = 0;
		@Override
		public void onEvent(String name, String params, byte[] data, int offset,
				int length) {
			if ("asr.volume".equals(name)) {
				JSONObject object = JSONObject.parseObject(params);
				if (object.containsKey("volume-percent")) {
					mAsrCallBackProxy.onVolume(object.getIntValue("volume-percent"));
				}
			}
			else if ("asr.ready".equals(name)) {
				LogUtil.logd("Asr:ready");
				sReady = SystemClock.elapsedRealtime();
			}
			else if ("asr.enter".equals(name)) {
				LogUtil.logd("Asr:enter");
			}
			else if ("asr.begin".equals(name)) {
				mAsrCallBackProxy.onBeginOfSpeech();
				sBegin = SystemClock.elapsedRealtime();
				LogUtil.logd("Asr:speachBegin,timeCast:"+(sBegin - sReady));
			}
			else if ("asr.partial".equals(name)) {
				LogUtil.logd("Asr:params="+params);
			}
			else if ("asr.end".equals(name)) {
				mAsrCallBackProxy.onEndOfSpeech();
				mRecording = false;
				sEnd = SystemClock.elapsedRealtime();
				LogUtil.logd("Asr:speachEnd,timeCast:"+(sEnd - sBegin));
			}
			else if ("asr.finish".equals(name)) {
				LogUtil.logd("Asr:finish,timeCast:"+(SystemClock.elapsedRealtime() - sEnd));
				fixResult(params);
			}
		}
	}
	@Override
	public int initialize(IInitCallback oRun) {
		if (oRun == null)
			init();
		else
			oRun.onInit(init());
		return 0;
	}
	
	private boolean init() {
		
		asr = EventManagerFactory.create(GlobalContext.get(), "asr","2.1");
		LogUtil.logd("Asr:init");
		mAsrCallBackListener = new AsrCallBackListener();
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				asr.registerListener(mAsrCallBackListener);
			}
		}, 0);
		
		config = new HashMap<String, Object>();
		config.put("vad", "model-vad");
		config.put("decoder", 0);
		config.put("sample", 16000);
//		config.put("language", "cmn-Hans-CN");
		config.put("nlu", "enable");
		config.put("pid", "810");
		config.put("key","19gMPEMvU74DWaXkMVpTM5bByGahlWmQ" );
		config.put("secret","zF6m3nonwFfmGR1bStwt5LQNSCEbHdXL" );
		String resFile = GlobalContext.get().getApplicationInfo().dataDir + "/solibs/libbd_easr_s1_merge_normal_20151216.dat.so";
		config.put("res-file", resFile);
		config.put("decoder-server.ptc", 306);
		config.put("url", "http://vse.baidu.com/echo.fcgi");
		config.put("license", "asset:///license-android-easr_txznet.txt");
//		config.put("outfile", Environment.getExternalStorageDirectory().getPath() + "/txz/file.pcm");
//		config.put("disable-punctuation", true);不能加，加了之后没有语义返回
//		config.setResourceType(VoiceRecognitionConfig.RESOURCE_TYPE_WISE);
		
		mAsrCallBackProxy = AsrCallbackFactory.proxy();
		
		
//		mWorkThread = new HandlerThread("baidu_record_thread");
//		mWorkThread.start();
//		mWorkHandler = new Handler(mWorkThread.getLooper());
		mConfig = new Config();
		mConfig.setSkipBytes(6400);

		LogUtil.logd("Asr:mInitOk");
		mInitOk = true;
		return true;
	}

	@Override
	public int start(AsrOption oOption) {
		if (!mInitOk) {
			init();
		}
		mAsrOption = oOption;
		mAsrCallBackProxy.setAsrOption(mAsrOption);
		mAsrCallBackProxy.onMonitor(MONITOR_INFO + MONITOR_ALL);
		
		mConfig.setBeginSpeechTime(oOption.mBeginSpeechTime);
		String strSaveDataPath = null;
		if (oOption.mVoiceID != 0){
			strSaveDataPath = oOption.mVoiceID+"";
		}
		mConfig.setmUID(oOption.mUID);
		mConfig.setmServerTime(oOption.mServerTime);
		mConfig.setbServerTimeConfidence(oOption.bServerTimeConfidence);
		mConfig.setSaveDataPath(strSaveDataPath);
		if (mAsrOption.mPlayBeepSound) {
			mConfig.setSkipBytes(6400);
		}else {
			mConfig.setSkipBytes(0);
		}
//    	resetInputStream();
		config.put("infile", "#com.txznet.txz.component.asr.mix.net.NetAsrBaiduImpl.getExtSource()");
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				asr.send("asr.start", new JSONObject(config).toString(), null, 0, 0);
			}
		}, 0);
//		mWorkHandler.postDelayed(recordingTask, 0);
		LogUtil.logd("Asr:start");
		mStatus = STATUS_BUSY;
		return 0;
	}

	@Override
	public void stop() {
		mStatus = STATUS_IDLE;
		if (mReady) {
			mRecording = false;
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					asr.send("asr.stop",null, null, 0, 0);
				}
			}, 0);
		}
		LogUtil.logd("Asr:stop");
	}

	@Override
	public void cancel() {
		mStatus = STATUS_IDLE;
		if (mReady) {
			mRecording = false;
			mReady = false;
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					asr.send("asr.cancel",null, null, 0, 0);
				}
			}, 0);
		}
		LogUtil.logd("Asr:cancel");
	}
	
	@Override
	public void release() {
		mInitOk = false;
		mStatus = STATUS_IDLE;
		LogUtil.logd("Asr:release");
	}

	@Override
	public boolean isBusy() {
		return mStatus == STATUS_BUSY;
	}

	@Override
	public boolean buildGrammar(SdkGrammar oGrammarData,
			IBuildGrammarCallback oCallback) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		LogUtil.logw("importKeywords bigen");
		// 没有这个接口也需要实现回调方法
		if (null != oCallback) {
			oCallback.onSuccess(oKeywords);
		}
		return false;
	}

	@Override
	public void releaseBuildGrammarData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void retryImportOnlineKeywords() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
		// TODO Auto-generated method stub
		
	}
	public static VoiceParseData baiduDataToTxzScene(VoiceParseData parseData) {

		VoiceParseData newData = new VoiceParseData();
		try {
			newData = VoiceParseData.parseFrom(VoiceParseData
					.toByteArray(parseData));
		} catch (InvalidProtocolBufferNanoException e) {
			e.printStackTrace();
		}
		newData.floatTextScore = 0f;
		NetTextBaiduParseImpl parse = new NetTextBaiduParseImpl();
		parse.parseData(newData);
		return newData;
	}
	
//	private void resetInputStream() {
//	    if (mRecordInputStream != null) {
//	        mRecordInputStream.close();
//	    }
//	    mRecordInputStream = new RecordInputStream();
//	}
	
	public static InputStream getExtSource() {
		LogUtil.logd("Asr:create InputStream");
		InputStream in = null;
		try {
			in = new TXZInputStream(mConfig);
		} catch (IOException e) {
		}
		return in;
	}
	
	public static class TXZInputStream extends InputStream{
	    private TXZAudioRecorder mAudioRecorder = null;
	    private int nSkipedBytes = 0;
		
		public TXZInputStream(Config config) throws IOException {
			if (config != null) {
				mConfig = config;
			} else {
				mConfig = new Config(true);
			}
			boolean bUsePreProcessedData = ProjectCfg.mEnableAEC && ProjectCfg.isUseSePreprocessedData();
			LogUtil.logd("asr:bUsePreProcessedData:" + bUsePreProcessedData);
			mAudioRecorder = new TXZAudioRecorder(bUsePreProcessedData);
			mAudioRecorder.startRecording();
			if(!TextUtils.isEmpty(mConfig.mSaveRecordDataPath)){
				mAudioRecorder.beginSaveCache(20*16000*2);
			}
			
			if (mConfig.mBeginSpeechTime <= 0) {
				nSkipedBytes = 0;
				mAudioRecorder.startRecording();
			} else {
				nSkipedBytes = mConfig.mNeedSkipBytes;
				mAudioRecorder.startRecording(mConfig.mBeginSpeechTime);
			}
		}

		public int read() throws IOException {
			throw new UnsupportedOperationException();
		}

		public int read(byte[] buffer) throws IOException {
			return read(buffer, 0, buffer.length);
		}

		public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
			int nRet = mAudioRecorder.read(buffer, byteOffset, byteCount);
			if (nRet > 0) {
				// 不停唤醒识别引擎,但是塞静音给引擎,规避不断重启唤醒，不能够马上唤醒的问题
				if (nSkipedBytes < mConfig.mNeedSkipBytes) {// 跳过指令字节的录音。主要是避免嘀的一声被录进去。
					int leftNeedSkipBytes = mConfig.mNeedSkipBytes - nSkipedBytes;
					if (leftNeedSkipBytes >= nRet) {
						Config.fillEmptyData(buffer, byteOffset, byteCount);
						nSkipedBytes += nRet;
						return 0;
					} else {
						System.arraycopy(buffer, leftNeedSkipBytes, buffer, byteOffset, nRet - leftNeedSkipBytes);
						nSkipedBytes += leftNeedSkipBytes;
						return nRet - leftNeedSkipBytes;
					}
				}
			}
			return nRet;
		}
	    
		public void close() throws IOException {
			mAudioRecorder.stop();
			if(!TextUtils.isEmpty(mConfig.mSaveRecordDataPath) && Arguments.sIsSaveVoice){
				RecordData mRecordData = new RecordData();
				mRecordData.uint32RecordType = UiRecord.RECORD_TYPE_ASR;
				mRecordData.uint32SampleRate = RecordFile.SAMPLE_RATE_16K;
				mRecordData.uint32Uid = mConfig.mUID;
				mRecordData.uint64RecordTime = mConfig.mServerTime;
				mRecordData.boolRecordTime = true;
				mAudioRecorder.endSaveCache(mConfig.mSaveRecordDataPath, mRecordData, Arguments.sIsSaveRawPCM);
			}
			mAudioRecorder.release();
			super.close();
		}
	}
	
	public static class Config {
		public final static int DEFAULT_SKIP_BYTES = 0;
		private int mNeedSkipBytes = DEFAULT_SKIP_BYTES;
		private boolean bEnable = true;
		private long mBeginSpeechTime = -1;
		private String mSaveRecordDataPath = null;
		private Integer mUID;
		private Long mServerTime;
		private Boolean bServerTimeConfidence = false;
		
		public Config() {
			bEnable = true;
		}

		public Config(boolean enable) {
			bEnable = enable;
		}

		public void enable(boolean enable) {
			LogUtil.logd("TXZAudioSource enable : " + enable);
			bEnable = enable;
		}

		public boolean voiceEnable() {
			return bEnable;
		}

		public void setSkipBytes(int bytes) {
			mNeedSkipBytes = bytes;
		}

		public void setBeginSpeechTime(long time) {
			mBeginSpeechTime = time;
		}

		public void setSaveDataPath(String strPath){
			mSaveRecordDataPath = strPath;
		}
		
		public void setmUID(Integer mUID) {
			this.mUID = mUID;
		}

		public void setmServerTime(Long mServerTime) {
			this.mServerTime = mServerTime;
		}
		
		public void setbServerTimeConfidence(Boolean bConfidence){
			this.bServerTimeConfidence = bConfidence;
		}

		public static void fillEmptyData(byte[] data, int offset, int len) {
			Arrays.fill(data, offset, len, (byte) 0);
		}
	}
	
	public class RecordInputStream extends InputStream {

	    private PipedInputStream mPipedInputStream;
	    private PipedOutputStream mPipedOutputStream;

	    private boolean mClosed = false;

	    public RecordInputStream() {
	        mPipedInputStream = new PipedInputStream();
	        try {
	            mPipedOutputStream = new PipedOutputStream(mPipedInputStream);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    public int feedAudioBuffer(byte[] buffer, int offset, int size) {
	        if (buffer == null || mPipedOutputStream == null) {
	            return -1;
	        }
	        try {
	            mPipedOutputStream.write(buffer, offset, size);
	        } catch (IOException e) {
	            return -1;
	        }

	        return size;
	    }

	    @Override
	    public int read() throws IOException {
	        if (mClosed || mPipedInputStream == null) {
	            return -1;
	        } else {
	            return mPipedInputStream.read();
	        }
	    }
	    
	    @Override
	    public void close() {
	        mClosed = true;
	        if (mPipedInputStream != null) {
	            try {
	                mPipedInputStream.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }

	        if (mPipedOutputStream != null) {
	            try {
	                mPipedOutputStream.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }

	    public void closeOutputStream() {
	        if (mPipedOutputStream != null) {
	            try {
	                mPipedOutputStream.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }

	    public boolean isClosed() {
	        return this.mClosed;
	    }
	}
}
