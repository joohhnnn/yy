package com.txznet.txz.component.wakeup.sence;

import java.util.ArrayList;
import java.util.List;

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
import android.os.RemoteException;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.reserve.service.ReserveService2;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.wakeup.ISenceWakeup;
import com.txznet.txz.component.wakeup.mix.WkMsgConstants;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.runnables.Runnable1;

public class WakeupSenceProxy  implements ISenceWakeup{
	private HandlerThread mWorkThread = null;
	private Handler mHandler= null;
    private Messenger mService = null;
    private Messenger mMessenger = null;
    private int mEngineType = WkMsgConstants.ENGINE_TYPE_YZS;
    public WakeupSenceProxy(){
    	mWorkThread = new HandlerThread("WakeupSenceProxy");
    	mWorkThread.start();
    	mHandler = new Handler(mWorkThread.getLooper()){
    		@Override
    		public void handleMessage(Message msg) {
    		      handleMsg(msg);
    		}
    	};
    	mMessenger = new Messenger(mHandler);
    	bindService();
    }
    
    private ServiceConnection mConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = new Messenger(service);
			procMsgQueue();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			bindService();
		}
    	
    };
    
    private void bindService(){
		try {
			Intent intent = new Intent(GlobalContext.get(), ReserveService2.class);
			// for android 5.0
			intent.setPackage(ServiceManager.TXZ);
			GlobalContext.get().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
		} catch (Exception e) {
		}
    }
    
    private void handleMsg(Message msg){
    	Bundle bundle = msg.getData();
    	switch(msg.what){
    	case WkMsgConstants.MSG_NOTIFY_INIT_RESULT:
    		boolean bInited = bundle.getBoolean(WkMsgConstants.WAKEUP_INIT_RESULT_BOOL);
    		onInit(bInited);
    		break;
    	case WkMsgConstants.MSG_NOTIFY_RESULT:{
    		String strResult = bundle.getString(WkMsgConstants.WAKEUP_RESULT_STR);
    		onResult(strResult);
    	}
    		break;
    	case WkMsgConstants.MSG_NOTIFY_RESULT_WITH_TIME:{
    		String strResult = bundle.getString(WkMsgConstants.WAKEUP_RESULT_STR);
    		int nTime = bundle.getInt(WkMsgConstants.WAKEUP_RESULT_TIME_INT);
    		onResult(strResult, nTime);
    	}
    		break;
    	case WkMsgConstants.MSG_NOTIFY_ERROR:
    		break;
    	case WkMsgConstants.MSG_NOTIFY_SPEECH_BEGIN:
    		onSpeechBegin();
    		break;
    	case WkMsgConstants.MSG_NOTIFY_SPEECH_END:
    		onSpeechEnd();
    		break;
    	case WkMsgConstants.MSG_NOTIFY_CANCEL:
    		onCancel();
    		break;
    	case WkMsgConstants.MSG_NOTIFY_ABORT:
    		onAbort();
    		break;
    	case WkMsgConstants.MSG_NOTIFY_VOLUME:
    		int nVol = bundle.getInt(WkMsgConstants.WAKEUP_VOLUME_CHANGE_INT);
    		onVolume(nVol);
    		break;
    	default:
    	}
    }
    
    private void onInit(boolean bSuccessed){
    	IInitCallback callback = mInitCallback;
    	mInitCallback = null;
    	if (callback != null){
    		callback.onInit(bSuccessed);
    	}
    }
    
    private void onResult(String strResult){
    	ISenceWakeupCallback callback = mWakeupCallback;
    	if (callback != null){
    		callback.onWakeUp(strResult,0);
    	}
    }
    private void onResult(String strResult, int nTime){
    	ISenceWakeupCallback callback = mWakeupCallback;
    	if (callback != null){
    		callback.onWakeUp(strResult, nTime);
    	}
    }
    
    private void onCancel(){
    	
    }
    
    private void onVolume(int vol){
    	ISenceWakeupCallback callback = mWakeupCallback;
    	if (callback != null){
    		callback.onVolume(vol);
    	}
    }
    
    private void onAbort(){
    	
    }
    
    private void onSpeechBegin(){
    	ISenceWakeupCallback callback = mWakeupCallback;
    	if (callback != null){
    		callback.onSpeechBegin();
    	}
    }
    
    private void onSpeechEnd(){
    	ISenceWakeupCallback callback = mWakeupCallback;
    	if (callback != null){
    		callback.onSpeechEnd();
    	}
    }
    
    List<Message> mMsgQueue = new ArrayList<Message>();

	public void procMsgQueue() {
		JNIHelper.logd("WakeupSenceServer procMsgQueue");
		if (mService != null) {
			synchronized (mMsgQueue) {
				for (Message m : mMsgQueue) {
					try {
						JNIHelper.logd("WakeupSenceServer send m.what:"+m.what);
						mService.send(m);
					} catch (RemoteException e) {
						e.printStackTrace();
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
    
	private IInitCallback mInitCallback = null;
	private ISenceWakeupCallback mWakeupCallback = null;
	@Override
	public int initialize(IInitCallback oRun) {
		mInitCallback = oRun;
		Bundle b = new Bundle();
		b.putInt(WkMsgConstants.PRE_ENGINE_TYPE_INT, mEngineType);
		b.putString(WkMsgConstants.APPID_STR,  ProjectCfg.getIflyAppId());
		b.putString(WkMsgConstants.APPKEY_STR,  ProjectCfg.getYunzhishengAppId());
		b.putString(WkMsgConstants.SECRET_STR,  ProjectCfg.getYunzhishengSecret());
		b.putInt(WkMsgConstants.WAKEUP_PROJECT_CFG_AEC_TYPE_INT, ProjectCfg.getFilterNoiseType());
	    b.putBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HOLE_BOOL, ProjectCfg.needBlackHole());
	    b.putBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HQUALITY_MODEL_BOOL, ProjectCfg.mUseHQualityWakeupModel);
		sendMsg(WkMsgConstants.MSG_REQ_INIT_WITH_APP_ID,  b);
		return 0;
	}

	@Override
	public int start(ISenceWakeupCallback oCallback,SenceWakeupOption option, int recordType, String[] cmds) {
		mWakeupCallback = oCallback;
		Bundle b = new Bundle();
		b.putInt(WkMsgConstants.PRE_ENGINE_TYPE_INT, mEngineType);
		b.putString(WkMsgConstants.APPID_STR,  ProjectCfg.getIflyAppId());
		b.putString(WkMsgConstants.APPKEY_STR,  ProjectCfg.getYunzhishengAppId());
		b.putString(WkMsgConstants.SECRET_STR,  ProjectCfg.getYunzhishengSecret());
		b.putInt(WkMsgConstants.WAKEUP_PROJECT_CFG_AEC_TYPE_INT, ProjectCfg.getFilterNoiseType());
	    b.putBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HOLE_BOOL, ProjectCfg.needBlackHole());
	    b.putBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HQUALITY_MODEL_BOOL, ProjectCfg.mUseHQualityWakeupModel);
	    b.putInt(WkMsgConstants.WAKEUP_PROJECT_CFG_RECORD_TYPE, recordType);
	    b.putStringArray(WkMsgConstants.WAKEUP_PROJECT_CFG_KEYWORDS, cmds);
	    if(option != null){
	    	b.putLong(WkMsgConstants.WAKEUP_ARGUMENT_BEGIN_TIME, option.mBeginSpeechTime);
	    }
		sendMsg(WkMsgConstants.MSG_REQ_START, b);
		return 0;
	}


//	public void setWakeupKeywords(String[] keywords) {
//		Bundle b = new Bundle();
//		b.putStringArray(WkMsgConstants.WAKEUP_WORDS_ARRAY_STR, keywords);
//		sendMsg(WkMsgConstants.MSG_REQ_SET_WORDS, b);
//	}

//	@Override
//	public void setWakeupThreshold(float val) {
//		Bundle b = new Bundle();
//		b.putFloat(WkMsgConstants.WAKEUP_THRESHHOLD_FLOAT, val);
//		sendMsg(WkMsgConstants.MSG_REQ_SET_THRESHOLD, b);
//	}

	@Override
	public void enableVoiceChannel(boolean enable) {
		Bundle b = new Bundle();
		b.putBoolean(WkMsgConstants.WAKEUP_VOICE_CHANNEL_BOOL, enable);
		sendMsg(WkMsgConstants.MSG_REQ_ENABLE_VOICE, b);
	}

	@Override
	public void stop() {
		mWakeupCallback = null;
		sendMsg(WkMsgConstants.MSG_REQ_STOP, null);
	}

}
