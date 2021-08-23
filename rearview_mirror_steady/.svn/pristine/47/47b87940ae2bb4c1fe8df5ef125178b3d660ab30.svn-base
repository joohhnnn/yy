package com.txznet.nav.manager;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.VisibleRegion;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.helper.OverlayImageSelector;
import com.txznet.nav.tool.BitmapProvider.OnBitmapInitedListener;
import com.txznet.nav.util.AnimationUtil;

public class OverlayManager {
	private Vector<Marker> markerList = new Vector<Marker>();
	private Vector<String> markerUserList = new Vector<String>();
	private Vector<String> mIgnoreUserIds = new Vector<String>();
	private ConcurrentHashMap<String, Marker> mId2Marker = new ConcurrentHashMap<String, Marker>();

	private AMap mAMap;
	private VisibleRegion mMarkVr;

	private boolean isTouch;

	private static OverlayManager instance = null;

	private OverlayManager() {

	}

	public static OverlayManager getInstance() {
		if (instance == null) {
			synchronized (OverlayManager.class) {
				if (instance == null) {
					instance = new OverlayManager();
				}
			}
		}
		return instance;
	}

	public void init(AMap map) {
		markerList = null;
		markerUserList = null;
		mId2Marker = null;
		OverlayImageSelector.getInstance().destory();

		markerList = new Vector<Marker>();
		markerUserList = new Vector<String>();
		mId2Marker = new ConcurrentHashMap<String, Marker>();

		mAMap = map;
		mAMap.setOnMapTouchListener(mOnMapTouchListener);
	}

