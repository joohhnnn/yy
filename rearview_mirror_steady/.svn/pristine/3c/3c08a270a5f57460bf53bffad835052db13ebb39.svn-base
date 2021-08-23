package com.txznet.nav;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.im.UiIm.ActionRoomIn_Resp;
import com.txz.ui.im.UiIm.ActionRoomMemberList_Resp;
import com.txz.ui.im.UiIm.ActionRoomOut_Resp;
import com.txz.ui.im.UiIm.ActionRoomUpdateNotify;
import com.txz.ui.im.UiIm.MemberInfo;
import com.txz.ui.im.UiIm.RoomMember;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.nav.data.UserNaviGpsInfo;
import com.txznet.nav.manager.MultiNavManager;

public class ServiceAnalysisor {
	private static final String TAG = "ServiceAnalysisor";

	/**
	 * 解析进入房间回复
	 * 
	 * @param datas
	 */
	public static void analysisRoomin(byte[] datas) {
		LogUtil.logd(TAG + " -- > analysisRoomin");
		try {
			ActionRoomIn_Resp arir = ActionRoomIn_Resp.parseFrom(datas);
			if (arir == null) {
				return;
			}

			long roomId = arir.uint64Rid;
			int navType = arir.uint32FromType;

			if (roomId != 0 && roomId != -1) {
				// 表示进入房间成功
				MultiNavManager.getInstance().preBegin(roomId, navType);
				MultiNavManager.getInstance().begin();
			}
		} catch (Exception e) {
			LogUtil.loge("ServiceAnalysisor -- > analysisRoomin exception");
		}
	}

	/**
	 * 解析退出房间回复
	 * 
	 * @param datas
	 */
	public static void analysisRoomOut(byte[] datas) {
		LogUtil.logd(TAG + " -- > analysisRoomOut");
		try {
			ActionRoomOut_Resp resp = ActionRoomOut_Resp.parseFrom(datas);
			if (resp == null) {
				return;
			}

			long roomid = resp.uint64Rid;
			if (roomid != 0 && roomid != -1) {
				MultiNavManager.getInstance().end();
			}
		} catch (InvalidProtocolBufferNanoException e) {
			LogUtil.loge("ServiceAnalysisor -- > analysisRoomout exception");
		} catch (Exception e) {
			LogUtil.loge("ServiceAnalysisor -- > analysisRoomout exception");
		}
	}

	/**
	 * 解析获取联系人列表回复
	 * 
	 * @param datas
	 */
	public static void analysisMemberList(byte[] datas) {
		LogUtil.logd(">>解析获取联系人");
		try {
			ActionRoomMemberList_Resp resp = ActionRoomMemberList_Resp.parseFrom(datas);
			if (resp == null) {
				LogUtil.loge(TAG + " -- > 解析联系人回复失败");
				return;
			}

			long roomId = resp.uint64Rid;
			if (roomId != MultiNavManager.getInstance().getRoomId()) {
				LogUtil.loge(TAG + " -- > 与本地房间号不一致，丢弃！");
				return;
			}

			List<UserNaviGpsInfo> mGdList = new ArrayList<UserNaviGpsInfo>();
			RoomMember[] rms = resp.rptMsgMemberList;
			if (rms != null) {
				for (RoomMember rm : rms) {
					UserNaviGpsInfo mGeoData = parseToGeoData(rm);
					if (mGeoData == null || TextUtils.isEmpty(mGeoData.userId)) {
						continue;
					}

					mGdList.add(mGeoData);
					LogUtil.logi(mGeoData.toString());
				}
			} else {
				// 所有人退出
			}

			// 此处加入本地的我
			UserNaviGpsInfo myGd = MultiNavManager.getInstance().getMyGeoData();
			if (myGd != null) {
				mGdList.add(myGd);
				// OverlayManager.getInstance().putIgnoreUid(myGd.userId);
			}

			MultiNavManager.getInstance().updateMemberList(mGdList);
		} catch (Exception e) {
			LogUtil.loge("ServiceAnalysisor -- > 解析获取联系人列表回复异常");
		}
	}

	/**
	 * 解析推送过来的数据
	 * 
	 * @param datas
	 */
	public static void analysisPushUpdate(byte[] datas) {
		try {
			ActionRoomUpdateNotify arun = ActionRoomUpdateNotify.parseFrom(datas);
			if (arun == null) {
				Log.e(TAG, TAG + " -- > 解析推送过来的数据失败");
				return;
			}

			long[] exitId = arun.rptUint64UserOutList;
			RoomMember[] enterRm = arun.rptMsgUserInList;

			if (enterRm != null) {
				for (RoomMember rm : enterRm) {
					UserNaviGpsInfo mGeoData = parseToGeoData(rm);
					if (mGeoData == null || TextUtils.isEmpty(mGeoData.userId)) {
						continue;
					}

					MultiNavManager.getInstance().join(mGeoData);
				}
			}

			if (exitId != null) {
				for (long id : exitId) {
					MultiNavManager.getInstance().quit(String.valueOf(id));
				}
			}

		} catch (InvalidProtocolBufferNanoException e) {
		} catch (Exception e) {
		}
	}

	/**
	 * 将一个RoomMember解析为GeoData对象
	 * 
	 * @param rm
	 * @return
	 */
	public static UserNaviGpsInfo parseToGeoData(RoomMember rm) {
		UserNaviGpsInfo mGeoData = new UserNaviGpsInfo();
		GpsInfo gi = rm.msgGps;
		MemberInfo mi = rm.msgInfo;
		if (gi == null || mi == null) {
			LogUtil.loge(TAG + " -- > RoomMember 信息不完整，丢弃！");
			return null;
		}

		String faceUrl = mi.strFaceUrl;
		long userId = mi.uint64Uid;
		if (userId == 0 || userId == -1) {
			LogUtil.loge(TAG + " -- > RoomMember 信息不完整，丢弃！");
			return null;
		}

		mGeoData.userId = String.valueOf(userId);

		if (faceUrl == null || "".equals(faceUrl)) {
			mGeoData.isGpsOnly = true;
		} else {
			mGeoData.imagePath = faceUrl;
		}

		double alt = 0, lat = 0, lng = 0;
		float dir = 0, rad = 0, spd = 0;

		if (gi.dblAltitude != null) {
			alt = gi.dblAltitude;
		}

		if (gi.dblLat != null) {
			lat = gi.dblLat;
		}

		if (gi.dblLng != null) {
			lng = gi.dblLng;
		}

		if (gi.fltDirection != null) {
			dir = gi.fltDirection;
		}

		if (gi.fltRadius != null) {
			rad = gi.fltRadius;
		}

		if (gi.fltSpeed != null) {
			spd = gi.fltSpeed;
		}

		if (gi.uint32GpsType != null) {
			mGeoData.gpsType = gi.uint32GpsType;
		}

		if (rm.uint32TargetDistance != null) {
			mGeoData.distance = rm.uint32TargetDistance;
		}

		if (rm.uint32TargetTime != null) {
			mGeoData.time = rm.uint32TargetTime;
		}

		if (TextUtils.isEmpty(rm.msgInfo.strUserNick)) {
			mGeoData.nickName = "用户-" + rm.msgInfo.uint64Uid;
		} else {
			mGeoData.nickName = rm.msgInfo.strUserNick;
		}

		try {
			mGeoData.altitude = alt;
			mGeoData.lat = lat;
			mGeoData.lng = lng;
			mGeoData.direction = dir;
			mGeoData.radius = rad;
			mGeoData.speed = spd;

		} catch (NullPointerException e) {
		} catch (Exception e) {
		}

		return mGeoData;
	}
}