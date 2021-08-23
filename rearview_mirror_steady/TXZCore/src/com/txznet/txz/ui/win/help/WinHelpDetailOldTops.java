package com.txznet.txz.ui.win.help;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil.AsrConfirmCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

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
import android.widget.ListView;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class WinHelpDetailOldTops extends WinDialog {
	private static WinHelpDetailOldTops mInstance;

	public WinHelpDetailOldTops() {
		super(new WinDialog.DialogBuildData().setWindowType(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 3),true);
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (mDialog != null ) {
					mDialog.getWindow().setFormat(PixelFormat.OPAQUE);
				}
			}
		});
	}
		
	BroadcastReceiver mWinRecordReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if("com.txznet.txz.record.show".equals(intent.getAction())){
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						ReportUtil.doReport(new ReportUtil.Report.Builder()
								.setAction("helplist")
								.setType("close")
								.putExtra("closetype",WinHelpManager.TYPE_CLOSE_FROM_OTHER)
								.setSessionId()
								.buildCommReport());
						dismiss();
					}
				}, 0);
			}
		}
	};

	private HomeObservable.HomeObserver mHomeReceiver = new HomeObservable.HomeObserver() {
		@Override
		public void onHomePressed() {
			// 短按Home键
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					dismiss();
				}
			}, 0);
			// 数据上报
			ReportUtil.doReport(new ReportUtil.Report.Builder()
					.setAction("helplist")
					.setType("close")
					.putExtra("closetype",WinHelpManager.TYPE_CLOSE_FROM_CLICK)
					.setSessionId()
					.buildCommReport());
		}
	};
	
	private boolean mIsRegisted; 

	@Override
	public void onShow() {
		super.show();
		if (!mIsRegisted) {
			mIsRegisted = true;
			initHelpList();
			getContext().sendBroadcast(new Intent("com.txznet.txz.action.WIN_HELP_DETAIL_SHOW"));
			IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
			GlobalObservableSupport.getHomeObservable().registerObserver(mHomeReceiver);
			getContext().registerReceiver(mWinRecordReceiver, new IntentFilter("com.txznet.txz.record.show"));
			registerCommand();
		}
	};
	
	private void initHelpList(){
		if(helpList!=null&&detailAdapter!=null&&detailAdapter.getCount()>0){
//			for(int i=0;i<detailAdapter.getCount();i++){
//				helpList.collapseGroup(i);
//			}
//			helpList.setSelectedGroup(0);
			
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
	protected void onDismiss() {
		super.onDismiss();
		if(mIsRegisted){
			GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeReceiver);
			getContext().unregisterReceiver(mWinRecordReceiver);
			//unregisterCommand();
			mIsRegisted = false;
		}
		getContext().sendBroadcast(new Intent("com.txznet.txz.action.WIN_HELP_DETAIL_DISMISS"));
	}

	public void dismiss() {
		super.dismiss("do dismiss");
	}

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static WinHelpDetailOldTops getInstance() {
		if (mInstance == null) {
			synchronized (WinHelpDetailOldTops.class) {
				if (mInstance == null) {
					mInstance = new WinHelpDetailOldTops();
				}
			}
		}
		return mInstance;
	}
	
	private void back(){
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.detail.back",null, null);
	}
	
	
	private ListView helpList;
	WinHelpDetailOldAdapter detailAdapter;
	@SuppressLint("InflateParams")
	@Override
	protected View createView() {
		mView = LayoutInflater.from(getContext()).inflate(R.layout.win_help_detail_old, null);
		View back = mView.findViewById(R.id.help_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				back();
				// 数据上报
				ReportUtil.doReport(new ReportUtil.Report.Builder()
						.setAction("helplist")
						.setType("close")
						.putExtra("closetype",WinHelpManager.TYPE_CLOSE_FROM_CLICK)
						.setSessionId()
						.buildCommReport());
			}
		});
		helpList = (ListView)mView.findViewById(R.id.list_help_detail);
		helpList.setSelector(R.drawable.selector_none);
//		helpList.setOnGroupExpandListener(new OnGroupExpandListener() {
//			@Override
//			public void onGroupExpand(int groupPosition) {
//				// 数据上报
//				ReportUtil.doReport(new ReportUtil.Report.Builder().setType("helper").setAction("view")
//						.putExtra("index", groupPosition).buildTouchReport());
//			}
//		});
		detailAdapter = new WinHelpDetailOldAdapter(this.getContext());
		//detailAdapter.registerDataSetObserver(observer);
		helpList.setAdapter(detailAdapter);
		detailAdapter.initHelpData(getContext());
		return mView;
	}

	@Override
	public String getReportDialogId() {
		return "win_help_third_1";
	}
}
