package com.txznet.txz.component.film;

import com.txznet.txz.component.choice.list.FilmWorkChoice;

import java.util.Comparator;

public class FilmScoreComparator implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof FilmWorkChoice.FilmItem && o2 instanceof FilmWorkChoice.FilmItem) {
            FilmWorkChoice.FilmItem mt1 = (FilmWorkChoice.FilmItem) o1;
            FilmWorkChoice.FilmItem mt2 = (FilmWorkChoice.FilmItem) o2;
            return 0 - Double.compare(mt1.score, mt2.score);
        }
        return 0;
    }
}
