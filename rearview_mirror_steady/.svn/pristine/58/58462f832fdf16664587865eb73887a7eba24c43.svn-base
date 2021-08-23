package com.txznet.txz.service;

import com.txznet.txz.jni.JNIHelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

public class StandbyManager {
	public static StandbyManager sIntance = new StandbyManager();
	private final static String HEARTBEAT_ACTION = "com.txznet.txz.standby.heartbeat";
	private BroadcastReceiver mReceiver = null;
	private boolean mInited =false;
	
	private StandbyManager(){
		
	}
	
	public synchronized static StandbyManager getInstance(){
		return sIntance;
	}
	
	public synchronized void init(Context context, final Runnable oRun){
		if (mInited){
			return;
		}
		
		JNIHelper.logd("heartbeat_alarm_init");
		mInited = true;
		mReceiver = new BroadcastReceiver(){
			boolean mFirstBroadcast = true;
			long mLastReceivedTime = 0;
			@Override
			public void onReceive(Context context, Intent intent) {
				long now = SystemClock.elapsedRealtime();
				JNIHelper.logd("heartbeat_alarm_receive : " + intent.getAction());
				if (now - mLastReceivedTime < 1000){
					return;
				}
				mLastReceivedTime = now;
				if (mFirstBroadcast){
					mFirstBroadcast = false;
					return;
				}
				JNIHelper.logd("heartbeat_alarm_receive_ok: " + intent.getAction());
				if (oRun != null){
					oRun.run();
				}
			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(HEARTBEAT_ACTION);
		context.registerReceiver(mReceiver, filter);
		
		Intent intent = new Intent(HEARTBEAT_ACTION);  
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);     
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);    
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 160*1000, pi); 
		
	}
	
}
