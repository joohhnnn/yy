package com.txznet.loader;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.record.GlobalObservableSupport;
import com.txznet.record.ImageLoaderInitialize;

import android.app.Application;

public class AppLogic extends AppLogicBase {

	@Override
	public void onCreate() {
		super.onCreate();
		ImageLoaderInitialize.initImageLoader((Application)GlobalContext.get());
		GlobalObservableSupport.getHomeObservable();
	}
}
