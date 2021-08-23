package com.txznet.record.bean;

import com.txznet.record.adapter.FlightTicketListAdapter;

/**
 * Created by daviddai on 2019/9/20
 */
public class FlightTicketMsg extends BaseDisplayMsg<FlightTicketListAdapter.FlightTicketItem> {

    public FlightTicketMsg() {
        super(TYPE_FROM_SYS_FLIGHT_TICKET);
    }
}
