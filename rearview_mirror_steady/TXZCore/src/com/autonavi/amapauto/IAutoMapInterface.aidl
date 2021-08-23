package com.autonavi.amapauto;

interface IAutoMapInterface {
     void zoomIn();
     void zoomOut();
     void moveMap(double lat,double lng);
     void setNightMode(boolean isNight);
}