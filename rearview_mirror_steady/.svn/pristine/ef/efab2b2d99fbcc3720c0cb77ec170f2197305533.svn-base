package com.txznet.audio.server.response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;

public abstract class CopyOfBlockTcpClient {

	private static final String TAG = "[MUSIC][TCP] ";
	long mTimeStamp;
	int mConnectTimeout = 5000;
	int mReadTimeout = 5000;
	int mSuggestRetryDelayDefault = 1000;
	int mSelectTimeout = 1000;
	/**
	 * 其实read返回0有3种情况，//@author ASUS User
	 * 一是某一时刻socketChannel中当前（注意是当前）没有数据可以读，这时会返回0，//@author ASUS User
	 * 其次是bytebuffer的position等于limit了，即bytebuffer的remaining等于0，这个时候也会返回0，//@author ASUS User
	 * 最后一种情况就是客户端的数据发送完毕了，这个时候客户端想获取服务端的反馈调用了recv函数，若服务端继续read，这个时候就会返回0。//@author ASUS User
	 */
	int readZeroSum=0;//读取0个字节的情况
	
	boolean mConnected = false;
	boolean mClosed = false;
	boolean mCanceled = false;
	boolean mIsError = false;
	boolean mIsTimeout = false;

	SocketChannel mSocketChannel = null;
	Selector mSelector = null;
	ByteBuffer mByteBufferRead = ByteBuffer.allocate(1024 * 200);

	int mSuggestRetryDelay = 0;

	protected void setSuggestRetryDelay(int delay) {
		mSuggestRetryDelay = delay;
	}

	public int getSuggestRetryDelay() {
		return mSuggestRetryDelay;
	}

	public abstract void onConnect() throws IOException;

	public void onIdle() throws IOException {

	}

	public void onDisconnect() throws IOException {

	}

	public abstract void onRead(byte[] data, int offset, int len)
			throws IOException;

	public void onError(String errDesc) throws IOException {

	}

	public void onConnectTimout() throws IOException {

	}

	public void onReadTimout() throws IOException {

	}

	private void _onError(String errDesc) throws IOException {
		mIsError = true;
		LogUtil.loge(TAG+"BlockTcpClient[" + this.hashCode() + "] " + errDesc);
		onError(errDesc);
	}

	public void setConnectTimeout(int t) {
		mConnectTimeout = t;
	}

	public void setReadTimeout(int t) {
		mReadTimeout = t;
	}

	public void setSelectTimeout(int t) {
		mSelectTimeout = t;
	}

	private void reset() {
		release();

		mConnected = false;
		mClosed = false;
		mCanceled = false;
		mIsError = false;
		mIsTimeout = false;

		mSuggestRetryDelay = 0;
	}

