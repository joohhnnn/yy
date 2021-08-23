package com.txznet.txz.module.nav;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.conn_head.ConnHead;
import com.txz.report_manager.ReportManager;
import com.txz.ui.contact.ContactData.UserNetConfigInfo;
import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.UserConfig;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.CommonAddress;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txz.ui.map.UiMap.UserAddress;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.runnables.Runnable1;

import android.text.TextUtils;

public class CollectLocsPlugin {
	private static UserConfig sUserConfig;

	public static void init() {
	}

	public static void handleServerReportData(byte[] data) {
		AppLogic.runOnBackGround(new Runnable1<byte[]>(data) {

			@Override
			public void run() {
				try {
					parseServerData(mP1);
				} catch (InvalidProtocolBufferNanoException e) {
					e.printStackTrace();
				}
			}
		}, 0);
	}

	public static boolean processNavigateInfo(NavigateInfo info, String action) {
		if (info == null) {
			return false;
		}

		String kws = info.strTargetName;
		UserConfig userConfig = sUserConfig;
		if (userConfig == null || userConfig.msgNetCfgInfo == null || userConfig.msgNetCfgInfo.msgFavouriteLocs == null
				|| userConfig.msgNetCfgInfo.msgFavouriteLocs.rptMsgItem == null
				|| userConfig.msgNetCfgInfo.msgFavouriteLocs.rptMsgItem.length < 1) {
			return false;
		}

		NavigateInfo[] infos = userConfig.msgNetCfgInfo.msgFavouriteLocs.rptMsgItem;
		for (NavigateInfo vi : infos) {
			if (vi.strBakName.equals(kws)) {
				if (!NavManager.getInstance().isLegalNavigateInfo(vi)) {
					return false;
				}

				Poi poi = new Poi();
				poi.setAction(action);
				poi.setLat(vi.msgGpsInfo.dblLat);
				poi.setLng(vi.msgGpsInfo.dblLng);
				poi.setName(vi.strTargetName);
				poi.setGeoinfo(vi.strTargetAddress);
				NavManager.getInstance().naviFavorsPoi(poi, kws);
				return true;
			}
		}

		return false;
	}

