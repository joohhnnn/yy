package com.txznet.txz.udprpc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.udprpc.UdpConfiger;
import com.txznet.comm.remote.udprpc.UdpIdManager;
import com.txznet.comm.remote.udprpc.UdpServer;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogicBase;

public class TXZUdpServer {

	private TXZUdpServer(){}
	
	private static TXZUdpServer sInstance = new TXZUdpServer();
	private static final String TAG = "TXZUdpServer ";
	
	private UdpServer mServer;
	
	public static TXZUdpServer getInstance(){
		return sInstance;
	}
	
	public void init() {
		if (GlobalContext.isTXZ() && AppLogicBase.isMainProcess()) {
			initServer();
		}
	}

	private synchronized void initServer() {
		if (mServer == null) {
			mServer = new UdpServer();
			int port = mServer.start();
			if (port > 0) {
				writePortFile("127.0.0.1", port);
			}
		}
	}
	
	public void setCmdDispatcher(UdpServer.ICmdDispatcher cmdDispatcher) {
		UdpServer.setCmdDispatcher(cmdDispatcher);
	}
	
	private void writePortFile(String hostName, int port) {
		File file = new File(UdpConfiger.FILE_PORT);
		if (file.exists()) {
			file.delete();
		}
		String content = "com.txznet.txz = " + hostName + ":" + port + "\n";
		FileOutputStream fos = null;
		try {
			file.createNewFile();
			fos = new FileOutputStream(file);
			fos.write(content.getBytes(), 0, content.getBytes().length);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized byte[] getInitData(String processName) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		if (mServer == null) {
			initServer();
		}
		jsonBuilder.put("port", mServer.getPort());
		int udpId = UdpIdManager.getInstance().getUdpId(processName);
		jsonBuilder.put("udpId", udpId);
		// jsonBuilder.put("logSeq", UdpLogInvoker.getInstance().getSeq(udpId));
		return jsonBuilder.toBytes();
	}
	
	/**
	 * 
	 * @param packageName
	 * @param command
	 * @param data
	 * @return
	 */
	public byte[] onInvoke(String packageName, String command, byte[] data) {
		if ("txz.udp.init".equals(command)) {
			JSONBuilder receiveJson = new JSONBuilder(data);
			String processName = receiveJson.getVal("processName", String.class);
			LogUtil.logd(TAG + "onUdpInit process:" + processName);
			ServiceManager.getInstance().sendInvoke(packageName, "comm.udp.initInfo", getInitData(processName), null);
		}
		return null;
	}
	
}
