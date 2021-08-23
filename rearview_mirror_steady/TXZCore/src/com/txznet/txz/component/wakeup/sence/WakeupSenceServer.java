package com.txznet.txz.component.wakeup.sence;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.wakeup.ISenceWakeup;
import com.txznet.txz.component.wakeup.ISenceWakeup.IInitCallback;
import com.txznet.txz.component.wakeup.ISenceWakeup.ISenceWakeupCallback;
import com.txznet.txz.component.wakeup.ISenceWakeup.SenceWakeupOption;
import com.txznet.txz.component.wakeup.mix.WkMsgConstants;
import com.txznet.txz.jni.JNIHelper;

public class WakeupSenceServer{
	private String mStrEngine = null;

	private ISenceWakeup mWakeup = null;
	private HandlerThread mWorkThread = null;
	private Handler mHandler = null;
	
	private Messenger mMessenger = null;
	private Messenger mClient = null;
	
	public WakeupSenceServer(String strEngine){
		mWorkThread = new HandlerThread("WakeupSenceServer");
		mWorkThread.start();
		mHandler = new Handler(mWorkThread.getLooper()){
			@Override
			public void handleMessage(Message msg) {
				JNIHelper.logd("WakeupSenceServer handleMsg msg:"+msg);
				handleMsg(msg);
			}
			
		};
		mMessenger = new Messenger(mHandler);
		mStrEngine = strEngine;
	}
	
	private ISenceWakeup createEngine(int engineType){
		ISenceWakeup instance = null;
		try {
			instance = (ISenceWakeup) Class.forName(mStrEngine).newInstance();
		} catch (Exception e) {
			LogUtil.logd("error : " + e.toString());
		}
		return instance;
	}
	
	public Messenger getMessenger(){
		JNIHelper.logd("WakeupSenceServer getMessenger");
		return mMessenger;
	}
    
	private void handleMsg(Message msg){
		mClient = msg.replyTo;
		Bundle b = msg.getData();
		switch(msg.what){
		case WkMsgConstants.MSG_REQ_INIT_WITH_APP_ID:
			ProjectCfg.setIflyAppId(b.getString(WkMsgConstants.APPID_STR));
			ProjectCfg.setYunzhishengAppId(b.getString(WkMsgConstants.APPKEY_STR));
			ProjectCfg.setYunzhishengSecret(b.getString(WkMsgConstants.SECRET_STR));
			ProjectCfg.setFilterNoiseType(b.getInt(WkMsgConstants.WAKEUP_PROJECT_CFG_AEC_TYPE_INT));
			ProjectCfg.enableBlackHole(b.getBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HOLE_BOOL));
			ProjectCfg.mUseHQualityWakeupModel = b.getBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HQUALITY_MODEL_BOOL);
			mWakeup = createEngine(b.getInt(WkMsgConstants.PRE_ENGINE_TYPE_INT));
			mWakeup.initialize(mInitCallback);
			break;
		case WkMsgConstants.MSG_REQ_START:
			final int recordType = b.getInt(WkMsgConstants.WAKEUP_PROJECT_CFG_RECORD_TYPE, -1);
			final String[] cmds = b.getStringArray(WkMsgConstants.WAKEUP_PROJECT_CFG_KEYWORDS);
			final long mBeginTime = b.getLong(WkMsgConstants.WAKEUP_ARGUMENT_BEGIN_TIME, 0);
			if (mWakeup == null){
				ProjectCfg.setIflyAppId(b.getString(WkMsgConstants.APPID_STR));
				ProjectCfg.setYunzhishengAppId(b.getString(WkMsgConstants.APPKEY_STR));
				ProjectCfg.setYunzhishengSecret(b.getString(WkMsgConstants.SECRET_STR));
				ProjectCfg.setFilterNoiseType(b.getInt(WkMsgConstants.WAKEUP_PROJECT_CFG_AEC_TYPE_INT));
				ProjectCfg.enableBlackHole(b.getBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HOLE_BOOL));
				ProjectCfg.mUseHQualityWakeupModel = b.getBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HQUALITY_MODEL_BOOL);
				
				mWakeup = createEngine(b.getInt(WkMsgConstants.PRE_ENGINE_TYPE_INT));
				mWakeup.initialize(new IInitCallback() {
					@Override
					public void onInit(boolean bSuccess) {
						mWakeup.start(mWakeupCallback, new SenceWakeupOption().setBeginTime(mBeginTime), recordType,cmds);
					}
				});
				return;
			}
			ProjectCfg.mUseHQualityWakeupModel = b.getBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HQUALITY_MODEL_BOOL);
            mWakeup.start(mWakeupCallback,new SenceWakeupOption().setBeginTime(mBeginTime),recordType,cmds);
			break;
		case WkMsgConstants.MSG_REQ_STOP:
			if (mWakeup != null) {
				mWakeup.stop();
			}
			break;
		case WkMsgConstants.MSG_REQ_SET_WORDS:
//			if (mWakeup != null) {
//				String[] wakeupWords = b.getStringArray(WkMsgConstants.WAKEUP_WORDS_ARRAY_STR);
//				mWakeup.setWakeupKeywords(wakeupWords);
//			}
			break;
		case WkMsgConstants.MSG_REQ_SET_THRESHOLD:
//			if (mWakeup != null) {
//				float threshold = b.getFloat(WkMsgConstants.WAKEUP_THRESHHOLD_FLOAT);
//				mWakeup.setWakeupThreshold(threshold);
//			}
			break;
		case WkMsgConstants.MSG_REQ_ENABLE_VOICE:
			if (mWakeup != null) {
				boolean enable = b.getBoolean(WkMsgConstants.WAKEUP_VOICE_CHANNEL_BOOL);
				mWakeup.enableVoiceChannel(enable);
			}
			break;
		default:
		} 
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
				b.putBoolean(WkMsgConstants.WAKEUP_INIT_RESULT_BOOL, bSuccess);
				sendMsg(WkMsgConstants.MSG_NOTIFY_INIT_RESULT, b);
		}
	};
	
	private ISenceWakeupCallback mWakeupCallback = new ISenceWakeupCallback() {
//		public void onSetWordsDone() {
//			
//		};
	    public void onSpeechBegin() {
	    	sendMsg(WkMsgConstants.MSG_NOTIFY_SPEECH_BEGIN, null);
	    };
	    public void onSpeechEnd() {
	    	sendMsg(WkMsgConstants.MSG_NOTIFY_SPEECH_END, null);
	    };
	    public void onVolume(int vol) {
	    	Bundle b = new Bundle();
	    	b.putInt(WkMsgConstants.WAKEUP_VOLUME_CHANGE_INT, vol);
	    	sendMsg(WkMsgConstants.MSG_NOTIFY_VOLUME, b);
	    };
//	    public void onWakeUp(String text) {
//	    	Bundle b = new Bundle();
//	    	b.putString(WkMsgConstants.WAKEUP_RESULT_STR, text);
//	    	sendMsg(WkMsgConstants.MSG_NOTIFY_RESULT, b);
//	    };
	    public void onWakeUp(String text, int time) {
	    	Bundle b = new Bundle();
	    	b.putString(WkMsgConstants.WAKEUP_RESULT_STR, text);
	    	b.putInt(WkMsgConstants.WAKEUP_RESULT_TIME_INT, time);
	    	sendMsg(WkMsgConstants.MSG_NOTIFY_RESULT_WITH_TIME, b);
	    };
	};
	
	
}
