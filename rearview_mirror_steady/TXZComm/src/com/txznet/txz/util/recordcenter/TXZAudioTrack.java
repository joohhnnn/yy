package com.txznet.txz.util.recordcenter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;

import com.txznet.audio.codec.TXZAudioResampler.Resampler;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.LittleEndianBytesUtil;
import com.txznet.txz.util.TXZHandler;

public class TXZAudioTrack extends AudioTrack {
	public static final int SAMPLE_RATE = 16000;
	public static final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
	public static final int ENCODING_PCM = AudioFormat.ENCODING_PCM_16BIT;
	public int NOTIFY_PERIOD = SAMPLE_RATE / 50; 
	public static final int DATA_UNIT_PERIOD = 20; // 数据单元周期
	public int DATA_UNIT = SAMPLE_RATE * 2 * DATA_UNIT_PERIOD / 1000; // 每次通知周期发送的碎片大小
	public static final int STREAM_BUFFR_ALIGN = 1024;
	public int currentRate = SAMPLE_RATE;
	boolean needResample = false;
	Resampler resampler = null;
	private int mMode;
	byte[] bufRemain = null;
	byte[] bufResampleRemain = null;
	
	public static boolean bEnableWriteBuffer = false;//对是否分发数据进行保护

	//AudioManager.STREAM_ACCESSIBILITY = 10
	public static final int STREAM_ACCESSIBILITY = 10;
	
	// ///////////////////////////////////////////////////////////////////////////////

