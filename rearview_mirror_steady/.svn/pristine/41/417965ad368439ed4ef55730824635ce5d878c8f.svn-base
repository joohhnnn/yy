package com.txznet.sdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZService.CommandProcessor;

/**
 * 类名：语音TTS播报管理器
 * 类描述：语音TTS播报相关设置管理类，主要包含语音默认TTS播报相关属性，如：通道，延时，播报速度，TTS主题包，beep音定制待，
 * 		  同时自定义外部TTS工具，替换语音默认播报工具。
 */
public class TXZTtsManager {
	private static TXZTtsManager sInstance = new TXZTtsManager();

	private TXZTtsManager() {
	}

	/**
	 * 获取单例
	 *
	 * @return 类实例
	 */
	public static TXZTtsManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		TtsUtil.clearTaskMap();
		
		if (mDefaultAudioStream != null)
			setDefaultAudioStream(mDefaultAudioStream);
		if (mVoiceSpeed != null) {
			setVoiceSpeed(mVoiceSpeed);
		}
		if (mTtsModel != null) {
			setTtsModel(mTtsModel);
		}
		if (mTtsTool != null) {
			setTtsTool(mTtsTool);
		}
		
		if (mBufferTime != null){
			setBufferTime(mBufferTime);
		}
		if (mBeepPath != null) {
			setBeepResources(mBeepPath);
		}
		if (mEnableDownVolume != null) {
			enableDownVolumeWhenNav(mEnableDownVolume);
		}
		if (mReplaceJson != null) {
			setReplaceSpeakWord(mReplaceJson);
		}
		if (mForceShowChoiceView != null) {
			forceShowTTSChoiceView(mForceShowChoiceView);
		}
		if (mThemeListener !=  null) {
			setChangeThemeListener(mThemeListener);
		}
	}

	/**
	 * 常量名：无效的tts任务id
	 * 变量描述：无效的tts任务使用此ID
	 */
	public final static int INVALID_TTS_TASK_ID = TtsUtil.INVALID_TTS_TASK_ID;

	/**
	 * 类名：TTS逻辑回调类
	 * 类描述：回调包含TTS播报状态：开始、结束、完成、取消、出错，详见{@link TtsUtil.ITtsCallback}
	 */
	public static abstract class ITtsCallback extends TtsUtil.ITtsCallback {

	}

	/**
	 * 类名：播报任务类
	 * 类描述：播报任务类型的封装类，用于播报特殊的任务
	 * 		   适用于特殊场景，如同一个TTS任务中执行多个播报，即播TTS,然后暂停100ms，再播BEEP音，再播报一段音频。
	 */
	public static class VoiceTask extends TtsUtil.VoiceTask {
		/**
		 * 枚举类名：任务类型
		 * 枚举类描述：播报任务类型分类
		 */
		public enum TaskType {
			/**
			 * 枚举名：文本任务类型
			 * 枚举描述：文本任务类型，使用{@link VoiceTask#setText(String)}
			 */
			TEXT,
			/**
			 * 枚举名：音频任务类型
			 * 枚举描述：音频任务类型，使用{@link VoiceTask#setUrl(String)}
			 */
			LOCAL_URL,
			/**
			 * 枚举名：beep音任务类型
			 * 枚举描述：beep音任务类型，不需要设置参数
			 */
			BEEP,
			/**
			 * 枚举名：静音任务类型
			 * 枚举描述：静音任务类型，使用{@link VoiceTask#setDuration(long)}
			 */
			QUIET//, ALERT, NET_URL
		}
		
		public VoiceTask(TaskType type) {
			if (type == null) {
				throw new NullPointerException("VoiceTask.TaskType == null");
			}
			switch (type) {
			case TEXT:
				this.type = TtsUtil.VoiceTask.VoiceTaskType.TEXT;
				break;
			case LOCAL_URL:
				this.type = TtsUtil.VoiceTask.VoiceTaskType.LOCAL_URL;
				break;
//			case NET_URL:
//				this.type = TtsUtil.VoiceTask.VoiceTaskType.NET_URL;
//				break;
			case BEEP:
				this.type = TtsUtil.VoiceTask.VoiceTaskType.BEEP;
				break;
			case QUIET:
				this.type = TtsUtil.VoiceTask.VoiceTaskType.QUIET;
				break;
//			case ALERT:
//				this.type = TtsUtil.VoiceTask.VoiceTaskType.ALERT;
//				break;
			default:
				break;
			}
		}

		/**
		 * 方法名：设置文本
		 * 方法描述：设置任务播报文本
		 *
		 * @param text 播报文本
		 * @return 任务实例
		 */
		public VoiceTask setText(String text) {
			this.text = text;
			return this;
		}

		/**
		 * 方法名：设置播报路径
		 * 方法描述：音频播报类型的地址
		 *
		 * @param url 音频播报类型的地址
		 * @return 任务实例
		 */
		public VoiceTask setUrl(String url) {
			this.url = url;
			return this;
		}

		/**
		 * 方法名：设置静音时长
		 * 方法描述：静音类型的时长
		 *
		 * @param duration 静音类型的时长
		 * @return 任务实例
		 */
		public VoiceTask setDuration(long duration) {
			this.duration = duration;
			return this;
		}
		
	}

	/**
	 * 枚举类名：TTS播报类型
	 * 枚举类描述：TTS播报时使用，针对不同类型处理方式不同
	 */
	public static enum PreemptType {
		/**
		 * 枚举名：不打断模式
		 * 枚举描述：TTS播报时设置此模式，TTS任务进入队列，不打断当前TTS
		 */
		PREEMPT_TYPE_NONE,
		/**
		 * 枚举名：立即打断模式
		 * 枚举描述：TTS播报时设置此模式，TTS任务直接打断当前TTS进行播报
		 */
		PREEMPT_TYPE_IMMEADIATELY,
		/**
		 * 枚举名：下一个插入模式
		 * 枚举描述：TTS播报时设置此模式，插入至TTS队列下一个
		 */
		PREEMPT_TYPE_NEXT,
		/**
		 * 枚举名：清空队列模式
		 * 枚举描述：TTS播报时设置此模式，TTS任务进入队列，并清空当前非自身队列任务
		 */
		PREEMPT_TYPE_FLUSH,
		/**
		 * 枚举名：立即打断模式
		 * 枚举描述：TTS播报时设置此模式，立即打断当前TTS，但不取消tts任务，当前TTS任务延后
		 */
		PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE;
	}

	/**
	 * 方法名：TTS播报
	 * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
	 * 			与{@link TXZResourceManager#speakTextOnRecordWin(String, boolean, Runnable)}有区别
	 * 			此方法仅为播报，文本不会显示在语音界面
	 *
	 * @param streamType 流类型，填写AudioManager.STREAM常量，默认使用AudioManager.STREAM_ALARM
	 * @param text       播报的文本
	 * @param type       打断类型
	 * @param callback   播报回调
	 * @return 返回Tts的任务ID
	 */
	public int speakText(int streamType, String text, PreemptType type,
			ITtsCallback callback) {
		TtsUtil.PreemptType pt = TtsUtil.PreemptType.PREEMPT_TYPE_NONE;
		switch (type) {
		case PREEMPT_TYPE_FLUSH:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_FLUSH;
			break;
		case PREEMPT_TYPE_IMMEADIATELY:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY;
			break;
		case PREEMPT_TYPE_NEXT:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_NEXT;
			break;
		case PREEMPT_TYPE_NONE:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_NONE;
			break;
		case PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE;
			break;
		default:
			break;
		}
		return TtsUtil.speakText(streamType, text, pt, callback);
	}

	/**
	 * 方法名：TTS播报
	 * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
	 * 			与{@link TXZResourceManager#speakTextOnRecordWin(String, boolean, Runnable)}有区别
	 * 			此方法仅为播报，文本不会显示在语音界面
	 *
	 * @param streamType 流类型，填写AudioManager.STREAM常量，默认使用AudioManager.STREAM_ALARM
	 * @param text       播报的文本
	 * @param delay      播报延时，在已经获取到焦点时不会延时
	 * @param type       打断类型
	 * @param callback   播报回调
	 * @return 返回Tts的任务ID
	 */
	public int speakText(int streamType, String text, long delay, PreemptType type,
			ITtsCallback callback) {
		TtsUtil.PreemptType pt = TtsUtil.PreemptType.PREEMPT_TYPE_NONE;
		switch (type) {
		case PREEMPT_TYPE_FLUSH:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_FLUSH;
			break;
		case PREEMPT_TYPE_IMMEADIATELY:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY;
			break;
		case PREEMPT_TYPE_NEXT:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_NEXT;
			break;
		case PREEMPT_TYPE_NONE:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_NONE;
			break;
		case PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE;
			break;
		default:
			break;
		}
		return TtsUtil.speakText(streamType, text, delay, pt, callback);
	}

	/**
	 * 方法名：TTS播报
	 * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
	 * 			与{@link TXZResourceManager#speakTextOnRecordWin(String, boolean, Runnable)}有区别
	 * 			此方法仅为播报，文本不会显示在语音界面
	 *
	 * @param text     播报的文本
	 * @param type     打断类型
	 * @param callback 播报回调
	 * @return 返回Tts的任务ID
	 */
	public int speakText(String text, PreemptType type, ITtsCallback callback) {
		return speakText(TtsUtil.DEFAULT_STREAM_TYPE, text, type, callback);
	}

	/**
	 * 方法名：TTS播报
	 * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
	 * 			与{@link TXZResourceManager#speakTextOnRecordWin(String, boolean, Runnable)}有区别
	 * 			此方法仅为播报，文本不会显示在语音界面
	 *
	 * @param text     播报的文本
	 * @param delay    播报延时，在已经获取到焦点时不会延时
	 * @param type     打断类型
	 * @param callback 播报回调
	 * @return 返回Tts的任务ID
	 */
	public int speakText(String text, long delay, PreemptType type, ITtsCallback callback) {
		return speakText(TtsUtil.DEFAULT_STREAM_TYPE, text, delay, type, callback);
	}


	/**
	 * 方法名：TTS播报
	 * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
	 * 			与{@link TXZResourceManager#speakTextOnRecordWin(String, boolean, Runnable)}有区别
	 * 			此方法仅为播报，文本不会显示在语音界面
	 *
	 * @param text     播报的文本
	 * @param callback 播报回调
	 * @return 返回Tts的任务ID
	 */
	public int speakText(String text, ITtsCallback callback) {
		return speakText(text, PreemptType.PREEMPT_TYPE_NONE, callback);
	}

	/**
	 * 方法名：TTS播报
	 * 方法描述：TTS播报方法，可以详细设置TTS任务细节。
	 * 			与{@link TXZResourceManager#speakTextOnRecordWin(String, boolean, Runnable)}有区别
	 * 			此方法仅为播报，文本不会显示在语音界面
	 *
	 * @param text 播报文本
	 * @return 返回Tts的任务ID
	 */
	public int speakText(String text) {
		return speakText(text, PreemptType.PREEMPT_TYPE_NONE, null);
	}

	/**
	 * 方法名：TTS播报
	 * 方法描述：通过语音内部资源ID播报，若无对应资源ID，则播报文本
	 *
	 * @param resId 资源ID
	 * @param text 播报文本
	 * @return 播报任务ID
	 */
	public int speakRes(String resId,String text) {
		return speakRes(resId, null, text);
	}

	/**
	 * 方法名：TTS播报
	 * 方法描述：通过语音内部资源ID播报，若无对应资源ID，则播报文本
	 *
	 * @param resId   资源ID
	 * @param resArgs 播报的字符串用用来替换占位符的文本
	 * @param text    播报文本
	 * @return 播报任务ID
	 */
	public int speakRes(String resId, String[] resArgs, String text){
		return TtsUtil.speakResource(resId, resArgs, text, null);
	}

	/**
	 * 方法名：TTS播报
	 * 方法描述：通过语音内部资源ID播报，若无对应资源ID，则播报文本
	 *
	 * @param resId   资源ID
	 * @param resArgs 播报的字符串用用来替换占位符的文本
	 * @param text    播报文本
	 * @param type    播报类型
	 * @param onRun   播报任务的回调
	 * @return 播报任务ID
	 */
	public int speakRes(String resId, String[] resArgs, String text, PreemptType type, ITtsCallback onRun) {
		TtsUtil.PreemptType pt = TtsUtil.PreemptType.PREEMPT_TYPE_NONE;
		switch (type) {
		case PREEMPT_TYPE_FLUSH:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_FLUSH;
			break;
		case PREEMPT_TYPE_IMMEADIATELY:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY;
			break;
		case PREEMPT_TYPE_NEXT:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_NEXT;
			break;
		case PREEMPT_TYPE_NONE:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_NONE;
			break;
		case PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE;
			break;
		default:
			break;
		}
		return TtsUtil.speakResource(TtsUtil.DEFAULT_STREAM_TYPE, resId, resArgs, text, pt, onRun);
	}

	/**
	 * 方法名：执行播报任务
	 * 方法描述：用来执行一连串的TTS任务
	 *
	 * @param voiceTasks 播报的具体任务
	 * @param oRun       播报任务的回调
	 * @return 返回任务ID
	 */
	public int speakVoiceTask(VoiceTask[] voiceTasks, final ITtsCallback oRun) {
		return speakVoiceTask(TtsUtil.DEFAULT_STREAM_TYPE, PreemptType.PREEMPT_TYPE_NONE, voiceTasks, oRun);
	}

	/**
	 * 方法名：执行播报任务
	 * 方法描述：用来执行一连串的TTS任务
	 *
	 * @param iStream    播报流
	 * @param voiceTasks 播报的具体任务
	 * @param oRun       播报任务的回调
	 * @return 返回任务ID
	 */
	public int speakVoiceTask(int iStream, VoiceTask[] voiceTasks, final ITtsCallback oRun) {
		return speakVoiceTask(iStream, PreemptType.PREEMPT_TYPE_NONE, voiceTasks, oRun);
	}

	/**
	 * 方法名：执行播报任务
	 * 方法描述：用来执行一连串的TTS任务
	 *
	 * @param bPreempt   播报类型
	 * @param voiceTasks 播报的具体任务
	 * @param oRun       播报任务的回调
	 * @return 返回任务ID
	 */
	public int speakVoiceTask(PreemptType bPreempt, VoiceTask[] voiceTasks, final ITtsCallback oRun) {
		return speakVoiceTask(TtsUtil.DEFAULT_STREAM_TYPE, bPreempt, voiceTasks, oRun);
	}

	/**
	 * 方法名：执行播报任务
	 * 方法描述：用来执行一连串的TTS任务
	 *
	 * @param iStream    播报流
	 * @param type       播报类型
	 * @param voiceTasks 播报的具体任务
	 * @param oRun       播报任务的回调
	 * @return 返回任务ID
	 */
	public int speakVoiceTask(int iStream, PreemptType type, VoiceTask[] voiceTasks, final ITtsCallback oRun) {
		TtsUtil.PreemptType pt = TtsUtil.PreemptType.PREEMPT_TYPE_NONE;
		switch (type) {
		case PREEMPT_TYPE_FLUSH:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_FLUSH;
			break;
		case PREEMPT_TYPE_IMMEADIATELY:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY;
			break;
		case PREEMPT_TYPE_NEXT:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_NEXT;
			break;
		case PREEMPT_TYPE_NONE:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_NONE;
			break;
		case PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE:
			pt = TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE;
			break;
		default:
			break;
		}
		return TtsUtil.speakVoiceTask(iStream, pt, voiceTasks, oRun);
	}

	/**
	 * 方法名：取消播报
	 * 方法描述：根据TTS任务ID，强制取消TTS任务
	 *
	 * @param taskId 需要取消的TTS任务ID
	 */
	public void cancelSpeak(int taskId) {
		TtsUtil.cancelSpeak(taskId);
	}

	private Integer mDefaultAudioStream = null;
	private Integer mVoiceSpeed = null;
	private String mTtsModel = null;
	private Integer mBufferTime = null;
	private String mBeepPath = null;
	private Boolean mEnableDownVolume = null;
	private Boolean mForceShowChoiceView = null;
	private String mReplaceJson = null;

	/**
	 * 方法名：设置TTS播报流
	 * 方法描述：设置默认的TTS播报音频流，使用AudioManager的STREAM常量，默认使用AudioManager.STREAM_ALARM
	 *
	 * @param stream 播报流
	 */
	public void setDefaultAudioStream(int stream) {
		mDefaultAudioStream = stream;
		TtsUtil.DEFAULT_TTS_STREAM = stream;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"comm.config.tts.setDefaultAudioStream",
				("" + stream).getBytes(), null);
	}

	/**
	 * 方法名：设置合成的TTS播放语速
	 * 方法描述：设置合成的TTS播放语速，取值范围1-100,数值越大，语速越快,同行者默认值是70
	 *
	 * @param speed 合成的TTS播放语速
	 */
	public void setVoiceSpeed(int speed) {
		if (speed < 1) {
			speed = 1;
		} else if (speed > 100) {
			speed = 100;
		}

		mVoiceSpeed = speed;
		String cmd = "comm.tts.set.voicespeed";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				("" + speed).toString().getBytes(), null);
		if (TXZConfigManager.getInstance().mInitParam != null) {
			TXZConfigManager.getInstance().mInitParam.setTtsVoiceSpeed(speed);
		}
		ConfigUtil.setVoiceSpeed(speed);
	}

	/**
	 * 方法名：设置TTS模型
	 * 方法描述：设置TTS模型路径，不同的模型音色不一样。TTS模型需要联系同行者对应支持人员获取
	 * 			 ttsModelPath设为 null将使用默认TTS模型
	 *
	 * @param ttsModelPath TTS模型绝对路径
	 */
	public void setTtsModel(String ttsModelPath) {
		if (ttsModelPath == null) {
			ttsModelPath = "";
		}
		mTtsModel = ttsModelPath;
		String cmd = "comm.tts.set.modelrole";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				ttsModelPath.getBytes(), null);
	}

	/**
	 * 方法名：设置语音播报TTS延时
	 * 方法描述：设置语音播报TTS延时，但只有在需要抢焦点时才会延时，已有焦点时不会延时
	 *
	 * @param delay 延时时间，单位ms
	 */
	public void setTtsDelay(long delay) {
		String cmd = "comm.tts.set.ttsdelay";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				(delay+"").toString().getBytes(), null);
	}
	
	/**
	 * 方法名：设置TTS播报的缓冲时间
	 * 方法描述：设置TTS播报的缓冲时间, 取值范围 0 ~ 15000, 默认值为300, 单位毫秒
	 * 			由于TTS是合成音，实际应用中系统会因为防POP音等问题造成TTS掉字，通过此方法规避问题
	 *
	 * @param nTime 缓冲时间，单位ms
	 */
	public void setBufferTime(int nTime) {
		String cmd = "comm.tts.set.buffettime";
		mBufferTime = nTime;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				("" + nTime).toString().getBytes(), null);
	}
	
	/**
	 * 方法名：设置beep资源路径
	 * 方法描述：替换语音默认Beep音，即语音开始录音的“嘟”的一声
	 * 			文件格式要求PCM：采样率16000，单声道，16位，建议不要超过200ms
	 *
	 * @param beepPath beep音文件路径
	 */
	public void setBeepResources(String beepPath) {
		byte[] data = null;
		mBeepPath = beepPath;
		if (!TextUtils.isEmpty(beepPath)) {
			data = mBeepPath.getBytes();
		}
		String cmd = "comm.tts.set.beep";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd, data, null);
	}

	/**
	 * 方法名：替换TTS播报文字
	 * 方法描述：替换TTS播报文字，实际场景中，部分离线多音字声调可能出现错误。通过此方法替换播报
	 * 			如：“单雄信”离线下无法区分是人名，应替换“扇雄信”。同时配合方法{@link TXZAsrManager#setRealFictitiousCmds(String, String...)}以更改语音界面显示文本
	 *
	 * @param original 原始文本数组，根据index对应，
	 * @param replace  替换播报的文本数组，根据index对应
	 */
	public void setReplaceSpeakWord(String[] original, String[] replace) {
		if (null == original || original.length == 0 || null == replace || replace.length == 0) {
			LogUtil.loge("original or replace is empty");
			return;
		}
		if (original.length != replace.length) {
			LogUtil.loge("original.length != replace.length");
			return;
		}
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < original.length; i++) {
			// 替换的原始文本不允许为空，替换文本允许是空串
			if (TextUtils.isEmpty(original[i]) || null == replace[i]) {
				continue;
			}
			JSONObject object = new JSONObject();
			try {
				object.put("original", original[i]);
				object.put("replace", replace[i]);
			} catch (JSONException e) {
				e.printStackTrace();
				LogUtil.loge("set replace word:", e);
				continue;
			}
			jsonArray.put(object);
		}
		if (jsonArray.length() == 0) {
			LogUtil.loge("set replace word invalid argument");
			return;
		}
		setReplaceSpeakWord(jsonArray.toString());
	}
	
	private void setReplaceSpeakWord(String replaceJson) {
		mReplaceJson = replaceJson;
		if (null == mReplaceJson) {
			return;
		}
		String cmd = "comm.tts.set.replaceword";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				mReplaceJson.getBytes(), null);
	}
	
	/**
	 * 方法名：设置导航播报语音时TTS是否降低音量
	 * 方法描述：设置导航播报语音时TTS是否降低音量，默认语音与导航共存，根据需求修改，一般推荐修改导航降低音量
	 *
	 * @param enable 是否降低音量
	 */
	public void enableDownVolumeWhenNav(boolean enable) {
		mEnableDownVolume = enable;
		String cmd = "comm.tts.set.enableDownVolume";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				("" + enable).toString().getBytes(), null);
	}

	/**
	 * 方法名：是否开启TTS主题切换列表显示
	 * 方法描述：切换TTS主题时，是否需要展示切换列表，默认展示，不展示时，直接根据TTS 序列号切换
	 *
	 * @param enable 是否启用列表显示功能
	 */
	public void forceShowTTSChoiceView(boolean enable) {
		mForceShowChoiceView = enable;
		String cmd = "comm.tts.set.forceShowChoiceView";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				("" + enable).toString().getBytes(), null);
	}

	/**
	 * 类名：TTS播报配置选项
	 * 类描述：Tts选项，如音量、语速、语调、语言等，推荐使用TTS任务取代
	 */
	public static class TtsOption {

	}

	/**
	 * 接口名：TTS回调
	 * 接口描述：TTS播报任务监听
	 */
	public static interface TtsCallback {
		/**
		 * 方法名：播报出错
		 * 方法描述：TTS播报任务出错时，回调此方法
		 */
		public void onError();

		/**
		 * 方法名：播报成功
		 * 方法描述：TTS播报成功时，回调此方法
		 */
		public void onSuccess();

		/**
		 * 方法名：播报取消
		 * 方法描述：TTS播报被取消时，回调此方法
		 */
		public void onCancel();
	}

	/**
	 * 接口名：Tts工具实现类
	 * 接口描述：需要使用外部TTS工具时，可以实现此接口
	 */
	public static interface TtsTool {
		/**
		 * 方法名：设置tts通用选项
		 * 方法描述：使用外部TTS工具，语音通知外部更改TTS播报选项
		 *
		 * @param option
		 */
		public void setOption(TtsOption option);

		/**
		 * 方法名：启动tts播报
		 * 方法描述：使用外部TTS工具，语音通知外部启动tts播报
		 *
		 * @param stream   使用的流
		 * @param text     播报的文本
		 * @param callback 播报的回调
		 */
		public void start(int stream, String text, TtsCallback callback);

		/**
		 * 方法名：取消播报
		 * 方法描述：使用外部TTS工具，语音通知外部取消播报
		 */
		public void cancel();
	}

	TtsTool mTtsTool = null;

	/**
	 * 方法名：设置TTS播报工具
	 * 方法描述：当语音TTS播报工具不满足需求时，可以设置外部TTS工具
	 *
	 * @param tool TTS播报工具
	 */
	public void setTtsTool(TtsTool tool) {
		mTtsTool = tool;
		if (tool == null) {
			TXZService.setCommandProcessor("tool.tts.", null);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.tool.tts.clearTool", null, null);
		} else {
			TXZService.setCommandProcessor("tool.tts.", new CommandProcessor() {
				@Override
				public byte[] process(String packageName, String command,
						final byte[] data) {

					if ("start".equals(command)) {
						JSONBuilder json = new JSONBuilder(data);
						int stream = json.getVal("stream", Integer.class,
								TtsUtil.DEFAULT_TTS_STREAM);
						String text = json.getVal("text", String.class);
						LogUtil.logd("tts tool start: stream=" + stream
								+ ", text=" + text);
						mTtsTool.start(stream, text, new TtsCallback() {
							@Override
							public void onSuccess() {
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.tts.onSuccess", data, null);
							}

							@Override
							public void onError() {
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.tts.onError", data, null);
							}

							@Override
							public void onCancel() {
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.tts.onCancel", data, null);
							}
						});
						return null;
					}

					if ("cancel".equals(command)) {
						LogUtil.logd("tts tool cancel");
						mTtsTool.cancel();
						return null;
					}

					if ("setOption".equals(command)) {
						TtsOption option = new TtsOption();
						mTtsTool.setOption(option);
						return null;
					}
					return null;
				}
			});

			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.tool.tts.setTool", null, null);
		}
	}

	/**
	 * 方法名：设置TTS主题
	 * 方法描述：手动切换TTS主题，需要提前获取TTS主题实例
	 *
	 * @param ttsTheme TTS主题
	 */
	public void setTtsThmeme(TtsTheme ttsTheme) {
		String cmd = "comm.tts.set.ttstheme";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("themeid", ttsTheme.mThemeId);
			jsonObject.put("themename", ttsTheme.mThemeName);
		} catch (JSONException e) {
			LogUtil.logw(e.getMessage());
			e.printStackTrace();
			return;
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd,
				jsonObject.toString().getBytes(), null);
	}

	private onChangeThemeListener mThemeListener;

	/**
	 * 方法名：设置TTS主题更新监听器
	 * 方法描述：设置TTS主题更新监听器，其它因素切换主题时回调此监听器
	 *
	 * @param listener TTS主题更新监听器
	 */
	public void setChangeThemeListener(onChangeThemeListener listener){
		mThemeListener = listener;
		if(listener == null){
			TXZService.setCommandProcessor("theme.tts.", null);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"comm.tts.clear.themeChangeListener", null, null);
		}else{
			TXZService.setCommandProcessor("theme.tts.", new CommandProcessor() {
				@Override
				public byte[] process(String packageName, String command, byte[] data) {
					if ("change".equals(command)) {
						JSONBuilder jsonBuilder = new JSONBuilder(data);
						String themeName = jsonBuilder.getVal("themeName",String.class);
						int themeId = jsonBuilder.getVal("themeId",int.class);
						TtsTheme ttsTheme = new TtsTheme();
						ttsTheme.mThemeId = themeId;
						ttsTheme.mThemeName = themeName;
						if (mThemeListener != null) {
							mThemeListener.changeSuccess(ttsTheme);
						}
					}
					return null;
				}
			});
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"comm.tts.set.themeChangeListener", null, null);
		}
	}

	/**
	 * 接口名：TTS主题更新监听器
	 * 接口描述：TTS主题更新时回调相关方法
	 */
	public static interface onChangeThemeListener {
		/**
		 * 方法名：TTS主题切换成功
		 * 方法描述：TTS主题切换成功提供切换后的主题
		 */
		public void changeSuccess(TtsTheme ttsTheme);
	}


	/**
	 * 方法名：获取当前TTS主题列表
	 * 方法描述：主动获取当前系统支持的同行者语音主题
	 *
	 * @return TTS主题列表
	 */
	public TtsTheme[] getTtsThemes() {
		String cmd = "comm.tts.getTtsThemes";
		ServiceData data = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, cmd,null);
		if (data == null) {
			return null;
		}
		return TtsTheme.pasreJson(data.getString());
	}

	/**
	 * 类名：TTS主题类
	 * 类描述：语音TTS主题抽象化类
	 */
	public static class TtsTheme {
		/**
		 * 变量名：TTS主题ID
		 * 变量描述：TTS主题ID，由同行者定义
		 */
		public int mThemeId;
		/**
		 * 变量名：TTS主题名称
		 * 变量描述：TTS主题名称，由同行者定义
		 */
		public String mThemeName;
		/**
		 * 变量名：当前主题是否使用中
		 * 变量描述：当前主题是否使用中
		 */
		public boolean isUsed = false;
		
		public static JSONArray toJsonArray(TtsTheme[] ttsThemes) {
			if (ttsThemes == null) {
				return null;
			}
			JSONArray jsonArray = new JSONArray();
			for (TtsTheme ttsTheme : ttsThemes) {
				if (ttsTheme != null) {
					jsonArray.put(ttsTheme.toJsonObject());
				}
			}
			return jsonArray;
		}
		
		private JSONObject toJsonObject() {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("themeId", this.mThemeId);
				jsonObject.put("themeName", this.mThemeName);
				jsonObject.put("isUsed", isUsed);
			} catch (JSONException e) {
				LogUtil.logw(e.getMessage());
				e.printStackTrace();
			}
			return jsonObject;
		}
		
		private static TtsTheme[] pasreJson(String json) {
			if (TextUtils.isEmpty(json)) {
				return null;
			}
			JSONArray jsonArray = null;
			try {
				jsonArray = new JSONArray(json);
			} catch (JSONException e) {
				jsonArray = null;
				LogUtil.logw(e.getMessage());
				e.printStackTrace();
			}
			return parseJsonArray(jsonArray);
		}
		
		private static TtsTheme[] parseJsonArray(JSONArray jsonArray) {
			if (jsonArray == null) {
				return null;
			}
			int len = jsonArray.length();
			TtsTheme[] ttsThemes = new TtsTheme[len];
			for (int i = 0; i < len; i++) {
				ttsThemes[i] = TtsTheme.parseJsonObject(jsonArray.optJSONObject(i));
			}
			return ttsThemes;
		}
		
		private static TtsTheme parseJsonObject(JSONObject jsonObject) {
			if (jsonObject == null) {
				return null;
			}
			TtsTheme ttsTheme = new TtsTheme();
			ttsTheme.mThemeId = jsonObject.optInt("themeId");
			ttsTheme.mThemeName = jsonObject.optString("themeName");
			ttsTheme.isUsed = jsonObject.optBoolean("isUsed");
			return ttsTheme;
		}
	}
}
