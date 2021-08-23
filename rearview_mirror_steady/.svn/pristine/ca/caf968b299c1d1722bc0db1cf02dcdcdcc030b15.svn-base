package com.txznet.nav.ui;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLongClickListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnPOIClickListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.NavService;
import com.txznet.nav.R;
import com.txznet.nav.manager.NavManager;
import com.txznet.nav.util.DateUtils;

public class CheckMapActivity extends BaseActivity implements
		OnMapClickListener, LocationSource, OnPOIClickListener,
		OnMarkerClickListener, AMapLocationListener {

	private MapView mapView;
	private Button mBtnStartNav;
	private ImageButton mLocMeIb;
	private LinearLayout mFlNavIntoBar;
	private TextView mNavNameTv;
	private TextView mNavDesTv;
	private ImageButton mZoomoutIb;
	private ImageButton mZoominIb;

	private AMap mAMap;
	private GeocodeSearch mGs;
	private BitmapDescriptor mBd;
	private OnLocationChangedListener mOlcl;

	private Marker mCurrMarker;
	private int mIntParams;

	private double mCurrentLat;
	private double mCurrentLng;

	private boolean mDispatchTouch;
	private boolean mAccessProcess;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppLogic.addActivity(this);
		setContentView(R.layout.activity_checkmap);
		initResource();
		initView(savedInstanceState);
		processIntent();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		processIntent();
	}

	private void processIntent() {
		LogUtil.logd("CheckMapActivity processIntent");
		Intent intent = getIntent();
		mIntParams = intent.getIntExtra("where", 0);
		if (mIntParams != 0) {
			mBtnStartNav.setText(getResources().getString(
					R.string.activity_check_map_set_text));
		} else {
			mBtnStartNav.setText(getResources().getString(
					R.string.activity_check_map_start_planing_text));
		}

		mAccessProcess = false;
		mBtnStartNav.setBackgroundColor(Color.parseColor("#adb6cc"));
	}

	private void initResource() {
		mBd = BitmapDescriptorFactory
				.fromResource(R.drawable.activity_check_map_ic_marker_selected);

		mDispatchTouch = false;
	}

	private void initView(Bundle savedInstanceState) {
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		mBtnStartNav = (Button) findViewById(R.id.btnStartNav);
		mLocMeIb = (ImageButton) findViewById(R.id.btnMoveToMyLocation);
		mFlNavIntoBar = (LinearLayout) findViewById(R.id.flNavIntoBar);
		mNavNameTv = (TextView) findViewById(R.id.txtName);
		mNavDesTv = (TextView) findViewById(R.id.txtDes);
		mZoomoutIb = (ImageButton) findViewById(R.id.zoom_out_ib);
		mZoominIb = (ImageButton) findViewById(R.id.zoom_in_ib);
		final AMap aMap = mapView.getMap();
		if (aMap != null) {
			if (DateUtils.isNight() && aMap.getMapType() != AMap.MAP_TYPE_NIGHT) {
				aMap.setMapType(AMap.MAP_TYPE_NIGHT);
				setNightView();
			}
		}

		mBtnStartNav.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mAccessProcess) {
					return;
				}

				// 开始导航
				if (mIntParams == 0) {
					preview();
				} else {
					if (NavManager.LOCATION_COMPANY == mIntParams) {
						NavManager.getInstance().setCompany(
								mNavNameTv.getText().toString(),
								mNavDesTv.getText().toString(), mCurrentLat,
								mCurrentLng);
					} else if (NavManager.LOCATION_HOME == mIntParams) {
						NavManager.getInstance().setHome(
								mNavNameTv.getText().toString(),
								mNavDesTv.getText().toString(), mCurrentLat,
								mCurrentLng);
					}

					AppLogic.showToast(getResources().getString(
							R.string.activity_check_map_set_up_success_text));
					finish();

					AppLogic
							.finishActivity(SetLocationActivity.class);
				}
			}
		});

		mLocMeIb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDispatchTouch = false;
				updateMapLocation(true, true);
				mLocMeIb.setImageResource(R.drawable.activity_check_map_ic_follow);
			}
		});

		mZoomoutIb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				zoomout();
			}
		});

		mZoominIb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				zoomin();
			}
		});

		setUpAMap();
		showNavBar(false);

		mGs = new GeocodeSearch(this);
		mGs.setOnGeocodeSearchListener(new OnGeocodeSearchListener() {

			@Override
			public void onRegeocodeSearched(RegeocodeResult rr, int rCode) {
				if (rCode == 0) {
					RegeocodeAddress ra = rr.getRegeocodeAddress();
					mNavDesTv.setText(ra.getFormatAddress());
					mAccessProcess = true;
					mBtnStartNav
							.setBackgroundResource(R.drawable.activity_search_start_button_bg);
				} else {

				}
			}

			@Override
			public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
			}
		});

		UiSettings us = mAMap.getUiSettings();
		if (us == null) {
			return;
		}

		us.setMyLocationButtonEnabled(false);
		us.setZoomControlsEnabled(false);
	}

	private void setNightView() {
		mZoominIb.setImageResource(R.drawable.nav_view_zoom_out_n);
		mZoomoutIb.setImageResource(R.drawable.nav_view_zoom_in_n);
		mLocMeIb.setBackgroundResource(R.drawable.activity_check_map_n_ic_bg);
	}

	private void zoomout() {
		mAMap.animateCamera(CameraUpdateFactory.zoomOut());
	}

	private void zoomin() {
		mAMap.animateCamera(CameraUpdateFactory.zoomIn());
	}

	private void setUpAMap() {
		mAMap = mapView.getMap();
		mAMap.setOnPOIClickListener(this);
		mAMap.setOnMarkerClickListener(this);
		mAMap.setOnMapClickListener(this);
		mAMap.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng ll) {
				mCurrentLat = ll.latitude;
				mCurrentLng = ll.longitude;
				mNavNameTv.setText("地图选点");
				reverseGeoCode(ll.latitude, ll.longitude);
				showNavBar(true);
				drawMarker(ll);
				moveToLocation(ll);
			}
		});

		mAMap.setLocationSource(this);
		mAMap.setMyLocationEnabled(true);
		mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		mAMap.setOnMapTouchListener(new OnMapTouchListener() {

			@Override
			public void onTouch(MotionEvent arg0) {
				if (DateUtils.isNight()) {
					mLocMeIb.setImageResource(R.drawable.activity_check_map_n_ic_point);
				} else {
					mLocMeIb.setImageResource(R.drawable.activity_check_map_ic_point);
				}
			}
		});
	}

	private void pickGeoInfo() {
		mAccessProcess = false;
		mBtnStartNav.setBackgroundColor(Color.parseColor("#adb6cc"));
	}

	private void reverseGeoCode(double lat, double lon) {
		pickGeoInfo();
		LatLonPoint llp = new LatLonPoint(lat, lon);
		RegeocodeQuery rq = new RegeocodeQuery(llp, 200, GeocodeSearch.AMAP);
		mGs.getFromLocationAsyn(rq);
	}

	private void updateMapLocation(boolean isMoveLoc, boolean isZoom) {
		LocationInfo li = NavManager.getInstance().getLocationInfo();
		if (li == null) {
			AppLogic.showToast("location is null");
			LogUtil.loge("location is null");
			return;
		}

		if (li.msgGpsInfo == null) {
			LogUtil.loge("location.msgGpsInfo is null");
			return;
		}

		if (isZoom) {
			if (isMoveLoc) {
				moveToLocation(new LatLng(li.msgGpsInfo.dblLat,
						li.msgGpsInfo.dblLng));
			}
		} else {
			if (isMoveLoc) {
				onLocationChange(new LatLng(li.msgGpsInfo.dblLat,
						li.msgGpsInfo.dblLng));
			}
		}
	}

	private void moveToLocation(LatLng ll) {
		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(ll, 16);
		mAMap.animateCamera(cu);
	}

	private void onLocationChange(LatLng ll) {
		float zoom = mAMap.getCameraPosition().zoom;
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
		mAMap.animateCamera(update);
	}

	private void drawMarker(LatLng ll) {
		if (mCurrMarker != null) {
			mCurrMarker.remove();
		}

		MarkerOptions mo = new MarkerOptions();
		mo.position(ll).icon(mBd).zIndex(9);
		mCurrMarker = mAMap.addMarker(mo);
	}

	private LocationManagerProxy mAMapLocationManager;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mDispatchTouch = true;
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void activate(OnLocationChangedListener arg0) {
		mOlcl = arg0;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			mAMapLocationManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, 2000, 10, this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onLocationChanged(AMapLocation location) {
		if (mDispatchTouch) {
			return;
		}

		LogUtil.logd("checkMapActivity onLocationChanged 获取最新定位位置");
		if (mOlcl != null && location != null) {
			mOlcl.onLocationChanged(location);
			NavService.getInstance().setLocationInfo(location);
		}

		updateMapLocation(true, false);
	}

	@Override
	public void deactivate() {
		mOlcl = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
			mAMapLocationManager = null;
		}
	}

	@Override
	public void onMapClick(LatLng arg0) {
		if (mCurrMarker != null) {
			mCurrMarker.remove();
		}
		showNavBar(false);
	}

	@Override
	public void onPOIClick(Poi poi) {
		LatLng ll = poi.getCoordinate();
		mCurrentLat = ll.latitude;
		mCurrentLng = ll.longitude;
		reverseGeoCode(ll.latitude, ll.longitude);
		if (mCurrMarker != null) {
			mCurrMarker.remove();
		}
		mNavNameTv.setText(poi.getName());
		showNavBar(true);
		drawMarker(ll);
		moveToLocation(ll);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (mCurrMarker != null) {
			mCurrMarker.remove();
		}
		showNavBar(false);
		return false;
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateMapLocation(true, true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
		if (mDispatchTouch) {
			mDispatchTouch = false;
		}
		mAMap.setMyLocationEnabled(true);
		activate(mOlcl);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		mAMap.setMyLocationEnabled(false);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	// 显示导航信息控制条
	private void showNavBar(boolean b) {
		if (b)
			mFlNavIntoBar.setVisibility(View.VISIBLE);
		else
			mFlNavIntoBar.setVisibility(View.GONE);
	}

	private void preview() {
		NavigateInfo info = new NavigateInfo();
		GpsInfo gpsInfo = new GpsInfo();
		gpsInfo.dblLat = mCurrentLat;
		gpsInfo.dblLng = mCurrentLng;
		info.msgGpsInfo = gpsInfo;
		info.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		info.strTargetName = mNavNameTv.getText().toString();
		info.strTargetAddress = mNavDesTv.getText().toString();
		NavManager.getInstance().NavigateTo(info);
	}
}