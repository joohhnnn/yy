package com.txznet.txzcar.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.service.IService;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txzcar.MultiNavManager;
import com.txznet.txzcar.MyApplication;
import com.txznet.txzcar.NavManager;
import com.txznet.txzcar.ServiceAnalysisor;

public class MyService extends Service {
	public class TXZCarBinder extends IService.Stub {
		@Override
		public byte[] sendInvoke(String packageName, String command, byte[] data) throws RemoteException {
			ServiceHandler.preInvoke(packageName, command, data);
			
			if (command.equals("nav.action.startnavi")) {
				try {
					NavigateInfo info = NavigateInfo.parseFrom(data);
					if(info == null){
						return null;
					}
					
					if(info.msgServerPushInfo == null){
						return null;						
					}
					
					NavManager.getInstance().NavigateTo(info);
				} catch (Exception e) {
					LogUtil.loge("parse NavigateInfo fail!");
				}
			} else if (command.equals("nav.action.stopnavi")) {
				NavManager.getInstance().stopNavi();
			} else if(command.startsWith("nav.multi")){
				return invokeMultiNav(packageName, command, data);
			}
			
			return null;
		}
	}
	
	private byte[] invokeMultiNav(String packageName, String command, byte[] data){
		Log.d("MultiNav", "MultiNav -- > packageName:"+packageName + ",command:"+command);
		if(command.equals("nav.multi.roomin")){
			MyApplication.getApp().runOnBackGround(new Runnable1<byte[]>(data) {

				@Override
				public void run() {
					ServiceAnalysisor.analysisRoomin(mP1);
				}
			}, 0);
		}else if(command.equals("nav.multi.roomout")){
			MyApplication.getApp().runOnBackGround(new Runnable1<byte[]>(data) {

				@Override
				public void run() {
					ServiceAnalysisor.analysisRoomOut(mP1);
				}
			}, 0);
		}else if(command.equals("nav.multi.memlist")){
			MyApplication.getApp().runOnBackGround(new Runnable1<byte[]>(data) {
				
				@Override
				public void run() {
					ServiceAnalysisor.analysisMemberList(mP1);
				}
			}, 0);
		}else if(command.equals("nav.multi.update")){
			MyApplication.getApp().runOnBackGround(new Runnable1<byte[]>(data) {
				
				@Override
				public void run() {
					ServiceAnalysisor.analysisPushUpdate(mP1);
				}
			}, 0);
		}
		
		return null;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new TXZCarBinder();
	}
}
