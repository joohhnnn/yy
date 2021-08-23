package com.txznet.nav.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.nav.ui.AMapConfig;
import com.txznet.nav.util.DateUtils;

public class TimeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.logd("Thread:" + Thread.currentThread().getName());
		if (!DateUtils.isNight()) { // 白天
			AMapConfig.getInstance().setNaviNight(false);
		} else { // 夜晚
			AMapConfig.getInstance().setNaviNight(true);
		}
	}
}