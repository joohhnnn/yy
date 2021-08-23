package com.txznet.txz.util.recordcenter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.media.AudioRecord;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.txz.ui.record.UiRecord;
import com.txz.ui.record.UiRecord.RecordData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.txz.util.LittleEndianBytesUtil;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.recordcenter.cache.DataWriter.OutputStreamDataWriter;
import com.txznet.txz.util.recordcenter.cache.DiscardCacheBuffer;
import com.txznet.txz.util.runnables.Runnable3;
import com.txznet.txz.util.runnables.Runnable4;


public class TXZAudioRecorder implements ITXZRecorder {
	private int mRecordType = ITXZSourceRecorder.READER_TYPE_MIC;
	private SocketChannel mSocket = null;
	private Selector mSelector = null;
	// private InputStream mStreamRead = null;
	// private OutputStream mStreamWrite = null;
	private boolean mIsRecording = false;

	private byte[] mCommand = new byte[9];

	private void connectCenter() {
		try {
			mSocket = SocketChannel.open();
			mSocket.configureBlocking(true);
			mSocket.connect(new InetSocketAddress("127.0.0.1",
					RecorderCenter.TXZ_RECORDER_PORT));
			mSocket.finishConnect();
			mSocket.configureBlocking(false);
			mSelector = Selector.open();
			LogUtil.logd("record client create: " 
					+ this.mSocket.socket().getLocalSocketAddress());
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.loge("TXZAudioRecorder error : " + e.toString());
		}
	}

	/**
	 * 
	 * @param needAEC
	 *            是否需要AEC信号，否则直接拿去mic信号
	 */
	public TXZAudioRecorder(boolean needAEC) {
		this.mRecordType = needAEC ? ITXZSourceRecorder.READER_TYPE_AEC
				: ITXZSourceRecorder.READER_TYPE_MIC;

		connectCenter();
	}

	/**
	 * 
	 * @param type
	 *            录音机类型，参考ITXZSourceRecorder.READER_TYPE_XXX
	 */
	public TXZAudioRecorder(int type) {
		this.mRecordType = type;

		connectCenter();
	}

	public TXZAudioRecorder() {
		this(ITXZSourceRecorder.READER_TYPE_MIC);
	}

	/**
	 * 设置录音机类型
	 * 
	 * @param type
	 *            录音机类型，参考ITXZSourceRecorder.READER_TYPE_XXX
	 */
	public synchronized void setType(int type) {
		this.mRecordType = type;
		if (mIsRecording) {
			startRecording();
		}
	}

	@Override
	public int startRecording() {
		return startRecording(null);
	}

	/**
	 * 从指定时间点启动录音
	 * 
	 * @param startTime
	 * @return
	 */
	public synchronized int startRecording(Long startTime) {
		try {
			LogUtil.logd("record client start cmd: "
					+ this.mSocket.socket().getLocalSocketAddress()
					+ ", startTime = " + startTime);

			if (startTime == null || startTime <= 0) {
				switch (this.mRecordType) {
				case ITXZSourceRecorder.READER_TYPE_AEC:
					mCommand[0] = RecorderCenter.CMD_START_AEC;
					break;
				case ITXZSourceRecorder.READER_TYPE_REFER:
					mCommand[0] = RecorderCenter.CMD_START_REFER;
					break;
				case ITXZSourceRecorder.READER_TYPE_INNER:
					mCommand[0] = RecorderCenter.CMD_START_INNER;
					break;
				case ITXZSourceRecorder.READER_TYPE_MIC:
					mCommand[0] = RecorderCenter.CMD_START;
					break;
				default:
					throw new RuntimeException("unknow recorder type");
				}
			} else {
				mCommand[0] = RecorderCenter.CMD_START_WITH_WAKEUP_DATA;
				// 传入开始录音时间点
				byte[] startTimeData = LittleEndianBytesUtil
						.longToBytes(startTime);
				System.arraycopy(startTimeData, 0, mCommand, 1, 8);
			}

			this.mSocket.write(ByteBuffer.wrap(mCommand));
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.loge("TXZAudioRecorder error : " + e.toString());
			reConnectCenter();
			return -1;
		}
		mIsRecording = true;
		return 0;
	}