	public TXZAudioTrack(int streamType, int bufferSizeInBytes, int mode)
			throws IllegalArgumentException {
		this(streamType,SAMPLE_RATE,AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT,
				ENCODING_PCM),mode,0);
		
	}
	
	public TXZAudioTrack(int streamType, int sampleRateInHz,int bufferSizeInBytes, int mode,int sessionId)
			throws IllegalArgumentException {
		super(streamType, sampleRateInHz, CHANNEL_OUT, ENCODING_PCM,
				bufferSizeInBytes, mode,sessionId);
		LogUtil.logd("TXZAudioTrack Constructor streamType = " + streamType
				+ " ,sampleRateInHz = " + sampleRateInHz
				+ " ,bufferSizeInBytes = " + bufferSizeInBytes 
				+ " ,mode = " + mode);
		if(bEnableWriteBuffer && sampleRateInHz != SAMPLE_RATE){
			resampler = new Resampler(sampleRateInHz, SAMPLE_RATE);
			NOTIFY_PERIOD = sampleRateInHz / 50;
			DATA_UNIT = sampleRateInHz*2*DATA_UNIT_PERIOD/1000;
			currentRate = sampleRateInHz;
			needResample = true;
		}
		bufRemain = new byte[DATA_UNIT];
		bufResampleRemain = new byte[DATA_UNIT];
		
		mMode = mode;
		if(bEnableWriteBuffer){
			createDispatcherThread();
		}

	}

	public TXZAudioTrack(int streamType, int mode)
			throws IllegalArgumentException {
		this(streamType, AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT,
				ENCODING_PCM), mode);
	}

	public TXZAudioTrack(int streamType, int sampleRateInHz, int channelConfig,
			int audioFormat, int bufferSizeInBytes, int mode,int sessionId)
			throws IllegalArgumentException {
		this(streamType, sampleRateInHz,bufferSizeInBytes, mode,sessionId);
//		if (sampleRateInHz != SAMPLE_RATE) {
//			throw new RuntimeException("not support sampleRateInHz: "
//					+ sampleRateInHz);
//		}
		
		if (channelConfig != CHANNEL_OUT) {
			throw new RuntimeException("not support channelConfig: "
					+ channelConfig);
		}
		if (audioFormat != ENCODING_PCM) {
			throw new RuntimeException("not support audioFormat: "
					+ audioFormat);
		}
	}
	public TXZAudioTrack(int streamType, int sampleRateInHz, int channelConfig,
			int audioFormat, int bufferSizeInBytes, int mode)
			throws IllegalArgumentException {
		this(streamType, sampleRateInHz,channelConfig,audioFormat,bufferSizeInBytes, mode,0);
		
	}

	public TXZAudioTrack(AudioAttributes attributes, AudioFormat format,int streamType, int sampleRateInHz, int bufferSizeInBytes,
			int mode, int sessionId)
			throws IllegalArgumentException {
		super(attributes, format, bufferSizeInBytes, mode,sessionId);
		LogUtil.logd("TXZAudioTrack Constructor streamType = " + streamType
				+ " ,sampleRateInHz = " + sampleRateInHz
				+ " ,bufferSizeInBytes = " + bufferSizeInBytes
				+ " ,mode = " + mode);
		if(bEnableWriteBuffer && sampleRateInHz != SAMPLE_RATE){
			resampler = new Resampler(sampleRateInHz, SAMPLE_RATE);
			NOTIFY_PERIOD = sampleRateInHz / 50;
			DATA_UNIT = sampleRateInHz*2*DATA_UNIT_PERIOD/1000;
			currentRate = sampleRateInHz;
			needResample = true;
		}
		bufRemain = new byte[DATA_UNIT];
		bufResampleRemain = new byte[DATA_UNIT];

		mMode = mode;
		if(bEnableWriteBuffer){
			createDispatcherThread();
		}

	}

	public static TXZAudioTrack createAudioTrack(final int streamType, final int sampleRateInHz, final int channelConfig, final int audioFormat,
			final int bufferSizeInBytes, final int mode) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && streamType == STREAM_ACCESSIBILITY) {
			if (channelConfig != CHANNEL_OUT) {
				throw new RuntimeException("not support channelConfig: "
						+ channelConfig);
			}
			if (audioFormat != ENCODING_PCM) {
				throw new RuntimeException("not support audioFormat: "
						+ audioFormat);
			}
			return createAudioTrack(streamType, sampleRateInHz, bufferSizeInBytes, mode,0);
		} else {
			return new TXZAudioTrack(streamType,sampleRateInHz,channelConfig,audioFormat,bufferSizeInBytes,mode);
		}
	}

	public static TXZAudioTrack createAudioTrack (int streamType, int sampleRateInHz,int bufferSizeInBytes, int mode,int sessionId){
		TXZAudioTrack audioTrack = new TXZAudioTrack(
				createAudioAttributes(streamType),
				(new AudioFormat.Builder())
						.setChannelMask(CHANNEL_OUT)
						.setEncoding(ENCODING_PCM)
						.setSampleRate(sampleRateInHz)
						.build(),
				streamType,
				sampleRateInHz,
				bufferSizeInBytes, mode, sessionId);
		return audioTrack;
	}

	private static AudioAttributes createAudioAttributes (int streamType) {
		AudioAttributes audioAttributes = null;
		if (streamType == STREAM_ACCESSIBILITY) {
			audioAttributes = new AudioAttributes.Builder()
					.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
					.setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
					.build();
		} else {
			audioAttributes = new AudioAttributes.Builder()
					.setLegacyStreamType(streamType)
					.build();
		}

		return audioAttributes;
	}

	// ///////////////////////////////////////////////////////////////////////////////

	public static class AudioTrackBuffer {
		public long time;
		public byte[] data;
		public int total; // 总数据量
		public int offset; // 当前读取偏移量
		
		public AudioTrackBuffer(byte[] audioData, int offsetInBytes,
				int sizeInBytes, boolean align) {
			if (align) {
				data = new byte[(sizeInBytes + STREAM_BUFFR_ALIGN - 1)
						/ STREAM_BUFFR_ALIGN * STREAM_BUFFR_ALIGN];
			} else {
				data = new byte[sizeInBytes];
			}
			setBuffer(audioData, offsetInBytes, sizeInBytes);
		}

		public void setBuffer(byte[] audioData, int offsetInBytes,
				int sizeInBytes) {
			System.arraycopy(audioData, offsetInBytes, data, 0, sizeInBytes);
			total = sizeInBytes;
			offset = 0;
			time = SystemClock.elapsedRealtime();
		}
	}

	private List<AudioTrackBuffer> mBuffer = new ArrayList<AudioTrackBuffer>();
	private int mBufferIndex = 0; // 静态模式时当前播放到的索引
	private int mBufferCount = 0;
	private int mBufferRemain = 0; // 剩余的数据量

	private void insertBuffer(byte[] audioData, int offsetInBytes,
			int sizeInBytes) {
		synchronized (mBuffer) {
			mBufferRemain += sizeInBytes;
			for (int i = mBufferCount; i < mBuffer.size(); ++i) {
				AudioTrackBuffer buf = mBuffer.get(i);
				if (buf.data.length >= sizeInBytes) {
					mBuffer.remove(i);
					mBuffer.add(mBufferCount++, buf);
					buf.setBuffer(audioData, offsetInBytes, sizeInBytes);
					return;
				}
			}
			mBuffer.add(mBufferCount++,
					new AudioTrackBuffer(audioData, offsetInBytes, sizeInBytes,
							mMode != AudioTrack.MODE_STATIC));
		}
		LogUtil.logd("insertBuffer mBufferRemain = "+mBufferRemain);
	}

	private HandlerThread mDispatchThread = null;
	private Handler mDispatchHandler = null;

	private void createDispatcherThread() {
		if(mDispatchThread != null){
			mDispatchThread.quit();
		}
		mDispatchThread = new HandlerThread("DispathAudio", Thread.MAX_PRIORITY);
		mDispatchThread.start();

		mDispatchHandler = new Handler(mDispatchThread.getLooper());
		mDispatchHandler.post(new Runnable() {
			@Override
			public void run() {
				TXZHandler.updateMaxPriority();
			}
		});

		LogUtil.logd("create thread for dispather audio: "
				+ mDispatchThread.getId());
	}

	private Socket mSocket = null;
	private static byte[] mAudioDataCmd = new byte[] { RecorderCenter.CMD_AUDIO_DATA };
	private static byte[] mAudioDataLen = new byte[8];
	
	private static long mAudioDataTime = 0;

	private void releaseCenter() {
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
			}
			mSocket = null;
		}
	}

	private void connectCenter() {
		if (mSocket != null) {
			return;
		}
		try {
			mSocket = new Socket();
			mSocket.connect(new InetSocketAddress("127.0.0.1",
					RecorderCenter.TXZ_RECORDER_PORT));
			LogUtil.logd("track client create: "
					+ this.mSocket.getLocalSocketAddress().toString());
		} catch (IOException e) {
			e.printStackTrace();
			releaseCenter();
		}
	}

	private void DispatchData() {
		if (mSocket == null) {
			return;
		}
		try {
			mSocket.getOutputStream().write(mAudioDataCmd);
			long time = SystemClock.elapsedRealtime();
			if (mAudioDataTime == 0) {
				mAudioDataTime = time;
			} else if (mAudioDataTime <= time) {
				mAudioDataTime = time;
			}
			mSocket.getOutputStream().write(
					LittleEndianBytesUtil.longToBytes(mAudioDataTime, mAudioDataLen));
			int sendCount = 0;
			
			if (mMode == AudioTrack.MODE_STATIC) {
				synchronized (mBuffer) {
					int t = DATA_UNIT;
					if (t > mBufferRemain) {
						t = mBufferRemain;
					}
					sendCount = t;
					mBufferRemain -= t;
					if (!needResample) {// 不需要重采样
						mSocket.getOutputStream().write(
								LittleEndianBytesUtil.intToBytes(t,
										mAudioDataLen), 0, 4);

						while (t > 0) {
							AudioTrackBuffer buf = mBuffer.get(mBufferIndex);
							int len = buf.total - buf.offset;
							if (len > t) {
								len = t;
							}
							mSocket.getOutputStream().write(buf.data,
									buf.offset, len);
							buf.offset += len;
							t -= len;
							if (buf.offset >= buf.total) {
								mBufferIndex++;
								if (mBufferIndex >= mBufferCount) {
									break;
								}
								mBuffer.get(mBufferIndex).offset = 0;
							}
						}

					} else {// 需要重采样
						int offsetResample = 0;
						AudioTrackBuffer buf = null;
						while (t > 0) {
							buf = mBuffer.get(mBufferIndex);
							int len = buf.total - buf.offset;
							if (len > t) {
								len = t;
							}
							int readLen = resampler.resample(buf.data,
									buf.offset, len, bufResampleRemain,
									offsetResample);
							buf.offset += len;
							offsetResample += readLen;
							t -= len;
							if (buf.offset >= buf.total) {
								mBufferIndex++;
								if (mBufferIndex >= mBufferCount) {
									break;
								}
								mBuffer.get(mBufferIndex).offset = 0;
								offsetResample = 0;
							}
						}
						mSocket.getOutputStream().write(
								LittleEndianBytesUtil.intToBytes(
										offsetResample, mAudioDataLen), 0, 4);
						mSocket.getOutputStream().write(bufResampleRemain, 0,
								offsetResample);

					}
					
				}
			} else {//流类型的
				synchronized (mBuffer) {
					int t = DATA_UNIT;
					if (t > mBufferRemain) {//剩余的数据量小于一次发送所需的数据量，少的数据应该用静音来填补
						t = (mBufferRemain / (currentRate*2*DATA_UNIT_PERIOD/1000))*(currentRate*2*DATA_UNIT_PERIOD/1000);
					}
					sendCount = t;
					
					mBufferRemain -= t;
					if (!needResample) {// 不需要重采样
						mSocket.getOutputStream().write(
								LittleEndianBytesUtil.intToBytes(t,
										mAudioDataLen), 0, 4);
						while (t > 0) {
							AudioTrackBuffer buf = mBuffer.get(0);
							int len = buf.total - buf.offset;
							if (len > t) {
								len = t;
							} else {
								// 缓冲区往后挪用，避免多次内存分配
								mBuffer.remove(0);
								mBuffer.add(buf);
								mBufferCount--;
							}
							mSocket.getOutputStream().write(buf.data,
									buf.offset, len);
							buf.offset += len;
							t -= len;
						}
					} else {// 需要重采样
						int offsetResample = 0;
						AudioTrackBuffer buf = null;
						int bufRemainLen = 0; 
						while (t > 0) {

							buf = mBuffer.get(0);
							int len = buf.total - buf.offset;
							if (len > t) {
								len = t;
								
							} else {
								// 缓冲区往后挪用，避免多次内存分配
								mBuffer.remove(0);
								mBuffer.add(buf);
								mBufferCount--;

							}
							System.arraycopy(buf.data, buf.offset, bufRemain,bufRemainLen, len);
							bufRemainLen += len;
							t -= len;
							if(t==0){
								int readLen = resampler.resample(bufRemain, 0,
									bufRemainLen, bufResampleRemain, offsetResample);
								offsetResample += readLen;
								bufRemainLen = 0;
							}
							buf.offset += len;
						}

						mSocket.getOutputStream().write(
								LittleEndianBytesUtil.intToBytes(offsetResample,
										mAudioDataLen), 0, 4);
						if (buf == null) {
							return;
						}
						mSocket.getOutputStream().write(bufResampleRemain, 0,
								offsetResample);
					}
				}
			}

			mAudioDataTime += (sendCount / (SAMPLE_RATE * 2 / 1000));
		} catch (Exception e) {
			releaseCenter();
		}
	}

	private class RunnableDispathData implements Runnable {
		@Override
		public void run() {
			if(SystemClock.elapsedRealtime() > 60*1000 && SystemClock.elapsedRealtime() - initTime > 10*1000){
				connectCenter();
				DispatchData();
			}
		}
	};
	
	public static long initTime = 0;
	
	private OnPlaybackPositionUpdateListener mOnPlaybackPositionUpdateListenerSet;
	private OnPlaybackPositionUpdateListener mOnPlaybackPositionUpdateListener = new OnPlaybackPositionUpdateListener() {
		@Override
		public void onPeriodicNotification(AudioTrack track) {
			if (bEnableWriteBuffer) {
				// 分发线程开始分发数据
				if(mDispatchThread == null || mDispatchHandler == null){
					createDispatcherThread();
				}
				mDispatchHandler.post(new RunnableDispathData());
			}
			// 回调用户监听器
			OnPlaybackPositionUpdateListener l = mOnPlaybackPositionUpdateListenerSet;
			if (l != null) {
				l.onPeriodicNotification(TXZAudioTrack.this);
			}
		}

		@Override
		public void onMarkerReached(AudioTrack track) {
			OnPlaybackPositionUpdateListener l = mOnPlaybackPositionUpdateListenerSet;
			if (l != null) {
				l.onPeriodicNotification(TXZAudioTrack.this);
			}
		}
	};

	@Override
	public void setPlaybackPositionUpdateListener(
			OnPlaybackPositionUpdateListener listener) {
		mOnPlaybackPositionUpdateListenerSet = listener;
		super.setPlaybackPositionUpdateListener(mOnPlaybackPositionUpdateListener,mDispatchHandler);
	}

	@Override
	public int setPositionNotificationPeriod(int periodInFrames) {
		if (periodInFrames % NOTIFY_PERIOD != 0) {
			throw new RuntimeException("not support periodInFrames: "
					+ periodInFrames + ", must times of " + NOTIFY_PERIOD);
		}
		return super.setPositionNotificationPeriod(NOTIFY_PERIOD);
	}

	@Override
	public int write(short[] audioData, int offsetInShorts, int sizeInShorts) {
		throw new RuntimeException("not support short[] audio data");
	}
	
	@Override
	public int setLoopPoints(int startInFrames, int endInFrames, int loopCount) {
		throw new RuntimeException("not support setLoopPoints ");
	}

	@Override
	public void stop() throws IllegalStateException {
		super.stop();
	}

	@Override
	public void flush() {
		synchronized (mBuffer) {
			mBufferRemain = mBufferCount = mBufferIndex = 0;
		}
		super.flush();
	}

	@Override
	public int reloadStaticData() {
		if (mMode == AudioTrack.MODE_STATIC) {
			synchronized (mBuffer) {
				mBufferIndex = 0;
				mBufferRemain = 0;
				for (int i = 0; i < mBuffer.size(); ++i) {
					mBuffer.get(0).offset = 0;
					mBufferRemain += mBuffer.get(0).total;
				}
			}
		}
		return super.reloadStaticData();
	}

	@Override
	public void play() throws IllegalStateException {
		super.setPositionNotificationPeriod(NOTIFY_PERIOD); // 20ms回调一次
		super.setPlaybackPositionUpdateListener(mOnPlaybackPositionUpdateListener,mDispatchHandler);
		super.play();
	}

	@Override
	public int write(byte[] audioData, int offsetInBytes, int sizeInBytes) {
		int r =  super.write(audioData, offsetInBytes, sizeInBytes);
		if (r > 0 && bEnableWriteBuffer){
			insertBuffer(audioData, offsetInBytes, r);
//			mDispatchHandler.removeCallbacks(runnableDispathData);
//			mDispatchHandler.postDelayed(runnableDispathData, 0);
		}
		return r;
	}

