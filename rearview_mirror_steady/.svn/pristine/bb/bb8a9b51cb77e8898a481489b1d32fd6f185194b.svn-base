package com.txznet.sdk;

import android.media.AudioManager;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.StatusUtil;
import com.txznet.comm.remote.util.StatusUtil.GetStatusCallback;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZService.CommandProcessor;

/**
 * 状态管理器
 *
 */
public class TXZStatusManager {
	private static TXZStatusManager sInstance = new TXZStatusManager();

	private TXZStatusManager() {

	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static TXZStatusManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		if (mAudioLogicWhenAsr != null)
			setAudioLogicWhenAsr(mAudioLogicWhenAsr);
		if (mAudioLogicWhenTts != null)
			setAudioLogicWhenTts(mAudioLogicWhenTts);
		if (mAudioLogicWhenCall != null)
			setAudioLogicWhenCall(mAudioLogicWhenCall);
		if (mAudioFocusStreamType != null)
			setAudioFocusStreamType(mAudioFocusStreamType);
		if (hasSetAudioFocus) {
			setAudioFocusLogic(mRunnableRequestAudioFocus,
					mRunnableAbandonAudioFocus);
		}
		
		StatusUtil.getStatus(new GetStatusCallback() {
			@Override
			public void onGet() {
			}
		});
	}

	/**
	 * 声控模块是否繁忙
	 * 
	 * @return 声控模块是否繁忙
	 */
	public boolean isAsrBusy() {
		return StatusUtil.isAsrBusy();
	}

	/**
	 * 语音播报模块是否繁忙
	 * 
	 * @return 语音播报模块是否繁忙
	 */
	public boolean isTtsBusy() {
		return StatusUtil.isTtsBusy();
	}

	/**
	 * 电话模块是否繁忙
	 * 
	 * @return 电话模块是否繁忙
	 */
	public boolean isCallBusy() {
		return StatusUtil.isCallBusy();
	}

	/**
	 * 音乐是否正在播放，同TXZMusicManager.getInstance().isPlaying();
	 * 
	 * @return 音乐是否正在播放
	 */
	public boolean isMusicPlaying() {
		return TXZMusicManager.getInstance().isPlaying();
	}
	
	/**
	 * 录音窗口是否在显示
	 * 
	 * @return 录音窗口是否正在显示
	 */
	public boolean isRecordUIShowed() {
		byte[] data = ServiceManager.getInstance().sendTXZInvokeSync(
				"txz.record.ui.status.isShowing", null);
		boolean bRet = false;
		if (data == null) {
			return false;
		}
		try {
			bRet = Boolean.parseBoolean(new String(data));
		} catch (Exception e) {

		}
		return bRet;
	}

	/**
	 * 状态监听器
	 *
	 */
	public static interface StatusListener extends StatusUtil.StatusListener {
		/**
		 * 识别开始
		 */
		@Override
		public void onBeginAsr();

		/**
		 * 电话开始
		 */
		@Override
		public void onBeginCall();

		/**
		 * 语音播放
		 */
		@Override
		public void onBeginTts();

		/**
		 * 声控提示音结束
		 */
		@Override
		public void onBeepEnd();

		/**
		 * 结束识别
		 */
		@Override
		public void onEndAsr();

		/**
		 * 结束电话
		 */
		@Override
		public void onEndCall();

		/**
		 * 结束语音播报
		 */
		@Override
		public void onEndTts();

		/**
		 * 音乐播放暂停，临时暂停不触发
		 */
		@Override
		public void onMusicPause();

		/**
		 * 音乐播放开始
		 */
		@Override
		public void onMusicPlay();
	}

	/**
	 * 添加状态监听器
	 * 
	 * @param listener
	 *            监听器对象
	 */
	public void addStatusListener(StatusListener listener) {
		StatusUtil.addStatusListener(listener);
	}

	/**
	 * 删除状态监听器
	 * 
	 * @param listener
	 *            监听器对象
	 */
	public void removeStatusListener(StatusListener listener) {
		StatusUtil.removeStatusListener(listener);
	}

