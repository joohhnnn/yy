package com.txznet.txz.module.audio;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

import com.txz.ui.event.UiEvent;
import com.txz.ui.radio.UiRadio;
import com.txz.ui.radio.UiRadio.RADIOModel;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.audio.IAudio;
import com.txznet.txz.component.audio.kaola.AudioKaoLaImpl;
import com.txznet.txz.component.audio.tingting.AudioTingTingImpl;
import com.txznet.txz.component.audio.txz.AudioImpl;
import com.txznet.txz.component.audio.txz.AudioToMusicImpl;
import com.txznet.txz.component.audio.xmly.AudioXmly;
import com.txznet.txz.component.music.IMusic;
import com.txznet.txz.component.music.txz.AudioTxzImpl;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.MediaControlUtil;

import android.text.TextUtils;

public class AudioManager extends IModule {
	private Map<String, IAudio> mAudioTools = new HashMap<String, IAudio>();
	private String mAudioType;

	private static AudioManager sInstance = new AudioManager();
	private IAudio audioTXZ;

	private AudioManager() {
		IAudio mTtTool = new com.txznet.txz.component.audio.tingting.AudioTingTingImpl();
		IAudio mKlTool = new AudioKaoLaImpl();
		IAudio mXmlyTool = AudioXmly.getInstance();
		audioTXZ = new AudioImpl();

		mAudioTools.put(mTtTool.getPackageName(), mTtTool);
		mAudioTools.put(mKlTool.getPackageName(), mKlTool);
		mAudioTools.put(audioTXZ.getPackageName(), audioTXZ);
		mAudioTools.put(mXmlyTool.getPackageName(), mXmlyTool);
		mAudioType = audioTXZ.getPackageName();
	}

	public static AudioManager getInstance() {
		return sInstance;
	}

	public int initialize_BeforeLoadLibrary() {
		return super.initialize_BeforeLoadLibrary();
	}

	public int initialize_AfterLoadLibrary() {
		return super.initialize_AfterLoadLibrary();
	}

	public int initialize_BeforeStartJni() {
		return super.initialize_AfterStartJni();
	}

	public int initialize_AfterStartJni() {
		regEvent(UiEvent.EVENT_CUSTOM_RADIO, UiRadio.SUBEVENT_RADIO_PLAY);
		regEvent(UiEvent.EVENT_CUSTOM_RADIO, UiRadio.SUBEVENT_RADIO_PAUSE);
		regEvent(UiEvent.EVENT_CUSTOM_RADIO, UiRadio.SUBEVENT_RADIO_NEXT);
		regEvent(UiEvent.EVENT_CUSTOM_RADIO, UiRadio.SUBEVENT_RADIO_PREV);
		regEvent(UiEvent.EVENT_CUSTOM_RADIO, UiRadio.SUBEVENT_RADIO_EXIT);
		regCommandWithResult("AUDIO_CMD_PLAY");
		regCommandWithResult("AUDIO_CMD_STOP");
		regCommandWithResult("AUDIO_CMD_EXIT");
		regCommandWithResult("AUDIO_CMD_NEXT");
		regCommandWithResult("AUDIO_CMD_PREV");
		return super.initialize_AfterStartJni();
	}

	private boolean preInvokeAudioSence(JSONBuilder builder) {
		if (SenceManager.getInstance().noneedProcSence("audio",
				builder.toBytes())) {
			return true;
		}
		// 判断是否存在可用电台工具
		if (null == getLocalAudioTool()
				&& TextUtils.isEmpty(mAudioToolRemoteName)) {
			String spk = NativeData.getResString("RS_AUDIO_NO_AUDIO");
			RecorderWin.speakTextWithClose(spk, null);
			return true;
		}
		return false;
	}

	@Override
	public int onCommand(String cmd, String keywords, String voiceString) {
		JSONBuilder builder = new JSONBuilder().put("scene", "audio");
		builder.put("scene", "audio");
		builder.put("text", voiceString);
		// builder.put("keywords", keywords);
		if ("AUDIO_CMD_PLAY".equals(cmd)) {
			builder.put("action", "play");
			if (preInvokeAudioSence(builder))
				return 0;
			play();
		} else if ("AUDIO_CMD_STOP".equals(cmd)) {
			builder.put("action", "pause");
			if (preInvokeAudioSence(builder))
				return 0;
			pause();
		} else if ("AUDIO_CMD_EXIT".equals(cmd)) {
			builder.put("action", "exit");
			if (preInvokeAudioSence(builder))
				return 0;
			exit();
		} else if ("AUDIO_CMD_PREV".equals(cmd)) {
			builder.put("action", "prev");
			if (preInvokeAudioSence(builder))
				return 0;
			prev();
		} else if ("AUDIO_CMD_NEXT".equals(cmd)) {
			builder.put("action", "next");
			if (preInvokeAudioSence(builder))
				return 0;
			next();
		}
		return 0;
	}

