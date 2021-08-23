package com.txznet.txz.component.ticket;

import com.txznet.sdk.bean.TicketBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class TicketTimeComparator implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof TicketBean && o2 instanceof TicketBean) {
            TicketBean mt1 = (TicketBean) o1;
            TicketBean mt2 = (TicketBean) o2;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            try {
                Date date1 = sdf.parse(mt1.departureTime);
                Date date2 = sdf.parse(mt2.departureTime);
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
