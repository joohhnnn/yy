package com.txznet.sdkinner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.txz.util.runnables.Runnable2;

public class TXZServiceCommandDispatcher {

	public static interface CommandProcessor {
		public byte[] process(String packageName, String command, byte[] data);
	}

	public static Map<String, CommandProcessor> mProcessors = new ConcurrentHashMap<String, CommandProcessor>();

	public static void setCommandProcessor(String prefix, CommandProcessor processor) {
		ServiceManager.getInstance().runOnServiceThread(
				new Runnable2<String, CommandProcessor>(prefix, processor) {
					@Override
					public void run() {
						if (mP2 == null)
							mProcessors.remove(mP1);
						else
							mProcessors.put(mP1, mP2);
					}
				}, 0);
	}
}
