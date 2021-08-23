package com.txznet.txz.ui.win.help;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.ui.win.record.RecordInvokeFactory;

public class WinHelpTops extends WinDialog {
	public enum INDEX {
		/**
		 * 通用帮助
		 */
		COMMON,
		/**
		 * 录音失败
		 */
		RECORD_FAILED,
		/**
		 * 无法识别时，本地引擎正常
		 */
		UNKNOW_LOCAL_OK,
		/**
		 * 无法识别时，本地引擎异常
		 */
		UNKNOW_LOCAL_ABORT,
		/**
		 * 无法识别时，本地引擎更新中
		 */
		UNKNOW_LOCAL_PROCESSING,
	};

	private boolean mCloseWhenLoseTop = true;

	public void setCloseWhenPause() {
		mCloseWhenLoseTop = true;
	}

	Runnable mAutoCloseTask = new Runnable() {
		@Override
		public void run() {
			if (isShowing() && mCloseWhenLoseTop) {
				dismiss();
			}
		}
	};

	private WinHelpTops() {
		super(true);
		getWindow().setType(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 2);
	}

	private static WinHelpTops sInstance;

	public static WinHelpTops getInstance() {
		if (sInstance == null) {
			synchronized (WinHelpTops.class) {
				if (sInstance == null) {
					sInstance = new WinHelpTops();
				}
			}
		}
		return sInstance;
	}

