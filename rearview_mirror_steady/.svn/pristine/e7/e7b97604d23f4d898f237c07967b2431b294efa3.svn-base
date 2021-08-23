package com.txznet.comm.remote.util;

import static com.txznet.comm.remote.ServiceManager.TXZ;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.media.AudioManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseArray;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.util.runnables.Runnable2;

public class TtsUtil {
	/* default_val */
	public static final int DEFAULT_STREAM_TYPE = -1;
	private static final ITtsCallback DEFAULT_TTS_CALLBACK = null;
	private static final PreemptType DEFAULT_PREEMPT_FLAG = PreemptType.PREEMPT_TYPE_NONE;
	public static final int INVALID_TTS_TASK_ID = 0;
	public static int DEFAULT_TTS_STREAM = AudioManager.STREAM_ALARM;
	public static final String BEEP_VOICE_URL = "$BEEP";

	/* error_code_list */
	public static final int ERROR_CODE_TIEMOUT = 1;

	public static enum PreemptType {
		/*
		 * 不打断
		 */
		PREEMPT_TYPE_NONE,
		/*
		 * 立即打断，取消当前tts并且插队播放
		 */
		PREEMPT_TYPE_IMMEADIATELY,
		/*
		 * 下一个插入，不取消当前tts，插队下一个播放
		 */
		PREEMPT_TYPE_NEXT,
		/*
		 * 清空队列
		 */
		PREEMPT_TYPE_FLUSH,
		/*
		 * 立即打断，不取消当前tts
		 */
		PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE;
	}
	
	public static class VoiceTask {
		public enum VoiceTaskType {
			/** 文本任务类型，使用{@link VoiceTask#setText(String)} */
			TEXT, 
			/** 音频任务类型，使用{@link VoiceTask#setUrl(String)} */
			LOCAL_URL, 
			NET_URL,
			/** beep音任务类型，不需要设置参数 */
			BEEP, 
			/** 静音任务类型，使用{@link VoiceTask#setDuration(long)} */
			QUIET, ALERT
		}
		/** 任务播报类型 */
		public VoiceTaskType type;
		/** 文本类型的播报内容 */
		public String text = "";
		/** 音频播报类型的地址 */
		public String url = "";
		/** 静音类型的时长 */
		public long duration;
		
		// ************** 目前未使用参数 ************ // 
//		String voiceSpeaker;
//		float volumeRate;
//		int voiceSpeed;
		
		protected VoiceTask() {
		}
		
		public VoiceTask(VoiceTaskType type) {
			super();
			if (type == null) {
				throw new NullPointerException("VoiceTask.VoiceTaskType == null");
			}
			this.type = type;
		}

		public VoiceTask setText(String text) {
			this.text = text;
			return this;
		}

		public VoiceTask setUrl(String url) {
			this.url = url;
			return this;
		}

		public VoiceTask setDuration(long duration) {
			this.duration = duration;
			return this;
		}
		
		private JSONObject toJsonObject() {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("type", type.name());
				switch (type) {
				case TEXT:
					jsonObject.put("text", text);
					break;
				case LOCAL_URL:
				case NET_URL:
					jsonObject.put("url", url);
					break;
				case QUIET:
				case ALERT:
					jsonObject.put("duration", duration);
					break;
				case BEEP:
					break;
				default:
				}
			} catch (Exception e) {
				LogUtil.logw(e.getMessage());
				e.printStackTrace();
			}
			return jsonObject;
		}
		
		public static JSONArray toJsonArray(VoiceTask[] voiceTasks) {
			if (voiceTasks == null) {
				return null;
			}
			JSONArray jsonArray = new JSONArray();
			for (VoiceTask voiceTask : voiceTasks) {
				if (voiceTask != null) {
					jsonArray.put(voiceTask.toJsonObject());
				} else {
					jsonArray.put(null);
				}
			}
			return jsonArray;
		}
		