	@Override
	public synchronized void stop() {
		if(this.mSocket == null || mSelector == null){
			LogUtil.logw("record client stop warnning: Client has released");
			return;
		}
		mIsRecording = false;
		try {
			LogUtil.logd("record client stop cmd: "
					 + this.mSocket.socket().getLocalSocketAddress());
			mCommand[0] = RecorderCenter.CMD_STOP;
			this.mSocket.write(ByteBuffer.wrap(mCommand));
			mSelector.wakeup();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getRecordingState() {
		return mIsRecording ? AudioRecord.RECORDSTATE_RECORDING
				: AudioRecord.RECORDSTATE_STOPPED;
	}

	@Override
	public synchronized void release() {
		mIsRecording = false;
		// if (mStreamRead != null) {
		// closeQuitely(mStreamRead);
		// mStreamRead = null;
		// }
		// if (mStreamWrite != null) {
		// closeQuitely(mStreamWrite);
		// mStreamWrite = null;
		// }
		if (mSelector != null) {
			try {
				mSelector.close();
			} catch (IOException e) {
			}
			mSelector = null;
		}
		if (mSocket != null) {
			LogUtil.logd("record client release: " 
					+ this.mSocket.socket().getLocalSocketAddress());
			try {
				mSocket.close();
			} catch (Exception e) {
			}
			mSocket = null;
		}
	}

	DiscardCacheBuffer mReadBuffer = null;

	public void beginSaveCache(int cacheSize) {
		mReadBuffer = new DiscardCacheBuffer(cacheSize);
	}

	public boolean endSaveCache(String voiceId, RecordData recordData, boolean bEncrypt) {
		AppLogicBase.runOnSlowGround(new Runnable3<String, RecordData, Boolean>(voiceId, recordData, bEncrypt) {
			@Override
			public void run() {
				String rawFileName;
				if(mP3){
					rawFileName = "txz_asr_"+mP1+RecordFile.SUFFIX_PCM;
				}else{
					rawFileName = "."+android.os.Process.myPid()+mP1;
				}
				File rawFile = new File(Environment.getExternalStorageDirectory()+
						"/txz/voice/" + rawFileName);
				boolean saveRawFile = TXZConfigManager.getInstance().isVoiceprintRecognitionEnable();
				if(RecordFile.ENABLE_TEST_DEFINIT_VOICE_NAME){
					File voiceDir = new File(Environment.getExternalStorageDirectory(), "/txz/voice1/");
					if (!voiceDir.exists()) {
						voiceDir.mkdirs();
					}
					rawFile = new File(Environment.getExternalStorageDirectory()+
							"/txz/voice1/" + RecordFile.mDefinitVoiceName + mP1 + RecordFile.SUFFIX_PCM);
				}
				try {
					OutputStream out = new FileOutputStream(rawFile);
					if(!endSaveCache(out)){
						if (sRecordFileStateListeners != null) {
							for (RecordFileStateListener recordFileStateListener : sRecordFileStateListeners) {
								recordFileStateListener.saveFail(rawFile);
							}
						}
						return;
					}
					if (sRecordFileStateListeners != null) {
						for (RecordFileStateListener recordFileStateListener : sRecordFileStateListeners) {
							recordFileStateListener.saveSuccess(rawFile);
						}
					}
					out.close();
					String fileName = "txz_asr_"+mP1+RecordFile.SUFFIX_RF;
					createRecordFile(Environment.getExternalStorageDirectory()+
							"/txz/voice/"+fileName, rawFile, mP2);

				} catch (Exception e) {
				} finally {
					if (!saveRawFile) {
						if(rawFile != null && !mP3){
							rawFile.delete();
						}
					}
				}
			}
		}, 0);
		return true;
	}

	private static List<RecordFileStateListener> sRecordFileStateListeners = new ArrayList<RecordFileStateListener>();
	public static void addRecordFileStateListener(RecordFileStateListener listener) {
		sRecordFileStateListeners.add(listener);
	}

	public static void removeRecordFileStateListener(RecordFileStateListener listener) {
		sRecordFileStateListeners.remove(listener);
	}
	public interface RecordFileStateListener {
		void saveSuccess(File file);
		void saveFail(File file);
	}

	/**
	 * 创建RF文件
	 * @param filePath 
	 * @param rawFile
	 * @return
	 */
	private RecordFile createRecordFile(String filePath, File rawFile, RecordData recordData) {
		recordData.uint32SignalType = getSignalType();
		RecordFile rf = RecordFile.createFile(new File(filePath), recordData);
		if(rf == null){
			return null;
		}
		rf.completeRecordFile(rawFile);
		return rf;
	}

	/**
	 * 获取当前录音类型
	 * @return
	 */
	private int getSignalType() {
		int mSignalType = 0;
		switch (mRecordType) {
		case ITXZSourceRecorder.READER_TYPE_MIC:
			mSignalType = UiRecord.SIGNAL_TYPE_RAW;
			break;
		case ITXZSourceRecorder.READER_TYPE_REFER:
			mSignalType = UiRecord.SIGNAL_TYPE_REFER;
			break;
		case ITXZSourceRecorder.READER_TYPE_AEC:
			mSignalType = UiRecord.SIGNAL_TYPE_AEC;
			break;
		case ITXZSourceRecorder.READER_TYPE_INNER:
			mSignalType = UiRecord.SIGNAL_TYPE_INNER;
			break;

		default:
			break;
		}
		return mSignalType;
	}

	public boolean endSaveCache(OutputStream out) {
		try {
			if (mReadBuffer != null) {
				mReadBuffer.read(new OutputStreamDataWriter(out), null);
				return true;
			}
		} catch (IOException e) {
		} finally {
			mReadBuffer = null;
		}
		return false;
	}

	@Override
	public int read(byte[] data, int offset, int len) {
		try {
			while (this.mIsRecording) {
				mSocket.register(mSelector, SelectionKey.OP_READ);
				int n = mSelector.select();
				if (n < 0) {
					return n;
				}
				if (n == 0) {
					continue;
				}
				Iterator<SelectionKey> keyIter = mSelector.selectedKeys()
						.iterator();
				while (keyIter.hasNext()) {
					SelectionKey key = keyIter.next();
					keyIter.remove();
					if (!key.isValid()) {
						continue;
					}
					if (key.isReadable()) {
						int r = this.mSocket.read(ByteBuffer.wrap(data, offset,
								len));
						if (mReadBuffer != null && r > 0) {
							mReadBuffer.write(data, offset, r);
						}
						return r;
					}
				}
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public int getState() {
		return AudioRecord.STATE_INITIALIZED;
	}

	@Override
	public void rebuild() {

	}
	
	//如果start发生异常，调用该接口重置TCP连接
	private synchronized void reConnectCenter(){
		LogUtil.logd("TXZAudioRecorder reConnectCenter");
		release();
		//此处，阻塞200毫秒, 等待TCP连接释放结束
		try {
			Thread.sleep(200);
		} catch (Exception e2) {
		}
		connectCenter();
	}
}