//	final RunnableDispathData runnableDispathData = new RunnableDispathData(){
//
//		@Override
//		public void run() {
//			super.run();
//			if(getPlayState() == PLAYSTATE_PLAYING){
//				mDispatchHandler.postDelayed(runnableDispathData, 50);
//			}
//		}
//	};
	
	@Override
	public void release() {
		synchronized (mBuffer) {
			mBuffer.clear();
			mBufferCount = mBufferIndex = 0;
		}
		if(resampler != null){
			resampler.release();
			resampler = null;
		}
		if(mDispatchHandler != null){
			mDispatchHandler.removeCallbacksAndMessages(null);
			mDispatchHandler.postDelayed(runnableReleaseThread,0);
		}
		super.release();
	}
	
	Runnable runnableReleaseThread = new Runnable() {
		
		@Override
		public void run() {
			sendQuiteData(1000);
			releaseCenter();
			if(mDispatchThread != null){
				mDispatchThread.quit();
			}
		}
	};
	
	/**
	 * 在释放前发送静音数据，用于静音检测
	 */
	protected void sendQuiteData(long times) {
		LogUtil.logd("sendQuiteData length = "+times*SAMPLE_RATE*2/1000);
		connectCenter();
		if(mSocket == null){
			return;
		}
		try {
			int length = (int) (times*SAMPLE_RATE*2/1000);
			mSocket.getOutputStream().write(mAudioDataCmd);
			long time = SystemClock.elapsedRealtime();
			if (mAudioDataTime == 0) {
				mAudioDataTime = time;
			} else if (mAudioDataTime <= time) {
				mAudioDataTime = time;
			}
			mSocket.getOutputStream().write(
					LittleEndianBytesUtil.longToBytes(mAudioDataTime, mAudioDataLen));
			
			mSocket.getOutputStream().write(
					LittleEndianBytesUtil.intToBytes(length,
							mAudioDataLen), 0, 4);	//20ms静音数据
			
			mSocket.getOutputStream().write(RecorderCenter.getQuiteVoice(length), 0, length);
			mAudioDataTime += times;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 使写数据时同步向INNER缓冲区写数据
	 * @param enable
	 */
	public static void enableWriteBuffer(boolean enable){
		bEnableWriteBuffer = enable;
	}
	
}
