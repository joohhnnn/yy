package com.txznet.sdk;

import android.os.SystemClock;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.push_manager.PushManager;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.bean.FlowInfo;
import com.txznet.sdk.bean.TrafficControlData;
import com.txznet.sdk.bean.WeatherData;

/**
 * 网络数据提供者
 */
public class TXZNetDataProvider {
	private static TXZNetDataProvider sInstance;
	private final static int DEFAULT_TASK_TIMEOUT = 60 * 1000;

	private TXZNetDataProvider() {
	}

	public static TXZNetDataProvider getInstance() {
		if (sInstance == null) {
			synchronized (TXZNetDataProvider.class) {
				if (sInstance == null) {
					sInstance = new TXZNetDataProvider();
				}
			}
		}
		return sInstance;
	}

	public void onReconnectTXZ() {
		if (mOnPushListener != null) {
			setOnPushListener(mOnPushListener);
		}
	}

	public static interface NetDataCallback<T> {
		/**
		 * 成功时回调
		 */
		void onResult(T data);

		/**
		 * 失败时回调
		 */
		void onError(int errorCode);
	}

	static class RemoteTask {
		int remoteId = -1;
		NetDataCallback callback;
		long timeout = 0;
	}

	/**
	 * 获取当前城市的天气信息
	 * 若不支持该城市查询，则返回码 1004
	 * 若输入错误，则返回码 1001
	 */
	public void getWeatherInfo(final NetDataCallback<WeatherData> callback) {
		getWeatherInfo("cur", callback);
	}

	private Map<Integer, RemoteTask> remoteTaskMapper = new HashMap<Integer, RemoteTask>();

	/**
	 * 获取指定城市的天气信息
	 * 若不支持该城市查询，则返回码 1004
	 * 若输入错误，则返回码 1001
	 */
	public void getWeatherInfo(String city, final NetDataCallback<WeatherData> callback) {
		JSONBuilder doc = new JSONBuilder();
		doc.put("city", city);
		int localTaskId = ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.netdata.req.weather",
				doc.toBytes(), new ServiceManager.GetDataCallback() {
					@Override
					public void onGetInvokeResponse(ServiceData data) {
						if (data != null) {
							synchronized (remoteTaskMapper) {
								RemoteTask remoteTask = remoteTaskMapper.get(getTaskId());
								if (remoteTask != null) {
									remoteTask.remoteId = data.getInt();
								}
							}
						}
						if (callback != null && isTimeout()) {
							callback.onError(1);
							synchronized (remoteTaskMapper) {
								remoteTaskMapper.remove(getTaskId());
							}
						}
					}
				});

