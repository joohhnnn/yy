package com.txznet.alldemo;

import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

public class MyApplication extends Application {
	private static MyApplication sApp = null;
    private Handler mUiHandler;
    private Handler mWorkHandler;
	@Override
	public void onCreate() {
		super.onCreate();
		sApp = this;
	}

	public static MyApplication getApp() {
		return sApp;
	}
    
	public void showMsg(final String msg){
		Runnable oRun = new Runnable(){

			@Override
			public void run() {
				Toast.makeText(getApp(), msg, Toast.LENGTH_SHORT).show();
			}
		};
		if (mUiHandler == null){
			return;
		}
		mUiHandler.postDelayed(oRun, 0);
	}
	
	public void runUiThread(Runnable oRun, long delay){
		if (mUiHandler != null){
			mUiHandler.postDelayed(oRun, delay);
		}
	}
	
	public void runWorkerThread(Runnable oRun, long delay){
		if (mWorkHandler != null){
			mWorkHandler.postDelayed(oRun, delay);
		}
	}
	
	public void removeUiThread(Runnable oRun){
		if (mUiHandler != null){
			mUiHandler.removeCallbacks(oRun);
		}
	}
	
	public void removeWorkerThread(Runnable oRun){
		if (mWorkHandler != null){
			mWorkHandler.removeCallbacks(oRun);
		}
	}
	
	
}
