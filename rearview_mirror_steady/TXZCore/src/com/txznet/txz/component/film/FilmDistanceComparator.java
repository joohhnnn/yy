package com.txznet.txz.component.film;

import com.txznet.txz.component.choice.list.MovieTheaterWorkChoice;

import java.util.Comparator;

public class FilmDistanceComparator implements Comparator<MovieTheaterWorkChoice.MovieTheaterItem> {
    @Override
    public int compare(MovieTheaterWorkChoice.MovieTheaterItem o1, MovieTheaterWorkChoice.MovieTheaterItem o2) {
        String distanceO1 = o1.distance;
        String distanceO2 = o2.distance;
        double distance1 = 0;
        double distance2 = 0;
        if(distanceO1.contains("km")){
            distanceO1 = distanceO1.replace("km","");
            distance1 = Double.valueOf(distanceO1) * 1000;
        }else {
            distanceO1 = distanceO1.replace("m","");
            distance1 = Double.valueOf(distanceO1);
        }
        if(distanceO2.contains("km")){
            distanceO2 = distanceO2.replace("km","");
            distance2 = Double.valueOf(distanceO2) * 1000;
        }else {
            distanceO2 = distanceO2.replace("m","");
            distance2 = Double.valueOf(distanceO2);
        }
        return Double.compare(distance1, distance2);
    }
}
