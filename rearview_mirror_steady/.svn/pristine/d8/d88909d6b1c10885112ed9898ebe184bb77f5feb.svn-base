package com.txznet.nav;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.txznet.loader.AppLogic;
import com.txznet.nav.receiver.TimeReceiver;

public class TimeService extends Service {
	TimeReceiver mTimeObserver = new TimeReceiver();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		AppLogic.removeBackGroundCallback(mRegisterTimeService);
		AppLogic.runOnBackGround(mRegisterTimeService, 1000);
		return super.onStartCommand(intent, flags, startId);
	}

	Runnable mRegisterTimeService = new Runnable() {

		@Override
		public void run() {
			registerReceiver(mTimeObserver, new IntentFilter(
					Intent.ACTION_TIME_TICK));
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mTimeObserver);
	}
}