	private static void parseServerData(byte[] data) throws InvalidProtocolBufferNanoException {
		if (data == null) {
			JNIHelper.loge("parseServerData is null！");
			return;
		}

		UserAddress ua = UserAddress.parseFrom(data);
		if (ua == null) {
			JNIHelper.loge("parseServerData UserAddress is null！");
			return;
		}

		String downHash = ua.strHash;
		JNIHelper.logw("downHash:" + downHash);
		if (!TextUtils.isEmpty(downHash)) {
			String localHash = MD5Util.generateMD5(getHashInputStr());
			if (downHash.equals(localHash)) {
				return;
			} else {
				asyncLocsToServer(localHash);
			}
			return;
		}

		Integer ot = ua.uint32AllAddrOperaType;
		if (ot != null) {
			JNIHelper.logd("allAddrot:" + ot);
			if (ot == UiMap.DELETE_ALL) {
				clearAllAddrAndUpload();
			} else if (ot == UiMap.SELECT_ALL) {
			}
			return;
		}

		CommonAddress[] cas = ua.userLocation;
		if (cas != null && cas.length > 0) {
			List<CommonAddress> needUpList = new ArrayList<UiMap.CommonAddress>();
			UserConfig userConfig = sUserConfig;
			for (CommonAddress ca : cas) {
				CommonAddress address = updateFrequentlyLoc(ca, userConfig);
				if (address != null) {
					needUpList.add(address);
				}
			}

			JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_CONFIG, UiData.DATA_ID_CONFIG_USER,
					UserConfig.toByteArray(userConfig));

			if (needUpList.size() > 0) {
				uploadHashRun.update(needUpList.toArray(new CommonAddress[needUpList.size()]));
			} else {
				uploadHashRun.update(null);
			}

			AppLogic.removeBackGroundCallback(uploadHashRun);
			AppLogic.runOnBackGround(uploadHashRun, 200);
		}
	}

	private static CommonAddress updateFrequentlyLoc(CommonAddress ca,UserConfig userConfig) {
		CommonAddress cAddress = null;
		NavigateInfo newInfo = convertToNavigateInfo(ca);
		Integer addrType = ca.uint32AddressType;
		Integer action = ca.uint32OperationType;
		String kws = ca.strKeyword != null ? new String(ca.strKeyword) : null;
		JNIHelper.logd("updateFrequentlyLoc addrType:" + addrType + ",action:" + action + ",kws:" + kws);
		if (addrType != null) {
			switch (addrType) {
			case UiMap.HOME:
				cAddress = processHomeModify(action, newInfo, userConfig);
				break;
			case UiMap.COMPANY:
				cAddress = processCompanyModify(action, newInfo, userConfig);
				break;
			case UiMap.OTHER_TYPE:
				processOtherTypeModify(action, newInfo, userConfig);
				break;
			default:
				break;
			}
		}
		return cAddress;
	}

	static Runnable1<CommonAddress[]> uploadHashRun = new Runnable1<CommonAddress[]>(null) {

		@Override
		public void run() {
			uploadAddr(mP1, MD5Util.generateMD5(getHashInputStr()));
		}
	};

	private static String getHashInputStr() {
		com.txz.ui.data.UiData.UserConfig userConfig = sUserConfig;
		if (sUserConfig == null) {
			userConfig = sUserConfig = NativeData.getCurUserConfig();
		}
		String hash = "";
		if (userConfig == null || userConfig.msgNetCfgInfo == null) {
			hash = "";
		} else {
			if (NavManager.getInstance().isLegalNavigateInfo(userConfig.msgNetCfgInfo.msgHomeLoc)) {
				hash += getAddrSerial(userConfig.msgNetCfgInfo.msgHomeLoc.msgGpsInfo.dblLat,
						userConfig.msgNetCfgInfo.msgHomeLoc.msgGpsInfo.dblLng,
						userConfig.msgNetCfgInfo.msgHomeLoc.strTargetName,
						userConfig.msgNetCfgInfo.msgHomeLoc.strTargetAddress);
			}
			if (NavManager.getInstance().isLegalNavigateInfo(userConfig.msgNetCfgInfo.msgCompanyLoc)) {
				hash += getAddrSerial(userConfig.msgNetCfgInfo.msgCompanyLoc.msgGpsInfo.dblLat,
						userConfig.msgNetCfgInfo.msgCompanyLoc.msgGpsInfo.dblLng,
						userConfig.msgNetCfgInfo.msgCompanyLoc.strTargetName,
						userConfig.msgNetCfgInfo.msgCompanyLoc.strTargetAddress);
			}
			if (userConfig.msgNetCfgInfo.msgFavouriteLocs != null
					&& userConfig.msgNetCfgInfo.msgFavouriteLocs.rptMsgItem != null
					&& userConfig.msgNetCfgInfo.msgFavouriteLocs.rptMsgItem.length > 0) {
				NavigateInfoList niList = userConfig.msgNetCfgInfo.msgFavouriteLocs;
				for (NavigateInfo info : niList.rptMsgItem) {
					if (info == null || !NavManager.getInstance().isLegalNavigateInfo(info)) {
						continue;
					}
					hash += getAddrSerial(info.msgGpsInfo.dblLat, info.msgGpsInfo.dblLng, info.strTargetName,
							info.strTargetAddress);
				}
			}
		}
		JNIHelper.logi("return hashInput:" + hash);
		return hash;
	}

	/**
	 * lat+lng+name+addr
	 */
	private static String getAddrSerial(Double lat, Double lng, String name, String address) {
		StringBuilder builder = new StringBuilder();
		if (lat != null) {
			builder.append(lat);
		}
		if (lng != null) {
			builder.append(lng);
		}
		if (!TextUtils.isEmpty(name)) {
			builder.append(name);
		}
		if (!TextUtils.isEmpty(address)) {
			builder.append(address);
		}
		return builder.toString();
	}

	private static CommonAddress processHomeModify(int action, NavigateInfo newInfo, UserConfig userConfig) {
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();

		UserNetConfigInfo userNetConfigInfo = userConfig.msgNetCfgInfo;
		NavigateInfo homeLoc = userNetConfigInfo.msgHomeLoc;
		boolean needUpdate = true;
		if (homeLoc != null) {
			Integer time = homeLoc.uint32Time;
			if (time != null && homeLoc.uint32Time > newInfo.uint32Time) {
				// 如果客户端是最新的，则忽略本次更新，并将本地地址更新到服务器
				needUpdate = false;
			}
		}

		if (!needUpdate) {
			if (NavManager.getInstance().isLegalNavigateInfo(homeLoc)) {
				return makeCommonAddr(homeLoc.msgGpsInfo.dblLat, homeLoc.msgGpsInfo.dblLng, homeLoc.strTargetName,
						homeLoc.strTargetAddress, UiMap.HOME, homeLoc.uint32Time);
			}
			return makeCommonAddr(null, null, null, null, UiMap.HOME, homeLoc.uint32Time);
		}

		switch (action) {
		case UiMap.DELETE:
			NavigateInfo tmpInfo = new NavigateInfo();
			tmpInfo.uint32Time = newInfo.uint32Time;
			userNetConfigInfo.msgHomeLoc = tmpInfo;
			break;

		case UiMap.UPDATE:
		case UiMap.INSERT:
			userNetConfigInfo.msgHomeLoc = newInfo;
			break;
		default:
			break;
		}

		return null;
	}

	private static CommonAddress processCompanyModify(int action, NavigateInfo newInfo, UserConfig userConfig) {
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();

		UserNetConfigInfo userNetConfigInfo = userConfig.msgNetCfgInfo;
		NavigateInfo companyLoc = userNetConfigInfo.msgCompanyLoc;
		boolean needUpdate = true;
		if (companyLoc != null) {
			Integer time = companyLoc.uint32Time;
			if (time != null && companyLoc.uint32Time > newInfo.uint32Time) {
				// 如果客户端是最新的，则忽略本次更新
				needUpdate = false;
			}
		}

		if (!needUpdate) {
			if (NavManager.getInstance().isLegalNavigateInfo(companyLoc)) {
				return makeCommonAddr(companyLoc.msgGpsInfo.dblLat, companyLoc.msgGpsInfo.dblLng,
						companyLoc.strTargetName, companyLoc.strTargetAddress, UiMap.COMPANY, companyLoc.uint32Time);
			}
			return makeCommonAddr(null, null, null, null, UiMap.COMPANY, companyLoc.uint32Time);
		}

		switch (action) {
		case UiMap.DELETE:
			NavigateInfo tmpInfo = new NavigateInfo();
			tmpInfo.uint32Time = newInfo.uint32Time;
			userNetConfigInfo.msgCompanyLoc = tmpInfo;
			break;

		case UiMap.UPDATE:
		case UiMap.INSERT:
			userNetConfigInfo.msgCompanyLoc = newInfo;
			break;
		default:
			break;
		}

		return null;
	}

	private static void processOtherTypeModify(Integer action, NavigateInfo newInfo, UserConfig userConfig) {
		if (action == null) {
			JNIHelper.logw("action shouldn't null！");
			return;
		}
		
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();
		NavigateInfoList cInfo = userConfig.msgNetCfgInfo.msgFavouriteLocs;
		switch (action) {
		case UiMap.UPDATE:
		case UiMap.DELETE:
			if (cInfo == null || TextUtils.isEmpty(newInfo.strBakName)) {
				return;
			}
			NavigateInfo[] infos = cInfo.rptMsgItem;
			if (infos == null || infos.length < 1) {
				return;
			}
			for (int i = 0; i < infos.length; i++) {
				NavigateInfo info = infos[i];
				if (info != null && info.strBakName.equals(newInfo.strBakName)) {
					JNIHelper.logw("modify frequent kws:" + newInfo.strBakName);
					if (action == UiMap.DELETE) {
						cInfo.rptMsgItem[i] = null;
					} else if (action == UiMap.UPDATE) {
						cInfo.rptMsgItem[i] = newInfo;
					}
					break;
				}
			}
			break;
		case UiMap.INSERT:
			if (cInfo == null || cInfo.rptMsgItem == null) {
				cInfo = new NavigateInfoList();
				cInfo.rptMsgItem = new NavigateInfo[1];
				cInfo.rptMsgItem[0] = newInfo;
			} else {
				List<NavigateInfo> tmpInfos = new ArrayList<UiMap.NavigateInfo>();
				NavigateInfo[] localInfos = cInfo.rptMsgItem;
				for (int i = 0; i < localInfos.length; i++) {
					NavigateInfo nInfo = localInfos[i];
					if (nInfo != null) {
						tmpInfos.add(nInfo);
					}
				}
				tmpInfos.add(newInfo);
				cInfo.rptMsgItem = tmpInfos.toArray(new NavigateInfo[tmpInfos.size()]);
			}
			break;
		default:
			break;
		}

		userConfig.msgNetCfgInfo.msgFavouriteLocs = cInfo;
	}

	public static NavigateInfo convertToNavigateInfo(CommonAddress ca) {
		NavigateInfo info = new NavigateInfo();
		if (ca.uint32OperationType != null) {
			switch (ca.uint32OperationType) {
			case UiMap.DELETE:
				info.strBakName = ca.strKeyword != null ? new String(ca.strKeyword) : null;
				info.uint32Time = ca.uint32Time;
				break;
			case UiMap.UPDATE:
			case UiMap.INSERT:
				info.msgGpsInfo = new GpsInfo();
				info.msgGpsInfo.dblLat = ca.doubleLat;
				info.msgGpsInfo.dblLng = ca.doubleLng;
				info.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
				info.strBakName = ca.strKeyword != null ? new String(ca.strKeyword) : null;
				info.strTargetName = ca.strName;
				info.strTargetAddress = ca.strAddress;
				info.uint32Time = ca.uint32Time;
				break;
			default:
				break;
			}
		}
		return info;
	}

	public static NavigateInfo createNewNavigateInfo(double lat, double lng, String name, String addr, String kws) {
		NavigateInfo newInfo = new NavigateInfo();
		newInfo.msgGpsInfo = new GpsInfo();
		newInfo.msgGpsInfo.dblLat = lat;
		newInfo.msgGpsInfo.dblLng = lng;
		newInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		newInfo.strTargetName = name;
		newInfo.strTargetAddress = addr;
		if (!TextUtils.isEmpty(kws)) {
			newInfo.strBakName = kws;
		}
		return newInfo;
	}
	
	private static void clearAddressByOrder(NavigateInfo info) {
		if (info != null) {
			info.msgGpsInfo = null;
			info.strTargetName = null;
			info.strTargetAddress = null;
			info.uint32Time = null;
		}
	}
	
	public static void clearAllAddress() {
		UserConfig userConfig = sUserConfig;
		if (userConfig == null) {
			userConfig = sUserConfig = NativeData.getCurUserConfig();
		}
		if (userConfig != null && userConfig.msgNetCfgInfo != null) {
			clearAddressByOrder(userConfig.msgNetCfgInfo.msgHomeLoc);
			clearAddressByOrder(userConfig.msgNetCfgInfo.msgCompanyLoc);
			userConfig.msgNetCfgInfo.msgFavouriteLocs = null;
		}

		JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_CONFIG, UiData.DATA_ID_CONFIG_USER,
				UserConfig.toByteArray(userConfig));
	}
	
	/**
	 * 清空所有地址，并上报hash值
	 */
	private static void clearAllAddrAndUpload() {
		JNIHelper.logd("clearAllAddrAndUpload");
		clearAllAddress();
//		AppLogic.removeBackGroundCallback(saveAddr);
//		AppLogic.runOnBackGround(saveAddr, 200);
	}
	
	static Runnable saveAddr = new Runnable() {

		@Override
		public void run() {
			String hash = MD5Util.generateMD5(getHashInputStr());
			asyncLocsToServer(hash);
		}
	};

	/**
	 * 同步一下保存的常用地址
	 */
	public static void asyncLocsToServer(String hsCode) {
		UserConfig userConfig = sUserConfig;
		if (userConfig == null) {
			userConfig = sUserConfig = NativeData.getCurUserConfig();
		}
		if (userConfig.msgNetCfgInfo == null) {
			uploadLocs(null, null, null, hsCode);
		} else {
			NavigateInfo[] locs = null;
			if (userConfig.msgNetCfgInfo.msgFavouriteLocs != null) {
				locs = userConfig.msgNetCfgInfo.msgFavouriteLocs.rptMsgItem;
			}
			uploadLocs(userConfig.msgNetCfgInfo.msgHomeLoc, userConfig.msgNetCfgInfo.msgCompanyLoc, locs, hsCode);
		}
	}

	/**
	 * 上传家和公司和常用地址
	 * 
	 * @param homeLoc
	 * @param companyLoc
	 * @param freqLocs
	 */
	private static void uploadLocs(NavigateInfo homeLoc, NavigateInfo companyLoc, NavigateInfo[] freqLocs,
			String hashCode) {
		List<CommonAddress> addrs = new ArrayList<UiMap.CommonAddress>();
		if (NavManager.getInstance().isLegalNavigateInfo(homeLoc)) {
			addrs.add(makeCommonAddr(homeLoc.msgGpsInfo.dblLat, homeLoc.msgGpsInfo.dblLng, homeLoc.strTargetName,
					homeLoc.strTargetAddress, UiMap.HOME, homeLoc.uint32Time));
		} else {
			int editTime = 0;
			if (homeLoc != null && homeLoc.uint32Time != null) {
				editTime = homeLoc.uint32Time;
			}
			addrs.add(makeCommonAddr(null, null, null, null, UiMap.HOME, editTime));
		}

		if (NavManager.getInstance().isLegalNavigateInfo(companyLoc)) {
			addrs.add(makeCommonAddr(companyLoc.msgGpsInfo.dblLat, companyLoc.msgGpsInfo.dblLng,
					companyLoc.strTargetName, companyLoc.strTargetAddress, UiMap.COMPANY, companyLoc.uint32Time));
		} else {
			int editTime = 0;
			if (companyLoc != null && companyLoc.uint32Time != null) {
				editTime = companyLoc.uint32Time;
			}
			addrs.add(makeCommonAddr(null, null, null, null, UiMap.COMPANY, editTime));
		}

		if (freqLocs != null) {
			for (int i = 0; i < freqLocs.length; i++) {
				NavigateInfo info = freqLocs[i];
				double lat = info.msgGpsInfo.dblLat;
				double lng = info.msgGpsInfo.dblLng;
				String name = info.strTargetName;
				String address = info.strTargetAddress;
				String kws = info.strBakName;
				Integer editTime = info.uint32Time;
				addrs.add(makeCommonAddr(lat, lng, name, address, kws, editTime));
			}
		}

		uploadAddr(addrs.toArray(new CommonAddress[addrs.size()]), hashCode);
	}
	
	public static void uploadHomeLoc(UserConfig userConfig) {
		sUserConfig = userConfig;
		AppLogic.runOnBackGround(new Runnable1<UserConfig>(userConfig) {

			@Override
			public void run() {
				uploadHomeCompanyLoc(mP1, UiMap.HOME);
			}
		});
	}
	
	public static void uploadCompanyLoc(UserConfig userConfig) {
		sUserConfig = userConfig;
		AppLogic.runOnBackGround(new Runnable1<UserConfig>(userConfig) {

			@Override
			public void run() {
				uploadHomeCompanyLoc(mP1, UiMap.COMPANY);
			}
		});
	}

	/**
	 * @param info
	 * @param type
	 *            UiMap.HOME or UiMap.COMPANY
	 */
	private static void uploadHomeCompanyLoc(UserConfig userConfig, int type) {
		if (userConfig == null) {
			userConfig = sUserConfig;
		}
		Integer editTime = 0;
		double lat = 0;
		double lng = 0;
		String name = null;
		String address = null;

		if (userConfig.msgNetCfgInfo != null) {
			NavigateInfo tmpInfo = null;
			if (type == UiMap.HOME) {
				if (NavManager.getInstance().isLegalNavigateInfo(userConfig.msgNetCfgInfo.msgHomeLoc)) {
					tmpInfo = userConfig.msgNetCfgInfo.msgHomeLoc;
				}
				if (userConfig.msgNetCfgInfo.msgHomeLoc != null) {
					editTime = userConfig.msgNetCfgInfo.msgHomeLoc.uint32Time;
				}
			} else {
				if (NavManager.getInstance().isLegalNavigateInfo(userConfig.msgNetCfgInfo.msgCompanyLoc)) {
					tmpInfo = userConfig.msgNetCfgInfo.msgCompanyLoc;
				}
				if (userConfig.msgNetCfgInfo.msgCompanyLoc != null) {
					editTime = userConfig.msgNetCfgInfo.msgCompanyLoc.uint32Time;
				}
			}

			if (tmpInfo != null) {
				lat = tmpInfo.msgGpsInfo.dblLat;
				lng = tmpInfo.msgGpsInfo.dblLng;
				name = tmpInfo.strTargetName;
				address = tmpInfo.strTargetAddress;
			}
		}

		uploadNavigateInfo(lat, lng, name, address, type, editTime, userConfig);
	}

	/**
	 * // 上报修改后的家或者公司给后台
	 */
	public static void uploadNavigateInfo(Double lat, Double lng, String name, String address, int type,
			Integer editTime, UserConfig userConfig) {
		sUserConfig = userConfig;
		CommonAddress ca = makeCommonAddr(lat, lng, name, address, type, editTime);
		CommonAddress[] caArrays = new CommonAddress[1];
		caArrays[0] = ca;
		uploadAddr(caArrays, MD5Util.generateMD5(getHashInputStr()));
	}

	/**
	 * 生成家和公司类型
	 */
	private static CommonAddress makeCommonAddr(Double lat, Double lng, String name, String address, int type,
			Integer editTime) {
		UiMap.CommonAddress ca = new CommonAddress();
		ca.uint32Time = editTime;
		ca.strAddress = address;
		ca.doubleLat = lat;
		ca.doubleLng = lng;
		ca.strName = name;
		ca.strKeyword = type == UiMap.HOME ? "家".getBytes() : "公司".getBytes();
		ca.uint32AddressType = type;
		int oType = UiMap.INSERT;
		if (lat == null || lng == null || name == null || address == null) {
			oType = UiMap.DELETE;
		}
		ca.uint32OperationType = oType;
		return ca;
	}

	/**
	 * 生成常用地类型
	 */
	private static CommonAddress makeCommonAddr(Double lat, Double lng, String name, String address, String kws,
			Integer editTime) {
		UiMap.CommonAddress ca = new CommonAddress();
		ca.strAddress = address;
		ca.doubleLat = lat;
		ca.doubleLng = lng;
		ca.strName = name;
		ca.strKeyword = kws.getBytes();
		ca.uint32Time = editTime;
		ca.uint32OperationType = UiMap.UPDATE;
		ca.uint32AddressType = UiMap.OTHER_TYPE;
		return ca;
	}

	/**
	 * 上报数据
	 * 
	 * @param addrs
	 * @param hash
	 */
	private static void uploadAddr(CommonAddress[] addrs, String hash) {
		UserAddress addr = new UserAddress();
		addr.strHash = hash;
		addr.userLocation = addrs;

		ReportManager.ReportAddress address = new ReportManager.ReportAddress();
		address.userAddress = addr;

		UiEquipment.Command_information info = new UiEquipment.Command_information();
		info.uint32Cmd = ConnHead.CMD_REPORT_MANAGER;
		info.uint32SubCmd = ReportManager.SUBCMD_REPORT_ADDR;
		info.strData = ReportManager.ReportAddress.toByteArray(address);
		info.uint32Type = 2;
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_SEND_COMMAND, info);
		JNIHelper.logd("uploadhash:" + hash);
	}
}
