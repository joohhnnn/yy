package com.txznet.launcher.widget.nav;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.loader.AppLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 * 对高德地图的view做包装，包括了在view上显示一个当前位置图标，图标上显示一个地点名字。
 */

public class AmapMap extends LinearLayout implements IMap {

    public AmapMap(Context context) {
        this(context, null);
    }

    public AmapMap(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmapMap(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //    private MapView mMapView;
    private TextureMapView mMapView;
    private AMap mAmap;

    @Override
    public void init() {
        removeAllViews();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.map_amap_ly, null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(view, params);
        initMapView(view);
    }

    public void onResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    public void onDestory() {
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    private void initMapView(View view) {
        mMapView = (TextureMapView) view.findViewById(R.id.map);
        mMapView.onCreate(null);
        mAmap = mMapView.getMap();
        mAmap.getUiSettings().setScaleControlsEnabled(true);
        mAmap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
        mAmap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                LogUtil.logd("onMapLoaded");
                isMapLoaded = true;
            }
        });
        addWindowInfo();
    }

    /**
     * 定位到当前位置
     */
    public void moveToLocation() {
    }

    private List<Marker> mIconMarkers = new ArrayList<>();

    @Override
    public void addMarker(List<MapMarker> markers) {
        for (MapMarker marker : markers) {
            Marker m = mAmap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(marker.lat, marker.lng))
                            .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(
                                    GlobalContext.get().getResources(), marker.resId))));
            m.setTitle(marker.title);
            m.showInfoWindow();
            mIconMarkers.add(m);
        }

        if (markers.size() == 1) {
            double lat = markers.get(0).lat;
            double lng = markers.get(0).lng;
            mAmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 16));
        }
    }

    public void addWindowInfo() {
        mAmap.setInfoWindowAdapter(new AMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return createInfoWindow(marker.getTitle());
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
    }

    private View createInfoWindow(String poiName) {
        if (poiName == null) {
            poiName = "";
        }
        TextView tv = new TextView(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(params);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(16);
        tv.setTextColor(Color.WHITE);
        tv.setText(poiName);
        tv.setPadding(15, 8, 15, 11);
        tv.setBackgroundResource(R.drawable.poi_issued_name_bg);
        return tv;
    }

    @Override
    public void removeMarker(MapMarker mapMarker) {
    }

    @Override
    public void removeAllMarker() {
        for (Marker marker : mIconMarkers) {
            marker.destroy();
        }
        mIconMarkers.clear();
    }

    @Override
    public void showPathPolyline(List<Polyline> polylines) {

    }

    public static interface OnMapScreenShotListener {
        void onMapScreenShot(Bitmap bitmap);
    }

    private boolean isMapLoaded;
    private OnMapScreenShotListener mOnMapScreenShotListener;

    public void getMapScreenShot(final OnMapScreenShotListener listener) {
        if (mAmap == null) {
            listener.onMapScreenShot(null);
            return;
        }
        this.mOnMapScreenShotListener = listener;
        mAmap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(final Bitmap bitmap) {
            }

            @Override
            public void onMapScreenShot(Bitmap bitmap, int i) {
                LogUtil.logd("onMapScreenShot isMapLoaded=" + isMapLoaded + ", status=" + i);
                if (!isMapLoaded) { // 地图未渲染完整
                    AppLogic.runOnBackGround(new Runnable() {
                        @Override
                        public void run() {
                            getMapScreenShot(mOnMapScreenShotListener);
                        }
                    }, 500);
                } else {
                    mOnMapScreenShotListener.onMapScreenShot(bitmap);
                    mOnMapScreenShotListener = null;
                }
            }
        });
    }
}
