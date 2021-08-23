package com.txznet.txz.module.music.sdcard;

import com.txz.ui.event.UiEvent;
import com.txz.ui.music.UiMusic;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.music.AndroidMediaLibrary;
import com.txznet.txz.module.music.MusicManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MusicSdcardManager {
	private static final String TAG = "sdcard:";
	static Runnable mRunnableRefreshMediaList = new Runnable() {
		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(mRunnableRefreshMediaList);
			LogUtil.logd(MusicManager.TAG + TAG + "begin refresh media list");
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NEED_REFRESH_MEDIA_LIST);
			AndroidMediaLibrary.refreshSystemMedia();
		}
	};

	static BroadcastReceiver mRecviceSdcardEvent = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtil.logd(MusicManager.TAG + TAG + "recive sdcard event: "+ intent.getAction() + " enableScan=" + AndroidMediaLibrary.enableScanMediaLibrary);
			if (AndroidMediaLibrary.enableScanMediaLibrary) {
				AppLogic.removeBackGroundCallback(mRunnableRefreshMediaList);
				AppLogic.runOnBackGround(mRunnableRefreshMediaList, 2000);
			}
		}
	};

	public static void registerSdcardListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_SHARED);// 如果SDCard未安装,并通过USB大容量存储共享返回
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);// 表明sd对象是存在并具有读/写权限
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// SDCard已卸掉,如果SDCard是存在但没有被安装
		filter.addAction(Intent.ACTION_MEDIA_CHECKING); // 表明对象正在磁盘检查
		filter.addAction(Intent.ACTION_MEDIA_EJECT); // 物理的拔出 SDCARD
		filter.addAction(Intent.ACTION_MEDIA_REMOVED); // 完全拔出
		filter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
		GlobalContext.get().registerReceiver(mRecviceSdcardEvent, filter);
	}

}
