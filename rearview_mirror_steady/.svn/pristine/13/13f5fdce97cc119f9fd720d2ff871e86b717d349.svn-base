package com.txznet.txz.module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.CmdData;
import com.txz.ui.voice.VoiceData.CmdWord;
import com.txz.ui.voice.VoiceData.KeyCmds;
import com.txz.ui.voice.VoiceData.OneCmd;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;
import com.txznet.txz.util.runnables.Runnable3;

/**
 * 模块管理器，负责模块事件注册/分发，统一顺序初始化等
 * 
 * @author bihongpi
 *
 */
public class ModuleManager {
	static ModuleManager sModuleInstance = new ModuleManager();

	ConcurrentHashMap<Integer, Set<IModule>> mEventHandlers;
	ConcurrentHashMap<Long, Set<IModule>> mSubEventHandlers;
	ConcurrentHashMap<String, Set<IModule>> mCommandHandlers;

	long mCostStartTime = System.currentTimeMillis();

	private ModuleManager() {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mEventHandlers = new ConcurrentHashMap<Integer, Set<IModule>>();
				mSubEventHandlers = new ConcurrentHashMap<Long, Set<IModule>>();
				mCommandHandlers = new ConcurrentHashMap<String, Set<IModule>>();
			}
		}, 0);

	}

	public static ModuleManager getInstance() {
		return sModuleInstance;
	}

	JNIHelper mJNIEventHelper = null;

	public void setEventHelper(JNIHelper helper) {
		mJNIEventHelper = helper;
	}

	public int regEvent(IModule mod, int eventId) {
		if (null == mJNIEventHelper)
			return -1;
		AppLogic.runOnUiGround(
				new Runnable2<IModule, Integer>(mod, eventId) {
					@Override
					public void run() {
						IModule mod = mP1;
						int eventId = mP2;
						Set<IModule> cs;
						if (mEventHandlers.containsKey(eventId)) {
							cs = mEventHandlers.get(eventId);
						} else {
							cs = new HashSet<IModule>();
							mEventHandlers.put(eventId, cs);
						}
						cs.add(mod);
					}
				}, 0);
		return mJNIEventHelper._regEvent(eventId);
	}

	public int regEvent(IModule mod, int eventId, int subEventId) {
		if (null == mJNIEventHelper)
			return -1;
		AppLogic.runOnUiGround(
				new Runnable3<IModule, Integer, Integer>(mod, eventId,
						subEventId) {
					@Override
					public void run() {
						IModule mod = mP1;
						int eventId = mP2;
						int subEventId = mP3;
						long key = ((long) eventId << 32) + (long) subEventId;
						Set<IModule> cs;
						if (mSubEventHandlers.containsKey(key)) {
							cs = mSubEventHandlers.get(key);
						} else {
							cs = new HashSet<IModule>();
							mSubEventHandlers.put(key, cs);
						}
						cs.add(mod);
					}
				}, 0);
		return mJNIEventHelper._regEvent(eventId, subEventId);
	}

	public int unregEvent(IModule mod, int eventId) {
		if (null == mJNIEventHelper)
			return -1;
		AppLogic.runOnUiGround(
				new Runnable2<IModule, Integer>(mod, eventId) {
					@Override
					public void run() {
						IModule mod = mP1;
						int eventId = mP2;
						if (mEventHandlers.containsKey(eventId)) {
							Set<IModule> cs = mEventHandlers.get(eventId);
							cs.remove(mod);
							if (cs.isEmpty()) {
								mJNIEventHelper._unregEvent(eventId);
							}
						}
					}
				}, 0);
		return 0;
	}

	public int unregEvent(IModule mod, int eventId, int subEventId) {
		if (null == mJNIEventHelper)
			return -1;
		AppLogic.runOnUiGround(
				new Runnable3<IModule, Integer, Integer>(mod, eventId,
						subEventId) {
					@Override
					public void run() {
						IModule mod = mP1;
						int eventId = mP2;
						int subEventId = mP3;
						long key = ((long) eventId << 32) + (long) subEventId;
						if (mSubEventHandlers.containsKey(key)) {
							Set<IModule> cs = mSubEventHandlers.get(key);
							cs.remove(mod);
							if (cs.isEmpty()) {
								mJNIEventHelper._unregEvent(eventId, subEventId);
							}
						}
					}
				}, 0);
		return 0;
	}

	public int unregEvent(IModule mod) {
		if (null == mJNIEventHelper)
			return -1;
		AppLogic.runOnUiGround(new Runnable1<IModule>(mod) {
			@Override
			public void run() {
				IModule mod = mP1;
				{
					for (Integer key : mEventHandlers.keySet()) {
						Set<IModule> cs = mEventHandlers.get(key);
						cs.remove(mod);
						if (cs.isEmpty()) {
							mJNIEventHelper._unregEvent(key);
						}
					}
				}
				{
					for (Long key : mSubEventHandlers.keySet()) {
						Set<IModule> cs = mSubEventHandlers.get(key);
						cs.remove(mod);
						if (cs.isEmpty()) {
							mJNIEventHelper._unregEvent((int) (key >> 32),
									(int) (key & 0xFFFFFFFF));
						}
					}
				}
			}
		}, 0);

		return 0;
	}

	public int regCommand(IModule mod, String... cmds) {
		return regCommand(mod,UiEvent.EVENT_UI_BACK_GROUD_COMMAND, cmds);
	}
	public int regCommandWithResult(IModule mod, String... cmds) {
		return regCommand(mod,UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW,cmds);
	}
	public int regCommand(IModule mod,final int event, String... cmds) {
		if (null != mJNIEventHelper) {
			mJNIEventHelper._regEvent(event);
		}

		AppLogic.runOnUiGround(
				new Runnable2<IModule, String[]>(mod, cmds) {
					@Override
					public void run() {
						IModule mod = mP1;
						String[] cmdids = mP2;
						KeyCmds cmds = new KeyCmds();
						cmds.cmds = new OneCmd[cmdids.length];
						for (int i = 0; i < cmdids.length; ++i) {
							Set<IModule> cs;
							if (mCommandHandlers.containsKey(cmdids[i])) {
								cs = mCommandHandlers.get(cmdids[i]);
							} else {
								cs = new HashSet<IModule>();
								mCommandHandlers.put(cmdids[i], cs);
							}
							cs.add(mod);
							cmds.cmds[i] = new OneCmd();
							cmds.cmds[i].msgData = new CmdData();
							cmds.cmds[i].msgData.uint32Event = event;
							cmds.cmds[i].msgData.stringData = cmdids[i]
									.getBytes();
							cmds.cmds[i].uint32Type = VoiceData.CMD_TYPE_BACK_GROUD;
							cmds.cmds[i].strResId = cmdids[i];
						}
						JNIHelper
								.sendEvent(
										UiEvent.EVENT_VOICE,
										VoiceData.SUBEVENT_VOICE_ADD_KEYWORDS_CMD,
										cmds);
					}
				}, 0);

		return 0;
	}

	public int unregCommand(IModule mod, String... cmds) {
		if (null != mJNIEventHelper) {
			mJNIEventHelper._regEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND);
			mJNIEventHelper._regEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
		}

		AppLogic.runOnUiGround(
				new Runnable2<IModule, String[]>(mod, cmds) {
					@Override
					public void run() {
						IModule mod = mP1;
						String[] cmdids = mP2;
						for (String cmd : cmdids) {
							if (mCommandHandlers.containsKey(cmd)) {
								Set<IModule> cs = mCommandHandlers.get(cmd);
								cs.remove(mod);
								if (cs.isEmpty()) {
									KeyCmds cmds = new KeyCmds();
									cmds.cmds = new OneCmd[1];
									cmds.cmds[0] = new OneCmd();
									cmds.cmds[0].msgData = new CmdData();
									cmds.cmds[0].uint32Type = VoiceData.CMD_TYPE_BACK_GROUD;
									cmds.cmds[0].strResId = cmd;
									JNIHelper
											.sendEvent(
													UiEvent.EVENT_VOICE,
													VoiceData.SUBEVENT_VOICE_DEL_KEYWORDS_CMD,
													cmds);
								}
							}
						}
					}
				}, 0);

		return 0;
	}

	public int onEvent(int eventId, int subEventId, byte[] data) {
		AppLogic.runOnUiGround(
				new Runnable3<Integer, Integer, byte[]>(eventId, subEventId,
						data) {
					@Override
					public void run() {
						int eventId = mP1;
						int subEventId = mP2;
						byte[] data = mP3;
						Set<IModule> cs = new HashSet<IModule>();

						if (eventId == UiEvent.EVENT_UI_BACK_GROUD_COMMAND) {
							String cmd = new String(data);
							if (mCommandHandlers.containsKey(cmd)) {
								cs.addAll(mCommandHandlers.get(cmd));
							}
							for (IModule mod : cs) {
								JNIHelper.logd("[" + mod.toString()
										+ "]onCommand: cmd=" + cmd);
								mod.onCommand(cmd);
							}
							return;
						}
						else if (eventId == UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW) {
							try {
								CmdWord cmdWord = CmdWord.parseFrom(data);
								if (mCommandHandlers.containsKey(cmdWord.cmdData)) {
									cs.addAll(mCommandHandlers.get(cmdWord.cmdData));
								}
								for (IModule mod : cs) {
									JNIHelper.logd("[" + mod.toString()
											+ "]onCommand: cmd=" + cmdWord.cmdData+",data=" + cmdWord.stringData);
									mod.onCommand(cmdWord.cmdData,cmdWord.stringData,cmdWord.voiceData);
								}
								return;
							} catch (InvalidProtocolBufferNanoException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						long key = ((long) eventId << 32) + (long) subEventId;
						if (mSubEventHandlers.containsKey(key)) {
							cs.addAll(mSubEventHandlers.get(key));
						}
						if (mEventHandlers.containsKey(eventId)) {
							cs.addAll(mEventHandlers.get(eventId));
						}

						for (IModule mod : cs) {
							JNIHelper.logd("[" + mod.toString()
									+ "]onEvent: eventId=" + eventId
									+ ",subEventId=" + subEventId);
							mod.onEvent(eventId, subEventId, data);
						}
					}
				}, 0);
		return 0;
	}

	// //////////////////////////////////////////////////////////////////////////////////

	List<IModule> mModules = new ArrayList<IModule>();

	// Set<IModule> mModules = new HashSet<IModule>();

	public void addModule(IModule mod) {
		if (mModules.contains(mod))
			return;
		mModules.add(mod);
		long t = System.currentTimeMillis();
		JNIHelper.logd("addModule[" + mod.toString() + "] cost time: "
				+ (t - mCostStartTime) + "ms");
		mCostStartTime = t;
	}

	public int initialize_BeforeLoadLibrary() {
		for (IModule mod : mModules) {
			switch (mod.initialize_BeforeLoadLibrary()) {
			case IModule.ERROR_ABORT:
				return IModule.ERROR_ABORT;
			}
		}
		return IModule.ERROR_SUCCESS;
	}

	public int initialize_AfterLoadLibrary() {
		for (IModule mod : mModules) {
			switch (mod.initialize_AfterLoadLibrary()) {
			case IModule.ERROR_ABORT:
				return IModule.ERROR_ABORT;
			}
		}
		return IModule.ERROR_SUCCESS;
	}

	public int initialize_BeforeStartJni() {
		for (IModule mod : mModules) {
			switch (mod.initialize_BeforeStartJni()) {
			case IModule.ERROR_ABORT:
				return IModule.ERROR_ABORT;
			}
		}
		return IModule.ERROR_SUCCESS;
	}

	public int initialize_AfterStartJni() {
		for (IModule mod : mModules) {
			switch (mod.initialize_AfterStartJni()) {
			case IModule.ERROR_ABORT:
				return IModule.ERROR_ABORT;
			}
		}
		return IModule.ERROR_SUCCESS;
	}
	
	public int initialize_AfterInitSuccess() {
		for (IModule mod : mModules) {
			switch (mod.initialize_AfterInitSuccess()) {
			case IModule.ERROR_ABORT:
				return IModule.ERROR_ABORT;
			}
		}
		return IModule.ERROR_SUCCESS;
	}

	public void finalize_BeforeStopJni() {
		for (IModule mod : mModules) {
			mod.finalize_BeforeStopJni();
		}
	}

	public void finalize_AfterStopJni() {
		for (IModule mod : mModules) {
			mod.finalize_AfterStopJni();
		}
	}
	boolean isInit = false;
	public void initSdk_Ifly() {
		if (!isInit) {
			SpeechUtility.createUtility(GlobalContext.get(),
					"appid=" + ProjectCfg.getIflyAppId() + ","
							+ SpeechConstant.ENGINE_MODE + "="
							+ SpeechConstant.MODE_MSC);
			Setting.setShowLog(false);
		}
		isInit = true;
	}
}
