package com.txznet.comm.remote.util;

import android.os.HandlerThread;
import android.os.Looper;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.util.TXZHandler;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class StatusUtil {

	static {
		ServiceManager.getInstance().keepConnection(ServiceManager.TXZ,
				new Runnable() {
					@Override
					public void run() {
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.TXZ, "comm.subscribe.broadcast",
								null, null);
					}
				});
	}

	public static interface StatusListener {
		public void onBeginAsr();

		public void onEndAsr();

		public void onBeginTts();

		public void onEndTts();

		public void onBeginCall();

		public void onEndCall();

		public void onMusicPlay();

		public void onMusicPause();
		
		public void onBeepEnd();
	}

	private static boolean mAsrBusy = false;
	private static boolean mTtsBusy = false;
	private static boolean mCallBusy = false;

	//为下面ASR状态操作添加一个任务队列
	private static TXZHandler statusHandler;

	public static TXZHandler getStatusHandler(){
		if(statusHandler == null){
			synchronized (StatusUtil.class){
				if(statusHandler == null){
					HandlerThread handlerThread = new HandlerThread("StatusHandler");
					handlerThread.start();
					statusHandler = new TXZHandler(handlerThread.getLooper());
				}
			}
		}
		return statusHandler;
	}

	//向statusHandler队列中加入任务。
	public static void postDelayed(Runnable runnable, int delay){
		getStatusHandler().postDelayed(runnable,delay);
	}

	public static boolean isAsrBusy() {
		return mAsrBusy;
	}

	public static boolean isTtsBusy() {
		return mTtsBusy;
	}

	public static boolean isCallBusy() {
		return mCallBusy;
	}
	
	private static boolean updateAsrStatus(boolean b) {
		if (mAsrBusy == false && b) {
			mAsrBusy = b;
			postDelayed(new Runnable() {
				@Override
				public void run() {
					for (StatusListener l : mStatusListeners)
						l.onBeginAsr();
				}
			}, 0);
			return b;
		}
		if (mAsrBusy == true && b == false) {
			mAsrBusy = b;
			postDelayed(new Runnable() {
				@Override
				public void run() {
					for (StatusListener l : mStatusListeners)
						l.onEndAsr();
				}
			}, 0);
			return b;
		}
		return b;
	}

	private static boolean updateTtsStatus(boolean b) {
		if (mTtsBusy == false && b) {
			mTtsBusy = b;
			postDelayed(new Runnable() {
				@Override
				public void run() {
					for (StatusListener l : mStatusListeners)
						l.onBeginTts();
				}
			}, 0);
			return b;
		}
		if (mTtsBusy == true && b == false) {
			mTtsBusy = b;

			postDelayed(new Runnable() {
				@Override
				public void run() {
					for (StatusListener l : mStatusListeners)
						l.onEndTts();
				}
			}, 0);
			return b;
		}
		return b;
	}
	
	private static boolean updateCallStatus(boolean b) {
		if (mCallBusy == false && b) {
			mCallBusy = b;
			postDelayed(new Runnable() {
				@Override
				public void run() {
					for (StatusListener l : mStatusListeners)
						l.onBeginCall();
				}
			}, 0);
			return b;
		}
		if (mCallBusy == true && b == false) {
			mCallBusy = b;

			postDelayed(new Runnable() {
				@Override
				public void run() {
					for (StatusListener l : mStatusListeners)
						l.onEndCall();
				}
			}, 0);
			return b;
		}
		return b;
	}
	
	
	static Set<StatusListener> mStatusListeners = new HashSet<StatusListener>();

	public static void addStatusListener(StatusListener listener) {
		postDelayed(
				new Runnable1<StatusListener>(listener) {
					@Override
					public void run() {
						mStatusListeners.add(mP1);
					}
				}, 0);
	}

	public static void removeStatusListener(StatusListener listener) {

		postDelayed(
				new Runnable1<StatusListener>(listener) {
					@Override
					public void run() {
						mStatusListeners.remove(mP1);
					}
				}, 0);
	}

	public static byte[] notifyStatus(String status) {
		if (status.equals("onBeginMusic")) {

			postDelayed(new Runnable() {
				@Override
				public void run() {
					for (StatusListener l : mStatusListeners)
						l.onMusicPlay();
				}
			}, 0);
			return null;
		}
		if (status.equals("onEndMusic")) {
			postDelayed(new Runnable() {
				@Override
				public void run() {
					for (StatusListener l : mStatusListeners)
						l.onMusicPause();
				}
			}, 0);
			return null;
		}
		if (status.equals("onBeginAsr")) {
			updateAsrStatus(true);
			return null;
		}
		if (status.equals("onBeepEnd")) {
			if (mAsrBusy) {
				postDelayed(new Runnable() {
					@Override
					public void run() {
						for (StatusListener l : mStatusListeners)
							l.onBeepEnd();
					}
				}, 0);
			}
			return null;
		}
		if (status.equals("onEndAsr")) {
			updateAsrStatus(false);
			return null;
		}
		if (status.equals("onBeginTts")) {
			updateTtsStatus(true);
			return null;
		}
		if (status.equals("onEndTts")) {
			updateTtsStatus(false);
			return null;
		}
		if (status.equals("onBeginCall")) {
			updateCallStatus(true);
			return null;
		}
		if (status.equals("onEndCall")) {
			updateCallStatus(false);
			return null;
		}

		return null;
	}

	static {
		getStatus(new GetStatusCallback() {
			@Override
			public void onGet() {
				mAsrBusy = this.isBusyAsr;
				mTtsBusy = this.isBusyTts;
				mCallBusy = this.isBusyCall;
			}
		});
	}

	/*
	 * 批量状态获取工具，成员填为null不获取
	 */
	public static abstract class GetStatusCallback {
		public Boolean isBusyAsr = false;
		public Boolean isBusyTts = false;
		public Boolean isBusyCall = false;

		public abstract void onGet();
	}

	public static void getStatus(final GetStatusCallback cb) {
		if (cb == null)
			return;
		GetDataCallback res = new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(ServiceData data) {
				if (data != null) {
					JSONObject json = data.getJSONObject();
					try {
						if (cb.isBusyAsr != null) {
							cb.isBusyAsr = updateAsrStatus(json.getBoolean("asr"));
						}
					} catch (Exception e) {
					}
					try {
						if (cb.isBusyTts != null) {
							cb.isBusyTts = updateTtsStatus(json.getBoolean("tts"));
						}
					} catch (Exception e) {
					}
					try {
						if (cb.isBusyCall != null) {
							cb.isBusyCall = updateCallStatus(json.getBoolean("call"));
						}
					} catch (Exception e) {
					}
				}
				cb.onGet();
			}
		};
		JSONObject json = new JSONObject();
		try {
			if (cb.isBusyAsr != null)
				json.put("asr", true);
			if (cb.isBusyTts != null)
				json.put("tts", true);
			if (cb.isBusyCall != null)
				json.put("call", true);
		} catch (JSONException e) {
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"comm.status.get", json.toString().getBytes(), res);
	}
}
