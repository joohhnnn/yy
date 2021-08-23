package com.txznet.txz.module.wifi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import android.annotation.TargetApi;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Build;

import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class WifiDirectSocketControl {
	final int COMMU_PORT = 1987;
	final int RECONNECT_INTERVAL = 5000;

	WifiP2pInfo mWifiP2pInfo;

	public WifiDirectSocketControl(WifiP2pInfo wifiP2pInfo) {
		mWifiP2pInfo = wifiP2pInfo;
	}

	Thread mNetThread;
	Selector mSelector;
	ServerSocketChannel mServerSocketChannel;
	SocketChannel mClientSocketChannel;

	Runnable mRunnableConnectServer = new Runnable() {
		@Override
		public void run() {
			try {
				AppLogic
						.removeBackGroundCallback(mRunnableConnectServer);
				mClientSocketChannel = SocketChannel.open();
				mClientSocketChannel.configureBlocking(false);
				mClientSocketChannel.connect(new InetSocketAddress(
						mWifiP2pInfo.groupOwnerAddress, COMMU_PORT));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	void initServer() {
		JNIHelper.logd("initServer");
		try {
			mServerSocketChannel = ServerSocketChannel.open();
			mServerSocketChannel.configureBlocking(false);
			mServerSocketChannel.socket().bind(
					new InetSocketAddress(mWifiP2pInfo.groupOwnerAddress,
							COMMU_PORT));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void connectServer() {
		JNIHelper.logd("connectServer");
		AppLogic.removeBackGroundCallback(mRunnableConnectServer);
		mRunnableConnectServer.run();
	}

	void reconnectServer(long delay) {
		JNIHelper.logd("reconnectServer");
		AppLogic.removeBackGroundCallback(mRunnableConnectServer);
		AppLogic.runOnBackGround(mRunnableConnectServer, delay);
	}

	void onAcceptable(SelectionKey key) {
		if (mServerSocketChannel != key.channel()) {
			JNIHelper.logw("unknow acceptable socket");
			return;
		}
		if (mClientSocketChannel != null) {
			JNIHelper.logw("already accept");
			try {
				mServerSocketChannel.accept().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		try {
			JNIHelper.logd("client accept");
			mClientSocketChannel = mServerSocketChannel.accept();
			mClientSocketChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void onConnnectable(SelectionKey key) {
		if (mClientSocketChannel != key.channel()) {
			JNIHelper.logw("unknow connectable socket");
			return;
		}
		try {
			if (mClientSocketChannel.finishConnect()) {
				JNIHelper.logd("client connected");
				return;
			}
		} catch (IOException e) {
		}
		if (mClientSocketChannel.isConnected() == false) {
			JNIHelper.loge("connect failed");
			try {
				mClientSocketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mClientSocketChannel = null;
			reconnectServer(RECONNECT_INTERVAL);
		}
	}

	void onWritable(SelectionKey key) {
		if (mClientSocketChannel != key.channel()) {
			JNIHelper.logw("unknow writable socket");
			return;
		}
		if (mClientSocketChannel.isConnected() == false) {
			JNIHelper.logw("client socket not connect");
			return;
		}

		// 发送请求队列
		ByteBuffer b = WifiDirectManager.getInstance().getOneCommand();
		if (b != null) {
			try {
				mClientSocketChannel.write(b);
			} catch (IOException e) {
				JNIHelper.logw("write buffer error");
				e.printStackTrace();
			}
		}
	}

	void onReadable(SelectionKey key) {
		if (mClientSocketChannel != key.channel()) {
			JNIHelper.logw("unknow readable socket");
			return;
		}
		ByteBuffer buf = ByteBuffer.allocate(1024);
		int n;
		try {
			n = mClientSocketChannel.read(buf);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (n <= 0) {
			JNIHelper.logw("client closed");
			try {
				mClientSocketChannel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mClientSocketChannel = null;
			if (mWifiP2pInfo.isGroupOwner == false) {
				reconnectServer(RECONNECT_INTERVAL); // 尝试重连
			}
			return;
		}

		byte[] b = new byte[n];
		System.arraycopy(buf.array(), 0, b, 0, n);

		WifiDirectManager.getInstance().onRecvBuffer(b);
	}

	boolean initSelector() {
		if (mSelector != null) {
			try {
				mSelector.close();
			} catch (IOException e) {
				JNIHelper.logw("select close failed");
				e.printStackTrace();
			}
		}
		try {
			mSelector = Selector.open();
		} catch (IOException e) {
			JNIHelper.loge("select open failed");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	boolean initServerSelector() {
		if (mServerSocketChannel != null) {
			try {
				mServerSocketChannel
						.register(mSelector, SelectionKey.OP_ACCEPT);
			} catch (ClosedChannelException e) {
				JNIHelper.loge("select server socket failed");
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	boolean initClientSelector() {
		if (null != mClientSocketChannel) {
			int op = 0;
			if (mClientSocketChannel.isConnected() == false) {
				op |= SelectionKey.OP_CONNECT;
			} else {
				op |= SelectionKey.OP_READ;
				if (WifiDirectManager.getInstance().needSendCommand()) {
					op |= SelectionKey.OP_WRITE;
				}
			}
			try {
				mClientSocketChannel.register(mSelector, op);
			} catch (ClosedChannelException e) {
				JNIHelper.loge("select client socket failed: " + op);
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	void onThreadStart() {
		if (mWifiP2pInfo.isGroupOwner) {
			initServer();
		} else {
			connectServer();
		}
		while (mNetThread != null) {
			if (initSelector() == false)
				break;
			if (initServerSelector() == false)
				break;
			if (initClientSelector() == false)
				break;
			try {
				mSelector.select(500);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			// JNIHelper.logd("mSelector.selectedKeys size=" +
			// mSelector.selectedKeys().size() + "/" +
			// mSelector.keys().size());
			Iterator<SelectionKey> iter = mSelector.selectedKeys().iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				// 删除已选的key,以防重复处理
				iter.remove();
				if (key.isValid() == false)
					continue;
				// 客户端请求连接事件
				if (key.isAcceptable()) {
					onAcceptable(key);
					continue;
				}
				// 客户端连接
				if (key.isConnectable()) {
					onConnnectable(key);
					continue;
				}
				// 可写
				if (key.isWritable()) {
					onWritable(key);
					continue;
				}
				// 可读的事件
				if (key.isReadable()) {
					onReadable(key);
					continue;
				}
			} // while (iter.hasNext())
		} // dead loop
		clear();
	}

	public void start() {
		close();
		mNetThread = new Thread() {
			@Override
			public void run() {
				onThreadStart();
			}
		};
		mNetThread.start();
	}

	void clear() {
		AppLogic.removeBackGroundCallback(mRunnableConnectServer);
		
		if (mSelector != null) {
			try {
				mSelector.close();
			} catch (IOException e) {
			}
			mSelector = null;
		}
		if (mClientSocketChannel != null) {
			try {
				mClientSocketChannel.close();
			} catch (IOException e) {
			}
			mClientSocketChannel = null;
		}
		if (mServerSocketChannel != null) {
			try {
				mServerSocketChannel.close();
			} catch (IOException e) {
			}
			mServerSocketChannel = null;
		}
	}

	public void close() {
		if (mNetThread != null) {
			Thread t = mNetThread;
			mNetThread = null;
			t.interrupt();
		}
		// 等待结束
		while (mSelector != null)
			;
	}
}
