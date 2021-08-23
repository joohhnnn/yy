package com.txznet.txz.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import com.txznet.txz.util.TXZHandler;
import android.os.SystemClock;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.util.recordcenter.TXZAudioTrack;

public class BeepPlayer {
	private static class BeepAudioTrack extends AudioTrack {
		public BeepAudioTrack(int streamType, int sampleRateInHz,
				int channelConfig, int audioFormat, int bufferSizeInBytes,
				int mode) throws IllegalArgumentException {
			super(streamType, sampleRateInHz, channelConfig, audioFormat,
					bufferSizeInBytes, mode);
		}

		public BeepAudioTrack(int streamType, int sampleRateInHz,
				int channelConfig, int audioFormat, int bufferSizeInBytes,
				int mode, int sessionId) throws IllegalArgumentException {
			super(streamType, sampleRateInHz, channelConfig, audioFormat,
					bufferSizeInBytes, mode, sessionId);
		}

		public BeepAudioTrack(AudioAttributes attributes, AudioFormat format,int streamType, int sampleRateInHz, int bufferSizeInBytes,
				int mode, int sessionId) throws IllegalArgumentException {
			super(attributes, format, bufferSizeInBytes, mode, sessionId);
		}

		public static BeepAudioTrack createAudioTrack(int streamType, int sampleRateInHz,
				int channelConfig, int audioFormat, int bufferSizeInBytes,
				int mode) {
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && streamType == TXZAudioTrack.STREAM_ACCESSIBILITY) {
				return createAudioTrack(streamType, sampleRateInHz, channelConfig,audioFormat,bufferSizeInBytes, mode,0);
			} else {
				return new BeepAudioTrack(streamType,sampleRateInHz,channelConfig,audioFormat,bufferSizeInBytes,mode);
			}
		}

		public static BeepAudioTrack createAudioTrack (int streamType, int sampleRateInHz,int channelConfig, int audioFormat,int bufferSizeInBytes, int mode,int sessionId){
			BeepAudioTrack audioTrack = new BeepAudioTrack(
					createAudioAttributes(streamType),
					(new AudioFormat.Builder())
							.setChannelMask(channelConfig)
							.setEncoding(audioFormat)
							.setSampleRate(sampleRateInHz)
							.build(),
					streamType,
					sampleRateInHz,
					bufferSizeInBytes, mode, sessionId);
			return audioTrack;
		}

