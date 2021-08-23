package com.txznet.sdk;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;

import android.text.TextUtils;

public class TXZReportManager {

	private TXZReportManager() {
	}

	private static TXZReportManager sInstance = new TXZReportManager();

	public static TXZReportManager getInstance() {
		return sInstance;
	}

	public void doReport(String data) {
		if (!TextUtils.isEmpty(data)) {
			ReportUtil.doReport(9, data);
		} else {
			LogUtil.logw("report data empty!");
		}
	}

	public void doReportImmediately(String data) {
		if (!TextUtils.isEmpty(data)) {
			ReportUtil.doReportImmediate(9, data.getBytes());
		} else {
			LogUtil.logw("report data empty!");
		}
	}

}
