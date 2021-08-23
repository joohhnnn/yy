package com.txznet.txz.module.location;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;

import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;

public class LocationClientOfBaidu implements ILocationClient {
	Messenger mService = null;
	Context mContext;

	LocationInfo mLastLocation = null;
	/** 定位时间间隔 */
	private int mTimeInterval = ILocationClient.LOCATION_DEFAULT_TIME_INTERVAL;

	// GeoInfo mLastGeo = new GeoInfo();
	// long mLastCityTime = 0;

	public LocationClientOfBaidu() {
		mContext = GlobalContext.get();
	}

	private Runnable mRunnableBind = new Runnable() {
		@Override
		public void run() {
			if (mConnection != null) {
				Intent intent = new Intent(mContext, LocationServiceOfBaidu.class);
				// for android 5.0
				intent.setPackage("com.txznet.txz");
				mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
			}
		}
	};
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);

			quickLocation(mQuickLocation);
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;

			// 异常断开后3秒重新绑定，可能service异常crash了
			AppLogic.removeUiGroundCallback(mRunnableBind);
			AppLogic.runOnUiGround(mRunnableBind, 3000);
		}
	};

	class ClientHandler extends Handler {
		public ClientHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			try {
				mLastLocation = LocationInfo.parseFrom(msg.getData()
						.getByteArray("location"));

				// if (mLastLocation.msgGeoInfo == null
				// || TextUtils.isEmpty(mLastLocation.msgGeoInfo.strCity)) {
				// mLastLocation.msgGeoInfo = mLastGeo;
				// if (SystemClock.elapsedRealtime() - mLastCityTime > 3 * 60 *
				// 1000) {
				// mLastCityTime = SystemClock.elapsedRealtime();
				//
				// final GeoCoder mGeoCoder = GeoCoder.newInstance();
				// mGeoCoder
				// .setOnGetGeoCodeResultListener(new
				// OnGetGeoCoderResultListener() {
				// @Override
				// public void onGetReverseGeoCodeResult(
				// ReverseGeoCodeResult result) {
				// mGeoCoder.destroy();
				//
				// if (result.error == ERRORNO.NO_ERROR) {
				// try {
				// mLastGeo.strCity = result
				// .getAddressDetail().city;
				// mLastGeo.strAddr = result
				// .getAddress();
				// mLastGeo.strProvice = result
				// .getAddressDetail().province;
				// mLastGeo.strDistrict = result
				// .getAddressDetail().district;
				// mLastGeo.strStreet = result
				// .getAddressDetail().street;
				// mLastGeo.strStreetNum = result
				// .getAddressDetail().streetNumber;
				// mLastLocation.msgGeoInfo = mLastGeo;
				// LocationManager
				// .getInstance()
				// .notifyUpdatedLocation();
				// } catch (Exception e) {
				// }
				// JNIHelper.logd("geo code city: "
				// + mLastGeo.strCity);
				// }
				// }
				//
				// @Override
				// public void onGetGeoCodeResult(
				// GeoCodeResult result) {
				// }
				// });
				// mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
				// .location(new LatLng(
				// mLastLocation.msgGpsInfo.dblLat,
				// mLastLocation.msgGpsInfo.dblLng)));
				// }
				// }

				LocationManager.getInstance().notifyUpdatedLocation();
			} catch (Exception e) {
			}
		}
	}

	final Messenger mMessenger = new Messenger(new ClientHandler(
			Looper.getMainLooper()));

	private void bindService() {
		try {
			AppLogic.removeUiGroundCallback(mRunnableBind);
			AppLogic.runOnUiGround(mRunnableBind, 0);
		} catch (Exception e) {
		}
	}

	// 这个函数返回，service进程不能确保退出，可能会稍微滞后一点点
	private void unbind() {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				try {
					AppLogic.removeUiGroundCallback(mRunnableBind);
					if (mService != null) {
						ServiceConnection conn = mConnection;
						mConnection = null;
						mContext.unbindService(conn);
					}
				} catch (Exception e) {
				}
			}
		}, 0);
	}

	private boolean mQuickLocation = true;

	@Override
	public void quickLocation(boolean bQuick) {
		mQuickLocation = bQuick;
		if (mService == null) {
			bindService();
			return;
		}
		Message msg = Message.obtain();
		msg.replyTo = mMessenger;
		Bundle data = new Bundle();
		data.putBoolean("quick", mQuickLocation);
		data.putInt("timeInterval", mTimeInterval);
		msg.setData(data);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		LocationManager.getInstance().reinitLocationClientDelay();
	}

	@Override
	public void setLastLocation(LocationInfo location) {
		mLastLocation = location;
	}

	@Override
	public LocationInfo getLastLocation() {
		if (mService == null)
			bindService();
		return mLastLocation;
	}

	@Override
	public void release() {
		unbind();
		
		LocationManager.getInstance().removeReinitDelayRunnable();
	}

	@Override
	public void setTimeInterval(int timeInterval) {
		mTimeInterval = timeInterval;
	}
}
