package com.txznet.txz.module;

import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.CmdData;
import com.txz.ui.voice.VoiceData.KeyCmds;
import com.txz.ui.voice.VoiceData.OneCmd;
import com.txznet.txz.jni.JNIHelper;

public abstract class IModule {
	public static final int ERROR_SUCCESS = 0; // 初始化成功
	public static final int ERROR_FAILURE = 1; // 初始化失败，但不影响应用
	public static final int ERROR_ABORT = 2; // 初始化异常，应用不可启动
	public static final String STRING_CMD_ID = "TXZ_SERVER_CMD";

	public int initialize_BeforeLoadLibrary() {
		return ERROR_SUCCESS;
	}

	public int initialize_AfterLoadLibrary() {
		return ERROR_SUCCESS;
	}

	public int initialize_BeforeStartJni() {
		return ERROR_SUCCESS;
	}

	public int initialize_AfterStartJni() {
		return ERROR_SUCCESS;
	}
	
	public int initialize_AfterInitSuccess() {
		return ERROR_SUCCESS;
	}
	
	public int initialize_addPluginCommandProcessor() {
		return ERROR_SUCCESS;
	}

	public void finalize_BeforeStopJni() {
	}

	public void finalize_AfterStopJni() {
	}
	
	public void release_InstandRelease() {
	}
	
	public void release_DelayRelease() {
	}

	public void reinit() {
	}

	// ////////////////////////////////////////////////////////////////////////////////

	public int regEvent(int eventId) {
		return ModuleManager.getInstance().regEvent(this, eventId);
	}

	public int regEvent(int eventId, int subEventId) {
		return ModuleManager.getInstance().regEvent(this, eventId, subEventId);
	}

	public int unregEvent(int eventId) {
		return ModuleManager.getInstance().unregEvent(this, eventId);
	}

	public int unregEvent(int eventId, int subEventId) {
		return ModuleManager.getInstance()
				.unregEvent(this, eventId, subEventId);
	}

	public int onEvent(int eventId, int subEventId, byte[] data) {
		return 0;
	}

	public int regCommand(String... cmds) {
		return ModuleManager.getInstance().regCommand(this, cmds);
	}

	public int regCmdString(String... cmds) {
		return ModuleManager.getInstance().regString(this, cmds);
	}

	public int regCmdString(String cmdId, String... cmds){
		return ModuleManager.getInstance().regString(this, cmdId, cmds);
	}

	public int regCommandWithResult(String... cmds) {
		return ModuleManager.getInstance().regCommandWithResult(this, cmds);
	}
	
	public int unregCommand(String... cmds) {
		return ModuleManager.getInstance().unregCommand(this, cmds);
	}
	
	private KeyCmds mKeyCmds;

	public int regCustomCommand(String cmd, int eventId, int subEventId,
			byte[] data) {
		if ( null == mKeyCmds )
		{
			mKeyCmds = new KeyCmds();
			mKeyCmds.cmds = new OneCmd[1];
			mKeyCmds.cmds[0] = new OneCmd();
			mKeyCmds.cmds[0].msgData = new CmdData();
		}

		mKeyCmds.cmds[0].msgData.uint32Event = eventId;
		mKeyCmds.cmds[0].msgData.uint32SubEvent = subEventId;
		mKeyCmds.cmds[0].msgData.stringData = data;
		mKeyCmds.cmds[0].uint32Type = VoiceData.CMD_TYPE_BACK_GROUD;
		mKeyCmds.cmds[0].strResId = cmd;
		regEvent(eventId, subEventId);
		return JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_ADD_KEYWORDS_CMD, mKeyCmds);
	}

	public int regCustomCommand(String cmd, int eventId, int subEventId) {
		return regCustomCommand(cmd, eventId, subEventId, new byte[0]);
	}

	/**
	 * 慎用，会取消注册所有模块注册的后台命令字
	 * 
	 * @param cmd
	 * @return
	 */
	public int unregCustomCommand(String cmd) {
		if ( null == mKeyCmds )
		{
			mKeyCmds = new KeyCmds();
			mKeyCmds.cmds = new OneCmd[1];
			mKeyCmds.cmds[0] = new OneCmd();
			mKeyCmds.cmds[0].msgData = new CmdData();
		}
		mKeyCmds.cmds[0].uint32Type = VoiceData.CMD_TYPE_BACK_GROUD;
		mKeyCmds.cmds[0].strResId = cmd;
		
		mKeyCmds.cmds[0].msgData.uint32Event = 0;
		mKeyCmds.cmds[0].msgData.uint32SubEvent = 0;
		mKeyCmds.cmds[0].msgData.stringData = null;
				
		return JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_DEL_KEYWORDS_CMD, mKeyCmds);
	}

	public int onCommand(String cmd) {
		return 0;
	}
	
	public int onCommand(String cmd,String keywords,String voiceString) {
		return onCommand(cmd);
	}

	// ///////////////////////////////////

	protected boolean mInited = true;

	/**
	 * 是否初始化了
	 */
	public boolean isInited() {
		return mInited;
	}

	protected boolean mInitSuccessed = true;

	/**
	 * 是否初始化成功了
	 */
	public boolean isInitSuccessed() {
		return mInitSuccessed;
	}
}
