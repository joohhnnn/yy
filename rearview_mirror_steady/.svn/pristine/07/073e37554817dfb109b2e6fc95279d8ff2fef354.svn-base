package com.txznet.txz.component.choice.page;

import com.txznet.comm.ui.viewfactory.data.QiwuTrainTicketPayViewData;

import java.util.ArrayList;

public abstract class ResTicketPayPage  extends ResourcePage<QiwuTrainTicketPayViewData, QiwuTrainTicketPayViewData.TicketPayBean> {

    public ResTicketPayPage(QiwuTrainTicketPayViewData resources) {
        super(resources, resources.mTicketBeans.size());
    }

    @Override
    protected void clearCurrRes(QiwuTrainTicketPayViewData currRes) {

        if (currRes == null) {
            return;
        }
        currRes.title = "";
        if (currRes.mTicketBeans == null || currRes.mTicketBeans.size() == 0) {
            return;
        }
        currRes.mTicketBeans.clear();

    }

    @Override
    protected QiwuTrainTicketPayViewData notifyPage(int sIdx, int len, QiwuTrainTicketPayViewData sourceRes) {
        QiwuTrainTicketPayViewData data = null;
        if (sourceRes == null) {
            return null;
        }
        data = new QiwuTrainTicketPayViewData();
        //data.mTrainTicketBeans = sourceRes.mTrainTicketBeans;

        data.title = sourceRes.title;
        data.mTicketBeans = sourceRes.mTicketBeans;
        if(sourceRes.mTicketBeans == null){
            return null;
        }
        data.mTicketBeans = new ArrayList<QiwuTrainTicketPayViewData.TicketPayBean>(len);
        for (int i = sIdx; i < sIdx + len; i++) {
            if (i >= 0 && i < sourceRes.mTicketBeans.size()) {
                data.mTicketBeans.add(sourceRes.mTicketBeans.get(i));
            }
        }
        return data;
    }


    @Override
    protected int getCurrResSize(QiwuTrainTicketPayViewData currRes) {
        if (currRes == null || currRes.mTicketBeans == null) {
            return 0;
        }
        return currRes.mTicketBeans.size();
    }

    @Override
    public QiwuTrainTicketPayViewData.TicketPayBean getItemFromCurrPage(int idx) {
        QiwuTrainTicketPayViewData currRes = getResource();
        int size = getCurrResSize(currRes);
        if (idx >= 0 && idx < size) {
            return currRes.mTicketBeans.get(idx);
        }
        return null;
    }

    @Override
    public QiwuTrainTicketPayViewData.TicketPayBean getItemFromSource(int idx) {
        QiwuTrainTicketPayViewData currRes = mSourceRes;
        int size = getCurrResSize(currRes);
        if (idx >= 0 && idx < size) {
            return currRes.mTicketBeans.get(idx);
        }
        return null;
    }
}
