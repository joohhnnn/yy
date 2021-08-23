package com.txznet.txzcar;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.ServiceManager;

public class ServiceRequest {

	public static void sendBeginInvoke(NavigateInfo info){
		try {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.multi.nav.inner.beginMultiNav", MessageNano.toByteArray(info), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sendEndInvoke(NavigateInfo info){
		try {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.multi.nav.inner.endMultiNav", MessageNano.toByteArray(info), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sendGetDetailMemberListInfo(long roomId){
		try {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.multi.nav.inner.getMultiNavMemList.detail", String.valueOf(roomId).getBytes(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sendGetGPSMemInfoList(long roomId){
		try {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.multi.nav.inner.getMultiNavMemList.gps", String.valueOf(roomId).getBytes(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
