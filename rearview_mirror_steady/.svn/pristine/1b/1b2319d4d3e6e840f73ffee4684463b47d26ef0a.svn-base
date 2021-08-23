package com.txznet.txz.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.TtsUtil;

public class PluginManager {

	public static interface CommandProcessor {
		public Object invoke(String command, Object[] args);
	}

	static final ReadWriteLock lock = new ReentrantReadWriteLock(false);

	static Map<String, CommandProcessor> sMapProcessor = new HashMap<String, CommandProcessor>();

	/**
	 * 多线程操作 使用读写锁，新注册的命令字必须注释增加版本号
	 * @param prefix
	 * @param proc
	 */
	public static void addCommandProcessor(String prefix, CommandProcessor proc) {
		lock.writeLock().lock();
		sMapProcessor.put(prefix, proc);
		lock.writeLock().unlock();
	}
	
	public static int PLUGIN_MGR_VERSION = 1;

	public static Object invoke(String command, Object... args) {
		lock.readLock().lock();
		for (Entry<String, CommandProcessor> entry : sMapProcessor.entrySet()) {
			if (command.startsWith(entry.getKey())) {
				lock.readLock().unlock();
				return entry.getValue().invoke(
						command.substring(entry.getKey().length()), args);
			}
		}
		lock.readLock().unlock();
		return null;
	}

	static {
		// add module's PLUGIN_MGR_VERSION >= 1以上支持
		LogUtil.addPluginCommandProcessor();
		MonitorUtil.addPluginCommandProcessor();
		TtsUtil.addPluginCommandProcessor();
		AsrUtil.addPluginCommandProcessor();
		
	}

}
