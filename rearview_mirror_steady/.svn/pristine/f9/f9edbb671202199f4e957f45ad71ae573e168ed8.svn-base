package com.txznet.txz.util.recordcenter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.LittleEndianBytesUtil;
import com.txznet.txz.util.TXZHandler;
import com.txznet.txz.util.recordcenter.cache.DataWriter;
import com.txznet.txz.util.recordcenter.cache.DiscardCacheBuffer;
import com.txznet.txz.util.recordcenter.cache.TraceCacheBuffer_PcmMono16K;

public class RecorderCenter {
	public final static short TXZ_RECORDER_PORT_DEFAULT = 22342; // 默认端口
	public final static int TXZ_MAX_RECORDER = 20; // 最多5个录音机：唤醒、离线识别、在线识别、微信、行车记录仪
	public static final int CACHE_BUFFER_SIZE = 5 * 16000 * 16 / 8; // 缓冲区大小，默认保存5秒

	public final static String UPDATE_PORT = "com.txznet.txz.RecorderCenter.UpdatePort";
	public final static File PORT_CFG_FILE_NAME = new File(GlobalContext.get()
			.getApplicationInfo().dataDir, "../" + ServiceManager.TXZ
			+ "/RecorderCenter.port");

	public static int TXZ_RECORDER_PORT = 0; // 默认端口

	// 免唤醒识别开启状态
	private static boolean bEnableInstantAsr = false;

	//打开AEC声音信号的缓存
	private static boolean bEnableCacheAEC = false;
	
