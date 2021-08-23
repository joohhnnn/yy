package com.txznet.txz.module.audio;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.sdk.media.constant.InvokeConstants;
import com.txznet.txz.component.media.MediaPriorityManager;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.media.base.MediaToolConstants;
import com.txznet.txz.component.media.chooser.AudioPriorityChooser;
import com.txznet.txz.component.media.loader.MediaToolManager;
import com.txznet.txz.component.media.model.MediaModel;
import com.txznet.txz.component.media.remote.RemoteAudioTool;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;

import org.json.JSONObject;

public class AudioManager extends IModule {
	public static final String TAG = "CORE:AUDIO:";
	private static final String AUDIO_PLUGIN_VERSION = "1.0";

	private static AudioManager sInstance = new AudioManager();

	private AudioManager() {
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



	@Override
	public int initialize_addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("com.audio.command.", new CommandProcessor() {

			@Override
			public Object invoke(String command, Object[] params) {
				if (StringUtils.isNotEmpty(command)) {
					if (params!=null&&params.length>0) {
						return AudioManager.getInstance().invokeTXZAudio("com.txznet.audio.plugin", command,(byte[])params[0]);
					}
					return AudioManager.getInstance().invokeTXZAudio("com.txznet.audio.plugin", command,null);
				}
				return null;
			}
		});
		return super.initialize_addPluginCommandProcessor();
	}

	public int initialize_AfterStartJni() {
		return super.initialize_AfterStartJni();
	}

	@Override
	public int onCommand(String cmd, String keywords, String voiceString) {
        // TODO: 2018/5/13 这种为特定电台注册的命令字要确定下要不要合并至新版语音交互中
        //AudioXmly.getInstance().onCommand(cmd);
        return 0;
	}

	/**
	 * 检测是否设置了电台工具（由音乐工具转换的电台工具不算在内）
	 *
	 * @return
	 */
	public boolean isAudioToolSet() {
        return null != AudioPriorityChooser.getInstance().getMediaTool(null);
    }

	public boolean hasRemoteTool() {
        return RemoteAudioTool.getInstance().isEnabled();
	}

	public void cancelAllRequest() {
        // TODO: 2018/5/13 cancelRequest 逻辑重构
	}

    // TODO: 2018/5/13 电台上报迁移
    private static void reportPlayAudio(String name) {
        ReportUtil.doReport(new ReportUtil.Report.Builder().setType("audio")
                .setAction("play").setSessionId().putExtra("executor", name)
                .buildCommReport());
    }

	public int onEvent(int eventId, int subEventId, byte[] data) {
		JNIHelper.logd(TAG+"event["+eventId+"|"+subEventId+"]");
		return 0;
	}

	ConnectionListener mConnectionListener = new ConnectionListener() {

		@Override
		public void onDisconnected(String serviceName) {
            if (serviceName.equals(RemoteAudioTool.getInstance().getPackageName())) {
				invokeTXZAudio(null, "cleartool", null);
			}
		}

		@Override
		public void onConnected(String serviceName) {
		}
	};

    // TODO: 2018/5/13 需要将广播通知逻辑转移至MediaPriorityManager
    public void onBeginAudio() {
        String command = "comm.status.onBeginAudio";
        ServiceManager.getInstance().broadInvoke(command, null);
    }

	public void onEndAudio() {
		String command = "comm.status.onEndAudio";
		ServiceManager.getInstance().broadInvoke(command, null);
	}

    public boolean isPlaying() {
        return IMediaTool.PLAYER_STATUS.PLAYING ==
                MediaPriorityManager.getInstance().getCurrentMediaToolStatus();
    }

	public byte[] invokeTXZAudio(final String packageName, String command,
			byte[] data) {
		JNIHelper.logd(TAG+"receiver:command:" + command);
        if (command.startsWith("sdk.")) {
            return RemoteAudioTool.getInstance().onAudioSdkInvoke(packageName, command.substring
                    ("sdk.".length()), data);
        }

		if (command.equals("cleartool")) {
            RemoteAudioTool.getInstance().clearPackageName();
			// 默认工具和远程工具不再冲突, 指定时不再关联处理
            //AudioPriorityChooser.getInstance().setDefaultTool("");
			ServiceManager.getInstance().removeConnectionListener(
					mConnectionListener);
			invokeTXZAudio(null, "notifyMusicStatusChange", null);
		}
		if (command.equals("setInnerTool")) {
            if (null != data) {
				// 默认工具和远程工具不再冲突, 指定时不再关联处理
                //RemoteAudioTool.getInstance().clearPackageName();
                AudioPriorityChooser.getInstance().setDefaultTool(new String(data));
            }
        } else if ("setTool".equals(command)) {
			// 默认工具和远程工具不再冲突, 指定时不再关联处理
            //AudioPriorityChooser.getInstance().setDefaultTool("");
			JSONBuilder paramBuilder = new JSONBuilder(data);
            RemoteAudioTool.getInstance().setPackageName(packageName, paramBuilder);
        }

		if (command.equals("isPlaying")) {
			return (isPlaying() + "").getBytes();
		} else if (command.equals("play")) {
//			MediaPriorityManager.getInstance().continuePlay(true,
//					MediaPriorityManager.PRIORITY_TYPE.AUDIO);
			MediaPriorityManager.getInstance().open(true,
					MediaPriorityManager.PRIORITY_TYPE.AUDIO, true);
		} else if (command.equals("prev")) {
			MediaPriorityManager.getInstance().prev(true,
					MediaPriorityManager.PRIORITY_TYPE.AUDIO);
		} else if (command.equals("next")) {
			MediaPriorityManager.getInstance().next(true,
					MediaPriorityManager.PRIORITY_TYPE.AUDIO);
		} else if (command.equals("pause")) {
			MediaPriorityManager.getInstance().pause(true,
					MediaPriorityManager.PRIORITY_TYPE.AUDIO);
		} else if (command.equals("exit")) {
			MediaPriorityManager.getInstance().exit(true);
		} else if (command.equals("playFm")) {
			try {
				MediaPriorityManager.getInstance().play(true,
						MediaPriorityManager.PRIORITY_TYPE.AUDIO,
						MediaModel.fromJsonObject(new JSONObject(new String(data))));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if (command.equals("setkey.xmly")) {
			MediaToolManager.getInstance().sendInvoke(MediaToolConstants.PACKAGE_AUDIO_XMLY,
					"set.key", data);
        } else if (command.equals("showSelect.xmly")) {
			boolean show = Boolean.parseBoolean(new String(data));
			AudioPriorityChooser.getInstance().setSearchConfig(MediaToolConstants.TYPE_AUDIO_XMLY,
					show, 10000);
        } else if (InvokeConstants.INVOKE_SEARCH_CONFIG.equals(command)) {
			JSONBuilder builder = new JSONBuilder(data);
			String type = builder.getVal(InvokeConstants.PARAM_SEARCH_TOOL_TYPE, String.class);
			boolean showResult = builder.getVal(InvokeConstants.PARAM_SEARCH_SHOW_RESULT,
					boolean.class);
			int timeout = builder.getVal(InvokeConstants.PARAM_SEARCH_TIMEOUT, int.class);
			AudioPriorityChooser.getInstance().setSearchConfig(type, showResult, timeout);
			return null;
		}
		return null;
	}
}