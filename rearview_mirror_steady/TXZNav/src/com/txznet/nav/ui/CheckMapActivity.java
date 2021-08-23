package com.txznet.nav.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.nav.MyApplication;
import com.txznet.nav.NavManager;
import com.txznet.nav.NavService;
import com.txznet.nav.R;
import com.txznet.nav.util.BDLocationUtil;

public class CheckMapActivity extends BaseActivity {

	private int mIntWhere;

	// 导航信息控制条在界面的最下方
	private FrameLayout mFlNavIntoBar;
	private TextView mTxtName; // 导航信息控制条的文字
	private TextView mTxtDes;// 导航信息控制条的文字
	private Button mBtnStartNav; // 开始按钮

	private ImageButton mBtnMyLocation;

	// baiduView
	private MapView mMapView;
	// baiduView接口
	private BaiduMap mBaiduMap;
	// 百度接口实现类
	private BaiduImplClass mBaiduImplClass;

	// 定位配置：跟踪
	private MyLocationConfiguration mLocConfigFollow;
	// 定位配置
	private MyLocationConfiguration mLocConfigNormal;

	private GeoCoder mGeoCoder; // 搜索模块，也可去掉地图模块独立使用
	private OnGetGeoCoderResultListener mOnGetGeoCoderResultListener;
	// 记录点击的经纬度
	private LatLng mLatLng;
	// 点击点的图标
	private BitmapDescriptor mBitmapDescriptor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		setContentView(R.layout.activity_checkmap);
		initView();
		initOnClick();
		mBaiduMap.setMyLocationConfigeration(mLocConfigFollow);
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMap.hideInfoWindow();
		showNavBar(false);
		NavManager.getInstance().quickLocation(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		processIntent(getIntent());
		// 移动到最后的位置
		updateMaplocation(true);

		if (0 != mIntWhere) {
			mBtnStartNav.setText(getResources().getString(
					R.string.activity_check_map_set_text));
		} else {
			mBtnStartNav.setText(getResources().getString(
					R.string.activity_check_map_start_planing_text));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mMapView != null)
			mMapView.onResume();
	}

	@Override
	protected void onPause() {
		if (mMapView != null)
			mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (mMapView != null)
			mMapView.onDestroy();
		NavManager.getInstance().quickLocation(false);
		if (mGeoCoder != null)
			mGeoCoder.destroy();
		super.onDestroy();
	}

	private void init() {
		mBaiduImplClass = new BaiduImplClass();
		// 初始化搜索模块，注册事件监听
		mOnGetGeoCoderResultListener = new OnGetGeoCoderResult();
		// 百度定位
		mLocConfigFollow = new MyLocationConfiguration(
				MyLocationConfiguration.LocationMode.FOLLOWING, true, null);
		mLocConfigNormal = new MyLocationConfiguration(
				MyLocationConfiguration.LocationMode.NORMAL, true, null);

		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(mOnGetGeoCoderResultListener);

		mBitmapDescriptor = BitmapDescriptorFactory
				.fromResource(R.drawable.activity_check_map_ic_marker_selected);
	}

	private void initView() {
		mFlNavIntoBar = (FrameLayout) findViewById(R.id.flNavIntoBar);
		mTxtName = (TextView) findViewById(R.id.txtName);
		mTxtDes = (TextView) findViewById(R.id.txtDes);
		// 开始导航按钮
		mBtnStartNav = (Button) findViewById(R.id.btnStartNav);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();

		mBtnMyLocation = (ImageButton) findViewById(R.id.btnMoveToMyLocation);
	}

