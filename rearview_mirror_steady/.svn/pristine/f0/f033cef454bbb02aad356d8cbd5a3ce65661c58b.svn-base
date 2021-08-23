package com.txznet.nav.util;

import java.util.HashMap;
import java.util.Map;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.txznet.comm.remote.util.LogUtil;

public class AnimationUtil {
	private static final float ANIM_DURATION = 3000.0f;

	Map<Marker, LatLngAnim> markerToAnim = new HashMap<Marker, LatLngAnim>();

	private static AnimationUtil instance = new AnimationUtil();

	public static AnimationUtil getInstance() {
		return instance;
	}

	public void startMoveAnim(Marker marker, double desLat, double desLng) {
		LatLngAnim lla = markerToAnim.get(marker);
		if (lla == null) {
			lla = new LatLngAnim(marker);
			markerToAnim.put(marker, lla);
		}

		lla.startAnim(desLat, desLng);
	}

	/**
	 * 移动的动画
	 */
	public static class LatLngAnim {
		Marker marker;
		double sLat;
		double sLng;
		double eLat;
		double eLng;
		double latDelay;
		double lngDelay;
		Interpolator mInterpolator;
		long startTime;

		boolean isFinish;

		public LatLngAnim(Marker marker) {
			this.marker = marker;
			mInterpolator = new LinearInterpolator();
		}

		public void startAnim(double eLat, double eLng) {
			LatLng ll = marker.getPosition();
			sLat = ll.latitude;
			sLng = ll.longitude;
			this.eLat = eLat;
			this.eLng = eLng;
			latDelay = eLat - sLat;
			lngDelay = eLng - sLng;
			startTime = System.currentTimeMillis();

			computeOffset();
		}

		Runnable mComputeOffsetRun = new Runnable() {

			@Override
			public void run() {
				long cur = System.currentTimeMillis();
				if (Math.abs(cur - startTime) > ANIM_DURATION) {
					isFinish = true;
					marker.setPosition(new LatLng(eLat, eLng));
					return;
				}

				// 如果还未结束则继续计算
				double lat = sLat
						+ latDelay * mInterpolator.getInterpolation(Math.abs(cur - startTime) / ANIM_DURATION);
				double lng = sLng
						+ lngDelay * mInterpolator.getInterpolation(Math.abs(cur - startTime) / ANIM_DURATION);
				marker.setPosition(new LatLng(lat, lng));
			}
		};

		public void computeOffset() {
			long cur = System.currentTimeMillis();
			if (Math.abs(cur - startTime) > ANIM_DURATION) {
				isFinish = true;
				marker.setPosition(new LatLng(eLat, eLng));
				return;
			}

			// 如果还未结束则继续计算
			double lat = sLat + latDelay * mInterpolator.getInterpolation(Math.abs(cur - startTime) / ANIM_DURATION);
			double lng = sLng + lngDelay * mInterpolator.getInterpolation(Math.abs(cur - startTime) / ANIM_DURATION);
			marker.setPosition(new LatLng(lat, lng));
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				LogUtil.loge(e.toString());
			}

			computeOffset();
		}
	}

	public void clear() {
		markerToAnim.clear();
	}
}
