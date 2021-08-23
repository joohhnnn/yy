package com.txznet.txz.ui.win.record;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Intent;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.record.ui.WinRecord;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.selector.SelectorHelper;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.nav.SearchEditDialog;
import com.txznet.txz.ui.win.record.RecorderWin.StatusObervable.StatusObserver;

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
		WinRecord.getInstance().setWinRecordObserver(mCycleObserver);
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

	// 监测WinRecord的生命状态
	WinRecordCycleObserver mCycleObserver = new WinRecordCycleObserver() {

		@Override
		public void show() {
			JNIHelper.logd(" >>> WinRecordCycle show");
			SearchEditDialog.getInstance().setNeedCloseDialog(true);
			SearchEditDialog.getInstance().dismiss();
			onWinShow();
		}

		@Override
		public void loseFocus() {
			JNIHelper.logd("WinRecordCycle loseFocus");
			for (String taskId : mTaskAsrMap.keySet()) {
				WakeupManager.getInstance().recoverWakeupFromAsr(taskId);
			}
			if (mTaskAsrMap.size() > 0) {
				SelectorHelper.removeDismissTask();
			}
		}

		@Override
		public void getFocus() {
			JNIHelper.logd("WinRecordCycle getFocus");
			Set<Entry<String, AsrComplexSelectCallback>> asrSets = mTaskAsrMap
					.entrySet();
			for (Entry<String, AsrComplexSelectCallback> entry : asrSets) {
				WakeupManager.getInstance().useWakeupAsAsr(entry.getValue());
			}

			if (asrSets.size() > 0) {
				SelectorHelper.onResumeDelayTask();
			}
		}

		@Override
		public void dismiss() {
			JNIHelper.logd(" >>> WinRecordCycle dismiss");
		}
	};

	public void addAsrComplexSelectCallback(AsrComplexSelectCallback callback,
			String taskId) {
		if (mTaskAsrMap.containsKey(taskId)) {
			return;
		}

		mTaskAsrMap.put(taskId, callback);
	}

	public void clearAsrComplexSelectCallback(String taskId) {
		mTaskAsrMap.remove(taskId);
	}

	private void onWinShow() {
		// 关闭窗口
		Intent intent = new Intent(NavThirdApp.CLOSE_WIN_ACTION);
		GlobalContext.get().sendBroadcast(intent);
	}
}