	/**
	 * 检测是否设置了电台工具（由音乐工具转换的电台工具不算在内）
	 * 
	 * @return
	 */
	public boolean isAudioToolSet() {
		IAudio localAudioTool = getLocalAudioTool();

		if (localAudioTool == null) {
			return false;
		}

		if (localAudioTool instanceof AudioToMusicImpl) {
			return false;
		}

		return true;
	}
	public boolean hasRemoteTool() {
		return !TextUtils.isEmpty(mAudioToolRemoteName);
	}

	public void cancelAllRequest() {
		IAudio audio = getLocalAudioTool();
		if (audio != null) {
			audio.cancelRequest();
		}
	}

	public IAudio getLocalAudioTool() {
		String type = mAudioType;
		if (!TextUtils.isEmpty(type)
				&& !PackageManager.getInstance().checkAppExist(type)) {
			type = "";
		}

		if (TextUtils.isEmpty(type)) {
			if (PackageManager.getInstance().checkAppExist(
					audioTXZ.getPackageName())) {
				type = audioTXZ.getPackageName();
			}
			if (PackageManager.getInstance().checkAppExist(
					AudioKaoLaImpl.PACKAGE_NAME)) {
				type = AudioKaoLaImpl.PACKAGE_NAME;
			} else if (PackageManager.getInstance().checkAppExist(
					AudioXmly.PACKAGE_NAME)) {
				type = AudioXmly.PACKAGE_NAME;
			} else if (PackageManager.getInstance().checkAppExist(
					AudioTingTingImpl.PACKAGE_NAME)) {
				type = AudioTingTingImpl.PACKAGE_NAME;
			}
		}

		IAudio ret = null;

		synchronized (mAudioTools) {
			ret = mAudioTools.get(type);
		}

		if (ret == null) {
			IMusic musicTool = MusicManager.getInstance().getMusicTool();
			if (musicTool != null) {
				return new AudioToMusicImpl(musicTool);
			}
		}

		return ret;
	}

	public void play(final String jsonModel) {
		if (!TextUtils.isEmpty(mAudioToolRemoteName)) {
			RecorderWin.speakTextWithClose("", new Runnable() {

				@Override
				public void run() {
					invokeTXZAudio("", "playFm", jsonModel.getBytes());
				}
			});
			return;
		}
		final IAudio audioTool = getLocalAudioTool();
		if (audioTool instanceof AudioKaoLaImpl
				|| audioTool instanceof AudioImpl) {
			audioTool.playFm(jsonModel);
			return;
		}

		if ((audioTool instanceof AudioXmly) && AudioXmly.SHOW_SEL_LIST) {
			String search = NativeData.getResString("RS_AUDIO_SEARCH");
			String spk = NativeData.getResString("RS_VOICE_DOING_COMMAND").replace("%CMD%", search);
			TtsUtil.speakTextOnRecordWin(spk, false, false,
					new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							audioTool.playFm(jsonModel);
						}
					});

