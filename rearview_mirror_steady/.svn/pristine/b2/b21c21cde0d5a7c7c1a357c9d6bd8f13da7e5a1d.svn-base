package com.txznet.nav.service;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.im.UiIm.ActionRoomUpdateNotify;
import com.txz.ui.im.UiIm.RoomMember;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.nav.MyApplication;
import com.txznet.nav.NavManager;
import com.txznet.nav.NavService;
import com.txznet.nav.multinav.MultiNavManager;
import com.txznet.nav.multinav.MultiNavManager.LocationData;
import com.txznet.nav.ui.SetLocationActivity;
import com.txznet.txz.service.IService;
import com.txznet.txz.util.runnables.Runnable1;

public class MyService extends Service {
	public class SampleBinder extends IService.Stub {
		@Override
		public byte[] sendInvoke(String packageName, String command, byte[] data)
				throws RemoteException {

			// LogUtil.logd("[MODULE="
			// + GlobalContext.get().getApplicationInfo().packageName
			// + ",FROM=" + packageName + ",CMD=" + command
			// + "] do invoke data:" + data + ". ");

			ServiceHandler.preInvoke(packageName, command, data);
			if (command.startsWith("nav.action")) {
				return invokeAction(packageName, command, data);
			} else if (command.startsWith("nav.status")) {
				return invokeStatus(packageName, command, data);
			} else if (command.startsWith("txz.nav")) {
				return invokeTXZNav(packageName, command, data);
			} else if (command.startsWith("nav.multi")) {
				return invokeMultiNav(packageName, command, data);
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
			} else {
				// LogUtil.loge("[MODULE="
				// + GlobalContext.get().getApplicationInfo().packageName
				// + ",FROM=" + packageName + ",CMD=" + command +
				// "] unknown cmd.");
			}
			return null;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new SampleBinder();
	}

