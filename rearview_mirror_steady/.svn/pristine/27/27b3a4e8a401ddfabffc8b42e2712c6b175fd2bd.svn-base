package com.txznet.txz.module.music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.audio.UiAudio;
import com.txz.ui.event.UiEvent;
import com.txz.ui.music.UiMusic;
import com.txz.ui.music.UiMusic.MediaCategoryList;
import com.txz.ui.music.UiMusic.MediaItem;
import com.txz.ui.music.UiMusic.MediaList;
import com.txz.ui.music.UiMusic.MediaModel;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ProtoBufferUtil;
import com.txznet.comm.version.VersionPreference;
import com.txznet.loader.AppLogic;
import com.txznet.record.ui.WinRecord;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.sdk.TXZStatusManager.AudioLogicType;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr.IAsrCallback;
import com.txznet.txz.component.audio.txz.AudioImpl;
import com.txznet.txz.component.music.IMusic;
import com.txznet.txz.component.music.ITxzMedia;
import com.txznet.txz.component.music.kaola.MusicKaolaImpl;
import com.txznet.txz.component.music.kuwo.MusicKuwoImpl;
import com.txznet.txz.component.music.txz.AudioTxzImpl;
import com.txznet.txz.component.music.txz.MusicTxzImpl;
import com.txznet.txz.component.selector.SelectorHelper;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.music.bean.AudioShowData;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.tts.TtsManager.TtsTask;
import com.txznet.txz.module.volume.AudioServiceAdapter;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.MediaControlUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.text.TextUtils;
import android.util.Log;

/**
 * 音乐管理器，负责音乐逻辑处理及事件处理
 * 
 * @author bihongpi
 *
 */
public class MusicManager extends IModule {

	static MusicManager sModuleInstance = new MusicManager();

