package com.txznet.music.fragment;

import java.util.List;
import java.util.Observer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.fragment.base.BaseFragment;

public abstract class BaseDataFragment<T> extends BaseFragment implements
		Observer {

	public final static int START_FLAG = 0;
	public final static int END_FLAG = 1;

	private class MyHandler extends Handler {
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.arg1) {
			case START_FLAG:
				BaseDataFragment.this.beginGetData();
				break;
			case END_FLAG:
				BaseDataFragment.this.notify((List<T>) msg.obj);
				break;
			}
			super.dispatchMessage(msg);
		}
	}
	private MyHandler handler=new MyHandler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		ObserverManage.getObserver().addObserver(this);
		super.onCreate(savedInstanceState);
	}
	
	
	protected void notifyData() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				handler.sendEmptyMessage(START_FLAG);
				Message msg = Message.obtain();
				msg.arg1 = END_FLAG;
				msg.obj = getDataFromLocal();
				handler.sendMessage(msg);
			}
		}).start();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		notifyData();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 获得数据
	 * 
	 * @return
	 */
	public abstract List<T> getDataFromLocal();

	/**
	 * 更新数据
	 * 
	 * @param t
	 */
	public abstract void notify(List<T> t);

	/**
	 * 开始获取数据
	 */
	public void beginGetData() {
	}

	@Override
	public void onDestroyView() {
		ObserverManage.getObserver().deleteObserver(this);
		super.onDestroyView();
	}

}
