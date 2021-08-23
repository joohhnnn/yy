package com.txznet.nav.manager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.txznet.loader.AppLogic;
import com.txznet.nav.ui.AMapConfig;
import com.txznet.nav.ui.NavViewActivity;
import com.txznet.nav.ui.NaviCustomView;
import com.txznet.nav.ui.NaviListener;

public class AMapInitor {

	private AMap mAMap;
	private AMapNaviView mAnv;

	public AMapInitor(AMap aMap) {
		this.mAMap = aMap;
	}

	public AMapInitor(AMapNaviView anv) {
		this.mAnv = anv;
	}

	public AMapInitor(AMap aMap, AMapNaviView anv) {
		this.mAMap = aMap;
		this.mAnv = anv;
		AMapConfig.initAMapOption(aMap);
	}

	public void setAMapListener(AMap map) {
		if (map == null) {
			map = mAMap;
			if (map == null) {
				return;
			}
		}
		final AMap m = map;

		m.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChangeFinish(CameraPosition arg0) {
			}

			@Override
			public void onCameraChange(CameraPosition arg0) {
			}
		});

		m.setCustomRenderer(new CustomRenderer() {

			@Override
			public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			}

			@Override
			public void onSurfaceChanged(GL10 gl, int width, int height) {
			}

			@Override
			public void onDrawFrame(GL10 gl) {
			}

			@Override
			public void OnMapReferencechanged() {
			}
		});
		m.setOnMapLoadedListener(new OnMapLoadedListener() {

			@Override
			public void onMapLoaded() {
				AMapProcessor.getInstance().resumeCameraPosition();
			}
		});
	}

	public void initBeforeGpsNaviStart() {
		AMapNavi.getInstance(AppLogic.getApp()).setAMapNaviListener(NaviListener.getInstance());
		setAMapListener(mAMap);
	}

	public void entryNaviActivity(AMapNaviView anv, NavViewActivity activity) {
		if (anv == null) {
			anv = mAnv;
			if (anv == null) {
				return;
			}
		}

		anv.setAMapNaviViewListener(NaviListener.getInstance());
		AMapProcessor.getInstance().assignAMap(mAMap);
		AMapConfig.getInstance().initAMapNaviView(anv);
		NaviCustomView.getInstance().attachActivity(activity);
		NaviCustomView.getInstance().initAMapNaviView(anv);
	}

	public void endNaviActivity() {
		mAnv.onDestroy();
		NaviCustomView.getInstance().onDestory();
		AMapConfig.getInstance().onDestory();
		NaviListener.getInstance().onDestory();
		AMapProcessor.getInstance().procOnEndNavi();
	}
}