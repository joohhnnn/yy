package com.txznet.txz.component.asr.mix;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.service.ReserveService0;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;

public class SuperEngineProxy  extends SuperEngineBase{
	private HandlerThread mWorkThread = null;
	private Handler mHandler= null;
    private Messenger mService = null;
    private Messenger mMessenger = null;
    private boolean mAsrStarting = false;
    private long mAsrStartTime = 0;
    private static SuperEngineProxy sProxy = null;
    private static SuperEngineProxy sLocalProxy = null;
    private boolean mLocaled = false;
    private final static int CONNECT_TIMEOUT = 5000;
    
    public static SuperEngineProxy getProxy(){
    	if (sProxy == null){
    		synchronized (SuperEngineProxy.class) {
				if (sProxy == null){
					sProxy = new SuperEngineProxy();
				}
			}
    	}
    	return sProxy;
    };
    
    public static SuperEngineProxy getProxy(boolean bLocal){
    	if (sLocalProxy == null){
    		synchronized (SuperEngineProxy.class) {
				if (sLocalProxy == null){
					sLocalProxy = new SuperEngineProxy(bLocal);
				}
			}
    	}
    	return sLocalProxy;
    };
    
    private AsrServer mAsrServer = null;
    private SuperEngineProxy(){
    	mWorkThread = new HandlerThread("AsrProxy");
    	mWorkThread.start();
    	mHandler = new Handler(mWorkThread.getLooper()){
    		@Override
    		public void handleMessage(Message msg) {
    		      handleMsg(msg);
    		}
    	};
    	mMessenger = new Messenger(mHandler);
    	addConnectTask();
    }
    
    private SuperEngineProxy(boolean bLocal){
    	mWorkThread = new HandlerThread("AsrProxy");
    	mWorkThread.start();
    	mHandler = new Handler(mWorkThread.getLooper()){
    		@Override
    		public void handleMessage(Message msg) {
    		      handleMsg(msg);
    		}
    	};
    	mLocaled = true;
    	mMessenger = new Messenger(mHandler);
    	mAsrServer = new AsrServer(null);
    	mService = mAsrServer.getMessenger();
    }
    
