package com.txznet.comm.remote.util;

import static com.txznet.comm.remote.ServiceManager.TXZ;

import java.io.Serializable;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;

import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class RecorderUtil {
	private static final String TAG = "RecorderUtil";
	private static RecordCallback recordCallBack = null;

	public static class RecordOption implements Serializable {
		public int mAudioSource = AudioSource.MIC; // 录音源
		public int mSampleRate = 16000; // 采样率
		public int mMaxSpeech = 60 * 1000; // 最大录音时长60s，为0不限制
		public int mMaxMute = 5 * 1000; // 最大静音时长5秒，到达后自动停止，为0后不限制
		public boolean mEncodeMp3 = true; // 进行MP3编码
		public float mIncrease = 3.0f; // 录音增益
		public boolean mSkipMute = true; // 跳过静音包，减小录音数据
		public String mSavePathPrefix = ""; // 保存文件的路径前缀，自动补mp3和pcm，没填则回调onBuffer，填了不回调onBuffer
		public boolean mNeedOnLineParse = false; // 是否需要在线识别文本
		public long mOnLineParseTaskId = -1;

		public RecordOption setAudioSource(int param) {
			mAudioSource = param;
			return this;
		}

		public RecordOption setSampleRate(int param) {
			mSampleRate = param;
			return this;
		}

		public RecordOption setMaxSpeech(int param) {
			mMaxSpeech = param;
			return this;
		}

		public RecordOption setMaxMute(int param) {
			mMaxMute = param;
			return this;
		}

		public RecordOption setEncodeMp3(boolean param) {
			mEncodeMp3 = param;
			return this;
		}

		public RecordOption setIncrease(float param) {
			mIncrease = param;
			return this;
		}

		public RecordOption setSkipMute(boolean param) {
			mSkipMute = param;
			return this;
		}

		public RecordOption setSavePathPrefix(String param) {
			mSavePathPrefix = param;
			return this;
		}

		public RecordOption setNeedOnLineParse(boolean mNeedOnLineParse) {
			this.mNeedOnLineParse = mNeedOnLineParse;
			return this;
		}
		
		public RecordOption setOnLineParseTaskId(long taskId){
			this.mOnLineParseTaskId = taskId;
			return this;
		}
	}
	
	public static abstract class RecordCallback {
		/*
		 * 开始录音
		 */
		public abstract void onBegin();

		/*
		 * 录音结束，可能达到时长，可能手动取消
		 * speechLength为语音的长度，单位ms
		 */
		public abstract void onEnd(int speechLength);
		
		/*
		 * 识别结果回调
		 * voiceLength 音频媒体的在线录音时长
		 * voiceTxt 音频媒体的在线识别结果
		 * voiceUrl 音频媒体的服务器路径
		 */
		public abstract void onParseResult(int voiceLength, String voiceText, String voiceUrl);

		/*
		 * 达到最大录音时长
		 */
		public abstract void onSpeechTimeout();

		/*
		 * 达到最大静音时长
		 */
		public abstract void onMuteTimeout();

		/*
		 * 发生错误
		 */
		public abstract void onError(int err);

		/*
		 * 取消录音
		 */
		public abstract void onCancel();

		/*
		 * 音量变化
		 */
		public abstract void onVolume(int vol);

		/*
		 * 出现静音包，time为静音已持续的时间ms
		 */
		public abstract void onMute(int time);

		/*
		 * PCM录音数据
		 */
		public abstract void onPCMBuffer(short[] buffer, int len);

		/*
		 * MP3录音数据，需开启编码转换选项
		 */
		public abstract void onMP3Buffer(byte[] buffer);
	}

	private RecorderUtil() {

	}

	public static void start(RecordCallback callBack, RecordOption option) {
		recordCallBack = callBack;
		if (option.mNeedOnLineParse) {
			option.mOnLineParseTaskId = System.currentTimeMillis();
		}
		JSONBuilder json = new JSONBuilder();
		json.put("AudioSource", option.mAudioSource);
		json.put("EncodeMp3", option.mEncodeMp3);
		json.put("Increase", option.mIncrease);
		json.put("MaxMute", option.mMaxMute);
		json.put("MaxSpeech", option.mMaxSpeech);
		json.put("SampleRate", option.mSampleRate);
		json.put("SavePathPrefix", option.mSavePathPrefix);
		json.put("SkipMute", option.mSkipMute);
		json.put("NeedOnLineParse", option.mNeedOnLineParse);
		json.put("OnLineParseTaskId", option.mOnLineParseTaskId);

		ServiceManager.getInstance().sendInvoke(TXZ, "comm.record.start",
				json.toBytes(), null);
	}

	public static void stop() {
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.record.stop", null,
				null);
	}
	
	public static void cancel() {
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.record.cancel",
				null, null);
	}

	public static void notifyCallback(String event, byte[] data) {
		if (recordCallBack == null) {
			Log.i(TAG, "recordCallBack == null");
			return;
		}

		if (event.equals("end")) {
			JSONBuilder json = new JSONBuilder(new String(data));
			recordCallBack.onEnd(json.getVal("length", Integer.class));
		} else if (event.equals("parse")) {
			JSONBuilder json = new JSONBuilder(new String(data));
			recordCallBack.onParseResult(json.getVal("length", Integer.class), json.getVal("text", String.class), json.getVal("url", String.class));
		} else if (event.equals("cancel")) {
			recordCallBack.onCancel();
		} else if (event.equals("begin")) {
			recordCallBack.onBegin();
		} else if (event.equals("mute")) {
			int time = 0;//
			try {
				time = Integer.parseInt(new String(data));
			} catch (NumberFormatException e) {
				Log.e(TAG, "convert string to int error");
			}
			recordCallBack.onMute(time);
		} else if (event.equals("mutetimeout")) {
			recordCallBack.onMuteTimeout();
		} else if (event.equals("speechtimeout")) {
			recordCallBack.onSpeechTimeout();
		} else if (event.equals("volume")) {
			int vol = 0;//
			try {
				vol = Integer.parseInt(new String(data));
			} catch (NumberFormatException e) {
				Log.e(TAG, "convert string to int error");
			}
			recordCallBack.onVolume(vol);
		} else if (event.equals("error")) {
			int err = 0;//
			try {
				err = Integer.parseInt(new String(data));
			} catch (NumberFormatException e) {
				Log.e(TAG, "convert string to int error");
			}
			recordCallBack.onError(err);
		} else if (event.equals("mp3buf")) {
			recordCallBack.onMP3Buffer(data);
		}
	}
}
