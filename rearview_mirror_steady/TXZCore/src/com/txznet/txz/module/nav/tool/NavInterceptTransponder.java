package com.txznet.txz.module.nav.tool;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.StatusObervable.StatusObserver;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

/**
 * 导航拦截转发器
 */
public class NavInterceptTransponder {

	/**
	 * @param <F>
	 *            Intent or Bundle
	 */
	private class TransponderRecord<F> {
		public class Param<T> {
			public T intent;
			public long time;
			public boolean isForeground;
		}

		public NavThirdApp navApp;
		public List<Param> entryList = new ArrayList<Param>();

		public TransponderRecord(NavThirdApp navThirdApp) {
			this.navApp = navThirdApp;
		}

		private void sendInner() {
			synchronized (entryList) {
				if (entryList.size() <= 0) {
					return;
				}

				Param<F> nextParam = null;
				while (entryList.size() > 0) {
					nextParam = entryList.remove(0);
					LogUtil.logd("transponder sendInner");
					if (nextParam.intent instanceof Intent) {
						navApp.handleIntent((Intent) nextParam.intent);
					} else if (nextParam.intent instanceof Bundle) {
						navApp.handleBundle((Bundle) nextParam.intent);
					}
				}
			}
		}

		public void resend() {
			sendInner();
		}

		public void addIntentToList(Intent intent, boolean isForeground) {
			Param<Intent> param = new Param<Intent>();
			param.time = SystemClock.elapsedRealtime();
			param.intent = intent;
			param.isForeground = isForeground;
			synchronized (entryList) {
				entryList.add(param);
				LogUtil.logd("intercept addIntent:" + intent.getExtras());
			}
		}

		public void addBundleToList(Bundle bundle, boolean isForeground) {
			Param<Bundle> param = new Param<Bundle>();
			param.time = SystemClock.elapsedRealtime();
			param.intent = bundle;
			param.isForeground = isForeground;
			synchronized (entryList) {
				entryList.add(param);
				LogUtil.logd("intercept addBundle:" + bundle);
			}
		}
	}

	private List<TransponderRecord> mRecords = new ArrayList<TransponderRecord>();

	private static NavInterceptTransponder sTransponder = new NavInterceptTransponder();

	public static NavInterceptTransponder getInstance() {
		return sTransponder;
	}
	
	public void init() {
		RecorderWin.OBSERVABLE.registerObserver(new StatusObserver() {
			
			@Override
			public void onShow() {
			}
			
			@Override
			public void onDismiss() {
				AppLogic.runOnBackGround(new Runnable() {
					
					@Override
					public void run() {
						synchronized (mRecords) {
							for (TransponderRecord record : mRecords) {
								record.resend();
							}

							mRecords.clear();
						}
					}
				}, 500);
			}
		});
	}

	public boolean interceptGroundIntent(NavThirdApp navThirdApp, Intent intent,boolean isForeground) {
		if (RecorderWin.isOpened() && WinManager.getInstance().isActivityDialog()) {
			synchronized (mRecords) {
				boolean found = false;
				for (TransponderRecord<Intent> record : mRecords) {
					if (record.navApp == navThirdApp) {
						found = true;
						record.addIntentToList(intent, isForeground);
						break;
					}
				}
				if (!found) {
					TransponderRecord<Intent> record = new TransponderRecord<Intent>(navThirdApp);
					record.addIntentToList(intent, isForeground);
					mRecords.add(record);
				}
			}
			return true;
		} else if (WinManager.getInstance().isActivityDialog() && !mRecords.isEmpty() && isForeground) {
			synchronized (mRecords) {
				mRecords.clear();
			}
		}
		return false;
	}

	public boolean interceptGroundBundle(NavThirdApp navThirdApp, Bundle bundle, boolean isForeground) {
		if (RecorderWin.isOpened() && WinManager.getInstance().isActivityDialog()) {
			synchronized (mRecords) {
				boolean found = false;
				for (TransponderRecord<Intent> record : mRecords) {
					if (record.navApp == navThirdApp) {
						found = true;
						record.addBundleToList(bundle, isForeground);
						break;
					}
				}
				if (!found) {
					TransponderRecord<Bundle> record = new TransponderRecord<Bundle>(navThirdApp);
					record.addBundleToList(bundle, isForeground);
					mRecords.add(record);
				}
			}
			return true;
		} else if (WinManager.getInstance().isActivityDialog() && !mRecords.isEmpty() && isForeground) {
			synchronized (mRecords) {
				mRecords.clear();
			}
		}
		return false;
	}
}