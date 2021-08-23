package com.txznet.music.utils;

import com.txznet.comm.remote.util.LogUtil;

import android.os.Handler;
import android.os.Message;

public abstract class MyAsyncTask<P, T> implements Runnable {

	public final static int START_FLAG = 0;
	public final static int END_FLAG = 1;
	private Handler handler;
	private P[] params;
	private Thread mThread;

	public MyAsyncTask() {
		handler = new Handler() {
			@Override
			public void dispatchMessage(Message msg) {
				switch (msg.arg1) {
				case START_FLAG:
					onPreExecute();
					break;
				case END_FLAG:
					onPostExecute((T) msg.obj);
					break;
				}
				super.dispatchMessage(msg);
			}
		};

	}

	protected void onPreExecute() {
	}

	protected void onPostExecute(T result) {
	}

	protected T doInBackground(P... params) {
		return null;
	}

	@Override
	public void run() {

		handler.sendEmptyMessage(START_FLAG);
		Message msg = Message.obtain();
		msg.arg1 = END_FLAG;
		msg.obj = doInBackground(params);
		handler.sendMessage(msg);

	}

	public MyAsyncTask<P, T> execute(P... params) {
		try {
			this.params = params;
			mThread = new Thread(this);
			mThread.start();
		} catch (Exception e) {
			e.printStackTrace();
			// 线程中断
		}
		return this;
	}

	public final boolean cancel(boolean mayInterruptIfRunning) {
		LogUtil.logd("[MUSIC][ASYNC]cancle");
		if (mThread != null) {
			mThread.interrupt();
			return true;
		} else {
			return false;
		}
	}

}
