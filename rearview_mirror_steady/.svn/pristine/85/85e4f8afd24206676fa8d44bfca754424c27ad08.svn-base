package com.txznet.txz.udprpc;

import com.txznet.comm.remote.udprpc.UdpDataFactory;
import com.txznet.comm.remote.udprpc.UdpDataFactory.UdpData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;

import android.util.Log;
import android.util.SparseIntArray;

public class UdpLogInvoker extends BaseUdpInvoker{

	private static final String TAG = "UdpLogInvoker";

	private UdpLogInvoker() {
	}

	private static UdpLogInvoker sInstance = new UdpLogInvoker();

	private SparseIntArray mMapIdSeq = new SparseIntArray();
	
	public static UdpLogInvoker getInstance() {
		return sInstance;
	}

	public int getSeq(int udpId) {
		return mMapIdSeq.get(udpId);
	}
	
	public void invokeLog(int udpId, byte[] data) {
		JSONBuilder jsonDoc = new JSONBuilder(new String(data));
		int level = jsonDoc.getVal("level", Integer.class);
		String tag = jsonDoc.getVal("tag", String.class);
		String content = jsonDoc.getVal("content", String.class);
		Integer pid = jsonDoc.getVal("pid", Integer.class, 0);
		Long tid = jsonDoc.getVal("tid", Long.class, 0L);
		Integer seq = jsonDoc.getVal("seq", Integer.class);
		String packageName = jsonDoc.getVal("package", String.class);
		if (udpId == 0) {
			JNIHelper._logRaw(pid, tid, packageName, level, tag, "[" + udpId + "/" + seq + "]" + content + "[by udp]");
			return;
		}
		int nextSeq = mMapIdSeq.get(udpId);
		if (seq == 0) {
			nextSeq = 0;
		}
		if (seq > nextSeq) {
			JNIHelper._logRaw(pid, tid, packageName, Log.WARN, "UdpLog",
					"" + (seq - nextSeq) + " logs are missing or in wrong order");
			nextSeq = seq < Integer.MAX_VALUE ? seq + 1 : 0;
		} else if (seq < nextSeq) {
			JNIHelper._logRaw(pid, tid, packageName, Log.WARN, "UdpLog",
					"this log in wrong order,expected seq:" + nextSeq);
			// 如果相差过大，可能是C端进程重启了或其他异常，重置seq为下一个
			if (nextSeq - seq > 10) {
				nextSeq = seq + 1;
			}
		} else {
			nextSeq = seq < Integer.MAX_VALUE ? seq + 1 : 0;
		}
		mMapIdSeq.put(udpId, nextSeq);
		JNIHelper._logRaw(pid, tid, packageName, level, tag, "[" + udpId + "/" + seq + "]" + content + "[by udp]");
	}

	@Override
	public UdpData onInvoke(UdpData udpData) {
		invokeLog(udpData.udpId, udpData.data);
		return new UdpDataFactory.UdpData(1, UdpDataFactory.UdpData.INVOKE_ASYNC,
				UdpDataFactory.UdpData.CMD_LOG_ACK,null);
	}
}
