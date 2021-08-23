package com.txznet.audio.player;

import java.io.File;
import java.io.FileOutputStream;

import android.media.AudioTrack;
import android.os.Environment;

import com.txznet.audio.codec.TXZAudioDirectDecoder;
import com.txznet.audio.player.factory.CreateParamFactory;
import com.txznet.comm.remote.util.LogUtil;

/**
 * 直播解码
 * 
 * @author ASUS User
 *
 */
public class AudioDirectCodecTrack {
	public final static int DECODE_PIECE_SIZE = 1 * 1024;
	public final static int READ_RUNNALE_QUEUE_SIZE = 2;
	public final static String TAG = "[DirectPlayer] ";

	private long sessionID;

	AudioTrack mAudioTrack;

	private State state;

	private boolean mIsRelease;// 是否释放标志位

	float leftVolume = 1.0f, rightVolume = 1.0f;// 左右声道
	private String path;

	private OnStateListener listener;

	// CreateRunnable mCreateRunnable;

	/**
	 * 创建解码器线程
	 */

	public class DecodeRunnable implements Runnable {
		String path;

		DecodeRunnable(String path) {
			this.path = path;
		}

		@Override
		public void run() {
			LogUtil.logi(TAG + "DecodeRunnable is running ");
			// 创建解码线程，
			TXZAudioDirectDecoder.beginDecode(sessionID, path);
			// 执行真正的释放逻辑
			destoryDecoder();
			LogUtil.logi(TAG + "DecodeRunnable is end ");
		}
	}

	public static int MAX_BUFFER_SIZE = 8192;

	/**
	 * 往AudioTrack中写线程
	 * 
	 * @author lenovo
	 *
	 */
	public class WriteRunnable implements Runnable {

		@Override
		public void run() {
			LogUtil.logi(TAG + "WriteRunnable is running ");
			int[] params = new int[] { 0, 0, 0 };
			byte[] data = null;// 100*1024
			boolean first = true;
			int offset = 0;
			int channelConfig = params[0];
			int sampleRateInHz = params[1];
			int audioFormat = params[2];

			// 缓冲状态
			if (null != listener) {
				listener.onState(State.buffered);
			}
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(new File(
						Environment.getExternalStorageDirectory(),
						"/testJava.pcm"));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			while (true) {
				if (mIsRelease) {
					break;
				}
				// 状态通知,
				int readSize = -1;
				if ((readSize = TXZAudioDirectDecoder.readFrame(sessionID,
						params, data, offset)) < 0) {
					LogUtil.loge("[DirectPlayer] readFrame error,readSize:"
							+ readSize);
					// 抛错误码
					if (null != listener) {
						listener.onError(MediaError.ERR_READ_FRAME,
								"read frame error");
					}
					return;
				}
				
				// 异常,抛错误码
				if (sampleRateInHz != params[1] || channelConfig != params[0]
						|| audioFormat != params[2]) {
					int sample_rate = params[1];// 采样率
					int audio_format = params[2];// 分辨率,量化字节数（字节）
					int channels = params[0];// 通道数
					
					int max = sample_rate * 10 * audio_format 
							* channels;
					if (max < MAX_BUFFER_SIZE) {
						max = MAX_BUFFER_SIZE;
					}
					data = new byte[max + audio_format  * channels];
					
					
					channelConfig = channels;
					sampleRateInHz = sample_rate;
					audioFormat = audio_format;
					if (null != mAudioTrack) {
						mAudioTrack.release();
						mAudioTrack = null;
					}

					LogUtil.logd(TAG + "channel:"
							+ CreateParamFactory.getChannel(channels)
							+ ",sampleRate:" + sample_rate + ",audioFormat:"
							+ CreateParamFactory.getAudioFormat(audio_format));

					try {
						mAudioTrack = new AudioTrack(
								mStreamType,
								sample_rate,
								CreateParamFactory.getChannel(channels),
								CreateParamFactory.getAudioFormat(audio_format),
								AudioTrack
										.getMinBufferSize(
												sample_rate,
												CreateParamFactory
														.getChannel(channels),
												CreateParamFactory
														.getAudioFormat(audio_format)),
								AudioTrack.MODE_STREAM);
						
						
						offset = 0;
						first = true;
						while (mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
						}
						mAudioTrack.play();

						setStereoVolume(leftVolume, rightVolume);
					} catch (Exception e) {
						if (null != listener) {
							listener.onError(MediaError.ERR_CREATE_TRACK,
									"create AudioTrack error");
						}
					}
				}
//				
//				if (readSize == 0) {
//					continue;
//				}


				if (first
						&& offset < sampleRateInHz * channelConfig
								* audioFormat  * 5) {
					offset += readSize;
					continue;
				}

				first = false;

				if (mAudioTrack != null) {
					// if (null != listener) {
					// listener.onState(State.played);
					// }
					// 长度为总共读取到的数据量+上次剩余的数据量-这次不应该读取到的数据量
					// TODO:字节对齐余4
					readSize += offset;
					int remainSize = readSize
							% (channelConfig * audioFormat );
					LogUtil.logd("mAudioTrack begin write,"
							+ (readSize - remainSize));
					mAudioTrack.write(data, 0, readSize - remainSize);

					try {
						fileOutputStream.write(data, 0, readSize - remainSize);
						fileOutputStream.flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
					for (int i = 0; i < remainSize; ++i) {
						data[i] = data[readSize - remainSize + i];
					}
					offset = remainSize;
				}
			}
			//
			// destoryDecoder();
			LogUtil.logi(TAG + " WriteRunnable is end  ");
		}

	}