	private void initOnClick() {
		mBaiduMap.setOnMapStatusChangeListener(mBaiduImplClass);
		mBaiduMap.setOnMapClickListener(mBaiduImplClass);
		// 长按设置点
		mBaiduMap.setOnMapLongClickListener(mBaiduImplClass);

		mBtnStartNav.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 开始导航按钮
				if (mIntWhere == 0) {
					preview();
				} else {
					if (NavManager.LOCATION_COMPANY == mIntWhere) {
						NavManager.getInstance().setCompany(
								mTxtName.getText().toString(),
								mTxtDes.getText().toString(), mLatLng.latitude,
								mLatLng.longitude, UiMap.GPS_TYPE_BD09);
					} else if (NavManager.LOCATION_HOME == mIntWhere) {
						NavManager.getInstance().setHome(
								mTxtName.getText().toString(),
								mTxtDes.getText().toString(), mLatLng.latitude,
								mLatLng.longitude, UiMap.GPS_TYPE_BD09);
					}

					MyApplication.showToast(getResources().getString(
							R.string.activity_check_map_set_up_success_text));

					MyApplication.getInstance().finishActivity(
							CheckMapActivity.this);
					
					MyApplication.getInstance().finishActivity(
							SetLocationActivity.class);
				}
			}
		});

		mBtnMyLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				updateMaplocation(true);
				mBtnMyLocation
						.setImageResource(R.drawable.activity_check_map_ic_follow);
			}
		});
	}

	private void processIntent(Intent intent) {
		mIntWhere = 0;
		if (intent == null)
			return;
		mIntWhere = intent.getIntExtra("where", 0);
	}

	private void updateMaplocation(boolean ismapmovetomyloc) {
		LocationInfo location = NavManager.getInstance().getLocationInfo();
		if (location == null) {
			MyApplication.showToast("location is null");
			LogUtil.loge("location is null");
			return;
		}

		if (location.msgGpsInfo == null) {
			// MyApplication.showToast("location.msgGpsInfo is null");
			LogUtil.loge("location.msgGpsInfo is null");
			return;
		}

		LatLng ll = BDLocationUtil.getLocation(location.msgGpsInfo);
		if (ll == null) {
			LogUtil.loge("返回百度坐标系的Latlng error, BDLocationUtil.getLocation return is null.");
			return;
		}

		if (location.msgGpsInfo.fltRadius == null)
			location.msgGpsInfo.fltRadius = 0f;

		// 设置当前位置
		MyLocationData locData = new MyLocationData.Builder()
				.accuracy(location.msgGpsInfo.fltRadius)
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(0).latitude(ll.latitude).longitude(ll.longitude)
				.build();

		if (ismapmovetomyloc) {
			mBaiduMap.setMyLocationConfigeration(mLocConfigFollow);
			// 定位当前位置，并放大地图

			MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 16);
			mBaiduMap.animateMapStatus(u);
		} else {
			mBaiduMap.setMyLocationConfigeration(mLocConfigNormal);
		}

		mBaiduMap.setMyLocationData(locData);
		mBaiduMap.hideInfoWindow();
		LogUtil.logd("update info");
	}

	public void refreshMapLocation() {
		updateMaplocation(false);
	}

	private class BaiduImplClass implements OnMapStatusChangeListener,
			OnMapClickListener, OnMapLongClickListener {

		@Override
		public void onMapStatusChangeStart(MapStatus arg0) {
			mBtnMyLocation
					.setImageResource(R.drawable.activity_check_map_ic_point);
		}

		@Override
		public void onMapStatusChangeFinish(MapStatus arg0) {
		}

		@Override
		public void onMapStatusChange(MapStatus arg0) {
		}

		@Override
		public boolean onMapPoiClick(MapPoi arg0) {
			LogUtil.logd("latitude=" + arg0.getPosition().latitude
					+ " longitude=" + arg0.getPosition().longitude + " name"
					+ arg0.getName());

			mLatLng = arg0.getPosition();
			reverseGeoCode();
			mBaiduMap.clear();
			mTxtName.setText("");
			mTxtName.setText(arg0.getName());
			showNavBar(true);
			drawMark();
			MoveToLocation(mLatLng);
			return true;
		}

		@Override
		public void onMapClick(LatLng arg0) {
			mBaiduMap.clear();
			showNavBar(false);
		}

		@Override
		public void onMapLongClick(LatLng arg0) {
			mLatLng = arg0; 
			mTxtName.setText("地图选点");
			reverseGeoCode();
			showNavBar(true);
			drawMark();
			MoveToLocation(mLatLng);
		}
	}

	/**
	 * / 通过经纬度搜索poi结果类
	 *
	 */
	private class OnGetGeoCoderResult implements OnGetGeoCoderResultListener {

		@Override
		public void onGetGeoCodeResult(GeoCodeResult result) {
		}

		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				Context context = MyApplication.getApp();
				Toast.makeText(context, "没有搜索到结果", Toast.LENGTH_LONG).show();
			}
			mTxtDes.setText(result.getAddress());
		}

	}

	// 根据经纬搜索地理位置
	private void reverseGeoCode() {
		LatLng ptCenter = new LatLng(mLatLng.latitude, mLatLng.longitude);
		// 反Geo搜索
		if (!mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
				.location(ptCenter))) {
			LogUtil.loge("mSearch.reverseGeoCode");
		}
	}

	// 显示导航信息控制条
	private void showNavBar(boolean b) {
		if (b)
			mFlNavIntoBar.setVisibility(View.VISIBLE);
		else
			mFlNavIntoBar.setVisibility(View.GONE);
	}

	private void drawMark() {
		mBaiduMap.clear();
		OverlayOptions ob = new MarkerOptions().position(mLatLng)
				.icon(mBitmapDescriptor).zIndex(9);
		mBaiduMap.addOverlay(ob);
	}

	private void MoveToLocation(LatLng latLng) {
		// // 定位当前位置，并放大地图
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(latLng, 16);
		mBaiduMap.animateMapStatus(u);
	}

	// 导航
	private void preview() {
		NavManager.getInstance().startPreview(mTxtName.getText().toString(),
				mTxtDes.getText().toString(), mLatLng.latitude,
				mLatLng.longitude, UiMap.GPS_TYPE_BD09);
	}
}
