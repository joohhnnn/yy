package com.txznet.wakeup.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.txznet.wakeup.component.wakeup.IWakeup.IInitCallback;
import com.txznet.wakeup.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.wakeup.component.wakeup.iflytek.WakeupImpl;

public class WakeupService extends Service {
	
	public final static int MSG_REQ_INIT_WITH_APP_ID = 1; // 使用AppId初始化
	public final static int MSG_REQ_START = 2; // 启动唤醒
	public final static int MSG_REQ_STOP = 3; // 停止唤醒
	public final static int MSG_REQ_SET_WAKEUP_KEYWORDS = 4; // 设置唤醒词
	public final static int MSG_REQ_START_WITH_RECORD = 5; // 启动唤醒
	public final static int MSG_REQ_STOP_WITH_RECORD = 6; // 停止唤醒
	public final static int MSG_REQ_SET_WAKEUP_OPT_THRESHOLD = 7; //设置唤醒识别门限

	public final static int MSG_NOTIFY_INIT_RESULT = 100; // 初始化结果通知
	public final static int MSG_NOTIFY_SET_WAKEUP_KEYWORDS_DONE = 101; // 设置唤醒词结束通知

	public final static int MSG_NOTIFY_WAKEUP_VOLUME = 200; // 唤醒音量通知
	public final static int MSG_NOTIFY_WAKEUP_RESULT = 201; // 唤醒结果通知

	public final static int MSG_DATA_WRITE_ASR_BUFFER = 300; // 使用识别的录音数据进行唤醒
	
	public final static int MSG_NOTIFY_WAKEUP_SPEECH_BEGIN = 400; //检测到用户开始说话
	public final static int MSG_NOTIFY_WAKEUP_SPEECH_END = 401; // 检测到用户说话结束
	
	final Messenger mMessenger = new Messenger(new IncomingHandler(
			Looper.getMainLooper()));
	static Messenger mClient = null;

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	public static void sendMsg(Message msg) {
		if (mClient != null) {
			try {
				mClient.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	WakeupImpl mWakeup = null;
	String[] mWakeupKeywords = { "你好小踢" };
	boolean mInited = false;

	IWakeupCallback mWakeupCallback = new IWakeupCallback() {
		public void onSetWordsDone() {
			Message msg = Message.obtain();
			msg.what = MSG_NOTIFY_SET_WAKEUP_KEYWORDS_DONE;
			sendMsg(msg);
		};

		public void onVolume(int vol) {
			Message msg = Message.obtain();
			msg.what = MSG_NOTIFY_WAKEUP_VOLUME;
			Bundle b = new Bundle();
			b.putInt("vol", vol);
			msg.setData(b);
			sendMsg(msg);
		};

		public void onWakeUp(String text) {
			Message msg = Message.obtain();
			msg.what = MSG_NOTIFY_WAKEUP_RESULT;
			Bundle b = new Bundle();
			b.putString("text", text);
			msg.setData(b);
			sendMsg(msg);
		};
		
		public void onSpeechBegin(){
			Message msg = Message.obtain();
			msg.what = MSG_NOTIFY_WAKEUP_SPEECH_BEGIN;
			sendMsg(msg);
		}
		
		public void onSpeechEnd(){
			Message msg = Message.obtain();
			msg.what = MSG_NOTIFY_WAKEUP_SPEECH_END;
			sendMsg(msg);
		}
		
	};

	IInitCallback mInitCallback = new IInitCallback() {
		@Override
		public void onInit(boolean bSuccess) {
			mInited = bSuccess;

			Message msg = Message.obtain();
			msg.what = MSG_NOTIFY_INIT_RESULT;
			Bundle b = new Bundle();
			b.putBoolean("success", bSuccess);
			msg.setData(b);
			sendMsg(msg);
		}
	};

	static byte[] mAsrBuffer = null;

	public static Integer readAsrBuffer(byte[] buffer, int size) {
		synchronized (WakeupService.class) {
			if (mAsrBuffer == null)
				return null;
			int read = size;
			if (mAsrBuffer.length < size)
				read = mAsrBuffer.length;
			System.arraycopy(mAsrBuffer, 0, buffer, 0, read);
			byte[] nbs = new byte[mAsrBuffer.length - read];
			if (mAsrBuffer.length > read)
				System.arraycopy(mAsrBuffer, read, nbs, 0, mAsrBuffer.length
						- read);
			mAsrBuffer = nbs;
			return read;
		}
	}

	public static void appendAsrBuffer(byte[] buffer) {
		synchronized (WakeupService.class) {
			if (buffer == null) {
				mAsrBuffer = null;
				// JNIHelper.logd("write data from remote: end");
				return;
			}
			if (mAsrBuffer == null) {
				mAsrBuffer = buffer;
			} else {
				byte[] nbs = new byte[mAsrBuffer.length + buffer.length];
				if (mAsrBuffer.length > 0)
					System.arraycopy(mAsrBuffer, 0, nbs, 0, mAsrBuffer.length);
				if (buffer.length > 0)
					System.arraycopy(buffer, 0, nbs, mAsrBuffer.length,
							buffer.length);
				mAsrBuffer = nbs;
			}
			// JNIHelper.logd("write data from remote: " + mAsrBuffer.length);
		}
	}

	class IncomingHandler extends Handler {
		public IncomingHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			mClient = msg.replyTo;
			Bundle b = msg.getData();

			// JNIHelper.logd("recvice msg.what=" + msg.what);

			try {
				switch (msg.what) {
				case MSG_REQ_INIT_WITH_APP_ID:
					if (mWakeup == null) {
						mWakeup = new WakeupImpl();
						mWakeup.initialize(mWakeupKeywords, mInitCallback);
					}
					break;
				case MSG_REQ_SET_WAKEUP_KEYWORDS:
					mWakeupKeywords = b.getStringArray("kws");
					if (mInited) {
						mWakeup.setWakeupKeywords(mWakeupKeywords);
					}
					break;
				case MSG_REQ_START:
					if (mInited) {
						mWakeup.start(mWakeupCallback);
					}
					break;
				case MSG_REQ_STOP:
					if (mInited) {
						mWakeup.stop();
					}
					break;
				case MSG_REQ_START_WITH_RECORD:
					if (mInited) {
						String savePathPrefix = b.getString("savePathPrefix");
						String[] overTag = b.getStringArray("overTag");
						mWakeup.startWithRecord(mWakeupCallback, savePathPrefix, overTag);
					}
					break;
				case MSG_REQ_STOP_WITH_RECORD:
					if (mInited) {
						mWakeup.stopWithRecord();
					}
					break;
				case MSG_REQ_SET_WAKEUP_OPT_THRESHOLD:
					if (mInited) {
						mWakeup.setWakeupThreshold(b.getFloat("val"));
					}
					break;
				case MSG_DATA_WRITE_ASR_BUFFER:
					appendAsrBuffer(b.getByteArray("data"));
					break;
				}
			} catch (Exception e) {
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public static void writeAsrBuffer(byte[] bs, int len) {
		if (bs != null && bs.length <= 0)
			return;
		if (mClient == null)
			return;
		Bundle b = new Bundle();
		if (bs == null || bs.length == len) {
			b.putByteArray("data", bs);
		} else {
			byte[] nbs = new byte[len];
			System.arraycopy(bs, 0, nbs, 0, len);
			b.putByteArray("data", nbs);
		}
		Message m = new Message();
		m.what = WakeupService.MSG_DATA_WRITE_ASR_BUFFER;
		m.setData(b);
		sendMsg(m);
	}
}
