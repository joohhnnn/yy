package com.txznet.txz.module.wifi;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Build;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.util.runnables.Runnable1;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class WifiDirectManager extends IModule {
	static WifiDirectManager sModuleInstance = null;

	private WifiDirectManager() {
		mManager = (WifiP2pManager) GlobalContext.get().getSystemService(
				Context.WIFI_P2P_SERVICE);

		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		GlobalContext.get().registerReceiver(mWifiDirectStateLisener, filter);

		mRunnaleResetChannel.run();
	}

	public static WifiDirectManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (WifiDirectManager.class) {
				if (sModuleInstance == null)
					sModuleInstance = new WifiDirectManager();
			}
		}
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	WifiDirectStateLisener mWifiDirectStateLisener = new WifiDirectStateLisener();

	// /////////////////////////////////////////////////////////////////

	WifiP2pManager mManager;
	Channel mChannel;

	Runnable mRunnaleResetChannel = new Runnable() {
		@Override
		public void run() {
			JNIHelper.logd("mResetChannel run");
			mChannel = mManager.initialize(GlobalContext.get(),
					AppLogic.getBackgroundLooper(), new ChannelListener() {
						@Override
						public void onChannelDisconnected() {
							JNIHelper.logw("mChannel onChannelDisconnected");
							// 5秒后重新建立channel
							resetChannel(5000);
						}
					});

			// 重建通道后自动尝试重连
			if (mConnectingAddr != null) {
				restartConnectDevice();
			}

			startScanPeerList();
		}
	};

	void resetChannel(long delay) {
		// 重置socket连接
		resetConnection();
		// 断开连接信息请求
		stopRequestConnectionInfo();
		// 停止连接设备
		stopConnectDevice();
		// 停止扫描列表
		stopScanPeerList();
		AppLogic.removeBackGroundCallback(mRunnaleResetChannel);
		AppLogic.runOnBackGround(mRunnaleResetChannel, delay);
	}

	// /////////////////////////////////////////////////////////////////

	Runnable mRunnableScanPeerList = new Runnable() {
		@Override
		public void run() {
			JNIHelper.logd("mScanPeerList run");
			mManager.discoverPeers(mChannel,
					new WifiP2pManager.ActionListener() {
						@Override
						public void onSuccess() {
							requestPeers();

							AppLogic.runOnBackGround(
									mRunnableScanPeerList, 10000);
						}

						@Override
						public void onFailure(int reasonCode) {
							JNIHelper.logw("discoverPeers onFailure: "
									+ reasonCode);
							// 5秒后重新扫描列表
							switch (reasonCode) {
							case WifiP2pManager.BUSY:
								break;
							case WifiP2pManager.ERROR:
								break;
							case WifiP2pManager.NO_SERVICE_REQUESTS:
								break;
							case WifiP2pManager.P2P_UNSUPPORTED:
								break;
							}
							AppLogic.runOnBackGround(
									mRunnableScanPeerList, 10000);
						}
					});
		}
	};

	int mScanPeerListErrorCount = 0;

	void startScanPeerList() {
		mScanPeerListErrorCount = 0;
		AppLogic.removeBackGroundCallback(mRunnableScanPeerList);
		mRunnableScanPeerList.run();
	}

	void stopScanPeerList() {
		// TODO 刷新界面扫描状态
		mManager.stopPeerDiscovery(mChannel, null);
		AppLogic.removeBackGroundCallback(mRunnableScanPeerList);
	}

	void requestPeers() {
		mManager.requestPeers(mChannel, new PeerListListener() {
			@Override
			public void onPeersAvailable(WifiP2pDeviceList peerList) {
				// TODO 刷新列表
			}
		});
	}

	// /////////////////////////////////////////////////////////////////

	String mConnectingAddr;// = "0a:d8:33:77:34:4f"; //可采用schema或扫描二维码传入

	int mConnectDeviceErrorCount = 0;

	Runnable mRunnableConnectDevice = new Runnable() {
		@Override
		public void run() {
			WifiP2pConfig config = new WifiP2pConfig();
			config.deviceAddress = mConnectingAddr;
			config.wps.setup = WpsInfo.PBC;

			JNIHelper.logd("begin connect device: " + mConnectingAddr);

			mManager.connect(mChannel, config, new ActionListener() {
				@Override
				public void onSuccess() {
					JNIHelper
							.logd("connect request success, but maybe not connected: "
									+ mConnectingAddr);
					startRequestConnectionInfo();
				}

				@Override
				public void onFailure(int reason) {
					JNIHelper.loge("device connect failed " + reason + ": "
							+ mConnectingAddr);

					mConnectDeviceErrorCount++;
					if (mConnectDeviceErrorCount >= 5) {
						// 连续5次连接失败则重建通道
						resetChannel(5000);
					} else {
						// 重试连接
						AppLogic.runOnBackGround(mRunnableConnectDevice,
								5000);
					}

					// 提示没有找到对应设备
					AppLogic.showToast("连接指定设备失败");

					// TODO 提供重新设置设备的交互
				}
			});
		}
	};

	public void stopConnectDevice() {
		mManager.cancelConnect(mChannel, null);
		AppLogic.removeBackGroundCallback(mRunnableConnectDevice);
	}

	public void startConnectDevice(String addr) {
		mConnectingAddr = addr;
		mRunnaleResetChannel.run();
	}

	void restartConnectDevice() {
		mConnectDeviceErrorCount = 0;
		mRunnableConnectDevice.run();
	}

	// /////////////////////////////////////////////////////////////////

	Runnable mRunnableRequestConnectionInfo = new Runnable() {
		@Override
		public void run() {
			JNIHelper.logd("mRunnableRequestConnectionInfo run");
			mManager.requestConnectionInfo(mChannel,
					new ConnectionInfoListener() {
						@Override
						public void onConnectionInfoAvailable(WifiP2pInfo info) {
							if (info.groupOwnerAddress == null) {
								mRequestConnectionInfoErrorCount++;
								// 连续5次失败，则调用重连
								if (mRequestConnectionInfoErrorCount >= 5) {
									AppLogic.runOnBackGround(
											mRunnableConnectDevice, 5000);
								} else {
									// 5秒后重新获取连接信息
									AppLogic.runOnBackGround(
											mRunnableRequestConnectionInfo,
											5000);
								}
							} else {
								onP2PConnected(info);
							}
						}
					});
		}
	};

	int mRequestConnectionInfoErrorCount = 0;

	void startRequestConnectionInfo() {
		mRequestConnectionInfoErrorCount = 0;
		stopRequestConnectionInfo();
		mRunnableRequestConnectionInfo.run();
	}

	void stopRequestConnectionInfo() {
		AppLogic
				.removeBackGroundCallback(mRunnableRequestConnectionInfo);
	}

	// /////////////////////////////////////////////////////////////////

	WifiP2pDevice mSelfDevice;

	void updateSelfDevice(WifiP2pDevice d) {
		mSelfDevice = d;
		JNIHelper.logd("updateSelfDevice: " + mSelfDevice.deviceName + "["
				+ mSelfDevice.status + "]=" + mSelfDevice.deviceAddress);
	}

	void resetConnection() {
		if (mWifiDirectSocketControl != null) {
			mWifiDirectSocketControl.close();
			mWifiDirectSocketControl = null;
		}
	}

	// /////////////////////////////////////////////////////////////////

	final byte[] EMPTY_REQUEST = new byte[0];
	byte[] mRecvRequest = EMPTY_REQUEST;

	void procRecvRequest() {
		String s = new String(mRecvRequest);
		JNIHelper.logd("proc cmd: " + s);
		AppLogic.showToast(s);

		mRecvRequest = EMPTY_REQUEST;

		AppLogic.runOnBackGround(new Runnable1<String>(s) {
			@Override
			public void run() {
				if (mP1.length() > 0) {
					if (mP1.charAt(0) >= 'a' && mP1.charAt(0) <= 'z')
						sendCommand(mP1.toUpperCase());
					else
						sendCommand(mP1.toLowerCase());
				}
			}
		}, 2000);

	}

	List<ByteBuffer> mRequestList = new ArrayList<ByteBuffer>();

	public void sendCommand(String cmd) {
		JNIHelper.logd("send cmd: " + cmd);
		synchronized (mRequestList) {
			mRequestList.add(ByteBuffer.wrap(cmd.getBytes()));
		}
	}

	boolean needSendCommand() {
		synchronized (mRequestList) {
			return mRequestList.isEmpty() == false;
		}
	}

	ByteBuffer getOneCommand() {
		ByteBuffer b = null;
		synchronized (mRequestList) {
			if (mRequestList.isEmpty() == false) {
				b = ByteBuffer.wrap(mRequestList.get(0).array());
				mRequestList.remove(0);
			}
		}
		return b;
	}

	void onRecvBuffer(byte[] b) {
		byte[] d = new byte[mRecvRequest.length + b.length];
		JNIHelper.logd("recv buffer: " + mRecvRequest.length + "+" + b.length
				+ "=" + d.length);
		if (mRecvRequest.length > 0) {
			System.arraycopy(mRecvRequest, 0, d, 0, mRecvRequest.length);
		}
		System.arraycopy(b, 0, d, mRecvRequest.length, b.length);
		mRecvRequest = d;

		// 处理请求
		procRecvRequest();
	}

	// /////////////////////////////////////////////////////////////////

	WifiDirectSocketControl mWifiDirectSocketControl;

	void onP2PConnected(WifiP2pInfo wifiP2pInfo) {
		JNIHelper.logd("onP2PConnected: isOwner=" + wifiP2pInfo.isGroupOwner
				+ ",addr=" + wifiP2pInfo.groupOwnerAddress);

		stopRequestConnectionInfo();

		if (mWifiDirectSocketControl != null) {
			mWifiDirectSocketControl.close();
		}
		mWifiDirectSocketControl = new WifiDirectSocketControl(wifiP2pInfo);
		mWifiDirectSocketControl.start();
	}

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		return super.onEvent(eventId, subEventId, data);
	}

}
