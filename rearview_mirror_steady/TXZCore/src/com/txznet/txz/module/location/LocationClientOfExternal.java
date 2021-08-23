package com.txznet.txz.module.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.StreetNumber;
import com.qihu.mobile.lbs.geocoder.Geocoder;
import com.qihu.mobile.lbs.geocoder.GeocoderAsy;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.util.runnables.Runnable2;

import java.util.List;
import java.util.Random;

public class LocationClientOfExternal implements ILocationClient {
    private static final String LOCATION_SEND_ACTION = "com.txznet.txz.location.action.send";
    private static final String LOCATION_RECV_ACTION = "com.txznet.txz.location.action.recv";

    public static interface LocOutterInitCallback {
        void onInit(boolean bLink);
    }

    private static final int KEY_TYPE_MITT = 1;
    private static final int KEY_TYPE_RES_LOCATION = 2;

    private boolean isExsitOutterTool = false;
    private LocOutterInitCallback mCallback;
    private HandlerThread mHandlerThread;
    private GeoSearch mGeoSearch;
    private GeoSearchResultListener mGeoSearchResultListener;

    public LocationClientOfExternal(LocOutterInitCallback callback) {
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                doOnRecv(intent);
            }
        }, new IntentFilter(LOCATION_RECV_ACTION));
        mCallback = callback;
        createLocTool();
    }

    private void createLocTool() {
        Intent intent = new Intent();
        intent.putExtra("KEY_TYPE", 1);
        sendAction(intent);
    }

    private void doOnRecv(Intent intent) {
        int keyType = intent.getIntExtra("KEY_TYPE", -1);
        String sApp = intent.getStringExtra("SOURCE_APP");
        if (DebugCfg.ENABLE_TRACE_GPS) {
            LogUtil.logd("keyType:" + keyType + ",sApp:" + sApp);
        }
        switch (keyType) {
            case KEY_TYPE_MITT:
                isExsitOutterTool = intent.getBooleanExtra("isExist", false);
                if (mCallback != null) {
                    mCallback.onInit(isExsitOutterTool);
                }
                LogUtil.logd("isExsitOutterTool:" + isExsitOutterTool);
                break;
            case KEY_TYPE_RES_LOCATION:
                convBundle(intent.getExtras());
                break;
            default:
                break;
        }
    }

    public boolean isAvailable() {
        return isExsitOutterTool;
    }

    private LocationInfo mCurrLocationInfo;

    private void convBundle(Bundle bundle) {
        if (bundle == null) {
            LogUtil.loge("null bundle！");
            return;
        }
        if (DebugCfg.ENABLE_TRACE_GPS) {
            LogUtil.logd("bundle:" + bundle);
        }


        mCurrLocationInfo = new LocationInfo();
        mCurrLocationInfo.uint32Time = (int) (bundle.getLong("time") / 1000);
        mCurrLocationInfo.msgGpsInfo = new GpsInfo();
        mCurrLocationInfo.msgGpsInfo.dblLat = bundle.getDouble("lat");
        mCurrLocationInfo.msgGpsInfo.dblLng = bundle.getDouble("lng");
        mCurrLocationInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;


        String type = bundle.getString("type");
        if (TextUtils.equals(type, "qihoo")) {
            if (mGeoSearch == null) {
                mGeoSearch = new QiHooGeoImpl();
            }
            if (mGeoSearchResultListener == null) {
                mGeoSearchResultListener = new GeoSearchResultListener() {
                    @Override
                    public void onResult(final LocationInfo info) {
                        setLastLocation(info);
                        LocationManager.getInstance().notifyUpdatedLocation();
                    }

                    @Override
                    public void onError(final LocationInfo info, final String errorMessage) {
                        setLastLocation(info);
                        LocationManager.getInstance().notifyUpdatedLocation();
                    }
                };
            }
            mGeoSearch.reverseGeoCode(mCurrLocationInfo, mGeoSearchResultListener);
        } else if (TextUtils.equals(type, "amap")) {
            if (mGeoSearch == null) {
                mGeoSearch = new AmapGeoImpl();
            }
            if (mGeoSearchResultListener == null) {
                mGeoSearchResultListener = new GeoSearchResultListener() {
                    @Override
                    public void onResult(final LocationInfo info) {
                        setLastLocation(info);
                        LocationManager.getInstance().notifyUpdatedLocation();
                    }

                    @Override
                    public void onError(final LocationInfo info, final String errorMessage) {
                        setLastLocation(info);
                        LocationManager.getInstance().notifyUpdatedLocation();
                    }
                };
            }
            mGeoSearch.reverseGeoCode(mCurrLocationInfo, mGeoSearchResultListener);
        } else {
            mCurrLocationInfo.msgGeoInfo = new GeoInfo();
            mCurrLocationInfo.msgGeoInfo.strAddr = bundle.getString("address");
            mCurrLocationInfo.msgGeoInfo.strCity = bundle.getString("city");
            mCurrLocationInfo.msgGeoInfo.strCityCode = bundle.getString("cityCode");
            mCurrLocationInfo.msgGeoInfo.strDistrict = bundle.getString("district");
            mCurrLocationInfo.msgGeoInfo.strProvice = bundle.getString("provice");
            LocationManager.getInstance().notifyUpdatedLocation();
        }
    }


    private int mTimeInterval;

    @Override
    public void quickLocation(boolean bQuick) {
        Intent intent = new Intent();
        intent.putExtra("KEY_TYPE", 2);
        intent.putExtra("QUICK", bQuick);
        if (bQuick) {
            intent.putExtra("TIMEINTERVAL", mTimeInterval * 1000);
        } else {
            intent.putExtra("TIMEINTERVAL", 3 * 60 * 1000);
        }
        sendAction(intent);

        LocationManager.getInstance().reinitLocationClientDelay();
    }

    @Override
    public void setLastLocation(LocationInfo location) {
        LogUtil.logd("setLastLocation:" + location);
        this.mCurrLocationInfo = location;
    }

    @Override
    public LocationInfo getLastLocation() {
        LogUtil.logd("getLastLocation:" + mCurrLocationInfo);
        return mCurrLocationInfo;
    }

    @Override
    public void setTimeInterval(int timeInterval) {
        LogUtil.logd("setTimeInterval:" + timeInterval);
        mTimeInterval = timeInterval;
    }

    @Override
    public void release() {
        Intent intent = new Intent();
        intent.putExtra("KEY_TYPE", 3);
        sendAction(intent);

        LocationManager.getInstance().removeReinitDelayRunnable();
    }


    private void sendAction(Intent intent) {
        intent.setAction(LOCATION_SEND_ACTION);
        intent.putExtra("SOURCE_APP", "txz");
        GlobalContext.get().sendBroadcast(intent);
    }

    interface GeoSearchResultListener {
        void onResult(LocationInfo info);

        void onError(LocationInfo info, String errorMessage);
    }

    interface GeoSearch {
        void reverseGeoCode(LocationInfo locationInfo, GeoSearchResultListener listener);
    }

    class QiHooGeoImpl implements GeoSearch {
        GeocoderAsy mGeocoderAsy;

        @Override
        public void reverseGeoCode(final LocationInfo location,
                final GeoSearchResultListener searchResultListener) {
            if (mGeocoderAsy == null) {
                mGeocoderAsy = new GeocoderAsy(GlobalContext.get());
            }
            // 创建逆地理编码和地理编码检索监听者
            GeocoderAsy.GeocoderListener listener = new GeocoderAsy.GeocoderListener() {
                // 地理编码监听函数
                @Override
                public void onGeocodeResult(Geocoder.GeocoderResult result) {
                }

                // 逆地理编码监听函数，输出经纬度点对应的地址信息
                @Override
                public void onRegeoCodeResult(Geocoder.GeocoderResult result, String description) {
                    if (result.code != 0) {
                        if (searchResultListener != null) {
                            searchResultListener.onError(location, result.code + "");
                        }
                        return;
                    }
                    List<Geocoder.QHAddress> list = result.address;
                    if (list != null && list.size() > 0) {
                        Geocoder.QHAddress address = list.get(0);
                        GeoInfo geoInfo = new GeoInfo();
                        geoInfo.strAddr = address.getFormatedAddress();
                        geoInfo.strCity = address.getCity();
                        geoInfo.strDistrict = address.getDistrict();
                        geoInfo.strProvice = address.getProvince();
                        geoInfo.strStreet = address.getStreet();
                        location.msgGeoInfo = geoInfo;
                        if (searchResultListener != null) {
                            searchResultListener.onResult(location);
                        }
                    }
                }
            };
            mGeocoderAsy
                    .regeocode(location.msgGpsInfo.dblLat, location.msgGpsInfo.dblLng, listener);
        }
    }

    class AmapGeoImpl implements GeoSearch {
        GeocodeSearch mSearch;
        int mSearchSeq = new Random().nextInt();
        int mLastSearchSeq = 0;

        int getNextSearchSeq() {
            ++mSearchSeq;
            if (mSearchSeq == 0) {
                ++mSearchSeq;
            }
            return mSearchSeq;
        }

        LocationManager.SeqGeoSearchListener mSearchListener;

        public void cancelRequestGeoCode() {
            if (mSearchListener != null) {
                mSearchListener.cancelRequest();
            }
        }

        private static final int SEARCH_TIME_EXCEED = 4000;
        Runnable searchTimerOutTask;

        public void reverseGeoCode(final LocationInfo location,
                final GeoSearchResultListener searchResultListener) {
            if (mSearch == null) {
                mSearch = new GeocodeSearch(GlobalContext.get());
            }
            searchTimerOutTask = new Runnable2<LocationInfo, GeoSearchResultListener>(location,
                    searchResultListener) {

                @Override
                public void run() {
                    mLastSearchSeq = 0;
                    AppLogic.removeBackGroundCallback(searchTimerOutTask);
                    if (mP2 != null) {
                        mP2.onError(mP1, -1 + "");
                    }
                }
            };
            int seq = mLastSearchSeq = getNextSearchSeq();
            AppLogic.removeBackGroundCallback(searchTimerOutTask);
            AppLogic.runOnBackGround(searchTimerOutTask, SEARCH_TIME_EXCEED);

            if (mSearchListener != null) {
                mSearchListener.cancelRequest();
            }
            mSearchListener = new LocationManager.SeqGeoSearchListener(seq) {
                @Override
                public void onRegeocodeSearched(RegeocodeResult result, int argq, int seq) {
                    if (seq != mLastSearchSeq) {
                        if (searchResultListener != null) {
                            searchResultListener.onError(location, -1 + "");
                        }
                        return;
                    }
                    if (mSearch == null) {
                        if (searchResultListener != null) {
                            searchResultListener.onError(location, -1 + "");
                        }
                        return;
                    }
                    mSearch = null;
                    RegeocodeAddress rAddress = result.getRegeocodeAddress();
                    if (rAddress == null) {
                        if (searchResultListener != null) {
                            searchResultListener.onError(location, -1 + "");
                        }
                        return;
                    }

                    GeoInfo geoInfo = new GeoInfo();
                    geoInfo.strAddr = rAddress.getFormatAddress();
                    geoInfo.strCity = rAddress.getCity();
                    geoInfo.strProvice = rAddress.getProvince();
                    geoInfo.strDistrict = rAddress.getDistrict();
                    if (TextUtils.isEmpty(geoInfo.strCity)) {
                        geoInfo.strCity = geoInfo.strProvice;
                    }
                    StreetNumber sn = rAddress.getStreetNumber();
                    if (sn != null) {
                        geoInfo.strStreet = sn.getStreet();
                        geoInfo.strStreetNum = sn.getNumber();
                    }

                    location.msgGeoInfo = geoInfo;
                    AppLogic.removeBackGroundCallback(searchTimerOutTask);
                    if (searchResultListener != null) {
                        searchResultListener.onResult(location);
                    }
                }
            };
            mSearch.setOnGeocodeSearchListener(mSearchListener);
            LatLonPoint llp =
                    new LatLonPoint(location.msgGpsInfo.dblLat, location.msgGpsInfo.dblLng);
            RegeocodeQuery rq = new RegeocodeQuery(llp, 200, GeocodeSearch.AMAP);
            mSearch.getFromLocationAsyn(rq);
        }
    }


}