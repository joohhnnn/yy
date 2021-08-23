package com.txznet.txz.module.record;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONObject;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.record.UiRecord;
import com.txz.ui.record.UiRecord.RecordData;
import com.txz.ui.record.UiRecord.RecordTime;
import com.txz.ui.wechat.UiWechat.WechatVoiceMessage;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.RecorderUtil;
import com.txznet.comm.remote.util.RecorderUtil.RecordCallback;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.record.IRecord;
import com.txznet.txz.component.tts.yunzhisheng_3_0.AudioSourceDistributer;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.Pcm2Wav;
import com.txznet.txz.util.recordcenter.ITXZSourceRecorder;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;
import com.txznet.txz.util.recordcenter.TXZSourceRecorderManager;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.util.LongSparseArray;

public class RecordManager extends IModule {
	public final static String RECORD_TASK_ID = "RecordManager";
	static RecordManager sModuleInstance = null;
    private IRecord mRecorder = null;
   
	private RecordManager() {
		try {
			mRecorder = (IRecord) Class.forName("com.txznet.txz.module.record.WXRecordImpl").newInstance();
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
		regEvent(com.txz.ui.event.UiEvent.EVENT_VOICE, com.txz.ui.record.UiRecord.SUBEVENT_BEGIN_SAVE_RECORD);
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
		case com.txz.ui.event.UiEvent.EVENT_VOICE:
			switch (subEventId) {
			case com.txz.ui.record.UiRecord.SUBEVENT_BEGIN_SAVE_RECORD:
				if(data == null){
					break;
				}
				try {
					RecordTime mRecordTime = RecordTime.parseFrom(data);
					AppLogic.runOnBackGround(new Runnable1<RecordTime>(mRecordTime) {

						@Override
						public void run() {
							JNIHelper.logd("receive save record startTime = "+mP1.uint64StartTime+", duration = "+mP1.uint32Duration);
							saveRecordByTime(mP1);
						}
					}, 0);
				} catch (InvalidProtocolBufferNanoException e) {
					break;
				}
				break;
			default:
				break;
			}
		default:
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}
	
	TXZAudioRecorder mAudioRecorder;
	
	/**
	 * 通过下发的时间来存储录音
	 * @param mRecordTime 
	 */
	private void saveRecordByTime(RecordTime mRecordTime) {
		if(mRecordTime == null || mRecordTime.uint32Duration == null || mRecordTime.uint64StartTime == null){
			JNIHelper.loge("push params error");
			return;
		}
		if(!ProjectCfg.enableSaveVoice()){
			LogUtil.loge("enable save voice false");
			return;
		}
		mAudioRecorder = new TXZAudioRecorder(ITXZSourceRecorder.READER_TYPE_AEC);
		RecordData mRecordData = new RecordData();
		long startTime = mRecordTime.uint64StartTime*1000L;
		int cacheSize = mRecordTime.uint32Duration*16*2;
		mRecordData.uint32RecordType = UiRecord.RECORD_TYPE_TIME;
		mRecordData.uint32SampleRate = RecordFile.SAMPLE_RATE_16K;
		mRecordData.uint32Uid = NativeData.getUID();
		mRecordData.bytesRecordResult = (startTime+"").getBytes();
		long nowTime = mRecordData.uint64RecordTime = startTime;
		mRecordData.boolRecordTime = true;
		if(nowTime == 0 || nowTime > startTime){
			JNIHelper.loge("time is wrong");
			return;
		}
		AppLogic.runOnSlowGround(new Runnable3<Integer, Long, RecordData>(cacheSize, startTime, mRecordData) {

			@Override
			public void run() {
				mAudioRecorder.startRecording();
				File rawFile = new File(ProjectCfg.AUDIO_SAVE_PATH+"/."+android.os.Process.myPid()+mP2);
				try {
					FileOutputStream fos = new FileOutputStream(rawFile);
					byte[] mBuffer = new byte[mP1];
					int offset = 0;
					int len = 0;
					while (offset < mP1) {
						len = mAudioRecorder.read(mBuffer, offset, mP1 - offset);
						offset += len;
					}
					mAudioRecorder.stop();
					fos.write(mBuffer);
					fos.close();
					RecordFile.createFile(new File(ProjectCfg.AUDIO_SAVE_PATH+"/txz_time_"+mP2+RecordFile.SUFFIX_RF), mP3, rawFile);
					ReportUtil.doVoiceReport(new ReportUtil.Report.Builder().setRecordType(UiRecord.RECORD_TYPE_TIME).setTaskID(mP2+"").buildTimeVoiceReport(), UiRecord.RECORD_TYPE_TIME, mP2);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if(rawFile != null){
						rawFile.delete();
					}
				}
			}
			
		}, startTime-nowTime);
		
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
	
	public void cancel() {
		if (mRecorder != null) {
			mRecorder.cancel();
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
		} else if (command.startsWith("comm.record.cancel")) {
			RecordManager.getInstance().cancel();
		}
		return null;
	}
	
	/**
	 * 录音功能是否可用
	 * @param enable
	 */
	public void setEnableRecording(boolean enable) {
		JNIHelper.logd("recordcenter: setEnableRecording: " + enable);
		if (enable == ProjectCfg.isEnableRecording()) {
			return;
		}
		ProjectCfg.setEnableRecording(enable);
		if (enable) {
			forceStartRecord();
		} else {
			forceStopRecord();
		}
	}
	
	/**
	 * 停止录音
	 */
	private void forceStopRecord() {
		WakeupManager.getInstance().stopComplete();
		RecorderWin.close();
		// 停止当前RecordManager录音, 防止微信正在录音时来电，来电播报被录到微信录音中并发送
		RecordManager.this.cancel();
		TXZSourceRecorderManager.stop();
	}
	
	/**
	 * 恢复录音
	 */
	private void forceStartRecord() {
		TXZSourceRecorderManager.start();
		//启动唤醒
		WakeupManager.getInstance().start();
	}
}