	/**
	 * 更新一个marker
	 */
	public void updateMarker(final String uid, final String imagePath, double lat, double lng, final double direction) {
		LogUtil.logd("OverlayManager updateMarker uid = " + uid + ",Gps =[" + lat + "," + lng + "]");
		if (TextUtils.isEmpty(uid)) {
			return;
		}

		if (mIgnoreUserIds.contains(uid)) {
			LogUtil.logd("OverlayManager IgnoreUserId =:" + uid);
			Marker m = mId2Marker.get(uid);
			if (m != null) {
				m.remove();
				m.destroy();
				m = null;
			}
			return;
		}

		if (!isVisibleLatLng(lat, lng) || isTouch) {
			LogUtil.logd("OverlayManager Gps[" + lat + "," + lng + "] is not visible!");
			// removeMarker(uid);
			// addMarker(uid, imagePath, lat, lng, (float) direction);
			return;
		}

		final Marker marker = mId2Marker.get(uid);
		if (marker != null) {
			LatLng ll = marker.getPosition();
			if (ll.latitude == lat && ll.longitude == lng) {
				return;
			}

			AnimationUtil.getInstance().startMoveAnim(marker, lat, lng);
			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					// marker.setIcon(BitmapDescriptorFactory
					// .fromBitmap(OverlaySelector.getInstance()
					// .getUserBitmap(uid, imagePath,
					// (float) direction)));
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(OverlayImageSelector.getInstance()
							.getUserBitmap(uid, imagePath, (float) direction, new OnBitmapInitedListener() {

						@Override
						public void onInited(Bitmap bitmap) {
							if (bitmap != null) {
								marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
							} else {
								updateMarkerDrawable(uid, (float) direction);
							}
						}
					})));
				}
			}, 0);
		} else {
			addMarker(uid, imagePath, lat, lng, (float) direction);
		}
	}

	/**
	 * 更新用户的头像
	 * 
	 * @param uid
	 */
	public void updateMarkerDrawable(final String uid, final float degree) {
		if (TextUtils.isEmpty(uid)) {
			return;
		}

		if (mIgnoreUserIds.contains(uid)) {
			return;
		}

		final Marker marker = mId2Marker.get(uid);
		if (marker != null) {
			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					// marker.setIcon(BitmapDescriptorFactory
					// .fromBitmap(OverlaySelector.getInstance()
					// .getUserBitmap(uid, null, degree)));
					marker.setIcon(BitmapDescriptorFactory.fromBitmap(OverlayImageSelector.getInstance()
							.getUserBitmap(uid, null, degree, new OnBitmapInitedListener() {

						@Override
						public void onInited(Bitmap bitmap) {
							if (bitmap != null) {
								marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
							}
						}
					})));
				}
			}, 0);
		}
	}

	/**
	 * 添加一个Marker
	 */
	public boolean addMarker(final String uid, final String imagePath, double lat, double lng, final float degree) {
		if (mIgnoreUserIds.contains(uid)) {
			return true;
		}

		if (mAMap == null)
			return false;

		if (markerUserList.contains(uid)) {
			updateMarker(uid, null, lat, lng, degree);
			return false;
		}

		final MarkerOptions mo = new MarkerOptions();
		mo.position(new LatLng(lat, lng));
		mo.zIndex(0);
		mo.anchor(0.5f, 0.5f);
		// mo.icon(BitmapDescriptorFactory.fromBitmap(OverlaySelector
		// .getInstance().getUserBitmap(uid, imagePath, degree)));

		mo.icon(BitmapDescriptorFactory.fromBitmap(
				OverlayImageSelector.getInstance().getUserBitmap(uid, imagePath, degree, new OnBitmapInitedListener() {

					@Override
					public void onInited(Bitmap bitmap) {
						if (bitmap != null) {
							Marker m = mId2Marker.get(uid);
							if (m != null) {
								m.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
							}
						} else {
							updateMarkerDrawable(uid, degree);
						}
					}
				})));

		Marker marker = mAMap.addMarker(mo);
		LogUtil.logd("OverlayManager addMarker Gps = [" + lat + "," + lng + "]");
		marker.setVisible(true);
		mId2Marker.put(uid, marker);
		markerList.add(marker);
		markerUserList.add(uid);

		return true;
	}

	/**
	 * 移除一个marker
	 */
	public boolean removeMarker(String uid) {
		if (!markerUserList.contains(uid)) {
			return false;
		}

		Marker marker = mId2Marker.get(uid);
		if (marker != null) {
			marker.remove();

			mId2Marker.remove(uid);
			markerList.remove(marker);
			markerUserList.remove(uid);
			removeIgnoreUid(uid);
			marker.destroy();
			marker = null;

			OverlayImageSelector.getInstance().removeUid(uid);
			return true;
		}

		return false;
	}

	public void putIgnoreUid(String uid) {
		mIgnoreUserIds.add(uid);
	}

	public void removeIgnoreUid(String uid) {
		if (mIgnoreUserIds.contains(uid)) {
			mIgnoreUserIds.remove(uid);
		}
	}

	public void destoryAllMarker() {
		for (Marker marker : markerList) {
			marker.remove();
			marker.destroy();
			marker = null;
		}

		markerList.clear();
		markerUserList.clear();
		mId2Marker.clear();
		mAMap.clear();
		mIgnoreUserIds.clear();
		mAMap = null;
		instance = null;
	}

	private boolean isVisibleLatLng(double lat, double lng) {
		if (mAMap.getProjection() != null) {
			VisibleRegion vr = mAMap.getProjection().getVisibleRegion();
			if (vr != null) {
				LatLngBounds bounds = vr.latLngBounds;
				if (bounds != null) {
					return bounds.contains(new LatLng(lat, lng));
				}
			}
		}
		return false;
	}

	/**
	 * 得到当前所有Marker的LatLngBounds
	 * 
	 * @return
	 */
	public LatLngBounds getMaxLatLngBounds() {
		Builder builder = null;
		for (Marker marker : markerList) {
			if (builder == null) {
				builder = LatLngBounds.builder();
			}

			builder.include(marker.getPosition());
		}

		return builder.build();
	}

	/**
	 * 缩放地图
	 */
	public void zoomAMap() {
		// 记录上次的VisibleRegion，为了恢复
		mMarkVr = mAMap.getProjection().getVisibleRegion();
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(getMaxLatLngBounds(), 0);
		mAMap.animateCamera(cu);
	}

	/**
	 * 恢复上次的视图
	 */
	public void restore() {
		LatLngBounds llb = mMarkVr.latLngBounds;
		mAMap.animateCamera(CameraUpdateFactory.newLatLngBounds(llb, 0));
	}

	private OnMapTouchListener mOnMapTouchListener = new OnMapTouchListener() {

		@Override
		public void onTouch(MotionEvent arg0) {
			LogUtil.logd("onMapTouchListener MotionEvent:" + arg0.getAction());
			switch (arg0.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isTouch = true;
				break;

			case MotionEvent.ACTION_MOVE:
				isTouch = true;
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				isTouch = false;
				break;
			}
		}
	};
}