	Runnable mRunnableRefreshMediaList = new Runnable() {
		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(mRunnableRefreshMediaList);
			JNIHelper.logd("begin refresh media list");
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_NEED_REFRESH_MEDIA_LIST);
			AndroidMediaLibrary.refreshSystemMedia();
		}
	};

	BroadcastReceiver mRecviceSdcardEvent = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			JNIHelper.logd("recive sdcard event: " + intent.getAction());
			AppLogic.removeBackGroundCallback(mRunnableRefreshMediaList);
			AppLogic.runOnBackGround(mRunnableRefreshMediaList, 2000);
		}
	};

	private MusicManager() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.get.version", null, new GetDataCallback() {

					@Override
					public int getTimeout() {
						return 30 * 1000;
					}

					@Override
					public void onGetInvokeResponse(ServiceData data) {
						if (null != data) {
							AudioTxzImpl.newVersion = data.getBoolean();
						} else {
							AudioTxzImpl.newVersion = false;
						}
						LogUtil.logd("AudioTxzImpl.newVersion：："
								+ AudioTxzImpl.newVersion);
						// if (AudioTxzImpl.isNewVersion()) {
						// mMusicTxzImpl = new AudioTxzImpl();
						// } else {
						// mMusicTxzImpl = new MusicTxzImpl();
						// }
						// mMusicInnerToolMap.put(ServiceManager.MUSIC,
						// mMusicTxzImpl);
					}
				});
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_SHARED);// 如果SDCard未安装,并通过USB大容量存储共享返回
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);// 表明sd对象是存在并具有读/写权限
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// SDCard已卸掉,如果SDCard是存在但没有被安装
		filter.addAction(Intent.ACTION_MEDIA_CHECKING); // 表明对象正在磁盘检查
		filter.addAction(Intent.ACTION_MEDIA_EJECT); // 物理的拔出 SDCARD
		filter.addAction(Intent.ACTION_MEDIA_REMOVED); // 完全拔出
		filter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
		GlobalContext.get().registerReceiver(mRecviceSdcardEvent, filter);

		mMusicInnerToolMap.put(MusicKuwoImpl.PACKAGE_NAME, new MusicKuwoImpl());
		mMusicInnerToolMap.put(MusicKaolaImpl.PACKAGE_NAME,
				new MusicKaolaImpl());
		LogUtil.logd("AudioTxzImpl.newVersion::init()"
				+ AudioTxzImpl.isNewVersion());
		if (AudioTxzImpl.isNewVersion()) {
			mMusicTxzImpl = new AudioTxzImpl();
		} else {
			mMusicTxzImpl = new MusicTxzImpl();
		}
		mMusicInnerToolMap.put(ServiceManager.MUSIC, mMusicTxzImpl);
	}

	IMusic mMusicTxzImpl;

	public static MusicManager getInstance() {
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////

	MediaModel mSearchMediaModel = null;
	boolean mSearchMoreResult = false;

	public void cancelSearchMedia() {
		mSearchMediaModel = null;
		mSearchMoreResult = false;

		// 取消其它音乐的搜索
		for (String musicKey : mMusicInnerToolMap.keySet()) {
			IMusic m = mMusicInnerToolMap.get(musicKey);
			if (m != null
					&& PackageManager.getInstance().checkAppExist(
							m.getPackageName())) {
				m.cancelRequest();
			}
		}
	}

	public void searchMoreMedia(MediaModel model) {
		mSearchMediaModel = model;
		mSearchMoreResult = true;
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE, model);
	}

	// /////////////////////////////////////////////////////////////////////

	public void updateMusicList(byte[] path) {
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, 0x10, path);
	}

	/**
	 * 是否正在播放音乐
	 * 
	 * @return
	 */
	public boolean isPlaying() {
		if (!ProjectCfg.isFixCallFunction()) {
			if (!TextUtils.isEmpty(mMusicToolServiceName)) {
				return mRemoteMusicToolisPlaying;
			}
		}
		IMusic musicTool = getMusicTool();
		if (musicTool != null) {
			return musicTool.isPlaying();
		}
		return false;
	}

	/**
	 * 是否正在缓冲中
	 * 
	 * @return
	 */
	public boolean isBuffering() {
		IMusic musicTool = getMusicTool();
		if (musicTool != null) {
			return musicTool.isBuffering();
		}
		return false;
	}

	// /////////////////////////////////////////////////////////////////////

	AudioLogicType mAudioLogicWhenTts = AudioLogicType.AUDIO_LOGIC_PAUSE;
	AudioLogicType mAudioLogicWhenAsr = AudioLogicType.AUDIO_LOGIC_PAUSE;
	AudioLogicType mAudioLogicWhenCall = AudioLogicType.AUDIO_LOGIC_PAUSE;

	private boolean requestAudioFocus(AudioLogicType type) {
		if (!TextUtils.isEmpty(mAudioFocusLogicService)) {
			ServiceManager.getInstance().sendInvoke(mAudioFocusLogicService,
					"status.focus.RequestAudioFocus", null, null);
			return true;
		}
		boolean ret = requestAudioFocus(type, afChangeListener,
				mAudioFocusStreamType == null ? AudioManager.STREAM_MUSIC
						: mAudioFocusStreamType);

		if (ret == false) {
			JNIHelper.logw("request audio focus failed");
			onLostAudioFocus();
		}

		return ret;
	}

	private boolean requestAudioFocus(AudioLogicType type,
			OnAudioFocusChangeListener listener, int stream) {
		boolean ret = true;
		switch (type) {
		case AUDIO_LOGIC_NONE:
			break;
		case AUDIO_LOGIC_DUCK:
			AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
			AudioServiceAdapter.enableFocusControlTemp(true);
			ret = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAm
					.requestAudioFocus(listener, stream,
							AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
			AudioServiceAdapter.enableFocusControlTemp(false);
			return ret;
		case AUDIO_LOGIC_PAUSE:
			AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
			AudioServiceAdapter.enableFocusControlTemp(true);
			ret = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAm
					.requestAudioFocus(listener, stream,
							AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			AudioServiceAdapter.enableFocusControlTemp(false);
			return ret;
		case AUDIO_LOGIC_STOP:
			AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
			AudioServiceAdapter.enableFocusControlTemp(true);
			ret = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAm
					.requestAudioFocus(listener, stream,
							AudioManager.AUDIOFOCUS_GAIN);
			AudioServiceAdapter.enableFocusControlTemp(false);
			return ret;
		case AUDIO_LOGIC_MUTE:
			AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
			AudioServiceAdapter.enableFocusControlTemp(true);
			ret = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAm
					.requestAudioFocus(listener, stream,
							AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			AudioServiceAdapter.enableFocusControlTemp(false);
			return ret;
		default:
			break;
		}
		return true;
	}

	private Runnable mRunnableReleaseAudioFocus = new Runnable() {
		@Override
		public void run() {

			WakeupManager.getInstance().checkUsingAsr(null, new Runnable() {
				@Override
				public void run() {
					if (!TtsManager.getInstance().isBusy()
							&& !AsrManager.getInstance().isBusy()
							&& (mAudioFocusStreamType != null || CallManager
									.getInstance().isIdle())
							&& !RecorderWin.isOpened()) {
						if (!TextUtils.isEmpty(mAudioFocusLogicService)) {
							ServiceManager.getInstance().sendInvoke(
									mAudioFocusLogicService,
									"status.focus.AbandonAudioFocus", null, null);
							return;
						}
						AudioManager mAm = (AudioManager) GlobalContext.get()
								.getSystemService(Context.AUDIO_SERVICE);
						mAm.abandonAudioFocus(afChangeListener);
					}

				}

			});

		}
	};

	public void releaseAudioFocusImmediately() {
		AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
		mRunnableReleaseAudioFocus.run();
	}

	private void releaseAudioFocus() {
		AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
		AppLogic.runOnBackGround(mRunnableReleaseAudioFocus, 500);
	}

	AudioManager mAm = (AudioManager) GlobalContext.get().getSystemService(
			Context.AUDIO_SERVICE);

	public boolean onBeginTts(int stream,TtsTask ttsTask) {
		JNIHelper.logd("[Core] onBeginTts");
		// 先发AsrEnd
		if (ProjectCfg.mEnableAEC == false || ProjectCfg.needStopWkWhenTts()
				|| (ttsTask != null && ttsTask.isForceStopWakeup())) {
			WakeupManager.getInstance().enbaleVoiceChannel(false);
			// WakeupManager.getInstance().stop();
			// VolumeManager.getInstance().muteAll(false);
		}

		//	再发TtsBegin
		String command = "comm.status.onBeginTts";
		// ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
		// command,
		// null, null);
		ServiceManager.getInstance().broadInvoke(command, null);
		
		VolumeManager.getInstance().muteAll(false);
		return requestAudioFocus(mAudioLogicWhenTts);
	}

	public void onEndTts() {
		releaseAudioFocus();

		String command = "comm.status.onEndTts";
		// ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
		// command,
		// null, null);
		ServiceManager.getInstance().broadInvoke(command, null);
		WakeupManager.getInstance().enbaleVoiceChannel(true);
		// WakeupManager.getInstance().startDelay(300);
		/*
		 * 识别的时候还是需要禁掉其他的声音，不然别扭 if (ProjectCfg.mEnableAEC) { return; }
		 */

		WakeupManager.getInstance().checkUsingAsr(new Runnable() {
			@Override
			public void run() {
				VolumeManager.getInstance().muteAll(true);
			}
		}, null);
	}

	boolean mHasAudioFocus = false;

	OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			JNIHelper.logd("foucsChange:" + focusChange);
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				mHasAudioFocus = false;
			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				mHasAudioFocus = false;
			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
				mHasAudioFocus = false;
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				mHasAudioFocus = true;
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) {
				mHasAudioFocus = true;
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
				mHasAudioFocus = true;
			}

			if (!mHasAudioFocus) {
				JNIHelper.logw("onLostAudioFocus");
				onLostAudioFocus();
			}
		}
	};

	void onLostAudioFocus() {
		JNIHelper.logw("onLostAudioFocus");
		if (mAudioFocusStreamType == null)
			return;
		mAm.abandonAudioFocus(afChangeListener);
		TtsManager.getInstance().errorCurTask();
		AsrManager.getInstance().cancel();
		RecordManager.getInstance().stop();
		WinRecord.getInstance().dismiss();
	}

	Runnable mRunnableProtectEndAsr = new Runnable() {
		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(this);
			AppLogic.runOnBackGround(this, 5000);
			onEndAsr();
		}
	};

	public boolean onBeginAsr() {
		String command = "comm.status.onBeginAsr";
		// ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
		// command,
		// null, null);
		ServiceManager.getInstance().broadInvoke(command, null);

		AppLogic.removeBackGroundCallback(mRunnableProtectEndAsr);
		AppLogic.runOnBackGround(mRunnableProtectEndAsr, 5000);

		/*
		 * 识别的时候还是需要禁掉其他的声音，不然别扭 if (ProjectCfg.mEnableAEC) { return; }
		 */

		return requestAudioFocus(mAudioLogicWhenAsr);
	}

	public void onEndAsr() {

		if (AsrManager.getInstance().isBusy())
			return;

		Runnable runFalse = new Runnable() {
			
			@Override
			public void run() {
				if (RecordManager.getInstance().isBusy()) {
					return;
				}
				AppLogic.removeBackGroundCallback(mRunnableProtectEndAsr);

				releaseAudioFocus();

				String command = "comm.status.onEndAsr";
				// ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				// command,
				// null, null);
				ServiceManager.getInstance().broadInvoke(command, null);
				/*
				 * 识别的时候还是需要禁掉其他的声音，不然别扭 if (ProjectCfg.mEnableAEC) { return; }
				 */
				VolumeManager.getInstance().muteAll(false);
			}
		}; 
		if (!WakeupManager.getInstance().isBusy())
			runFalse.run();
		else
			WakeupManager.getInstance().checkUsingAsr(null,runFalse);
		
		
	}

	public void onEndBeep() {
		
		Runnable runTrue = new Runnable() {
			@Override
			public void run() {
				String command = "comm.status.onBeepEnd";
				ServiceManager.getInstance().broadInvoke(command, null);
				/*
				 * 识别的时候还是需要禁掉其他的声音，不然别扭 if (ProjectCfg.mEnableAEC) { return; }
				 */
				VolumeManager.getInstance().muteAll(true);
			}
		};
		
		if (AsrManager.getInstance().isBusy()) {
			runTrue.run();
		}else if(RecordManager.getInstance().isBusy()){
			runTrue.run();//1.0版本微信录音中也需要静音。2.0版本不需要，应该2.0走的是下面的分支
		}else {
			WakeupManager.getInstance().checkUsingAsr(runTrue, null);	
		}
	}

	public void onBeginCall() {
		String command = "comm.status.onBeginCall";
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, command,
				null, null);
		ServiceManager.getInstance().broadInvoke(command, null);

		if (mAudioFocusStreamType == null) {
			requestAudioFocus(mAudioLogicWhenCall);
		}
		TtsManager.getInstance().pause();
	}

	public void onEndCall() {
		releaseAudioFocus();
		TtsManager.getInstance().resume();
		String command = "comm.status.onEndCall";
		// ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
		// command,
		// null, null);
		ServiceManager.getInstance().broadInvoke(command, null);
	}

	public void onBeginMusic() {
		String command = "comm.status.onBeginMusic";
		ServiceManager.getInstance().broadInvoke(command, null);
	}

	public void onEndMusic() {
		String command = "comm.status.onEndMusic";
		ServiceManager.getInstance().broadInvoke(command, null);
	}

	// /////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_REMOTE_PROC_PLAY_MUSIC);

		regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PLAY_LIST);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_SYNC_LIST);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_CATEGORY_LIST_UPDATED);

		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_NOTIFY_DOWNLOAD_FINISH);

		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE_RESULT);

		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_SPEAK_MUSIC_INFO);

		regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PLAY);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PAUSE);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_EXIT);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NEXT);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PREV);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_RANDOM);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_MODE_LOOP_SINGLE);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_MODE_LOOP_ALL);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_MODE_RANDOM);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_FAVOURITE_CUR);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE_CUR);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_HATE_CUR);

		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_PLAY_FAVOURITE_LIST);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MEDIA_LIST);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MUSIC_LIST);

		regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_NOTIFY_LOCAL_MEDIA_SERVER_PORT);
		regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PLAY_ALL);

		regEvent(UiEvent.EVENT_ACTION_AUDIO,
				UiAudio.SUBEVENT_RESP_DATA_INTERFACE);

		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 音乐的命令字全部注册为远程音乐场景，底层特殊处理远程音乐场景

