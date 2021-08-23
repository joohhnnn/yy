package com.txznet.launcher.module.nav;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.LatLng;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.component.nav.PoiIssuedTool;
import com.txznet.launcher.domain.nav.NavManager;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.img.GlideApp;
import com.txznet.launcher.img.ImgLoader;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.widget.container.MainContainer;
import com.txznet.launcher.widget.nav.AmapMap;
import com.txznet.launcher.widget.nav.IMap;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZLocationManager;
import com.txznet.sdk.bean.LocationData;
import com.txznet.txz.util.runnables.Runnable1;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 * 展示下发地址的界面
 */
public class PoiIssuedModule extends BaseModule {

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        mParent = (MainContainer) parent;
        View view = View.inflate(context, R.layout.layout_poi_issued, null);
        initView(view);
        return view;
    }

    private MainContainer mParent;
    private TextView mPoiFromTv;
    private LinearLayout mMapLy;
    //    private TextView mPoiNameTv;
    private TextView mDistanceTv;
    private TextView mAddressTv;
    private TextView mWakeupKwsTv;
    private AmapMap mAmapMap;

    private ViewGroup mPoiMapLy;
    //    private PageContainer mPageContainer;
    private Context mContext;

    private LinearLayout pageInfoLy;
    private ImageView prevPageIv;
    private TextView helpTipTv;
    private ImageView mapReviewIv;
    private ImageView pointIv;

    private void initView(View view) {
        mContext = view.getContext();
        mPoiFromTv = (TextView) view.findViewById(R.id.poi_from_tv);
        mMapLy = (LinearLayout) view.findViewById(R.id.map_ly);
//        mPoiNameTv = (TextView) view.findViewById(R.id.poi_name_tv);
        mDistanceTv = (TextView) view.findViewById(R.id.distance_tv);
        mAddressTv = (TextView) view.findViewById(R.id.address_tv);
        mWakeupKwsTv = (TextView) view.findViewById(R.id.wakeup_kws_tv);
        mPoiMapLy = (ViewGroup) view.findViewById(R.id.poi_map_ly);
//        mPageContainer = (PageContainer) view.findViewById(R.id.page_ly);

        pageInfoLy = view.findViewById(R.id.page_info_ly);
        prevPageIv = view.findViewById(R.id.prev_page_iv);
        helpTipTv = view.findViewById(R.id.help_tip_tv);
        mapReviewIv = view.findViewById(R.id.map_review_iv);
        pointIv = view.findViewById(R.id.point_iv);
    }

    private void initState(boolean isSingle) {
//        if (isSingle) {
//            mPoiMapLy.setVisibility(View.VISIBLE);
//            mPageContainer.setVisibility(View.GONE);
//        } else {
//            mParent.updateContentLayoutParams(0);
//            mPageContainer.setVisibility(View.VISIBLE);
//            mPoiMapLy.setVisibility(View.GONE);
//            mPageContainer.setContentView(mPoiMapLy);
//        }

        mParent.updateContentLayoutParams(0);
        if (isSingle) {
            if (View.VISIBLE == pageInfoLy.getVisibility()) {
                pageInfoLy.setVisibility(View.INVISIBLE);
            }
        } else {
            if (View.VISIBLE != pageInfoLy.getVisibility()) {
                pageInfoLy.setVisibility(View.VISIBLE);
            }
        }
    }

    private void replaceMap(Context context) {
        mAmapMap = new AmapMap(context);
        mAmapMap.init();

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMapLy.removeAllViewsInLayout();
        mMapLy.addView(mAmapMap, params);
        mAmapMap.onResume();
        TextureMapView mMapView = (TextureMapView) mAmapMap.findViewById(R.id.map);
        mMapView.getMap().getUiSettings().setZoomControlsEnabled(false);
        mMapView.getMap().getUiSettings().setScaleControlsEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{EventTypes.EVENT_POI_ISSUED};
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        onResume();
    }

    @Override
    public void onResume() {
        replaceMap(mContext);
        List data = NavManager.getInstance().getCurrNotifyItems();
        if (data != null && data.size() > 1) {
            initState(false);
        } else {
            initState(true);
        }
        applyIssued(NavManager.getInstance().getCurrActiveItem());
        NavManager.getInstance().registerPoiIssuedWakeupAsr(new NavManager.WakeupAsrSelectCallback() {
            @Override
            public void onNext() {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    applyIssued(NavManager.getInstance().getCurrActiveItem());
                    prevPageIv.setVisibility(View.VISIBLE);
                    helpTipTv.setText("说“上一页”或“下一页”查看");
                } else {
                    AppLogic.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            applyIssued(NavManager.getInstance().getCurrActiveItem());
                            prevPageIv.setVisibility(View.VISIBLE);
                            helpTipTv.setText("说“上一页”或“下一页”查看");
                        }
                    });
                }
            }

            @Override
            public void onPre() {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    applyIssued(NavManager.getInstance().getCurrActiveItem());
                    helpTipTv.setText("说“上一页”或“下一页”查看");
                    mapReviewIv.setVisibility(View.VISIBLE);
                } else {
                    AppLogic.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            applyIssued(NavManager.getInstance().getCurrActiveItem());
                            helpTipTv.setText("说“上一页”或“下一页”查看");
                            mapReviewIv.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onPrePage() {
                LogUtil.logd("poiIssued onPrePage");
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        prevPageIv.setVisibility(View.INVISIBLE);
                        helpTipTv.setText("说“下一页”查看");
                        pointIv.setImageResource(R.drawable.artboard_adown);
                        mapReviewIv.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onLastPage() {
                LogUtil.logd("poiIssued onLastPage");
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        helpTipTv.setText("说“上一页”查看");
                        pointIv.setImageResource(R.drawable.artboard_upward);
                        mapReviewIv.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onNotifyKwsUpdate(String noticeText) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    mWakeupKwsTv.setText(Html.fromHtml(noticeText));
                } else {
                    AppLogic.runOnUiGround(new Runnable1<String>(noticeText) {
                        @Override
                        public void run() {
                            mWakeupKwsTv.setText(Html.fromHtml(mP1));
                        }
                    });
                }
            }
        });
    }

    private void applyIssued(PoiIssuedTool.IssuedPoi item) {
        if (item == null) {
            return;
        }
        String source = item.source;
        String name = item.poiName;
        String address = item.poiAddress;
        double lat = item.lat;
        double lng = item.lng;

        clearAllMarker();
        markAndMoveToCenter(name, lat, lng);
        mPoiFromTv.setText(source);
//        mPoiNameTv.setText(name);

        LocationData locationData = TXZLocationManager.getInstance().getCurrentLocationInfo();
        if (locationData != null) {
            float distance = AMapUtils.calculateLineDistance(
                    new LatLng(locationData.dbl_lat, locationData.dbl_lng),
                    new LatLng(lat, lng));
            String distanceStr;
            if (distance < 1000) {
                distanceStr = Math.round(distance) + "M";
            } else {
                distanceStr = new DecimalFormat("#.#").format(distance / 1000) + "KM";
            }
            mDistanceTv.setText(distanceStr);
        }
        mAddressTv.setText(address);

        mapReviewIv.setVisibility(View.VISIBLE);
        mapReviewIv.setImageDrawable(null);
        mAmapMap.getMapScreenShot(new AmapMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(Bitmap bitmap) {
                LogUtil.logd("onMapScreenShot, bitmap=" + bitmap);
                if (bitmap != null&&mapReviewIv.getVisibility()==View.VISIBLE) {
                    Bitmap shot = getEdgeBitmap(mapReviewIv, bitmap);
                    ImgLoader.loadRoundedImage(shot, mapReviewIv, 10);
//                    mapReviewIv.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 获取下一张地图的顶部边缘
     *
     * @return
     */
    private Bitmap getEdgeBitmap(View view, @NonNull final Bitmap oriBitmap) {
        LogUtil.logd("onMapScreenShot, " + view.getWidth() + ":" + view.getHeight());
        final Bitmap result = Bitmap.createBitmap(oriBitmap, 0, 0, view.getWidth(), view.getHeight());
        if (!oriBitmap.isRecycled()) {
            oriBitmap.recycle();
        }
        return result;
    }

    private void clearAllMarker() {
        if (mAmapMap != null) {
            mAmapMap.removeAllMarker();
        }
    }

    private void markAndMoveToCenter(String title, double lat, double lng) {
        if (mAmapMap == null) {
            return;
        }
        List<IMap.MapMarker> markers = new ArrayList<>();
        IMap.MapMarker marker = new IMap.MapMarker(title, R.drawable.icon_map_marker, lat, lng);
        markers.add(marker);
        mAmapMap.addMarker(markers);
    }

    @Override
    public void onPreRemove() {
        if (mAmapMap != null) {
            mAmapMap.onPause();
            mAmapMap.onDestory();
        }
        NavManager.getInstance().recoverPoiIssuedWakeupAsr();
    }

    @Override
    protected void onEvent(String eventType) {
        if (EventTypes.EVENT_POI_ISSUED.equals(eventType)) {
        }
    }
}