	/**
	 * 媒体状态逻辑类型
	 */
	public enum AudioLogicType {
		/**
		 * 不修改音频状态
		 */
		AUDIO_LOGIC_NONE,
		/**
		 * 降低音量
		 */
		AUDIO_LOGIC_DUCK,
		/**
		 * 暂停音乐
		 */
		AUDIO_LOGIC_PAUSE,
		/**
		 * 停止音乐
		 */
		AUDIO_LOGIC_STOP,
		/**
		 * 音乐不暂停，只静音，暂时不支持
		 */
		AUDIO_LOGIC_MUTE,
	}

	AudioLogicType mAudioLogicWhenAsr = null;

	/**
	 * 声控下音频状态逻辑设置，默认暂停
	 */
	public void setAudioLogicWhenAsr(AudioLogicType logic) {
		mAudioLogicWhenAsr = logic;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.audioLogic.asr", logic.name().getBytes(), null);
	}

	AudioLogicType mAudioLogicWhenTts = null;

	/**
	 * 语音播报下音频状态逻辑设置，默认暂停
	 */
	public void setAudioLogicWhenTts(AudioLogicType logic) {
		mAudioLogicWhenTts = logic;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.audioLogic.tts", logic.name().getBytes(), null);
	}

	AudioLogicType mAudioLogicWhenCall = null;

	/**
	 * 电话下音频状态逻辑设置，默认暂停
	 */
	public void setAudioLogicWhenCall(AudioLogicType logic) {
		mAudioLogicWhenCall = logic;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.audioLogic.call", logic.name().getBytes(), null);
	}

	Integer mAudioFocusStreamType = null;

	/**
	 * 设置抢占的音频焦点流
	 */
	public void setAudioFocusStreamType(int stream) {
		mAudioFocusStreamType = stream;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.setAudioFocusStreamType", ("" + stream).getBytes(),
				null);
	}

	Runnable mRunnableRequestAudioFocus = null;
	Runnable mRunnableAbandonAudioFocus = null;

	/**
	 * 设置音频焦点抢占逻辑
	 * 
	 * @param onRequestAudioFocus
	 *            需要音频焦点时执行的逻辑
	 * @param onAbandonAudioFocus
	 *            需要释放焦点时执行的逻辑
	 */
	boolean hasSetAudioFocus = false;
	public void setAudioFocusLogic(Runnable onRequestAudioFocus,
			Runnable onAbandonAudioFocus) {
		hasSetAudioFocus = true;
		mRunnableRequestAudioFocus = onRequestAudioFocus;
		mRunnableAbandonAudioFocus = onAbandonAudioFocus;
		if (mRunnableRequestAudioFocus != null
				&& mRunnableAbandonAudioFocus != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.music.setAudioFocusLogic", null, null);
			TXZService.setCommandProcessor("status.focus.",
					new CommandProcessor() {
						@Override
						public byte[] process(String packageName,
								String command, byte[] data) {
							if ("RequestAudioFocus".equals(command)) {
								if (mRunnableRequestAudioFocus != null) {
									mRunnableRequestAudioFocus.run();
								}
								return null;
							}
							if ("AbandonAudioFocus".equals(command)) {
								if (mRunnableAbandonAudioFocus != null) {
									mRunnableAbandonAudioFocus.run();
								}
								return null;
							}
							return null;
						}
					});
		} else {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.music.clearAudioFocusLogic", null, null);
		}
	}

    /**
     * 通知同行者当前的焦点状态
     *
     * @param focusChange 音频焦点的状态
     *                    参考
     *                    {@link AudioManager#AUDIOFOCUS_GAIN},
     *                    {@link AudioManager#AUDIOFOCUS_LOSS},
     *                    {@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT}
     *                    {@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK}.
     */
    public void notifyAudioFocusChange(int focusChange) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("focusChange", focusChange);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.notifyAudioFocusChange", jsonBuilder.toBytes(), null);
    }
}
