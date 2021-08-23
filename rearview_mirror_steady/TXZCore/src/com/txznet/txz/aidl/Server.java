package com.txznet.txz.aidl;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.txznet.comm.remote.util.LogUtil;

public abstract class Server {
	
	private class TxzBinder extends ITxzMessenger.Stub {

		@Override
		public void send(TxzMessage msg) throws RemoteException {
			mClient = msg.replyTo;
			handleClientMsg(msg);
		}
		
	}
	private HandlerThread mWorkThread = null;
	protected Handler mHandler= null;
	private ITxzMessenger mMessenger;
	private ITxzMessenger mClient;

	public Server() {
		super();
		// 使用子类的类名作为线程名，
    	mWorkThread = new HandlerThread(this.getClass().getSimpleName());
		mWorkThread.start();
		mHandler = new Handler(mWorkThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				handleMsg(msg);
			}
		};
		 
		mMessenger = new TxzBinder();
	}
	
	public ITxzMessenger getMessenger() {
		return mMessenger;
	}
	
	public IBinder getBinder() {
        return mMessenger.asBinder();
    }
	
	/**
	 * Handler的消息处理，如果有需要使用{@link #sendMessage(Message)}时，需要重写这个方法
	 * @param msg
	 */
	protected void handleMsg(Message msg) {
		
	}
	
	/**
	 * 发送Handler的消息，需要重写{@link #handleMsg(Message)}
	 * @param msg
	 */
	public void sendMsg(Message msg) {
		mHandler.sendMessage(msg);
	}
	
	public void postDelayed(Runnable r, long delayMillis) {
		mHandler.postDelayed(r, delayMillis);
	}

	/**
	 * 处理客户端发送的消息，同步方法
	 * @param msg
	 */
	protected abstract void handleClientMsg(TxzMessage msg);

	public void sendMsgToClient(int what, Bundle b) {
		TxzMessage msg = new TxzMessage();
		msg.what = what;
		msg.setData(b);
		try {
			if (mClient != null) {
				mClient.send(msg);
			}
		} catch (RemoteException e) {
			LogUtil.loge("Server send message error " + e.toString());
		}
	}

}
