package com.txznet.music.engine.base;

import android.app.Activity;

//import android.support.v4.app.FragmentActivity;

public abstract class BaseEngine {

	protected Activity activity;
	
	
	public BaseEngine(Activity activity) {
		super();
		this.activity = activity;
	}



	public  interface DataOperater{
		public void onSuccess();//成功回调
		public void onFailure();//失败回调
	}
}
