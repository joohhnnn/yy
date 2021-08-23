package com.txznet.txz.module.remoteregcmd;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.CmdData;
import com.txz.ui.voice.VoiceData.KeyCmds;
import com.txz.ui.voice.VoiceData.OneCmd;
import com.txz.ui.voice.VoiceData.RmtCmdInfo;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.ui.win.record.RecorderWin;

public class RemoteRegCmdManager extends IModule {
	private static RemoteRegCmdManager sModuleInstance = null;

	private RemoteRegCmdManager() {

	}

	public static RemoteRegCmdManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (RemoteRegCmdManager.class) {
				if (sModuleInstance == null)
					sModuleInstance = new RemoteRegCmdManager();
			}
		}
		return sModuleInstance;
	}

	public void regComand(String remotePackageName, String data, String cmd) {
		String[] words = new String[1];
		words[0] = cmd;

		RmtCmdInfo rmtCmdInfo = new RmtCmdInfo();

		rmtCmdInfo.rmtData = data;
		rmtCmdInfo.rmtServName = remotePackageName;

		regCommand(words, rmtCmdInfo.toString().getBytes());
	}

	public void regComand(String remotePackageName, String data, String[] cmds) {
		if (cmds == null) {
			JNIHelper.loge("cmds == null");
			return;
		}
		RmtCmdInfo rmtCmdInfo = new RmtCmdInfo();

		rmtCmdInfo.rmtCmd = "";
		rmtCmdInfo.rmtData = data;
		rmtCmdInfo.rmtServName = remotePackageName;

		regCommand(cmds, MessageNano.toByteArray(rmtCmdInfo));
	}

	public void regCommand(String[] words, byte[] data) {
		if (words.length == 0)
			JNIHelper.loge("regcmd: word is null");
		
		KeyCmds cmds = new KeyCmds();
		cmds.cmds = new OneCmd[1];
		cmds.cmds[0] = new OneCmd();
		cmds.cmds[0].msgData = new CmdData();
		cmds.cmds[0].msgData.uint32Event = UiEvent.EVENT_REMOTE_THIRDPARTY_COMM;
		cmds.cmds[0].msgData.stringData = data;
		cmds.cmds[0].uint32Type = VoiceData.CMD_TYPE_REMOTE;
		// cmds.cmds[0].strResId = cmd;
		int wordCount = 0;
		wordCount = words.length;
		if (wordCount > 0) {
			cmds.cmds[0].word = new String[wordCount];
			for (int i = 0; i < wordCount; i++) {
				cmds.cmds[0].word[i] = words[i];
				JNIHelper.logd("regcmd:" + words[i]);
			}
		}
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_ADD_KEYWORDS_CMD, cmds);
	}

	public void unregCommand(String remotePackageName, String[] cmds) {
		if (cmds == null) {
			JNIHelper.loge("cmds == null");
			return;
		}
		RmtCmdInfo rmtCmdInfo = new RmtCmdInfo();
		// 没有使用的字段也必须填充内容，否则C层解析不出数据
		rmtCmdInfo.rmtCmd = "";
		rmtCmdInfo.rmtData = "";
		rmtCmdInfo.rmtServName = remotePackageName;

		unregCommand(cmds, MessageNano.toByteArray(rmtCmdInfo));

	}

	public void unregCommand(String remotePackageName, String cmd) {
		String[] words = new String[1];
		words[0] = cmd;

		RmtCmdInfo rmtCmdInfo = new RmtCmdInfo();

		rmtCmdInfo.rmtCmd = "";
		rmtCmdInfo.rmtData = "";
		rmtCmdInfo.rmtServName = remotePackageName;

		regCommand(words, rmtCmdInfo.toString().getBytes());
	}

	public int unregCommand(String[] words, byte[] data) {
		JNIHelper.logd("unregcmd:" + words);
		KeyCmds cmds = new KeyCmds();
		cmds.cmds = new OneCmd[1];
		cmds.cmds[0] = new OneCmd();
		cmds.cmds[0].msgData = new CmdData();
		cmds.cmds[0].msgData.uint32Event = UiEvent.EVENT_REMOTE_THIRDPARTY_COMM;
		cmds.cmds[0].msgData.stringData = data;
		cmds.cmds[0].uint32Type = VoiceData.CMD_TYPE_REMOTE;
		// cmds.cmds[0].strResId = cmd;
		int wordCount = 0;
		wordCount = words.length;
		if (wordCount > 0) {
			cmds.cmds[0].word = new String[wordCount];
			for (int i = 0; i < wordCount; i++) {
				cmds.cmds[0].word[i] = words[i];
				JNIHelper.logd("unregcmd:" + words[i]);
			}
		}
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_DEL_KEYWORDS_CMD, cmds);
		return 0;
	}

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_REMOTE_THIRDPARTY_COMM);
		// 注册需要处理的事件
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_REMOTE_THIRDPARTY_COMM: {
			try {
				RmtCmdInfo rmtCmdInfo = RmtCmdInfo.parseFrom(data);
				JNIHelper.logd("regcmd:" + rmtCmdInfo.rmtCmd + ";"
						+ rmtCmdInfo.rmtServName + ";" + rmtCmdInfo.rmtData);

				JSONBuilder json = new JSONBuilder();
				json.put("cmd", rmtCmdInfo.rmtCmd);
				json.put("data", rmtCmdInfo.rmtData);
				if (ConfigManager.getInstance().mCloseRecorderWin) {
					RecorderWin.closeDelay(500);
				}
				ServiceManager.getInstance().sendInvoke(rmtCmdInfo.rmtServName,
						"comm.asr.event.regcmdnotify",
						json.toString().getBytes(), null);
				
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
			}
		}
		}
		// 处理事件
		return super.onEvent(eventId, subEventId, data);
	}
}
