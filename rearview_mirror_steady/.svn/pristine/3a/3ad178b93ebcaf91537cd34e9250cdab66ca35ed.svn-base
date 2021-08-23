package com.txznet.comm.remote.udprpc;

import java.util.HashMap;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;

import android.text.TextUtils;

/**
 * UdpId生成类。该类由server进程运行并维护，<p>
 * client初始化时请求并分配该ID，该ID根据processName来决定，<p>
 * 并且相同processName不同时刻 的ID都相同
 * 
 * @author Terry
 */
public class UdpIdManager {

	private UdpIdManager() {
	}

	private HashMap<String, Integer> mMapProcessIds = new HashMap<String, Integer>();
	
	/**
	 * C端可用id从2开始，1:Core,0:id未获取,-1 获取失败
	 */
	private int mCurIdPoint = 2;
	
	private static UdpIdManager sInstance = new UdpIdManager();

	public static UdpIdManager getInstance() {
		return sInstance;
	}

	public synchronized int getUdpId(String processName) {
		if (TextUtils.isEmpty(processName)) {
			LogUtil.loge("processName is null");
			return -1;
		}
		if(processName.equals(ServiceManager.TXZ)){
			return 1;
		}
		if (mMapProcessIds.get(processName) != null) {
			return mMapProcessIds.get(processName);
		}
		LogUtil.logd("UdpId processName:" + processName + ",id:" + mCurIdPoint);
		mMapProcessIds.put(processName, mCurIdPoint++);
		return mMapProcessIds.get(processName);
	}
}