		private static AudioAttributes createAudioAttributes (int streamType) {
			AudioAttributes audioAttributes = null;
			if (streamType == TXZAudioTrack.STREAM_ACCESSIBILITY) {
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

		public Runnable mEndRunnable = null;
		public boolean released = false;

		public synchronized void doEndRunnable() {
			try {
				if (this.getState() != STATE_UNINITIALIZED) {
					this.stop();
				}
			} catch (Exception e) {
			}
			Runnable r = mEndRunnable;
			mEndRunnable = null;
			if (r != null) {
				r.run();
			}
		}

		private Runnable mDestroyRunnable = new Runnable() {
			@Override
			public void run() {
				doEndRunnable();
				if (!released) {
					released = true;
					BeepAudioTrack.this.release();
				}
			}
		};

		public void destroyPlayer() {
			try {
				if (this.getState() != STATE_UNINITIALIZED) {
					this.stop();
				}
			} catch (Exception e) {
			}
			mBeepHandler.removeCallbacks(mDestroyRunnable);
			mBeepHandler.post(mDestroyRunnable);
		}
	}

	private static HandlerThread mBeepThread = null;
	private static Handler mBeepHandler = null;
	private static byte[] mBeepBuffer;
	private static int mPlaySeq = 0;
	private static BeepAudioTrack mBeepAudioTrack;
	private static int mLastStream = TtsUtil.DEFAULT_TTS_STREAM;
	
	public static synchronized void setBeepResources(String beepPath) {
		byte[] data = null;
		try {
			InputStream in = null;
			if (TextUtils.isEmpty(beepPath)) {
				in = GlobalContext.get().getAssets().open("beep.pcm");
			} else {
				in = new FileInputStream(beepPath);
			}
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			data = null;
			JNIHelper.loge("load beep resource error: " + e.toString());
			e.printStackTrace();
		}
		if (data != null) {
			mBeepBuffer = data;
			createBeepPlayer(mLastStream);
		}
	}

	private static void createPlayer() {
		try {
			InputStream in = GlobalContext.get().getAssets().open("beep.pcm");
			mBeepBuffer = new byte[in.available()];
			in.read(mBeepBuffer);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mBeepThread = new HandlerThread("PlayBeep", Thread.MAX_PRIORITY);
		mBeepThread.start();

		mBeepHandler = new Handler(mBeepThread.getLooper());
		mBeepHandler.post(new Runnable() {
			@Override
			public void run() {
				TXZHandler.updateMaxPriority();
			}
		});
	}

	static {
		createPlayer();
		// createBeepPlayer(TtsUtil.DEFAULT_TTS_STREAM);
	}

	public static synchronized void play(final Runnable runComplete) {
		play(TtsUtil.DEFAULT_TTS_STREAM, runComplete);
	}
	
	private static void createBeepPlayer(int stream) {
		mBeepAudioTrack = BeepAudioTrack.createAudioTrack(mLastStream = stream, 16000, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, mBeepBuffer.length, AudioTrack.MODE_STATIC);
		mBeepAudioTrack.write(mBeepBuffer, 0, mBeepBuffer.length);
	}
	
	private static void resetBeepPlayer(int stream) {
		if (mBeepAudioTrack != null) {
			if (mLastStream == stream) {
				try {
					if (mBeepAudioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
						mBeepAudioTrack.stop();
					}
				} catch (Exception e) {
				}
				mBeepAudioTrack.reloadStaticData();
				return;
			}
			mBeepAudioTrack.destroyPlayer();
		}
		createBeepPlayer(stream);
	}

	public static synchronized void play(final int stream,
			final Runnable runComplete) {
		LogUtil.logd("beep play:"+SystemClock.elapsedRealtime());
		mBeepHandler.removeCallbacksAndMessages(null);
		resetBeepPlayer(stream);
		mPlaySeq++;
		final int idx = mPlaySeq;
		// LogUtil.logd("begin beep: " + idx);
		mBeepHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					VolumeManager.getInstance().muteAll(false);
					mBeepAudioTrack.mEndRunnable = runComplete;
					mBeepHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							mBeepAudioTrack.doEndRunnable();
						}
					}, AsrManager.getInstance().mBeepTimeout == 0 ? 1000
							: AsrManager.getInstance().mBeepTimeout); // 最大beep超时保护
					// if (mAudioTrack.getState() ==
					// AudioTrack.STATE_INITIALIZED)
					{
//						if (idx == mPlaySeq) {
//							// LogUtil.logd("write beep: " + idx);
//							audioTrack
//									.write(mBeepBuffer, 0, mBeepBuffer.length);
//						}
						if (idx == mPlaySeq) {
							// LogUtil.logd("play beep: " + idx);
							mBeepAudioTrack
									.setPlaybackPositionUpdateListener(new OnPlaybackPositionUpdateListener() {
										private int count = 0;

										@Override
										public void onPeriodicNotification(
												AudioTrack track) {
											BeepAudioTrack audioTrack = (BeepAudioTrack) track;
											++count;
											// LogUtil.logd("pbh beep test count: "
											// + count);
											if (count == 17
													&& AsrManager.getInstance().mBeepTimeout == 0) {
												audioTrack.doEndRunnable();
											}
//											if (count >= 20) {
//												try {
//													mBeepAudioTrack.destroyPlayer();
//												} catch (Exception e) {
//												}
//											}
										}

										@Override
										public void onMarkerReached(
												AudioTrack track) {
											// TODO Auto-generated method stub

										}
									});
						}
						if (idx == mPlaySeq) {
							mBeepAudioTrack
									.setPositionNotificationPeriod(16000 / 100);
						}
						// LogUtil.logd("pbh beep test begin");
						if (idx == mPlaySeq) {
							LogUtil.logd("beep start play");
							mBeepAudioTrack.play();
						}

						if (idx != mPlaySeq) {
							try {
								if (mBeepAudioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
									mBeepAudioTrack.stop();
								}
							} catch (Exception e) {
							}
						}
					}
				} catch (Exception e) {
				}
			}
		});
	}

	public static synchronized void cancel() {
		// LogUtil.logd("cancel beep");
		mPlaySeq++;
		cancelMusic();
	}

	private static BeepAudioTrack mAudioTrackMusic;
	private static int mPlayMusicSeq = 0;

	public static synchronized void playLoopMusic(String assetsName,
			Runnable runComplete, int beginRate) {
		playMusic(TtsUtil.DEFAULT_TTS_STREAM, -1, assetsName, runComplete,
				beginRate, beginRate, 0);
	}

	public static synchronized void playLoopMusic(String assetsName,
			Runnable runComplete, int beginRate, int endRate, int rateTime) {
		playMusic(TtsUtil.DEFAULT_TTS_STREAM, -1, assetsName, runComplete,
				beginRate, endRate, rateTime);
	}

	public static synchronized void playMusic(final int stream,
			final int loopCount, final String assetsName,
			final Runnable runComplete, final int beginRate, final int endRate,
			final int rateTime) {
		mBeepHandler.removeCallbacksAndMessages(null);
		cancelMusic();
		mPlayMusicSeq++;
		final int idx = mPlayMusicSeq;
		mBeepHandler.post(new Runnable() {
			@Override
			public void run() {
				if (TtsManager.getInstance().isBusy())
					return;
				try {
					byte[] data = null;
					try {
						InputStream in = GlobalContext.get().getAssets()
								.open(assetsName);
						data = new byte[in.available()];
						in.read(data);
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					BeepAudioTrack audioTrack = mAudioTrackMusic = BeepAudioTrack.createAudioTrack(
							stream, beginRate, AudioFormat.CHANNEL_OUT_MONO,
							AudioFormat.ENCODING_PCM_16BIT, AudioTrack
									.getMinBufferSize(44100,
											AudioFormat.CHANNEL_OUT_MONO,
											AudioFormat.ENCODING_PCM_16BIT),
							AudioTrack.MODE_STREAM);
					if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
						if (idx == mPlayMusicSeq) {
							audioTrack.play();
						}
						final Runnable runEnd = new Runnable() {
							@Override
							public void run() {
								mBeepHandler.removeCallbacks(this);
								if (runComplete != null && idx == mPlayMusicSeq) {
									runComplete.run();
								}
							}
						};
						int count = loopCount;
						long t = SystemClock.elapsedRealtime();
						do {
							if (idx == mPlayMusicSeq) {
								if (rateTime > 0 && beginRate != endRate) {
									audioTrack.setPlaybackRate(beginRate
											+ (int) ((endRate - beginRate)
													* (SystemClock
															.elapsedRealtime() - t) / rateTime));
								}
								audioTrack.write(data, 0, data.length);
							} else {
								break;
							}
						} while (loopCount < 0 || count-- > 0);
						if (idx != mPlayMusicSeq) {
							audioTrack.destroyPlayer();
						}
					}
				} catch (Exception e) {
				}
			}
		});
	}

	public static void playWaitMusic() {
//		WakeupManager.getInstance().checkUsingAsr(null, new Runnable() {
//			@Override
//			public void run() {
//				if (RecorderWin.sPauseRecordWin == false && RecorderWin.isOpened()) {
//					playLoopMusic("wait_tone.pcm", null, 16000, 10000, 20 * 1000);
//				}
//			}
//		});
//		
	}

	public static synchronized void cancelMusic() {
		// LogUtil.logd("cancel beep");
		mPlayMusicSeq++;
		if (mAudioTrackMusic != null) {
			try {
				mAudioTrackMusic.destroyPlayer();
			} catch (Exception e) {
			}
		}
	}

}