	public void connect(String server, int port) throws IOException {
		reset();

		try {
			try {
				mSocketChannel = SocketChannel.open();
			} catch (IOException e) {
				setSuggestRetryDelay(mSuggestRetryDelayDefault);
				this._onError("create error: " + e.getMessage());
				return;
			}
			try {
				mSocketChannel.configureBlocking(false);
			} catch (IOException e) {
				setSuggestRetryDelay(mSuggestRetryDelayDefault);
				this._onError("config error: " + e.getMessage());
				return;
			}

			try {
				mSelector = Selector.open();
			} catch (IOException e) {
				setSuggestRetryDelay(mSuggestRetryDelayDefault);
				this._onError("create selector error: " + e.getMessage());
				return;
			}

			mTimeStamp = System.currentTimeMillis();

			try {
				mSocketChannel.connect(new InetSocketAddress(server, port));
			} catch (Exception e) {
				setSuggestRetryDelay(mSuggestRetryDelayDefault);
				this._onError("connect Exception: [" + server + ":" + port
						+ "]" + e);
				return;
			} catch (Error e) {
				setSuggestRetryDelay(mSuggestRetryDelayDefault);
				this._onError("connect error: [" + server + ":" + port + "]"
						+ e);
				return;
			}
			try {
				mSocketChannel.register(mSelector, SelectionKey.OP_CONNECT
						| SelectionKey.OP_READ);
			} catch (ClosedChannelException e) {
				setSuggestRetryDelay(mSuggestRetryDelayDefault);
				this._onError("register selector error: " + e.getMessage());
				return;
			}

			while (true) {
				// 取消判断
				if (mCanceled) {
					break;
				}

				// 空闲处理
				onIdle();

				if (mClosed)
					return;

				if (mConnected) {
					if (System.currentTimeMillis() - mTimeStamp > mReadTimeout) {
						LogUtil.logw("BlockTcpClient[" + this.hashCode()
								+ "] onReadTimout: " + mReadTimeout);

						setSuggestRetryDelay(mSuggestRetryDelayDefault);

						mIsTimeout = true;
						onReadTimout();

						return;
					}
				} else {
					if (System.currentTimeMillis() - mTimeStamp > mConnectTimeout) {
						LogUtil.logw("BlockTcpClient[" + this.hashCode()
								+ "] onConnectTimout: " + mConnectTimeout);

						setSuggestRetryDelay(mSuggestRetryDelayDefault);
						
						mIsTimeout = true;
						onConnectTimout();

						return;
					}
				}

				// 开始select
				int ret = 0;
				try {
					ret = mSelector.select(mSelectTimeout);
				} catch (IOException e) {
					setSuggestRetryDelay(mSuggestRetryDelayDefault);
					this._onError("select error: " + e.getMessage());
				}

				// 处理select结果
				if (ret == 0) {
//					if (Constant.ISTESTDATA) {
//						LogUtil.logd("ret =" + ret);
//					}
					continue;
				}
				Iterator<SelectionKey> keyIter = mSelector.selectedKeys()
						.iterator();
				while (keyIter.hasNext()) {
					SelectionKey key = keyIter.next();
					keyIter.remove();
					if (key.isReadable()) {
						if (!mConnected) {
							setSuggestRetryDelay(mSuggestRetryDelayDefault);
							this._onError("connect status error");
							return;
						}

						try {
							int countR = 0;
							while (true) {
								onIdle();
								mTimeStamp = System.currentTimeMillis();
								if (countR==0) {
									mByteBufferRead.clear();
								}
								int r = mSocketChannel.read(mByteBufferRead);
								if (Constant.ISTESTDATA) {
									LogUtil.logd("r=" + r);
								}
								if (r < 0) {
									cancel();
									break;
								} else if (r > 0) {
									readZeroSum=0;
									countR += r;
									if (Constant.ISTESTDATA) {
										LogUtil.logd("r=" + r + "/" + countR
												+ ",mByteBufferRead="
												+ mByteBufferRead.capacity());
									}
									if (countR>400*1024) {//预缓冲量达到50k就发送到播放器
										onRead(mByteBufferRead.array(), 0, countR);
										countR=0;
									}
								} else {
									if (readZeroSum++>3) {
										break;
									}
								}
							}
							if (countR>0) {
								onRead(mByteBufferRead.array(), 0, countR);
								countR=0;
							}
						} catch (Exception e) {
							if (mCanceled == false) {
								LogUtil.logw("BlockTcpClient["
										+ this.hashCode() + "] onDisconnect: "
										+ e.getClass() + "-" + e.getMessage());

								setSuggestRetryDelay(mSuggestRetryDelayDefault);
								mClosed = true;
								onDisconnect();
							}
							return;
						}
						continue;
					}
					if (key.isConnectable()) {
						try {
							mConnected = mSocketChannel.finishConnect();
						} catch (IOException e) {
							this._onError("connect error: " + e.getMessage());
							return;
						}

						mTimeStamp = System.currentTimeMillis();

						onConnect();

						if (mClosed)
							return;

						continue;
					}
				}
			}
		} catch (RuntimeException e) {
		} finally {
			release();
		}
	}

	private void release() {
		mClosed = true;
		if (mSocketChannel != null) {
			try {
				mSocketChannel.close();
				mSocketChannel = null;
			} catch (Exception e) {
			}
		}
		if (mSelector != null) {
			try {
				mSelector.close();
			} catch (Exception e) {
			}
			mSelector = null;
		}
	}

	public boolean isError() {
		return mIsError;
	}

	public boolean isTimeout() {
		return mIsTimeout;
	}

	public boolean isClosed() {
		return mClosed;
	}

	public boolean isConnected() {
		return mConnected;
	}

	public boolean isCanceled() {
		return mCanceled;
	}

	public void cancel() {
		mCanceled = true;

		release();
	}

	public void write(byte[] data, int offset, int len) throws IOException {
		if (mSocketChannel != null) {
			try {
				mSocketChannel.write(ByteBuffer.wrap(data, offset, len));
			} catch (Exception e) {
				_onError("write data[" + len + "] error: " + e.getMessage());
				release();
			}
		}
	}

	public void write(byte[] data) throws IOException {
		write(data, 0, data.length);
	}

	public void write(String data) throws IOException {
		write(data.getBytes());
	}
}
