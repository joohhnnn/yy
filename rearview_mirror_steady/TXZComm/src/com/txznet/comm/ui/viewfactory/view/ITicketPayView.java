package com.txznet.comm.ui.viewfactory.view;

import com.txznet.comm.ui.viewfactory.data.ViewData;

/**
 * Created by daviddai on 2019/9/11
 */
public abstract class ITicketPayView extends IListView {

    @Override
    public void init() {
        mViewType = ViewData.TYPE_FULL_TICKET_PAY;
    }
}
