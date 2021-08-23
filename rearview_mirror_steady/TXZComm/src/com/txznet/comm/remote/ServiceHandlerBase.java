package com.txznet.comm.remote;

import android.text.TextUtils;

import com.txznet.comm.config.BaseConfiger;
import com.txznet.comm.config.NavControlConfiger;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.RecorderUtil;
import com.txznet.comm.remote.util.StatusUtil;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.VoiceprintRecognitionUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.version.ApkVersion;
import com.txznet.comm.version.TXZVersion;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZNetDataProvider;
import com.txznet.sdk.TXZTtsPlayerManager;
import com.txznet.sdk.TXZWheelControlManager;
import com.txznet.txz.plugin.PluginLoader;

public class ServiceHandlerBase {
    private static final String TAG = "music:receive:handler:AIDL:";

    private ServiceHandlerBase() {

	}

    // 预处理
    public static byte[] preInvoke(String packageName, String command,
                                   final byte[] data) {
        //增加日志表明：为什么关闭应用，不响应
        if (!TextUtils.equals("comm.log", command) && !TextUtils.equals("tool.loc.updateLoc", command)) {
            try {
                LogUtil.logd(TAG + GlobalContext.get().getPackageName() + ",from:" + packageName + "/" + command);
            } catch (Exception e) {
                LogUtil.logd(TAG + "null" + ",from:" + packageName + "/" + command);
            }
        }

        if (command.startsWith("comm.tts.event.")) {
            return TtsUtil.preInvokeTtsEvent(packageName, command.substring("comm.tts.event.".length()), data);
        }
        if (command.startsWith("comm.asr.event")) {
            return preInvokeAsrEvent(packageName, command, data);
        }
        if (command.startsWith("comm.status.")) {
            return StatusUtil.notifyStatus(command.substring("comm.status."
                    .length()));
        }
        if (command.startsWith("comm.record.event")) {
            return preInvokeRecordEvent(packageName, command, data);
        }
        if (command.startsWith(VoiceprintRecognitionUtil.PREFIX_CALLBACK_EVENT)) {
            return preInvokeVoiceprintRecognitionEvent(packageName, command, data);
        }
        if (command.startsWith("comm.subscribe.broadcast")) {
            ServiceManager.getInstance()
                    .sendInvoke(packageName, "", null, null);
            return null;
        }
        if (command.startsWith("comm.config.")) {
            return preInvokeConfigEvent(packageName,
                    command.substring("comm.config.".length()), data);
        }
        if (command.equals("comm.log.setConsoleLogLevel")) {
            try {
                AppLogicBase.runOnBackGround(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.setConsoleLogLevel(Integer.parseInt(new String(data)));
                    }
                });
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("comm.log.setFileLogLevel")) {
            try {
                LogUtil.setFileLogLevel(Integer.parseInt(new String(data)));
            } catch (Exception e) {
            }
            return null;
        }
        if (command.startsWith("comm.text.event")) {
            return preInvokeTextEvent(packageName, command, data);
        }
        if (command.startsWith("comm.plugin.")) {
            return PluginLoader.invokePluginCommand(packageName,
                    command.substring("comm.plugin.".length()), data);
        }
        if (command.startsWith("comm.update.")) {
//			return UpdateCenter.process(packageName,
//					command.substring("comm.update.".length()), data);
		}
		// 获取包信息
		if (command.equals("comm.PackageInfo")) {
			// LogUtil.logd("request VERSION from: " + packageName +", result: "
			// + ApkVersion.versionName);
			JSONBuilder json = new JSONBuilder();
			json.put("versionCode", ApkVersion.versionCode);
			json.put("versionName", ApkVersion.versionName);
			json.put("sourceDir",
					GlobalContext.get().getApplicationInfo().sourceDir);
			json.put("versionCompile", TXZVersion.PACKTIME + "_" + TXZVersion.SVNVERSION);
			return json.toBytes();
		}
		if (command.startsWith("comm.netdata.resp.")) {
			return preInvokeNetDataEvent(packageName, command, data);
		}
		if (command.startsWith("txz.wheelcontrol.notify.")) {
			return preInvokeWheelControlEvent(packageName, command, data);
		}
		if (command.startsWith(BaseConfiger.INVOKE_COMM_PREFIX)) {
			return preInvokeCommConfigEvent(packageName, command.substring(BaseConfiger.INVOKE_COMM_PREFIX.length()),
					data);
		}
		if (command.startsWith(TXZTtsPlayerManager.TTS_PLAYER_CMD_PREFIX)) {
			return TXZTtsPlayerManager.preInvokeTtsPlayerEvent(packageName,command.substring(TXZTtsPlayerManager.TTS_PLAYER_CMD_PREFIX.length()),data);
		}
		return null;
	}
	
	private static byte[] preInvokeCommConfigEvent(String packageName, String command, byte[] data) {
		LogUtil.logd("preInvokeCommConfigEvent  cmd" + command + ",from:" + packageName);
		if(command.startsWith(BaseConfiger.INVOKE_NAV_CONTROL)){
			return NavControlConfiger.getInstance().onEvent(packageName, command.substring(BaseConfiger.INVOKE_NAV_CONTROL.length()), data);
		}
		return null;
	}
	
	private static byte[] preInvokeWheelControlEvent(String packageName, String command, byte[] data){
		return TXZWheelControlManager.getInstance().notifyCallback(command, data);
	}

	private static byte[] preInvokeConfigEvent(String packageName,
			String command, final byte[] data) {
		if (command.equals("showHelpInfos")) {
			ConfigUtil.setShowHelpInfos(Boolean.parseBoolean(new String(data)));
			return null;
		}
		if (command.equals("showSettings")) {
			ConfigUtil.setShowSettings(Boolean.parseBoolean(new String(data)));
			return null;
		}
		if (command.equals("showCloseIcon")) {
			ConfigUtil.setShowCloseIcon(Boolean.parseBoolean(new String(data)));
			return null;
		}
		if (command.equals("syncData")) {
			new Thread() {
				@Override
				public void run() {
					ConfigUtil.notifyConfigChanged(new String(data));
				}
			}.start();
			return null;
		}
		if (command.equals("tts.setDefaultAudioStream")) {
			TtsUtil.DEFAULT_TTS_STREAM = Integer.parseInt(new String(data));
			return null;
		}
		if (command.equals("restore")) {
			ConfigUtil.notifyRestoreToDefault();
			return null;
		}
		return null;
	}

	private static byte[] preInvokeTextEvent(String packageName,
			String command, byte[] data) {
		if (command.equals("comm.text.event.result")) {
			TextUtil.notifyTextCallback("result", data);
			return null;
		}

		if (command.equals("comm.text.event.cancel")) {
			TextUtil.notifyTextCallback("cancel", data);
			return null;
		}

		if (command.equals("comm.text.event.error")) {
			TextUtil.notifyTextCallback("error", data);
			return null;
		}

		return null;
	}
	
	private static byte[] preInvokeNetDataEvent(String packageName, String command, byte[] data){
		return TXZNetDataProvider.getInstance().notifyCallback(command.substring("comm.netdata.resp.".length()), data);
	}


	private static byte[] preInvokeAsrEvent(String packageName, String command,
			byte[] data) {
		if (command.equals("comm.asr.event.success")) {
			AsrUtil.notifyCallback("success", data);
		} else if (command.equals("comm.asr.event.cancel")) {
			AsrUtil.notifyCallback("cancel", data);
		} else if (command.equals("comm.asr.event.error")) {
			AsrUtil.notifyCallback("error", data);
		} else if (command.equals("comm.asr.event.end")) {
			AsrUtil.notifyCallback("end", data);
		} else if (command.equals("comm.asr.event.start")) {
			AsrUtil.notifyCallback("start", data);
		} else if (command.equals("comm.asr.event.abort")) {
			AsrUtil.notifyCallback("abort", data);
		} else if (command.equals("comm.asr.event.volume")) {
			AsrUtil.notifyCallback("volume", data);
		} else if (command.equals("comm.asr.event.regcmdnotify")) {
			AsrUtil.notifyCallback("regnotify", data);
		} else if (command.equals("comm.asr.event.onWakeupAsrResult")) {
			AsrUtil.notifyWakeupAsrResult(new String(data));
		} else if(command.equals("comm.asr.event.onTtsEnd")){
			AsrUtil.notifyOnTtsEndResult(new String(data));
		} else if(command.equals("comm.asr.event.onTtsBegin")){
		AsrUtil.notifyOnTtsBeginResult(new String(data));
        } else if (command.equals("comm.asr.event.onSpeechEnd")) {
            AsrUtil.notifyonSpeechEndResult(new String(data));
        } else if (command.equals("comm.asr.event.onSpeechBegin")) {
            AsrUtil.notifyonSpeechBeginResult(new String(data));
        }

		return null;
	}

	private static byte[] preInvokeRecordEvent(String packageName,
			String command, byte[] data) {
		if (command.equals("comm.record.event.begin")) {
			RecorderUtil.notifyCallback("begin", data);
		} else if (command.equals("comm.record.event.end")) {
			RecorderUtil.notifyCallback("end", data);
		} else if (command.equals("comm.record.event.parse")) {
			RecorderUtil.notifyCallback("parse", data);
		} else if (command.equals("comm.record.event.cancel")) {
			RecorderUtil.notifyCallback("cancel", data);
		} else if (command.equals("comm.record.event.error")) {
			RecorderUtil.notifyCallback("error", data);
		} else if (command.equals("comm.record.event.mp3buf")) {
			RecorderUtil.notifyCallback("mp3buf", data);
		} else if (command.equals("comm.record.event.mute")) {
			RecorderUtil.notifyCallback("mute", data);
		} else if (command.equals("comm.record.event.mutetimeout")) {
			RecorderUtil.notifyCallback("mutetimeout", data);
		} else if (command.equals("comm.record.event.speechtimeout")) {
			RecorderUtil.notifyCallback("speechtimeout", data);
		} else if (command.equals("comm.record.event.volume")) {
			RecorderUtil.notifyCallback("volume", data);
		}
		return null;
	}
    private static byte[] preInvokeVoiceprintRecognitionEvent(String packageName, String command, byte[] data) {
        return VoiceprintRecognitionUtil.notifyCallback(command, data);
    }
}
