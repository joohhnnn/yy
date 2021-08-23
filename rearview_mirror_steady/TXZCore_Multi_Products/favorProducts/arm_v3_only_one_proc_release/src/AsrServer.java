package com.txznet.txz.component.asr.mix;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IAsrCallback;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.component.asr.IAsr.IInitCallback;
import com.txznet.txz.util.recordcenter.RecordFile;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class AsrServer{
	private String mStrEngine = null;

	private IAsr mAsr = null;
	private HandlerThread mWorkThread = null;
	private Handler mHandler = null;
	
	private Messenger mMessenger = null;
	private Messenger mClient = null;
	
	public AsrServer(String strEngine){
		mWorkThread = new HandlerThread("AsrServer");
		mWorkThread.start();
		mHandler = new Handler(mWorkThread.getLooper()){
			@Override
			public void handleMessage(Message msg) {
				handleMsg(msg);
			}
			
		};
		mMessenger = new Messenger(mHandler);
		mStrEngine = strEngine;
	}
	
	private IAsr createEngine(int engineType){
		switch(engineType){
		case AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET:
			mStrEngine = AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET_IMPL;
			break;
		case AsrMsgConstants.ENGINE_TYPE_YZS_NET:
			mStrEngine = AsrMsgConstants.ENGINE_TYPE_YZS_NET_IMPL;
			break;
		case AsrMsgConstants.ENGINE_TYPE_SOUGOU_NET:
			mStrEngine = AsrMsgConstants.ENGINE_TYPE_SOGOU_NET_IMPL;
			break;
		}
		LogUtil.logd("mStrEngine = " + mStrEngine + ", engineType = " + engineType);
		IAsr instance = null;
		try {
			instance = (IAsr) Class.forName(mStrEngine).newInstance();
		} catch (Exception e) {
			LogUtil.logd("error : " + e.toString());
		}
		return instance;
	}
	
	public Messenger getMessenger(){
		return mMessenger;
	}
    
	private void handleMsg(Message msg){
		Bundle b = msg.getData();
		mClient = msg.replyTo;
		switch(msg.what){
		case AsrMsgConstants.MSG_REQ_EXIT:
			LogUtil.logd("process will exit");
			System.exit(0);
			break;
		case AsrMsgConstants.MSG_REQ_INIT_WITH_APP_ID:
			ProjectCfg.setIflyAppId(b.getString(AsrMsgConstants.APPID_STR));
			ProjectCfg.setYunzhishengAppId(b.getString(AsrMsgConstants.APPKEY_STR));
			ProjectCfg.setYunzhishengSecret(b.getString(AsrMsgConstants.SECRET_STR));
			ProjectCfg.setFilterNoiseType(b.getInt(AsrMsgConstants.ASR_PROJECT_CFG_AEC_TYPE_INT));
			ProjectCfg.setYzsActivator(b.getByteArray("yzs_activator"));
			mAsr = createEngine(b.getInt(AsrMsgConstants.ENGINE_TYPE_INT));
			mAsr.initialize(mInitCallback);
			break;
		case AsrMsgConstants.MSG_REQ_START:
			Arguments.sCurrCity = b.getString(AsrMsgConstants.ASR_ARGUMENT_GENERAL_CITY_STR);
			Arguments.sGpsInfo = b.getString(AsrMsgConstants.ASR_ARGUMENT_GENERAL_GPSINFO_STR);
			String strJsonOption = b.getString(AsrMsgConstants.ASR_OPTION_JSON_STR);
			Arguments.sEncryptKey = b.getString(AsrMsgConstants.ASR_ARGUMENT_GENERAL_ENCRYPT_KEY);
			Arguments.sIsSaveVoice = b.getBoolean(AsrMsgConstants.ASR_ARGUMENT_GENERAL_SAVE_VOICE, true);
			Arguments.sIsSaveRawPCM = b.getBoolean(AsrMsgConstants.ASR_ARGUMENT_GENERAL_SAVE_RAW_PCM, false);
			RecordFile.setEncryptKey(Arguments.sEncryptKey);
			if (mAsr == null){
				ProjectCfg.setIflyAppId(b.getString(AsrMsgConstants.APPID_STR));
				ProjectCfg.setYunzhishengAppId(b.getString(AsrMsgConstants.APPKEY_STR));
				ProjectCfg.setYunzhishengSecret(b.getString(AsrMsgConstants.SECRET_STR));
				ProjectCfg.setFilterNoiseType(b.getInt(AsrMsgConstants.ASR_PROJECT_CFG_AEC_TYPE_INT));
				ProjectCfg.setYzsActivator(b.getByteArray("yzs_activator"));
				mAsr = createEngine(b.getInt(AsrMsgConstants.ENGINE_TYPE_INT));
			}
			mOption = AsrMsgConstants.JsonToOption(strJsonOption);
			
			mOption.setCallback(mAsrCallback);
			mAsr.start(mOption);
			break;
		case AsrMsgConstants.MSG_REQ_STOP:
			if (mAsr != null) {
				mAsr.stop();
			}
			break;
		case AsrMsgConstants.MSG_REQ_CANCEL:
			if (mAsr != null) {
				mAsr.cancel();
			}
			break;
		case AsrMsgConstants.MSG_REQ_IMPORT_WORDS:
			byte[] data = b.getByteArray(AsrMsgConstants.ASR_IMPORT_KEYWORD_BYTEARRAY);
			try {
				mSetNetDataSdkKeywords = SdkKeywords.parseFrom(data);
				mAsr.importKeywords(mSetNetDataSdkKeywords, mSetNetDataCallback);
			} catch (Exception e) {
				LogUtil.logd("exception : " + e.toString());
				importKeywordResult(false, IImportKeywordsCallback.ERROR_ENGINE_NOT_READY);
			}
			break;
		default:
		} 
	}
	
	private SdkKeywords mSetNetDataSdkKeywords = null;
	private IImportKeywordsCallback mSetNetDataCallback = new IImportKeywordsCallback() {
		@Override
		public void onSuccess(SdkKeywords mSdkKeywords) {
			importKeywordResult(true, 0);
		}

		@Override
		public void onError(int error, SdkKeywords mSdkKeywords) {
			importKeywordResult(false, error);
		}
	};
	
	private void importKeywordResult(boolean bSuccessed, int code){
		Bundle b = new Bundle();
		b.putBoolean(AsrMsgConstants.ASR_IMPORT_RESULT_BOOL, bSuccessed);
		b.putInt(AsrMsgConstants.ASR_IMPORT_RESULT_CODE_INT, code);
		sendMsg(AsrMsgConstants.MSG_NOTIFY_SET_IMPORT_WORDS_DONE, b);
	}
	
	private void sendMsg(int what, Bundle b){
		Message msg = Message.obtain();
		msg.what = what;
        msg.setData(b);
		try {
			mClient.send(msg);
		} catch (RemoteException e) {
			LogUtil.logd("error : " + e.toString());
		}
	}
	
	private IInitCallback mInitCallback = new IInitCallback() {
		@Override
		public void onInit(boolean bSuccess) {
			    Bundle b = new Bundle();
				b.putBoolean(AsrMsgConstants.ASR_INIT_RESULT_BOOL, bSuccess);
				sendMsg(AsrMsgConstants.MSG_NOTIFY_INIT_RESULT, b);
		}
	};
	
	private AsrOption mOption = null;
	
	private IAsrCallback mAsrCallback = new IAsrCallback(){
		@Override
		public void onStart(AsrOption option){
			sendMsg(AsrMsgConstants.MSG_NOTIFY_RECORDING_BEGIN, null);
		}
		
		public void onEnd(AsrOption option){
			sendMsg(AsrMsgConstants.MSG_NOTIFY_RECORDING_END, null);
		}
		
		public void onVolume(AsrOption option, int volume){
			Bundle b = new Bundle();
			b.putInt(AsrMsgConstants.ASR_VOLUME_CHANGE_INT, volume);
			sendMsg(AsrMsgConstants.MSG_NOTIFY_VOLUME, b);
		}
		
		public void onAbort(AsrOption option, int error){
			sendMsg(AsrMsgConstants.MSG_NOTIFY_ABORT, null);
		}
		
		public void onSuccess(AsrOption option, VoiceParseData oVoiceParseData){
			Bundle b = new Bundle();
	        b.putByteArray(AsrMsgConstants.ASR_RESULT_VOICE_BYTEARRAY, MessageNano.toByteArray(oVoiceParseData));
			sendMsg(AsrMsgConstants.MSG_NOTIFY_RESULT, b);
		}
		
		public void onError(AsrOption option, int error, String desc, String speech, int error2){
			Bundle b = new Bundle();
			b.putInt(AsrMsgConstants.ASR_ERROR_INT, error2);
			sendMsg(AsrMsgConstants.MSG_NOTIFY_ERROR, b);
		}
		public void onMonitor(String attr) {
			Bundle b = new Bundle();
			b.putString(AsrMsgConstants.ASR_MONITOR_ATTR, attr);
			sendMsg(AsrMsgConstants.MSG_NOTIFY_MONITOR, b);
		};
	};
}
