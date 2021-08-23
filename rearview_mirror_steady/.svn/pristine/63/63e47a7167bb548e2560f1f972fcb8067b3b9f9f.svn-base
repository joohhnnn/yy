package com.txznet.txz.udprpc;

import com.txznet.comm.remote.udprpc.UdpDataFactory;
import com.txznet.comm.remote.udprpc.UdpDataFactory.UdpData;
import com.txznet.txz.jni.JNIHelper;

public class UdpReportInvoker extends BaseUdpInvoker {

	private UdpReportInvoker() {
	}

	private static UdpReportInvoker sInstance = new UdpReportInvoker();

	public static UdpReportInvoker getInstance() {
		return sInstance;
	}

	@Override
	public UdpData onInvoke(UdpData udpData) {
		UdpDataFactory.ReportData reportData = UdpDataFactory.separateReportData(udpData.data);
		if (udpData.cmd == UdpData.CMD_REPORT) {
			JNIHelper.doReport(reportData.type, reportData.data);
		} else if (udpData.cmd == UdpData.CMD_REPORT_IMME) {
			JNIHelper.doReportImmediate(reportData.type, reportData.data);
		}
		return null;
	}
 
}
