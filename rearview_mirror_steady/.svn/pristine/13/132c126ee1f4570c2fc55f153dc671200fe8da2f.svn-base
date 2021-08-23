package com.txznet.txz.component.film;

import com.txznet.txz.component.choice.list.MovieTimeWorkChoice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class FilmTimeComparator implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof MovieTimeWorkChoice.MovieTimeItem && o2 instanceof MovieTimeWorkChoice.MovieTimeItem) {
            MovieTimeWorkChoice.MovieTimeItem mt1 = (MovieTimeWorkChoice.MovieTimeItem) o1;
            MovieTimeWorkChoice.MovieTimeItem mt2 = (MovieTimeWorkChoice.MovieTimeItem) o2;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date date1 = sdf.parse(mt1.showTime);
                Date date2 = sdf.parse(mt2.showTime);
                if(date1.before(date2)){
                    return -1;
                }else {
                    return 1;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}
