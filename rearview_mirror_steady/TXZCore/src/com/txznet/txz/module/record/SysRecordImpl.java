package com.txznet.txz.module.record;

import android.os.SystemClock;


public class SysRecordImpl/* implements IRecord*/{
//	static {
//		System.loadLibrary("mp3lame");
//	}
//	
//	boolean mWorking = false;
//	Thread mRecordThread;
//    
//	@Override
//	public boolean isBusy() {
//		return mWorking;
//	}
//    
//	@Override
//	public void start(RecordCallback callback) {
//		start(callback, new RecordOption());
//	}
//    
//	@Override
//	public void start(RecordCallback callback, RecordOption option) {
//		WakeupManager.getInstance().stop();
//		AsrManager.getInstance().cancel();
//		stop();
//
//		AppLogic.runOnUiGround(
//				new Runnable2<RecordCallback, RecordOption>(callback, option) {
//					@Override
//					public void run() {
//						MusicManager.getInstance().onBeginAsr();
//						mWorking = true;
//						mRecordThread = new Thread(
//								new Runnable2<RecordCallback, RecordOption>(
//										mP1, mP2) {
//									@Override
//									public void run() {
//										RecordCallback callback = mP1;
//										RecordOption option = mP2;
//										int voiceLength = 0;
//										AudioRecord rec;
//										long session = MP3Encoder.INVALID_SESSION_ID;
//										if (option.mEncodeMp3) {
//											session = MP3Encoder
//													.openSession(option.mSampleRate);
//										}
//										FileOutputStream pcmStream = null;
//										FileOutputStream mp3Stream = null;
//										if (TextUtils
//												.isEmpty(option.mSavePathPrefix) == false) {
//											try {
//												pcmStream = new FileOutputStream(
//														option.mSavePathPrefix
//																+ ".pcm");
//											} catch (FileNotFoundException e) {
//												e.printStackTrace();
//											}
//											try {
//												mp3Stream = new FileOutputStream(
//														option.mSavePathPrefix
//																+ ".mp3");
//											} catch (FileNotFoundException e) {
//												e.printStackTrace();
//											}
//										}
//										int bufferSize =5 * AudioRecord
//												.getMinBufferSize(
//														option.mSampleRate,
//														AudioFormat.CHANNEL_IN_MONO,
//														AudioFormat.ENCODING_PCM_16BIT);
//										rec = new AudioRecord(
//												option.mAudioSource,
//												option.mSampleRate,
//												AudioFormat.CHANNEL_IN_MONO,
//												AudioFormat.ENCODING_PCM_16BIT,
//												bufferSize);
//										short[] buffer = new short[bufferSize];
//										rec.startRecording();
//										callback.onBegin();
//										callback.onMute(0);
//										byte[] data;
//										try {
//											long startTime = SystemClock.elapsedRealtime();
//											long lastSoundTime = startTime;
//											while (mWorking) {
//												boolean isMute = true;
//												int bufferReadResult = rec
//														.read(buffer, 0,
//																bufferSize);
//												long curTime = SystemClock.elapsedRealtime();
//												// 静音检测
//												{
//													double avg = 0;
//													for (int i = 0; i < bufferReadResult; ++i) {
//														avg += buffer[i];
//													}
//													avg /= bufferReadResult;
//													double jitter = 0;
//													for (int i = 0; i < bufferReadResult; ++i) {
//														jitter += (buffer[i] - avg)
//																* (buffer[i] - avg);
//													}
//													jitter = Math.sqrt(jitter
//															/ bufferReadResult);
//													int vol = (int) (jitter / 100);
//													if (vol > 100)
//														vol = 100;
//													callback.onVolume(vol);
//													if (jitter > 2000)
//														isMute = false;
//												}
//												// // 判断是否跳过静音包
//												// if (option.mSkipMute == false
//												// || isMute == false)
//												{
//													// 增益
//													// {
//													// for (int i = 0; i <
//													// bufferReadResult; ++i) {
//													// long d = buffer[i];
//													// d *= option.mIncrease;
//													// if (d > 0x7FFF)
//													// d = 0x7FFF;
//													// else if (d < -0x8000)
//													// d = -0x8000;
//													// buffer[i] = (short) d;
//													// }
//													// }
//													if (pcmStream != null) {
//														for (int i = 0; i < bufferReadResult
//																&& i < buffer.length; i++) {
//															try {
//																pcmStream
//																		.write(buffer[i] & 0xff);
//																pcmStream
//																		.write((buffer[i] >> 8) & 0xff);
//															} catch (IOException e) {
//																e.printStackTrace();
//															}
//														}
//													} else {
//														callback.onPCMBuffer(
//																buffer,
//																bufferReadResult);
//													}
//													if (session != MP3Encoder.INVALID_SESSION_ID) {
//														data = MP3Encoder
//																.encodeSession(
//																		session,
//																		buffer,
//																		0,
//																		bufferReadResult);
//														if (mp3Stream != null) {
//															try {
//																mp3Stream
//																		.write(data);
//															} catch (IOException e) {
//																e.printStackTrace();
//															}
//														} else {
//															callback.onMP3Buffer(data);
//														}
//													}
//												}
//
//												voiceLength = (int) (curTime - startTime);
//												if (option.mMaxSpeech > 0
//														&& voiceLength >= option.mMaxSpeech) {
//													// 达到最大录音时长
//													callback.onSpeechTimeout();
//													break;
//												}
//												if (isMute == false) {
//													lastSoundTime = curTime;
//												} else {
//													int muteTime = (int) (curTime - lastSoundTime);
//													callback.onMute(muteTime);
//													if (option.mMaxMute > 0
//															&& muteTime >= option.mMaxMute) {
//														// 达到最大静音时长
//														callback.onMuteTimeout();
//														break;
//													}
//												}
//											}
//											if (session != MP3Encoder.INVALID_SESSION_ID) {
//												data = MP3Encoder
//														.closeSession(session);
//												if (mp3Stream != null) {
//													try {
//														mp3Stream.write(data);
//													} catch (IOException e) {
//														e.printStackTrace();
//													}
//												} else {
//													callback.onMP3Buffer(data);
//												}
//												session = MP3Encoder.INVALID_SESSION_ID;
//											}
//											if (mWorking == false) {
//												callback.onCancel();
//											}
//											if (TextUtils
//													.isEmpty(option.mSavePathPrefix) == false) {
//												Pcm2Wav.encode(
//														option.mSavePathPrefix
//																+ ".pcm",
//														option.mSavePathPrefix
//																+ ".wav", 16000);
//											}
//
//										} finally {
//											if (session != MP3Encoder.INVALID_SESSION_ID) {
//												MP3Encoder
//														.closeSession(session);
//											}
//											rec.stop();
//											rec.release();
//											WakeupManager.getInstance().stop();
//											mWorking = false;
//											MusicManager.getInstance().onEndAsr();
//											mRecordThread = null;
//											callback.onEnd(voiceLength);
//											AppLogic.runOnBackGround(new Runnable() {
//												@Override
//												public void run() {
//													WakeupManager.getInstance().start();
//												}
//											}, 1000);
//										}
//									}
//								});
//						mRecordThread.start();
//					}
//				}, 0);
//	}
//    
//	@Override
//	public void stop() {
//		AppLogic.runOnUiGround(new Runnable() {
//			@Override
//			public void run() {
//				mWorking = false;
//				if (mRecordThread != null) {
//					try {
//						mRecordThread.wait();
//					} catch (Exception e) {
//					}
//					mRecordThread = null;
//				}
//			}
//		}, 0);
//	}
//    
}