		private static VoiceTask parseJsonObject(JSONObject jsonObject) {
			if (jsonObject == null) {
				return null;
			}
			String type = jsonObject.optString("type", null);
			if (type == null) {
				return null;
			}
			VoiceTask voiceTask = new VoiceTask(VoiceTaskType.valueOf(type));
			switch (voiceTask.type) {
			case TEXT:
				voiceTask.setText(jsonObject.optString("text", ""));
				break;
			case LOCAL_URL:
			case NET_URL:
				voiceTask.setUrl(jsonObject.optString("url", ""));
				break;
			case QUIET:
			case ALERT:
				voiceTask.setDuration(jsonObject.optLong("duration", 0L));
				break;
			case BEEP:
				break;
			default:
				voiceTask = null;
				break;
			}
			return voiceTask;
		}
		
		public static VoiceTask[] parseJsonArray(JSONArray jsonArray) {
			if (jsonArray == null) {
				return null;
			}
			int len = jsonArray.length();
			VoiceTask[] voiceTasks = new VoiceTask[len];
			for (int i = 0; i < len; i++) {
				voiceTasks[i] = VoiceTask.parseJsonObject(jsonArray.optJSONObject(i));
			}
			return voiceTasks;
		}
		
		public static String toText(VoiceTask[] voiceTasks) {
			if (voiceTasks == null || voiceTasks.length == 0) {
				return null;
			}
			String string = "";
			for (VoiceTask voiceTask : voiceTasks) {
				if (voiceTask != null && voiceTask.type == VoiceTaskType.TEXT && voiceTask.text != null) {
					string += voiceTask.text;
				}
			}
			return string;
		}
		
		public static String[] toUrls(VoiceTask[] voiceTasks) {
			if (voiceTasks == null || voiceTasks.length == 0) {
				return null;
			}
			
			ArrayList<String> urls = new ArrayList<String>();
			for (VoiceTask voiceTask : voiceTasks) {
				if (voiceTask == null) {
					continue;
				}
				
				String string = "";
				switch (voiceTask.type) {
				case LOCAL_URL:
				case NET_URL:
					string = voiceTask.url;
					break;
				case BEEP:
					string = "$BEEP";
					break;
				default:
					break;
				}
				
				if (!TextUtils.isEmpty(string)) {
					urls.add(string);
				}
			}
			return urls.toArray(new String[urls.size()]);
		}