			return;
		}
		String play = NativeData.getResString("RS_AUDIO_PLAY");
		String spk = NativeData.getResString("RS_VOICE_DOING_COMMAND").replace("%CMD%", play);
		RecorderWin.speakTextWithClose(spk
				+ MusicManager.getInstance().genMediaModelTitle(jsonModel),
				new Runnable() {
					@Override
					public void run() {
						try {
							audioTool.playFm(jsonModel);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	}

	private void play() {
		if (!TextUtils.isEmpty(mAudioToolRemoteName)) {
			RecorderWin.speakTextWithClose("", new Runnable() {

				@Override
				public void run() {
					invokeTXZAudio("", "play", null);
					// MediaControlUtil.play();
				}
			});
			return;
		}
		String play = NativeData.getResString("RS_AUDIO_PLAY_AUDIO");
		String spk = NativeData.getResString("RS_VOICE_DOING_COMMAND").replace("%CMD%", play);
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				try {
					IAudio audioTool = getLocalAudioTool();
					audioTool.start();
					// MediaControlUtil.play();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void prev() {
		if (!TextUtils.isEmpty(mAudioToolRemoteName)) {
			RecorderWin.speakTextWithClose("", new Runnable() {

				@Override
				public void run() {
					 invokeTXZAudio("", "prev", null);
					//MediaControlUtil.prev();
				}
			});
			return;
		}
		String pre = NativeData.getResString("RS_AUDIO_SWITCH_PRE");
		String spk = NativeData.getResString("RS_VOICE_DOING_COMMAND").replace("%CMD%", pre);
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				try {
					// IAudio audioTool = getLocalAudioTool();
					// audioTool.prev();
					MediaControlUtil.prev();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void next() {
		if (!TextUtils.isEmpty(mAudioToolRemoteName)) {
			RecorderWin.speakTextWithClose("", new Runnable() {

				@Override
				public void run() {
					invokeTXZAudio("", "next", null);
					//MediaControlUtil.next();
				}
			});
			return;
		}
		String next = NativeData.getResString("RS_AUDIO_SWITCH_NEXT");
		String spk = NativeData.getResString("RS_VOICE_DOING_COMMAND").replace("%CMD%", next);
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				try {
					// IAudio audioTool = getLocalAudioTool();
					// audioTool.next();
					MediaControlUtil.next();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void pause() {
		if (!TextUtils.isEmpty(mAudioToolRemoteName)) {
			RecorderWin.speakTextWithClose("", new Runnable() {

				@Override
				public void run() {
					invokeTXZAudio("", "pause", null);
					//MediaControlUtil.pause();
				}
			});
			return;
		}
		String pause = NativeData.getResString("RS_AUDUI_PAUSE");
		String spk = NativeData.getResString("RS_VOICE_DOING_COMMAND").replace("%CMD%", pause);
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				try {
					// IAudio audioTool = getLocalAudioTool();
					// audioTool.pause();
					MediaControlUtil.pause();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void exit() {
		if (!TextUtils.isEmpty(mAudioToolRemoteName)) {
			RecorderWin.speakTextWithClose("", new Runnable() {

				@Override
				public void run() {
					invokeTXZAudio("", "exit", null);
				}
			});
			return;
		}
		String exit = NativeData.getResString("RS_AUDIO_EXIT");
		String spk = NativeData.getResString("RS_VOICE_DOING_COMMAND").replace("%CMD%", exit);
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				try {
					IAudio audioTool = getLocalAudioTool();
					audioTool.exit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public int onEvent(int eventId, int subEventId, byte[] data) {
		if (eventId == UiEvent.EVENT_CUSTOM_RADIO) {
			if (getLocalAudioTool() == null
					&& TextUtils.isEmpty(mAudioToolRemoteName)) {
				LogUtil.logd("audio::App::" + getLocalAudioTool()
						+ ",mAudioToolRemoteName：：" + mAudioToolRemoteName
						+ ",mAudioTools::" + mAudioTools.toString());
				String spk = NativeData.getResString("RS_AUDIO_NO_AUDIO");
				RecorderWin.speakTextWithClose(spk, null);
				return 0;
			}
			if (getLocalAudioTool() != null
					&& getLocalAudioTool().equals(
							mAudioTools.get(audioTXZ.getPackageName()))
					&& !AudioTxzImpl.newVersion) {
				LogUtil.logd("audio::App::" + getLocalAudioTool()
						+ ",mAudioToolRemoteName：：" + mAudioToolRemoteName
						+ ",mAudioTools::" + mAudioTools.toString()
						+ ",AudioTxzImpl.newVersion=" + AudioTxzImpl.newVersion);
				String spk = NativeData.getResString("RS_AUDIO_NO_AUDIO");
				RecorderWin.speakTextWithClose(spk, null);
				return 0;
			}

			switch (subEventId) {
			case UiRadio.SUBEVENT_RADIO_PLAY:
				try {
					UiRadio.RADIOModel radioModel = RADIOModel.parseFrom(data);
					// String strKeyWord = "";
					// for (int i = 0; i < radioModel.rptStrKeywords.length;
					// i++) {
					// strKeyWord = strKeyWord + radioModel.rptStrKeywords[i] +
					// " ";
					// }
					JSONBuilder builder = new JSONBuilder();
					if (null != radioModel.rptStrArtist) {
						builder.put("artists", radioModel.rptStrArtist);
					}
					if (null != radioModel.strTitle) {
						builder.put("title", radioModel.strTitle);
					}
					if (null != radioModel.strCategory) {
						builder.put("category", radioModel.strCategory);
					}
					if (null != radioModel.rptStrKeywords) {
						builder.put("keywords", radioModel.rptStrKeywords);
					}
					if (null != radioModel.strTag) {
						builder.put("tag", radioModel.strTag);
					}
					if (null != radioModel.strAlbum) {
						builder.put("album", radioModel.strAlbum);
					}
					LogUtil.logd("RADIOModel::" + builder.toString());
					play(builder.toString());
					// play(strKeyWord);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case UiRadio.SUBEVENT_RADIO_PAUSE:
				break;
			case UiRadio.SUBEVENT_RADIO_NEXT:
				break;
			case UiRadio.SUBEVENT_RADIO_PREV:
				break;
			case UiRadio.SUBEVENT_RADIO_EXIT:
				break;
			default:
				break;
			}
		}
		return 0;
	}

	private String mAudioToolRemoteName;

	ConnectionListener mConnectionListener = new ConnectionListener() {

		@Override
		public void onDisconnected(String serviceName) {
			if (serviceName.equals(mAudioToolRemoteName)) {
				invokeTXZAudio(null, "cleartool", null);
			}
		}

		@Override
		public void onConnected(String serviceName) {
		}
	};

	public void onBeginAudio() {
		String command = "comm.status.onBeginAudio";
		ServiceManager.getInstance().broadInvoke(command, null);
	}

	public void onEndAudio() {
		String command = "comm.status.onEndAudio";
		ServiceManager.getInstance().broadInvoke(command, null);
	}

	private boolean mRemoteAudioToolPlaying;

	public boolean isPlaying() {
		if (!ProjectCfg.isFixCallFunction()) {
			if (!TextUtils.isEmpty(mAudioToolRemoteName)) {
				return mRemoteAudioToolPlaying;
			}
		}

		IAudio ia = getLocalAudioTool();
		if (ia != null) {
			// TODO 应针对每个播放器做播放处理
			return (ia.getCurrentFmName() == null || ia.getCurrentFmName()
					.equals("")) ? false : true;
		}
		return false;
	}

	public byte[] invokeTXZAudio(final String packageName, String command,
			byte[] data) {
		if (command.equals("cleartool")) {
			mAudioType = null;
			mAudioToolRemoteName = null;
			ServiceManager.getInstance().removeConnectionListener(
					mConnectionListener);
			invokeTXZAudio(null, "notifyMusicStatusChange", null);
		}
		if (command.equals("setInnerTool")) {
			mAudioType = null;
			mAudioToolRemoteName = null;
			try {
				while (true) {
					if (data == null) {
						break;
					}

					String tool = new String(data);
					if (tool.equals("")) {
						break;
					}
					if (tool.equals("AUDIO_TXZ")) {
						mAudioType = audioTXZ.getPackageName();
					} else if (tool.equals("AUDIO_KL")) {
						mAudioType = AudioKaoLaImpl.PACKAGE_NAME;
					} else if (tool.equals("AUDIO_TT")) {
						mAudioType = AudioTingTingImpl.PACKAGE_NAME;
					} else if (tool.equals("AUDIO_XMLY")) {
						mAudioType = AudioXmly.PACKAGE_NAME;
					}
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			ServiceManager.getInstance().addConnectionListener(
					mConnectionListener);
			ServiceManager.getInstance().sendInvoke(packageName,
					"notifyMusicStatusChange", null, new GetDataCallback() {

						@Override
						public void onGetInvokeResponse(ServiceData data) {
							if (data != null) {
								mAudioToolRemoteName = packageName;
							}
						}
					});
		}
		if (command.equals("notifyMusicStatusChange")) {
			ServiceManager.getInstance().runOnServiceThread(new Runnable() {

				@Override
				public void run() {
					if (!TextUtils.isEmpty(mAudioToolRemoteName)) {
						ServiceManager.getInstance().sendInvoke(
								mAudioToolRemoteName, "tool.audio.isPlaying",
								null, new GetDataCallback() {

									@Override
									public void onGetInvokeResponse(
											ServiceData data) {
										try {
											if (data != null) {
												mRemoteAudioToolPlaying = data
														.getBoolean();
											}
											if (mRemoteAudioToolPlaying) {
												onBeginAudio();
											} else {
												onEndAudio();
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
					}
				}
			}, 0);
		}

		boolean useTool = (packageName == null || !ProjectCfg
				.isFixCallFunction());
		if (useTool) {
			if (!TextUtils.isEmpty(mAudioToolRemoteName)) {
				ServiceManager.getInstance().sendInvoke(mAudioToolRemoteName,
						"tool.audio." + command, data, null);
				return null;
			}
		}
		if (command.equals("isPlaying")) {
			return (isPlaying() + "").getBytes();
		} else if (command.equals("play")) {
			play();
		} else if (command.equals("prev")) {
			prev();
		} else if (command.equals("next")) {
			next();
		} else if (command.equals("pause")) {
			pause();
		} else if (command.equals("exit")) {
			exit();
		} else if (command.equals("playFm")) {
			try {
				play(new String(data));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (command.equals("setkey.xmly")) {
			JSONBuilder builder = new JSONBuilder(data);
			String appSecret = builder.getVal("appSecret", String.class);
			String appKey = builder.getVal("appKey", String.class);
			String pkgName = builder.getVal("pkgName", String.class);
			AudioXmly.getInstance().setAppkey(appSecret, appKey, pkgName);
		}
		if(command.equals("showSelect.xmly")){
			boolean show = Boolean.parseBoolean(new String(data));
			AudioXmly.SHOW_SEL_LIST = show;
		}
		return null;
	}
}