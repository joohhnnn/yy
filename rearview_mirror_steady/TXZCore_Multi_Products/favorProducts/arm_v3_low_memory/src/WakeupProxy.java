package com.txznet.txz.component.wakeup.mix;

import java.util.LinkedList;
import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.service.ReserveService1;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.wakeup.WakeupManager;
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
import android.text.TextUtils;

public class WakeupProxy  implements IWakeup{
	private HandlerThread mWorkThread = null;
	private Handler mHandler= null;
    private Messenger mService = null;
    private Messenger mMessenger = null;
    private int mEngineType = WkMsgConstants.ENGINE_TYPE_YZS;
    private boolean mInitedSuccessed = false;
	private String[] mLastWkWords = null;
	private boolean mLastWkStatus = false;
	private boolean mLastVoiceChannelStatus = true;
	private String mActionName = null;
	private boolean bInited = false;
	
	private static IWakeup sLocalWakeupProxy = null;
	
	public static IWakeup getProxy(){
		if (sLocalWakeupProxy == null){
			synchronized (WakeupProxy.class) {
				if (sLocalWakeupProxy == null){
					sLocalWakeupProxy = new WakeupProxy();
				}
			}
		}
		return sLocalWakeupProxy;
	}
	
    private WakeupProxy(){
    	mWorkThread = new HandlerThread("WakeupProxy");
    	mWorkThread.start();
    	mHandler = new Handler(mWorkThread.getLooper()){
    		@Override
    		public void handleMessage(Message msg) {
    		      handleMsg(msg);
    		}
    	};
    	mMessenger = new Messenger(mHandler);
    	mActionName = null;
    	bindService();
    }
    
	private static IWakeup sRemoteWakeupProxy = null;
	
	public static IWakeup getProxy(String action){
		if (sRemoteWakeupProxy  == null){
			synchronized (WakeupProxy.class) {
				if (sRemoteWakeupProxy == null){
					sRemoteWakeupProxy  = new WakeupProxy(action);
				}
			}
		}
		return sRemoteWakeupProxy;
	}
    
