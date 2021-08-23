package com.txznet.txz.module.sence;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;

public class SenceManager extends IModule {
	static SenceManager sModuleInstance = null;

	private SenceManager() {

	}

	public static SenceManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (SenceManager.class) {
				if (sModuleInstance == null)
					sModuleInstance = new SenceManager();
			}
		}
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
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
		// 处理事件
		return super.onEvent(eventId, subEventId, data);
	}

	private Map<String, String> mRemoteSenceMap = new ConcurrentHashMap<String, String>();

	public boolean noneedProcSence(String sence, byte[] data) {
		byte[] bs = procSenceByRemote(sence, data);
		if (bs != null && "true".equals(new String(bs)))
			return true;
		return false;
	}
	
	public boolean noneedProcCommand(String cmd, String keywrods) {
		JSONBuilder json = new JSONBuilder();
		json.put("cmd", cmd);
		json.put("keywords", keywrods);
		return noneedProcSence("command", json.toBytes());
	}

	public byte[] procSenceByRemote(String sence, byte[] data) {
		String service = mRemoteSenceMap.get(sence);
		
		if (TextUtils.isEmpty(service)) {
			sence = "all";
			service = mRemoteSenceMap.get(sence);	
		}
			
		if (TextUtils.isEmpty(service)) {
			return "none".getBytes();
		}

		ServiceData ret = ServiceManager.getInstance().sendInvokeSync(service,
				"tool.sence." + sence, data);

		if (ret != null)
			return ret.getBytes();

		return null;
	}

	ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void onConnected(String serviceName) {
		}

		@Override
		public void onDisconnected(String serviceName) {
			Iterator<Entry<String, String>> it = mRemoteSenceMap.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				if (entry.getValue().equals(serviceName)) {
					it.remove();
				}
			}
		}
	};

	public byte[] getRemoteSenceService(String sence) {
		String r = mRemoteSenceMap.get("sence");
		if (r == null)
			return null;
		return r.getBytes();
	}

	public byte[] invokeTXZSence(final String packageName, String command,
			byte[] data) {
		if (command.startsWith("set.")) {
			ServiceManager.getInstance().addConnectionListener(
					mConnectionListener);
			final String sence = command.substring("set.".length());
			JNIHelper.logd("SenceManager invokeTXZSence sence=" + sence + ", pkg=" + packageName + ", data=" + data);
			mRemoteSenceMap.put(sence, packageName);
//			ServiceManager.getInstance().sendInvoke(packageName, "", null,
//					new GetDataCallback() {
//						@Override
//						public void onGetInvokeResponse(ServiceData data) {
//							// 记录工具
//							JNIHelper.logd("SenceManager onGetInvokeResponse sence=" + sence + ", pkg=" + packageName + ", data=" + data);
//							if (data != null)
//								mRemoteSenceMap.put(sence, packageName);
//						}
//					});
			return null;
		}

		if (command.startsWith("clear.")) {
			String sence = command.substring("clear.".length());
			mRemoteSenceMap.remove(sence);
			return null;
		}
		return null;
	}

}
