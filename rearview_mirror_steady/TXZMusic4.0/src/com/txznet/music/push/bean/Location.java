package com.txznet.music.push.bean;

/**
 * Created by brainBear on 2017/12/12.
 */

public class Location {

    private double lng;
    private double lat;
    private String from;

    public Location() {
    }

    public Location(double lng, double lat, String from) {
        this.lng = lng;
        this.lat = lat;
        this.from = from;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "Location{" +
                "lng=" + lng +
                ", lat=" + lat +
                ", from='" + from + '\'' +
                '}';
    }
}