    private ServiceConnection mConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			JNIHelper.logd("connectTo " + name.getPackageName() + ":" + name.getClassName());
			mConnectStatus = ConnectStatus.STATUS_CONNECTED;
			mService = new Messenger(service);
			procMsgQueue();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			JNIHelper.logd("disconnect" + name.getPackageName() + ":" + name.getClassName());
			mConnectStatus = ConnectStatus.STATUS_DISCONNECTED;
			mService = null;
			//如果当前有未完成的upload任务，就会启动5分钟重试逻辑
			//upload任务是队列式的即只有上一个任务完成了(不管失败还是成功)才会执行下一个任务。
			//因此,如果某一个任务丢了，会导致后面的任务都不会执行
			onImportWordsDone(false, IImportKeywordsCallback.ERROR_ENGINE_NOT_READY);
			//回调异常给上一层调用
			if (mAsrStarting){
				JNIHelper.logd("asr is interrupted");
				asrAgain();
//				onError(-1);
			}
			addConnectTask();
		}
    	
    };
    
    public static enum ConnectStatus{
    	STATUS_DISCONNECTED,
    	STATUS_CONNECTING,
    	STATUS_CONNECTED
    }
    
    private ConnectStatus mConnectStatus = ConnectStatus.STATUS_DISCONNECTED;
    private long mLastConnectTime = SystemClock.elapsedRealtime();
	public void addConnectTask() {
		do {
			if (mLocaled){
				JNIHelper.logd("mLocaled=true");
				break;
			}
			if (mService != null){
				JNIHelper.logd("mService != null");
				break;
			}
			if (isForbidConnect()){
				JNIHelper.logd("isForbidConnect=true");
				break;
			}
			if (ConnectStatus.STATUS_CONNECTED == mConnectStatus) {
				JNIHelper.logd("have been connected...");
				break;
			}
			if (ConnectStatus.STATUS_CONNECTING == mConnectStatus) {
				long now = SystemClock.elapsedRealtime();
				if (now - mLastConnectTime < CONNECT_TIMEOUT){
					JNIHelper.logd("conneting...");
					break;
				}
			}
			JNIHelper.logd("begin connet to target remote service");
			mLastConnectTime = SystemClock.elapsedRealtime();
			mConnectStatus = ConnectStatus.STATUS_CONNECTING;
			connectService();
		} while (false);
	}
    
    private void connectService(){
		try {
			Intent intent = new Intent(GlobalContext.get(), ReserveService0.class);
			// for android 5.0
			intent.setPackage(ServiceManager.TXZ);
			GlobalContext.get().startService(intent);
			GlobalContext.get().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
		} catch (Exception e) {
		}
    }
    
    /**
     * 识别过程中意外断开，根据开始时间重试一次识别
     */
    private void asrAgain() {
    	JNIHelper.logd("asrAgain mAsrStartTime " + mAsrStartTime);
    	if(mAsrStartTime == 0){
    		onError(-1);
    		return;
    	}
    	mOption.mBeginSpeechTime = mAsrStartTime;
    	mOption.mRecoverAsr = true;
    	start(mOption);
    }

	private void handleMsg(Message msg){
    	Bundle bundle = msg.getData();
    	switch(msg.what){
    	case AsrMsgConstants.MSG_NOTIFY_INIT_RESULT:
    		boolean bInited = bundle.getBoolean(AsrMsgConstants.ASR_INIT_RESULT_BOOL, false);
    		onInit(bInited);
    		break;
    	case AsrMsgConstants.MSG_NOTIFY_RESULT:
            byte[] data = bundle.getByteArray(AsrMsgConstants.ASR_RESULT_VOICE_BYTEARRAY);
            onResult(data);
    		break;
    	case AsrMsgConstants.MSG_NOTIFY_ERROR:
    		int errCode = bundle.getInt(AsrMsgConstants.ASR_ERROR_INT, IAsr.ERROR_CODE);
    		onError(errCode);
    		break;
    	case AsrMsgConstants.MSG_NOTIFY_RECORDING_BEGIN:
    		onRecordingBegin();
    		break;
    	case AsrMsgConstants.MSG_NOTIFY_RECORDING_END:
    		onRecordingEnd();
    		break;
    	case AsrMsgConstants.MSG_NOTIFY_SPEECH_BEGIN:
    		onSpeechBegin();
    		break;
    	case AsrMsgConstants.MSG_NOTIFY_SPEECH_END:
    		onSpeechEnd();
    		break;
    	case AsrMsgConstants.MSG_NOTIFY_CANCEL:
    		onCancel();
    		break;
    	case AsrMsgConstants.MSG_NOTIFY_ABORT:
    		onAbort();
    		break;
    	case AsrMsgConstants.MSG_NOTIFY_VOLUME:
    		int nVol = bundle.getInt(AsrMsgConstants.ASR_VOLUME_CHANGE_INT, 0);
    		onVolume(nVol);
    		break;
    	case AsrMsgConstants.MSG_NOTIFY_SET_IMPORT_WORDS_DONE:
    		boolean bSuccessed = bundle.getBoolean(AsrMsgConstants.ASR_IMPORT_RESULT_BOOL, false);
    		int code = bundle.getInt(AsrMsgConstants.ASR_IMPORT_RESULT_CODE_INT, 0);
    		onImportWordsDone(bSuccessed, code);
    		break;
    	case AsrMsgConstants.MSG_NOTIFY_MONITOR:
    		String attr = bundle.getString(AsrMsgConstants.ASR_MONITOR_ATTR);
    		MonitorUtil.monitorCumulant(attr);
    		break;
		case AsrMsgConstants.MSG_NOTIFY_PARTIAL_RESULT:
			String result = bundle.getString(AsrMsgConstants.ASR_PARTIAL_RESULT_KEY);
			onPartialResult(result);
			break;
    	default:
    	}
    }

	private void onPartialResult (String result) {
		if (mOption != null && mOption.mCallback != null) {
			mOption.mCallback.onPartialResult(mOption, result);
		}
	}

    private void onInit(boolean bSuccessed){
    	if (mInitCallback != null){
    		mInitCallback.onInit(bSuccessed);
    	}
    }
    
	private void onResult(byte[] data) {
		if (mOption != null && mOption.mCallback != null) {
			try {
				VoiceParseData oVoiceParseData = VoiceParseData.parseFrom(data);
				mOption.mCallback.onSuccess(mOption, oVoiceParseData);
			} catch (Exception e) {
				JNIHelper.logd("exception : " + e.toString());
			}
		}
	}
	
    private void onError(int errCode){
    	if (mOption != null && mOption.mCallback != null){
    		mOption.mCallback.onError(mOption, 0, null, null, errCode);
    	}
    }
    
    private void onCancel(){
    	if (mOption != null && mOption.mCallback != null){
    		mOption.mCallback.onCancel(mOption);
    	}
    }
    
    private void onVolume(int vol){
    	if (mOption != null && mOption.mCallback != null){
    		mOption.mCallback.onVolume(mOption, vol);
    	}
    }
    
    private void onAbort(){
    	if (mOption != null && mOption.mCallback != null){
    		mOption.mCallback.onAbort(mOption, IAsr.ERROR_ABORT);
    	}
    }
    
	private Runnable mRunnableImportOnlineKeywords = new Runnable() {
		@Override
		public void run() {
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_RETRY_ONLINE_KEYWORDS);
		}
	};
	
	private synchronized void onImportWordsDone(boolean successed, int code) {
		IImportKeywordsCallback oCallBack = mImportKeywordsCallback;
		mImportKeywordsCallback = null;
		if (oCallBack != null) {
			JNIHelper.logd("import words successed  : " + successed + ", code : " + code);
			if (successed) {
				oCallBack.onSuccess(mKeywords);
			} else {
				oCallBack.onError(code, mKeywords);
				//引擎未准备好的时候过1分钟后再次尝试
				if (code == IImportKeywordsCallback.ERROR_ENGINE_NOT_READY){
					AppLogic.removeBackGroundCallback(mRunnableImportOnlineKeywords);
					AppLogic.runOnBackGround(mRunnableImportOnlineKeywords, 1*60*1000);
				}else if (code == IImportKeywordsCallback.ERROR_UPLOAD_TOO_FAST){
					//更新太频繁的时候过5分钟后再次尝试
					AppLogic.removeBackGroundCallback(mRunnableImportOnlineKeywords);
					AppLogic.runOnBackGround(mRunnableImportOnlineKeywords, 5*60*1000);
				}
			}
		}
	}
    
    private void onRecordingBegin(){
    	if (mOption != null && mOption.mCallback != null){
    		mOption.mCallback.onStart(mOption);
    	}
    }
    
    private void onRecordingEnd(){
    	if (mOption != null && mOption.mCallback != null){
    		mOption.mCallback.onEnd(mOption);
    	}
    }
    
    private void onSpeechBegin(){
    	if (mOption != null && mOption.mCallback != null){
    		mOption.mCallback.onSpeechBegin(mOption);
    	}
    }
    
    private void onSpeechEnd(){
    	if (mOption != null && mOption.mCallback != null){
    		mOption.mCallback.onSpeechEnd(mOption);
    	}
    }
    
    List<Message> mMsgQueue = new ArrayList<Message>();

	public void procMsgQueue() {
		if (mService != null) {
			synchronized (mMsgQueue) {
				for (Message m : mMsgQueue) {
					try {
						mService.send(m);
					} catch (Exception e) {
						JNIHelper.logd("procMsg excption : " + e.toString());
						if (m != null && m.what == AsrMsgConstants.MSG_REQ_IMPORT_WORDS){
							onImportWordsDone(false, -1);
						}
					}
				}
				mMsgQueue.clear();
			}
		}
	}

	public void sendMsg(int what, Bundle b) {
		Message msg = Message.obtain();
		msg.replyTo = mMessenger;
		msg.what = what;
		msg.setData(b);
		mHandler.postDelayed(new Runnable1<Message>(msg) {
			@Override
			public void run() {
				synchronized (mMsgQueue) {
					mMsgQueue.add(mP1);
				}
				procMsgQueue();
			}
		}, 0);
	}
    
	private void resetForbidConnectFlag(){
		if (mLocaled){
			return;
		}
		forbidConnect(false);
	}
	
	private IInitCallback mInitCallback = null;
	private AsrOption mOption = null;
	@Override
	public int initialize(IInitCallback oRun) {
		resetForbidConnectFlag();
		addConnectTask();
		mInitCallback = oRun;
		Bundle b = new Bundle();
		b.putInt(AsrMsgConstants.ENGINE_TYPE_INT,  getEngineType());
		b.putString(AsrMsgConstants.APPID_STR,  ProjectCfg.getIflyAppId());
		b.putString(AsrMsgConstants.APPKEY_STR,  ProjectCfg.getYunzhishengAppId());
		b.putString(AsrMsgConstants.SECRET_STR,  ProjectCfg.getYunzhishengSecret());
		b.putInt(AsrMsgConstants.ASR_PROJECT_CFG_AEC_TYPE_INT, ProjectCfg.getFilterNoiseType());
		b.putBoolean(AsrMsgConstants.ASR_ARGUMENT_USE_SE_PREPROCESSED_DATA_BOOLEAN, ProjectCfg.isUseSePreprocessedData());
		//云知声V3引擎需要激活码
		int yzs_sdk_version = ProjectCfg.YZS_SDK_VERSION; 
		byte[] activator = ProjectCfg.getYzsActivator();
		if (getEngineType() ==  AsrMsgConstants.ENGINE_TYPE_YZS_NET  
				&& yzs_sdk_version == 3 && activator != null){
			JNIHelper.logd("pass_yzs_activator");
			b.putByteArray(AsrMsgConstants.YZS_ACTIVATOR, activator);
		}
		
		sendMsg(AsrMsgConstants.MSG_REQ_INIT_WITH_APP_ID,  b);
		return 0;
	}

	@Override
	public void release() {
		if (mLocaled){
			JNIHelper.logd("SuperEngineProxy can't release. mLocaled : " + mLocaled);
			return;
		}
		JNIHelper.logd("send proc exit cmd");
		sendMsg(AsrMsgConstants.MSG_REQ_EXIT, null);
	}
	
	@Override
	public int start(AsrOption oOption) {
		resetForbidConnectFlag();
		addConnectTask();
		mAsrStarting = true;
		if(oOption == null){
			oOption = new AsrOption();
		}
		if(!oOption.mRecoverAsr){//不是恢复识别，记录当前开始时间
			mAsrStartTime = SystemClock.elapsedRealtime();
		}
		mOption = oOption;
		Bundle b = new Bundle();
		b.putInt(AsrMsgConstants.ENGINE_TYPE_INT, getEngineType());
		b.putString(AsrMsgConstants.APPID_STR,  ProjectCfg.getIflyAppId());
		b.putString(AsrMsgConstants.APPKEY_STR,  ProjectCfg.getYunzhishengAppId());
		b.putString(AsrMsgConstants.SECRET_STR,  ProjectCfg.getYunzhishengSecret());
		b.putInt(AsrMsgConstants.ASR_PROJECT_CFG_AEC_TYPE_INT, ProjectCfg.getFilterNoiseType());
		b.putString(AsrMsgConstants.ASR_OPTION_JSON_STR, AsrMsgConstants.OptionToJson(mOption));
		b.putString(AsrMsgConstants.ASR_ARGUMENT_GENERAL_CITY_STR, getCurrentCity());
		b.putString(AsrMsgConstants.ASR_ARGUMENT_GENERAL_GPSINFO_STR, getGpsInfo());
		b.putString(AsrMsgConstants.ASR_ARGUMENT_GENERAL_ENCRYPT_KEY, RecordFile.getEncryptKey());
		b.putBoolean(AsrMsgConstants.ASR_ARGUMENT_GENERAL_SAVE_VOICE, ProjectCfg.enableSaveVoice());
		b.putBoolean(AsrMsgConstants.ASR_ARGUMENT_GENERAL_SAVE_RAW_PCM, ProjectCfg.enableSaveRawPCM());
		b.putBoolean(AsrMsgConstants.ASR_ARGUMENT_USE_SE_PREPROCESSED_DATA_BOOLEAN, ProjectCfg.isUseSePreprocessedData());
		
		//云知声V3引擎需要激活码
		int yzs_sdk_version = ProjectCfg.YZS_SDK_VERSION; 
		byte[] activator = ProjectCfg.getYzsActivator();
		if (getEngineType() ==  AsrMsgConstants.ENGINE_TYPE_YZS_NET  
				&& yzs_sdk_version == 3 && activator != null){
			JNIHelper.logd("pass_yzs_activator");
			b.putByteArray(AsrMsgConstants.YZS_ACTIVATOR, activator);
		}
		
		sendMsg(AsrMsgConstants.MSG_REQ_START, b);
		return 0;
	}

	@Override
	public void stop() {
		sendMsg(AsrMsgConstants.MSG_REQ_STOP, null);
	}

	@Override
	public void cancel() {
		mAsrStarting = false;
		mAsrStartTime = 0;
		if(mOption != null){
			mOption.mRecoverAsr = false;
		}
		sendMsg(AsrMsgConstants.MSG_REQ_CANCEL, null);
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
    
	private SdkKeywords mKeywords = null;
	private IImportKeywordsCallback mImportKeywordsCallback = null;
	@Override
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		mKeywords = oKeywords;
		mImportKeywordsCallback = oCallback;
        Bundle b = new Bundle();
        b.putByteArray(AsrMsgConstants.ASR_IMPORT_KEYWORD_BYTEARRAY, MessageNano.toByteArray(mKeywords));
		sendMsg(AsrMsgConstants.MSG_REQ_IMPORT_WORDS, b);
		return true;
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
	
	@Override
	public int capacity(){
		return NET_ASR_CAPACITY;
	}
}
