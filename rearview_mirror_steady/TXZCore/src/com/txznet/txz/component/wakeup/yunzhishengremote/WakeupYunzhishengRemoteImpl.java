package com.txznet.txz.component.wakeup.yunzhishengremote;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.util.runnables.Runnable1;

public class WakeupYunzhishengRemoteImpl implements IWakeup {
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			procMsgQueue();
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			mInited = false;

			// 异常断开后3秒重新绑定，可能service异常crash了
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					bindService();
				}
			}, 3000);
		}
	};

	private void bindService() {
		try {
			Intent intent = new Intent(GlobalContext.get(), WakeupService.class);
			// for android 5.0
			intent.setPackage(ServiceManager.TXZ);
			GlobalContext.get().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
		} catch (Exception e) {
		}
	}

	static WakeupYunzhishengRemoteImpl sInstance = null;

	public WakeupYunzhishengRemoteImpl() {
		sInstance = this;
		bindService();
	}

	class ClientHandler extends Handler {
		public ClientHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			try {
				// JNIHelper.logd("recive msg.what=" + msg.what);

				Bundle b = msg.getData();

				switch (msg.what) {
				case WakeupService.MSG_NOTIFY_INIT_RESULT:
					boolean bSuccess = b.getBoolean("success");
					if (bSuccess) {
						setWakeupKeywords(mWakeupKeywords);
					}
					if (mInitCallback != null) {
						mInitCallback.onInit(bSuccess);
					}
					break;
				case WakeupService.MSG_NOTIFY_SET_WAKEUP_KEYWORDS_DONE:
					if (mWakeupCallback != null) {
						mWakeupCallback.onSetWordsDone();
					}
					break;
				case WakeupService.MSG_NOTIFY_WAKEUP_VOLUME:
					if (mWakeupCallback != null) {
						mWakeupCallback.onVolume(b.getInt("vol"));
					}
					break;
				case WakeupService.MSG_NOTIFY_WAKEUP_RESULT:
					if (mWakeupCallback != null) {
						mWakeupCallback.onWakeUp(b.getString("text"), 0f);
					}
					break;
				case WakeupService.MSG_DATA_WRITE_ASR_BUFFER:
					WakeupService.appendAsrBuffer(b.getByteArray("data"));
					break;
				case WakeupService.MSG_NOTIFY_WAKEUP_SPEECH_BEGIN:
					if (mWakeupCallback != null) {
						mWakeupCallback.onSpeechBegin();
					}
					break;
				case WakeupService.MSG_NOTIFY_WAKEUP_SPEECH_END:
					if (mWakeupCallback != null) {
						mWakeupCallback.onSpeechEnd();
					}
					break;

				}
			} catch (Exception e) {
			}
		}
	}

	final Messenger mMessenger = new Messenger(new ClientHandler(
			Looper.getMainLooper()));

	List<Message> mMsgQueue = new ArrayList<Message>();

	public void procMsgQueue() {
		if (mService != null) {
			for (Message m : mMsgQueue) {
				try {
					mService.send(m);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mMsgQueue.clear();
		}
	}

	public void sendMsg(int what, Bundle b) {
		Message msg = Message.obtain();
		msg.replyTo = mMessenger;
		msg.what = what;
		msg.setData(b);
		AppLogic.runOnUiGround(new Runnable1<Message>(msg) {
			@Override
			public void run() {
				mMsgQueue.add(mP1);
				procMsgQueue();
			}
		}, 0);
	}

	Messenger mService = null;
	String[] mWakeupKeywords = { "你好小踢" };
	boolean mInited = false;
	boolean mStarted = false;

	IInitCallback mInitCallback = null;
	IWakeupCallback mWakeupCallback = null;

	@Override
	public int initialize(String[] cmds, IInitCallback oRun) {
		mInitCallback = oRun;
		mWakeupKeywords = cmds;

		Bundle b = new Bundle();
		b.putString("appId", ProjectCfg.getYunzhishengAppId());
		b.putString("secret", ProjectCfg.getYunzhishengSecret());
		sendMsg(WakeupService.MSG_REQ_INIT_WITH_APP_ID, b);
		return 0;
	}

	@Override
	public int start(WakeupOption oOption) {
		mWakeupCallback = oOption.wakeupCallback;
		sendMsg(WakeupService.MSG_REQ_START, null);
		mStarted = true;
		return 0;
	}

	@Override
	public void stop() {
		mStarted = false;
		sendMsg(WakeupService.MSG_REQ_STOP, null);
	}

	@Override
	public void setWakeupKeywords(String[] keywords) {
		mWakeupKeywords = keywords;
		Bundle b = new Bundle();
		b.putStringArray("kws", mWakeupKeywords);
		sendMsg(WakeupService.MSG_REQ_SET_WAKEUP_KEYWORDS, b);
	}

	// 从识别模块读取录音数据
	public static void writeAsrBuffer(byte[] bs, int len) {
		if (bs != null && bs.length <= 0)
			return;
		if (sInstance == null || sInstance.mService == null
				|| sInstance.mStarted == false)
			return;
		Bundle b = new Bundle();
		if (bs == null || bs.length == len) {
			b.putByteArray("data", bs);
		} else {
			byte[] nbs = new byte[len];
			System.arraycopy(bs, 0, nbs, 0, len);
			b.putByteArray("data", nbs);
		}
		sInstance.sendMsg(WakeupService.MSG_DATA_WRITE_ASR_BUFFER, b);
	}

	@Override
	public int startWithRecord(IWakeupCallback oCallback,
			RecordOption options, String[] overTag) {
		mWakeupCallback = oCallback;
		Bundle b = new Bundle();
		b.putString("savePathPrefix", options.mSavePathPrefix);
		b.putStringArray("overTag", overTag);
		sendMsg(WakeupService.MSG_REQ_START_WITH_RECORD, b);
		mStarted = true;
		return 0;
	}

	@Override
	public void stopWithRecord() {
		mStarted = false;
		sendMsg(WakeupService.MSG_REQ_STOP_WITH_RECORD, null);
	}

	@Override
	public void setWakeupThreshold(float val) {
		Bundle b = new Bundle();
		b.putFloat("val", val);
		sendMsg(WakeupService.MSG_REQ_SET_WAKEUP_OPT_THRESHOLD, b);
	}

	@Override
	public void enableVoiceChannel(boolean enable) {
		// TODO Auto-generated method stub
		
	}
}
