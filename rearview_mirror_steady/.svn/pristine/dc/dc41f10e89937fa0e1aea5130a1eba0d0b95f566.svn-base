package com.txznet.music.receiver;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.utils.NetHelp;
import com.txznet.music.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;

/**
 * @author telenewbie
 * @version 创建时间：2016年3月23日 上午10:44:56
 * 
 */
public class NetReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		NetworkInfo networkInfo = Utils.getNetworkInfo(context);
		LogUtil.logd("[MUSIC]networkInfo==" + (networkInfo==null?"null":networkInfo.getState()));
		if (networkInfo == null || !networkInfo.isAvailable()) {
//			TtsUtil.speakText("当前网络不可用");
		} else {
			AppLogic.runOnBackGround(NetHelp.requestAgain, 2000);
		}
	}

}