	private static final String ACTION_DISMISS = "com.txznet.txz.record.dismiss";
	private BroadcastReceiver mRecordStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_DISMISS)) {
				AppLogic.runOnUiGround(mAutoCloseTask, 1000 * 5);
			}
		}
	};

	private boolean mIsRegisted;
	
	
	private Runnable mCheckActTask = new Runnable() {
		@Override
		public void run() {
			String currAct = getCurrentTop();
			if((currAct == null || !currAct.equals(mActWhenShow)) && isShowing()){
				dismiss();
			}
			AppLogic.removeUiGroundCallback(mCheckActTask);
			AppLogic.runOnUiGround(mCheckActTask, 1000);
		}
	}; 

	private String mActWhenShow;
	@Override
	public void show() {
		if(true){
			return;
		}
	
		super.show();
		// 注册监听广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_DISMISS);
		GlobalContext.get().registerReceiver(mRecordStatusReceiver, intentFilter);
		mIsRegisted = true;
		// 查询当前活动的Activity
		mActWhenShow = getCurrentTop();
		AppLogic.runOnUiGround(mCheckActTask, 1000);
	}

	@Override
	public void dismiss() {
		if (RecordInvokeFactory.hasThirdImpl()) {
			return;
		}
		if (mIsRegisted) {
			GlobalContext.get().unregisterReceiver(mRecordStatusReceiver);
			mIsRegisted = false;
		}
		AppLogic.removeUiGroundCallback(mCheckActTask);
		super.dismiss();
	}

	public void show(final INDEX index) {
		if (RecordInvokeFactory.hasThirdImpl()) {
			return;
		}
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mWinData = index;
				setContentView(createViewRaw());
				show();
			}
		}, 0);
	}

	@Override
	public void onWindowFocusChanged(boolean newFocus) {
		if (!newFocus && mCloseWhenLoseTop) {
			dismiss();
		}
		super.onWindowFocusChanged(newFocus);
	}

	@Override
	protected View createView() {
		return new View(getContext());
	}
	
	private View createViewRaw(){
		if (mWinData == null)
			mWinData = INDEX.COMMON;
		INDEX index = (INDEX) mWinData;
		switch (index) {
		case RECORD_FAILED:
			mView = createView_RecordFailed(getContext());
			break;
		default:
			mView = createView_ShowBrief(getContext());
			break;
		}
		mView.findViewById(R.id.btnHelp_Close).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
		mCloseWhenLoseTop = true;
		return mView;
	}

	protected View createView_RecordFailed(Context context) {
		mView = LayoutInflater.from(context).inflate(
				R.layout.win_help_record_fail, null);
		TextView secure1 = (TextView) mView.findViewById(R.id.secure1);
		secure1.setText(Html
				.fromHtml("请设同行者为信任程序:在<font color='#34bfff'>手机安全设置</font>下选择同行者应用程序,"));
		TextView secure2 = (TextView) mView.findViewById(R.id.secure2);
		secure2.setText(Html.fromHtml("并将其设为<font color='#34bfff'>信任程序</font>"));
		return mView;
	}

	private Object mWinData;

	protected View createView_ShowBrief(Context context) {
		mView = LayoutInflater.from(context).inflate(R.layout.win_help_brief,
				null);

		if (mWinData == null)
			mWinData = INDEX.COMMON;
		INDEX index = (INDEX) mWinData;

		mView.findViewById(R.id.help_brief).setVisibility(View.GONE);
		mView.findViewById(R.id.help_local_ok).setVisibility(View.GONE);
		mView.findViewById(R.id.help_local_abort).setVisibility(View.GONE);
		mView.findViewById(R.id.help_local_proc).setVisibility(View.GONE);

		switch (index) {
		case UNKNOW_LOCAL_ABORT:
			mView.findViewById(R.id.help_local_abort).setVisibility(
					View.VISIBLE);
			TextView abortMsg = (TextView) mView
					.findViewById(R.id.txtHelpMsg_local_abort);
			StringBuilder abortMsgs = new StringBuilder();
			for (String msg : HelpMsgManager.getFailedMsgWhenLocalAbort()) {
				abortMsgs.append(msg + "\n");
			}
			abortMsg.setText(abortMsgs.toString());
			break;
		case UNKNOW_LOCAL_OK:
			mView.findViewById(R.id.help_local_ok).setVisibility(View.VISIBLE);
			TextView okMsg = (TextView) mView
					.findViewById(R.id.txtHelpMsg_local_ok);
			StringBuilder okMsgs = new StringBuilder();
			for (String msg : HelpMsgManager.getFailedMsgWhenLocalOk()) {
				okMsgs.append(msg + "\n");
			}
			okMsg.setText(okMsgs.toString());
			break;
		case UNKNOW_LOCAL_PROCESSING:
			mView.findViewById(R.id.help_local_proc)
					.setVisibility(View.VISIBLE);
			mView.findViewById(R.id.help_local_ok).setVisibility(View.VISIBLE);
			TextView procMsg = (TextView) mView
					.findViewById(R.id.txtHelpMsg_local_proc);
			StringBuilder procMsgs = new StringBuilder();
			for (String msg : HelpMsgManager.getFailedMsgWhenLocalProc()) {
				procMsgs.append(msg + "\n");
			}
			procMsg.setText(procMsgs.toString());
			break;
		case COMMON:
		default:
			mView.findViewById(R.id.help_brief).setVisibility(View.VISIBLE);
			LinearLayout li = (LinearLayout) mView
					.findViewById(R.id.help_brief_container);
			String[] helps = HelpMsgManager.getBriefMsgs();
			for (int i = 0; i < helps.length; i++) {
				TextView v = (TextView) LayoutInflater.from(context).inflate(
						R.layout.win_help_brief_textview, li, false);
				v.setText(helps[i]);
				li.addView(v);
			}
			break;
		}

		View btnDetail = mView.findViewById(R.id.btnHelp_Detail);
		if (btnDetail != null) {
			btnDetail.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// 打开帮助细节窗口
					mCloseWhenLoseTop = false;
					WinHelpDetail.getInstance().show();
				}
			});
		}
		return mView;
	}

	private String getCurrentTop() {
		try {
			ActivityManager manager = (ActivityManager) getContext()
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
			RunningTaskInfo runningTaskInfo = runningTasks.get(0);
			ComponentName topActivity = runningTaskInfo.topActivity;
			return topActivity.getPackageName();
		} catch (Exception e) {
		}
		return null;
	}
}