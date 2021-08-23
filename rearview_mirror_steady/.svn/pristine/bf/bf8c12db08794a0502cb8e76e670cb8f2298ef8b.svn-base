package com.txznet.txz.module.record;

import org.json.JSONObject;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.wechat.UiWechat.WechatVoiceMessage;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.RecorderUtil;
import com.txznet.comm.remote.util.RecorderUtil.RecordCallback;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.record.IRecord;
import com.txznet.txz.component.tts.yunzhisheng_3_0.AudioSourceDistributer;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.util.Pcm2Wav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.util.LongSparseArray;

public class RecordManager extends IModule {
	static RecordManager sModuleInstance = null;
    private IRecord mRecorder = null;

	private RecordManager() {
		try {
			mRecorder = (IRecord) Class.forName("com.txznet.txz.module.record.WakeupRecordImpl").newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			mRecorder = null;
		}
		//注册广播接收器，将录音数据通过UDP分发出去，当前主要卡尔威需求。
		regReceiver();
	}

	public static RecordManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (RecordManager.class) {
				if (sModuleInstance == null)
					sModuleInstance = new RecordManager();
			}
		}
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		regEvent(com.txz.ui.event.UiEvent.EVENT_ACTION_WECHAT,  com.txz.ui.wechat.UiWechat.SUBEVENT_UPLOAD_VOICE_RESULT);
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
		switch (eventId) {
		case com.txz.ui.event.UiEvent.EVENT_ACTION_WECHAT:
			switch (subEventId) {
			case com.txz.ui.wechat.UiWechat.SUBEVENT_UPLOAD_VOICE_RESULT:
				try {
					WechatVoiceMessage message = WechatVoiceMessage.parseFrom(data);
					if(message.boolSuccess){
						synchronized (mRecordCallbacks) {
							RecordCallback callback = mRecordCallbacks.get(message.uint64Timestamp);
							if(callback != null){
								synchronized (mRecordOptions) {
									RecordOption option = mRecordOptions.get(message.uint64Timestamp);
									if(option != null){
										Pcm2Wav.encode(message.strVoicePcm, option.mSavePathPrefix + ".wav", 16000);
										mRecordOptions.remove(message.uint64Timestamp);
									}
								}
								if (message.strVoiceTxt== null || message.strVoiceUrl == null) {
									JNIHelper.loge("upload_voice_error, resp[txt="+ message.strVoiceTxt + ", url=" + message.strVoiceUrl +"]");
								}
								callback.onParseResult(message.uint32VoiceLen, message.strVoiceTxt, message.strVoiceUrl);
								mRecordCallbacks.remove(message.uint64Timestamp);
							}
						}
					} else {
						JNIHelper.loge("upload_voice_error, desc=" + message.strErrDesc);
						synchronized (mRecordCallbacks) {
							RecordCallback callback = mRecordCallbacks.get(message.uint64Timestamp);
							if(callback != null){
								callback.onError(500);
							}
						}
					}
				} catch (InvalidProtocolBufferNanoException e) {
					e.printStackTrace();
				}
			}
			break;
		default:
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}

	public boolean isBusy() {
		if (mRecorder != null){
			return mRecorder.isBusy();
		}
		return false;
	}

	public void start(RecordCallback callback) {
		if (mRecorder != null) {
			mRecorder.start(callback);
		}
	}

	public void start(RecordCallback callback, RecordOption option) {
		if (mRecorder != null){
			mRecorder.start(callback, option);
		}
	}

	public void stop() {
		if (mRecorder != null){
			mRecorder.stop();
		}
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("com.txznet.action.request.udp.start")){
				int port = intent.getIntExtra("port", 0);
				if (port != 0){
				   startRequestUdpData("127.0.0.1", port);
				}
			}
			if (intent.getAction().equals("com.txznet.action.request.udp.stop")){
				int port = intent.getIntExtra("port", 0);
				stopRequestUdpData("127.0.0.1", port);
			}
		}
		
	};
	private void regReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.txznet.action.request.udp.start");
		filter.addAction("com.txznet.action.request.udp.stop");
		GlobalContext.get().registerReceiver(mReceiver, filter);
		
		Intent intent = new Intent();
		intent.setAction("com.txznet.action.request.udp.init.ok");
		GlobalContext.get().sendStickyBroadcast(intent);
	}
	
	private SimpleRecorder mNetRecorder = null;
	public void startRequestUdpData(String ip, int port){
		if (null == mNetRecorder){
			mNetRecorder = new SimpleRecorder();
			mNetRecorder.addInetAddr(ip, port);
			AudioSourceDistributer.getIntance().addRecorder(mNetRecorder);
		}else{
			mNetRecorder.addInetAddr(ip, port);
		}	
	}
	
	public void stopRequestUdpData(String ip, int port){
		if (null != mNetRecorder){
			AudioSourceDistributer.getIntance().delRecorder(mNetRecorder);
			mNetRecorder.removeInetAddr(ip, port);
			mNetRecorder.close();
			mNetRecorder = null;
		}
	}
	
	LongSparseArray<RecorderUtil.RecordCallback> mRecordCallbacks = new LongSparseArray<RecorderUtil.RecordCallback>();
	LongSparseArray<RecorderUtil.RecordOption> mRecordOptions = new LongSparseArray<RecorderUtil.RecordOption>();

	public byte[] invokeCommRecord(final String packageName, String command, byte[] data) {
		if (command.startsWith("comm.record.start")) {
			final RecordOption option = new RecordOption();
			RecordCallback callback = new RecordCallback() {
				public void onBegin() {
					JNIHelper.logd("remote record begin");
					ServiceManager.getInstance().sendInvoke(packageName, "comm.record.event.begin", null, null);
				}

				public void onSpeechTimeout() {
					JNIHelper.logd("remote record speektimeout");
					ServiceManager.getInstance().sendInvoke(packageName, "comm.record.event.speechtimeout", null, null);
				}

				public void onMuteTimeout() {
					JNIHelper.logd("remote record mutetimeout");
					ServiceManager.getInstance().sendInvoke(packageName, "comm.record.event.mutetimeout", null, null);
				}

				public void onError(int err) {
					JNIHelper.logd("remote record error");
					ServiceManager.getInstance().sendInvoke(packageName, "comm.record.event.error",
							("" + err).getBytes(), null);
				}

				public void onCancel() {
					JNIHelper.logd("remote record cancel");
					ServiceManager.getInstance().sendInvoke(packageName, "comm.record.event.cancel", null, null);
				}

				public void onVolume(int vol) {
					JNIHelper.logd("remote record onvolume: " + vol);
					ServiceManager.getInstance().sendInvoke(packageName, "comm.record.event.volume",
							("" + vol).getBytes(), null);
				}

				public void onMute(int time) {
					JNIHelper.logd("remote record mute: " + time);
					ServiceManager.getInstance().sendInvoke(packageName, "comm.record.event.mute",
							("" + time).getBytes(), null);
				}

				public void onPCMBuffer(short[] buffer, int len) {
				}

				public void onMP3Buffer(byte[] buffer) {
					JNIHelper.logd("remote record mp3buffer");
					ServiceManager.getInstance().sendInvoke(packageName, "comm.record.event.mp3buf", null, null);
				}

				@Override
				public void onEnd(int speechLength) {
					JNIHelper.logd("remote record stop: " + speechLength);
					String retData = new JSONBuilder().put("length", speechLength).toString();
					ServiceManager.getInstance().sendInvoke(packageName, "comm.record.event.end", retData.getBytes(),
							null);
				}

				@Override
				public void onParseResult(int voiceLength, String voiceText, String voiceUrl) {
					JNIHelper.logd("remote record parse result: len=" + voiceLength + ", txt=" + voiceText + ", url=" + voiceUrl);
					String retData = new JSONBuilder().put("length", voiceLength).put("text", voiceText).put("url", voiceUrl).toString();
					ServiceManager.getInstance().sendInvoke(packageName, "comm.record.event.parse", retData.getBytes(),
							null);
				}
			};
			
			try {
				JSONObject json = new JSONObject(new String(data));
				if (json.has("AudioSource"))
					option.mAudioSource = json.getInt("AudioSource");
				if (json.has("EncodeMp3"))
					option.mEncodeMp3 = json.getBoolean("EncodeMp3");
				if (json.has("Increase"))
					option.mIncrease = json.getInt("Increase");
				if (json.has("MaxMute"))
					option.mMaxMute = json.getInt("MaxMute");
				if (json.has("MaxSpeech"))
					option.mMaxSpeech = json.getInt("MaxSpeech");
				if (json.has("SampleRate"))
					option.mSampleRate = json.getInt("SampleRate");
				if (json.has("SavePathPrefix"))
					option.mSavePathPrefix = json.getString("SavePathPrefix");
				if (json.has("SkipMute"))
					option.mSkipMute = json.getBoolean("SkipMute");
				if (json.has("NeedOnLineParse"))
					option.mNeedOnLineParse = json.getBoolean("NeedOnLineParse");
				if (json.has("OnLineParseTaskId")) {
					option.setOnLineParseTaskId(json.getLong("OnLineParseTaskId"));
				}
			} catch (Exception e) {
			}

			if(option.mNeedOnLineParse){
				synchronized (mRecordCallbacks) {
					mRecordCallbacks.put(option.mOnLineParseTaskId, callback);
				}
				synchronized (mRecordOptions) {
					mRecordOptions.put(option.mOnLineParseTaskId, option);
				}
			}
			RecordManager.this.start(callback, option);
		} else if (command.startsWith("comm.record.stop")) {
			RecordManager.getInstance().stop();
		}
		return null;
	}
}