//		regCommand("MEDIA_CMD_PLAY");
		regCommandWithResult("MEDIA_CMD_PLAY");
		regCommand("MEDIA_CMD_OPEN");

		regCustomCommand("MUSIC_CMD_PLAY", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_PLAY);
		regCustomCommand("MUSIC_CMD_STOP", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_PAUSE);
		regCustomCommand("MUSIC_CMD_EXIT", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_EXIT);
		regCustomCommand("MUSIC_CMD_NEXT", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_NEXT);
		regCustomCommand("MUSIC_CMD_PREV", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_PREV);
		regCustomCommand("MUSIC_CMD_RANDOM", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_RANDOM);
		regCustomCommand("MUSIC_CMD_SWITCH_LOOP_ONE",
				UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_MODE_LOOP_SINGLE);
		regCustomCommand("MUSIC_CMD_SWITCH_LOOP_ALL",
				UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_MODE_LOOP_ALL);
		regCustomCommand("MUSIC_CMD_SWITCH_RANDOM",
				UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_MODE_RANDOM);

		regCustomCommand("MUSIC_FAVOURITE", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_FAVOURITE_CUR);
		regCustomCommand("MUSIC_CANCEL_FAVOURITE",
				UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE_CUR);
		regCustomCommand("MUSIC_HATE", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_HATE_CUR);
		regCustomCommand("MUSIC_PLAY_FAVOURITE_LIST",
				UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_PLAY_FAVOURITE_LIST);
		regCustomCommand("REFRESH_MEDIA_LIST",
				UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MEDIA_LIST);
		regCustomCommand("REFRESH_MUSIC_LIST",
				UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
				UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MUSIC_LIST);

		// 播放音乐请求
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_PLAY_MEDIA);

		return super.initialize_AfterStartJni();
	}

	@Override
	public int initialize_AfterInitSuccess() {
		AndroidMediaLibrary.refreshSystemMedia();
		return super.initialize_AfterInitSuccess();
	}
	
	@Override
	public int onCommand(String cmd, String keywords, String voiceString) {
		if (cmd.equals("MEDIA_CMD_PLAY")) {
			continuePlay(voiceString);
		}
		return super.onCommand(cmd, keywords, voiceString);
	}

	@Override
	public int onCommand(String cmd) {
		if ("MEDIA_CMD_OPEN".equals(cmd)) {
			// 打开指令
			IMusic tool = getMusicTool();
			if (null != tool && tool instanceof ITxzMedia) {
				ITxzMedia txzMedia = (ITxzMedia) tool;
				txzMedia.openApp();
			} else {
				RecorderWin.speakTextWithClose(
						NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"),
						null);
			}
		}

		return 0;
	}

	/**
	 * 继续播放
	 */
	public void continuePlay(String text) {
		if (SenceManager.getInstance().noneedProcSence("music", new JSONBuilder().put("action", "continue").put("scene", "music").put("text", text).toBytes()))
			return ;
		if (isAudioToolSet() && TextUtils.isEmpty(mMusicToolServiceName)) {
			String spk = NativeData.getResString("RS_MUSIC_COUNTINUE");
			RecorderWin.speakTextWithClose(spk, new Runnable() {
				@Override
				public void run() {
					MediaControlUtil.play();
				}

			});
		} else {
			// 判断是否有可用音乐工具
			IMusic tool = getMusicTool();
			if (!TextUtils.isEmpty(mMusicToolServiceName)
					|| getMusicTool() != null) {
				RecorderWin.speakTextWithClose(
						NativeData.getResString("RS_VOICE_WILL_PLAY_MUSIC"),
						new Runnable() {
							@Override
							public void run() {
								invokeTXZMusic(null, "play", null);
							}
						});
			} else {
				String spk = NativeData.getResString("RS_MUSIC_NOT_TOOL");
				RecorderWin.speakTextWithClose(spk, null);
			}

		}

	}

	abstract class PlayMediaConfirmTtsCallback extends ITtsCallback {
		MediaList mMediaList;

		public PlayMediaConfirmTtsCallback(MediaList lst) {
			mMediaList = lst;
		}
	}

	abstract class PlayMediaConfirmAsrCallback extends IAsrCallback {
		MediaList mMediaList;

		public PlayMediaConfirmAsrCallback(MediaList lst) {
			mMediaList = lst;
		}
	}

	// 通过语音收藏当前播放的歌曲
	public boolean favouriteMusicFromVoice(boolean favourite, boolean hint) {
		if (MusicTxzImpl.mTxzMusicToolisPlaying && !AudioTxzImpl.isNewVersion()) {// 不是最新版本
			if (MusicTxzImpl.mTxzMusicToolCurrentMusicModel != null) {
				MediaModel mediaModel = MusicTxzImpl.mTxzMusicToolCurrentMusicModel.msgMedia;
				if (mediaModel != null) {
					favouriteMusic(mediaModel, favourite);
					if (hint) {
						if (favourite) {
							String spk = NativeData.getResString("RS_MUSIC_FAVOURITE_ADD");
							RecorderWin.speakTextWithClose(spk, null);
						} else {
							String spk = NativeData.getResString("RS_MUSIC_FAVOURITE_CANCEL");
							RecorderWin.speakTextWithClose(spk, null);
						}
					}
					return true;
				}
			}
		} else if (AudioTxzImpl.isNewVersion()) {
			String spk = NativeData.getResString("RS_MUSIC_FUNCTION_UNSUPPORT");
			RecorderWin.speakText(spk, null);
		}
		if (hint) {
			String spk = NativeData.getResString("RS_MUSIC_NO_PLAY");
			RecorderWin.speakTextWithClose(spk, null);
		}
		return false;
	}

	// 收藏指定的歌曲
	public void favouriteMusic(MediaModel mediaModel, boolean favourite) {
		if (AudioTxzImpl.isNewVersion()) {
			String spk = NativeData.getResString("RS_MUSIC_FUNCTION_UNSUPPORT");
			RecorderWin.speakText(spk, null);
			return;
		}

		if (null == mediaModel)
			return;
		mediaModel.bFavourite = favourite;
		ServiceManager.getInstance()
				.sendInvoke(ServiceManager.MUSIC, "music.updateFavour",
						MessageNano.toByteArray(mediaModel), null);
		if (favourite) {
			JNIHelper.logd("favourite music: " + mediaModel.strPath);
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_FAVOURITE, mediaModel);
		} else {
			JNIHelper.logd("cancel favourite music: " + mediaModel.strPath);
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE, mediaModel);
		}
		return;
	}

	public void procMediaPath(MediaModel m) {
		if (m == null)
			return;
		m.strPath = NativeData.getMeidaPlayUrl(m.strPath);
	}

	public void procMediaPath(MediaItem m) {
		if (m == null)
			return;
		if (m.msgMedia != null) {
			procMediaPath(m.msgMedia);
		}
		for (int i = 0; m.rptMsgReferenceMedia != null
				&& i < m.rptMsgReferenceMedia.length; ++i) {
			procMediaPath(m.rptMsgReferenceMedia[i]);
		}
	}

	public void procMediaPath(MediaList m) {
		for (int i = 0; m.rptMediaItem != null && i < m.rptMediaItem.length; ++i) {
			procMediaPath(m.rptMediaItem[i]);
		}
	}

	public void procMediaPath(MediaCategoryList m) {
		for (int i = 0; m.rptMsgCategoryList != null
				&& i < m.rptMsgCategoryList.length; ++i) {
			if (m.rptMsgCategoryList[i] == null)
				continue;
			procMediaPath(m.rptMsgCategoryList[i].msgMediaList);
		}
	}

	public String genMediaModelTitle(String jsonModel) {
		MusicModel musicModel = new MusicModel();
		JSONBuilder builder = new JSONBuilder(jsonModel);
		musicModel.setArtist(builder.getVal("artists", String[].class));
		musicModel.setKeywords(new String[] { builder.getVal("tag",
				String.class) });
		musicModel.setAlbum(builder.getVal("album", String.class));
		musicModel.setTitle(builder.getVal("title", String.class));

		return genMediaModelTitle(musicModel);
	}

	public String genMediaModelTitle(MusicModel musicModel) {
		String[] kws = musicModel.getKeywords();
		String kw = "";
		if (kws != null && kws.length > 0) {
			kw = kws[0];
		}
		return genMediaModelTitle(musicModel.getTitle(), musicModel.getAlbum(),
				musicModel.getArtist(), kw,
				musicModel.getField() == AudioTxzImpl.MUSICFLAG ? "歌曲" : "节目");
	}

	public String genMediaModelTitle(String title, String album,
			String[] artists, String keywrod, String type) {
		StringBuilder artist = new StringBuilder();
		if (artists != null) {
			for (int i = 0; i < artists.length; ++i) {
				if (artist.length() == 0) {
					artist.append(artists[i]);
				} else if (i == artists.length - 1) {
					artist.append("和");
					artist.append(artists[i]);
				} else {
					artist.append("、");
					artist.append(artists[i]);
				}
			}
		}
		if (TextUtils.isEmpty(title) && TextUtils.isEmpty(artist)) {
			if (TextUtils.isEmpty(album)) {
				return keywrod + "类型" + type;
			} else {
				return album;
			}
		}
		if (TextUtils.isEmpty(artist) == false) {
			artist.append("的");
		}
		if (TextUtils.isEmpty(title)) {
			artist.append(type);
		} else {
			artist.append(title);
		}
		return artist.toString();
	}

	// public byte[] currentData;

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_ACTION_AUDIO: {
			switch (subEventId) {
			case UiAudio.SUBEVENT_RESP_DATA_INTERFACE: {
				ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
						"music.dataInterface", data, null);
				break;
			}
			}
			break;
		}
		case UiEvent.EVENT_VOICE: {
			switch (subEventId) {
			case VoiceData.SUBEVENT_VOICE_PLAY_MEDIA: {
				String title = "歌曲";
				try {
					MediaList mediaList = MediaList.parseFrom(data);
					if (mediaList.msgSearchFilter != null) {
						String keywrods = mediaList.msgSearchFilter.strType;
						if (TextUtils.isEmpty(keywrods)
								&& mediaList.msgSearchFilter.rptStrKeywords != null
								&& mediaList.msgSearchFilter.rptStrKeywords.length > 0) {
							keywrods = mediaList.msgSearchFilter.rptStrKeywords[0];
						}
						title = genMediaModelTitle(
								mediaList.msgSearchFilter.strTitle,
								mediaList.msgSearchFilter.strAlbum,
								mediaList.msgSearchFilter.rptStrArtist,
								keywrods, "歌曲");
					}
				} catch (Exception e) {
				}
				String spk = NativeData.getResPlaceholderString(
						"RS_MUSIC_WILL_PLAY", "%CMD%", title);
				RecorderWin.speakTextWithClose(spk,
						new Runnable1<byte[]>(data) {
							@Override
							public void run() {
								JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
										UiMusic.SUBEVENT_MEDIA_PLAY_LIST, mP1);
							}
						});
				break;
			}
			}
			break;
		}
		case UiEvent.EVENT_REMOTE_PROC_PLAY_MUSIC: {
			String kw = null;
			JSONBuilder json = new JSONBuilder(data);
			String[] kws = json.getVal("keywords", String[].class);
			if (kws != null && kws.length > 0) {
				kw = kws[0];
			}
			if (!TextUtils.isEmpty(mMusicToolServiceName)) {

				String title = genMediaModelTitle(
						json.getVal("title", String.class),
						json.getVal("album", String.class),
						json.getVal("artist", String[].class), kw, "歌曲");
				if (mIsNotNeedTts) {
					if (RecorderWin.isOpened()) {
						RecorderWin.close();
					}

					ServiceManager.getInstance().sendInvoke(
							mMusicToolServiceName, "tool.music.playMusic",
							data, null);
					break;
				}
				String spk = NativeData.getResPlaceholderString(
						"RS_MUSIC_WILL_PLAY", "%CMD%", title);
				RecorderWin.speakTextWithClose(spk,
						new Runnable1<byte[]>(data) {
							@Override
							public void run() {
								ServiceManager.getInstance().sendInvoke(
										mMusicToolServiceName,
										"tool.music.playMusic", mP1, null);
							}
						});
				break;
			}
			final IMusic musicTool = getMusicTool();
			if (musicTool != null) {
				MusicModel model = new MusicModel();
				model.setTitle(json.getVal("title", String.class));
				model.setArtist(json.getVal("artist", String[].class));
				model.setAlbum(json.getVal("album", String.class));
				model.setKeywords(kws);
				String title = genMediaModelTitle(model.getTitle(),
						model.getAlbum(), model.getArtist(), kw, "歌曲");
				if (musicTool instanceof MusicKaolaImpl
						&& MusicKaolaImpl.SHOW_SEARCH_RESULT) {
					musicTool.playMusic(model);
					break;
				}
				if (musicTool instanceof AudioTxzImpl) {
					musicTool.playMusic(model);
					break;
				}
				if (musicTool instanceof MusicKuwoImpl
						&& MusicKuwoImpl.SHOW_SEARCH_RESULT) {
					musicTool.playMusic(model);
					String spk = NativeData.getResPlaceholderString(
							"RS_MUSIC_SEARCHING", "%CMD%", title);
					Log.d("leng", spk);
					RecorderWin.addSystemMsg(spk);
					TtsManager.getInstance().speakText(spk);
					break;
				}
				String spk = NativeData.getResPlaceholderString(
						"RS_MUSIC_WILL_PLAY", "%CMD%", title);
				RecorderWin.speakTextWithClose(spk,
						new Runnable2<IMusic, MusicModel>(musicTool, model) {
							@Override
							public void run() {
								AppLogic.runOnBackGround(new Runnable() {

									@Override
									public void run() {
										musicTool.playMusic(mP2);
									}
								}, mAudioFocusStreamType == null ? 0 : 500);
							}
						});
				break;
			} else {
				AsrManager.getInstance().setNeedCloseRecord(false);
				String spk = NativeData.getResString("RS_MUSIC_NOT_TOOL");
				RecorderWin.speakTextWithClose(spk, null);
			}
			break;
		}
		case UiEvent.EVENT_SYSTEM_MUSIC:
			switch (subEventId) {
			case UiMusic.SUBEVENT_MEDIA_PLAY:
				RecorderWin.speakTextWithClose(
						NativeData.getResString("RS_VOICE_WILL_PLAY_MUSIC"),
						new Runnable() {
							@Override
							public void run() {
								invokeTXZMusic(null, "play", null);
							}
						});
				break;
			case UiMusic.SUBEVENT_MEDIA_PAUSE:
				RecorderWin.speakTextWithClose(
						NativeData.getResString("RS_VOICE_WILL_STOP_MUSIC"),
						new Runnable() {
							@Override
							public void run() {
								invokeTXZMusic(null, "pause", null);
							}
						});
				break;
			case UiMusic.SUBEVENT_MEDIA_EXIT:
				RecorderWin.speakTextWithClose(
						NativeData.getResString("RS_VOICE_WILL_EXIT_MUSIC"),
						new Runnable() {
							@Override
							public void run() {
								invokeTXZMusic(null, "exit", null);
							}
						});
				break;
			case UiMusic.SUBEVENT_MEDIA_NEXT:
				RecorderWin.speakTextWithClose(
						NativeData.getResString("RS_VOICE_WILL_NEXT_MUSIC"),
						new Runnable() {
							@Override
							public void run() {
								invokeTXZMusic(null, "next", null);
							}
						});
				break;
			case UiMusic.SUBEVENT_MEDIA_PREV:
				RecorderWin.speakTextWithClose(
						NativeData.getResString("RS_VOICE_WILL_PREV_MUSIC"),
						new Runnable() {
							@Override
							public void run() {
								invokeTXZMusic(null, "prev", null);
							}
						});
				break;
			case UiMusic.SUBEVENT_MEDIA_RANDOM:
				RecorderWin.speakTextWithClose(
						NativeData.getResString("RS_VOICE_WILL_SWITCH_MUSIC"),
						new Runnable() {
							@Override
							public void run() {
								invokeTXZMusic(null, "switchSong", null);
							}
						});
				break;
			case UiMusic.SUBEVENT_MEDIA_MODE_LOOP_SINGLE:
				RecorderWin.speakTextWithClose(NativeData
						.getResString("RS_VOICE_WILL_SWICTH_MODE_LOOP_ONE"),
						new Runnable() {
							@Override
							public void run() {
								invokeTXZMusic(null, "switchModeLoopOne", null);
							}
						});
				break;
			case UiMusic.SUBEVENT_MEDIA_MODE_LOOP_ALL:
				RecorderWin.speakTextWithClose(NativeData
						.getResString("RS_VOICE_WILL_SWICTH_MODE_LOOP_ALL"),
						new Runnable() {
							@Override
							public void run() {
								invokeTXZMusic(null, "switchModeLoopAll", null);
							}
						});
				break;
			case UiMusic.SUBEVENT_MEDIA_MODE_RANDOM:
				RecorderWin.speakTextWithClose(NativeData
						.getResString("RS_VOICE_WILL_SWICTH_MODE_RANDOM"),
						new Runnable() {
							@Override
							public void run() {
								invokeTXZMusic(null, "switchModeRandom", null);
							}
						});
				break;
			case UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_PLAY_FAVOURITE_LIST:
				invokeTXZMusic(null, "playFavourMusic", null);
				break;
			case UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MEDIA_LIST:
			case UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MUSIC_LIST:
				RecorderWin.speakTextWithClose(
						NativeData.getResString("RS_VOICE_WILL_REFRESH_MUSIC"),
						new Runnable() {
							@Override
							public void run() {
								invokeTXZMusic(null, "playRandom", null);
							}
						});
				break;
			case UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE:
				if (mSearchMoreResult)
					break;
				try {
					// 打开音乐搜索中界面
					mSearchMediaModel = MediaModel.parseFrom(data);
					// TtsManager.getInstance().speakText("本地未找到，即将上网进行搜索");
					if (mSearchMediaModel == null
							|| (TextUtils.isEmpty(mSearchMediaModel.strTitle)
									&& TextUtils
											.isEmpty(mSearchMediaModel.strAlbum) && TextUtils
										.isEmpty(mSearchMediaModel.strType))) {
						boolean bEmpty = true;
						if (mSearchMediaModel != null) {
							for (int i = 0; bEmpty
									&& i < mSearchMediaModel.rptStrArtist.length; ++i) {
								if (TextUtils
										.isEmpty(mSearchMediaModel.rptStrArtist[i]) == false) {
									bEmpty = false;
								}
							}
							for (int i = 0; bEmpty
									&& i < mSearchMediaModel.rptStrKeywords.length; ++i) {
								if (TextUtils
										.isEmpty(mSearchMediaModel.rptStrKeywords[i]) == false) {
									bEmpty = false;
								}
							}
						}
						if (bEmpty) {
							if (TextUtils
									.isEmpty(mSearchMediaModel.strSearchText)) {
								String spk = NativeData.getResString("RS_MUSIC_EMPTY");
								RecorderWin.speakTextWithClose(
										spk, null);
							} else {
								String spk = NativeData.getResString("RS_MUSIC_EMPTY_ABOUT");
								RecorderWin.speakTextWithClose(
										spk, null);
							}
							break;
						}
					}
					String spk = NativeData.getResString("RS_MUSIC_NOT_FOUND");
					RecorderWin.speakTextWithClose(spk, null);
				} catch (Exception e) {
				}
				break;
			case UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE_RESULT:
				if (mSearchMediaModel != null) {
					try {
						final MediaList lst = MediaList.parseFrom(data);
						if (ProtoBufferUtil.isStringEmpty(lst.strErrMsg) == false) {
							mSearchMoreResult = false;
							JNIHelper.loge("recive search result error: "
									+ lst.strErrMsg);
							AppLogic.showToast(lst.strErrMsg);
							TtsManager.getInstance().speakText(lst.strErrMsg);
							break;
						}
						if (ProtoBufferUtil.isEqual(lst.msgSearchFilter,
								mSearchMediaModel) == false) {
							JNIHelper.logw("recive search result not match");
							break;
						}
						// 本次搜索响应已回
						mSearchMediaModel = null;
						if (lst.rptMediaItem == null
								|| lst.rptMediaItem.length == 0) {
							JNIHelper.logd("empty media list");
							String spk = NativeData.getResString("RS_MUSIC_SEARCH_NULL");
							TtsManager.getInstance().speakText(spk);
							mSearchMoreResult = false;
							break;
						}
						if (mSearchMoreResult) {
							mSearchMoreResult = false;
							procMediaPath(lst);
							ServiceManager.getInstance().sendInvoke(
									ServiceManager.MUSIC,
									"music.update.appendMediaList",
									MessageNano.toByteArray(lst), null);
							break;
						}
						if (lst.int32CurIndex == null || lst.int32CurIndex < 0
								|| lst.int32CurIndex > lst.rptMediaItem.length)
							lst.int32CurIndex = 0;
						String hint;
						if (lst.msgSearchFilter.rptStrArtist != null
								&& lst.msgSearchFilter.rptStrArtist.length > 0
								&& ProtoBufferUtil
										.isStringEmpty(lst.msgSearchFilter.strTitle)
								&& ProtoBufferUtil
										.isStringEmpty(lst.msgSearchFilter.strAlbum)
								&& ProtoBufferUtil
										.isStringEmpty(lst.msgSearchFilter.strType)) {
							// 歌手搜索
							hint = NativeData.getResString("RS_MUSIC_FOUND_DETAIL")
									.replace("%NUM%", lst.rptMediaItem.length+"")
									.replace("%ART%", lst.msgSearchFilter.rptStrArtist[0]);
						} else if ((lst.msgSearchFilter.rptStrArtist == null || lst.msgSearchFilter.rptStrArtist.length <= 0)
								&& ProtoBufferUtil
										.isStringEmpty(lst.msgSearchFilter.strTitle)
								&& ProtoBufferUtil
										.isStringEmpty(lst.msgSearchFilter.strAlbum)
								&& ProtoBufferUtil
										.isStringEmpty(lst.msgSearchFilter.strType) == false) {
							// 分类搜索
							hint = NativeData.getResString("RS_MUSIC_FOUND_DETAIL")
									.replace("%NUM%", lst.rptMediaItem.length+"")
									.replace("%ART%", lst.msgSearchFilter.strType);
						} else if ((lst.msgSearchFilter.rptStrArtist == null || lst.msgSearchFilter.rptStrArtist.length <= 0)
								&& ProtoBufferUtil
										.isStringEmpty(lst.msgSearchFilter.strTitle)
								&& ProtoBufferUtil
										.isStringEmpty(lst.msgSearchFilter.strAlbum) == false
								&& ProtoBufferUtil
										.isStringEmpty(lst.msgSearchFilter.strType)) {
							// 分类搜索
							hint = NativeData.getResString("RS_MUSIC_FOUND_DETAIL")
									.replace("%NUM%", lst.rptMediaItem.length+"")
									.replace("%ART%", lst.msgSearchFilter.strAlbum);
						} else {
							hint = NativeData.getResPlaceholderString(
									"RS_MUSIC_FOUND", "%CMD%", 
									getMediaSpeakInfo(lst.rptMediaItem[lst.int32CurIndex].msgMedia));
						}

						lst.msgSearchFilter = null; // 置null，不允许下次查找更多结果
						procMediaPath(lst);

						TtsManager.getInstance().speakText(hint,
								new ITtsCallback() {
									@Override
									public void onEnd() {
										AppLogic.runOnBackGround(
												new Runnable() {
													@Override
													public void run() {
														ServiceManager
																.getInstance()
																.sendInvoke(
																		ServiceManager.MUSIC,
																		"music.update.playMediaList",
																		MessageNano
																				.toByteArray(lst),
																		null);
													}
												},
												mAudioFocusStreamType == null ? 0
														: 500);
									}
								});
					} catch (Exception e) {
					}
				}
				break;
			case UiMusic.SUBEVENT_MEDIA_PLAY_LIST:
				try {
					MediaList m = MediaList.parseFrom(data);
					procMediaPath(m);
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.MUSIC, "music.update.playMediaList",
							MessageNano.toByteArray(m), null);
				} catch (Exception e) {
				}
				break;
			case UiMusic.SUBEVENT_MEDIA_SYNC_LIST:
				try {
					MediaList m = MediaList.parseFrom(data);
					procMediaPath(m);
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.MUSIC, "music.update.syncMediaList",
							MessageNano.toByteArray(m), null);
				} catch (Exception e) {
				}
				break;
			case UiMusic.SUBEVENT_MEDIA_FAVOURITE_CUR:
				if (!TextUtils.isEmpty(mMusicToolServiceName)) {
					RecorderWin.speakTextWithClose("", new Runnable() {

						@Override
						public void run() {
							invokeTXZMusic(null, "favourMusic", null);
						}
					});
					break;
				}
				invokeTXZMusic(null, "favourMusic", null);
				break;
			case UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE_CUR:
				if (!TextUtils.isEmpty(mMusicToolServiceName)) {
					RecorderWin.speakTextWithClose("", new Runnable() {

						@Override
						public void run() {
							invokeTXZMusic(null, "unfavourMusic", null);
						}
					});
					break;
				}
				invokeTXZMusic(null, "unfavourMusic", null);
				break;
			case UiMusic.SUBEVENT_MEDIA_HATE_CUR:
				if (!TextUtils.isEmpty(mMusicToolServiceName)) {
					RecorderWin.speakTextWithClose("", new Runnable() {

						@Override
						public void run() {
							ServiceManager.getInstance().sendInvoke(
									mMusicToolServiceName,
									"tool.music.unfavourMusic", null, null);
							ServiceManager.getInstance().sendInvoke(
									mMusicToolServiceName,
									"tool.music.switchSong", null, null);
						}
					});
					break;
				}
				IMusic musicTool = getMusicTool();
				if (musicTool != null) {
					musicTool.unfavourMusic();
					musicTool.switchSong();
					break;
				}
				break;
			case UiMusic.SUBEVENT_MEDIA_CATEGORY_LIST_UPDATED: {
				try {
					MediaCategoryList lst = MediaCategoryList.parseFrom(data);
					procMediaPath(lst);
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.MUSIC,
							"music.update.syncMediaCategoryList",
							MessageNano.toByteArray(lst), null);
				} catch (Exception e) {
				}
				break;
			}
			case UiMusic.SUBEVENT_MEDIA_SPEAK_MUSIC_INFO: {
				speakMusicInfo();
				break;
			}
			case UiMusic.SUBEVENT_MEDIA_NOTIFY_DOWNLOAD_FINISH: {
				ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
						"music.notifyDownloadFinish", data, null);
				try {
					MediaModel model = MediaModel.parseFrom(data);
					// AppLogic.showToast(getMediaSpeakInfo(model) + "下载完成");
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case UiMusic.SUBEVENT_MEDIA_NOTIFY_LOCAL_MEDIA_SERVER_PORT: {
				ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
						"music.notifyMediaServerPort", data, null);
				break;
			}
			case UiMusic.SUBEVENT_MEDIA_PLAY_ALL:
				AsrManager.getInstance().setNeedCloseRecord(true);
				continuePlay(new String(data));
				break;
			default:
				break;
			}
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}

	public String getMediaSpeakInfo(MediaModel mediaModel) {
		StringBuilder singer = new StringBuilder();
		String name = "";

		if (null != mediaModel) {
			if (mediaModel.rptStrArtist != null) {
				for (int i = 0; i < mediaModel.rptStrArtist.length; ++i) {
					if (mediaModel.rptStrArtist[i].length() <= 0)
						continue;
					if (singer.length() > 0) {
						if (i == mediaModel.rptStrArtist.length - 1) {
							singer.append("和");
						} else {
							singer.append("、");
						}
					}
					singer.append(mediaModel.rptStrArtist[i]);
				}
			}
			if (mediaModel.strTitle != null) {
				name = mediaModel.strTitle;
			}
			if (name.length() <= 0) {
				if (mediaModel.strFileName != null) {
					singer = new StringBuilder();
					name = mediaModel.strFileName; // 使用文件名
				}
			}
		}

		if (singer.length() > 0)
			singer.append("的");

		if (name.length() <= 0) {
			if (singer.length() <= 0)
				name = "未知音乐";
			else
				name = "歌";
		}
		return singer.toString() + name;
	}

	// 播报音乐信息
	public void speakMusicInfo() {
		if (isPlaying()) {
			MediaModel model = null;
			if (TextUtils.isEmpty(mMusicToolServiceName)) {
				IMusic musicTool = getMusicTool();
				if (musicTool != null) {
					MusicModel m = musicTool.getCurrentMusicModel();
					if (m != null) {
						model = new MediaModel();
						model.strAlbum = m.getAlbum();
						model.strTitle = m.getTitle();
						model.rptStrArtist = m.getArtist();
						model.rptStrKeywords = m.getKeywords();
					}
				}
			} else {
				JSONObject json;
				try {
					MusicModel m;

					IMusic musicTool = getMusicTool();
					if (musicTool != null
							&& (m = musicTool.getCurrentMusicModel()) != null) {
						json = new JSONObject(m.toString());
					} else {
						json = new JSONObject(new String(
								mRemoteMusicToolgetCurrentMusicModel));
					}
					model = new MediaModel();
					if (json.has("title"))
						model.strTitle = json.getString("title");
					if (json.has("album"))
						model.strAlbum = json.getString("album");
					if (json.has("artist")) {
						JSONArray arr = json.getJSONArray("artist");
						model.rptStrArtist = new String[arr.length()];
						for (int i = 0; i < arr.length(); ++i) {
							model.rptStrArtist[i] = arr.getString(i);
						}
					}
				} catch (JSONException e) {
					model = null;
				}
			}
			if (model != null) {
				LogUtil.logd("speakmodel=" + model.strTitle + "," + model);
				String spk = NativeData.getResPlaceholderString(
						"RS_MUSIC_IS_PLAY",
						"%CMD%", getMediaSpeakInfo(model));
				RecorderWin.speakTextWithClose(spk, null);
				return;
			}
		}
		String spk = NativeData.getResString("RS_MUSIC_NO_PLAY");
		RecorderWin.speakTextWithClose(spk, null);
	}

	private IMusic getTXZMusicTool() {
		if (AudioTxzImpl.isNewVersion()) {
			if (!(mMusicTxzImpl instanceof AudioTxzImpl)) {
				mMusicTxzImpl = new AudioTxzImpl();
				synchronized (mMusicInnerToolMap) {
					return mMusicInnerToolMap.put(ServiceManager.MUSIC,
							mMusicTxzImpl);
				}
			}
		} else {
			if ((mMusicTxzImpl instanceof AudioTxzImpl)) {
				mMusicTxzImpl = new MusicTxzImpl();
				synchronized (mMusicInnerToolMap) {
					return mMusicInnerToolMap.put(ServiceManager.MUSIC,
							mMusicTxzImpl);
				}
			}
		}
		return mMusicTxzImpl;
	}

	private String preSelectMusicTool() {
		String type = "";
		if (VersionPreference.VERSION_KAOLA) {
			if (PackageManager.getInstance().checkAppExist(
					MusicKaolaImpl.PACKAGE_NAME)) {
				type = MusicKaolaImpl.PACKAGE_NAME;
			} else if (PackageManager.getInstance().checkAppExist(
					ServiceManager.MUSIC)) {
				type = ServiceManager.MUSIC;
			} else if (PackageManager.getInstance().checkAppExist(
					MusicKuwoImpl.PACKAGE_NAME)) {
				type = MusicKuwoImpl.PACKAGE_NAME;
			}
		}
		return type;
	}

	public IMusic getMusicTool() {
		String type = mMusicToolApp;
		if (!TextUtils.isEmpty(type)
				&& !PackageManager.getInstance().checkAppExist(type)) {
			type = null;
		}
		if (TextUtils.isEmpty(type)) {
			if (PackageManager.getInstance()
					.checkAppExist(ServiceManager.MUSIC)) {
				type = ServiceManager.MUSIC;
			} else if (PackageManager.getInstance().checkAppExist(
					MusicKuwoImpl.PACKAGE_NAME)) {
				type = MusicKuwoImpl.PACKAGE_NAME;
			} else if (PackageManager.getInstance().checkAppExist(
					MusicKaolaImpl.PACKAGE_NAME)) {
				type = MusicKaolaImpl.PACKAGE_NAME;
			}

			String tool = preSelectMusicTool();
			if (!"".equals(tool)) {
				type = tool;
			}
		}
		getTXZMusicTool();
		synchronized (mMusicInnerToolMap) {
			return mMusicInnerToolMap.get(type);
		}
	}

	private boolean mIsNotNeedTts = false;
	private String mMusicToolServiceName;
	private Map<String, IMusic> mMusicInnerToolMap = new HashMap<String, IMusic>();
	private String mMusicToolApp = null;
	private boolean mRemoteMusicToolisPlaying = false;
	private byte[] mRemoteMusicToolgetCurrentMusicModel;

	public boolean hasRemoteProcTool() {
		if (!TextUtils.isEmpty(mMusicToolServiceName))
			return true;
		if (ServiceManager.MUSIC.equals(mMusicToolApp)
				&& PackageManager.getInstance().checkAppExist(
						ServiceManager.MUSIC)) {
			return AudioTxzImpl.isNewVersion();
		}
		if (!TextUtils.isEmpty(mMusicToolApp))
			return true;
		// 未设置音乐工具时
		if (PackageManager.getInstance().checkAppExist(ServiceManager.MUSIC)) {
			return AudioTxzImpl.isNewVersion();
		}
		if (PackageManager.getInstance().checkAppExist(
				MusicKuwoImpl.PACKAGE_NAME))
			return true;
		if (PackageManager.getInstance().checkAppExist(
				MusicKaolaImpl.PACKAGE_NAME))
			return true;
		return false;
	}

	public String getDisableResaon() {
		if (!TextUtils.isEmpty(mMusicToolServiceName))
			return "";
		IMusic musicTool = getMusicTool();
		if (musicTool != null) {
			if (PackageManager.getInstance().checkAppExist(
					musicTool.getPackageName()))
				return "";
		}
		return NativeData.getResString("RS_VOICE_NO_MUSIC_TOOL");
	}

	ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void onConnected(String serviceName) {
		}

		@Override
		public void onDisconnected(String serviceName) {
			if (serviceName.equals(mMusicToolServiceName)) {
				invokeTXZMusic(null, "cleartool", null);
			}
		}
	};

	String mAudioFocusLogicService = null;
	Integer mAudioFocusStreamType = null;

	public byte[] invokeTXZMusic(final String packageName, String command,
			byte[] data) {
		if (command.equals("dataInterface")) {
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_AUDIO,
					UiAudio.SUBEVENT_REQ_DATA_INTERFACE, data);
			return null;
		}
		if (command.equals("syncmusiclist")) {
			LogUtil.logd("------------------this is show data ="
					+ new String(data));
			JSONArray array = null;
			try {
				array = new JSONArray(new String(data));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			List<AudioShowData> audioShowDatas = new ArrayList<AudioShowData>();
			if (null != array && array.length() >= 0) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject jsonObject = array.optJSONObject(i);
					AudioShowData audioShowData = new AudioShowData();
					if (jsonObject.has("title")) {
						audioShowData.setTitle(jsonObject.optString("title"));
					}
					if (jsonObject.has("id")) {
						audioShowData.setId(jsonObject.optLong("id"));
					}
					if (jsonObject.has("name")) {
						audioShowData.setName(jsonObject.optString("name"));
					}
					audioShowDatas.add(audioShowData);
				}
			}
			LogUtil.logd("------------------2" + audioShowDatas);
			if (RecorderWin.isOpened()) {
//				MusicSelectControl.showContactSelectList(
//						UiEvent.EVENT_ACTION_AUDIO, audioShowDatas);
				SelectorHelper.entryMusicSelector(audioShowDatas,null);
			} else {
				LogUtil.logd("RecorderWin isclosed");
			}
			return null;
		}
		if (command.equals("isNewVersion")) {
			AudioTxzImpl.newVersion = true;
		}
		// 设置音频逻辑
		if (command.equals("audioLogic.tts")) {
			try {
				mAudioLogicWhenTts = AudioLogicType.valueOf(new String(data));
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("audioLogic.asr")) {
			try {
				mAudioLogicWhenAsr = AudioLogicType.valueOf(new String(data));
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("audioLogic.call")) {
			try {
				mAudioLogicWhenCall = AudioLogicType.valueOf(new String(data));
			} catch (Exception e) {
			}
			return null;
		}
		if ("setAudioFocusStreamType".equals(command)) {
			try {
				mAudioFocusStreamType = Integer.parseInt(new String(data));
			} catch (Exception e) {
			}
			return null;
		}
		if ("setAudioFocusLogic".equals(command)) {
			mAudioFocusLogicService = packageName;
			return null;
		}
		if ("clearAudioFocusLogic".equals(command)) {
			mAudioFocusLogicService = null;
			return null;
		}

		// 同步播放状态获取
		if (command.equals("isPlaying")) {
			return ("" + isPlaying()).getBytes();
		}
		// 同步缓冲状态获取
		if (command.equals("isBuffering")) {
			return ("" + isBuffering()).getBytes();
		}
		// 同步音乐模型获取
		if (command.equals("getCurrentMusicModel")) {
			if (!ProjectCfg.isFixCallFunction()) {
				if (!TextUtils.isEmpty(mMusicToolServiceName)) {
					return mRemoteMusicToolgetCurrentMusicModel;
				}
			}
			IMusic musicTool = getMusicTool();
			if (musicTool != null && musicTool.getCurrentMusicModel() != null) {
				return musicTool.getCurrentMusicModel().toString().getBytes();
			}
			return null;
		}
		// music应用通知更新
		if (command.equals("inner.isPlaying")) {
			if (AudioTxzImpl.isNewVersion()) {
				AudioTxzImpl.mTxzMusicToolisPlaying = Boolean
						.parseBoolean(new String(data));
			} else {
				MusicTxzImpl.mTxzMusicToolisPlaying = Boolean
						.parseBoolean(new String(data));
			}
			JNIHelper
					.logd("music status update: isPlaying="
							+ (MusicTxzImpl.mTxzMusicToolisPlaying | AudioTxzImpl.mTxzMusicToolisPlaying));
			if (TextUtils.isEmpty(mMusicToolServiceName)) {
				if (MusicTxzImpl.mTxzMusicToolisPlaying) {
					onBeginMusic();
				} else {
					onEndMusic();
				}
			}
			return null;
		}
		// music应用通知更新
		if (command.equals("inner.isBuffering")) {
			if (AudioTxzImpl.isNewVersion()) {
				AudioTxzImpl.mTxzMusicToolisBuffering = Boolean
						.parseBoolean(new String(data));
			} else {
				MusicTxzImpl.mTxzMusicToolisBuffering = Boolean
						.parseBoolean(new String(data));
			}
			JNIHelper
					.logd("music status update: isPlaying="
							+ (MusicTxzImpl.mTxzMusicToolisBuffering | AudioTxzImpl.mTxzMusicToolisBuffering));
			if (TextUtils.isEmpty(mMusicToolServiceName)) {
				if (MusicTxzImpl.mTxzMusicToolisBuffering) {
					onBeginMusic();
				} else {
					onEndMusic();
				}
			}
			return null;
		}
		if (command.equals("inner.musicModel")) {
			try {
				MusicTxzImpl.mTxzMusicToolCurrentMusicModel = MediaItem
						.parseFrom(data);
				if (TextUtils.isEmpty(mMusicToolServiceName)) {
					if (MusicTxzImpl.mTxzMusicToolisPlaying) {
						onBeginMusic();
					} else {
						onEndMusic();
					}
				}
				JNIHelper
						.logd("music status update: Model="
								+ getMediaSpeakInfo(MusicTxzImpl.mTxzMusicToolCurrentMusicModel.msgMedia));
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("inner.audioModel")) {// 音频对象
			try {
				LogUtil.logd("innerModer=" + new String(data)
						+ ",mMusicToolServiceName=" + mMusicToolServiceName);
				AudioTxzImpl.mTxzMusicToolCurrentMusicModel = new String(data);
				AudioImpl.mTxzMusicToolCurrentMusicModel = new String(data);
				if (TextUtils.isEmpty(mMusicToolServiceName)) {
					if (MusicTxzImpl.mTxzMusicToolisPlaying) {
						onBeginMusic();
					} else {
						onEndMusic();
					}
				}
				if (AudioTxzImpl.isNewVersion()) {
					JNIHelper.logd("music status update:new version= Model="
							+ AudioTxzImpl.mTxzMusicToolCurrentMusicModel);
				} else {
					JNIHelper
							.logd("music status update: Model="
									+ getMediaSpeakInfo(MusicTxzImpl.mTxzMusicToolCurrentMusicModel.msgMedia));
				}
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("inner.deleteMusic")) {
			try {
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_DELETE, data);
				JNIHelper.logd("music delete path="
						+ (data == null ? "null" : new String(data)));
			} catch (Exception e) {

			}
			return null;
		}

		if (command.equals("inner.favourMusic")) {
			try {
				MediaModel m = MediaModel.parseFrom(data);
				favouriteMusic(m, true);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("inner.unfavourMusic")) {
			try {
				MediaModel m = MediaModel.parseFrom(data);
				favouriteMusic(m, false);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("inner.cancelSerachMedia")) {
			cancelSearchMedia();
			return null;
		}
		if (command.equals("inner.searchMoreMedia")) {
			try {
				MediaModel m = MediaModel.parseFrom(data);
				searchMoreMedia(m);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("inner.refreshMusicList")) {
			try {
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_REFRESH_MUSIC_LIST);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("inner.updateMusicList")) {
			updateMusicList(data);
		}
		if (command.equals("inner.syncMusicList")) {
			try {
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_NEED_REFRESH_MEDIA_LIST);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("inner.refreshCategoryList")) {
			try {
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_CATEGORY_GET_LIST);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("txztool.playRandom")) {
			try {
				getTXZMusicTool().playRandom();
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("txztool.playFavourMusic")) {
			try {
				getTXZMusicTool().playFavourMusic();
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("txztool.favourMusic")) {
			try {
				favouriteMusicFromVoice(true, false);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("txztool.unfavourMusic")) {
			try {
				favouriteMusicFromVoice(false, false);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("txztool.playMusic")) {
			try {
				getTXZMusicTool().playMusic(
						MusicModel.fromString(new String(data)));
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("inner.getSongListByCategory")) {
			try {
				JNIHelper
						.sendEvent(
								UiEvent.EVENT_SYSTEM_MUSIC,
								UiMusic.SUBEVENT_MEDIA_CATEGORY_GET_SONG_LIST_BY_CATEGORY,
								data);
			} catch (Exception e) {
			}
			return null;
		}
		// 工具类接口
		if (command.equals("cleartool")) {
			mMusicToolServiceName = null;
			mMusicToolApp = null;
			mIsNotNeedTts = false;
			ServiceManager.getInstance().removeConnectionListener(
					mConnectionListener);
			invokeTXZMusic(null, "notifyMusicStatusChange", null); // 设置工具时刷新一下状态
			return null;
		}
		if (command.equals("settool")) {
			mMusicToolServiceName = null;
			mMusicToolApp = null;
			try {
				mIsNotNeedTts = !Boolean.parseBoolean(new String(data));
			} catch (Exception e) {
				e.printStackTrace();
			}
			ServiceManager.getInstance().addConnectionListener(
					mConnectionListener);
			ServiceManager.getInstance().sendInvoke(packageName,
					"notifyMusicStatusChange", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							// 记录工具
							if (data != null)
								mMusicToolServiceName = packageName;
						}
					});
			return null;
		}
		if (command.equals("setInnerTool")) {
			mMusicToolServiceName = null;
			mMusicToolApp = null;
			String tool = new String(data);
			if ("MUSIC_TOOL_KUWO".equals(tool)) {
				mMusicToolApp = MusicKuwoImpl.PACKAGE_NAME;
				return null;
			}
			if ("MUSIC_TOOL_TXZ".equals(tool)) {
				mMusicToolApp = ServiceManager.MUSIC;
				return null;
			}
			if ("MUSIC_TOOL_KAOLA".equals(tool)) {
				mMusicToolApp = MusicKaolaImpl.PACKAGE_NAME;
				return null;
			}
			return null;
		}
		// 状态变更通知接口
		if (command.equals("notifyMusicStatusChange")) {
			ServiceManager.getInstance().runOnServiceThread(new Runnable() {
				@Override
				public void run() {
					if (!TextUtils.isEmpty(mMusicToolServiceName)) {
						ServiceManager.getInstance().sendInvoke(
								mMusicToolServiceName, "tool.music.isPlaying",
								null, new GetDataCallback() {
									@Override
									public void onGetInvokeResponse(
											ServiceData data) {
										try {
											if (data != null)
												mRemoteMusicToolisPlaying = data
														.getBoolean();
										} catch (Exception e) {
										}
										if (mRemoteMusicToolisPlaying) {
											onBeginMusic();
										} else {
											onEndMusic();
										}
									}
								});
						ServiceManager.getInstance().sendInvoke(
								mMusicToolServiceName,
								"tool.music.getCurrentMusicModel", null,
								new GetDataCallback() {
									@Override
									public void onGetInvokeResponse(
											ServiceData data) {
										if (data != null)
											mRemoteMusicToolgetCurrentMusicModel = data
													.getBytes();
									}
								});
					}
				}
			}, 0);
			return null;
		}

		if (command.equals("showKuwoSearchResult")) {
			Boolean showSearchResult = Boolean.parseBoolean(new String(data));
			MusicKuwoImpl.SHOW_SEARCH_RESULT = showSearchResult;
			return null;
		}

		if (command.equals("syncMuicList")) {
			AndroidMediaLibrary.syncMusicList(MusicModel
					.collecionFromString(new String(data)));
			return null;
		}
		if (command.equals("syncExMuicList")) {
			AndroidMediaLibrary.syncExMusicList(MusicModel
					.collecionFromString(new String(data)));
			return null;
		}

		boolean useTool = (packageName == null || !ProjectCfg
				.isFixCallFunction());
		if (useTool) {
			// 使用工具执行操作
			if (!TextUtils.isEmpty(mMusicToolServiceName)) {
				ServiceManager.getInstance().sendInvoke(mMusicToolServiceName,
						"tool.music." + command, data, null);
				return null;
			}
		}

		IMusic musicTool = getMusicTool();
		if (musicTool == null)
			return null;

		// 同行者音乐远程接口实现
		if (command.equals("play")) {
			musicTool.start();
			return null;
		}
		if (command.equals("cont")) {
			if (musicTool instanceof MusicKuwoImpl) {
				((MusicKuwoImpl) musicTool).continuePlay();
			}
			return null;
		}
		// 同行者音乐远程接口实现
		if (command.equals("play.extra")) {
			if (musicTool instanceof ITxzMedia) {
				MusicModel currentMusicModel = musicTool.getCurrentMusicModel();
				if (null != currentMusicModel) {
					MediaControlUtil.play();
				} else {
					musicTool.start();
				}
			} else {
				musicTool.start();
			}
			return null;
		}
		if (command.equals("pause")) {
			if (isAudioToolSet()) {
				MediaControlUtil.pause();
			} else {
				musicTool.pause();
			}
			return null;
		}
		if (command.equals("exit")) {
			musicTool.pause();
			musicTool.exit();
			return null;
		}
		if (command.equals("next")) {
			// 紧急处理凯立德众鸿问题
			if (packageName != null && packageName.startsWith("com.zhonghong.")) {
				musicTool.next();
				return null;
			}

			if (isAudioToolSet()) {
				MediaControlUtil.next();
			} else {
				musicTool.next();
			}

			return null;
		}
		if (command.equals("prev")) {
			// 紧急处理凯立德众鸿问题
			if (packageName != null && packageName.startsWith("com.zhonghong.")) {
				musicTool.prev();
				return null;
			}
			if (isAudioToolSet()) {
				MediaControlUtil.prev();
			} else {
				musicTool.prev();
			}

			return null;
		}
		if (command.equals("playFavourMusic")) {
			musicTool.playFavourMusic();
			return null;
		}
		if (command.equals("favourMusic")) {
			musicTool.favourMusic();
			return null;
		}
		if (command.equals("unfavourMusic")) {
			musicTool.unfavourMusic();
			return null;
		}
		if (command.equals("switchModeLoopAll")) {
			musicTool.switchModeLoopAll();
			return null;
		}
		if (command.equals("switchModeLoopOne")) {
			musicTool.switchModeLoopOne();
			return null;
		}
		if (command.equals("switchModeRandom")) {
			musicTool.switchModeRandom();
			return null;
		}
		if (command.equals("switchSong")) {
			musicTool.switchSong();
			return null;
		}
		if (command.equals("playRandom")) {
			musicTool.playRandom();
			return null;
		}
		return null;
	}

	private boolean isAudioToolSet() {
		return com.txznet.txz.module.audio.AudioManager.getInstance()
				.isAudioToolSet();
	}

	/**
	 * 向音乐客户端发送第几个的数据
	 * 
	 * @param index
	 */
	public void sendResult(int index) {

		// TODO:
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.playmusiclist.index", ("" + index).getBytes(), null);
		// ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
		// "music.update.playMediaList", MessageNano.toByteArray(mMediaList),
		// null);
	}

	@Override
	public int initialize_AfterLoadLibrary() {
		// 初始化，library
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.get.version", null, new GetDataCallback() {

					@Override
					public int getTimeout() {
						return 60 * 1000;
					}

					@Override
					public void onGetInvokeResponse(ServiceData data) {
						if (null != data) {
							AudioTxzImpl.newVersion = data.getBoolean();
						} else {
							AudioTxzImpl.newVersion = false;
						}
					}
				});
		return super.initialize_AfterLoadLibrary();
	}
}
