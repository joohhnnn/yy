package com.txznet.txz.util;

public class DistanceUtil {
    private static final double EARTH_RADIUS = 6378137;//地球半径,单位米

    public static double getDistanceFromLngLat(double startLat, double startLng, double endLat, double endLng) {
        double lat1 = startLat * Math.PI / 180.0;
        double lng1 = startLng * Math.PI / 180.0;
        double lat2 = endLat * Math.PI / 180.0;
        double lng2 = endLng * Math.PI / 180.0;

        double lat = lat1 - lat2;
        double lng = lng1 - lng2;

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(lat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(lng / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
}
