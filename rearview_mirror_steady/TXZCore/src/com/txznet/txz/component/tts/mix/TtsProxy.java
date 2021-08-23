package com.txznet.txz.component.tts.mix;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.service.ReserveService3;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.runnables.Runnable2;

public class TtsProxy implements ITts {
	
	private HandlerThread mWorkThread = null;
	private Handler mHandler= null;
    private Messenger mService = null;
    private Messenger mMessenger = null;
    
    private ServiceConnection mConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			JNIHelper.logw("tts theme : TtsServer Connected");
			mService = new Messenger(service);
			procMsgQueue();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			JNIHelper.logw("tts theme : TtsServer Disconnected");
			mService = null;
			if (isBusy) {
				// 子进程断开连接
				onError(TtsMsgConstants.ERROR_CODE_SERVICE_DISCONNECTED);
			}
			if (mMsgQueue.size() > 0) {
				procMsgQueue();
			}
			isProcMsgQueue = true;
			
			// 防止退出进程后还是会创建一个进程
			// bindService();
		}
    	
    };
    
    private static TtsProxy sInstance = new TtsProxy();
    
	/**
	 * TTS 引擎代理类，创建对象之后需要调用{@link #setTtsEngineFilePath(String)}
	 */
	private TtsProxy() {
		mWorkThread = new HandlerThread("TtsProxy");
		mWorkThread.start();
		mHandler = new Handler(mWorkThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				handleMsg(msg);
			}
		};
		mMessenger = new Messenger(mHandler);
		bindService();
	}
	
	public static TtsProxy getInstance() {
		return sInstance;
	}
	
	private void bindService() {
		try {
			Intent intent = new Intent(GlobalContext.get(), ReserveService3.class);
			// for android 5.0
			intent.setPackage(ServiceManager.TXZ);
			GlobalContext.get().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
		} catch (Exception e) {
		}
	}

	private void handleMsg(Message msg) {
		Bundle bundle = msg.getData();
		switch (msg.what) {
		case TtsMsgConstants.MSG_NOTIFY_INIT_RESULT:
			boolean bInited = bundle.getBoolean(TtsMsgConstants.TTS_INIT_RESULT_BOOL, false);
			onInit(bInited);
			break;
		case TtsMsgConstants.MSG_NOTIFY_CALLBACKE_END:
			onEnd();
			break;
		case TtsMsgConstants.MSG_NOTIFY_CALLBACKE_CANCEL:
			// 屏蔽回调延时
//			onCancel();
			break;
		case TtsMsgConstants.MSG_NOTIFY_CALLBACKE_SUCCESS:
			onSuccess();
			break;
		case TtsMsgConstants.MSG_NOTIFY_CALLBACKE_ERROR:
			int errCode = bundle.getInt(TtsMsgConstants.TTS_CALLBACK_ERROR_INT);
			onError(errCode);
			break;
		}
	}
	
	private void onInit(boolean bSuccessed) {
		if (mInitCallback != null){
			/*IInitCallback callback = mInitCallback;
			mInitCallback = null;
			callback.onInit(bSuccessed);*/
			// 这里不能释放IInitCallback的引用，需要持续持有这个回调。
			// 因为子进程可能被杀死然后重启 TTS 引擎需要重新初始化，会有回调引擎
			mInitCallback.onInit(bSuccessed);
		}
	}

	private void onEnd() {
		isBusy = false;
		if (mTtsCallback != null) {
//			JNIHelper.logd("send message : callback from server : onEnd");
			ITtsCallback callback = mTtsCallback;
			mTtsCallback = null;
			callback.onEnd();
		}
	}

	private void onCancel() {
		isBusy = false;
		if (mTtsCallback != null) {
//			JNIHelper.logd("send message : callback from server : onCancel");
			ITtsCallback callback = mTtsCallback;
			mTtsCallback = null;
			callback.onCancel();
		}
	}

	private void onSuccess() {
		isBusy = false;
		if (mTtsCallback != null) {
//			JNIHelper.logd("send message : callback from server : onSuccess");
			ITtsCallback callback = mTtsCallback;
			mTtsCallback = null;
			callback.onSuccess();
		}
	}

	private void onError(int errCode) {
		isBusy = false;
		if (mTtsCallback != null) {
			ITtsCallback callback = mTtsCallback;
			mTtsCallback = null;
			callback.onError(errCode);
		}
	}

	private List<Message> mMsgQueue = new ArrayList<Message>();
	
	/**
	 * 是否处理消息队列
	 */
	private volatile boolean isProcMsgQueue = true;
	
	private void procMsgQueue() {
		if (mService != null) {
			synchronized (mMsgQueue) {
				for (Message m : mMsgQueue) {
					try {
//						JNIHelper.logd("send message : send to server : " + m.what);
						mService.send(m);
					} catch (RemoteException e) {
						JNIHelper.loge("tts theme : TtsProxy send message error " + e.toString());
					}
				}
				mMsgQueue.clear();
			}
		} else {
			bindService();
		}
	}
	
	private void sendMsg(int what, Bundle b) {
		Message msg = Message.obtain();
		msg.replyTo = mMessenger;
		msg.what = what;
		msg.setData(b);
//		JNIHelper.logd("send message : send message : " + msg.what);
		mHandler.postDelayed(new Runnable2<Message, Boolean>(msg, isProcMsgQueue) {
			@Override
			public void run() {
				synchronized (mMsgQueue) {
//					JNIHelper.logd("send message : add to queue : " + mP1.what);
					mMsgQueue.add(mP1);
				}
				if (mP2) {
					procMsgQueue();
				}
			}
		}, 0);
	}
	
	// tts外部引擎zip路径
	private String mTtsFilePath = null;
	
	public void setTtsEngineFilePath(String filePath) {
		mTtsFilePath = filePath;
	}
	
	private IInitCallback mInitCallback = null;
	private ITtsCallback mTtsCallback = null;
	private boolean isBusy = false;
	
	@Override
	public int initialize(final IInitCallback oRun) {
		if (TextUtils.isEmpty(mTtsFilePath)) {
			JNIHelper.loge("tts theme : ERROR : before invoke initialize() you should invoke setTtsEngineFilePath(not null) ");
			AppLogic.runOnBackGround(new Runnable() {
				
				@Override
				public void run() {
					if (oRun != null) {
						oRun.onInit(false);
					}
				}
			}, 0);
			return 0;
		}
		mInitCallback = oRun;
		Bundle b = new Bundle();
		// 只是为了兼容使用内部引擎代码中使用到的APPID，当外部TTS使用的引擎与内部TTS使用相同的引擎时，需要使用到这些APPID
		b.putString(TtsMsgConstants.APPID_STR,  ProjectCfg.getIflyAppId());
		b.putString(TtsMsgConstants.APPKEY_STR,  ProjectCfg.getYunzhishengAppId());
		b.putString(TtsMsgConstants.SECRET_STR,  ProjectCfg.getYunzhishengSecret());
		
		b.putString(TtsMsgConstants.TTS_PKT_FILE_PATH_STR, mTtsFilePath);
		sendMsg(TtsMsgConstants.MSG_REQ_INIT,b);
		return 0;
	}
	@Override
	public void release() {
		mTtsFilePath = null;
		sendMsg(TtsMsgConstants.MSG_REQ_RELEASE, null);
		isProcMsgQueue = false;
	}
	@Override
	public int start(int iStream, String sText, ITtsCallback oRun) {
		mTtsCallback = oRun;
		Bundle b = new Bundle();
		
		b.putString(TtsMsgConstants.APPID_STR,  ProjectCfg.getIflyAppId());
		b.putString(TtsMsgConstants.APPKEY_STR,  ProjectCfg.getYunzhishengAppId());
		b.putString(TtsMsgConstants.SECRET_STR,  ProjectCfg.getYunzhishengSecret());
		
		b.putString(TtsMsgConstants.TTS_PKT_FILE_PATH_STR, mTtsFilePath);
		b.putInt(TtsMsgConstants.TTS_START_STREAM_INT, iStream);
		b.putString(TtsMsgConstants.TTS_START_TEXT_STR, sText);
		sendMsg(TtsMsgConstants.MSG_REQ_START,b);
		isBusy = true;
		return 0;
	}
	@Override
	public void stop() {
		if (isBusy) {
			sendMsg(TtsMsgConstants.MSG_REQ_STOP, null);
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					onCancel();
				}
			},0);
		}
		isBusy = false;
	}
	@Override
	public boolean isBusy() {
		return isBusy;
	}
	
	
	@Override
	public int pause() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int resume() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void setTtsModel(String ttsModel) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int setLanguage(Locale loc) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void setVoiceSpeed(int speed) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getVoiceSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setOption(TTSOption oOption) {
		
	}

}