	private static void listenUpdatePort() {
		// 监听广播，更新端口
		IntentFilter f = new IntentFilter(UPDATE_PORT);
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				RecorderCenter.TXZ_RECORDER_PORT = intent.getShortExtra("port",
						(short)RecorderCenter.TXZ_RECORDER_PORT);
				if (RecorderCenter.TXZ_RECORDER_PORT < 0) {
					RecorderCenter.TXZ_RECORDER_PORT += 65536;
				}
			}
		}, f);
		TXZ_RECORDER_PORT = TXZ_RECORDER_PORT_DEFAULT;
		FileInputStream in = null;
		try {
			in = new FileInputStream(PORT_CFG_FILE_NAME);
			byte[] data = new byte[2];
			if (2 == in.read(data, 0, 2)) {
				int port = ((int) ((data[1] << 8) | data[0])) & 0xFFFF;
				if (port < 0) {
					port += 65536;
				}
				RecorderCenter.TXZ_RECORDER_PORT = port;
				LogUtil.logd("load record port: "
						+ RecorderCenter.TXZ_RECORDER_PORT);
			}
		} catch (Exception e) {
			LogUtil.logd("load record port exception: " + e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		LogUtil.logd("final record port: " + RecorderCenter.TXZ_RECORDER_PORT);
	}

	static {
		listenUpdatePort();
	}

	/*************** 测试语音文件接口 *************/
	public final static int BUFFER_SIZE_READ = 1200;
	private static byte[] QUITE_VOICE = null; // 静音包
	private static byte[] mSourceFileBuffer = null;
	private static File mSourceFile = null; // 最后设置的PCM录音文件
	private static FileInputStream mSourceStream = null;

	public static void setSourceFile(String path) {
		if (TextUtils.isEmpty(path)) {
			setSourceFile((File) null);
			return;
		}
		setSourceFile(new File(path));
	}

	public static void setSourceFile(File path) {
		releaseStream();
		mSourceFile = path;
		try {
			mSourceStream = new FileInputStream(mSourceFile);
			LogUtil.logd("recordcenter open record file: "
					+ mSourceStream.toString() + "|"
					+ mSourceFile.getAbsolutePath());
		} catch (Exception e) {
		}
	}

	public static void setEnableInstantAsr(boolean enable) {
		bEnableInstantAsr = enable;
	}
	public static void setEnableCacheAEC(boolean enable) {
		LogUtil.logd("setEnableCacheAEC enable = "+enable);
		bEnableCacheAEC = enable;
	}

	private static void releaseStream() {
		FileInputStream stream = mSourceStream;
		mSourceStream = null;
		if (stream != null) {
			try {
				stream.close();
				LogUtil.logd("recordcenter release record file: "
						+ stream.toString());
			} catch (IOException e) {
			}
		}
		// mSourceFileBuffer = null;
	}

	public static byte[] getQuiteVoice(int len) {
		if (QUITE_VOICE == null || QUITE_VOICE.length < len) {
			QUITE_VOICE = new byte[(len + BUFFER_SIZE_READ - 1)
					/ BUFFER_SIZE_READ * BUFFER_SIZE_READ];
			InputStream in = null;
			try {
				in = GlobalContext.get().getAssets().open("quite.pcm");
				in.read(QUITE_VOICE);
			} catch (Exception e) {
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
					}
				}
			}
		}
		return QUITE_VOICE;
	}

	private static boolean dispatchFileData(int len) {
		if (mSourceStream == null) {
			return false;
		}
		if (mSourceFileBuffer == null || mSourceFileBuffer.length < len) {
			mSourceFileBuffer = new byte[(len + BUFFER_SIZE_READ - 1)
					/ BUFFER_SIZE_READ * BUFFER_SIZE_READ];
		}

		byte[] data = getQuiteVoice(len);
		int r = len;
		try {
			r = mSourceStream.read(mSourceFileBuffer, 0, len);

			// LogUtil.logd("recordcenter read record file: " + r + "|"
			// + mSourceFile.getAbsolutePath());

			if (r < 0) {
				r = len;
			} else {
				data = mSourceFileBuffer;
			}
		} catch (Exception e) {
			r = len;
		}
		for (Client rec : mClients) {
			if (rec == null || rec.mState == CMD_STOP)
				continue;
			rec.write(data, 0, r);
		}
		return true;
	}

	/*************** 测试语音文件接口 *************/

	public static final byte CMD_STOP = 1;
	public static final byte CMD_START = 2;
	public static final byte CMD_START_AEC = 3;
	public static final byte CMD_START_WITH_WAKEUP_DATA = 4;
	public static final byte CMD_START_REFER = 5;
	public static final byte CMD_START_INNER = 6;
	public static final byte CMD_AUDIO_DATA = 60; // 收到语音数据


	/**
	 * 
	 * 录音机客户端
	 *
	 */
	private static class Client {
		byte[] mCmdBuffer = new byte[9];
		int mCmdLen = 0;
		Integer mAudioDataLen = null;
		byte[] mAudioDataLenBuffer = new byte[4];
		byte[] mAudioDataBuffer = null;
		
		int mState = CMD_STOP;
		SocketChannel mSocketChannel;
		boolean mException = false;
		HandlerThread mWriteThread;
		TXZHandler mWriteHandler;
		long mStartTime = 0; // 启动录音的时间
		DiscardCacheBuffer mBuffer = new DiscardCacheBuffer(
				DiscardCacheBuffer.DEFAULT_CACHE_SIZE) {
			@Override
			public String getDebugId() {
				try {
					return Client.this.mSocketChannel.socket()
							.getRemoteSocketAddress().toString();
				} catch (Exception e) {
					return this.toString();
				}
			};
		};

		DataWriter mDataWriter = new DataWriter() {
			@Override
			public int writeData(byte[] data, int offset, int len)
					throws IOException {
				return Client.this.write(data, offset, len);
			}
		};

		private DataWriter mSocketDataWriter = new DataWriter() {
			@Override
			public int writeData(byte[] data, int offset, int len)
					throws IOException {
				return Client.this.mSocketChannel.write(ByteBuffer
						.wrap(data, offset, len));
			}
		};

		private Runnable mReadRunnable1 = new Runnable() {
			@Override
			public void run() {
				read();
			}
		};

		private Runnable mReadRunnable2 = new Runnable() {
			@Override
			public void run() {
				read();
			}
		};

		public void setState(int state) {
			mState = state;
			if (mState == CMD_STOP) {
				flush();
			}

			if (CMD_START_WITH_WAKEUP_DATA == mState) {
				mBuffer = new DiscardCacheBuffer(CACHE_BUFFER_SIZE);
			}
		}

		public void setStartTime(long startTime) {
			mStartTime = startTime;
		}

		public void flush() {
			mBuffer.flush();
		}

		private Runnable runInRead = new Runnable() {
			@Override
			public void run() {
				Client.this.mWriteHandler.heartbeat();
			}
		};
		
		private void read() {
			try {
				mBuffer.read(mSocketDataWriter, runInRead);
			} catch (IOException e) {
				try {
					mSocketChannel.close();
				} catch (IOException e1) {
				}
				// 按关闭套接字处理，让删除操作到一个线程去执行
				// removeRecord(mSocketChannel);
				mException = true;
			}
		}

		public Client(SocketChannel sock) {
			mSocketChannel = sock;
			this.mWriteThread = new HandlerThread("RecWrite" + mSocketChannel.socket().getPort(),
					Thread.NORM_PRIORITY);
			this.mWriteThread.start();
			this.mWriteHandler = new TXZHandler(this.mWriteThread.getLooper());
			this.mWriteHandler.post(new Runnable() {
				@Override
				public void run() {
					TXZHandler.updateToPriorityPriority(Process.THREAD_PRIORITY_FOREGROUND);
				}
			});
		}

		public void release() {
			LogUtil.logd("release client: " + mSocketChannel.socket().getRemoteSocketAddress());
			this.mWriteThread.quit();
		}

		public int write(byte[] data, int offset, int len) {
			int ret = mBuffer.write(data, offset, len);

			this.mWriteHandler.removeCallbacks(mReadRunnable1);
			this.mWriteHandler.removeCallbacks(mReadRunnable2);
			this.mWriteHandler.post(mReadRunnable1);
			this.mWriteHandler.post(mReadRunnable2);

			// LogUtil.logd("update wirte[" + mBufferWriteIndex + "]: "
			// + mSocketChannel.socket().getRemoteSocketAddress());
			return ret;
		}
	}

	// /////////////////////////录音客户端管理///////////////////////////
	private static int mClientCount = 0;
	private static Client[] mClients = new Client[TXZ_MAX_RECORDER];

	private static int mNeedRawCount = 0;

	// 是否有需要原始数据的录音机
	private static boolean needMICData() {
		return (mNeedRawCount > 0);
	}

	private static int mNeedACECount = 0;

	// 是否有需要回音消除数据的录音机
	private static boolean needAECData() {
		// 免唤醒使用的数据缓冲区缓存的是AEC的录音数据，这里需要判断开启状态
		return (mNeedACECount > 0 || bEnableInstantAsr || bEnableCacheAEC);
	}

	private static int mNeedReferCount = 0;

	// 是否有需要参考信号的录音机
	private static boolean needReferData() {
		return mNeedReferCount > 0;
	}

	private static Client createRecord(SocketChannel sock) {
		for (int i = 0; i < mClients.length; ++i) {
			if (mClients[i] != null && mClients[i].mException) {
				LogUtil.logd("get excepiton recorder: "
						+ mClients[i].mSocketChannel.socket().getRemoteSocketAddress());
				mClients[i].release();
				mClients[i] = null;
			}
			if (mClients[i] == null) {
				mClients[i] = new Client(sock);
				LogUtil.logd("record server  create recorder: "
						+ sock.socket().getRemoteSocketAddress());
				if (mClientCount <= i) {
					mClientCount = i + 1;
				}
				return mClients[i];
			} else if (mClients[i].mSocketChannel == sock) {
				LogUtil.logd("record server  create recorder from exist: "
						+ sock.socket().getRemoteSocketAddress());
				return mClients[i];
			}
		}
		LogUtil.loge("record server  create recorder over max length["
				+ mClients.length + "]: "
				+ sock.socket().getRemoteSocketAddress());
		return null;
	}

	// 更改录音机状态
	private static void setRecordState(SocketChannel sock, int state) {
		if(state != 60){
			LogUtil.logd("record server set recorder state[" + state + "]: "
					+ sock.socket().getRemoteSocketAddress());
		}
		Client recorder = null;
		for (int i = 0; i < mClientCount; ++i) {
			if (mClients[i] == null)
				continue;
			if (mClients[i].mSocketChannel == sock) {
				recorder = mClients[i];
				break;
			}
		}
		if (recorder == null) {
			LogUtil.loge("can not find recorder: "
					+ sock.socket().getRemoteSocketAddress());
			return;
		}
		switch (recorder.mState) {
		case CMD_START:
			--mNeedRawCount;
			break;
		case CMD_START_AEC:
			--mNeedACECount;
			break;
		case CMD_START_REFER:
			--mNeedReferCount;
			break;
		case CMD_START_WITH_WAKEUP_DATA:
			--mNeedACECount;
			break;
		}

		recorder.setState(state);
		switch (recorder.mState) {
		case CMD_START:
			++mNeedRawCount;
			break;
		case CMD_START_AEC:
			++mNeedACECount;
			break;
		case CMD_START_REFER:
			++mNeedReferCount;
			break;
		case CMD_START_WITH_WAKEUP_DATA:
			++mNeedACECount;
			break;
		}
		if (state != 60) {
			LogUtil.logd("record server count RAW=" + mNeedRawCount + ", AEC="
					+ mNeedACECount + ", REF=" + mNeedReferCount);
		}
		//回调当前录音机个数
		final IRecorderListener listener = mRecorderListener;
		if (listener != null){
			listener.onRecorderCount(mNeedRawCount, mNeedACECount);
		}
	}

	private static void setRecordStartTime(SocketChannel sock, long startTime) {
		LogUtil.logd("record server set recorder startTime[" + startTime
				+ "]: " + sock.socket().getRemoteSocketAddress());

		Client recorder = findRecorder(sock);
		if (recorder == null) {
			LogUtil.loge("can not find recorder: "
					+ sock.socket().getRemoteSocketAddress());
			return;
		}

		recorder.setStartTime(startTime);
	}

	/**
	 * 根据channel查找指定recorder
	 * 
	 * @param sock
	 * @return recorder，recorder不存在则返回null
	 */
	private static Client findRecorder(SocketChannel sock) {
		Client recorder = null;
		for (int i = 0; i < mClientCount; ++i) {
			if (mClients[i] == null)
				continue;
			if (mClients[i].mSocketChannel == sock) {
				recorder = mClients[i];
				break;
			}
		}

		return recorder;
	}

	// 删除录音机
	private static void removeRecord(SocketChannel sock) {
		LogUtil.logd("record server remove recorder: "
				+ sock.socket().getRemoteSocketAddress());

		try {
			Client recorder = null;
			for (int i = 0; i < mClientCount; ++i) {
				if (mClients[i] == null)
					continue;
				if (mClients[i].mSocketChannel == sock) {
					recorder = mClients[i];
					mClients[i] = null;
					// if (i == mRecorderCount - 1) {
					// --mRecorderCount;
					// }
					break;
				}
			}
			if (recorder == null) {
				LogUtil.logw("record server  can not find recorder: "
						+ sock.socket().getRemoteSocketAddress());
				return;
			}

			switch (recorder.mState) {
			case CMD_START:
				--mNeedRawCount;
				break;
			case CMD_START_AEC:
				--mNeedACECount;
				break;
			case CMD_START_REFER:
				--mNeedReferCount;
				break;
			}
			LogUtil.logd("record server count RAW=" + mNeedRawCount + ", AEC="
					+ mNeedACECount + ", REF=" + mNeedReferCount);
			recorder.release();
			
			//回调当前录音机个数
			final IRecorderListener listener = mRecorderListener;
			if (listener != null){
				listener.onRecorderCount(mNeedRawCount, mNeedACECount);
			}
			
		} finally {
			try {
				sock.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void saveLastCacheBuffer(DataWriter writer) {
		if (mCacheBufferAEC != null) {
			try {
				mCacheBufferAEC.readAll(writer);
			} catch (Exception e) {
			}
		}
	}

	
	// 根据状态分发录音数据
	private static void dispatchData(int state, byte[] data, int offset, int len) {
		for (Client rec : mClients) {
			/*
			 * if (rec == null || rec.mState != state || rec.mState == CMD_STOP)
			 * continue;
			 */

			if (null == rec || CMD_STOP == rec.mState) {
				continue;
			}

			if (CMD_START_WITH_WAKEUP_DATA == rec.mState
					&& CMD_START_AEC == state) {
				// 先从缓冲区读取数据
				if (bEnableInstantAsr || bEnableCacheAEC) {
					if (rec.mStartTime > 0) {
						try {
							mCacheBufferAEC.readByClock(rec.mDataWriter,
									rec.mStartTime);
						} catch (Exception e) {
						}
					}
					rec.setStartTime(0);
				}
				rec.write(data, offset, len);

				continue;
			}

			if (rec.mState != state) {
				continue;
			}

			rec.write(data, offset, len);
		}
	}

	public static TraceCacheBuffer_PcmMono16K mCacheBufferAEC = new TraceCacheBuffer_PcmMono16K(
			CACHE_BUFFER_SIZE);

	// /////////////////////////录音调度管理///////////////////////////
	private static Thread sControlThread = null;

	static ServerSocketChannel mServerSocket = null;
	static Selector mSelector = null;

	// static ByteBuffer mCommandBuffer = ByteBuffer.allocate(1);
	// static ByteBuffer mCommandParamBuffer = ByteBuffer.allocate(8);

	// 处理远程录音机链接
	private static void selectRecorder() {
		while (true) {
			try {
				int ret = mSelector.select();
				// 处理select结果
				if (ret == 0)
					continue;
				Iterator<SelectionKey> keyIter = mSelector.selectedKeys()
						.iterator();
				
				
				while (keyIter.hasNext()) {
					SelectionKey key = keyIter.next();
					// 移除处理过的key
					keyIter.remove();

					if (!key.isValid()) {
						continue;
					}

					if (key.isAcceptable()) {
						// 处理新的录音连接请求
						SocketChannel sock = mServerSocket.accept();
						sock.configureBlocking(false);
						Client rec = createRecord(sock);
						sock.register(mSelector, SelectionKey.OP_READ, rec);
						continue;
					}

					if (key.isReadable()) {
						// 处理录音机的指令
						SocketChannel sock = (SocketChannel) key.channel();
						int read = -1;
						Client recorder = findRecorder(sock);
						if (recorder == null) {
							LogUtil.loge("record server find record failed:"
									+ sock.socket().getRemoteSocketAddress());
							try {
								sock.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
							continue;
						}
						try {
							if (recorder.mState == CMD_AUDIO_DATA) {
								if (recorder.mAudioDataLen == null ) {
									read = sock.read(ByteBuffer.wrap(
											recorder.mAudioDataLenBuffer,
											recorder.mCmdLen,
											recorder.mAudioDataLenBuffer.length
													- recorder.mCmdLen));
									if (read == 0) {
										continue;
									}
									if (read > 0) {
										recorder.mCmdLen += read;
										if (recorder.mCmdLen >= recorder.mAudioDataLenBuffer.length) {
											recorder.mAudioDataLen = LittleEndianBytesUtil
													.bytesToInt(recorder.mAudioDataLenBuffer);
											recorder.mCmdLen = 0;
											if (recorder.mAudioDataBuffer == null
													|| recorder.mAudioDataBuffer.length < recorder.mAudioDataLen) {
												recorder.mAudioDataBuffer = new byte[(recorder.mAudioDataLen + 1023) / 1024 * 1024];
											}
										}
										continue;
									}
								} else {
									read = sock.read(ByteBuffer.wrap(
											recorder.mAudioDataBuffer,
											recorder.mCmdLen,
											recorder.mAudioDataLen
													- recorder.mCmdLen));
									if (read >= 0) {
										if (read > 0) {
//											fos.write(
//													recorder.mAudioDataBuffer,
//													recorder.mCmdLen, read);
											dispatchData(CMD_START_INNER,
													recorder.mAudioDataBuffer,
													recorder.mCmdLen, read);
											recorder.mCmdLen += read;
										}
										if (recorder.mCmdLen >= recorder.mAudioDataLen) {
											recorder.mState = CMD_STOP;
											recorder.mCmdLen = 0;
											recorder.mAudioDataLen = null;
										}
										continue;
									}

								}
							} else {
								read = sock.read(ByteBuffer.wrap(
										recorder.mCmdBuffer, recorder.mCmdLen,
										recorder.mCmdBuffer.length
												- recorder.mCmdLen));
//								LogUtil.logd("record server recv cmd len[" + read
//										+ "]: "
//										+ sock.socket().getRemoteSocketAddress());
							}
						} catch (Exception e) {
							removeRecord(sock);
							continue;
						}
						if (read < 0) {
							removeRecord(sock);
							continue;
						}

						recorder.mCmdLen += read;
						if (recorder.mCmdLen >= 9) {
							int state = recorder.mCmdBuffer[0];
							// 判断是否是CMD_START_WITH_WAKEUP_DATA
							if (state == CMD_START_WITH_WAKEUP_DATA /*|| state == CMD_AUDIO_DATA*/) {
								long startTime = LittleEndianBytesUtil
										.bytesToLong(recorder.mCmdBuffer, 1);
								LogUtil.logd("record server START_WITH_DATA got startTime="
										+ startTime);
								setRecordStartTime(sock, startTime);
							}
							setRecordState(sock, state);
							recorder.mCmdLen = 0;
							continue;
						}

						continue;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 启动控制端口，监听录音机控制连接
	private static void controlMain() {
		if (mServerSocket != null)
			return;
		if (RecorderCenter.TXZ_RECORDER_PORT == 0) {
			RecorderCenter.TXZ_RECORDER_PORT = RecorderCenter.TXZ_RECORDER_PORT_DEFAULT;
		}
		while (true) {
			try {
				try {
					if(mServerSocket != null){
						mServerSocket.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				mServerSocket = null;
				mServerSocket = ServerSocketChannel.open();
				mServerSocket.socket()
						.bind(new InetSocketAddress(
								RecorderCenter.TXZ_RECORDER_PORT));
				break;
			} catch (Exception e) {
				++RecorderCenter.TXZ_RECORDER_PORT;
			}
		}
		// 写入端口号到文件
		if (RecorderCenter.TXZ_RECORDER_PORT != RecorderCenter.TXZ_RECORDER_PORT_DEFAULT) {
			LogUtil.logd("save record port: "
					+ RecorderCenter.TXZ_RECORDER_PORT);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(PORT_CFG_FILE_NAME);
				byte[] data = new byte[2];
				data[0] = (byte) ((RecorderCenter.TXZ_RECORDER_PORT >>> 0) & 0xff);
				data[1] = (byte) ((RecorderCenter.TXZ_RECORDER_PORT >>> 8) & 0xff);
				out.write(data);
			} catch (Exception e) {
				LogUtil.logd("save record port exception");
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (Exception e) {
					}
				}
			}
			if (!PORT_CFG_FILE_NAME.setReadable(true, false)) {
				LogUtil.logd("save record port failed");
			}
		}
		// 通知客户端更新port
		Intent in = new Intent(UPDATE_PORT);
		in.putExtra("port", (short)(RecorderCenter.TXZ_RECORDER_PORT & 0xFFFF)); //兼容老版本，老版本的数据类型是short
		GlobalContext.get().sendBroadcast(in);
		// 处理客户端连接
		try {
			mServerSocket.configureBlocking(false);

			mSelector = Selector.open();
			mServerSocket.register(mSelector, SelectionKey.OP_ACCEPT);

			selectRecorder();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 创建控制线程
	private static void createControlThread() {
		if (sControlThread == null) {
			sControlThread = new Thread() {
				@Override
				public void run() {
					TXZHandler.updateToPriorityPriority(Process.THREAD_PRIORITY_FOREGROUND);
					controlMain();
				}
			};
			sControlThread.setName("RecCtrl");
			sControlThread.setPriority(Thread.NORM_PRIORITY);
			sControlThread.start();
		}
	}

	// /////////////////////////录音数据读取///////////////////////////

	private static Thread sRecordThread = null;
	private static ITXZSourceRecorder mRecorder = null;

	private static DataWriter mMICWriter = new DataWriter() {
		@Override
		public int writeData(byte[] data, int offset, int len) {
			if (dispatchFileData(len)) {
				return len;
			}
			dispatchData(CMD_START, data, offset, len);
			return len;
		}
	};

	private static DataWriter mAECWriter = new DataWriter() {
		@Override
		public int writeData(byte[] data, int offset, int len) {
			if (dispatchFileData(len)) {
				return len;
			}
			if (bEnableInstantAsr || bEnableCacheAEC) {
				mCacheBufferAEC.write(data, offset, len);
			}
			dispatchData(CMD_START_AEC, data, offset, len);
			return len;
		}
	};

	private static DataWriter mReferWriter = new DataWriter() {
		@Override
		public int writeData(byte[] data, int offset, int len) {
			dispatchData(CMD_START_REFER, data, offset, len);
			return len;
		}
	};
	
	private static IRecorderListener mRecorderListener = null;
	public static synchronized void run(IRecorderListener listener){
		createControlThread();
		mRecorderListener = listener;
	}
	private final static long RECORD_SYNC_TIMEOUT = 5000;
	private final static long RECORD_CHECK_RECOVERY = 2000;
	private final static long TIMEOUT_ACCURACY = 10;//超时时间精度
	public static synchronized void run(ITXZSourceRecorder recorder, IRecorderListener listener) {
		createControlThread();
		
		mRecorderListener = listener;
		// 释放前一次的录音线程
		if (mRecorder != null) {
			mRecorder.preStopRecorder();
			boolean bNeedCheck = false;
			LogUtil.logd("RecorderCenter stop");
			mRecorder.stopRecorder();
			long begin = SystemClock.elapsedRealtime();
			LogUtil.logd("RecorderCenter wait...");
			try {
				sRecordThread.join(RECORD_SYNC_TIMEOUT);
			} catch (Exception e) {
				LogUtil.loge("RecorderCenter wait exception : " + e.toString());
			}
			if (SystemClock.elapsedRealtime() - begin >= RECORD_SYNC_TIMEOUT - TIMEOUT_ACCURACY){
				LogUtil.loge("RecorderCenter wait too long");
				bNeedCheck = true;
			}
			mRecorder.releaseRecorder();
			
			//检测录音阻塞是否可以恢复
			if (bNeedCheck){
				begin = SystemClock.elapsedRealtime();
				LogUtil.loge("RecorderCenter check recovery...");
				try {
					sRecordThread.join(RECORD_CHECK_RECOVERY);
				} catch (Exception e) {
					LogUtil.loge("RecorderCenter wait exception : " + e.toString());
				}
				if (SystemClock.elapsedRealtime() - begin >= RECORD_CHECK_RECOVERY - TIMEOUT_ACCURACY){
					LogUtil.loge("RecorderCenter can not recovery");
					mRecorder.die();
				}
				LogUtil.loge("RecorderCenter check recovery end");
			}
		}

		// 如果recorder为null表示停止录音
		if (recorder == null) {
			mRecorder = recorder;
			return;
		}
		
		// 启动新的录音线程
		mRecorder = recorder;
		sRecordThread = new Thread() {
			@Override
			public void run() {
				TXZHandler.updateToPriorityPriority(Process.THREAD_PRIORITY_FOREGROUND);
				final ITXZSourceRecorder sRecorder = mRecorder;
				try {
					sRecorder.startRecorder(new Runnable() {
						@Override
						public void run() {
							sRecorder.setDataWriter(
									ITXZSourceRecorder.READER_TYPE_MIC,
									needMICData() ? mMICWriter : null);
							sRecorder.setDataWriter(
									ITXZSourceRecorder.READER_TYPE_AEC,
									needAECData() ? mAECWriter : null);
							sRecorder.setDataWriter(
									ITXZSourceRecorder.READER_TYPE_REFER,
									needReferData() ? mReferWriter : null);
						}
					});
				} catch (Exception e) {
					LogUtil.logd("RecRead thead occur error : " + e.toString());
					if (sRecorder != null && sRecorder.isLive()){
						LogUtil.logd("RecRead thead notify error");
						sRecorder.notifyError(0);
					}
				}
				LogUtil.logd("RecRead thead end");
			}
		};
		sRecordThread.setName("RecRead");
		sRecordThread.setPriority(Thread.NORM_PRIORITY);
		mRecorder.preStartRecorder();
		sRecordThread.start();
	}
}
