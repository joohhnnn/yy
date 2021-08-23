package com.txznet.txz.module.location;


class GPSManager {
	
}

//class GPSManager extends IModule {
//	static GPSManager sModuleInstance = null;
//	private LocationManager locMgr;
//
//	public static GPSManager getInstance() {
//		if (sModuleInstance == null) {
//			synchronized (GPSManager.class) {
//				if (sModuleInstance == null)
//					sModuleInstance = new GPSManager();
//			}
//		}
//		return sModuleInstance;
//	}
//
//	@Override
//	public int initialize_AfterStartJni() {
//
//		locMgr = (LocationManager) TXZApp.getApp().getSystemService(
//				Context.LOCATION_SERVICE);
//
//		// 获取上次保存的位置
//		mLocationInfo = PreferenceUtil.getInstance().getLocationInfo();
//
//		if (mLocationInfo != null) {
//			Location location = getLastBestLocation();
//			if (null != location) {
//				Converter c = new Converter();
//				Converter.Point p = c.getEncryPoint(location.getLongitude(),
//						location.getLatitude());
//				mLocationInfo.msgGpsInfo.dblLat = p.y; // 纬度
//				mLocationInfo.msgGpsInfo.dblLng = p.x;// 经度
//				mLocationInfo.msgGpsInfo.dblAltitude = location.getAltitude();// 海拔
//				mLocationInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
//				mLocationInfo.uint32Time = (int) location.getTime() / 1000;
//			}
//		} else {
//			// 设置默认值
//			mLocationInfo = new LocationInfo();
//			mLocationInfo.msgGpsInfo = new UiMap.GpsInfo();
//			mLocationInfo.msgGpsInfo.dblAltitude = 0.0;
//			mLocationInfo.msgGpsInfo.dblLat = 39.90819;
//			mLocationInfo.msgGpsInfo.dblLng = 116.39678;
//			mLocationInfo.msgGpsInfo.fltDirection = 0f;
//			mLocationInfo.msgGpsInfo.fltRadius = 0f;
//			mLocationInfo.msgGpsInfo.fltSpeed = 0f;
//		}
//
//		start();
//
//		return super.initialize_AfterStartJni();
//	}
//
//	boolean m_bStart = false;
//	boolean m_bGPSStartOk = false;
//
//	public void start() {
//		m_bStart = true;
//		
//		if (m_bGPSStartOk)
//			return;
//
//		// 为获取地理位置信息时设置查询条件
//		// String bestProvider = lm.getBestProvider(getCriteria(), true);
//		// 获取位置信息
//		// 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
//		// Location location = lm
//		// .getLastKnownLocation(LocationManager.GPS_PROVIDER);
//		// 监听状态
//		locMgr.addGpsStatusListener(gpsStatusListener);
//
//		// 绑定监听，有4个参数
//		// 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
//		// 参数2，位置信息更新周期，单位毫秒
//		// 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
//		// 参数4，监听
//		// 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新
//
//		// 1秒更新一次，或最小位移变化超过1米更新一次；
//		// 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
//
//		// 判断GPS是否正常启动
//		if (locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//			locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000,
//					1, locationGPSListener);
//			m_bGPSStartOk = true;
//			JNIHelper.logd("gps provider start ok");
//		} else {
//			// TODO 提示打开GPS
//			JNIHelper.loge("GPS_PROVIDER no open.");
//		}
//
////		if (locMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
////			locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
////					3000, 1, locationGPSListener);
////			m_bNetworkStartOk = true;
////			JNIHelper.logd("network provider start ok");
////		}
//	}
//
//	public void stop() {
//		m_bStart = false;
//		m_bGPSStartOk = false;
//		locMgr.removeUpdates(locationGPSListener);
//		locMgr.removeGpsStatusListener(gpsStatusListener);
//		JNIHelper.logd("stop gps");
//	}
//
//	LocationInfo mLocationInfo;
//
//	// 通知GPS信息更新
//	public void notifyUpdatedLocation(LocationInfo pbLocationInfo) {
//		if (pbLocationInfo != null) {
//			JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_LOCATION_CHANGE,
//					0, MessageNano.toByteArray(pbLocationInfo));
//
//			PreferenceUtil.getInstance().setLocationInfo(pbLocationInfo);
//
//			// 通知导航应用
//			ServiceManager.getInstance().sendInvoke(ServiceManager.NAV,
//					"txz.nav.updateMylocation",
//					LocationInfo.toByteArray(pbLocationInfo), null);
//		}
//	}
//
//	public LocationInfo getLocation() {
//		return mLocationInfo;
//	}
//
//	/**
//	 * @return the last know best location
//	 */
//	private Location getLastBestLocation() {
//		Location locationGPS = locMgr
//				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//		Location locationNet = locMgr
//				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//		long GPSLocationTime = 0;
//		if (null != locationGPS) {
//			GPSLocationTime = locationGPS.getTime();
//		}
//
//		long NetLocationTime = 0;
//
//		if (null != locationNet) {
//			NetLocationTime = locationNet.getTime();
//		}
//
//		if (0 < GPSLocationTime - NetLocationTime) {
//			return locationGPS;
//		} else {
//			return locationNet;
//		}
//
//	}
//
//	// 位置监听
//	private LocationListener locationGPSListener = new LocationListener() {
//
//		/**
//		 * 位置信息变化时触发
//		 */
//		public void onLocationChanged(Location location) {
//			JNIHelper.logd("gps time=" + location.getTime() + ", lng="
//					+ location.getLongitude() + ", lat="
//					+ location.getLatitude() + ", alt="
//					+ location.getAltitude());
//
//			if (mLocationInfo != null) {
//				Converter c = new Converter();
//				Converter.Point p = c.getEncryPoint(location.getLongitude(),
//						location.getLatitude());
//				mLocationInfo.msgGpsInfo.dblLat = p.y; // 纬度
//				mLocationInfo.msgGpsInfo.dblLng = p.x;// 经度
//				mLocationInfo.msgGpsInfo.dblAltitude = location.getAltitude();// 海拔
//				mLocationInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
//				mLocationInfo.uint32Time = (int) location.getTime() / 1000;
//
//				notifyUpdatedLocation(mLocationInfo);
//			}
//		}
//
//		/**
//		 * GPS状态变化时触发
//		 */
//		public void onStatusChanged(String provider, int status, Bundle extras) {
//			switch (status) {
//			// GPS状态为可见时
//			case LocationProvider.AVAILABLE:
//				JNIHelper.logd("Gps onStatusChanged AVAILABLE");
//				break;
//			// GPS状态为服务区外时
//			case LocationProvider.OUT_OF_SERVICE:
//				JNIHelper.logd("Gps onStatusChanged OUT_OF_SERVICE");
//				break;
//			// GPS状态为暂停服务时
//			case LocationProvider.TEMPORARILY_UNAVAILABLE:
//				JNIHelper.logd("Gps onStatusChanged TEMPORARILY_UNAVAILABLE");
//				break;
//			}
//		}
//
//		/**
//		 * GPS开启时触发
//		 */
//		public void onProviderEnabled(String provider) {
//			Location location = locMgr.getLastKnownLocation(provider);
//			JNIHelper.logd("onProviderEnabled");
//			if (m_bStart) {
//				start();
//			}
//		}
//
//		/**
//		 * GPS禁用时触发
//		 */
//		public void onProviderDisabled(String provider) {
//			JNIHelper.logd("onProviderDisabled");
//		}
//
//	};
//
//	// 状态监听
//	GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
//		public void onGpsStatusChanged(int event) {
//			switch (event) {
//			// 第一次定位
//			case GpsStatus.GPS_EVENT_FIRST_FIX:
//				JNIHelper.logd("GPS_EVENT_FIRST_FIX");
//				break;
//			// 卫星状态改变
//			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
//				// JNIHelper.logd("GPS_EVENT_SATELLITE_STATUS");
////				// 获取当前状态
////				GpsStatus gpsStatus = locMgr.getGpsStatus(null);
////				// 获取卫星颗数的默认最大值
////				int maxSatellites = gpsStatus.getMaxSatellites();
////				// 创建一个迭代器保存所有卫星
////				Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
////						.iterator();
////				int count = 0;
////				while (iters.hasNext() && count <= maxSatellites) {
////					GpsSatellite s = iters.next();
////					count++;
////				}
////				JNIHelper.logd("find" + count + " satellites");
//				break;
//			// 定位启动
//			case GpsStatus.GPS_EVENT_STARTED: {
//				if (m_bStart) {
//					start();
//				}
//				JNIHelper.logd("GPS_EVENT_STARTED");
//			}
//				break;
//			// 定位结束
//			case GpsStatus.GPS_EVENT_STOPPED:
//				JNIHelper.logd("GPS_EVENT_STOPPED");
//				break;
//			}
//		};
//	};
//
//}
