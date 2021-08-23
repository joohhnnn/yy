package com.txznet.txz.module.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import com.txz.ui.data.UiData;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.util.TXZHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.HandlerThread;
import android.text.TextUtils;

/**
 * 网络管理模块，负责网络状态监听，网络事件处理
 * 
 * @author bihongpi
 *
 */
public class NetworkManager extends IModule {
	public static final String TXZ_DOMAIN_1 = "conn1.txzing.com";
	public static final String TXZ_DOMAIN_2 = "conn2.txzing.com";
	
	private TXZHandler mCheckNetHandler;
	private HandlerThread mCheckNetHandlerThread;

	int mNetworkType = UiData.NETWORK_STATUS_NONE;
	private String mApnType = "";
	// 加一个timer避免
	private Runnable checkRun = new Runnable() {
		@Override
		public void run() {
			onNetChange();
			AppLogic.runOnUiGround(this, 2 * 60 * 1000);
		}
	};

	static NetworkManager sModuleInstance = null;

	private NetworkManager() {
		AppLogic.runOnUiGround(checkRun, 1 * 60 * 1000);
		registerDateTransReceiver();
		updateNetInfo();
	}

	public static NetworkManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (NetworkManager.class) {
				
				if (sModuleInstance == null) {
					sModuleInstance = new NetworkManager();
				}
			}
		}
		return sModuleInstance;
	}

	@Override
	public int initialize_AfterStartJni() {
		return super.initialize_AfterStartJni();
	}

	public synchronized void updateNetInfo() {
		try {

			int oldType = mNetworkType;
			int netType = NetworkUtil.getSystemNetwork(GlobalContext.get());
			mNetworkType = netType;
			JNIHelper.logd("oldType=" + Integer.toString(oldType)
					+ "; netType=" + Integer.toString(netType));
			if (netType != oldType) {
				notifyNetChange();
			}
			ConnectivityManager connectivityManager = (ConnectivityManager)GlobalContext.get().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager
					.getActiveNetworkInfo();
			String exrea = null;
			if (activeNetworkInfo != null) {
				exrea = activeNetworkInfo.getExtraInfo();
			}
			JNIHelper.logd("net event:" + mNetworkType + " " + exrea);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void onNetChange() {
		updateNetInfo();
	}

	public int getNetType() {
		return mNetworkType;
	}
	
	/**
	 * 流量不足
	 * @return
	 */
	public boolean checkLeastFlow() {
		if(ConfigManager.getInstance().getServerConfig().bDataPartner == null || ConfigManager.getInstance().getServerConfig().bDataPartner == false){
			return false;
		}
		byte[] leastFlowBytes = NativeData.getNativeData(UiData.DATA_ID_LEAST_FLOW);
		String leastFlow = new String(leastFlowBytes);
		if(TextUtils.equals(leastFlow, "0") && getNetType() != UiData.NETWORK_STATUS_WIFI){
			return true;
		}
		return false;
	}
	
	public synchronized String getApnType() {
		return mApnType;
	}

	public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

	private void registerDateTransReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(CONNECTIVITY_CHANGE_ACTION);
		// filter.setPriority(1000);
		GlobalContext.get().registerReceiver(new NetStatReceiver(), filter);
	}

	public class NetStatReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			JNIHelper.logd("onReceive:" + action);
			if (TextUtils.equals(action, CONNECTIVITY_CHANGE_ACTION)) {// 网络变化的时候会发送通知
				updateNetInfo();
				return;
			}
		}
	}

	void notifyNetChange() {
		JNIHelper.sendEvent(UiEvent.EVENT_NETWORK_CHANGE, 0);
		// TODO 网络变化通知界面
	}
	
	public boolean hasNet() {
		boolean bRet = false;
		switch (NetworkManager.getInstance().getNetType()) {
		case UiData.NETWORK_STATUS_UNKNOW:
		case UiData.NETWORK_STATUS_2G:
		case UiData.NETWORK_STATUS_3G:
		case UiData.NETWORK_STATUS_4G:
		case UiData.NETWORK_STATUS_WIFI:
			bRet = true;
			break;
		default:
		}
		return bRet;
	}

	/**
	 * 检测当前网络是否可访问
	 * 
	 * @param timeout
	 * @param onGetNetRun
	 * @param onFailNetRun
	 */
	public void checkNetConnect(final long timeout, final Runnable onGetNetRun, final Runnable onFailNetRun) {
		ensureHandler();
		Runnable checkTask = new Runnable() {

			@Override
			public void run() {
				ArrayList<String> doms = new ArrayList<String>();
				doms.add(TXZ_DOMAIN_1);
				doms.add(TXZ_DOMAIN_2);
				while (!doms.isEmpty()) {
					Socket socket = new Socket();
					try {
						SocketAddress address = new InetSocketAddress(doms.remove(0), 443);
						socket.connect(address, (int) timeout);
						boolean isConn = socket.isConnected();
						if (isConn) {
							if (onGetNetRun != null) {
								onGetNetRun.run();
							}
							return;
						}
					} catch (IOException e) {
					} finally {
						try {
							if (socket != null) {
								socket.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				
				// 如果走到下面，则表示网络不可用
				if (onFailNetRun != null) {
					onFailNetRun.run();
				}
			}
		};

		mCheckNetHandler.post(checkTask);
	}

	private void ensureHandler() {
		if (mCheckNetHandlerThread == null) {
			mCheckNetHandlerThread = new HandlerThread("checkNetThread");
			mCheckNetHandlerThread.start();
		}

		if (mCheckNetHandler == null) {
			mCheckNetHandler = new TXZHandler(mCheckNetHandlerThread.getLooper());
		}
	}
}
