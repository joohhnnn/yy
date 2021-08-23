package com.txznet.team.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.team.WinControler;
import com.txznet.txz.service.IService;

public class MyService extends Service {
	public class SampleBinder extends IService.Stub {
		@Override
		public byte[] sendInvoke(String packageName, String command, byte[] data) throws RemoteException {
			byte[] ret = ServiceHandler.preInvoke(packageName, command, data);
			if (command.equalsIgnoreCase("team.info.qrcode")) {
				JSONBuilder doc = new JSONBuilder(data);
				boolean issuccess = doc.getVal("issuccess", Boolean.class);
				if (!issuccess) {
					WinControler.getInstance().respBQCode(false, false, null, null);
					return null;
				}
				boolean isbind = doc.getVal("isbind", Boolean.class);
				String qrUrl = doc.getVal("qrcode", String.class);
				String carinfo = doc.getVal("carinfo", String.class);
				WinControler.getInstance().respBQCode(issuccess, isbind, qrUrl, carinfo);
			} else if (command.equalsIgnoreCase("team.info.bind")) {
				JSONBuilder doc = new JSONBuilder(data);
				int uint32Type = doc.getVal("uint32Type", Integer.class);
				String strUrl = doc.getVal("strUrl", String.class);
				String strCarInfo = doc.getVal("strCarInfo", String.class);
				if (uint32Type == UiEquipment.CTMT_BIND) {
					WinControler.getInstance().respBQCode(true, true, strUrl, strCarInfo);
				} else {
					WinControler.getInstance().respBQCode(true, false, strUrl, null);
				}
			}
			return ret;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new SampleBinder();
	}
}