	private byte[] invokeAction(String packageName, String command, byte[] data) {
		if (command.equals("nav.action.open")) {
			ComponentName cn = new ComponentName("com.txznet.nav",
					"com.txznet.nav.ui.MainActivity");
			Intent it = new Intent(Intent.ACTION_MAIN);
			it.addCategory(Intent.CATEGORY_LAUNCHER);
			it.setComponent(cn);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			try {
				MyApplication.getInstance().startActivity(it);
			} catch (Exception e) {
				LogUtil.loge("open mainactivity error!");
			}
		} else if (command.equals("nav.action.startnavi")) {
			try {
				NavigateInfo info = NavigateInfo.parseFrom(data);
				Log.d("MulitNav",
						"MulitNav ----> nav.action.startnavi --- > ServerPushInfo:"
								+ info.msgServerPushInfo);
				NavManager.getInstance().NavigateTo(info);
			} catch (Exception e) {
				LogUtil.loge("parse NavigateInfo fail!");
			}
		} else if (command.equals("nav.action.stopnavi")) {
			NavManager.getInstance().stopNavi();
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
				NavManager.getInstance().startSearch(strDest, city,
						isNearbySearch, where);
			} catch (Exception e) {
				LogUtil.loge("parse search fail!");
			}
		} else if (command.equals("nav.action.navipreviewstopcount")) {
			NavManager.getInstance().stopPreviewCount();
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

	private byte[] invokeTXZNav(String packageName, String command, byte[] data) {
		if (command.equals("txz.nav.updateMylocation")) {
			try {
				LocationInfo l = LocationInfo.parseFrom(data);
				NavService.getInstance().setLocationInfo(l);
				// TODO 刷新界面
			} catch (Exception e) {
				LogUtil.loge("set home error!");
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
			MyApplication.getInstance().finishActivity(
					SetLocationActivity.class);
		}
		return null;
	}

	private byte[] invokeMultiNav(String packageName, String command,
			byte[] data) {
		Log.d("MultiNav", "MultiNav -- > packageName:" + packageName
				+ ",command:" + command);
		if (command.equals("nav.multi.roomin")) {
			MyApplication.getApp().runOnBackGround(new Runnable1<byte[]>(data) {

				@Override
				public void run() {
					handleRoomin(mP1);
				}
			}, 0);
		} else if (command.equals("nav.multi.roomout")) {
			MyApplication.getApp().runOnBackGround(new Runnable1<byte[]>(data) {

				@Override
				public void run() {
					handleRoomout(mP1);
				}
			}, 0);
		} else if (command.equals("nav.multi.memlist")) {
			MyApplication.getApp().runOnBackGround(new Runnable1<byte[]>(data) {

				@Override
				public void run() {
					handleUpdateMemList(mP1);
				}
			}, 0);
		} else if (command.equals("nav.multi.update")) {
			MyApplication.getApp().runOnBackGround(new Runnable1<byte[]>(data) {

				@Override
				public void run() {
					handlePushUpdate(mP1);
				}
			}, 0);
		}

		return null;
	}

	/**
	 * 进入房间
	 * 
	 * @param data
	 */
	private void handleRoomin(byte[] data) {
		// try {
		// ActionRoomIn_Resp arir = ActionRoomIn_Resp.parseFrom(data);
		// Log.d("MultiNav",
		// "MultiNav -------> handleRoomin --> ActionRoomIn_Resp is:"+arir);
		// if (arir != null) {
		// MultiNavManager.getInstance().setNavType(arir.uint32FromType);
		// Log.d("MultiNav",
		// "MultiNav -------> handleRoomin --> RoomId is:"+arir.uint64Rid);
		// MultiNavManager.getInstance().setRoomId(arir.uint64Rid);
		// }
		//
		// Log.d("MultiNav", "MultiNav -------> 本设备进入多车同行");
		//
		// long roomId = MultiNavManager.getInstance().getRoomId();
		// if (roomId != 0 && roomId != -1) {
		// MultiNavManager.getInstance().setIsMultiNav(true);
		// MultiNavManager.getInstance().begin();
		//
		// // 开始获取联系人列表
		// NavManager.getInstance().getMultiDetailMemInfoList(MultiNavManager.getInstance().getRoomId());
		// }
		// } catch (InvalidProtocolBufferNanoException e) {
		// e.printStackTrace();
		// }
		MultiNavManager.getInstance().handleRoomIn(data);
	}

	/**
	 * 退出房间
	 * 
	 * @param data
	 */
	private void handleRoomout(byte[] data) {
		Log.d("MultiNav", "MultiNav -------> 本设备退出多车同行");
		MultiNavManager.getInstance().handleRoomOut();
	}

	/**
	 * 刷新多车联系人
	 * 
	 * @param data
	 */
	private void handleUpdateMemList(byte[] data) {
		Log.d("MultiNav", "MultiNav ------->刷新多车同行的联系人");
		NavManager.getInstance().refreshMemberList(data);
	}

	/**
	 * 推送过来的消息
	 * 
	 * @param data
	 */
	private void handlePushUpdate(byte[] data) {
		Log.d("MultiNav", "MultiNav ------->handlePushUpdate<-----------------");
		try {
			ActionRoomUpdateNotify arun = ActionRoomUpdateNotify
					.parseFrom(data);
			if (arun != null) {
				RoomMember[] enterRm = arun.rptMsgUserInList;
				long[] exitId = arun.rptUint64UserOutList;

				if (enterRm != null) { // 加入
					for (RoomMember rm : enterRm) {
						Log.d("MultiNav", "MultiNav------>"
								+ rm.msgInfo.strUserNick + "加入多车同行");
						LocationData ld = new LocationData();
						ld.userId = String.valueOf(rm.msgInfo.uint64Uid);
						ld.isGps = false;
						ld.latitudeE6 = (int) (rm.msgGps.dblLat * 1e5);
						ld.longitudeE6 = (int) (rm.msgGps.dblLng * 1e5);
						ld.rotateDegress = rm.msgGps.fltDirection;
						ld.imagePath = rm.msgInfo.strFaceUrl;

						MultiNavManager.getInstance().updateLocationData(
								ld.userId, ld);
					}
				}

				if (exitId != null) { // 退出
					for (long id : exitId) {
						Log.d("MultiNav", "MultiNav------>UID:" + id + "退出多车同行");
						MultiNavManager.getInstance().updateLocationData(
								String.valueOf(id), null);
					}
				}
			}
		} catch (InvalidProtocolBufferNanoException e) {
		}
	}
}
