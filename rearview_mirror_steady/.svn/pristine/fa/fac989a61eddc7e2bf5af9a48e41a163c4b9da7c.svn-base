package com.txznet.txz.component.ticket;

import com.txznet.sdk.bean.TicketBean;

import java.util.Comparator;

public class TicketPriceComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof TicketBean && o2 instanceof TicketBean) {
            TicketBean mt1 = (TicketBean) o1;
            TicketBean mt2 = (TicketBean) o2;
            return Double.compare(Double.valueOf(mt1.recommendPrice), Double.valueOf(mt2.recommendPrice));
        }
        return 0;
    }
}
