package com.txznet.launcher.widget.nav;

import java.util.List;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 * 定义自己的地图View已经具有的功能
 */

public interface IMap {

    class MapMarker {
        public String title;
        public int resId;
        public double lat;
        public double lng;

        public MapMarker(String title, int resId, double lat, double lng) {
            this.title = title;
            this.resId = resId;
            this.lat = lat;
            this.lng = lng;
        }
    }

    class Polyline {

    }

    void init();

    void addMarker(List<MapMarker> markers);

    void removeMarker(MapMarker mapMarker);

    void removeAllMarker();

    void showPathPolyline(List<Polyline> polylines);
}