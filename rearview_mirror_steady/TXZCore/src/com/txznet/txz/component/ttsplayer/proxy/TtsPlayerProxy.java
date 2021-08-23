package com.txznet.txz.component.ttsplayer.proxy;

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
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.reserve.service.ReserveService5;
import com.txznet.sdk.TXZTtsPlayerManager;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.runnables.Runnable2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TtsPlayerProxy implements ITts {
	
	private HandlerThread mWorkThread = null;
	private Handler mHandler= null;
    private Messenger mService = null;
    private Messenger mMessenger = null;
    
    private ServiceConnection mConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			JNIHelper.logw("TtsPlayerServer Connected");
			mService = new Messenger(service);
			procMsgQueue();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			JNIHelper.logw("TtsPlayerServer Disconnected");
			mService = null;
			if (isBusy) {
				// 子进程断开连接
				onError(TtsPlayerMsgConstants.ERROR_CODE_SERVICE_DISCONNECTED);
			}
			if (mMsgQueue.size() > 0) {
				procMsgQueue();
			}
			isProcMsgQueue = true;
			
			// 防止退出进程后还是会创建一个进程
			// bindService();
		}
    	
    };
    
    private static TtsPlayerProxy sInstance = new TtsPlayerProxy();
    
	/**
	 *
	 */
	private TtsPlayerProxy() {
		mWorkThread = new HandlerThread("TtsPlayerProxy");
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
	
	public static TtsPlayerProxy getInstance() {
		return sInstance;
	}
	
	private void bindService() {
		try {
			Intent intent = new Intent(GlobalContext.get(), ReserveService5.class);
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
		case TtsPlayerMsgConstants.MSG_NOTIFY_INIT_RESULT:
			boolean bInited = bundle.getBoolean(TtsPlayerMsgConstants.TTS_INIT_RESULT_BOOL, false);
			onInit(bInited);
			break;
		case TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_END:
			onEnd();
			break;
		case TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_CANCEL:
			// 屏蔽回调延时
//			onCancel();
			break;
		case TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_SUCCESS:
			onSuccess();
			break;
		case TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_ERROR:
			int errCode = bundle.getInt(TtsPlayerMsgConstants.TTS_CALLBACK_ERROR_INT);
			onError(errCode);
			break;
		case TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_RESUME:
			onResume();
			break;
		case TtsPlayerMsgConstants.MSG_NOTIFY_CALLBACKE_PAUSE:
			onPause();
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
			TXZTtsPlayerManager.ITtsCallback callback = mTtsCallback;
			mTtsCallback = null;
			callback.onEnd();
		}
	}

	private void onCancel() {
		isBusy = false;
		if (mTtsCallback != null) {
//			JNIHelper.logd("send message : callback from server : onCancel");
			TXZTtsPlayerManager.ITtsCallback callback = mTtsCallback;
			mTtsCallback = null;
			callback.onCancel();
		}
	}

	private void onSuccess() {
		isBusy = false;
		if (mTtsCallback != null) {
//			JNIHelper.logd("send message : callback from server : onSuccess");
			TXZTtsPlayerManager.ITtsCallback callback = mTtsCallback;
			mTtsCallback = null;
			callback.onSuccess();
		}
	}

	private void onError(int errCode) {
		isBusy = false;
		if (mTtsCallback != null) {
			TXZTtsPlayerManager.ITtsCallback callback = mTtsCallback;
			mTtsCallback = null;
			callback.onError(errCode);
		}
	}

	private void onPause() {
		if (mTtsCallback != null) {
			TXZTtsPlayerManager.ITtsCallback callback = mTtsCallback;
			callback.onPause();
		}
	}

	private void onResume() {
		if (mTtsCallback != null) {
			TXZTtsPlayerManager.ITtsCallback callback = mTtsCallback;
			callback.onResume();
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
						JNIHelper.loge("TtsPlayerProxy send message error " + e.toString());
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
	
	private IInitCallback mInitCallback = null;
	private TXZTtsPlayerManager.ITtsCallback mTtsCallback = null;
	private boolean isBusy = false;
	
	@Override
	public int initialize(final IInitCallback oRun) {

		mInitCallback = oRun;
		Bundle b = new Bundle();
		// 只是为了兼容使用内部引擎代码中使用到的APPID，当外部TTS使用的引擎与内部TTS使用相同的引擎时，需要使用到这些APPID
		b.putString(TtsPlayerMsgConstants.APPKEY_STR,  ProjectCfg.getYunzhishengAppId());
		b.putString(TtsPlayerMsgConstants.SECRET_STR,  ProjectCfg.getYunzhishengSecret());

		sendMsg(TtsPlayerMsgConstants.MSG_REQ_INIT,b);
		return 0;
	}
	@Override
	public void release() {
		sendMsg(TtsPlayerMsgConstants.MSG_REQ_RELEASE, null);
		isProcMsgQueue = false;
	}
	@Override
	public int start(int iStream, String sText, TtsUtil.ITtsCallback oRun) {
		mTtsCallback = (TXZTtsPlayerManager.ITtsCallback) oRun;
		Bundle b = new Bundle();

		b.putString(TtsPlayerMsgConstants.APPKEY_STR,  ProjectCfg.getYunzhishengAppId());
		b.putString(TtsPlayerMsgConstants.SECRET_STR,  ProjectCfg.getYunzhishengSecret());

		b.putInt(TtsPlayerMsgConstants.TTS_START_STREAM_INT, iStream);
		b.putString(TtsPlayerMsgConstants.TTS_START_TEXT_STR, sText);
		sendMsg(TtsPlayerMsgConstants.MSG_REQ_START,b);
		isBusy = true;
		return 0;
	}
	@Override
	public void stop() {
		if (isBusy) {
			sendMsg(TtsPlayerMsgConstants.MSG_REQ_STOP, null);
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
		sendMsg(TtsPlayerMsgConstants.MSG_REQ_PAUSE, null);
		return 0;
	}
	@Override
	public int resume() {
		sendMsg(TtsPlayerMsgConstants.MSG_REQ_RESUME, null);
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
