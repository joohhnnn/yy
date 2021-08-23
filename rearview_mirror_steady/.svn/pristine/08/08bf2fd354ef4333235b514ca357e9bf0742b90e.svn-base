package com.txznet.nav.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.txznet.loader.AppLogic;
import com.txznet.nav.ui.RoutePlanActivity;

public class NetWorkReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mActiveInfo = cm.getActiveNetworkInfo();
		if(mActiveInfo != null){
			// 已经连接上网络
			notifyRoutePlan();
		}
	}
	
	private void notifyRoutePlan(){
		Intent intent = new Intent(RoutePlanActivity.ACTION_RECALCUTE);
		AppLogic.getApp().sendBroadcast(intent);
	}
}
