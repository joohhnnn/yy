package com.txznet.fm.manager;

import android.os.Bundle;

import java.util.Observable;

import com.txznet.fm.bean.InfoMessage;
import com.txznet.loader.AppLogic;

/**
 * 观察者管理类
 * 
 * @author ASUS User
 *
 */
public class ObserverManage extends Observable {

	private ObserverManage() {
	}

	private static ObserverManage myobserver = null;

	public static ObserverManage getObserver() {
		if (myobserver == null) {
			myobserver = new ObserverManage();
		}
		return myobserver;
	}

	public void setMessage(final Object data) {
		AppLogic.runOnUiGround(new Runnable() {
			
			@Override
			public void run() {
				myobserver.setChanged();
				myobserver.notifyObservers(data);
			}
		}, 0);
	}

	/**
	 * 简单的传递信息
	 * 
	 * @param type
	 *            需要传送的信息
	 */
	public void send(int type) {
		InfoMessage info = new InfoMessage();
		info.setType(type);
		ObserverManage.getObserver().setMessage(info);
	}
	
	public  void send(int type ,Object object){
		InfoMessage info =new InfoMessage();
		info.setType(type);
		info.setObj(object);
		ObserverManage.getObserver().setMessage(info);
	}
	

	/**
	 * 简单的传递信息
	 * 
	 * @param type
	 *            需要传送的信息
	 */
	public void send(int type, int errCode, String errMessage) {
		InfoMessage info = new InfoMessage();
		info.setType(type);
		info.setErrCode(errCode);
		info.setErrMessage(errMessage);
		ObserverManage.getObserver().setMessage(info);
	}

	public  void   send(int type , Bundle bundle){
		InfoMessage info = new InfoMessage();
		info.setType(type);
		info.setObj(bundle);
		ObserverManage.getObserver().setMessage(info);
	}
}
