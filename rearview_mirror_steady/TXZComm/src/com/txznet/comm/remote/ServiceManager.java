package com.txznet.comm.remote;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.service.IService;
import com.txznet.txz.util.TXZHandler;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServiceManager {
    private static final String LOG_TAG = "ServiceManager";
    private static int DEFAULT_RECONNECT_DELAY = 1000; // 默认重连时延

	private static final int DEFAULT_TIMEOUT = 30000; // 默认超时时长
	public static final int DEFAULT_TIMEOUT_LONG = 60000; // 默认长超时时长
	public static final int DEFAULT_TIMEOUT_SHORT = 3000; // 默认短超时时长

    private static final String REMOTE_TRANSACT_DESCRIPTOR = "com.txznet.txz.service.IService";
    private static final int REMOTE_TRANSACT_CODE_SENDINVOKE = android.os.IBinder.FIRST_CALL_TRANSACTION;

    // 以下定义服务名，与包名和IService所处的包名一致
    public final static String TXZ = "com.txznet.txz";
	public final static String BT = "com.txznet.bluetooth";
	public final static String NAV = "com.txznet.nav";
	public final static String TXZ_AMAP = "com.txznet.amap";
	public final static String MUSIC = "com.txznet.music";
	public final static String RECORD = "com.txznet.record";
	public final static String WEBCHAT = "com.txznet.webchat";
	public final static String LAUNCHER = "com.txznet.launcher";
	public final static String FM = "com.txznet.fm";
	public final static String TEAM = "com.txznet.team";
	public final static String WAKEUP = "com.txznet.wakeup";
	public final static String SETTING = "com.txznet.txzsetting";

	/**************************传输字段（其它应用通过TXZCore请求数据）***********************/
	public static final String REQ_DATA_PREFIX = "txz.transfer.";
	public static final String COMMAND_PROCESSOR_PREFIX = "transfer.data.";
	public static final String FIELD_QUERYACTIVED = "queryActived";
	public static final String FIELD_REQGREET = "reqGreet";
	public static final String FIELD_REQIDENTICODE = "reqIdentiCode";
	public static final String FIELD_REQQRCODE = "reqQRCode";
	public static final String FIELD_VERIFYIDCODE = "verifyIdCode";
	public static final String FIELD_SUBMITACTIVEINFO = "submitActiveInfo";
	public static final String FIELD_REQNOTIFYDATA = "reqNotifyData";
	public static final String FIELD_REQWEATHERPIC = "reqWeatherPic";
	public static final String FIELD_SUBMITCARINFOCHANGE = "submitCarInfoChange";
	public static final String FIELD_REQTRANSFER = "reqTransfer";
	public static final String FIELD_RESPTRANSFER = "respTransfer";

	public static final String COMMAND_REQ_TRANSFER = REQ_DATA_PREFIX + FIELD_REQTRANSFER;
	public static final String COMMAND_RESP_TRANSFER = COMMAND_PROCESSOR_PREFIX + FIELD_RESPTRANSFER;

	static {
//		String packageName = GlobalContext.get().getPackageName();
//		try {
//			if (packageName.equals(TXZ) || packageName.equals(BT) || packageName.equals(NAV) || packageName.equals(TXZ_AMAP) || 
//					packageName.equals(MUSIC) || packageName.equals(RECORD) || packageName.equals(WEBCHAT) || packageName.equals(LAUNCHER) || 
//					packageName.equals(FM) || packageName.equals(TEAM) || packageName.equals(WAKEUP)) {
//				DEFAULT_TIMEOUT = 3000;
//			}
//		} catch (Exception e) {
//		}
		try {
			File f = new File("/etc/txz/txz_service.json");
			FileInputStream in = new FileInputStream(f);
			byte[] bs = new byte[(int) f.length()];
			int t = 0;
			while (t < bs.length) {
				int r = in.read(bs, t, bs.length - t);
				if (r < 0)
					break;
				t += r;
			}
			in.close();
			JSONBuilder json = new JSONBuilder(bs);
			DEFAULT_RECONNECT_DELAY = json.getVal("DEFAULT_RECONNECT_DELAY", Integer.class, 1000);
		} catch (Exception e) {
		}
		Log.d("txz_service", "DEFAULT_RECONNECT_DELAY=" + DEFAULT_RECONNECT_DELAY);
	}

	

	HandlerThread mServiceThread;
	TXZHandler mServiceThreadHandler;
	ServiceRequest mLastReq = null;
	String mLastService = null;

	public void removeOnServiceThread(Runnable runnable) {
		mServiceThreadHandler.removeCallbacks(runnable);
	}

	public void runOnServiceThread(Runnable runnable, int delay) {
		mServiceThreadHandler.postDelayed(runnable, delay);
	}

	public void removeServiceThreadCallback(Runnable runnable) {
		mServiceThreadHandler.removeCallbacks(runnable);
	}

	public static class ServiceData {
		byte[] mData;

			ServiceData(byte[] data) {
				mData = data;
		}

		public String getString() {
			try {
				return new String(mData);
			} catch (Exception e) {
				return null;
			}
		}

		public byte[] getBytes() {
			return mData;
		}

		public Integer getInt() {
			try {
				return Integer.parseInt(new String(mData));
			} catch (Exception e) {
				return null;
			}
		}

		public Long getLong() {
			try {
				return Long.parseLong(new String(mData));
			} catch (Exception e) {
				return null;
			}
		}

		public Double getDouble() {
			try {
				return Double.parseDouble(new String(mData));
			} catch (Exception e) {
				return null;
			}
		}

		public Boolean getBoolean() {
			try {
				return Boolean.parseBoolean(new String(mData));
			} catch (Exception e) {
				return null;
			}
		}

		public JSONObject getJSONObject() {
			try {
				return new JSONObject(new String(mData));
			} catch (Exception e) {
				return null;
			}
		}

		public JSONArray getJSONArray() {
			try {
				return new JSONArray(new String(mData));
			} catch (Exception e) {
				return null;
			}
		}
	}

	public static abstract class GetDataCallback {
        int mTaskId;
        boolean mIsTimeout;

        public abstract void onGetInvokeResponse(ServiceData data);

        public GetDataCallback() {

        }

		public boolean isTimeout() {
			return mIsTimeout;
		}

		public int getTaskId() {
			return this.mTaskId;
		}
    }

	class ServiceRequest {
		int mId; // ID
		String mCommand;
		byte[] mData; // 请求数据
        GetDataCallback mCallback; // 不为null表明是请求同步获取数据
        long timeout; // 超时时间点(= 请求开始时间 + 超时阈值)
        long timeoutThreshold; // 超时阈值
    }

	public boolean mDisableSendInvoke = false;
	private int mNextReqId = 1;
	private long mLastBindServiceTime = 0;

	class ServiceRecord {
		public ServiceRecord(String serviceName) {
			this.mServiceName = serviceName;
			mTXZServiceConnection = new TXZServiceConnection(serviceName);
		}

		TXZServiceConnection mTXZServiceConnection;
		String mServiceName;
		IService mConnectionInterface; // 连接接口对象
		boolean bTargetServiceExists = true; // 指定Service是否存在
		List<ServiceRequest> mRequestQueue = new ArrayList<ServiceRequest>();

		int mRebindDelay = DEFAULT_RECONNECT_DELAY;
		Runnable mRunnableRebind = new Runnable() {
			@Override
			public void run() {
				if (mRebindDelay < 10000) {
					mRebindDelay += 1000;
				}
				bindService();
			}
		};

		public void doDisconnect() {
			mServiceThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mConnectionInterface != null) {
						for (ConnectionListener listener : mConnectionListeners) {
							listener.onDisconnected(mServiceName);
						}
						mConnectionInterface = null;
						mRebindDelay = DEFAULT_RECONNECT_DELAY;
						if (mRebindDelay > 0) {
							ServiceManager.getInstance().removeOnServiceThread(mRunnableRebind);
							ServiceManager.getInstance().runOnServiceThread(mRunnableRebind, mRebindDelay);
						}
						procQueue();
					}
				}
			});
		}

		private void bindService() {
			if (mDisableSendInvoke) {
//				Log.w("sss", "pbh disable send invoke: bind service");
				return;
			}

			if (!bTargetServiceExists) {
				return;
			}

			if (mConnectionInterface != null) {
				return;
			}
			Intent intent = new Intent(mServiceName + ".service.TXZService");
			intent.setPackage(mServiceName);

			try {
				Intent intentStart = new Intent(mServiceName + ".startTXZService");
				intentStart.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				intentStart.setPackage(mServiceName);
				// 如果上次发的广播没有超过超时时间，则不发
				if (mLastBindServiceTime == 0 || mLastBindServiceTime - System.currentTimeMillis() >= DEFAULT_RECONNECT_DELAY) {
					GlobalContext.get().sendBroadcast(intentStart);
					mLastBindServiceTime = SystemClock.elapsedRealtime();
				}
				GlobalContext.get().startService(intent);
				GlobalContext.get().bindService(intent, mTXZServiceConnection,
						Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
			} catch (Exception e) {
				// e.printStackTrace();
				// LogUtil.loge("ServiceManager::bindService encountered error:
				// " + e.toString());
			}

			// mConnectionInterface为null， 检查指定Service是否存在
			if (null == mConnectionInterface) {
//				Log.d("logServiceManager", "bindService failed: componentName is null");

				if (!checkTargetPackageExists(mServiceName)) {
                    Log.d(LOG_TAG, "target package not exist, set flag");
                    bTargetServiceExists = false;
                    return;
				}
			}

			ServiceManager.getInstance().removeOnServiceThread(mRunnableRebind);
			ServiceManager.getInstance().runOnServiceThread(mRunnableRebind, mRebindDelay);
		}

		private boolean checkTargetPackageExists(String pkg) {
			try {
				PackageManager pm = GlobalContext.get().getPackageManager();
				ApplicationInfo info = pm.getApplicationInfo(pkg, 0);

				if (null == info || TextUtils.isEmpty(info.packageName)) {
                    Log.d(LOG_TAG, "ApplicationInfo is null: " + pkg);
                    return false;
                }
			} catch (NameNotFoundException e) {
                Log.d(LOG_TAG, "target package not found: " + pkg);
                return false;
            } catch (Exception e) {
                Log.d(LOG_TAG, "checkTargetService encountered error: " + e.toString());
            }

			return true;
		}
		
		
		private class TXZInvokeTimeoutException extends RuntimeException {

			private static final long serialVersionUID = 2367467062456909244L;
			private String mFrom;
			private String mTo;
			private String mCommand;
			private long mTime;
			public TXZInvokeTimeoutException(String from , String to , String command , long time) {
				super("TXZInvokeTimeoutException");
				mFrom = from;
				mTo = to;
				mCommand = command;
				mTime = time;
			}
			
			@Override
			public void printStackTrace(PrintWriter err) {
				err.println("TXZInvokeTimeoutException");
				err.println("From=" + mFrom);
				err.println("To=" + mTo);
				err.println("Command=" + mCommand);
				err.println("CostTime=" + mTime);
			}
			
		}

		/**
		 * 处理服务请求队列的服务请求
		 */
		public void procQueue() {
			cleanTimeout(mServiceName, mRequestQueue, bTargetServiceExists);// 清除超时服务
			if (mConnectionInterface == null) {// 连接接口对象为空，则服务没有连接
				// 没有连接时先发送广播通知启动服务
				// Intent intent = new Intent(mServiceName +
				// ".startTXZService");
				// GlobalContext.get().sendBroadcast(intent);
				bindService();
				return;
			}
			for (int i = 0; i < mRequestQueue.size();) {// 执行服务请求队列中第一个请求，因为超时的服务已经清除
				ServiceRequest request = mRequestQueue.get(i);
				if (mConnectionInterface != null) {
					long beginTime = SystemClock.elapsedRealtime();
					mLastReq = request;
					mLastService = mServiceName;
					byte[] remoteResp = (byte[]) remoteSendInvoke(
							// 通过服务对象发送服务调用 返回码
                            // 调用的callback为null说明不关注调用结果, 对于此类调用采用oneway flag
                            mConnectionInterface, mServiceName, request.mCommand, request.mData,
                            isRequestAsync(request));
                    mLastReq = null;
                    mLastService = null;
					long costTime = SystemClock.elapsedRealtime() - beginTime;
					if (costTime >= 50 && !request.mCommand.equals("comm.log")) {
						String from = GlobalContext.get().getApplicationInfo().packageName;
						LogUtil.logw("command[" + request.mCommand + "] from ["
								+ from + "] to [" + mServiceName
								+ "] cost too much time: " + costTime);

                        if (isRequestTimeout(request, costTime)) {
                            Log.e(LOG_TAG, "request timed out, command = " + request.mCommand
                                    + ", async = " + isRequestAsync(request));
                            // 超时则生成crash信息
                            CrashCommonHandler.dumpExceptionToSDCard(GlobalContext.get(),
                                    Environment.getExternalStorageDirectory().getPath()
                                            + "/txz/report/", null, new TXZInvokeTimeoutException(
                                            from, mServiceName, request.mCommand, costTime));
                        }
					}
                    if (request.mCallback != null) {
                        try {
                            request.mCallback.onGetInvokeResponse(new ServiceData(remoteResp));
                        } catch (Exception e) {
                            e.printStackTrace();
						}
					}
				}
				mRequestQueue.remove(i);// 清除已经执行的服务请求
			}
		}

        /**
         * 判断request是否是异步请求
         *
         * @param request
         * @return
         */
        private boolean isRequestAsync(ServiceRequest request) {
            return null == request.mCallback;
        }

        /**
         * 判断是否应该为request生成InvokeTimeout日志
         * 针对同步请求执行超过2s则认为超时, 异步请求1s
         *
         * @param request
         * @param costTime
         * @return
         */
        private boolean isRequestTimeout(ServiceRequest request, long costTime) {
            if (isRequestAsync(request)) {
                return costTime >= 1000;
            } else {
                return costTime >= 2000;
            }
        }
    }

	/**
	 * 清除服务队列中的超时服务请求
	 * 
	 * @param serverName
	 * @param requestQueue
	 * @param needLog
	 *            是否打印超时日志
	 */
	private void cleanTimeout(String serverName, List<ServiceRequest> requestQueue, boolean needLog) {
		long now = SystemClock.elapsedRealtime();// 从开机到现在的毫秒书（手机睡眠(sleep)的时间也包括在内）
		for (int i = 0; i < requestQueue.size(); i++) {
			ServiceRequest request = requestQueue.get(i);
			if (now >= request.timeout) {
                if (request.mCallback != null) {
                    request.mCallback.mIsTimeout = true;
                    request.mCallback.onGetInvokeResponse(null);
                }

				if (!request.mCommand.equals("comm.log") && needLog) {
                    LogUtil.loge("[FROM=" + GlobalContext.get().getApplicationInfo().packageName
                            + ",TO=" + serverName + ",CMD=" + request.mCommand + "] timeout="
                            + request.timeoutThreshold);
                }

				requestQueue.remove(i--);
			}
		}
	}

	public void releaseAllConnection() {
		synchronized (mServiceMap) {
			Iterator<String> iterator = mServiceMap.keySet().iterator();
			while (iterator.hasNext()) {
				ServiceRecord record = mServiceMap.get(iterator.next());
				if (record.mTXZServiceConnection != null) {
					try {
						record.doDisconnect();
						GlobalContext.get().unbindService(record.mTXZServiceConnection);
					} catch (Exception e) {
					}
				}
			}
		}
	}
	
	public void releaseAllConnectionExcludeTXZ() {
		synchronized (mServiceMap) {
			Iterator<String> iterator = mServiceMap.keySet().iterator();
			while (iterator.hasNext()) {
				ServiceRecord record = mServiceMap.get(iterator.next());
				if (TXZ.equals(record.mServiceName)) {
					continue;
				}
				if (record.mTXZServiceConnection != null) {
					try {
						record.doDisconnect();
						GlobalContext.get().unbindService(record.mTXZServiceConnection);
					} catch (Exception e) {
					}
				}
			}
		}
	}

	public void releaseConnection(String serviceName) {
		synchronized (mServiceMap) {
			Iterator<String> iterator = mServiceMap.keySet().iterator();
			while (iterator.hasNext()) {
				ServiceRecord record = mServiceMap.get(iterator.next());
				if (record.mTXZServiceConnection != null && record.mServiceName != null
						&& record.mServiceName.equals(serviceName)) {
					try {
						record.doDisconnect();
						GlobalContext.get().unbindService(record.mTXZServiceConnection);
					} catch (Exception e) {
					}
					break;
				}
			}
		}
	}

	Map<String, ServiceRecord> mServiceMap = new HashMap<String, ServiceRecord>();

	private ServiceManager() {
		mServiceThread = new HandlerThread("ServiceManagerThread");
		mServiceThread.start();
		mServiceThreadHandler = new TXZHandler(mServiceThread.getLooper()) {
			@Override
			public String getInfo() {
				ServiceRequest req = mLastReq;
				if (req == null)
					return null;
				return "LastReuqest{to=" + mLastService + ", cmd=" + req.mCommand + "}";
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addDataScheme("package");
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String packageName = intent.getDataString().substring(8);
				Log.d("logServiceManager", "package installed: " + packageName);
				resetTargetServiceRecord(packageName);
			}
		}, filter);
	}

	/**
	 * 重置对应ServiceRecord的Service存在标志
	 * 
	 * @param pkgName
	 */
	private void resetTargetServiceRecord(String pkgName) {
		if (null == mServiceMap) {
			return;
		}

		synchronized (mServiceMap) {
			ServiceRecord record = mServiceMap.get(pkgName);

			if (null != record) {
				Log.d("logServiceManager", "reset ServiceRecord: " + pkgName);
				record.bTargetServiceExists = true;
			}
		}
	}

	/**
	 * 服务对象发送服务调用 返回 码
	 * 
	 * @param connectionInterface
	 * @param serviceName
	 * @param command
	 * @param data
     * @param async 是否采用异步方式发起调用
     * @return
     */
    Object remoteSendInvoke(IService connectionInterface, String serviceName, String command,
                            byte[] data, boolean async) {
        try {

			/*
			 * if (!command.equals("comm.log")) { // send this function log
			 * String content = "[FROM=" +
			 * GlobalContext.get().getApplicationInfo().packageName + ",TO=" +
			 * serviceName + ",CMD=" + command + "] data:" + data + ". ";
			 * 
			 * if (GlobalContext.isTXZ()) { LogUtil.logd(content); } else {
			 * String logdata = new JSONBuilder() .put("level",
			 * android.util.Log.DEBUG) .put("tag",
			 * "ServiceManager.remoteSendInvoke") .put("content",
			 * content).toString(); connectionInterface.sendInvoke(TXZ,
			 * "comm.log", logdata.getBytes()); sendTXZInvokeSync("comm.log",
			 * logdata.getBytes()); } // sendTXZInvokeSync }
			 */
            if (async) {
                LogUtil.justConsole_logi(LOG_TAG, "using oneway flag for command: " + command + ", target = "
                        + serviceName);
                return remoteSendInvokeAsync(connectionInterface, command, data);
            } else {
            	LogUtil.justConsole_logi(LOG_TAG, "using normal flag for command: " + command + ", target = "
                        + serviceName);
                return connectionInterface.sendInvoke(
                        GlobalContext.get().getApplicationInfo().packageName, command, data);
            }

		} catch (NullPointerException npe) {
			// Log.d("ServiceManager", "conn=" + connectionInterface + ",
			// context=" + GlobalContext.get() + ", comm=" + command + ", data="
			// + data);
            Log.e(LOG_TAG, "[FROM=" + GlobalContext.get().getApplicationInfo().packageName + ",TO=" + serviceName
                    + ",CMD=" + command + "] NullPointerException");
        } catch (DeadObjectException de) {
            Log.e(LOG_TAG, "[FROM=" + GlobalContext.get().getApplicationInfo().packageName + ",TO=" + serviceName
                    + ",CMD=" + command + "] DeadObjectException");
        } catch (Throwable e) {
            Log.e(LOG_TAG, "[FROM=" + GlobalContext.get().getApplicationInfo().packageName + ",TO=" + serviceName
                    + ",CMD=" + command + "] Throwable");
            e.printStackTrace();
		}
		return null;
	}

    /**
     * 采用异步方式发起远程调用
     * <p>
     * 对于不关注调用结果的远程调用采用IBinder.FLAG_ONEWAY标记发起调用, 避免阻塞调用端线程, 规避被调端执行
     * 时间过长或transact耗时导致的调用端Timeout
     *
     * @param connectionInterface
     * @param command
     * @param data
     * @return
     * @throws RemoteException
     */
    private Object remoteSendInvokeAsync(IService connectionInterface, String command, byte[] data)
            throws RemoteException {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        byte[] _result;
        try {
            _data.writeInterfaceToken(REMOTE_TRANSACT_DESCRIPTOR);
            _data.writeString(GlobalContext.get().getApplicationInfo().packageName);
            _data.writeString(command);
            _data.writeByteArray(data);
            connectionInterface.asBinder().transact(REMOTE_TRANSACT_CODE_SENDINVOKE, _data, _reply,
                    IBinder.FLAG_ONEWAY);
            _reply.readException();
            _result = _reply.createByteArray();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

	public static interface ConnectionListener {
		public void onConnected(String serviceName);

		public void onDisconnected(String serviceName);
	}

	private Set<ConnectionListener> mConnectionListeners = new HashSet<ConnectionListener>();// 服务监听队列

	/**
	 * 添加连接监听到队列
	 * 
	 * @param listener
	 */
	public void addConnectionListener(final ConnectionListener listener) {
		runOnServiceThread(new Runnable() {
			@Override
			public void run() {
				mConnectionListeners.add(listener);
			}
		}, 0);
	}

	/**
	 * 从队列清除监听
	 * 
	 * @param listener
	 */
	public void removeConnectionListener(final ConnectionListener listener) {
		runOnServiceThread(new Runnable() {
			@Override
			public void run() {
				mConnectionListeners.remove(listener);
			}
		}, 0);
	}

	class TXZServiceConnection implements ServiceConnection {
		String mServiceName;

		public TXZServiceConnection(String serviceName) {
			mServiceName = serviceName;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtil.logd("onServiceConnected ComponentName=" + name);
			mServiceThreadHandler.post(new Runnable1<IBinder>(service) {
				@Override
				public void run() {
					for (ConnectionListener listener : mConnectionListeners) {
						listener.onConnected(mServiceName);
					}
					IBinder service = mP1;
					ServiceRecord rec;
					synchronized (mServiceMap) {
						rec = mServiceMap.get(TXZServiceConnection.this.mServiceName);
					}
					rec.mRebindDelay = DEFAULT_RECONNECT_DELAY;
					ServiceManager.getInstance().removeOnServiceThread(rec.mRunnableRebind);
					rec.mConnectionInterface = IService.Stub.asInterface(service);
					rec.mServiceName = TXZServiceConnection.this.mServiceName;
					rec.procQueue();
				}
			});
		}

		public void doDisconnect() {
			ServiceRecord rec = null;
			synchronized (mServiceMap) {
				if (mServiceMap.containsKey(TXZServiceConnection.this.mServiceName)) {
					rec = mServiceMap.get(TXZServiceConnection.this.mServiceName);
				}
			}
			if (rec != null) {
				rec.doDisconnect();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			LogUtil.logd("onServiceDisconnected ComponentName=" + name);
			doDisconnect();
		}
	}

	static ServiceManager sInstance = new ServiceManager();

	public static ServiceManager getInstance() {
		return sInstance;
	}
	
	public void broadInvoke(final String command, final byte[] data) {
		broadInvoke(command, data, DEFAULT_TIMEOUT);
	}

	// 广播命令
	public void broadInvoke(final String command, final byte[] data, final int timeout) {
		if (mDisableSendInvoke && !command.startsWith("txz.camera.") && !command.startsWith("tool.camera.")) {
//			Log.w("sss", "pbh disable send invoke: broadcast service");
			return;
		}
		mServiceThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				// LogUtil.logd("[FROM="
				// + GlobalContext.get().getApplicationInfo().packageName
				// + ",TO=all,CMD=" + command + "] broad data=" + data
				// + ".");
				synchronized (mServiceMap) {
					Iterator<String> iterator = mServiceMap.keySet().iterator();
					while (iterator.hasNext()) {
						ServiceRecord record = mServiceMap.get(iterator.next());
						if (record.mTXZServiceConnection != null) {// 同行者服务连接不为空，则发送服务调用
                            sendInvoke(record.mServiceName, command, data, null, timeout);
                        }
                    }
				}
			}
		});
	}

	// 同步调用
    public ServiceData sendInvokeSync(String serviceName, String command, byte[] data) {
        if (mDisableSendInvoke && !command.startsWith("txz.camera.") && !command.startsWith("tool.camera.")) {
//			Log.w("sss", "pbh disable send invoke: send invoke sync");
            return null;
        }
        IService service = getService(serviceName);
        if (service != null) {// 判断请求服务的名称对应的服务是否存在
            try {
                return new ServiceData(
                        service.sendInvoke(GlobalContext.get().getApplicationInfo().packageName, command, data));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int sendInvoke(final String servicename, String command, byte[] data,
                          GetDataCallback callback) {
        return sendInvoke(servicename, command, data, callback, DEFAULT_TIMEOUT);
    }

    // serviceName 远端
    public int sendInvoke(final String serviceName, String command, byte[] data,
                          GetDataCallback callback, long timeout) {
        if (mDisableSendInvoke && !command.startsWith("txz.camera.")) {
//			Log.w("sss", "pbh disable send invoke: send invoke");
			return -1;
		}
		// if (!command.equals("comm.log")) {
		// LogUtil.logd("[FROM="
		// + GlobalContext.get().getApplicationInfo().packageName
		// + ",TO=" + serviceName + ",CMD=" + command + "] data:"
		// + data + ". ");
		// }

		ServiceRequest req = new ServiceRequest();
		req.mId = mNextReqId++;
		req.mCommand = command;
        req.mData = data;
        req.mCallback = callback;
        req.timeoutThreshold = timeout;
        req.timeout = SystemClock.elapsedRealtime() + timeout;
        if (req.mCallback != null) {// 如果服务请求的回调不为空 ，表明是请求同步获取数据
            req.mCallback.mTaskId = req.mId;
        }
		mServiceThreadHandler.post(new Runnable2<String, ServiceRequest>(serviceName, req) {
			@Override
			public void run() {
				ServiceRecord rec;
				synchronized (mServiceMap) {// mServiceMap作为存储服务记录的队列，不能被同时操作
					if (mServiceMap.containsKey(mP1)) {
						rec = mServiceMap.get(mP1);
					} else {
						rec = new ServiceRecord(serviceName);
						mServiceMap.put(mP1, rec);
					}
				}
				rec.mRequestQueue.add(mP2);
				rec.procQueue();
				mServiceThreadHandler.postDelayed(new Runnable1<ServiceRecord>(rec) {
					@Override
					public void run() {
                        mP1.procQueue();

                    }
                }, mP2.timeoutThreshold);
            }
		});
		return req.mId;
	}

	public void addServiceRecord(String serviceName){
		synchronized (mServiceMap) {
			if(mServiceMap.containsKey(serviceName)){
				return;
			}
			mServiceMap.put(serviceName, new ServiceRecord(serviceName));
		}
	}
	
	public IService getService(String serviceName) {
		synchronized (mServiceMap) {
			Iterator<String> iterator = mServiceMap.keySet().iterator();
			while (iterator.hasNext()) {
				ServiceRecord record = mServiceMap.get(iterator.next());
				if (record.mTXZServiceConnection != null && record.mServiceName != null
						&& record.mServiceName.equals(serviceName)) {
					return record.mConnectionInterface;
				}
			}
		}
		return null;
	}

	public byte[] sendTXZInvokeSync(String command, byte[] data) {
		ServiceData sendInvokeSync = sendInvokeSync(TXZ, command, data);
		byte[] result=null;
		if (sendInvokeSync!=null) {
			result=sendInvokeSync.getBytes();
		}else{
			LogUtil.loge("请先初始化语音声控引擎");
		}
		return result;
	}

	/**
	 * 保持远程连接，断开收到对方启动或重连时自动调用
	 * 
	 * @param remoteServiceName
	 *            远程连接的服务名
	 * @param onConnected
	 *            连接上后需要执行的操作
	 */
	public void keepConnection(final String remoteServiceName, final Runnable onConnected) {

		final GetDataCallback callback = new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(ServiceData data) {
				if (data == null)
					return;
				removeOnServiceThread(onConnected);
				runOnServiceThread(onConnected, 200);
			}
		};

		addConnectionListener(new ConnectionListener() {
			@Override
			public void onDisconnected(String serviceName) {
				// 断开时尝试再调用一下
				if (serviceName.equals(remoteServiceName)) {
					ServiceManager.getInstance().sendInvoke(remoteServiceName, "", null, callback);
				}
			}

			@Override
			public void onConnected(String serviceName) {
				// 连上时执行一遍
				if (remoteServiceName.equals(serviceName)) {
					removeOnServiceThread(onConnected);
					runOnServiceThread(onConnected, 200);
				}
			}
		});

		// 收到启动广播时也执行一遍
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				ServiceManager.getInstance().sendInvoke(remoteServiceName, "", null, callback);
			}
		}, new IntentFilter(remoteServiceName + ".onCreateApp"));

		// 直接执行一遍
		ServiceManager.getInstance().sendInvoke(remoteServiceName, "", null, callback);
	}
}
