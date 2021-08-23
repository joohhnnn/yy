package com.txznet.txz.component.nav.base;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.sdk.bean.Poi;

/**
 * 路线信息(总)
 */
public class BaseRoadInfo {
    public double fromPoiLat;
    public double fromPoiLng;
    public String fromPoiAddr;
    public String fromPoiName;
    public double toPoiLat;
    public double toPoiLng;
    public String toPoiAddr;
    public String toPoiName;
    public BasePathInfo[] pathInfos;
    public int pathNum;

    public Poi getDestinationPoi() {
        Poi poi = new Poi();
        poi.setLat(toPoiLat);
        poi.setLng(toPoiLng);
        poi.setName(toPoiName);
        poi.setGeoinfo(toPoiAddr);
        poi.setSourceType(Poi.POI_SOURCE_GAODE_IMPL);
        return poi;
    }

    public Poi getCurrentPoi() {
        Poi poi = new Poi();
        poi.setLat(fromPoiLat);
        poi.setLng(fromPoiLng);
        poi.setName(fromPoiName);
        poi.setGeoinfo(fromPoiAddr);
        poi.setSourceType(Poi.POI_SOURCE_GAODE_IMPL);
        return poi;
    }

    public void printLatLngInfo() {
        LogUtil.logd("fromPoiLat:" + fromPoiLat + ",fromPoiLng:" + fromPoiLng + ",toPoiLat:" + toPoiLat
                + ",toPoiLng:" + toPoiLng);
    }
}