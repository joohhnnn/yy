package com.txznet.nav;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.manager.NavManager;
import com.txznet.nav.ui.SetLocationActivity;
import com.txznet.txz.service.IService;
import com.txznet.txz.util.runnables.Runnable1;

public class MyService extends Service {
	public class TXZDGNavBinder extends IService.Stub {
		@Override
		public byte[] sendInvoke(String packageName, String command, byte[] data) throws RemoteException {
			byte[] ret = ServiceHandler.preInvoke(packageName, command, data);
			if (command.startsWith("nav.action")) {
				return invokeAction(packageName, command, data);
			} else if (command.startsWith("nav.status")) {
				return invokeStatus(packageName, command, data);
			} else if (command.startsWith("nav.multi")) {
				return invokeMultiNav(packageName, command, data);
			} else if (command.equals("txz.nav.updateMylocation")) {
				try {
					LocationInfo l = LocationInfo.parseFrom(data);
					NavService.getInstance().setLocationInfo(l);
				} catch (Exception e) {
					LogUtil.loge("set home error!");
				}
			} else if (command.equals("txz.nav.startSetLocation")) {
				try {
					NavManager.getInstance().startSetLocation(0, 1, new String(data));
				} catch (Exception e) {
					LogUtil.loge(e.toString());
				}
			} else if (command.equals("txz.nav.updateHomeLocation")) {
				try {
					NavigateInfo n = NavigateInfo.parseFrom(data);
					NavService.getInstance().setHome(n);
				} catch (Exception e) {
					LogUtil.loge("set home error!");
				}
			} else if (command.equals("txz.nav.updateCompanyLocation")) {
				try {
					NavigateInfo n = NavigateInfo.parseFrom(data);
					NavService.getInstance().setCompany(n);
				} catch (Exception e) {
					LogUtil.loge("set home error!");
				}
			} else if (command.equals("txz.nav.closeSetLocation")) {
				AppLogic.finishActivity(SetLocationActivity.class);
			} else if (command.equals("txz.location.historylistchange")) {
				NavigateInfoList list = null;
				try {
					list = NavigateInfoList.parseFrom(data);
				} catch (InvalidProtocolBufferNanoException e) {
					LogUtil.loge("解析历史列表失败！");
				}
				if (list != null) {
					NavService.getInstance().setHistoryList(list);
				}
			} else if (command.startsWith("txz.nav.")) {
				return NavManager.getInstance().invokeNavi(packageName, command.substring("txz.nav.".length()), data);
			}

			return ret;
		}
	}

	private byte[] invokeAction(String packageName, String command, byte[] data) {
		if (command.equals("nav.action.open")) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			boolean navi = NavManager.getInstance().isNavi();
			ComponentName cn;
			if (navi) {
				cn = new ComponentName("com.txznet.nav", "com.txznet.nav.ui.NavViewActivity");
			} else {
				cn = new ComponentName("com.txznet.nav", "com.txznet.nav.ui.MainActivity");
			}
			intent.setComponent(cn);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			try {
				AppLogic.getApp().startActivity(intent);
			} catch (Exception e) {
				LogUtil.loge("open mainactivity error!");
			}
		} else if (command.equals("nav.action.startnavi")) {
			try {
				NavigateInfo info = NavigateInfo.parseFrom(data);
				if (info == null) {
					return null;
				}
				NavManager.getInstance().NavigateTo(info);
			} catch (Exception e) {
				LogUtil.loge("parse NavigateInfo fail!\n" + e.toString());
			}
		} else if (command.equals("nav.action.stopnavi")) {
			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					NavManager.getInstance().stopNavi(true);
				}
			}, 0);
		} else if (command.equals("nav.action.search")) {
			try {
				String s = new String(data);
				String[] searchInfo = s.split(";");
				if (searchInfo == null || searchInfo.length < 4)
					return null;
				String strDest = searchInfo[0];
				String city = searchInfo[1];
				boolean isNearbySearch = Boolean.parseBoolean(searchInfo[2]);
				int where = Integer.parseInt(searchInfo[3]);
				NavManager.getInstance().startSearch(strDest, city, isNearbySearch, where);
			} catch (Exception e) {
				LogUtil.loge("parse search fail!");
			}
		} else if (command.equals("nav.action.navipreviewstopcount")) {
			NavManager.getInstance().stopPreviewCount();
		}

		return null;
	}

	private byte[] invokeMultiNav(String packageName, String command, byte[] data) {
		if (command.equals("nav.multi.roomin")) {
			AppLogic.runOnBackGround(new Runnable1<byte[]>(data) {

				@Override
				public void run() {
					ServiceAnalysisor.analysisRoomin(mP1);
				}
			}, 0);
		} else if (command.equals("nav.multi.roomout")) {
			AppLogic.runOnBackGround(new Runnable1<byte[]>(data) {

				@Override
				public void run() {
					ServiceAnalysisor.analysisRoomOut(mP1);
				}
			}, 0);
		} else if (command.equals("nav.multi.memlist")) {
			AppLogic.runOnBackGround(new Runnable1<byte[]>(data) {

				@Override
				public void run() {
					ServiceAnalysisor.analysisMemberList(mP1);
				}
			}, 0);
		} else if (command.equals("nav.multi.update")) {
			AppLogic.runOnBackGround(new Runnable1<byte[]>(data) {

				@Override
				public void run() {
					ServiceAnalysisor.analysisPushUpdate(mP1);
				}
			}, 0);
		}

		return null;
	}

	private byte[] invokeStatus(String packageName, String command, byte[] data) {
		if (command.equals("nav.status.getIsNavi")) {
			Boolean b = NavManager.getInstance().isNavi();
			return b.toString().getBytes();
		} else if (command.equals("nav.status.getIsInit")) {
			Boolean b = NavManager.getInstance().isInit();
			return b.toString().getBytes();
		}
		return null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new TXZDGNavBinder();
	}
}
