package com.txznet.txz.component.asr.ifly;

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

import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;

public class BuildGrammarClient {
	Messenger mService = null;
	boolean mBound = false;
	boolean mReady = false;
	Context mContext;

	VoiceData.SdkKeywords mSdkKeywords = null;
	IImportKeywordsCallback mCallback = null;

	public BuildGrammarClient() {
		mContext = GlobalContext.get();
	}

	public void regKeywords(VoiceData.SdkKeywords sdkKeywords,
			IImportKeywordsCallback oCallback) {
		// TXZApp.printStatementCycle("");
		mSdkKeywords = sdkKeywords;
		mCallback = oCallback;
		mSuccessed = false;
		if (!mBound) {
			bindService();
		} else if (mReady) {
			sendRegMsg();
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// TXZApp.printStatementCycle("");
			mService = new Messenger(service);
			mBound = true;
			registerClient();
		}

		public void onServiceDisconnected(ComponentName className) {
			// TXZApp.printStatementCycle("");
			mService = null;
			mBound = false;
			mReady = false;
			if (mSuccessed)
				onSuccess();
			else
				onError();
		}
	};

	class ClientHandler extends Handler {
		public ClientHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			handleBackMsg(msg);
		}
	}

	final Messenger mMessenger = new Messenger(new ClientHandler(
			Looper.getMainLooper()));

	private void registerClient() {
		// TXZApp.printStatementCycle("");
		Message msg = Message.obtain();
		msg.what = MsgConst.MSG_REGISTER_CLIENT;
		msg.replyTo = mMessenger;
		Bundle b = new Bundle();
		b.putString("appId", ProjectCfg.getIflyAppId());
		msg.setData(b);
		sendMsg(msg);
	}

	public void bindService() {
		// TXZApp.printStatementCycle("");
		Intent intent = new Intent(mContext, BuildGrammarService.class);
		// for android 5.0
		intent.setPackage("com.txznet.txz");
		mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	public boolean sendMsg(Message msg) {
		if (mBound) {
			try {
				mService.send(msg);
				return true;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		onError();
		return false;
	}

	// 这个函数返回，service进程不能确保退出，可能会稍微滞后一点点
	public void unbind() {
		// TXZApp.printStatementCycle("");
		if (mBound) {
			mContext.unbindService(mConnection);
			mBound = false;
		}
	}

	private void sendRegMsg() {
		Message req = Message.obtain();
		req.what = MsgConst.MSG_REG_KEYWORDS;
		Bundle b = new Bundle();
		b.putByteArray("data", VoiceData.SdkKeywords.toByteArray(mSdkKeywords));
		req.setData(b);
		sendMsg(req);
		mReady = false;
	}

	public void handleBackMsg(Message msg) {
		if (mBound == false)
			return;
		if (msg.what == MsgConst.MSG_READY) {
			mReady = true;
			if (null != mSdkKeywords) {
				sendRegMsg();
			}
		} else if (msg.what == MsgConst.MSG_SUCCESS) {
			mSuccessed = true; // 设置标志
			Message req = Message.obtain();
			req.what = MsgConst.MSG_EXIT_SERVICE;
			sendMsg(req);
		}
	}

	void onError() {
		// TXZApp.printStatementCycle("");
		if (mSdkKeywords != null) {
			if (null != mCallback)
				mCallback.onError(0, mSdkKeywords);
			mSdkKeywords = null;
		}
	}

	boolean mSuccessed = false;

	void onSuccess() {
		// TXZApp.printStatementCycle("");
		if (mSdkKeywords != null) {
			if (null != mCallback)
				mCallback.onSuccess(mSdkKeywords);
			mSdkKeywords = null;
		}
	}
}
