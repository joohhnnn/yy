package com.txznet.record.ui;

import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.record.bean.ChatMessage;
import com.txznet.reserve.activity.ReserveSingleInstanceActivity1;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

public class WinRecordImpl3 implements IWinRecord {
	
	private static WinRecordImpl3 sInstance = new WinRecordImpl3();

	public static WinRecordImpl3 getInstance() {
		return sInstance;
	}
	
	public boolean isShowing() {
		return isForeground(GlobalContext.get(), "com.txznet.reserve.activity.ReserveSingleInstanceActivity1");
	}

	
	private boolean isForeground(Context context, String className) {    
	       if (context == null || TextUtils.isEmpty(className)) {    
	           return false;    
	       }    
	       ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);    
	       List<RunningTaskInfo> list = am.getRunningTasks(1);    
	       if (list != null && list.size() > 0) {    
	           ComponentName cpn = list.get(0).topActivity;    
	           if (className.equals(cpn.getClassName())) {    
	               return true;    
	           }    
	       }    
	       return false;    
	   }   
	
	public void setIsFullSreenDialog(boolean isFullScreen) {
	}

	public void realInit() {

	}

	public void updateDialogType(int type) {
	}

	public void addMsg(ChatMessage chatMsg) {
		LogUtil.logd("addMsg");
		if (ReserveSingleInstanceActivity1.sInstance != null && isShowing()) {
			ReserveSingleInstanceActivity1.sInstance.addMsg(chatMsg);
			return;
		}
		if (isShowing()) {
			ReserveSingleInstanceActivity1.sPreLoadMsg = chatMsg;
		}
	}

	public void notifyUpdateVolume(int volume) {
		if (ReserveSingleInstanceActivity1.sInstance != null && isShowing()) {
			ReserveSingleInstanceActivity1.sInstance.notifyUpdateVolume(volume);;
		}
	}

	public void notifyUpdateLayout(int status) {
		if (ReserveSingleInstanceActivity1.sInstance != null && isShowing()) {
			ReserveSingleInstanceActivity1.sInstance.notifyUpdateLayout(status);
			return;
		}
		if (isShowing()) {
			ReserveSingleInstanceActivity1.sPreState = status;
		}
	}

	public void notifyUpdateProgress(int val, int selection) {
		if (ReserveSingleInstanceActivity1.sInstance != null && isShowing()) {
			ReserveSingleInstanceActivity1.sInstance.notifyUpdateProgress(val, selection);
		}
	}

	public void enableAnim(boolean enable) {
	}

	private static Integer sIntentFlags = null;

	public static void setIntentFlags(int flags){
		sIntentFlags = flags;
	}

	public void show() {
		Intent intent = new Intent(GlobalContext.get(), ReserveSingleInstanceActivity1.class);
		if (sIntentFlags != null) {
			intent.setFlags(sIntentFlags);
		} else {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		GlobalContext.get().startActivity(intent);
	}

	public void dismiss() {
		if (ReserveSingleInstanceActivity1.sInstance != null) {
			ReserveSingleInstanceActivity1.sInstance.finish();
		}
	}

	public void setWinRecordObserver(WinRecordCycleObserver observer) {
		ReserveSingleInstanceActivity1.setWinRecordObserver(observer);
	}

	@Override
	public void setWinFlags(int flags) {

	}

	@Override
	public void setWinType(int type) {

	}

	@Override
	public void newWinInstance() {

	}

	@Override
	public void setContentWidth(int width) {

	}

	@Override
	public void setIfSetWinBg(boolean ifSet) {
		
	}

	@Override
	public void updateDisplayArea(int x, int y, int width, int height) {

	}

	@Override
	public void setBannerAdvertisingView(View view) {

	}

	@Override
	public void removeBannerAdvertisingView() {

	}

	@Override
	public void setBackground(Drawable drawable) {

	}


	@Override
	public void setWinBgAlpha(Float winBgAlpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDialogCancel(boolean flag) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setSystemUiVisibility(int type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDialogCanceledOnTouchOutside(boolean cancel) {

	}

	@Override
	public void setWinSoft(int soft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAllowOutSideClickSentToBehind(boolean allow) {
		// TODO Auto-generated method stub
		
	}
}
