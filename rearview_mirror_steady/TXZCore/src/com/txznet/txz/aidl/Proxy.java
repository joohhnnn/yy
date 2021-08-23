package com.txznet.txz.aidl;

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
import android.os.RemoteException;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.runnables.Runnable1;

public abstract class Proxy {
	
	private class TxzBinder extends ITxzMessenger.Stub {

		@Override
		public void send(TxzMessage msg) throws RemoteException {
			handleServerMsg(msg);
		}
		
	}
	
	class TxzServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ITxzMessenger.Stub.asInterface(service);
			doServiceConnected(name, service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			doServiceDisconnected(name);
		}
		
	}
	
	private TxzServiceConnection mConnection = null;
	
	private HandlerThread mWorkThread = null;
	private Handler mHandler= null;
	private ITxzMessenger mMessenger;
    private ITxzMessenger mService = null;
    
    /**
     * 子进程必须调用这个构造方法
     */
    public Proxy() {
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
		
		mConnection = new TxzServiceConnection();
		mMessenger = new TxzBinder();
    }
	
	/**
	 * Handler的消息处理，如果有需要使用{@link #sendMsg(Message)}时，需要重写这个方法
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
	
	public boolean bindService(String packageName, Class<?> cls) {
		try {
			Intent intent = new Intent(GlobalContext.get(), cls);
			// for android 5.0  ServiceManager.TXZ
			intent.setPackage(packageName);
			return GlobalContext.get().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean bindService(String packageName, String action) {
		try {
			Intent intent = new Intent(action);
			// for android 5.0  ServiceManager.TXZ
			intent.setPackage(packageName);
			return GlobalContext.get().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * @see #bindService(String, Class)
	 * @see #bindService(String, String)
	 */
	public abstract void bindService();
	
	protected void unbindService() {
		try {
			GlobalContext.get().unbindService(mConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mService = null;
	}

	protected List<TxzMessage> mMsgQueue = new ArrayList<TxzMessage>();

	protected void procMsgQueue() {
		synchronized (mMsgQueue) {
			while (mMsgQueue.size() > 0 && mService != null) {
				try {
					TxzMessage msg = mMsgQueue.get(0);
					mMsgQueue.remove(0);
					mService.send(msg);
				} catch (Exception e) {
					JNIHelper.loge("Proxy send message error " + e.toString());
					break;
				}
			}
		}
		if (mService == null) {
			bindService();
		}
	}

	public void sendMsgToServer(int what, Bundle b) {
		TxzMessage msg = new TxzMessage();
		msg.replyTo = mMessenger;
		msg.what = what;
		msg.setData(b);
		mHandler.postDelayed(new Runnable1<TxzMessage>(msg) {

			@Override
			public void run() {
				synchronized (mMsgQueue) {
					mMsgQueue.add(mP1);
				}
				procMsgQueue();
			}
		}, 0);
	}

	/**
	 * 处理服务端发送的消息，同步方法
	 * @param msg
	 */
	protected abstract void handleServerMsg(TxzMessage msg);

	/**
	 * 处理服务连接的回调
	 * @param name
	 * @param service
	 */
	protected abstract void doServiceConnected(ComponentName name, IBinder service);
	/**
	 * 处理服务断开连接的回调
	 * @param name
	 */
	protected abstract void doServiceDisconnected(ComponentName name);
}
