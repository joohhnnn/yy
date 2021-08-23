package com.txznet.txz.ui.win.record;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.comm.ui.recordwin.RecordWin2;
import com.txznet.loader.AppLogic;
import com.txznet.record.ui.WinRecord;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.nav.SearchEditManager;
import com.txznet.txz.ui.win.nav.SelectCityDialog;
import com.txznet.txz.ui.win.record.RecorderWin.StatusObervable.StatusObserver;

import android.content.Intent;

/**
 * 监测WinRecord生命周期
 *
 */
public class WinRecordCycler {

	private Map<String, AsrComplexSelectCallback> mTaskAsrMap = new HashMap<String, AsrComplexSelectCallback>();
	private static WinRecordCycler mRecordCycler = new WinRecordCycler();

	public static WinRecordCycler getInstance() {
		return mRecordCycler;
	}

	private WinRecordCycler() {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				WinRecord.getInstance().setWinRecordObserver(mCycleObserver);
			}
		});
		RecorderWin.OBSERVABLE.registerObserver(new StatusObserver() {
			@Override
			public void onShow() {
				onWinShow();
			}
			@Override
			public void onDismiss() {
			}
		});
	}

	public void setWin2Observer() {
		RecordWin2.getInstance().setWinRecordObserver(mCycleObserver);
	}
	
	// 监测WinRecord的生命状态
	WinRecordCycleObserver mCycleObserver = new WinRecordCycleObserver() {

		@Override
		public void show() {
			SearchEditManager.getInstance().setNeedCloseDialog(true);
			SearchEditManager.getInstance().dismiss();
			onWinShow();
		}

		@Override
		public void loseFocus() {
			synchronized (mTaskAsrMap) {
				for (String taskId : mTaskAsrMap.keySet()) {
					WakeupManager.getInstance().recoverWakeupFromAsr(taskId);
				}
				if (mTaskAsrMap.size() > 0) {
					ChoiceManager.getInstance().clearTimeout();
				}
			}
		}

		@Override
		public void getFocus() {
			synchronized (mTaskAsrMap) {
				Set<Entry<String, AsrComplexSelectCallback>> asrSets = mTaskAsrMap.entrySet();
				for (Entry<String, AsrComplexSelectCallback> entry : asrSets) {
					WakeupManager.getInstance().useWakeupAsAsr(entry.getValue());
				}

				if (asrSets.size() > 0) {
					ChoiceManager.getInstance().checkTimeout(true);
				}
			}
		}

		@Override
		public void dismiss() {
		}
	};

	public void addAsrComplexSelectCallback(AsrComplexSelectCallback callback, String taskId) {
		synchronized (mTaskAsrMap) {
			mTaskAsrMap.put(taskId, callback);
		}
	}

	public void clearAsrComplexSelectCallback(String taskId) {
		synchronized (mTaskAsrMap) {
			mTaskAsrMap.remove(taskId);
		}
	}

	private void onWinShow() {
		// 关闭窗口
		Intent intent = new Intent(NavThirdApp.CLOSE_WIN_ACTION);
		GlobalContext.get().sendBroadcast(intent);
	}
}