		if (callback != null) {
			addTask(localTaskId, callback);
		}
	}
	
	/**
	 * 获取当前所插卡的流量套餐信息
	 * 参数错误 1001
	 */
	public void getFlowInfo(final NetDataCallback<FlowInfo> callback) {
		int localTaskId = ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.netdata.req.flowInfo", null, new ServiceManager.GetDataCallback() {
			
			@Override
			public void onGetInvokeResponse(ServiceData data) {
				if (data != null) {
					synchronized (remoteTaskMapper) {
						RemoteTask remoteTask = remoteTaskMapper.get(getTaskId());
						if (remoteTask != null) {
							remoteTask.remoteId = data.getInt();
						}
					}
				}
				if (callback != null && isTimeout()) {
					callback.onError(1);
					synchronized (remoteTaskMapper) {
						remoteTaskMapper.remove(getTaskId());
					}
				}
			}
		});
		if (callback != null) {
			addTask(localTaskId, callback);
		}
	}

	public void addTask(int localTaskId, NetDataCallback callback){
	    addTask(localTaskId, callback, DEFAULT_TASK_TIMEOUT);
    }

	public void addTask(int localTaskId, NetDataCallback callback, int timeout){
		synchronized (remoteTaskMapper) {
			LogUtil.d("kevin","add task :"+localTaskId);
			long now = SystemClock.elapsedRealtime();
            cleanTimeoutTask(now);
            RemoteTask remoteTask = new RemoteTask();
			remoteTask.callback = callback;
			remoteTask.timeout = now + timeout;
			remoteTaskMapper.put(localTaskId, remoteTask);
		}
	}

    private void cleanTimeoutTask(long now) {
        Iterator<Integer> iterator = remoteTaskMapper.keySet().iterator();
        while(iterator.hasNext()){
            Integer key = iterator.next();
            RemoteTask task = remoteTaskMapper.get(key);
            if(now > task.timeout){
                if(task.callback != null){
                    task.callback.onError(2);
                }
                LogUtil.logd("task("+key+") process timeout clean");
                iterator.remove();
            }
        }
    }


    /**
	 * 获取当前城市的限行信息
	 * 若不支持该城市查询，则返回码 1004
	 * 若输入错误，则返回码 1001
	 */
	public void getTrafficControlInfo(final NetDataCallback<TrafficControlData> callback) {
		getTrafficControlInfo("cur", callback);
	}

	/**
	 * 获取指定城市的限行信息
	 * 若不支持该城市查询，则返回码 1004
	 * 若输入错误，则返回码 1001
	 */
	public void getTrafficControlInfo(String city, final NetDataCallback<TrafficControlData> callback) {
		JSONBuilder doc = new JSONBuilder();
		doc.put("city", city);
		int localTaskId = ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.netdata.req.traffic",
				doc.toBytes(), new ServiceManager.GetDataCallback() {
					@Override
					public void onGetInvokeResponse(ServiceData data) {
						if (data != null) {
							synchronized (remoteTaskMapper) {
								RemoteTask remoteTask = remoteTaskMapper.get(getTaskId());
								if (remoteTask != null) {
									remoteTask.remoteId = data.getInt();
								}
							}
						}

						if (callback != null && isTimeout()) {
							callback.onError(1);
							synchronized (remoteTaskMapper) {
								remoteTaskMapper.remove(getTaskId());
							}
						}
					}
				});

		if (callback != null) {
			addTask(localTaskId, callback);
		}
	}

	/**
	 * error 1超时 -1json解析异常 -2不是 style不等于joke
	 *
	 * @param callback
	 */
	public void getJokeInfo(final NetDataCallback<String> callback){
		int localTaskId = ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.netdata.req.joke",
				null, new ServiceManager.GetDataCallback() {
					@Override
					public void onGetInvokeResponse(ServiceData data) {
						if (data != null) {
							synchronized (remoteTaskMapper) {
								RemoteTask remoteTask = remoteTaskMapper.get(getTaskId());
								if (remoteTask != null) {
									remoteTask.remoteId = data.getInt();
								}
							}
						}

						if (callback != null && isTimeout()) {
							callback.onError(1);
							synchronized (remoteTaskMapper) {
								remoteTaskMapper.remove(getTaskId());
							}
						}
					}
				});

		if (callback != null) {
			addTask(localTaskId, callback);
		}
	}

	public byte[] notifyCallback(String type, byte[] data) {
		synchronized (remoteTaskMapper) {
			for (Integer key : remoteTaskMapper.keySet()) {
				final RemoteTask remoteTask = remoteTaskMapper.get(key);
				if (remoteTask != null && remoteTask.callback != null) {
					JSONBuilder doc = new JSONBuilder(data);
					int rc = doc.getVal("rc", Integer.class);
					int remoteTaskId = doc.getVal("taskId", Integer.class);
					if (remoteTask.remoteId == remoteTaskId) {
						if (rc == 0) {
							if ("weather".equals(type)) {
								final String jData = doc.getVal("data", String.class);
								AppLogicBase.runOnUiGround(new Runnable(){
									public void run() {
										remoteTask.callback.onResult(JSON.parseObject(jData, WeatherData.class));
									};
								}, 0);
							} else if ("traffic".equals(type)) {
								final String jData = doc.getVal("data", String.class);
								AppLogicBase.runOnUiGround(new Runnable(){
									public void run() {
										remoteTask.callback.onResult(JSON.parseObject(jData, TrafficControlData.class));
									};
								}, 0);
							} else if("flowInfo".equals(type)) {
								final String jData = doc.getVal("data", String.class);
								AppLogicBase.runOnUiGround(new Runnable() {
									
									@Override
									public void run() {
										remoteTask.callback.onResult(JSON.parseObject(jData, FlowInfo.class));
									}
								}, 0);
								
							}else if("joke".equals(type)){
								final String jData = doc.getVal("data", String.class);
								AppLogicBase.runOnUiGround(new Runnable() {

									@Override
									public void run() {
										remoteTask.callback.onResult(jData);
									}
								}, 0);
							}
						} else {
							int errCode = doc.getVal("errorCode", Integer.class);
							remoteTask.callback.onError(errCode);
						}
						remoteTaskMapper.remove(key);
						break;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 设置推送的监听，针对包名的推送
	 */
	public interface OnPushListener{
		void onPush(byte[] cmd, byte[] data);
	}
	private OnPushListener mOnPushListener = null;

	/**
	 * 监听针对本应用的推送
	 * @param listener
	 */
	public void setOnPushListener(OnPushListener listener){
		mOnPushListener = listener;
		if (mOnPushListener == null) {
			TXZService.setCommandProcessor("txz.thirdapp.",null);
		} else {
			TXZService.setCommandProcessor("txz.thirdapp.", new TXZService.CommandProcessor() {
				@Override
				public byte[] process(final String packageName, final String command,
						final byte[] data) {
					if (TextUtils.equals(command, "push")) {
						if (mOnPushListener != null) {
							try {
								PushManager.PushCmd_ThirdAppMessage pushCmd_thirdAppMessage = PushManager.PushCmd_ThirdAppMessage
										.parseFrom(data);
								mOnPushListener.onPush(pushCmd_thirdAppMessage.strSubCmd, pushCmd_thirdAppMessage.strData);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					return null;
				}
			});
		}
	}
}