	int mPassTime = 0;
	int mStreamType;

	protected AudioDirectCodecTrack(int streamType) {
		mStreamType = streamType;
	}

	public static AudioDirectCodecTrack createAudioTrack(int streamType,
			String url) {
		AudioDirectCodecTrack ret = new AudioDirectCodecTrack(streamType);
		ret.path = url;
		createResource(ret);
		return ret;
	}

	/**
	 * 创建资源
	 */
	private static void createResource(AudioDirectCodecTrack ret) {
		if (null == ret) {
			LogUtil.loge("please don't input null");
			return;
		}
		long sessionID = TXZAudioDirectDecoder.createDecoder();
		if (sessionID == 0) {
			LogUtil.logd("create directCodec error");
			return;
		}

		ret.sessionID = sessionID;
		// 让两个线程直接跑
		Thread thread = new Thread(ret.new DecodeRunnable(ret.path));
		thread.setName("ACTDec");
		thread.start();
		Thread writeThread = new Thread(ret.new WriteRunnable());
		writeThread.setName("ACTWri");
		writeThread.start();
	}

	/**
	 * 释放整个对象，
	 */
	public void release() {
		if (null != mAudioTrack) {
			mAudioTrack.pause();
		}
		releaseDecoder();
	}

	/**
	 * 释放解码资源
	 */
	private void releaseDecoder() {

		synchronized (AudioDirectCodecTrack.this) {
			mIsRelease = true;
			TXZAudioDirectDecoder.beginRelease(sessionID);
			if (null != mAudioTrack) {
				mAudioTrack.release();
				mAudioTrack = null;
			}
		}
	}

	public boolean isPlaying() {
		if (null != mAudioTrack) {
			return mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
		}
		return false;
	}

	public void setStereoVolume(float leftVolume, float rightVolume) {
		this.leftVolume = leftVolume;
		this.rightVolume = rightVolume;
		if (null != mAudioTrack) {
			mAudioTrack.setStereoVolume(leftVolume, rightVolume);
		}
	}

	/**
	 * 开始播放
	 */
	public void start() {
		// TODO:创建过不允许重复播放
		// 重新创建资源
		// createResource(this);
	}

	private synchronized void destoryDecoder() {
		if (sessionID != 0) {
			TXZAudioDirectDecoder.destory(sessionID);
			sessionID = 0;
		}
	}

	// 枚举
	enum State {
		inited, buffered, played, paused, exited;
	}

	// 接口
	public static interface OnStateListener {
		public void onError(int errcode, String errDesc);

		public void onState(State state);
	}

	/**
	 * 设置状态回调
	 * 
	 * @param listener
	 */
	public void setOnStateListener(OnStateListener listener) {
		this.listener = listener;
	}

}
