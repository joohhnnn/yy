package com.txznet.txz.component.film;

import com.txznet.txz.component.choice.list.MovieTimeWorkChoice;

import java.util.Comparator;

public class FilmPriceComparator implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof MovieTimeWorkChoice.MovieTimeItem && o2 instanceof MovieTimeWorkChoice.MovieTimeItem) {
            MovieTimeWorkChoice.MovieTimeItem mt1 = (MovieTimeWorkChoice.MovieTimeItem) o1;
            MovieTimeWorkChoice.MovieTimeItem mt2 = (MovieTimeWorkChoice.MovieTimeItem) o2;
            return Double.compare(mt1.unitPrice, mt2.unitPrice);
        }
        return 0;
    }
}
