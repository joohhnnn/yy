package com.txznet.txz.module.location;

import java.util.List;
import java.util.Random;

import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.chetuobang.android.CTBSDKManager;
import com.chetuobang.android.PermissionListener;
import com.chetuobang.android.vtdapi.traffic.CTBQTIErrorCode;
import com.chetuobang.android.vtdapi.traffic.QueryListener;
import com.chetuobang.android.vtdapi.traffic.QueryTraffic;
import com.google.protobuf.nano.MessageNano;
import com.qihu.mobile.lbs.geocoder.Geocoder.GeocoderResult;
import com.qihu.mobile.lbs.geocoder.Geocoder.QHAddress;
import com.qihu.mobile.lbs.geocoder.GeocoderAsy;
import com.qihu.mobile.lbs.geocoder.GeocoderAsy.GeocoderListener;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.qihoo.NavQihooImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.PreferenceUtil;

public class LocationManager extends IModule {
	static LocationManager sModuleInstance = new LocationManager();
	ILocationClient mLocationClient = null;
	CTBSDKManager mCTBSDKManager = null;
	SeqGeoSearchListener mSearchListener;
	final boolean USE_CHETUOBANGE = false;
	public static boolean sUseAndroidSysGps = true;

	private LocationManager() {
		if (PackageManager.getInstance().checkAppExist(NavQihooImpl.PACKAGE_NAME)) {
			mLocationClient = new LocationClientOfQihoo();
		} else {
			mLocationClient = new LocationClientOfAMap();
		}

		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				if (USE_CHETUOBANGE) {
					mCTBSDKManager = new CTBSDKManager(GlobalContext.get());
					mCTBSDKManager.CTBManagerInit("", "SZTONGXINGZHE", new PermissionListener() {
						@Override
						public void onGetPermissionSuccess() {
							JNIHelper.logi("CTBSDKManager CTBManagerInit onGetPermissionSuccess");
						}

						@Override
						public void onGetPermissionFailed() {
							JNIHelper.loge("CTBSDKManager CTBManagerInit onGetPermissionFailed");
						}
					});
				}
			}
		}, 0);

		// 测试代码
		// TXZApp.getApp().runOnBackGround(new Runnable() {
		// int n = 0;
		// @Override
		// public void run() {
		// try{
		// LocationInfo locationInfo = getLastLocation();
		// if (locationInfo != null) {
		// locationInfo.msgGpsInfo.dblLat +=n*0.001;
		// n++;
		// }
		// JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_LOCATION_CHANGE,
		// 0, MessageNano.toByteArray(locationInfo));
		// }
		// catch(Exception e) {
		//
		// }
		// TXZApp.getApp().runOnBackGround(this, 3000);
		// }
		// }, 3000);
	}

	public static LocationManager getInstance() {
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterInitSuccess() {
		// 发送初始化需要触发的事件

		quickLocation(true);

		// 初始化时还没有定位到则获取最后的位置
		if (getLastLocation() == null) {
			LocationInfo pbLocationInfo = PreferenceUtil.getInstance().getLocationInfo();
			synchronized (LocationManager.class) {
				if (null != pbLocationInfo) {
					// JNIHelper.loge("****lat="+pbLocationInfo.msgGpsInfo.dblLat.toString()+",lng="+pbLocationInfo.msgGpsInfo.dblLng.toString());
					mLocationClient.setLastLocation(pbLocationInfo);
				} else {
					pbLocationInfo = NativeData.getLocationInfo();
					if (null != pbLocationInfo) {
						mLocationClient.setLastLocation(pbLocationInfo);
						PreferenceUtil.getInstance().setLocationInfo(pbLocationInfo);
					}
				}
			}
		}
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SHOW_LOCATION);
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SHOW_ROAD_TRAFFIC);

		return super.initialize_AfterInitSuccess();
	}

