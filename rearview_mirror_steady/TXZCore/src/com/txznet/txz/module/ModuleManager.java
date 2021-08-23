package com.txznet.txz.module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.os.SystemClock;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.CmdData;
import com.txz.ui.voice.VoiceData.CmdWord;
import com.txz.ui.voice.VoiceData.KeyCmds;
import com.txz.ui.voice.VoiceData.OneCmd;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
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

	long mCostStartTime = SystemClock.elapsedRealtime();
	
	private static final ReadWriteLock mEventHandlersReadWriteLock = new ReentrantReadWriteLock(false);
	private static final ReadWriteLock mSubEventHandlersReadWriteLock = new ReentrantReadWriteLock(false);
	private static final ReadWriteLock mCommandHandlersReadWriteLock = new ReentrantReadWriteLock(false);

	private ModuleManager() {
//		AppLogic.runOnUiGround(new Runnable() {
//			@Override
//			public void run() {
				mEventHandlers = new ConcurrentHashMap<Integer, Set<IModule>>();
				mSubEventHandlers = new ConcurrentHashMap<Long, Set<IModule>>();
				mCommandHandlers = new ConcurrentHashMap<String, Set<IModule>>();
//			}
//		}, 0);

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
		mEventHandlersReadWriteLock.writeLock().lock();
		Set<IModule> cs;
		if (mEventHandlers.containsKey(eventId)) {
			cs = mEventHandlers.get(eventId);
		} else {
			cs = new HashSet<IModule>();
			mEventHandlers.put(eventId, cs);
		}
		cs.add(mod);
		mEventHandlersReadWriteLock.writeLock().unlock();
		return mJNIEventHelper._regEvent(eventId);
	}

	public int regEvent(IModule mod, int eventId, int subEventId) {
		if (null == mJNIEventHelper)
			return -1;
		
		long key = ((long) eventId << 32) + (long) subEventId;
		mSubEventHandlersReadWriteLock.writeLock().lock();
		Set<IModule> cs;
		if (mSubEventHandlers.containsKey(key)) {
			cs = mSubEventHandlers.get(key);
		} else {
			cs = new HashSet<IModule>();
			mSubEventHandlers.put(key, cs);
		}
		cs.add(mod);
		mSubEventHandlersReadWriteLock.writeLock().unlock();
						
		return mJNIEventHelper._regEvent(eventId, subEventId);
	}

	public int unregEvent(IModule mod, int eventId) {
		if (null == mJNIEventHelper)
			return -1;
		
		mEventHandlersReadWriteLock.writeLock().lock();
		if (mEventHandlers.containsKey(eventId)) {
			Set<IModule> cs = mEventHandlers.get(eventId);
			cs.remove(mod);
			if (cs.isEmpty()) {
				mJNIEventHelper._unregEvent(eventId);
			}
		}
		mEventHandlersReadWriteLock.writeLock().unlock();
		
		return 0;
	}

	public int unregEvent(IModule mod, int eventId, int subEventId) {
		if (null == mJNIEventHelper)
			return -1;
		
		long key = ((long) eventId << 32) + (long) subEventId;
		mSubEventHandlersReadWriteLock.writeLock().lock();
		if (mSubEventHandlers.containsKey(key)) {
			Set<IModule> cs = mSubEventHandlers.get(key);
			cs.remove(mod);
			if (cs.isEmpty()) {
				mJNIEventHelper._unregEvent(eventId, subEventId);
			}
		}
		mSubEventHandlersReadWriteLock.writeLock().unlock();
		
		return 0;
	}

	public int unregEvent(IModule mod) {
		if (null == mJNIEventHelper)
			return -1;
		
		{
			mEventHandlersReadWriteLock.writeLock().lock();
			for (Integer key : mEventHandlers.keySet()) {
				Set<IModule> cs = mEventHandlers.get(key);
				cs.remove(mod);
				if (cs.isEmpty()) {
					mJNIEventHelper._unregEvent(key);
				}
			}
			mEventHandlersReadWriteLock.writeLock().unlock();
		}
		{
			mSubEventHandlersReadWriteLock.writeLock().lock();
			for (Long key : mSubEventHandlers.keySet()) {
				Set<IModule> cs = mSubEventHandlers.get(key);
				cs.remove(mod);
				if (cs.isEmpty()) {
					mJNIEventHelper._unregEvent((int) (key >> 32),
							(int) (key & 0xFFFFFFFF));
				}
			}
			mSubEventHandlersReadWriteLock.writeLock().unlock();
		}

		return 0;
	}

	public int regCommand(IModule mod, String... cmds) {
		return regCommand(mod,UiEvent.EVENT_UI_BACK_GROUD_COMMAND, cmds);
	}
	public int regCommandWithResult(IModule mod, String... cmds) {
		return regCommand(mod,UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW,cmds);
	}
	
	public int regString(IModule mod, String cmdId, String... cmds) {
		if (null != mJNIEventHelper) {
			mJNIEventHelper._regEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
		}

		
		String cmdid = cmdId;
		KeyCmds cmds2 = new KeyCmds();
		cmds2.cmds = new OneCmd[1];
		
		mCommandHandlersReadWriteLock.writeLock().lock();
		Set<IModule> cs;
		if (mCommandHandlers.containsKey(cmdid)) {
			cs = mCommandHandlers.get(cmdid);
		} else {
			cs = new HashSet<IModule>();
			mCommandHandlers.put(cmdid, cs);
		}
		cs.add(mod);
		mCommandHandlersReadWriteLock.writeLock().unlock();
		cmds2.cmds[0] = new OneCmd();
		cmds2.cmds[0].msgData = new CmdData();
		cmds2.cmds[0].msgData.uint32Event = UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW;
		cmds2.cmds[0].msgData.stringData = cmdid
				.getBytes();
		cmds2.cmds[0].uint32Type = VoiceData.CMD_TYPE_BACK_GROUD;
		cmds2.cmds[0].word = cmds;
		cmds2.cmds[0].strResId = cmdid;
		
		JNIHelper
				.sendEvent(
						UiEvent.EVENT_VOICE,
						VoiceData.SUBEVENT_VOICE_ADD_KEYWORDS_CMD,
						cmds2);

		return 0;
	}
	
	public int regString(IModule mod, String... cmds) {
		return regString(mod, IModule.STRING_CMD_ID, cmds);
	}

	public int regCommand(IModule mod,final int event, String... cmds) {
		if (null != mJNIEventHelper) {
			mJNIEventHelper._regEvent(event);
		}

		
		KeyCmds cmds2 = new KeyCmds();
		cmds2.cmds = new OneCmd[cmds.length];
		for (int i = 0; i < cmds.length; ++i) {
			mCommandHandlersReadWriteLock.writeLock().lock();
			Set<IModule> cs;
			if (mCommandHandlers.containsKey(cmds[i])) {
				cs = mCommandHandlers.get(cmds[i]);
			} else {
				cs = new HashSet<IModule>();
				mCommandHandlers.put(cmds[i], cs);
			}
			cs.add(mod);
			mCommandHandlersReadWriteLock.writeLock().unlock();
			cmds2.cmds[i] = new OneCmd();
			cmds2.cmds[i].msgData = new CmdData();
			cmds2.cmds[i].msgData.uint32Event = event;
			cmds2.cmds[i].msgData.stringData = cmds[i]
					.getBytes();
			cmds2.cmds[i].uint32Type = VoiceData.CMD_TYPE_BACK_GROUD;
			cmds2.cmds[i].strResId = cmds[i];
		}
		JNIHelper
				.sendEvent(
						UiEvent.EVENT_VOICE,
						VoiceData.SUBEVENT_VOICE_ADD_KEYWORDS_CMD,
						cmds2);

		return 0;
	}

	public int unregCommand(IModule mod, String... cmds) {
		if (null != mJNIEventHelper) {
			mJNIEventHelper._regEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND);
			mJNIEventHelper._regEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW);
		}

		
		mCommandHandlersReadWriteLock.writeLock().lock();
		for (String cmd : cmds) {
			if (mCommandHandlers.containsKey(cmd)) {
				Set<IModule> cs = mCommandHandlers.get(cmd);
				cs.remove(mod);
				if (cs.isEmpty()) {
					KeyCmds cmds2 = new KeyCmds();
					cmds2.cmds = new OneCmd[1];
					cmds2.cmds[0] = new OneCmd();
					cmds2.cmds[0].msgData = new CmdData();
					cmds2.cmds[0].uint32Type = VoiceData.CMD_TYPE_BACK_GROUD;
					cmds2.cmds[0].strResId = cmd;
					JNIHelper
							.sendEvent(
									UiEvent.EVENT_VOICE,
									VoiceData.SUBEVENT_VOICE_DEL_KEYWORDS_CMD,
									cmds2);
				}
			}
		}
		mCommandHandlersReadWriteLock.writeLock().unlock();

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
							mCommandHandlersReadWriteLock.readLock().lock();
							if (mCommandHandlers.containsKey(cmd)) {
								cs.addAll(mCommandHandlers.get(cmd));
							}
							mCommandHandlersReadWriteLock.readLock().unlock();
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
								mCommandHandlersReadWriteLock.readLock().lock();
								if (mCommandHandlers.containsKey(cmdWord.cmdData)) {
									cs.addAll(mCommandHandlers.get(cmdWord.cmdData));
								}
								mCommandHandlersReadWriteLock.readLock().unlock();
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
						mSubEventHandlersReadWriteLock.readLock().lock();
						if (mSubEventHandlers.containsKey(key)) {
							cs.addAll(mSubEventHandlers.get(key));
						}
						mSubEventHandlersReadWriteLock.readLock().unlock();
						mEventHandlersReadWriteLock.readLock().lock();
						if (mEventHandlers.containsKey(eventId)) {
							cs.addAll(mEventHandlers.get(eventId));
						}
						mEventHandlersReadWriteLock.readLock().unlock();
				
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
		long t = SystemClock.elapsedRealtime();
		JNIHelper.logd("addModule[" + mod.toString() + "] cost time: "
				+ (t - mCostStartTime) + "ms");
		mCostStartTime = t;
	}

	public int initialize_addPluginCommandProcessor() {
		for (IModule mod : mModules) {
			switch (mod.initialize_addPluginCommandProcessor()) {
			case IModule.ERROR_ABORT:
				return IModule.ERROR_ABORT;
			}
		}
		// 一些不在工具类里也需要添加插件命令字的情况
		if (NativeData.initialize_addPluginCommandProcessor() == IModule.ERROR_ABORT) {
			return IModule.ERROR_ABORT;
		}
		return IModule.ERROR_SUCCESS;
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
	
	public void release_InstandRelease() {
		for (IModule mod : mModules) {
			mod.release_InstandRelease();
		}
	}
	
	public void release_DelayRelease() {
		for (IModule mod : mModules) {
			mod.release_DelayRelease();
		}
	}

	public void reinit() {
		for (IModule mod : mModules) {
			mod.reinit();
		}
	}
	
}
