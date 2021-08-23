package com.txznet.txz.component.ticket;

import com.txznet.comm.ui.viewfactory.data.QiwuTrainTicketPayViewData;
import com.txznet.sdk.bean.TicketBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class TicketOrdTimeComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof QiwuTrainTicketPayViewData.TicketPayBean && o2 instanceof QiwuTrainTicketPayViewData.TicketPayBean) {
            QiwuTrainTicketPayViewData.TicketPayBean mt1 = (QiwuTrainTicketPayViewData.TicketPayBean) o1;
            QiwuTrainTicketPayViewData.TicketPayBean mt2 = (QiwuTrainTicketPayViewData.TicketPayBean) o2;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date date1 = sdf.parse(mt1.orderTime);
                Date date2 = sdf.parse(mt2.orderTime);
                if(date1.before(date2)){
                    return 1;
                }else {
                    return -1;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}
