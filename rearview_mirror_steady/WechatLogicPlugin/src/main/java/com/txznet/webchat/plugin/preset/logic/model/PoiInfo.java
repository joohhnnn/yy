package com.txznet.webchat.plugin.preset.logic.model;

import android.text.TextUtils;

/**
 * Created by J on 2017/7/12.
 */

public class PoiInfo {
    /**
     * 纬度，gcj02坐标系
     */
    double lat;
    /**
     * 经度，gcj02坐标系
     */
    double lng;
    /**
     * 地理信息字符串
     */
    String geo;

    public PoiInfo setLat(double lat) {
        this.lat = lat;
        return this;
    }

    public PoiInfo setLng(double lng) {
        this.lng = lng;
        return this;
    }

    public PoiInfo setGeoInfo(String geo) {
        this.geo = geo;
        return this;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getGeoInfo() {
        return geo;
    }

    /**
     * 检查此地址信息是否完整
     * 地址信息完整表示地址信息的3个字段都已填充
     *
     * @return
     */
    public boolean isPoiComplete() {
        if (!isPoiLegal() || TextUtils.isEmpty(geo)) {
            return false;
        }

        return true;
    }

    /**
     * 检查此地址信息是否合法
     * <p>
     * 因为位置信息有可能无法完全解析成功, 有时地址信息可能只包含经纬度信息, 此时仍
     * 认为此地址信息是合法的
     *
     * @return
     */
    public boolean isPoiLegal() {
        return !(0 >= lat && 0 >= lng);
    }

    @Override
    public String toString() {
        return String.format("PoiInfo[%s(%s, %s)]", geo, lat, lng);
    }
}
