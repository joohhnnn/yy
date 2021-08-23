package com.txznet.music.receiver;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.music.Constant;
import com.txznet.music.bean.req.ReqDataStats.Action;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.utils.NetHelp;

import android.app.Activity;
import android.content.Intent;

/**
 * 界面的操作类，支持退出，进入等操作
 * 
 * @author ASUS User
 *
 */
public class UIHelper {

	/**
	 * 退出播放器
	 */
	public static void exit() {
		LogUtil.logd("receive a broascast about exit");
		MediaPlayerActivityEngine.getInstance().exit();
		MediaPlayerActivityEngine.getInstance().clearState();
		NetHelp.sendReportData(Action.PAUSE_SOUND);
		MediaPlayerActivityEngine.getInstance().closeAllRunnable();
		ActivityStack.getInstance().exit();
		Constant.setIsExit(true);
	}

	/**
	 * 
	 * 打开界面
	 * 
	 * @param activity
	 */
	public static void open(Activity activity) {

	}

}
