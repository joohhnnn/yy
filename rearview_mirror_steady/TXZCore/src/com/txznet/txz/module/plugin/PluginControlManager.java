package com.txznet.txz.module.plugin;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.plugin.IPluginEventListener;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;

public class PluginControlManager extends IModule {
	private static final PluginControlManager sInstance = new PluginControlManager();
	private static final String TAG = "PluginControlManager:";

	public static PluginControlManager getInstance() {
		return sInstance;
	}

	private PluginControlManager() {

	}

	@Override
	public int initialize_AfterInitSuccess() {
		return super.initialize_AfterInitSuccess();
	}

	private List<IPluginEventListener> mEventListenerList = new ArrayList<IPluginEventListener>();

	@Override
	public int initialize_addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("txz.plugin.", new CommandProcessor() {

			@Override
			public Object invoke(String command, Object[] args) {
				if ("regEvent".equals(command)) {
					if (args != null && args.length > 2) {
						JNIHelper.logd(TAG + "regEvent " + (Integer)args[0] + " " + (Integer) args[1]);
						regEvent((Integer) args[0], (Integer) args[1]);
						mEventListenerList.add((IPluginEventListener) args[2]);
					}
				} else if ("sendEvent".equals(command)) {
					if (args != null && args.length > 1) {
						JNIHelper.logd(TAG + "sendEvent " + (Integer) args[0] + " " + (Integer) args[1]);
						JNIHelper.sendEvent((Integer) args[0], (Integer) args[1], (byte[]) args[2]);
					}
				} else if ("getUid".equals(command)) {
					return ProjectCfg.getUid();
				}
				return null;
			}
		});
		return super.initialize_addPluginCommandProcessor();
	}

	@Override
	public int initialize_BeforeStartJni() {

		return super.initialize_BeforeStartJni();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {

		for (IPluginEventListener listener : mEventListenerList) {
			listener.onEvent(eventId, subEventId, data);
		}
		return super.onEvent(eventId, subEventId, data);
	}

	@Override
	public int onCommand(String cmd) {
		return super.onCommand(cmd);
	}

}
