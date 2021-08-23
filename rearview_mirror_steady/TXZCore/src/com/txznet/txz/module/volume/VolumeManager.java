package com.txznet.txz.module.volume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Vibrator;
import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZSysManager.VolumeSettingCallBack;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.sys.SysTool;

/**
 * 音量管理模块，负责音量适配，解决音量异常问题
 * 
 * @author bihongpi
 *
 */
public class VolumeManager extends IModule {
	static VolumeManager sModuleInstance = null;

	private VolumeManager() {
		mAudioManager = (AudioManager) GlobalContext.get().getSystemService(
				Context.AUDIO_SERVICE);
		AudioServiceAdapter.adapter();
		registerVolumeReceiver();
	}

	public static VolumeManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (VolumeManager.class) {
				if (sModuleInstance == null)
					sModuleInstance = new VolumeManager();
			}
		}
		return sModuleInstance;
	}

	private void registerVolumeReceiver() {
		BroadcastReceiver mVolReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(
						"android.media.VOLUME_CHANGED_ACTION")) {
					int streamType = intent.getIntExtra(
							"android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
					if (streamType == AudioManager.STREAM_MUSIC) {
						onSystemVolChange();
					}
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction("android.media.VOLUME_CHANGED_ACTION");
		GlobalContext.get().registerReceiver(mVolReceiver, filter);
	}

	// /////////////////////////////////////////////////////////////////////

	AudioManager mAudioManager;

	public void checkVolume(int streamtype, boolean toast, boolean vibrate) {
		int vol = mAudioManager.getStreamVolume(streamtype);
		int maxVol = mAudioManager.getStreamMaxVolume(streamtype);
		float f = vol / (float) maxVol;
		if (f < 0.3) {
			JNIHelper.logd("checkVolume: " + streamtype + "=[" + vol + "/"
					+ maxVol + "]");
			if (toast) {
				// TXZApp.showToast("当前音量很小");
				// 卡尔威需求:TTS莫名其妙就没有声音了
				if (mZeroVolToast && f < 0.1) {
					AppLogic.showToast("当前音量很小");
					mAudioManager.setStreamVolume(streamtype, maxVol, 0);
				}
			}
			// 老逻辑 没有必要 删除
			/*if (vibrate) {
				Vibrator vibrator = (Vibrator) GlobalContext.get()
						.getSystemService(Context.VIBRATOR_SERVICE);
				if (vibrator != null) {
					long[] pattern = { 100, 400, 100, 400 }; // 停止 开启 停止 开启
					try {
						vibrator.vibrate(pattern, -1); // 重复两次上面的pattern
						// 如果只想震动一次，index设为-1
					} catch (Exception e) {
						JNIHelper.loge(e.toString());
					}
				}
			}*/
		}
	}

	private boolean mZeroVolToast = false;

	public void enableZeroVolToast(boolean enable) {
		mZeroVolToast = enable;
	}

	/**
	 * 增加音量
	 * 
	 * @return 没有达到最大音量则返回true
	 * @author MarvinYang
	 * 2017/6/6 返回值意义修改
	 *  如果三方工具处理则true
	 * 如果默认处理 若音量未达到最大值则true 若达到音量最大值则返回false 
	 */
	public boolean incVolume(final VolumeSettingCallBack callback) {
		if (SysTool.procByRemoteTool(SysTool.VOLUME, "incVolume")) {
			ServiceManager.getInstance().sendInvoke(SysTool.getRemoteVolumeTool(), SysTool.VOLUME + "." + "isMaxVolume", null, new GetDataCallback() {
				
				@Override
				public void onGetInvokeResponse(ServiceData data) {
					// 回调为空，调用程序调用错误
					if(callback == null){
						LogUtil.logd("incVolume onGetInvokeResponse callback = null");
						return;
					}
					// data为空，情况：各种奇葩错误,其它错误如适配程序无限dead或被卸载
					if(data == null){
						if (isTimeout()){
							callback.onError(-1);
							LogUtil.logd("incVolume onGetInvokeResponse time out ,code = -1");
							return;
						} else {
							callback.onError(-2);
							LogUtil.logd("incVolume failed onGetInvokeResponse data = null,code = -2");
							return;
						}
					}
					// data不为空，但数据为空，情况：超时、适配程序中SDK版本不支持最大音量
					if(data.getBytes() == null){
						callback.onOperateResult(false);// 兼容老SDK
						return;
					}
					//Core未返回值？
					Boolean isMaxVolume = data.getBoolean();
					if(isMaxVolume == null){
						callback.onOperateResult(false);// 兼容老SDK
						return;
					}
					callback.onOperateResult(isMaxVolume);
				}
			});
			return true;
		}
		AudioServiceAdapter.enableVolumeControlTemp(true);
		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
		AudioServiceAdapter.enableVolumeControlTemp(false);
		if (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) <= mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC)) {
			return false;
		}
		return true;
	}

	public boolean decVolume(final VolumeSettingCallBack callback) {
		if (SysTool.procByRemoteTool(SysTool.VOLUME, "decVolume")) {
			ServiceManager.getInstance().sendInvoke(SysTool.getRemoteVolumeTool(),SysTool.VOLUME + "." + "isMinVolume", null,new GetDataCallback() {

						@Override
						public void onGetInvokeResponse(ServiceData data) {
							// 回调为空，调用程序调用错误
							if (callback == null) {
								LogUtil.logd("incVolume onGetInvokeResponse callback = null");
								return;
							}
							// data为空，情况：数据调用失败
							if (data == null) {
								if (isTimeout()){
									callback.onError(-1);;// 超时
									LogUtil.logd("incVolume onGetInvokeResponse : time is out ,code = -1");
									return;
								}else{
									callback.onError(-2);;// 彻底失败
									LogUtil.logd("incVolume failed onGetInvokeResponse data = null,code = -2");
									return;
								}
							}
							// data不为空，但数据为空，情况：超时、适配程序中SDK版本不支持最大音量
							if (data.getBytes() == null) {
								callback.onOperateResult(false);//兼容老版本
								return;
							}
							//Core未返回值？
							Boolean isMinVolume = data.getBoolean();
							if(isMinVolume == null){
								callback.onOperateResult(false);// 正常情况到这一定会有，保险
								return;
							}
							callback.onOperateResult(isMinVolume);
						}
					});
			return true;
		}
		AudioServiceAdapter.enableVolumeControlTemp(true);
		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
		AudioServiceAdapter.enableVolumeControlTemp(false);
		if (0 >= mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) {
			return false;
		}
		return true;
	}

	public boolean decVolume(final VolumeSettingCallBack callback,String data) {
		if (!SysTool.hasRemoteVolumeTool()) {
			if(callback == null){
				LogUtil.logd("decVolume RemoteVolumeTool == null callback = null");
				return false;
			}
			callback.onError(-3);
			LogUtil.logd("decVolume failed RemoteVolumeTool == null");
			return  true;
		}
		ServiceManager.getInstance().sendInvoke(SysTool.getRemoteVolumeTool(), SysTool.VOLUME + "." + "decVolume", data.getBytes(), new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(ServiceData data) {
				// 回调为空，调用程序调用错误
				if(callback == null){
					LogUtil.logd("decVolume onGetInvokeResponse callback = null");
					return;
				}
				// data为空，情况：各种奇葩错误,其它错误如适配程序无限dead或被卸载
				if(data == null){
					if (isTimeout()){
						callback.onError(-1);
						LogUtil.logd("decVolume onGetInvokeResponse time out ,code = -1");
					} else {
						callback.onError(-2);
						LogUtil.logd("decVolume failed onGetInvokeResponse data = null,code = -2");
					}
					return;
				}
				// data不为空，但数据为空，情况：超时、适配程序中SDK版本不支持最大音量
				if(data.getBytes() == null){
					callback.onOperateResult(false);// 兼容老SDK
					return;
				}
				//Core未返回值？
				Boolean isOperate = data.getBoolean();
				if(isOperate == null){
					callback.onOperateResult(false);// 兼容老SDK
					return;
				}
				callback.onOperateResult(isOperate);
			}
		});
		return true;
	}

	public boolean incVolume(final VolumeSettingCallBack callback,String data) {
		if (!SysTool.hasRemoteVolumeTool()) {
			if(callback == null){
				LogUtil.logd("incVolume RemoteVolumeTool == null callback = null");
				return false;
			}
			callback.onError(-3);
			LogUtil.logd("incVolume failed RemoteVolumeTool == null");
			return  true;
		}
		ServiceManager.getInstance().sendInvoke(SysTool.getRemoteVolumeTool(), SysTool.VOLUME + "." + "incVolume", data.getBytes(), new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(ServiceData data) {
				// 回调为空，调用程序调用错误
				if(callback == null){
					LogUtil.logd("incVolume onGetInvokeResponse callback = null");
					return;
				}
				// data为空，情况：各种奇葩错误,其它错误如适配程序无限dead或被卸载
				if(data == null){
					if (isTimeout()){
						callback.onError(-1);
						LogUtil.logd("incVolume onGetInvokeResponse time out ,code = -1");
					} else {
						callback.onError(-2);
						LogUtil.logd("incVolume failed onGetInvokeResponse data = null,code = -2");
					}
					return;
				}
				// data不为空，但数据为空，情况：超时、适配程序中SDK版本不支持最大音量
				if(data.getBytes() == null){
					callback.onOperateResult(false);// 兼容老SDK
					return;
				}
				//Core未返回值？
				Boolean isOperate = data.getBoolean();
				if(isOperate == null){
					callback.onOperateResult(false);// 兼容老SDK
					return;
				}
				callback.onOperateResult(isOperate);
			}
		});
		return true;
	}

	public boolean setVolume(final VolumeSettingCallBack callback,String data) {
		if (!SysTool.hasRemoteVolumeTool()) {
			if(callback == null){
				LogUtil.logd("setVolume RemoteVolumeTool == null callback = null");
				return false;
			}
			callback.onError(-3);
			LogUtil.logd("setVolume failed RemoteVolumeTool == null");
			return  true;
		}
		ServiceManager.getInstance().sendInvoke(SysTool.getRemoteVolumeTool(), SysTool.VOLUME + "." + "setVolume", data.getBytes(), new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(ServiceData data) {
				// 回调为空，调用程序调用错误
				if(callback == null){
					LogUtil.logd("incVolume onGetInvokeResponse callback = null");
					return;
				}
				// data为空，情况：各种奇葩错误,其它错误如适配程序无限dead或被卸载
				if(data == null){
					if (isTimeout()){
						callback.onError(-1);
						LogUtil.logd("incVolume onGetInvokeResponse time out ,code = -1");
					} else {
						callback.onError(-2);
						LogUtil.logd("incVolume failed onGetInvokeResponse data = null,code = -2");

					}
					return;
				}
				// data不为空，但数据为空，情况：超时、适配程序中SDK版本不支持最大音量
				if(data.getBytes() == null){
					callback.onOperateResult(false);// 兼容老SDK
					return;
				}
				//Core未返回值？
				Boolean isOperate = data.getBoolean();
				if(isOperate == null){
					callback.onOperateResult(false);// 兼容老SDK
					return;
				}
				callback.onOperateResult(isOperate);
			}
		});
		return true;
	}

	public void maxVolume() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.maxVolume", null, null);
		if (SysTool.procByRemoteTool(SysTool.VOLUME, "maxVolume")) {
			return;
		}

		AudioServiceAdapter.enableVolumeControlTemp(true);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
				AudioManager.FLAG_PLAY_SOUND);
		AudioServiceAdapter.enableVolumeControlTemp(false);
	}

	public void minVolume() {
		if (SysTool.procByRemoteTool(SysTool.VOLUME, "minVolume")) {
			return;
		}

		AudioServiceAdapter.enableVolumeControlTemp(true);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
				AudioManager.FLAG_PLAY_SOUND);
		AudioServiceAdapter.enableVolumeControlTemp(false);
	}

	boolean mLastMuteFlag = false;

	public void mute(boolean b) {
		if (SysTool.procByRemoteTool(SysTool.VOLUME, "mute", b)) {
			return;
		}

		if (b) {
			enableMute();
		} else {
			disableMute();
		}
	}

	private void enableMute() {
		AudioServiceAdapter.enableVolumeControlTemp(true);
		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
		mLastMuteFlag = true;
		AudioServiceAdapter.enableVolumeControlTemp(false);
	}

	private void disableMute() {
		AudioServiceAdapter.enableVolumeControlTemp(true);
		for (int i = 0; i < 5; i++) {
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
		}
		mLastMuteFlag = false;
		AudioServiceAdapter.enableVolumeControlTemp(false);
	}

	private void muteMusicStream(boolean b) {
		int vol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		LogUtil.logd("muteMusicStream=" + b + "/" + vol);
		// 如果已经静音了，不要再设置静音了。因为设置静音和取消静音需要成对出现，不然有可能打不开声音了。
		if (b && (0 == vol)) {
			return;
		}
		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, b);
	}

	boolean bMuteAll = false;

	private synchronized void onSystemVolChange() {
		// AppID为空时
		if (ProjectCfg.getYunzhishengAppId() == null
				|| ProjectCfg.getYunzhishengSecret() == null
				|| ProjectCfg.getYunzhishengAppId().isEmpty()
				|| ProjectCfg.getYunzhishengSecret().isEmpty()) {
			JNIHelper.logd("AppId is empty, do not onSystemVolChange");
			return;
		}
		// 如果音量由第三方控制，那么就不需要去考虑声控静音后，手动调节音量无法生效的问题。(HDIT)
		// 第三方应该自己考虑这个问题
		if (SysTool.hasRemoteVolumeTool()) {
			return;
		}

		// 如果系统被TXZCore静音了,当外界调节音量时，先取消静音
		// 如果正在使用声控，则不允许恢复静音
		if (!bMuteAll) {
			JNIHelper.logd("volumeAction unMute");
			mute(false);
		}
	}

	public synchronized void muteAll(boolean b) {
		// AppID为空时
		if (ProjectCfg.getYunzhishengAppId() == null
				|| ProjectCfg.getYunzhishengSecret() == null
				|| ProjectCfg.getYunzhishengAppId().isEmpty()
				|| ProjectCfg.getYunzhishengSecret().isEmpty()) {
			JNIHelper.logd("AppId is empty, do not muteAll");
			return;
	}
		JNIHelper.logd("muteAll enter: " + b + "/" + bMuteAll);

		// 第三方处理
		if (b) {
			if (SysTool.procByRemoteTool(SysTool.MUTEALL, "mute")) {
				return;
}
		} else {
			if (SysTool.procByRemoteTool(SysTool.MUTEALL, "unmute")) {
				return;
			}
		}

		JNIHelper.logd("muteAll2 enter: " + b + "/" + bMuteAll);
		//新增标志位，解决益光导航时静音被解除的问题
		UiEquipment.ServerConfig config = ConfigManager.getInstance().getServerConfig();
		if (config == null || config.uint64Flags == null ||
				(config.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_SILENT_MUTE) == 0) {
			JNIHelper.logd("muteAll2 default::" + (bMuteAll && b));
			//如果已经是静音，那么直接返回
			if (bMuteAll && b) {
				return ;
			}
		}else{
			//新增标志位
			JNIHelper.logd("muteAll2 new Identification Force::" + (bMuteAll == b));
			if (bMuteAll == b) {
				return;
			}
		}

		/*// 如果已经是静音，那么直接返回
		if (bMuteAll && b) {
			return;
		}*/

		bMuteAll = b;

		AudioServiceAdapter.enableVolumeControlTemp(true);
		if (!b) {
			muteMusicStream(mLastMuteFlag);
		} else {
			muteMusicStream(true);
		}
		mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, b);
		mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, b);
		mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, b);
		mAudioManager.setStreamMute(AudioManager.STREAM_RING, b);
		mAudioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, b);
		AudioServiceAdapter.enableVolumeControlTemp(false);
	}
}
