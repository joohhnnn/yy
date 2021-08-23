package com.txznet.record.bean;

import com.txznet.record.adapter.TrainTicketListAdapter;

/**
 * Created by daviddai on 2019/9/20
 */
public class TrainTicketMsg extends BaseDisplayMsg<TrainTicketListAdapter.TrainTicketItem> {

    public TrainTicketMsg() {
        super(TYPE_FROM_SYS_TRAIN_TICKET);
    }
}