		@Override
		public String toString() {
			String string;
			switch (type) {
			case TEXT:
				string = type.toString() + ":" + text;
				break;
			case LOCAL_URL:
			case NET_URL:
				string = type.toString() + ":" + url;
				break;
			case BEEP:
				string = type.toString();
				break;
			case QUIET:
			case ALERT:
				string = type.toString() + ":" + duration;
				break;
			default:
				string = "unkown type";
				break;
			}
			return string;
		}
	}

	public static abstract class ITtsCallback {
		protected Object mData;
		
		/**
		 * 开始播报时回调，tts会有排队，真正开始播报时回调
		 */
		public void onBegin() {
			
		}

		/**
		 * 结束时回调，无论是Cancel、Succes、Error最后都会调用
		 */
		public void onEnd() {

		}

		/**
		 * 取消TTS时回调
		 */
		public void onCancel() {
		}

		/**
		 * TTS成功完成时回调
		 */
		public void onSuccess() {
		}

		/**
		 * TTS出错时回调
		 *
		 * @param iError
		 */
		public void onError(int iError) {
		}

		public ITtsCallback setData(Object d) {
			mData = d;
			return this;
		}

		protected int mTaskId = INVALID_TTS_TASK_ID;

		public ITtsCallback setTaskId(int taskId) {
			this.mTaskId = taskId;
			return this;
		}
		
		/**
		 * 在TTS播放的时候是否需要开启识别，用于随意打断功能
		 * @return
		 */
		public boolean isNeedStartAsr(){
			return false;
		}
	}

	private static class RemoteTtsTask {
		int remoteId = -1;
		boolean isCanceled;
		ITtsCallback callback;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + remoteId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RemoteTtsTask other = (RemoteTtsTask) obj;
			if (remoteId != other.remoteId)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "RemoteTtsTask [remoteId=" + remoteId + ", isCanceled=" + isCanceled + ", callback=" + callback + "]";
		}
	}

	private static SparseArray<RemoteTtsTask> mTaskMap = new SparseArray<RemoteTtsTask>();
	private static class TaskEvent {
		String event;
		Integer error;
		long time;
	};
	private static SparseArray<ArrayList<TaskEvent>> mTaskEventMap = new SparseArray< ArrayList<TaskEvent> >(); //存储过早回包的tts事件
	
	public static void addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("comm.tts.",new CommandProcessor() {
			
			@Override
			public Object invoke(String command, Object[] args) {
				try {
					if("speakText".equals(command)){
						if(!(args[0] instanceof Integer) || !(args[1] instanceof String) 
								|| !(args[2] instanceof PreemptType) 
								|| !(args[3] instanceof ITtsCallback)){
							LogUtil.logd("comm.tts.speakText params error");
							return null;
						}
						int iStream = (Integer) args[0];
						String sText = (String) args[1];
						PreemptType bPreempt = (PreemptType) args[2];
						ITtsCallback oRun = (ITtsCallback) args[3];
						return speakText(iStream, sText, bPreempt, oRun);
					}else if("cancelSpeak".equals(command)){
						if(!(args[0] instanceof Integer)){
							LogUtil.logd("comm.tts.cancelSpeak params error");
							return null;
						}
						int iTaskId = (Integer) args[0];
						cancelSpeak(iTaskId);
					}else if("speakTextOnRecordWin".equals(command)){
						if(!(args[0] instanceof String) || !(args[1] instanceof Boolean) 
								|| !(args[2] instanceof Boolean) 
								||!(args[3] instanceof Runnable) ){
							LogUtil.logd("comm.tts.speakTextOnRecordWin params error");
							return null;
						}
						String sText = (String) args[0];
						boolean close = (Boolean) args[1];
						boolean needAsr = (Boolean) args[2];
						Runnable oRun = (Runnable) args[3];
						speakTextOnRecordWin(sText, close, needAsr, oRun);
					}
				} catch (Exception e) {
				}
				return null;
			}
		});
	}

	public static void clearTaskMap() {
		synchronized (mTaskMap) {
			for (int i = 0; i < mTaskMap.size(); i++) {
				RemoteTtsTask task = mTaskMap.valueAt(i);
				if (task != null && task.callback != null) {
					task.callback.onError(-1024);
				}
			}
			mTaskMap.clear();
		}
	}
	
	public static int speakText(int iStream, String sText, PreemptType bPreempt) {
		return speakText(iStream, sText, bPreempt, DEFAULT_TTS_CALLBACK);
	}

	public static int speakText(int iStream, String sText, ITtsCallback oRun) {
		return speakText(iStream, sText, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public static int speakText(int iStream, String sText) {
		return speakText(iStream, sText, DEFAULT_PREEMPT_FLAG, DEFAULT_TTS_CALLBACK);
	}

	public static int speakText(String sText, PreemptType bPreempt, ITtsCallback oRun) {
		return speakText(DEFAULT_STREAM_TYPE, sText, bPreempt, oRun);
	}

	public static int speakText(String sText, PreemptType bPreempt) {
		return speakText(DEFAULT_STREAM_TYPE, sText, bPreempt, DEFAULT_TTS_CALLBACK);
	}

	public static int speakText(String sText, ITtsCallback oRun) {
		return speakText(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public static int speakText(String sText) {
		return speakText(DEFAULT_STREAM_TYPE, sText, DEFAULT_PREEMPT_FLAG, DEFAULT_TTS_CALLBACK);
	}
	
	public static int speakText(int iStream, String sText, PreemptType bPreempt, ITtsCallback oRun) {
		return speakVoice(iStream, sText, null, bPreempt, oRun);
	}
	public static int speakText(int iStream, String sText, long delay, PreemptType bPreempt, ITtsCallback oRun) {
		return speakVoice(iStream, sText, null, delay, bPreempt, oRun);
	}

	///////////////////////// 
	
	public static int speakResource(String resId, String defaultText) {
		return speakResource(DEFAULT_STREAM_TYPE, resId, null, defaultText, null, DEFAULT_PREEMPT_FLAG, null);
	}
	
	public static int speakResource(String resId, String[] resArgs, String defaultText){
		return speakResource(DEFAULT_STREAM_TYPE, resId, resArgs, defaultText, null, DEFAULT_PREEMPT_FLAG, null);
	}
	
	public static int speakResource(String resId, String defaultText, ITtsCallback oRun) {
		return speakResource(DEFAULT_STREAM_TYPE, resId, null, defaultText, null, DEFAULT_PREEMPT_FLAG, oRun);
	}
	
	public static int speakResource(String resId, String[] resArgs, String defaultText, ITtsCallback oRun){
		return speakResource(DEFAULT_STREAM_TYPE, resId, resArgs, defaultText, null, DEFAULT_PREEMPT_FLAG, oRun);
	}
	
	public static int speakResource(int iStream, String resId, String defaultText, PreemptType bPreempt, ITtsCallback oRun) {
		return speakResource(iStream, resId, null, defaultText, null, bPreempt, oRun);
	}
	
	public static int speakResource(int iStream, String resId, String[] resArgs, String defaultText, PreemptType bPreempt, ITtsCallback oRun){
		return speakResource(iStream, resId, resArgs, defaultText, null, bPreempt, oRun);
	}
	
	public static int speakResource(int iStream, String resId, String[] resArgs, String defaultText, String[] voiceUrls, PreemptType preempt, ITtsCallback oRun) {
		return speakVoice(iStream, resId, resArgs, defaultText, voiceUrls, 0, preempt, oRun);
	}

	///////////////////////////
	
	public static int speakVoice(String voiceUrl, PreemptType bPreempt, ITtsCallback oRun) {
		return speakVoice("", voiceUrl, bPreempt, oRun);
	}

	public static int speakVoice(String voiceUrl, ITtsCallback oRun) {
		return speakVoice("", voiceUrl, DEFAULT_PREEMPT_FLAG, oRun);
	}

	public static int speakVoice(String sText, String voiceUrl, PreemptType bPreempt) {
		return speakVoice(sText, voiceUrl, bPreempt, null);
	}

	public static int speakVoice(String sText, String voiceUrl) {
		return speakVoice(sText, voiceUrl, DEFAULT_PREEMPT_FLAG, null);
	}

	public static int speakVoice(String voiceUrl, PreemptType bPreempt) {
		return speakVoice("", voiceUrl, bPreempt, null);
	}

	public static int speakVoice(String voiceUrl) {
		return speakVoice("", voiceUrl, DEFAULT_PREEMPT_FLAG, null);
	}

	public static int speakVoice(String sText, String voiceUrl, ITtsCallback oRun) {
		return speakVoice(sText, voiceUrl, DEFAULT_PREEMPT_FLAG, oRun);
	}
	
	public static int speakVoice(String sText, String voiceUrl, PreemptType bPreempt, ITtsCallback oRun) {
		if (voiceUrl == null)
			return speakVoice(DEFAULT_STREAM_TYPE, sText, new String[] {}, bPreempt, oRun);
		return speakVoice(DEFAULT_STREAM_TYPE, sText, new String[] { voiceUrl }, bPreempt, oRun);
	}

	public static int speakVoice(int iStream, String sText, String[] voiceUrls, PreemptType bPreempt, final ITtsCallback oRun) {
		return speakVoice(iStream,"", null, sText, voiceUrls,0,bPreempt,oRun);
	}

	public static int speakVoice(int iStream, String sText, String[] voiceUrls, long delay, PreemptType bPreempt, final ITtsCallback oRun) {
		return speakVoice(iStream, "", null, sText, voiceUrls, delay, bPreempt, oRun);
	}
	
	public static int speakVoice(int iStream, String resId, String[] resArgs, String sText, String[] voiceUrls, PreemptType bPreempt, final ITtsCallback oRun){
		return speakVoice(iStream, resId, resArgs, sText, voiceUrls, 0, bPreempt, oRun);
	}
	
	public static int speakVoiceTask(VoiceTask[] voiceTasks, final ITtsCallback oRun) {
		return speakVoiceTask(DEFAULT_STREAM_TYPE, DEFAULT_PREEMPT_FLAG, voiceTasks, oRun);
	}
	
	public static int speakVoiceTask(int iStream, VoiceTask[] voiceTasks, final ITtsCallback oRun) {
		return speakVoiceTask(iStream, DEFAULT_PREEMPT_FLAG, voiceTasks, oRun);
	}
	
	public static int speakVoiceTask(PreemptType bPreempt, VoiceTask[] voiceTasks, final ITtsCallback oRun) {
		return speakVoiceTask(DEFAULT_STREAM_TYPE, bPreempt, voiceTasks, oRun);
	}
	
	public static int speakVoiceTask(int iStream, PreemptType bPreempt, VoiceTask[] voiceTasks, final ITtsCallback oRun) {
		return speakVoice(iStream, "", null, "", null, 0, bPreempt, voiceTasks, oRun);
	}
	
	private static Boolean isSupportOnBegin = null;
	public static void refreshFeatures() {
		if (isSupportOnBegin != null) {
			return;
		}
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.tts.getFeatures", null, new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(final ServiceData data) {
				if (null != data) {
					ServiceManager.getInstance().runOnServiceThread(new Runnable() {
						@Override
						public void run() {
							synchronized (mTaskMap) {
								JSONObject features = data.getJSONObject();
								Boolean isSupportOnBeginOld = isSupportOnBegin;
								if (null != features) {
									isSupportOnBegin = features.optBoolean("isSupportOnBegin", false);
								} else {
									isSupportOnBegin = false;
								}
								LogUtil.logd("isSupportOnBegin: " + isSupportOnBegin);
								
								//首次同步到标志时，发现如果不支持onBegin，则把已经生成的任务遍历执行一遍onBegin
								if (null == isSupportOnBeginOld && isSupportOnBegin == false) {
									for (int i = 0; i < mTaskMap.size(); ++i) {
										RemoteTtsTask task = mTaskMap.valueAt(i);
										if (task.remoteId != -1) {
											if (null != task.callback){
												task.callback.onBegin();
											}
										}
									}
								}
								
								procSpeakVoiceEvent(null);
							}
						}
					}, 500);
				}
			}
		});
	}
	static {
		refreshFeatures();
	}
	
	private static void procSpeakVoiceEvent(Integer remotetaskId) {
		synchronized (mTaskMap) {
			if (isSupportOnBegin == null) {
				return;
			}
			for (int i = 0; i < mTaskEventMap.size(); ) {
				final int remoteId = mTaskEventMap.keyAt(i);
				final ArrayList<TaskEvent> taskEventList = mTaskEventMap.valueAt(i);
				// 处理空任务事件
				if (taskEventList.isEmpty()) {
					mTaskEventMap.removeAt(i);
					continue;
				}
				// 处理超时任务事件
				if (SystemClock.elapsedRealtime() -  taskEventList.get(taskEventList.size()-1).time > 5*60*1000) {
					mTaskEventMap.removeAt(i);
					continue;
				}
				// 处理先到的事件
				if (remotetaskId == null || remoteId == remotetaskId) {
					ServiceManager.getInstance().runOnServiceThread(new Runnable() {
						@Override
						public void run() {
							for (TaskEvent taskEvent: taskEventList) {
								LogUtil.logw("process tts old event " + taskEvent.event + " for :" + remoteId);
								notifyTtsCallback(taskEvent.event, remoteId, taskEvent.error);
							}
						}
					}, 0);
					mTaskEventMap.removeAt(i);
					continue;
				}
				++i;
			}
		}
	}
	
	public static int speakVoice(int iStream, String resId, String[] resArgs, String sText, String[] voiceUrls, long delay, PreemptType bPreempt, final ITtsCallback oRun) {
		return speakVoice(iStream, resId, resArgs, sText, voiceUrls, delay, bPreempt, null, oRun);
	}
	
	public static int speakVoice(int iStream, String resId, String[] resArgs, String sText, String[] voiceUrls, long delay, PreemptType bPreempt, VoiceTask[] voiceTasks, final ITtsCallback oRun) {
		String data = genInvokeData(iStream, sText, voiceUrls, bPreempt, resId, resArgs, delay, voiceTasks);
		final ITtsCallback cb = oRun ==null ? null : new ITtsCallback() {
			boolean hasBegin = false;
			boolean hasComplete = false;
			boolean hasEnd = false;
			@Override
			public void onBegin() {
				if (!hasBegin) {
					hasBegin = true;
					oRun.onBegin();
				}
			}
			@Override
			public void onEnd() {
				if (!hasEnd) {
					hasEnd = true;
					oRun.onEnd();
				}
			}
			@Override
			public void onCancel() {
				if (!hasComplete) {
					hasComplete = true;
					oRun.onCancel();
					this.onEnd();
				}
			}
			@Override
			public void onSuccess() {
				if (!hasBegin) {
					isSupportOnBegin = false;
					onBegin();
				}
				if (!hasComplete) {
					hasComplete = true;
					oRun.onSuccess();
					this.onEnd();
				}
			}
			@Override
			public void onError(int iError) {
				if (!hasComplete) {
					hasComplete = true;
					oRun.onError(iError);
					this.onEnd();
				}
			}
		};
		synchronized (mTaskMap) {
			int localTtsId = ServiceManager.getInstance().sendInvoke(TXZ, "comm.tts.speak", data.getBytes(), new GetDataCallback() {
				@Override
				public void onGetInvokeResponse(ServiceData data) {
					synchronized (mTaskMap) {
						RemoteTtsTask remoteTtsTask = mTaskMap
								.get(getTaskId());
						if (remoteTtsTask == null) {
							LogUtil.logw("find local task failed: "
									+ getTaskId());
							return;
						}
						
						if (isSupportOnBegin != null) {
							// 不支持begin的直接调用begin
							if (isSupportOnBegin == false) {
								if (cb != null) {
									cb.onBegin();
								}
							}
						}
	
						if (data != null) {
							remoteTtsTask.remoteId = data.getInt();
							if (remoteTtsTask.isCanceled) {
								ServiceManager.getInstance().sendInvoke(TXZ, "comm.tts.cancel", ("" + remoteTtsTask.remoteId).getBytes(), null);
								mTaskMap.remove(getTaskId());
								return;
							}
							
							procSpeakVoiceEvent(remoteTtsTask.remoteId);
						} else {
							if (isTimeout()) {
								if (cb != null) {
									cb.onBegin();
									cb.onError(ERROR_CODE_TIEMOUT);
								}
								mTaskMap.remove(getTaskId());
							}
						}
					}
				}
			});
			
			if (mTaskMap.get(localTtsId) != null) {
				LogUtil.logw("already exist tts task id: " + localTtsId);
			}
			RemoteTtsTask remoteTtsTask = new RemoteTtsTask();
			remoteTtsTask.callback = cb;
			mTaskMap.put(localTtsId, remoteTtsTask);
			return localTtsId;
		}
	}
	
	public static void cancelSpeak(int iTaskId) {
		synchronized (mTaskMap) {
			RemoteTtsTask task = mTaskMap.get(iTaskId);
			if (task != null) {
				// 优先回调
				if (task.callback != null) {
					ServiceManager.getInstance().runOnServiceThread(new Runnable2<RemoteTtsTask, Integer>(task, iTaskId) {
						@Override
						public void run() {
							if (mP1.callback != null) {
								mP1.callback.onCancel();
							}
						}
					}, 0);
				}
				if (task.remoteId == -1) {
					task.isCanceled = true;
				} else {
					ServiceManager.getInstance().sendInvoke(TXZ, "comm.tts.cancel", ("" + task.remoteId).getBytes(), null);
					if (task.callback == null) {
						mTaskMap.remove(iTaskId);
					}
				}
			}
		}
	}

	public static void notifyTtsCallback(final String event, int remoteTtsId, final Integer iError) {
		synchronized (mTaskMap) {
			if ("begin".equals(event)) {
				isSupportOnBegin = true;
			}
			if (isSupportOnBegin != null) {
				for (int i = 0; i < mTaskMap.size(); ++i) {
					final RemoteTtsTask task = mTaskMap.valueAt(i);
					if (task.remoteId == remoteTtsId) {
						if (task.callback != null) {
							ServiceManager.getInstance().runOnServiceThread(new Runnable() {
								@Override
								public void run() {
									if ("begin".equals(event)) {
										task.callback.onBegin();
									} else if ("success".equals(event)) {
										task.callback.onSuccess();
									} else if ("cancel".equals(event)) {
										task.callback.onCancel();
									} else if ("error".equals(event)) {
										task.callback.onError(iError);
									}
								}
							}, 0);
						}
						if (!"begin".equals(event)) {
							mTaskMap.removeAt(i);
						}
						return;
					}
				}
				
				LogUtil.logw("can not found task: " + remoteTtsId + " for event " + event);
			} else {
				LogUtil.logw("need sync feature: " + remoteTtsId + " for event " + event);
			}
			
			ArrayList<TaskEvent> taskEventList = mTaskEventMap.get(remoteTtsId);
			if (taskEventList == null) {
				taskEventList = new ArrayList<TaskEvent>();
				mTaskEventMap.put(remoteTtsId, taskEventList);
			}
			TaskEvent taskEvent = new TaskEvent();
			taskEvent.event = event;
			taskEvent.time = SystemClock.elapsedRealtime();
			taskEvent.error = iError;
			taskEventList.add(taskEvent);
		}
		
	}

	public static byte[] preInvokeTtsEvent(String packageName, String command, byte[] data) {
		if (command.equals("speakTextOnRecordWin.end")) {
			if (mRunSpeakTextOnRecordWinEnd != null) {
				mRunSpeakTextOnRecordWinEnd.run();
			}
			return null;
		}
		JSONBuilder jsonDoc = new JSONBuilder(new String(data));
		int ttsId = jsonDoc.getVal("ttsId", Integer.class);
		if (command.equals("begin")) {
			TtsUtil.notifyTtsCallback("begin", ttsId, null);
		} else if (command.equals("success")) {
			TtsUtil.notifyTtsCallback("success", ttsId, null);
		} else if (command.equals("cancel")) {
			TtsUtil.notifyTtsCallback("cancel", ttsId, null);
		} else if (command.equals("error")) {
			int error = jsonDoc.getVal("error", Integer.class);
			TtsUtil.notifyTtsCallback("success", ttsId, error);
		}
		return null;
	}

	static Runnable mRunSpeakTextOnRecordWinEnd = null;

	public static void speakTextOnRecordWin(String sText, boolean close,
			Runnable oRun) {
		speakTextOnRecordWin(sText, close, true, oRun);
	}
	public static void speakTextOnRecordWin(String resId,String sText, boolean close,
			Runnable oRun) {
		speakTextOnRecordWin(resId,sText, null,close, true, oRun);
	}
	public static void speakTextOnRecordWin(String resId,String sText, String[] resArgs, boolean close,
			Runnable oRun) {
		speakTextOnRecordWin(resId,sText,resArgs, close, true, oRun);
	}
	public static void speakTextOnRecordWinWithCancle(String resId,String sText, String[] resArgs, boolean  isCancleExecute,
			Runnable oRun) {
		speakTextOnRecordWin(resId,sText,resArgs, true, true, isCancleExecute,oRun);
	}


	/**
	 * 
	 * @param sText
	 *            播报的内容
	 * @param close
	 *            播报完毕后是否关闭界面
	 * @param needAsr
	 *            是否开启识别的状态
	 * @param oRun
	 *            播报完毕执行的逻辑
	 */
	public static void speakTextOnRecordWin(String sText, boolean close,
			boolean needAsr, Runnable oRun) {
		speakTextOnRecordWin("",sText, null,close, needAsr, oRun);
	}
	public static void speakTextOnRecordWin(String resId,String sText, String[] resArgs, boolean close,
			boolean needAsr, Runnable oRun) {
		speakTextOnRecordWin(resId,sText,resArgs,close,needAsr,true,oRun);
	}
	public static void speakTextOnRecordWin(String resId,String sText, String[] resArgs, boolean close,
			boolean needAsr,boolean isCancleExecute, Runnable oRun) {

		mRunSpeakTextOnRecordWinEnd = oRun;
		JSONBuilder json = new JSONBuilder();
		json.put("text", sText);
		json.put("close", close);
		json.put("needAsr", needAsr);
		if (resId != null && resId.length() != 0)
			json.put("resId", resId);
		if (resArgs != null && resArgs.length != 0)
			json.put("resArgs", resArgs);
		json.put("isCancleExecute", isCancleExecute);
		
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"comm.tts.speakTextOnRecordWin", json.toBytes(), null);
	}
	
	private static String genInvokeData(int iStream, String sText, String[] voiceUrls, PreemptType bPreempt, String resId, String[] resArgs) {
		if (resId == null || resId.length() == 0){
			return new JSONBuilder().put("iStream", iStream).put("sText", sText).put("voiceUrls", voiceUrls).put("bPreempt", bPreempt.toString()).toString();
		}else{
			JSONBuilder builder = new JSONBuilder();
			builder.put("iStream", iStream);
			builder.put("sText", sText);
			builder.put("voiceUrls", voiceUrls);
			builder.put("bPreempt", bPreempt.toString());
			builder.put("resId", resId);
			if(resArgs != null){
				builder.put("resArgs", resArgs);
			}
			return builder.toString();
		}
	}
	private static String genInvokeData(int iStream, String sText, String[] voiceUrls, PreemptType bPreempt, String resId, String[] resArgs, long delay) {
		if (resId == null || resId.length() == 0){
			return new JSONBuilder().put("iStream", iStream).put("sText", sText).put("voiceUrls", voiceUrls).put("bPreempt", bPreempt.toString()).put("delay", delay).toString();
		}else{
			JSONBuilder builder = new JSONBuilder();
			builder.put("iStream", iStream);
			builder.put("sText", sText);
			builder.put("voiceUrls", voiceUrls);
			builder.put("bPreempt", bPreempt.toString());
			builder.put("resId", resId);
			builder.put("delay", delay);
			if(resArgs != null){
				builder.put("resArgs", resArgs);
			}
			return builder.toString();
		}
	}
	
	private static String genInvokeData(int iStream, String sText, String[] voiceUrls, PreemptType bPreempt, String resId, String[] resArgs, long delay,VoiceTask[] voiceTasks) {
		JSONBuilder builder = new JSONBuilder();
		builder.put("iStream", iStream);
		builder.put("bPreempt", bPreempt.toString());
		builder.put("sText", sText);
		builder.put("voiceUrls", voiceUrls);
		if (!TextUtils.isEmpty(resId)) {
			builder.put("resId", resId);
			if (resArgs != null) {
				builder.put("resArgs", resArgs);
			}
		}
		builder.put("delay", delay);
		if (voiceTasks != null && voiceTasks.length > 0) {
			builder.put("voiceTask", VoiceTask.toJsonArray(voiceTasks));
			String text = VoiceTask.toText(voiceTasks);
			if (!TextUtils.isEmpty(text)) {
				builder.put("sText", text);
			}
			String[] urls = VoiceTask.toUrls(voiceTasks);
			if (urls != null && urls.length > 0) {
				builder.put("voiceUrls", urls);
			}
		}
		return builder.toString();
	}
}
