package com.txznet.txz.udprpc;

import android.util.Log;

import com.txznet.comm.remote.udprpc.UdpDataFactory;
import com.txznet.comm.remote.udprpc.UdpServer;
import com.txznet.txz.udprpc.TXZUdpServer;
import com.txznet.txz.udprpc.UdpLogInvoker;
import com.txznet.txz.udprpc.UdpReportInvoker;

/**
 * Created by Terry on 2017/10/14.
 */

public class UdpCmdDispatcher implements UdpServer.ICmdDispatcher {

	@Override
	public UdpDataFactory.UdpData onInvoke(UdpDataFactory.UdpData udpData) {
//		Log.i("yangtong", "onInvoke cmd:" + udpData.cmd);
		switch (udpData.cmd) {
		case UdpDataFactory.UdpData.CMD_CHECK_CONNECTION:
			byte[] data = null;
			if (udpData.data != null) {
				String processName = new String(udpData.data);
				data = TXZUdpServer.getInstance().getInitData(processName);
			}
			return new UdpDataFactory.UdpData(1, UdpDataFactory.UdpData.INVOKE_ASYNC,
					UdpDataFactory.UdpData.CMD_RESP_CONNECTION, data);
		case UdpDataFactory.UdpData.CMD_LOG:
			return UdpLogInvoker.getInstance().onInvoke(udpData);
		case UdpDataFactory.UdpData.CMD_REPORT_IMME:
		case UdpDataFactory.UdpData.CMD_REPORT:
			return UdpReportInvoker.getInstance().onInvoke(udpData);
		default:
			break;
		}
		return null;
	}

}