//	GeoCoder mGeoCoder = null;
//
//	public void cancelReverseGeo() {
//		if (mGeoCoder != null) {
//			mGeoCoder.destroy();
//			mGeoCoder = null;
//		}
//	}
	

	public void cancelQueryTraffic() {
		if (USE_CHETUOBANGE) {
			QueryTraffic.getInstance().stopQuery();
		}
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_VOICE:
			switch (subEventId) {
			case VoiceData.SUBEVENT_VOICE_SHOW_ROAD_TRAFFIC: {
				String city = "";
				String addr = "";
				// 情景预处理
				try {
					final VoiceData.RoadTrafficQueryInfo info = VoiceData.RoadTrafficQueryInfo.parseFrom(data);
					city = info.strCity;
					addr = info.strKeywords;
					JSONObject jData = new JSONObject().put("strCity", info.strCity)
							.put("strDirection", info.strDirection).put("strKeywords", info.strKeywords);
					if (SenceManager.getInstance().noneedProcSence("traffic", jData.toString().getBytes())) {
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (TextUtils.isEmpty(city) && "附近".equals(addr)) {
					LocationInfo li = getLastLocation();
					if (li != null && li.msgGeoInfo != null) {
						city = li.msgGeoInfo.strCity;
						addr = li.msgGeoInfo.strAddr;
					}
				}

				// TODO 到地图界面展示路况信息
				boolean isShow = NavManager.getInstance().showTraffic(city, addr);
				if (isShow) {
					break;
				}

				if (USE_CHETUOBANGE == false) {
					String spk = NativeData.getResString("RS_LOCATION_UNSUPPORT");
					RecorderWin.speakTextWithClose(spk, null);
					break;
				}
				try {
					final VoiceData.RoadTrafficQueryInfo info = VoiceData.RoadTrafficQueryInfo.parseFrom(data);
					if (info.strKeywords == null) {
						info.strKeywords = "";
					} else if ("ROAD_AHEAD".equals(info.strKeywords)) {
						info.strKeywords = "前方";
					} else if ("ROAD_SURROUNDING".equals(info.strKeywords)) {
						info.strKeywords = "周边";
					}

					if (info.strDirection == null) {
						info.strDirection = "";
					} else if ("INTO_TOWN".equals(info.strDirection)) {
						info.strDirection = "进城";
					} else if ("OUT_OF_TOWN".equals(info.strDirection)) {
						info.strDirection = "出城";
					} else if ("OUTER_RING".equals(info.strDirection)) {
						info.strDirection = "外圈";
					} else if ("INNER_CIRCLE".equals(info.strDirection)) {
						info.strDirection = "内圈";
					} else if ("EAST_TO_WEST".equals(info.strDirection)) {
						info.strDirection = "东向西";
					} else if ("WEST_TO_EAST".equals(info.strDirection)) {
						info.strDirection = "西向东";
					} else if ("NORTH_TO_SOUTH".equals(info.strDirection)) {
						info.strDirection = "北向南";
					} else if ("SOUTH_TO_NORTH".equals(info.strDirection)) {
						info.strDirection = "南向北";
					}

					if (TextUtils.isEmpty(info.strCity) || "CURRENT_CITY".equals(info.strCity)) {
						try {
							info.strCity = getLastLocation().msgGeoInfo.strCity;
						} catch (Exception e) {
						}
					}

					JNIHelper.logd("QueryTraffic city=" + info.strCity + ", keywords=" + info.strKeywords
							+ ", direction=" + info.strDirection);

					if (TextUtils.isEmpty(info.strCity)) {
						JNIHelper.loge("QueryTraffic empty city");
						String spk = NativeData.getResString("RS_LOCATION_FAIL");
						RecorderWin.speakTextWithClose(spk, null);
						break;
					}
					Integer adminCode = null;
					StringBuilder citys = new StringBuilder();
					int[] cityAdminCode = mCTBSDKManager.getAdminList();
					for (int i = 0; i < cityAdminCode.length; ++i) {
						String cityName = mCTBSDKManager.getAdminName(cityAdminCode[i]);
						if (TextUtils.isEmpty(cityName))
							continue;
						if (citys.length() > 0)
							citys.append("、");
						citys.append(cityName);
						if (info.strCity.startsWith(cityName) || cityName.startsWith(info.strCity)) {
							adminCode = cityAdminCode[i];
							break;
						}
					}
					int n = citys.lastIndexOf("、");
					if (n > 0)
						citys.replace(n, n + 1, "和");
					if (adminCode == null) {
						JNIHelper.loge("QueryTraffic no city admin code");
						String spk = NativeData.getResPlaceholderString(
								"RS_LOCATION_UNSUPPORT_CITY", "%CMD%",
								info.strCity);
						RecorderWin.speakTextWithClose(spk, null);
						String hint = NativeData.getResPlaceholderString(
								"RS_LOCATION_HINT_SUPPORT", "%CMD%",
								citys.toString());
						RecorderWin.addSystemMsg(hint);
						break;
					}

					JNIHelper.logd("QueryTraffic cityAdminCode=" + adminCode);

					// TtsManager.getInstance().speakText("正在为您查询路况信息");
					String spk = NativeData.getResPlaceholderString("RS_LOCATION_QUERY",
							"%CMD%", info.strCity + info.strKeywords
									+ info.strDirection);
					RecorderWin.addSystemMsg(spk);
					QueryTraffic.getInstance().setQueryListener(new QueryListener() {
						@Override
						public void onQueryStart(boolean isAllowRequest, int adminCode, String strQuery) {
							JNIHelper.logd("QueryTraffic onQueryStart: " + strQuery);
						}

						@Override
						public void onQueryCompleted(CTBQTIErrorCode error, int adminCode, String strQuery,
								String result) {
							JNIHelper.logd("QueryTraffic onQueryCompleted " + error.name() + ": " + result);
							switch (error) {
							case CTBEC_BLANK:
								if (info.strKeywords.equals("") || info.strKeywords.equals("前方")
										|| info.strKeywords.equals("周边")) {
									String[] hints = new String[] { NativeData.getResString("RS_LOCATION_UNSUPPORT_ONE"),
											NativeData.getResString("RS_LOCATION_UNSUPPORT_TWO") };
									RecorderWin.speakTextWithClose(hints[new Random().nextInt(hints.length)], null);
								} else {
									String spk = NativeData.getResPlaceholderString(
											"RS_LOCATION_QUERY_FAIL", 
											"%CMD%", info.strCity + info.strKeywords + info.strDirection);
									RecorderWin.speakTextWithClose(spk, null);
								}
								break;
							case CTBEC_CANCEL:
								break;
							case CTBEC_OK:
								RecorderWin.speakTextWithClose(result, null);
								break;
							case CTBEC_PARSE_FAIL:
							case CTBEC_REQUEST_FAIL:
							case CTBEC_AUTH_FAIL:
							default:
								String spk = NativeData.getResPlaceholderString(
										"RS_LOCATION_QUERY_ERROR", 
										"%CMD%", 
										info.strCity + info.strKeywords + info.strDirection);
								RecorderWin.speakTextWithClose(spk, null);
								break;
							}
						}
					});
					QueryTraffic.getInstance().startQuery(adminCode, info.strKeywords + info.strDirection, true);
				} catch (Exception e) {
					JNIHelper.loge("QueryTraffic Exception");
					e.printStackTrace();
					String spk = NativeData.getResPlaceholderString(
							"RS_LOCATION_QUERY_ERROR", "%CMD%", "");
					RecorderWin.speakTextWithClose(spk, null);
				}
				break;
			}
			case VoiceData.SUBEVENT_VOICE_SHOW_LOCATION:
				final LocationInfo location = getLastLocation();
				// if (location != null && location.msgGeoInfo != null
				// && !TextUtils.isEmpty(location.msgGeoInfo.strAddr)) {
				// RecorderWin.speakTextWithClose(
				// "您当前的位置是："
				// + location.msgGeoInfo.strAddr
				// // + "，东经"
				// // + String.format("%.6f",
				// // location.msgGpsInfo.dblLng)
				// // + "，北纬"
				// // + String.format("%.6f",
				// // location.msgGpsInfo.dblLat)
				// , null);
				// break;
				// }
				if (location != null && location.msgGpsInfo != null && location.msgGpsInfo.dblLat != null
						&& location.msgGpsInfo.dblLng != null) {
					String spk = NativeData.getResString("RS_LOCATION_QUERYING");
					TtsManager.getInstance().speakText(spk);
					String hint = NativeData.getResString("RS_LOCATION_HINT_QUERYING");
					RecorderWin.addSystemMsg(hint);
					if (mLocationClient instanceof LocationClientOfQihoo) {
						reverseQihooGeoCode(location);
					} else {
						reverseAmapGeoCode(location);
					}
					break;
				}
				String spk = NativeData.getResString("RS_LOCATION_SITE_ERROR");
				RecorderWin.speakTextWithClose(spk, null);
				break;
			}
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}

//	public void reverseBaiduGeoCode(final LocationInfo location) {
//		if (mGeoCoder == null) {
//			mGeoCoder = GeoCoder.newInstance();
//		}
//		mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
//			@Override
//			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
//				if (mGeoCoder == null) {
//					RecorderWin.close();
//					return;
//				}
//				mGeoCoder.destroy();
//				mGeoCoder = null;
//
//				if (result.error == ERRORNO.NO_ERROR)
//					RecorderWin.speakTextWithClose("您当前的位置是：" + result.getAddress(), null);
//				else
//					RecorderWin.speakTextWithClose("您当前的位置是：东经" + String.format("%.6f", location.msgGpsInfo.dblLng)
//							+ "，北纬" + String.format("%.6f", location.msgGpsInfo.dblLat) + "，无法获取地理信息", null);
//			}
//
//			@Override
//			public void onGetGeoCodeResult(GeoCodeResult result) {
//			}
//		});
//		mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
//				.location(new LatLng(location.msgGpsInfo.dblLat, location.msgGpsInfo.dblLng)));
//	}
	
	class SeqGeoSearchListener implements OnGeocodeSearchListener {
		int seq = 0;

		public SeqGeoSearchListener(int seq) {
			this.seq = seq;
		}

		public void cancelRequest() {
			seq = 0;
		}

		public void onRegeocodeSearched(RegeocodeResult result, int arg1, int seq) {

		}

		@Override
		public void onGeocodeSearched(GeocodeResult arg0, int arg1) {

		}

		@Override
		public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
			if (seq == 0) {
				JNIHelper.logd("onRegeocodeSearched cancel");
				return;
			}

			onRegeocodeSearched(arg0, arg1, seq);
		}
	}
	
	GeocoderAsy mGeocoderAsy;
	public void reverseQihooGeoCode(final LocationInfo location) {
		if (mGeocoderAsy == null) {
			mGeocoderAsy = new GeocoderAsy(GlobalContext.get());
		}
		// 创建逆地理编码和地理编码检索监听者
		GeocoderListener listener = new GeocoderListener() {
			// 地理编码监听函数
			@Override
			public void onGeocodeResult(GeocoderResult result) {
			}

			// 逆地理编码监听函数，输出经纬度点对应的地址信息
			@Override
			public void onRegeoCodeResult(GeocoderResult result, String description) {
				if (result.code != 0) {
					String spk = NativeData
							.getResString("RS_LOCATION_NOW_UNKNOWN")
							.replace(
									"%LNG%",
									String.format("%.6f",
											location.msgGpsInfo.dblLng))
							.replace(
									"%LAT%",
									String.format("%.6f",
											location.msgGpsInfo.dblLat));
					RecorderWin.speakTextWithClose(spk, null);
					return;
				}
				List<QHAddress> list = result.address;
				if (list != null && list.size() > 0) {
					QHAddress address = list.get(0);
					GeoInfo geoInfo = new GeoInfo();
					geoInfo.strAddr = address.getFormatedAddress();
					geoInfo.strCity = address.getCity();
					geoInfo.strDistrict = address.getDistrict();
					geoInfo.strProvice = address.getProvince();
					geoInfo.strStreet = address.getStreet();
					location.msgGeoInfo = geoInfo;
					String spk = NativeData.getResPlaceholderString(
							"RS_LOCATION_IS", "%CMD%",
							address.getFormatedAddress());
					RecorderWin.speakTextWithClose(spk, null);
				}
			}
		};
		mGeocoderAsy.regeocode(location.msgGpsInfo.dblLat, location.msgGpsInfo.dblLng, listener);
	}

	GeocodeSearch mSearch;
	int mSearchSeq = new Random().nextInt();
	int mLastSearchSeq = 0;

	int getNextSearchSeq() {
		++mSearchSeq;
		if (mSearchSeq == 0)
			++mSearchSeq;
		return mSearchSeq;
	}

	public void cancelRequestGeoCode() {
		if (mSearchListener != null) {
			mSearchListener.cancelRequest();
		}
	}

	public void reverseAmapGeoCode(final LocationInfo location) {
		if (mSearch == null) {
			mSearch = new GeocodeSearch(GlobalContext.get());
		}
		int seq = mLastSearchSeq = getNextSearchSeq();
		AppLogic.removeBackGroundCallback(searchTimerOutTask);
		AppLogic.runOnBackGround(searchTimerOutTask, SEARCH_TIME_EXCEED);

		if (mSearchListener != null) {
			mSearchListener.cancelRequest();
		}

		mSearch.setOnGeocodeSearchListener(mSearchListener = new SeqGeoSearchListener(seq) {
			@Override
			public void onRegeocodeSearched(RegeocodeResult result, int argq, int seq) {
				if (seq != mLastSearchSeq) {
					return;
				}
				if (mSearch == null) {
					RecorderWin.close();
					return;
				}
				mSearch = null;
				RegeocodeAddress ra = result.getRegeocodeAddress();
				AppLogic.removeBackGroundCallback(searchTimerOutTask);
				if (ra != null) {
					String spk = NativeData.getResPlaceholderString(
							"RS_LOCATION_IS", "%CMD%", ra.getFormatAddress());
					RecorderWin.speakTextWithClose(spk, null);
				} else {
					String spk = NativeData
							.getResString("RS_LOCATION_NOW_UNKNOWN")
							.replace(
									"%LNG%",
									String.format("%.6f",
											location.msgGpsInfo.dblLng))
							.replace(
									"%LAT%",
									String.format("%.6f",
											location.msgGpsInfo.dblLat));
					RecorderWin.speakTextWithClose(spk, null);
				}
			}
		});
		LatLonPoint llp = new LatLonPoint(location.msgGpsInfo.dblLat, location.msgGpsInfo.dblLng);
		RegeocodeQuery rq = new RegeocodeQuery(llp, 200, GeocodeSearch.AMAP);
		mSearch.getFromLocationAsyn(rq);
		RecorderWin.addCloseRunnable(new Runnable() {
			@Override
			public void run() {
				mLastSearchSeq = 0;
			}
		});
	}

	private static final int SEARCH_TIME_EXCEED = 4000;
	Runnable searchTimerOutTask = new Runnable() {

		@Override
		public void run() {
			mLastSearchSeq = 0;
			AppLogic.removeBackGroundCallback(searchTimerOutTask);
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					String spk = NativeData.getResString("RS_LOCATION_GET_FAIL");
					RecorderWin.speakTextWithClose(spk, null);
				}
			}, 0);
		}
	};

	public void setLastLocation(LocationInfo location) {
		synchronized (LocationManager.class) {
			mLocationClient.setLastLocation(location);
		}
		notifyUpdatedLocation();
	}

	public LocationInfo getLastLocation() {
		synchronized (LocationManager.class) {
			return mLocationClient.getLastLocation();
		}
	}

	boolean mQuick = false;

	public void quickLocation(boolean bQuick) {
		// 诺威达定位特殊处理
		mQuick = bQuick;
		synchronized (LocationManager.class) {
			mLocationClient.quickLocation(bQuick);
		}
	}

	int mLocSuccessCount = 0;

	public void notifyUpdatedLocation() {
		LocationInfo locationInfo = getLastLocation();
		if (locationInfo != null) {
			++mLocSuccessCount;
			synchronized (LocationManager.class) {
				// 定位到10次后切换到系统的gps定位
				if (mLocSuccessCount > 10 && mLocationClient instanceof LocationClientOfAMap) {
					if (sUseAndroidSysGps) {
						JNIHelper.logd("switch to android gps location system");
						mLocationClient.release();
						mLocationClient = new LocationClientOfAndroidSystem();
					}
					mLocationClient.setLastLocation(locationInfo);
					mLocationClient.quickLocation(mQuick);
				}
			}
			JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_LOCATION_CHANGE, 0,
					MessageNano.toByteArray(locationInfo));
			PreferenceUtil.getInstance().setLocationInfo(locationInfo);
		}
		// 通知导航应用
		ServiceManager.getInstance().sendInvoke(ServiceManager.NAV, "txz.nav.updateMylocation",
				LocationInfo.toByteArray(locationInfo), null);
	}

	public void checkLocResApp() {
		synchronized (LocationManager.class) {
			try {
				Class<? extends ILocationClient> locClazz = null;
				// 用户指定优先度最高
				if (mLocToolType != null) {
					if ("TXZ".equals(mLocToolType)) {
						if (mLocSuccessCount > 10) {
							locClazz = LocationClientOfAndroidSystem.class;
						} else {
							locClazz = LocationClientOfAMap.class;
						}
					} else if ("QIHOO".equals(mLocToolType)) {
						locClazz = LocationClientOfQihoo.class;
					}
				} else {
					if (PackageManager.getInstance().checkAppExist(NavQihooImpl.PACKAGE_NAME)) {
						locClazz = LocationClientOfQihoo.class;
					} else {
						if (mLocSuccessCount > 10) {
							locClazz = LocationClientOfAndroidSystem.class;
						} else {
							locClazz = LocationClientOfAMap.class;
						}
					}
				}
				LocationInfo locationInfo = getLastLocation();
				if (mLocationClient != null && mLocationClient.getClass().equals(locClazz)) {
					return;
				}
				if (mLocationClient != null) {
					mLocationClient.release();
					if (mLocationClient instanceof LocationClientOfQihoo) {
						ActivityManager activityManager = (ActivityManager) GlobalContext.get()
								.getSystemService(Context.ACTIVITY_SERVICE);
						List<RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
						for (RunningAppProcessInfo info : appProcessInfos) {
							if ((ServiceManager.TXZ + ":qh_loc").equals(info.processName)) {
								android.os.Process.killProcess(info.pid);
							}
						}
					}
				}
				mLocationClient = locClazz.newInstance();
				mLocationClient.setLastLocation(locationInfo);
				mLocationClient.quickLocation(mQuick);
			} catch (Exception e) {
			}
		}
	}
	
	private String mLocToolType = null;

	public byte[] processRemoteCommand(String packageName, String command, byte[] data) {
		if (command.equals("getLocation")) {
			return LocationInfo.toByteArray(getLastLocation());
		} else if (command.equals("cleartool")) {
			mLocToolType = null;
			checkLocResApp();
		} else if (command.equals("setInnerTool")) {
			mLocToolType = null;
			String toolType = new String(data);
			mLocToolType = toolType;
			checkLocResApp();
		}
		return null;
	}

}