    private WakeupProxy(String action){
    	mWorkThread = new HandlerThread("WakeupProxy");
    	mWorkThread.start();
    	mHandler = new Handler(mWorkThread.getLooper()){
    		@Override
    		public void handleMessage(Message msg) {
    		      handleMsg(msg);
    		}
    	};
    	mMessenger = new Messenger(mHandler);
    	mActionName = action;
    	bindService(action);
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
			mInitedSuccessed = false;
			//调用过初始化才调用, 不然有可能其他条件尚未满足导致初始化失败
			if (bInited){
				initialize(mLastWkWords, mInitCallback);//断开连接需要重新初始化
			}
			if (TextUtils.isEmpty(mActionName)){
				bindService();
			}else{
				bindService(mActionName);
			}
		}
    	
    };
    
    private void bindService(){
		try {
			Intent intent = new Intent(GlobalContext.get(), ReserveService1.class);
			// for android 5.0
			intent.setPackage(ServiceManager.TXZ);
			GlobalContext.get().startService(intent);
			GlobalContext.get().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
		} catch (Exception e) {
		}
    }
    
    private void bindService(String action){
		try {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName("com.txznet.wakeup", "com.txznet.wakeup.service.WakeupService"));
			intent.setPackage(ServiceManager.WAKEUP);
			GlobalContext.get().startService(intent);
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
    		float score = bundle.getFloat(WkMsgConstants.WAKEUP_RESULT_SCORE);
    		onResult(strResult, score);
    	}
    		break;
    	case WkMsgConstants.MSG_NOTIFY_RESULT_WITH_TIME:{
    		String strResult = bundle.getString(WkMsgConstants.WAKEUP_RESULT_STR);
    		int nTime = bundle.getInt(WkMsgConstants.WAKEUP_RESULT_TIME_INT);
    		float score = bundle.getFloat(WkMsgConstants.WAKEUP_RESULT_SCORE);
    		onResult(strResult, nTime, score);
    	}
    		break;
    	case WkMsgConstants.MSG_NOTIFY_ERROR:
    		int errCode = 0;
    		errCode = bundle.getInt(WkMsgConstants.WAKEUP_ERROR_INT);
    		onError(errCode);
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
    	mInitedSuccessed = bSuccessed;
    	if (callback != null){
    		callback.onInit(bSuccessed);
    	}else{
    		//重连后的初始化需要执行一遍消息队列
			{
				Bundle b = new Bundle();
				b.putBoolean(WkMsgConstants.WAKEUP_VOICE_CHANNEL_BOOL, mLastVoiceChannelStatus);
				addMessage(0, WkMsgConstants.MSG_REQ_ENABLE_VOICE, b);
			}
			{
				Bundle b = new Bundle();
				if (mLastWkStatus){
					b.putBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HQUALITY_MODEL_BOOL, ProjectCfg.mUseHQualityWakeupModel);
					addMessage(1, WkMsgConstants.MSG_REQ_START, b);
				}else{
					addMessage(1, WkMsgConstants.MSG_REQ_STOP, null);
				}
			}
			
			//解决唤醒进程crash后, 没法唤醒的问题
			if (ProjectCfg.wakeupPrebuiltGrammar()){
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						WakeupManager.getInstance().stop();
						WakeupManager.getInstance().start();
					}
				}, 100);
			}
			
    		procMsgQueue();
    	}
    }
    
    private void onResult(String strResult, float score){
    	IWakeupCallback callback = mWakeupCallback;
    	if (callback != null){
    		callback.onWakeUp(strResult, score);
    	}
    }
    private void onResult(String strResult, int nTime, float score){
    	IWakeupCallback callback = mWakeupCallback;
    	if (callback != null){
    		callback.onWakeUp(strResult, nTime, score);
    	}
    }
    
    private void onCancel(){
    	
    }
    
    private void onVolume(int vol){
    	IWakeupCallback callback = mWakeupCallback;
    	if (callback != null){
    		callback.onVolume(vol);
    	}
    }
    
    private void onError(int errCode){
    	IWakeupCallback callback = mWakeupCallback;
    	if (callback != null){
    		callback.onError(errCode);
    	}
    }
    
    private void onAbort(){
    	
    }
    
    private void onSpeechBegin(){
    	IWakeupCallback callback = mWakeupCallback;
    	if (callback != null){
    		callback.onSpeechBegin();
    	}
    }
    
    private void onSpeechEnd(){
    	IWakeupCallback callback = mWakeupCallback;
    	if (callback != null){
    		callback.onSpeechEnd();
    	}
    }
    
    List<Message> mMsgQueue = new LinkedList<Message>();

	public void procMsgQueue() {
		if (mService != null) {
			synchronized (mMsgQueue) {
				for (Message m : mMsgQueue) {
					try {
						if (!mInitedSuccessed){
							if (m.what == WkMsgConstants.MSG_REQ_INIT_WITH_APP_ID){
								mService.send(m);
								mMsgQueue.remove(m);
								mLastWkWords = m.getData().getStringArray(WkMsgConstants.WAKEUP_WORDS_ARRAY_STR);
							}
							return;
						}
						mService.send(m);
						//保存最后一次设置的状态即属性
						if (m.what == WkMsgConstants.MSG_REQ_SET_WORDS){
							mLastWkWords = m.getData().getStringArray(WkMsgConstants.WAKEUP_WORDS_ARRAY_STR);
						}else if (m.what == WkMsgConstants.MSG_REQ_START){
							mLastWkStatus = true;
						}else if (m.what == WkMsgConstants.MSG_REQ_STOP){
							mLastWkStatus = false;
						}else if (m.what == WkMsgConstants.MSG_REQ_ENABLE_VOICE){
							mLastVoiceChannelStatus = m.getData().getBoolean(WkMsgConstants.WAKEUP_VOICE_CHANNEL_BOOL);
						}
					} catch (Exception e) {
						JNIHelper.loge("procMsgQueue : " + e.toString());
					}
				}
				mMsgQueue.clear();
			}
		}
	}
    private void addMessage(int index, int what, Bundle b){
		Message msg = Message.obtain();
		msg.replyTo = mMessenger;
		msg.what = what;
		msg.setData(b);
    	synchronized (mMsgQueue){
    		mMsgQueue.add(index, msg);
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
	
	public void sendMsg(int what, Bundle b, final boolean top) {
		Message msg = Message.obtain();
		msg.replyTo = mMessenger;
		msg.what = what;
		msg.setData(b);
		mHandler.postDelayed(new Runnable1<Message>(msg) {
			@Override
			public void run() {
				synchronized (mMsgQueue) {
					if (top){
						mMsgQueue.add(0, mP1);
					}else{
						mMsgQueue.add(mP1);
					}
				}
				procMsgQueue();
			}
		}, 0);
	}
    
	private IInitCallback mInitCallback = null;
	private IWakeupCallback mWakeupCallback = null;
	
	private int getEngineType(){
		int type = WkMsgConstants.ENGINE_TYPE_YZS;
		if (ProjectCfg.wakeupPrebuiltGrammar()){
			type = WkMsgConstants.ENGINE_TYPE_PREBUILT;
		}
		mEngineType = type;
		return type;
	}
	
	@Override
	public int initialize(String[] cmds, IInitCallback oRun) {
		bInited = true;
		mInitCallback = oRun;
		Bundle b = new Bundle();
		b.putInt(WkMsgConstants.ENGINE_TYPE_INT, getEngineType());
		b.putString(WkMsgConstants.APPID_STR,  ProjectCfg.getIflyAppId());
		b.putString(WkMsgConstants.APPKEY_STR,  ProjectCfg.getYunzhishengAppId());
		b.putString(WkMsgConstants.SECRET_STR,  ProjectCfg.getYunzhishengSecret());
		b.putInt(WkMsgConstants.WAKEUP_PROJECT_CFG_AEC_TYPE_INT, ProjectCfg.getFilterNoiseType());
	    b.putBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HOLE_BOOL, ProjectCfg.needBlackHole());
	    b.putBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HQUALITY_MODEL_BOOL, ProjectCfg.mUseHQualityWakeupModel);
		b.putStringArray(WkMsgConstants.WAKEUP_WORDS_ARRAY_STR, cmds);
		sendMsg(WkMsgConstants.MSG_REQ_INIT_WITH_APP_ID,  b, true);//初始化消息需要插到消息队列最前面
		return 0;
	}

	@Override
	public int start(WakeupOption oOption) {
		mWakeupCallback = oOption.wakeupCallback;
		Bundle b = new Bundle();
		b.putInt(WkMsgConstants.ENGINE_TYPE_INT, getEngineType());
		b.putString(WkMsgConstants.APPID_STR,  ProjectCfg.getIflyAppId());
		b.putString(WkMsgConstants.APPKEY_STR,  ProjectCfg.getYunzhishengAppId());
		b.putString(WkMsgConstants.SECRET_STR,  ProjectCfg.getYunzhishengSecret());
		b.putInt(WkMsgConstants.WAKEUP_PROJECT_CFG_AEC_TYPE_INT, ProjectCfg.getFilterNoiseType());
	    b.putBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HOLE_BOOL, ProjectCfg.needBlackHole());
	    b.putBoolean(WkMsgConstants.WAKEUP_PROJECT_CFG_HQUALITY_MODEL_BOOL, ProjectCfg.mUseHQualityWakeupModel);
	    b.putLong(WkMsgConstants.WAKEUP_ARGUMENT_BEGIN_TIME, oOption.mBeginSpeechTime);
	    //当前是普通唤醒还是识别唤醒
		WakeupManager.getInstance().checkUsingAsr(new Runnable1<Bundle>(b){
			@Override
			public void run() {
				 mP1.putBoolean(WkMsgConstants.WAKEUP_ARGUMENT_IS_ASR_BOOL, true);
			}
		}, new Runnable1<Bundle>(b) {
			@Override
			public void run() {
				 mP1.putBoolean(WkMsgConstants.WAKEUP_ARGUMENT_IS_ASR_BOOL, false);
			}
		});
		sendMsg(WkMsgConstants.MSG_REQ_START, b);
		return 0;
	}

	@Override
	public int startWithRecord(IWakeupCallback oCallback, RecordOption options,
			String[] overTag) {
		
		return 0;
	}

	@Override
	public void stopWithRecord() {
		
		
	}

	@Override
	public void setWakeupKeywords(String[] keywords) {
		Bundle b = new Bundle();
		b.putStringArray(WkMsgConstants.WAKEUP_WORDS_ARRAY_STR, keywords);
		sendMsg(WkMsgConstants.MSG_REQ_SET_WORDS, b);
	}

	@Override
	public void setWakeupThreshold(float val) {
		Bundle b = new Bundle();
		b.putFloat(WkMsgConstants.WAKEUP_THRESHHOLD_FLOAT, val);
		sendMsg(WkMsgConstants.MSG_REQ_SET_THRESHOLD, b);
	}

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
