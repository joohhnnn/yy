package com.txznet.txz.module.location;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.StreetNumber;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

public class LocationServiceOfBaidu extends Service {
	final Messenger mMessenger = new Messenger(new IncomingHandler(Looper.getMainLooper()));
	Messenger mClient = null;
	
	/** 定位时间间隔 */
	public static int mTimeInterval = ILocationClient.LOCATION_DEFAULT_TIME_INTERVAL;

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	public void sendMsgBack(Message msg) {
		if (mClient != null) {
			try {
				mClient.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	class IncomingHandler extends Handler {
		public IncomingHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			try {
				mClient = msg.replyTo;
				Bundle bundle = msg.getData();
				if (bundle == null) {
					return;
				}
				mTimeInterval = bundle.getInt("timeInterval", ILocationClient.LOCATION_DEFAULT_TIME_INTERVAL);
				quickLocation(bundle.getBoolean("quick"));
			} catch (Exception e) {
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		// Log.d("LocationService", "LocationService onCreate");

		try {
			mLocationClient = new LocationClient(GlobalContext.get()); // 声明LocationClient类
			mLocationClient.registerLocationListener(mLocationListener); // 注册监听函数

			SDKInitializer.initialize(GlobalContext.get());
		} catch (Exception e) {
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			mLocationClient.stop();

			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
					List<RunningAppProcessInfo> apps = am.getRunningAppProcesses();
					for (RunningAppProcessInfo app : apps) {
						if ("com.txznet.txz:remote".equals(app.processName)) {
							android.os.Process.killProcess(app.pid);
						}
					}
				}
			}, 2000);

			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			}, 2000);
		} catch (Exception e) {
		}
	}

	private BDLocationListener mLocationListener = new TXZLocationListener();

	LocationInfo mLastLocationInfo;
	GeoInfo mLastGeo = new GeoInfo();
	long mLastCityTime = 0;

	private class TXZLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// Log.d("LocationService", "onReceiveLocation");

			try {
				if (location == null)
					return;

				switch (location.getLocType()) {
				case BDLocation.TypeGpsLocation:
				case BDLocation.TypeNetWorkLocation:
				case BDLocation.TypeCacheLocation:
				case BDLocation.TypeOffLineLocation:
					break;
				case BDLocation.TypeNone:
				case BDLocation.TypeCriteriaException:
				case BDLocation.TypeNetWorkException:
				case BDLocation.TypeOffLineLocationFail:
				case BDLocation.TypeOffLineLocationNetworkFail:
				case BDLocation.TypeServerError:
				default:
					Log.e("LocationService", "LocationService get error location response" + location.getLocType());
					return;
				}

			// 过滤定位失败的情况
			// Log.d("LocationService", "LocationService get new location: "
			// + location.getLocType());

			mLastLocationInfo = BDLocationUtil.Convert_BDLocation_To_LocationInfo(location);

			if (mLastLocationInfo.msgGeoInfo == null || TextUtils.isEmpty(mLastLocationInfo.msgGeoInfo.strCity)) {
				mLastLocationInfo.msgGeoInfo = mLastGeo;
				if (SystemClock.elapsedRealtime() - mLastCityTime > 3 * 60 * 1000) {
					mLastCityTime = SystemClock.elapsedRealtime();

//					final GeoCoder mGeoCoder = GeoCoder.newInstance();
//					mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
//						@Override
//						public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
//							mGeoCoder.destroy();
//
//							if (result.error == ERRORNO.NO_ERROR) {
//								try {
//									mLastGeo.strCity = result.getAddressDetail().city;
//									mLastGeo.strAddr = result.getAddress();
//									mLastGeo.strProvice = result.getAddressDetail().province;
//									mLastGeo.strDistrict = result.getAddressDetail().district;
//									mLastGeo.strStreet = result.getAddressDetail().street;
//									mLastGeo.strStreetNum = result.getAddressDetail().streetNumber;
//									mLastLocationInfo.msgGeoInfo = mLastGeo;
//									sendLocation(mLastLocationInfo);
//								} catch (Exception e) {
//								}
//								Log.d("LocationService", "geo code city: " + mLastGeo.strCity);
//							}
//						}
//
//							@Override
//							public void onGetGeoCodeResult(GeoCodeResult result) {
//							}
//						});
//						mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(
//								new LatLng(mLastLocationInfo.msgGpsInfo.dblLat, mLastLocationInfo.msgGpsInfo.dblLng)));
					
						LocationManager.getInstance().reverseGeoCode(mLastLocationInfo.msgGpsInfo.dblLat,
								mLastLocationInfo.msgGpsInfo.dblLng, new OnGeocodeSearchListener() {

									@Override
									public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
									}

									@Override
									public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
										RegeocodeAddress rAddress = arg0.getRegeocodeAddress();
										if (rAddress == null) {
											return;
										}
										mLastGeo.strCity = rAddress.getCity();
										mLastGeo.strAddr = rAddress.getFormatAddress();
										mLastGeo.strProvice = rAddress.getProvince();
										mLastGeo.strDistrict = rAddress.getDistrict();
										StreetNumber sn = rAddress.getStreetNumber();
										if (sn != null) {
											mLastGeo.strStreet = sn.getStreet();
											mLastGeo.strStreetNum = sn.getNumber();
										}

										mLastLocationInfo.msgGeoInfo = mLastGeo;
										sendLocation(mLastLocationInfo);
									}
								});
					}
				}

				sendLocation(mLastLocationInfo);
			} catch (Exception e) {
			}
		}

		@Override
		public void onConnectHotSpotMessage(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
	}

	private void sendLocation(LocationInfo loc) {
		// 发送位置给客户端
		if (mClient != null) {
			try {
				Message msg = Message.obtain();
				msg.replyTo = mMessenger;
				Bundle data = new Bundle();
				data.putByteArray("location", MessageNano.toByteArray(loc));
				msg.setData(data);
				mClient.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private LocationClient mLocationClient;

	public void quickLocation(boolean bQuick) {
		Log.d("LocationService", "LocationService  quickLocation: " + bQuick);

		// mLocationClient.stop();
		try {
			LocationClientOption locationClientOption = new LocationClientOption();
			locationClientOption.setLocationMode(LocationMode.Hight_Accuracy);
			locationClientOption.setIsNeedAddress(true);
			locationClientOption.setCoorType("gcj02");
			locationClientOption.setNeedDeviceDirect(true);
			locationClientOption.setLocationNotify(false);
			if (bQuick) {
				locationClientOption.setOpenGps(true);
				locationClientOption.setScanSpan(mTimeInterval * 1000);
			} else {
				locationClientOption.setOpenGps(false);
				locationClientOption.setScanSpan(3 * 60 * 1000);
			}
			mLocationClient.setLocOption(locationClientOption);

			// mLocationClient.setForBaiduMap(true);

			if (!mLocationClient.isStarted()) {
				mLocationClient.start();
			}

			mLocationClient.requestLocation(); // 立马请求定位一次
		} catch (Exception e) {
		}
	}
}
