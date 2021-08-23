package com.txznet.fm.manager;

import android.os.Bundle;

import com.txznet.fm.bean.InfoMessage;
import com.txznet.loader.AppLogic;

import java.util.Observable;

/**
 * 观察者管理类
 *
 * @author ASUS User
 */
public class ObserverManage extends Observable {

	private static ObserverManage mObserver = null;

	private ObserverManage() {
	}

	public static ObserverManage getObserver() {
		if (mObserver == null) {
			mObserver = new ObserverManage();
		}
		return mObserver;
	}

	public void setMessage(final Object data) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mObserver.setChanged();
				mObserver.notifyObservers(data);
			}
		}, 0);
	}

	/**
	 * 简单的传递信息
	 *
	 * @param type 需要传送的信息
	 */
	public void send(String type) {
		InfoMessage info = new InfoMessage();
		info.setType(type);
		ObserverManage.getObserver().setMessage(info);
	}

	public void send(String type, Object object) {
		InfoMessage info = new InfoMessage();
		info.setType(type);
		info.setObj(object);
		ObserverManage.getObserver().setMessage(info);
	}


	/**
	 * 简单的传递信息
	 *
	 * @param type 需要传送的信息
	 */
	public void send(String type, int errCode, String errMessage) {
		InfoMessage info = new InfoMessage();
		info.setType(type);
		info.setErrCode(errCode);
		info.setErrMessage(errMessage);
		ObserverManage.getObserver().setMessage(info);
	}

	public void send(String type, Bundle bundle) {
		InfoMessage info = new InfoMessage();
		info.setType(type);
		info.setObj(bundle);
		ObserverManage.getObserver().setMessage(info);
	}
}
