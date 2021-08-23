package com.txznet.txz.ui.win.help;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil.AsrConfirmCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;

public class WinHelpDetail extends WinDialog {
	private static WinHelpDetail mInstance;

	public WinHelpDetail() {
		super(true);
		getWindow().setType(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 2);
		getWindow().setFormat(PixelFormat.OPAQUE);
	}
	
	private BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
		private static final String LOG_TAG = "HomeReceiver";
		private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
		private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
		private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
		private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
		private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(LOG_TAG, "onReceive: action: " + action);
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				// android.intent.action.CLOSE_SYSTEM_DIALOGS
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
					// 短按Home键
					dismiss();
				} else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
					// 长按Home键 或者 activity切换键
				} else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
					// 锁屏
				} else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
					// samsung 长按Home键
				}
			}
		}
	};
	
	private boolean mIsRegisted;
	
	@Override
	public void show() {
		super.show();
		if (!mIsRegisted) {
			mIsRegisted = true;
			initHelpList();
			getContext().sendBroadcast(new Intent("com.txznet.txz.action.WIN_HELP_DETAIL_SHOW"));
			IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
			getContext().registerReceiver(mHomeReceiver, intentFilter);
			registerCommand();
		}
	};
	
	private void initHelpList(){
		if(helpList!=null&&detailAdapter!=null&&detailAdapter.getGroupCount()>0){
			for(int i=0;i<detailAdapter.getGroupCount();i++){
				helpList.collapseGroup(i);
			}
			helpList.setSelectedGroup(0);
			detailAdapter.initHelpData(this.getContext());
			helpList.setAdapter(detailAdapter);
		}	
	}
	
	@Override
	protected void onStop() {
		unregisterCommand();
		super.onStop();
	}

	private int HELP_TTS_TASK = TtsManager.INVALID_TTS_TASK_ID;
	
	private void registerCommand(){
		HELP_TTS_TASK = TtsManager.getInstance().speakText(NativeData.getResString("RS_HELP_INTRO_HINT"));
		WakeupManager.getInstance().useWakeupAsAsr(asrConfirmCallback);
	}
	
	String[] cancel = new String[] { "取消","退出","返回" };
	String[] sure   = new String[] {};
	
	AsrConfirmCallback asrConfirmCallback = new AsrConfirmCallback(sure, cancel) {
		
		@Override
		public boolean needAsrState() {
			
			return false;
		}
		
		@Override
		public String getTaskId() {
			
			return TASK_HELP;
		}
		
		@Override
		public void onSure() {
			
		}
		
		@Override
		public void onCancel() {
			back();
		}
	};
	
	public static final String TASK_HELP = "WinHelpControl";
	
	private void unregisterCommand(){
		LogUtil.logi("unregisterCommand");
		TtsManager.getInstance().cancelSpeak(HELP_TTS_TASK);
		WakeupManager.getInstance().recoverWakeupFromAsr(TASK_HELP);
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
		if (mIsRegisted) {
			mIsRegisted = false;
			getContext().unregisterReceiver(mHomeReceiver);
			getContext().sendBroadcast(new Intent("com.txznet.txz.action.WIN_HELP_DETAIL_DISMISS"));
		}
	}

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static WinHelpDetail getInstance() {
		if (mInstance == null) {
			synchronized (WinHelpDetail.class) {
				if (mInstance == null) {
					mInstance = new WinHelpDetail();
				}
			}
		}
		return mInstance;
	}
	
	private void back(){
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.detail.back",null, null);
	}
	
	
	private ExpandableListView helpList;
	WinHelpDetailAdapter detailAdapter;
	@SuppressLint("InflateParams")
	@Override
	protected View createView() {
		mView = LayoutInflater.from(getContext()).inflate(R.layout.win_help_detail, null);
		View back = mView.findViewById(R.id.help_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				back();
			}
		});
		helpList = (ExpandableListView)mView.findViewById(R.id.list_help_detail);
		helpList.setSelector(R.drawable.selector_none);
		detailAdapter = new WinHelpDetailAdapter(this.getContext());
		helpList.setAdapter(detailAdapter);
		return mView;
	}
	
	@Override
	public void onWindowFocusChanged(boolean newFocus) {
		if(!newFocus){
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					WinHelpTops.getInstance().setCloseWhenPause();
					dismiss();
				}
			}, 0);
		}
		super.onWindowFocusChanged(newFocus);
	}
}
